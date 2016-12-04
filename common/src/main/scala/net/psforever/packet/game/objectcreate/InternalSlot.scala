// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.packet.game.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * The same kind of data as required for a formal ObjectCreateMessage but with a required and implicit parent relationship.
  * Data preceding this entry will define the existence of the parent.
  * @param objectClass na
  * @param guid na
  * @param parentSlot na
  * @param obj na
  */
case class InternalSlot(objectClass : Int,
                        guid : PlanetSideGUID,
                        parentSlot : Int,
                        obj : Option[ConstructorData])

object InternalSlot extends Marshallable[InternalSlot] {
  type objPattern = Int :: PlanetSideGUID :: Int :: Option[ConstructorData] :: HNil

  implicit val codec : Codec[InternalSlot] = (
    ignore(1) :: //TODO determine what this bit does
      ("objectClass" | uintL(11)) ::
      ("guid" | PlanetSideGUID.codec) ::
      ("parentSlot" | PacketHelpers.encodedStringSize) ::
      bits
    ).exmap[objPattern] (
    {
      case _ :: cls :: guid :: slot :: data :: HNil =>
        Attempt.successful(cls :: guid :: slot :: Mold.decode(cls, data) :: HNil)
    },
    {
      case cls :: guid :: slot :: None :: HNil =>
        Attempt.failure(Err("no constructor data could be found"))
      case cls :: guid :: slot :: mold :: HNil =>
        Attempt.successful(() :: cls :: guid :: slot :: Mold.encode(cls, mold.get) :: HNil)
    }
  ).exmap[objPattern] (
    {
      case cls :: guid :: slot :: None :: HNil =>
        Attempt.failure(Err("no decoded constructor data"))
      case cls :: guid :: slot :: mold :: HNil =>
        Attempt.successful(cls :: guid :: slot :: mold :: HNil)
    },
    {
      case cls :: guid :: slot :: BitVector.empty :: HNil =>
        Attempt.failure(Err("no encoded constructor data"))
      case cls :: guid :: slot :: data :: HNil =>
        Attempt.successful(cls :: guid :: slot :: data :: HNil)
    }
  ).as[InternalSlot]
}
