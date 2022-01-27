// Copyright (c) 2021 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.{GlobalDefinitions, Tool, Vehicle}
import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.vital.base.{DamageModifiers, DamageReason, DamageResolution}
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.prop.DamageWithPosition
import net.psforever.objects.vital.resolution.DamageResistanceModel
import net.psforever.types.Vector3

final case class ArmorSiphonReason(
                                    hostVehicle: Vehicle,
                                    siphon: Tool,
                                    damageModel: DamageResistanceModel
                                  ) extends DamageReason {
  assert(GlobalDefinitions.isBattleFrameArmorSiphon(siphon.Definition), "acting entity is not an armor siphon")

  def source: DamageWithPosition = siphon.Projectile

  def resolution: DamageResolution.Value = DamageResolution.Resolved

  def same(test: DamageReason): Boolean = test match {
    case asr: ArmorSiphonReason => (asr.hostVehicle eq hostVehicle) && (asr.siphon eq siphon)
    case _                      => false
  }

  def adversary: Option[SourceEntry] = None

  override def attribution: Int = hostVehicle.Definition.ObjectId
}

object ArmorSiphonModifiers {
  trait Mod extends DamageModifiers.Mod {
    def calculate(damage: Int, data: DamageInteraction, cause: DamageReason): Int = {
      cause match {
        case o: ArmorSiphonReason => calculate(damage, data, o)
        case _ => 0
      }
    }

    def calculate(damage: Int, data: DamageInteraction, cause: ArmorSiphonReason): Int
  }
}

case object ArmorSiphonMaxDistanceCutoff extends ArmorSiphonModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ArmorSiphonReason): Int = {
    if (Vector3.DistanceSquared(data.target.Position, cause.hostVehicle.Position) < cause.source.DamageRadius * cause.source.DamageRadius) {
      damage
    }
    else {
      0
    }
  }
}
