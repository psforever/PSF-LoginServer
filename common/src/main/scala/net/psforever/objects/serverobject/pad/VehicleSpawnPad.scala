// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad

import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.PlanetSideGUID

/**
  * A structure-owned server object that is a "spawn pad" for vehicles.<br>
  * <br>
  * Spawn pads have no purpose on their own but
  * maintain the operative queue that introduces the vehicle into the game world and applies initial activity to it and
  * maintain a position and a direction where the vehicle will be made to appear (as a `PlanetSideServerObject`).
  * The actual functionality managed by this object is wholly found on its accompanying `Actor`.
  * @param spDef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  * @see `VehicleSpawnControl`
  */
class VehicleSpawnPad(spDef : VehicleSpawnPadDefinition) extends Amenity {
  def Definition : VehicleSpawnPadDefinition = spDef
}

object VehicleSpawnPad {

  /**
    * Communicate to the spawn pad that it should enqueue the following vehicle.
    * This is the entry point to vehicle spawn pad functionality.
    * @param player the player who submitted the order (the "owner")
    * @param vehicle the vehicle produced from the order
    */
  final case class VehicleOrder(player : Player, vehicle : Vehicle)

  /**
    * The first callback step in spawning the vehicle.
    * An packet `GenericObjectActionMessage(/player/, 36)`, when used on a player character,
    * will cause that player character's model to fade into transparency.
    */
  final case class ConcealPlayer()

  /**
    * A callback step in spawning the vehicle.
    * The vehicle is properly introduced into the game world.
    * If information about the vehicle itself that is important to its spawning has not yet been set,
    * this callback is the last ideal situation to set that properties without having to adjust the vehicle visually.
    * The primary operation that should occur is a content-appropriate `ObjectCreateMessage` packet and
    * having the player sit down in the driver's seat (seat 0) of the vehicle.
    * @param vehicle the vehicle being spawned
    * @param pad the pad
    */
  final case class LoadVehicle(vehicle : Vehicle, pad : VehicleSpawnPad)

  /**
    * A TEMPORARY callback step in spawning the vehicle.
    * From a state of transparency, while the vehicle is attached to the lifting platform of the spawn pad,
    * the player designated the "owner" by callback is made to sit in the driver's seat (always seat 0).
    * This message is the next step after that.
    * @param vehicle the vehicle being spawned
    */
  final case class PlayerSeatedInVehicle(vehicle : Vehicle)

  /**
    * A TEMPORARY callback step in (successfully) spawning the vehicle.
    * While the vehicle is still occupying the pad just after being spawned and its driver seat mounted,
    * that vehicle is considered blocking the pad from being used for further spawning operations.
    * This message allows the user to be made known about this blockage.
    * @param vehicle the vehicle
    * @param warning_count the number of times a warning period has occurred
    */
  final case class SpawnPadBlockedWarning(vehicle : Vehicle, warning_count : Int)

  /**
    * A TEMPORARY callback step in (successfully) spawning the vehicle.
    * While the vehicle is still occupying the pad just after being spawned and its driver seat mounted,
    * that vehicle is considered blocking the pad from being used for further spawning operations.
    * A timeout will begin counting until the vehicle is despawned automatically for its driver's negligence.
    * This message is used to clear the deconstruction countdown, primarily.
    * @param vehicle_guid the vehicle
    */
  final case class SpawnPadUnblocked(vehicle_guid : PlanetSideGUID)

  /**
    * Overloaded constructor.
    * @param spDef the spawn pad's definition entry
    * @return a `VehicleSpawnPad` object
    */
  def apply(spDef : VehicleSpawnPadDefinition) : VehicleSpawnPad = {
    new VehicleSpawnPad(spDef)
  }

  import akka.actor.ActorContext
  import net.psforever.types.Vector3

  /**
    * Instantiate an configure a `VehicleSpawnPad` object
    * @param pos the position (used to determine spawn point)
    * @param orient the orientation (used to indicate spawn direction)
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `VehicleSpawnPad` object
    */
  def Constructor(pos : Vector3, orient : Vector3)(id : Int, context : ActorContext) : VehicleSpawnPad = {
    import akka.actor.Props
    import net.psforever.objects.GlobalDefinitions

    val obj = VehicleSpawnPad(GlobalDefinitions.spawn_pad)
    obj.Position = pos
    obj.Orientation = orient
    obj.Actor = context.actorOf(Props(classOf[VehicleSpawnControl], obj), s"${GlobalDefinitions.spawn_pad.Name}_$id")
    obj
  }
}
