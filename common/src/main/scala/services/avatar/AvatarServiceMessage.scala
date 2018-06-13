// Copyright (c) 2017 PSForever
package services.avatar

final case class AvatarServiceMessage(forChannel : String, actionMessage : AvatarAction.Action)

object AvatarServiceMessage {
  final case class Corpse(msg : Any)
  final case class Ground(msg : Any)
}
