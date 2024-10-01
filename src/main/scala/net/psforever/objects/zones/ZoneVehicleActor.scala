// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.definition.{ObjectDefinition, VehicleDefinition}
import net.psforever.objects.serverobject.deploy.{Deployment, Interference}
import net.psforever.objects.vital.InGameHistory
import net.psforever.objects.{Default, Vehicle}
import net.psforever.packet.game.ChatMsg
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{ChatMessageType, DriveState, PlanetSideEmpire, Vector3}

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
  private val log = org.log4s.getLogger(s"${zone.id}-vehicles")

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

    case Zone.Vehicle.TryDeploymentChange(vehicle, DriveState.Deploying) =>
      if (ZoneVehicleActor.ReportOnInterferenceResults(
        zone,
        vehicle,
        ZoneVehicleActor.temporaryInterferenceTest(vehicle, temporaryInterference) ++
          Interference.Test(zone, vehicle).map(_.Definition)
      )) {
        sender() ! Zone.Vehicle.CanNotDeploy(zone, vehicle, DriveState.Deploying, "blocked by a nearby entity")
      } else {
        tryAddToInterferenceField(vehicle.Position, vehicle.Faction, vehicle.Definition)
        vehicle.Actor.tell(Deployment.TryDeploymentChange(DriveState.Deploying), sender())
      }

    case Zone.Vehicle.TryDeploymentChange(vehicle, toDeployState) =>
      vehicle.Actor.tell(Deployment.TryDeploymentChange(toDeployState), sender())

    case Zone.Vehicle.HasDespawned(_, _) => ()

    case Zone.Vehicle.CanNotDespawn(_, _, _) => ()

    case Zone.Vehicle.CanNotDeploy(_, vehicle, DriveState.Deploying, reason) =>
      ZoneVehicleActor.ReportOnInterferenceResults(
        zone,
        vehicle,
        ZoneVehicleActor.temporaryInterferenceTest(vehicle, temporaryInterference) ++
          Interference.Test(zone, vehicle).map(_.Definition)
      )
      val pos = vehicle.Position
      val driverMoniker = vehicle.Seats.headOption.flatMap(_._2.occupant).map(_.Name).getOrElse("Driver")
      log.warn(s"$driverMoniker's ${vehicle.Definition.Name} can not deploy in ${zone.id} because $reason")
      temporaryInterference = temporaryInterference.filterNot(_._1 == pos)

    case Zone.Vehicle.CanNotDeploy(_, vehicle, _, reason) =>
      val pos = vehicle.Position
      val driverMoniker = vehicle.Seats.headOption.flatMap(_._2.occupant).map(_.Name).getOrElse("Driver")
      log.warn(s"$driverMoniker's ${vehicle.Definition.Name} can not deploy in ${zone.id} because $reason")
      temporaryInterference = temporaryInterference.filterNot(_._1 == pos)

    case ZoneVehicleActor.ClearInterference(pos) =>
      temporaryInterference = temporaryInterference.filterNot(_._1 == pos)

    case _ => ()
  }

  private def tryAddToInterferenceField(
                                         position: Vector3,
                                         faction: PlanetSideEmpire.Value,
                                         definition: VehicleDefinition
                                       ): Boolean = {
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
    val causesInterference = definition.interference ne Interference.AllowAll
    if (causesInterference) {
      temporaryInterference = temporaryInterference :+ (position, faction, definition)
      context.system.scheduler.scheduleOnce(
        definition.DeployTime.milliseconds,
        self,
        ZoneVehicleActor.ClearInterference(position)
      )
    }
    causesInterference
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
                                       ): Seq[VehicleDefinition] = {
    val vPosition = vehicle.Position
    val vFaction = vehicle.Faction
    val vDefinition = vehicle.Definition
    if (vDefinition.interference eq Interference.AllowAll) {
      Nil
    } else {
      existingInterferences
        .collect { case (p, faction, d) if faction == vFaction => (p, d) }
        .filter { case (position, definition) =>
          val interference = definition.interference
          (interference ne Interference.AllowAll) && {
            lazy val distanceSq = Vector3.DistanceSquared(position, vPosition)
            definition == vDefinition && distanceSq < interference.main * interference.main
          }
        }
        .map(_._2)
    }
  }

  private def ReportOnInterferenceResults(
                                           zone: Zone,
                                           vehicle: Vehicle,
                                           reportedInterferenceList: Seq[ObjectDefinition]
                                         ): Boolean = {
    if (reportedInterferenceList.nonEmpty) {
      reportedInterferenceList
        .find(_.isInstanceOf[VehicleDefinition])
        .map { definition => s"@nodeploy_${definition.Name}" }
        .orElse {
          val sharedGroupId = vehicle.Definition.interference.sharedGroupId
          if (sharedGroupId > 0) {
            reportedInterferenceList
              .find(_.interference.sharedGroupId == sharedGroupId)
              .map(_ => "@nodeploy_sharedinterference")
          } else {
            None
          }
        }
        .foreach { msg =>
          zone.VehicleEvents ! VehicleServiceMessage(
            vehicle.Seats.headOption.flatMap(_._2.occupant).map(_.Name).getOrElse(""),
            VehicleAction.SendResponse(Service.defaultPlayerGUID, ChatMsg(ChatMessageType.UNK_227, msg))
          )
        }
      true
    } else {
      false
    }
  }
}
