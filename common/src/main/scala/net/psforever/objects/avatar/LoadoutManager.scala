// Copyright (c) 2019 PSForever
package net.psforever.objects.avatar

import net.psforever.objects.loadouts.Loadout
import net.psforever.types.LoadoutType

import scala.util.Success

class LoadoutManager(size : Int) {
  private val entries : Array[Option[Loadout]] = Array.fill[Option[Loadout]](size)(None)

  def SaveLoadout(owner : Any, label : String, line : Int) : Unit = {
    Loadout.Create(owner, label) match {
      case Success(loadout) =>
        entries(line) = Some(loadout)
      case _ => ;
    }
  }

  def LoadLoadout(line : Int) : Option[Loadout] = entries.lift(line).flatten

  def DeleteLoadout(line : Int) : Unit = {
    entries(line) = None
  }

  def Loadouts : Seq[(Int, Loadout)] = entries.zipWithIndex.collect { case(Some(loadout), index) => (index, loadout) } toSeq
}
