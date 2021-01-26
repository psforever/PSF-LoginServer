package net.psforever.objects.definition.converter

import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.structures.Building
import net.psforever.packet.game.objectcreate.{CaptureFlagData, PlacementData}

import java.util.concurrent.TimeUnit
import scala.util.{Success, Try}

class CaptureFlagConverter extends ObjectCreateConverter[CaptureFlag]() {
  override def ConstructorData(obj : CaptureFlag) : Try[CaptureFlagData] = {
    val hackInfo = obj.Owner.asInstanceOf[Building].CaptureTerminal.get.HackedBy.get
    val millisecondsRemaining = TimeUnit.MILLISECONDS.convert(math.max(0, hackInfo.hackStartTime + hackInfo.hackDuration - System.nanoTime), TimeUnit.NANOSECONDS)

    Success(
      CaptureFlagData(
        new PlacementData(obj.Position, obj.Orientation, obj.Velocity),
        obj.Faction,
        obj.Owner.asInstanceOf[Building].GUID.guid,
        obj.Target.GUID.guid,
        millisecondsRemaining
      )
    )
  }
}