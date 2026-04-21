// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.generator

import akka.actor.{Actor, Cancellable}
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.{Default, PlanetSideGameObject, Player, Tool}
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.DamageableEntity
import net.psforever.objects.serverobject.repair.{AmenityAutoRepair, Repairable, RepairableEntity}
import net.psforever.objects.serverobject.terminals.{GeneratorTerminalDefinition, Terminal}
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.TriggerEffectMessage
import net.psforever.types.{PlanetSideGeneratorState, Vector3}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * An `Actor` that handles messages being dispatched to a specific `Generator`.
  *
  * @param gen the `Generator` object being governed
  */
class GeneratorControl(gen: Generator)
    extends Actor
    with FactionAffinityBehavior.Check
    with DamageableEntity
    with RepairableEntity
    with AmenityAutoRepair {
  def FactionObject: Generator    = gen
  def DamageableObject: Generator = gen
  def RepairableObject: Generator = gen
  def AutoRepairObject: Generator = gen
  /** flagged to explode after some time */
  var imminentExplosion: Boolean   = false
  /** explode when this timer completes */
  var queuedExplosion: Cancellable = Default.Cancellable
  /** when damaged, announce that damage was dealt on a schedule */
  var alarmCooldown: Cancellable   = Default.Cancellable
  /** the canned explosion used by this generator */
  lazy val explosionFunc: (PlanetSideGameObject, PlanetSideGameObject, Float) => Boolean = {
    /*
    to determine the orientation of the generator room, locate the unique terminal - the generator terminal
    there will only be one terminal in the facility and it will be between the entry door and the generator itself
    this will define the "forward-facing" direction of the generator
    */
    gen.Owner.Amenities.find {
      case t: Terminal => t.Definition.isInstanceOf[GeneratorTerminalDefinition]
      case _           => false
    } match {
      case Some(t) => //installed in a facility; use common dimensions of room
        GeneratorControl.generatorRoomExplosionDetectionTestSetup(t.Position, gen)
      case None => //unverifiable state; explicit default calculations
        val func: (PlanetSideGameObject, PlanetSideGameObject, Float) => Boolean = Zone.distanceCheck
        func
    }
  }

  /*
  behavior of the generator piggybacks from the logic used in `AmenityAutoRepair`
  AAR splits its logic based on whether or not it has detected a source of nanite transfer units (NTU)
  this amenity is the bridge between NTU and facility power so it leverages that logic
  it is split between when detecting ntu and when starved for ntu
   */

  def receive: Receive = withNtu

  /** behavior that is valid for both "with-ntu" and "without-ntu" */
  val commonBehavior: Receive =
    checkBehavior
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse(autoRepairBehavior)
      .orElse {
        case GeneratorControl.UnderThreatAlarm() =>
          //alert to damage and block other damage alerts for a time
          if (alarmCooldown.isCancelled) {
            GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.UnderAttack))
            alarmCooldown.cancel()
            alarmCooldown = context.system.scheduler.scheduleOnce(delay = 5 seconds, self, GeneratorControl.AlarmReset())
          }

        case GeneratorControl.AlarmReset() =>
          //clear the blocker for alerting to damage
          alarmCooldown = Default.Cancellable
      }

  /*
  when NTU is detected,
  the generator can be properly destabilized and explode
  the generator can be repaired to operational status and power the facility in which it is installed
   */
  def withNtu: Receive =
    commonBehavior
      .orElse {
        case GeneratorControl.Destabilized() =>
          imminentExplosion = true
          //the generator's condition is technically destroyed, but avoid official reporting until the explosion
          gen.Condition = PlanetSideGeneratorState.Destroyed
          GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Destabilized))
          queuedExplosion.cancel()
          queuedExplosion = context.system.scheduler.scheduleOnce(10 seconds, self, GeneratorControl.GeneratorExplodes())

        case GeneratorControl.GeneratorExplodes() =>
          //TODO this only works with projectiles right now!
          val zone = gen.Zone
          gen.Health = 0
          super.DestructionAwareness(gen, gen.LastDamage.get)
          GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Destroyed))
          //kaboom
          zone.AvatarEvents ! AvatarServiceMessage(
            zone.id,
            AvatarAction.SendResponse(
              Service.defaultPlayerGUID,
              TriggerEffectMessage(gen.GUID, "explosion_generator", None, None)
            )
          )
          queuedExplosion.cancel()
          queuedExplosion = Default.Cancellable
          imminentExplosion = false
          //hate on everything nearby
          Zone.serverSideDamage(gen.Zone, gen, Zone.explosionDamage(gen.LastDamage), explosionFunc, Zone.findAllTargets)

        case GeneratorControl.Restored() =>
          gen.ClearHistory()
          GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Online))

        case _ => ;
      }

  /*
  when ntu is not expected,
  the generator can still be destroyed but will not explode
  handles the possibility that ntu was lost during an ongoing destabilization and cancels the explosion
   */
  def withoutNtu: Receive =
    commonBehavior
      .orElse {
        case GeneratorControl.GeneratorExplodes() =>
          queuedExplosion.cancel()
          queuedExplosion = Default.Cancellable
          imminentExplosion = false

        case GeneratorControl.Destabilized() =>
          //if the generator is destabilized but has no ntu, it will not explode
          gen.Health = 0
          super.DestructionAwareness(gen, gen.LastDamage.get)
          queuedExplosion.cancel()
          queuedExplosion = Default.Cancellable
          imminentExplosion = false
          gen.Condition = PlanetSideGeneratorState.Destroyed
          GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Destroyed))

        case _ =>
      }

  override protected def CanPerformRepairs(obj: Target, player: Player, item: Tool): Boolean = {
    //if an explosion is queued, disallow repairs
    !imminentExplosion && super.CanPerformRepairs(obj, player, item)
  }

  override protected def WillAffectTarget(target: Target, damage: Int, cause: DamageResult): Boolean = {
    //if an explosion is queued, disallow further damage
    !imminentExplosion && super.WillAffectTarget(target, damage, cause)
  }

  override protected def DamageAwareness(target: Target, cause: DamageResult, amount: Any): Unit = {
    tryAutoRepair()
    super.DamageAwareness(target, cause, amount)
    val damageTo = amount match {
      case a: Int => a
      case _ => 0
    }
    GeneratorControl.DamageAwareness(gen, cause, damageTo)
  }

  override protected def DestructionAwareness(target: Target, cause: DamageResult): Unit = {
    tryAutoRepair()
    //if the target is already destroyed, do not let it be destroyed again
    if (!target.Destroyed) {
      target.Health = 1 //temporary
      GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Offline))
      self ! GeneratorControl.Destabilized()
    }
  }

  override def PerformRepairs(target : Target, amount : Int) : Int = {
    val newHealth = super.PerformRepairs(target, amount)
    if(newHealth == target.Definition.MaxHealth) {
      stopAutoRepair()
    }
    if(gen.Condition == PlanetSideGeneratorState.Critical && newHealth > (target.MaxHealth / 2)) {
      gen.Condition = PlanetSideGeneratorState.Normal
      GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Normal))
    }
    newHealth
  }

  override def Restoration(obj: Repairable.Target): Unit = {
    super.Restoration(obj)
    gen.Condition = PlanetSideGeneratorState.Normal
    GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Normal))
    self ! GeneratorControl.Restored()
  }

  override def withNtuSupplyCallback() : Unit = {
    context.become(withNtu)
    super.withNtuSupplyCallback()
    //if not destroyed when a source of ntu is detected, restore facility power
    if(!gen.Destroyed) {
      self ! GeneratorControl.Restored()
    }
  }

  override def noNtuSupplyCallback() : Unit = {
    //auto-repair must stop naturally
    context.become(withoutNtu)
    super.noNtuSupplyCallback()
    //if not destroyed when cutoff from a source of ntu, stop facility power generation
    if(!gen.Destroyed) {
      GeneratorControl.UpdateOwner(gen, Some(GeneratorControl.Event.Offline))
    }
    //quit any explosion (see withoutNtu->GeneratorControl.Destabilized)
    if(!queuedExplosion.isCancelled) {
      queuedExplosion.cancel()
      self ! GeneratorControl.Destabilized()
    }
  }
}

object GeneratorControl {
  /**
    * na
    */
  private case class Destabilized()

  /**
    * na
    */
  private case class GeneratorExplodes()

  /**
    * na
    */
  private case class UnderThreatAlarm()

  /**
    * na
    */
  private case class AlarmReset()

  /**
    * na
    */
  private case class Restored()

  /**
    * na
    */
  object Event extends Enumeration {
    val
    Critical, //PlanetSideGeneratorState.Critical
    UnderAttack,
    Destabilized,
    Destroyed, //PlanetSideGeneratorState.Destroyed
    Offline,
    Normal, //PlanetSideGeneratorState.Normal
    Online
    = Value
  }

  /**
    * Send a message back to the owner for which this `Amenity` entity is installed.
    * @param obj the entity doing the self-reporting
    * @param data optional information that indicates the nature of the state change
    */
  private def UpdateOwner(obj: Generator, data: Option[Any] = None): Unit = {
    obj.Owner.Actor ! BuildingActor.AmenityStateChange(obj, data)
  }

  /**
    * If not destroyed, it will complain about being damaged.
    * @param target the entity being damaged
    * @param cause historical information about the damage
    * @param amount the amount of damage
    */
  def DamageAwareness(target: Generator, cause: DamageResult, amount: Int): Unit = {
    if (!target.Destroyed) {
      val health: Float = target.Health.toFloat
      val max: Float    = target.MaxHealth.toFloat
      if (target.Condition != PlanetSideGeneratorState.Critical && health / max < 0.51f) { //becoming critical
        target.Condition = PlanetSideGeneratorState.Critical
        GeneratorControl.UpdateOwner(target, Some(GeneratorControl.Event.Critical))
      }
      //the generator is under attack
      target.Actor ! UnderThreatAlarm()
    }
  }

  /**
    * The explosion of the generator affects all targets within the generator room.
    * Perform setup using basic input to calculate the data that will orient the "room"
    * in terms of what targets can be affected by the explosion.
    * @param pointTowardsFront starting from the generator's centroid,
    *                          a point that represents something "in front of" the generator
    * @param source the generator
    * @return a function that takes source and target and
    *         calculates whether or not the target will be affected by an explosion of the source
    */
  def generatorRoomExplosionDetectionTestSetup(
                                                pointTowardsFront: Vector3,
                                                source: PlanetSideGameObject
                                              ): (PlanetSideGameObject, PlanetSideGameObject, Float)=> Boolean = {
    import net.psforever.types.Vector3._
    val sourceGeometry = source.Definition.Geometry(source)
    val sourcePositionXY = source.Position.xy
    val up = Vector3(0,0,1)
    val inFrontOf = if (pointTowardsFront.xy == sourcePositionXY) {
      pointTowardsFront.xy + Vector3(1,0,0)
    } else {
      pointTowardsFront.xy
    }
    val front = inFrontOf - sourcePositionXY
    val side = CrossProduct(front, up)
    generatorRoomExplosionDetectionTest(
      sourcePositionXY,
      Unit(front),
      Unit(side),
      Unit(up),
      sourceGeometry.pointOnOutside(up).asVector3.z,
      sourceGeometry.pointOnOutside(neg(up)).asVector3.z
    )
  }

  /**
    * The explosion of the generator affects all targets within the generator room.
    * The generator room is not perfectly geometric nor it is even properly centered on the generator unit.
    * As a consequence, different measurements must be performed to determine that the target is "within" and
    * that the target is not "outside" of the detection radius of the room.
    * Magic numbers for the room dimensions are employed.
    * @see `Zone.distanceCheck`
    * @see `Zone.serverSideDamage`
    * @param g1ctrXY the center of the generator on the xy-axis
    * @param ufront a `Vector3` entity that points to the "front" direction of the generator;
    *               the `u` prefix indicates a "unit vector"
    * @param uside a `Vector3` entity that points to the "side" direction of the generator;
    *              the `u` prefix indicates a "unit vector"
    * @param uup a `Vector3` entity that points to the "top" direction of the generator;
    *            the `u` prefix indicates a "unit vector"
    * @param topPoint a point at the top of the generator;
    *                 represents the highest possible point of damage
    * @param basePoint a point at the bottom of the generator;
    *                  represents the lowest possible point of damage
    * @param source a game entity, should be the source of the explosion
    * @param target a game entity, should be the target of the explosion
    * @param maxDistance the square of the maximum distance permissible between game entities;
    *                    not used here
    * @return `true`, if the target entities are near enough to each other;
    *        `false`, otherwise
    */
  def generatorRoomExplosionDetectionTest(
                                           g1ctrXY: Vector3,
                                           ufront: Vector3,
                                           uside: Vector3,
                                           uup: Vector3,
                                           topPoint: Float,
                                           basePoint: Float
                                         )
                                         (
                                           source: PlanetSideGameObject,
                                           target: PlanetSideGameObject,
                                           maxDistance: Float
                                         ): Boolean = {
    import net.psforever.types.Vector3._
    val g2 = target.Definition.Geometry(target)
    val udir = Unit(target.Position.xy - g1ctrXY) //direction from source to target, xy-axis
    val dir = g2.pointOnOutside(neg(udir)).asVector3.xy - g1ctrXY //distance from source to target, xy-axis
    /* withinBaseToTop */
    topPoint > g2.pointOnOutside(neg(uup)).asVector3.z &&
    basePoint <= g2.pointOnOutside(uup).asVector3.z &&
    /* withinSideToSide; squaring negates the "which side" concern */
    MagnitudeSquared(VectorProjection(dir, uside)) < 121 &&
    ( /* withinFrontBack */
      if (DotProduct(udir, ufront) > 0) {
        MagnitudeSquared(VectorProjection(dir, ufront)) < 210 //front, towards entry door
      } else {
        MagnitudeSquared(VectorProjection(dir, neg(ufront))) < 72 //back, towards back of room
      }
    )
  }
}
