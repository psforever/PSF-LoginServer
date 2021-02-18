// Copyright (c) 2021 PSForever
package net.psforever.services.time

import akka.actor.{Actor, ActorRef, Props}

import scala.collection.concurrent.TrieMap

class ShuttleTimerService extends Actor {
  val channels: TrieMap[String, ActorRef] = TrieMap[String, ActorRef]()

  def receive: Receive = {
    case out : ShuttleTimer.PairWith =>
      val zone = out.zone
      val channel = zone.id
      (channels.get(channel) match {
        case Some(o) =>
          o
        case None =>
          val actor = context.actorOf(Props(classOf[ShuttleTimer], zone), s"$channel-shuttle-timer")
          channels.put(channel, actor)
          actor
      }).tell(out, sender())

    case out @ ShuttleTimer.Update(inZone, _) =>
      channels.get(inZone) match {
        case Some(o) => o ! out
        case _ =>
      }

    case _ => ;
  }
}
