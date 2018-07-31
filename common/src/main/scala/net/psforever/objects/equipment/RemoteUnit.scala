// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

import net.psforever.packet.game.PlanetSideGUID

trait RemoteUnit {
  private var companion : Option[PlanetSideGUID] = None

  def Companion : Option[PlanetSideGUID] = companion

  def Companion_=(guid : PlanetSideGUID) : Option[PlanetSideGUID] = {
    if(companion.isEmpty) {
      companion = Some(guid)
    }
    Companion
  }
}
