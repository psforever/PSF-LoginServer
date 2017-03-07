// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
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
object ChatMessageType extends Enumeration {
  type Type = Value
  val UNK_0,                        // ???
      CMT_ALLIANCE,                 // ??? unused
      CMT_BATTLEGROUP,              // /bg (not working???)
      CMT_BROADCAST,                // /broadcast OR /b
      CMT_COMMAND,                  // /command OR /c
      CMT_COMMAND_ALLZONES,         // /comall
      CMT_COMMAND_REPORT,           // /sitrep OR /situationreport
      CMT_COMMAND_SANCTUARY,        // /comsanctuary OR /comsan
      CMT_COMMAND_STATION,          // ??? unused
      CMT_COMMAND_CAVERN1,          // /comsu OR /comsupai
      CMT_COMMAND_CAVERN2,          // /comhu OR /comhunhau
      CMT_COMMAND_CAVERN3,          // /comad OR /comadlivun
      CMT_COMMAND_CAVERN4,          // /comby OR /combyblos
      CMT_COMMAND_CAVERN5,          // /coman OR /comannwn
      CMT_COMMAND_CAVERN6,          // /comdr OR /comdrugaskan
      CMT_COMMAND_ZONE1,            // /comso OR /comsolsar
      CMT_COMMAND_ZONE2,            // /comho OR /comhossin
      CMT_COMMAND_ZONE3,            // /comcy OR /comcyssor
      CMT_COMMAND_ZONE4,            // /comis OR /comishundar
      CMT_COMMAND_ZONE5,            // /comfo OR /comforseral
      CMT_COMMAND_ZONE6,            // /comce OR /comceryshen
      CMT_COMMAND_ZONE7,            // /comes OR /comesamir
      CMT_COMMAND_ZONE8,            // /comos OR /comoshur
      CMT_COMMAND_ZONE8_PRIME,      // /compr OR /comoshurprime
      CMT_COMMAND_ZONE9,            // /comse OR /comsearhus
      CMT_COMMAND_ZONE10,           // /comam OR /comamerish
      CMT_OPEN,                     // /local OR /l
      CMT_OUTFIT,                   // /outfit OR /o
      CMT_PLATOON,                  // /platoon OR /p
      CMT_PLATOONLEADER,            // /platoonleader OR /pl
      CMT_SQUAD,                    // /squad OR /s
      CMT_SQUADLEADER,              // /sl
      CMT_TELL,                     // /tell OR /t
      U_CMT_BLACKOPS_CHAT,          // ??? No slash command?
      CMT_GMBROADCAST,              // /gmbroadcast
      CMT_GMBROADCAST_NC,           // /ncbroadcast
      CMT_GMBROADCAST_TR,           // /trbroadcast
      CMT_GMBROADCAST_VS,           // /vsbroadcast
      CMT_GMBROADCASTWORLD,         // /worldbroadcast OR /wb
      CMT_GMOPEN,                   // /gmlocal
      CMT_GMTELL,                   // /gmtell (actually causes normal /tell 0x20 when not a gm???)
      CMT_NOTE,                     // /note
      CMT_GMBROADCASTPOPUP,         // /gmpopup
      U_CMT_GMTELLFROM,             // ??? Recipient of /gmtell?
      U_CMT_TELLFROM,               // ??? Recipient of /t?
      UNK_45,                       // ??? empty
      CMT_CULLWATERMARK,            // ??? This actually causes the client to ping back to the server with some stringified numbers "80 120" (with the same 46 chatmsg causing infinite loop?) - may be incorrect decoding
      CMT_INSTANTACTION,            // /instantaction OR /ia
      CMT_RECALL,                   // /recall
      CMT_OUTFIT_RECALL,            // /outfitrecall
      CMT_SQUAD_REMATRIX,           // ???
      CMT_OUTFITRENAME_USER,        // /outfitrename
      CMT_RENAME_USER,              // /rename
      CMT_REPORTUSER,               // /report
      CMT_VOICE,                    // quickchat (v-- commands)
      CMT_WHO,                      // /who
      CMT_WHO_CSR,                  // /whocsr OR /whogm
      CMT_WHO_PLATOONLEADERS,       // /whoplatoonleaders OR /whopl
      CMT_WHO_SQUADLEADERS,         // /whosquadleaders OR /whosl
      CMT_WHO_TEAMS,                // /whoteams OR /whoempires
      CMT_WHO_CR,                   // /who cr<#>
      CMT_QUIT,                     // /quit OR /q
      CMT_HIDE_HELMET,              // /hide_helmet OR /helmet
      CMT_TOGGLE_HAT,               // /hat
      CMT_TOGGLE_SHADES,            // /shades
      CMT_TOGGLE_EARPIECE,          // /earpiece
      CMT_TOGGLE_GM,                // /gmtoggle
      CMT_ANONYMOUS,                // /anon OR /anonymous
      CMT_DESTROY,                  // /destroy
      CMT_KICK,                     // /worldkick
      CMT_KICK_BY_ID,               // /worldkickid
      UNK_71,                       // ??? empty (though the game elsewhere handles it similarly to 69!)
      CMT_LOCKSERVER,               // /lockserver
      CMT_UNLOCKSERVER,             // /unlockserver
      CMT_CAPTUREBASE,              // /capturebase
      CMT_CREATE,                   // /create
      CMT_HACK_DOORS,               // /hackdoors
      CMT_OUTFITCLAIM,              // /outfitclaim
      CMT_OUTFITUNCLAIM,            // /outfitunclaim
      CMT_SETBASERESOURCES,         // /setbaseresources
      U_CMT_SETHEALTH_TARGET,       // /sethealth t
      U_CMT_SETARMOR_TARGET,        // /setarmor t
      CMT_SETVEHICLERESOURCES,      // /setvehicleresources
      CMT_SUPER_CREATE,             // /supercreate OR /sc
      CMT_VORTEX_MODULE,            // /vortex OR /vtx
      U_CMT_RESPAWNAMS,             // /respawnams ("Create AMS Resp")
      CMT_ADDBATTLEEXPERIENCE,      // /addbep
      CMT_ADDCERTIFICATION,         // /certadd
      CMT_ADDCOMMANDEXPERIENCE,     // /addcep
      CMT_ADDIMPLANT,               // /addimplant
      CMT_ARMOR,                    // /armor
      CMT_ENABLEIMPLANT,            // /enableimplant
      CMT_EXPANSIONS,               // /expansions
      CMT_FIRST_TIME_EVENTS,        // /firsttime OR /fte
      CMT_INVENTORYLAYOUT,          // /inventory
      CMT_REMOVECERTIFICATION,      // /certrm
      CMT_REMOVEIMPLANT,            // /removeimplant 
      CMT_SAVE,                     // /save
      CMT_SETBATTLEEXPERIENCE,      // /setbep
      CMT_SETBATTLERANK,            // /setbr
      CMT_SETCAPACITANCE,           // /setcapacitance
      CMT_SETCOMMANDEXPERIENCE,     // /setcep
      CMT_SETCOMMANDRANK,           // /setcr
      CMT_SETFAVORITE,              // /setfavorite
      CMT_SETHEALTH,                // /sethealth
      CMT_SETARMOR,                 // /setarmor
      CMT_SETIMPLANTSLOTS,          // /setimplantslots
      CMT_SETSTAMINA,               // /setstamina
      CMT_SUICIDE,                  // /suicide
      CMT_USEFAVORITE,              // /favorite
      CMT_TOGGLERESPAWNPENALTY,     // /togglerespawnpenalty
      CMT_SETAMMO,                  // /setammo
      CMT_AWARD_QUALIFY,            // /award_qualify
      CMT_AWARD_PROGRESS,           // /award_progress
      CMT_AWARD_ADD,                // /award_add
      CMT_AWARD_REMOVE,             // /award_remove
      CMT_STAT_ADD,                 // /stat_add
      CMT_BFR_SETCAVERNCAPS,        // /bfr_setcaverncaps
      CMT_BFR_SETCAVERNKILLS,       // /bfr_setcavernkills
      CMT_BFR_IMPRINT,              // /bfr_imprint
      CMT_BFR_DAMAGE,               // /bfrdamage
      CMT_BFR_DAMAGEWEAPON,         // /bfrdamageweapon
      CMT_RESETPURCHASETIMERS,      // /resetpurchasetimers
      CMT_EVENTPLAYTIME,            // /eventplaytime
      CMT_SETTIME,                  // /settime
      CMT_SETTIMESPEED,             // /settimespeed
      CMT_SNOOP,                    // /snoop
      CMT_SOULMARK,                 // /soulmark
      CMT_FLY,                      // /fly
      CMT_SPEED,                    // /speed
      CMT_TOGGLESPECTATORMODE,      // /spectator
      CMT_INFO,                     // /info
      CMT_SHOWZONES,                // /showzones
      CMT_SYNC,                     // /sync
      CMT_SECURITY,                 // /security
      CMT_FIND,                     // /find
      CMT_OUTFITCONFIRM,            // /outfitconfirm
      CMT_OUTFITDELETE,             // /outfitdelete
      CMT_OUTFITRENAME_GM,          // /outfitrenamegm
      CMT_OUTFITLEADER,             // /outfitleader
      CMT_OUTFITPOINTS,             // /outfitpoints
      CMT_OUTFITPOINTS_CHARACTER,   // /giveop
      CMT_OUTFITCREDITS,            // /outfitcredits
      CMT_RENAME_GM,                // /renamegm
      CMT_SETGRIEF,                 // /setgrief
      CMT_SILENCE,                  // /silence
      CMT_SILENCE_OUTFIT,           // /silenceoutfit
      CMT_COMMAND_SILENCE,          // /commandsilence
      CMT_COMMAND_SILENCE_OUTFIT,   // /commandsilenceoutfit
      CMT_COMMAND_SILENCE_EMPIRE,   // /commandsilenceempire
      CMT_SUMMON,                   // /summon
      CMT_AWARD,                    // /award
      U_CMT_AWARD_ZONE,             // /award
      CMT_AWARD_REVOKE,             // /award_revoke
      U_CMT_AWARD_REVOKE_ZONE,      // /award_revoke
      CMT_RESET_CERTS,              // /resetcerts
      U_CMT_SETBATTLERANK_OTHER,    // /setbr <player_name>
      U_CMT_SETCOMMANDRANK_OTHER,   // /setcr <player_name>
      CMT_GIVE_XP,                  // /givexp
      CMT_BLACKOPS,                 // /blackops
      CMT_GRANT_BFR_IMPRINT,        // /grant_bfr_imprint
      CMT_WARP,                     // /warp 
      CMT_SETPOPULATIONCAP,         // /popcap
      U_CMT_SHUTDOWN,               // /shutdown "Shutdown"
      CMT_MEMORYLOG,                // /memorylog
      U_CMT_ZONEROTATE,             // /zonerotate
      CMT_SHIFTER_ENABLE,           // /shifteron
      CMT_SHIFTER_DISABLE,          // /shifteroff
      CMT_SHIFTER_CREATE,           // /shiftercreate
      CMT_SHIFTER_CLEAR,            // /shifterclear
      CMT_MODULE_SPAWN_STAGGER,     // /modulespawnstagger OR /stagger
      CMT_MODULE_SPAWN_POP,         // /modulespawnpop OR /pop
      CMT_PROPERTY_OVERRIDE,        // /setprop
      CMT_PROPERTY_OVERRIDE_ZONE,   // /setpropzone
      CMT_SET_EMPIRE_BENEFIT,       // /setempirebenefit
      CMT_REMOVE_EMPIRE_BENEFIT,    // /removeempirebenefit
      CMT_SET_DEFCON_LEVEL,         // /setdefconlevel
      GET_DEFCON_TIME,              // /get_defcon_time
      CMT_RELOAD_ACTOFGOD_INFO,     // /reload_actofgod
      CMT_SET_DEFCON_WEIGHT,        // /setdefconweight
      CMT_SET_DEFCON_EVENT_NAME,    // /setdefconeventname
      CMT_EARTHQUAKE,               // /earthquake
      CMT_METEOR_SHOWER,            // /meteor_shower
      CMT_SPAWN_MONOLITH,           // /spawn_monolith
      CMT_PKG,                      // /pkg
      CMT_ZONECOMMAND,              // /zcommand
      CMT_ZONESTART,                // /zstart
      CMT_ZONESTOP,                 // /zstop
      CMT_ZONELOCK,                 // /zlock
      CMT_BOLOCK,                   // /bolock
      CMT_TRAINING_ZONE,            // /train
      CMT_ZONE,                     // /zone
      CMT_SHOWDAMAGE,               // /showdamage
      CMT_EMPIREINCENTIVES,         // /empireincentives
      CMT_MINE,                     // /mine
      CMT_BATTLEPLANPUBLISH,        // /publish
      CMT_BATTLEPLANUNPUBLISH,      // /unpublish
      CMT_BATTLEPLANSUBSCRIBE,      // /subscribe
      CMT_BATTLEPLANUNSUBSCRIBE,    // /unsubscribe
      CMT_TEST_BLDG_MODULES,        // /testmodule
      CMT_SQUAD_TEST,               // /squadtest
      CMT_HOTSPOT,                  // /hotspot OR /hot
      CMT_SHIFTER_SAVE,             // /shiftersave
      CMT_SHIFTER_LOAD,             // /shifterload
      CMT_LIST_ASSETS,              // /listassets
      CMT_CHECK_PROPERTIES,         // /checkprops
      CMT_PROPERTY_QUERY,           // /queryprop
      CMT_INACTIVITYTIMEOUT,        // /inactivitytimeout
      CMT_SHOW_XPSPLIT,             // /xpsplit
      CMT_XPDIST,                   // /xpdist
      CMT_SHOWXPDIST,               // /showxpdist
      CMT_ENTITYREPORT,             // /entityreport
      CMT_START_MISSION,            // /startmission
      CMT_RESET_MISSION,            // /resetmission
      CMT_CANCEL_MISSION,           // /cancelmission
      CMT_COMPLETE_MISSION,         // /completemission
      CMT_GOTO_MISSION_STEP,        // /gotomissionstep
      CMT_SHOW_MISSION_TRIGGERS,    // /showmissiontriggers
      CMT_ADD_VANUMODULE,           // /moduleadd
      CMT_REMOVE_VANUMODULE,        // /moduleremove OR /modulerm
      CMT_DEBUG_MASSIVE,            // /debugmassive
      CMT_WARP_TO_NEXT_BILLBOARD,   // ???
      UNK_222,                      // ??? "CTF Flag stolen"
      UNK_223,                      // ??? "CTF Flag lost"
      UNK_224,                      // ??? "Vehicle Dismount"
      UNK_225,                      // ??? empty
      UNK_226,                      // ??? empty
      UNK_227,                      // ??? empty
      UNK_228,                      // ??? empty
      UNK_229,                      // ??? empty
      UNK_230,                      // ??? "Vehicle Mount"
      UNK_231,                      // ??? empty
      UNK_232,                      // ??? empty
      CMT_ALARM,                    // /alarm
      CMT_APPEAL,                   // /appeal
      CMT_BUGREPORT,                // /bug
      CMT_CHATLOG,                  // /log
      CMT_CREATE_MACRO,             // /macro
      CMT_EMOTE,                    // /emote OR /em
      CMT_FILTER,                   // /filter
      CMT_FRIENDS,                  // /friends
      CMT_IGNORE,                   // /ignore
      CMT_HELP,                     // /help OR /gm
      CMT_LOC,                      // /loc
      CMT_REPLY,                    // /reply OR /r
      CMT_TIME,                     // /time
      CMT_TIMEDHELP,                // /timedhelp
      CMT_TOGGLE_STATS,             // /stats
      CMT_VERSION,                  // /version
      CMT_INCENTIVES,               // /incentives
      CMT_HIDESPECTATOR,            // /hidespectator
      CMT_HUMBUG,                   // /humbug
      CMT_SOUND_HORNS,              // /horns
      CMT_SQUADINVITE,              // /invite OR /squadinvite
      CMT_SQUADKICK,                // /kick
      CMT_SQUADACCEPTINVITATION     // /accept OR /yes

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

      = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint8L)
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
