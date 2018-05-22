// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor
import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game.PlanetSideGUID

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

/**
  * na
  * @param equipmentOnGround a `List` of items (`Equipment`) dropped by players on the ground and can be collected again
  */
class ZoneGroundActor(equipmentOnGround : ListBuffer[Equipment]) extends Actor {
  //private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case Zone.Ground.DropItem(item, pos, orient) =>
      sender ! (FindItemOnGround(item.GUID) match {
        case None =>
          equipmentOnGround += item
          Zone.Ground.ItemOnGround(item, pos, orient)
        case Some(_) =>
          Zone.Ground.CanNotDropItem(item)
      })

    case Zone.Ground.PickupItem(item_guid) =>
      sender ! (FindItemOnGround(item_guid) match {
        case Some(item) =>
          Zone.Ground.ItemInHand(item)
        case None =>
          Zone.Ground.CanNotPickupItem(item_guid)
      })

    case _ => ;
  }

  /**
    * Shift through objects on the ground to find the location of a specific item.
    * @param item_guid the global unique identifier of the piece of `Equipment` being sought
    * @return the index of the object matching `item_guid`, if found;
    *         `None`, otherwise
    */
  private def FindItemOnGround(item_guid : PlanetSideGUID) : Option[Equipment] = {
    recursiveFindItemOnGround(equipmentOnGround.iterator, item_guid) match {
      case Some(index) =>
        Some(equipmentOnGround.remove(index))
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
