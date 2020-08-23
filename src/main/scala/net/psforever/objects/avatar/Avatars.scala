// Copyright (c) 2017 PSForever
package net.psforever.objects.avatar

/**
  * An `Enumeration` of all the avatar types in the game, paired with their object id as the `Value`.
  * #121 is the most important.
  */
object Avatars extends Enumeration {
  final val avatar                          = Value(121)
  final val avatar_bot                      = Value(122)
  final val avatar_bot_agile                = Value(123)
  final val avatar_bot_agile_no_weapon      = Value(124)
  final val avatar_bot_max                  = Value(125)
  final val avatar_bot_max_no_weapon        = Value(126)
  final val avatar_bot_reinforced           = Value(127)
  final val avatar_bot_reinforced_no_weapon = Value(128)
  final val avatar_bot_standard             = Value(129)
  final val avatar_bot_standard_no_weapon   = Value(130)
}
