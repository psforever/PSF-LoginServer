package net.psforever.objects.definition.converter

import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.structures.Building
import net.psforever.packet.game.objectcreate.{CaptureFlagData, PlacementData}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}

import java.util.concurrent.TimeUnit
import scala.util.{Success, Try}

class CaptureFlagConverter extends ObjectCreateConverter[CaptureFlag]() {
  override def ConstructorData(obj : CaptureFlag) : Try[CaptureFlagData] = {
    val hackInfo = obj.Owner.asInstanceOf[Building].CaptureTerminal.get.HackedBy match {
      case Some(hackInfo) => hackInfo
      case _ => Hackable.HackInfo("", PlanetSideGUID(0), PlanetSideEmpire.NEUTRAL, Vector3.Zero, 0L, 0L)
    }

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
