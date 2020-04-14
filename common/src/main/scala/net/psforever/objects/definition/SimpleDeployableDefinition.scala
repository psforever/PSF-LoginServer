// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import akka.actor.{ActorContext, ActorRef}
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ce.{Deployable, DeployableCategory, DeployedItem}
import net.psforever.objects.definition.converter.SmallDeployableConverter
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.vital.resistance.ResistanceProfileMutators
import net.psforever.objects.vital.{DamageResistanceModel, NoResistanceSelection, StandardDeployableDamage, VitalityDefinition}

import scala.concurrent.duration._

trait BaseDeployableDefinition {
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

abstract class DeployableDefinition(objectId : Int) extends ObjectDefinition(objectId)
  with DamageResistanceModel
  with ResistanceProfileMutators
  with VitalityDefinition
  with BaseDeployableDefinition {
  private val item = DeployedItem(objectId) //let throw NoSuchElementException
  DamageUsing = StandardDeployableDamage
  ResistUsing = NoResistanceSelection

  def Item : DeployedItem.Value = item
}

class SimpleDeployableDefinition(objectId : Int) extends DeployableDefinition(objectId) {
  Packet = new SmallDeployableConverter
}

abstract class ComplexDeployableDefinition(objectId : Int) extends DeployableDefinition(objectId)

object SimpleDeployableDefinition {
  def apply(item : DeployedItem.Value) : SimpleDeployableDefinition =
    new SimpleDeployableDefinition(item.id)

  def SimpleUninitialize(obj : PlanetSideGameObject, context : ActorContext) : Unit = { }

  def SimpleUninitialize(obj : PlanetSideServerObject, context : ActorContext) : Unit = {
    context.stop(obj.Actor)
    obj.Actor = ActorRef.noSender
  }
}
