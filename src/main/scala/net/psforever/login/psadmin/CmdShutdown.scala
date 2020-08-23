package net.psforever.login.psadmin

import akka.actor.{Actor, ActorRef}

import scala.collection.mutable.Map

class CmdShutdown(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  override def preStart() = {
    var data = Map[String, Any]()
    context.parent ! CommandGoodResponse("Shutting down", data)
    context.system.terminate()
  }

  override def receive = {
    case default =>
  }
}
