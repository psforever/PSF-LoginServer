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
  * `6 - transform the (other) avatar in backpack on ground`<br>
  * `14 - Something with grief`<br>
  * `15 - Weapon Lock. Value exemple : 600 to have 1 min lock. Max possible is 30min lock`<br>
  * `17 - BEP. Value seems to be the same as BattleExperienceMessage`<br>
  * `18 - CEP.`<br>
  * `19 - Anchors. Value is 0 to disengage, 1 to engage.`<br>
  * `24 - Certifications with value :`<br>
  *         01 : Medium Assault<br>
  *         02 : Heavy Assault<br>
  *         03 : Special Assault<br>
  *         04 : Anti-Vehicular<br>
  *         05 : Sniping<br>
  *         06 : Elite Assault<br>
  *         07 : Air Cavalry, Scout<br>
  *         08 : Air Cavalry, Interceptor<br>
  *         09 : Air Cavalry, Assault<br>
  *         10 : Air Support<br>
  *         11 : ATV<br>
  *         12 : Light Scout<br>
  *         13 : Assault Buggy<br>
  *         14 : Armored Assault 1<br>
  *         15 : Armored Assault 2<br>
  *         16 : Ground Transport<br>
  *         17 : Ground Support<br>
  *         18 : BattleFrame Robotics<br>
  *         19 : Flail<br>
  *         20 : Switchblade<br>
  *         21 : Harasser<br>
  *         22 : Phantasm<br>
  *         23 : Galaxy Gunship<br>
  *         24 : BFR Anti Aircraft<br>
  *         25 : BFR Anti Infantry<br>
  *         26 : ?! Removed Cert ?<br>
  *         27 : ?! Removed Cert ?<br>
  *         28 : Reinforced ExoSuit<br>
  *         29 : Infiltration Suit<br>
  *         30 : MAX (Burster)<br>
  *         31 : MAX (Dual-Cycler)<br>
  *         32 : MAX (Pounder)<br>
  *         33 : Uni-MAX<br>
  *         34 : Medical<br>
  *         35 : Advanced Medical<br>
  *         36 : Hacking<br>
  *         37 : Advanced Hacking<br>
  *         38 : Expert Hacking<br>
  *         39 : Data Corruption<br>
  *         40 : Electronics Expert (= Expert Hacking + Data Corruption) Must have Advanced Hacking<br>
  *         41 : Engineering<br>
  *         42 : Combat Engineering<br>
  *         43 : Fortification Engineering<br>
  *         44 : Assault Engineering<br>
  *         45 : Advanced Engineering (= Fortification Engineering + Assault Engineering) Must have Combat Engineering<br>
  * `29 - Visible ?! That's not the cloaked effect, Maybe for spectator mode ?. Value is 0 to visible, 1 to invisible.`<br>
  * `31 - Info under avatar name : 0 = LFS, 1 = Looking For Squad Members`<br>
  * `32 - Info under avatar name : 0 = Looking For Squad Members, 1 = LFS`<br>
  * `35 - BR. Value is the BR`<br>
  * `36 - CR. Value is the CR`<br>
  * `43 - Info on avatar name : 0 = Nothing, 1 = "(LD)" message`<br>
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
