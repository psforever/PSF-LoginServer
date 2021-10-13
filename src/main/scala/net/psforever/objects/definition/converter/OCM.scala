// Copyright (c) 2021 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.PlanetSideGameObject
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{ObjectCreateDetailedMessage, ObjectCreateMessage}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent

/**
  * Compose an `ObjectCreateMessage` packet or, if requesting and allowing, an `ObjectCreateDetailedMessage` packet.
  */
object OCM {
  /**
    * Compose an `ObjectCreateMessage` packet of an entity.
    * @param obj the entity being converted into a packet
    * @return an `ObjectCreateMessage` packet
    */
  def apply(obj: PlanetSideGameObject): PlanetSideGamePacket = {
    val definition = obj.Definition
    ObjectCreateMessage(
      definition.ObjectId,
      obj.GUID,
      definition.Packet.ConstructorData(obj).get
    )
  }

  /**
    * Compose a contained `ObjectCreateMessage` packet of an entity.
    * @param obj the entity being converted into a packet
    * @param parent information about the container for this entity
    * @return an `ObjectCreateMessage` packet
    */
  def apply(obj: PlanetSideGameObject, parent: Option[ObjectCreateMessageParent]): PlanetSideGamePacket = {
    parent match {
      case Some(info) => apply(obj, info)
      case _          => apply(obj)
    }
  }
  /**
    * Compose a contained `ObjectCreateMessage` packet of an entity.
    * @param obj the entity being converted into a packet
    * @param parent information about the container for this entity
    * @return an `ObjectCreateMessage` packet
    */
  def apply(obj: PlanetSideGameObject, parent: ObjectCreateMessageParent): PlanetSideGamePacket = {
    val definition = obj.Definition
    ObjectCreateMessage(
      definition.ObjectId,
      obj.GUID,
      parent,
      definition.Packet.ConstructorData(obj).get
    )
  }

  def detailed(obj: PlanetSideGameObject): PlanetSideGamePacket = {
    val definition = obj.Definition
    val packet = definition.Packet
    if (packet.noDetailedForm(obj)) {
      apply(obj) //fall back
    } else {
      ObjectCreateDetailedMessage(
        definition.ObjectId,
        obj.GUID,
        definition.Packet.DetailedConstructorData(obj).get
      )
    }
  }

  /**
    * Compose a contained detailed `ObjectCreateMessage` packet of an entity.
    * @param obj the entity being converted into a packet
    * @param parent information about the container for this entity
    * @return an `ObjectCreateMessage` packet
    */
  def detailed(obj: PlanetSideGameObject, parent: Option[ObjectCreateMessageParent]): PlanetSideGamePacket = {
    parent match {
      case Some(info) => detailed(obj, info)
      case _          => detailed(obj)
    }
  }
  /**
    * Compose a contained detailed `ObjectCreateMessage` packet of an entity.
    * @param obj the entity being converted into a packet
    * @param parent information about the container for this entity
    * @return an `ObjectCreateMessage` packet
    */
  def detailed(obj: PlanetSideGameObject, parent: ObjectCreateMessageParent): PlanetSideGamePacket = {
    val definition = obj.Definition
    val packet = definition.Packet
    if (packet.noDetailedForm(obj)) {
      apply(obj, parent) //fall back
    } else {
      ObjectCreateDetailedMessage(
        definition.ObjectId,
        obj.GUID,
        parent,
        definition.Packet.DetailedConstructorData(obj).get
      )
    }
  }
}
