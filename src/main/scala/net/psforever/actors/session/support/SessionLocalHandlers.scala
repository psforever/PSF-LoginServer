// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.ActorContext
import net.psforever.objects.{Players, TurretDeployable}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.interior.Sidedness
import net.psforever.packet.game.{GenericObjectActionMessage, ObjectDeleteMessage, PlanetsideAttributeMessage, TriggerEffectMessage}
import net.psforever.services.base.EventResponse
import net.psforever.types.{PlanetSideGUID, Vector3}

trait LocalHandlerFunctions extends CommonSessionInterfacingFunctionality {
  def ops: SessionLocalHandlers

  def handleTurretDeployableIsDismissed(obj: TurretDeployable): Unit

  def handleDeployableIsDismissed(obj: Deployable): Unit

  def handle(toChannel: String, guid: PlanetSideGUID, reply: EventResponse): Unit
}

class SessionLocalHandlers(
                            val sessionLogic: SessionData,
                            implicit val context: ActorContext
                          ) extends CommonSessionInterfacingFunctionality {
  def deactivateTelpadDeployableMessages(guid: PlanetSideGUID): Unit = {
    sendResponse(GenericObjectActionMessage(guid, code = 29))
    sendResponse(GenericObjectActionMessage(guid, code = 30))
  }

  def handleTurretDeployableIsDismissed(obj: TurretDeployable): Unit = {
    Players.buildCooldownReset(continent, player.Name, obj.GUID)
    TaskWorkflow.execute(GUIDTask.unregisterDeployableTurret(continent.GUID, obj))
  }

  def handleDeployableIsDismissed(obj: Deployable): Unit = {
    Players.buildCooldownReset(continent, player.Name, obj.GUID)
    TaskWorkflow.execute(GUIDTask.unregisterObject(continent.GUID, obj))
  }

  def doorLoadRange(): Float = {
    if (Sidedness.equals(player.WhichSide, Sidedness.InsideOf))
      100f
    else if (sessionLogic.general.canSeeReallyFar)
      800f
    else
      400f
  }



  /**
   * Common behavior for deconstructing deployables in the game environment.
   * @param obj the deployable
   * @param guid the globally unique identifier for the deployable
   * @param pos the previous position of the deployable
   * @param orient the previous orientation of the deployable
   * @param deletionType the value passed to `ObjectDeleteMessage` concerning the deconstruction animation
   */
  def DeconstructDeployable(
                             obj: Deployable,
                             guid: PlanetSideGUID,
                             pos: Vector3,
                             orient: Vector3,
                             deletionType: Int
                           ): Unit = {
    sendResponse(TriggerEffectMessage("spawn_object_failed_effect", pos, orient))
    sendResponse(PlanetsideAttributeMessage(guid, 29, 1)) //make deployable vanish
    sendResponse(ObjectDeleteMessage(guid, deletionType))
  }
}
