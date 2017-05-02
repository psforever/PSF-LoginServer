// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of ammunition that can be created using `ObjectCreateMessage` packet data.
  * This data will help construct a "box" of that type of ammunition when standalone.
  * It can also be constructed directly inside a weapon as its magazine.<br>
  * <br>
  * This ammunition object ompletely ignores thr capacity field, normal to detailed ammunition objects.
  * Creating an object of this type directly and picking it up or observing it (in a weapon) reveals a single round.
  * @param unk na;
  *            defaults to 0
  * @see `DetailedAmmoBoxData`
  */
final case class AmmoBoxData(unk : Int = 0) extends ConstructorData {
  override def bitsize : Long = 24L
}

object AmmoBoxData extends Marshallable[AmmoBoxData] {
  /**
    * An abbreviated constructor for creating `AmmoBoxData` while masking use of `InternalSlot`.
    * @param cls the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param parentSlot a parent-defined slot identifier that explains where the child is to be attached to the parent
    * @param ammo the ammunition object
    * @return an `InternalSlot` object that encapsulates `AmmoBoxData`
    */
  def apply(cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : AmmoBoxData) : InternalSlot =
    new InternalSlot(cls, guid, parentSlot, ammo)

  implicit val codec : Codec[AmmoBoxData] = (
    uint4L ::
    ("unk" | uint4L) ::
    uint(16)
  ).exmap[AmmoBoxData] (
    {
      case 0xC :: unk :: 0 :: HNil  =>
        Attempt.successful(AmmoBoxData(unk))
      case _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid ammunition data format"))
    },
    {
      case AmmoBoxData(unk) =>
        Attempt.successful(0xC :: unk :: 0 :: HNil)
    }
  )
}
