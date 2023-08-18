// Copyright (c) 2022 PSForever
package net.psforever.persistence

import org.joda.time.LocalDateTime

case class Machinedestroyedinstance(
                                     index: Int,
                                     avatarId: Long,
                                     weaponId: Int,
                                     machineType: Int,
                                     machineFaction: Int,
                                     hackedFaction: Int,
                                     asCargo: Boolean,
                                     zoneNum: Int,
                                     px: Int, //Position.x * 1000
                                     py: Int, //Position.y * 1000
                                     pz: Int, //Position.z * 1000
                                     timestamp: LocalDateTime = LocalDateTime.now()
                                   )
