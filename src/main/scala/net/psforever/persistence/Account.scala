package net.psforever.persistence

import org.joda.time.LocalDateTime

case class Account(
    id: Int,
    username: String,
    password: String,
    passhash: String,
    created: LocalDateTime = LocalDateTime.now(),
    lastModified: LocalDateTime = LocalDateTime.now(),
    inactive: Boolean = false,
    gm: Boolean = false,
    lastFactionId: Int = 3,
    avatarLoggedIn: Long,
    sessionId: Long,
    token: Option[String],
    tokenCreated: Option[LocalDateTime]
)
