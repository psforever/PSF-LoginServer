// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.objectcreate.MountItem.MountItem
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of a generic vehicle, with optional mounted weapons.
  * This data will help construct vehicular options such as the Switchblade and the Mosquito.
  * @param basic data common to objects
  * @param unk1 na
  * @param health the amount of health the vehicle has, as a percentage of a filled bar
  * @param unk2 na
  * @param driveState the drivable condition
  * @param unk4 na
  * @param unk5 na
  * @param mountings data regarding the mounted utilities, usually weapons
  * @param mount_capacity implicit;
  *                       the total number of mounted utilities allowed on this vehicle;
  *                       defaults to 1;
  *                       -1 or less ignores the imposed checks
  * @see `VehicleData`
  */
final case class Vehicle2Data(basic : CommonFieldData,
                              unk1 : Int,
                              health : Int,
                              unk2 : Int,
                              driveState : DriveState.Value,
                              unk4 : Boolean,
                              unk5 : Int,
                              unk6 : Int,
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
    11L + VehicleData.baseVehicleSize + basicSize + mountSize
  }
}

object Vehicle2Data extends Marshallable[Vehicle2Data] {
  /**
    * Overloaded constructor that mandates information about a single weapon mount.
    * @param basic data common to objects
    * @param health the amount of health the object has, as a percentage of a filled bar
    * @param mount data regarding the mounted weapon
    * @return a `Vehicle2Data` object
    */
  def apply(basic : CommonFieldData, health : Int, mount : MountItem) : Vehicle2Data =
    Vehicle2Data(basic, 0, health, 0, DriveState.Mobile, false, 0, 0, Some(mount :: Nil))

  /**
    * Overloaded constructor that mandates information about a single weapon mount and deployment state.
    * @param basic data common to objects
    * @param health the amount of health the object has, as a percentage of a filled bar
    * @param driveState the drivable condition
    * @param mount data regarding the mounted weapon
    * @return a `Vehicle2Data` object
    */
  def apply(basic : CommonFieldData, health : Int, driveState : DriveState.Value, mount : MountItem) : Vehicle2Data =
    Vehicle2Data(basic, 0, health, 0, driveState, false, 0, 0, Some(mount :: Nil))

  /**
    * A `Codec` for `Vehicle2Data`.
    * @param mount_capacity the total number of mounted weapons that are attached to this vehicle;
    *                       defaults to 1
    * @param mountCheck implicit;
    *                   an evaluation of the provided `List` of objects;
    *                   a function that takes an object and returns `true` if the object passed its defined test;
    *                   defaults to `onlyWeapons`
    * @return a `VehicleData` object or a `BitVector`
    */
  def codec(mount_capacity : Int = 1)(implicit mountCheck : (List[MountItem]) => Boolean = VehicleData.onlyWeapons) : Codec[Vehicle2Data] = (
    VehicleData.basic_vehicle_codec :+
      uint8L :+
      uint2L :+
      optional(bool, "mountings" | VehicleData.mountedUtilitiesCodec(mountCheck))
    ).exmap[Vehicle2Data] (
    {
      case basic :: u1 :: health :: u2 :: driveState :: u4 :: u5 :: u6 :: mountings :: HNil =>
        val onboardMountCount : Int = if(mountings.isDefined) { mountings.get.size } else { 0 }
        if(mount_capacity > -1 && mount_capacity != onboardMountCount) {
          Attempt.failure(Err(s"vehicle decodes wrong number of mounts - actual $onboardMountCount, expected $mount_capacity"))
        }
        else {
          Attempt.successful(Vehicle2Data(basic, u1, health, u2, driveState, u4, u5, u6, mountings)(onboardMountCount))
        }

      case _ =>
        Attempt.failure(Err("invalid vehicle data format"))
    },
    {
      case obj @ Vehicle2Data(basic, u1, health, u2, driveState, u4, u5, u6, mountings) =>
        val objMountCapacity = obj.mount_capacity
        if(objMountCapacity < 0 || mount_capacity < 0) {
          Attempt.successful(basic :: u1 :: health :: u2 :: driveState :: u4 :: u5 :: u6 :: mountings :: HNil)
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
            Attempt.successful(basic :: u1 :: health :: u2 :: driveState :: u4 :: u5 :: u6 :: mountings :: HNil)
          }
        }

      case _ =>
        Attempt.failure(Err("invalid vehicle data format"))
    }
  )

  implicit val codec : Codec[Vehicle2Data] = codec()
}
