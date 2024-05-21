// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.ActorContext
import net.psforever.objects.{Players, TurretDeployable}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.services.local.LocalResponse
import net.psforever.types.PlanetSideGUID

trait LocalHandlerFunctions extends CommonSessionInterfacingFunctionality {
  def ops: SessionLocalHandlers

  def handleTurretDeployableIsDismissed(obj: TurretDeployable): Unit

  def handleDeployableIsDismissed(obj: Deployable): Unit

  def handle(toChannel: String, guid: PlanetSideGUID, reply: LocalResponse.Response): Unit
}

class SessionLocalHandlers(
                            val sessionLogic: SessionData,
                            implicit val context: ActorContext
                          ) extends CommonSessionInterfacingFunctionality {


  def handleTurretDeployableIsDismissed(obj: TurretDeployable): Unit = {
    Players.buildCooldownReset(continent, player.Name, obj)
    TaskWorkflow.execute(GUIDTask.unregisterDeployableTurret(continent.GUID, obj))
  }

  def handleDeployableIsDismissed(obj: Deployable): Unit = {
    Players.buildCooldownReset(continent, player.Name, obj)
    TaskWorkflow.execute(GUIDTask.unregisterObject(continent.GUID, obj))
  }
}
