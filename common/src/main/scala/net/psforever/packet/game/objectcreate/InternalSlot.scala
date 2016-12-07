// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.packet.game.PlanetSideGUID
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * The same kind of data as required for a formal `ObjectCreateMessage` but with a required and implicit parent relationship.
  * Some data preceding this entry will clarify the existence of the parent.<br>
  * <br>
  * As indicated, an `InternalSlot` object is not a top-level object.
  * This is true in relation between one object and another, as well as in how this object is sorted in the `ObjectCreateMessage` data.
  * The data outlined by this class encompasses the same kind as the outer-most `ObjectCreateMessage`.
  * By contrast, this object always has a dedicated parent object and a known slot to be attached to that parent.
  * It's not optional.
  * @param objectClass the code for the type of object being constructed
  * @param guid the GUID this object will be assigned
  * @param parentSlot a parent-defined slot identifier that explains where the child is to be attached to the parent
  * @param obj the data used as representation of the object to be constructed
  */
case class InternalSlot(objectClass : Int,
                        guid : PlanetSideGUID,
                        parentSlot : Int,
                        obj : ConstructorData) {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  def bitsize : Long = {
    val first : Long = if(parentSlot > 127) 44L else 36L
    val second : Long = obj.bitsize
    first + second
  }
}

object InternalSlot extends Marshallable[InternalSlot] {
  implicit val codec : Codec[InternalSlot] = (
    ignore(1) :: //TODO determine what this bit does
      (("objectClass" | uintL(11)) >>:~ { obj_cls =>
        ("guid" | PlanetSideGUID.codec) ::
          ("parentSlot" | PacketHelpers.encodedStringSize) ::
          ("obj" | ObjectClass.selectDataCodec(obj_cls))
      })
    ).xmap[InternalSlot] (
    {
      case _ :: cls :: guid :: slot :: Some(obj) :: HNil =>
        InternalSlot(cls, guid, slot, obj)
    },
    {
      case InternalSlot(cls, guid, slot, obj) =>
        () :: cls :: guid :: slot :: Some(obj) :: HNil
    }
  ).as[InternalSlot]
}
