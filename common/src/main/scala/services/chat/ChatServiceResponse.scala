// Copyright (c) 2017 PSForever
package services.chat

import net.psforever.objects.zones.Zone
import net.psforever.packet.game.ChatMsg
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}
import services.GenericEventBusMsg

final case class ChatServiceResponse(toChannel : String,
                                     avatar_guid : PlanetSideGUID,
                                     avatar_name : String,
                                     cont : Zone = Zone.Nowhere,
                                     avatar_pos : Vector3 = Vector3(0f,0f,0f),
                                     avatar_faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL,

                                     target : Int,
                                     replyMessage : ChatMsg
                                    ) extends GenericEventBusMsg