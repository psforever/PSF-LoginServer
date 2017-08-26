// Copyright (c) 2017 PSForever
package net.psforever.objects.entity

import net.psforever.types.Vector3

import scala.collection.mutable

class MobileWorldEntity extends WorldEntity {
  private var coords : mutable.Stack[TimeEntry] = mutable.Stack(TimeEntry.invalid) //history of last #n positional updates
  private var orient : mutable.Stack[TimeEntry] = mutable.Stack(TimeEntry.invalid) //history of last #n orientation updates
  private var vel : Option[Vector3] = None

  def Position : Vector3 = coords.head.entry

  def Position_=(vec : Vector3) : Vector3 = {
    coords = MobileWorldEntity.pushNewStack(coords, vec, SimpleWorldEntity.validatePositionEntry)
    Position
  }

  def AllPositions : scala.collection.immutable.List[TimeEntry] = coords.toList

  def Orientation : Vector3 = orient.head.entry

  def Orientation_=(vec : Vector3) : Vector3 = {
    orient = MobileWorldEntity.pushNewStack(orient, vec, SimpleWorldEntity.validateOrientationEntry)
    Orientation
  }

  def AllOrientations : scala.collection.immutable.List[TimeEntry] = orient.toList

  def Velocity : Option[Vector3] = vel

  def Velocity_=(vec : Option[Vector3]) : Option[Vector3] = {
    vel = vec
    vel
  }

  override def toString : String = WorldEntity.toString(this)
}

object MobileWorldEntity {
  def pushNewStack(lst : mutable.Stack[TimeEntry], newEntry : Vector3, validate : (Vector3) => Vector3) : mutable.Stack[TimeEntry] = {
    lst.slice(0, 199).push(TimeEntry(validate(newEntry)))
  }
}
