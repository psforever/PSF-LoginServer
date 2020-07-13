package net.psforever.actors.session

import akka.actor.Cancellable
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.{Default, GlobalDefinitions, Player, Session}
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.turret.{FacilityTurret, TurretUpgrade, WeaponTurrets}
import net.psforever.objects.zones.Zoning
import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ChatMessage, DeadState, RequestDestroyMessage, ZonePopulationUpdateMessage}
import net.psforever.types.{ChatMessageType, PlanetSideEmpire, PlanetSideGUID, Vector3}
import net.psforever.util.PointOfInterest
import net.psforever.zones.Zones
import services.chat.ChatService
import services.chat.ChatService.ChatChannel
import services.local.{LocalAction, LocalServiceMessage}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object ChatActor {
  def apply(sessionActor: ActorRef[SessionActor.Command]): Behavior[Command] =
    Behaviors.setup(context => new ChatActor(context, sessionActor))

  sealed trait Command

  final case class JoinChannel(channel: ChatChannel)  extends Command
  final case class LeaveChannel(channel: ChatChannel) extends Command
  final case class Message(message: ChatMessage)      extends Command
  final case class SetSession(session: Session)       extends Command

  private case class ListingResponse(listing: Receptionist.Listing)                                extends Command
  private case class IncomingMessage(session: Session, message: ChatMessage, channel: ChatChannel) extends Command
}

class ChatActor(context: ActorContext[ChatActor.Command], sessionActor: ActorRef[SessionActor.Command])
    extends AbstractBehavior[ChatActor.Command](context) {
  import ChatActor._

  private[this] val log                                  = org.log4s.getLogger
  var channels: List[ChatChannel]                        = List()
  var session: Option[Session]                           = None
  var chatService: Option[ActorRef[ChatService.Command]] = None
  var silenceTimer: Cancellable                          = Default.Cancellable

  val chatServiceAdapter: ActorRef[ChatService.MessageResponse] = context.messageAdapter[ChatService.MessageResponse] {
    case ChatService.MessageResponse(session, message, channel) => IncomingMessage(session, message, channel)
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      silenceTimer.cancel()
      if (chatService.isDefined) chatService.get ! ChatService.LeaveAllChannels(chatServiceAdapter)
      this
  }

  override def onMessage(msg: Command): Behavior[Command] = {
    import ChatMessageType._

    msg match {
      case ListingResponse(ChatService.ChatServiceKey.Listing(listings)) =>
        chatService = Some(listings.head)
        chatService.get ! ChatService.JoinChannel(chatServiceAdapter, session.get, ChatChannel.Default())
        channels ++= List(ChatChannel.Default())
        this

      case SetSession(newSession) =>
        session = Some(newSession)
        if (chatService.isEmpty && newSession.player != null) { // TODO the player check sucks...
          context.system.receptionist ! Receptionist.Find(
            ChatService.ChatServiceKey,
            context.messageAdapter[Receptionist.Listing](ListingResponse)
          )
        }
        this

      case JoinChannel(channel) =>
        chatService.get ! ChatService.JoinChannel(chatServiceAdapter, session.get, channel)
        channels ++= List(channel)
        this

      case LeaveChannel(channel) =>
        chatService.get ! ChatService.LeaveChannel(chatServiceAdapter, channel)
        channels = channels.filter(_ == channel)
        this

      /** Some messages are sent during login so we handle them prematurely because main message handler requires the
        * session object and chat service and they may not be set yet
        */
      case Message(ChatMessage(CMT_CULLWATERMARK, _, _, contents, _)) =>
        val connectionState =
          if (contents.contains("40 80")) 100
          else if (contents.contains("120 200")) 25
          else 50
        sessionActor ! SessionActor.SetConnectionState(connectionState)
        this

      case Message(ChatMessage(CMT_ANONYMOUS, _, _, _, _)) =>
        // ??
        this

      case Message(ChatMessage(CMT_TOGGLE_GM, _, _, _, _)) =>
        // ??
        this

      case Message(message) =>
        log.info("Chat: " + message)

        (session, chatService) match {
          case (Some(session), Some(chatService)) =>
            (message.messageType, message.recipient.trim, message.contents.trim) match {
              case (CMT_FLY, recipient, contents) if session.admin =>
                val flying = contents match {
                  case "on"  => true
                  case "off" => false
                  case _     => !session.flying
                }
                sessionActor ! SessionActor.SetFlying(flying)
                sessionActor ! SessionActor.SendResponse(
                  ChatMessage(CMT_FLY, false, recipient, if (flying) "on" else "off", None)
                )

              case (CMT_SPEED, recipient, contents) =>
                val speed =
                  try {
                    contents.toFloat
                  } catch {
                    case _: Throwable =>
                      1f
                  }
                sessionActor ! SessionActor.SetSpeed(speed)
                sessionActor ! SessionActor.SendResponse(message.copy(contents = f"$speed%.3f"))

              case (CMT_TOGGLESPECTATORMODE, _, contents) if session.admin =>
                val spectator = contents match {
                  case "on"  => true
                  case "off" => false
                  case _     => !session.player.spectator
                }
                sessionActor ! SessionActor.SetSpectator(spectator)
                sessionActor ! SessionActor.SendResponse(message.copy(contents = if (spectator) "on" else "off"))

              case (CMT_RECALL, _, _) =>
                val sanctuary = Zones.SanctuaryZoneId(session.player.Faction)
                val errorMessage = session.zoningType match {
                  case Zoning.Method.Quit => Some("You can't recall to your sanctuary continent while quitting")
                  case Zoning.Method.InstantAction =>
                    Some("You can't recall to your sanctuary continent while instant actioning")
                  case Zoning.Method.Recall => Some("You already requested to recall to your sanctuary continent")
                  case _ if session.zone.Id == sanctuary =>
                    Some("You can't recall to your sanctuary when you are already in your sanctuary")
                  case _ if !session.player.isAlive || session.deadState != DeadState.Alive =>
                    Some(if (session.player.isAlive) "@norecall_deconstructing" else "@norecall_dead")
                  case _ if session.player.VehicleSeated.nonEmpty => Some("@norecall_invehicle")
                  case _                                          => None
                }
                errorMessage match {
                  case Some(errorMessage) =>
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(
                        CMT_QUIT,
                        false,
                        "",
                        errorMessage,
                        None
                      )
                    )
                  case None =>
                    sessionActor ! SessionActor.Recall(sanctuary)
                }

              case (CMT_INSTANTACTION, _, _) =>
                if (session.zoningType == Zoning.Method.Quit) {
                  sessionActor ! SessionActor.SendResponse(
                    ChatMessage(CMT_QUIT, false, "", "You can't instant action while quitting.", None)
                  )
                } else if (session.zoningType == Zoning.Method.InstantAction) {
                  sessionActor ! SessionActor.SendResponse(
                    ChatMessage(CMT_QUIT, false, "", "@noinstantaction_instantactionting", None)
                  )
                } else if (session.zoningType == Zoning.Method.Recall) {
                  sessionActor ! SessionActor.SendResponse(
                    ChatMessage(
                      CMT_QUIT,
                      false,
                      "",
                      "You won't instant action. You already requested to recall to your sanctuary continent",
                      None
                    )
                  )
                } else if (!session.player.isAlive || session.deadState != DeadState.Alive) {
                  if (session.player.isAlive) {
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(CMT_QUIT, false, "", "@noinstantaction_deconstructing", None)
                    )
                  } else {
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(CMT_QUIT, false, "", "@noinstantaction_dead", None)
                    )
                  }
                } else if (session.player.VehicleSeated.nonEmpty) {
                  sessionActor ! SessionActor.SendResponse(
                    ChatMessage(CMT_QUIT, false, "", "@noinstantaction_invehicle", None)
                  )
                } else {
                  sessionActor ! SessionActor.InstantAction()
                }

              case (CMT_QUIT, _, _) =>
                if (session.zoningType == Zoning.Method.Quit) {
                  sessionActor ! SessionActor.SendResponse(ChatMessage(CMT_QUIT, false, "", "@noquit_quitting", None))
                } else if (!session.player.isAlive || session.deadState != DeadState.Alive) {
                  if (session.player.isAlive) {
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(CMT_QUIT, false, "", "@noquit_deconstructing", None)
                    )
                  } else {
                    sessionActor ! SessionActor.SendResponse(ChatMessage(CMT_QUIT, false, "", "@noquit_dead", None))
                  }
                } else if (session.player.VehicleSeated.nonEmpty) {
                  sessionActor ! SessionActor.SendResponse(ChatMessage(CMT_QUIT, false, "", "@noquit_invehicle", None))
                } else {
                  sessionActor ! SessionActor.Quit()
                }

              case (CMT_SUICIDE, _, _) =>
                if (session.player.isAlive && session.deadState != DeadState.Release) {
                  sessionActor ! SessionActor.Suicide()
                }

              case (CMT_DESTROY, recipient, contents) =>
                val guid = contents.toInt
                session.zone.GUID(session.zone.Map.TerminalToSpawnPad.getOrElse(guid, guid)) match {
                  case Some(pad: VehicleSpawnPad) =>
                    pad.Actor ! VehicleSpawnControl.ProcessControl.Flush
                  case Some(turret: FacilityTurret) if turret.isUpgrading =>
                    WeaponTurrets.FinishUpgradingMannedTurret(turret, TurretUpgrade.None)
                  case _ =>
                    sessionActor ! SessionActor.SendResponse(
                      PacketCoding.CreateGamePacket(0, RequestDestroyMessage(PlanetSideGUID(guid))).packet
                    )
                }
                sessionActor ! SessionActor.SendResponse(message)

              case (_, _, "!loc") =>
                val continent = session.zone
                val player    = session.player
                val loc =
                  s"zone=${continent.Id} pos=${player.Position.x},${player.Position.y},${player.Position.z}; ori=${player.Orientation.x},${player.Orientation.y},${player.Orientation.z}"
                log.info(loc)
                sessionActor ! SessionActor.SendResponse(message.copy(contents = loc))

              case (_, _, contents) if contents.startsWith("!list") =>
                val zone = contents.split(" ").lift(1) match {
                  case None =>
                    Some(session.zone)
                  case Some(id) =>
                    Zones.zones.get(id)
                }

                zone match {
                  case Some(zone) =>
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(
                        CMT_GMOPEN,
                        message.wideContents,
                        "Server",
                        "\\#8Name (Faction) [ID] at PosX PosY PosZ",
                        message.note
                      )
                    )

                    (zone.LivePlayers ++ zone.Corpses)
                      .filter(_.CharId != session.player.CharId)
                      .sortBy(_.Name)
                      .foreach(player => {
                        sessionActor ! SessionActor.SendResponse(
                          ChatMessage(
                            CMT_GMOPEN,
                            message.wideContents,
                            "Server",
                            s"\\#7${player.Name} (${player.Faction}) [${player.CharId}] at ${player.Position.x.toInt} ${player.Position.y.toInt} ${player.Position.z.toInt}",
                            message.note
                          )
                        )
                      })
                  case None =>
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(
                        CMT_GMOPEN,
                        message.wideContents,
                        "Server",
                        "Invalid zone ID",
                        message.note
                      )
                    )
                }

              case (_, _, contents) if session.admin && contents.startsWith("!kick") =>
                val input = contents.split("\\s+").drop(1)
                if (input.length > 0) {
                  val numRegex = raw"(\d+)".r
                  val id       = input(0)
                  val determination: Player => Boolean = id match {
                    case numRegex(_) => _.CharId == id.toLong
                    case _           => _.Name.equals(id)
                  }
                  session.zone.LivePlayers
                    .find(determination)
                    .orElse(session.zone.Corpses.find(determination)) match {
                    case Some(player) =>
                      input.lift(1) match {
                        case Some(numRegex(time)) =>
                          sessionActor ! SessionActor.Kick(player, Some(time.toLong))
                        case _ =>
                          sessionActor ! SessionActor.Kick(player)
                      }
                    case None =>
                      sessionActor ! SessionActor.SendResponse(
                        ChatMessage(
                          CMT_GMOPEN,
                          message.wideContents,
                          "Server",
                          "Invalid player",
                          message.note
                        )
                      )
                  }
                }

              case (CMT_CAPTUREBASE, _, contents) if session.admin =>
                val args = contents.split(" ").filter(_ != "")

                val (faction, factionPos) = args.zipWithIndex
                  .map { case (faction, pos) => (faction.toLowerCase, pos) }
                  .flatMap {
                    case ("tr", pos)   => Some(PlanetSideEmpire.TR, pos)
                    case ("nc", pos)   => Some(PlanetSideEmpire.NC, pos)
                    case ("vs", pos)   => Some(PlanetSideEmpire.VS, pos)
                    case ("none", pos) => Some(PlanetSideEmpire.NEUTRAL, pos)
                    case _             => None
                  }
                  .headOption match {
                  case Some((faction, pos)) => (faction, Some(pos))
                  case None                 => (session.player.Faction, None)
                }

                val (buildingsOption, buildingPos) = args.zipWithIndex.flatMap {
                  case (_, pos) if factionPos.isDefined && factionPos.get == pos => None
                  case ("all", pos) =>
                    Some(
                      Some(
                        session.zone.Buildings
                          .filter {
                            case (_, building) => building.CaptureTerminal.isDefined
                          }
                          .values
                          .toSeq
                      ),
                      Some(pos)
                    )
                  case (name, pos) =>
                    session.zone.Buildings.find {
                      case (_, building) => name.equalsIgnoreCase(building.Name) && building.CaptureTerminal.isDefined
                    } match {
                      case Some((_, building)) => Some(Some(Seq(building)), Some(pos))
                      case None =>
                        try {
                          // check if we have a timer
                          name.toInt
                          None
                        } catch {
                          case _: Throwable =>
                            Some(None, Some(pos))
                        }
                    }
                }.headOption match {
                  case Some((buildings, pos)) => (buildings, pos)
                  case None                   => (None, None)
                }

                val (timerOption, timerPos) = args.zipWithIndex.flatMap {
                  case (_, pos)
                      if factionPos.isDefined && factionPos.get == pos || buildingPos.isDefined && buildingPos.get == pos =>
                    None
                  case (timer, pos) =>
                    try {
                      val t = timer.toInt // TODO what is the timer format supposed to be?
                      Some(Some(t), Some(pos))
                    } catch {
                      case _: Throwable =>
                        Some(None, Some(pos))
                    }
                }.headOption match {
                  case Some((timer, posOption)) => (timer, posOption)
                  case None                     => (None, None)
                }

                (factionPos, buildingPos, timerPos, buildingsOption, timerOption) match {
                  case // [[<empire>|none [<timer>]]
                      (Some(0), None, Some(1), None, Some(_)) | (Some(0), None, None, None, None) |
                      (None, None, None, None, None) |
                      // [<building name> [<empire>|none [timer]]]
                      (None | Some(1), Some(0), None, Some(_), None) | (Some(1), Some(0), Some(2), Some(_), Some(_)) |
                      // [all [<empire>|none]]
                      (Some(1) | None, Some(0), None, Some(_), None) =>
                    val buildings = buildingsOption.getOrElse(
                      session.zone.Buildings
                        .filter {
                          case (_, building) =>
                            building.PlayersInSOI.exists { soiPlayer =>
                              session.player.CharId == soiPlayer.CharId
                            }
                        }
                        .map { case (_, building) => building }
                    )
                    buildings foreach { building =>
                      // TODO implement timer
                      building.Faction = faction
                      session.zone.LocalEvents ! LocalServiceMessage(
                        session.zone.Id,
                        LocalAction.SetEmpire(building.GUID, faction)
                      )
                    }
                  case (_, Some(0), _, None, _) =>
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(
                        UNK_229,
                        true,
                        "",
                        s"\\#FF4040ERROR - \'${args(0)}\' is not a valid building name.",
                        None
                      )
                    )
                  case (Some(0), _, Some(1), _, None) | (Some(1), Some(0), Some(2), _, None) =>
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(
                        UNK_229,
                        true,
                        "",
                        s"\\#FF4040ERROR - \'${args(timerPos.get)}\' is not a valid timer value.",
                        None
                      )
                    )
                  case _ =>
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(
                        UNK_229,
                        true,
                        "",
                        "usage: /capturebase [[<empire>|none [<timer>]] | [<building name> [<empire>|none [timer]]] | [all [<empire>|none]]",
                        None
                      )
                    )
                }

              case (CMT_GMBROADCAST | CMT_GMBROADCAST_NC | CMT_GMBROADCAST_VS | CMT_GMBROADCAST_TR, _, _)
                  if session.admin =>
                chatService ! ChatService.Message(
                  session,
                  message.copy(recipient = session.player.Name),
                  ChatChannel.Default()
                )

              case (CMT_GMTELL, _, _) if session.admin =>
                chatService ! ChatService.Message(
                  session,
                  message.copy(recipient = session.player.Name),
                  ChatChannel.Default()
                )

              case (CMT_GMBROADCASTPOPUP, _, _) if session.admin =>
                chatService ! ChatService.Message(
                  session,
                  message.copy(recipient = session.player.Name),
                  ChatChannel.Default()
                )

              case (_, _, contents) if contents.startsWith("!whitetext ") && session.admin =>
                chatService ! ChatService.Message(
                  session,
                  ChatMessage(UNK_227, true, "", contents.replace("!whitetext ", ""), None),
                  ChatChannel.Default()
                )

              case (_, "tr", contents) =>
                sessionActor ! SessionActor.SendResponse(
                  ZonePopulationUpdateMessage(4, 414, 138, contents.toInt, 138, contents.toInt / 2, 138, 0, 138, 0)
                )

              case (_, "nc", contents) =>
                sessionActor ! SessionActor.SendResponse(
                  ZonePopulationUpdateMessage(4, 414, 138, 0, 138, contents.toInt, 138, contents.toInt / 3, 138, 0)
                )

              case (_, "vs", contents) =>
                sessionActor ! SessionActor.SendResponse(
                  ZonePopulationUpdateMessage(4, 414, 138, contents.toInt * 2, 138, 0, 138, contents.toInt, 138, 0)
                )

              case (_, "bo", contents) =>
                sessionActor ! SessionActor.SendResponse(
                  ZonePopulationUpdateMessage(4, 414, 138, 0, 138, 0, 138, 0, 138, contents.toInt)
                )

              case (_, _, contents) if contents.startsWith("!ntu") && session.admin =>
                session.zone.Buildings.values.foreach(building =>
                  building.Amenities.foreach(amenity =>
                    amenity.Definition match {
                      case GlobalDefinitions.resource_silo =>
                        val r        = new scala.util.Random
                        val silo     = amenity.asInstanceOf[ResourceSilo]
                        val ntu: Int = 900 + r.nextInt(100) - silo.ChargeLevel
                        //                val ntu: Int = 0 + r.nextInt(100) - silo.ChargeLevel
                        silo.Actor ! ResourceSilo.UpdateChargeLevel(ntu)

                      case _ => ;
                    }
                  )
                )

              case (CMT_OPEN, _, _) if !session.player.silenced =>
                chatService ! ChatService.Message(
                  session,
                  message.copy(recipient = session.player.Name),
                  ChatChannel.Default()
                )

              case (CMT_VOICE, _, _) =>
                chatService ! ChatService.Message(
                  session,
                  message.copy(recipient = session.player.Name),
                  ChatChannel.Default()
                )

              case (CMT_TELL, _, _) if !session.player.silenced =>
                chatService ! ChatService.Message(
                  session,
                  message,
                  ChatChannel.Default()
                )

              case (CMT_BROADCAST, _, _) if !session.player.silenced =>
                chatService ! ChatService.Message(
                  session,
                  message.copy(recipient = session.player.Name),
                  ChatChannel.Default()
                )

              case (CMT_PLATOON, _, _) if !session.player.silenced =>
                chatService ! ChatService.Message(
                  session,
                  message.copy(recipient = session.player.Name),
                  ChatChannel.Default()
                )

              case (CMT_COMMAND, _, _) if session.admin =>
                chatService ! ChatService.Message(
                  session,
                  message.copy(recipient = session.player.Name),
                  ChatChannel.Default()
                )

              case (CMT_NOTE, _, _) =>
                chatService ! ChatService.Message(session, message, ChatChannel.Default())

              case (CMT_SILENCE, _, _) if session.admin =>
                chatService ! ChatService.Message(session, message, ChatChannel.Default())

              case (CMT_SQUAD, _, _) =>
                channels.foreach {
                  case channel: ChatChannel.Squad =>
                    chatService ! ChatService.Message(session, message.copy(recipient = session.player.Name), channel)
                  case _ =>
                }

              case (
                    CMT_WHO | CMT_WHO_CSR | CMT_WHO_CR | CMT_WHO_PLATOONLEADERS | CMT_WHO_SQUADLEADERS | CMT_WHO_TEAMS,
                    _,
                    _
                  ) =>
                val players  = session.zone.Players
                val popTR    = players.count(_.faction == PlanetSideEmpire.TR)
                val popNC    = players.count(_.faction == PlanetSideEmpire.NC)
                val popVS    = players.count(_.faction == PlanetSideEmpire.VS)
                val contName = session.zone.Map.Name

                sessionActor ! SessionActor.SendResponse(
                  ChatMessage(ChatMessageType.CMT_WHO, true, "", "That command doesn't work for now, but : ", None)
                )
                sessionActor ! SessionActor.SendResponse(
                  ChatMessage(ChatMessageType.CMT_WHO, true, "", "NC online : " + popNC + " on " + contName, None)
                )
                sessionActor ! SessionActor.SendResponse(
                  ChatMessage(ChatMessageType.CMT_WHO, true, "", "TR online : " + popTR + " on " + contName, None)
                )
                sessionActor ! SessionActor.SendResponse(
                  ChatMessage(ChatMessageType.CMT_WHO, true, "", "VS online : " + popVS + " on " + contName, None)
                )

              case (CMT_ZONE, _, contents) if session.admin =>
                val buffer = contents.toLowerCase.split("\\s+")
                val (zone, gate, list) = (buffer.lift(0), buffer.lift(1)) match {
                  case (Some("-list"), None) =>
                    (None, None, true)
                  case (Some(zoneId), Some("-list")) =>
                    (PointOfInterest.get(zoneId), None, true)
                  case (Some(zoneId), gateId) =>
                    val zone = PointOfInterest.get(zoneId)
                    val gate = (zone, gateId) match {
                      case (Some(zone), Some(gateId)) => PointOfInterest.getWarpgate(zone, gateId)
                      case (Some(zone), None)         => Some(PointOfInterest.selectRandom(zone))
                      case _                          => None
                    }
                    (zone, gate, false)
                  case _ =>
                    (None, None, false)
                }
                (zone, gate, list) match {
                  case (None, None, true) =>
                    sessionActor ! SessionActor.SendResponse(ChatMessage(UNK_229, true, "", PointOfInterest.list, None))
                  case (Some(zone), None, true) =>
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(UNK_229, true, "", PointOfInterest.listWarpgates(zone), None)
                    )
                  case (Some(zone), Some(gate), false) =>
                    sessionActor ! SessionActor.SetZone(zone.zonename, gate)
                  case (_, None, false) =>
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(UNK_229, true, "", "Gate id not defined (use '/zone <zone> -list')", None)
                    )
                  case (_, _, _) if buffer.isEmpty || buffer(0).equals("-help") =>
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(UNK_229, true, "", "usage: /zone <zone> [gatename] | [-list]", None)
                    )
                }

              case (CMT_WARP, _, contents) if session.admin =>
                val buffer = contents.toLowerCase.split("\\s+")
                val (coordinates, waypoint) = (buffer.lift(0), buffer.lift(1), buffer.lift(2)) match {
                  case (Some(x), Some(y), Some(z))            => (Some(x, y, z), None)
                  case (Some("to"), Some(character), None)    => (None, None) // TODO not implemented
                  case (Some("near"), Some(objectName), None) => (None, None) // TODO not implemented
                  case (Some(waypoint), None, None)           => (None, Some(waypoint))
                  case _                                      => (None, None)
                }
                (coordinates, waypoint) match {
                  case (Some((x, y, z)), None) if List(x, y, z).forall { str =>
                        val coordinate = str.toFloatOption
                        coordinate.isDefined && coordinate.get >= 0 && coordinate.get <= 8191
                      } =>
                    sessionActor ! SessionActor.SetPosition(Vector3(x.toFloat, y.toFloat, z.toFloat))
                  case (None, Some(waypoint)) if waypoint != "-help" =>
                    PointOfInterest.getWarpLocation(session.zone.Id, waypoint) match {
                      case Some(location) => sessionActor ! SessionActor.SetPosition(location)
                      case None =>
                        sessionActor ! SessionActor.SendResponse(
                          ChatMessage(UNK_229, true, "", s"unknown location '$waypoint", None)
                        )
                    }
                  case _ =>
                    sessionActor ! SessionActor.SendResponse(
                      ChatMessage(
                        UNK_229,
                        true,
                        "",
                        s"usage: /warp <x><y><z> OR /warp to <character> OR /warp near <object> OR /warp above <object> OR /warp waypoint",
                        None
                      )
                    )
                }

              case _ =>
                log.info("unhandled chat message $message")
            }
          case (None, _) | (_, None) =>
            log.error("failed to handle message because dependencies are missing")
        }
        this

      case IncomingMessage(fromSession, message, channel) =>
        (session) match {
          case Some(session) =>
            message.messageType match {
              case CMT_TELL | U_CMT_TELLFROM | CMT_BROADCAST | CMT_SQUAD | CMT_PLATOON | CMT_COMMAND | UNK_45 | UNK_71 |
                  CMT_NOTE | CMT_GMBROADCAST | CMT_GMBROADCAST_NC | CMT_GMBROADCAST_TR | CMT_GMBROADCAST_VS |
                  CMT_GMBROADCASTPOPUP | CMT_GMTELL | U_CMT_GMTELLFROM | UNK_227 =>
                sessionActor ! SessionActor.SendResponse(message)
              case CMT_OPEN =>
                if (
                  session.zone == fromSession.zone &&
                  Vector3.Distance(session.player.Position, fromSession.player.Position) < 25 &&
                  session.player.Faction == fromSession.player.Faction
                ) {
                  sessionActor ! SessionActor.SendResponse(message)
                }
              case CMT_VOICE =>
                if (
                  session.zone == fromSession.zone &&
                  Vector3.Distance(session.player.Position, fromSession.player.Position) < 25
                ) {
                  sessionActor ! SessionActor.SendResponse(message)
                }
              case CMT_SILENCE =>
                val args = message.contents.split(" ")
                val (name, time) = (args.lift(0), args.lift(1)) match {
                  case (Some(name), _) if name != session.player.Name =>
                    log.error("received silence message for other player")
                    (None, None)
                  case (Some(name), None)                                     => (Some(name), Some(5))
                  case (Some(name), Some(time)) if time.toIntOption.isDefined => (Some(name), Some(time.toInt))
                  case _                                                      => (None, None)
                }
                (name, time) match {
                  case (Some(_), Some(time)) =>
                    if (session.player.silenced) {
                      sessionActor ! SessionActor.SetSilenced(false)
                      sessionActor ! SessionActor.SendResponse(
                        ChatMessage(ChatMessageType.UNK_71, true, "", "@silence_off", None)
                      )
                      if (!silenceTimer.isCancelled) silenceTimer.cancel()
                    } else {
                      sessionActor ! SessionActor.SetSilenced(true)
                      sessionActor ! SessionActor.SendResponse(
                        ChatMessage(ChatMessageType.UNK_71, true, "", "@silence_on", None)
                      )
                      silenceTimer = context.system.scheduler.scheduleOnce(
                        time minutes,
                        () => {
                          sessionActor ! SessionActor.SetSilenced(false)
                          sessionActor ! SessionActor.SendResponse(
                            ChatMessage(ChatMessageType.UNK_71, true, "", "@silence_timeout", None)
                          )
                        }
                      )
                    }

                  case (name, time) =>
                    log.error(s"bad silence args $name $time")
                }
              case _ =>
                log.error(s"unexpected messageType $message")
            }
          case None =>
            log.error("failed to handle incoming message because dependencies are missing")
        }
        this

    }
  }

}
