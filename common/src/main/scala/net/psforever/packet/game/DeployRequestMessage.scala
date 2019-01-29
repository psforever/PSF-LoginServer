// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{DriveState, Vector3}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client when the player attempts to deploy a vehicle.
  * Dispatched by the server to cause a specific vehicle to be deployed.<br>
  * <br>
  * "Deployment" usually isn't enough by itself.
  * It only changes the physical configuration of the vehicle.
  * (It's an animation request/trigger?)
  * Anything that can be "deployed" does so for a very specific reason, to perform a complex function.
  * These functions are not immediately available.
  * Attributes must be set properly for the transition between behaviors to occur properly.
  * In addition, the recently-deployed vehicles will hang in a state of limbo if not configured properly.
  * It will not even dispatch an un-deploy request upon command in this state.
  * <br>
  * This packet has nothing to do with ACE deployables.
  * @param player_guid the player requesting the deployment
  * @param vehicle_guid the vehicle to be deployed
  * @param deploy_state either requests for a specific deployment state or assignment of the requested state
  * @param unk2 na;
  *             usually 0
  * @param unk3 na
  * @param pos the position where the object will deploy itself
  */
final case class DeployRequestMessage(player_guid : PlanetSideGUID,
                                      vehicle_guid : PlanetSideGUID,
                                      deploy_state : DriveState.Value,
                                      unk2 : Int,
                                      unk3 : Boolean,
                                      pos : Vector3)
  extends PlanetSideGamePacket {
  type Packet = DeployRequestMessage
  def opcode = GamePacketOpcode.DeployRequestMessage
  def encode = DeployRequestMessage.encode(this)
}

object DeployRequestMessage extends Marshallable[DeployRequestMessage] {
  private val driveState3u = uint(3).xmap[DriveState.Value] (
    n => DriveState(n),
    n => {
      if(n.id > 7) {
        0
      }
      else {
        n.id
      }
    }
  )

  implicit val codec : Codec[DeployRequestMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("vehicle_guid" | PlanetSideGUID.codec) ::
      ("deploy_state" | driveState3u) ::
      ("unk2" | uint(5)) ::
      ("unk3" | bool) ::
      ("pos" | Vector3.codec_pos)
    ).as[DeployRequestMessage]
}
