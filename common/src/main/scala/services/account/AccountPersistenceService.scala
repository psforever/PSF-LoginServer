// Copyright (c) 2020 PSForever
package services.account

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import net.psforever.objects.guid.GUIDTask
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.serverobject.mount.Mountable

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import net.psforever.objects._
import net.psforever.objects.zones.Zone
import net.psforever.types.{PlanetSideEmpire, Vector3}
import services.{RemoverActor, Service, ServiceManager}
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.vehicle.VehicleServiceMessage

class AccountPersistenceService extends Actor {
  val log = org.log4s.getLogger
  var accountIndex : Long = 0
  val accounts : mutable.Map[String, ActorRef] = mutable.Map[String, ActorRef]()

  var squad : ActorRef = ActorRef.noSender
  var resolver : ActorRef = ActorRef.noSender

  override def preStart = {
    ServiceManager.serviceManager ! ServiceManager.Lookup("squad")
    ServiceManager.serviceManager ! ServiceManager.Lookup("taskResolver")
    log.trace("Starting; awaiting system hooks ...")
  }

  def receive : Receive = Setup

  val Started : Receive = {
    case msg @ AccountPersistenceService.Login(name) =>
      (accounts.get(name) match {
        case Some(ref) => ref
        case None => CreateNewPlayerToken(name)
      }).tell(msg, sender)

    case msg @ AccountPersistenceService.Update(name, _, _, _) =>
      accounts.get(name) match {
        case Some(ref) =>
          ref ! msg
        case None =>
          log.warn(s"tried to update a player entry ($name) that did not yet exist; capable of recovery")
          CreateNewPlayerToken(name).tell(AccountPersistenceService.Login(name), sender)
      }

    case PlayerToken.LogoutInfo(target) =>
      accounts.remove(target)

    case _ => ;
  }

  val Setup : Receive = {
    case ServiceManager.LookupResult(id, endpoint) =>
      id match {
        case "squad" =>
          squad = endpoint
        case "taskResolver" =>
          resolver = endpoint
      }
      if(squad != ActorRef.noSender &&
        resolver != ActorRef.noSender) {
        log.trace("Hooks obtained.  Continuing with standard operation.")
        context.become(Started)
      }
    case _ => ;
  }

  def CreateNewPlayerToken(name : String) : ActorRef = {
    val ref = context.actorOf(Props(classOf[PlayerToken], name, squad, resolver), s"$name-$accountIndex")
    accountIndex += 1
    accounts += name -> ref
    ref
  }
}

object AccountPersistenceService {
  final case class Login(name : String)

  /**
    * na
    * Corpses become ineligible.
    * The player's name should be able to satisfy the condition:<br>
    * `zone.LivePlayers.exists(p => p.Name.equals(name))`<br>
    * If the player is in a facility SOI, record that facility's faction affinity.
    * If the player is in the wilderness, always list as `NEUTRAL`.
    * The zone's affinity is `NEUTRAL` unless it is empire locked.
    * @param name the unique name of the player
    * @param zone the current zone the player is in;
    * @param regionAffinity the last reported faction affinity of the surrounding region
    * @param zoneAffinity the last reported faction affinity of the zone
    */
  final case class Update(name : String, zone : Zone, regionAffinity : PlanetSideEmpire.Value, zoneAffinity : PlanetSideEmpire.Value)
}

class PlayerToken(name : String, squadService : ActorRef, taskResolver : ActorRef) extends Actor {
  val log = org.log4s.getLogger(s"Logout-$name")
  var inZone : Zone = Zone.Nowhere
  var regionAffinity : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  var zoneAffinity : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  var timer : Cancellable = DefaultCancellable.obj
  var timeup : Boolean = false
  UpdateTimer()

  override def postStop() : Unit = {
    super.postStop
    timer.cancel
    context.parent ! PlayerToken.LogoutInfo(name)
    performLogout()
  }

  def receive : Receive = {
    case PlayerToken.Logout(_) =>
      timeup = timer.cancel
      context.stop(self)

    case AccountPersistenceService.Login(_) =>
      sender ! PlayerToken.LoginInfo(name, inZone, regionAffinity, zoneAffinity)

    case AccountPersistenceService.Update(_, z, rf, zf) =>
      inZone = z
      regionAffinity = rf
      zoneAffinity = zf
      UpdateTimer()

    case _ => ;
  }

  def UpdateTimer() : Unit = {
    if(!timeup) {
      timer.cancel
      timer = context.system.scheduler.scheduleOnce(60000 milliseconds, self, PlayerToken.Logout(name))
    }
  }

  def performLogout() : Unit = {
    log.info(s"attempting logout of $name")
    val parent = context.parent
    (inZone.Players.find(p => p.name == name), inZone.LivePlayers.find(p => p.Name == name)) match {
      case (Some(avatar), Some(player)) if player.VehicleSeated.nonEmpty =>
        log.error("unhandled vehicle condition")
        //...

      case (Some(avatar), Some(player)) =>
        log.info(s"HANDLED infantry condition")
        val pguid = player.GUID
        player.Position = Vector3.Zero
        player.Health = 0
        squadService.tell(Service.Leave(Some(player.CharId.toString)), parent)
        Deployables.Disown(inZone, avatar, parent)
        inZone.Population.tell(Zone.Population.Release(avatar), parent)
        inZone.AvatarEvents.tell(AvatarServiceMessage(inZone.Id, AvatarAction.ObjectDelete(pguid, pguid)), parent)
        DisownVehicle(player)
        taskResolver.tell(GUIDTask.UnregisterAvatar(player)(inZone.GUID), parent)

      case (Some(avatar), None) =>
        log.error("unhandled dead condition")
        //...

      case _ =>
        log.error("unhandled unforeseen condition")
    }
  //    if(player != null && player.HasGUID) {
  //      PlayerActionsToCancel()
  //      val player_guid = player.GUID
  //      //handle orphaned deployables
  //      Disown()
  //      //clean up boomer triggers and telepads
  //      val equipment = (
  //        player.Holsters()
  //          .zipWithIndex
  //          .collect({ case (slot, index) if slot.Equipment.nonEmpty => InventoryItem(slot.Equipment.get, index) }) ++
  //          player.Inventory.Items
  //        )
  //        .filterNot({ case InventoryItem(obj, _) => obj.isInstanceOf[BoomerTrigger] || obj.isInstanceOf[Telepad] })
  //      //put any temporary value back into the avatar
  //      //TODO final character save before doing any of this (use equipment)
  //      continent.Population ! Zone.Population.Release(avatar)
  //      if(player.isAlive) {
  //        //actually being alive or manually deconstructing
  //        player.Position = Vector3.Zero
  //        //if seated, dismount
  //        player.VehicleSeated match {
  //          case Some(_) =>
  //            //quickly and briefly kill player to avoid disembark animation?
  //            continent.AvatarEvents ! AvatarServiceMessage(continent.Id, AvatarAction.PlanetsideAttribute(player_guid, 0, 0))
  //            DismountVehicleOnLogOut()
  //          case _ => ;
  //        }
  //        continent.AvatarEvents ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid))
  //        taskResolver ! GUIDTask.UnregisterAvatar(player)(continent.GUID)
  //        //TODO normally, the actual player avatar persists a minute or so after the user disconnects
  //      }
  //      else if(continent.LivePlayers.contains(player) && !continent.Corpses.contains(player)) {
  //        //player disconnected while waiting for a revive, maybe
  //        //similar to handling ReleaseAvatarRequestMessage
  //        player.Release
  //        player.VehicleSeated match {
  //          case None =>
  //            FriskCorpse(player) //TODO eliminate dead letters
  //            if(!WellLootedCorpse(player)) {
  //              continent.Population ! Zone.Corpse.Add(player)
  //              continent.AvatarEvents ! AvatarServiceMessage(continent.Id, AvatarAction.Release(player, continent))
  //              taskResolver ! GUIDTask.UnregisterLocker(player.Locker)(continent.GUID) //rest of player will be cleaned up with corpses
  //            }
  //            else {
  //              //no items in inventory; leave no corpse
  //              val player_guid = player.GUID
  //              player.Position = Vector3.Zero //save character before doing this
  //              continent.AvatarEvents ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid))
  //              taskResolver ! GUIDTask.UnregisterAvatar(player)(continent.GUID)
  //            }
  //
  //          case Some(_) =>
  //            val player_guid = player.GUID
  //            player.Position = Vector3.Zero //save character before doing this
  //            continent.AvatarEvents ! AvatarServiceMessage(continent.Id, AvatarAction.ObjectDelete(player_guid, player_guid))
  //            taskResolver ! GUIDTask.UnregisterAvatar(player)(continent.GUID)
  //            DismountVehicleOnLogOut()
  //        }
  //      }
  //      //disassociate and start the deconstruction timer for any currently owned vehicle
  //      SpecialCaseDisownVehicle()
  //      continent.Population ! Zone.Population.Leave(avatar)
  }

  /**
    * Vehicle cleanup that is specific to log out behavior.
    */
  def DisownVehicle(player : Player) : Unit = {
    Vehicles.Disown(player, inZone) match {
      case out @ Some(vehicle) =>
        inZone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(vehicle), inZone))
        inZone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.AddTask(vehicle, inZone,
          if(vehicle.Flying) {
            //TODO gravity
            Some(0 seconds) //immediate deconstruction
          }
          else {
            vehicle.Definition.DeconstructionTime //normal deconstruction
          }
        ))
      case None => ;
    }
  }
}

object PlayerToken {
  final case class LoginInfo(name : String, zone : Zone, regionAffinity : PlanetSideEmpire.Value, zoneAffinity : PlanetSideEmpire.Value)
  private case class Logout(name : String)
  final case class LogoutInfo(name : String)
}
