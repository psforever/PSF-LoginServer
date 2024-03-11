// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.interior

import net.psforever.objects.serverobject.doors.Door

sealed trait SidenessComparison

sealed trait Sidedness {
  protected def value: SidenessComparison
}

object Sidedness {
  sealed trait Inside

  sealed trait Outside

  /* Comparison values */
  private case object IsInside extends SidenessComparison

  private case object IsOutside extends SidenessComparison

  private case object IsBetween extends SidenessComparison

  /* Immutable value containers */
  case object InsideOf extends Inside with Sidedness {
    protected def value: SidenessComparison = IsInside
  }

  case object OutsideOf extends Outside with Sidedness {
    protected def value: SidenessComparison = IsOutside
  }

  case object StrictlyBetweenSides extends Inside with Outside with Sidedness {
    protected def value: SidenessComparison = IsBetween
  }

  /* Mutable value container */
  class InBetweenSides(
                        private val door: Door,
                        private val strictly: Sidedness
                      ) extends Inside with Outside with Sidedness {
    protected def value: SidenessComparison = {
      if (door.isOpen) {
        IsBetween
      } else {
        strictly.value
      }
    }
  }

  object InBetweenSides {
    def apply(door: Door, strictly: Sidedness): InBetweenSides = new InBetweenSides(door, strictly)
  }

  def equals(a: Sidedness, b: Sidedness): Boolean = {
    val avalue = a.value
    val bvalue = b.value
    (avalue eq bvalue) || avalue == IsBetween || bvalue == IsBetween
  }
}
