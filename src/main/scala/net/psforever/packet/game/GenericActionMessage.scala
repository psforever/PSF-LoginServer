// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import enumeratum.values.{IntEnum, IntEnumEntry}
import scodec.Codec
import scodec.codecs._

sealed abstract class GenericAction(val value: Int) extends IntEnumEntry

object GenericAction extends IntEnum[GenericAction] {
  val values: IndexedSeq[GenericAction] = findValues

  final case object ShowMosquitoRadar                extends GenericAction(value = 3)
  final case object HideMosquitoRadar                extends GenericAction(value = 4)
  final case object MissileLock                      extends GenericAction(value = 7)
  final case object WaspMissileLock                  extends GenericAction(value = 8)
  final case object TRekLock                         extends GenericAction(value = 9)
  final case object DropSpecialItem                  extends GenericAction(value = 11)
  final case object FacilityCaptureFanfare           extends GenericAction(value = 12)
  final case object NewCharacterBasicTrainingPrompt  extends GenericAction(value = 14)
  final case object MaxAnchorsExtend_RCV             extends GenericAction(value = 15)
  final case object MaxAnchorsRelease_RCV            extends GenericAction(value = 16)
  final case object MaxSpecialEffect_RCV             extends GenericAction(value = 20)
  final case object StopMaxSpecialEffect_RCV         extends GenericAction(value = 21)
  final case object CavernFacilityCapture            extends GenericAction(value = 22)
  final case object CavernFacilityKill               extends GenericAction(value = 23)
  final case object Imprinted                        extends GenericAction(value = 24)
  final case object NoLongerImprinted                extends GenericAction(value = 25)
  final case object PurchaseTimersReset              extends GenericAction(value = 27)
  final case object LeaveWarpQueue_RCV               extends GenericAction(value = 28)
  final case object AwayFromKeyboard_RCV             extends GenericAction(value = 29)
  final case object BackInGame_RCV                   extends GenericAction(value = 30)
  final case object FirstPersonViewWithEffect        extends GenericAction(value = 31)
  final case object FirstPersonViewFailToDeconstruct extends GenericAction(value = 32)
  final case object FailToDeconstruct                extends GenericAction(value = 33)
  final case object LookingForSquad_RCV              extends GenericAction(value = 36)
  final case object NotLookingForSquad_RCV           extends GenericAction(value = 37)
  final case object Unknown45                        extends GenericAction(value = 45)

  final case class Unknown(override val value: Int) extends GenericAction(value)
}

/**
  * Reports that something has happened, or makes something happen.
  * @param action what this packet does
  */
final case class GenericActionMessage(action: GenericAction) extends PlanetSideGamePacket {
  type Packet = GenericActionMessage
  def opcode = GamePacketOpcode.GenericActionMessage
  def encode = GenericActionMessage.encode(this)
}

object GenericActionMessage extends Marshallable[GenericActionMessage] {
  def apply(i: Int): GenericActionMessage = {
    GenericActionMessage(GenericAction.values.find { _.value == i } match {
      case Some(enum) => enum
      case None       => GenericAction.Unknown(i)
    })
  }

  private val genericActionCodec = uint(bits = 6).xmap[GenericAction](
    { i =>
      GenericAction.values.find { _.value == i } match {
        case Some(enum) => enum
        case None       => GenericAction.Unknown(i)
      }
    },
    e => e.value
  )

  implicit val codec: Codec[GenericActionMessage] = ("action" | genericActionCodec).as[GenericActionMessage]
}
