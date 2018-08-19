// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import akka.actor.{ActorContext, ActorRef}
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ce.{Deployable, DeployableCategory, DeployedItem}
import net.psforever.objects.serverobject.PlanetSideServerObject

import scala.concurrent.duration._

trait DeployableDefinition {
  private var category : DeployableCategory.Value = DeployableCategory.Boomers
  private var deployTime : Long = (1 second).toMillis //ms

  def Item : DeployedItem.Value

  def DeployCategory : DeployableCategory.Value = category

  def DeployCategory_=(cat : DeployableCategory.Value) : DeployableCategory.Value = {
    category = cat
    DeployCategory
  }

  def DeployTime : Long = deployTime

  def DeployTime_=(time : FiniteDuration) : Long = DeployTime_=(time.toMillis)

  def DeployTime_=(time: Long) : Long = {
    deployTime = time
    DeployTime
  }

  def Initialize(obj : PlanetSideGameObject with Deployable, context : ActorContext) : Unit = { }

  def Initialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) : Unit = { }

  def Uninitialize(obj : PlanetSideGameObject with Deployable, context : ActorContext) : Unit = { }

  def Uninitialize(obj : PlanetSideServerObject with Deployable, context : ActorContext) : Unit = { }
}

object DeployableDefinition {
  def SimpleUninitialize(obj : PlanetSideGameObject, context : ActorContext) : Unit = { }

  def SimpleUninitialize(obj : PlanetSideServerObject, context : ActorContext) : Unit = {
    obj.Actor ! akka.actor.PoisonPill
    obj.Actor = ActorRef.noSender
  }
}
