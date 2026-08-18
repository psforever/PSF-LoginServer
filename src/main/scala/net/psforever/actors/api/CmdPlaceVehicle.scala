package net.psforever.actors.api

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{Actor, ActorRef}
import net.psforever.objects.guid.{GUIDTask, StraightforwardTask, TaskBundle, TaskWorkflow}
import net.psforever.objects.zones.Zone
import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.services.{InterstellarClusterService, ServiceManager}
import net.psforever.services.base.envelope.MessageEnvelope
import net.psforever.services.vehicle.VehicleAction
import net.psforever.types.{DriveState, PlanetSideEmpire, Vector3}
import net.psforever.util.TerrainHeight

import scala.collection.mutable.Map
import scala.concurrent.Future

/**
  * Place a vehicle into a continent from outside a session, for an administrator.
  *
  * Three shapes of the same operation:
  *
  *   - `vehicle:<name>` -- an ordinary vehicle, mobile, sitting on the ground;
  *   - `ams`            -- an AMS already in its deployed state, so it is immediately a spawn point;
  *   - `router`         -- a Router already deployed. A second click's coordinates may be supplied
  *                         as `telepadX`/`telepadY` and are accepted and echoed in the response, but
  *                         KNOWN GAP: nothing here yet constructs the matching `TelepadDeployable` or
  *                         links it via `TelepadLike.InitializeTelepadDeployable`, so the Router is
  *                         placed with no working telepad -- it is not yet the "useless without one"
  *                         pair the second click implies. Wiring that up is the next step here.
  *
  * Height and attitude are DERIVED, not supplied. The caller clicks a point on a 2D map, so it knows
  * only x and y; the ground height comes from the same terrain resource the orbital strike samples, and
  * pitch and roll come from the local slope so a vehicle sits on a hillside rather than through it.
  *
  * args: `<zoneId> <kind> <faction> <x> <y> [<telepadX> <telepadY>]`
  */
class CmdPlaceVehicle(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  private val zoneId: String  = args.headOption.getOrElse("")
  private val kind: String    = args.lift(1).getOrElse("")
  private val factionArg      = args.lift(2).getOrElse("")
  private val x: Float        = args.lift(3).flatMap(v => scala.util.Try(v.toFloat).toOption).getOrElse(-1f)
  private val y: Float        = args.lift(4).flatMap(v => scala.util.Try(v.toFloat).toOption).getOrElse(-1f)
  private val padX            = args.lift(5).flatMap(v => scala.util.Try(v.toFloat).toOption)
  private val padY            = args.lift(6).flatMap(v => scala.util.Try(v.toFloat).toOption)

  private val faction: Option[PlanetSideEmpire.Value] = factionArg.toUpperCase match {
    case "TR" => Some(PlanetSideEmpire.TR)
    case "NC" => Some(PlanetSideEmpire.NC)
    case "VS" => Some(PlanetSideEmpire.VS)
    case _    => None
  }

  /** The vehicle to build, and whether it should arrive already deployed. */
  private val plan: Option[(net.psforever.objects.definition.VehicleDefinition, Boolean)] = kind match {
    case "ams"    => Some((GlobalDefinitions.ams, true))
    case "router" => Some((GlobalDefinitions.router, true))
    case k if k.startsWith("vehicle:") =>
      CmdPlaceVehicle.Placeable.get(k.drop("vehicle:".length).toLowerCase).map(d => (d, false))
    case _ => None
  }

  override def preStart(): Unit = {
    if (zoneId.isEmpty) {
      fail("no zone given")
    } else if (faction.isEmpty) {
      fail("faction must be TR, NC or VS")
    } else if (plan.isEmpty) {
      fail(s"unknown vehicle '$kind'")
    } else if (x < 0 || y < 0) {
      fail("a position is required")
    } else {
      ServiceManager.receptionist ! Receptionist.Find(
        InterstellarClusterService.InterstellarClusterServiceKey,
        context.self
      )
    }
  }

  override def receive: Receive = {
    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      listings.head ! InterstellarClusterService.FilterZones(z => z.id.equalsIgnoreCase(zoneId), context.self)

    case InterstellarClusterService.ZonesResponse(zones) =>
      zones.headOption match {
        case None => fail(s"no such zone '$zoneId'")
        case Some(zone) => place(zone)
      }

    case default => log.error(s"Unexpected message $default")
  }

  private def place(zone: Zone): Unit = {
    val (definition, deployed) = plan.get
    val ground = TerrainHeight.sample(zone.id, x, y)

    if (ground.isEmpty) {
      fail(s"no terrain height data for '${zone.id}'")
      return
    }

    // "On terrain" is enforced by proximity to a structure rather than by a true containment test: the
    // caller gives a 2D point, and a point inside a facility's sphere of influence is a point where the
    // ground height is meaningless because a building occupies it. An AMS there would sit inside a wall.
    val insideStructure = zone.Buildings.values.exists { b =>
      val soi = b.Definition.SOIRadius.toFloat
      soi > 0 && Vector3.DistanceSquared(Vector3(x, y, 0), Vector3(b.Position.x, b.Position.y, 0)) < soi * soi
    }

    if (kind == "ams" && insideStructure) {
      fail("an AMS may only be deployed on open terrain, not inside a facility")
      return
    }

    val position = Vector3(x, y, ground.get)
    val vehicle  = Vehicle(definition)
    vehicle.Position = position
    vehicle.Orientation = slopeOrientation(zone.id, x, y)
    vehicle.Faction = faction.get
    if (deployed) {
      vehicle.DeploymentState = DriveState.Deployed
    }

    TaskWorkflow.execute(TaskBundleFor(zone, vehicle))

    val what = if (deployed) s"deployed ${definition.Name}" else definition.Name
    log.info(s"admin API placed a ${faction.get} $what in ${zone.id} at $position")

    val data = Map[String, Any]()
    data("zone_id") = zone.id
    data("kind") = kind
    data("faction") = factionArg.toUpperCase
    data("x") = x
    data("y") = y
    data("z") = ground.get
    data("deployed") = deployed
    data("inside_structure") = insideStructure
    padX.zip(padY).foreach { case (tx, ty) =>
      data("telepad_x") = tx
      data("telepad_y") = ty
    }
    context.parent ! CommandGoodResponse(s"placed a ${faction.get} $what in ${zone.id}\n", data)
    context.stop(self)
  }

  /**
    * Attitude matching the ground under the vehicle.
    *
    * The slope is read from the height field either side of the point, one cell out, and turned into
    * pitch and roll. Without this a vehicle placed on a hillside is level while the ground is not, so it
    * appears half-buried on the uphill side. Yaw is left at zero: there is no meaningful facing implied
    * by a click, and a caller that wants one can rotate afterwards.
    */
  private def slopeOrientation(zone: String, x: Float, y: Float): Vector3 = {
    val step = 8f
    val samples = for {
      (dx, dy) <- Seq((-step, 0f), (step, 0f), (0f, -step), (0f, step))
    } yield TerrainHeight.sample(zone, x + dx, y + dy)

    samples match {
      case Seq(Some(west), Some(east), Some(south), Some(north)) =>
        // Degrees of tilt across two cells. Negated on the x axis because the client's pitch is
        // positive nose-down.
        val pitch = -math.toDegrees(math.atan2(north - south, 2 * step)).toFloat
        val roll  = math.toDegrees(math.atan2(east - west, 2 * step)).toFloat
        Vector3(pitch, roll, 0f)
      case _ =>
        Vector3.Zero
    }
  }

  /** Registers a GUID for the vehicle then spawns and broadcasts it, as the spawn pads do. */
  private def TaskBundleFor(zone: Zone, vehicle: Vehicle) = {
    TaskBundle(
      new StraightforwardTask() {
        private val localVehicle = vehicle
        private val localZone    = zone

        override def description(): String = s"admin API places a ${localVehicle.Definition.Name}"

        def action(): Future[Any] = {
          localZone.Transport ! Zone.Vehicle.Spawn(localVehicle)
          localZone.VehicleEvents ! MessageEnvelope(
            localZone.id,
            VehicleAction.LoadVehicle(
              localVehicle,
              localVehicle.Definition.ObjectId,
              localVehicle.GUID,
              localVehicle.Definition.Packet.ConstructorData(localVehicle).get
            )
          )
          Future.successful(true)
        }
      },
      List(GUIDTask.registerVehicle(zone.GUID, vehicle))
    )
  }

  private def fail(message: String): Unit = {
    context.parent ! CommandErrorResponse(s"$message\n", Map[String, Any]())
    context.stop(self)
  }
}

private object CmdPlaceVehicle {
  /**
    * Vehicles an administrator may place, by internal name.
    *
    * A deliberate whitelist rather than a scan of every definition. `GlobalDefinitions` exposes no
    * collection to search, and even if it did, most vehicle-ish definitions are mounted turrets and
    * wreck variants that make no sense to drop on a hillside. This is the set a GM would actually want,
    * and it doubles as the list the portal's menu offers.
    */
  val Placeable: scala.collection.immutable.Map[String, net.psforever.objects.definition.VehicleDefinition] =
    scala.collection.immutable.Map(
      // ground
      "quadassault"           -> GlobalDefinitions.quadassault,
      "quadstealth"           -> GlobalDefinitions.quadstealth,
      "fury"                  -> GlobalDefinitions.fury,
      "lightning"             -> GlobalDefinitions.lightning,
      "skyguard"              -> GlobalDefinitions.skyguard,
      "prowler"               -> GlobalDefinitions.prowler,
      "vanguard"              -> GlobalDefinitions.vanguard,
      "magrider"              -> GlobalDefinitions.magrider,
      "apc_tr"                -> GlobalDefinitions.apc_tr,
      "apc_nc"                -> GlobalDefinitions.apc_nc,
      "apc_vs"                -> GlobalDefinitions.apc_vs,
      "ant"                   -> GlobalDefinitions.ant,
      "ams"                   -> GlobalDefinitions.ams,
      "router"                -> GlobalDefinitions.router,
      // air
      "mosquito"              -> GlobalDefinitions.mosquito,
      "lightgunship"          -> GlobalDefinitions.lightgunship,
      "wasp"                  -> GlobalDefinitions.wasp,
      "liberator"             -> GlobalDefinitions.liberator,
      "vulture"               -> GlobalDefinitions.vulture,
      "dropship"              -> GlobalDefinitions.dropship,
      "galaxy_gunship"        -> GlobalDefinitions.galaxy_gunship,
      "lodestar"              -> GlobalDefinitions.lodestar,
      "phantasm"              -> GlobalDefinitions.phantasm,
    )
}
