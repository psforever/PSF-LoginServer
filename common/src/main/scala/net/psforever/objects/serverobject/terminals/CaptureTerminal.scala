package net.psforever.objects.serverobject.terminals


import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.structures.Amenity

class CaptureTerminal(private val idef : CaptureTerminalDefinition) extends Amenity with Hackable {
  def Definition : CaptureTerminalDefinition = idef
}

object CaptureTerminal {
  /**
    * Overloaded constructor.
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  def apply(tdef : CaptureTerminalDefinition) : CaptureTerminal = {
    new CaptureTerminal(tdef)
  }

  import akka.actor.ActorContext
  def Constructor(tdef: CaptureTerminalDefinition)(id : Int, context : ActorContext) : CaptureTerminal = {
    import akka.actor.Props
    val obj = CaptureTerminal(tdef)
    obj.Actor = context.actorOf(Props(classOf[CaptureTerminalControl], obj), s"${tdef.Name}_$id")
    obj
  }
}

