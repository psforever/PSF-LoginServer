// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ballistics.Projectile

trait Vitality {
  this : PlanetSideGameObject =>

  private var actions : List[VitalAction] = List()

  def Actions : List[VitalAction] = actions

  def Action_=(vitalAction: VitalAction) : List[VitalAction] = {
    actions = actions :+ vitalAction
    Actions
  }
}


abstract class VitalAction(private val targets : Affects.Value,
                  private val amount : Int) {
  private val time : Long = System.nanoTime

  def Time : Long = time

  def Targets : Affects.Value = targets

  def Amount : Int = amount

  def Source : Any
}

object Affects extends Enumeration {
  val
  Undefined,
  Health,
  Armor,
  Shield
  = Value
}

class ProjectileDamage(private val projectile : Projectile,
                       private val targets : Affects.Value,
                       private val amount : Int)
  extends VitalAction(targets, amount) {
  def Source : Projectile = projectile
}
