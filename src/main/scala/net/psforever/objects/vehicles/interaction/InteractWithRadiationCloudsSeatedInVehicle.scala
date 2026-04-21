// Copyright (c) 2021 PSForever
package net.psforever.objects.vehicles.interaction

import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.mount.interaction.{InteractWithRadiationCloudsSeatedInEntity, RadiationInMountableInteraction}
import net.psforever.objects.zones.blockmap.SectorPopulation
import net.psforever.objects.zones.interaction.InteractsWithZone

/**
 * This game entity may infrequently test whether it may interact with radiation cloud projectiles
 * that may be emitted in the game environment for a limited amount of time.
 * Since the entity in question is a vehicle, the occupants of the vehicle's mounted vehicles get tested for their interaction.
 */
class InteractWithRadiationCloudsSeatedInVehicle(
                                                  private val obj: Vehicle,
                                                  override val range: Float
                                                ) extends InteractWithRadiationCloudsSeatedInEntity(obj, range) {
  /**
   * Drive into a radiation cloud and all the vehicle's occupants suffer the consequences.
   * @param sector the portion of the block map being tested
   * @param target the fixed element in this test
   */
  override def interaction(sector: SectorPopulation, target: InteractsWithZone): Unit = {
    super.interaction(sector, target) //vehicle is a mountable entity and must call down
    obj.CargoHolds
      .values
      .collect {
        case hold if hold.isOccupied =>
          val target = hold.occupant.get
          target
            .interaction()
            .find(_.Type == RadiationInMountableInteraction)
            .foreach(func => func.interaction(sector, obj))
          }
  }
}
