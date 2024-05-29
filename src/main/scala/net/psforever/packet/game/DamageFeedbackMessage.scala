// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

/**
 * na
 * @param unk1 na
 * @param unk2 a modifier that customizes one of the values for the `unk2...` determination values;
 *             when not using the GUID field, `true` when using the string field
 * @param unk2a the global unique identifier of the entity inflicting the damage
 * @param unk2b if no global unique identifier (above), the name of the entity inflicting the damage
 * @param unk2c if no global unique identifier (above), the object type of the entity inflicting the damage
 * @param unk3 a modifier that customizes one of the values for the `unk3...` determination values;
 *             when not using the GUID field, `true` when using the string field
 * @param unk3a the global unique identifier of the entity absorbing the damage
 * @param unk3b if no global unique identifier (above), the name of the entity absorbing the damage
 * @param unk3c if no global unique identifier (above), the object type of the entity absorbing the damage
 * @param unk3d na
 * @param unk4 an indicator for the target-specific vital statistic being affected
 * @param unk5 the amount of damage
 * @param unk6 na
 */
final case class DamageFeedbackMessage(
                                        unk1: Int,
                                        unk2: Option[Boolean],
                                        unk2a: Option[PlanetSideGUID],
                                        unk2b: Option[String],
                                        unk2c: Option[Int],
                                        unk3: Option[Boolean],
                                        unk3a: Option[PlanetSideGUID],
                                        unk3b: Option[String],
                                        unk3c: Option[Int],
                                        unk3d: Option[Int],
                                        unk4: Int,
                                        unk5: Long,
                                        unk6: Int
                                      ) extends PlanetSideGamePacket {
  assert(
    {
      val unk2aEmpty = unk2a.isEmpty
      val unk2bEmpty = unk2b.isEmpty
      val unk2cEmpty = unk2c.isEmpty
      if (!unk2aEmpty) unk2bEmpty && unk2cEmpty
      else if (!unk2bEmpty) unk2aEmpty && unk2cEmpty
      else unk2aEmpty && unk2bEmpty && !unk2cEmpty
    }
  )
  assert(
    {
      val unk3aEmpty = unk3a.isEmpty
      val unk3bEmpty = unk3b.isEmpty
      val unk3cEmpty = unk3c.isEmpty
      if (!unk3aEmpty) unk3bEmpty && unk3cEmpty
      else if (!unk3bEmpty) unk3aEmpty && unk3cEmpty
      else unk3aEmpty && unk3bEmpty && !unk3cEmpty
    }
  )
  assert(unk3a.isEmpty == unk3d.nonEmpty)

  type Packet = DamageFeedbackMessage
  def opcode: Type = GamePacketOpcode.DamageFeedbackMessage
  def encode: Attempt[BitVector] = DamageFeedbackMessage.encode(this)
}

object DamageFeedbackMessage extends Marshallable[DamageFeedbackMessage] {
  def apply(
             unk1: Int,
             unk2a: Option[PlanetSideGUID],
             unk2b: Option[String],
             unk2c: Option[Int],
             unk3a: Option[PlanetSideGUID],
             unk3b: Option[String],
             unk3c: Option[Int],
             unk3d: Option[Int],
             unk4: Int,
             unk5: Long
           ): DamageFeedbackMessage = {
    DamageFeedbackMessage(unk1, None, unk2a, unk2b, unk2c, None, unk3a, unk3b, unk3c, unk3d, unk4, unk5, 0)
  }

  def apply(
             unk1: Int,
             unk2: PlanetSideGUID,
             unk3: PlanetSideGUID,
             unk4: Int,
             unk5: Long
           ): DamageFeedbackMessage = {
    DamageFeedbackMessage(unk1, None, Some(unk2), None, None, None, Some(unk3), None, None, None, unk4, unk5, 0)
  }

  private case class EntryFields(
                                  usesGuid: Boolean,
                                  usesStr: Boolean,
                                  guidOpt: Option[PlanetSideGUID],
                                  strOpt: Option[String],
                                  intOpt: Option[Int]
                                )

  /**
   * na
   * @param adjustment na;
   *                   can not be a negative number
   * @return na
   */
  private def entityFieldFormatCodec(adjustment: Int): Codec[EntryFields] = {
    ((bool :: bool) >>:~ { case usesGuid :: usesString :: HNil =>
      conditional(usesGuid, PlanetSideGUID.codec) ::
        conditional(!usesGuid && usesString, PacketHelpers.encodedWideStringAligned(adjustment)) ::
        conditional(!usesGuid && !usesString, uintL(bits = 11))
    }).xmap[EntryFields](
      {
        case (a :: b :: HNil) :: c :: d :: e :: HNil => EntryFields(a, b, c, d, e)
      },
      {
        case EntryFields(a, b, c, d, e) => (a :: b :: HNil) :: c :: d :: e :: HNil
      }
    )
  }

  implicit val codec: Codec[DamageFeedbackMessage] = (
    ("unk1" | uint4) :: {
      entityFieldFormatCodec(adjustment = 4) >>:~ { fieldsA =>
        val offset = if (fieldsA.usesGuid) { 0 } else if (fieldsA.usesStr) { 6 } else { 5 }
        entityFieldFormatCodec(offset) >>:~ { fieldsB =>
          ("unk3d" | conditional(!fieldsB.usesGuid, uint2)) ::
            ("unk4" | uint(bits = 3)) ::
            ("unk5" | uint32L) ::
            ("unk6" | uint2)
        }
      }
    }).xmap[DamageFeedbackMessage](
    {
      case u1 :: EntryFields(_, u2, u2a, u2b, u2c) :: EntryFields(_, u3, u3a, u3b, u3c) :: u3d :: u4 :: u5 :: u6 :: HNil =>
        val u2False = if (u2a.nonEmpty && !u2) { Some(false) } else { None }
        val u3False = if (u3a.nonEmpty && !u3) { Some(false) } else { None }
        DamageFeedbackMessage(u1, u2False, u2a, u2b, u2c, u3False, u3a, u3b, u3c, u3d, u4, u5, u6)
    },
    {
      case DamageFeedbackMessage(u1, u2, u2a, u2b, u2c, u3, u3a, u3b, u3c, u3d, u4, u5, u6) =>
        val(u2Boola, u2Boolb) = if (u2a.nonEmpty) {
          (true, u2.getOrElse(true))
        } else {
          (false, u2b.nonEmpty)
        }
        val(u3Boola, u3Boolb) = if (u3a.nonEmpty) {
          (true, u3.getOrElse(true))
        } else {
          (false, u3b.nonEmpty)
        }
        u1 :: EntryFields(u2Boola, u2Boolb, u2a, u2b, u2c) :: EntryFields(u3Boola, u3Boolb, u3a, u3b, u3c) :: u3d :: u4 :: u5 :: u6 :: HNil
    }
  )
}
