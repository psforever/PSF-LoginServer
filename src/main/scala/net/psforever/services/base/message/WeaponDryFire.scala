// Copyright (c) 2026 PSForever
package net.psforever.services.base.message

import net.psforever.types.PlanetSideGUID

final case class WeaponDryFire(weapon_guid: PlanetSideGUID) extends SelfRespondingEvent
