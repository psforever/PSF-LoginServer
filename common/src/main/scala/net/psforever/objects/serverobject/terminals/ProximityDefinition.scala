// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.Player
import net.psforever.packet.game.ItemTransactionMessage

import scala.collection.mutable

/**
  * The definition for any `Terminal` that possesses a proximity-based effect.
  * This includes the limited proximity-based functionality of the formal medical terminals
  * and the actual proximity-based functionality of the cavern crystals.
  * Objects created by this definition being linked by their use of `ProximityTerminalUseMessage`.
  */
trait ProximityDefinition {
  private var useRadius : Float = 0f //TODO belongs on a wider range of object definitions
  private val targetValidation : mutable.ListBuffer[(Any)=>Boolean] = new mutable.ListBuffer[(Any)=>Boolean]()

  def UseRadius : Float = useRadius

  def UseRadius_=(radius : Float) : Float = {
    useRadius = radius
    UseRadius
  }

  def TargetValidation : mutable.ListBuffer[(Any)=>Boolean] = targetValidation

  def Buy(player : Player, msg : ItemTransactionMessage) : Terminal.Exchange = Terminal.NoDeal()
}
