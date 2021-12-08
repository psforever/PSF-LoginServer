// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.serverobject.structures.AmenityDefinition
import net.psforever.types.Vector3

/**
  * The definition for any `VehicleSpawnPad`.
  */
class VehicleSpawnPadDefinition(objectId: Int) extends AmenityDefinition(objectId) {

  // Different pads require a Z offset to stop vehicles falling through the world after the pad rises from the floor, these values are found in game_objects.adb.lst
  private var vehicle_creation_z_offset = 0f

  // Different pads also require an orientation offset when detaching vehicles from the rails associated with the spawn pad, again in game_objects.adb.lst
  // For example: 9754:add_property dropship_pad_doors vehiclecreationzorientoffset 90
  // However, it seems these values need to be reversed to turn CCW to CW rotation (e.g. +90 to -90)
  private var vehicle_creation_z_orient_offset = 0f

  def VehicleCreationZOffset: Float = vehicle_creation_z_offset
  def VehicleCreationZOrientOffset: Float = vehicle_creation_z_orient_offset

  def VehicleCreationZOffset_=(offset: Float): Float = {
    vehicle_creation_z_offset = offset
    vehicle_creation_z_offset
  }
  def VehicleCreationZOrientOffset_=(offset: Float): Float = {
    vehicle_creation_z_orient_offset = offset
    vehicle_creation_z_orient_offset
  }

  /** The region surrounding a vehicle spawn pad that is cleared of damageable targets prior to a vehicle being spawned.
    * I mean to say that, if it can die, that target will die.
    * @see `net.psforever.objects.serverobject.pad.process.VehicleSpawnControlRailJack` */
  var killBox: (VehicleSpawnPad, Boolean)=>(PlanetSideGameObject, PlanetSideGameObject, Float)=> Boolean =
    VehicleSpawnPadDefinition.prepareKillBox(forwardLimit = 0, backLimit = 0, sideLimit = 0, aboveLimit = 0)
}

object VehicleSpawnPadDefinition {
  /**
    * A function that sets up the region around a vehicle spawn pad
    * to be cleared of damageable targets upon spawning of a vehicle.
    * All measurements are provided in terms of distance from the center of the pad.
    * These generic pads are rectangular in bounds and the kill box is cuboid in shape.
    * @param forwardLimit how far in front of the spawn pad is to be cleared
    * @param backLimit how far behind the spawn pad to be cleared;
    *                  "back" is a squared direction usually in that direction of the corresponding terminal
    * @param sideLimit how far to either side of the spawn pad is to be cleared
    * @param aboveLimit how far above the spawn pad is to be cleared
    * @param pad the vehicle spawn pad in question
    * @param flightVehicle whether the current vehicle being ordered is a flying craft
    * @return a function that describes a region around the vehicle spawn pad
    */
  def prepareKillBox(
                      forwardLimit: Float,
                      backLimit: Float,
                      sideLimit: Float,
                      aboveLimit: Float
                    )
                    (
                      pad: VehicleSpawnPad,
                      flightVehicle: Boolean
                    ): (PlanetSideGameObject, PlanetSideGameObject, Float) => Boolean = {
    val forward = Vector3(0,1,0).Rz(pad.Orientation.z + pad.Definition.VehicleCreationZOrientOffset)
    val side = Vector3.CrossProduct(forward, Vector3(0,0,1))
    vehicleSpawnKillBox(
      forward,
      side,
      pad.Position,
      if (flightVehicle) backLimit else forwardLimit,
      backLimit,
      sideLimit,
      if (flightVehicle) aboveLimit * 2 else aboveLimit,
    )
  }

  /**
    * A function that finalizes the detection for the region around a vehicle spawn pad
    * to be cleared of damageable targets upon spawning of a vehicle.
    * All measurements are provided in terms of distance from the center of the pad.
    * These generic pads are rectangular in bounds and the kill box is cuboid in shape.
    * @param forward a direction in a "forwards" direction relative to the orientation of the spawn pad
    * @param side a direction in a "side-wards" direction relative to the orientation of the spawn pad
    * @param origin the center of the spawn pad
    * @param forwardLimit how far in front of the spawn pad is to be cleared
    * @param backLimit how far behind the spawn pad to be cleared
    * @param sideLimit how far to either side of the spawn pad is to be cleared
    * @param aboveLimit how far above the spawn pad is to be cleared
    * @param obj1 a game entity, should be the source
    * @param obj2 a game entity, should be the target
    * @param maxDistance the square of the maximum distance permissible between game entities
    *                    before they are no longer considered "near"
    * @return `true`, if the two entities are near enough to each other;
    *        `false`, otherwise
    */
  protected def vehicleSpawnKillBox(
                                     forward: Vector3,
                                     side: Vector3,
                                     origin: Vector3,
                                     forwardLimit: Float,
                                     backLimit: Float,
                                     sideLimit: Float,
                                     aboveLimit: Float
                                   )
                                   (
                                     obj1: PlanetSideGameObject,
                                     obj2: PlanetSideGameObject,
                                     maxDistance: Float
                                   ): Boolean = {
    val dir: Vector3 = {
      val g2 = obj2.Definition.Geometry(obj2)
      val cdir = Vector3.Unit(origin - g2.center.asVector3)
      val point = g2.pointOnOutside(cdir).asVector3
      point - origin
    }
    val originZ = origin.z
    val obj2Z = obj2.Position.z
    originZ - 1 <= obj2Z && originZ + aboveLimit > obj2Z &&
    {
      val calculatedForwardDistance = Vector3.ScalarProjection(dir, forward)
      if (calculatedForwardDistance >= 0) {
        calculatedForwardDistance < forwardLimit
      }
      else {
        -calculatedForwardDistance < backLimit
      }
    } &&
    math.abs(Vector3.ScalarProjection(dir, side)) < sideLimit
  }

  /**
    * A function that sets up the region around a vehicle spawn pad
    * to be cleared of damageable targets upon spawning of a vehicle.
    * All measurements are provided in terms of distance from the center of the pad.
    * These pads are only found in the cavern zones and are cylindrical in shape.
    * @param radius the distance from the middle of the spawn pad
    * @param aboveLimit how far above the spawn pad is to be cleared
    * @param pad he vehicle spawn pad in question
    * @param flightVehicle whether the current vehicle being ordered is a flying craft
    * @return a function that describes a region around the vehicle spawn pad
    */
  def prepareVanuKillBox(
                          radius: Float,
                          aboveLimit: Float
                        )
                        (
                          pad: VehicleSpawnPad,
                          flightVehicle: Boolean
                        ): (PlanetSideGameObject, PlanetSideGameObject, Float) => Boolean = {
    if (flightVehicle) {
      cylinderKillBox(pad.Position, radius, aboveLimit * 2)
    } else {
      cylinderKillBox(pad.Position, radius * 1.2f, aboveLimit)
    }
  }

  /**
    * A function that sets up the region around a battleframe vehicle spawn chamber's doors
    * to be cleared of damageable targets upon spawning of a vehicle.
    * All measurements are provided in terms of distance from the middle of the door.
    * Internally, the pad is referred to as `bfr_door`;
    * colloquially, the pad is referred to as a "BFR shed".
    * @param radius the distance from the middle of the spawn pad
    * @param aboveLimit how far above the spawn pad is to be cleared
    * @param pad he vehicle spawn pad in question
    * @param requiredButUnused required by the function prototype
    * @return a function that describes a region ahead of the battleframe vehicle spawn shed
    */
  def prepareBfrShedKillBox(
                             radius: Float,
                             aboveLimit: Float
                           )
                           (
                             pad: VehicleSpawnPad,
                             requiredButUnused: Boolean
                           ): (PlanetSideGameObject, PlanetSideGameObject, Float) => Boolean = {
    cylinderKillBox(
      Vector3(0,radius,0).Rz(pad.Orientation.z + pad.Definition.VehicleCreationZOrientOffset) + pad.Position,
      radius,
      aboveLimit
    )
  }

  /**
    * A function that finalizes the detection for the region around a vehicle spawn pad
    * to be cleared of damageable targets upon spawning of a vehicle.
    * All measurements are provided in terms of distance from the center of the pad.
    * These pads are cylindrical in shape.
    * @param origin the center of the spawn pad
    * @param radius the distance from the middle of the spawn pad
    * @param aboveLimit how far above the spawn pad is to be cleared
    * @param obj1 a game entity, should be the source
    * @param obj2 a game entity, should be the target
    * @param maxDistance the square of the maximum distance permissible between game entities
    *                    before they are no longer considered "near"
    * @return `true`, if the two entities are near enough to each other;
    *        `false`, otherwise
    */
  def cylinderKillBox(
                       origin: Vector3,
                       radius: Float,
                       aboveLimit: Float
                     )
                     (
                       obj1: PlanetSideGameObject,
                       obj2: PlanetSideGameObject,
                       maxDistance: Float
                     ): Boolean = {
    val dir: Vector3 = {
      val g2 = obj2.Definition.Geometry(obj2)
      val cdir = Vector3.Unit(origin - g2.center.asVector3)
      val point = g2.pointOnOutside(cdir).asVector3
      point - origin
    }
    val originZ = origin.z
    val obj2Z = obj2.Position.z
    originZ - 1 <= obj2Z && originZ + aboveLimit > obj2Z &&
    Vector3.MagnitudeSquared(dir.xy) < radius * radius
  }
}
