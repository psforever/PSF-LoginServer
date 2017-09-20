// Copyright (c) 2017 PSForever
package net.psforever.objects.continent

import akka.actor.Actor
import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game.PlanetSideGUID

import scala.annotation.tailrec

class ZoneActor(continent : Zone) extends Actor {
  private[this] val log = org.log4s.getLogger
  import Zone._

  def receive : Receive = {
    case DropItemOnGround(item, pos, orient) =>
      item.Position = pos
      item.Orientation = orient
      continent.EquipmentOnGround += item

    case GetItemOnGround(player, item_guid) =>
      FindItemOnGround(item_guid) match {
        case Some(item) =>
          sender ! ItemFromGround(player, item)
        case None =>
          log.warn(s"item on ground $item_guid was requested by $player for pickup but was not found")
      }

    case _ => ;
  }

  private def FindItemOnGround(item_guid : PlanetSideGUID) : Option[Equipment] = {
    recursiveFindItemOnGround(continent.EquipmentOnGround.iterator, item_guid) match {
      case Some(index) =>
        Some(continent.EquipmentOnGround.remove(index))
      case None =>
        None
    }
  }

  /**
    * Shift through objects on the ground to find the location of a specific item.
    * @param iter an `Iterator` of `Equipment`
    * @param item_guid the global unique identifier of the piece of `Equipment` being sought
    * @param index the current position in the array-list structure used to create the `Iterator`
    * @return the index of the object matching `item_guid`, if found;
    *         `None`, otherwise
    */
  @tailrec private def recursiveFindItemOnGround(iter : Iterator[Equipment], item_guid : PlanetSideGUID, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val item : Equipment = iter.next
      if(item.GUID == item_guid) {
        Some(index)
      }
      else {
        recursiveFindItemOnGround(iter, item_guid, index + 1)
      }
    }
  }
}
