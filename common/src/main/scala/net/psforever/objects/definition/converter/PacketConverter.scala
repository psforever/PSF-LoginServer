// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.PlanetSideGameObject
import net.psforever.packet.game.objectcreate.ConstructorData

import scala.util.{Failure, Try}

/**
  * The base trait for polymorphic assignment for `ObjectCreateConverter`.
  */
sealed trait PacketConverter

/**
  * A converter that accepts an object and prepares it for transformation into an `0x17` packet or an `0x18` packet.
  * This is the decoded packet form of the game object, as if hexadecimal data from a packet was decoded.
  * @tparam A the type of game object
  */
abstract class ObjectCreateConverter[A <: PlanetSideGameObject] extends PacketConverter {
//  def ObjectCreate(obj : A) : Try[ObjectCreateMessage] = {
//    Success(
//      ObjectCreateMessage(obj.Definition.ObjectId, obj.GUID,
//        DroppedItemData(
//          PlacementData(obj.Position, obj.Orientation.x.toInt, obj.Orientation.y.toInt, obj.Orientation.z.toInt, Some(obj.Velocity)),
//          ConstructorData(obj).get
//        )
//      )
//    )
//  }
//
//  def ObjectCreate(obj : A, info : PlacementData) : Try[ObjectCreateMessage] = {
//    Success(ObjectCreateMessage(obj.Definition.ObjectId, obj.GUID, DroppedItemData(info, ConstructorData(obj).get)))
//  }
//
//  def ObjectCreate(obj : A, info : ObjectCreateMessageParent) : Try[ObjectCreateMessage] = {
//    Success(ObjectCreateMessage(obj.Definition.ObjectId, obj.GUID, info, ConstructorData(obj).get))
//  }
//
//  def ObjectCreateDetailed(obj : A) : Try[ObjectCreateDetailedMessage] = {
//    Success(
//      ObjectCreateDetailedMessage(obj.Definition.ObjectId, obj.GUID,
//        DroppedItemData(
//          PlacementData(obj.Position, obj.Orientation.x.toInt, obj.Orientation.y.toInt, obj.Orientation.z.toInt, Some(obj.Velocity)),
//          DetailedConstructorData(obj).get
//        )
//      )
//    )
//  }
//
//  def ObjectCreateDetailed(obj : A, info : PlacementData) : Try[ObjectCreateDetailedMessage] = {
//    Success(ObjectCreateDetailedMessage(obj.Definition.ObjectId, obj.GUID, DroppedItemData(info, DetailedConstructorData(obj).get)))
//  }
//
//  def ObjectCreateDetailed(obj : A, info : ObjectCreateMessageParent) : Try[ObjectCreateDetailedMessage] = {
//    Success(ObjectCreateDetailedMessage(obj.Definition.ObjectId, obj.GUID, info, DetailedConstructorData(obj).get))
//  }

  /**
    * Take a game object and transform it into its equivalent data for an `0x17` packet.
    * @param obj the game object
    * @return the specific `ConstructorData` that is equivalent to this object
    */
  def ConstructorData(obj : A) : Try[ConstructorData] = { Failure(new NoSuchMethodException(s"method not defined for object $obj")) }

  /**
    * Take a game object and transform it into its equivalent data for an `0x18` packet.
    * @param obj the game object
    * @return the specific `ConstructorData` that is equivalent to this object
    */
  def DetailedConstructorData(obj : A) : Try[ConstructorData] = { Failure(new NoSuchMethodException(s"method not defined for object $obj")) }
}
