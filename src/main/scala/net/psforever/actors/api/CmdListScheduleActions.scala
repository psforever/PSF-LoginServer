package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, ActorRef}
import net.psforever.services.{InterstellarClusterService, ServiceManager}

import scala.collection.mutable.Map

/**
  * The actions a schedule can perform, described well enough for the portal to build a form.
  *
  * The zone list is read live so the continent picker offers what the server actually holds; a
  * static table here would be a second thing to remember when a zone is added.
  */
class CmdListScheduleActions(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  override def preStart(): Unit = {
    ServiceManager.receptionist ! Receptionist.Find(
      InterstellarClusterService.InterstellarClusterServiceKey,
      context.self
    )
  }

  override def receive: Receive = {
    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      listings.head ! InterstellarClusterService.FilterZones(_ => true, context.self)

    case InterstellarClusterService.ZonesResponse(zones) =>
      val data = Map[String, Any]()
      data("actions") = ScheduledActions.catalogue(zones.toSeq)
      context.parent ! CommandGoodResponse("", data)
      context.stop(self)

    case default =>
      log.error(s"Unexpected message $default")
  }
}
