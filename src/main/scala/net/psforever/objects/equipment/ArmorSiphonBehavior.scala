// Copyright (c) 2021 PSForever
package net.psforever.objects.equipment

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.ballistics.VehicleSource
import net.psforever.objects.{GlobalDefinitions, Tool, Vehicle}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.vital.RepairFromArmorSiphon
import net.psforever.objects.vital.etc.{ArmorSiphonModifiers, ArmorSiphonReason}
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.packet.game.QuantityUpdateMessage
import net.psforever.services.Service
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.PlanetSideGUID

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object ArmorSiphonBehavior {
  sealed case class RepairedByArmorSiphon(cause: DamageInteraction, amount: Int)

  sealed case class Recharge(guid: PlanetSideGUID)

  trait Target {
    _: Actor with Damageable =>
    def SiphonableObject: Vehicle

    val siphoningBehavior: Receive = {
      case CommonMessages.Use(player, Some(item : Tool))
        if GlobalDefinitions.isBattleFrameArmorSiphon(item.Definition) && player.Faction != DamageableObject.Faction =>
        val obj = SiphonableObject
        val zone = obj.Zone
        val iguid = item.GUID
        //see Damageable.takesDamage
        zone.Vehicles.find { v =>
          v.Weapons.values.exists { slot => slot.Equipment.nonEmpty && slot.Equipment.get.GUID == iguid}
        } match {
          case Some(v: Vehicle) if v.CanDamage =>
            //remember: we are the vehicle being siphoned; we need the vehicle doing the siphoning
            val before = item.Magazine
            val after = item.Discharge()
            if (before > after) {
              v.Actor ! ArmorSiphonBehavior.Recharge(iguid)
              PerformDamage(
                obj,
                DamageInteraction(
                  VehicleSource(obj),
                  ArmorSiphonReason(v, item, obj.DamageModel),
                  obj.Position
                ).calculate()
              )
            }
          case _ => ;
        }
    }
  }

  trait SiphonOwner {
    _: Actor =>
    def SiphoningObject: Vehicle

    private val siphonRecharge: mutable.HashMap[PlanetSideGUID, Cancellable] = mutable.HashMap[PlanetSideGUID, Cancellable]()

    def repairPostStop(): Unit = {
      siphonRecharge.keys.foreach { endSiphonRecharge }
    }

    val siphonRepairBehavior: Receive = {
      case RepairedByArmorSiphon(cause, amount) =>
        val obj = SiphoningObject
        val before = obj.Health
        cause.cause match {
          case asr: ArmorSiphonReason
            if before < obj.MaxHealth =>
            val after = obj.Health += amount
            if(before < after) {
              obj.History(RepairFromArmorSiphon(asr.siphon.Definition, before - after))
              val zone = obj.Zone
              zone.VehicleEvents ! VehicleServiceMessage(
                zone.id,
                VehicleAction.PlanetsideAttribute(Service.defaultPlayerGUID, obj.GUID, 0, after)
              )
            }
          case _ => ;
        }

      case ArmorSiphonBehavior.Recharge(guid) =>
        siphonRecharge.remove(guid) match {
          case Some(timer) => timer.cancel()
          case None => ;
        }
        val obj = SiphoningObject
        obj.Weapons.values.find { slot => slot.Equipment.nonEmpty && slot.Equipment.get.GUID == guid } match {
          case Some(siphonSlot) =>
            val siphon = siphonSlot.Equipment.get.asInstanceOf[Tool]
            val zone = obj.Zone
            //update current charge level
            zone.VehicleEvents ! VehicleServiceMessage(
              obj.Actor.toString,
              VehicleAction.SendResponse(Service.defaultPlayerGUID, QuantityUpdateMessage(siphon.AmmoSlot.Box.GUID, siphon.Magazine))
            )
            siphonRecharge.put(guid, context.system.scheduler.scheduleWithFixedDelay(
              initialDelay = 3000 milliseconds,
              delay = 200 milliseconds,
              self,
              SiphonOwner.Recharge(guid)
            ))
          case _ => ;
        }

      case SiphonOwner.Recharge(guid) =>
        val obj = SiphoningObject
        val zone = obj.Zone
        obj.Weapons.values.find { slot => slot.Equipment.nonEmpty && slot.Equipment.get.GUID == guid } match {
          case Some(slot: EquipmentSlot) =>
            val siphon = slot.Equipment.get.asInstanceOf[Tool]
            val before = siphon.Magazine
            val after = siphon.Magazine = before + 1
            if (after > before) {
              zone.VehicleEvents ! VehicleServiceMessage(
                obj.Actor.toString,
                VehicleAction.SendResponse(Service.defaultPlayerGUID, QuantityUpdateMessage(siphon.AmmoSlot.Box.GUID, after))
              )
              if (after == siphon.MaxMagazine) {
                endSiphonRecharge(guid)
              }
            }

          case _ =>
            endSiphonRecharge(guid)
        }
    }

    def endSiphonRecharge(guid: PlanetSideGUID): Unit = {
      siphonRecharge.remove(guid) match {
        case Some(c) => c.cancel()
        case None => ;
      }
    }
  }

  object SiphonOwner {
    private case class Recharge(guid: PlanetSideGUID)
  }
}

case object ArmorSiphonRepairHost extends ArmorSiphonModifiers.Mod {
  def calculate(damage: Int, data: DamageInteraction, cause: ArmorSiphonReason): Int = {
    if (damage > 0) {
      cause.hostVehicle.Actor ! ArmorSiphonBehavior.RepairedByArmorSiphon(data, damage)
    }
    damage
  }
}
