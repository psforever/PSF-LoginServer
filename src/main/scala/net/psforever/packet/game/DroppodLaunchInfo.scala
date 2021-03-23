// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Attempt.Successful
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Information related to this droppod event.
  * @see `DroppodLaunchRequestMessage`
  * @see `DroppodLaunchResponseMessage`
  * @param guid the player using the droppod
  * @param zone_number the zone to which the player desires transportation
  * @param xypos where in the zone (relative to the ground) the player will be placed
  */
final case class DroppodLaunchInfo(
                                    guid: PlanetSideGUID,
                                    zone_number: Int,
                                    xypos: Vector3
                                  )

object DroppodLaunchInfo {
  val codec: Codec[DroppodLaunchInfo] = (
    ("guid" | PlanetSideGUID.codec) ::
    ("zone_number" | uint16L) ::
    (floatL :: floatL).narrow[Vector3](
      {
        case x :: y :: HNil => Successful(Vector3(x, y, 0))
      },
      {
        case Vector3(x, y, _) => x :: y :: HNil
      }
    )
    ).as[DroppodLaunchInfo]
}
