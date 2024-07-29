package net.psforever.objects.serverobject.terminals.capture

import akka.actor.Actor.Receive
import net.psforever.objects.serverobject.structures.Amenity

import scala.annotation.unused

/**
  * The behaviours corresponding to an Amenity that is marked as being CaptureTerminalAware
  * @see CaptureTerminalAware
  */
trait CaptureTerminalAwareBehavior {
  def CaptureTerminalAwareObject: Amenity with CaptureTerminalAware

  val captureTerminalAwareBehaviour: Receive = {
    case CaptureTerminalAwareBehavior.TerminalStatusChanged(terminal, true) =>
      captureTerminalIsResecured(terminal)

    case CaptureTerminalAwareBehavior.TerminalStatusChanged(terminal, _) =>
      captureTerminalIsHacked(terminal)
  }

  protected def captureTerminalIsResecured(@unused terminal: CaptureTerminal): Unit = { /* intentionally blank */ }

  protected def captureTerminalIsHacked(@unused terminal: CaptureTerminal): Unit = { /* intentionally blank */ }
}

object CaptureTerminalAwareBehavior {
  final case class TerminalStatusChanged(terminal: CaptureTerminal, isResecured: Boolean)
}
