package net.psforever.objects.serverobject.painbox

import akka.actor.Cancellable
import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.structures.{Building, PoweredAmenityControl}
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.etc.PainboxReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.{Default, GlobalDefinitions, Player}
import net.psforever.types.{PlanetSideEmpire, Vector3}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class PainboxControl(painbox: Painbox) extends PoweredAmenityControl {
  private[this] val log         = org.log4s.getLogger(s"Painbox")
  var painboxTick: Cancellable  = Default.Cancellable
  var nearestDoor: Option[Door] = None
  var domain: PainboxControl.Shape = PainboxControl.Unshaped()
  var disabled = false

  def initialStartup(): Unit = {
    if (painbox.Owner.Continent.matches("c[0-9]")) {
      //are we in a safe zone?
      // todo: handle non-radius painboxes in caverns properly
      log.debug(s"Skipping initialization of ${painbox.GUID} on ${painbox.Owner.Continent} - ${painbox.Position}")
      disabled = true
    } else {
      if (painbox.Definition.HasNearestDoorDependency) {
        //whether an open door summons the pain
        (painbox.Owner match {
          case obj : Building =>
            obj.Amenities
              .collect { case door : Door => door }
              .sortBy(door => Vector3.DistanceSquared(painbox.Position, door.Position))
              .headOption
          case _ =>
            None
        }) match {
          case door@Some(_) =>
            nearestDoor = door
          case _ =>
            log.error(
              s"Painbox ${painbox.GUID} on ${painbox.Owner.Continent} - ${painbox.Position} can not find a door that it is dependent on"
            )
            disabled = true
        }
      }
      //the region the painbox endangers
      domain = painbox.Definition.innateDamage match {
        case Some(properties) if properties.DamageRadius > 0f =>
          PainboxControl.Spherical(painbox.Position + painbox.Definition.sphereOffset, properties.DamageRadius)
        case _ =>
          PainboxControl.Box(painbox)
      }
    }
  }

  var commonBehavior: Receive = {
    case "startup" =>
      if (!disabled && domain.midpoint == Vector3.Zero) {
        initialStartup()
      }

    case Painbox.Stop() =>
      painboxTick.cancel()
      painboxTick = Default.Cancellable
  }

  def poweredStateLogic: Receive =
    commonBehavior
      .orElse {
        case Painbox.Start() if isPowered && !disabled =>
          painboxTick.cancel()
          painboxTick = context.system.scheduler.scheduleWithFixedDelay(0 seconds, 1 second, self, Painbox.Tick())

        case Painbox.Tick() =>
          //todo: Account for overlapping pain fields
          //todo: Pain module
          //todo: REK boosting
          val faction = painbox.Faction
          if (
            isPowered && faction != PlanetSideEmpire.NEUTRAL && (nearestDoor match {
              case Some(door) => door.Open.nonEmpty;
              case _          => true
            })
          ) {
            val pain = PainboxReason(painbox)
            domain
              .filterTargets(
                (painbox.Owner match {
                  case b: Building => b.PlayersInSOI
                  case _           => painbox.Zone.LivePlayers
                }).filter { p =>
                  p.Faction != faction && p.Health > 0
                }
              )
              .foreach { p =>
                p.Actor ! Vitality.Damage(DamageInteraction(SourceEntry(p), pain, p.Position).calculate())
              }
          }

        case _ => ;
      }

  def unpoweredStateLogic: Receive =
    commonBehavior
      .orElse {
        case _ => ;
      }

  def powerTurnOffCallback(): Unit = {
    self ! Painbox.Stop()
  }

  def powerTurnOnCallback(): Unit = {
    painbox.Owner match {
      case b: Building if b.PlayersInSOI.nonEmpty && !disabled =>
        self ! Painbox.Start()
      case _ => ;
    }
  }
}

object PainboxControl {
  sealed trait Shape {
    def midpoint: Vector3
    def filterTargets(available : List[Player]): List[Player]
  }

  final case class Unshaped() extends Shape {
    def midpoint: Vector3 = Vector3.Zero

    def filterTargets(available: List[Player]) : List[Player] = Nil
  }

  final case class Passthrough(midpoint: Vector3) extends Shape {

    def filterTargets(available: List[Player]) : List[Player] = available
  }

  final case class Spherical(midpoint: Vector3, radius: Float) extends Shape {
    def filterTargets(available: List[Player]): List[Player] = {
      available.filter { p =>
        Vector3.DistanceSquared(p.Position, midpoint) < radius
      }
    }
  }

  final case class Box(painbox: Painbox) extends Shape {
    private val (bBoxMinCorner, bBoxMaxCorner, bBoxMidPoint): (Vector3, Vector3, Vector3) = {
      painbox.Owner match {
        case obj : Building =>
          val planarRange = 16.5f
          val aboveRange = 5
          val belowRange = 5
          // Find amenities within the specified range
          val nearbyAmenities = obj.Amenities
            .filter(amenity =>
              amenity.Position != Vector3.Zero
              && (amenity.Definition == GlobalDefinitions.mb_locker
                  || amenity.Definition == GlobalDefinitions.respawn_tube
                  || amenity.Definition == GlobalDefinitions.spawn_terminal
                  || amenity.Definition == GlobalDefinitions.order_terminal
                  || amenity.Definition == GlobalDefinitions.door)
              && amenity.Position.x > painbox.Position.x - planarRange && amenity.Position.x < painbox.Position.x + planarRange
              && amenity.Position.y > painbox.Position.y - planarRange && amenity.Position.y < painbox.Position.y + planarRange
              && amenity.Position.z > painbox.Position.z - belowRange && amenity.Position.z < painbox.Position.z + aboveRange
            )
          // Calculate bounding box of amenities
          //0.5 is added/removed to ensure entirety of valid amenities were encompassed by field
          val min = Vector3(
            nearbyAmenities.minBy(_.Position.x).Position.x - 0.5f,
            nearbyAmenities.minBy(_.Position.y).Position.y - 0.5f,
            nearbyAmenities.minBy(_.Position.z).Position.z - 0.5f
          )
          val max = Vector3(
            nearbyAmenities.maxBy(_.Position.x).Position.x + 0.5f,
            nearbyAmenities.maxBy(_.Position.y).Position.y + 0.5f,
            painbox.Position.z + 0.5f
          )
          (min, max, Vector3.midpoint(min, max))
        case _ =>
          (Vector3.Zero, Vector3.Zero, painbox.Position)
      }
    }
    private val ownerRotZRadians = painbox.Owner.Orientation.z.toRadians

    def midpoint: Vector3 = bBoxMidPoint

    def filterTargets(available : List[Player]) : List[Player] = {
      available.filter { p =>
        /*
        This may be cpu intensive with a large number of players in SOI. Further performance tweaking may be required
        The bounding box is calculated aligned to the world XY axis, instead of rotating the painbox corners to match the base rotation
        we instead rotate the player's current coordinates to match the base rotation,
        allowing for much simplified checking of if the player is within the bounding box
        */
        val playerRot = Vector3.PlanarRotateAroundPoint(
          p.Position,
          bBoxMidPoint,
          ownerRotZRadians
        )
        bBoxMinCorner.x <= playerRot.x && playerRot.x <= bBoxMaxCorner.x &&
        bBoxMinCorner.y <= playerRot.y && playerRot.y <= bBoxMaxCorner.y &&
        bBoxMinCorner.z <= playerRot.z && playerRot.z <= bBoxMaxCorner.z
      }
    }
  }
}
