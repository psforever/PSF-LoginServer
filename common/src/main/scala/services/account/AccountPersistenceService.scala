// Copyright (c) 2020 PSForever
package services.account

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import net.psforever.objects.DefaultCancellable
import net.psforever.objects.zones.Zone
import net.psforever.types.PlanetSideEmpire
import services.ServiceManager

class AccountPersistenceService extends Actor {
  val log = org.log4s.getLogger
  var accountIndex : Long = 0
  val accounts : mutable.Map[String, ActorRef] = mutable.Map[String, ActorRef]()

  var taskResolver : ActorRef = ActorRef.noSender

  override def preStart = {
    log.trace("Starting; awaiting system hooks ...")
  }

  def receive : Receive = Initial

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
        case "taskResolver" =>
          taskResolver = endpoint
      }
      if(taskResolver != Actor.noSender) {
        log.trace("Hooks obtained.  Continuing with standard operation.")
        context.become(Started)
      }
  }

  val Initial : Receive = Setup
    .orElse(Started)
    .orElse {
      case _ => ;
    }

  def CreateNewPlayerToken(name : String) : ActorRef = {
    val ref = context.actorOf(Props(classOf[PlayerToken], name), s"$name-$accountIndex")
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

class PlayerToken(name : String) extends Actor {
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
    //todo handle logout
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
      timer = context.system.scheduler.scheduleOnce(600000 milliseconds, self, PlayerToken.Logout(name))
    }
  }
}

object PlayerToken {
  final case class LoginInfo(name : String, zone : Zone, regionAffinity : PlanetSideEmpire.Value, zoneAffinity : PlanetSideEmpire.Value)
  private case class Logout(name : String)
  final case class LogoutInfo(name : String)
}
