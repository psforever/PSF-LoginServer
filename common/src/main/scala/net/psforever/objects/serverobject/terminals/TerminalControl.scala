// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, SimpleItem}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.DamageableAmenity
import net.psforever.objects.serverobject.hackable.HackableBehavior
import net.psforever.objects.serverobject.repair.RepairableAmenity
import net.psforever.objects.serverobject.structures.Building

/**
  * An `Actor` that handles messages being dispatched to a specific `Terminal`.
  * @param term the `Terminal` object being governed
  */
class TerminalControl(term : Terminal) extends Actor
  with FactionAffinityBehavior.Check
  with HackableBehavior.GenericHackable
  with DamageableAmenity
  with RepairableAmenity {
  def FactionObject = term
  def HackableObject = term
  def DamageableObject = term
  def RepairableObject = term

  def receive : Receive = checkBehavior
    .orElse(hackableBehavior)
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse {
      case Terminal.Request(player, msg) =>
        sender ! Terminal.TerminalMessage(player, msg, term.Request(player, msg))

      case CommonMessages.Use(player, Some(item : SimpleItem)) if item.Definition == GlobalDefinitions.remote_electronics_kit =>
        //TODO setup certifications check
        term.Owner match {
          case b : Building if (b.Faction != player.Faction || b.CaptureConsoleIsHacked) && term.HackedBy.isEmpty =>
            sender ! CommonMessages.Hack(player, term, Some(item))
          case _ => ;
        }

//      case CommonMessages.Use(player, None) if term.Faction == player.Faction =>
//        val tdef = term.Definition
//        if(tdef.isInstanceOf[MatrixTerminalDefinition]) {
//          //TODO matrix spawn point; for now, just blindly bind to show work (and hope nothing breaks)
//          term.Zone.AvatarEvents ! AvatarServiceMessage(
//            player.Name,
//            AvatarAction.SendResponse(Service.defaultPlayerGUID, BindPlayerMessage(BindStatus.Bind, "", true, true, SpawnGroup.Sanctuary, 0, 0, term.Position))
//          )
//        }
//        else if(tdef == GlobalDefinitions.multivehicle_rearm_terminal || tdef == GlobalDefinitions.bfr_rearm_terminal ||
//          tdef == GlobalDefinitions.air_rearm_terminal || tdef == GlobalDefinitions.ground_rearm_terminal) {
//          FindLocalVehicle match {
//            case Some(vehicle) =>
//              sendResponse(UseItemMessage(player.GUID, PlanetSideGUID(0), object_guid, unk2, unk3, unk4, unk5, unk6, unk7, unk8, itemType))
//              sendResponse(UseItemMessage(player.GUID, PlanetSideGUID(0), vehicle.GUID, unk2, unk3, unk4, unk5, unk6, unk7, unk8, vehicle.Definition.ObjectId))
//            case None =>
//              log.error("UseItem: expected seated vehicle, but found none")
//          }
//        }
//        else if(tdef == GlobalDefinitions.teleportpad_terminal) {
//          //explicit request
//          term.Actor ! Terminal.Request(
//            player,
//            ItemTransactionMessage(term.GUID, TransactionType.Buy, 0, "router_telepad", 0, PlanetSideGUID(0))
//          )
//        }
//        else {
//          sendResponse(UseItemMessage(player.GUID, PlanetSideGUID(0), term.GUID, unk2, unk3, unk4, unk5, unk6, unk7, unk8, tdef.ObjectId))
//        }

      case _ => ;
    }

  override def toString : String = term.Definition.Name
}
