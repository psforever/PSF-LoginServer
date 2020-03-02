// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, Player, Tool}
import net.psforever.objects.equipment.{Ammo, JammableMountedWeapons}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.DamageableWeaponTurret
import net.psforever.objects.serverobject.repair.RepairableWeaponTurret
import net.psforever.packet.game.{InventoryStateMessage, RepairMessage}
import net.psforever.types.{PlanetSideGUID, Vector3}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.local.{LocalAction, LocalServiceMessage}
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


/**
  * An `Actor` that handles messages being dispatched to a specific `MannedTurret`.<br>
  * <br>
  * Mounted turrets have only slightly different entry requirements than a normal vehicle
  * because they encompass both faction-specific facility turrets
  * and faction-blind cavern sentry turrets.
  * @param turret the `MannedTurret` object being governed
  */
class FacilityTurretControl(turret : FacilityTurret) extends Actor
  with FactionAffinityBehavior.Check
  with MountableBehavior.Dismount
  with DamageableWeaponTurret
  with RepairableWeaponTurret
  with JammableMountedWeapons {
  def FactionObject = turret
  def MountableObject = turret
  def JammableObject = turret
  def DamageableObject = turret
  def RepairableObject = turret
  if(turret.Definition == GlobalDefinitions.vanu_sentry_turret) {
    // todo: Schedule this to start when weapon is discharged, not all the time
    context.system.scheduler.schedule(3 seconds, 200 milliseconds, self, FacilityTurret.RechargeAmmo())
  }

  def receive : Receive = checkBehavior
    .orElse(jammableBehavior)
    .orElse(dismountBehavior)
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse {
      case FacilityTurret.RechargeAmmo() =>
        val weapon = turret.ControlledWeapon(1).get.asInstanceOf[net.psforever.objects.Tool]
        // recharge when last shot fired 3s delay, +1, 200ms interval
        if(weapon.Magazine < weapon.MaxMagazine && System.nanoTime() - weapon.LastDischarge > 3000000000L) {
          weapon.Magazine += 1
          val seat = turret.Seat(0).get
          seat.Occupant match {
            case Some(player: Player) => turret.Zone.LocalEvents ! LocalServiceMessage(turret.Zone.Id, LocalAction.RechargeVehicleWeapon(player.GUID, turret.GUID, weapon.GUID))
            case _ => ;
          }
        }

      case Mountable.TryMount(user, seat_num) =>
        turret.Seat(seat_num) match {
          case Some(seat) =>
            if((!turret.Definition.FactionLocked || user.Faction == turret.Faction) &&
              (seat.Occupant = user).contains(user)) {
              user.VehicleSeated = turret.GUID
              sender ! Mountable.MountMessages(user, Mountable.CanMount(turret, seat_num))
            }
            else {
              sender ! Mountable.MountMessages(user, Mountable.CanNotMount(turret, seat_num))
            }
          case None =>
            sender ! Mountable.MountMessages(user, Mountable.CanNotMount(turret, seat_num))
        }

      case CommonMessages.Use(player, Some(item : Tool)) if item.Definition == GlobalDefinitions.nano_dispenser =>
        if(!player.isMoving && Vector3.Distance(player.Position, turret.Position) < 5 &&
          player.Faction == turret.Faction && turret.Health < turret.Definition.MaxHealth &&
          item.AmmoType == Ammo.armor_canister && item.Magazine > 0) {
          val zone = turret.Zone
          val zoneId = zone.Id
          val events = zone.AvatarEvents
          val pname = player.Name
          val tguid = turret.GUID
          val magazine = item.Magazine -= 1
          val health = turret.Health += 12 + item.FireMode.Modifiers.Damage1
          val repairPercent: Long = health * 100 / turret.Definition.MaxHealth
          events ! AvatarServiceMessage(pname, AvatarAction.SendResponse(Service.defaultPlayerGUID, InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)))
          events ! AvatarServiceMessage(pname, AvatarAction.SendResponse(Service.defaultPlayerGUID, RepairMessage(tguid, repairPercent)))
          events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 0, health))
          if(turret.Destroyed && health > turret.Definition.RepairRestoresAt) {
            turret.Destroyed = false
            events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 50, 0))
            events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 51, 0))
            turret.Weapons
              .map({ case (index, slot) => (index, slot.Equipment) })
              .collect { case (index, Some(tool : Tool)) =>
                zone.VehicleEvents ! VehicleServiceMessage(
                  zone.Id,
                  VehicleAction.EquipmentInSlot(PlanetSideGUID(0), tguid, index, tool)
                )
              }

          }
        }

      case _ => ;
    }
}
