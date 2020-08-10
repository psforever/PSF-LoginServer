// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.aggravated

sealed class Aura(val id: Int)

object Aura {
  final case object None extends Aura(id = 0)

  final case object Plasma extends Aura(id = 1)

  final case object Comet extends Aura(id = 2)

  final case object Napalm extends Aura(id = 4)

  final case object Fire extends Aura(id = 8)
}
