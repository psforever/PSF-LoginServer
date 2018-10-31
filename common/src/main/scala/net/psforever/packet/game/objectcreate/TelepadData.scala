// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the telepad portion of `ObjectCreateMessage` packet data.
  * This data will help construct the "cosntruction tool"
  * that can be obtained from the Router vehicle - the Router telepad.
  * It issued to construct a bidirectional teleportation point associated with a Router if that Router is deployed.
  * @param unk na
  * @param router_guid the Router
  */
final case class TelepadData(unk : Int, router_guid : Option[PlanetSideGUID]) extends ConstructorData {
  override def bitsize : Long = {
    val rguidSize = if(router_guid.nonEmpty) 16 else 0
    34L + rguidSize
  }
}

object TelepadData extends Marshallable[TelepadData] {
  def apply(unk : Int) : TelepadData = TelepadData(unk, None)

  def apply(unk : Int, router_guid : PlanetSideGUID) : TelepadData = TelepadData(unk, Some(router_guid))

  implicit val codec : Codec[TelepadData] = (
    ("unk" | uint(6)) ::
      optional(bool, "router_guid" | PlanetSideGUID.codec) ::
      uint(27)
    ).exmap[TelepadData] (
    {
      case unk :: rguid :: 0 :: HNil =>
        Attempt.successful(TelepadData(unk, rguid))
      case _ =>
        Attempt.failure(Err("invalid telepad format"))
    },
    {
      case TelepadData(unk, rguid) =>
        Attempt.successful(unk :: rguid :: 0 :: HNil)
    }
  )
}
