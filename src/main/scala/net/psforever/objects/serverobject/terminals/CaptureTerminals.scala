// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

import scala.util.{Failure, Success}

object CaptureTerminals {
  private val log = org.log4s.getLogger("CaptureTerminals")

  /**
    * The process of hacking an object is completed.
    * Pass the message onto the hackable object and onto the local events system.
    * @param target the `Hackable` object that has been hacked
    * @param unk na;
    *            used by `HackMessage` as `unk5`
    * @see `HackMessage`
    */
  //TODO add params here depending on which params in HackMessage are important
  def FinishHackingCaptureConsole(target: CaptureTerminal, user: Player, unk: Long)(): Unit = {
    import akka.pattern.ask
    import scala.concurrent.duration._
    log.info(s"Hacked a $target")
    // Wait for the target actor to set the HackedBy property, otherwise LocalAction.HackTemporarily will not complete properly
    import scala.concurrent.ExecutionContext.Implicits.global
    val tplayer = user
    ask(target.Actor, CommonMessages.Hack(tplayer, target))(1 second).mapTo[Boolean].onComplete {
      case Success(_) =>
        val zone   = target.Zone
        val zoneId = zone.id
        val pguid  = tplayer.GUID
        zone.LocalEvents ! LocalServiceMessage(
          zoneId,
          LocalAction.TriggerSound(pguid, target.HackSound, tplayer.Position, 30, 0.49803925f)
        )
        zone.LocalEvents ! LocalServiceMessage(
          zoneId,
          LocalAction.HackCaptureTerminal(pguid, zone, target, unk, 8L, tplayer.Faction == target.Faction)
        )
      case Failure(_) => log.warn(s"Hack message failed on target guid: ${target.GUID}")
    }
  }
}
