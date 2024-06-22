// Copyright (c) 2024 PSForever
package net.psforever.objects

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.sourcing.{DeployableSource, PlayerSource, SourceEntry}
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
    lazy val deployableSource = DeployableSource(mine)
    val zone = mine.Zone
    val ownerName = mine.OwnerName
    val blame = zone
      .Players
      .find(a => ownerName.contains(a.name))
      .collect { a =>
        val name = a.name
        assignBlameToFrom(name, zone.LivePlayers)
          .orElse(assignBlameToFrom(name, zone.Corpses))
          .getOrElse {
            val player = PlayerSource(name, mine.Faction, mine.Position) //might report minor inconsistencies, e.g., exo-suit type
            player.copy(unique = player.unique.copy(charId = a.id), progress = a.scorecard.CurrentLife)
          }
      }
      .getOrElse(deployableSource)
    TrippedMineReason(deployableSource, blame)
  }

  /**
   * Find a player with a given name from this list of possible players.
   * If the player is seated, attach a shallow copy of the mounting information.
   * @param name player name
   * @param blameList possible players in which to find the player name
   * @return discovered player as a reference, or `None` if not found
   */
  private def assignBlameToFrom(name: String, blameList: List[Player]): Option[SourceEntry] = {
    blameList
      .find(_.Name.equals(name))
      .map { player =>
        PlayerSource
          .mountableAndSeat(player)
          .map { case (mount, seat) => PlayerSource.inSeat(player, mount, seat) }
          .getOrElse { PlayerSource(player) }
      }
  }
}
