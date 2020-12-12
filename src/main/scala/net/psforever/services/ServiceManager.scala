// Copyright (c) 2017 PSForever
package net.psforever.services

import akka.actor.{Actor, ActorIdentity, ActorRef, ActorSystem, Identify, InvalidActorNameException, Props, typed}
import akka.actor.typed.scaladsl.adapter._
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
  val retainedRequests: mutable.HashMap[String, Set[ActorRef]] = mutable.HashMap()

  override def preStart() = {
    log.info("Starting...")
  }

  def receive = {
    case Register(props, name) =>
      log.info(s"Registered $name service")
      try {
        val ref = context.actorOf(props, name)
        val result = LookupResult(name, ref)
        //handle logged premature requests
        retainedRequests.remove(name) match {
          case Some(oldRequests) =>
            oldRequests.foreach {
              _ ! result
            }
          case None => ;
        }
        //handle active requests that will probably miss
        val poorlytTimedRequests = lookups.filter {
          _._2.request.equals(name)
        }
        poorlytTimedRequests.foreach { case (id, entry) =>
          entry.responder ! result
          lookups.remove(id)
        }
      }
      catch {
        case e: InvalidActorNameException => //if an entry already exists, no harm, no foul, just don't do it again
          log.warn(s"service manager says: service already exists - ${e.getMessage}")
        case e: Exception =>
          log.error(s"service manager says: service could not start - ${e.getMessage}")
      }

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
        case _ => ;
      }

    case ActorIdentity(id, None) =>
      val idNumber = id.asInstanceOf[Long]
      lookups.get(idNumber) match {
        case Some(RequestEntry(name, sender)) =>
          log.error(s"service manager says: request #$idNumber for service `$name` came back empty; it may not exist")
          lookups.remove(idNumber)
          retainedRequests(name) = retainedRequests.getOrElse(name, Set[ActorRef]()) ++ Set(sender)
        case _ => ;
      }

    case default =>
      log.error(s"service manager says: invalid message received - $default")
  }

  protected case class RequestEntry(request: String, responder: ActorRef)
}
