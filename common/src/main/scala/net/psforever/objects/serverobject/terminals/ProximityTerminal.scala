// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.types.Vector3
import services.Service

/**
  * A server object that is a "terminal" that can be accessed for amenities and services,
  * triggered when a certain distance from the unit itself (proximity-based).<br>
  * <br>
  * Unlike conventional terminals, this structure is not necessarily structure-owned.
  * For example, the cavern crystals are considered owner-neutral elements that are not attached to a `Building` object.
  * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class ProximityTerminal(tdef : ProximityTerminalDefinition) extends Terminal(tdef) with ProximityUnit {
  override def Request(player : Player, msg : Any) : Terminal.Exchange = {
    msg match {
      case message : CommonMessages.Use =>
        Actor ! message
      case _ =>
    }
    Terminal.NoDeal()
  }
}

object ProximityTerminal {
  /**
    * Overloaded constructor.
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  def apply(tdef : ProximityTerminalDefinition) : ProximityTerminal = {
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
  def Constructor(tdef : ProximityTerminalDefinition)(id : Int, context : ActorContext) : Terminal = {
    import akka.actor.Props
    val obj = ProximityTerminal(tdef)
    obj.Actor = context.actorOf(Props(classOf[ProximityTerminalControl], obj), s"${tdef.Name}_$id")
    obj
  }

  /**
    * Instantiate an configure a `Terminal` object, with position coordinates.
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    * @param pos the location of the object
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `Terminal` object
    */
  def Constructor(pos : Vector3, tdef : ProximityTerminalDefinition)(id : Int, context : ActorContext) : Terminal = {
    import akka.actor.Props
    val obj = ProximityTerminal(tdef)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[ProximityTerminalControl], obj), s"${tdef.Name}_$id")
    obj
  }

  /**
    * Assemble some logic for a provided owned object after both it ands its owner have been constructed.
    * @param obj an `Amenity` object;
    *            anticipating a `Terminal` object using this same definition
    * @param context hook to the local `Actor` system
    */
  def Setup(obj : Amenity, context : ActorContext) : Unit = {
    import akka.actor.{ActorRef, Props}
    if(obj.Actor == ActorRef.noSender) {
      obj.Actor = context.actorOf(Props(classOf[ProximityTerminalControl], obj), PlanetSideServerObject.UniqueActorName(obj))
      obj.Actor ! Service.Startup()
    }
  }
}
