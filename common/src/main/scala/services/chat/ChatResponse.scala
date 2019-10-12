// Copyright (c) 2017 PSForever
package services.chat

import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.ChatMessageType

object ChatResponse {
  trait Response

  final case class Local(sender : String, messageType : ChatMessageType.Value, wideContents : Boolean, recipient : String, contents : String, note : Option[String]) extends Response
  final case class Tell(sender : String, messageType : ChatMessageType.Value, wideContents : Boolean, recipient : String, contents : String, note : Option[String]) extends Response
  final case class UTell(sender : String, messageType : ChatMessageType.Value, wideContents : Boolean, recipient : String, contents : String, note : Option[String]) extends Response
  final case class Broadcast(messageType : ChatMessageType.Value, wideContents : Boolean, recipient : String, contents : String, note : Option[String]) extends Response
  final case class Voice(messageType : ChatMessageType.Value, wideContents : Boolean, recipient : String, contents : String, note : Option[String]) extends Response
  final case class Unk45(sender : String, messageType : ChatMessageType.Value, wideContents : Boolean, recipient : String, contents : String, note : Option[String]) extends Response
  final case class Squad(sender : String, messageType : ChatMessageType.Value, wideContents : Boolean, recipient : String, contents : String, note : Option[String]) extends Response

  final case class Text(toChannel : String, avatar_guid : PlanetSideGUID, personal : Int, messageType : ChatMessageType.Value, wideContents : Boolean, recipient : String, contents : String, note : Option[String])
}