// Copyright (c) 2017 PSForever
package net.psforever.services

import akka.actor.{Actor, ActorIdentity, ActorRef, ActorSystem, Identify, Props}
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed
import akka.actor.typed.receptionist.Receptionist

import scala.collection.mutable

object ServiceManager {
  var serviceManager = ActorRef.noSender

  var receptionist: typed.ActorRef[Receptionist.Command] = null

  def boot(implicit system: ActorSystem) = {
    serviceManager = system.actorOf(Props[ServiceManager](), "service")
    receptionist = system.toTyped.receptionist
    serviceManager
  }

  case class Register(props: Props, name: String)

  case class Lookup(name: String)

  case class LookupFromTyped(name: String, replyTo: typed.ActorRef[LookupResult])

  case class LookupResult(request: String, endpoint: ActorRef)

}

class ServiceManager extends Actor {
  import ServiceManager._
  private[this] val log = org.log4s.getLogger

  var nextLookupId: Long                     = 0
  val lookups: mutable.LongMap[RequestEntry] = mutable.LongMap()

  override def preStart() = {
    log.info("Starting...")
  }

  def receive = {
    case Register(props, name) =>
      log.info(s"Registered $name service")
      context.actorOf(props, name)
    case Lookup(name) =>
      context.actorSelection(name) ! Identify(nextLookupId)
      lookups += nextLookupId -> RequestEntry(name, sender())
      nextLookupId += 1

    case LookupFromTyped(name, replyTo) =>
      context.actorSelection(name) ! Identify(nextLookupId)
      lookups += nextLookupId -> RequestEntry(name, replyTo.toClassic)
      nextLookupId += 1

    case ActorIdentity(id, Some(ref)) =>
      val idNumber = id.asInstanceOf[Long]
      lookups.get(idNumber) match {
        case Some(RequestEntry(name, sender)) =>
          sender ! LookupResult(name, ref)
          lookups.remove(idNumber)
        case _ =>
        //TODO something
      }

    case ActorIdentity(id, None) =>
      val idNumber = id.asInstanceOf[Long]
      lookups.get(idNumber) match {
        case Some(RequestEntry(name, _)) =>
          log.error(s"request #$idNumber for service `$name` came back empty; it may not exist")
          lookups.remove(idNumber)
        case _ =>
        //TODO something
      }

    case default =>
      log.error(s"invalid message received - $default")
  }

  protected case class RequestEntry(request: String, responder: ActorRef)
}
