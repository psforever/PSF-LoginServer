// Copyright (c) 2017 PSForever
package services.avatar

import net.psforever.packet.game.PlanetSideGUID
import services.GenericEventBusMsg

final case class AvatarServiceResponse(toChannel : String,
                                       avatar_guid : PlanetSideGUID,
                                       replyMessage : AvatarResponse.Response
                                      ) extends GenericEventBusMsg
