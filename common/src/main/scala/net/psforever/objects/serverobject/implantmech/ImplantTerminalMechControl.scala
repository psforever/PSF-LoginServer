// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.implantmech

import akka.actor.Actor
import net.psforever.objects.{GlobalDefinitions, Player, SimpleItem}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.damage.DamageableMountable
import net.psforever.objects.serverobject.hackable.HackableBehavior
import net.psforever.objects.serverobject.repair.RepairableAmenity
import net.psforever.objects.serverobject.structures.Building

/**
  * An `Actor` that handles messages being dispatched to a specific `ImplantTerminalMech`.
  * @param mech the "mech" object being governed
  */
class ImplantTerminalMechControl(mech : ImplantTerminalMech) extends Actor
  with FactionAffinityBehavior.Check
  with MountableBehavior.Mount
  with MountableBehavior.Dismount
  with HackableBehavior.GenericHackable
  with DamageableMountable
  with RepairableAmenity {
  def MountableObject = mech
  def HackableObject = mech
  def FactionObject = mech
  def DamageableObject = mech
  def RepairableObject = mech

  def receive : Receive = checkBehavior
    .orElse(mountBehavior)
    .orElse(dismountBehavior)
    .orElse(hackableBehavior)
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse {
      case CommonMessages.Use(player, Some(item : SimpleItem)) if item.Definition == GlobalDefinitions.remote_electronics_kit =>
        //TODO setup certifications check
        mech.Owner match {
          case b : Building if (b.Faction != player.Faction || b.CaptureConsoleIsHacked) && mech.HackedBy.isEmpty =>
            sender ! CommonMessages.Hack(player, mech, Some(item))
          case _ => ;
        }
      case _ => ;
    }

  override protected def MountTest(obj : PlanetSideServerObject with Mountable, seatNumber : Int, player : Player) : Boolean = {
    val zone = obj.Zone
    zone.Map.TerminalToInterface.get(obj.GUID.guid) match {
      case Some(interface_guid) =>
        (zone.GUID(interface_guid) match {
          case Some(interface) => !interface.Destroyed
          case None => false
        }) &&
        super.MountTest(obj, seatNumber, player)
      case None =>
        false
    }
  }
}
