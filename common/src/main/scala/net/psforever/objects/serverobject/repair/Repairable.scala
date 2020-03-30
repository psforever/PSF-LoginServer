//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import akka.actor.Actor.Receive
import net.psforever.objects.equipment.Ammo
import net.psforever.objects.{GlobalDefinitions, Player, Tool}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.vital.Vitality

/**
  * The base "control" `Actor` mixin for repair-handling code
  * related to the nano dispenser tool loaded with an armor repair canister.
  * Unlike the `Damageable` mixin,
  * which should be extended to interact with all aspects of a target that impede access to its health points,
  * shield, armor, etc., `Repairable` only affects `Vitality.Health`.
  */
trait Repairable {
  /**
    * Contextual access to the object being the target of this damage.
    * Needs declaration in lowest implementing code.
    * @return the entity controlled by this actor
    */
  def RepairableObject : Repairable.Target

  /**
    * The official mixin hook; `orElse` onto the "control" `Actor` `receive`;
    * catch the expected repair message and apply initial checks to the item
    * @see `Ammo`
    * @see `CanBeRepairedByNanoDispenser`
    * @see `CommonMessages.Use`
    * @see `GlobalDefinitions`
    * @see `Tool.AmmoType`
    */
  final val canBeRepairedByNanoDispenser : Receive = {
    case CommonMessages.Use(player, Some(item : Tool))
      if item.Definition == GlobalDefinitions.nano_dispenser && item.AmmoType == Ammo.armor_canister =>
      CanBeRepairedByNanoDispenser(player, item)
  }

  /**
    * Implementation of the mixin hook will be provided by a child class.
    * Override this method only when directly implementing.
    * @see `canBeRepairedByNanoDispenser`
    */
  def CanBeRepairedByNanoDispenser(player : Player, item : Tool) : Unit

  /**
    * The amount of repair that any specific tool provides.
    * @see `Repairable.Quality`
    * @param item the tool in question
    * @return an amount to add to the repair attempt progress
    */
  def RepairValue(item : Tool) : Int = 0

  /**
    * The entity is no longer destroyed.
    * @param obj the entity
    */
  def Restoration(obj : Repairable.Target) : Unit = {
    Repairable.Restoration(obj)
  }
}

object Repairable {
  /* the type of all entities governed by this mixin; see Damageable.Target */
  final type Target = PlanetSideServerObject with Vitality
  /* the basic repair value; originally found on the `armor_canister` object definition */
  final val Quality : Int = 12

  /**
    * The entity is no longer destroyed.
    * @param target the entity
    */
  def Restoration(target : Repairable.Target) : Unit = {
    target.Destroyed = false
  }
}
