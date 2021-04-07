package net.psforever.objects.serverobject.llu

import akka.actor.{ActorContext, Props}
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.types.Vector3


/**
  * Represents the LLU sockets found within bases that require LLU hacks.
  * It is used as a position reference for spawning the LLU in the correct location when the base is hacked
  * @param tDef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class CaptureFlagSocket(tDef: CaptureFlagSocketDefinition) extends Amenity {
  def Definition : CaptureFlagSocketDefinition = tDef
}

object CaptureFlagSocket {
  def apply(tDef: CaptureFlagSocketDefinition) : CaptureFlagSocket = {
    new CaptureFlagSocket(tDef)
  }

  def Constructor(pos: Vector3)(id: Int, context: ActorContext) : CaptureFlagSocket = {
    Constructor(GlobalDefinitions.llm_socket, pos)(id, context)
  }

  def Constructor(tdef: CaptureFlagSocketDefinition, pos: Vector3)(id: Int, context: ActorContext): CaptureFlagSocket = {
    val obj = CaptureFlagSocket(tdef)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[CaptureFlagSocketControl], obj), s"${obj.Definition.Name}_$id")
    obj
  }
}
