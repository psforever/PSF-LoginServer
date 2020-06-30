// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

/**
  * An `Enumeration` of all the turret type objects in the game, paired with their object id as the `Value`.
  */
object Turrets extends Enumeration {
  final val manned_turret             = Value(480)
  final val portable_manned_turret    = Value(685)
  final val portable_manned_turret_nc = Value(686)
  final val portable_manned_turret_tr = Value(687)
  final val portable_manned_turret_vs = Value(688)
  final val spitfire_aa               = Value(819)
  final val spitfire_cloaked          = Value(825)
  final val spitfire_turret           = Value(826)
  final val vanu_sentry_turret        = Value(943)
}
