// Copyright (c) 2019 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

object SquadResponseType extends Enumeration {
  type Type = Value
  val
  Invite,
  Unk01,
  Accept,
  Reject,
  Cancel,
  Leave,
  Disband,
  PlatoonInvite,
  PlatoonAccept,
  PlatoonReject,
  PlatoonCancel,
  PlatoonLeave,
  PlatoonDisband
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)

  def fromRequest(response : SquadResponseType.Value) : SquadRequestType.Value = {
    val id = response.id
    if(id < 6) SquadRequestType(id)
    else if(id > 5) SquadRequestType(id + 1)
    else throw new NoSuchElementException("response does not stem from an applicable request")
  }
}
