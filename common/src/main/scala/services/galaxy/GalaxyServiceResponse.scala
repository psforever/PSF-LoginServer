// Copyright (c) 2017 PSForever
package services.galaxy

import net.psforever.packet.game.PlanetSideGUID
import services.GenericEventBusMsg

final case class GalaxyServiceResponse(toChannel : String,
                                       replyMessage : GalaxyResponse.Response
                                     ) extends GenericEventBusMsg
