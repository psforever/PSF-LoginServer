// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.prop

trait DamageWithPosition
  extends DamageProperties {
  /** for radial damage, how much damage has been lost the further away from the point of origin (m) */
  private var damageAtEdge: Float                   = 1f
  /** for radial damage, the distance of the (explosion) effect (m) */
  private var damageRadius: Float                   = 0f
  /** for radial damage, the distance before degradation of the (explosion) effect (m) */
  private var damageRadiusMin: Float                = 1f

  def DamageAtEdge: Float = damageAtEdge

  def DamageAtEdge_=(atEdge: Float): Float = {
    damageAtEdge = atEdge
    DamageAtEdge
  }

  def DamageRadius: Float = damageRadius

  def DamageRadius_=(radius: Float): Float = {
    damageRadius = radius
    DamageRadius
  }

  def DamageRadiusMin: Float = damageRadiusMin

  def DamageRadiusMin_=(radius: Float): Float = {
    damageRadiusMin = radius
    DamageRadiusMin
  }
}
