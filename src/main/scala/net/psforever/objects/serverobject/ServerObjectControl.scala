// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject

import akka.actor.Actor
import net.psforever.types.PlanetSideGUID

abstract class ServerObjectControl
  extends Actor {
  protected val log = org.log4s.getLogger(toString())

  val attributeBehavior: Receive = {
    case ServerObject.AttributeMsg(attribute, value, other) =>
      parseAttribute(attribute, value, other)

    case ServerObject.GenericObjectAction(guid, action, other) =>
      parseObjectAction(guid, action, other)

    case ServerObject.GenericAction(guid, action, other) =>
      parseGenericAction(guid, action, other)
  }

  def parseAttribute(attribute: Int, value: Long, other: Option[Any]): Unit

  def parseGenericAction(guid: PlanetSideGUID, action: Int, other: Option[Any]): Unit = { /*intentionally blank*/ }

  def parseObjectAction(guid: PlanetSideGUID, action: Int, other: Option[Any]): Unit = { /*intentionally blank*/ }
}

object ServerObject {
  final case class AttributeMsg(attribute: Int, value: Long, other: Option[Any] = None)

  final case class GenericAction(guid: PlanetSideGUID, action: Int, other: Option[Any] = None)

  final case class GenericObjectAction(guid: PlanetSideGUID, action: Int, other: Option[Any] = None)

  final case class StateChangeDenied(original: Any, msg: String)
}
