// Copyright (c) 2019 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

object SquadRequestType extends Enumeration {
  type Type = Value
  val
  Invite,
  ProximityInvite,
  Accept,
  Reject,
  Cancel,
  Leave,
  Promote,
  Disband,
  PlatoonInvite,
  PlatoonAccept,
  PlatoonReject,
  PlatoonCancel,
  PlatoonLeave,
  PlatoonDisband
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)

  def toResponse(request : SquadRequestType.Value) : SquadResponseType.Value = {
    val id = request.id
    if(id < 6) SquadResponseType(id)
    else if(id > 6) SquadResponseType(id - 1)
    else throw new NoSuchElementException("request does not have an applicable response")
  }
}
