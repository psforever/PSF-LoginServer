// Copyright (c) 2017 PSForever
package services.local

import net.psforever.packet.game.PlanetSideGUID
import services.GenericEventBusMsg

final case class LocalServiceResponse(toChannel : String,
                                      avatar_guid : PlanetSideGUID,
                                      replyMessage : LocalResponse.Response
                                     ) extends GenericEventBusMsg
