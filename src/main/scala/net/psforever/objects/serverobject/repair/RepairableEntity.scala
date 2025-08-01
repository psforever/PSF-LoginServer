//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import net.psforever.objects.avatar.PlayerControl.sendResponse
import net.psforever.objects.ce.DeployableCategory
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.objects.vital.RepairFromEquipment
import net.psforever.objects.{Player, Tool}
import net.psforever.packet.game.{ChatMsg, InventoryStateMessage, RepairMessage}
import net.psforever.types.{ChatMessageType, PlanetSideEmpire, Vector3}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

/**
  * The "control" `Actor` mixin for repair-handling code,
  * for both expansion into other mixins and specific application on its own.
  *
  * @see `Player`
  * @see `Tool`
  */
trait RepairableEntity extends Repairable {

  /**
    * Catch the expected repair message and
    * apply further checks to the combination of the target, the equipment, and tis user.
    * If the checks pass, perform the repair.
    *
    * @param player the user of the nano dispenser tool
    * @param item the nano dispenser tool
    */
  def CanBeRepairedByNanoDispenser(player: Player, item: Tool): Unit = {
    val obj = RepairableObject
    if (CanPerformRepairs(obj, player, item)) {
      PerformRepairs(obj, player, item)
    }
  }

  /**
    * Test the combination of target entity, equipment user, and the equipment
    * to determine if the repair process attempt would be permitted.
    * It is not necessary to check that the tool and its ammunition are correct types;
    * that test was already performed.<br>
    * <br>
    * The target entity must be repairable and have less than full health
    * and, if it is destroyed, must have an object attribute that permits it to be repaired after being destroyed.<br>
    * The user must have the same faction affinity as the target entity or be neutral.<br>
    * The equipment must have some ammunition.<br>
    * The user must be alive and be within a certain distance of the target entity.
    * @see `org.log4s.getLogger`
    * @see `PlanetSideEmpire`
    * @see `Vector3.Distance`
    * @see `VitalityDefinition`
    * @param target the entity being repaired
    * @param player the user of the nano dispenser tool
    * @param item the nano dispenser tool
    * @return `true`, if the target entity can be repaired;
    *        `false`, otherwise
    */
  protected def CanPerformRepairs(target: Repairable.Target, player: Player, item: Tool): Boolean = {
    val definition = target.Definition
    definition.Repairable && target.Health < definition.MaxHealth && (definition.RepairIfDestroyed || !target.Destroyed) &&
    (target.Faction == player.Faction || target.Faction == PlanetSideEmpire.NEUTRAL) && item.Magazine > 0 &&
    player.isAlive && Vector3.Distance(target.Position, player.Position) < definition.RepairDistance
  }

  /**
    * Calculate the health points change and enact that repair action if the targets are stationary.
    * Restore the target entity to a not destroyed state if applicable.
    * Always show the repair progress bar window by using the appropriate packet.
    * @see `AvatarAction.PlanetsideAttributeToAll`
    * @see `AvatarAction.SendResponse`
    * @see `AvatarService`
    * @see `InventoryStateMessage`
    * @see `PlanetSideGameObject.isMoving`
    * @see `RepairMessage`
    * @see `Service.defaultPlayerGUID`
    * @see `Tool.Discharge`
    * @see `Zone.AvatarEvents`
    * @param target the entity being repaired
    * @param player the user of the nano dispenser tool
    * @param item the nano dispenser tool
    */
  protected def PerformRepairs(target: Repairable.Target, player: Player, item: Tool): Unit = {
    val definition     = target.Definition
    val events         = target.Zone.AvatarEvents
    val name           = player.Name
    val originalHealth = target.Health
    val updatedHealth =
      if (!(player.isMoving(test = 1f) || target.isMoving(test = 1f))) { //only allow stationary repairs within margin of error
        val repairValue = Repairable.applyLevelModifier(player, item, RepairToolValue(item)).toInt + target.Definition.RepairMod
        val magazine  = item.Discharge()
        events ! AvatarServiceMessage(
          player.Name,
          AvatarAction.SendResponse(
            Service.defaultPlayerGUID,
            InventoryStateMessage(item.AmmoSlot.Box.GUID, item.GUID, magazine.toLong)
          )
        )
        target.LogActivity(
          RepairFromEquipment(
            PlayerSource(player),
            item.Definition,
            repairValue
          )
        )
        PerformRepairs(target, repairValue)
      } else {
        originalHealth
      }
    //progress bar remains visible
    events ! AvatarServiceMessage(
      name,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        RepairMessage(target.GUID, updatedHealth * 100 / definition.MaxHealth)
      )
    )
    //if vehicle and vehicle is owned by another player, send repair chat message to the vehicle's owner
    if (target.Zone.Vehicles.exists(_.GUID == target.GUID)) {
      val vehicle = target.Zone.Vehicles.filter(_.GUID == target.GUID).head
      val vehicleOwner = vehicle.OwnerName.getOrElse("someone")
      val amount = updatedHealth - originalHealth
      if (vehicleOwner != "someone" && vehicle.OwnerGuid.get != player.GUID && amount > 0) {
        val repairMessageOther = s"@YourWasHealedMessage^@${vehicle.Definition.Name}~^repaired~^$amount~^armor~^${player.Name}~"
          sendResponse(vehicle.Zone, vehicleOwner, ChatMsg(ChatMessageType.UNK_227, repairMessageOther))
      }
    }
    //same check for field turret
    if (target.Zone.DeployableList.exists(_.GUID == target.GUID)) {
      val turret = target.Zone.DeployableList.filter(_.GUID == target.GUID).head
      val turretOwner = turret.OwnerName.getOrElse("someone")
      val amount = updatedHealth - originalHealth
      if (turret.Definition.DeployCategory == DeployableCategory.FieldTurrets && turretOwner != "someone"
      && turret.OwnerGuid.get != player.GUID && amount > 0) {
        val repairMessageOther = s"@YourWasHealedMessage^@${turret.Definition.Name}~^repaired~^$amount~^armor~^${player.Name}~"
        sendResponse(turret.Zone, turretOwner, ChatMsg(ChatMessageType.UNK_227, repairMessageOther))
      }
    }
  }

  protected def PerformRepairs(target: Repairable.Target, amount: Int): Int = {
    val zone      = target.Zone
    val zoneId    = zone.id
    val events    = zone.AvatarEvents
    val tguid     = target.GUID
    val newHealth = target.Health = target.Health + amount
    if (target.Destroyed) {
      if (newHealth >= target.Definition.RepairRestoresAt) {
        events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 0, newHealth))
        Restoration(target)
      }
    } else {
      events ! AvatarServiceMessage(zoneId, AvatarAction.PlanetsideAttributeToAll(tguid, 0, newHealth))
    }
    newHealth
  }
}
