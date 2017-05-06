// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.game.objectcreate.RibbonBars
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * An `Enumeration` of the slots for award ribbons on a player's `RibbonBars`.
  */
object RibbonBarsSlot extends Enumeration {
  type Type = Value

  val Top,
      Middle,
      Bottom,
      TermOfService //technically,the slot above "Top"
      = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}

/**
  * Dispatched to configure a player's merit commendation ribbons.<br>
  * <br>
  * Normally, this packet is dispatched by the client when managing merit commendations through the "Character Info/Achievements" tab.
  * On Gemini Live, this packet was also always dispatched once by the server during character login.
  * It set the term of service ribbon explicitly.
  * Generally, this was unnecessary, as the encoded character data maintains information about displayed ribbons.
  * This behavior was probably a routine that ensured that correct yearly progression was tracked if the player earned it while offline.
  * It never set any of the other ribbon slot positions during login.<br>
  * <br>
  * A specific ribbon may only be set once to one slot.
  * The last set slot is considered the valid position to which that ribbon will be placed/moved.
  * @param player_guid the player
  * @param ribbon the award to be displayed;
  *               defaults to `RibbonsBars.noRibbon`;
  *               use `RibbonsBars.noRibbon` when indicating "no ribbon"
  * @param bar any of the four positions where the award ribbon is to be displayed;
  *            defaults to `TermOfService`
  * @see `RibbonBars`
  */
final case class DisplayedAwardMessage(player_guid : PlanetSideGUID,
                                       ribbon : Long = RibbonBars.noRibbon,
                                       bar : RibbonBarsSlot.Value = RibbonBarsSlot.TermOfService)
  extends PlanetSideGamePacket {
  type Packet = DisplayedAwardMessage
  def opcode = GamePacketOpcode.DisplayedAwardMessage
  def encode = DisplayedAwardMessage.encode(this)
}

object DisplayedAwardMessage extends Marshallable[DisplayedAwardMessage] {
  implicit val codec : Codec[DisplayedAwardMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("ribbon" | uint32L) ::
      ("bar" | RibbonBarsSlot.codec)
    ).as[DisplayedAwardMessage]
}
