// Copyright (c) 2023 PSForever
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

case class Buildingcapture(
                            index: Int,
                            avatarId: Long,
                            zoneId: Int,
                            buildingId: Int,
                            exp: Long,
                            expType: String,
                            timestamp: LocalDateTime = LocalDateTime.now()
                          )

case class Kdasession (
                        avatarId: Long,
                        sessionId: Int,
                        kills: Int,
                        deaths: Int,
                        assists: Int,
                        revives: Int
                      )

case class Killactivity(
                         index: Int,
                         killerId: Long,
                         victimId: Long,
                         victimExosuit: Int,
                         victimMounted: Int, //object type id * 10 + seat type
                         weaponId: Int,
                         zoneId: Int,
                         px: Int, //Position.x * 1000
                         py: Int, //Position.y * 1000
                         pz: Int, //Position.z * 1000
                         exp: Long,
                         timestamp: LocalDateTime = LocalDateTime.now()
                       )

case class Machinedestroyed(
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

case class Ntuactivity (
                         avatarId: Long,
                         zoneId: Int,
                         buildingId: Int,
                         exp: Long
                       )

case class Supportactivity(
                            index: Int,
                            userId: Long,
                            targetId: Long,
                            targetExosuit: Int,
                            interactionType: Int,
                            implementType: Int,
                            intermediateType: Int,
                            exp: Long,
                            timestamp: LocalDateTime = LocalDateTime.now()
                          )

case class Weaponstatsession(
                              avatarId: Long,
                              weaponId: Int,
                              shotsFired: Int,
                              shotsLanded: Int,
                              kills: Int,
                              assists: Int,
                              sessionId: Long
                            )
