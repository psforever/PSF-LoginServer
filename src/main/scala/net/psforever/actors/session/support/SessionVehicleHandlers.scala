// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, ActorRef, typed}
import net.psforever.actors.session.AvatarActor
import net.psforever.services.vehicle.VehicleResponse
import net.psforever.types.PlanetSideGUID

trait VehicleHandlerFunctions extends CommonSessionInterfacingFunctionality {
  def ops: SessionVehicleHandlers

  def handle(toChannel: String, guid: PlanetSideGUID, reply: VehicleResponse.Response): Unit
}

class SessionVehicleHandlers(
                              val sessionLogic: SessionData,
                              val avatarActor: typed.ActorRef[AvatarActor.Command],
                              val galaxyService: ActorRef,
                              implicit val context: ActorContext
                            ) extends CommonSessionInterfacingFunctionality
