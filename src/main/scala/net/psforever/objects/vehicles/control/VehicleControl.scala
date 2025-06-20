// Copyright (c) 2017-2021 PSForever
package net.psforever.objects.vehicles.control

import akka.actor.Cancellable
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects._
import net.psforever.objects.definition.{VehicleDefinition, WithShields}
import net.psforever.objects.definition.converter.OCM
import net.psforever.objects.entity.WorldEntity
import net.psforever.objects.equipment.{ArmorSiphonBehavior, Equipment, EquipmentSlot, JammableMountedWeapons}
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.inventory.{GridInventory, InventoryItem}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject, ServerObjectControl}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.containable.{Containable, ContainableBehavior}
import net.psforever.objects.serverobject.damage.Damageable.Target
import net.psforever.objects.serverobject.damage.{AggravatedBehavior, DamageableVehicle}
import net.psforever.objects.serverobject.environment._
import net.psforever.objects.serverobject.environment.interaction.common.Watery
import net.psforever.objects.serverobject.environment.interaction.{InteractWithEnvironment, RespondsToZoneEnvironment}
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior, RadiationInMountableInteraction}
import net.psforever.objects.serverobject.repair.RepairableVehicle
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.serverobject.turret.auto.AffectedByAutomaticTurretFire
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry, VehicleSource}
import net.psforever.objects.vehicles._
import net.psforever.objects.vehicles.interaction.WithWater
import net.psforever.objects.vital.interaction.DamageResult
import net.psforever.objects.vital.{DamagingActivity, InGameActivity, ShieldCharge, SpawningActivity, VehicleDismountActivity, VehicleMountActivity}
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
import scala.util.Random

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
    with CargoBehavior
    with AffectedByAutomaticTurretFire {
  //make control actors belonging to utilities when making control actor belonging to vehicle
  vehicle.Utilities.foreach { case (_, util) => util.Setup }

  def MountableObject: Vehicle = vehicle
  def JammableObject: Vehicle = vehicle
  def FactionObject: Vehicle = vehicle
  def DamageableObject: Vehicle = vehicle
  def SiphonableObject: Vehicle = vehicle
  def RepairableObject: Vehicle = vehicle
  def ContainerObject: Vehicle = vehicle
  def InteractiveObject: Vehicle = vehicle
  def CargoObject: Vehicle = vehicle
  def AffectedObject: Vehicle = vehicle

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
    passengerRadiationCloudTimer.cancel()
    vehicle.Utilities.values.foreach { util =>
      context.stop(util().Actor)
      util().Actor = Default.Actor
    }
    respondToEnvironmentPostStop()
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
    .orElse(takeAutomatedDamage)
    .orElse {
      case Vehicle.Ownership(None) =>
        LoseOwnership()

      case Vehicle.Ownership(Some(player)) =>
        GainOwnership(player)

      case Mountable.TryMount(user, mountPoint)
        if vehicle.DeploymentState == DriveState.AutoPilot =>
        sender() ! Mountable.MountMessages(user, Mountable.CanNotMount(vehicle, mountPoint))

      case msg @ Mountable.TryMount(player, mount_point) =>
        mountBehavior.apply(msg)
        mountCleanup(mount_point, player)

        // Issue 1133. Todo: There may be a better way to address the issue?
      case Mountable.TryDismount(user, seat_num, bailType) if GlobalDefinitions.isFlightVehicle(vehicle.Definition) &&
           (vehicle.History.find { entry => entry.isInstanceOf[SpawningActivity] } match {
        case Some(entry) if System.currentTimeMillis() - entry.time < 3000L => true
        case _ => false
        }) =>
        sender() ! Mountable.MountMessages(user, Mountable.CanNotDismount(vehicle, seat_num, bailType))

      case Mountable.TryDismount(user, seat_num, bailType) if !GlobalDefinitions.isFlightVehicle(vehicle.Definition) &&
           (vehicle.History.find { entry => entry.isInstanceOf[SpawningActivity] } match {
          case Some(entry) if System.currentTimeMillis() - entry.time < 8500L => true
          case _ => false
        }) =>
        sender() ! Mountable.MountMessages(user, Mountable.CanNotDismount(vehicle, seat_num, bailType))

      case Mountable.TryDismount(user, seat_num, bailType)
        if vehicle.Health <= (vehicle.Definition.MaxHealth * .1).round && bailType == BailType.Bailed
          && GlobalDefinitions.isFlightVehicle(vehicle.Definition)
          && (seat_num == 0 || vehicle.SeatPermissionGroup(seat_num).getOrElse(0) == AccessPermissionGroup.Gunner)
          && (vehicle.History.findLast { entry => entry.isInstanceOf[DamagingActivity] } match {
          case Some(entry) if System.currentTimeMillis() - entry.time < 4000L => true
          case _ if Random.nextInt(10) == 1 => false
          case _ => true }) =>
        sender() ! Mountable.MountMessages(user, Mountable.CanNotDismount(vehicle, seat_num, bailType))

      case Mountable.TryDismount(user, seat_num, bailType)
        if vehicle.Health <= (vehicle.Definition.MaxHealth * .2).round && bailType == BailType.Bailed
          && GlobalDefinitions.isFlightVehicle(vehicle.Definition)
          && (seat_num == 0 || vehicle.SeatPermissionGroup(seat_num).getOrElse(0) == AccessPermissionGroup.Gunner)
          && (vehicle.History.findLast { entry => entry.isInstanceOf[DamagingActivity] } match {
          case Some(entry) if System.currentTimeMillis() - entry.time < 3500L => true
          case _ if Random.nextInt(5) == 1 => false
          case _ => true }) =>
        sender() ! Mountable.MountMessages(user, Mountable.CanNotDismount(vehicle, seat_num, bailType))

      case Mountable.TryDismount(user, seat_num, bailType)
        if vehicle.Health <= (vehicle.Definition.MaxHealth * .35).round && bailType == BailType.Bailed
          && GlobalDefinitions.isFlightVehicle(vehicle.Definition)
          && (seat_num == 0 || vehicle.SeatPermissionGroup(seat_num).getOrElse(0) == AccessPermissionGroup.Gunner)
          && (vehicle.History.findLast { entry => entry.isInstanceOf[DamagingActivity] } match {
          case Some(entry) if System.currentTimeMillis() - entry.time < 3000L => true
          case _ if Random.nextInt(4) == 1 => false
          case _ => true }) =>
        sender() ! Mountable.MountMessages(user, Mountable.CanNotDismount(vehicle, seat_num, bailType))

      case Mountable.TryDismount(user, seat_num, bailType)
        if vehicle.DeploymentState == DriveState.AutoPilot =>
        sender() ! Mountable.MountMessages(user, Mountable.CanNotDismount(vehicle, seat_num, bailType))

      case msg @ Mountable.TryDismount(player, seat_num, _) =>
        dismountBehavior.apply(msg)
        dismountCleanup(seat_num, player)

      case CommonMessages.ChargeShields(amount, motivator) =>
        chargeShields(amount, motivator.collect { case o: PlanetSideGameObject with FactionAffinity => SourceEntry(o) })

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
            Vehicles.FinishHackingVehicle(vehicle, player),
            GenericHackables.HackingTickAction(HackState1.Unk1, player, vehicle, item.GUID)
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

      case VehicleControl.Disable(kickPassengers) =>
        PrepareForDisabled(kickPassengers)
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
          vehicle.interaction().find { _.Type == RadiationInMountableInteraction } match {
            case Some(func) => func.interaction(vehicle.getInteractionSector, vehicle)
            case _ => ()
          }
        case _ => ()
      }

  def commonDisabledBehavior: Receive = checkBehavior
    .orElse {
      case msg @ Mountable.TryDismount(user, seat_num, _) =>
        dismountBehavior.apply(msg)
        dismountCleanup(seat_num, user)

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
      vehicle.OwnerGuid.contains(user.GUID) || vehicle.OwnerGuid.isEmpty || permission != VehicleLockState.Locked
    } else {
      permission != VehicleLockState.Locked
    }) &&
    super.mountTest(obj, seatNumber, user)
  }

  def mountCleanup(mount_point: Int, user: Player): Unit = {
    val obj = MountableObject
    obj.PassengerInSeat(user) match {
      case Some(seatNumber) =>
        val vsrc = VehicleSource(vehicle)
        user.LogActivity(VehicleMountActivity(vsrc, PlayerSource.inSeat(user, vsrc, seatNumber), vehicle.Zone.Number))
        //if the driver mount, change ownership if that is permissible for this vehicle
        if (seatNumber == 0 && !obj.OwnerName.contains(user.Name) && obj.Definition.CanBeOwned.nonEmpty) {
          //whatever vehicle was previously owned
          vehicle.Zone.GUID(user.avatar.vehicle) match {
            case Some(v: Vehicle) =>
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

  def dismountCleanup(seatBeingDismounted: Int, user: Player): Unit = {
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
      user.LogActivity(VehicleDismountActivity(VehicleSource(vehicle), PlayerSource(user), vehicle.Zone.Number))
      //we were only owning the vehicle while we sat in its driver seat
      val canBeOwned = obj.Definition.CanBeOwned
      if (canBeOwned.contains(false) && seatBeingDismounted == 0) {
        LoseOwnership()
      }
      //are we already decaying? are we unowned? is no one seated anywhere?
      if (!decaying &&
          obj.Definition.undergoesDecay &&
          obj.OwnerGuid.isEmpty &&
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
    //escape being someone else's cargo
    vehicle.MountedIn.foreach(_ => startCargoDismounting(bailed = true))
    if (!vehicle.isFlying || kickPassengers) {
      //kick all passengers (either not flying, or being explicitly instructed)
      vehicle.Seats.values.foreach { seat =>
        seat.occupant.foreach { player =>
          seat.unmount(player, BailType.Kicked)
          player.VehicleSeated = None
          if (player.isAlive) {
            zone.actor ! ZoneActor.AddToBlockMap(player, vehicle.Position)
          }
          if (player.HasGUID) {
            events ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(player.GUID, 4, unk2 = true, guid))
          }
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
    vehicle.ClearHistory()
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
        obj.OwnerGuid.isEmpty &&
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
  def canChargeShields: Boolean = {
    val func: InGameActivity => Boolean = WithShields.LastShieldChargeOrDamage(System.currentTimeMillis(), vehicle.Definition)
    vehicle.Health > 0 && vehicle.Shields < vehicle.MaxShields &&
    vehicle.History.findLast(func).isEmpty
  }

  def chargeShields(amount: Int, motivator: Option[SourceEntry]): Unit = {
    if (canChargeShields) {
      vehicle.LogActivity(ShieldCharge(amount, motivator))
      vehicle.Shields = vehicle.Shields + amount
      vehicle.Zone.VehicleEvents ! VehicleServiceMessage(
        s"${vehicle.Actor}",
        VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), vehicle.GUID, vehicle.Definition.shieldUiAttribute, vehicle.Shields)
      )
    }
  }

  /**
    * Without altering the state or progress of a zone interaction related to water,
    * update the visual progress element (progress bar) that is visible to the recipient's client.
    * @param player the recipient of this ui update
    */
  def updateZoneInteractionProgressUI(player: Player) : Unit = {
    val interactions = vehicle
      .interaction()
      .collectFirst { case inter: InteractWithEnvironment => inter.Interactions }
      .getOrElse(RespondsToZoneEnvironment.defaultInteractions)
    //water
    interactions
      .get(EnvironmentAttribute.Water)
      .collect {
        case watery: WithWater if watery.Condition.map(_.state).contains(OxygenState.Suffocation) =>
          val percentage: Float = {
            val (a, _, c) = Watery.drowningInWater(vehicle, watery)
            if (a && GlobalDefinitions.isFlightVehicle(vehicle.Definition)) {
              0f //no progress bar
            } else {
              c
            }
          }
          WithWater.doInteractingWithTargets(player, percentage, watery.Condition.map(_.body).get, List(player))
        case watery: WithWater if watery.Condition.map(_.state).contains(OxygenState.Recovery) =>
          WithWater.stopInteractingWithTargets(
            player,
            Watery.recoveringFromWater(vehicle, watery)._3,
            watery.Condition.map(_.body).get,
            List(player)
          )
        case watery: WithWater =>
          watery.recoverFromInteracting(player)
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
      subsystems.foreach { _.jam() }
      vehicleSubsystemMessages(subsystems.flatMap { _.changedMessages(vehicle) })
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

  override protected def DestructionAwareness(target: Target, cause: DamageResult): Unit = {
    passengerRadiationCloudTimer.cancel()
    super.DestructionAwareness(target, cause)
  }
}

object VehicleControl {
  private case class PrepareForDeletion()

  final case class Disable(kickPassengers: Boolean = false)

  private case class Deletion()

  private case object RadiationTick

  final case class AssignOwnership(player: Option[Player])
}
