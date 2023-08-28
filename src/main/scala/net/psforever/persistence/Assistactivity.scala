package net.psforever.persistence

import org.joda.time.LocalDateTime

case class Assistactivity(
                           index: Int,
                           killerId: Long,
                           victimId: Long,
                           weaponId: Int,
                           zoneId: Int,
                           px: Int, //Position.x * 1000
                           py: Int, //Position.y * 1000
                           pz: Int, //Position.z * 1000
                           exp: Long,
                           timestamp: LocalDateTime = LocalDateTime.now()
                         )
