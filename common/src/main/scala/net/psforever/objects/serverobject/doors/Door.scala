// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.doors

import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.Player
import net.psforever.packet.game.UseItemMessage
import net.psforever.types.Vector3

/**
  * A structure-owned server object that is a "door" that can open and can close.
  * @param ddef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class Door(private val ddef : DoorDefinition) extends PlanetSideServerObject {
  private var openState : Option[Player] = None
  /** a vector in the direction of the "outside" of a room;
    * typically, any locking utility is on that same "outside" */
  private var outwards : Vector3 = Vector3.Zero

  /**
    * While setting the normal rotation angle for the door (?),
    * use the angular data to determine an "inside" side and an "outside" side.<br>
    * <br>
    * Doors are always positioned with the frame perpendicular to the ground.
    * The `i` and `j` components can be excused for this reason and only the `k` component (rotation around world-up) matters.
    * Due to angle-corrected North, add 90 degrees before switching to radians and negate the cosine.
    * @param orient the orientation of the door
    * @return the clamped orientation of the door
    */
  override def Orientation_=(orient : Vector3) : Vector3 = {
    val ret = super.Orientation_=(orient)
    //transform angular data into unit circle components
    val rang = math.toRadians(orient.z + 90)
    outwards = Vector3(-math.cos(rang).toFloat, math.sin(rang).toFloat, 0)
    ret
  }

  def Outwards : Vector3 = outwards

  def isOpen : Boolean = openState.isDefined

  def Open : Option[Player] = openState

  def Open_=(player : Player) : Option[Player] = {
    Open_=(Some(player))
  }

  def Open_=(open : Option[Player]) : Option[Player] = {
    openState = open
    Open
  }

  def Use(player : Player, msg : UseItemMessage) : Door.Exchange = {
    if(openState.isEmpty) {
      openState = Some(player)
      Door.OpenEvent()
    }
    else {
      openState = None
      Door.CloseEvent()
    }
  }

  def Definition : DoorDefinition = ddef
}

object Door {
  /**
    * Entry message into this `Door` that carries the request.
    * @param player the player who sent this request message
    * @param msg the original packet carrying the request
    */
  final case class Use(player : Player, msg : UseItemMessage)

  /**
    * A basic `Trait` connecting all of the actionable `Door` response messages.
    */
  sealed trait Exchange

  /**
    * Message that carries the result of the processed request message back to the original user (`player`).
    * @param player the player who sent this request message
    * @param msg the original packet carrying the request
    * @param response the result of the processed request
    */
  final case class DoorMessage(player : Player, msg : UseItemMessage, response : Exchange)

  /**
    * This door will open.
    */
  final case class OpenEvent() extends Exchange

  /**
    * This door will close.
    */
  final case class CloseEvent() extends Exchange

  /**
    * This door will do nothing.
    */
  final case class NoEvent() extends Exchange

  /**
    * Overloaded constructor.
    * @param tdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  def apply(tdef : DoorDefinition) : Door = {
    new Door(tdef)
  }

  import akka.actor.ActorContext
  /**
    * Instantiate and configure a `Door` object.
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `Door` object
    */
  def Constructor(id : Int, context : ActorContext) : Door = {
    import akka.actor.Props
    import net.psforever.objects.GlobalDefinitions

    val obj = Door(GlobalDefinitions.door)
    obj.Actor = context.actorOf(Props(classOf[DoorControl], obj), s"${GlobalDefinitions.door.Name}_$id")
    obj
  }

  import net.psforever.types.Vector3
  /**
    * Instantiate and configure a `Door` object that has knowledge of both its position and outwards-facing direction.
    * The assumption is that this door will be paired with an IFF Lock, thus, has conditions for opening.
    * @param pos the position of the door
    * @param outwards_direction a vector in the direction of the door's outside
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `Door` object
    */
  def Constructor(pos : Vector3, outwards_direction : Vector3)(id : Int, context : ActorContext) : Door = {
    import akka.actor.Props
    import net.psforever.objects.GlobalDefinitions

    val obj = Door(GlobalDefinitions.door)
    obj.Position = pos
    obj.Orientation = outwards_direction
    obj.Actor = context.actorOf(Props(classOf[DoorControl], obj), s"${GlobalDefinitions.door.Name}_$id")
    obj
  }
}
