// Copyright (c) 2019 PSForever
package net.psforever.objects.equipment

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ballistics.ResolvedProjectile
import net.psforever.objects.serverobject.terminals.TargetValidation

import scala.collection.mutable

trait JammableUnit {
  private var jammed : Boolean = false

  def Jammed : Boolean = jammed

  def Jammed_=(state : Boolean) : Boolean = {
    jammed = state
    Jammed
  }
}

object JammableUnit {
  final case class Jammer()

  final case class Jammered(cause : ResolvedProjectile)

  final case class ClearJammeredSound()

  final case class ClearJammeredStatus()
}

trait JammingUnit {
  private val jammedEffectDuration : mutable.ListBuffer[(TargetValidation, Int)] = new mutable.ListBuffer()

  def HasJammedEffectDuration : Boolean = jammedEffectDuration.isEmpty

  def JammedEffectDuration : mutable.ListBuffer[(TargetValidation, Int)] = jammedEffectDuration
}

object JammingUnit {
  def FindJammerDuration(jammer : JammingUnit, target : PlanetSideGameObject) : Option[Int] = {
    jammer.JammedEffectDuration
      .collect { case (TargetValidation(_, test), duration) if test(target) => duration }
      .toList
      .sortWith(_ > _)
      .headOption
  }

  def FindJammerDuration(jammer : JammingUnit, targets : Seq[PlanetSideGameObject]) : Seq[Option[Int]] = {
    targets.map { target => FindJammerDuration(jammer, target) }
  }
}
