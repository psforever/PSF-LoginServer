// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class XY(x : Float,
                    y : Float)

final case class OrbitalStrikeWaypointMessage(unk : PlanetSideGUID,
                                              coords : Option[XY] = None)
  extends PlanetSideGamePacket {
  type Packet = OrbitalStrikeWaypointMessage
  def opcode = GamePacketOpcode.OrbitalStrikeWaypointMessage
  def encode = OrbitalStrikeWaypointMessage.encode(this)
}

object OrbitalStrikeWaypointMessage extends Marshallable[OrbitalStrikeWaypointMessage] {
  def apply(player_guid : PlanetSideGUID, coords : XY) : OrbitalStrikeWaypointMessage =
    new OrbitalStrikeWaypointMessage(player_guid, Option(coords))

  private val coords_value : Codec[XY] = (
    ("x" | newcodecs.q_float(0.0, 8192.0, 20)) ::
      ("y" | newcodecs.q_float(0.0, 8192.0, 20))
    ).xmap[XY] (
    {
      case x :: y :: HNil =>
        XY(x, y)
    },
    {
      case XY(x, y) =>
        x :: y :: HNil
    }
  )

  implicit val codec : Codec[OrbitalStrikeWaypointMessage] = (
    ("unk" | PlanetSideGUID.codec) ::
      (bool >>:~ { test =>
        conditional(test, coords_value).hlist
      })
    ).xmap[OrbitalStrikeWaypointMessage] (
    {
      case u :: _ :: coords :: HNil =>
        OrbitalStrikeWaypointMessage(u, coords)
    },
    {
      case OrbitalStrikeWaypointMessage(u, coords) =>
        u :: coords.isDefined :: coords :: HNil
    }
  )
}
