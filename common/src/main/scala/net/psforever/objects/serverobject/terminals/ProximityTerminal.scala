// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

/**
  * A server object that is a "terminal" that can be accessed for amenities and services,
  * triggered when a certain distance from the unit itself (proximity-based).<br>
  * <br>
  * Unlike conventional terminals, this structure is not necessarily structure-owned.
  * For example, the cavern crystals are considered owner-neutral elements that are not attached to a `Building` object.
  * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class ProximityTerminal(tdef : TerminalDefinition with ProximityDefinition) extends Terminal(tdef) with ProximityUnit

object ProximityTerminal {
  /**
    * Overloaded constructor.
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  def apply(tdef : TerminalDefinition with ProximityDefinition) : ProximityTerminal = {
    new ProximityTerminal(tdef)
  }

  import akka.actor.ActorContext

  /**
    * Instantiate an configure a `Terminal` object
    * @param tdef    the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    * @param id      the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `Terminal` object
    */
  def Constructor(tdef : TerminalDefinition with ProximityDefinition)(id : Int, context : ActorContext) : Terminal = {
    import akka.actor.Props
    val obj = ProximityTerminal(tdef)
    obj.Actor = context.actorOf(Props(classOf[ProximityTerminalControl], obj), s"${tdef.Name}_$id")
    obj
  }
}
