// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of a class of weapons that can be created using `ObjectCreateMessage` packet data.
  * A "concurrent feed weapon" refers to a weapon system that can chamber multiple types of ammunition simultaneously.
  * This data will help construct a "weapon" such as a Punisher.<br>
  * <br>
  * The data for the weapons nests information for the default (current) type and number of ammunition in its magazine.
  * This ammunition data essentially is the weapon's magazines as numbered slots.
  * @param unk na
  * @param ammo `List` data regarding the currently loaded ammunition types and quantities
  * @see WeaponData
  * @see AmmoBoxData
  */
final case class ConcurrentFeedWeaponData(unk : Int,
                                          ammo : List[InternalSlot]) extends ConstructorData {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @see InternalSlot.bitsize
    * @see AmmoBoxData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = {
    var bitsize : Long = 0L
    for(o <- ammo) {
      bitsize += o.bitsize
    }
    61L + bitsize
  }
}

object ConcurrentFeedWeaponData extends Marshallable[ConcurrentFeedWeaponData] {
  /**
    * An abbreviated constructor for creating `ConcurrentFeedWeaponData` while masking use of `InternalSlot` for its `AmmoBoxData`.<br>
    * <br>
    * Exploration:<br>
    * This class may need to be rewritten later to support objects spawned in the world environment.
    * @param unk na
    * @param cls the code for the type of object (ammunition) being constructed
    * @param guid the globally unique id assigned to the ammunition
    * @param parentSlot the slot where the ammunition is to be installed in the weapon
    * @param ammo the constructor data for the ammunition
    * @return a WeaponData object
    */
  def apply(unk : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : AmmoBoxData) : ConcurrentFeedWeaponData =
    new ConcurrentFeedWeaponData(unk, InternalSlot(cls, guid, parentSlot, ammo) :: Nil)

  implicit val codec : Codec[ConcurrentFeedWeaponData] = (
    ("unk" | uint4L) ::
      uint4L ::
      uint24 ::
      uint16 ::
      uint2L ::
      (uint8L >>:~ { size =>
        uint2L ::
          ("ammo" | PacketHelpers.listOfNSized(size, InternalSlot.codec)) ::
          bool
      })
    ).exmap[ConcurrentFeedWeaponData] (
    {
      case code :: 8 :: 2 :: 0 :: 3 :: size :: 0 :: ammo :: false :: HNil =>
        if(size != ammo.size)
          Attempt.failure(Err("weapon encodes wrong number of ammunition"))
        else if(size == 0)
          Attempt.failure(Err("weapon needs to encode at least one type of ammunition"))
        else
          Attempt.successful(ConcurrentFeedWeaponData(code, ammo))
      case code :: _ :: _ ::  _ :: _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid weapon data format"))
    },
    {
      case ConcurrentFeedWeaponData(code, ammo) =>
        val size = ammo.size
        if(size == 0)
          Attempt.failure(Err("weapon needs to encode at least one type of ammunition"))
        else if(size >= 255)
          Attempt.failure(Err("weapon has too much ammunition (255+ types!)"))
        else
          Attempt.successful(code :: 8 :: 2 :: 0 :: 3 :: size :: 0 :: ammo :: false :: HNil)
    }
  ).as[ConcurrentFeedWeaponData]

  /**
    * Transform between WeaponData and ConstructorData.
    */
  val genericCodec : Codec[ConstructorData.genericPattern] = codec.exmap[ConstructorData.genericPattern] (
    {
      case x =>
        Attempt.successful(Some(x.asInstanceOf[ConstructorData]))
    },
    {
      case Some(x) =>
        Attempt.successful(x.asInstanceOf[ConcurrentFeedWeaponData])
      case _ =>
        Attempt.failure(Err("can not encode weapon data"))
    }
  )
}
