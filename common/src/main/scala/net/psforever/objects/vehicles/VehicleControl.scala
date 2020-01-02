// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.objects.ballistics.{ResolvedProjectile, VehicleSource}
import net.psforever.objects.equipment.{JammableMountedWeapons, JammableUnit}
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.deploy.{Deployment, DeploymentBehavior}
import net.psforever.objects.vital.{VehicleShieldCharge, Vitality}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{DriveState, ExoSuitType, Vector3}
import services.{RemoverActor, Service}
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}
import services.vehicle.{VehicleAction, VehicleService, VehicleServiceMessage}

/**
  * An `Actor` that handles messages being dispatched to a specific `Vehicle`.<br>
  * <br>
  * Vehicle-controlling actors have two behavioral states - responsive and "`Disabled`."
  * The latter is applicable only when the specific vehicle is being deconstructed.
  * @param vehicle the `Vehicle` object being governed
  */
class VehicleControl(vehicle : Vehicle) extends Actor
  with FactionAffinityBehavior.Check
  with DeploymentBehavior
  with MountableBehavior.Mount
  with MountableBehavior.Dismount
  with JammableMountedWeapons {

  //make control actors belonging to utilities when making control actor belonging to vehicle
  vehicle.Utilities.foreach({case (_, util) => util.Setup })

  def MountableObject = vehicle

  def JammableObject = vehicle

  def FactionObject = vehicle

  def DeploymentObject = vehicle

  def receive : Receive = Enabled

  override def postStop() : Unit = {
    super.postStop()
    vehicle.Utilities.values.foreach { util =>
      util().Actor ! akka.actor.PoisonPill
      util().Actor = ActorRef.noSender
    }
  }

  def Enabled : Receive = checkBehavior
    .orElse(deployBehavior)
    .orElse(dismountBehavior)
    .orElse(jammableBehavior)
    .orElse {
      case Mountable.TryMount(user, seat_num) =>
        val exosuit = user.ExoSuit
        val restriction = vehicle.Seats(seat_num).ArmorRestriction
        val seatGroup = vehicle.SeatPermissionGroup(seat_num).getOrElse(AccessPermissionGroup.Passenger)
        val permission = vehicle.PermissionGroup(seatGroup.id).getOrElse(VehicleLockState.Empire)
        if(
          (if(seatGroup == AccessPermissionGroup.Driver) {
              vehicle.Owner.contains(user.GUID) || vehicle.Owner.isEmpty || permission != VehicleLockState.Locked
            }
            else {
              permission != VehicleLockState.Locked
            }) &&
            (exosuit match {
              case ExoSuitType.MAX => restriction == SeatArmorRestriction.MaxOnly
              case ExoSuitType.Reinforced => restriction == SeatArmorRestriction.NoMax
              case _ => restriction != SeatArmorRestriction.MaxOnly
            })
        ) {
          mountBehavior.apply(Mountable.TryMount(user, seat_num))
        }
        else {
          sender ! Mountable.MountMessages(user, Mountable.CanNotMount(vehicle, seat_num))
        }

      case Vitality.Damage(damage_func) =>
        if(vehicle.Health > 0) {
          val originalHealth = vehicle.Health
          val originalShields = vehicle.Shields
          val cause = damage_func(vehicle)
          val health = vehicle.Health
          val shields = vehicle.Shields
          val damageToHealth = originalHealth - health
          val damageToShields = originalShields - shields
          VehicleControl.HandleDamageResolution(vehicle, cause, damageToHealth + damageToShields)
          if(damageToHealth > 0 || damageToShields > 0) {
            val name = vehicle.Actor.toString
            val slashPoint = name.lastIndexOf("/")
            org.log4s.getLogger("DamageResolution").info(s"${name.substring(slashPoint + 1, name.length - 1)}: BEFORE=$originalHealth/$originalShields, AFTER=$health/$shields, CHANGE=$damageToHealth/$damageToShields")
          }
        }

      case Vehicle.ChargeShields(amount) =>
        val now : Long = System.nanoTime
        //make certain vehicle doesn't charge shields too quickly
        if(vehicle.Health > 0 && vehicle.Shields < vehicle.MaxShields &&
          !vehicle.History.exists(VehicleControl.LastShieldChargeOrDamage(now))) {
          vehicle.History(VehicleShieldCharge(VehicleSource(vehicle), amount))
          vehicle.Shields = vehicle.Shields + amount
          vehicle.Zone.VehicleEvents ! VehicleServiceMessage(s"${vehicle.Actor}", VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), vehicle.GUID, 68, vehicle.Shields))
        }

      case FactionAffinity.ConvertFactionAffinity(faction) =>
        val originalAffinity = vehicle.Faction
        if(originalAffinity != (vehicle.Faction = faction)) {
          vehicle.Utilities.foreach({ case(_ : Int, util : Utility) => util().Actor forward FactionAffinity.ConfirmFactionAffinity() })
        }
        sender ! FactionAffinity.AssertFactionAffinity(vehicle, faction)

      case Vehicle.PrepareForDeletion() =>
        CancelJammeredSound(vehicle)
        CancelJammeredStatus(vehicle)
        context.become(Disabled)

      case _ => ;
    }

  def Disabled : Receive = checkBehavior
    .orElse(dismountBehavior)
    .orElse {
      case Vehicle.Reactivate() =>
        context.become(Enabled)

      case _ =>
    }
}

object VehicleControl {
  import net.psforever.objects.vital.{DamageFromProjectile, VehicleShieldCharge, VitalsActivity}
  import scala.concurrent.duration._

  /**
    * Determine if a given activity entry would invalidate the act of charging vehicle shields this tick.
    * @param now the current time (in nanoseconds)
    * @param act a `VitalsActivity` entry to test
    * @return `true`, if the vehicle took damage in the last five seconds or
    *        charged shields in the last second;
    *        `false`, otherwise
    */
  def LastShieldChargeOrDamage(now : Long)(act : VitalsActivity) : Boolean = {
    act match {
      case DamageFromProjectile(data) => now - data.hit_time < (5 seconds).toNanos //damage delays next charge by 5s
      case vsc : VehicleShieldCharge => now - vsc.time < (1 seconds).toNanos //previous charge delays next by 1s
      case _ => false
    }
  }

  /**
    * na
    * @param target na
    */
  def HandleDamageResolution(target : Vehicle, cause : ResolvedProjectile, damage : Int) : Unit = {
    val zone = target.Zone
    val targetGUID = target.GUID
    val playerGUID = zone.LivePlayers.find { p => cause.projectile.owner.Name.equals(p.Name) } match {
      case Some(player) => player.GUID
      case _ => PlanetSideGUID(0)
    }
    if(target.Health > 0) {
      //activity on map
      if(damage > 0) {
        zone.Activity ! Zone.HotSpot.Activity(cause.target, cause.projectile.owner, cause.hit_pos)
        //alert occupants to damage source
        HandleDamageAwareness(target, playerGUID, cause)
      }
      if(cause.projectile.profile.JammerProjectile) {
        target.Actor ! JammableUnit.Jammered(cause)
      }
    }
    else {
      //alert to vehicle death (hence, occupants' deaths)
      HandleDestructionAwareness(target, playerGUID, cause)
    }
    zone.VehicleEvents ! VehicleServiceMessage(zone.Id, VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 0, target.Health))
    zone.VehicleEvents ! VehicleServiceMessage(s"${target.Actor}", VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, targetGUID, 68, target.Shields))
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDamageAwareness(target : Vehicle, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    val zone = target.Zone
    //alert occupants to damage source
    target.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    }).foreach(seat => {
      val tplayer = seat.Occupant.get
      zone.AvatarEvents ! AvatarServiceMessage(tplayer.Name, AvatarAction.HitHint(attribution, tplayer.GUID))
    })
    //alert cargo occupants to damage source
    target.CargoHolds.values.foreach(hold => {
      hold.Occupant match {
        case Some(cargo) =>
          cargo.Health = 0
          cargo.Shields = 0
          cargo.History(lastShot)
          HandleDamageAwareness(cargo, attribution, lastShot)
        case None => ;
      }
    })
  }

  /**
    * na
    * @param target na
    * @param attribution na
    * @param lastShot na
    */
  def HandleDestructionAwareness(target : Vehicle, attribution : PlanetSideGUID, lastShot : ResolvedProjectile) : Unit = {
    target.Actor ! JammableUnit.ClearJammeredSound()
    target.Actor ! JammableUnit.ClearJammeredStatus()
    val zone = target.Zone
    val continentId = zone.Id
    //alert to vehicle death (hence, occupants' deaths)
    target.Seats.values.filter(seat => {
      seat.isOccupied && seat.Occupant.get.isAlive
    }).foreach(seat => {
      val tplayer = seat.Occupant.get
      val tplayerGUID = tplayer.GUID
      zone.AvatarEvents ! AvatarServiceMessage(tplayer.Name, AvatarAction.KilledWhileInVehicle(tplayerGUID))
      zone.AvatarEvents ! AvatarServiceMessage(continentId, AvatarAction.ObjectDelete(tplayerGUID, tplayerGUID)) //dead player still sees self
    })
    //vehicle wreckage has no weapons
    target.Weapons.values
      .filter {
        _.Equipment.nonEmpty
      }
      .foreach(slot => {
        val wep = slot.Equipment.get
        zone.AvatarEvents ! AvatarServiceMessage(continentId, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, wep.GUID))
      })
    target.CargoHolds.values.foreach(hold => {
      hold.Occupant match {
        case Some(cargo) =>
          cargo.Health = 0
          cargo.Shields = 0
          cargo.Position += Vector3.z(1)
          cargo.History(lastShot) //necessary to kill cargo vehicle occupants //TODO: collision damage
          HandleDestructionAwareness(cargo, attribution, lastShot) //might cause redundant packets
        case None => ;
      }
    })
    target.Definition match {
      case GlobalDefinitions.ams =>
        target.Actor ! Deployment.TryDeploymentChange(DriveState.Undeploying)
      case GlobalDefinitions.router =>
        target.Actor ! Deployment.TryDeploymentChange(DriveState.Undeploying)
        VehicleService.BeforeUnloadVehicle(target, zone)
        zone.LocalEvents ! LocalServiceMessage(zone.Id, LocalAction.ToggleTeleportSystem(PlanetSideGUID(0), target, None))
      case _ => ;
    }
    zone.AvatarEvents ! AvatarServiceMessage(continentId, AvatarAction.Destroy(target.GUID, attribution, attribution, target.Position))
    zone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.ClearSpecific(List(target), zone))
    zone.VehicleEvents ! VehicleServiceMessage.Decon(RemoverActor.AddTask(target, zone, Some(1 minute)))
  }
}
