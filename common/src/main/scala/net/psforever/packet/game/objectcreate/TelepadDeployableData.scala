// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.Codec
import scodec.codecs._

/**
  * A representation of simple objects that are spawned by the adaptive construction engine.
  * @param deploy data common to game objects
  * @param unk1 na
  * @param unk2 na
  */
final case class TelepadDeployableData(deploy : CommonFieldDataWithPlacement,
                                       unk1 : Int,
                                       unk2 : Int) extends ConstructorData {
  override def bitsize : Long = {
    20 + deploy.bitsize
  }
}

object TelepadDeployableData extends Marshallable[TelepadDeployableData] {
  implicit val codec : Codec[TelepadDeployableData] = (
    ("deploy" | CommonFieldDataWithPlacement.codec) ::
      ("unk1" | uint16L) ::
      ("unk2" | uint4)
    ).as[TelepadDeployableData]
}
