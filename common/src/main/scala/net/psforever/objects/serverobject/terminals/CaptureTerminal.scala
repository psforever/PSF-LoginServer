package net.psforever.objects.serverobject.terminals

import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.TriggeredSound
import net.psforever.types.Vector3

class CaptureTerminal(private val idef: CaptureTerminalDefinition) extends Amenity with Hackable {
  def Definition: CaptureTerminalDefinition = idef
  HackDuration = Array(60, 40, 20, 15)
  HackSound = TriggeredSound.HackTerminal
}

object CaptureTerminal {

  /**
    * Overloaded constructor.
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  def apply(tdef: CaptureTerminalDefinition): CaptureTerminal = {
    new CaptureTerminal(tdef)
  }

  import akka.actor.ActorContext
  def Constructor(pos: Vector3, tdef: CaptureTerminalDefinition)(id: Int, context: ActorContext): CaptureTerminal = {
    import akka.actor.Props
    val obj = CaptureTerminal(tdef)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[CaptureTerminalControl], obj), s"${tdef.Name}_$id")
    obj
  }
}
