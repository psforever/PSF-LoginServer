// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import net.psforever.objects.sourcing.PlayerSource

private case class WeaponStats(
                                weapon_id: Int,
                                amount: Int,
                                shots: Int,
                                time: Long
                              )

private case class ContributionStats(
                                      player: PlayerSource,
                                      weapons: Seq[WeaponStats],
                                      amount: Int,
                                      totalDamage: Int,
                                      shots: Int,
                                      time: Long
                                    )

sealed case class ContributionStatsOutput(
                                           player: PlayerSource,
                                           implements: Seq[Int],
                                           percentage: Float
                                         )
