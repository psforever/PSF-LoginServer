// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

/**
  * An `Enumeration` of the available turret upgrade states.
  */
object TurretUpgrade extends Enumeration {
  val None, //default, always
  AVCombo,  //phalanx_avcombo
  FlakCombo //phalanx_flakcombo
  = Value
}
