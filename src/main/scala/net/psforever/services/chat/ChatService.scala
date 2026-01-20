// Copyright (c) 2017 PSForever
package net.psforever.services.chat

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import net.psforever.objects.{Session, SessionSource}
import net.psforever.packet.game.ChatMsg
import net.psforever.services.base.{EventMessage, EventResponse}
import net.psforever.types.{ChatMessageType, PlanetSideEmpire}

object ChatService {
  val ChatServiceKey: ServiceKey[Command] = ServiceKey[ChatService.Command]("chatService")

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      context.system.receptionist ! Receptionist.Register(ChatServiceKey, context.self)
      new ChatService(context)
    }

  sealed trait Command extends EventMessage {
    def response(): EventResponse = null
  }

  final case class JoinChannel(actor: ActorRef[MessageResponse], sessionSource: SessionSource, channel: ChatChannel) extends Command
  final case class LeaveChannel(actor: ActorRef[MessageResponse], channel: ChatChannel)                  extends Command
  final case class LeaveAllChannels(actor: ActorRef[MessageResponse])                                    extends Command

  final case class Message(session: Session, message: ChatMsg, channel: ChatChannel) extends Command
  final case class MessageResponse(session: Session, message: ChatMsg, channel: ChatChannel) extends EventResponse
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
        subscriptions = subscriptions.filterNot {
          case JoinChannel(a, _, c) => actor == a && channel == c
        }
        this

      case LeaveAllChannels(actor) =>
        subscriptions = subscriptions.filterNot {
          case JoinChannel(a, _, _) => actor == a
        }
        this

      case Message(session, message, channel) =>
        (channel, message.messageType) match {
          case (SquadChannel(_), CMT_SQUAD)                                      => ()
          case (SquadChannel(_), CMT_VOICE) if message.contents.startsWith("SH") => ()
          case (OutfitChannel(_), CMT_OUTFIT)                                    => ()
          case (DefaultChannel, messageType) if messageType != CMT_SQUAD && messageType != CMT_OUTFIT => ()
          case (SpectatorChannel, messageType) if messageType != CMT_SQUAD && messageType != CMT_OUTFIT => ()
          case _ =>
            log.error(s"invalid chat channel $channel for messageType ${message.messageType}")
            return this
        }
        val subs = subscriptions.filter(_.channel == channel)
        message.messageType match {
          case mtype @ (CMT_TELL | CMT_GMTELL) =>
            val playerName = session.player.Name
            val playerNameLower = playerName.toLowerCase()
            val recipientName = message.recipient
            val recipientNameLower = recipientName.toLowerCase()
            (
              subs.find(_.sessionSource.session.player.Name.toLowerCase().equals(playerNameLower)),
              subs.find(_.sessionSource.session.player.Name.toLowerCase().equals(recipientNameLower))
            ) match {
              case (Some(JoinChannel(sender, _, _)), Some(JoinChannel(receiver, _, _))) =>
                val replyType = if (mtype == CMT_TELL) { U_CMT_TELLFROM } else { U_CMT_GMTELLFROM }
                sender ! MessageResponse(
                  session,
                  message.copy(messageType = replyType),
                  channel
                )
                receiver ! MessageResponse(session, message.copy(recipient = playerName), channel)
              case (None, Some(JoinChannel(receiver, _, _))) =>
                log.warn(s"ChatMsg->$mtype: can not find subscription object for: sender=$playerName")
                receiver ! MessageResponse(session, message.copy(recipient = playerName), channel)
              case (Some(JoinChannel(sender, _, _)), None) =>
                val replyType = if (mtype == CMT_TELL) { U_CMT_TELLFROM } else { U_CMT_GMTELLFROM }
                sender ! MessageResponse(
                  session,
                  message.copy(messageType = replyType),
                  channel
                )
                sender ! MessageResponse(
                  session,
                  ChatMsg(ChatMessageType.UNK_45, wideContents = false, "", "@NoTell_Target", None),
                  channel
                )
              case (None, None) =>
                log.error(s"ChatMsg->$mtype: can not find subscription object for: sender=$playerName")
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

            val sender = subs.find(_.sessionSource.session.player.Name == session.player.Name)

            (sender, name, time, error) match {
              case (Some(sender), Some(name), Some(_), None) =>
                val recipient = subs.find(_.sessionSource.session.player.Name == name)
                recipient match {
                  case Some(recipient) =>
                    if (recipient.sessionSource.session.player.silenced) {
                      sender.actor ! MessageResponse(
                        session,
                        ChatMsg(UNK_229, wideContents = true, "", "@silence_disabled_ack", None),
                        channel
                      )
                    } else {
                      sender.actor ! MessageResponse(
                        session,
                        ChatMsg(UNK_229, wideContents = true, "", "@silence_enabled_ack", None),
                        channel
                      )
                    }
                    recipient.actor ! MessageResponse(session, message, channel)
                  case None =>
                    sender.actor ! MessageResponse(
                      session,
                      ChatMsg(UNK_229, wideContents = true, "", s"unknown player '$name'", None),
                      channel
                    )
                }
              case (Some(sender), _, _, error) =>
                sender.actor ! MessageResponse(
                  session,
                  ChatMsg(UNK_229, wideContents = false, "", error.getOrElse("usage: /silence <name> [<time>]"), None),
                  channel
                )

              case (None, _, _, _) =>
                log.warn("received message from non-subscribed actor")

            }

          case CMT_SQUAD =>
            subs.foreach(_.actor ! MessageResponse(session, message, channel))

          case CMT_OUTFIT =>
            subs.foreach(_.actor ! MessageResponse(session, message, channel))

          case CMT_NOTE =>
            subs
              .filter(_.sessionSource.session.player.Name == message.recipient)
              .foreach(
                _.actor ! MessageResponse(session, message.copy(recipient = session.player.Name), channel)
              )

          // faction commands
          case CMT_OPEN | CMT_PLATOON | CMT_COMMAND =>
            subs
              .filter(_.sessionSource.session.player.Faction == session.player.Faction)
              .foreach(
                _.actor ! MessageResponse(session, message, channel)
              )

          case CMT_GMBROADCAST_NC =>
            subs.filter(_.sessionSource.session.player.Faction == PlanetSideEmpire.NC).foreach {
              case JoinChannel(actor, _, _) => actor ! MessageResponse(session, message, channel)
            }

          case CMT_GMBROADCAST_TR =>
            subs.filter(_.sessionSource.session.player.Faction == PlanetSideEmpire.TR).foreach {
              case JoinChannel(actor, _, _) => actor ! MessageResponse(session, message, channel)
            }

          case CMT_GMBROADCAST_VS =>
            subs.filter(_.sessionSource.session.player.Faction == PlanetSideEmpire.VS).foreach {
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
