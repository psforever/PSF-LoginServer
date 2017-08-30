// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of an object that can be interacted with when using a variety of terminals.
  * This object is generally invisible.
  * @param faction the faction that can access the terminal
  * @param unk na
  */
final case class CommonTerminalData(faction : PlanetSideEmpire.Value,
                                    unk : Int = 0
                                   ) extends ConstructorData {
  override def bitsize : Long = 24L
}

object CommonTerminalData extends Marshallable[CommonTerminalData] {
  /**
    * Overloaded constructor for a type of common terminal.
    * @param cls the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param parentSlot a parent-defined slot identifier that explains where the child is to be attached to the parent
    * @param terminal the `CommonTerminalData`
    * @return an `InternalSlot` object
    */
  def apply(cls : Int, guid : PlanetSideGUID, parentSlot : Int, terminal : CommonTerminalData) : InternalSlot =
    InternalSlot(cls, guid, parentSlot, terminal)

  implicit val codec : Codec[CommonTerminalData] = (
    ("faction" | PlanetSideEmpire.codec) ::
      uint2L ::
      ("unk" | uint2L) ::
      uint(18)
    ).exmap[CommonTerminalData] (
    {
      case fac :: 0 :: unk :: 0 :: HNil =>
        Attempt.successful(CommonTerminalData(fac, unk))
      case _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid terminal data format"))
    },
    {
      case CommonTerminalData(fac, unk) =>
        Attempt.successful(fac :: 0 :: unk :: 0 :: HNil)
    }
  )
}
