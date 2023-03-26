package net.psforever.tools.client

import enumeratum.{Enum, EnumEntry}
import net.psforever.packet.PlanetSidePacket
import net.psforever.packet.control.ServerStart
import net.psforever.packet.crypto.ServerFinished
import net.psforever.packet.game.{
  AvatarDeadStateMessage,
  CharacterInfoMessage,
  DeadState,
  LoginRespMessage,
  ObjectCreateDetailedMessage,
  PlayerStateMessage,
  SetCurrentAvatarMessage,
  VNLWorldStatusMessage,
  WorldInformation
}
import net.psforever.tools.client.State.{Avatar, Connection}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}

object State {
  sealed trait Connection extends EnumEntry
  object Connection extends Enum[Connection] {
    case object Disconnected    extends Connection
    case object CryptoSetup     extends Connection
    case object Login           extends Connection
    case object WorldSelection  extends Connection
    case object AvatarSelection extends Connection
    //case object AvatarCreation  extends Connection

    val values: IndexedSeq[Connection] = findValues
  }

  case class Avatar(
      guid: Option[PlanetSideGUID] = None,
      state: Option[DeadState.Value] = None,
      position: Option[Vector3] = None,
      faction: Option[PlanetSideEmpire.Value] = None,
      crouching: Boolean = false,
      velocity: Option[Vector3] = None,
      yaw: Float = 0,
      pitch: Float = 0,
      yawUpper: Float = 0,
      jumping: Boolean = false,
      cloaked: Boolean = false
  ) {
    def update(packet: PlanetSidePacket): Avatar = {
      packet match {
        case SetCurrentAvatarMessage(guid, _, _) => this.copy(guid = Some(guid))
        case AvatarDeadStateMessage(state, _, _, pos, faction, _) =>
          this.copy(
            state = Some(state),
            position = Some(pos),
            faction = Some(faction)
          )
        // doesn't look like PlayerStateMessage is sent for own avatar
        //case PlayerStateMessage(guid, pos, vel, yaw, pitch, yawUpper, _, crouching, jumping, _, cloaked)
        //    if this.guid.contains(guid) =>
        //  this.copy(
        //    position = Some(pos),
        //    velocity = vel,
        //    crouching = Some(crouching),
        //    jumping = Some(jumping),
        //    cloaked = Some(cloaked),
        //   yaw = Some(yaw),
        //   pitch = Some(pitch),
        //   yawUpper = Some(yawUpper)
        // )

        case _ => this
      }
    }

  }
}

case class State(
    connection: Connection = Connection.Disconnected,
    worlds: Seq[WorldInformation] = Seq(),
    token: Option[String] = None,
    objects: Seq[Integer] = Seq(),
    characters: Seq[CharacterInfoMessage] = Seq(),
    avatar: Avatar = Avatar()
) {
  def update(packet: PlanetSidePacket): State = {
    (packet match {
      case ServerStart(_, _)                                       => this.copy(connection = Connection.CryptoSetup)
      case ServerFinished(_)                                       => this.copy(connection = Connection.Login)
      case LoginRespMessage(token, _, _, _, _, _, _)               => this.copy(token = Some(token))
      case VNLWorldStatusMessage(_, worlds)                        => this.copy(worlds = worlds, connection = Connection.WorldSelection)
      case ObjectCreateDetailedMessage(_, objectClass, guid, _, _) => this.copy(objects = objects ++ Seq(guid.guid))
      case message @ CharacterInfoMessage(_, _, _, _, finished, _) =>
        // if finished is true, it is not real character but rather signal that list is complete
        if (finished) {
          this.copy(connection = Connection.AvatarSelection)
        } else {
          this.copy(characters = characters ++ Seq(message), connection = Connection.AvatarSelection)
        }

      case _ => this
    }).copy(avatar = avatar.update(packet))

  }
}
