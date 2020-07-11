// Copyright (c) 2017 PSForever
package services.chat

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import net.psforever.objects.Session
import net.psforever.packet.game.ChatMessage
import net.psforever.types.{ChatMessageType, PlanetSideGUID}

object ChatService {
  val ChatServiceKey = ServiceKey[ChatService.Command]("chatService")

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      context.system.receptionist ! Receptionist.Register(ChatServiceKey, context.self)
      new ChatService(context)
    }

  sealed trait Command

  final case class JoinChannel(actor: ActorRef[MessageResponse], session: Session, channel: ChatChannel) extends Command
  final case class LeaveChannel(actor: ActorRef[MessageResponse], channel: ChatChannel)                  extends Command
  final case class LeaveAllChannels(actor: ActorRef[MessageResponse])                                    extends Command

  final case class Message(session: Session, message: ChatMessage, channel: ChatChannel) extends Command
  final case class MessageResponse(session: Session, message: ChatMessage, channel: ChatChannel)

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

  private[this] val log = org.log4s.getLogger
  var subscriptions     = List[JoinChannel]()

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
          case CMT_TELL =>
            subs.find(_.session.player.Name == session.player.Name).foreach {
              case JoinChannel(sender, _, _) =>
                sender ! MessageResponse(
                  session,
                  ChatMessage(
                    ChatMessageType.U_CMT_TELLFROM,
                    message.wideContents,
                    message.recipient,
                    message.contents,
                    None
                  ),
                  channel
                )
                subs.find(_.session.player.Name == message.recipient) match {
                  case Some(JoinChannel(receiver, _, _)) =>
                    receiver ! MessageResponse(
                      session,
                      ChatMessage(
                        ChatMessageType.CMT_TELL,
                        message.wideContents,
                        session.player.Name,
                        message.contents,
                        None
                      ),
                      channel
                    )
                  case None =>
                    sender ! MessageResponse(
                      session,
                      ChatMessage(ChatMessageType.UNK_45, false, "", "@NoTell_Target", None),
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
                        ChatMessage(UNK_71, true, "", "@silence_disabled_ack", None),
                        channel
                      )
                    } else {
                      sender.actor ! MessageResponse(
                        session,
                        ChatMessage(UNK_71, true, "", "@silence_enabled_ack", None),
                        channel
                      )
                    }
                    recipient.actor ! MessageResponse(session, message, channel)
                  case None =>
                    sender.actor ! MessageResponse(
                      session,
                      ChatMessage(UNK_71, true, "", s"unknown player '$name'", None),
                      channel
                    )
                }
              case (Some(sender), _, _, error) =>
                sender.actor ! MessageResponse(
                  session,
                  ChatMessage(UNK_71, false, "", error.getOrElse("usage: /silence <name> [<time>]"), None),
                  channel
                )

              case (None, _, _, _) =>
                log.error("received message from non-subscribed actor")

            }

          case CMT_NOTE =>
            subs.filter(_.session.player.Name == message.recipient).foreach {
              case JoinChannel(actor, _, _) =>
                actor ! MessageResponse(session, message.copy(recipient = session.player.Name), channel)
            }

          // faction commands
          case CMT_OPEN | CMT_PLATOON | CMT_COMMAND =>
            subs.filter(_.session.player.Faction == session.player.Faction).foreach {
              case JoinChannel(actor, _, _) => actor ! MessageResponse(session, message, channel)
            }

          // cross faction commands
          case CMT_BROADCAST | CMT_VOICE =>
            subs.foreach {
              case JoinChannel(actor, _, _) => actor ! MessageResponse(session, message, channel)
            }

          case _ =>
            log.warn(s"unhandled chat message, should possibly add a case for ${message}")
        }
        this
    }
  }
}
