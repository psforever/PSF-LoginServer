// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.deploy

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.{DeployableDefinition, ObjectDefinition}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.zones.Zone
import net.psforever.types.{DriveState, Vector3}

import scala.annotation.unused

/**
 * Block the deployment of certain entities within a certain distance.
 * Deployable vehicles and combat engineer entities both have a deployment condition that can be influenced by these ranges.
 * Vehicles of an object type block other vehicles of that object type.
 * Combat engineering entities block combat engineering entities of the same category.
 * @param main distance between which this kind of entity blocks itself (m)
 * @param sharedGroupId identifier for the similar entity group
 * @param shared distance between entities that belong to the similar group block one another (m)
 * @param deployables distance between which this entity may block deployment of combat engineering entities (m);
 *                   defaults to 0
 */
final case class InterferenceRange(main: Float = 0f, sharedGroupId: Int = 0, shared: Float = 0f, deployables: Float = 0f) {
  assert(
    main > -1f && deployables > -1f,
    "if set, interference range must be positive non-zero float value"
  )
  assert(
    sharedGroupId == 0 || shared > 0f,
    "if set, a shared group of a non-zero id should have a positive non-zero float value"
  )
}

object Interference {
  final val AllowAll: InterferenceRange = InterferenceRange()

  final val MaxRange: Float = 125f

  /**
   * When two entities connected by similarity or distance exert influence on one another
   * that stops the later-acting entity from manifesting
   * or disables certain behaviors from the later-acting entity.
   * Only dynamic entities are examined.
   * Static entities like facility amenities or zone-specific elements that exert interference
   * must manage their interactions through other methods.
   * @param zone game world in which this test will be conducted;
   *             entity should be `ZoneAware`, but it may not be set correctly during this part of its internal process
   * @param obj entity that may be interfered with
   * @return a different entity that causes the test entity to suffer interference
   */
  def Test(zone: Zone, obj: PlanetSideGameObject with FactionAffinity): Option[PlanetSideGameObject with FactionAffinity] = {
    val (data, filterFunc) = SetupForTest(zone, obj)
    data.find(filterFunc)
  }

  /**
   * When two entities connected by similarity or distance exert influence on one another
   * that stops the later-acting entity from manifesting
   * or disables certain behaviors from the later-acting entity.
   * @param zone game world in which this test will be conducted;
   *             entity should be `ZoneAware`, but it may not be set correctly during this part of its internal process
   * @param obj entity that may be interfered with
   * @return list of entities to run an interference test on, and
   *         predicate to determine which entities pass the test
   */
  def SetupForTest(
                    zone: Zone,
                    obj: PlanetSideGameObject with FactionAffinity
                  ): (List[PlanetSideGameObject with FactionAffinity], PlanetSideGameObject => Boolean) = {
    val objectDefinition = obj.Definition
    if (objectDefinition.interference eq Interference.AllowAll) {
      (List(), interferenceTestNoResults) //no targets can block, and test will never pass
    } else {
      val position = obj.Position
      val faction = obj.Faction
      val sharedGroupId = objectDefinition.interference.sharedGroupId
      val sector = zone.blockMap.sector(position, Interference.MaxRange)
      val targets = (sector.deployableList ++ sector.vehicleList.filter(_.DeploymentState >= DriveState.Deploying))
        .collect { case target: PlanetSideGameObject with FactionAffinity
          if target.Faction == faction &&
            (target.Definition.asInstanceOf[ObjectDefinition].interference ne Interference.AllowAll) =>
          target
        }
      if (sharedGroupId != 0) {
        (targets, interferenceTestWithSharedGroup(position, objectDefinition, sharedGroupId))
      } else {
        (targets, interferenceTestNoSharedGroup(position, objectDefinition))
      }
    }
  }

  private def interferenceTestNoResults(@unused p: PlanetSideGameObject): Boolean = false

  private def interferenceTestNoSharedGroup(
                                             position: Vector3,
                                             objectDefinition: ObjectDefinition
                                           ): PlanetSideGameObject => Boolean = { p =>
    val pDefinition = p.Definition
    val objectInterference = objectDefinition.interference
    lazy val distanceSq = Vector3.DistanceSquared(position, p.Position)
    if (pDefinition == objectDefinition) {
      distanceSq < objectInterference.main * objectInterference.main
    } else if (pDefinition.isInstanceOf[DeployableDefinition]) {
      distanceSq < objectInterference.deployables * objectInterference.deployables
    } else {
      false
    }
  }

  private def interferenceTestWithSharedGroup(
                                               position: Vector3,
                                               objectDefinition: ObjectDefinition,
                                               sharedGroupId: Int
                                             ): PlanetSideGameObject => Boolean = { p =>
    val pDefinition = p.Definition
    val objectInterference = objectDefinition.interference
    lazy val distanceSq = Vector3.DistanceSquared(position, p.Position)
    if (pDefinition == objectDefinition) {
      distanceSq < objectInterference.main * objectInterference.main
    } else if (sharedGroupId == pDefinition.interference.sharedGroupId) {
      distanceSq < objectInterference.shared * objectInterference.shared
    } else if (pDefinition.isInstanceOf[DeployableDefinition]) {
      distanceSq < objectInterference.deployables * objectInterference.deployables
    } else {
      false
    }
  }
}
