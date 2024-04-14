// Copyright (c) 2024 PSForever
package net.psforever.actors.session.support

import akka.actor.Actor.Receive
import akka.actor.ActorRef

trait ModeLogic {
  def avatarResponse: AvatarHandlerFunctions
  def galaxy: GalaxyHandlerFunctions
  def general: GeneralFunctions
  def local: LocalHandlerFunctions
  def mountResponse: MountHandlerFunctions
  def squad: SquadHandlerFunctions
  def shooting: WeaponAndProjectileFunctions
  def terminals: TerminalHandlerFunctions
  def vehicles: VehicleFunctions
  def vehicleResponse: VehicleHandlerFunctions

  def parse(sender: ActorRef): Receive
}

trait PlayerMode {
  def setup(data: SessionData): ModeLogic
}
