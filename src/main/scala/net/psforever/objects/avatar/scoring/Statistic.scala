// Copyright (c) 2023 PSForever
package net.psforever.objects.avatar.scoring

/**
 * Organizes the eight fields one would find in an `AvatarServiceMessage` statistic field.
 * The `_c` fields and the `_s` fields are paired when the values populate the packet
 * where `c` stands for "campaign" and `s` stands for "session".
 * "Session" values reflect on the UI as the K in K/D
 * while "campaign" values reflect on the Character Info window, stats section.
 * @param tr_c terran republic campaign stat
 * @param tr_s terran republic session stat
 * @param nc_c new conglomerate campaign stat
 * @param nc_s new conglomerate session stat
 * @param vs_c vanu sovereignty campaign stat
 * @param vs_s vanu sovereignty session stat
 * @param ps_c generic faction campaign stat
 * @param ps_s generic faction session stat
 */
final case class Statistic(tr_c: Int, tr_s: Int, nc_c: Int, nc_s: Int, vs_c: Int, vs_s: Int, ps_c: Int, ps_s: Int)

final case class StatisticByContext(tr: Int, nc: Int, vs: Int, ps: Int) {
  def total: Int = tr + nc + vs + ps
}

object CampaignStatistics {
  def apply(stat: Statistic): StatisticByContext = {
    StatisticByContext(stat.tr_c, stat.nc_c, stat.vs_c, stat.ps_c)
  }
}

object SessionStatistics {
  def apply(stat: Statistic): StatisticByContext = {
    StatisticByContext(stat.tr_s, stat.nc_s, stat.vs_s, stat.ps_s)
  }
}
