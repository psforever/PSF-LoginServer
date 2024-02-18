// Copyright (c) 2024 PSForever
package net.psforever.objects.avatar.interaction

import net.psforever.objects.serverobject.aura.{Aura, AuraEffectBehavior}
import net.psforever.objects.serverobject.environment.interaction.{InteractionWith, RespondsToZoneEnvironment}
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentTrait, PieceOfEnvironment, interaction}
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.environment.EnvironmentReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones.InteractsWithZone

import scala.annotation.unused
import scala.concurrent.duration._

class WithLava()
  extends InteractionWith {
  val attribute: EnvironmentTrait = EnvironmentAttribute.Lava

  /**
   * Lava causes players to take (considerable) damage until they inevitably die.
   * @param obj the target
   * @param body the environment
   */
  def doInteractingWith(
                         obj: InteractsWithZone,
                         body: PieceOfEnvironment,
                         @unused data: Option[Any]
                       ): Unit = {
    if (!obj.Destroyed) {
      obj.Actor ! Vitality.Damage(
        DamageInteraction(
          SourceEntry(obj),
          EnvironmentReason(body, obj),
          obj.Position
        ).calculate()
      )
      obj.Actor ! AuraEffectBehavior.StartEffect(Aura.Fire, duration = 1250L) //burn
      //keep doing damage
      obj.Actor ! RespondsToZoneEnvironment.Timer(attribute, delay = 250 milliseconds, obj.Actor, interaction.InteractingWithEnvironment(body, None))
    }
  }
}
