// Copyright (c) 2019 PSForever
package services.teamwork

import net.psforever.objects.Player
import net.psforever.objects.zones.Zone

final case class SquadServiceMessage(tplayer : Player, zone : Zone, actionMessage : Any)

object SquadServiceMessage {
  final case class RecoverSquadMembership()
}
