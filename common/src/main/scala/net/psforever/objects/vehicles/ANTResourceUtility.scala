// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.Vehicle

/**
  * A `Utility` designed to simulate the NTU-distributive functions of an ANT.
  * @param objectId the object id that is associated with this sort of `Utility`
  * @param vehicle the `Vehicle` to which this `Utility` is attached
  */
class ANTResourceUtility(objectId : Int, vehicle : Vehicle) extends Utility(objectId, vehicle) {
  private var currentNTU : Int = 0

  def NTU : Int = currentNTU

  def NTU_=(ntu : Int) : Int = {
    currentNTU = ntu
    currentNTU = math.max(math.min(currentNTU, MaxNTU), 0)
    NTU
  }

  def MaxNTU : Int = ANTResourceUtility.MaxNTU
}

object ANTResourceUtility {
  private val MaxNTU : Int = 300 //TODO what should this value be?

  def apply(objectId : Int, vehicle : Vehicle) : ANTResourceUtility = {
    new ANTResourceUtility(objectId, vehicle)
  }
}