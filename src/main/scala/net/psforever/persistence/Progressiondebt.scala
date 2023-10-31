// Copyright (c) 2023 PSForever
package net.psforever.persistence

import org.joda.time.LocalDateTime

case class Progressiondebt(
                            avatarId:Long,
                            experience: Long,
                            maxExperience: Long = -1,
                            enrollTime: LocalDateTime = LocalDateTime.now(),
                            clearTime: LocalDateTime = LocalDateTime.now()
                          )
