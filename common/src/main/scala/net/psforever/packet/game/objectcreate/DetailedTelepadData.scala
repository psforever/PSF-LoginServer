// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the telepad portion of `ObjectCreateDetailedMessage` packet data.
  * This data will help construct the "cosntruction tool"
  * that can be obtained from the Router vehicle - the Router telepad.
  * It issued to construct a bidirectional teleportation point associated with a Router if that Router is deployed.
  * @param unk na
  * @param router_guid the Router
  */
final case class DetailedTelepadData(unk : Int, router_guid : Option[PlanetSideGUID]) extends ConstructorData {
  override def bitsize : Long = {
    val rguidSize = if(router_guid.nonEmpty) 16 else 0
    51L + rguidSize
  }
}

object DetailedTelepadData extends Marshallable[DetailedTelepadData] {
  def apply(unk : Int) : DetailedTelepadData = DetailedTelepadData(unk, None)

  def apply(unk : Int, router_guid : PlanetSideGUID) : DetailedTelepadData = DetailedTelepadData(unk, Some(router_guid))

  implicit val codec : Codec[DetailedTelepadData] = (
    ("unk" | uint(6)) ::
      optional(bool, "router_guid" | PlanetSideGUID.codec) ::
      uint(24) ::
      uint(18) ::
      uint2
    ).exmap[DetailedTelepadData] (
    {
      case unk :: rguid :: 1 :: 1 :: 0 :: HNil =>
        Attempt.successful(DetailedTelepadData(unk, rguid))
      case _ =>
        Attempt.failure(Err("invalid detailed telepad format"))
    },
    {
      case DetailedTelepadData(unk, rguid) =>
        Attempt.successful(unk :: rguid :: 1 :: 1 :: 0 :: HNil)
    }
  )
}
