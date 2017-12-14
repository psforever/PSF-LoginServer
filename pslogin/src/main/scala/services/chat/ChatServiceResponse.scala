// Copyright (c) 2017 PSForever
package services.chat

import net.psforever.packet.game.PlanetSideGUID
import services.GenericEventBusMsg

final case class ChatServiceResponse(toChannel : String,
                                      avatar_guid : PlanetSideGUID,
                                      replyMessage : ChatResponse.Response
                                     ) extends GenericEventBusMsg
