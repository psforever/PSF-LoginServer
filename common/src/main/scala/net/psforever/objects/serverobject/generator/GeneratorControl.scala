// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.generator

import akka.actor.Actor
import net.psforever.objects.{Player, Tool}
import net.psforever.objects.ballistics._
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.DamageableEntity
import net.psforever.objects.serverobject.repair.{Repairable, RepairableEntity}
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.vital.DamageFromExplosion
import net.psforever.packet.game.TriggerEffectMessage
import net.psforever.types.{PlanetSideGeneratorState, Vector3}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}

/**
  * An `Actor` that handles messages being dispatched to a specific `Generator`.
  * @param gen the `Generator` object being governed
  */
class GeneratorControl(gen : Generator) extends Actor
  with FactionAffinityBehavior.Check
  with DamageableEntity
  with RepairableEntity {
  def FactionObject = gen
  def DamageableObject = gen
  def RepairableObject = gen

  def receive : Receive = checkBehavior
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse {
      case GeneratorControl.GeneratorExplodes() => //TODO this only works with projectiles right now!
        val zone = gen.Zone
        gen.Health = 0
        super.DestructionAwareness(gen, gen.LastShot.get)
        gen.Condition = PlanetSideGeneratorState.Destroyed
        GeneratorControl.UpdateOwner(gen)
        //kaboom
        zone.AvatarEvents ! AvatarServiceMessage(
          zone.Id, AvatarAction.SendResponse(
            Service.defaultPlayerGUID, TriggerEffectMessage(gen.GUID, "explosion_generator", None, None)
          )
        )
        //kill everyone within 14m
        gen.Owner match {
          case b : Building =>
            val genDef = gen.Definition
            b.PlayersInSOI.collect {
              case player if player.isAlive && Vector3.DistanceSquared(player.Position, gen.Position) < 196 =>
                player.History(DamageFromExplosion(PlayerSource(player), genDef))
                player.Actor ! Player.Die()
            }
          case _ => ;
        }
        gen.ClearHistory()

      case _ => ;
    }

  override protected def CanPerformRepairs(obj : Target, player : Player, item : Tool) : Boolean = {
    gen.Condition != PlanetSideGeneratorState.Critical && super.CanPerformRepairs(obj, player, item)
  }

  override protected def WillAffectTarget(target : Target, damage : Int, cause : ResolvedProjectile) : Boolean = {
    gen.Condition != PlanetSideGeneratorState.Critical && super.WillAffectTarget(target, damage, cause)
  }

  override protected def DestructionAwareness(target : Target, cause : ResolvedProjectile) : Unit = {
    if(target.Health == 0) {
      target.Health = 1 //temporary
      gen.Condition = PlanetSideGeneratorState.Critical
      GeneratorControl.UpdateOwner(gen)
      //imminent kaboom
      import scala.concurrent.duration._
      import scala.concurrent.ExecutionContext.Implicits.global
      context.system.scheduler.scheduleOnce(10 seconds, self, GeneratorControl.GeneratorExplodes())
    }
  }

  override def Restoration(obj : Repairable.Target) : Unit = {
    super.Restoration(obj)
    gen.Condition = PlanetSideGeneratorState.Normal
    GeneratorControl.UpdateOwner(gen)
  }
}

object GeneratorControl {
  /**
    * na
    */
  private case class GeneratorExplodes()

  /**
    * na
    * @param obj na
    */
  private def UpdateOwner(obj : Generator) : Unit = {
    obj.Owner match {
      case b : Building => b.Actor ! Building.AmenityStateChange(obj)
      case _ => ;
    }
  }
}
