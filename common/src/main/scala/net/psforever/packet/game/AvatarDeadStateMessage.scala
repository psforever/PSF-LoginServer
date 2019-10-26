// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideEmpire, Vector3}
import scodec.Attempt.{Failure, Successful}
import scodec.{Codec, Err}
import scodec.codecs._

/**
  * An `Enumeration` of the various states a `Player` may possess in the cycle of nanite life and death.
  */
object DeadState extends Enumeration {
  type Type = Value

  val
  Alive,
  Dead,
  Release,
  RespawnTime
    = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uintL(3))
}

/**
  * Dispatched by the server to manipulate the client's management of the `Player` object owned by the user as his "avatar."<br>
  * <br>
  * The cycle of a player is generally `Alive` to `Dead` and `Dead` to `Release` and `Release` to `RespawnTimer` to `Alive`.
  * When deconstructing oneself, the user makes a jump between `Alive` and `Release`;
  * and, he may make a further jump from `Release` to `Alive` depending on spawning choices.
  * Being `Alive` is the most common state.
  * (Despite what anyone says.)
  * Being `Dead` is just a technical requirement to initialize the revive timer.
  * The player should be sufficiently "dead" by having his health points decreased to zero.
  * If the timer is reduced to zero, the player is sent back to their faction-appropriate sanctuary continent.<br>
  * <br>
  * `Release` causes a "dead" player to have its character model converted into a backpack or a form of pastry.
  * This cancels the revival timer - the player may no longer be revived - and brings the user to the deployment map.
  * From the deployment map, the user may select a place where they may respawn a new character.
  * The options available form this spawn are not only related to the faction affinity of the bases compared to the user's player(s)
  * but also to the field `faction` as is provided in the packet.
  * If the player is converted to a state of `Release` while being alive, the deployment map is still displayed.
  * Their character model is not replaced by a backpack or pastry.<br>
  * <br>
  * `RespawnTimer` is like `Dead` as it is just a formal distinction to cause the client to display a timer.
  * The state indicates that the player is being resurrected at a previously-selected location in the state `Alive`.
  * @param state avatar's mortal relationship with the world;
  *              the following timers are applicable during `Death` and `RespawnTimer`;
  *              `faction` is applicable mainly during `Release`
  * @param timer_max total length of respawn countdown, in milliseconds
  * @param timer initial length of the respawn timer, in milliseconds
  * @param pos player's last position
  * @param faction spawn points available for this faction on redeployment map
  * @param unk5 na
  */
final case class AvatarDeadStateMessage(state : DeadState.Value,
                                        timer_max : Long,
                                        timer : Long,
                                        pos : Vector3,
                                        faction : PlanetSideEmpire.Value,
                                        unk5 : Boolean)
  extends PlanetSideGamePacket {
  type Packet = AvatarDeadStateMessage
  def opcode = GamePacketOpcode.AvatarDeadStateMessage
  def encode = AvatarDeadStateMessage.encode(this)
}

object AvatarDeadStateMessage extends Marshallable[AvatarDeadStateMessage] {
  /**
    * allocate all values from the `PlanetSideEmpire` `Enumeration`
    */
  private val factionLongValues = PlanetSideEmpire.values map { _.id.toLong }

  /**
    * `Codec` for converting between the limited `PlanetSideEmpire` `Enumeration` and a `Long` value.
    */
  private val factionLongCodec = uint32L.exmap[PlanetSideEmpire.Value] (
    fv =>
      if(factionLongValues.contains(fv)) {
        Successful(PlanetSideEmpire(fv.toInt))
      }
      else {
        Failure(Err(s"$fv is not mapped to a PlanetSideEmpire value"))
      },
    f =>
      Successful(f.id.toLong)
  )

  implicit val codec : Codec[AvatarDeadStateMessage] = (
    ("state" | DeadState.codec) ::
      ("timer_max" | uint32L) ::
      ("timer" | uint32L) ::
      ("pos" | Vector3.codec_pos) ::
      ("faction" | factionLongCodec) ::
      ("unk5" | bool)
    ).as[AvatarDeadStateMessage]
}
