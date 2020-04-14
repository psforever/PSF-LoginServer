// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.hackable

import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.packet.game.{HackMessage, HackState}
import net.psforever.types.PlanetSideGUID
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}

import scala.util.{Failure, Success}

object GenericHackables {
  private val log = org.log4s.getLogger("HackableBehavior")

  /**
    * na
    * @param player the player doing the hacking
    * @param obj the object being hacked
    * @return the percentage amount of progress per tick
    */
  def GetHackSpeed(player : Player, obj: PlanetSideServerObject): Float = {
    val playerHackLevel = Player.GetHackLevel(player)
    val timeToHack = obj match {
      case vehicle : Vehicle => vehicle.JackingDuration(playerHackLevel)
      case hackable : Hackable => hackable.HackDuration(playerHackLevel)
      case _ =>
        log.warn(s"${player.Name} tried to hack an object that has no hack time defined - ${obj.Definition.Name}#${obj.GUID} on ${obj.Zone.Id}")
        0
    }
    if(timeToHack == 0) {
      log.warn(s"${player.Name} tried to hack an object that they don't have the correct hacking level for - ${obj.Definition.Name}#${obj.GUID} on ${obj.Zone.Id}")
      0f
    }
    else {
      //timeToHack is in seconds; progress is measured in quarters of a second (250ms)
      (100 / timeToHack) / 4
    }
  }

  /**
    * Evaluate the progress of the user applying a tool to modify some server object.
    * This action is using the remote electronics kit to convert an enemy unit into an allied unit, primarily.
    * The act of transforming allied units of one kind into allied units of another kind (facility turret upgrades)
    * is also governed by this action per tick of progress.
    * @see `HackMessage`
    * @see `HackState`
    * @param progressType 1 - remote electronics kit hack (various ...);
    *                     2 - nano dispenser (upgrade canister) turret upgrade
    * @param tplayer the player performing the action
    * @param target the object being affected
    * @param tool_guid the tool being used to affest the object
    * @param progress the current progress value
    * @return `true`, if the next cycle of progress should occur;
    *         `false`, otherwise
    */
  def HackingTickAction(progressType : Int, tplayer : Player, target : PlanetSideServerObject, tool_guid : PlanetSideGUID)(progress : Float) : Boolean = {
    //hack state for progress bar visibility
    val vis = if(progress <= 0L) {
      HackState.Start
    }
    else if(progress >= 100L) {
      HackState.Finished
    }
    else if(target.isMoving(1f)) {
      // If the object is moving (more than slightly to account for things like magriders rotating, or the last velocity reported being the magrider dipping down on dismount) then cancel the hack
      HackState.Cancelled
    }
    else {
      HackState.Ongoing
    }
    target.Zone.AvatarEvents ! AvatarServiceMessage(
      tplayer.Name,
      AvatarAction.SendResponse(Service.defaultPlayerGUID,
        if(!target.HasGUID) {
          //cancel the hack (target is gone)
          HackMessage(progressType, target.GUID, tplayer.GUID, 0, 0L, HackState.Cancelled, 8L)
        }
        else if(vis == HackState.Cancelled) {
          //cancel the hack (e.g. vehicle drove away)
          HackMessage(progressType, target.GUID, tplayer.GUID, 0, 0L, vis, 8L)
        }
        else {
          HackMessage(progressType, target.GUID, tplayer.GUID, progress.toInt, 0L, vis, 8L)
        }
      )
    )
    vis != HackState.Cancelled
  }

  /**
    * The process of hacking an object is completed.
    * Pass the message onto the hackable object and onto the local events system.
    * @param target the `Hackable` object that has been hacked
    * @param user the player that is performing this hacking task
    * @param unk na;
    *            used by `HackMessage` as `unk5`
    * @see `HackMessage`
    */
  //TODO add params here depending on which params in HackMessage are important
  def FinishHacking(target : PlanetSideServerObject with Hackable, user : Player, unk : Long)() : Unit = {
    import akka.pattern.ask
    import scala.concurrent.duration._
    log.info(s"Hacked a $target")
    // Wait for the target actor to set the HackedBy property, otherwise LocalAction.HackTemporarily will not complete properly
    import scala.concurrent.ExecutionContext.Implicits.global
    val tplayer = user
    ask(target.Actor, CommonMessages.Hack(tplayer, target))(1 second).mapTo[Boolean].onComplete {
      case Success(_) =>
        val zone = target.Zone
        val zoneId = zone.Id
        val pguid = tplayer.GUID
        zone.LocalEvents ! LocalServiceMessage(zoneId, LocalAction.TriggerSound(pguid, target.HackSound, tplayer.Position, 30, 0.49803925f))
        zone.LocalEvents ! LocalServiceMessage(zoneId, LocalAction.HackTemporarily(pguid, zone, target, unk, target.HackEffectDuration(Player.GetHackLevel(user))))
      case Failure(_) => log.warn(s"Hack message failed on target guid: ${target.GUID}")
    }
  }
}

