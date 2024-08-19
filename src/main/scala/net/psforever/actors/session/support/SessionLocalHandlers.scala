// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.ActorContext
import net.psforever.objects.{PlanetSideGameObject, Players, TurretDeployable}
import net.psforever.objects.ce.Deployable
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.environment.EnvironmentAttribute
import net.psforever.objects.serverobject.environment.interaction.InteractWithEnvironment
import net.psforever.objects.serverobject.environment.interaction.common.Watery
import net.psforever.objects.serverobject.interior.Sidedness
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.zones.InteractsWithZone
import net.psforever.packet.game.GenericObjectActionMessage
import net.psforever.services.local.{LocalAction, LocalResponse, LocalServiceMessage}
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
  def deactivateTelpadDeployableMessages(guid: PlanetSideGUID): Unit = {
    sendResponse(GenericObjectActionMessage(guid, code = 29))
    sendResponse(GenericObjectActionMessage(guid, code = 30))
  }

  def handleTurretDeployableIsDismissed(obj: TurretDeployable): Unit = {
    Players.buildCooldownReset(continent, player.Name, obj)
    TaskWorkflow.execute(GUIDTask.unregisterDeployableTurret(continent.GUID, obj))
  }

  def handleDeployableIsDismissed(obj: Deployable): Unit = {
    Players.buildCooldownReset(continent, player.Name, obj)
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
   * na
   * @param target evaluate this to determine if to continue with this loss
   * @return whether or not we are sufficiently submerged in water
   */
  def wadingInWater(target: PlanetSideGameObject with InteractsWithZone): Boolean = {
    target
      .interaction()
      .collectFirst {
        case env: InteractWithEnvironment =>
          env
            .Interactions
            .get(EnvironmentAttribute.Water)
            .collectFirst {
              case water: Watery => water.Depth > 0f
            }
      }
      .flatten
      .contains(true)
  }

  /**
   * na
   * @param flagGuid flag that may exist
   * @param target evaluate this to determine if to continue with this loss
   */
  def loseFlagViolently(flagGuid: Option[PlanetSideGUID], target: PlanetSideGameObject with InteractsWithZone): Unit = {
    continent
      .GUID(flagGuid)
      .collect {
        case flag: CaptureFlag if wadingInWater(target) =>
          flag.Destroyed = true
          continent.LocalEvents ! LocalServiceMessage("", LocalAction.LluLost(flag))
      }
  }
}
