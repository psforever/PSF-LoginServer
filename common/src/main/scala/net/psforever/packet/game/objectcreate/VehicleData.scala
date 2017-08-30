// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.objectcreate.MountItem.MountItem
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of a generic vehicle, with optional mounted weapons.
  * This data will help construct most of the game's vehicular options such as the Lightning and the Harasser.<br>
  * <br>
  * Vehicles utilize their own packet to communicate position to the server, known as `VehicleStateMessage`.
  * This takes the place of `PlayerStateMessageUpstream` when the player avatar is in control;
  * and, it takes the place of `PlayerStateMessage` for other players when they are in control.
  * If the vehicle is sufficiently complicated, a `ChildObjectStateMessage` will be used.
  * This packet will control any turret(s) on the vehicle.
  * For very complicated vehicles, the packets `FrameVehicleStateMessage` and `VehicleSubStateMessage` will also be employed.
  * The tasks that these packets perform are different based on the vehicle that responds or generates them.<br>
  * <br>
  * Vehicles have a variety of features.
  * They have their own inventory space, seating space for driver and passengers, Infantry mounting positions for the former two, and weapon mounting positions.
  * Specialized vehicles also have terminals attached to them.
  * The trunk is little different from player character inventories save for capacity and that it must be manually accessed.
  * It is usually on the rear of the vehicle if that vehicle has a trunk at all.
  * Weapons and infantry are allocated mounting slots from the same list.
  * Weapons are constructed in their given slot with the vehicle itself and Infantry sit aside the weapons.
  * Certain slots ("seats") allow control of one of the weapons in another slot ("weapon mounting").
  * ("Seat" and "weapon mounting" do not coincide numerically.)
  * For trunk and for Infantry slots, various glyphs are projected onto the ground, called "mounting positions."
  * Standing nearly on top of the glyph and facing the vehicle allows access or seat-taking.
  * ("Seat" and "mounting positions" will not necessarily coincide numerically either.)<br>
  * <br>
  * Outside of managing mounted weaponry, any vehicle with special "utilities" must be handled as a special case.
  * Utilities are plastered onto the chassis and carried around with the vehicle.
  * Some vehicles have to go through a sessile physical conversion known as "deploying" to get access to their utilities.<br>
  * <br>
  * An "expected" number of mounting data can be passed into the class for the purposes of validating input.
  * @param basic data common to objects
  * @param unk1 na
  * @param health the amount of health the vehicle has, as a percentage of a filled bar
  * @param unk2 na
  * @param driveState the drivable condition
  * @param unk4 na
  * @param unk5 na;
  *             1 causes the `quadstealth` (Wraith) to cloak
  * @param mountings data regarding the mounted utilities, usually weapons
  * @param mount_capacity implicit;
  *                       the total number of mounted utilities allowed on this vehicle;
  *                       defaults to 1;
  *                       -1 or less ignores the imposed checks
  * @see `Vehicle2Data`
  */
final case class VehicleData(basic : CommonFieldData,
                             unk1 : Int,
                             health : Int,
                             unk2 : Int,
                             driveState : DriveState.Value,
                             unk4 : Boolean,
                             unk5 : Int,
                             mountings : Option[List[MountItem]] = None
                            )(implicit val mount_capacity : Int = 1) extends ConstructorData {
  override def bitsize : Long = {
    val basicSize = basic.bitsize
    val mountSize = if(mountings.isDefined) {
      var bSize : Long = 0L
      for(item <- mountings.get) {
        bSize += item.bitsize
      }
      10 + bSize
    }
    else {
      0L
    }
    3L + VehicleData.baseVehicleSize + basicSize + mountSize
  }
}

object VehicleData extends Marshallable[VehicleData] {
  val baseVehicleSize : Long = 21L //2u + 8u + 2u + 8u + 1u

  /**
    * Overloaded constructor that mandates information about a single weapon mount.
    * @param basic data common to objects
    * @param health the amount of health the object has, as a percentage of a filled bar
    * @param mount data regarding the mounted weapon
    * @return a `VehicleData` object
    */
  def apply(basic : CommonFieldData, health : Int, mount : MountItem) : VehicleData =
    VehicleData(basic, 0, health, 0, DriveState.Mobile, false, 0, Some(mount :: Nil))

  /**
    * Overloaded constructor that mandates information about a single weapon mount and deployment state.
    * @param basic data common to objects
    * @param health the amount of health the object has, as a percentage of a filled bar
    * @param driveState the drivable condition
    * @param mount data regarding the mounted weapon
    * @return a `Vehicle2Data` object
    */
  def apply(basic : CommonFieldData, health : Int, driveState : DriveState.Value, mount : MountItem) : VehicleData =
    VehicleData(basic, 0, health, 0, driveState, false, 0, Some(mount :: Nil))

  /**
    * A `Codec` for mounted utilities, generally weapons (as `WeaponData`).
    * @param mountCheck a function that takes a `List` of `InternalSlot` objects and returns `true` if those objects passed its test
    * @return a `List` of mounted objects or a `BitVector` of the same
    * @see `InventoryData`
    */
  def mountedUtilitiesCodec(mountCheck : (List[MountItem]) => Boolean) : Codec[List[MountItem]] =
    InventoryData.codec(MountItem.codec).exmap[List[MountItem]] (
      {
        case InventoryData(list) =>
          if(!mountCheck(list)) {
            Attempt.failure(Err("vehicle mount decoding is disallowed by test failure"))
          }
          else {
            Attempt.successful(list)
          }

        case _ =>
          Attempt.failure(Err("invalid mounting data format"))
      },
      {
        case list =>
          if(list.size > 255) {
            Attempt.failure(Err("vehicle encodes too many weapon mountings (255+ objects!)"))
          }
          else if(!mountCheck(list)) {
            Attempt.failure(Err("vehicle mount encoding is disallowed by test failure"))
          }
          else {
            Attempt.successful(InventoryData(list))
          }
      }
    )

  /**
    * These values are parsed by all vehicles.
    * Comments about the fields are provided where helpful.
    */
  val basic_vehicle_codec : Codec[CommonFieldData :: Int :: Int :: Int :: DriveState.Value :: Boolean :: HNil] = (
    CommonFieldData.codec :: //not certain if player_guid is valid
      uint2L :: //often paired with the assumed 16u field in previous?
      uint8L :: //usually "health"
      uint2L :: //usually 0; second bit turns off vehicle seat entry points
      DriveState.codec :: //special field (AMS and ANT use for deploy state)
      bool //unknown but generally false; can cause stream misalignment if set when unexpected
    ).as[CommonFieldData :: Int :: Int :: Int :: DriveState.Value :: Boolean :: HNil]

  /**
    * Perform an evaluation of the provided object.
    * @param list a List of objects to be compared against some criteria
    * @return `true`, if the objects pass this test; false, otherwise
    */
  def onlyWeapons(list : List[MountItem]) : Boolean = !list.exists(!_.obj.isInstanceOf[WeaponData])

  /**
    * A `Codec` for `VehicleData`.
    * @param mount_capacity the total number of mounted weapons that are attached to this vehicle;
    *                       defaults to 1
    * @param mountCheck implicit;
    *                   an evaluation of the provided `List` of objects;
    *                   a function that takes an object and returns `true` if the object passed its defined test;
    *                   defaults to `onlyWeapons`
    * @return a `VehicleData` object or a `BitVector`
    */
  def codec(mount_capacity : Int = 1)(implicit mountCheck : (List[MountItem]) => Boolean = onlyWeapons) : Codec[VehicleData] = (
    basic_vehicle_codec :+
      uint2L :+
      optional(bool, "mountings" | mountedUtilitiesCodec(mountCheck))
    ).exmap[VehicleData] (
    {
      case basic :: u1 :: health :: u2 :: driveState :: u4 :: u5 :: mountings :: HNil =>
        val onboardMountCount : Int = if(mountings.isDefined) { mountings.get.size } else { 0 }
        if(mount_capacity > -1 && mount_capacity != onboardMountCount) {
          Attempt.failure(Err(s"vehicle decodes wrong number of mounts - actual $onboardMountCount, expected $mount_capacity"))
        }
        else {
          Attempt.successful(VehicleData(basic, u1, health, u2, driveState, u4, u5, mountings)(onboardMountCount))
        }
    },
    {
      case obj @ VehicleData(basic, u1, health, u2, driveState, u4, u5, mountings) =>
        val objMountCapacity = obj.mount_capacity
        if(objMountCapacity < 0 || mount_capacity < 0) {
          Attempt.successful(basic :: u1 :: health :: u2 :: driveState :: u4 :: u5 :: mountings :: HNil)
        }
        else {
          val onboardMountCount : Int = if(mountings.isDefined) { mountings.get.size } else { 0 }
          if(mount_capacity != objMountCapacity) {
            Attempt.failure(Err(s"different encoding expectations for amount of mounts - actual $objMountCapacity, expected $mount_capacity"))
          }
          else if(mount_capacity != onboardMountCount) {
            Attempt.failure(Err(s"vehicle encodes wrong number of mounts - actual $onboardMountCount, expected $mount_capacity"))
          }
          else {
            Attempt.successful(basic :: u1 :: health :: u2 :: driveState :: u4 :: u5 :: mountings :: HNil)
          }
        }

      case _ =>
        Attempt.failure(Err("invalid vehicle data format"))
    }
  )

  implicit val codec : Codec[VehicleData] = codec()
}
