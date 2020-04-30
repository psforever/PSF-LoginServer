// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor
import net.psforever.objects.{BoomerDeployable, BoomerTrigger}
import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}
import services.{RemoverActor, Service}
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

/**
  * na
  * @param equipmentOnGround a `List` of items (`Equipment`) dropped by players on the ground and can be collected again
  */
class ZoneGroundActor(zone : Zone, equipmentOnGround : ListBuffer[Equipment]) extends Actor {
  //private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case Zone.Ground.DropItem(item, pos, orient) =>
      sender ! (if(!item.HasGUID) {
        Zone.Ground.CanNotDropItem(zone, item, "not registered yet")
      }
      else if(zone.GUID(item.GUID).isEmpty) {
        Zone.Ground.CanNotDropItem(zone, item, "registered to some other zone")
      }
      else if(equipmentOnGround.contains(item)) {
        Zone.Ground.CanNotDropItem(zone, item, "already dropped")
      }
      else {
        equipmentOnGround += item
        item.Position = pos
        item.Orientation = orient
        ZoneGroundActor.PutItemOnGround(zone, item)
        Zone.Ground.ItemOnGround(item, pos, orient)
      })

    case Zone.Ground.PickupItem(item_guid) =>
      sender ! (FindItemOnGround(item_guid) match {
        case Some(item) =>
          Zone.Ground.ItemInHand(item)
        case None =>
          Zone.Ground.CanNotPickupItem(zone, item_guid, "can not find")
      })

    case Zone.Ground.RemoveItem(item_guid) =>
      FindItemOnGround(item_guid) //intentionally no callback

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

object ZoneGroundActor {
  /**
    * Primary functionality for tranferring a piece of equipment from a player's hands or his inventory to the ground.
    * Items are always dropped at player's feet because for simplicity's sake
    * because, by virtue of already standing there, the stability of the surface has been proven.
    * The only exception to this is dropping items while falling.
    * We have no case for that yet, so the item hangs in midair, typically unreachable.
    * @see `Player.Find`<br>
    *       `ObjectDetachMessage`
    * @param item the `Equipment` object in the player's hand
    */
  def PutItemOnGround(zone : Zone, item : Equipment) : Unit = {
    item match {
      case trigger : BoomerTrigger =>
        //dropped the trigger, no longer own the boomer; make certain whole faction is aware of that
        zone.GUID(trigger.Companion) match {
          case Some(obj : BoomerDeployable) =>
            val guid = obj.GUID
            val factionChannel = obj.Faction.toString
            val owner = obj.OwnerName.getOrElse("")
            val info = DeployableInfo(guid, DeployableIcon.Boomer, obj.Position, PlanetSideGUID(0))
            zone.LocalEvents ! LocalServiceMessage(owner, LocalAction.AlertDestroyDeployable(Service.defaultPlayerGUID, obj))
            obj.AssignOwnership(None)
            obj.Faction = PlanetSideEmpire.NEUTRAL
            zone.LocalEvents ! LocalServiceMessage.Deployables(RemoverActor.AddTask(obj, zone))
            zone.LocalEvents ! LocalServiceMessage(factionChannel, LocalAction.DeployableMapIcon(Service.defaultPlayerGUID, DeploymentAction.Dismiss, info))
            zone.AvatarEvents ! AvatarServiceMessage(factionChannel, AvatarAction.SetEmpire(Service.defaultPlayerGUID, guid, PlanetSideEmpire.NEUTRAL))
          case Some(_) | None =>
            //TODO pointless trigger
            println("[ERROR] Ground: you have a pointless boomer trigger that I can't clean up")
//            val guid = item.GUID
//            zone.Ground ! Zone.Ground.RemoveItem(guid) //undo; no callback
//            zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.ObjectDelete(PlanetSideGUID(0), guid))
//            taskResolver ! GUIDTask.UnregisterObjectTask(item)(zone.GUID)
        }
      case _ => ;
    }
    item.Faction = PlanetSideEmpire.NEUTRAL
    zone.AvatarEvents ! AvatarServiceMessage(zone.Id, AvatarAction.DropItem(Service.defaultPlayerGUID, item, zone))
  }
}