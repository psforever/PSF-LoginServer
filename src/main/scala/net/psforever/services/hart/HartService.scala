// Copyright (c) 2021 PSForever
package net.psforever.services.hart

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.util.Config

import scala.collection.concurrent.TrieMap

/**
  * Coordinate the components - facility landing pad and orbital shuttle -
  * of the high altitude rapid transport (HART) system for any zone that attempts to register.
  * When a pair of staging pad and orbital shuttle attempt to register with the system,
  * either locate an existing zone-based manager or create a new manager for this zone,
  * and tell that manager that the pair is (now) under its supervision.
  * @see `HartTimer`
  */
class HartService extends Actor {
  /** key - a zone id; value - the manager for that zone's HART system */
  val zoneTimers: TrieMap[String, ActorRef] = TrieMap[String, ActorRef]()

  def receive: Receive = {
    case out : HartTimer.PairWith =>
      val zone = out.zone
      val channel = zone.id
      (zoneTimers.get(channel) match {
        case Some(o) =>
          o
        case None =>
          val actor = context.actorOf(Props(classOf[HartTimer], zone), s"$channel-shuttle-timer")
          zoneTimers.put(channel, actor)
          actor ! HartTimer.SetEventDurations(
            channel,
            Config.app.game.hart.inFlightDuration,
            Config.app.game.hart.boardingDuration
          )
          actor
      }).tell(out, out.from)

    case out: HartTimer.MessageToHartInZone =>
      zoneTimers.get(out.inZone) match {
        case Some(o) => o ! out
        case _ =>
      }

    case _ => ;
  }
}
