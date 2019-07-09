// Copyright (c) 2019 PSForever
package services.teamwork

import net.psforever.objects.Player

final case class SquadServiceMessage(tplayer : Player, actionMessage : Any)

object SquadServiceMessage {
  final case class RecoverSquadMembership()
}
