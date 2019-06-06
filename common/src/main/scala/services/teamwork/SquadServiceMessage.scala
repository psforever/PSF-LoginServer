// Copyright (c) 2019 PSForever
package services.teamwork

import net.psforever.objects.Player
import net.psforever.packet.game.{PlanetSideGUID, SquadAction}

final case class SquadServiceMessage(forChannel : String, actionMessage : Any)

object SquadServiceMessage {
  final case class SquadDefinitionAction(player : Player, zone_ordinal_number : Int, guid : PlanetSideGUID, line : Int, action : SquadAction)

  final case class RecoverSquadMembership()
}
