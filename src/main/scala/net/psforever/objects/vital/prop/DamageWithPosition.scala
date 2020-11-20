// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.prop

/**
  * Damage that has a sense of occurring in a place where the target is not
  * but the target is still affected by the damage.
  * The distance between the target and the point of activation can have a modifying effect.
  */
trait DamageWithPosition
  extends DamageProperties {
  /** for radial damage, how much damage has been lost the furthest away from the point of origin (m) */
  private var damageAtEdge: Float                   = 1f
  /** for radial damage, the distance of the effect (m) */
  private var damageRadius: Float                   = 0f
  /** for radial damage, the distance before initial degradation of the effect (m) */
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
