// Copyright (c) 2025 PSForever
package net.psforever.persistence

import org.joda.time.LocalDateTime

/**
  * One scheduled action the portal performs against the world server.
  *
  * Nothing in the world server reads this. It lives in this database because the portal has none of
  * its own and a schedule must be durable and shared -- see the V019 migration for the reasoning, and
  * for why a condition trigger carries `armed`.
  */
case class Portalschedule(
    id: Int,
    name: String,
    action: String,
    payload: String,
    enabled: Boolean = true,
    atTime: Option[String] = None,
    everyDays: Option[Int] = None,
    condition: Option[String] = None,
    threshold: Option[Int] = None,
    armed: Boolean = true,
    lastRun: Option[LocalDateTime] = None,
    lastResult: Option[String] = None,
    createdBy: String,
    created: LocalDateTime = LocalDateTime.now()
)
