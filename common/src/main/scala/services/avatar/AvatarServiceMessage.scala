// Copyright (c) 2017 PSForever
package services.avatar

import net.psforever.objects.Player

final case class AvatarServiceMessage(forChannel : String, actionMessage : AvatarAction.Action)

object AvatarServiceMessage {
  final case class RemoveSpecificCorpse(corpse : List[Player])
}
