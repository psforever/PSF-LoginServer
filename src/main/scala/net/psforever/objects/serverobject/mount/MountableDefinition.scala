// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

import net.psforever.types.Vector3

import scala.collection.mutable

final case class MountInfo(seatIndex: Int, positionOffset: Vector3)

object MountInfo {
  def apply(seatIndex: Int): MountInfo = MountInfo(seatIndex, Vector3.Zero)
}

trait MountableDefinition {
  /* key - mount index, value - mount object */
  private val seats: mutable.HashMap[Int, SeatDefinition]  = mutable.HashMap[Int, SeatDefinition]()
  /* key - entry point index, value - mount index */
  private val mountPoints: mutable.HashMap[Int, MountInfo] = mutable.HashMap()

  def Seats: mutable.HashMap[Int, SeatDefinition] = seats

  def MountPoints: mutable.HashMap[Int, MountInfo] = mountPoints
}
