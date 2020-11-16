package net.psforever.actors.session

import akka.actor.Cancellable
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.{ActorRef, Behavior, PostStop, SupervisorStrategy}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.avatar.{BattleRank, Certification, CommandRank, Cosmetic}
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.{Default, Player, Session}
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.serverobject.turret.{FacilityTurret, TurretUpgrade, WeaponTurrets}
import net.psforever.objects.zones.Zoning
import net.psforever.packet.game.{ChatMsg, DeadState, RequestDestroyMessage, ZonePopulationUpdateMessage}
import net.psforever.types.{ChatMessageType, PlanetSideEmpire, PlanetSideGUID, Vector3}
import net.psforever.util.{Config, PointOfInterest}
import net.psforever.zones.Zones
import net.psforever.services.chat.ChatService
import net.psforever.services.chat.ChatService.ChatChannel
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import akka.actor.typed.scaladsl.adapter._

object ChatActor {
  def apply(
      sessionActor: ActorRef[SessionActor.Command],
      avatarActor: ActorRef[AvatarActor.Command]
  ): Behavior[Command] =
    Behaviors
      .supervise[Command] {
        Behaviors.withStash(100) { buffer =>
          Behaviors.setup(context => new ChatActor(context, buffer, sessionActor, avatarActor).start())
        }
      }
      .onFailure[Exception](SupervisorStrategy.restart)

  sealed trait Command

  final case class JoinChannel(channel: ChatChannel)  extends Command
  final case class LeaveChannel(channel: ChatChannel) extends Command
  final case class Message(message: ChatMsg)          extends Command
  final case class SetSession(session: Session)       extends Command

  private case class ListingResponse(listing: Receptionist.Listing)                            extends Command
  private case class IncomingMessage(session: Session, message: ChatMsg, channel: ChatChannel) extends Command
}

class ChatActor(
    context: ActorContext[ChatActor.Command],
    buffer: StashBuffer[ChatActor.Command],
    sessionActor: ActorRef[SessionActor.Command],
    avatarActor: ActorRef[AvatarActor.Command]
) {

  import ChatActor._

  implicit val ec: ExecutionContextExecutor = context.executionContext

  private[this] val log                                  = org.log4s.getLogger
  var channels: List[ChatChannel]                        = List()
  var session: Option[Session]                           = None
  var chatService: Option[ActorRef[ChatService.Command]] = None
  var silenceTimer: Cancellable                          = Default.Cancellable

  val chatServiceAdapter: ActorRef[ChatService.MessageResponse] = context.messageAdapter[ChatService.MessageResponse] {
    case ChatService.MessageResponse(session, message, channel) => IncomingMessage(session, message, channel)
  }

  context.system.receptionist ! Receptionist.Find(
    ChatService.ChatServiceKey,
    context.messageAdapter[Receptionist.Listing](ListingResponse)
  )

  def start(): Behavior[Command] = {
    Behaviors
      .receiveMessage[Command] {
        case ListingResponse(ChatService.ChatServiceKey.Listing(listings)) =>
          chatService = Some(listings.head)
          channels ++= List(ChatChannel.Default())
          postStartBehaviour()

        case SetSession(newSession) =>
          session = Some(newSession)
          postStartBehaviour()

        case other =>
          buffer.stash(other)
          Behaviors.same
      }

  }

  def postStartBehaviour(): Behavior[Command] = {
    (session, chatService) match {
      case (Some(session), Some(chatService)) if session.player != null =>
        chatService ! ChatService.JoinChannel(chatServiceAdapter, session, ChatChannel.Default())
        buffer.unstashAll(active(session, chatService))
      case _ =>
        Behaviors.same
    }

  }

  def active(session: Session, chatService: ActorRef[ChatService.Command]): Behavior[Command] = {
    import ChatMessageType._

    Behaviors
      .receiveMessagePartial[Command] {
        case SetSession(newSession) =>
          active(newSession, chatService)

        case JoinChannel(channel) =>
          chatService ! ChatService.JoinChannel(chatServiceAdapter, session, channel)
          channels ++= List(channel)
          Behaviors.same

        case LeaveChannel(channel) =>
          chatService ! ChatService.LeaveChannel(chatServiceAdapter, channel)
          channels = channels.filter(_ == channel)
          Behaviors.same

        case Message(message) =>
          log.info("Chat: " + message)

          val gmCommandAllowed =
            session.account.gm || Config.app.development.unprivilegedGmCommands.contains(message.messageType)

          (message.messageType, message.recipient.trim, message.contents.trim) match {
            case (CMT_FLY, recipient, contents) if gmCommandAllowed =>
              val flying = contents match {
                case "on"  => true
                case "off" => false
                case _     => !session.flying
              }
              sessionActor ! SessionActor.SetFlying(flying)
              sessionActor ! SessionActor.SendResponse(
                ChatMsg(CMT_FLY, false, recipient, if (flying) "on" else "off", None)
              )

            case (CMT_ANONYMOUS, _, _) =>
            // ?

            case (CMT_TOGGLE_GM, _, _) =>
            // ?

            case (CMT_CULLWATERMARK, _, contents) =>
              val connectionState =
                if (contents.contains("40 80")) 100
                else if (contents.contains("120 200")) 25
                else 50
              sessionActor ! SessionActor.SetConnectionState(connectionState)

            case (CMT_SPEED, recipient, contents) if gmCommandAllowed =>
              val speed =
                try {
                  contents.toFloat
                } catch {
                  case _: Throwable =>
                    1f
                }
              sessionActor ! SessionActor.SetSpeed(speed)
              sessionActor ! SessionActor.SendResponse(message.copy(contents = f"$speed%.3f"))

            case (CMT_TOGGLESPECTATORMODE, _, contents) if gmCommandAllowed =>
              val spectator = contents match {
                case "on"  => true
                case "off" => false
                case _     => !session.player.spectator
              }
              sessionActor ! SessionActor.SetSpectator(spectator)
              sessionActor ! SessionActor.SendResponse(message.copy(contents = if (spectator) "on" else "off"))
              sessionActor ! SessionActor.SendResponse(
                message.copy(
                  messageType = UNK_227,
                  contents = if (spectator) "@SpectatorEnabled" else "@SpectatorDisabled"
                )
              )

            case (CMT_RECALL, _, _) =>
              val errorMessage = session.zoningType match {
                case Zoning.Method.Quit => Some("You can't recall to your sanctuary continent while quitting")
                case Zoning.Method.InstantAction =>
                  Some("You can't recall to your sanctuary continent while instant actioning")
                case Zoning.Method.Recall => Some("You already requested to recall to your sanctuary continent")
                case _ if session.zone.id == Zones.sanctuaryZoneId(session.player.Faction) =>
                  Some("You can't recall to your sanctuary when you are already in your sanctuary")
                case _ if !session.player.isAlive || session.deadState != DeadState.Alive =>
                  Some(if (session.player.isAlive) "@norecall_deconstructing" else "@norecall_dead")
                case _ if session.player.VehicleSeated.nonEmpty => Some("@norecall_invehicle")
                case _                                          => None
              }
              errorMessage match {
                case Some(errorMessage) =>
                  sessionActor ! SessionActor.SendResponse(
                    ChatMsg(
                      CMT_QUIT,
                      false,
                      "",
                      errorMessage,
                      None
                    )
                  )
                case None =>
                  sessionActor ! SessionActor.Recall()
              }

            case (CMT_INSTANTACTION, _, _) =>
              if (session.zoningType == Zoning.Method.Quit) {
                sessionActor ! SessionActor.SendResponse(
                  ChatMsg(CMT_QUIT, false, "", "You can't instant action while quitting.", None)
                )
              } else if (session.zoningType == Zoning.Method.InstantAction) {
                sessionActor ! SessionActor.SendResponse(
                  ChatMsg(CMT_QUIT, false, "", "@noinstantaction_instantactionting", None)
                )
              } else if (session.zoningType == Zoning.Method.Recall) {
                sessionActor ! SessionActor.SendResponse(
                  ChatMsg(
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
                    ChatMsg(CMT_QUIT, false, "", "@noinstantaction_deconstructing", None)
                  )
                } else {
                  sessionActor ! SessionActor.SendResponse(
                    ChatMsg(CMT_QUIT, false, "", "@noinstantaction_dead", None)
                  )
                }
              } else if (session.player.VehicleSeated.nonEmpty) {
                sessionActor ! SessionActor.SendResponse(
                  ChatMsg(CMT_QUIT, false, "", "@noinstantaction_invehicle", None)
                )
              } else {
                sessionActor ! SessionActor.InstantAction()
              }

            case (CMT_QUIT, _, _) =>
              if (session.zoningType == Zoning.Method.Quit) {
                sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, false, "", "@noquit_quitting", None))
              } else if (!session.player.isAlive || session.deadState != DeadState.Alive) {
                if (session.player.isAlive) {
                  sessionActor ! SessionActor.SendResponse(
                    ChatMsg(CMT_QUIT, false, "", "@noquit_deconstructing", None)
                  )
                } else {
                  sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, false, "", "@noquit_dead", None))
                }
              } else if (session.player.VehicleSeated.nonEmpty) {
                sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, false, "", "@noquit_invehicle", None))
              } else {
                sessionActor ! SessionActor.Quit()
              }

            case (CMT_SUICIDE, _, _) =>
              if (session.player.isAlive && session.deadState != DeadState.Release) {
                sessionActor ! SessionActor.Suicide()
              }

            case (CMT_DESTROY, _, contents) =>
              val guid = contents.toInt
              session.zone.GUID(session.zone.map.terminalToSpawnPad.getOrElse(guid, guid)) match {
                case Some(pad: VehicleSpawnPad) =>
                  pad.Actor ! VehicleSpawnControl.ProcessControl.Flush
                case Some(turret: FacilityTurret) if turret.isUpgrading =>
                  WeaponTurrets.FinishUpgradingMannedTurret(turret, TurretUpgrade.None)
                case _ =>
                  // FIXME we shouldn't do it like that
                  sessionActor.toClassic ! RequestDestroyMessage(PlanetSideGUID(guid))
              }
              sessionActor ! SessionActor.SendResponse(message)

            /** Messages starting with ! are custom chat commands */
            case (messageType, recipient, contents) if contents.startsWith("!") =>
              (messageType, recipient, contents) match {
                case (_, _, contents) if contents.startsWith("!whitetext ") && session.account.gm =>
                  chatService ! ChatService.Message(
                    session,
                    ChatMsg(UNK_227, true, "", contents.replace("!whitetext ", ""), None),
                    ChatChannel.Default()
                  )

                case (_, _, "!loc") =>
                  val continent = session.zone
                  val player    = session.player
                  val loc =
                    s"zone=${continent.id} pos=${player.Position.x},${player.Position.y},${player.Position.z}; ori=${player.Orientation.x},${player.Orientation.y},${player.Orientation.z}"
                  log.info(loc)
                  sessionActor ! SessionActor.SendResponse(message.copy(contents = loc))

                case (_, _, contents) if contents.startsWith("!list") =>
                  val zone = contents.split(" ").lift(1) match {
                    case None =>
                      Some(session.zone)
                    case Some(id) =>
                      Zones.zones.find(_.id == id)
                  }

                  zone match {
                    case Some(zone) =>
                      sessionActor ! SessionActor.SendResponse(
                        ChatMsg(
                          CMT_GMOPEN,
                          message.wideContents,
                          "Server",
                          "\\#8Name (Faction) [ID] at PosX PosY PosZ",
                          message.note
                        )
                      )

                      (zone.LivePlayers ++ zone.Corpses)
                        .filter(_.CharId != session.player.CharId)
                        .sortBy(p => (p.Name, !p.isAlive))
                        .foreach(player => {
                          val color = if (!player.isAlive) "\\#7" else ""
                          sessionActor ! SessionActor.SendResponse(
                            ChatMsg(
                              CMT_GMOPEN,
                              message.wideContents,
                              "Server",
                              s"${color}${player.Name} (${player.Faction}) [${player.CharId}] at ${player.Position.x.toInt} ${player.Position.y.toInt} ${player.Position.z.toInt}",
                              message.note
                            )
                          )
                        })
                    case None =>
                      sessionActor ! SessionActor.SendResponse(
                        ChatMsg(
                          CMT_GMOPEN,
                          message.wideContents,
                          "Server",
                          "Invalid zone ID",
                          message.note
                        )
                      )
                  }

                case (_, _, contents) if contents.startsWith("!ntu") && gmCommandAllowed =>
                  val buffer = contents.toLowerCase.split("\\s+")
                  val (facility, customNtuValue) = (buffer.lift(1), buffer.lift(2)) match {
                    case (Some(x), Some(y)) if y.toIntOption.nonEmpty => (Some(x), Some(y.toInt))
                    case (Some(x), None) if x.toIntOption.nonEmpty    => (None, Some(x.toInt))
                    case _                                            => (None, None)
                  }
                  val silos = (facility match {
                    case Some(cur) if cur.toLowerCase().startsWith("curr") =>
                      val position = session.player.Position
                      session.zone.Buildings.values
                        .filter { building =>
                          val soi2 = building.Definition.SOIRadius * building.Definition.SOIRadius
                          Vector3.DistanceSquared(building.Position, position) < soi2
                        }
                    case Some(x) =>
                      session.zone.Buildings.values.find { _.Name.equalsIgnoreCase(x) }.toList
                    case _ =>
                      session.zone.Buildings.values
                  })
                    .flatMap { building => building.Amenities.filter { _.isInstanceOf[ResourceSilo] } }
                  if(silos.isEmpty) {
                    sessionActor ! SessionActor.SendResponse(
                      ChatMsg(UNK_229, true, "Server", s"no targets for ntu found with parameters $facility", None)
                    )
                  }
                  customNtuValue match {
                    // x = n0% of maximum capacitance
                    case Some(value) if value > -1 && value < 11 =>
                      silos.collect { case silo: ResourceSilo =>
                        silo.Actor ! ResourceSilo.UpdateChargeLevel(value * silo.MaxNtuCapacitor * 0.1f - silo.NtuCapacitor)
                      }
                    // capacitance set to x (where x > 10) exactly, within limits
                    case Some(value) =>
                      silos.collect { case silo: ResourceSilo =>
                        silo.Actor ! ResourceSilo.UpdateChargeLevel(value - silo.NtuCapacitor)
                      }
                    case None =>
                      // x >= n0% of maximum capacitance and x <= maximum capacitance
                      val rand = new scala.util.Random
                      silos.collect { case silo: ResourceSilo =>
                        val a = 7
                        val b = 10 - a
                        val tenth = silo.MaxNtuCapacitor * 0.1f
                        silo.Actor ! ResourceSilo.UpdateChargeLevel(
                          a * tenth + rand.nextFloat() * b * tenth - silo.NtuCapacitor
                        )
                      }
                  }

                case _ =>
                // unknown ! commands are ignored
              }

            case (CMT_CAPTUREBASE, _, contents) if gmCommandAllowed =>
              val args = contents.split(" ").filter(_ != "")

              val (faction, factionPos): (PlanetSideEmpire.Value, Option[Int]) = args.zipWithIndex
                .map { case (factionName, pos) => (factionName.toLowerCase, pos) }
                .flatMap {
                  case ("tr", pos)   => Some(PlanetSideEmpire.TR, pos)
                  case ("nc", pos)   => Some(PlanetSideEmpire.NC, pos)
                  case ("vs", pos)   => Some(PlanetSideEmpire.VS, pos)
                  case ("none", pos) => Some(PlanetSideEmpire.NEUTRAL, pos)
                  case _             => None
                }
                .headOption match {
                case Some((isFaction, pos)) => (isFaction, Some(pos))
                case None                 => (session.player.Faction, None)
              }

              val (buildingsOption, buildingPos): (Option[Seq[Building]], Option[Int]) = args.zipWithIndex.flatMap {
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
                case (name: String, pos) =>
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

              val (timerOption, timerPos): (Option[Int], Option[Int]) = args.zipWithIndex.flatMap {
                case (_, pos)
                    if factionPos.isDefined && factionPos.get == pos || buildingPos.isDefined && buildingPos.get == pos =>
                  None
                case (timer: String, pos) =>
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
                  val buildings: Seq[Building] = buildingsOption.getOrElse(
                    session.zone.Buildings
                      .values
                      .filter { building =>
                        building.PlayersInSOI.exists { soiPlayer =>
                          session.player.CharId == soiPlayer.CharId
                        }
                      }
                      .toSeq
                  )
                  buildings foreach { building =>
                    // TODO implement timer
                    building.Actor ! BuildingActor.SetFaction(faction)
                  }
                case (_, Some(0), _, None, _) =>
                  sessionActor ! SessionActor.SendResponse(
                    ChatMsg(
                      UNK_229,
                      true,
                      "",
                      s"\\#FF4040ERROR - \'${args(0)}\' is not a valid building name.",
                      None
                    )
                  )
                case (Some(0), _, Some(1), _, None) | (Some(1), Some(0), Some(2), _, None) =>
                  sessionActor ! SessionActor.SendResponse(
                    ChatMsg(
                      UNK_229,
                      true,
                      "",
                      s"\\#FF4040ERROR - \'${args(timerPos.get)}\' is not a valid timer value.",
                      None
                    )
                  )
                case _ =>
                  sessionActor ! SessionActor.SendResponse(
                    message.copy(messageType = UNK_229, contents = "@CMT_CAPTUREBASE_usage")
                  )
              }

            case (CMT_GMBROADCAST | CMT_GMBROADCAST_NC | CMT_GMBROADCAST_VS | CMT_GMBROADCAST_TR, _, _)
                if gmCommandAllowed =>
              chatService ! ChatService.Message(
                session,
                message.copy(recipient = session.player.Name),
                ChatChannel.Default()
              )

            case (CMT_GMTELL, _, _) if gmCommandAllowed =>
              chatService ! ChatService.Message(
                session,
                message,
                ChatChannel.Default()
              )

            case (CMT_GMBROADCASTPOPUP, _, _) if gmCommandAllowed =>
              chatService ! ChatService.Message(
                session,
                message.copy(recipient = session.player.Name),
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

            case (CMT_COMMAND, _, _) if gmCommandAllowed =>
              chatService ! ChatService.Message(
                session,
                message.copy(recipient = session.player.Name),
                ChatChannel.Default()
              )

            case (CMT_NOTE, _, _) =>
              chatService ! ChatService.Message(session, message, ChatChannel.Default())

            case (CMT_SILENCE, _, _) if gmCommandAllowed =>
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
              val contName = session.zone.map.name

              sessionActor ! SessionActor.SendResponse(
                ChatMsg(ChatMessageType.CMT_WHO, true, "", "That command doesn't work for now, but : ", None)
              )
              sessionActor ! SessionActor.SendResponse(
                ChatMsg(ChatMessageType.CMT_WHO, true, "", "NC online : " + popNC + " on " + contName, None)
              )
              sessionActor ! SessionActor.SendResponse(
                ChatMsg(ChatMessageType.CMT_WHO, true, "", "TR online : " + popTR + " on " + contName, None)
              )
              sessionActor ! SessionActor.SendResponse(
                ChatMsg(ChatMessageType.CMT_WHO, true, "", "VS online : " + popVS + " on " + contName, None)
              )

            case (CMT_ZONE, _, contents) if gmCommandAllowed =>
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
                  sessionActor ! SessionActor.SendResponse(ChatMsg(UNK_229, true, "", PointOfInterest.list, None))
                case (Some(zone), None, true) =>
                  sessionActor ! SessionActor.SendResponse(
                    ChatMsg(UNK_229, true, "", PointOfInterest.listWarpgates(zone), None)
                  )
                case (Some(zone), Some(gate), false) =>
                  sessionActor ! SessionActor.SetZone(zone.zonename, gate)
                case (_, None, false) =>
                  sessionActor ! SessionActor.SendResponse(
                    ChatMsg(UNK_229, true, "", "Gate id not defined (use '/zone <zone> -list')", None)
                  )
                case (_, _, _) if buffer.isEmpty || buffer(0).equals("-help") =>
                  sessionActor ! SessionActor.SendResponse(
                    message.copy(messageType = UNK_229, contents = "@CMT_ZONE_usage")
                  )
              }

            case (CMT_WARP, _, contents) if gmCommandAllowed =>
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
                  PointOfInterest.getWarpLocation(session.zone.id, waypoint) match {
                    case Some(location) => sessionActor ! SessionActor.SetPosition(location)
                    case None =>
                      sessionActor ! SessionActor.SendResponse(
                        ChatMsg(UNK_229, true, "", s"unknown location '$waypoint'", None)
                      )
                  }
                case _ =>
                  sessionActor ! SessionActor.SendResponse(
                    message.copy(messageType = UNK_229, contents = "@CMT_WARP_usage")
                  )
              }

            case (CMT_SETBATTLERANK, _, contents) if gmCommandAllowed =>
              val buffer = contents.toLowerCase.split("\\s+")
              val (target, rank) = (buffer.lift(0), buffer.lift(1)) match {
                case (Some(target), Some(rank)) if target == session.avatar.name =>
                  rank.toIntOption match {
                    case Some(rank) => (None, BattleRank.withValueOpt(rank))
                    case None       => (None, None)
                  }
                case (Some(target), Some(rank)) =>
                  // picking other targets is not supported for now
                  (None, None)
                case (Some(rank), None) =>
                  rank.toIntOption match {
                    case Some(rank) => (None, BattleRank.withValueOpt(rank))
                    case None       => (None, None)
                  }
                case _ => (None, None)
              }
              (target, rank) match {
                case (_, Some(rank)) =>
                  avatarActor ! AvatarActor.SetBep(rank.experience)
                  sessionActor ! SessionActor.SendResponse(message.copy(contents = "@AckSuccessSetBattleRank"))
                case _ =>
                  sessionActor ! SessionActor.SendResponse(
                    message.copy(messageType = UNK_229, contents = "@CMT_SETBATTLERANK_usage")
                  )
              }

            case (CMT_SETCOMMANDRANK, _, contents) if gmCommandAllowed =>
              val buffer = contents.toLowerCase.split("\\s+")
              val (target, rank) = (buffer.lift(0), buffer.lift(1)) match {
                case (Some(target), Some(rank)) if target == session.avatar.name =>
                  rank.toIntOption match {
                    case Some(rank) => (None, CommandRank.withValueOpt(rank))
                    case None       => (None, None)
                  }
                case (Some(target), Some(rank)) =>
                  // picking other targets is not supported for now
                  (None, None)
                case (Some(rank), None) =>
                  rank.toIntOption match {
                    case Some(rank) => (None, CommandRank.withValueOpt(rank))
                    case None       => (None, None)
                  }
                case _ => (None, None)
              }
              (target, rank) match {
                case (_, Some(rank)) =>
                  avatarActor ! AvatarActor.SetCep(rank.experience)
                  sessionActor ! SessionActor.SendResponse(message.copy(contents = "@AckSuccessSetCommandRank"))
                case _ =>
                  sessionActor ! SessionActor.SendResponse(
                    message.copy(messageType = UNK_229, contents = "@CMT_SETCOMMANDRANK_usage")
                  )
              }

            case (CMT_ADDBATTLEEXPERIENCE, _, contents) if gmCommandAllowed =>
              contents.toIntOption match {
                case Some(bep) => avatarActor ! AvatarActor.AwardBep(bep)
                case None =>
                  sessionActor ! SessionActor.SendResponse(
                    message.copy(messageType = UNK_229, contents = "@CMT_ADDBATTLEEXPERIENCE_usage")
                  )
              }

            case (CMT_ADDCOMMANDEXPERIENCE, _, contents) if gmCommandAllowed =>
              contents.toIntOption match {
                case Some(cep) => avatarActor ! AvatarActor.AwardCep(cep)
                case None =>
                  sessionActor ! SessionActor.SendResponse(
                    message.copy(messageType = UNK_229, contents = "@CMT_ADDCOMMANDEXPERIENCE_usage")
                  )
              }

            case (CMT_TOGGLE_HAT, _, contents) =>
              val cosmetics = session.avatar.cosmetics.getOrElse(Set())
              val nextCosmetics = contents match {
                case "off" =>
                  cosmetics.diff(Set(Cosmetic.BrimmedCap, Cosmetic.Beret))
                case _ =>
                  if (cosmetics.contains(Cosmetic.BrimmedCap)) {
                    cosmetics.diff(Set(Cosmetic.BrimmedCap)) + Cosmetic.Beret
                  } else if (cosmetics.contains(Cosmetic.Beret)) {
                    cosmetics.diff(Set(Cosmetic.BrimmedCap, Cosmetic.Beret))
                  } else {
                    cosmetics + Cosmetic.BrimmedCap
                  }
              }
              val on = nextCosmetics.contains(Cosmetic.BrimmedCap) || nextCosmetics.contains(Cosmetic.Beret)

              avatarActor ! AvatarActor.SetCosmetics(nextCosmetics)
              sessionActor ! SessionActor.SendResponse(
                message.copy(
                  messageType = UNK_229,
                  contents = s"@CMT_TOGGLE_HAT_${if (on) "on" else "off"}"
                )
              )

            case (CMT_HIDE_HELMET | CMT_TOGGLE_SHADES | CMT_TOGGLE_EARPIECE, _, contents) =>
              val cosmetics = session.avatar.cosmetics.getOrElse(Set())

              val cosmetic = message.messageType match {
                case CMT_HIDE_HELMET     => Cosmetic.NoHelmet
                case CMT_TOGGLE_SHADES   => Cosmetic.Sunglasses
                case CMT_TOGGLE_EARPIECE => Cosmetic.Earpiece
                case _                   => null
              }

              val on = contents match {
                case "on"  => true
                case "off" => false
                case _     => !cosmetics.contains(cosmetic)
              }

              avatarActor ! AvatarActor.SetCosmetics(
                if (on) cosmetics + cosmetic
                else cosmetics.diff(Set(cosmetic))
              )

              sessionActor ! SessionActor.SendResponse(
                message.copy(
                  messageType = UNK_229,
                  contents = s"@${message.messageType.toString}_${if (on) "on" else "off"}"
                )
              )

            case (CMT_ADDCERTIFICATION, _, contents) if gmCommandAllowed =>
              val certs = contents.split(" ").filter(_ != "").map(name => Certification.values.find(_.name == name))
              if (certs.nonEmpty) {
                if (certs.contains(None)) {
                  sessionActor ! SessionActor.SendResponse(
                    message.copy(
                      messageType = UNK_229,
                      contents = s"@AckErrorCertifications"
                    )
                  )
                } else {
                  avatarActor ! AvatarActor.SetCertifications(session.avatar.certifications ++ certs.flatten)
                  sessionActor ! SessionActor.SendResponse(
                    message.copy(
                      messageType = UNK_229,
                      contents = s"@AckSuccessCertifications"
                    )
                  )
                }
              } else {
                if (session.avatar.certifications.size < Certification.values.size) {
                  avatarActor ! AvatarActor.SetCertifications(Certification.values.toSet)
                } else {
                  avatarActor ! AvatarActor.SetCertifications(Certification.values.filter(_.cost == 0).toSet)
                }
                sessionActor ! SessionActor.SendResponse(
                  message.copy(
                    messageType = UNK_229,
                    contents = s"@AckSuccessCertifications"
                  )
                )
              }

            case (CMT_KICK, _, contents) if gmCommandAllowed =>
              val inputs = contents.split("\\s+").filter(_ != "")
              inputs.headOption match {
                case Some(input) =>
                  val determination: Player => Boolean = input.toLongOption match {
                    case Some(id) => _.CharId == id
                    case _        => _.Name.equals(input)
                  }
                  session.zone.LivePlayers
                    .find(determination)
                    .orElse(session.zone.Corpses.find(determination)) match {
                    case Some(player) =>
                      inputs.lift(1).map(_.toLongOption) match {
                        case Some(Some(time)) =>
                          sessionActor ! SessionActor.Kick(player, Some(time))
                        case _ =>
                          sessionActor ! SessionActor.Kick(player)
                      }

                      sessionActor ! SessionActor.SendResponse(
                        ChatMsg(
                          UNK_229,
                          message.wideContents,
                          "Server",
                          "@kick_i",
                          message.note
                        )
                      )
                    case None =>
                      sessionActor ! SessionActor.SendResponse(
                        ChatMsg(
                          UNK_229,
                          message.wideContents,
                          "Server",
                          "@kick_o",
                          message.note
                        )
                      )
                  }
                case None =>
                  sessionActor ! SessionActor.SendResponse(
                    ChatMsg(
                      UNK_229,
                      message.wideContents,
                      "Server",
                      "@kick_o",
                      message.note
                    )
                  )
              }

            case _ =>
              log.info(s"unhandled chat message $message")
          }
          Behaviors.same

        case IncomingMessage(fromSession, message, channel) =>
          message.messageType match {
            case CMT_TELL | U_CMT_TELLFROM | CMT_BROADCAST | CMT_SQUAD | CMT_PLATOON | CMT_COMMAND | UNK_45 | UNK_71 |
                CMT_NOTE | CMT_GMBROADCAST | CMT_GMBROADCAST_NC | CMT_GMBROADCAST_TR | CMT_GMBROADCAST_VS |
                CMT_GMBROADCASTPOPUP | CMT_GMTELL | U_CMT_GMTELLFROM | UNK_227 | UNK_229 =>
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
                      ChatMsg(ChatMessageType.UNK_229, true, "", "@silence_off", None)
                    )
                    if (!silenceTimer.isCancelled) silenceTimer.cancel()
                  } else {
                    sessionActor ! SessionActor.SetSilenced(true)
                    sessionActor ! SessionActor.SendResponse(
                      ChatMsg(ChatMessageType.UNK_229, true, "", "@silence_on", None)
                    )
                    silenceTimer = context.system.scheduler.scheduleOnce(
                      time minutes,
                      () => {
                        sessionActor ! SessionActor.SetSilenced(false)
                        sessionActor ! SessionActor.SendResponse(
                          ChatMsg(ChatMessageType.UNK_229, true, "", "@silence_timeout", None)
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
          Behaviors.same
      }
      .receiveSignal {
        case (_, _: PostStop) =>
          silenceTimer.cancel()
          chatService ! ChatService.LeaveAllChannels(chatServiceAdapter)
          Behaviors.same
        case _ =>
          Behaviors.same
      }

  }

}
