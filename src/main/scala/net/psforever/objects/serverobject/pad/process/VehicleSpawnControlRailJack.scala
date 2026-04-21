// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.Props
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.etc.{ExplodingEntityReason, VehicleSpawnReason}
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.Zone

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles vehicle spawning orders for a `VehicleSpawnPad`.
  * The basic `VehicleSpawnControl` is the root of a simple tree of "spawn control" objects that chain to each other.
  * Each object performs on (or more than one related) actions upon the vehicle order that was submitted.<br>
  * <br>
  * When the vehicle is added into the environment, it is attached to the spawn pad platform.
  * On cue, the trapdoor of the platform will open, and the vehicle will be raised on a railed platform.
  * @param pad the `VehicleSpawnPad` object being governed
  */
class VehicleSpawnControlRailJack(pad: VehicleSpawnPad) extends VehicleSpawnControlBase(pad) {
  def LogId = "-lifter"

  val seatDriver =
    context.actorOf(Props(classOf[VehicleSpawnControlSeatDriver], pad), s"${context.parent.path.name}-mount")

  def receive: Receive = {
    case order @ VehicleSpawnControl.Order(driver, vehicle) =>
      vehicle.MountedIn = pad.GUID
      Zone.serverSideDamage(
        pad.Zone,
        pad,
        VehicleSpawnControlRailJack.prepareSpawnExplosion(pad, SourceEntry(driver), SourceEntry(vehicle)),
        pad.Definition.killBox(pad, vehicle.Definition.CanFly),
        Zone.findAllTargets
      )
      pad.Zone.VehicleEvents ! VehicleSpawnPad.AttachToRails(vehicle, pad)
      context.system.scheduler.scheduleOnce(10 milliseconds, seatDriver, order)

    case msg @ (VehicleSpawnControl.ProcessControl.Reminder | VehicleSpawnControl.ProcessControl.GetNewOrder) =>
      context.parent ! msg

    case _ => ;
  }
}

object VehicleSpawnControlRailJack {
  def prepareSpawnExplosion(
                             pad: VehicleSpawnPad,
                             driver: SourceEntry,
                             vehicle: SourceEntry
                           ):
                           (
                             PlanetSideGameObject with FactionAffinity with Vitality,
                             PlanetSideGameObject with FactionAffinity with Vitality
                           ) => DamageInteraction = {
    vehicleSpawnExplosion(
      vehicle,
      pad.Definition.innateDamage.get,
      Some(DamageInteraction(
        SourceEntry(pad),
        VehicleSpawnReason(driver, vehicle),
        pad.Position
      ).calculate()(pad))
    )
  }

  def vehicleSpawnExplosion(
                             vehicle: SourceEntry,
                             properties: DamageProperties,
                             cause: Option[DamageResult]
                           )
                           (
                             source: PlanetSideGameObject with FactionAffinity with Vitality,
                             target: PlanetSideGameObject with FactionAffinity with Vitality
                           ): DamageInteraction = {
    DamageInteraction(
      SourceEntry(target),
      ExplodingEntityReason(
        vehicle,
        properties,
        target.DamageModel,
        cause
      ),
      target.Position
    )
  }
}
