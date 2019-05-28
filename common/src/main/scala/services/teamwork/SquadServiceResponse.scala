// Copyright (c) 2019 PSForever
package services.teamwork

import net.psforever.packet.game.SquadInfo
import services.GenericEventBusMsg

final case class SquadServiceResponse(toChannel : String, response : SquadResponse.Response) extends GenericEventBusMsg
