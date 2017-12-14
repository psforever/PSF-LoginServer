// Copyright (c) 2017 PSForever
package services.chat

import akka.actor.Actor
import services.{GenericEventBus, Service}

class ChatService extends Actor {
  private [this] val log = org.log4s.getLogger

  override def preStart = {
    log.info("Starting....")
  }

  val ChatEvents = new GenericEventBus[ChatServiceResponse]

  def receive = {
    case Service.Join(channel) =>
      val path = s"/Chat/$channel"
      val who = sender()
      log.info(s"$who has joined $path")
      ChatEvents.subscribe(who, path)
    case Service.Leave() =>
      ChatEvents.unsubscribe(sender())
    case Service.LeaveAll() =>
      ChatEvents.unsubscribe(sender())

    case ChatServiceMessage(forChannel, action) =>
      action match {
        case ChatAction.Local(player_guid, player_name, cont, msg) =>
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, ChatResponse.Local(player_name, msg.messageType,msg.wideContents,msg.recipient,msg.contents,msg.note))
          )
        case ChatAction.Tell(player_guid, cont, msg) =>
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, ChatResponse.Tell(msg.messageType,msg.wideContents,msg.recipient,msg.contents,msg.note))
          )
        case ChatAction.Broadcast(player_guid, cont, msg) =>
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, ChatResponse.Broadcast(msg.messageType,msg.wideContents,msg.recipient,msg.contents,msg.note))
          )
        case ChatAction.Voice(player_guid, cont, msg) =>
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, ChatResponse.Voice(msg.messageType,msg.wideContents,msg.recipient,msg.contents,msg.note))
          )
        case _ => ;
      }

    case msg =>
      log.info(s"Unhandled message $msg from $sender")
  }
}
