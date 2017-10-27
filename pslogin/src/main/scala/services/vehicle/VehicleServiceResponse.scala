// Copyright (c) 2017 PSForever
package services.vehicle

import net.psforever.packet.game.PlanetSideGUID
import services.GenericEventBusMsg

final case class VehicleServiceResponse(toChannel : String,
                                        avatar_guid : PlanetSideGUID,
                                        replyMessage : VehicleResponse.Response
                                       ) extends GenericEventBusMsg

