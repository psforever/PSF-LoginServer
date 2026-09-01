// Copyright (c) 2026 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.types.{DriveState, VehicleFormat}
import scodec.Attempt.{Failure, Successful}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.HNil

/**
 * A basic `Trait` connecting all of the vehicle data formats (excepting `Normal`/`None`).
 */
sealed abstract class SpecificVehicleData(val format: VehicleFormat) extends StreamBitSize

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
 * @param boostMaxHealth vehicle gets 10% more armor from vehicle armor benefit given by Cyssor empire lock
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
case class VehiclePatternData(
                               boostMaxHealth: Boolean,
                               health: Int,
                               unk4: Boolean,
                               no_mount_points: Boolean,
                               driveState: DriveState,
                               unk5: Boolean,
                               unk6: Boolean,
                               cloak: Boolean,
                               vehicle_format_data: Option[SpecificVehicleData],
                               inventory: Option[InventoryData] = None
                             )(val vehicle_type: VehicleFormat = VehicleFormat.Normal) extends StreamBitSize {
  /**
   * The base size is 23L.
   * If inventory is defined under `u5`,
   * add 10L for the inventory data (`InventoryData`) plus the size of the inventory fields.
   * @return the number of bits necessary to measure an object of this class
   */
  override def bitsize: Long = {
    val extraBitsSize: Long = vehicle_format_data.map(_.bitsize).getOrElse(0L)
    val inventorySize: Long = inventory.map(_.bitsize).getOrElse(0L)
    VehiclePatternData.bitsize + extraBitsSize + inventorySize
  }
}

object VehiclePatternData {
  final val bitsize: Long = 23L

  private val driveState8u = uint8.xmap[DriveState](
    n => DriveState(n),
    {
      case n if n.id < 0 => DriveState.Mobile.id
      case n => n.id
    }
  )

  /**
   * `Codec` for the "utility" format.
   */
  private val utility_data_codec: Codec[SpecificVehicleData] = {
    import shapeless.::
    uintL(VehicleFormat.Utility.value).hlist.exmap[SpecificVehicleData](
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
    uintL(VehicleFormat.Variant.value).hlist.exmap[SpecificVehicleData](
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
  private def selectFormatReader(vehicleFormat: VehicleFormat): Codec[SpecificVehicleData] =
    vehicleFormat match {
      case VehicleFormat.Utility =>
        utility_data_codec
      case VehicleFormat.Variant =>
        variant_data_codec
      case _ =>
        Failure(Err(s"$vehicleFormat is not a valid vehicle format for parsing data"))
          .asInstanceOf[Codec[SpecificVehicleData]]
    }

  def codec(velocity_data: Boolean, vehicle_type: VehicleFormat): Codec[VehiclePatternData] = {
    import shapeless.::
    (("unk3" | bool) ::
      ("health" | uint8) ::
      ("unk4" | bool) :: //usually 0
      ("no_mount_points" | bool) ::
      ("driveState" | driveState8u) :: //used for deploy state
      ("unk5" | bool) ::               //unknown but generally false; can cause stream misalignment if set when unexpectedly
      ("unk6" | bool) ::
      ("cloak" | bool) :: //cloak as wraith, phantasm
      ("vehicle_format_data" | conditional(vehicle_type != VehicleFormat.Normal, selectFormatReader(vehicle_type))) ::
      ("inventory" | optional(bool, MountableInventory.custom_inventory_codec(velocity_data, vehicle_type)))
      ).exmap[VehiclePatternData](
      {
        case u3 :: health :: u4 :: no_mount :: driveState :: u5 :: u6 :: cloak :: format :: inv :: HNil =>
          Attempt.successful(
            VehiclePatternData(u3, health, u4, no_mount, driveState, u5, u6, cloak, format, inv)(vehicle_type)
          )

        case data =>
          Attempt.failure(Err(s"invalid vehicle data format - $data"))
      },
      {
        case obj @ VehiclePatternData(u3, health, u4, no_mount, driveState, u5, u6, cloak, format, inv) =>
          if (obj.vehicle_type == VehicleFormat.Normal && format.nonEmpty) {
            Attempt.failure(Err("invalid vehicle data format; variable bits not expected"))
          } else if (obj.vehicle_type != VehicleFormat.Normal && format.isEmpty) {
            Attempt.failure(Err(s"invalid vehicle data format; variable bits for ${obj.vehicle_type} expected"))
          } else {
            Attempt.successful(
              u3 :: health :: u4 :: no_mount :: driveState :: u5 :: u6 :: cloak :: format :: inv :: HNil
            )
          }
      }
    )
  }
}
