// Copyright (c) 2017 PSForever
package net.psforever.objects.continent

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.Player
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.actor.{NumberPoolAccessorActor, NumberPoolActor}
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3

import scala.collection.mutable.ListBuffer

class Continent(zoneId : String, map : String) {
  private var actor = ActorRef.noSender
  private val guid : NumberPoolHub = new NumberPoolHub(new LimitedNumberSource(65536))
  private var accessor : ActorRef = ActorRef.noSender

  def Actor(implicit context : ActorContext) : ActorRef =  {
    if(actor == ActorRef.noSender) {
      actor = context.actorOf(Props(classOf[ContinentActor], this), s"$zoneId-actor")

      val pool = guid.AddPool("pool", (400 to 599).toList)
      val poolActor = context.actorOf(Props(classOf[NumberPoolActor], pool), name = s"$ZoneId-poolActor")
      pool.Selector = new RandomSelector
      accessor = context.actorOf(Props(classOf[NumberPoolAccessorActor], guid, pool, poolActor), s"$ZoneId-accessor")
    }
    actor
  }

  private val equipmentOnGround : ListBuffer[Equipment] = ListBuffer[Equipment]()

  def ZoneId : String = zoneId

  def Map : String = map

  def GUID : ActorRef = accessor

  def GUID(object_guid : PlanetSideGUID) : Option[IdentifiableEntity] = guid(object_guid.guid)

  def EquipmentOnGround : ListBuffer[Equipment] = equipmentOnGround
}

object Continent {
  final def Nowhere : Continent = { Continent("", "") } //TODO needs overrides

  final case class DropItemOnGround(item : Equipment, pos : Vector3, orient : Vector3)

  final case class GetItemOnGround(player : Player, item_guid : PlanetSideGUID)

  final case class GiveItemFromGround(player : Player, item : Equipment)

  def apply(zoneId : String, map : String) : Continent = {
    new Continent(zoneId, map)
  }
}
