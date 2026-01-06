// Copyright (c) 2024 PSForever
package net.psforever.objects.vehicles.interaction

import net.psforever.objects.serverobject.environment.interaction.{InteractionWith, RespondsToZoneEnvironment}
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentTrait, PieceOfEnvironment, interaction}
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.environment.EnvironmentReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones.interaction.InteractsWithZone

import scala.concurrent.duration._

class WithLava()
  extends InteractionWith {
  val attribute: EnvironmentTrait = EnvironmentAttribute.Lava

  private var stopBurn: Boolean = false

  /**
    * Lava causes vehicles to take (considerable) damage until they are inevitably destroyed.
    * @param obj the target
    * @param body the environment
    * //@param data additional interaction information, if applicable
    */
  def doInteractingWith(
                         obj: InteractsWithZone,
                         body: PieceOfEnvironment,
                         data: Option[Any]
                       ): Unit = {
    if (stopBurn && data.nonEmpty) {
      stopBurn = false
    } else if (!obj.Destroyed) {
      obj.Actor ! Vitality.Damage(
        DamageInteraction(
          SourceEntry(obj),
          EnvironmentReason(body, obj),
          obj.Position
        ).calculate()
      )
      //keep doing damage
      obj.Actor ! RespondsToZoneEnvironment.Timer(attribute, delay = 250 milliseconds, obj.Actor, interaction.InteractingWithEnvironment(body, Some("burning")))
    }
  }

  override def stopInteractingWith(obj: InteractsWithZone, body: PieceOfEnvironment, parentInfo: Option[Any]): Unit = {
    stopBurn = true
  }
}
