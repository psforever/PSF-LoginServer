// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.packet.game.PlanetSideGUID
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * An intermediate class for the primary fields of `ObjectCreateMessage` with an implicit parent-child relationship.<br>
  * <br>
  * Any object that is contained in a "slot" of another object will use `InternalSlot` to hold the anchoring data.
  * This prior object will clarify the identity of the "parent" object that owns the given `parentSlot`.<br>
  * <br>
  * Try to avoid exposing `InternalSlot` in the process of implementing code.
  * @param objectClass the code for the type of object being constructed
  * @param guid the GUID this object will be assigned
  * @param parentSlot a parent-defined slot identifier that explains where the child is to be attached to the parent
  * @param obj the data used as representation of the object to be constructed
  * @see ObjectClass.selectDataCodec
  */
final case class InternalSlot(objectClass : Int,
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
