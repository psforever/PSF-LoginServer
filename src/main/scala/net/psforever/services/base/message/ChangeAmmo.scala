// Copyright (c) 2026 PSForever
package net.psforever.services.base.message

import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.types.PlanetSideGUID

final case class ChangeAmmo(
                             weapon_guid: PlanetSideGUID,
                             weapon_slot: Int,
                             old_ammo_guid: PlanetSideGUID,
                             ammo_id: Int,
                             ammo_guid: PlanetSideGUID,
                             ammo_data: ConstructorData
                           ) extends SelfRespondingEvent
