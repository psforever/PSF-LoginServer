// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

object ProjectileResolution extends Enumeration {
  type Type = Value

  val
  Unresolved,
  Resolved,
  MissedShot,
  Hit,
  Splash,
  Lash
  = Value
}
