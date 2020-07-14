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

  /**
    * Take a game object and transform it into its equivalent data for an `0x17` packet.
    * @param obj the game object
    * @return the specific `ConstructorData` that is equivalent to this object
    */
  def ConstructorData(obj: A): Try[ConstructorData] = {
    Failure(new NoSuchMethodException(s"method not defined for object $obj"))
  }

  /**
    * Take a game object and transform it into its equivalent data for an `0x18` packet.
    * @param obj the game object
    * @return the specific `ConstructorData` that is equivalent to this object
    */
  def DetailedConstructorData(obj: A): Try[ConstructorData] = {
    Failure(new NoSuchMethodException(s"method not defined for object $obj"))
  }
}
