// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import net.psforever.actors.session.support.{ChatFunctions, GalaxyHandlerFunctions, GeneralFunctions, LocalHandlerFunctions, ModeLogic, MountHandlerFunctions, PlayerMode, SessionData, SquadHandlerFunctions, TerminalHandlerFunctions, VehicleFunctions, VehicleHandlerFunctions, WeaponAndProjectileFunctions}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.Session
import net.psforever.packet.PlanetSidePacket
import net.psforever.packet.game.ChatMsg
import net.psforever.types.ChatMessageType

class CustomerServiceRepresentativeMode(data: SessionData) extends ModeLogic {
  val avatarResponse: AvatarHandlerLogic = AvatarHandlerLogic(data.avatarResponse)
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
    val sendResponse: PlanetSidePacket=>Unit = data.sendResponse
    //
    continent.actor ! ZoneActor.RemoveFromBlockMap(player)
    sendResponse(ChatMsg(ChatMessageType.UNK_225, "CSR MODE ON"))
  }

  override def switchFrom(session: Session): Unit = {
    val player = data.player
    val sendResponse: PlanetSidePacket => Unit = data.sendResponse
    //
    data.continent.actor ! ZoneActor.AddToBlockMap(player, player.Position)
    sendResponse(ChatMsg(ChatMessageType.UNK_225, "CSR MODE OFF"))
  }
}

case object CustomerServiceRepresentativeMode extends PlayerMode {
  def setup(data: SessionData): ModeLogic = {
    new CustomerServiceRepresentativeMode(data)
  }
}
