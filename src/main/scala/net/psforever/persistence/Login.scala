package net.psforever.persistence

import org.joda.time.LocalDateTime

case class Login(
    id: Int,
    accountId: Int,
    loginTime: LocalDateTime = LocalDateTime.now(),
    ipAddress: String,
    canonicalHostname: String,
    hostname: String,
    port: Int
)
