// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.Props
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.etc.{ExplodingEntityReason, VehicleSpawnReason}
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.zones.Zone
import net.psforever.types.Vector3

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

  val killBoxFunc: (PlanetSideGameObject, PlanetSideGameObject, Float) => Boolean =
    VehicleSpawnControlRailJack.prepareKillBox(pad)

  def receive: Receive = {
    case order @ VehicleSpawnControl.Order(driver, vehicle) =>
      vehicle.MountedIn = pad.GUID
      Zone.serverSideDamage(
        pad.Zone,
        pad,
        VehicleSpawnControlRailJack.prepareSpawnExplosion(pad, SourceEntry(driver), SourceEntry(vehicle)),
        killBoxFunc
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

  def prepareKillBox(pad: VehicleSpawnPad): (PlanetSideGameObject, PlanetSideGameObject, Float) => Boolean = {
    val forward = Vector3(0,1,0).Rz(pad.Orientation.z + pad.Definition.VehicleCreationZOrientOffset)
    vehicleSpawnKillBox(
      forward,
      Vector3.CrossProduct(forward, Vector3(0,0,1)),
      pad.Position,
      forwardLimit = 14,
      backLimit = 10,
      sideLimit = 7.5f,
      aboveLimit = 5
    )
  }

  protected def vehicleSpawnKillBox(
                                     forward: Vector3,
                                     side: Vector3,
                                     origin: Vector3,
                                     forwardLimit: Float,
                                     backLimit: Float,
                                     sideLimit: Float,
                                     aboveLimit: Float
                                   )
                                   (
                                     obj1: PlanetSideGameObject,
                                     obj2: PlanetSideGameObject,
                                     maxDistance: Float
                                   ): Boolean = {
    val dir: Vector3 = {
      val g2 = obj2.Definition.Geometry(obj2)
      val cdir = Vector3.Unit(origin - g2.center.asVector3)
      val point = g2.pointOnOutside(cdir).asVector3
      point - origin
    }
    val originZ = origin.z
    val obj2Z = obj2.Position.z
    originZ - 1 <= obj2Z && originZ + aboveLimit > obj2Z &&
    {
      val calculatedForwardDistance = Vector3.ScalarProjection(dir, forward)
      if (calculatedForwardDistance >= 0) {
        calculatedForwardDistance < forwardLimit
      }
      else {
        -calculatedForwardDistance < backLimit
      }
    } &&
    math.abs(Vector3.ScalarProjection(dir, side)) < sideLimit
  }
}
