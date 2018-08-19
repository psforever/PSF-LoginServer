// Copyright (c) 2017 PSForever
package net.psforever.objects.ce

import net.psforever.objects.definition.DeployableDefinition

trait LargeDeployable extends Deployable {
  def Health : Int
  def Health_=(toHealth : Int) : Int
}

trait LargeDeployableDefinition extends DeployableDefinition {
  private var maxHealth : Int = 1

  def MaxHealth : Int = maxHealth

  def MaxHealth_=(toHealth : Int) : Int = {
    maxHealth = toHealth
    MaxHealth
  }
}
