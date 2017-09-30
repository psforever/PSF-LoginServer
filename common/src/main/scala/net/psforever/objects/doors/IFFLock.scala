// Copyright (c) 2017 PSForever
package net.psforever.objects.doors

import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject}
import net.psforever.types.PlanetSideEmpire

class IFFLock extends PlanetSideGameObject {
  private var hackedBy : Option[PlanetSideEmpire.Value] = None

  def Hacker : Option[PlanetSideEmpire.Value] = hackedBy

  def Hacker_=(hacker : PlanetSideEmpire.Value) : Option[PlanetSideEmpire.Value] = {
    Hacker = Some(hacker)
  }

  def Hacker_=(hacker : Option[PlanetSideEmpire.Value]) : Option[PlanetSideEmpire.Value] = {
    hackedBy = hacker
    Hacker
  }

  def Definition : IFFLockDefinition = GlobalDefinitions.external_lock
}

object IFFLock {
  def apply() : IFFLock = {
    new IFFLock
  }
}
