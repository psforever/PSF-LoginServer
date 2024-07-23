// Copyright (c) 2024 PSForever
package net.psforever.objects

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.sourcing.{DeployableSource, SourceEntry}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.etc.TrippedMineReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.types.Vector3

import scala.concurrent.duration._

class MineDeployableDefinition(private val objectId: Int)
  extends ExplosiveDeployableDefinition(objectId) {
  override def Initialize(obj: Deployable, context: ActorContext): Unit = {
    obj.Actor =
      context.actorOf(Props(classOf[MineDeployableControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }
}

object MineDeployableDefinition {
  def apply(dtype: DeployedItem.Value): MineDeployableDefinition = {
    new MineDeployableDefinition(dtype.id)
  }
}

class MineDeployableControl(mine: ExplosiveDeployable)
  extends ExplosiveDeployableControl(mine) {

  def receive: Receive =
    commonMineBehavior
      .orElse {
        case ExplosiveDeployable.TriggeredBy(obj) =>
          setTriggered(Some(obj), delay = 200)

        case MineDeployableControl.Triggered() =>
          explodes(testForTriggeringTarget(
            mine,
            mine.Definition.innateDamage.map { _.DamageRadius }.getOrElse(mine.Definition.triggerRadius)
          ))

        case _ => ()
      }

  override def finalizeDeployable(callback: ActorRef): Unit = {
    super.finalizeDeployable(callback)
    //initial triggering upon build
    setTriggered(testForTriggeringTarget(mine, mine.Definition.triggerRadius), delay = 1000)
  }

  def testForTriggeringTarget(mine: ExplosiveDeployable, range: Float): Option[PlanetSideServerObject] = {
    val position = mine.Position
    val faction = mine.Faction
    val range2 = range * range
    val sector = mine.Zone.blockMap.sector(position, range)
    (sector.livePlayerList ++ sector.vehicleList)
      .find { thing => thing.Faction != faction && Vector3.DistanceSquared(thing.Position, position) < range2 }
  }

  def setTriggered(instigator: Option[PlanetSideServerObject], delay: Long): Unit = {
    instigator
      .collect {
        case _ if isConstructed.contains(true) && setup.isCancelled =>
          //re-use the setup timer here
          import scala.concurrent.ExecutionContext.Implicits.global
          setup = context.system.scheduler.scheduleOnce(delay milliseconds, self, MineDeployableControl.Triggered())
      }
  }

  override def CanDetonate(obj: Vitality with FactionAffinity, damage: Int, data: DamageInteraction): Boolean = {
    super.CanDetonate(obj, damage, data) || data.cause.isInstanceOf[TrippedMineReason]
  }

  def explodes(instigator: Option[PlanetSideServerObject]): Unit = {
    //reset
    setup = Default.Cancellable
    instigator
      .collect {
        case _ =>
          //explosion
          HandleDamage(
            mine,
            DamageInteraction(
              SourceEntry(mine),
              MineDeployableControl.trippedMineReason(mine),
              mine.Position
            ).calculate()(mine),
            damage = 0
          )
      }
  }
}

object MineDeployableControl {
  private case class Triggered()

  def trippedMineReason(mine: ExplosiveDeployable): TrippedMineReason = {
    val deployableSource = DeployableSource(mine)
    val blame = Deployables.AssignBlameTo(mine.Zone, mine.OwnerName, deployableSource)
    TrippedMineReason(deployableSource, blame)
  }
}
