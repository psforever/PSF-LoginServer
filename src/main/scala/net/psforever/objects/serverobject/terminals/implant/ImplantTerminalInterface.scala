// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.terminals.implant

import akka.actor.ActorContext
import net.psforever.objects.serverobject.terminals.{Terminal, TerminalDefinition}
import net.psforever.types.Vector3

object ImplantTerminalInterface {
  /**
   * Instantiate and configure a `Terminal` object
   * @param pdef `ObjectDefinition` that constructs this object and maintains some of its immutable fields
   * @param pos position
   * @param id the unique id that will be assigned to this entity
   * @param context a context to allow the object to properly set up `ActorSystem` functionality
   * @return the `Terminal` object
   */
  def Constructor(pos: Vector3, pdef: TerminalDefinition)(id: Int, context: ActorContext): Terminal = {
    import akka.actor.Props

    val obj = Terminal(pdef)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[ImplantInterfaceControl], obj), s"${obj.Definition.Name}_$id")
    obj
  }
}
