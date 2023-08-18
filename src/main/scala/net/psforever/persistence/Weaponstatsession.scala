// Copyright (c) 2022 PSForever
package net.psforever.persistence

case class Weaponstatsession(
                              avatarId: Long,
                              weaponId: Int,
                              shotsFired: Int,
                              shotsLanded: Int,
                              kills: Int,
                              assists: Int,
                              sessionId: Long
                            )
