// Copyright (c) 2017-2020 PSForever
package net.psforever.objects.vehicles

import akka.actor.{Actor, Cancellable}
import net.psforever.objects._
import net.psforever.objects.ballistics.{ResolvedProjectile, VehicleSource}
import net.psforever.objects.ce.TelepadLike
import net.psforever.objects.equipment.{Equipment, EquipmentSlot, JammableMountedWeapons}
import net.psforever.objects.guid.GUIDTask
import net.psforever.objects.inventory.{GridInventory, InventoryItem}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.containable.{Containable, ContainableBehavior}
import net.psforever.objects.serverobject.damage.{AggravatedBehavior, DamageableVehicle}
import net.psforever.objects.serverobject.deploy.Deployment.DeploymentObject
import net.psforever.objects.serverobject.deploy.{Deployment, DeploymentBehavior}
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.transfer.TransferBehavior
import net.psforever.objects.serverobject.repair.RepairableVehicle
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.vital.VehicleShieldCharge
import net.psforever.objects.zones.Zone
import net.psforever.packet.game._
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.types.{DriveState, ExoSuitType, PlanetSideGUID, Vector3}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles messages being dispatched to a specific `Vehicle`.<br>
  * <br>
  * Vehicle-controlling actors have two behavioral states - responsive and "`Disabled`."
  * The latter is applicable only when the specific vehicle is being deconstructed.
  *
  * @param vehicle the `Vehicle` object being governed
  */
class VehicleControl(vehicle: Vehicle)
    extends Actor
    with FactionAffinityBehavior.Check
    with DeploymentBehavior
    with MountableBehavior.Mount
    with MountableBehavior.Dismount
    with CargoBehavior
    with DamageableVehicle
    with RepairableVehicle
    with JammableMountedWeapons
    with ContainableBehavior
    with AntTransferBehavior
    with AggravatedBehavior {

  //make control actors belonging to utilities when making control actor belonging to vehicle
  vehicle.Utilities.foreach({ case (_, util) => util.Setup })
  
  def MountableObject = vehicle

  def CargoObject = vehicle

  def JammableObject = vehicle

  def FactionObject = vehicle

  def DeploymentObject = vehicle

  def DamageableObject = vehicle

  def RepairableObject = vehicle

  def ContainerObject = vehicle

  def ChargeTransferObject = vehicle

  if(vehicle.Definition == GlobalDefinitions.ant) {
    findChargeTargetFunc = Vehicles.FindANTChargingSource
    findDischargeTargetFunc = Vehicles.FindANTDischargingTarget
  }

  /** cheap flag for whether the vehicle is decaying */
  var decaying: Boolean = false

  /** primary vehicle decay timer */
  var decayTimer: Cancellable = Default.Cancellable

  def receive: Receive = Enabled

  override def postStop(): Unit = {
    super.postStop()
    damageableVehiclePostStop()
    decaying = false
    decayTimer.cancel()
    vehicle.Utilities.values.foreach { util =>
      context.stop(util().Actor)
      util().Actor = Default.Actor
    }
  }

  def Enabled: Receive =
    checkBehavior
      .orElse(deployBehavior)
      .orElse(cargoBehavior)
      .orElse(jammableBehavior)
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse(containerBehavior)
      .orElse(antBehavior)
      .orElse {
        case Vehicle.Ownership(None) =>
          LoseOwnership()

        case Vehicle.Ownership(Some(player)) =>
          GainOwnership(player)

        case msg @ Mountable.TryMount(player, seat_num) =>
          tryMountBehavior.apply(msg)
          val obj = MountableObject
          //check that the player has actually been sat in the expected seat
          if (obj.PassengerInSeat(player).contains(seat_num)) {
            //if the driver seat, change ownership
            if (seat_num == 0 && !obj.OwnerName.contains(player.Name)) {
              //whatever vehicle was previously owned
              vehicle.Zone.GUID(player.avatar.vehicle) match {
                case Some(v: Vehicle) =>
                  v.Actor ! Vehicle.Ownership(None)
                case _ =>
                  player.avatar.vehicle = None
              }
              LoseOwnership()       //lose our current ownership
              GainOwnership(player) //gain new ownership
            } else {
              decaying = false
              decayTimer.cancel()
            }
          }

        case msg: Mountable.TryDismount =>
          dismountBehavior.apply(msg)
          val obj = MountableObject

          // Reset velocity to zero when driver dismounts, to allow jacking/repair if vehicle was moving slightly before dismount
          if (!obj.Seats(0).isOccupied) {
            obj.Velocity = Some(Vector3.Zero)
          }
          //are we already decaying? are we unowned? is no one seated anywhere?
          if (!decaying && obj.Owner.isEmpty && obj.Seats.values.forall(!_.isOccupied)) {
            decaying = true
            decayTimer = context.system.scheduler.scheduleOnce(
              MountableObject.Definition.DeconstructionTime.getOrElse(5 minutes),
              self,
              VehicleControl.PrepareForDeletion()
            )
          }

        case Vehicle.ChargeShields(amount) =>
          val now: Long = System.nanoTime
          //make certain vehicle doesn't charge shields too quickly
          if (
            vehicle.Health > 0 && vehicle.Shields < vehicle.MaxShields &&
            !vehicle.History.exists(VehicleControl.LastShieldChargeOrDamage(now))
          ) {
            vehicle.History(VehicleShieldCharge(VehicleSource(vehicle), amount))
            vehicle.Shields = vehicle.Shields + amount
            vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
              s"${vehicle.Actor}",
              VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), vehicle.GUID, 68, vehicle.Shields)
            )
          }

        case FactionAffinity.ConvertFactionAffinity(faction) =>
          val originalAffinity = vehicle.Faction
          if (originalAffinity != (vehicle.Faction = faction)) {
            vehicle.Utilities.foreach({
              case (_: Int, util: Utility) => util().Actor forward FactionAffinity.ConfirmFactionAffinity()
            })
          }
          sender() ! FactionAffinity.AssertFactionAffinity(vehicle, faction)

        case CommonMessages.Use(player, Some(item: SimpleItem))
            if item.Definition == GlobalDefinitions.remote_electronics_kit =>
          //TODO setup certifications check
          if (vehicle.Faction != player.Faction) {
            sender() ! CommonMessages.Progress(
              GenericHackables.GetHackSpeed(player, vehicle),
              Vehicles.FinishHackingVehicle(vehicle, player, 3212836864L),
              GenericHackables.HackingTickAction(progressType = 1, player, vehicle, item.GUID)
            )
          }

        case Terminal.TerminalMessage(player, msg, reply) =>
          reply match {
            case Terminal.VehicleLoadout(definition, weapons, inventory) =>
              org.log4s
                .getLogger(vehicle.Definition.Name)
                .info(s"changing vehicle equipment loadout to ${player.Name}'s option #${msg.unk1 + 1}")
              //remove old inventory
              val oldInventory = vehicle.Inventory.Clear().map { case InventoryItem(obj, _) => (obj, obj.GUID) }
              //"dropped" items are lost; if it doesn't go in the trunk, it vanishes into the nanite cloud
              val (_, afterInventory) = inventory.partition(ContainableBehavior.DropPredicate(player))
              val (oldWeapons, newWeapons, finalInventory) = if (vehicle.Definition == definition) {
                //vehicles are the same type
                //TODO want to completely swap weapons, but holster icon vanishes temporarily after swap
                //TODO BFR arms must be swapped properly
//              //remove old weapons
//              val oldWeapons = vehicle.Weapons.values.collect { case slot if slot.Equipment.nonEmpty =>
//                val obj = slot.Equipment.get
//                slot.Equipment = None
//                (obj, obj.GUID)
//              }.toList
//              (oldWeapons, weapons, afterInventory)
                //TODO for now, just refill ammo; assume weapons stay the same
                vehicle.Weapons
                  .collect { case (_, slot: EquipmentSlot) if slot.Equipment.nonEmpty => slot.Equipment.get }
                  .collect {
                    case weapon: Tool =>
                      weapon.AmmoSlots.foreach { ammo => ammo.Box.Capacity = ammo.Box.Definition.Capacity }
                  }
                (Nil, Nil, afterInventory)
              } else {
                //vehicle loadout is not for this vehicle
                //do not transfer over weapon ammo
                if (
                  vehicle.Definition.TrunkSize == definition.TrunkSize && vehicle.Definition.TrunkOffset == definition.TrunkOffset
                ) {
                  (Nil, Nil, afterInventory) //trunk is the same dimensions, however
                } else {
                  //accommodate as much of inventory as possible
                  val (stow, _) = GridInventory.recoverInventory(afterInventory, vehicle.Inventory)
                  (Nil, Nil, stow)
                }
              }
              finalInventory.foreach { _.obj.Faction = vehicle.Faction }
              player.Zone.VehicleEvents ! VehicleServiceMessage(
                player.Zone.id,
                VehicleAction.ChangeLoadout(vehicle.GUID, oldWeapons, newWeapons, oldInventory, finalInventory)
              )
              player.Zone.AvatarEvents ! AvatarServiceMessage(
                player.Name,
                AvatarAction.TerminalOrderResult(msg.terminal_guid, msg.transaction_type, true)
              )

            case _ => ;
          }

        case Vehicle.Deconstruct(time) =>
          time match {
            case Some(delay) =>
              decaying = true
              decayTimer.cancel()
              decayTimer = context.system.scheduler.scheduleOnce(delay, self, VehicleControl.PrepareForDeletion())
            case _ =>
              PrepareForDeletion()
          }

        case VehicleControl.PrepareForDeletion() =>
          PrepareForDeletion()

        case _ => ;
      }

  val tryMountBehavior: Receive = {
    case msg @ Mountable.TryMount(user, seat_num) =>
      val exosuit     = user.ExoSuit
      val restriction = vehicle.Seats(seat_num).ArmorRestriction
      val seatGroup   = vehicle.SeatPermissionGroup(seat_num).getOrElse(AccessPermissionGroup.Passenger)
      val permission  = vehicle.PermissionGroup(seatGroup.id).getOrElse(VehicleLockState.Empire)
      if (
        (if (seatGroup == AccessPermissionGroup.Driver) {
           vehicle.Owner.contains(user.GUID) || vehicle.Owner.isEmpty || permission != VehicleLockState.Locked
         } else {
           permission != VehicleLockState.Locked
         }) &&
        (exosuit match {
          case ExoSuitType.MAX        => restriction == SeatArmorRestriction.MaxOnly
          case ExoSuitType.Reinforced => restriction == SeatArmorRestriction.NoMax
          case _                      => restriction != SeatArmorRestriction.MaxOnly
        })
      ) {
        mountBehavior.apply(msg)
      } else {
        sender() ! Mountable.MountMessages(user, Mountable.CanNotMount(vehicle, seat_num))
      }
  }

  def PrepareForDeletion(): Unit = {
    decaying = false
    val guid   = vehicle.GUID
    val zone   = vehicle.Zone
    val zoneId = zone.id
    val events = zone.VehicleEvents
    //miscellaneous changes
    Vehicles.BeforeUnloadVehicle(vehicle, zone)
    //become disabled
    context.become(Disabled)
    //cancel jammed behavior
    CancelJammeredSound(vehicle)
    CancelJammeredStatus(vehicle)
    //escape being someone else's cargo
    vehicle.MountedIn match {
      case Some(_) =>
        CargoBehavior.HandleVehicleCargoDismount(
          zone,
          guid,
          bailed = false,
          requestedByPassenger = false,
          kicked = false
        )
      case _ => ;
    }
    //kick all passengers
    vehicle.Seats.values.foreach(seat => {
      seat.Occupant match {
        case Some(player) =>
          seat.Occupant = None
          player.VehicleSeated = None
          if (player.HasGUID) {
            events ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(player.GUID, 4, false, guid))
          }
        case None => ;
      }
      //abandon all cargo
      vehicle.CargoHolds.values
        .collect {
          case hold if hold.isOccupied =>
            val cargo = hold.Occupant.get
            CargoBehavior.HandleVehicleCargoDismount(
              cargo.GUID,
              cargo,
              guid,
              vehicle,
              bailed = false,
              requestedByPassenger = false,
              kicked = false
            )
        }
    })
    //unregister
    zone.tasks ! GUIDTask.UnregisterVehicle(vehicle)(zone.GUID)
    //banished to the shadow realm
    vehicle.Position = Vector3.Zero
    //queue final deletion
    decayTimer = context.system.scheduler.scheduleOnce(5 seconds, self, VehicleControl.Deletion())
  }

  def Disabled: Receive =
    checkBehavior
      .orElse {
        case msg: Deployment.TryUndeploy =>
          deployBehavior.apply(msg)

        case VehicleControl.Deletion() =>
          val zone = vehicle.Zone
          zone.VehicleEvents ! VehicleServiceMessage(
            zone.id,
            VehicleAction.UnloadVehicle(Service.defaultPlayerGUID, vehicle, vehicle.GUID)
          )
          zone.Transport ! Zone.Vehicle.Despawn(vehicle)
        case _ =>
      }

  override def TryJammerEffectActivate(target: Any, cause: ResolvedProjectile): Unit = {
    if (vehicle.MountedIn.isEmpty) {
      super.TryJammerEffectActivate(target, cause)
    }
  }

  def LoseOwnership(): Unit = {
    val obj = MountableObject
    Vehicles.Disown(obj.GUID, obj)
    if (!decaying && obj.Seats.values.forall(!_.isOccupied)) {
      decaying = true
      decayTimer = context.system.scheduler.scheduleOnce(
        obj.Definition.DeconstructionTime.getOrElse(5 minutes),
        self,
        VehicleControl.PrepareForDeletion()
      )
    }
  }

  def GainOwnership(player: Player): Unit = {
    Vehicles.Own(MountableObject, player) match {
      case Some(_) =>
        decaying = false
        decayTimer.cancel()
      case None => ;
    }
  }

  def MessageDeferredCallback(msg: Any): Unit = {
    msg match {
      case Containable.MoveItem(_, item, _) =>
        //momentarily put item back where it was originally
        val obj = ContainerObject
        obj.Find(item) match {
          case Some(slot) =>
            obj.Zone.AvatarEvents ! AvatarServiceMessage(
              self.toString,
              AvatarAction.SendResponse(Service.defaultPlayerGUID, ObjectAttachMessage(obj.GUID, item.GUID, slot))
            )
          case None => ;
        }
      case _ => ;
    }
  }

  def RemoveItemFromSlotCallback(item: Equipment, slot: Int): Unit = {
    val zone = ContainerObject.Zone
    zone.VehicleEvents ! VehicleServiceMessage(
      self.toString,
      VehicleAction.UnstowEquipment(Service.defaultPlayerGUID, item.GUID)
    )
  }

  def PutItemInSlotCallback(item: Equipment, slot: Int): Unit = {
    val obj        = ContainerObject
    val oguid      = obj.GUID
    val zone       = obj.Zone
    val channel    = self.toString
    val events     = zone.VehicleEvents
    val iguid      = item.GUID
    val definition = item.Definition
    item.Faction = obj.Faction
    events ! VehicleServiceMessage(
      //TODO when a new weapon, the equipment slot ui goes blank, but the weapon functions; remount vehicle to correct it
      if (obj.VisibleSlots.contains(slot)) zone.id else channel,
      VehicleAction.SendResponse(
        Service.defaultPlayerGUID,
        ObjectCreateMessage(
          definition.ObjectId,
          iguid,
          ObjectCreateMessageParent(oguid, slot),
          definition.Packet.ConstructorData(item).get
        )
      )
    )
    item match {
      case box: AmmoBox =>
        events ! VehicleServiceMessage(
          channel,
          VehicleAction.InventoryState2(Service.defaultPlayerGUID, iguid, oguid, box.Capacity)
        )
      case weapon: Tool =>
        weapon.AmmoSlots.map { slot => slot.Box }.foreach { box =>
          events ! VehicleServiceMessage(
            channel,
            VehicleAction.InventoryState2(Service.defaultPlayerGUID, iguid, weapon.GUID, box.Capacity)
          )
        }
      case _ => ;
    }
  }

  def SwapItemCallback(item: Equipment, fromSlot: Int): Unit = {
    val obj  = ContainerObject
    val zone = obj.Zone
    val toChannel = if (obj.VisibleSlots.contains(fromSlot)) zone.id else self.toString
    zone.VehicleEvents ! VehicleServiceMessage(
      toChannel,
      VehicleAction.ObjectDelete(Service.defaultPlayerGUID, item.GUID)
    )
  }

  override def TryDeploymentChange(obj: Deployment.DeploymentObject, state: DriveState.Value): Boolean = {
    VehicleControl.DeploymentAngleCheck(obj) && super.TryDeploymentChange(obj, state)
  }

  override def DeploymentAction(
      obj: DeploymentObject,
      state: DriveState.Value,
      prevState: DriveState.Value
  ): DriveState.Value = {
    val out = super.DeploymentAction(obj, state, prevState)
    obj match {
      case vehicle: Vehicle =>
        val guid        = vehicle.GUID
        val zone        = vehicle.Zone
        val zoneChannel = zone.id
        val GUID0       = Service.defaultPlayerGUID
        val driverChannel = vehicle.Seats(0).Occupant match {
          case Some(tplayer) => tplayer.Name
          case None          => ""
        }
        Vehicles.ReloadAccessPermissions(vehicle, vehicle.Faction.toString)
        //ams
        if (vehicle.Definition == GlobalDefinitions.ams) {
          val events = zone.VehicleEvents
          state match {
            case DriveState.Deployed =>
              events ! VehicleServiceMessage.AMSDeploymentChange(zone)
              events ! VehicleServiceMessage(driverChannel, VehicleAction.PlanetsideAttribute(GUID0, guid, 81, 1))
            case _ => ;
          }
        }
        //ant
        else if (vehicle.Definition == GlobalDefinitions.ant) {
          state match {
            case DriveState.Deployed =>
              // Start ntu regeneration
              // If vehicle sends UseItemMessage with silo as target NTU regeneration will be disabled and orb particles will be disabled
              context.system.scheduler.scheduleOnce(
                delay = 1000 milliseconds,
                vehicle.Actor,
                TransferBehavior.Charging(Ntu.Nanites)
              )
            case _ => ;
          }
        }
        //router
        else if (vehicle.Definition == GlobalDefinitions.router) {
          val events = zone.LocalEvents
          state match {
            case DriveState.Deploying =>
              vehicle.Utility(UtilityType.internal_router_telepad_deployable) match {
                case Some(util: Utility.InternalTelepad) =>
                  util.Active = true
                case _ =>
                //log.warn(s"DeploymentActivities: could not find internal telepad in router@${vehicle.GUID.guid} while $state")
              }
            case DriveState.Deployed =>
              //let the timer do all the work
              events ! LocalServiceMessage(
                zoneChannel,
                LocalAction.ToggleTeleportSystem(GUID0, vehicle, TelepadLike.AppraiseTeleportationSystem(vehicle, zone))
              )
            case _ => ;
          }
        }
      case _ => ;
    }
    out
  }

  override def UndeploymentAction(
      obj: DeploymentObject,
      state: DriveState.Value,
      prevState: DriveState.Value
  ): DriveState.Value = {
    val out = if (decaying) state else super.UndeploymentAction(obj, state, prevState)
    obj match {
      case vehicle: Vehicle =>
        val guid  = vehicle.GUID
        val zone  = vehicle.Zone
        val GUID0 = Service.defaultPlayerGUID
        val driverChannel = vehicle.Seats(0).Occupant match {
          case Some(tplayer) => tplayer.Name
          case None          => ""
        }
        Vehicles.ReloadAccessPermissions(vehicle, vehicle.Faction.toString)
        //ams
        if (vehicle.Definition == GlobalDefinitions.ams) {
          val events = zone.VehicleEvents
          state match {
            case DriveState.Undeploying =>
              events ! VehicleServiceMessage.AMSDeploymentChange(zone)
              events ! VehicleServiceMessage(driverChannel, VehicleAction.PlanetsideAttribute(GUID0, guid, 81, 0))
            case _ => ;
          }
        }
        //ant
        else if (vehicle.Definition == GlobalDefinitions.ant) {
          state match {
            case DriveState.Undeploying =>
              TryStopChargingEvent(vehicle)
            case _ => ;
          }
        }
        //router
        else if (vehicle.Definition == GlobalDefinitions.router) {
          state match {
            case DriveState.Undeploying =>
              //deactivate internal router before trying to reset the system
              Vehicles.RemoveTelepads(vehicle)
              zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.ToggleTeleportSystem(GUID0, vehicle, None))
            case _ => ;
          }
        }
      case _ => ;
    }
    out
  }
}

object VehicleControl {
  import net.psforever.objects.vital.{DamageFromProjectile, VehicleShieldCharge, VitalsActivity}
  import scala.concurrent.duration._

  private case class PrepareForDeletion()

  private case class Deletion()

  /**
    * Determine if a given activity entry would invalidate the act of charging vehicle shields this tick.
    * @param now the current time (in nanoseconds)
    * @param act a `VitalsActivity` entry to test
    * @return `true`, if the vehicle took damage in the last five seconds or
    *        charged shields in the last second;
    *        `false`, otherwise
    */
  def LastShieldChargeOrDamage(now: Long)(act: VitalsActivity): Boolean = {
    act match {
      case DamageFromProjectile(data) => now - data.hit_time < (5 seconds).toNanos //damage delays next charge by 5s
      case vsc: VehicleShieldCharge   => now - vsc.time < (1 seconds).toNanos      //previous charge delays next by 1s
      case _                          => false
    }
  }

  def DeploymentAngleCheck(obj: Deployment.DeploymentObject): Boolean = {
    obj.Orientation.x <= 30 || obj.Orientation.x >= 330
  }
}
