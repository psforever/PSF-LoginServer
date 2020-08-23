package net.psforever.types

import enumeratum.{Enum, EnumEntry}
import net.psforever.packet.PacketHelpers
import scodec.Codec
import scodec.codecs._

/**
  * ChatMessageTypes were reversed by:
  * Checking the type in the packet upon receipt by server while using slash commands
  * Replaying the ChatMsg packet back to the sender after modifying the messagetype value
  * Shaql magic
  * english.str references to the type names
  *
  * Message type names that are not based on actual names are prefixed with U_
  * Message type names that are completely unknown are named UNK_#
  *
  * Some un-assigned names (probably not all of them):
  * CMT_METAGAME, CMT_SPEEDHACK, CMT_DUMPBUILDINGTREE, CMT_CLIENT_BATCH
  * CMT_COMMAND_ZONE96, CMT_COMMAND_ZONE97, CMT_COMMAND_ZONE98, CMT_COMMAND_ZONE99
  */
sealed abstract class ChatMessageType extends EnumEntry {}

object ChatMessageType extends Enum[ChatMessageType] {

  val values: IndexedSeq[ChatMessageType] = findValues

  case object UNK_0                   extends ChatMessageType // ???
  case object CMT_ALLIANCE            extends ChatMessageType // ??? unused
  case object CMT_BATTLEGROUP         extends ChatMessageType // /bg (not working???)
  case object CMT_BROADCAST           extends ChatMessageType // /broadcast OR /b
  case object CMT_COMMAND             extends ChatMessageType // /command OR /c
  case object CMT_COMMAND_ALLZONES    extends ChatMessageType // /comall
  case object CMT_COMMAND_REPORT      extends ChatMessageType // /sitrep OR /situationreport
  case object CMT_COMMAND_SANCTUARY   extends ChatMessageType // /comsanctuary OR /comsan
  case object CMT_COMMAND_STATION     extends ChatMessageType // ??? unused
  case object CMT_COMMAND_CAVERN1     extends ChatMessageType // /comsu OR /comsupai
  case object CMT_COMMAND_CAVERN2     extends ChatMessageType // /comhu OR /comhunhau
  case object CMT_COMMAND_CAVERN3     extends ChatMessageType // /comad OR /comadlivun
  case object CMT_COMMAND_CAVERN4     extends ChatMessageType // /comby OR /combyblos
  case object CMT_COMMAND_CAVERN5     extends ChatMessageType // /coman OR /comannwn
  case object CMT_COMMAND_CAVERN6     extends ChatMessageType // /comdr OR /comdrugaskan
  case object CMT_COMMAND_ZONE1       extends ChatMessageType // /comso OR /comsolsar
  case object CMT_COMMAND_ZONE2       extends ChatMessageType // /comho OR /comhossin
  case object CMT_COMMAND_ZONE3       extends ChatMessageType // /comcy OR /comcyssor
  case object CMT_COMMAND_ZONE4       extends ChatMessageType // /comis OR /comishundar
  case object CMT_COMMAND_ZONE5       extends ChatMessageType // /comfo OR /comforseral
  case object CMT_COMMAND_ZONE6       extends ChatMessageType // /comce OR /comceryshen
  case object CMT_COMMAND_ZONE7       extends ChatMessageType // /comes OR /comesamir
  case object CMT_COMMAND_ZONE8       extends ChatMessageType // /comos OR /comoshur
  case object CMT_COMMAND_ZONE8_PRIME extends ChatMessageType // /compr OR /comoshurprime
  case object CMT_COMMAND_ZONE9       extends ChatMessageType // /comse OR /comsearhus
  case object CMT_COMMAND_ZONE10      extends ChatMessageType // /comam OR /comamerish
  case object CMT_OPEN                extends ChatMessageType // /local OR /l
  case object CMT_OUTFIT              extends ChatMessageType // /outfit OR /o
  case object CMT_PLATOON             extends ChatMessageType // /platoon OR /p
  case object CMT_PLATOONLEADER       extends ChatMessageType // /platoonleader OR /pl
  case object CMT_SQUAD               extends ChatMessageType // /squad OR /s
  case object CMT_SQUADLEADER         extends ChatMessageType // /sl
  case object CMT_TELL                extends ChatMessageType // /tell OR /t
  case object U_CMT_BLACKOPS_CHAT     extends ChatMessageType // ??? No slash command?
  case object CMT_GMBROADCAST         extends ChatMessageType // /gmbroadcast
  case object CMT_GMBROADCAST_NC      extends ChatMessageType // /ncbroadcast
  case object CMT_GMBROADCAST_TR      extends ChatMessageType // /trbroadcast
  case object CMT_GMBROADCAST_VS      extends ChatMessageType // /vsbroadcast
  case object CMT_GMBROADCASTWORLD    extends ChatMessageType // /worldbroadcast OR /wb
  case object CMT_GMOPEN              extends ChatMessageType // /gmlocal
  case object CMT_GMTELL              extends ChatMessageType // /gmtell (actually causes normal /tell 0x20 when not a gm???)
  case object CMT_NOTE                extends ChatMessageType // /note
  case object CMT_GMBROADCASTPOPUP    extends ChatMessageType // /gmpopup
  case object U_CMT_GMTELLFROM        extends ChatMessageType // Acknowledgement of /gmtell for sender
  case object U_CMT_TELLFROM          extends ChatMessageType // Acknowledgement of /tell for sender
  case object UNK_45                  extends ChatMessageType // ??? empty
  case object CMT_CULLWATERMARK
      extends ChatMessageType // ??? This actually causes the client to ping back to the server with some stringified numbers "80 120" (with the same 46 chatmsg causing infinite loop?) - may be incorrect decoding
  case object CMT_INSTANTACTION          extends ChatMessageType // /instantaction OR /ia
  case object CMT_RECALL                 extends ChatMessageType // /recall
  case object CMT_OUTFIT_RECALL          extends ChatMessageType // /outfitrecall
  case object CMT_SQUAD_REMATRIX         extends ChatMessageType // ???
  case object CMT_OUTFITRENAME_USER      extends ChatMessageType // /outfitrename
  case object CMT_RENAME_USER            extends ChatMessageType // /rename
  case object CMT_REPORTUSER             extends ChatMessageType // /report
  case object CMT_VOICE                  extends ChatMessageType // quickchat (v-- commands)
  case object CMT_WHO                    extends ChatMessageType // /who
  case object CMT_WHO_CSR                extends ChatMessageType // /whocsr OR /whogm
  case object CMT_WHO_PLATOONLEADERS     extends ChatMessageType // /whoplatoonleaders OR /whopl
  case object CMT_WHO_SQUADLEADERS       extends ChatMessageType // /whosquadleaders OR /whosl
  case object CMT_WHO_TEAMS              extends ChatMessageType // /whoteams OR /whoempires
  case object CMT_WHO_CR                 extends ChatMessageType // /who cr<#>
  case object CMT_QUIT                   extends ChatMessageType // /quit OR /q
  case object CMT_HIDE_HELMET            extends ChatMessageType // /hide_helmet OR /helmet
  case object CMT_TOGGLE_HAT             extends ChatMessageType // /hat
  case object CMT_TOGGLE_SHADES          extends ChatMessageType // /shades
  case object CMT_TOGGLE_EARPIECE        extends ChatMessageType // /earpiece
  case object CMT_TOGGLE_GM              extends ChatMessageType // /gmtoggle
  case object CMT_ANONYMOUS              extends ChatMessageType // /anon OR /anonymous
  case object CMT_DESTROY                extends ChatMessageType // /destroy
  case object CMT_KICK                   extends ChatMessageType // /worldkick
  case object CMT_KICK_BY_ID             extends ChatMessageType // /worldkickid
  case object UNK_71                     extends ChatMessageType // ??? empty (though the game elsewhere handles it similarly to 69!)
  case object CMT_LOCKSERVER             extends ChatMessageType // /lockserver
  case object CMT_UNLOCKSERVER           extends ChatMessageType // /unlockserver
  case object CMT_CAPTUREBASE            extends ChatMessageType // /capturebase
  case object CMT_CREATE                 extends ChatMessageType // /create
  case object CMT_HACK_DOORS             extends ChatMessageType // /hackdoors
  case object CMT_OUTFITCLAIM            extends ChatMessageType // /outfitclaim
  case object CMT_OUTFITUNCLAIM          extends ChatMessageType // /outfitunclaim
  case object CMT_SETBASERESOURCES       extends ChatMessageType // /setbaseresources
  case object U_CMT_SETHEALTH_TARGET     extends ChatMessageType // /sethealth t
  case object U_CMT_SETARMOR_TARGET      extends ChatMessageType // /setarmor t
  case object CMT_SETVEHICLERESOURCES    extends ChatMessageType // /setvehicleresources
  case object CMT_SUPER_CREATE           extends ChatMessageType // /supercreate OR /sc
  case object CMT_VORTEX_MODULE          extends ChatMessageType // /vortex OR /vtx
  case object U_CMT_RESPAWNAMS           extends ChatMessageType // /respawnams ("Create AMS Resp")
  case object CMT_ADDBATTLEEXPERIENCE    extends ChatMessageType // /addbep
  case object CMT_ADDCERTIFICATION       extends ChatMessageType // /certadd
  case object CMT_ADDCOMMANDEXPERIENCE   extends ChatMessageType // /addcep
  case object CMT_ADDIMPLANT             extends ChatMessageType // /addimplant
  case object CMT_ARMOR                  extends ChatMessageType // /armor
  case object CMT_ENABLEIMPLANT          extends ChatMessageType // /enableimplant
  case object CMT_EXPANSIONS             extends ChatMessageType // /expansions
  case object CMT_FIRST_TIME_EVENTS      extends ChatMessageType // /firsttime OR /fte
  case object CMT_INVENTORYLAYOUT        extends ChatMessageType // /inventory
  case object CMT_REMOVECERTIFICATION    extends ChatMessageType // /certrm
  case object CMT_REMOVEIMPLANT          extends ChatMessageType // /removeimplant
  case object CMT_SAVE                   extends ChatMessageType // /save
  case object CMT_SETBATTLEEXPERIENCE    extends ChatMessageType // /setbep
  case object CMT_SETBATTLERANK          extends ChatMessageType // /setbr
  case object CMT_SETCAPACITANCE         extends ChatMessageType // /setcapacitance
  case object CMT_SETCOMMANDEXPERIENCE   extends ChatMessageType // /setcep
  case object CMT_SETCOMMANDRANK         extends ChatMessageType // /setcr
  case object CMT_SETFAVORITE            extends ChatMessageType // /setfavorite
  case object CMT_SETHEALTH              extends ChatMessageType // /sethealth
  case object CMT_SETARMOR               extends ChatMessageType // /setarmor
  case object CMT_SETIMPLANTSLOTS        extends ChatMessageType // /setimplantslots
  case object CMT_SETSTAMINA             extends ChatMessageType // /setstamina
  case object CMT_SUICIDE                extends ChatMessageType // /suicide
  case object CMT_USEFAVORITE            extends ChatMessageType // /favorite
  case object CMT_TOGGLERESPAWNPENALTY   extends ChatMessageType // /togglerespawnpenalty
  case object CMT_SETAMMO                extends ChatMessageType // /setammo
  case object CMT_AWARD_QUALIFY          extends ChatMessageType // /award_qualify
  case object CMT_AWARD_PROGRESS         extends ChatMessageType // /award_progress
  case object CMT_AWARD_ADD              extends ChatMessageType // /award_add
  case object CMT_AWARD_REMOVE           extends ChatMessageType // /award_remove
  case object CMT_STAT_ADD               extends ChatMessageType // /stat_add
  case object CMT_BFR_SETCAVERNCAPS      extends ChatMessageType // /bfr_setcaverncaps
  case object CMT_BFR_SETCAVERNKILLS     extends ChatMessageType // /bfr_setcavernkills
  case object CMT_BFR_IMPRINT            extends ChatMessageType // /bfr_imprint
  case object CMT_BFR_DAMAGE             extends ChatMessageType // /bfrdamage
  case object CMT_BFR_DAMAGEWEAPON       extends ChatMessageType // /bfrdamageweapon
  case object CMT_RESETPURCHASETIMERS    extends ChatMessageType // /resetpurchasetimers
  case object CMT_EVENTPLAYTIME          extends ChatMessageType // /eventplaytime
  case object CMT_SETTIME                extends ChatMessageType // /settime
  case object CMT_SETTIMESPEED           extends ChatMessageType // /settimespeed
  case object CMT_SNOOP                  extends ChatMessageType // /snoop
  case object CMT_SOULMARK               extends ChatMessageType // /soulmark
  case object CMT_FLY                    extends ChatMessageType // /fly
  case object CMT_SPEED                  extends ChatMessageType // /speed
  case object CMT_TOGGLESPECTATORMODE    extends ChatMessageType // /spectator
  case object CMT_INFO                   extends ChatMessageType // /info
  case object CMT_SHOWZONES              extends ChatMessageType // /showzones
  case object CMT_SYNC                   extends ChatMessageType // /sync
  case object CMT_SECURITY               extends ChatMessageType // /security
  case object CMT_FIND                   extends ChatMessageType // /find
  case object CMT_OUTFITCONFIRM          extends ChatMessageType // /outfitconfirm
  case object CMT_OUTFITDELETE           extends ChatMessageType // /outfitdelete
  case object CMT_OUTFITRENAME_GM        extends ChatMessageType // /outfitrenamegm
  case object CMT_OUTFITLEADER           extends ChatMessageType // /outfitleader
  case object CMT_OUTFITPOINTS           extends ChatMessageType // /outfitpoints
  case object CMT_OUTFITPOINTS_CHARACTER extends ChatMessageType // /giveop
  case object CMT_OUTFITCREDITS          extends ChatMessageType // /outfitcredits
  case object CMT_RENAME_GM              extends ChatMessageType // /renamegm
  case object CMT_SETGRIEF               extends ChatMessageType // /setgrief
  case object CMT_SILENCE                extends ChatMessageType // /silence
  case object CMT_SILENCE_OUTFIT         extends ChatMessageType // /silenceoutfit
  case object CMT_COMMAND_SILENCE        extends ChatMessageType // /commandsilence
  case object CMT_COMMAND_SILENCE_OUTFIT extends ChatMessageType // /commandsilenceoutfit
  case object CMT_COMMAND_SILENCE_EMPIRE extends ChatMessageType // /commandsilenceempire
  case object CMT_SUMMON                 extends ChatMessageType // /summon
  case object CMT_AWARD                  extends ChatMessageType // /award
  case object U_CMT_AWARD_ZONE           extends ChatMessageType // /award
  case object CMT_AWARD_REVOKE           extends ChatMessageType // /award_revoke
  case object U_CMT_AWARD_REVOKE_ZONE    extends ChatMessageType // /award_revoke
  case object CMT_RESET_CERTS            extends ChatMessageType // /resetcerts
  case object U_CMT_SETBATTLERANK_OTHER  extends ChatMessageType // /setbr <player_name>
  case object U_CMT_SETCOMMANDRANK_OTHER extends ChatMessageType // /setcr <player_name>
  case object CMT_GIVE_XP                extends ChatMessageType // /givexp
  case object CMT_BLACKOPS               extends ChatMessageType // /blackops
  case object CMT_GRANT_BFR_IMPRINT      extends ChatMessageType // /grant_bfr_imprint
  case object CMT_WARP                   extends ChatMessageType // /warp
  case object CMT_SETPOPULATIONCAP       extends ChatMessageType // /popcap
  case object U_CMT_SHUTDOWN             extends ChatMessageType // /shutdown "Shutdown"
  case object CMT_MEMORYLOG              extends ChatMessageType // /memorylog
  case object U_CMT_ZONEROTATE           extends ChatMessageType // /zonerotate
  case object CMT_SHIFTER_ENABLE         extends ChatMessageType // /shifteron
  case object CMT_SHIFTER_DISABLE        extends ChatMessageType // /shifteroff
  case object CMT_SHIFTER_CREATE         extends ChatMessageType // /shiftercreate
  case object CMT_SHIFTER_CLEAR          extends ChatMessageType // /shifterclear
  case object CMT_MODULE_SPAWN_STAGGER   extends ChatMessageType // /modulespawnstagger OR /stagger
  case object CMT_MODULE_SPAWN_POP       extends ChatMessageType // /modulespawnpop OR /pop
  case object CMT_PROPERTY_OVERRIDE      extends ChatMessageType // /setprop
  case object CMT_PROPERTY_OVERRIDE_ZONE extends ChatMessageType // /setpropzone
  case object CMT_SET_EMPIRE_BENEFIT     extends ChatMessageType // /setempirebenefit
  case object CMT_REMOVE_EMPIRE_BENEFIT  extends ChatMessageType // /removeempirebenefit
  case object CMT_SET_DEFCON_LEVEL       extends ChatMessageType // /setdefconlevel
  case object GET_DEFCON_TIME            extends ChatMessageType // /get_defcon_time
  case object CMT_RELOAD_ACTOFGOD_INFO   extends ChatMessageType // /reload_actofgod
  case object CMT_SET_DEFCON_WEIGHT      extends ChatMessageType // /setdefconweight
  case object CMT_SET_DEFCON_EVENT_NAME  extends ChatMessageType // /setdefconeventname
  case object CMT_EARTHQUAKE             extends ChatMessageType // /earthquake
  case object CMT_METEOR_SHOWER          extends ChatMessageType // /meteor_shower
  case object CMT_SPAWN_MONOLITH         extends ChatMessageType // /spawn_monolith
  case object CMT_PKG                    extends ChatMessageType // /pkg
  case object CMT_ZONECOMMAND            extends ChatMessageType // /zcommand
  case object CMT_ZONESTART              extends ChatMessageType // /zstart
  case object CMT_ZONESTOP               extends ChatMessageType // /zstop
  case object CMT_ZONELOCK               extends ChatMessageType // /zlock
  case object CMT_BOLOCK                 extends ChatMessageType // /bolock
  case object CMT_TRAINING_ZONE          extends ChatMessageType // /train
  case object CMT_ZONE                   extends ChatMessageType // /zone
  case object CMT_SHOWDAMAGE             extends ChatMessageType // /showdamage
  case object CMT_EMPIREINCENTIVES       extends ChatMessageType // /empireincentives
  case object CMT_MINE                   extends ChatMessageType // /mine
  case object CMT_BATTLEPLANPUBLISH      extends ChatMessageType // /publish
  case object CMT_BATTLEPLANUNPUBLISH    extends ChatMessageType // /unpublish
  case object CMT_BATTLEPLANSUBSCRIBE    extends ChatMessageType // /subscribe
  case object CMT_BATTLEPLANUNSUBSCRIBE  extends ChatMessageType // /unsubscribe
  case object CMT_TEST_BLDG_MODULES      extends ChatMessageType // /testmodule
  case object CMT_SQUAD_TEST             extends ChatMessageType // /squadtest
  case object CMT_HOTSPOT                extends ChatMessageType // /hotspot OR /hot
  case object CMT_SHIFTER_SAVE           extends ChatMessageType // /shiftersave
  case object CMT_SHIFTER_LOAD           extends ChatMessageType // /shifterload
  case object CMT_LIST_ASSETS            extends ChatMessageType // /listassets
  case object CMT_CHECK_PROPERTIES       extends ChatMessageType // /checkprops
  case object CMT_PROPERTY_QUERY         extends ChatMessageType // /queryprop
  case object CMT_INACTIVITYTIMEOUT      extends ChatMessageType // /inactivitytimeout
  case object CMT_SHOW_XPSPLIT           extends ChatMessageType // /xpsplit
  case object CMT_XPDIST                 extends ChatMessageType // /xpdist
  case object CMT_SHOWXPDIST             extends ChatMessageType // /showxpdist
  case object CMT_ENTITYREPORT           extends ChatMessageType // /entityreport
  case object CMT_START_MISSION          extends ChatMessageType // /startmission
  case object CMT_RESET_MISSION          extends ChatMessageType // /resetmission
  case object CMT_CANCEL_MISSION         extends ChatMessageType // /cancelmission
  case object CMT_COMPLETE_MISSION       extends ChatMessageType // /completemission
  case object CMT_GOTO_MISSION_STEP      extends ChatMessageType // /gotomissionstep
  case object CMT_SHOW_MISSION_TRIGGERS  extends ChatMessageType // /showmissiontriggers
  case object CMT_ADD_VANUMODULE         extends ChatMessageType // /moduleadd
  case object CMT_REMOVE_VANUMODULE      extends ChatMessageType // /moduleremove OR /modulerm
  case object CMT_DEBUG_MASSIVE          extends ChatMessageType // /debugmassive
  case object CMT_WARP_TO_NEXT_BILLBOARD extends ChatMessageType // ???
  case object UNK_222                    extends ChatMessageType // ??? "CTF Flag stolen"
  case object UNK_223                    extends ChatMessageType // ??? "CTF Flag lost"
  case object UNK_224                    extends ChatMessageType // ??? "Vehicle Dismount"
  case object UNK_225                    extends ChatMessageType // ??? empty
  case object UNK_226                    extends ChatMessageType // ??? empty
  case object UNK_227                    extends ChatMessageType // ??? empty
  case object UNK_228                    extends ChatMessageType // ??? empty
  case object UNK_229                    extends ChatMessageType // ??? empty
  case object UNK_230                    extends ChatMessageType // ??? "Vehicle Mount"
  case object UNK_231                    extends ChatMessageType // ??? empty
  case object UNK_232                    extends ChatMessageType // ??? empty
  case object CMT_ALARM                  extends ChatMessageType // /alarm
  case object CMT_APPEAL                 extends ChatMessageType // /appeal
  case object CMT_BUGREPORT              extends ChatMessageType // /bug
  case object CMT_CHATLOG                extends ChatMessageType // /log
  case object CMT_CREATE_MACRO           extends ChatMessageType // /macro
  case object CMT_EMOTE                  extends ChatMessageType // /emote OR /em
  case object CMT_FILTER                 extends ChatMessageType // /filter
  case object CMT_FRIENDS                extends ChatMessageType // /friends
  case object CMT_IGNORE                 extends ChatMessageType // /ignore
  case object CMT_HELP                   extends ChatMessageType // /help OR /gm
  case object CMT_LOC                    extends ChatMessageType // /loc
  case object CMT_REPLY                  extends ChatMessageType // /reply OR /r
  case object CMT_TIME                   extends ChatMessageType // /time
  case object CMT_TIMEDHELP              extends ChatMessageType // /timedhelp
  case object CMT_TOGGLE_STATS           extends ChatMessageType // /stats
  case object CMT_VERSION                extends ChatMessageType // /version
  case object CMT_INCENTIVES             extends ChatMessageType // /incentives
  case object CMT_HIDESPECTATOR          extends ChatMessageType // /hidespectator
  case object CMT_HUMBUG                 extends ChatMessageType // /humbug
  case object CMT_SOUND_HORNS            extends ChatMessageType // /horns
  case object CMT_SQUADINVITE            extends ChatMessageType // /invite OR /squadinvite
  case object CMT_SQUADKICK              extends ChatMessageType // /kick
  case object CMT_SQUADACCEPTINVITATION  extends ChatMessageType // /accept OR /yes

  /* TODO: Past this point, the types overflow 8 bits, so need a way to either map them for ChatMsg or just take them completely out

      CMT_SQUADREJECTINVITATION,    // /reject OR /no
      CMT_SQUADCANCELINVITATION,    // /cancel
      CMT_SQUADPROMOTE,             // /promote
      CMT_SQUADDISBAND,             // /disband
      CMT_SQUADLEAVE,               // /leave
      CMT_SQUADPROXIMITY,           // /proximity
      CMT_PLATOONINVITE,            // /pinvite
      CMT_PLATOONKICK,              // /pkick
      CMT_PLATOONACCEPTINVITATION,  // /paccept
      CMT_PLATOONREJECTINVITATION,  // /preject
      CMT_PLATOONCANCELINVITATION,  // /pcancel
      CMT_PLATOONDISBAND,           // /pdisband
      CMT_PLATOONLEAVE,             // /pleave
      CMT_OUTFITCREATE,             // /outfitcreate
      CMT_OUTFITCREATEFROMSQUAD,    // /outfitform
      CMT_OUTFITINVITE,             // /outfitinvite
      CMT_OUTFITKICK,               // /outfitkick
      CMT_OUTFITACCEPTINVITATION,   // /outfitaccept
      CMT_OUTFITREJECTINVITATION,   // /outfitreject
      CMT_OUTFITCANCELINVITATION,   // /outfitcancel
      CMT_OUTFITPROMOTE,            // ???
      CMT_OUTFITLEAVE,              // /outfitleave
      CMT_OUTFITTEST,               // /outfittest
      CMT_BENEFITPING,              // /benefitping
      CMT_BENEFITTEXT,              // /benefittext
      CMT_VOICE_DEBUG,              // /voice_debug
      CMT_VOICE_HOST,               // /voice_host
      CMT_VOICE_REMOTEHOST,         // /voice_remote
      CMT_VOICE_KILLHOST,           // /voice_killhost
      CMT_VOICE_CONNECT,            // /voice_connect
      CMT_VOICE_DISCONNECT,         // /voice_disconnect
      CMT_VOICE_ISCONNECTED,        // /voice_isconnected
      CMT_VOICE_ENABLEVOX,          // /voice_enablevox
      CMT_VOICE_SETVOXLEVEL,        // /voice_voxlevel
      CMT_VOICE_SETVOXDELAY,        // /voice_voxdelay
      CMT_VOICE_ENABLELOOPBACK,     // /voice_enableloopback
      CMT_VOICE_AMPLIFYIN,          // /voice_amplifyin
      CMT_VOICE_AMPLIFYOUT,         // /voice_amplifyout
      CMT_VOICE_WHO,                // /voice_who
      CMT_VOICE_SETCODEC,           // /voice_setcodec
      CMT_GMREPLY,                  // /gmreply
      CMT_SETWARPGATEEMPIRE,        // /swe
      U_CMT_SHOWPROFILE,            // /showprofile
      U_CMT_SHOWTERRAINMAP,         // /showterrainmap
      U_CMT_TACTICAL,               // /tactical
      U_CMT_CLIENTEXPERIMENTAL1,    // /clientexperimental1
      U_CMT_CLIENTEXPERIMENTAL2,    // /clientexperimental2
      CMT_UPLINKFRIENDLY,           // /showfriendly
      CMT_UPLINKENEMY,              // /showenemy
      CMT_UPLINKEMPBLAST,           // /emp
      CMT_UPLINKRESETTIMERS,        // /resetuplinktimers
      CMT_PAINTBALL,                // /paintball
      U_CMT_DUMPANIMCACHE,          // /dumpanimcache
      U_CMT_SLASH,                  // /
      UNK_310,                      // ??? empty
      UNK_311,                      // ??? empty
      UNK_312,                      // ??? empty
      UNK_313,                      // ??? empty
      UNK_314                       // ??? empty
      // Could be more types?

   */

  implicit val codec: Codec[ChatMessageType] = PacketHelpers.createEnumCodec(this, uint8L)
}

/*
    Additional comments from shaql:
      IDs other than the ones tested already:
      (ones that are actually checked by the client)

      54
      225
      94
      238
      26
      226
      230
      224
      145: chat_prefix_gmtell, target of /silence <character> [minutes]
      69, 71: uses text "Enabled = false\nParent.Cancel.Enabled\nui_hidedialog" (maybe /hidespectator?)
      92: /expansions <id> [on|off] (free play being 46 or so?)
      66: does something about on/off, "basic_string" (which seems like a generic thing)
      46: very similar to 66
      72:chat_prefix_gm, shows an OK-dialog, uses '@ERRNoWCharToCharConversion'
      45
      221
      47-50, 61
      192: /showdamage [on|off]
      231
      222, 223, 229: something about "_sound"; in some cases ends up in the 'default' ID. could be anything...
      228
      232
      130: something about on, but no off (and no .str checking), and something about a 1.0 float
      128
      129: similar to 130
      33 is handled similarly to 4-25
 */
