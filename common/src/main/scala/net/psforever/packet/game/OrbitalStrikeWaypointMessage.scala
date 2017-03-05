// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * The position of a waypoint in the game world.
  * Only two coordinates are required as the beam travels from a specific height to ground level.
  * @param x the x-coordinate of the waypoint
  * @param y the y-coordinate of the waypoint
  */
final case class Waypoint(x : Float,
                          y : Float)

/**
  * Dispatched by the server to tell the client to display an orbital strike waypoint somewhere in the game world.<br>
  * <br>
  * Waypoints are kept unique by the `guid` that is passed with them.
  * To clear a waypoint is to pass the another packet to the client with the same GUID but with no coordinates.
  * Passing new coordinates with that GUID will update the position of the indicated waypoint.
  * If the GUID sent with the packet belongs to the client's avatar that player will be given text overlay instructions:<br>
  * "Press the fire key or button to launch an orbital strike at the waypoint."<br>
  * The text will fade shortly after the waypoint has been cleared.<br>
  * <br>
  * All `OrbitalStrikeWaypointMessage` packets sent to a client will create a waypoint that will be seen by that client.
  * All rendered waypoints, regardless of the users who summoned them, will be seen in the faction color of the client's avatar.
  * (Black OPs orbital strike waypoints are green, as expected.)
  * The server should not notify the wrong clients about another faction's prepared orbital strikes;
  * however, even if it did, those beams would be seen as a same-faction's marker.
  * @param guid coordinates used to identify the waypoint;
  *             ostensibly, the GUID of the player who placed the waypoint
  * @param coords the coordinates of the waypoint;
  *               `None` if clearing a waypoint (use the same `guid` as to create it)
  */
final case class OrbitalStrikeWaypointMessage(guid : PlanetSideGUID,
                                              coords : Option[Waypoint] = None)
  extends PlanetSideGamePacket {
  type Packet = OrbitalStrikeWaypointMessage
  def opcode = GamePacketOpcode.OrbitalStrikeWaypointMessage
  def encode = OrbitalStrikeWaypointMessage.encode(this)
}

object OrbitalStrikeWaypointMessage extends Marshallable[OrbitalStrikeWaypointMessage] {
  /**
    * An abbreviated constructor for creating `OrbitalStrikeWaypointMessage`, assuming mandatory coordinates.
    * @param guid na
    * @param x the x-coordinate of the waypoint
    * @param y the y-coordinate of the waypoint
    * @return an `OrbitalStrikeWaypointMessage` object
    */
  def apply(guid : PlanetSideGUID, x : Float, y : Float) : OrbitalStrikeWaypointMessage =
    new OrbitalStrikeWaypointMessage(guid, Option(Waypoint(x, y)))

  /**
    * A `Codec` for recording the two coordinates of the waypoint map position, if they are present.
    */
  private val coords_value : Codec[Waypoint] = (
    ("x" | newcodecs.q_float(0.0, 8192.0, 20)) ::
      ("y" | newcodecs.q_float(0.0, 8192.0, 20))
    ).xmap[Waypoint] (
    {
      case x :: y :: HNil =>
        Waypoint(x, y)
    },
    {
      case Waypoint(x, y) =>
        x :: y :: HNil
    }
  )

  implicit val codec : Codec[OrbitalStrikeWaypointMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
      optional(bool, coords_value)
    ).xmap[OrbitalStrikeWaypointMessage] (
    {
      case u :: coords :: HNil =>
        OrbitalStrikeWaypointMessage(u, coords)
    },
    {
      case OrbitalStrikeWaypointMessage(u, coords) =>
        u :: coords :: HNil
    }
  )
}
