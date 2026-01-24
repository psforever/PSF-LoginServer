// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import net.psforever.actors.session.support.{AvatarHandlerFunctions, ChatFunctions, GalaxyHandlerFunctions, GeneralFunctions, LocalHandlerFunctions, MountHandlerFunctions, SquadHandlerFunctions, TerminalHandlerFunctions, VehicleFunctions, VehicleHandlerFunctions, WeaponAndProjectileFunctions}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.avatar.{BattleRank, CommandRank, DeployableToolbox, FirstTimeEvents, Implant, ProgressDecoration, Shortcut => AvatarShortcut}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.serverobject.ServerObject
import net.psforever.objects.{GlobalDefinitions, Player, Session, SimpleItem, Vehicle}
import net.psforever.packet.PlanetSidePacket
import net.psforever.packet.game.{DeployableInfo, DeployableObjectsInfoMessage, DeploymentAction, ObjectCreateDetailedMessage, ObjectDeleteMessage}
import net.psforever.packet.game.objectcreate.{ObjectClass, ObjectCreateMessageParent, RibbonBars}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.chat.SpectatorChannel
import net.psforever.services.teamwork.{SquadAction, SquadServiceMessage}
import net.psforever.types.{CapacitorStateType, ChatMessageType, ExoSuitType, MeritCommendation, SquadRequestType}
//
import net.psforever.actors.session.AvatarActor
import net.psforever.actors.session.support.{ModeLogic, PlayerMode, SessionData}
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.packet.game.{ChatMsg, CreateShortcutMessage, UnuseItemMessage}

class SpectatorModeLogic(data: SessionData) extends ModeLogic {
  val avatarResponse: AvatarHandlerFunctions = AvatarHandlerLogic(data.avatarResponse)
  val chat: ChatFunctions = ChatLogic(data.chat)
  val galaxy: GalaxyHandlerFunctions = net.psforever.actors.session.normal.GalaxyHandlerLogic(data.galaxyResponseHandlers)
  val general: GeneralFunctions = GeneralLogic(data.general)
  val local: LocalHandlerFunctions = net.psforever.actors.session.normal.LocalHandlerLogic(data.localResponse)
  val mountResponse: MountHandlerFunctions = MountHandlerLogic(data.mountResponse)
  val shooting: WeaponAndProjectileFunctions = WeaponAndProjectileLogic(data.shooting)
  val squad: SquadHandlerFunctions = SquadHandlerLogic(data.squad)
  val terminals: TerminalHandlerFunctions = TerminalHandlerLogic(data.terminals)
  val vehicles: VehicleFunctions = VehicleLogic(data.vehicles)
  val vehicleResponse: VehicleHandlerFunctions = VehicleHandlerLogic(data.vehicleResponseOperations)

  override def switchTo(session: Session): Unit = {
    val player = session.player
    val continent = session.zone
    val pguid = player.GUID
    val sendResponse: PlanetSidePacket=>Unit = data.sendResponse
    //
    continent.actor ! ZoneActor.RemoveFromBlockMap(player)
    continent
      .GUID(data.terminals.usingMedicalTerminal)
      .foreach { case term: Terminal with ProximityUnit =>
        data.terminals.StopUsingProximityUnit(term)
      }
    data.general.accessedContainer
      .collect {
        case veh: Vehicle if player.VehicleSeated.isEmpty || player.VehicleSeated.get != veh.GUID =>
          sendResponse(UnuseItemMessage(pguid, veh.GUID))
          sendResponse(UnuseItemMessage(pguid, pguid))
          data.general.unaccessContainer(veh)
        case container => //just in case
          if (player.VehicleSeated.isEmpty || player.VehicleSeated.get != container.GUID) {
            // Ensure we don't close the container if the player is seated in it
            // If the container is a corpse and gets removed just as this runs it can cause a client disconnect, so we'll check the container has a GUID first.
            if (container.HasGUID) {
              sendResponse(UnuseItemMessage(pguid, container.GUID))
            }
            sendResponse(UnuseItemMessage(pguid, pguid))
            data.general.unaccessContainer(container)
          }
      }
    player.CapacitorState = CapacitorStateType.Idle
    player.Capacitor = 0f
    player.Inventory.Items
      .foreach { entry => sendResponse(ObjectDeleteMessage(entry.GUID, 0)) }
    sendResponse(ObjectDeleteMessage(player.avatar.locker.GUID, 0))
    continent.AvatarEvents ! AvatarServiceMessage(continent.id, pguid, AvatarAction.ObjectDelete(pguid))
    player.Holsters()
      .collect { case slot if slot.Equipment.nonEmpty => sendResponse(ObjectDeleteMessage(slot.Equipment.get.GUID, 0)) }
    val vehicleAndSeat = data.vehicles.GetMountableAndSeat(None, player, continent) match {
      case (Some(obj: Vehicle), Some(seatNum)) if seatNum == 0 =>
        data.vehicles.ServerVehicleOverrideStop(obj)
        obj.Actor ! ServerObject.AttributeMsg(10, 3) //faction-accessible driver seat
        obj.Seat(seatNum).foreach(_.unmount(player))
        player.VehicleSeated = None
        Some(ObjectCreateMessageParent(obj.GUID, seatNum))
      case (Some(obj), Some(seatNum)) =>
        obj.Seat(seatNum).foreach(_.unmount(player))
        player.VehicleSeated = None
        Some(ObjectCreateMessageParent(obj.GUID, seatNum))
      case _ =>
        None
    }
    data.general.dropSpecialSlotItem()
    data.general.toggleMaxSpecialState(enable = false)
    data.terminals.CancelAllProximityUnits()
    data.terminals.lastTerminalOrderFulfillment = true
    data.squadService ! SquadServiceMessage(
      player,
      continent,
      SquadAction.Membership(SquadRequestType.Leave, player.CharId, Some(player.CharId), player.Name, None)
    )
    player.avatar
      .shortcuts
      .zipWithIndex
      .collect { case (Some(_), index) => index + 1 }
      .map(CreateShortcutMessage(pguid, _, None))
      .foreach(sendResponse)
    player.avatar.implants
      .collect { case Some(implant) if implant.active =>
        data.general.avatarActor ! AvatarActor.DeactivateImplant(implant.definition.implantType)
      }
    val playerFaction = player.Faction
    continent
      .DeployableList
      .filter(_.Faction == playerFaction)
      .foreach { obj =>
        sendResponse(DeployableObjectsInfoMessage(
          DeploymentAction.Dismiss,
          DeployableInfo(obj.GUID, Deployable.Icon.apply(obj.Definition.Item), obj.Position, Service.defaultPlayerGUID)
        ))
      }
    if (player.silenced) {
      data.chat.commandIncomingSilence(session, ChatMsg(ChatMessageType.CMT_SILENCE, "player 0"))
    }
    //
    player.spectator = true
    player.allowInteraction = false
    data.chat.JoinChannel(SpectatorChannel)
    val newPlayer = SpectatorModeLogic.spectatorCharacter(player)
    newPlayer.LogActivity(player.History.headOption)
    val simpleHandHeldThing = GlobalDefinitions.flail_targeting_laser
    val handheld = new SimpleItem(simpleHandHeldThing)
    handheld.GUID = player.avatar.locker.GUID
    sendResponse(ObjectCreateDetailedMessage(
      0L,
      ObjectClass.avatar,
      pguid,
      vehicleAndSeat,
      newPlayer.Definition.Packet.DetailedConstructorData(newPlayer).get
    ))
    sendResponse(ObjectCreateDetailedMessage(
      0L,
      simpleHandHeldThing.ObjectId,
      handheld.GUID,
      Some(ObjectCreateMessageParent(pguid, 4)),
      handheld.Definition.Packet.DetailedConstructorData(handheld).get
    ))
    data.zoning.spawn.HandleSetCurrentAvatar(newPlayer)
    sendResponse(ChatMsg(ChatMessageType.CMT_TOGGLESPECTATORMODE, "on"))
    sendResponse(ChatMsg(ChatMessageType.UNK_227, "@SpectatorEnabled"))
    data.session = session.copy(player = player)
  }

  override def switchFrom(session: Session): Unit = {
    import scala.concurrent.duration._
    val player = data.player
    val zoning = data.zoning
    val pguid = player.GUID
    val sendResponse: PlanetSidePacket => Unit = data.sendResponse
    //
    player.spectator = false
    player.allowInteraction = true
    data.general.stop()
    player.avatar.shortcuts.slice(1, 4)
      .zipWithIndex
      .collect { case (None, slot) => slot + 1 } //set only actual blank slots blank
      .map(CreateShortcutMessage(pguid, _, None))
      .foreach(sendResponse)
    data.chat.LeaveChannel(SpectatorChannel)
    sendResponse(ObjectDeleteMessage(player.avatar.locker.GUID, 0)) //free up the slot
    sendResponse(ChatMsg(ChatMessageType.CMT_TOGGLESPECTATORMODE, "off"))
    sendResponse(ChatMsg(ChatMessageType.UNK_227, "@SpectatorDisabled"))
    zoning.zoneReload = true
    zoning.spawn.randomRespawn(10.milliseconds) //to sanctuary
  }
}

object SpectatorModeLogic {
  final val SpectatorImplants: Seq[Option[Implant]] = Seq(
    Some(Implant(GlobalDefinitions.targeting, initialized = true)),
    Some(Implant(GlobalDefinitions.darklight_vision, initialized = true)),
    Some(Implant(GlobalDefinitions.range_magnifier, initialized = true))
  )

  private def spectatorCharacter(player: Player): Player = {
    val avatar = player.avatar
    val newAvatar = avatar.copy(
      basic = avatar.basic,
      bep = BattleRank.BR18.experience,
      cep = CommandRank.CR5.experience,
      certifications = Set(),
      decoration = ProgressDecoration(
        ribbonBars = RibbonBars(
          MeritCommendation.BendingMovieActor,
          MeritCommendation.BendingMovieActor,
          MeritCommendation.BendingMovieActor,
          MeritCommendation.BendingMovieActor
        ),
        firstTimeEvents = FirstTimeEvents.All
      ),
      deployables = {
        val dt = new DeployableToolbox()
        dt.Initialize(Set())
        dt
      },
      implants = SpectatorImplants,
      lookingForSquad = false,
      shortcuts = {
        val allShortcuts: Array[Option[AvatarShortcut]] = Array.fill[Option[AvatarShortcut]](64)(None)
        SpectatorImplants.zipWithIndex.collect { case (Some(implant), slot) =>
          allShortcuts.update(slot + 1, Some(AvatarShortcut(2, implant.definition.Name)))
        }
        allShortcuts
      }
    )
    val newPlayer = Player(newAvatar)
    newPlayer.GUID = player.GUID
    newPlayer.ExoSuit = ExoSuitType.Infiltration
    newPlayer.Position = player.Position
    newPlayer.Orientation = player.Orientation
    newPlayer.spectator = true
    newPlayer.bops = true
    newPlayer.Spawn()
    newPlayer
  }
}

case object SpectatorMode extends PlayerMode {
  def setup(data: SessionData): ModeLogic = {
    new SpectatorModeLogic(data)
  }
}
