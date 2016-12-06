// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.packet.game.PlanetSideGUID
import scodec.Codec
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
                        obj : Option[ConstructorData]) {
  def bsize : Long = {
    val first : Long = if(parentSlot > 127) 44L else 36L
    val second : Long = if(obj.isDefined) obj.get.bsize else 0L
    first + second
  }
}

object InternalSlot extends Marshallable[InternalSlot] {
  type objPattern = Int :: PlanetSideGUID :: Int :: ConstructorData :: HNil

  implicit val codec : Codec[InternalSlot] = (
    ignore(1) :: //TODO determine what this bit does
      (("objectClass" | uintL(11)) >>:~ { obj_cls =>
        ("guid" | PlanetSideGUID.codec) ::
          ("parentSlot" | PacketHelpers.encodedStringSize) ::
          ("obj" | ObjectClass.selectDataCodec(obj_cls))
      })
    ).as[InternalSlot]
}