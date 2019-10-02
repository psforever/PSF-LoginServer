// Copyright (c) 2019 PSForever
package services.teamwork

import services.GenericEventBusMsg

final case class SquadServiceResponse(toChannel : String, exclude : Iterable[Long], response : SquadResponse.Response) extends GenericEventBusMsg

object SquadServiceResponse {
  def apply(toChannel : String, response : SquadResponse.Response) : SquadServiceResponse =
    SquadServiceResponse(toChannel, Nil, response)

  def apply(toChannel : String, exclude : Long, response : SquadResponse.Response) : SquadServiceResponse =
    SquadServiceResponse(toChannel, Seq(exclude), response)
}
