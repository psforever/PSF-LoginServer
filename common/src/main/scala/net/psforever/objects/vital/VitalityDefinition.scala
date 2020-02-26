// Copyright (c) 2020 PSForever
package net.psforever.objects.vital

trait VitalityDefinition {
  private var maxHealth : Int = 0
  private var defaultHealth : Option[Int] = None

  def MaxHealth : Int = maxHealth

  def MaxHealth_=(max : Int) : Int = {
    maxHealth = math.min(math.max(0, max), 65535)
    MaxHealth
  }

  def DefaultHealth : Int = defaultHealth.getOrElse(MaxHealth)

  def DefaultHealth_=(default : Int) : Int = DefaultHealth_=(Some(default))

  def DefaultHealth_=(default : Option[Int]) : Int = {
    defaultHealth = default
    DefaultHealth
  }
}
