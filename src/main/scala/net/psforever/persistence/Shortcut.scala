// Copyright (c) 2022 PSForever
package net.psforever.persistence

case class Shortcut(
                     avatarId: Long,
                     slot: Int,
                     purpose: Int,
                     tile: String,
                     effect1: Option[String] = None,
                     effect2: Option[String] = None
                   )
