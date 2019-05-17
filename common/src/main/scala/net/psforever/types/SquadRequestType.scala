// Copyright (c) 2019 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

object SquadRequestType extends Enumeration {
  type Type = Value
  val Invite,   //00
  Unk01,    //01
  Accept,   //02
  Reject,   //03
  Cancel,   //04
  Leave,    //05
  Promote,  //06
  Disband,  //07
  PInvite,  //08
  PAccept,  //09
  PReject,  //10
  PCancel,  //11
  PLeave,   //12
  PDisband, //13
  Unk14,    //14
  Unk15     //15
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}
