// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import akka.actor.{ActorContext, ActorRef}
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ce.{Deployable, DeployableCategory, DeployedItem}
import net.psforever.objects.definition.converter.SmallDeployableConverter
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.vital.resistance.ResistanceProfileMutators
import net.psforever.objects.vital.{DamageResistanceModel, NoResistanceSelection, StandardDeployableDamage, StandardResistanceProfile}

import scala.concurrent.duration._

trait BaseDeployableDefinition extends DamageResistanceModel
  with ResistanceProfileMutators {
  private var category : DeployableCategory.Value = DeployableCategory.Boomers
  private var deployTime : Long = (1 second).toMillis //ms
  private var maxHealth : Int = 1
  Damage = StandardDeployableDamage
  Resistance = NoResistanceSelection

  def Item : DeployedItem.Value

  def MaxHealth : Int = maxHealth

  def MaxHealth_=(toHealth : Int) : Int = {
    maxHealth = toHealth
    MaxHealth
  }

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

class DeployableDefinition(private val objectId : Int) extends ObjectDefinition(objectId)
  with BaseDeployableDefinition {
  private val item = DeployedItem(objectId) //let throw NoSuchElementException
  Packet = new SmallDeployableConverter

  def Item : DeployedItem.Value = item
}

object DeployableDefinition {
  def apply(item : DeployedItem.Value) : DeployableDefinition =
    new DeployableDefinition(item.id)

  def SimpleUninitialize(obj : PlanetSideGameObject, context : ActorContext) : Unit = { }

  def SimpleUninitialize(obj : PlanetSideServerObject, context : ActorContext) : Unit = {
    obj.Actor ! akka.actor.PoisonPill
    obj.Actor = ActorRef.noSender
  }
}
