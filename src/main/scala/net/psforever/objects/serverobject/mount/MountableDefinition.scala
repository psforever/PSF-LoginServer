// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

import scala.collection.mutable

trait MountableDefinition {
  /* key - mount index, value - mount object */
  private val seats: mutable.HashMap[Int, SeatDefinition]  = mutable.HashMap[Int, SeatDefinition]()
  /* key - entry point index, value - mount index */
  private val mountPoints: mutable.HashMap[Int, Int] = mutable.HashMap()

  def Seats: mutable.HashMap[Int, SeatDefinition] = seats

  def MountPoints: mutable.HashMap[Int, Int] = mountPoints
}
