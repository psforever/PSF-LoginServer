package net.psforever.objects.serverobject.structures

import net.psforever.objects.{NtuContainerDefinition, SpawnPointDefinition}
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.types.{CaptureBenefit, CavernBenefit, LatticeBenefit}

class BuildingDefinition(objectId: Int)
  extends ObjectDefinition(objectId)
    with NtuContainerDefinition
    with SphereOfInfluence {
  Name = "building"
  MaxNtuCapacitor = Int.MaxValue
  private var latBenefit: LatticeBenefit = LatticeBenefit.None
  private var cavBenefit: CavernBenefit = CavernBenefit.None

  def LatticeLinkBenefit: LatticeBenefit = latBenefit

  def LatticeLinkBenefit_=(bfit: LatticeBenefit): CaptureBenefit = {
    latBenefit = bfit
    LatticeLinkBenefit
  }

  def CavernLinkBenefit: CavernBenefit = cavBenefit

  def CavernLinkBenefit_=(cfit: CavernBenefit): CavernBenefit = {
    cavBenefit = cfit
    CavernLinkBenefit
  }
}

class WarpGateDefinition(objectId: Int) extends BuildingDefinition(objectId) with SpawnPointDefinition
