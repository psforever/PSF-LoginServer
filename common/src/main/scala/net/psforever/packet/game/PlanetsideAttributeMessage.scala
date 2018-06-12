// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * na<br>
  * Global:<br>
  * `50 - Common Initialization?`<br>
  * `51 - Common Initialization?`<br>
  * `67 - ???`<br>
  * <br>
  * Global (GUID=0)<br>
  * `82 - ???`
  * `83 - max boomers`<br>
  * `84 - max he mines`<br>
  * `85 - max disruptor mines`<br>
  * `86 - max spitfire turrets`<br>
  * `87 - max motion sensors`<br>
  * `88 - max shadow turrets`<br>
  * `89 - max cerebus turrets`<br>
  * `90 - max Aegis shield generators`<br>
  * `91 - max TRAPs`<br>
  * `92 - max OMFTs`<br>
  * `93 - max sensor disruptors`<br>
  * `94 - boomers`<br>
  * `95 - he mines`<br>
  * `96 - disruptor mines`<br>
  * `97 - spitfire turrets`<br>
  * `98 - motion sensors`<br>
  * `99 - shadow turrets`<br>
  * `100 - cerebus turrets`<br>
  * `101 - Aegis shield generators`<br>
  * `102 - TRAPSs`<br>
  * `103 - OMFTs`<br>
  * `104 - sensor disruptors`<br>
  * <br>
  * Players/General:<br>
  * Server to client : <br>
  * `0 - health (setting to zero on vehicles/terminals will destroy them)`<br>
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
  * `24 - Learn certifications with value :`<br>
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
  *         28 : Reinforced ExoSuitDefinition<br>
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
  * `25 - Forget certifications (same order as 24)`
  * `29 - Visible ?! That's not the cloaked effect, Maybe for spectator mode ?. Value is 0 to visible, 1 to invisible.`<br>
  * `31 - Info under avatar name : 0 = LFS, 1 = Looking For Squad Members`<br>
  * `32 - Info under avatar name : 0 = Looking For Squad Members, 1 = LFS`<br>
  * `35 - BR. Value is the BR`<br>
  * `36 - CR. Value is the CR`<br>
  * `43 - Info on avatar name : 0 = Nothing, 1 = "(LD)" message`<br>
  * `45 - NTU charge bar 0-10, 5 = 50% full. Seems to apply to both ANT and NTU Silo (possibly siphons?)`<br>
  *  47 - Sets base NTU level to CRITICAL. MUST use base modelId not base GUID
  *  48 - Set to 1 to send base power loss message & turns on red warning lights throughout base. MUST use base modelId not base GUID
  * 49 - Vehicle texture effects state? (>0 turns on ANT panel glow or ntu silo panel glow + orbs) (bit?)
  * `52 - Vehicle particle effects? (>0 turns on orbs going towards ANT. Doesn't affect silo) (bit?)
  * `53 - LFS. Value is 1 to flag LFS`<br>
  * `54 - Player "Aura". Values can be expressed in the first byte's lower nibble:`<br>
  * - 0 is nothing<br>
  * - 1 is plasma<br>
  * - 2 is ancient<br>
  * - 4 is LLU (?)<br>
  * - 8 is fire<br>
  * -- e.g., 13 = 8 + 4 + 1 = fire and LLU and plasma<br>
  * `55 - "Someone is attempting to Heal you". Value is 1`<br>
  * `56 - "Someone is attempting to Repair you". Value is 1`<br>
  * `67 - Enables base shields (from cavern module/lock). MUST use base modelId not GUID`<br>
  * `73 - "You are locked into the Core Beam. Charging your Module now.". Value is 1 to active`<br>
  * `77 - Cavern Facility Captures. Value is the number of captures`<br>
  * `78 - Cavern Kills. Value is the number of kills`<br>
  * `106 - Custom Head`<br>
  * `116 - Apply colour to REK beam and REK icon above players (0 = yellow, 1 = red, 2 = purple, 3 = blue)`<br>
  * Client to Server : <br>
  * `106 - Custom Head`<br>
  * <br>
  * `Vehicles:`<br>
  * `10 - Driver seat permissions (0 = Locked, 1 = Group, 3 = Empire)`<br>
  * `11 - Gunner seat(s) permissions (same)`<br>
  * `12 - Passenger seat(s) permissions (same)`<br>
  * `13 - Trunk permissions (same)`<br>
  * `21 - Declare a player the vehicle's owner, by globally unique identifier`<br>
  * `22 - Toggles gunner and passenger mount points (1 = hides, 0 = reveals; this also locks their permissions)`<br>
  * `54 -  Vehicle EMP? Plays sound as if vehicle had been hit by EMP`<br>
  * `68 - Vehicle shield health`<br>
  * `80 - Damage vehicle (unknown value)`<br>
  * `81 - ???`<br>
  * `113 - `Vehicle capacitor - e.g. Leviathan EMP charge`
  *
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
