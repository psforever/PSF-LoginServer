// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import net.psforever.objects.sourcing.PlayerSource

sealed trait ItemUseStats {
  def equipment_id: Int
  def shots: Int
  def time: Long
  def contributions: Float
}

private case class WeaponStats(
                                equipment_id: Int,
                                amount: Int,
                                shots: Int,
                                time: Long,
                                contributions: Float
                              ) extends ItemUseStats

private case class EquipmentStats(
                                   equipment_id: Int,
                                   shots: Int,
                                   time: Long,
                                   contributions: Float
                                 ) extends ItemUseStats

private[exp] case class ContributionStats(
                                           player: PlayerSource,
                                           weapons: Seq[WeaponStats],
                                           amount: Int,
                                           total: Int,
                                           shots: Int,
                                           time: Long
                                         )

sealed case class ContributionStatsOutput(
                                           player: PlayerSource,
                                           implements: Seq[Int],
                                           percentage: Float
                                         )
