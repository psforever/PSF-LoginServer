// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Attribute Type:<br>
  * Server to client : <br>
  * `0 - health`<br>
  * `1 - healthMax`<br>
  * `2 - stamina`<br>
  * `3 - staminaMax`<br>
  * `4 - armor`<br>
  * `5 - armorMax`<br>
  * `14 - Something with grief`<br>
  * `15 - Weapon Lock. Value exemple : 600 to have 1 min lock. Max possible is 30min lock`<br>
  * `17 - BEP. Value seems to be the same as BattleExperienceMessage`<br>
  * `18 - CEP.`<br>
  * `19 - Anchors. Value is 0 to disengage, 1 to engage.`<br>
  * `24 - Certifications`<br>
  * `29 - Visible ?! That's not the cloaked effect, Maybe for spectator mode ?. Value is 0 to visible, 1 to invisible.`<br>
  * `35 - BR. Value is the BR`<br>
  * `36 - CR. Value is the CR`<br>
  * `53 - LFS. Value is 1 to flag LFS`<br>
  * `54 - Player "Aura". Values are : 0 for nothing, 1 for plasma, 2 for ancient, 3 for plasma + ancient,<br>
  *         4 for LLU?, 5 for plasma + LLU?, 6 for ancient + LLU?, 7 for plasma + ancient + LLU?, 8 for fire,<br>
  *         9 for plasma + fire, 10 for ancient + fire, 11 for plasma + ancient + fire,<br>
  *         12 for LLU? + fire, 13 for plasma + LLU? + fire, 14 for ancient + LLU? + fire,<br>
  *         15 for plasma + ancient + LLU? + fire,`<br>
  * `55 - "Someone is attempting to Heal you". Value is 1`<br>
  * `56 - "Someone is attempting to Repair you". Value is 1`<br>
  * `73 - "You are locked into the Core Beam. Charging your Module now.". Value is 1 to active`<br>
  * `77 - Cavern Facility Captures. Value is the number of captures`<br>
  * `78 - Cavern Kills. Value is the number of kills`<br>
  * `106 - Custom Head`
  * Client to Server : <br>
  * `106 - Custom Head`
  * @param player_guid the player
  * @param attribute_type na
  * @param attribute_value na
  */
final case class PlanetsideAttributeMessage(player_guid : PlanetSideGUID,
                                            attribute_type : Int,
                                            attribute_value : Long)
  extends PlanetSideGamePacket {
  type Packet = PlanetsideAttributeMessage
  def opcode = GamePacketOpcode.PlanetsideAttributeMessage
  def encode = PlanetsideAttributeMessage.encode(this)
}

object PlanetsideAttributeMessage extends Marshallable[PlanetsideAttributeMessage] {
  implicit val codec : Codec[PlanetsideAttributeMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("attribute_type" | uint8L) ::
      ("attribute_value" | uint32L)
    ).as[PlanetsideAttributeMessage]
}