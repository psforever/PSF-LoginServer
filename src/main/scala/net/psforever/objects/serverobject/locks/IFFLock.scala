// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.locks

import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.TriggeredSound
import net.psforever.types.Vector3

/**
  * A structure-owned server object that is a "door lock."<br>
  * <br>
  * The "door lock" exerts an "identify friend or foe" field that detects the faction affiliation of a target player.
  * It also indirectly inherits faction affiliation from the structure to which it is connected
  * or it can be "hacked" whereupon the person exploiting it leaves their "faction" as the aforementioned affiliated faction.
  * The `IFFLock` is ideally associated with a server map object - a `Door` - to which it acts as a gatekeeper.
  * @param idef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class IFFLock(private val idef: IFFLockDefinition) extends Amenity with Hackable {
  def Definition: IFFLockDefinition = idef
  HackSound = TriggeredSound.HackDoor
  HackEffectDuration = Array(60, 180, 300, 360)
  HackDuration = Array(5, 3, 1, 1)

  /** a vector in the direction of the "outside" of a room;
    * typically, any locking utility is on that same "outside"
    */
  private var outwards: Vector3 = Vector3.Zero

  /**
    * While setting the normal rotation angle for the IFF lock for a door (?),
    * use the angular data to determine an "inside" side and an "outside" side.<br>
    * <br>
    * Doors are always positioned with the frame perpendicular to the ground.
    * The `i` and `j` components can be excused for this reason and only the `k` component (rotation around world-up) matters.
    * Due to angle-corrected North, add 90 degrees before switching to radians and negate the cosine.
    * @param orient the orientation of the door
    * @return the clamped orientation of the door
    */
  override def Orientation_=(orient: Vector3): Vector3 = {
    val ret = super.Orientation_=(orient)
    //transform angular data into unit circle components
    val rang = math.toRadians(orient.z + 90)
    outwards = Vector3(-math.cos(rang).toFloat, math.sin(rang).toFloat, 0)
    ret
  }

  def Outwards: Vector3 = outwards
}

object IFFLock {

  /**
    * Overloaded constructor.
    * @param idef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  def apply(idef: IFFLockDefinition): IFFLock = {
    new IFFLock(idef)
  }

  import akka.actor.ActorContext

  /**
    * Instantiate an configure a `IFFLock` object
    *
    * @param id      the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @param pos the position of the IFF lock
    * @param outwards_direction a vector used to determine which direction is inside/outside for the linked door
    * @return the `IFFLock` object
    */
  def Constructor(pos: Vector3, outwards_direction: Vector3)(id: Int, context: ActorContext): IFFLock = {
    import akka.actor.Props
    import net.psforever.objects.GlobalDefinitions

    val obj = IFFLock(GlobalDefinitions.lock_external)
    obj.Position = pos
    obj.Orientation = outwards_direction
    obj.Actor = context.actorOf(Props(classOf[IFFLockControl], obj), s"${GlobalDefinitions.lock_external.Name}_$id")
    obj
  }
}
