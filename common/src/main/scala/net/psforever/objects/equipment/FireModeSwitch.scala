// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

/**
  * Fire mode is a non-complex method of representing variance in `Equipment` output.<br>
  * <br>
  * All weapons and some support items have fire modes, though most only have one.
  * The number of fire modes is visually indicated by the bubbles next to the icon of the `Equipment` in a holster slot.
  * The specifics of how a fire mode affects the output is left to implementation and execution.
  * Contrast how `Tool`s deal with multiple types of ammunition.
  * @tparam Mode the type parameter representing the fire mode
  */
trait FireModeSwitch[Mode] {
  def FireModeIndex: Int

  def FireModeIndex_=(index: Int): Int

  def FireMode: Mode

  def NextFireMode: Mode
}
