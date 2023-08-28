// Copyright (c) 2022 PSForever
package net.psforever.persistence

import org.joda.time.LocalDateTime

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
