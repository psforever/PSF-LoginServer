// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of a vehicle called the Advanced Nanite Transport (ANT).
  * @param basic data common to objects
  * @param unk1 na
  * @param health the amount of health the object has, as a percentage of a filled bar
  * @param unk2 na
  * @param driveState the drivable condition;
  *                    defaults to `Mobile`
  * @param unk3 na;
  *             defaults to 0
  */
final case class ANTData(basic : CommonFieldData,
                         unk1 : Int,
                         health : Int,
                         unk2 : Int,
                         driveState : DriveState.Value = DriveState.Mobile,
                         unk3 : Int = 0
                        ) extends ConstructorData {
  override def bitsize : Long = {
    val basicSize = basic.bitsize
    val vehicleBasicSize : Long = VehicleData.baseVehicleSize
    9L + basicSize + vehicleBasicSize
  }
}

object ANTData extends Marshallable[ANTData] {
  /**
    * Overloaded constructor.
    * @param basic data common to objects
    * @param health the amount of health the object has, as a percentage of a filled bar
    * @param driveState the drivable condition
    * @return an `ANTData` object
    */
  def apply(basic : CommonFieldData, health : Int, driveState : DriveState.Value) : ANTData =
    new ANTData(basic, 0, health, 0, driveState, 0)

  implicit val codec : Codec[ANTData] = (
    VehicleData.basic_vehicle_codec :+
      uint8L :+
      bool //false for vehicle driving control; ditto u4 from above
    ).exmap[ANTData] (
    {
      case basic :: unk1 :: health :: unk2 :: driveState :: false :: unk3 :: false :: HNil =>
        Attempt.successful(ANTData(basic, unk1, health, unk2, driveState, unk3))

      case _ =>
        Attempt.failure(Err("invalid ant data format"))
    },
    {
      case ANTData(basic, unk1, health, unk2, driveState, unk3) =>
        Attempt.successful(basic :: unk1 :: health :: unk2 :: driveState :: false :: unk3 :: false :: HNil)
    }
  )
}
