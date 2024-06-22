// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import net.psforever.actors.session.support.{ChatFunctions, GeneralFunctions, LocalHandlerFunctions, MountHandlerFunctions, SquadHandlerFunctions, TerminalHandlerFunctions, VehicleFunctions, VehicleHandlerFunctions, WeaponAndProjectileFunctions}
import net.psforever.actors.session.support.{ModeLogic, PlayerMode, SessionData}

class NormalModeLogic(data: SessionData) extends ModeLogic {
  val avatarResponse: AvatarHandlerLogic = AvatarHandlerLogic(data.avatarResponse)
  val chat: ChatFunctions = ChatLogic(data.chat)
  val galaxy: GalaxyHandlerLogic = GalaxyHandlerLogic(data.galaxyResponseHandlers)
  val general: GeneralFunctions = GeneralLogic(data.general)
  val local: LocalHandlerFunctions = LocalHandlerLogic(data.localResponse)
  val mountResponse: MountHandlerFunctions = MountHandlerLogic(data.mountResponse)
  val shooting: WeaponAndProjectileFunctions = WeaponAndProjectileLogic(data.shooting)
  val squad: SquadHandlerFunctions = SquadHandlerLogic(data.squad)
  val terminals: TerminalHandlerFunctions = TerminalHandlerLogic(data.terminals)
  val vehicles: VehicleFunctions = VehicleLogic(data.vehicles)
  val vehicleResponse: VehicleHandlerFunctions = VehicleHandlerLogic(data.vehicleResponseOperations)
}

case object NormalMode extends PlayerMode {
  def setup(data: SessionData): ModeLogic = {
    new NormalModeLogic(data)
  }
}
