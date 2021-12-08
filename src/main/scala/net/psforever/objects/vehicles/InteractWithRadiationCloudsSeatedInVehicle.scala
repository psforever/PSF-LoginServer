// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.Vehicle
import net.psforever.objects.ballistics.{Projectile, ProjectileQuality, SourceEntry}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.base.DamageResolution
import net.psforever.objects.vital.etc.RadiationReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones.{InteractsWithZone, Zone, ZoneInteraction}
import net.psforever.types.PlanetSideGUID

/**
  * This game entity may infrequently test whether it may interact with radiation cloud projectiles
  * that may be emitted in the game environment for a limited amount of time.
  * Since the entity in question is a vehicle, the occupants of the vehicle get tested their interaction.
  */
class InteractWithRadiationCloudsSeatedInVehicle(
                                                  private val obj: Vehicle,
                                                  range: Float
                                                ) extends ZoneInteraction {
  /**
    * radiation clouds that, though detected, are skipped from affecting the target;
    * in between interaction tests, a memory of the clouds that were tested last are retained and
    * are excluded from being tested this next time;
    * clouds that are detected a second time are cleared from the list and are available to be tested next time
    */
  private var skipTargets: List[PlanetSideGUID] = List()

  /**
    * Drive into a radiation cloud and all the vehicle's occupants suffer the consequences.
    * @param target the fixed element in this test
    */
  override def interaction(target: InteractsWithZone): Unit = {
    val position = target.Position
    //collect all projectiles in sector/range
    val projectiles = target.Zone.blockMap
      .sector(position, range)
      .projectileList
      .filter { cloud =>
        cloud.Definition.radiation_cloud && Zone.distanceCheck(target, cloud, cloud.Definition.DamageRadius)
      }
      .distinct
    val notSkipped = projectiles.filterNot { t => skipTargets.contains(t.GUID) }
    skipTargets = notSkipped.map { _.GUID }
    if (notSkipped.nonEmpty) {
      (
        //isolate one of each type of projectile
        notSkipped
          .foldLeft(Nil: List[Projectile]) {
            (acc, next) => if (acc.exists { _.profile == next.profile }) acc else next :: acc
          },
        obj.Seats
          .values
          .collect { case seat => seat.occupant }
          .flatten
      ) match {
        case (uniqueProjectiles, targets) if uniqueProjectiles.nonEmpty && targets.nonEmpty =>
          val shielding = obj.Definition.RadiationShielding
          targets.foreach { t =>
            uniqueProjectiles.foreach { p =>
              t.Actor ! Vitality.Damage(
                DamageInteraction(
                  SourceEntry(t),
                  RadiationReason(
                    ProjectileQuality.modifiers(p, DamageResolution.Radiation, t, t.Position, None),
                    t.DamageModel,
                    shielding
                  ),
                  position
                ).calculate()
              )
            }
          }
        case _ => ;
      }
    }
    obj.CargoHolds
      .values
      .collect {
        case hold if hold.isOccupied =>
          val target = hold.occupant.get
          target.interaction().find { func => func.isInstanceOf[InteractWithRadiationCloudsSeatedInVehicle] } match {
            case Some(func) => func.interaction(target)
            case _ => ;
          }
      }
  }

  /**
    * Any radiation clouds blocked from being tested should be cleared.
    * All that can be done is blanking our retained previous effect targets.
    * @param target the fixed element in this test
    */
  def resetInteraction(target: InteractsWithZone): Unit = {
    skipTargets = List()
  }
}
