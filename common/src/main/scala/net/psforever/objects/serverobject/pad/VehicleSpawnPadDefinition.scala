// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad

import net.psforever.objects.definition.ObjectDefinition

/**
  * The definition for any `VehicleSpawnPad`.
  */
class VehicleSpawnPadDefinition(objectId : Int) extends ObjectDefinition(objectId) {

  // Different pads require a Z offset to stop vehicles falling through the world after the pad rises from the floor, these values are found in game_objects.adb.lst
  private var vehicle_creation_z_offset = 0f

  // Different pads also require an orientation offset when detaching vehicles from the rails associated with the spawn pad, again in game_objects.adb.lst
  // For example: 9754:add_property dropship_pad_doors vehiclecreationzorientoffset 90
  // However, it seems these values need to be reversed to turn CCW to CW rotation (e.g. +90 to -90)
  private var vehicle_creation_z_orient_offset = 0f

  def VehicleCreationZOffset : Float = vehicle_creation_z_offset
  def VehicleCreationZOrientOffset : Float = vehicle_creation_z_orient_offset

  objectId match {
    case 141 =>
      Name = "bfr_door"
      vehicle_creation_z_offset = -4.5f
      vehicle_creation_z_orient_offset = 90f
    case 261 =>
      Name = "dropship_pad_doors"
      vehicle_creation_z_offset = 4.89507f
      vehicle_creation_z_orient_offset = -90f
    case 525 =>
      Name = "mb_pad_creation"
      vehicle_creation_z_offset = 2.52604f
    case 615 => Name = "pad_create"
    case 616 =>
      Name = "pad_creation"
      vehicle_creation_z_offset = 1.70982f
    case 816 => Name = "spawnpoint_vehicle"
    case 947 => Name = "vanu_vehicle_creation_pad"
    case _ => throw new IllegalArgumentException("Not a valid object id with the type vehicle_creation_pad")
  }
}
