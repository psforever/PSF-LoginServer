package net.psforever.persistence

import org.joda.time.LocalDateTime

case class Avatar(
    id: Int,
    name: String,
    accountId: Int,
    factionId: Int,
    genderId: Int,
    headId: Int,
    voiceId: Int,
    bep: Long = 0,
    cep: Long = 0,
    cosmetics: Option[Int] = None,
    created: LocalDateTime = LocalDateTime.now(),
    lastLogin: LocalDateTime = LocalDateTime.now(),
    lastModified: LocalDateTime = LocalDateTime.now(),
    deleted: Boolean = false
)
