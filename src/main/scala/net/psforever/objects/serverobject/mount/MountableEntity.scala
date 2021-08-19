// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

import net.psforever.types.PlanetSideGUID

trait MountableEntity {
  private var bailProtection: Boolean = false

  def BailProtection: Boolean = bailProtection

  def BailProtection_=(protect: Boolean) = {
    bailProtection = protect
    BailProtection
  }

  private var mountedIn: Option[PlanetSideGUID] = None

  def MountedIn: Option[PlanetSideGUID] = mountedIn

  def MountedIn_=(cargo_vehicle_guid: PlanetSideGUID): Option[PlanetSideGUID] = MountedIn_=(Some(cargo_vehicle_guid))

  def MountedIn_=(cargo_vehicle_guid: Option[PlanetSideGUID]): Option[PlanetSideGUID] = {
    mountedIn = cargo_vehicle_guid
    MountedIn
  }
}
