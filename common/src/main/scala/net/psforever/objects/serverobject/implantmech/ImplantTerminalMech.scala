// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.implantmech

import net.psforever.objects.Player
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.mount.Mountable
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.vehicles.Seat

/**
  * A structure-owned server object that is the visible and `Mountable` component of an implant terminal.
  * For the most part, it merely implements the support data structures indicated by `Mountable`.
  * @param idef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class ImplantTerminalMech(private val idef : ImplantTerminalMechDefinition) extends PlanetSideServerObject with Mountable {
  private val seats : Map[Int, Seat] = Map( 0 -> new Seat(idef.Seats(0)) )

  def Seats : Map[Int, Seat] = seats

  def Seat(seatNum : Int) : Option[Seat] = seats.get(seatNum)

  def MountPoints : Map[Int, Int] = idef.MountPoints

  def GetSeatFromMountPoint(mount : Int) : Option[Int] = idef.MountPoints.get(mount)

  def PassengerInSeat(user : Player) : Option[Int] = {
    if(seats(0).Occupant.contains(user)) {
      Some(0)
    }
    else {
      None
    }
  }

  def Definition : ObjectDefinition = idef
}

object ImplantTerminalMech {
  def apply(idef : ImplantTerminalMechDefinition) : ImplantTerminalMech = {
    new ImplantTerminalMech(idef)
  }
}
