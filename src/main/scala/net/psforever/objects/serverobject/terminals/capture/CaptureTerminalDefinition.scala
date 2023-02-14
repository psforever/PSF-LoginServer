package net.psforever.objects.serverobject.terminals.capture

import net.psforever.objects.serverobject.structures.AmenityDefinition
import scala.concurrent.duration.{Duration, FiniteDuration}

class CaptureTerminalDefinition(objectId: Int) extends AmenityDefinition(objectId) {
  private var hackTime: FiniteDuration = Duration.Zero

  def FacilityHackTime: FiniteDuration = hackTime

  def FacilityHackTime_=(time: FiniteDuration): FiniteDuration = {
    hackTime = time
    FacilityHackTime
  }
}
