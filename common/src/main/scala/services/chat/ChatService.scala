// Copyright (c) 2017 PSForever
package services.chat

import akka.actor.Actor
import net.psforever.objects.LivePlayerList
import net.psforever.packet.game.{ChatMsg, PlanetSideGUID}
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
    case Service.Leave(None) =>
      ChatEvents.unsubscribe(sender())
    case Service.Leave(Some(channel)) =>
      val path = s"/Chat/$channel"
      val who = sender()
      log.info(s"$who has left $path")
      ChatEvents.unsubscribe(who, path)
    case Service.LeaveAll() =>
      ChatEvents.unsubscribe(sender())

    case ChatServiceMessage(forChannel, action) =>
      action match {
        case ChatAction.Local(player_guid, player_name, cont, player_pos, player_faction, msg) => // local
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, player_name, cont, player_pos, player_faction, 2, ChatMsg(ChatMessageType.CMT_OPEN,msg.wideContents,player_name,msg.contents,None))
          )
        case ChatAction.Tell(player_guid, player_name, msg) => // tell
          var good : Boolean = false
          LivePlayerList.WorldPopulation(_ => true).foreach(char => {
            if (char.name.equalsIgnoreCase(msg.recipient)) {
              good = true
            }
          })
          if(good) {
            ChatEvents.publish(
              ChatServiceResponse(s"/Chat/$forChannel", player_guid, player_name, target = 0, replyMessage = ChatMsg(ChatMessageType.CMT_TELL,msg.wideContents,msg.recipient,msg.contents,None))
            )
            ChatEvents.publish(
              ChatServiceResponse(s"/Chat/$forChannel", player_guid, player_name, target = 1, replyMessage = ChatMsg(ChatMessageType.U_CMT_TELLFROM,msg.wideContents,msg.recipient,msg.contents,None))
            )
          } else {
            ChatEvents.publish(
              ChatServiceResponse(s"/Chat/$forChannel", player_guid, player_name, target = 1, replyMessage = ChatMsg(ChatMessageType.U_CMT_TELLFROM,msg.wideContents,msg.recipient,msg.contents,None))
            )
            ChatEvents.publish(
              ChatServiceResponse(s"/Chat/$forChannel", player_guid, player_name, target = 1, replyMessage = ChatMsg(ChatMessageType.UNK_45,msg.wideContents,"","@NoTell_Target",None))
            )
          }
        case ChatAction.Broadcast(player_guid, player_name, cont, player_pos, player_faction, msg) => // broadcast
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, player_name, cont, player_pos, player_faction, 2, ChatMsg(msg.messageType,msg.wideContents,player_name,msg.contents,None))
          )
        case ChatAction.Voice(player_guid, player_name, cont, player_pos, player_faction, msg) => // voice
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, player_name, cont, player_pos, player_faction, 2, ChatMsg(ChatMessageType.CMT_VOICE,false,player_name,msg.contents,None))
          )

        case ChatAction.Note(player_guid, player_name, msg) => // note
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, player_name, target = 1, replyMessage = ChatMsg(ChatMessageType.U_CMT_GMTELLFROM,true, msg.recipient,msg.contents,None))
          )
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, player_name, target = 1, replyMessage = ChatMsg(ChatMessageType.CMT_GMTELL,true,"Server","Why do you try to /note ? That's a GM command ! ... Or not, nobody can /note",None))
          )
        case ChatAction.Squad(player_guid, player_name, cont, player_pos, player_faction, msg) => // squad
          ChatEvents.publish(
            ChatServiceResponse(s"/Chat/$forChannel", player_guid, player_name, cont, player_pos, player_faction, 2, ChatMsg(ChatMessageType.CMT_SQUAD,msg.wideContents,player_name,msg.contents,None))
          )
        case ChatAction.GM(player_guid, player_name, msg) => // GM
          msg.messageType match {
            case ChatMessageType.CMT_SILENCE =>
              ChatEvents.publish(
                ChatServiceResponse(s"/Chat/$forChannel", player_guid, msg.contents, target = 0, replyMessage = ChatMsg(ChatMessageType.CMT_SILENCE, true, "", "", None))
              )
//              if(player_guid != PlanetSideGUID(0)) {
//
//                val args = msg.contents.split(" ")
//                var silence_name : String = ""
//                var silence_time : Int = 5
//                if (args.length == 1) {
//                  silence_name = args(0)
//                }
//                else if (args.length == 2) {
//                  silence_name = args(0)
//                  silence_time = args(1).toInt
//                }
//                ChatEvents.publish(
//                  ChatServiceResponse(s"/Chat/$forChannel", player_guid, player_name, target = 1, replyMessage = ChatMsg(ChatMessageType.UNK_45, true, "", silence_name + " silenced for " + silence_time + " min(s)", None))
//                )
//              }
            case _ => ;
          }
        case _ => ;
      }

    case msg =>
      log.info(s"Unhandled message $msg from $sender")
  }
}