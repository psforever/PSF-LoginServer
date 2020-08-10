// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.aggravated

import net.psforever.objects.serverobject.aggravated.{Aura => AuraEffect}

trait AuraContainer {
  private var aura : Set[AuraEffect] = Set.empty[AuraEffect]

  def Aura : Set[AuraEffect] = aura

  def AddEffectToAura(effect : AuraEffect) : Set[AuraEffect] = {
    if(effect != AuraEffect.None) {
      aura = aura + effect
    }
    Aura
  }

  def RemoveEffectFromAura(effect : AuraEffect) : Set[AuraEffect] = {
    aura = aura - effect
    Aura
  }
}
