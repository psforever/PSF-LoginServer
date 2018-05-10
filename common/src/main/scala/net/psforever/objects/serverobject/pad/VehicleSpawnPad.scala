// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad

import net.psforever.objects.serverobject.pad.process.AutoDriveControls
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
    * Use the in-game railed platform to lift the spawned vehicle out of the trench.
    * When set, the client performs the standard vehicle entry procedure, including lifting platform animations.
    * When unset, the client depicts the player manually boarding the new vehicle within the trench area.
    * Eventually, the vehicle is then hoisted out into the open; without this set, that hoisting is abrupt.
    * The main reason to disable this feature is to avoid an `ObjectAttachMessage` for an incorrect object designation.
    * Unset if not guaranteed to have the correct globally unique id of the spawn pad.
    */
  private var onRails : Boolean = true

  private var guidedPath : List[AutoDriveControls.Configuration] = Nil

  def Railed : Boolean = onRails

  def Railed_=(useRails : Boolean) : Boolean = {
    onRails = useRails
    Railed
  }

  def Guide : List[AutoDriveControls.Configuration] = guidedPath

  def Guide_=(path : List[AutoDriveControls.Configuration]) : List[AutoDriveControls.Configuration] = {
    guidedPath = path
    Guide
  }

  def Definition : VehicleSpawnPadDefinition = spDef
}

object VehicleSpawnPad {

  /**
    * Message to the spawn pad to enqueue the following vehicle order.
    * This is the entry point to vehicle spawn pad functionality.
    * @param player the player who submitted the order (the "owner")
    * @param vehicle the vehicle produced from the order
    */
  final case class VehicleOrder(player : Player, vehicle : Vehicle)

  /**
    * Message to indicate that a certain player should be made transparent.
    * @see `GenericObjectActionMessage`
    * @param player_guid the player
    * @param zone_id the zone in which the spawn pad is located
    */
  final case class ConcealPlayer(player_guid : PlanetSideGUID, zone_id : String)

  /**
    * Message is intended to undo the effects of the above message, `ConcealPlayer`.
    * @see `ConcealPlayer`
    * @param player_guid the player
    * @param zone_id the zone in which the spawn pad is located
    */
  final case class RevealPlayer(player_guid : PlanetSideGUID, zone_id : String)

  /**
    * Message to properly introduce the vehicle into the zone.
    * @param vehicle the vehicle being spawned
    * @param zone the zone in which the spawn pad is located
    */
  final case class LoadVehicle(vehicle : Vehicle, zone : Zone)

  /**
    * Message to attach the vehicle to the spawn pad's lifting platform ("put on rails").
    * The attachment process (to the third slot) itself begins autonomous operation of the lifting platform.
    * @see `ObjectAttachMessage`
    * @param vehicle the vehicle being spawned
    * @param pad the spawn pad
    * @param zone_id the zone in which the spawn pad is located
    */
  final case class AttachToRails(vehicle : Vehicle, pad : VehicleSpawnPad, zone_id : String)

  /**
    * Message to detach the vehicle from the spawn pad's lifting platform ("put on rails").
    * @see `ObjectDetachMessage`
    * @param vehicle the vehicle being spawned
    * @param pad the spawn pad
    * @param zone_id the zone in which the spawn pad is located
    */
  final case class DetachFromRails(vehicle : Vehicle, pad : VehicleSpawnPad, zone_id : String)

  /**
    * Message that resets the spawn pad for its next order fulfillment operation by lowering the lifting platform.
    * @see `GenericObjectActionMessage`
    * @param pad the spawn pad
    * @param zone_id the zone in which the spawn pad is located
    */
  final case class ResetSpawnPad(pad : VehicleSpawnPad, zone_id : String)

  /**
    * Message that acts as callback to the driver that the process of sitting in the driver seat will be initiated soon.
    * This information should only be communicated to the driver's client only.
    * @param vehicle the vehicle being spawned
    * @param pad the spawn pad
    */
  final case class StartPlayerSeatedInVehicle(vehicle : Vehicle, pad : VehicleSpawnPad)

  /**
    * Message that acts as callback to the driver that the process of sitting in the driver seat should be finished.
    * This information should only be communicated to the driver's client only.
    * @param vehicle the vehicle being spawned
    * @param pad the spawn pad
    */
  final case class PlayerSeatedInVehicle(vehicle : Vehicle, pad : VehicleSpawnPad) //TODO while using fake rails

  /**
    * Message that starts the newly-spawned vehicle to begin driving away from the spawn pad.
    * Information about the driving process is available on the vehicle itself.
    * This information should only be communicated to the driver's client only.
    * @see `VehicleDefinition`
    * @param vehicle the vehicle
    * @param pad the spawn pad
    */
  final case class ServerVehicleOverrideStart(vehicle : Vehicle, pad : VehicleSpawnPad)

  /**
    * Message that transitions the newly-spawned vehicle into a cancellable auto-drive state.
    * Information about the driving process is available on the vehicle itself.
    * This information should only be communicated to the driver's client only.
    * @see `VehicleDefinition`
    * @param vehicle the vehicle
    * @param pad the spawn pad
    */
  final case class ServerVehicleOverrideEnd(vehicle : Vehicle, pad : VehicleSpawnPad)

  /**
    * Message to initiate the process of properly disposing of the vehicle that may have been or was spawned into the game world.
    * @param vehicle the vehicle
    * @param zone the zone in which the spawn pad is located
    */
  final case class DisposeVehicle(vehicle : Vehicle, zone : Zone)

  /**
    * Message to send targeted messages to the clients of specific users.
    * @param reason the nature of the message
    * @param data optional information for rendering the message to the client
    */
  final case class PeriodicReminder(reason : Reminders.Value, data : Option[Any] = None)

  /**
    * An `Enumeration` of reasons for sending a periodic reminder to the user.
    */
  object Reminders extends Enumeration {
    val
    Queue, //optional data is the numeric position in the queue
    Blocked, //optional data is a message regarding the blockage
    Cancelled
    = Value
  }

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
    * Instantiate and configure a `VehicleSpawnPad` object
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
