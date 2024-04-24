// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.serverobject.deploy.{Deployment, Interference}
import net.psforever.objects.vital.InGameHistory
import net.psforever.objects.{Default, Vehicle}
import net.psforever.types.{DriveState, PlanetSideEmpire, Vector3}

import scala.annotation.tailrec
import scala.collection.mutable

/**
  * Synchronize management of the list of `Vehicles` maintained by some `Zone`.
  */
class ZoneVehicleActor(
                        zone: Zone,
                        vehicleList: mutable.ListBuffer[Vehicle],
                        turretToMount: mutable.HashMap[Int, Int]
                      ) extends Actor {
  private var temporaryInterference: Seq[(Vector3, PlanetSideEmpire.Value, VehicleDefinition)] = Seq()

  def receive: Receive = {
    case Zone.Vehicle.Spawn(vehicle) =>
      if (!vehicle.HasGUID) {
        sender() ! Zone.Vehicle.CanNotSpawn(zone, vehicle, "not registered yet")
      } else if (vehicleList.contains(vehicle)) {
        sender() ! Zone.Vehicle.CanNotSpawn(zone, vehicle, "already in zone")
      } else if (vehicle.Actor != Default.Actor) {
        sender() ! Zone.Vehicle.CanNotSpawn(zone, vehicle, "already in another zone")
      } else {
        vehicleList += vehicle
        vehicle.Zone = zone
        val vguid = vehicle.GUID.guid
        vehicle
          .Weapons
          .values
          .flatten { _.Equipment.map { _.GUID.guid } }
          .foreach { guid =>
            turretToMount.put(guid, vguid)
          }
        vehicle.Definition.Initialize(vehicle, context)
      }
      if (vehicle.MountedIn.isEmpty) {
        zone.actor ! ZoneActor.AddToBlockMap(vehicle, vehicle.Position)
      }
      InGameHistory.SpawnReconstructionActivity(vehicle, zone.Number, None)
      sender() ! Zone.Vehicle.HasSpawned(zone, vehicle)

    case Zone.Vehicle.Despawn(vehicle) =>
      ZoneVehicleActor.recursiveFindVehicle(vehicleList.iterator, vehicle) match {
        case Some(index) =>
          vehicleList.remove(index)
          vehicle.Definition.Uninitialize(vehicle, context)
          val vguid = vehicle.GUID.guid
          turretToMount.filterInPlace { case (_, guid) => guid != vguid }
          vehicle.Position = Vector3.Zero
          vehicle.ClearHistory()
          zone.actor ! ZoneActor.RemoveFromBlockMap(vehicle)
          sender() ! Zone.Vehicle.HasDespawned(zone, vehicle)
        case None =>
          sender() ! Zone.Vehicle.CanNotDespawn(zone, vehicle, "can not find")
      }

    case Zone.Vehicle.TryDeploymentChange(vehicle, toDeployState)
      if toDeployState == DriveState.Deploying &&
        (ZoneVehicleActor.temporaryInterferenceTest(vehicle,temporaryInterference) || Interference.Test(zone, vehicle).nonEmpty) =>
      sender() ! Zone.Vehicle.CanNotDeploy(zone, vehicle, toDeployState, "blocked by a nearby entity")

    case Zone.Vehicle.TryDeploymentChange(vehicle, toDeployState)
      if toDeployState == DriveState.Deploying =>
      import scala.concurrent.duration._
      import scala.concurrent.ExecutionContext.Implicits.global
      val position = vehicle.Position
      temporaryInterference = temporaryInterference :+ (position, vehicle.Faction, vehicle.Definition)
      context.system.scheduler.scheduleOnce(
        vehicle.Definition.DeployTime.milliseconds,
        self,
        ZoneVehicleActor.ClearInterference(position)
      )
      vehicle.Actor.tell(Deployment.TryDeploymentChange(toDeployState), sender())

    case Zone.Vehicle.TryDeploymentChange(vehicle, toDeployState) =>
      vehicle.Actor.tell(Deployment.TryDeploymentChange(toDeployState), sender())

    case Zone.Vehicle.HasDespawned(_, _) => ()

    case Zone.Vehicle.CanNotDespawn(_, _, _) => ()

    case Zone.Vehicle.CanNotDeploy(_, vehicle, _, _) => ()
      val pos = vehicle.Position
      temporaryInterference = temporaryInterference.filterNot(_._1 == pos)

    case ZoneVehicleActor.ClearInterference(pos) =>
      temporaryInterference = temporaryInterference.filterNot(_._1 == pos)

    case _ => ()
  }
}

object ZoneVehicleActor {
  private case class ClearInterference(pos: Vector3)

  @tailrec final def recursiveFindVehicle(iter: Iterator[Vehicle], target: Vehicle, index: Int = 0): Option[Int] = {
    if (!iter.hasNext) {
      None
    } else {
      if (iter.next().equals(target)) {
        Some(index)
      } else {
        recursiveFindVehicle(iter, target, index + 1)
      }
    }
  }

  private def temporaryInterferenceTest(
                                         vehicle: Vehicle,
                                         existingInterferences: Seq[(Vector3, PlanetSideEmpire.Value, VehicleDefinition)]
                                       ): Boolean = {
    val vPosition = vehicle.Position
    val vFaction = vehicle.Faction
    val vDefinition = vehicle.Definition
    existingInterferences
      .collect { case (p, faction, d) if faction == vFaction => (p, d) }
      .exists { case (position, definition) =>
        val interference = definition.interference
        (interference ne Interference.AllowAll) && {
          lazy val distanceSq = Vector3.DistanceSquared(position, vPosition)
          definition == vDefinition && distanceSq < interference.main * interference.main
        }
      }
  }
}
