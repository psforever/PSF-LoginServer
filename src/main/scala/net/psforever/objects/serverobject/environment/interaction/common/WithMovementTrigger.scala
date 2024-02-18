// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.environment.interaction.common

import net.psforever.objects.serverobject.environment._
import net.psforever.objects.serverobject.environment.interaction.InteractionWith
import net.psforever.objects.zones.InteractsWithZone

import scala.annotation.unused

class WithMovementTrigger()
  extends InteractionWith {
  val attribute: EnvironmentTrait = EnvironmentAttribute.MovementFieldTrigger
  /**
   * The target will be affected by this action.
   * @param obj target
   * @param body environment
   * //@param data additional interaction information, if applicable
   */
  def doInteractingWith(
                         obj: InteractsWithZone,
                         body: PieceOfEnvironment,
                         @unused data: Option[Any]
                       ): Unit = {
    body.asInstanceOf[GeneralMovementField].triggerAction(obj)
  }
}
