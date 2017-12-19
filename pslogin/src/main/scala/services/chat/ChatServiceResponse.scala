// Copyright (c) 2017 PSForever
package services.chat

import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.ChatMessageType
import services.GenericEventBusMsg

final case class ChatServiceResponse(toChannel : String,
                                      avatar_guid : PlanetSideGUID,
                                      personal : Int,
                                     messageType : ChatMessageType.Value,
                                     wideContents : Boolean,
                                     recipient : String,
                                     contents : String,
                                     note : Option[String]
                                     ) extends GenericEventBusMsg
