package net.psforever.objects.serverobject.aggravated

import net.psforever.objects.serverobject.aggravated.{Aura => AuraEffect}

trait AuraContainer {
  private var aura : Set[AuraEffect.Value] = Set.empty[AuraEffect.Value]

  def Aura : Set[AuraEffect.Value] = aura

  def AddEffectToAura(effect : AuraEffect.Value) : Set[AuraEffect.Value] = {
    if(effect != AuraEffect.None) {
      aura = aura + effect
    }
    Aura
  }

  def RemoveEffectFromAura(effect : AuraEffect.Value) : Set[AuraEffect.Value] = {
    aura = aura - effect
    Aura
  }
}
