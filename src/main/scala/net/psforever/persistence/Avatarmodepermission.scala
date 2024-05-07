// Copyright (c) 2024 PSForever
package net.psforever.persistence

case class Avatarmodepermission(
                                 avatarId: Int,
                                 canSpectate: Boolean = false,
                                 canGm: Boolean = false
                               )
