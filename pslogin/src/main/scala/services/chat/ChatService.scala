// Copyright (c) 2017 PSForever
package services.chat

import akka.actor.Actor
import net.psforever.objects.LivePlayerList
import net.psforever.types.ChatMessageType
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
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, 2, ChatMessageType.CMT_OPEN,msg.wideContents,player_name,msg.contents,None)
          )
        case ChatAction.Tell(player_guid, player_name, msg) =>
          var good : Boolean = false
          LivePlayerList.WorldPopulation(_ => true).foreach(char => {
            if (char.Name.equalsIgnoreCase(msg.recipient)) {
              good = true
            }
          })
          if(good) {
            ChatEvents.publish(
              ChatServiceResponse(s"/Chat/$forChannel", player_guid, 0, ChatMessageType.CMT_TELL,msg.wideContents,player_name,msg.contents,None)
            )
            ChatEvents.publish(
              ChatServiceResponse(s"/Chat/$forChannel", player_guid, 1, ChatMessageType.U_CMT_TELLFROM,msg.wideContents,msg.recipient,msg.contents,None)
            )
          } else {
            ChatEvents.publish(
              ChatServiceResponse(s"/Chat/$forChannel", player_guid, 1, ChatMessageType.U_CMT_TELLFROM,msg.wideContents,msg.recipient,msg.contents,None)
            )
            ChatEvents.publish(
              ChatServiceResponse(s"/Chat/$forChannel", player_guid, 1, ChatMessageType.UNK_45,msg.wideContents,"","@NoTell_Target",None)
            )
          }
        case ChatAction.Broadcast(player_guid, cont, msg) =>
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, 2, msg.messageType,msg.wideContents,msg.recipient,msg.contents,None)
          )
        case ChatAction.Voice(player_guid, player_name, cont, msg) =>
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, 2, ChatMessageType.CMT_VOICE,false,player_name,msg.contents,None)
          )

        case ChatAction.Note(player_guid, _, _) =>
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, 1, ChatMessageType.U_CMT_GMTELLFROM,true,"Server","Why do you try to /note ? That's a GM command ! ... Or not, nobody can /note",None)
          )

        case _ => ;
      }

    case msg =>
      log.info(s"Unhandled message $msg from $sender")
  }
}
