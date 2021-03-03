// Copyright (c) 2017-2020 PSForever
package net.psforever.objects.vehicles

import akka.actor.{Actor, Cancellable}
import net.psforever.objects._
import net.psforever.objects.ballistics.VehicleSource
import net.psforever.objects.ce.TelepadLike
import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.equipment.{Equipment, EquipmentSlot, JammableMountedWeapons}
import net.psforever.objects.guid.GUIDTask
import net.psforever.objects.inventory.{GridInventory, InventoryItem}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.containable.{Containable, ContainableBehavior}
import net.psforever.objects.serverobject.damage.{AggravatedBehavior, DamageableVehicle}
import net.psforever.objects.serverobject.deploy.Deployment.DeploymentObject
import net.psforever.objects.serverobject.deploy.{Deployment, DeploymentBehavior}
import net.psforever.objects.serverobject.environment._
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.repair.RepairableVehicle
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.serverobject.transfer.TransferBehavior
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.VehicleShieldCharge
import net.psforever.objects.vital.environment.EnvironmentReason
import net.psforever.objects.vital.etc.SuicideReason
import net.psforever.objects.zones._
import net.psforever.packet.game._
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.types._
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
    with MountableBehavior
    with CargoBehavior
    with DamageableVehicle
    with RepairableVehicle
    with JammableMountedWeapons
    with ContainableBehavior
    with AntTransferBehavior
    with AggravatedBehavior
    with RespondsToZoneEnvironment {
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

  def InteractiveObject = vehicle
  SetInteraction(EnvironmentAttribute.Water, doInteractingWithWater)
  SetInteraction(EnvironmentAttribute.Lava, doInteractingWithLava)
  SetInteraction(EnvironmentAttribute.Death, doInteractingWithDeath)
  if (!vehicle.Definition.CanFly) { //can not recover from sinking disability
    SetInteractionStop(EnvironmentAttribute.Water, stopInteractingWithWater)
  }

  if (vehicle.Definition == GlobalDefinitions.ant) {
    findChargeTargetFunc = Vehicles.FindANTChargingSource
    findDischargeTargetFunc = Vehicles.FindANTDischargingTarget
  }
  /** cheap flag for whether the vehicle is decaying */
  var decaying : Boolean = false
  /** primary vehicle decay timer */
  var decayTimer : Cancellable = Default.Cancellable
  /** becoming waterlogged, or drying out? */
  var submergedCondition : Option[OxygenState] = None

  def receive : Receive = Enabled

  override def postStop() : Unit = {
    super.postStop()
    damageableVehiclePostStop()
    decaying = false
    decayTimer.cancel()
    vehicle.Utilities.values.foreach { util =>
      context.stop(util().Actor)
      util().Actor = Default.Actor
    }
    recoverFromEnvironmentInteracting()
  }

  def Enabled : Receive =
    checkBehavior
      .orElse(deployBehavior)
      .orElse(cargoBehavior)
      .orElse(jammableBehavior)
      .orElse(takesDamage)
      .orElse(canBeRepairedByNanoDispenser)
      .orElse(containerBehavior)
      .orElse(antBehavior)
      .orElse(environmentBehavior)
      .orElse {
        case Vehicle.Ownership(None) =>
          LoseOwnership()

        case Vehicle.Ownership(Some(player)) =>
          GainOwnership(player)

        case msg @ Mountable.TryMount(player, mount_point) =>
          mountBehavior.apply(msg)
          mountCleanup(mount_point, player)

        case msg @ Mountable.TryDismount(_, seat_num) =>
          dismountBehavior.apply(msg)
          dismountCleanup(seat_num)

        case Vehicle.ChargeShields(amount) =>
          val now : Long = System.currentTimeMillis()
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

        case Vehicle.UpdateZoneInteractionProgressUI(player) =>
          updateZoneInteractionProgressUI(player)

        case FactionAffinity.ConvertFactionAffinity(faction) =>
          val originalAffinity = vehicle.Faction
          if (originalAffinity != (vehicle.Faction = faction)) {
            vehicle.Utilities.foreach({
              case (_ : Int, util : Utility) => util().Actor forward FactionAffinity.ConfirmFactionAffinity()
            })
          }
          sender() ! FactionAffinity.AssertFactionAffinity(vehicle, faction)

        case CommonMessages.Use(player, Some(item : SimpleItem))
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
                  .collect { case (_, slot : EquipmentSlot) if slot.Equipment.nonEmpty => slot.Equipment.get }
                  .collect {
                    case weapon : Tool =>
                      weapon.AmmoSlots.foreach { ammo => ammo.Box.Capacity = ammo.Box.Definition.Capacity }
                  }
                (Nil, Nil, afterInventory)
              }
              else {
                //vehicle loadout is not for this vehicle
                //do not transfer over weapon ammo
                if (
                  vehicle.Definition.TrunkSize == definition.TrunkSize && vehicle.Definition.TrunkOffset == definition.TrunkOffset
                ) {
                  (Nil, Nil, afterInventory) //trunk is the same dimensions, however
                }
                else {
                  //accommodate as much of inventory as possible
                  val (stow, _) = GridInventory.recoverInventory(afterInventory, vehicle.Inventory)
                  (Nil, Nil, stow)
                }
              }
              finalInventory.foreach {
                _.obj.Faction = vehicle.Faction
              }
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

        case VehicleControl.Disable() =>
          PrepareForDisabled(kickPassengers = false)
          context.become(Disabled)

        case Vehicle.Deconstruct(time) =>
          time match {
            case Some(delay) if vehicle.Definition.undergoesDecay =>
              decaying = true
              decayTimer.cancel()
              decayTimer = context.system.scheduler.scheduleOnce(delay, self, VehicleControl.PrepareForDeletion())
            case _ =>
              PrepareForDisabled(kickPassengers = true)
              PrepareForDeletion()
              context.become(ReadyToDelete)
          }

        case VehicleControl.PrepareForDeletion() =>
          PrepareForDisabled(kickPassengers = true)
          PrepareForDeletion()
          context.become(ReadyToDelete)

        case VehicleControl.AssignOwnership(player) =>
          vehicle.AssignOwnership(player)

        case _ => ;
      }

  def Disabled : Receive =
    checkBehavior
      .orElse {
        case msg : Deployment.TryUndeploy =>
          deployBehavior.apply(msg)

        case msg @ Mountable.TryDismount(_, seat_num) =>
          dismountBehavior.apply(msg)
          dismountCleanup(seat_num)

        case Vehicle.Deconstruct(time) =>
          time match {
            case Some(delay) if vehicle.Definition.undergoesDecay =>
              decaying = true
              decayTimer.cancel()
              decayTimer = context.system.scheduler.scheduleOnce(delay, self, VehicleControl.PrepareForDeletion())
            case _ =>
              PrepareForDeletion()
              context.become(ReadyToDelete)
          }

        case VehicleControl.PrepareForDeletion() =>
          PrepareForDeletion()
          context.become(ReadyToDelete)

        case _ =>
      }

  def ReadyToDelete : Receive =
    checkBehavior
      .orElse {
        case msg : Deployment.TryUndeploy =>
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

  override protected def mountTest(
                                    obj: PlanetSideServerObject with Mountable,
                                    seatNumber: Int,
                                    user: Player
                                  ): Boolean = {
    val seatGroup = vehicle.SeatPermissionGroup(seatNumber).getOrElse(AccessPermissionGroup.Passenger)
    val permission = vehicle.PermissionGroup(seatGroup.id).getOrElse(VehicleLockState.Empire)
    (if (seatGroup == AccessPermissionGroup.Driver) {
      vehicle.Owner.contains(user.GUID) || vehicle.Owner.isEmpty || permission != VehicleLockState.Locked
    } else {
      permission != VehicleLockState.Locked
    }) &&
    super.mountTest(obj, seatNumber, user)
  }

  def mountCleanup(mount_point: Int, user: Player): Unit = {
    val obj = MountableObject
    obj.GetSeatFromMountPoint(mount_point) match {
      case Some(seatNumber) =>
        //check that the player has actually been sat in the expected mount
        if (obj.PassengerInSeat(user).contains(seatNumber)) {
          //if the driver mount, change ownership if that is permissible for this vehicle
          if (seatNumber == 0 && !obj.OwnerName.contains(user.Name) && obj.Definition.CanBeOwned.nonEmpty) {
            //whatever vehicle was previously owned
            vehicle.Zone.GUID(user.avatar.vehicle) match {
              case Some(v : Vehicle) =>
                v.Actor ! Vehicle.Ownership(None)
              case _ =>
                user.avatar.vehicle = None
            }
            LoseOwnership() //lose our current ownership
            GainOwnership(user) //gain new ownership
          }
          else {
            decaying = false
            decayTimer.cancel()
          }
          updateZoneInteractionProgressUI(user)
        }
      case _ =>
    }
  }

  override protected def dismountTest(
                                       obj: Mountable with WorldEntity,
                                       seatNumber: Int,
                                       user: Player
                                     ): Boolean = {
    vehicle.DeploymentState == DriveState.Deployed || super.dismountTest(obj, seatNumber, user)
  }

  def dismountCleanup(seatBeingDismounted: Int): Unit = {
    val obj = MountableObject
    // Reset velocity to zero when driver dismounts, to allow jacking/repair if vehicle was moving slightly before dismount
    if (!obj.Seats(0).isOccupied) {
      obj.Velocity = Some(Vector3.Zero)
    }
    if (!obj.Seats(seatBeingDismounted).isOccupied) { //seat was vacated
      //we were only owning the vehicle while we sat in its driver seat
      val canBeOwned = obj.Definition.CanBeOwned
      if (canBeOwned.contains(false) && seatBeingDismounted == 0) {
        LoseOwnership()
      }
      //are we already decaying? are we unowned? is no one seated anywhere?
      if (!decaying &&
          obj.Definition.undergoesDecay &&
          obj.Owner.isEmpty &&
          obj.Seats.values.forall(!_.isOccupied)) {
        decaying = true
        decayTimer = context.system.scheduler.scheduleOnce(
          MountableObject.Definition.DeconstructionTime.getOrElse(5 minutes),
          self,
          VehicleControl.PrepareForDeletion()
        )
      }
    }
  }

  def PrepareForDisabled(kickPassengers: Boolean) : Unit = {
    val guid = vehicle.GUID
    val zone = vehicle.Zone
    val zoneId = zone.id
    val events = zone.VehicleEvents
    //miscellaneous changes
    recoverFromEnvironmentInteracting()
    Vehicles.BeforeUnloadVehicle(vehicle, zone)
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
    if (!vehicle.Flying || kickPassengers) {
      //kick all passengers (either not flying, or being explicitly instructed)
      vehicle.Seats.values.foreach { seat =>
        seat.occupant match {
          case Some(player) =>
            seat.unmount(player)
            player.VehicleSeated = None
            if (player.HasGUID) {
              events ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(player.GUID, 4, false, guid))
            }
          case None => ;
        }
      }
    }
    //abandon all cargo
    vehicle.CargoHolds.values
      .collect {
        case hold if hold.isOccupied =>
          val cargo = hold.occupant.get
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
    }

  def PrepareForDeletion() : Unit = {
    decaying = false
    val zone = vehicle.Zone
    //miscellaneous changes
    Vehicles.BeforeUnloadVehicle(vehicle, zone)
    //cancel jammed behavior
    CancelJammeredSound(vehicle)
    CancelJammeredStatus(vehicle)
    //unregister
    zone.tasks ! GUIDTask.UnregisterVehicle(vehicle)(zone.GUID)
    //banished to the shadow realm
    vehicle.Position = Vector3.Zero
    //queue final deletion
    decayTimer = context.system.scheduler.scheduleOnce(5 seconds, self, VehicleControl.Deletion())
  }

  override def TryJammerEffectActivate(target: Any, cause: DamageResult): Unit = {
    if (vehicle.MountedIn.isEmpty) {
      super.TryJammerEffectActivate(target, cause)
    }
  }

  def LoseOwnership(): Unit = {
    val obj = MountableObject
    Vehicles.Disown(obj.GUID, obj)
    if (!decaying &&
        obj.Definition.undergoesDecay &&
        obj.Owner.isEmpty &&
        obj.Seats.values.forall(!_.isOccupied)) {
      decaying = true
      decayTimer = context.system.scheduler.scheduleOnce(
        obj.Definition.DeconstructionTime.getOrElse(5 minutes),
        self,
        VehicleControl.PrepareForDeletion()
      )
    }
  }

  def GainOwnership(player: Player): Unit = {
    val obj = MountableObject
    Vehicles.Own(obj, player) match {
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
      VehicleAction.ObjectDelete(item.GUID)
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
        val driverChannel = vehicle.Seats(0).occupant match {
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
        val driverChannel = vehicle.Seats(0).occupant match {
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

  /**
    * Water causes vehicles to become disabled if they dive off too far, too deep.
    * Flying vehicles do not display progress towards being waterlogged.  They just disable outright.
    * @param obj the target
    * @param body the environment
    * @param data additional interaction information, if applicable
    */
  def doInteractingWithWater(obj: PlanetSideServerObject, body: PieceOfEnvironment, data: Option[OxygenStateTarget]): Unit = {
    val (effect: Boolean, time: Long, percentage: Float) = {
      val (a, b, c) = RespondsToZoneEnvironment.drowningInWateryConditions(obj, submergedCondition, interactionTime)
      if (a && vehicle.Definition.CanFly) {
        (true, 0L, 0f) //no progress bar
      } else {
        (a, b, c)
      }
    }
    if (effect) {
      import scala.concurrent.ExecutionContext.Implicits.global
      submergedCondition = Some(OxygenState.Suffocation)
      interactionTime = System.currentTimeMillis() + time
      interactionTimer = context.system.scheduler.scheduleOnce(delay = time milliseconds, self, VehicleControl.Disable())
      doInteractingWithWaterToTargets(
        percentage,
        body,
        vehicle.Seats.values
          .flatMap { case seat if seat.isOccupied => seat.occupants }
          .filter { p => p.isAlive && (p.Zone eq vehicle.Zone) }
      )
    }
  }

  /**
    * Tell the given targets that
    * water causes vehicles to become disabled if they dive off too far, too deep.
    * @see `InteractWithEnvironment`
    * @see `OxygenState`
    * @see `OxygenStateTarget`
    * @param percentage the progress bar completion state
    * @param body the environment
    * @param targets recipients of the information
    */
  def doInteractingWithWaterToTargets(
                                       percentage: Float,
                                       body: PieceOfEnvironment,
                                       targets: Iterable[PlanetSideServerObject]
                                     ): Unit = {
    val vtarget = Some(OxygenStateTarget(vehicle.GUID, OxygenState.Suffocation, percentage))
    targets.foreach { target =>
      target.Actor ! InteractWithEnvironment(target, body, vtarget)
    }
  }

  /**
    * Lava causes vehicles to take (considerable) damage until they are inevitably destroyed.
    * @param obj the target
    * @param body the environment
    * @param data additional interaction information, if applicable
    */
  def doInteractingWithLava(obj: PlanetSideServerObject, body: PieceOfEnvironment, data: Option[OxygenStateTarget]): Unit = {
    val vehicle = DamageableObject
    if (!obj.Destroyed) {
      PerformDamage(
        vehicle,
        DamageInteraction(
          VehicleSource(vehicle),
          EnvironmentReason(body, vehicle),
          vehicle.Position
        ).calculate()
      )
      //keep doing damage
      if (vehicle.Health > 0) {
        import scala.concurrent.ExecutionContext.Implicits.global
        interactionTimer = context.system.scheduler.scheduleOnce(delay = 250 milliseconds, self, InteractWithEnvironment(obj, body, None))
      }
    }
  }

  /**
    * Death causes vehicles to be destroyed outright.
    * It's not even considered as environmental damage anymore.
    * @param obj the target
    * @param body the environment
    * @param data additional interaction information, if applicable
    */
  def doInteractingWithDeath(obj: PlanetSideServerObject, body: PieceOfEnvironment, data: Option[OxygenStateTarget]): Unit = {
    if (!obj.Destroyed) {
      PerformDamage(
        vehicle,
        DamageInteraction(
          VehicleSource(vehicle),
          SuicideReason(),
          vehicle.Position
        ).calculate()
      )
    }
  }

  /**
    * When out of water, the vehicle no longer risks becoming disabled.
    * It does have to endure a recovery period to get back to full dehydration
    * Flying vehicles are exempt from this process due to the abrupt disability they experience.
    * @param obj the target
    * @param body the environment
    * @param data additional interaction information, if applicable
    */
  def stopInteractingWithWater(obj: PlanetSideServerObject, body: PieceOfEnvironment, data: Option[OxygenStateTarget]): Unit = {
    val (effect: Boolean, time: Long, percentage: Float) =
      RespondsToZoneEnvironment.recoveringFromWateryConditions(obj, submergedCondition, interactionTime)
    if (effect) {
      recoverFromEnvironmentInteracting()
      import scala.concurrent.ExecutionContext.Implicits.global
      submergedCondition = Some(OxygenState.Recovery)
      interactionTime = System.currentTimeMillis() + time
      interactionTimer = context.system.scheduler.scheduleOnce(delay = time milliseconds, self, RecoveredFromEnvironmentInteraction())
      stopInteractingWithWaterToTargets(
        percentage,
        body,
        vehicle.Seats.values
          .flatMap { case seat if seat.isOccupied => seat.occupants }
          .filter { p => p.isAlive && (p.Zone eq vehicle.Zone) }
      )
    }
  }

  /**
    * Tell the given targets that,
    * when out of water, the vehicle no longer risks becoming disabled.
    * @see `EscapeFromEnvironment`
    * @see `OxygenState`
    * @see `OxygenStateTarget`
    * @param percentage the progress bar completion state
    * @param body the environment
    * @param targets recipients of the information
    */
  def stopInteractingWithWaterToTargets(
                                         percentage: Float,
                                         body: PieceOfEnvironment,
                                         targets: Iterable[PlanetSideServerObject]
                                       ): Unit = {
    val vtarget = Some(OxygenStateTarget(vehicle.GUID, OxygenState.Recovery, percentage))
    targets.foreach { target =>
      target.Actor ! EscapeFromEnvironment(target, body, vtarget)
    }
  }

  /**
    * Reset the environment encounter fields and completely stop whatever is the current mechanic.
    * This does not perform messaging relay either with mounted occupants or with any other service.
    */
  override def recoverFromEnvironmentInteracting(): Unit = {
    super.recoverFromEnvironmentInteracting()
    submergedCondition = None
  }

  /**
    * Without altering the state or progress of a zone interaction related to water,
    * update the visual progress element (progress bar) that is visible to the recipient's client.
    * @param player the recipient of this ui update
    */
  def updateZoneInteractionProgressUI(player : Player) : Unit = {
    submergedCondition match {
      case Some(OxygenState.Suffocation) =>
        interactWith match {
          case Some(body) =>
            val percentage: Float = {
              val (a, _, c) = RespondsToZoneEnvironment.drowningInWateryConditions(vehicle, submergedCondition, interactionTime)
              if (a && vehicle.Definition.CanFly) {
                0f //no progress bar
              } else {
                c
              }
            }
            doInteractingWithWaterToTargets(percentage, body, List(player))
          case _ =>
            recoverFromEnvironmentInteracting()
        }
      case Some(OxygenState.Recovery) =>
        vehicle.Zone.map.environment.find { _.attribute == EnvironmentAttribute.Water } match {
          case Some(body) => //any body of water will do ...
            stopInteractingWithWaterToTargets(
              RespondsToZoneEnvironment.recoveringFromWateryConditions(vehicle, submergedCondition, interactionTime)._3,
              body,
              List(player)
            )
          case _ =>
            recoverFromEnvironmentInteracting()
        }
      case None => ;
    }
  }
}

object VehicleControl {
  import net.psforever.objects.vital.{DamageFromProjectile, VehicleShieldCharge, VitalsActivity}
  import scala.concurrent.duration._

  private case class PrepareForDeletion()

  private case class Disable()

  private case class Deletion()

  final case class AssignOwnership(player: Option[Player])

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
      case DamageFromProjectile(data) => now - data.interaction.hitTime < (5 seconds).toMillis //damage delays next charge by 5s
      case vsc: VehicleShieldCharge   => now - vsc.time < (1 seconds).toMillis      //previous charge delays next by 1s
      case _                          => false
    }
  }

  def DeploymentAngleCheck(obj: Deployment.DeploymentObject): Boolean = {
    obj.Orientation.x <= 30 || obj.Orientation.x >= 330
  }
}
