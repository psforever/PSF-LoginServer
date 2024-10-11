// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import net.psforever.actors.session.support.{AvatarHandlerFunctions, ChatFunctions, GalaxyHandlerFunctions, GeneralFunctions, LocalHandlerFunctions, MountHandlerFunctions, SquadHandlerFunctions, TerminalHandlerFunctions, VehicleFunctions, VehicleHandlerFunctions, WeaponAndProjectileFunctions}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.serverobject.ServerObject
import net.psforever.objects.{Session, Vehicle}
import net.psforever.packet.PlanetSidePacket
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.chat.SpectatorChannel
import net.psforever.services.teamwork.{SquadAction, SquadServiceMessage}
import net.psforever.types.{ChatMessageType, SquadRequestType}
//
import net.psforever.actors.session.support.{ModeLogic, PlayerMode, SessionData}
import net.psforever.packet.game.ChatMsg

class SpectatorCSRModeLogic(data: SessionData) extends ModeLogic {
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
  val vehicleResponse: VehicleHandlerFunctions = net.psforever.actors.session.normal.VehicleHandlerLogic(data.vehicleResponseOperations)

  override def switchTo(session: Session): Unit = {
    val player = session.player
    val continent = session.zone
    val pguid = player.GUID
    val sendResponse: PlanetSidePacket=>Unit = data.sendResponse
    //
    continent.actor ! ZoneActor.RemoveFromBlockMap(player)
    data.vehicles.GetMountableAndSeat(None, player, continent) match {
      case (Some(obj: Vehicle), Some(seatNum)) if seatNum == 0 =>
        data.vehicles.ServerVehicleOverrideStop(obj)
        obj.Actor ! ServerObject.AttributeMsg(10, 3) //faction-accessible driver seat
        obj.Seat(seatNum).foreach(_.unmount(player))
        player.VehicleSeated = None
      case (Some(obj), Some(seatNum)) =>
        obj.Seat(seatNum).foreach(_.unmount(player))
        player.VehicleSeated = None
      case _ => ()
    }
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
    player.bops = true
    continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.ObjectDelete(pguid, pguid))
    data.chat.JoinChannel(SpectatorChannel)
    sendResponse(ChatMsg(ChatMessageType.CMT_TOGGLESPECTATORMODE, "on"))
    sendResponse(ChatMsg(ChatMessageType.UNK_225, "CSR SPECTATOR MODE ON"))
  }

  override def switchFrom(session: Session): Unit = {
    val player = data.player
    val pguid = player.GUID
    val continent = data.continent
    val avatarId = player.Definition.ObjectId
    val sendResponse: PlanetSidePacket => Unit = data.sendResponse
    //
    data.chat.LeaveChannel(SpectatorChannel)
    player.spectator = false
    player.bops = false
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      AvatarAction.LoadPlayer(pguid, avatarId, pguid, player.Definition.Packet.ConstructorData(player).get, None)
    )
    data.continent.actor ! ZoneActor.AddToBlockMap(player, player.Position)
    sendResponse(ChatMsg(ChatMessageType.CMT_TOGGLESPECTATORMODE, "off"))
    sendResponse(ChatMsg(ChatMessageType.UNK_225, "CSR SPECTATOR MODE OFF"))
  }
}

case object SpectateAsCustomerServiceRepresentativeMode extends PlayerMode {
  def setup(data: SessionData): ModeLogic = {
    new SpectatorCSRModeLogic(data)
  }
}
