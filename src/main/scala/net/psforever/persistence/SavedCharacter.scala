// Copyright (c) 2022 PSForever
package net.psforever.persistence

import org.joda.time.LocalDateTime

case class Savedplayer(
                        avatarId: Long,
                        px: Int, //Position.x * 1000
                        py: Int, //Position.y * 1000
                        pz: Int, //Position.z * 1000
                        orientation: Int, //Orientation.z * 1000
                        zoneNum: Int,
                        health: Int,
                        armor: Int,
                        exosuitNum: Int,
                        loadout: String
                      )

case class Savedavatar(
                        avatarId: Long,
                        forgetCooldown: LocalDateTime,
                        purchaseCooldowns: String,
                        useCooldowns: String
                      )
