// Copyright (c) 2019 PSForever
package services.teamwork

import net.psforever.objects.Player
import net.psforever.packet.game.SquadAction

final case class SquadServiceMessage(forChannel : String, actionMessage : Any)

object SquadServiceMessage {
  final case class SquadDefinitionAction(player : Player, zone_ordinal_number : Int, u1 : Int, u2 : Int, action : SquadAction)

  final case class RecoverSquadMembership()
}
