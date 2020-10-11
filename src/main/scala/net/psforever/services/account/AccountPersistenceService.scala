// Copyright (c) 2020 PSForever
package net.psforever.services.account

import akka.actor.{Actor, ActorRef, Cancellable, Props}

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import net.psforever.objects.guid.GUIDTask
import net.psforever.objects._
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.zones.Zone
import net.psforever.types.Vector3
import net.psforever.services.{Service, ServiceManager}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

/**
  * A global service that manages user behavior as divided into the following three categories:
  * persistence (ongoing participation in the game world),
  * relogging (short-term client connectivity issue resolution), and
  * logout (end-of-life conditions involving the separation of a user from the game world).<br>
  * <br>
  * A user polls this service and the net.psforever.services either creates a new `PersistenceMonitor` entity
  * or returns whatever `PersistenceMonitor` entity currently exists.
  * Performing informative pdates to the monitor about the user's eventual player avatar instance
  * (which can be performed by messaging the service indirectly,
  * though sending directly to the monitor is recommended)
  * facilitate the management of persistence.
  * If connectivity isssues with the client are encountered by the user,
  * within a reasonable amount of time to connection restoration,
  * the user may regain control of their existing persistence monitor and, thus, the same player avatar.
  * End of life is mainly managed by the monitors internally
  * and the monitors only communicate up to this service when executing their "end-of-life" operations.
  */
class AccountPersistenceService extends Actor {

  /** an association of user text descriptors - player names - and their current monitor indices<br>
    * key - player name, value - monitor index
    */
  var userIndices: mutable.Map[String, Int] = mutable.Map[String, Int]()

  /**
    * an association of user test descriptors - player names - and their current monitor<br>
    * key - player name, value - player monitor
    */
  val accounts: mutable.Map[String, ActorRef] = mutable.Map[String, ActorRef]()

  /** squad service event hook */
  var squad: ActorRef = ActorRef.noSender

  /** log, for trace and warnings only */
  val log = org.log4s.getLogger

  /**
    * Retrieve the required system event service hooks.
    *
    * @see `ServiceManager.LookupResult`
    */
  override def preStart(): Unit = {
    ServiceManager.serviceManager ! ServiceManager.Lookup("squad")
    log.trace("Awaiting system service hooks ...")
  }

  override def postStop(): Unit = {
    accounts.foreach { case (_, monitor) => context.stop(monitor) }
    accounts.clear()
  }

  def receive: Receive = Setup

  /**
    * Entry point for persistence monitoring setup.
    * Primarily intended to deal with the initial condition of verifying/assuring of an enqueued persistence monitor.
    * Updates to persistence can be received and will be distributed, if possible;
    * but, updating should be reserved for individual persistence monitor callback (by the user who is being monitored).
    */
  val Started: Receive = {
    case msg @ AccountPersistenceService.Login(name) =>
      (accounts.get(name) match {
        case Some(ref) => ref
        case None      => CreateNewPlayerToken(name)
      }).tell(msg, sender())

    case msg @ AccountPersistenceService.Update(name, _, _) =>
      accounts.get(name) match {
        case Some(ref) =>
          ref ! msg
        case None =>
          log.warn(s"tried to update a player entry for $name that did not yet exist; rebuilding entry ...")
          CreateNewPlayerToken(name).tell(msg, sender())
      }

    case msg @ AccountPersistenceService.PersistDelay(name, _) =>
      accounts.get(name) match {
        case Some(ref) =>
          ref ! msg
        case _ =>
          log.warn(s"player entry for $name not found; not logged in")
      }

    case msg @ AccountPersistenceService.Kick(name, _) =>
      accounts.get(name) match {
        case Some(ref) =>
          ref ! msg
        case _ =>
          log.warn(s"player entry for $name not found; not logged in")
      }

    case AccountPersistenceService.Logout(name) =>
      accounts.remove(name) match {
        case Some(ref) =>
          ref ! Logout(name)
        case _ =>
          log.warn(s"player entry for $name not found; not logged in")
      }

    case Logout(target) => //TODO use context.watch and Terminated?
      accounts.remove(target)

    case _ => ;
  }

  /**
    * Process the system event service hooks when they arrive, before starting proper persistence monitoring.
    * @see `ServiceManager.LookupResult`
    */
  val Setup: Receive = {
    case ServiceManager.LookupResult("squad", endpoint) =>
      squad = endpoint
      if (squad != ActorRef.noSender) {
        log.trace("Service hooks obtained.  Continuing with standard operation.")
        context.become(Started)
      }

    case msg =>
      log.warn(s"not yet started; received a $msg that will go unhandled")
  }

  /**
    * Enqueue a new persistency monitor object for this player.
    * @param name the unique name of the player
    * @return the persistence monitor object
    */
  def CreateNewPlayerToken(name: String): ActorRef = {
    val ref =
      context.actorOf(Props(classOf[PersistenceMonitor], name, squad), s"$name-${NextPlayerIndex(name)}")
    accounts += name -> ref
    ref
  }

  /**
    * Get the next account unique login index.
    * The index suggests the number of times the player has logged into the game.
    * The main purpose is to give each player a meaninfgul ordinal number of logging agencies
    * whose names did not interfere with each other (`Actor` name uniqueness).
    * @param name the text personal descriptor used by the player
    * @return the next index for this player, starting at 0
    */
  def NextPlayerIndex(name: String): Int = {
    userIndices.get(name) match {
      case Some(n) =>
        val p = n + 1
        userIndices += name -> p
        p
      case None =>
        userIndices += name -> 0
        0
    }
  }
}

object AccountPersistenceService {

  /**
    * Message to begin persistence monitoring of user with this text descriptor (player name).
    * If the persistence monitor already exists, use that instead and synchronize the data.
    * @param name the unique name of the player
    */
  final case class Login(name: String)

  /**
    * Update the persistence monitor that was setup for a user with the given text descriptor (player name).
    * The player's name should be able to satisfy the condition:<br>
    * `zone.LivePlayers.exists(p => p.Name.equals(name))`<br>
    * @param name the unique name of the player
    * @param zone the current zone the player is in
    * @param position the location of the player in game world coordinates
    */
  final case class Update(name: String, zone: Zone, position: Vector3)

  final case class Kick(name: String, time: Option[Long] = None)

  /**
    * Update the persistence monitor that was setup for a user for a custom persistence delay.
    * If set to `None`, the default persistence time should assert itself.
    * @param name the unique name of the player
    * @param time the duration that this user's player characters will persist without update in seconds
    */
  final case class PersistDelay(name: String, time: Option[Long])

  /**
    * Message that indicates that persistence is no longer necessary for this player character.
    * @param name the unique name of the player
    */
  final case class Logout(name: String)
}

/**
  * Observe and manage the persistence of a single named player avatar entity in the game world,
  * with special care to the conditions of short interruption in connectivity (relogging)
  * and end-of-life operations.
  * Upon login, the monitor will echo all of the current information about the user's (recent) login back to the `sender`.
  * With a zone and a coordinate position in that zone, a user's player avatar can be properly reconnected
  * or can be reconstructed.
  * Without actual recent activity,
  * the default information about the zone is an indication that the user must start this player avatar from scratch.
  * The monitor expects a reliable update messaging (heartbeat) to keep track of the important information
  * and to determine the conditions for end-of-life activity.
  * @param name the unique name of the player
  * @param squadService a hook into the `SquadService` event system
  */
class PersistenceMonitor(name: String, squadService: ActorRef) extends Actor {

  /** the last-reported zone of this player */
  var inZone: Zone = Zone.Nowhere

  /** the last-reported game coordinate position of this player */
  var lastPosition: Vector3 = Vector3.Zero

  /**
    */
  var kicked: Boolean = false

  /**
    */
  var kickTime: Option[Long] = None

  /** a custom logout time for this player; 60s by default */
  var persistTime: Option[Long] = None

  /** the ongoing amount of permissible inactivity */
  var timer: Cancellable = Default.Cancellable

  /** the sparingly-used log */
  val log = org.log4s.getLogger

  /**
    * Perform logout operations before the persistence monitor finally stops.
    */
  override def postStop(): Unit = {
    timer.cancel()
    PerformLogout()
  }

  def receive: Receive = {
    case AccountPersistenceService.Login(_) =>
      sender() ! (if (kicked) {
                    PlayerToken.CanNotLogin(name, PlayerToken.DeniedLoginReason.Kicked)
                  } else {
                    UpdateTimer()
                    PlayerToken.LoginInfo(name, inZone, lastPosition)
                  })

    case AccountPersistenceService.Update(_, z, p) if !kicked =>
      inZone = z
      lastPosition = p
      UpdateTimer()

    case AccountPersistenceService.PersistDelay(_, delay) if !kicked =>
      persistTime = delay
      UpdateTimer()

    case AccountPersistenceService.Kick(_, time) =>
      persistTime = None
      kickTime match {
        case None if kicked =>
          UpdateTimer()
        case _ => ;
      }
      kicked = true
      kickTime = time.orElse(Some(300L))

    case Logout(_) =>
      kickTime match {
        case Some(time) =>
          PerformLogout()
          kickTime = None
          timer.cancel()
          timer = context.system.scheduler.scheduleOnce(time seconds, self, Logout(name))
        case None =>
          context.parent ! Logout(name)
          context.stop(self)
      }

    case _ => ;
  }

  /**
    * Restart the minimum activity timer.
    */
  def UpdateTimer(): Unit = {
    timer.cancel()
    timer = context.system.scheduler.scheduleOnce(persistTime.getOrElse(60L) seconds, self, Logout(name))
  }

  /**
    * When the sustenance updates of the persistence monitor come to an end,
    * and the persistence monitor itself is about to clean itself up,
    * the player and avatar combination that has been associated with it will also undergo independent end of life activity.
    * This is the true purpose of the persistence object - to perform a proper logout.<br>
    * <br>
    * The updates have been providing the zone
    * and the basic information about the user (player name) has been provided since the beginning
    * and it's a trivial matter to find where the avatar and player and asess their circumstances.
    * The four important vectors are:
    * the player avatar is in a vehicle,
    * the player avatar is standing,
    * only the avatar exists and the player released,
    * and neither the avatar nor the player exist.
    * It does not matter whether the player, if encountered, is alive or dead,
    * only if they have been rendered a corpse and did not respawn.
    * The fourth condition is not technically a failure condition,
    * and can arise during normal transitional gameplay,
    * but should be uncommon.
    */
  def PerformLogout(): Unit = {
    (inZone.Players.find(p => p.name == name), inZone.LivePlayers.find(p => p.Name == name)) match {
      case (Some(avatar), Some(player)) if player.VehicleSeated.nonEmpty =>
        //alive or dead in a vehicle
        //if the avatar is dead while in a vehicle, they haven't released yet
        //TODO perform any last minute saving now ...
        (inZone.GUID(player.VehicleSeated) match {
          case Some(obj: Mountable) =>
            (Some(obj), obj.Seat(obj.PassengerInSeat(player).getOrElse(-1)))
          case _ => (None, None) //bad data?
        }) match {
          case (Some(_), Some(seat)) =>
            seat.Occupant = None //unseat
          case _ => ;
        }
        PlayerAvatarLogout(avatar, player)

      case (Some(avatar), Some(player)) =>
        //alive or dead, as standard Infantry
        //TODO perform any last minute saving now ...
        PlayerAvatarLogout(avatar, player)

      case (Some(avatar), None) =>
        //player has released
        //our last body was turned into a corpse; just the avatar remains
        //TODO perform any last minute saving now ...
        inZone.GUID(avatar.vehicle) match {
          case Some(obj: Vehicle) if obj.OwnerName.contains(avatar.name) =>
            obj.Actor ! Vehicle.Ownership(None)
          case _ => ;
        }
        AvatarLogout(avatar)

      case _ =>
      //user stalled during initial session, or was caught in between zone transfer
    }
  }

  /**
    * A common set of actions to perform in the course of logging out a player avatar.
    * Of the four scenarios described - in transport, on foot, released, missing - two of them utilize these operations.
    * One of the other two uses a modified version of some of these activities to facilitate its log out.
    * As this persistence monitor is about to become invalid,
    * any messages sent in response to what we are sending are received by the monitor's parent.
    * @see `Avatar`
    * @see `AvatarAction.ObjectDelete`
    * @see `AvatarServiceMessage`
    * @see `GUIDTask.UnregisterPlayer`
    * @see `Player`
    * @see `Zone.AvatarEvents`
    * @see `Zone.Population.Release`
    * @param avatar the avatar
    * @param player the player
    */
  def PlayerAvatarLogout(avatar: Avatar, player: Player): Unit = {
    val pguid  = player.GUID
    val parent = context.parent
    player.Position = Vector3.Zero
    player.Health = 0
    inZone.GUID(player.avatar.vehicle) match {
      case Some(vehicle: Vehicle) if vehicle.OwnerName.contains(player.Name) && vehicle.Actor != Default.Actor =>
        vehicle.Actor ! Vehicle.Ownership(None)
      case _ => ;
    }
    inZone.Population.tell(Zone.Population.Release(avatar), parent)
    inZone.AvatarEvents.tell(AvatarServiceMessage(inZone.id, AvatarAction.ObjectDelete(pguid, pguid)), parent)
    inZone.tasks.tell(GUIDTask.UnregisterPlayer(player)(inZone.GUID), parent)
    AvatarLogout(avatar)
  }

  /**
    * A common set of actions to perform in the course of logging out an avatar.
    * Of the four scenarios described - in transport, on foot, released, missing - three of them utilize these operations.
    * The avatar will virtually always be in an existential position, one that needs to be handled at logout
    * @see `Avatar`
    * @see `Deployables.Disown`
    * @see `Service.Leave`
    * @see `Zone.Population.Leave`
    * @param avatar the avatar
    */
  def AvatarLogout(avatar: Avatar): Unit = {
    LivePlayerList.Remove(avatar.id)
    squadService.tell(Service.Leave(Some(avatar.id.toString)), context.parent)
    Deployables.Disown(inZone, avatar, context.parent)
    inZone.Population.tell(Zone.Population.Leave(avatar), context.parent)
    inZone.tasks.tell(GUIDTask.UnregisterObjectTask(avatar.locker)(inZone.GUID), context.parent)
    log.info(s"logout of ${avatar.name}")
  }
}

/**
  * Internal message that flags that the player has surpassed the maximum amount of inactivity allowable
  * and should stop existing.
  * @param name the unique name of the player
  */
private[this] case class Logout(name: String)

object PlayerToken {
  object DeniedLoginReason extends Enumeration {
    val Denied, //generic
    Kicked = Value
  }

  /**
    * Message dispatched to confirm that a player with given locational attributes exists.
    * Agencies outside of the `AccountPersistanceService`/`PlayerToken` system make use of this message.
    * ("Exists" does not imply an ongoing process and can also mean "just joined the game" here.)
    * @param name the name of the player
    * @param zone the zone in which the player is location
    * @param position where in the zone the player is located
    */
  final case class LoginInfo(name: String, zone: Zone, position: Vector3)

  final case class CanNotLogin(name: String, reason: DeniedLoginReason.Value)
}
