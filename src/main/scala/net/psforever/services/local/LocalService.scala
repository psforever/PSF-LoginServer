// Copyright (c) 2017 PSForever
package net.psforever.services.local

import akka.actor.Props
import net.psforever.objects.zones.Zone
import net.psforever.services.local.support._
import net.psforever.services.base.{EventSystemStamp, GenericEventServiceWithSupport}

case object LocalStamp extends EventSystemStamp

object LocalService {
  def apply(zone: Zone): Props = {
    Props(
      classOf[GenericEventServiceWithSupport],
      LocalStamp,
      List(DoorCloserSupport, HackClearSupport, HackCaptureSupport, CaptureFlagSupport(zone))
    )
  }
}
