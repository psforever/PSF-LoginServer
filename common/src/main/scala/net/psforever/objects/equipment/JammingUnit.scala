// Copyright (c) 2019 PSForever
package net.psforever.objects.equipment

import net.psforever.objects.serverobject.terminals.TargetValidation

import scala.collection.mutable
import scala.concurrent.duration.Duration

trait JammingUnit {
  private val jammedEffectDuration : mutable.ListBuffer[(TargetValidation, Int)] = new mutable.ListBuffer()

  def HasJammedEffectDuration : Boolean = jammedEffectDuration.isEmpty

  def JammedEffectDuration : mutable.ListBuffer[(TargetValidation, Int)] = jammedEffectDuration
}
