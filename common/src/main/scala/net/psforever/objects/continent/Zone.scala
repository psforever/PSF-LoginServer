// Copyright (c) 2017 PSForever
package net.psforever.objects.continent

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.{PlanetSideGameObject, Player}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.actor.{NumberPoolAccessorActor, NumberPoolActor}
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.packet.GamePacket
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3

import scala.collection.mutable.ListBuffer

class Zone(id : String, map : ZoneMap, zoneNumber : Int) {
  private var actor = ActorRef.noSender
  private var accessor : ActorRef = ActorRef.noSender
  //private var startupUtilities : List[ServerObjectBuilder] = List()
  private var guid : NumberPoolHub = new NumberPoolHub(new LimitedNumberSource(65536))

  def Actor : ActorRef = actor

  def Init(implicit context : ActorContext) : Unit = {
    //TODO wrong initialization
    implicit val guid = this.guid
    val pool = guid.AddPool("pool", (200 to 1000).toList)
    pool.Selector = new RandomSelector
    val poolActor = context.actorOf(Props(classOf[NumberPoolActor], pool), name = s"$ZoneId-poolActor")
    accessor = context.actorOf(Props(classOf[NumberPoolAccessorActor], guid, pool, poolActor), s"$ZoneId-accessor")

    map.LocalObjects.foreach({builderObject =>
      builderObject.Build
    })
  }

  def Actor_=(zoneActor : ActorRef) : ActorRef = {
    if(actor == ActorRef.noSender) {
      actor = zoneActor
    }
    Actor
  }

  private val equipmentOnGround : ListBuffer[Equipment] = ListBuffer[Equipment]()

  def ZoneId : String = id

  def ZoneNumber : Int = zoneNumber

  def Map : ZoneMap = map

  def GUID : ActorRef = accessor

  def GUID(hub : NumberPoolHub) : ActorRef = {
    if(actor == ActorRef.noSender) {
      guid = hub
    }
    Actor
  }

  def GUID(object_guid : PlanetSideGUID) : Option[PlanetSideGameObject] = GUID(object_guid.guid)

  def GUID(object_guid : Int) : Option[PlanetSideGameObject] = guid(object_guid) match {
    case Some(obj) =>
      Some(obj.asInstanceOf[PlanetSideGameObject]) //potential casting error
    case None =>
      None
  }

  def EquipmentOnGround : ListBuffer[Equipment] = equipmentOnGround

//  def AddUtility(obj : ServerObjectBuilder) : Unit = {
//    startupUtilities = startupUtilities :+ obj
//  }
//
//  def StartupUtilities : List[ServerObjectBuilder] =  {
//    val utilities = startupUtilities
//    startupUtilities = Nil
//    utilities
//  }

  def ClientInitialization() : List[GamePacket] = {
    List.empty[GamePacket]
  }

  def ClientConfiguration() : List[GamePacket] = {
    List.empty[GamePacket]
  }
}

object Zone {
  final case class DropItemOnGround(item : Equipment, pos : Vector3, orient : Vector3)

  final case class GetItemOnGround(player : Player, item_guid : PlanetSideGUID)

  final case class ItemFromGround(player : Player, item : Equipment)

  final case class ClientInitialization(list : List[GamePacket])

  def apply(id : String, map : ZoneMap, number : Int) : Zone = {
    new Zone(id, map, number)
  }
}
