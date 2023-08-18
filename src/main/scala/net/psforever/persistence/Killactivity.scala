// Copyright (c) 2022 PSForever
package net.psforever.persistence

case class Killactivity(
                         index: Int,
                         killerId: Long,
                         victimId: Long,
                         victimExosuit: Int,
                         victimMounted: Int, //object type id * 10 + seat type
                         weaponId: Int,
                         zoneId: Int,
                         px: Int, //Position.x * 1000
                         py: Int, //Position.y * 1000
                         pz: Int, //Position.z * 1000
                         exp: Long
                       )
