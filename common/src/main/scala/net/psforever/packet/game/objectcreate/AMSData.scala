// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of a vehicle called the Advanced Mobile Station (AMS).<br>
  * <br>
  * The AMS has four utilities associated with its `Deployed` mode.
  * It has two flanking equipment terminals, a front matrix panel, and a rear deconstruction terminal.
  * This is consistent from AMS to AMS, regardless of the faction that spawned the vehicle originally.
  * For that reason, the only thing that changes between different AMS's are the GUIDs used for each terminal.
  * @param basic data common to objects
  * @param unk1 na
  * @param health the amount of health the object has, as a percentage of a filled bar
  * @param unk2 na
  * @param driveState the drivable condition
  * @param unk3 na;
  *             common values are 0 or 63;
  *             usually in a non-`Mobile` state when non-zero
  * @param matrix_guid the GUID for the spawn matrix panel on the front
  * @param respawn_guid the GUID for the respawn apparatus on the rear
  * @param term_a_guid the GUID for the equipment terminal on the AMS on the left side
  * @param term_b_guid the GUID for the equipment on the AMS on the right side
  */
final case class AMSData(basic : CommonFieldData,
                         unk1 : Int,
                         health : Int,
                         unk2 : Int,
                         driveState : DriveState.Value,
                         unk3 : Int,
                         matrix_guid : PlanetSideGUID,
                         respawn_guid : PlanetSideGUID,
                         term_a_guid : PlanetSideGUID,
                         term_b_guid : PlanetSideGUID
                        ) extends ConstructorData {
  override def bitsize : Long = {
    val basicSize = basic.bitsize
    val vehicleSize : Long = VehicleData.baseVehicleSize
    //the four utilities should all be the same size
    val utilitySize : Long = 4 * InternalSlot(ObjectClass.matrix_terminalc, matrix_guid, 1, CommonTerminalData(basic.faction)).bitsize
    19L + basicSize + vehicleSize + utilitySize
  }
}

object AMSData extends Marshallable[AMSData] {
  /**
    * Overloaded constructor that ignores all of the unknown fields.
    * @param basic data common to objects
    * @param health the amount of health the object has, as a percentage of a filled bar
    * @param driveState the drivable condition
    * @param matrix_guid the GUID for the spawn matrix panel on the front
    * @param respawn_guid the GUID for the respawn apparatus on the rear
    * @param term_a_guid the GUID for the equipment terminal on the AMS on the left side
    * @param term_b_guid the GUID for the equipment on the AMS on the right side
    * @return an `AMSData` object
    */
  def apply(basic : CommonFieldData, health : Int, driveState : DriveState.Value, matrix_guid : PlanetSideGUID, respawn_guid : PlanetSideGUID, term_a_guid : PlanetSideGUID, term_b_guid : PlanetSideGUID) : AMSData =
    new AMSData(basic, 0, health, 0, driveState, 0, matrix_guid, respawn_guid, term_a_guid, term_b_guid)

  implicit val codec : Codec[AMSData] = (
    VehicleData.basic_vehicle_codec :+
      uintL(6) :+
      bool :+
      uintL(12) :+
      InternalSlot.codec :+
      InternalSlot.codec :+
      InternalSlot.codec :+
      InternalSlot.codec
    ).exmap[AMSData] (
    {
      case basic :: unk1 :: health :: unk2 :: driveState :: false :: unk3 :: false :: 0x41 ::
        InternalSlot(ObjectClass.matrix_terminalc, matrix_guid, 1, CommonTerminalData(_, _)) ::
        InternalSlot(ObjectClass.ams_respawn_tube, respawn_guid,2, CommonTerminalData(_, _)) ::
        InternalSlot(ObjectClass.order_terminala,  terma_guid,  3, CommonTerminalData(_, _)) ::
        InternalSlot(ObjectClass.order_terminalb,  termb_guid,  4, CommonTerminalData(_, _)) :: HNil =>
        Attempt.successful(AMSData(basic, unk1, health, unk2, driveState, unk3, matrix_guid, respawn_guid, terma_guid, termb_guid))

      case _ =>
        Attempt.failure(Err("invalid AMS data"))
    },
    {
      case AMSData(basic, unk1, health, unk2, driveState, unk3, matrix_guid, respawn_guid, terma_guid, termb_guid) =>
        val faction : PlanetSideEmpire.Value = basic.faction
        Attempt.successful(
          basic :: unk1 :: health :: unk2 :: driveState :: false :: unk3 :: false :: 0x41 ::
            InternalSlot(ObjectClass.matrix_terminalc, matrix_guid, 1, CommonTerminalData(faction)) ::
            InternalSlot(ObjectClass.ams_respawn_tube, respawn_guid,2, CommonTerminalData(faction)) ::
            InternalSlot(ObjectClass.order_terminala,  terma_guid,  3, CommonTerminalData(faction)) ::
            InternalSlot(ObjectClass.order_terminalb,  termb_guid,  4, CommonTerminalData(faction)) :: HNil
        )
    }
  )
}
