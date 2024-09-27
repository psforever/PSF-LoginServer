// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import net.psforever.actors.session.support.{AvatarHandlerFunctions, ChatFunctions, GalaxyHandlerFunctions, GeneralFunctions, LocalHandlerFunctions, MountHandlerFunctions, SquadHandlerFunctions, TerminalHandlerFunctions, VehicleFunctions, VehicleHandlerFunctions, WeaponAndProjectileFunctions}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.serverobject.ServerObject
import net.psforever.objects.{Session, Vehicle}
import net.psforever.packet.PlanetSidePacket
import net.psforever.packet.game.ObjectDeleteMessage
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.chat.SpectatorChannel
import net.psforever.services.teamwork.{SquadAction, SquadServiceMessage}
import net.psforever.types.{CapacitorStateType, ChatMessageType, SquadRequestType}
//
import net.psforever.actors.session.support.{ModeLogic, PlayerMode, SessionData}
import net.psforever.objects.serverobject.terminals.{ProximityUnit, Terminal}
import net.psforever.packet.game.{ChatMsg, UnuseItemMessage}

class SpectatorCSRModeLogic(data: SessionData) extends ModeLogic {
  val avatarResponse: AvatarHandlerFunctions = AvatarHandlerLogic(data.avatarResponse)
  val chat: ChatFunctions = ChatLogic(data.chat)
  val galaxy: GalaxyHandlerFunctions = GalaxyHandlerLogic(data.galaxyResponseHandlers)
  val general: GeneralFunctions = GeneralLogic(data.general)
  val local: LocalHandlerFunctions = LocalHandlerLogic(data.localResponse)
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
    continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.ObjectDelete(pguid, pguid))
    player.Holsters()
      .collect { case slot if slot.Equipment.nonEmpty => sendResponse(ObjectDeleteMessage(slot.Equipment.get.GUID, 0)) }
    data.vehicles.GetMountableAndSeat(None, player, continent) match {
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
      case _ => ()
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
    if (player.silenced) {
      data.chat.commandIncomingSilence(session, ChatMsg(ChatMessageType.CMT_SILENCE, "player 0"))
    }
    //
    player.spectator = true
    data.chat.JoinChannel(SpectatorChannel)
    sendResponse(ChatMsg(ChatMessageType.CMT_TOGGLESPECTATORMODE, "on"))
    sendResponse(ChatMsg(ChatMessageType.UNK_225, "CSR SPECTATOR MODE"))
    data.session = session.copy(player = player)
  }

  override def switchFrom(session: Session): Unit = {
    val player = data.player
    val sendResponse: PlanetSidePacket => Unit = data.sendResponse
    //
    data.continent.actor ! ZoneActor.AddToBlockMap(player, player.Position)
    data.general.stop()
    data.chat.LeaveChannel(SpectatorChannel)
    player.spectator = false
    sendResponse(ChatMsg(ChatMessageType.CMT_TOGGLESPECTATORMODE, "off"))
    sendResponse(ChatMsg(ChatMessageType.UNK_227, "@SpectatorDisabled"))
  }
}

case object SpectateAsCustomerServiceRepresentativeMode extends PlayerMode {
  def setup(data: SessionData): ModeLogic = {
    new SpectatorCSRModeLogic(data)
  }
}
