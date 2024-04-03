// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.environment.interaction.common

import net.psforever.objects.serverobject.environment.interaction.InteractionWith
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentTrait, PieceOfEnvironment}
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.etc.SuicideReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.{IncarnationActivity, ReconstructionActivity, Vitality}
import net.psforever.objects.zones.InteractsWithZone

import scala.annotation.unused

class WithDeath()
  extends InteractionWith {
  val channel: String = ""
  val attribute: EnvironmentTrait = EnvironmentAttribute.Death

  /**
   * Death causes target to be destroyed outright.
   * It's not even considered as environmental damage anymore.
   * @param obj target
   * @param body environment
   * @param data additional interaction information, if applicable
   */
  def doInteractingWith(
                         obj: InteractsWithZone,
                         @unused body: PieceOfEnvironment,
                         @unused data: Option[Any]
                       ): Unit = {
    if (!obj.Destroyed) {
      obj.History.findLast { entry => entry.isInstanceOf[IncarnationActivity] } match {
        case Some(entry) if System.currentTimeMillis() - entry.time > 4000L =>
          obj.Actor ! Vitality.Damage(
            DamageInteraction(
              SourceEntry(obj),
              SuicideReason(),
              obj.Position
            ).calculate()
          )
        case _ =>
      }
    }
  }
}

