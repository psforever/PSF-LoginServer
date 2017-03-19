// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the ammunition portion of `ObjectCreateDetailedMessage` packet data.
  * This data will help construct a "box" of that type of ammunition when standalone.
  * It can also be constructed directly inside a weapon as its magazine.<br>
  * <br>
  * The maximum amount of ammunition that can be stored in a single box is 65535 units.
  * Regardless of the interface, however, the number will never be fully visible.
  * Only the first three digits or the first four digits may be represented.
  * @param unk na
  * @param magazine the number of rounds available
  * @see DetailedWeaponData
  */
final case class DetailedAmmoBoxData(unk : Int,
                                     magazine : Int
                                    ) extends ConstructorData {
  override def bitsize : Long = 40L
}

object DetailedAmmoBoxData extends Marshallable[DetailedAmmoBoxData] {
  /**
    * An abbreviated constructor for creating `DetailedWeaponData` while masking use of `InternalSlot`.
    * @param cls the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param parentSlot a parent-defined slot identifier that explains where the child is to be attached to the parent
    * @param ammo the `DetailedAmmoBoxData`
    * @return an `InternalSlot` object that encapsulates `DetailedAmmoBoxData`
    */
  def apply(cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : DetailedAmmoBoxData) : InternalSlot =
    new InternalSlot(cls, guid, parentSlot, ammo)

  implicit val codec : Codec[DetailedAmmoBoxData] = (
    uint4L ::
      ("unk" | uint4L) ::
      uint(15) ::
      ("magazine" | uint16L) ::
      bool
    ).exmap[DetailedAmmoBoxData] (
    {
      case 0xC :: unk :: 0 :: mag :: false :: HNil =>
        Attempt.successful(DetailedAmmoBoxData(unk, mag))
      case _ =>
        Attempt.failure(Err("invalid ammunition data format"))
    },
    {
      case DetailedAmmoBoxData(unk, mag) =>
        Attempt.successful(0xC :: unk :: 0 :: mag :: false:: HNil)
    }
  )
}
