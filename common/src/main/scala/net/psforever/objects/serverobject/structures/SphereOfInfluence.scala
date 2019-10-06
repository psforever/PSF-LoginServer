package net.psforever.objects.serverobject.structures

import net.psforever.objects.Player

trait SphereOfInfluence {
  private var soiRadius : Int = 0

  def SOIRadius : Int = soiRadius

  def SOIRadius_=(radius : Int) : Int = {
    soiRadius = radius
    SOIRadius
  }
}