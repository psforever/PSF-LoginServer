package net.psforever.objects

import net.psforever.objects.avatar.Avatar
import net.psforever.objects.zones.{Zone, Zoning}
import net.psforever.packet.game.DeadState

case class Session(
    id: Long = 0,
    zone: Zone = Zone.Nowhere,
    account: Account = null,
    player: Player = null,
    avatar: Avatar = null,
    zoningType: Zoning.Method.Value = Zoning.Method.None,
    deadState: DeadState.Value = DeadState.Alive,
    speed: Float = 1.0f,
    flying: Boolean = false
)
