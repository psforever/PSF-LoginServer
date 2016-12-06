// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

abstract class ConstructorData() {
  def bsize : Long = 0L
}

object ConstructorData {
  type genericPattern = Option[ConstructorData]
}