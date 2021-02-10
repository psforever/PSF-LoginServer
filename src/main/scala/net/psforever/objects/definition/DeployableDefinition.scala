// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import akka.actor.ActorContext
import net.psforever.objects.Default
import net.psforever.objects.ce.{Deployable, DeployableCategory, DeployedItem}
import net.psforever.objects.definition.converter.SmallDeployableConverter
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.resistance.ResistanceProfileMutators
import net.psforever.objects.vital.resolution.DamageResistanceModel
import net.psforever.objects.vital.{NoResistanceSelection, VitalityDefinition}

import scala.concurrent.duration._

trait BaseDeployableDefinition {
  private var category: DeployableCategory.Value = DeployableCategory.Boomers
  private var deployTime: Long                   = (1 second).toMillis //ms

  def Item: DeployedItem.Value

  def DeployCategory: DeployableCategory.Value = category

  def DeployCategory_=(cat: DeployableCategory.Value): DeployableCategory.Value = {
    category = cat
    DeployCategory
  }

  def DeployTime: Long = deployTime

  def DeployTime_=(time: FiniteDuration): Long = DeployTime_=(time.toMillis)

  def DeployTime_=(time: Long): Long = {
    deployTime = time
    DeployTime
  }

  def Initialize(obj: Deployable, context: ActorContext): Unit = {}

  def Uninitialize(obj: Deployable, context: ActorContext): Unit = {
    context.stop(obj.Actor)
    obj.Actor = Default.Actor
  }
}

abstract class DeployableDefinition(objectId: Int)
    extends ObjectDefinition(objectId)
    with DamageResistanceModel
    with ResistanceProfileMutators
    with VitalityDefinition
    with BaseDeployableDefinition {
  private val item = DeployedItem(objectId) //let throw NoSuchElementException
  DamageUsing = DamageCalculations.AgainstVehicle
  ResistUsing = NoResistanceSelection
  Packet = new SmallDeployableConverter

  def Item: DeployedItem.Value = item
}
