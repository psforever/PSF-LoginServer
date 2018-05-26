// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate
import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.Attempt.{Failure, Successful}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

import net.psforever.types.DriveState

/**
  * An `Enumeration` of the various formats that known structures that the stream of bits for `VehicleData` can assume.
  */
object VehicleFormat extends Enumeration {
  type Type = Value

  val
  Battleframe, //future expansion?
  Normal,
  Utility,
  Variant = Value
}

/**
  * A basic `Trait` connecting all of the vehicle data formats (excepting `Normal`/`None`).
  */
sealed trait SpecificVehicleData extends StreamBitSize

/**
  * The format of vehicle data for the type of vehicles that are considered "utility."
  * The vehicles in this category are two:
  * the advanced nanite transport, and
  * the advanced mobile station.
  * @param unk na
  */
final case class UtilityVehicleData(unk : Int) extends SpecificVehicleData {
  override def bitsize : Long = 6L
}

/**
  * A common format variant of vehicle data.
  * This category includes all flying vehicles and the ancient cavern vehicles.
  * @param unk na
  */
final case class VariantVehicleData(unk : Int) extends SpecificVehicleData {
  override def bitsize : Long = 8L
}

/**
  * A representation of a generic vehicle.<br>
  * <br>
  * Vehicles utilize their own packet to communicate position to the server, known as `VehicleStateMessage`.
  * This takes the place of `PlayerStateMessageUpstream` when the player avatar is in control;
  * and, it takes the place of `PlayerStateMessage` for other players when they are in control.
  * If the vehicle is sufficiently complicated, a `ChildObjectStateMessage` will be used.
  * This packet will control any turret(s) on the vehicle.
  * For very complicated vehicles, the packets `FrameVehicleStateMessage` and `VehicleSubStateMessage` will also be employed.
  * The tasks that these packets perform are different based on the vehicle that responds or generates them.
  * @param basic data common to objects
  * @param unk1 na. Valid values seem to be 0-3. Anything higher spawns a completely broken NC vehicle with no guns that can't move
  * @param health the amount of health the vehicle has, as a percentage of a filled bar (255)
  * @param unk2 na
  * @param no_mount_points do not display entry points for the seats
  * @param driveState a representation for the current mobility state;
  *                   various vehicles also use this field to indicate "deployment," e.g., AMS
  * @param unk3 na
  * @param unk5 na
  * @param cloak if a cloakable vehicle is cloaked
  * @param unk4 na
  * @param inventory the seats, mounted weapons, and utilities (such as terminals) that are currently included;
  *                  will also include trunk contents
  * @param vehicle_type a modifier for parsing the vehicle data format differently;
  *                     defaults to `Normal`
  */
final case class VehicleData(basic : CommonFieldData,
                             unk1 : Int,
                             health : Int,
                             unk2 : Boolean,
                             no_mount_points : Boolean,
                             driveState : DriveState.Value,
                             unk3 : Boolean,
                             unk5 : Boolean,
                             cloak : Boolean,
                             unk4 : Option[SpecificVehicleData],
                             inventory : Option[InventoryData] = None
                            )(val vehicle_type : VehicleFormat.Value = VehicleFormat.Normal) extends ConstructorData {
  override def bitsize : Long = {
    val basicSize = basic.bitsize
    val extraBitsSize : Long = if(unk4.isDefined) { unk4.get.bitsize } else { 0L }
    val inventorySize = if(inventory.isDefined) { inventory.get.bitsize } else { 0L }
    24L + basicSize + extraBitsSize + inventorySize
  }
}

object VehicleData extends Marshallable[VehicleData] {
  /**
    * Overloaded constructor for specifically handling `Normal` vehicle format.
    * @param basic data common to objects
    * @param unk1 na
    * @param health the amount of health the vehicle has, as a percentage of a filled bar (255)
    * @param unk2 na
    * @param driveState a representation for the current mobility state;
    * @param unk3 na
    * @param unk4 na
    * @param inventory the seats, mounted weapons, and utilities (such as terminals) that are currently included
    * @return a `VehicleData` object
    */
  def apply(basic : CommonFieldData, unk1 : Int, health : Int, unk2 : Int, driveState : DriveState.Value, unk3 : Boolean, unk4 : Int, inventory : Option[InventoryData]) : VehicleData = {
    new VehicleData(basic, unk1, health, unk2>0, false, driveState, unk3, unk4>0, false, None, inventory)(VehicleFormat.Normal)
  }

  /**
    * Overloaded constructor for specifically handling `Utility` vehicle format.
    * @param basic data common to objects
    * @param unk1 na
    * @param health the amount of health the vehicle has, as a percentage of a filled bar (255)
    * @param unk2 na
    * @param driveState a representation for the current mobility state;
    * @param unk3 na
    * @param unk4 utility-specific field
    * @param unk5 na
    * @param inventory the seats, mounted weapons, and utilities (such as terminals) that are currently included
    * @return a `VehicleData` object
    */
  def apply(basic : CommonFieldData, unk1 : Int, health : Int, unk2 : Int, driveState : DriveState.Value, unk3 : Boolean, unk4 : UtilityVehicleData, unk5 : Int, inventory : Option[InventoryData]) : VehicleData = {
    new VehicleData(basic, unk1, health, unk2>0, false, driveState, unk3, unk5>0, false, Some(unk4), inventory)(VehicleFormat.Utility)
  }

  /**
    * Overloaded constructor for specifically handling `Variant` vehicle format.
    * @param basic data common to objects
    * @param unk1 na
    * @param health the amount of health the vehicle has, as a percentage of a filled bar (255)
    * @param unk2 na
    * @param driveState a representation for the current mobility state;
    * @param unk3 na
    * @param unk4 variant-specific field
    * @param unk5 na
    * @param inventory the seats, mounted weapons, and utilities (such as terminals) that are currently included
    * @return a `VehicleData` object
    */
  def apply(basic : CommonFieldData, unk1 : Int, health : Int, unk2 : Int, driveState : DriveState.Value, unk3 : Boolean, unk4 : VariantVehicleData, unk5 : Int, inventory : Option[InventoryData]) : VehicleData = {
    new VehicleData(basic, unk1, health, unk2>0, false, driveState, unk3, unk5>0, false, Some(unk4), inventory)(VehicleFormat.Variant)
  }

  private val driveState8u = PacketHelpers.createEnumerationCodec(DriveState, uint8L)

  /**
    * `Codec` for the "utility" format.
    */
  private val utility_data_codec : Codec[SpecificVehicleData] = uintL(6).hlist.exmap[SpecificVehicleData] (
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
  /**
    * `Codec` for the "variant" format.
    */
  private val variant_data_codec : Codec[SpecificVehicleData] = uint8L.hlist.exmap[SpecificVehicleData] (
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

  /**
    * Select an appropriate `Codec` in response to the requested stream format
    * @param vehicleFormat the requested format
    * @return the appropriate `Codec` for parsing that format
    */
  private def selectFormatReader(vehicleFormat : VehicleFormat.Value) : Codec[SpecificVehicleData] = vehicleFormat match {
    case VehicleFormat.Utility =>
      utility_data_codec
    case VehicleFormat.Variant =>
      variant_data_codec
    case _ =>
      Failure(Err(s"$vehicleFormat is not a valid vehicle format for parsing data")).asInstanceOf[Codec[SpecificVehicleData]]
  }

  def codec(vehicle_type : VehicleFormat.Value) : Codec[VehicleData] = (
    ("basic" | CommonFieldData.codec) ::
      ("unk1" | uint2L) ::
      ("health" | uint8L) ::
      ("unk2" | bool) :: //usually 0
      ("no_mount_points" | bool) ::
      ("driveState" | driveState8u) :: //used for deploy state
      ("unk3" | bool) :: //unknown but generally false; can cause stream misalignment if set when unexpectedly
      ("unk4" | bool) ::
      ("cloak" | bool) :: //cloak as wraith, phantasm
      conditional(vehicle_type != VehicleFormat.Normal, "unk5" | selectFormatReader(vehicle_type)) :: //padding?
      optional(bool, "inventory" | InventoryData.codec)
    ).exmap[VehicleData] (
    {
      case basic :: u1 :: health :: u2 :: no_mount :: driveState :: u3 :: u4 :: u5 :: cloak :: inv :: HNil =>
        Attempt.successful(new VehicleData(basic, u1, health, u2, no_mount, driveState, u3, u4, u5, cloak, inv)(vehicle_type))

      case _ =>
        Attempt.failure(Err("invalid vehicle data format"))
    },
    {
      case obj @ VehicleData(basic, u1, health, u2, no_mount, driveState, u3, u4, cloak, Some(u5), inv) =>
        if(obj.vehicle_type == VehicleFormat.Normal) {
          Attempt.failure(Err("invalid vehicle data format; variable bits not expected; will ignore ..."))
        }
        else {
          Attempt.successful(basic :: u1 :: health :: u2 :: no_mount :: driveState :: u3 :: u4 :: cloak :: Some(u5) :: inv :: HNil)
        }

      case obj @ VehicleData(basic, u1, health, u2, no_mount, driveState, u3, u4, cloak, None, inv) =>
        if(obj.vehicle_type != VehicleFormat.Normal) {
          Attempt.failure(Err("invalid vehicle data format; variable bits expected"))
        }
        else {
          Attempt.successful(basic :: u1 :: health :: u2 :: no_mount :: driveState :: u3 :: u4 :: cloak :: None :: inv :: HNil)
        }

      case _ =>
        Attempt.failure(Err("invalid vehicle data format"))
    }
  )

  implicit val codec : Codec[VehicleData] = codec(VehicleFormat.Normal)
}
