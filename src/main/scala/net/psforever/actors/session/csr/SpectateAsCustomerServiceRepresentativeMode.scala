// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import net.psforever.actors.session.support.{AvatarHandlerFunctions, ChatFunctions, GalaxyHandlerFunctions, GeneralFunctions, LocalHandlerFunctions, MountHandlerFunctions, SquadHandlerFunctions, TerminalHandlerFunctions, VehicleFunctions, VehicleHandlerFunctions, WeaponAndProjectileFunctions}
import net.psforever.objects.serverobject.ServerObject
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.{Player, Session, Vehicle}
import net.psforever.objects.zones.Zone
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
    val sendResponse: PlanetSidePacket => Unit = data.sendResponse
    //
    data.squadService ! SquadServiceMessage(
      player,
      continent,
      SquadAction.Membership(SquadRequestType.Leave, player.CharId, Some(player.CharId), player.Name, None)
    )
    if (requireDismount(data, continent, player)) {
      CustomerServiceRepresentativeMode.renderPlayer(data, continent, player)
    }
    //
    player.spectator = true
    data.chat.JoinChannel(SpectatorChannel)
    continent.AvatarEvents ! AvatarServiceMessage(continent.id, pguid, AvatarAction.ObjectDelete(pguid))
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
    player.spectator = false
    data.chat.LeaveChannel(SpectatorChannel)
    continent.AvatarEvents ! AvatarServiceMessage(
      continent.id,
      pguid,
      AvatarAction.LoadPlayer(avatarId, pguid, player.Definition.Packet.ConstructorData(player).get, None)
    )
    sendResponse(ChatMsg(ChatMessageType.CMT_TOGGLESPECTATORMODE, "off"))
  }

  private def requireDismount(data: SessionData, zone: Zone, player: Player): Boolean = {
    data.vehicles.GetMountableAndSeat(None, player, zone) match {
      case (Some(obj: Vehicle), Some(seatNum)) if seatNum == 0 =>
        data.vehicles.ServerVehicleOverrideStop(obj)
        obj.Actor ! ServerObject.AttributeMsg(10, 3) //faction-accessible driver seat
        obj.Actor ! Mountable.TryDismount(player, seatNum)
        player.VehicleSeated = None
        true
      case (Some(obj), Some(seatNum)) =>
        obj.Actor ! Mountable.TryDismount(player, seatNum)
        player.VehicleSeated = None
        true
      case _ =>
        player.VehicleSeated = None
        false
    }
  }
}

case object SpectateAsCustomerServiceRepresentativeMode extends PlayerMode {
  def setup(data: SessionData): ModeLogic = {
    new SpectatorCSRModeLogic(data)
  }
}
