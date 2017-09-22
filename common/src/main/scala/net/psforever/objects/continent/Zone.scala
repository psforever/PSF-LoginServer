// Copyright (c) 2017 PSForever
package net.psforever.objects.continent

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.{PlanetSideGameObject, Player}
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
  private var accessor : ActorRef = ActorRef.noSender
  private var startupUtilities : List[(PlanetSideGameObject, Int)] = List()
  private var guid : NumberPoolHub = new NumberPoolHub(new LimitedNumberSource(65536))

  def Actor : ActorRef = actor

  def Init(implicit context : ActorContext) : Unit = {
    //TODO wrong initialization
    val pool = guid.AddPool("pool", (200 to 1000).toList)
    pool.Selector = new RandomSelector
    val poolActor = context.actorOf(Props(classOf[NumberPoolActor], pool), name = s"$ZoneId-poolActor")
    accessor = context.actorOf(Props(classOf[NumberPoolAccessorActor], guid, pool, poolActor), s"$ZoneId-accessor")

    StartupUtilities.foreach({case ((obj, uid)) =>
      guid.register(obj, uid)
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

  def Map : String = map

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

  def AddUtility(obj : PlanetSideGameObject, id : Int) : Unit = {
    startupUtilities = startupUtilities :+ (obj, id)
  }

  def StartupUtilities : List[(IdentifiableEntity, Int)] =  {
    val utilities = startupUtilities
    startupUtilities = Nil
    utilities
  }

  def ClientInitialization() : List[GamePacket] = {
    List.empty[GamePacket]
  }

  def ClientConfiguration() : List[GamePacket] = {
    List.empty[GamePacket]
  }
}

object Zone {
  final def Nowhere : Zone = { Zone("nowhere", 0, "nowhere") } //TODO needs overrides

  final case class DropItemOnGround(item : Equipment, pos : Vector3, orient : Vector3)

  final case class GetItemOnGround(player : Player, item_guid : PlanetSideGUID)

  final case class ItemFromGround(player : Player, item : Equipment)

  final case class ClientInitialization(list : List[GamePacket])

  def apply(zoneId : String, zoneNumber : Int, map : String) : Zone = {
    new Zone(zoneId, zoneNumber, map)
  }
}
