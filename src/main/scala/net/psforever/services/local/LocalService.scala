// Copyright (c) 2017 PSForever
package net.psforever.services.local

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.zones.Zone
import net.psforever.services.local.support.CaptureFlagManager
import net.psforever.services.local.support._
import net.psforever.services.base.{EventServiceSupport, EventSystemStamp, GenericEventServiceWithSupport}

case object DoorCloserSupport
  extends EventServiceSupport {
  def label: String = "doorCloser"
  def constructor(context: ActorContext): ActorRef = {
    context.actorOf(Props[DoorCloseActor](), name = "DoorCloser")
  }
}

case object HackClearSupport
  extends EventServiceSupport {
  def label: String = "hackClearer"
  def constructor(context: ActorContext): ActorRef = {
    context.actorOf(Props[HackClearActor](), name = "HackClearer")
  }
}

case object HackCaptureSupport
  extends EventServiceSupport {
  def label: String = "hackCapturer"
  def constructor(context: ActorContext): ActorRef = {
    context.actorOf(Props[HackCaptureActor](), name = "HackCapturer")
  }
}

case class CaptureFlagSupport(zone: Zone)
  extends EventServiceSupport {
  def label: String = "captureFlagManager"
  def constructor(context: ActorContext): ActorRef = {
    context.actorOf(Props(classOf[CaptureFlagManager], zone), name = "CaptureFlagManager")
  }
}

case object LocalStamp extends EventSystemStamp

class LocalService(zone: Zone)
  extends GenericEventServiceWithSupport(
    stamp = LocalStamp,
    eventSupportServices = List(DoorCloserSupport, HackClearSupport, HackCaptureSupport, CaptureFlagSupport(zone))
  )
