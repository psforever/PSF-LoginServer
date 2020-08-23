// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.converter.{ObjectCreateConverter, PacketConverter}

/**
  * Associate an object's canned in-game representation with its basic game identification unit.
  * The extension of this `class` would identify the common data necessary to construct such a given game object.<br>
  * <br>
  * The converter transforms a game object that is created by this `ObjectDefinition` into packet data through method-calls.
  * The field for this converter is a `PacketConverter`, the superclass for `ObjectCreateConverter`;
  * the type of the mutator's parameter is `ObjectCreateConverter` of a wildcard `tparam`;
  * and, the accessor return type is `ObjectCreateConverter[PlanetSideGameObject]`, a minimum-true statement.
  * The actual type of the converter at a given point, casted or otherwise, is mostly meaningless.
  * Casting the external object does not mutate any of the types used by the methods within that object.
  * So long as it is an `ObjectCreatePacket`, those methods can be called correctly for a game object of the desired type.
  * @param objectId the object's identifier number
  */
abstract class ObjectDefinition(private val objectId: Int) extends BasicDefinition {

  /** a data converter for this type of object */
  protected var packet: PacketConverter = new ObjectCreateConverter[PlanetSideGameObject]() {}
  Name = "object definition"

  /**
    * Get the conversion object.
    * @return
    */
  final def Packet: ObjectCreateConverter[PlanetSideGameObject] =
    packet.asInstanceOf[ObjectCreateConverter[PlanetSideGameObject]]

  /**
    * Assign this definition a conversion object.
    * @param pkt the new converter
    * @return the current converter, after assignment
    */
  final def Packet_=(pkt: ObjectCreateConverter[_]): PacketConverter = {
    packet = pkt
    Packet
  }

  def ObjectId: Int = objectId
}
