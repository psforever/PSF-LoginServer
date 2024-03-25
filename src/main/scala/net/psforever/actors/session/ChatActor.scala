package net.psforever.actors.session

import akka.actor.Cancellable
import akka.actor.typed.{ActorRef, Behavior, PostStop, SupervisorStrategy}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.scaladsl.adapter._
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.objects.zones.ZoneInfo
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

import scala.annotation.unused
import scala.collection.{Seq, mutable}
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
//
import net.psforever.actors.zone.BuildingActor
import net.psforever.login.WorldSession
import net.psforever.objects.{Default, Player, Session}
import net.psforever.objects.avatar.{BattleRank, Certification, CommandRank, Shortcut => AvatarShortcut}
import net.psforever.objects.definition.ImplantDefinition
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.serverobject.turret.{FacilityTurret, TurretUpgrade, WeaponTurrets}
import net.psforever.objects.zones.Zoning
import net.psforever.packet.game.objectcreate.DrawnSlot
import net.psforever.packet.game.{ChatMsg, CreateShortcutMessage, DeadState, RequestDestroyMessage, Shortcut, ZonePopulationUpdateMessage}
import net.psforever.services.{CavernRotationService, InterstellarClusterService}
import net.psforever.services.chat.ChatService
import net.psforever.services.chat.ChatService.ChatChannel
import net.psforever.types.ChatMessageType.{CMT_GMOPEN, UNK_227, UNK_229}
import net.psforever.types.{ChatMessageType, Cosmetic, ExperienceType, ImplantType, PlanetSideEmpire, PlanetSideGUID, Vector3}
import net.psforever.util.{Config, PointOfInterest}
import net.psforever.zones.Zones

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

  /**
    * For a provided number of facility nanite transfer unit resource silos,
    * charge the facility's silo with an expected amount of nanite transfer units.
    * @see `Amenity`
    * @see `ChatMsg`
    * @see `ResourceSilo`
    * @see `ResourceSilo.UpdateChargeLevel`
    * @see `SessionActor.Command`
    * @see `SessionActor.SendResponse`
    * @param session messaging reference back tothe target session
    * @param resources the optional number of resources to set to each silo;
    *                  different values provide different resources as indicated below;
    *                  an undefined value also has a condition
    * @param silos where to deposit the resources
    * @param debugContent something for log output context
    */
  private def setBaseResources(
                                session: ActorRef[SessionActor.Command],
                                resources: Option[Int],
                                silos: Iterable[Amenity],
                                debugContent: String
                              ): Unit = {
    if (silos.isEmpty) {
      session ! SessionActor.SendResponse(
        ChatMsg(UNK_229, wideContents=true, "Server", s"no targets for ntu found with parameters $debugContent", None)
      )
    }
    resources match {
      // x = n0% of maximum capacitance
      case Some(value) if value > -1 && value < 11 =>
        silos.collect {
          case silo: ResourceSilo =>
            silo.Actor ! ResourceSilo.UpdateChargeLevel(
              value * silo.MaxNtuCapacitor * 0.1f - silo.NtuCapacitor
            )
        }
      // capacitance set to x (where x > 10) exactly, within limits
      case Some(value) =>
        silos.collect {
          case silo: ResourceSilo =>
            silo.Actor ! ResourceSilo.UpdateChargeLevel(value - silo.NtuCapacitor)
        }
      case None =>
        // x >= n0% of maximum capacitance and x <= maximum capacitance
        val rand = new scala.util.Random
        silos.collect {
          case silo: ResourceSilo =>
            val a     = 7
            val b     = 10 - a
            val tenth = silo.MaxNtuCapacitor * 0.1f
            silo.Actor ! ResourceSilo.UpdateChargeLevel(
              a * tenth + rand.nextFloat() * b * tenth - silo.NtuCapacitor
            )
        }
    }
  }
}

class ChatActor(
    context: ActorContext[ChatActor.Command],
    buffer: StashBuffer[ChatActor.Command],
    sessionActor: ActorRef[SessionActor.Command],
    avatarActor: ActorRef[AvatarActor.Command]
) {

  import ChatActor._

  implicit val ec: ExecutionContextExecutor = context.executionContext

  private[this] val log                                             = org.log4s.getLogger
  var channels: List[ChatChannel]                                   = List()
  var session: Option[Session]                                      = None
  var chatService: Option[ActorRef[ChatService.Command]]            = None
  var cluster: Option[ActorRef[InterstellarClusterService.Command]] = None
  var silenceTimer: Cancellable                                     = Default.Cancellable
  /**
    * when another player is listed as one of our ignored players,
    * and that other player sends an emote,
    * that player is assigned a cooldown and only one emote per period will be seen<br>
    * key - character unique avatar identifier, value - when the current cooldown period will end
    */
  var ignoredEmoteCooldown: mutable.LongMap[Long]                   = mutable.LongMap[Long]()

  val chatServiceAdapter: ActorRef[ChatService.MessageResponse] = context.messageAdapter[ChatService.MessageResponse] {
    case ChatService.MessageResponse(_session, message, channel) => IncomingMessage(_session, message, channel)
  }

  context.system.receptionist ! Receptionist.Find(
    ChatService.ChatServiceKey,
    context.messageAdapter[Receptionist.Listing](ListingResponse)
  )

  context.system.receptionist ! Receptionist.Find(
    InterstellarClusterService.InterstellarClusterServiceKey,
    context.messageAdapter[Receptionist.Listing](ListingResponse)
  )

  def start(): Behavior[Command] = {
    Behaviors
      .receiveMessage[Command] {
      case ListingResponse(InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings)) =>
        listings.headOption match {
          case Some(ref) =>
            cluster = Some(ref)
            postStartBehaviour()
          case None =>
            context.system.receptionist ! Receptionist.Find(
              InterstellarClusterService.InterstellarClusterServiceKey,
              context.messageAdapter[Receptionist.Listing](ListingResponse)
            )
            Behaviors.same
        }

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
    (session, chatService, cluster) match {
      case (Some(_session), Some(_chatService), Some(_cluster)) if _session.player != null =>
        _chatService ! ChatService.JoinChannel(chatServiceAdapter, _session, ChatChannel.Default())
        buffer.unstashAll(active(_session, _chatService, _cluster))
      case _ =>
        Behaviors.same
    }
  }

  def active(
              session: Session,
              chatService: ActorRef[ChatService.Command],
              cluster: ActorRef[InterstellarClusterService.Command]
            ): Behavior[Command] = {
    import ChatMessageType._

    Behaviors
      .receiveMessagePartial[Command] {
        case SetSession(newSession) =>
          this.session = Some(newSession)
          active(newSession, chatService, cluster)

        case JoinChannel(channel) =>
          chatService ! ChatService.JoinChannel(chatServiceAdapter, session, channel)
          channels ++= List(channel)
          Behaviors.same

        case LeaveChannel(channel) =>
          chatService ! ChatService.LeaveChannel(chatServiceAdapter, channel)
          channels = channels.filterNot(_ == channel)
          Behaviors.same

        case Message(message) =>
          val gmCommandAllowed =
            session.account.gm || Config.app.development.unprivilegedGmCommands.contains(message.messageType)

          (message.messageType, message.recipient.trim, message.contents.trim) match {
            /** Messages starting with ! are custom chat commands */
            case (_, _, contents) if contents.startsWith("!") &&
              customCommandMessages(message, session, chatService, cluster, gmCommandAllowed) => ;

            case (CMT_FLY, recipient, contents) if gmCommandAllowed =>
              val flying = contents match {
                case "on"  => true
                case "off" => false
                case _     => !session.flying
              }
              sessionActor ! SessionActor.SetFlying(flying)
              sessionActor ! SessionActor.SendResponse(
                ChatMsg(CMT_FLY, wideContents=false, recipient, if (flying) "on" else "off", None)
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
                  sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, errorMessage))
                case None =>
                  sessionActor ! SessionActor.Recall()
              }

            case (CMT_INSTANTACTION, _, _) =>
              if (session.zoningType == Zoning.Method.Quit) {
                sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, "You can't instant action while quitting."))
              } else if (session.zoningType == Zoning.Method.InstantAction) {
                sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, "@noinstantaction_instantactionting"))
              } else if (session.zoningType == Zoning.Method.Recall) {
                sessionActor ! SessionActor.SendResponse(
                  ChatMsg(CMT_QUIT, "You won't instant action. You already requested to recall to your sanctuary continent")
                )
              } else if (!session.player.isAlive || session.deadState != DeadState.Alive) {
                if (session.player.isAlive) {
                  sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, "@noinstantaction_deconstructing"))
                } else {
                  sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, "@noinstantaction_dead"))
                }
              } else if (session.player.VehicleSeated.nonEmpty) {
                sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, "@noinstantaction_invehicle"))
              } else {
                sessionActor ! SessionActor.InstantAction()
              }

            case (CMT_QUIT, _, _) =>
              if (session.zoningType == Zoning.Method.Quit) {
                sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, "@noquit_quitting"))
              } else if (!session.player.isAlive || session.deadState != DeadState.Alive) {
                if (session.player.isAlive) {
                  sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, "@noquit_deconstructing"))
                } else {
                  sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, "@noquit_dead"))
                }
              } else if (session.player.VehicleSeated.nonEmpty) {
                sessionActor ! SessionActor.SendResponse(ChatMsg(CMT_QUIT, "@noquit_invehicle"))
              } else {
                sessionActor ! SessionActor.Quit()
              }

            case (CMT_SUICIDE, _, _) =>
              if (session.player.isAlive && session.deadState != DeadState.Release) {
                sessionActor ! SessionActor.Suicide()
              }

            case (CMT_DESTROY, _, contents) if contents.matches("\\d+") =>
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

            case (CMT_SETBASERESOURCES, _, contents) if gmCommandAllowed =>
              val buffer = contents.toLowerCase.split("\\s+")
              val customNtuValue = buffer.lift(1) match {
                case Some(x) if x.toIntOption.nonEmpty => Some(x.toInt)
                case _                                 => None
              }
              val silos = {
                val position = session.player.Position
                session.zone.Buildings.values
                  .filter { building =>
                    val soi2 = building.Definition.SOIRadius * building.Definition.SOIRadius
                    Vector3.DistanceSquared(building.Position, position) < soi2
                  }
              }
                .flatMap { building => building.Amenities.filter { _.isInstanceOf[ResourceSilo] } }
              ChatActor.setBaseResources(sessionActor, customNtuValue, silos, debugContent="")

            case (CMT_ZONELOCK, _, contents) if gmCommandAllowed =>
              val buffer = contents.toLowerCase.split("\\s+")
              val (zoneOpt, lockVal) = (buffer.lift(1), buffer.lift(2)) match {
                case (Some(x), Some(y)) =>
                  val zone = if (x.toIntOption.nonEmpty) {
                    val xInt = x.toInt
                    Zones.zones.find(_.Number == xInt)
                  } else {
                    Zones.zones.find(z => z.id.equals(x))
                  }
                  val value = if (y.toIntOption.nonEmpty && y.toInt == 0) {
                    0
                  } else {
                    1
                  }
                  (zone, Some(value))
                case _ =>
                  (None, None)
              }
              (zoneOpt, lockVal) match {
                case (Some(zone), Some(lock)) if zone.id.startsWith("c") =>
                  //caverns must be rotated in an order
                  if (lock == 0) {
                    cluster ! InterstellarClusterService.CavernRotation(CavernRotationService.HurryRotationToZoneUnlock(zone.id))
                  } else {
                    cluster ! InterstellarClusterService.CavernRotation(CavernRotationService.HurryRotationToZoneLock(zone.id))
                  }
                case (Some(zone), Some(lock)) =>
                  //normal zones can lock when all facilities and towers on it belong to the same faction
                  //normal zones can lock when ???
                case _ => ;
              }

            case (U_CMT_ZONEROTATE, _, contents) if gmCommandAllowed =>
              cluster ! InterstellarClusterService.CavernRotation(CavernRotationService.HurryNextRotation)

            case (CMT_CAPTUREBASE, _, contents) if gmCommandAllowed =>
              val buffer = contents.split(" ").filterNot(_ == "").take(3)
              //walk through the param buffer
              val (foundFacilities, foundFacilitiesTag, factionBuffer) = firstParam(session, buffer, captureBaseParamFacilities)
              val (foundFaction, foundFactionTag, timerBuffer) = firstParam(session, factionBuffer, captureBaseParamFaction)
              val (foundTimer, foundTimerTag, _) = firstParam(session, timerBuffer, captureBaseParamTimer)
              //resolve issues with the initial params
              var facilityError: Int = 0
              var factionError: Boolean = false
              var timerError: Boolean = false
              var usageMessage: Boolean = false
              val resolvedFacilities = foundFacilities
                .orElse {
                  if (foundFacilitiesTag.nonEmpty) {
                    if (foundFaction.isEmpty) {
                      /* /capturebase <bad_facility> OR /capturebase <bad_facility> <no_faction> */
                      //malformed facility tag error
                      facilityError = 2
                      None
                    } else if (!foundFacilitiesTag.contains("curr")) { //did we do this next check already
                      /* /capturebase <faction>, potentially */
                      val buildings = captureBaseCurrSoi(session)
                      if (buildings.nonEmpty) {
                        //convert facilities to faction
                        Some(buildings.toSeq)
                      } else {
                        //no facilities error
                        facilityError = 1
                        None
                      }
                    } else {
                      //no facilities error
                      facilityError = 1
                      None
                    }
                  } else {
                    //no params; post command usage reminder
                    usageMessage = true
                    None
                  }
                }
              val resolvedFaction = foundFaction
                .orElse {
                  if (resolvedFacilities.nonEmpty) {
                    /* /capturebase <facility> OR /capturebase <facility> <timer> */
                    if (foundFactionTag.isEmpty || foundTimer.nonEmpty) {
                      //convert facilities to OUR PLAYER'S faction
                      Some(session.player.Faction)
                    } else {
                      //malformed faction tag error
                      factionError = true
                      None
                    }
                  } else {
                    //incorrect params; already posted an error message
                    None
                  }
                }
              val resolvedTimer = foundTimer
                .orElse {
                  //todo stop command execution? post command usage reminder?
                  if (resolvedFaction.nonEmpty && foundTimerTag.nonEmpty) {
                    /* /capturebase <?> <?> <bad_timer> */
                    //malformed timer tag error
                    timerError = true
                    None
                  } else {
                    //eh
                    Some(1)
                  }
                }
              //evaluate results
              (resolvedFacilities, resolvedFaction, resolvedTimer) match {
                case (Some(buildings), Some(faction), Some(_)) =>
                  buildings.foreach { building =>
                    //TODO implement timer
                    val terminal = building.CaptureTerminal.get
                    val zone = building.Zone
                    val zoneActor = zone.actor
                    val buildingActor = building.Actor
                    //clear any previous hack
                    if (building.CaptureTerminalIsHacked) {
                      zone.LocalEvents ! LocalServiceMessage(
                        zone.id,
                        LocalAction.ResecureCaptureTerminal(terminal, PlayerSource.Nobody)
                      )
                    }
                    //push any updates this might cause
                    zoneActor ! ZoneActor.ZoneMapUpdate()
                    //convert faction affiliation
                    buildingActor ! BuildingActor.SetFaction(faction)
                    buildingActor ! BuildingActor.AmenityStateChange(terminal, Some(false))
                    //push for map updates again
                    zoneActor ! ZoneActor.ZoneMapUpdate()
                  }
                case _ =>
                  if (usageMessage) {
                    sessionActor ! SessionActor.SendResponse(
                      message.copy(messageType = UNK_229, contents = "@CMT_CAPTUREBASE_usage")
                    )
                  } else {
                    val msg = if (facilityError == 1) { "can not contextually determine building target" }
                    else if (facilityError == 2) { s"\'${foundFacilitiesTag.get}\' is not a valid building name" }
                    else if (factionError) { s"\'${foundFactionTag.get}\' is not a valid faction designation" }
                    else if (timerError) { s"\'${foundTimerTag.get}\' is not a valid timer value" }
                    else { "malformed params; check usage" }
                    sessionActor ! SessionActor.SendResponse(ChatMsg(UNK_229, wideContents=true, "", s"\\#FF4040ERROR - $msg", None))
                  }
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

            case (CMT_OPEN, _, _) if !session.player.silenced =>
              chatService ! ChatService.Message(
                session,
                message.copy(recipient = session.player.Name),
                ChatChannel.Default()
              )

            case (CMT_VOICE, _, contents) =>
              // SH prefix are tactical voice macros only sent to squad
              if (contents.startsWith("SH")) {
                channels.foreach {
                  case channel: ChatChannel.Squad =>
                    chatService ! ChatService.Message(session, message.copy(recipient = session.player.Name), channel)
                  case _ =>
                }
              } else {
                chatService ! ChatService.Message(
                  session,
                  message.copy(recipient = session.player.Name),
                  ChatChannel.Default()
                )
              }

            case (CMT_TELL, _, _) if !session.player.silenced =>
              if (AvatarActor.onlineIfNotIgnored(message.recipient, session.avatar.name)) {
                chatService ! ChatService.Message(
                  session,
                  message,
                  ChatChannel.Default()
                )
              } else if (AvatarActor.getLiveAvatarForFunc(message.recipient, (_,_,_)=>{}).isEmpty) {
                sessionActor ! SessionActor.SendResponse(
                  ChatMsg(ChatMessageType.UNK_45, wideContents=false, "none", "@notell_target", None)
                )
              } else {
                sessionActor ! SessionActor.SendResponse(
                  ChatMsg(ChatMessageType.UNK_45, wideContents=false, "none", "@notell_ignore", None)
                )
              }

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

              if (popNC + popTR + popVS == 0) {
                sessionActor ! SessionActor.SendResponse(ChatMsg(ChatMessageType.CMT_WHO, "@Nomatches"))
              } else {
                val contName = session.zone.map.name
                sessionActor ! SessionActor.SendResponse(
                  ChatMsg(ChatMessageType.CMT_WHO, wideContents=true, "", "That command doesn't work for now, but : ", None)
                )
                sessionActor ! SessionActor.SendResponse(
                  ChatMsg(ChatMessageType.CMT_WHO, wideContents=true, "", "NC online : " + popNC + " on " + contName, None)
                )
                sessionActor ! SessionActor.SendResponse(
                  ChatMsg(ChatMessageType.CMT_WHO, wideContents=true, "", "TR online : " + popTR + " on " + contName, None)
                )
                sessionActor ! SessionActor.SendResponse(
                  ChatMsg(ChatMessageType.CMT_WHO, wideContents=true, "", "VS online : " + popVS + " on " + contName, None)
                )
              }

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
                  sessionActor ! SessionActor.SendResponse(ChatMsg(UNK_229, wideContents=true, "", PointOfInterest.list, None))
                case (Some(zone), None, true) =>
                  sessionActor ! SessionActor.SendResponse(
                    ChatMsg(UNK_229, wideContents=true, "", PointOfInterest.listWarpgates(zone), None)
                  )
                case (Some(zone), Some(gate), false) =>
                  sessionActor ! SessionActor.SetZone(zone.zonename, gate)
                case (_, None, false) =>
                  sessionActor ! SessionActor.SendResponse(
                    ChatMsg(UNK_229, wideContents=true, "", "Gate id not defined (use '/zone <zone> -list')", None)
                  )
                case (_, _, _) if buffer.isEmpty || buffer(0).equals("-help") =>
                  sessionActor ! SessionActor.SendResponse(
                    message.copy(messageType = UNK_229, contents = "@CMT_ZONE_usage")
                  )
                case _ => ()
              }

            case (CMT_WARP, _, contents) if gmCommandAllowed =>
              val buffer = contents.toLowerCase.split("\\s+")
              val (coordinates, waypoint) = (buffer.lift(0), buffer.lift(1), buffer.lift(2)) match {
                case (Some(x), Some(y), Some(z))                       => (Some(x, y, z), None)
                case (Some("to"), Some(character), None)               => (None, None) // TODO not implemented
                case (Some("near"), Some(objectName), None)            => (None, None) // TODO not implemented
                case (Some(waypoint), None, None) if waypoint.nonEmpty => (None, Some(waypoint))
                case _                                                 => (None, None)
              }
              (coordinates, waypoint) match {
                case (Some((x, y, z)), None) if List(x, y, z).forall { str =>
                      val coordinate = str.toFloatOption
                      coordinate.isDefined && coordinate.get >= 0 && coordinate.get <= 8191
                    } =>
                  sessionActor ! SessionActor.SetPosition(Vector3(x.toFloat, y.toFloat, z.toFloat))
                case (None, Some(waypoint)) if waypoint == "-list" =>
                  val zone = PointOfInterest.get(session.player.Zone.id)
                  zone match {
                    case Some(zone: PointOfInterest) =>
                      sessionActor ! SessionActor.SendResponse(
                        ChatMsg(UNK_229, wideContents=true, "", PointOfInterest.listAll(zone), None)
                      )
                    case _ => ChatMsg(UNK_229, wideContents=true, "", s"unknown player zone '${session.player.Zone.id}'", None)
                  }
                case (None, Some(waypoint)) if waypoint != "-help" =>
                  PointOfInterest.getWarpLocation(session.zone.id, waypoint) match {
                    case Some(location) => sessionActor ! SessionActor.SetPosition(location)
                    case None =>
                      sessionActor ! SessionActor.SendResponse(
                        ChatMsg(UNK_229, wideContents=true, "", s"unknown location '$waypoint'", None)
                      )
                  }
                case _ =>
                  sessionActor ! SessionActor.SendResponse(
                    message.copy(messageType = UNK_229, contents = "@CMT_WARP_usage")
                  )
              }

            case (CMT_SETBATTLERANK, _, contents) if gmCommandAllowed =>
              if (!setBattleRank(contents, session, AvatarActor.SetBep)) {
                sessionActor ! SessionActor.SendResponse(
                  message.copy(messageType = UNK_229, contents = "@CMT_SETBATTLERANK_usage")
                )
              }

            case (CMT_SETCOMMANDRANK, _, contents) if gmCommandAllowed =>
              if (!setCommandRank(contents, session)) {
                sessionActor ! SessionActor.SendResponse(
                  message.copy(messageType = UNK_229, contents = "@CMT_SETCOMMANDRANK_usage")
                )
              }

            case (CMT_ADDBATTLEEXPERIENCE, _, contents) if gmCommandAllowed =>
              contents.toIntOption match {
                case Some(bep) => avatarActor ! AvatarActor.AwardBep(bep, ExperienceType.Normal)
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
              val cosmetics = session.avatar.decoration.cosmetics.getOrElse(Set())
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
              val cosmetics = session.avatar.decoration.cosmetics.getOrElse(Set())

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

            case _ =>
              log.warn(s"Unhandled chat message $message")
          }
          Behaviors.same

        case IncomingMessage(fromSession, message, channel) =>
          message.messageType match {
            case CMT_BROADCAST | CMT_SQUAD | CMT_PLATOON | CMT_COMMAND | CMT_NOTE =>
              if (AvatarActor.onlineIfNotIgnored(session.avatar, message.recipient)) {
                sessionActor ! SessionActor.SendResponse(message)
              }
            case CMT_OPEN =>
              if (
                session.zone == fromSession.zone &&
                  Vector3.DistanceSquared(session.player.Position, fromSession.player.Position) < 625 &&
                  session.player.Faction == fromSession.player.Faction &&
                  AvatarActor.onlineIfNotIgnored(session.avatar, message.recipient)
              ) {
                sessionActor ! SessionActor.SendResponse(message)
              }
            case CMT_TELL | U_CMT_TELLFROM |
                 CMT_GMOPEN | CMT_GMBROADCAST | CMT_GMBROADCAST_NC | CMT_GMBROADCAST_TR | CMT_GMBROADCAST_VS |
                 CMT_GMBROADCASTPOPUP | CMT_GMTELL | U_CMT_GMTELLFROM | UNK_45 | UNK_71 | UNK_227 | UNK_229 =>
              sessionActor ! SessionActor.SendResponse(message)
            case CMT_VOICE =>
              if (
                (session.zone == fromSession.zone || message.contents.startsWith("SH")) && /*tactical squad voice macro*/
                  Vector3.DistanceSquared(session.player.Position, fromSession.player.Position) < 1600
              ) {
                val name = fromSession.avatar.name
                if (!session.avatar.people.ignored.exists { f => f.name.equals(name) } ||
                  {
                    val id = fromSession.avatar.id.toLong
                    val curr = System.currentTimeMillis()
                    ignoredEmoteCooldown.get(id) match {
                    case None =>
                      ignoredEmoteCooldown.put(id, curr + 15000L)
                      true
                    case Some(time) if time < curr =>
                      ignoredEmoteCooldown.put(id, curr + 15000L)
                      true
                    case _ =>
                      false
                  }}
                ) {
                  sessionActor ! SessionActor.SendResponse(message)
                }
              }
            case CMT_SILENCE =>
              val args = message.contents.split(" ")
              val (name, time) = (args.lift(0), args.lift(1)) match {
                case (Some(name), _) if name != session.player.Name =>
                  log.error("Received silence message for other player")
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
                      ChatMsg(ChatMessageType.UNK_229, wideContents=true, "", "@silence_off", None)
                    )
                    if (!silenceTimer.isCancelled) silenceTimer.cancel()
                  } else {
                    sessionActor ! SessionActor.SetSilenced(true)
                    sessionActor ! SessionActor.SendResponse(
                      ChatMsg(ChatMessageType.UNK_229, wideContents=true, "", "@silence_on", None)
                    )
                    silenceTimer = context.system.scheduler.scheduleOnce(
                      time minutes,
                      () => {
                        sessionActor ! SessionActor.SetSilenced(false)
                        sessionActor ! SessionActor.SendResponse(
                          ChatMsg(ChatMessageType.UNK_229, wideContents=true, "", "@silence_timeout", None)
                        )
                      }
                    )
                  }

                case (name, time) =>
                  log.warn(s"Bad silence args $name $time")
              }

            case _ =>
              log.warn(s"Unexpected messageType $message")

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

  /**
   * Create a medkit shortcut if there is no medkit shortcut on the hotbar.
   * Bounce the packet to the client and the client will bounce it back to the server to continue the setup,
   * or cancel / invalidate the shortcut creation.
   * @see `Array::indexWhere`
   * @see `CreateShortcutMessage`
   * @see `net.psforever.objects.avatar.Shortcut`
   * @see `net.psforever.packet.game.Shortcut.Medkit`
   * @see `SessionActor.SendResponse`
   * @param guid current player unique identifier for the target client
   * @param shortcuts list of all existing shortcuts, used for early validation
   */
  def medkitSanityTest(
                        guid: PlanetSideGUID,
                        shortcuts: Array[Option[AvatarShortcut]]
                      ): Unit = {
    if (!shortcuts.exists {
      case Some(a) => a.purpose == 0
      case None    => false
    }) {
      shortcuts.indexWhere(_.isEmpty) match {
        case -1 => ;
        case index =>
          //new shortcut
          sessionActor ! SessionActor.SendResponse(CreateShortcutMessage(
            guid,
            index + 1,
            Some(Shortcut.Medkit())
          ))
      }
    }
  }

  /**
   * Create all implant macro shortcuts for all implants whose shortcuts have been removed from the hotbar.
   * Bounce the packet to the client and the client will bounce it back to the server to continue the setup,
   * or cancel / invalidate the shortcut creation.
   * @see `CreateShortcutMessage`
   * @see `ImplantDefinition`
   * @see `net.psforever.objects.avatar.Shortcut`
   * @see `SessionActor.SendResponse`
   * @param guid current player unique identifier for the target client
   * @param haveImplants list of implants the player possesses
   * @param shortcuts list of all existing shortcuts, used for early validation
   */
  def implantSanityTest(
                         guid: PlanetSideGUID,
                         haveImplants: Iterable[ImplantDefinition],
                         shortcuts: Array[Option[AvatarShortcut]]
                       ): Unit = {
    val haveImplantShortcuts = shortcuts.collect {
      case Some(shortcut) if shortcut.purpose == 2 => shortcut.tile
    }
    var start: Int = 0
    haveImplants.filterNot { imp => haveImplantShortcuts.contains(imp.Name) }
      .foreach { implant =>
        shortcuts.indexWhere(_.isEmpty, start) match {
          case -1 => ;
          case index => ;
            //new shortcut
            start = index + 1
            sessionActor ! SessionActor.SendResponse(CreateShortcutMessage(
              guid,
              start,
              Some(implant.implantType.shortcut)
            ))
        }
      }
  }

  /**
   * Create a text chat macro shortcut if it doesn't already exist.
   * Bounce the packet to the client and the client will bounce it back to the server to continue the setup,
   * or cancel / invalidate the shortcut creation.
   * @see `Array::indexWhere`
   * @see `CreateShortcutMessage`
   * @see `net.psforever.objects.avatar.Shortcut`
   * @see `net.psforever.packet.game.Shortcut.Macro`
   * @see `SessionActor.SendResponse`
   * @param guid current player unique identifier for the target client
   * @param acronym three letters emblazoned on the shortcut icon
   * @param msg the message published to text chat
   * @param shortcuts a list of all existing shortcuts, used for early validation
   */
  def macroSanityTest(
                        guid: PlanetSideGUID,
                        acronym: String,
                        msg: String,
                        shortcuts: Array[Option[AvatarShortcut]]
                      ): Unit = {
    shortcuts.indexWhere(_.isEmpty) match {
      case -1 => ;
      case index => ;
        //new shortcut
        sessionActor ! SessionActor.SendResponse(CreateShortcutMessage(
          guid,
          index + 1,
          Some(Shortcut.Macro(acronym, msg))
        ))
    }
  }

  private def customCommandMessages(
                             message: ChatMsg,
                             session: Session,
                             chatService: ActorRef[ChatService.Command],
                             cluster: ActorRef[InterstellarClusterService.Command],
                             isGM: Boolean
                           ): Boolean = {

    var content = message.contents

    // stop processing if content is not a custom command
    if (!content.startsWith("!")) {
      return false
    }

    // remove ! character
    content = content.drop(1)

    // get command from contents
    val command = content.split(" ")(0)

    // remove command and space from contents
    content = content.drop(command.length() + 1)

    // match command and execute relevant function
    // if player is GM, process GM commands first
    if (isGM) {

      val handled = command match {

        case "whitetext" => commandGMWhiteText(message, command, content, session, chatService, cluster, isGM);
        case "loc" => commandGMLoc(message, command, content, session, chatService, cluster, isGM);
        case "list" => commandGMList(message, command, content, session, chatService, cluster, isGM);
        case "ntu" => commandGMNTU(message, command, content, session, chatService, cluster, isGM);
        case "zonerotate" => commandGMZoneRotate(message, command, content, session, chatService, cluster, isGM);

        // command is not a GM command, continue with normal commands
        case _ => false;
      }

      // stop processing if GM command was handled
      if (handled) {
        return true
      }

    }

    // process commands accessible to all players
    val handled = command match {

      case "suicide" => commandSuicide(message, command, content, session, chatService, cluster, isGM);
      case "grenade" => commandGrenade(message, command, content, session, chatService, cluster, isGM);
      case "macro" => commandMacro(message, command, content, session, chatService, cluster, isGM);
      case "progress" => commandProgress(message, command, content, session, chatService, cluster, isGM);

      // if no command was matched the command does not exist / is a GM only command but player is not GM
      case _ => false;
    }

    if (handled) {
      return true
    }

    // command was not handled
    sessionActor ! SessionActor.SendResponse(
      ChatMsg(
        CMT_GMOPEN, // CMT_GMTELL
        message.wideContents,
        "Server",
        s"Unknown command !$command",
        message.note
      )
    )

    // stop sending the failed command in chat
    true
  }

  private def commandGMWhiteText(
                          message: ChatMsg,
                          command: String,
                          content: String,
                          session: Session,
                          chatService: ActorRef[ChatService.Command],
                          cluster: ActorRef[InterstellarClusterService.Command],
                          isGM: Boolean
                        ): Boolean = {

    chatService ! ChatService.Message(
      session,
      ChatMsg(UNK_227, wideContents = true, "", content, None),
      ChatChannel.Default()
    )

    true
  }

  private def commandGMLoc(
                    message: ChatMsg,
                    command: String,
                    content: String,
                    session: Session,
                    chatService: ActorRef[ChatService.Command],
                    cluster: ActorRef[InterstellarClusterService.Command],
                    isGM: Boolean
                  ): Boolean = {

    val continent = session.zone
    val player = session.player
    val loc = s"zone=${continent.id} pos=${player.Position.x},${player.Position.y},${player.Position.z}; ori=${player.Orientation.x},${player.Orientation.y},${player.Orientation.z}"

    log.info(loc)
    sessionActor ! SessionActor.SendResponse(message.copy(contents = loc))

    true
  }

  private def commandGMList(
                     message: ChatMsg,
                     command: String,
                     content: String,
                     session: Session,
                     chatService: ActorRef[ChatService.Command],
                     cluster: ActorRef[InterstellarClusterService.Command],
                     isGM: Boolean
                   ): Boolean = {

    val zone = content.split(" ").lift(0) match {
      case Some("") =>
        Some(session.zone)
      case Some(id) =>
        Zones.zones.find(_.id == id)
    }

    zone match {

      case Some(inZone) =>
        sessionActor ! SessionActor.SendResponse(
          ChatMsg(
            CMT_GMOPEN,
            message.wideContents,
            "Server",
            "\\#8Name (Faction) [ID] at PosX PosY PosZ",
            message.note
          )
        )
        (inZone.LivePlayers ++ inZone.Corpses)
          .filter(_.CharId != session.player.CharId)
          .filter(!_.spectator)
          .sortBy(p => (p.Name, !p.isAlive))
          .foreach(player => {
            val color = if (!player.isAlive) "\\#7" else ""
            sessionActor ! SessionActor.SendResponse(
              ChatMsg(
                CMT_GMOPEN,
                message.wideContents,
                "Server",
                s"$color${player.Name} (${player.Faction}) [${player.CharId}] at ${player.Position.x.toInt} ${player.Position.y.toInt} ${player.Position.z.toInt}",
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

    true
  }

  private def commandGMNTU(
                    message: ChatMsg,
                    command: String,
                    content: String,
                    session: Session,
                    chatService: ActorRef[ChatService.Command],
                    cluster: ActorRef[InterstellarClusterService.Command],
                    isGM: Boolean
                  ): Boolean = {

    val buffer = content.toLowerCase.split("\\s+")
    val (facility, customNtuValue) = (buffer.headOption, buffer.lift(1)) match {
      case (Some(x), Some(y)) if y.toIntOption.nonEmpty => (Some(x), Some(y.toInt))
      case (Some(x), None) if x.toIntOption.nonEmpty => (None, Some(x.toInt))
      case _ => (None, None)
    }

    val silos = (facility match {

      case Some(cur) if cur.toLowerCase().startsWith("curr") =>
        val position = session.player.Position
        session.zone.Buildings.values
          .filter { building =>
            val soi2 = building.Definition.SOIRadius * building.Definition.SOIRadius
            Vector3.DistanceSquared(building.Position, position) < soi2
          }

      case Some(all) if all.toLowerCase.startsWith("all") =>
        session.zone.Buildings.values

      case Some(x) =>
        session.zone.Buildings.values.find {
          _.Name.equalsIgnoreCase(x)
        }.toList

      case _ =>
        session.zone.Buildings.values
    })
      .flatMap { building =>
        building.Amenities.filter {
          _.isInstanceOf[ResourceSilo]
        }
      }

    ChatActor.setBaseResources(sessionActor, customNtuValue, silos, debugContent = s"$facility")

    true
  }

  private def commandGMZoneRotate(
                           message: ChatMsg,
                           command: String,
                           content: String,
                           session: Session,
                           chatService: ActorRef[ChatService.Command],
                           cluster: ActorRef[InterstellarClusterService.Command],
                           isGM: Boolean
                         ): Boolean = {

    val buffer = content.toLowerCase.split("\\s+")
    cluster ! InterstellarClusterService.CavernRotation(buffer.headOption match {
      case Some("-list") | Some("-l") =>
        CavernRotationService.ReportRotationOrder(sessionActor.toClassic)
      case _ =>
        CavernRotationService.HurryNextRotation
    })

    true
  }

  private def commandSuicide(
                      message: ChatMsg,
                      command: String,
                      content: String,
                      session: Session,
                      chatService: ActorRef[ChatService.Command],
                      cluster: ActorRef[InterstellarClusterService.Command],
                      isGM: Boolean
                    ): Boolean = {

    // this is like CMT_SUICIDE but it ignores checks and forces a suicide state
    val tplayer = session.player

    tplayer.Revive
    tplayer.Actor ! Player.Die()

    true
  }

  private def commandGrenade(
                      message: ChatMsg,
                      command: String,
                      content: String,
                      session: Session,
                      chatService: ActorRef[ChatService.Command],
                      cluster: ActorRef[InterstellarClusterService.Command],
                      isGM: Boolean
                    ): Boolean = {

    WorldSession.QuickSwapToAGrenade(session.player, DrawnSlot.Pistol1.id, log)
    true
  }

  private def commandMacro(
                    message: ChatMsg,
                    command: String,
                    content: String,
                    session: Session,
                    chatService: ActorRef[ChatService.Command],
                    cluster: ActorRef[InterstellarClusterService.Command],
                    isGM: Boolean
                  ): Boolean = {

    val avatar = session.avatar
    val args = content.split(" ").filter(_ != "")

    (args.headOption, args.lift(1)) match {

      case (Some(cmd), other) =>
        cmd.toLowerCase() match {

          case "medkit" =>
            medkitSanityTest(session.player.GUID, avatar.shortcuts)
            true

          case "implants" =>
            //implant shortcut sanity test
            implantSanityTest(
              session.player.GUID,
              avatar.implants.collect {
                case Some(implant) if implant.definition.implantType != ImplantType.None => implant.definition
              },
              avatar.shortcuts
            )
            true

          case name
            if ImplantType.values.exists { a => a.shortcut.tile.equals(name) } =>
            avatar.implants.find {
              case Some(implant) => implant.definition.Name.equalsIgnoreCase(name)
              case None => false
            } match {

              case Some(Some(implant)) =>
                //specific implant shortcut sanity test
                implantSanityTest(session.player.GUID, Seq(implant.definition), avatar.shortcuts)
                true

              case _ if other.nonEmpty =>
                //add macro?
                macroSanityTest(session.player.GUID, name, args.drop(2).mkString(" "), avatar.shortcuts)
                true

              case _ =>
                false
            }

          case name
            if name.nonEmpty && other.nonEmpty =>
            //add macro
            macroSanityTest(session.player.GUID, name, args.drop(2).mkString(" "), avatar.shortcuts)
            true

          case _ =>
            false
        }

      case _ =>
        false
    }
  }

  private def commandProgress(
                            message: ChatMsg,
                            command: String,
                            content: String,
                            session: Session,
                            chatService: ActorRef[ChatService.Command],
                            cluster: ActorRef[InterstellarClusterService.Command],
                            isGM: Boolean
                          ): Boolean = {

    val ourRank = BattleRank.withExperience(session.avatar.bep).value
    if (!session.account.gm &&
      (ourRank <= Config.app.game.promotion.broadcastBattleRank ||
        ourRank > Config.app.game.promotion.resetBattleRank && ourRank < Config.app.game.promotion.maxBattleRank + 1)) {
      setBattleRank(content, session, AvatarActor.Progress)
      true
    } else {
      setBattleRank(contents = "1", session, AvatarActor.Progress)
      false
    }
  }

  private def setBattleRank(
                     contents: String,
                     session: Session,
                     msgFunc: Long => AvatarActor.Command
                   ): Boolean = {
    val buffer = contents.toLowerCase.split("\\s+")
    val (target, rank) = (buffer.lift(0), buffer.lift(1)) match {
      case (Some(target), Some(rank)) if target == session.avatar.name =>
        rank.toIntOption match {
          case Some(rank) => (None, BattleRank.withValueOpt(rank))
          case None       => (None, None)
        }
      case (Some("-h"), _) | (Some("-help"), _) =>
        (None, Some(BattleRank.BR1))
      case (Some(_), Some(_)) =>
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
      case (_, Some(rank)) if rank.value <= Config.app.game.maxBattleRank =>
        avatarActor ! msgFunc(rank.experience)
        true
      case _ =>
        false
    }
  }

  private def setCommandRank(
                      contents: String,
                      session: Session
                    ): Boolean = {
    val buffer = contents.toLowerCase.split("\\s+")
    val (target, rank) = (buffer.lift(0), buffer.lift(1)) match {
      case (Some(target), Some(rank)) if target == session.avatar.name =>
        rank.toIntOption match {
          case Some(rank) => (None, CommandRank.withValueOpt(rank))
          case None       => (None, None)
        }
      case (Some(_), Some(_)) =>
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
        true
      case _ =>
        false
    }
  }

  private def captureBaseParamFacilities(session: Session, token: Option[String]): Option[Seq[Building]] = {
    token.collect {
      case "curr" =>
        val list = captureBaseCurrSoi(session)
        if (list.nonEmpty) {
          Some(list.toSeq)
        } else {
          None
        }
      case "all" =>
        val list = session.zone.Buildings.values.filter(_.CaptureTerminal.isDefined)
        if (list.nonEmpty) {
          Some(list.toSeq)
        } else {
          None
        }
      case name =>
        val trueName = ZoneInfo
          .values
          .find(_.id.equals(session.zone.id))
          .flatMap { info =>
            info.aliases
              .facilities
              .collectFirst { case (key, internalName) if key.equalsIgnoreCase(name) => internalName }
          }
          .getOrElse(name)
        session.zone.Buildings
          .values
          .find {
            building => trueName.equalsIgnoreCase(building.Name) && building.CaptureTerminal.isDefined
          }
          .map(b => Seq(b))
    }
      .flatten
  }

  private def captureBaseCurrSoi(session: Session): Iterable[Building] = {
    val charId = session.player.CharId
    session.zone.Buildings.values.filter { building =>
      building.PlayersInSOI.exists(_.CharId == charId)
    }
  }

  private def captureBaseParamFaction(@unused session: Session, token: Option[String]): Option[PlanetSideEmpire.Value] = {
    token.collect {
      case faction =>
        faction.toLowerCase() match {
          case "tr"      => Some(PlanetSideEmpire.TR)
          case "nc"      => Some(PlanetSideEmpire.NC)
          case "vs"      => Some(PlanetSideEmpire.VS)
          case "none"    => Some(PlanetSideEmpire.NEUTRAL)
          case "bo"      => Some(PlanetSideEmpire.NEUTRAL)
          case "neutral" => Some(PlanetSideEmpire.NEUTRAL)
          case _         => None
        }
    }.flatten
  }

  private def captureBaseParamTimer(@unused session: Session, token: Option[String]): Option[Int] = {
    token.collect {
      case n if n.forall(Character.isDigit) => n.toInt
    }
  }

  private def firstParam[T](
                            session: Session,
                            buffer: Array[String],
                            func: (Session, Option[String])=>Option[T]
                          ): (Option[T], Option[String], Array[String]) = {
    val tokenOpt = buffer.headOption
    val valueOpt = func(session, tokenOpt)
    val outBuffer = if (valueOpt.nonEmpty) {
      buffer.drop(1)
    } else {
      buffer
    }
    (valueOpt, tokenOpt, outBuffer)
  }
}
