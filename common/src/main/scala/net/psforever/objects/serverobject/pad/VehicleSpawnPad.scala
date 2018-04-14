// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad

import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID

/**
  * A structure-owned server object that is a "spawn pad" for vehicles.<br>
  * <br>
  * Spawn pads have no purpose on their own but
  * maintain the operative queue that introduces the vehicle into the game world and applies initial activity to it and
  * maintain a position and a direction where the vehicle will be made to appear (as a `PlanetSideServerObject`).
  * The actual functionality managed by this object is wholly found on its accompanying `Actor`.
  * @see `VehicleSpawnControl`
  * @param spDef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class VehicleSpawnPad(spDef : VehicleSpawnPadDefinition) extends Amenity {
  /**
    * USE THIS BOOLEAN FOR DEVELOPMENT PURPOSES!<br>
    * Purpose: use the ingame railed platform to lift the spawned vehicle out of the trench.
    * When set, the client performs the standard vehicle entry procedure, including rail animations.
    * When unset, the client depicts the player manually boarding the new vehicle within the trench area.
    * Eventually, the vehicle is then hoisted out into the open.
    * The main reason to disable this feature is to avoid an `ObjectAttachMessage` that may be dispatched for an incorrect object.
    * Unset if not guaranteed to have the correct ingame globally unique id of the spawn pad.
    */
  private var onRails : Boolean = true

  def Railed : Boolean = onRails

  def Railed_=(useRails : Boolean) : Boolean = {
    onRails = useRails
    Railed
  }

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
  final case class ConcealPlayer(player_guid : PlanetSideGUID, zone_id : String)

  /**
    * Undoes the above message.
    */
  final case class RevealPlayer(player_guid : PlanetSideGUID, zone_id : String)

  /**
    * A callback step in spawning the vehicle.
    * The vehicle is properly introduced into the game world.
    * If information about the vehicle itself that is important to its spawning has not yet been set,
    * this callback is the last ideal situation to set that properties without having to adjust the vehicle visually.
    * The primary operation that should occur is a content-appropriate `ObjectCreateMessage` packet and
    * having the player sit down in the driver's seat (seat 0) of the vehicle.
    * @param vehicle the vehicle being spawned
    */
  final case class LoadVehicle(vehicle : Vehicle, zone : Zone)

  final case class AttachToRails(vehicle : Vehicle, pad : VehicleSpawnPad, zone_id : String)

  final case class DetachFromRails(vehicle : Vehicle, pad : VehicleSpawnPad, zone_id : String)

  final case class StartPlayerSeatedInVehicle(vehicle : Vehicle, pad : VehicleSpawnPad)

  /**
    * A TEMPORARY callback step in spawning the vehicle.
    * From a state of transparency, while the vehicle is attached to the lifting platform of the spawn pad,
    * the player designated the "owner" by callback is made to sit in the driver's seat (always seat 0).
    * This message is the next step after that.
    * @param vehicle the vehicle being spawned
    */
  final case class PlayerSeatedInVehicle(vehicle : Vehicle, pad : VehicleSpawnPad) //TODO while using fake rails

  final case class ServerVehicleOverrideStart(vehicle : Vehicle, pad : VehicleSpawnPad)

  final case class ServerVehicleOverrideEnd(vehicle : Vehicle, pad : VehicleSpawnPad)

  final case class ResetSpawnPad(pad : VehicleSpawnPad, zone_id : String)

  final case class PeriodicReminder(msg : String)

  final case class DisposeVehicle(vehicle : Vehicle, zone : Zone)

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
