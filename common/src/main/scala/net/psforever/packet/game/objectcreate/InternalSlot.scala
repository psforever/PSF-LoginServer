// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.packet.game.PlanetSideGUID
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Similar fields as required for a formal `ObjectCreateMessage` but with a required but implicit parent relationship.
  * Specifically, the purpose of the packet is to start to define a new object within the definition of a previous object.
  * This prior object will clarify the identity of the parent object that owns the given `parentSlot`.<br>
  * <br>
  * An `InternalSlot` object is not a top-level object.
  * Extra effort should be made to ensure the user does not have to directly construct an `InternalSlot`.
  * @param objectClass the code for the type of object being constructed
  * @param guid the GUID this object will be assigned
  * @param parentSlot a parent-defined slot identifier that explains where the child is to be attached to the parent
  * @param obj the data used as representation of the object to be constructed
  * @see ObjectClass.selectDataCodec
  */
case class InternalSlot(objectClass : Int,
                        guid : PlanetSideGUID,
                        parentSlot : Int,
                        obj : ConstructorData) extends StreamBitSize {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = {
    val base : Long = if(parentSlot > 127) 43L else 35L
    base + obj.bitsize
  }
}

object InternalSlot extends Marshallable[InternalSlot] {
  implicit val codec : Codec[InternalSlot] = (
    ("objectClass" | uintL(11)) >>:~ { obj_cls =>
      ("guid" | PlanetSideGUID.codec) ::
        ("parentSlot" | PacketHelpers.encodedStringSize) ::
        ("obj" | ObjectClass.selectDataCodec(obj_cls)) //it's fine for this call to fail
      }
    ).xmap[InternalSlot] (
    {
      case cls :: guid :: slot :: Some(obj) :: HNil =>
        InternalSlot(cls, guid, slot, obj)
    },
    {
      case InternalSlot(cls, guid, slot, obj) =>
        cls :: guid :: slot :: Some(obj) :: HNil
    }
  ).as[InternalSlot]
}
