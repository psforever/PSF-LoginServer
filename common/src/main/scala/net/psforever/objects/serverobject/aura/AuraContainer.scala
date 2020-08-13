package net.psforever.objects.serverobject.aura

import net.psforever.objects.serverobject.aura.{Aura => AuraEffect}

/**
  * An entity that can display specific special effects that decorate its model.
  * These animations confer information about the nature of some status that is affecting the target entity.
  */
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
