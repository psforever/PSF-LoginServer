// Copyright (c) 2017 PSForever
package net.psforever.objects.entity

import net.psforever.types.PlanetSideGUID

/**
  * Identifiable represents anything that has its own globally unique identifier (GUID).
  */
trait Identifiable {
  def GUID : PlanetSideGUID

  def GUID_=(guid : PlanetSideGUID) : PlanetSideGUID
}
