// Copyright (c) 2017-2021 PSForever
package net.psforever.objects.vehicles.control

import akka.actor.Cancellable
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects._
import net.psforever.objects.ballistics.VehicleSource
import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.definition.converter.OCM
import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.equipment.{ArmorSiphonBehavior, Equipment, EquipmentSlot, JammableMountedWeapons}
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.inventory.{GridInventory, InventoryItem}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject, ServerObjectControl}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.containable.{Containable, ContainableBehavior}
import net.psforever.objects.serverobject.damage.{AggravatedBehavior, DamageableVehicle}
import net.psforever.objects.serverobject.environment._
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.repair.RepairableVehicle
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.vehicles._
import net.psforever.objects.vital.interaction.{DamageInteraction, DamageResult}
import net.psforever.objects.vital.{DamagingActivity, VehicleShieldCharge, VitalsActivity}
import net.psforever.objects.vital.environment.EnvironmentReason
import net.psforever.objects.vital.etc.SuicideReason
import net.psforever.objects.zones._
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game._
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.types._
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles messages being dispatched to a specific `Vehicle`.<br>
  * <br>
  * Vehicle-controlling actors have two important behavioral states - responsive and "`Disabled`."
  * The latter is applicable only when the specific vehicle is being deconstructed.
  * Furthermore, being "ready to delete" is also a behavoral state for the end of life operations of the vehicle.
  * @param vehicle the `Vehicle` object being governed
  */
class VehicleControl(vehicle: Vehicle)
  extends ServerObjectControl
    with FactionAffinityBehavior.Check
    with MountableBehavior
    with DamageableVehicle
    with ArmorSiphonBehavior.Target
    with RepairableVehicle
    with JammableMountedWeapons
    with ContainableBehavior
    with AggravatedBehavior
    with RespondsToZoneEnvironment
    with CargoBehavior {
  //make control actors belonging to utilities when making control actor belonging to vehicle
  vehicle.Utilities.foreach { case (_, util) => util.Setup }

  def MountableObject = vehicle

  def JammableObject = vehicle

  def FactionObject = vehicle

  def DamageableObject = vehicle

  def SiphonableObject = vehicle

  def RepairableObject = vehicle

  def ContainerObject = vehicle

  def InteractiveObject = vehicle

  def CargoObject = vehicle

  SetInteraction(EnvironmentAttribute.Water, doInteractingWithWater)
  SetInteraction(EnvironmentAttribute.Lava, doInteractingWithLava)
  SetInteraction(EnvironmentAttribute.Death, doInteractingWithDeath)
  if (!vehicle.Definition.CanFly || GlobalDefinitions.isBattleFrameFlightVehicle(vehicle.Definition)) {
    //can recover from sinking disability
    SetInteractionStop(EnvironmentAttribute.Water, stopInteractingWithWater)
  }

  /** cheap flag for whether the vehicle is decaying */
  var decaying : Boolean = false
  /** primary vehicle decay timer */
  var decayTimer : Cancellable = Default.Cancellable
  /** becoming waterlogged, or drying out? */
  var submergedCondition : Option[OxygenState] = None
  /** ... */
  var passengerRadiationCloudTimer: Cancellable = Default.Cancellable

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
    endAllCargoOperations()
  }

  def commonEnabledBehavior: Receive = checkBehavior
    .orElse(attributeBehavior)
    .orElse(jammableBehavior)
    .orElse(takesDamage)
    .orElse(siphoningBehavior)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse(containerBehavior)
    .orElse(environmentBehavior)
    .orElse(cargoBehavior)
    .orElse {
      case Vehicle.Ownership(None) =>
        LoseOwnership()

      case Vehicle.Ownership(Some(player)) =>
        GainOwnership(player)

      case msg @ Mountable.TryMount(player, mount_point) =>
        mountBehavior.apply(msg)
        mountCleanup(mount_point, player)

      case msg @ Mountable.TryDismount(_, seat_num, _) =>
        dismountBehavior.apply(msg)
        dismountCleanup(seat_num)

      case Vehicle.ChargeShields(amount) =>
        chargeShields(amount)

      case Vehicle.UpdateZoneInteractionProgressUI(player) =>
        updateZoneInteractionProgressUI(player)

      case Vehicle.UpdateSubsystemStates(toChannel, stateToResolve) =>
        val events = vehicle.Zone.VehicleEvents
        val guid0 = Service.defaultPlayerGUID
        (stateToResolve match {
          case Some(state) =>
            vehicle.Subsystems().filter { _.Enabled == state } //only subsystems that are enabled or are disabled
          case None =>
            vehicle.Subsystems() //all subsystems
        })
          .flatMap { _.getMessage(vehicle) }
          .foreach { pkt =>
            events ! VehicleServiceMessage(toChannel, VehicleAction.SendResponse(guid0, pkt))
          }


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
        val zone = vehicle.Zone
        if (permitTerminalMessage(player, msg)) {
          reply match {
            case Terminal.VehicleLoadout(definition, weapons, inventory) =>
              log.info(s"changing vehicle equipment loadout to ${player.Name}'s option #${msg.unk1 + 1}")
              val (oldWeapons, newWeapons, oldInventory, finalInventory) =
                handleTerminalMessageVehicleLoadout(player, definition, weapons, inventory)
              zone.VehicleEvents ! VehicleServiceMessage(
                zone.id,
                VehicleAction.ChangeLoadout(vehicle.GUID, oldWeapons, newWeapons, oldInventory, finalInventory)
              )
              zone.AvatarEvents ! AvatarServiceMessage(
                player.Name,
                AvatarAction.TerminalOrderResult(msg.terminal_guid, msg.transaction_type, result = true)
              )

            case _ => ;
          }
        } else {
          zone.AvatarEvents ! AvatarServiceMessage(
            player.Name,
            AvatarAction.TerminalOrderResult(msg.terminal_guid, msg.transaction_type, result = false)
          )
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
    }

  final def Enabled: Receive =
    commonEnabledBehavior
      .orElse {
        case VehicleControl.RadiationTick =>
          vehicle.interaction().find { _.Type == RadiationInVehicleInteraction } match {
            case Some(func) => func.interaction(vehicle.getInteractionSector(), vehicle)
            case _ => ;
          }
        case _ => ;
      }

  def commonDisabledBehavior: Receive = checkBehavior
    .orElse {
      case msg @ Mountable.TryDismount(_, seat_num, _) =>
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
    }

  final def Disabled: Receive = commonDisabledBehavior
    .orElse {
      case _ => ;
    }

  def commonDeleteBehavior: Receive = checkBehavior
    .orElse {
      case VehicleControl.Deletion() =>
        val zone = vehicle.Zone
        zone.VehicleEvents ! VehicleServiceMessage(
          zone.id,
          VehicleAction.UnloadVehicle(Service.defaultPlayerGUID, vehicle, vehicle.GUID)
        )
        zone.Transport.tell(Zone.Vehicle.Despawn(vehicle), zone.Transport)
    }

  final def ReadyToDelete: Receive = commonDeleteBehavior
    .orElse {
      case _ => ;
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
    obj.PassengerInSeat(user) match {
      case Some(seatNumber) =>
        //if the driver mount, change ownership if that is permissible for this vehicle
        if (seatNumber == 0 && !obj.OwnerName.contains(user.Name) && obj.Definition.CanBeOwned.nonEmpty) {
          //whatever vehicle was previously owned
          vehicle.Zone.GUID(user.avatar.vehicle) match {
            case Some(v : Vehicle) =>
              v.Actor ! Vehicle.Ownership(None)
            case _ =>
              user.avatar.vehicle = None
          }
          GainOwnership(user) //gain new ownership
          passengerRadiationCloudTimer.cancel()
        } else {
          decaying = false
          decayTimer.cancel()
        }
        updateZoneInteractionProgressUI(user)
      case None => ;
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
    if (seatBeingDismounted == 0) {
      passengerRadiationCloudTimer = context.system.scheduler.scheduleWithFixedDelay(
        250.milliseconds,
        250.milliseconds,
        self,
        VehicleControl.RadiationTick
      )
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
    //escape being someone else's cargo
    vehicle.MountedIn match {
      case Some(_) =>
        startCargoDismounting(bailed = false)
      case _ => ;
    }
    if (!vehicle.isFlying || kickPassengers) {
      //kick all passengers (either not flying, or being explicitly instructed)
      vehicle.Seats.values.foreach { seat =>
        seat.occupant match {
          case Some(player) =>
            seat.unmount(player, BailType.Kicked)
            player.VehicleSeated = None
            if (player.isAlive) {
              zone.actor ! ZoneActor.AddToBlockMap(player, vehicle.Position)
            }
            if (player.HasGUID) {
              events ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(player.GUID, 4, unk2 = true, guid))
            }
          case None => ;
        }
      }
    }
  }

  def PrepareForDeletion() : Unit = {
    decaying = false
    val zone = vehicle.Zone
    //cancel jammed behavior
    CancelJammeredSound(vehicle)
    CancelJammeredStatus(vehicle)
    //unregister
    TaskWorkflow.execute(GUIDTask.unregisterVehicle(zone.GUID, vehicle))
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
    Vehicles.Disown(obj.GUID, obj)
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
    val obj      = ContainerObject
    val oguid    = obj.GUID
    val zone     = obj.Zone
    val channel  = self.toString
    val events   = zone.VehicleEvents
    val iguid    = item.GUID
    item.Faction = obj.Faction
    events ! VehicleServiceMessage(
      //TODO when a new weapon, the equipment slot ui goes blank, but the weapon functions; remount vehicle to correct it
      if (obj.VisibleSlots.contains(slot)) zone.id else channel,
      VehicleAction.SendResponse(
        Service.defaultPlayerGUID,
        OCM.detailed(item, ObjectCreateMessageParent(oguid, slot))
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
            VehicleAction.InventoryState2(Service.defaultPlayerGUID, box.GUID, iguid, box.Capacity)
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

  def permitTerminalMessage(player: Player, msg: ItemTransactionMessage): Boolean = true

  def handleTerminalMessageVehicleLoadout(
                                           player: Player,
                                           definition: VehicleDefinition,
                                           weapons: List[InventoryItem],
                                           inventory: List[InventoryItem]
                                         ): (
      List[(Equipment, PlanetSideGUID)],
      List[InventoryItem],
      List[(Equipment, PlanetSideGUID)],
      List[InventoryItem]
    ) = {
    //remove old inventory
    val oldInventory = vehicle.Inventory.Clear().map { case InventoryItem(obj, _) => (obj, obj.GUID) }
    //"dropped" items are lost; if it doesn't go in the trunk, it vanishes into the nanite cloud
    val (_, afterInventory) = inventory.partition(ContainableBehavior.DropPredicate(player))
    val (oldWeapons, newWeapons, finalInventory) = if (vehicle.Definition == definition) {
      //vehicles are the same type; just refill ammo, assuming weapons stay the same
      vehicle.Weapons
        .collect { case (_, slot: EquipmentSlot) if slot.Equipment.nonEmpty => slot.Equipment.get }
        .collect {
          case weapon: Tool =>
            weapon.AmmoSlots.foreach { ammo => ammo.Box.Capacity = ammo.MaxMagazine() }
        }
      (Nil, Nil, afterInventory)
    }
    else {
      //vehicle loadout is not for this vehicle; do not transfer over weapon ammo
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
    (oldWeapons, newWeapons, oldInventory, finalInventory)
  }

  //make certain vehicles don't charge shields too quickly
  def canChargeShields(): Boolean = {
    val func: VitalsActivity => Boolean = VehicleControl.LastShieldChargeOrDamage(System.currentTimeMillis(), vehicle.Definition)
    vehicle.Health > 0 && vehicle.Shields < vehicle.MaxShields &&
    !vehicle.History.exists(func)
  }

  def chargeShields(amount: Int): Unit = {
    if (canChargeShields()) {
      vehicle.History(VehicleShieldCharge(VehicleSource(vehicle), amount))
      vehicle.Shields = vehicle.Shields + amount
      vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
        s"${vehicle.Actor}",
        VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), vehicle.GUID, vehicle.Definition.shieldUiAttribute, vehicle.Shields)
      )
    }
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
      if (a && vehicle.Definition.CanFly && !GlobalDefinitions.isBattleFrameFlightVehicle(vehicle.Definition)) {
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
          .flatMap {
            case seat if seat.isOccupied => seat.occupants
            case _ => Nil
          }
          .filter { p => p.isAlive && (p.Zone eq vehicle.Zone) }
      )
    }
  }

  /**
    * Tell the given targets that
    * water causes vehicles to become disabled if they dive off too far, too deep.
    * @see `InteractingWithEnvironment`
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
      target.Actor ! InteractingWithEnvironment(target, body, vtarget)
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
        interactionTimer = context.system.scheduler.scheduleOnce(delay = 250 milliseconds, self, InteractingWithEnvironment(obj, body, None))
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
          .flatMap {
            case seat if seat.isOccupied => seat.occupants
            case _                       => Nil
          }
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

  override def parseAttribute(attribute: Int, value: Long, other: Option[Any]) : Unit = {
    val vguid = vehicle.GUID
    val (dname, dguid) = other match {
      case Some(p: Player) => (p.Name, p.GUID)
      case _               => (vehicle.OwnerName.getOrElse("The driver"), PlanetSideGUID(0))
    }
    val zone = vehicle.Zone
    if (9 < attribute && attribute < 14) {
      vehicle.PermissionGroup(attribute, value) match {
        case Some(allow) =>
          val group = AccessPermissionGroup(attribute - 10)
          log.info(s"$dname changed ${vehicle.Definition.Name}'s access permission $group to $allow")
          zone.VehicleEvents ! VehicleServiceMessage(
            zone.id,
            VehicleAction.SeatPermissions(dguid, vguid, attribute, value)
          )
          //kick players who should not be seated in the vehicle due to permission changes
          if (allow == VehicleLockState.Locked) { //TODO only important permission atm
            vehicle.Seats.foreach {
              case (seatIndex, seat) =>
                seat.occupant match {
                  case Some(tplayer: Player) =>
                    if (vehicle.SeatPermissionGroup(seatIndex).contains(group) && !tplayer.Name.equals(dname)) { //can not kick self
                      seat.unmount(tplayer)
                      tplayer.VehicleSeated = None
                      zone.VehicleEvents ! VehicleServiceMessage(
                        zone.id,
                        VehicleAction.KickPassenger(tplayer.GUID, 4, unk2 = false, vguid)
                      )
                    }
                  case _ => ; // No player seated
                }
            }
            vehicle.CargoHolds.foreach {
              case (cargoIndex, hold) =>
                hold.occupant match {
                  case Some(cargo) =>
                    if (vehicle.SeatPermissionGroup(cargoIndex).contains(group)) {
                      //todo: this probably doesn't work for passengers within the cargo vehicle
                      // Instruct client to start bail dismount procedure
                      self ! DismountVehicleCargoMsg(dguid, cargo.GUID, bailed = true, requestedByPassenger = false, kicked = false)
                    }
                  case None => ; // No vehicle in cargo
                }
            }
          }
        case None => ;
      }
    } else {
      log.warn(
        s"parseAttributes: unsupported change on $vguid - $attribute, $dname"
      )
    }
  }

  override def StartJammeredStatus(target: Any, dur: Int): Unit = {
    super.StartJammeredStatus(target, dur)
    val subsystems = vehicle.Subsystems()
    if (!subsystems.exists { _.Jammed }) {
      subsystems.foreach { _.jam() }
      vehicleSubsystemMessages(subsystems.flatMap { _.changedMessages(vehicle) })
    }
  }

  override def CancelJammeredStatus(target: Any): Unit = {
    super.CancelJammeredStatus(target)
    val subsystems = vehicle.Subsystems()
    if (subsystems.exists { _.Jammed }) {
      subsystems.foreach { _.unjam() }
      vehicleSubsystemMessages(subsystems.flatMap { _.changedMessages(vehicle) })
    }
  }

  def vehicleSubsystemMessages(messages: List[PlanetSideGamePacket]): Unit = {
    val zone = vehicle.Zone
    val zoneid = zone.id
    val events = zone.VehicleEvents
    val guid0 = Service.defaultPlayerGUID
    messages.foreach { pkt =>
      events ! VehicleServiceMessage(
        zoneid,
        VehicleAction.SendResponse(guid0, pkt)
      )
    }
  }
}

object VehicleControl {
  import net.psforever.objects.vital.{VehicleShieldCharge, VitalsActivity}

  private case class PrepareForDeletion()

  private case class Disable()

  private case class Deletion()

  private case object RadiationTick

  final case class AssignOwnership(player: Option[Player])

  /**
    * Determine if a given activity entry would invalidate the act of charging vehicle shields this tick.
    * @param now the current time (in nanoseconds)
    * @param act a `VitalsActivity` entry to test
    * @return `true`, if the shield charge would be blocked;
    *        `false`, otherwise
    */
  def LastShieldChargeOrDamage(now: Long, vdef: VehicleDefinition)(act: VitalsActivity): Boolean = {
    act match {
      case dact: DamagingActivity   => now - dact.time < vdef.ShieldDamageDelay //damage delays next charge
      case vsc: VehicleShieldCharge => now - vsc.time < vdef.ShieldPeriodicDelay //previous charge delays next
      case _                        => false
    }
  }
}
