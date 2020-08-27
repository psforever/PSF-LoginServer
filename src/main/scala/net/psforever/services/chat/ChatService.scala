// Copyright (c) 2017 PSForever
package net.psforever.services.chat

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import net.psforever.objects.Session
import net.psforever.packet.game.ChatMsg
import net.psforever.types.{ChatMessageType, PlanetSideEmpire, PlanetSideGUID}

object ChatService {
  val ChatServiceKey: ServiceKey[Command] = ServiceKey[ChatService.Command]("chatService")

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      context.system.receptionist ! Receptionist.Register(ChatServiceKey, context.self)
      new ChatService(context)
    }

  sealed trait Command

  final case class JoinChannel(actor: ActorRef[MessageResponse], session: Session, channel: ChatChannel) extends Command
  final case class LeaveChannel(actor: ActorRef[MessageResponse], channel: ChatChannel)                  extends Command
  final case class LeaveAllChannels(actor: ActorRef[MessageResponse])                                    extends Command

  final case class Message(session: Session, message: ChatMsg, channel: ChatChannel) extends Command
  final case class MessageResponse(session: Session, message: ChatMsg, channel: ChatChannel)

  trait ChatChannel
  object ChatChannel {
    // one of the default channels that the player is always subscribed to (local, broadcast, command...)
    final case class Default()                   extends ChatChannel
    final case class Squad(guid: PlanetSideGUID) extends ChatChannel
  }

}

class ChatService(context: ActorContext[ChatService.Command]) extends AbstractBehavior[ChatService.Command](context) {

  import ChatService._
  import ChatMessageType._

  private[this] val log                = org.log4s.getLogger
  var subscriptions: List[JoinChannel] = List[JoinChannel]()

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case subscription: JoinChannel =>
        subscriptions ++= List(subscription)
        this

      case LeaveChannel(actor, channel) =>
        subscriptions = subscriptions.filter {
          case JoinChannel(a, _, c) => actor != a && channel != c
        }
        this

      case LeaveAllChannels(actor) =>
        subscriptions = subscriptions.filter {
          case JoinChannel(a, _, _) => actor != a
        }
        this

      case Message(session, message, channel) =>
        (channel, message.messageType) match {
          case (ChatChannel.Squad(_), CMT_SQUAD)                                =>
          case (ChatChannel.Default(), messageType) if messageType != CMT_SQUAD =>
          case _ =>
            log.error(s"invalid chat channel $channel for messageType ${message.messageType}")
            return this
        }
        val subs = subscriptions.filter(_.channel == channel)
        message.messageType match {
          case CMT_TELL | CMT_GMTELL =>
            subs.find(_.session.player.Name == session.player.Name).foreach {
              case JoinChannel(sender, _, _) =>
                sender ! MessageResponse(
                  session,
                  message.copy(messageType = if (message.messageType == CMT_TELL) U_CMT_TELLFROM else U_CMT_GMTELLFROM),
                  channel
                )
                subs.find(_.session.player.Name.toLowerCase() == message.recipient.toLowerCase()) match {
                  case Some(JoinChannel(receiver, _, _)) =>
                    receiver ! MessageResponse(session, message.copy(recipient = session.player.Name), channel)
                  case None =>
                    sender ! MessageResponse(
                      session,
                      ChatMsg(ChatMessageType.UNK_45, wideContents = false, "", "@NoTell_Target", None),
                      channel
                    )
                }

            }

          case CMT_SILENCE =>
            val args = message.contents.split(" ")
            val (name, time, error) = (args.lift(0), args.lift(1)) match {
              case (Some(name), None) => (Some(name), Some(5), None)
              case (Some(name), Some(time)) =>
                time.toIntOption match {
                  case Some(time) =>
                    (Some(name), Some(time), None)
                  case None =>
                    (None, None, Some("bad time format"))
                }
              case _ => (None, None, None)
            }

            val sender = subs.find(_.session.player.Name == session.player.Name)

            (sender, name, time, error) match {
              case (Some(sender), Some(name), Some(_), None) =>
                val recipient = subs.find(_.session.player.Name == name)
                recipient match {
                  case Some(recipient) =>
                    if (recipient.session.player.silenced) {
                      sender.actor ! MessageResponse(
                        session,
                        ChatMsg(UNK_229, true, "", "@silence_disabled_ack", None),
                        channel
                      )
                    } else {
                      sender.actor ! MessageResponse(
                        session,
                        ChatMsg(UNK_229, true, "", "@silence_enabled_ack", None),
                        channel
                      )
                    }
                    recipient.actor ! MessageResponse(session, message, channel)
                  case None =>
                    sender.actor ! MessageResponse(
                      session,
                      ChatMsg(UNK_229, true, "", s"unknown player '$name'", None),
                      channel
                    )
                }
              case (Some(sender), _, _, error) =>
                sender.actor ! MessageResponse(
                  session,
                  ChatMsg(UNK_229, false, "", error.getOrElse("usage: /silence <name> [<time>]"), None),
                  channel
                )

              case (None, _, _, _) =>
                log.error("received message from non-subscribed actor")

            }

          case CMT_SQUAD =>
            subs.foreach(_.actor ! MessageResponse(session, message, channel))

          case CMT_NOTE =>
            subs
              .filter(_.session.player.Name == message.recipient)
              .foreach(
                _.actor ! MessageResponse(session, message.copy(recipient = session.player.Name), channel)
              )

          // faction commands
          case CMT_OPEN | CMT_PLATOON | CMT_COMMAND =>
            subs
              .filter(_.session.player.Faction == session.player.Faction)
              .foreach(
                _.actor ! MessageResponse(session, message, channel)
              )

          case CMT_GMBROADCAST_NC =>
            subs.filter(_.session.player.Faction == PlanetSideEmpire.NC).foreach {
              case JoinChannel(actor, _, _) => actor ! MessageResponse(session, message, channel)
            }

          case CMT_GMBROADCAST_TR =>
            subs.filter(_.session.player.Faction == PlanetSideEmpire.TR).foreach {
              case JoinChannel(actor, _, _) => actor ! MessageResponse(session, message, channel)
            }

          case CMT_GMBROADCAST_VS =>
            subs.filter(_.session.player.Faction == PlanetSideEmpire.VS).foreach {
              case JoinChannel(actor, _, _) => actor ! MessageResponse(session, message, channel)
            }

          // cross faction commands
          case CMT_BROADCAST | CMT_VOICE | CMT_GMBROADCAST | CMT_GMBROADCASTPOPUP | UNK_227 =>
            subs.foreach {
              case JoinChannel(actor, _, _) => actor ! MessageResponse(session, message, channel)
            }

          case _ =>
            log.warn(s"unhandled chat message, add a case for $message")
        }
        this
    }
  }
}
