// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.Attempt.{Failure, Successful}
import scodec.{Attempt, Codec, Err}
import shapeless.HNil
import scodec.codecs._
import net.psforever.types.DriveState

/**
  * An `Enumeration` of the various formats that known structures that the stream of bits for `VehicleData` can assume.
  */
object VehicleFormat extends Enumeration {
  type Type = Value

  val Battleframe, BattleframeFlight, Normal, Utility, Variant = Value
}

/**
  * A basic `Trait` connecting all of the vehicle data formats (excepting `Normal`/`None`).
  */
sealed abstract class SpecificVehicleData(val format: VehicleFormat.Value) extends StreamBitSize

/**
  * The format of vehicle data for the type of vehicles that are considered "utility."
  * The vehicles in this category are two:
  * the advanced nanite transport, and
  * the advanced mobile station.
  * @param unk na
  */
final case class UtilityVehicleData(unk: Int) extends SpecificVehicleData(VehicleFormat.Utility) {
  override def bitsize: Long = 6L
}

/**
  * A common format variant of vehicle data.
  * This category includes all flying vehicles and the ancient cavern vehicles.
  * @param unk na
  */
final case class VariantVehicleData(unk: Int) extends SpecificVehicleData(VehicleFormat.Variant) {
  override def bitsize: Long = 8L
}

/**
  * A representation of a generic vehicle.
  * @param pos where the vehicle is and how it is oriented in the game world
  * @param data common vehicle field data:<br>
  *             -bops - this vehicle belongs to the Black Ops, regardless of the faction field;
  *              activates the green camo and adjusts permissions<br>
  *             -destroyed - this vehicle has ben destroyed;
  *              health should be less than 3/255, or 0%<br>
  *             -jammered - vehicles will not be jammered by setting this field<br>
  *             -player_guid the vehicle's (official) owner;
  *              a living player in the game world on the same continent as the vehicle who may mount the driver mount
  * @param unk3 na
  * @param health the amount of health the vehicle has, as a percentage of a filled bar (255)
  * @param unk4 na
  * @param no_mount_points do not display entry points for the seats
  * @param driveState a representation for the current mobility state;
  *                   various vehicles also use this field to indicate "deployment," e.g., the advanced mobile spawn
  * @param unk5 na
  * @param unk6 na
  * @param cloak if a vehicle (that can cloak) is cloaked
  * @param vehicle_format_data extra information necessary to implement special-type vehicles;
  *                            see `vehicle_type`
  * @param inventory the seats, mounted weapons, and utilities (such as terminals) that are currently included;
  *                  will also include trunk contents;
  *                  the driver is the only valid mount entry (more will cause the access permissions to act up)
  * @param vehicle_type a modifier for parsing the vehicle data format differently;
  *                     see `vehicle_format_data`;
  *                     defaults to `Normal`
  */
final case class VehicleData(
    pos: PlacementData,
    data: CommonFieldData,
    unk3: Boolean,
    health: Int,
    unk4: Boolean,
    no_mount_points: Boolean,
    driveState: DriveState.Value,
    unk5: Boolean,
    unk6: Boolean,
    cloak: Boolean,
    vehicle_format_data: Option[SpecificVehicleData],
    inventory: Option[InventoryData] = None
)(val vehicle_type: VehicleFormat.Value = VehicleFormat.Normal)
    extends ConstructorData {
  override def bitsize: Long = {
    //factor guard bool values into the base size, not its corresponding optional field
    val posSize: Long  = pos.bitsize
    val dataSize: Long = data.bitsize
    val extraBitsSize: Long = if (vehicle_format_data.isDefined) { vehicle_format_data.get.bitsize }
    else { 0L }
    val inventorySize = if (inventory.isDefined) { inventory.get.bitsize }
    else { 0L }
    23L + posSize + dataSize + extraBitsSize + inventorySize
  }
}

object VehicleData extends Marshallable[VehicleData] {
  /**
    * Overloaded constructor for specifically handling `Normal` vehicle format.
    * @param basic a field that encompasses some data used by the vehicle, including `faction` and `owner`
    * @param health the amount of health the vehicle has, as a percentage of a filled bar (255)
    * @param driveState a representation for the current mobility state
    * @param cloak if a vehicle (that can cloak) is cloaked
    * @param inventory the seats, mounted weapons, and utilities (such as terminals) that are currently included
    */
  def apply(
      pos: PlacementData,
      basic: CommonFieldData,
      health: Int,
      driveState: DriveState.Value,
      cloak: Boolean,
      inventory: Option[InventoryData]
  ): VehicleData = {
    VehicleData(pos, basic, false, health, false, false, driveState, false, false, cloak, None, inventory)(
      VehicleFormat.Normal
    )
  }

  /**
    * Overloaded constructor for specifically handling `Utility` vehicle format.
    * @param basic a field that encompasses some data used by the vehicle, including `faction` and `owner`
    * @param health the amount of health the vehicle has, as a percentage of a filled bar (255)
    * @param driveState a representation for the current mobility state
    * @param cloak if a vehicle (that can cloak) is cloaked
    * @param inventory the seats, mounted weapons, and utilities (such as terminals) that are currently included
    */
  def apply(
      pos: PlacementData,
      basic: CommonFieldData,
      health: Int,
      driveState: DriveState.Value,
      cloak: Boolean,
      format: UtilityVehicleData,
      inventory: Option[InventoryData]
  ): VehicleData = {
    VehicleData(pos, basic, false, health, false, false, driveState, false, false, cloak, Some(format), inventory)(
      VehicleFormat.Utility
    )
  }

  /**
    * Overloaded constructor for specifically handling `Variant` vehicle format.
    * @param basic a field that encompasses some data used by the vehicle, including `faction` and `owner`
    * @param health the amount of health the vehicle has, as a percentage of a filled bar (255)
    * @param driveState a representation for the current mobility state
    * @param cloak if a vehicle (that can cloak) is cloaked
    * @param inventory the seats, mounted weapons, and utilities (such as terminals) that are currently included
    */
  def apply(
      pos: PlacementData,
      basic: CommonFieldData,
      health: Int,
      driveState: DriveState.Value,
      cloak: Boolean,
      format: VariantVehicleData,
      inventory: Option[InventoryData]
  ): VehicleData = {
    VehicleData(pos, basic, false, health, false, false, driveState, false, false, cloak, Some(format), inventory)(
      VehicleFormat.Variant
    )
  }

  private val driveState8u = uint8.xmap[DriveState.Value](
    n => DriveState(n),
    n => n.id
  )

  /**
    * `Codec` for the "utility" format.
    */
  private val utility_data_codec: Codec[SpecificVehicleData] = {
    import shapeless.::
    uintL(6).hlist.exmap[SpecificVehicleData](
      {
        case n :: HNil =>
          Successful(UtilityVehicleData(n).asInstanceOf[SpecificVehicleData])
      },
      {
        case UtilityVehicleData(n) =>
          Successful(n :: HNil)
        case _ =>
          Failure(Err("wrong kind of vehicle data object (wants 'Utility')"))
      }
    )
  }

  /**
    * `Codec` for the "variant" format.
    */
  private val variant_data_codec: Codec[SpecificVehicleData] = {
    import shapeless.::
    uint8L.hlist.exmap[SpecificVehicleData](
      {
        case n :: HNil =>
          Successful(VariantVehicleData(n).asInstanceOf[SpecificVehicleData])
      },
      {
        case VariantVehicleData(n) =>
          Successful(n :: HNil)
        case _ =>
          Failure(Err("wrong kind of vehicle data object (wants 'Variant')"))
      }
    )
  }

  /**
    * Select an appropriate `Codec` in response to the requested stream format
    * @param vehicleFormat the requested format
    * @return the appropriate `Codec` for parsing that format
    */
  private def selectFormatReader(vehicleFormat: VehicleFormat.Value): Codec[SpecificVehicleData] =
    vehicleFormat match {
      case VehicleFormat.Utility =>
        utility_data_codec
      case VehicleFormat.Variant =>
        variant_data_codec
      case _ =>
        Failure(Err(s"$vehicleFormat is not a valid vehicle format for parsing data"))
          .asInstanceOf[Codec[SpecificVehicleData]]
    }

  def codec(vehicle_type: VehicleFormat.Value): Codec[VehicleData] = {
    import shapeless.::
    (
      ("pos" | PlacementData.codec) >>:~ { pos =>
        ("data" | CommonFieldData.codec2(false)) ::
        ("unk3" | bool) ::
        ("health" | uint8L) ::
        ("unk4" | bool) :: //usually 0
        ("no_mount_points" | bool) ::
        ("driveState" | driveState8u) :: //used for deploy state
        ("unk5" | bool) ::               //unknown but generally false; can cause stream misalignment if set when unexpectedly
        ("unk6" | bool) ::
        ("cloak" | bool) :: //cloak as wraith, phantasm
        conditional(vehicle_type != VehicleFormat.Normal,"vehicle_format_data" | selectFormatReader(vehicle_type)) ::
        optional(bool, target = "inventory" | MountableInventory.custom_inventory_codec(pos.vel.isDefined, VehicleFormat.Normal))
      }
    ).exmap[VehicleData](
      {
        case pos :: data :: u3 :: health :: u4 :: no_mount :: driveState :: u5 :: u6 :: cloak :: format :: inv :: HNil =>
          Attempt.successful(
            new VehicleData(pos, data, u3, health, u4, no_mount, driveState, u5, u6, cloak, format, inv)(vehicle_type)
          )

        case data =>
          Attempt.failure(Err(s"invalid vehicle data format - $data"))
      },
      {
        case obj @ VehicleData(pos, data, u3, health, u4, no_mount, driveState, u5, u6, cloak, format, inv) =>
          if (obj.vehicle_type == VehicleFormat.Normal && format.nonEmpty) {
            Attempt.failure(Err("invalid vehicle data format; variable bits not expected"))
          } else if (obj.vehicle_type != VehicleFormat.Normal && format.isEmpty) {
            Attempt.failure(Err(s"invalid vehicle data format; variable bits for ${obj.vehicle_type} expected"))
          } else {
            Attempt.successful(
              pos :: data :: u3 :: health :: u4 :: no_mount :: driveState :: u5 :: u6 :: cloak :: format :: inv :: HNil
            )
          }
      }
    )
  }

  implicit val codec: Codec[VehicleData] = codec(VehicleFormat.Normal)
}
