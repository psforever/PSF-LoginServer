// Copyright (c) 2017 PSForever
package net.psforever.objects.ce

import akka.actor.{ActorContext, Cancellable}
import net.psforever.objects.{DefaultCancellable, PlanetSideGameObject}
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.PlanetSideGUID

trait TelepadLike {
  private var router : Option[PlanetSideGUID] = None
  private var activated : Boolean = false
  private val activation : Cancellable = DefaultCancellable.obj

  def Router : Option[PlanetSideGUID] = router

  def Router_=(rguid : PlanetSideGUID) : Option[PlanetSideGUID] = Router_=(Some(rguid))

  def Router_=(rguid : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    router match {
      case None =>
        router = rguid
      case Some(_) =>
        if(rguid.isEmpty || rguid.contains(PlanetSideGUID(0))) {
          router = None
        }
    }
    Router
  }

  def Active : Boolean = activated

  def Active_=(state : Boolean) : Boolean = {
    activated = state
    Active
  }
}

object TelepadLike {
  final case class Activate(obj : PlanetSideGameObject with TelepadLike)

  final case class Deactivate(obj : PlanetSideGameObject with TelepadLike)

  /**
    * Assemble some logic for a provided object.
    * @param obj an `Amenity` object;
    *            anticipating a `Terminal` object using this same definition
    * @param context hook to the local `Actor` system
    */
  def Setup(obj : Amenity, context : ActorContext) : Unit = {
    obj.asInstanceOf[TelepadLike].Router = obj.Owner.GUID
  }
}
