// Copyright (c) 2018 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import net.psforever.objects.ballistics.{DeployableSource, PlayerSource, SourceEntry, UniquePlayer}
import net.psforever.objects.ce._
import net.psforever.objects.definition.{DeployableDefinition, ExoSuitDefinition}
import net.psforever.objects.definition.converter.SmallDeployableConverter
import net.psforever.objects.equipment.JammableUnit
import net.psforever.objects.geometry.d3.VolumetricGeometry
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.damage.{Damageable, DamageableEntity}
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.vital.etc.TrippedMineReason
import net.psforever.objects.vital.resolution.ResolutionCalculations.Output
import net.psforever.objects.vital.{SimpleResolutions, Vitality}
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.objects.zones.Zone
import net.psforever.types.{CharacterSex, ExoSuitType, Vector3}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

import scala.concurrent.duration._

class ExplosiveDeployable(cdef: ExplosiveDeployableDefinition)
  extends Deployable(cdef)
    with JammableUnit {

  override def Definition: ExplosiveDeployableDefinition = cdef
}

object ExplosiveDeployable {
  final case class TriggeredBy(obj: PlanetSideServerObject)
}

class ExplosiveDeployableDefinition(private val objectId: Int)
  extends DeployableDefinition(objectId) {
  Name = "explosive_deployable"
  DeployCategory = DeployableCategory.Mines
  Model = SimpleResolutions.calculate
  Packet = new SmallDeployableConverter

  private var detonateOnJamming: Boolean = true

  var triggerRadius: Float = 0f

  def DetonateOnJamming: Boolean = detonateOnJamming

  def DetonateOnJamming_=(detonate: Boolean): Boolean = {
    detonateOnJamming = detonate
    DetonateOnJamming
  }

  override def Initialize(obj: Deployable, context: ActorContext) = {
    obj.Actor =
      context.actorOf(Props(classOf[MineDeployableControl], obj), PlanetSideServerObject.UniqueActorName(obj))
  }
}

object ExplosiveDeployableDefinition {
  def apply(dtype: DeployedItem.Value): ExplosiveDeployableDefinition = {
    new ExplosiveDeployableDefinition(dtype.id)
  }
}

abstract class ExplosiveDeployableControl(mine: ExplosiveDeployable)
  extends Actor
  with DeployableBehavior
  with Damageable {
  def DeployableObject = mine
  def DamageableObject = mine

  override def postStop(): Unit = {
    super.postStop()
    deployableBehaviorPostStop()
  }

  def commonMineBehavior: Receive =
    deployableBehavior
      .orElse(takesDamage)

  override protected def PerformDamage(
    target: Target,
    applyDamageTo: Output
  ): Unit = {
    if (mine.CanDamage) {
      val originalHealth = mine.Health
      val cause          = applyDamageTo(mine)
      val damage         = originalHealth - mine.Health
      if (CanDetonate(mine, damage, cause.interaction)) {
        ExplosiveDeployableControl.DamageResolution(mine, cause, damage)
      } else {
        mine.Health = originalHealth
      }
    }
  }

  /**
    * A supplement for checking target susceptibility
    * to account for sympathetic explosives even if there is no damage.
    * This does not supercede other underlying checks or undo prior damage checks.
    * @see `Damageable.CanDamageOrJammer`
    * @see `DamageProperties.SympatheticExplosives`
    * @param obj the entity being damaged
    * @param damage the amount of damage
    * @param data historical information about the damage
    * @return `true`, if the target can be affected;
    *        `false`, otherwise
    */
  def CanDetonate(obj: Vitality with FactionAffinity, damage: Int, data: DamageInteraction): Boolean = {
    !mine.Destroyed && (if (damage == 0 && data.cause.source.SympatheticExplosion) {
      Damageable.CanDamageOrJammer(mine, damage = 1, data)
    } else {
      Damageable.CanDamageOrJammer(mine, damage, data)
    })
  }
}

object ExplosiveDeployableControl {
  /**
    * na
    * @param target na
    * @param cause na
    * @param damage na
    */
  def DamageResolution(target: ExplosiveDeployable, cause: DamageResult, damage: Int): Unit = {
    target.History(cause)
    if (cause.interaction.cause.source.SympatheticExplosion) {
      explodes(target, cause)
      DestructionAwareness(target, cause)
    } else if (target.Health == 0) {
      DestructionAwareness(target, cause)
    } else if (!target.Jammed && Damageable.CanJammer(target, cause.interaction)) {
      if ( {
        target.Jammed = cause.interaction.cause match {
          case o: ProjectileReason =>
            val radius = o.projectile.profile.DamageRadius
            Vector3.DistanceSquared(cause.interaction.hitPos, cause.interaction.target.Position) < radius * radius
          case _ =>
            true
        }
      }
      ) {
        if (target.Definition.DetonateOnJamming) {
          explodes(target, cause)
        }
        DestructionAwareness(target, cause)
      }
    }
  }

  /**
    * na
    * @param target na
    * @param cause na
    */
  def explodes(target: Damageable.Target, cause: DamageResult): Unit = {
    target.Health = 1 // short-circuit logic in DestructionAwareness
    val zone = target.Zone
    zone.Activity ! Zone.HotSpot.Activity(cause)
    zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.Detonate(target.GUID, target))
    Zone.serverSideDamage(
      zone,
      target,
      Zone.explosionDamage(Some(cause)),
      ExplosiveDeployableControl.detectionForExplosiveSource(target)
    )
  }

  /**
    * na
    * @param target na
    * @param cause na
    */
  def DestructionAwareness(target: ExplosiveDeployable, cause: DamageResult): Unit = {
    val zone = target.Zone
    val attribution = DamageableEntity.attributionTo(cause, target.Zone)
    Deployables.AnnounceDestroyDeployable(
      target,
      Some(if (target.Jammed || target.Destroyed) 0 seconds else 500 milliseconds)
    )
    target.Destroyed = true
    zone.AvatarEvents ! AvatarServiceMessage(
      zone.id,
      AvatarAction.Destroy(target.GUID, attribution, Service.defaultPlayerGUID, target.Position)
    )
    if (target.Health == 0) {
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        LocalAction.TriggerEffect(Service.defaultPlayerGUID, "detonate_damaged_mine", target.GUID)
      )
    }
  }

  /**
    * Two game entities are considered "near" each other if they are within a certain distance of one another.
    * For explosives, the source of the explosion is always typically constant.
    * @see `detectsTarget`
    * @see `ObjectDefinition.Geometry`
    * @see `Vector3.relativeUp`
    * @param obj a game entity that explodes
    * @return a function that resolves a potential target as detected
    */
  def detectionForExplosiveSource(obj: PlanetSideGameObject): (PlanetSideGameObject, PlanetSideGameObject, Float) => Boolean = {
    val up = Vector3.relativeUp(obj.Orientation) //check relativeUp; rotate as little as necessary!
    val g1 = obj.Definition.Geometry(obj)
    detectTarget(g1, up)
  }

  /**
    * Two game entities are considered "near" each other if they are within a certain distance of one another.
    * For explosives, targets in the damage radius in the direction of the blast (above the explosive) are valid targets.
    * Targets that are ~0.5916f units in the opposite direction of the blast (below the explosive) are also selected.
    * @see `ObjectDefinition.Geometry`
    * @see `PrimitiveGeometry.pointOnOutside`
    * @see `Vector3.DistanceSquared`
    * @see `Vector3.neg`
    * @see `Vector3.relativeUp`
    * @see `Vector3.ScalarProjection`
    * @see `Vector3.Unit`
    * @param g1 a cached geometric representation that should belong to `obj1`
    * @param up a cached vector in the direction of "above `obj1`'s geometric representation"
    * @param obj1 a game entity that explodes
    * @param obj2 a game entity that suffers the explosion
    * @param maxDistance the square of the maximum distance permissible between game entities
    *                    before they are no longer considered "near"
    * @return `true`, if the target entities are near enough to each other;
    *        `false`, otherwise
    */
  def detectTarget(
                    g1: VolumetricGeometry,
                    up: Vector3
                  )
                  (
                    obj1: PlanetSideGameObject,
                    obj2: PlanetSideGameObject,
                    maxDistance: Float
                  ) : Boolean = {
    val g2 = obj2.Definition.Geometry(obj2)
    val dir = g2.center.asVector3 - g1.center.asVector3
    //val scalar = Vector3.ScalarProjection(dir, up)
    val point1 = g1.pointOnOutside(dir).asVector3
    val point2 = g2.pointOnOutside(Vector3.neg(dir)).asVector3
    val scalar = Vector3.ScalarProjection(point2 - obj1.Position, up)
    (scalar >= 0 || Vector3.MagnitudeSquared(up * scalar) < 0.35f) &&
    math.min(
      Vector3.DistanceSquared(g1.center.asVector3, g2.center.asVector3),
      Vector3.DistanceSquared(point1, point2)
    ) <= maxDistance
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

        case _ => ;
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
    instigator match {
      case Some(_) if isConstructed.contains(true) && setup.isCancelled =>
        //re-use the setup timer here
        import scala.concurrent.ExecutionContext.Implicits.global
        setup = context.system.scheduler.scheduleOnce(delay milliseconds, self, MineDeployableControl.Triggered())
      case _ => ;
    }
  }

  def explodes(instigator: Option[PlanetSideServerObject]): Unit = {
    instigator match {
      case Some(_) =>
        //explosion
        mine.Destroyed = true
        ExplosiveDeployableControl.DamageResolution(
          mine,
          DamageInteraction(
            SourceEntry(mine),
            MineDeployableControl.trippedMineReason(mine),
            mine.Position
          ).calculate()(mine),
          damage = 0
        )
      case None =>
        //reset
        setup = Default.Cancellable
    }
  }
}

object MineDeployableControl {
  private case class Triggered()

  def trippedMineReason(mine: ExplosiveDeployable): TrippedMineReason = {
    val deployableSource = DeployableSource(mine)
    val blame = mine.OwnerName match {
      case Some(name) =>
        val(charId, exosuit, seated): (Long, ExoSuitType.Value, Boolean) = mine.Zone
          .LivePlayers
          .find { _.Name.equals(name) } match {
          case Some(player) =>
            //if the owner is alive in the same zone as the mine, use data from their body to create the source
            (player.CharId, player.ExoSuit, player.VehicleSeated.nonEmpty)
          case None         =>
            //if the owner is as dead as a corpse or is not in the same zone as the mine, use defaults
            (0L, ExoSuitType.Standard, false)
        }
        val faction = mine.Faction
        PlayerSource(
          GlobalDefinitions.avatar,
          exosuit,
          seatedIn = None,
          100,
          0,
          mine.Position,
          Vector3.Zero,
          None,
          crouching = false,
          jumping = false,
          ExoSuitDefinition.Select(exosuit, faction),
          bep = 0,
          kills = Nil,
          UniquePlayer(charId, name, CharacterSex.Male, mine.Faction)
        )
      case None =>
        //credit where credit is due
        deployableSource
    }
    TrippedMineReason(deployableSource, blame)
  }
}
