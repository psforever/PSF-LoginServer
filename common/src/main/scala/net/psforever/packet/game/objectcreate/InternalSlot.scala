// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.PacketHelpers
import net.psforever.packet.game.PlanetSideGUID
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * An intermediate class for the primary fields of `ObjectCreate*Message` with an implicit parent-child relationship.<br>
  * <br>
  * Any object that is contained in a "slot" of another object will use `InternalSlot` to hold the anchoring data.
  * This prior object will clarify the identity of the "parent" object that owns the given `parentSlot`.
  * As the name implies, this should never have to be used in the representation of a non-child object.<br>
  * <br>
  * Try to avoid exposing this class in the process of implementing common object code.
  * Provide overrode constructors that mask the creation of `InternalSlot` where applicable.
  * @param objectClass the code for the type of object being constructed
  * @param guid the GUID this object will be assigned
  * @param parentSlot a parent-defined slot identifier that explains where the child is to be attached to the parent
  * @param obj the data used as representation of the object to be constructed
  * @see `ObjectClass.selectDataCodec`
  * @see `ObjectClass.selectDataDetailedCodec`
  */
final case class InternalSlot(objectClass : Int,
                              guid : PlanetSideGUID,
                              parentSlot : Int,
                              obj : ConstructorData) extends StreamBitSize {
  override def bitsize : Long = {
    val base : Long = if(parentSlot > 127) 43L else 35L
    base + obj.bitsize
  }
}

object InternalSlot {
  /**
    * Used for `0x18` `ObjectCreateDetailedMessage` packets
    */
  val codec_detailed : Codec[InternalSlot] = (
    ("objectClass" | uintL(11)) >>:~ { obj_cls =>
      ("guid" | PlanetSideGUID.codec) ::
        ("parentSlot" | PacketHelpers.encodedStringSize) ::
        ("obj" | ObjectClass.selectDataDetailedCodec(obj_cls)) //it's fine for this call to fail
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
  )

  /**
    * Used for `0x17` `ObjectCreateMessage` packets
    */
  val codec : Codec[InternalSlot] = (
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
  )
}
