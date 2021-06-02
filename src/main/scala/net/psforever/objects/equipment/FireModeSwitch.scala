// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

/**
  * Fire mode is a non-complex method of representing variance in `Equipment` output.<br>
  * <br>
  * All weapons and some support items have fire modes, though most only have one.
  * The number of fire modes is visually indicated by the bubbles next to the icon of the `Equipment` in a holster slot.
  * The specifics of how a fire mode affects the output is left to implementation and execution.
  * Contrast how `Tool`s deal with multiple types of ammunition.<br>
  * <br>
  * While most `Tools` - weapons and such - are known to have fire modes,
  * `ConstructionItem` equipment that produce deployable entities in the game world also support fire modes.
  * The mechanism is different, however, even while the user interactions work in a similar way.
  * For most weapons, the fire mode is just a modification of how the projectiles behave or the weapon behaves.
  * For example, the bounciness of the grenades or the number of shots fired by the Jackhammer.
  * One has to change tool ammo types to actual swap out different ammunitions such as, most commonly,
  * grey normal ammo for gold armor-piercing ammo.
  * For deployable-constructing entities, fire mode switches between the categories of deployables that can be built
  * and "changing ammunition" actually changes the subtype of deployable within that deployable category.
  * For example, fire modes go from "Boomers" to "Mines"
  * while ammo types for "Mines" goes from "HE mines" to "Disruptor Mines".
  * @tparam Mode the type parameter representing the fire mode
  */
trait FireModeSwitch[Mode] {
  def FireModeIndex: Int

  def FireModeIndex_=(index: Int): Int

  def FireMode: Mode

  def NextFireMode: Mode
}
