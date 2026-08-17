// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.types.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
 * A representation of simple objects that are spawned by the adaptive construction engine.
 * @param data data common to game objects
 * @param unk1 na
 * @param owner_guid guid of the player who placed this deployable;
 *                   also, see `CommonFieldData.guid`
 * @param unk3 na
 * @param unk4 na
 */
final case class TelepadDeployableData(
                                        data: CommonFieldData,
                                        unk1: Boolean,
                                        owner_guid: PlanetSideGUID,
                                        unk3: Boolean,
                                        unk4: Boolean
                                      ) extends ConstructorData {
  override def bitsize: Long = {
    20L + data.bitsize
  }
}

object TelepadDeployableData extends Marshallable[TelepadDeployableData] {
  implicit val codec: Codec[TelepadDeployableData] = (
    ("deploy" | CommonFieldData.codec) ::
      ("unk1" | bool) ::
      ("owner_guid" | PlanetSideGUID.codec) ::
      ("unk3" | bool) ::
      ("unk4" | bool) ::
      ignore(size = 1)
  ).exmap[TelepadDeployableData](
    {
      case data :: unk1 :: owner :: unk3 :: unk4 :: _ :: HNil =>
        Attempt.successful(TelepadDeployableData(data, unk1, owner, unk3, unk4))

      case data =>
        Attempt.failure(Err(s"invalid telepad data format - $data"))
    },
    {
      case TelepadDeployableData(data, unk1, owner, unk3, unk4) =>
        Attempt.successful(data :: unk1 :: owner :: unk3 :: unk4 ::() :: HNil)
    }
  )
}
