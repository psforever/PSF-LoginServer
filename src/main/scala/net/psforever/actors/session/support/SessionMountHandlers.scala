// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, typed}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.interior.Sidedness.OutsideOf
import net.psforever.objects.{PlanetSideGameObject, Tool, Vehicle}
import net.psforever.objects.vehicles.{CargoBehavior, MountableWeapons}
import net.psforever.objects.vital.InGameHistory
import net.psforever.packet.game.{DismountVehicleCargoMsg, InventoryStateMessage, MountVehicleCargoMsg, MountVehicleMsg, ObjectAttachMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{BailType, PlanetSideGUID, Vector3}
//
import net.psforever.actors.session.AvatarActor
import net.psforever.objects.Player
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.packet.game.DismountVehicleMsg

import scala.concurrent.duration._

trait MountHandlerFunctions extends CommonSessionInterfacingFunctionality {
  val ops: SessionMountHandlers

  def handleMountVehicle(pkt: MountVehicleMsg): Unit

  def handleDismountVehicle(pkt: DismountVehicleMsg): Unit

  def handleMountVehicleCargo(pkt: MountVehicleCargoMsg): Unit

  def handleDismountVehicleCargo(pkt: DismountVehicleCargoMsg): Unit

  def handle(tplayer: Player, reply: Mountable.Exchange): Unit
}

class SessionMountHandlers(
                            val sessionLogic: SessionData,
                            val avatarActor: typed.ActorRef[AvatarActor.Command],
                            implicit val context: ActorContext
                          ) extends CommonSessionInterfacingFunctionality {
  def handleMountVehicle(pkt: MountVehicleMsg): Unit = {
    val MountVehicleMsg(_, mountable_guid, entry_point) = pkt
    sessionLogic.validObject(mountable_guid, decorator = "MountVehicle").collect {
      case obj: Mountable =>
        obj.Actor ! Mountable.TryMount(player, entry_point)
      case _ =>
        log.error(s"MountVehicleMsg: object ${mountable_guid.guid} not a mountable thing, ${player.Name}")
    }
  }

  def handleDismountVehicle(pkt: DismountVehicleMsg): Unit = {
    val DismountVehicleMsg(player_guid, bailType, wasKickedByDriver) = pkt
    val dError: (String, Player)=> Unit = dismountError(bailType, wasKickedByDriver)
    //TODO optimize this later
    //common warning for this section
    if (player.GUID == player_guid) {
      //normally disembarking from a mount
      (sessionLogic.zoning.interstellarFerry.orElse(continent.GUID(player.VehicleSeated)) match {
        case out @ Some(obj: Vehicle) =>
          continent.GUID(obj.MountedIn) match {
            case Some(_: Vehicle) => None //cargo vehicle
            case _                => out  //arrangement "may" be permissible
          }
        case out @ Some(_: Mountable) =>
          out
        case _ =>
          dError(s"DismountVehicleMsg: player ${player.Name} not considered seated in a mountable entity", player)
          None
      }) match {
        case Some(obj: Mountable) =>
          obj.PassengerInSeat(player) match {
            case Some(seat_num) =>
              obj.Actor ! Mountable.TryDismount(player, seat_num, bailType)
              //short-circuit the temporary channel for transferring between zones, the player is no longer doing that
              sessionLogic.zoning.interstellarFerry = None

            case None =>
              dError(s"DismountVehicleMsg: can not find where player ${player.Name}_guid is seated in mountable ${player.VehicleSeated}", player)
          }
        case _ =>
          dError(s"DismountVehicleMsg: can not find mountable entity ${player.VehicleSeated}", player)
      }
    } else {
      //kicking someone else out of a mount; need to own that mount/mountable
      val dWarn: (String, Player)=> Unit = dismountWarning(bailType, wasKickedByDriver)
      player.avatar.vehicle match {
        case Some(obj_guid) =>
          (
            (
              sessionLogic.validObject(obj_guid, decorator = "DismountVehicle/Vehicle"),
              sessionLogic.validObject(player_guid, decorator = "DismountVehicle/Player")
            ) match {
              case (vehicle @ Some(obj: Vehicle), tplayer) =>
                if (obj.MountedIn.isEmpty) (vehicle, tplayer) else (None, None)
              case (mount @ Some(_: Mountable), tplayer) =>
                (mount, tplayer)
              case _ =>
                (None, None)
            }) match {
            case (Some(obj: Mountable), Some(tplayer: Player)) =>
              obj.PassengerInSeat(tplayer) match {
                case Some(seat_num) =>
                  obj.Actor ! Mountable.TryDismount(tplayer, seat_num, bailType)
                case None =>
                  dError(s"DismountVehicleMsg: can not find where other player ${tplayer.Name} is seated in mountable $obj_guid", tplayer)
              }
            case (None, _) =>
              dWarn(s"DismountVehicleMsg: ${player.Name} can not find his vehicle", player)
            case (_, None) =>
              dWarn(s"DismountVehicleMsg: player $player_guid could not be found to kick, ${player.Name}", player)
            case _ =>
              dWarn(s"DismountVehicleMsg: object is either not a Mountable or not a Player", player)
          }
        case None =>
          dWarn(s"DismountVehicleMsg: ${player.Name} does not own a vehicle", player)
      }
    }
  }

  def handleMountVehicleCargo(pkt: MountVehicleCargoMsg): Unit = {
    val MountVehicleCargoMsg(_, cargo_guid, carrier_guid, _) = pkt
    (continent.GUID(cargo_guid), continent.GUID(carrier_guid)) match {
      case (Some(cargo: Vehicle), Some(carrier: Vehicle)) =>
        carrier.CargoHolds.find({ case (_, hold) => !hold.isOccupied }) match {
          case Some((mountPoint, _)) =>
            cargo.Actor ! CargoBehavior.StartCargoMounting(carrier_guid, mountPoint)
          case _ =>
            log.warn(
              s"MountVehicleCargoMsg: ${player.Name} trying to load cargo into a ${carrier.Definition.Name} which oes not have a cargo hold"
            )
        }
      case (None, _) | (Some(_), None) =>
        log.warn(
          s"MountVehicleCargoMsg: ${player.Name} lost a vehicle while working with cargo - either $carrier_guid or $cargo_guid"
        )
      case _ => ()
    }
  }

  def handleDismountVehicleCargo(pkt: DismountVehicleCargoMsg): Unit = {
    val DismountVehicleCargoMsg(_, cargo_guid, bailed, _, kicked) = pkt
    continent.GUID(cargo_guid) match {
      case Some(cargo: Vehicle) =>
        cargo.Actor ! CargoBehavior.StartCargoDismounting(bailed || kicked)
      case _ => ()
    }
  }

  private def dismountWarning(
                               bailAs: BailType.Value,
                               kickedByDriver: Boolean
                             )
                             (
                               note: String,
                               player: Player
                             ): Unit = {
    log.warn(note)
    player.VehicleSeated = None
    sendResponse(DismountVehicleMsg(player.GUID, bailAs, kickedByDriver))
  }

  private def dismountError(
                             bailAs: BailType.Value,
                             kickedByDriver: Boolean
                           )
                           (
                             note: String,
                             player: Player
                           ): Unit = {
    log.error(s"$note; some vehicle might not know that ${player.Name} is no longer sitting in it")
    player.VehicleSeated = None
    sendResponse(DismountVehicleMsg(player.GUID, bailAs, kickedByDriver))
  }

  /**
   * Common activities/procedure when a player mounts a valid object.
   * @param tplayer the player
   * @param obj the mountable object
   * @param seatNum the mount into which the player is mounting
   */
  def MountingAction(tplayer: Player, obj: PlanetSideGameObject with FactionAffinity with InGameHistory, seatNum: Int): Unit = {
    val playerGuid: PlanetSideGUID = tplayer.GUID
    val objGuid: PlanetSideGUID    = obj.GUID
    sessionLogic.actionsToCancel()
    avatarActor ! AvatarActor.DeactivateActiveImplants
    avatarActor ! AvatarActor.SuspendStaminaRegeneration(3.seconds)
    sendResponse(ObjectAttachMessage(objGuid, playerGuid, seatNum))
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.MountVehicle(playerGuid, objGuid, seatNum)
    )
  }

  /**
   * Common activities/procedure when a player dismounts a valid mountable object.
   * @param tplayer the player
   * @param obj the mountable object
   * @param seatNum the mount out of which which the player is disembarking
   */
  def DismountVehicleAction(tplayer: Player, obj: PlanetSideGameObject with FactionAffinity with InGameHistory, seatNum: Int): Unit = {
    DismountAction(tplayer, obj, seatNum)
    tplayer.WhichSide = OutsideOf
    //until vehicles maintain synchronized momentum without a driver
    obj match {
      case v: Vehicle
        if seatNum == 0 =>
        /*sessionLogic.vehicles.serverVehicleControlVelocity.collect { _ =>
          sessionLogic.vehicles.ServerVehicleOverrideStop(v)
        }*/
        v.Velocity = Vector3.Zero
        continent.VehicleEvents ! VehicleServiceMessage(
          continent.id,
          VehicleAction.VehicleState(
            tplayer.GUID,
            v.GUID,
            unk1 = 0,
            tplayer.Position,
            v.Orientation,
            v.Velocity,
            v.Flying,
            unk3 = 0,
            unk4 = 0,
            wheel_direction = 15,
            unk5 = false,
            unk6 = v.Cloaked
          )
        )
      case _ => ()
    }
  }

  /**
   * Common activities/procedure when a player dismounts a valid mountable object.
   * @param tplayer the player
   * @param obj the mountable object
   * @param seatNum the mount out of which which the player is disembarking
   */
  def DismountAction(tplayer: Player, obj: PlanetSideGameObject with FactionAffinity with InGameHistory, seatNum: Int): Unit = {
    val playerGuid: PlanetSideGUID = tplayer.GUID
    tplayer.ContributionFrom(obj)
    sessionLogic.keepAliveFunc = sessionLogic.zoning.NormalKeepAlive
    val bailType = if (tplayer.BailProtection) {
      BailType.Bailed
    } else {
      BailType.Normal
    }
    sendResponse(DismountVehicleMsg(playerGuid, bailType, wasKickedByDriver = false))
    continent.VehicleEvents ! VehicleServiceMessage(
      continent.id,
      VehicleAction.DismountVehicle(playerGuid, bailType, unk2 = false)
    )
  }

  /**
   * From a mount, find the weapon controlled from it, and update the ammunition counts for that weapon's magazines.
   * @param objWithSeat the object that owns seats (and weaponry)
   * @param seatNum the mount
   */
  def updateWeaponAtSeatPosition(objWithSeat: MountableWeapons, seatNum: Int): Unit = {
    objWithSeat.WeaponControlledFromSeat(seatNum) foreach {
      case weapon: Tool =>
        //update mounted weapon belonging to mount
        weapon.AmmoSlots.foreach(slot => {
          //update the magazine(s) in the weapon, specifically
          val magazine = slot.Box
          sendResponse(InventoryStateMessage(magazine.GUID, weapon.GUID, magazine.Capacity.toLong))
        })
      case _ => () //no weapons to update
    }
  }
}
