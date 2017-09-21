// Copyright (c) 2017 PSForever
package net.psforever.objects.continent

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.Player
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.actor.{NumberPoolAccessorActor, NumberPoolActor, Register}
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.packet.GamePacket
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3

import scala.collection.mutable.ListBuffer

class Zone(id : String, zoneNumber : Int, map : String) {
  private var actor = ActorRef.noSender
  private var guid : NumberPoolHub = new NumberPoolHub(new LimitedNumberSource(65536))
  private var accessor : ActorRef = ActorRef.noSender
  private var startupUtilities : List[(IdentifiableEntity, Int)] = List()

  def Actor(implicit context : ActorContext) : ActorRef =  {
    if(actor == ActorRef.noSender) {
      actor = context.actorOf(Props(classOf[ZoneActor], this), s"$id-actor")

      val pool = guid.AddPool("pool", (200 to 400).toList)
      val poolActor = context.actorOf(Props(classOf[NumberPoolActor], pool), name = s"$ZoneId-poolActor")
      pool.Selector = new RandomSelector
      accessor = context.actorOf(Props(classOf[NumberPoolAccessorActor], guid, pool, poolActor), s"$ZoneId-accessor")

      startupUtilities.foreach({case ((obj, uid)) => accessor ! Register(obj, uid, actor)})
    }
    actor
  }

  private val equipmentOnGround : ListBuffer[Equipment] = ListBuffer[Equipment]()

  def ZoneId : String = id

  def ZoneNumber : Int = zoneNumber

  def Map : String = map

  def GUID : ActorRef = accessor

  def GUID_=(guidSrc : NumberPoolHub) : ActorRef = {
    if(accessor == ActorRef.noSender) {
      guid = guidSrc
    }
    accessor
  }

  def GUID(object_guid : PlanetSideGUID) : Option[IdentifiableEntity] = guid(object_guid.guid)

  def EquipmentOnGround : ListBuffer[Equipment] = equipmentOnGround

  def AddUtility(obj : IdentifiableEntity, id : Int) : Unit = {
    startupUtilities = startupUtilities :+ (obj, id)
  }

  def ZoneInitialization() : List[GamePacket] = {
    List.empty[GamePacket]
  }

  def ZoneConfiguration() : List[GamePacket] = {
    List.empty[GamePacket]
  }
}

object Zone {
  final def BlankInitFunction() : Unit = { }
  final def Nowhere : Zone = { Zone("nowhere", 0, "nowhere") } //TODO needs overrides

  final case class DropItemOnGround(item : Equipment, pos : Vector3, orient : Vector3)

  final case class GetItemOnGround(player : Player, item_guid : PlanetSideGUID)

  final case class ItemFromGround(player : Player, item : Equipment)

  final case class ZoneInitialization(list : List[GamePacket])

  def apply(zoneId : String, zoneNumber : Int, map : String) : Zone = {
    new Zone(zoneId, zoneNumber, map)
  }
}
