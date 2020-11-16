package net.psforever.objects.serverobject.painbox

import akka.actor.Cancellable
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.structures.{Building, PoweredAmenityControl}
import net.psforever.objects.{Default, GlobalDefinitions}
import net.psforever.types.{PlanetSideEmpire, Vector3}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class PainboxControl(painbox: Painbox) extends PoweredAmenityControl {
  private[this] val log         = org.log4s.getLogger(s"Painbox")
  var painboxTick: Cancellable  = Default.Cancellable
  var nearestDoor: Option[Door] = None
  var bBoxMinCorner             = Vector3.Zero
  var bBoxMaxCorner             = Vector3.Zero
  var bBoxMidPoint              = Vector3.Zero

  var disabled = false // Temporary to disable cavern non-radius fields

  def initialStartup(): Unit = {
    if (painbox.Definition.HasNearestDoorDependency) {
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
      }
    }
    else {
      if (painbox.Definition.Radius == 0f) {
        if (painbox.Owner.Continent.matches("c[0-9]")) {
          // todo: handle non-radius painboxes in caverns properly
          log.warn(s"Skipping initialization of ${painbox.GUID} on ${painbox.Owner.Continent} - ${painbox.Position}")
          disabled = true
        }
        else {
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
              bBoxMinCorner = Vector3(
                nearbyAmenities.minBy(amenity => amenity.Position.x).Position.x,
                nearbyAmenities.minBy(amenity => amenity.Position.y).Position.y,
                nearbyAmenities.minBy(x => x.Position.z).Position.z
              )
              bBoxMaxCorner = Vector3(
                nearbyAmenities.maxBy(amenity => amenity.Position.x).Position.x,
                nearbyAmenities.maxBy(amenity => amenity.Position.y).Position.y,
                painbox.Position.z
              )
              bBoxMidPoint = Vector3(
                (bBoxMinCorner.x + bBoxMaxCorner.x) / 2,
                (bBoxMinCorner.y + bBoxMaxCorner.y) / 2,
                (bBoxMinCorner.z + bBoxMaxCorner.z) / 2
              )
            case _ => None
          }
        }
      }
    }
    if (!disabled) {
      self ! BuildingActor.PowerOff()
    }
  }

  var commonBehavior: Receive = {
    case "startup" =>
      if (bBoxMidPoint == Vector3.Zero) {
        initialStartup()
      }

    case Painbox.Stop() =>
      painboxTick.cancel()
      painboxTick = Default.Cancellable
  }

  def poweredStateLogic: Receive =
    commonBehavior
      .orElse {
        case Painbox.Start() if isPowered =>
          painboxTick.cancel()
          painboxTick = context.system.scheduler.scheduleWithFixedDelay(0 seconds, 1 second, self, Painbox.Tick())

        case Painbox.Tick() =>
          //todo: Account for overlapping pain fields
          //todo: Pain module
          //todo: REK boosting
          val guid    = painbox.GUID
          val owner   = painbox.Owner.asInstanceOf[Building]
          val faction = owner.Faction
          if (
            isPowered && faction != PlanetSideEmpire.NEUTRAL && (nearestDoor match {
              case Some(door) => door.Open.nonEmpty;
              case _          => true
            })
          ) {
            val events   = painbox.Zone.AvatarEvents
            val damage   = painbox.Definition.Damage
            val radius   = painbox.Definition.Radius * painbox.Definition.Radius
            val position = painbox.Position

            if (painbox.Definition.Radius != 0f) {
              // Spherical pain field
              owner.PlayersInSOI
                .collect {
                  case p
                    if p.Faction != faction
                       && p.Health > 0
                       && Vector3.DistanceSquared(p.Position, position) < radius =>
                    events ! AvatarServiceMessage(p.Name, AvatarAction.EnvironmentalDamage(p.GUID, guid, damage))
                }
            } else {
              // Bounding box pain field
              owner.PlayersInSOI
                .collect {
                  case p
                    if p.Faction != faction
                       && p.Health > 0 =>
                    /*
                     This may be cpu intensive with a large number of players in SOI. Further performance tweaking may be required
                     The bounding box is calculated aligned to the world XY axis, instead of rotating the painbox corners to match the base rotation
                     we instead rotate the player's current coordinates to match the base rotation, which allows for much simplified checking of if the player is
                     within the bounding box
                     */
                    val playerRot =
                      Vector3.PlanarRotateAroundPoint(p.Position, bBoxMidPoint, painbox.Owner.Orientation.z.toRadians)
                    if (
                      bBoxMinCorner.x <= playerRot.x && playerRot.x <= bBoxMaxCorner.x && bBoxMinCorner.y <= playerRot.y && playerRot.y <= bBoxMaxCorner.y
                      && playerRot.z >= bBoxMinCorner.z && playerRot.z <= bBoxMaxCorner.z
                    ) {
                      events ! AvatarServiceMessage(p.Name, AvatarAction.EnvironmentalDamage(p.GUID, guid, damage))
                    }
                }
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
      case b: Building if b.PlayersInSOI.nonEmpty =>
        self ! Painbox.Start()
      case _ => ;
    }
  }
}
