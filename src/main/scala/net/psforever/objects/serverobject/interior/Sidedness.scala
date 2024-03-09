// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.interior

sealed trait Sidedness

sealed trait Inside

sealed trait Outside

sealed trait InBetween extends Inside with Outside

object Sidedness {
  case object InsideOf extends Inside with Sidedness

  case object OutsideOf extends Outside with Sidedness

  case object InBetweenSides extends InBetween with Sidedness

  def equals(a: Sidedness, b: Sidedness): Boolean = {
    (a eq b) || a == Sidedness.InBetweenSides || b == Sidedness.InBetweenSides
  }
}
