// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.hackable

import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.serverobject.dome.ForceDomeControl
import net.psforever.objects.serverobject.structures.{Amenity, Building, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.zones.blockmap.BlockMapEntity
import net.psforever.packet.game.{GenericObjectActionMessage, HackMessage, HackState, HackState1, HackState7, TriggeredSound}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

import scala.annotation.unused
import scala.util.{Failure, Success}

object GenericHackables {
  private val log = org.log4s.getLogger("HackableBehavior")

  /**
   * na
   *
   * @param player the player doing the hacking
   * @param obj    the object being hacked
   * @return the percentage amount of progress per tick
   */
  def GetHackSpeed(player: Player, obj: PlanetSideServerObject): Float = {
    val playerHackLevel = player.avatar.hackingSkillLevel()
    val timeToHack = obj match {
      case vehicle: Vehicle   => vehicle.JackingDuration(playerHackLevel).toFloat
      case hackable: Hackable => hackable.HackDuration(playerHackLevel).toFloat
      case _ =>
        log.warn(
          s"${player.Name} tried to hack an object that has no hack time defined - ${obj.Definition.Name}@${obj.GUID.guid} in ${obj.Zone.id}"
        )
        0f
    }
    if (timeToHack == 0) {
      log.warn(
        s"${player.Name} tried to hack an object that they don't have the correct hacking level for - ${obj.Definition.Name}@${obj.GUID.guid} in ${obj.Zone.id}"
      )
      0f
    } else {
      //timeToHack is in seconds; progress is measured in quarters of a second (250ms)
      25f / timeToHack
    }
  }

  private def DontStopHackAttempt(@unused target: PlanetSideServerObject, @unused hacker: Player): Boolean = false

  /**
   * Evaluate the progress of the user applying a tool to modify some server object.
   * This action is using the remote electronics kit to convert an enemy unit into an allied unit, primarily.
   * The act of transforming allied units of one kind into allied units of another kind (facility turret upgrades)
   * is also governed by this action per tick of progress.
   * @see `HackMessage`
   * @see `HackState`
   * @param progressType 1 - remote electronics kit hack (various ...);
   *                     2 - nano dispenser (upgrade canister) turret upgrade
   * @param hacker the player performing the action
   * @param target the object being affected
   * @param tool_guid the tool being used to affest the object
   * @param progress the current progress value
   * @param additionalCancellationTests context-specific tests for hack continuation
   * @return `true`, if the next cycle of progress should occur;
   *         `false`, otherwise
   */
  def HackingTickAction(
                         progressType: HackState1,
                         hacker: Player,
                         target: PlanetSideServerObject,
                         tool_guid: PlanetSideGUID,
                         additionalCancellationTests: (PlanetSideServerObject, Player) => Boolean
                       )(
    progress: Float
  ): Boolean = {
    //hack state for progress bar visibility
    val (progressState, progressGrade) = if (progress <= 0L) {
      (HackState.Start, 0)
    } else if (progress >= 100L) {
      (HackState.Finished, 100)
    } else if (target.isMoving(test = 1f) || target.Destroyed || !target.HasGUID || additionalCancellationTests(target, hacker)) {
      (HackState.Cancelled, 0)
    } else {
      (HackState.Ongoing, progress.toInt)
    }
    target.Zone.AvatarEvents ! AvatarServiceMessage(
      hacker.Name,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        HackMessage(progressType, target.GUID, hacker.GUID, progressGrade, 0L, progressState, HackState7.Unk8)
      )
    )
    progressState != HackState.Cancelled
  }
  /**
   * Evaluate the progress of the user applying a tool to modify some server object.
   * This action is using the remote electronics kit to convert an enemy unit into an allied unit, primarily.
   * The act of transforming allied units of one kind into allied units of another kind (facility turret upgrades)
   * is also governed by this action per tick of progress.
   * @param progressType 1 - remote electronics kit hack (various ...);
   *                     2 - nano dispenser (upgrade canister) turret upgrade
   * @param hacker the player performing the action
   * @param target the object being affected
   * @param tool_guid the tool being used to affest the object
   * @param progress the current progress value
   * @return `true`, if the next cycle of progress should occur;
   *         `false`, otherwise
   */
  def HackingTickAction(
                         progressType: HackState1,
                         hacker: Player,
                         target: PlanetSideServerObject,
                         tool_guid: PlanetSideGUID
                       )(
                         progress: Float
                       ): Boolean = {
    HackingTickAction(progressType, hacker, target, tool_guid, DontStopHackAttempt)(progress)
  }

  /**
   * The force dome prevents hacking if its protection has been declared over a capitol.
   * Under normal circumstances, the dome will be visible in the sky at his point,
   * blocking enemy encounter within its boundaries,
   * so anything that can be hacked is on that boundary perimeter,
   * or an alternate method of entry (Router) has been compromised.
   * @see `ForceDomeControl.TargetUnderForceDome`
   * @see `Sector`
   * @param target the `Hackable` object that has been hacked
   * @param hacker the player performing the action
   * @return `true`, if the target is within boundary of a working force dome and thus protected;
   *         `false`, otherwise
   */
  def ForceDomeProtectsFromHacking(target: PlanetSideServerObject, hacker: Player): Boolean = {
    //explicitly allow friendly hacking which is typically clearing a hack
    target.Faction != hacker.Faction &&
      (target match {
        case obj: Amenity => obj.Owner.asInstanceOf[Building].ForceDome.toList
        case obj: BlockMapEntity => target.Zone.blockMap.sector(obj).buildingList.flatMap(_.ForceDome)
        case _ => List()
      })
        .filter(_.Perimeter.nonEmpty)
        .exists(dome => ForceDomeControl.TargetUnderForceDome(dome.Perimeter)(dome, target, maxDistance = 0f))
  }

  /**
   * The process of hacking an object is completed.
   * Pass the message onto the hackable object and onto the local events system.
   * @param target the `Hackable` object that has been hacked
   * @param user the player that is performing this hacking task
   * @param hackValue na;
   * @param hackClearValue na
   * @see `HackMessage`
   */
  //TODO add params here depending on which params in HackMessage are important
  def FinishHacking(target: PlanetSideServerObject with Hackable, user: Player, hackValue: Int, hackClearValue: Int)(): Unit = {
    import akka.pattern.ask
    import scala.concurrent.duration._
    // Wait for the target actor to set the HackedBy property, otherwise LocalAction.HackTemporarily will not complete properly
    import scala.concurrent.ExecutionContext.Implicits.global
    val tplayer = user
    ask(target.Actor, CommonMessages.Hack(tplayer, target))(timeout = 2 second)
      .mapTo[CommonMessages.EntityHackState]
      .onComplete {
        case Success(_) =>
          val zone   = target.Zone
          val zoneId = zone.id
          val pguid  = tplayer.GUID
          log.info(s"${user.Name} hacked a ${target.Definition.Name}")
          zone.LocalEvents ! LocalServiceMessage(
            zoneId,
            LocalAction.TriggerSound(pguid, target.HackSound, tplayer.Position, 30, 0.49803925f)
          )
          zone.LocalEvents ! LocalServiceMessage(
            zoneId,
            LocalAction
              .HackTemporarily(pguid, zone, target, hackValue, hackClearValue, target.HackEffectDuration(user.avatar.hackingSkillLevel()))
          )
        case Failure(_) =>
          log.warn(s"Hack message failed on target: ${target.Definition.Name}@${target.GUID.guid}")
      }
  }

  def FinishVirusAction(target: PlanetSideServerObject with Hackable, user: Player, hackValue: Int, hackClearValue: Int, virus: Long)(): Unit = {
    import akka.pattern.ask
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
    val tplayer = user
    ask(target.Actor, CommonMessages.Hack(tplayer, target))(timeout = 2 second)
      .mapTo[CommonMessages.EntityHackState]
      .onComplete {
        case Success(_) =>
          val building = target.asInstanceOf[Terminal].Owner.asInstanceOf[Building]
          val zone     = target.Zone
          val zoneId   = zone.id
          val pguid    = tplayer.GUID
          if (tplayer.Faction == target.Faction) {
            //clear virus
            val currVirus = building.virusId
            building.virusId = 8
            building.virusInstalledBy = None
            zone.LocalEvents ! LocalServiceMessage(
              zoneId,
              LocalAction
                .ClearTemporaryHack(pguid, target)
            )
            zone.LocalEvents ! LocalServiceMessage(
              zone.id,
              LocalAction.SendResponse(GenericObjectActionMessage(target.GUID, 60))
            )
            currVirus match {
              case 0L =>
                building.HackableAmenities.filter(d => d.Definition == GlobalDefinitions.lock_external).foreach { iff =>
                  zone.LocalEvents ! LocalServiceMessage(
                    zoneId,
                    LocalAction.ClearTemporaryHack(PlanetSideGUID(0), iff)
                  )
                }
              case 4L =>
                building.HackableAmenities.filter(d => d.Definition == GlobalDefinitions.order_terminal).foreach { term =>
                  zone.LocalEvents ! LocalServiceMessage(
                    zoneId,
                    LocalAction.ClearTemporaryHack(PlanetSideGUID(0), term)
                  )
                }
              case _ => ()
            }
            building.Actor ! BuildingActor.MapUpdate()
          }
          else {
            //install virus
            val currVirus = building.virusId
            //clear previous virus unlocks to prevent virus stacking
            currVirus match {
              case 0L =>
                if (virus != 0) {
                  building.HackableAmenities.filter(d => d.Definition == GlobalDefinitions.lock_external).foreach { iff =>
                    zone.LocalEvents ! LocalServiceMessage(
                      zoneId,
                      LocalAction.ClearTemporaryHack(PlanetSideGUID(0), iff)
                    )
                  }
                }
              case 4L =>
                if (virus != 4) {
                  building.HackableAmenities.filter(d => d.Definition == GlobalDefinitions.order_terminal).foreach { term =>
                    zone.LocalEvents ! LocalServiceMessage(
                      zoneId,
                      LocalAction.ClearTemporaryHack(PlanetSideGUID(0), term)
                    )
                  }
                }
              case _ => ()
            }
            val virusLength: Map[Long, Int] = Map(
              0L -> 3600,
              1L -> 900,
              2L -> 3600,
              3L -> 900,
              4L -> 120
            )
            val installedVirusDuration = virusLength(virus)
            val hackStateMap: Map[Long, HackState7] = Map(
              0L -> HackState7.UnlockDoors,
              1L -> HackState7.DisableLatticeBenefits,
              2L -> HackState7.NTUDrain,
              3L -> HackState7.DisableRadar,
              4L -> HackState7.AccessEquipmentTerms
            )
            val hackState = hackStateMap.getOrElse(virus, HackState7.Unk8)
            building.virusId = virus
            building.virusInstalledBy = Some(tplayer.Faction.id)
            zone.LocalEvents ! LocalServiceMessage(
              zoneId,
              LocalAction.TriggerSound(pguid, TriggeredSound.TREKSuccessful, tplayer.Position, 30, 0.49803925f)
            )
            zone.LocalEvents ! LocalServiceMessage(
              zoneId,
              LocalAction
                .HackTemporarily(pguid, zone, target, installedVirusDuration, hackClearValue, installedVirusDuration, unk2=hackState)
            )
            zone.LocalEvents ! LocalServiceMessage(
              zone.id,
              LocalAction.SendResponse(GenericObjectActionMessage(target.GUID, 61))
            )
            zone.LocalEvents ! LocalServiceMessage(
              zone.id,
              LocalAction.SendResponse(GenericObjectActionMessage(target.GUID, 58))
            )
            //amenities if applicable
            virus match {
              case 0L =>
                building.HackableAmenities.filter(d => d.Definition == GlobalDefinitions.lock_external).foreach{ iff =>
                  var setHacked = iff.asInstanceOf[PlanetSideServerObject with Hackable]
                  setHacked.HackedBy = tplayer
                    zone.LocalEvents ! LocalServiceMessage(
                      zoneId,
                      LocalAction.HackTemporarily(pguid, zone, iff, hackValue, hackClearValue, installedVirusDuration)
                    )
                }
              case 4L =>
                building.HackableAmenities.filter(d => d.Definition == GlobalDefinitions.order_terminal).foreach{ term =>
                  var setHacked = term.asInstanceOf[PlanetSideServerObject with Hackable]
                  setHacked.HackedBy = tplayer
                    zone.LocalEvents ! LocalServiceMessage(
                      zoneId,
                      LocalAction.HackTemporarily(pguid, zone, term, hackValue, hackClearValue, installedVirusDuration)
                    )
                }
              case _ => ()
            }
            building.Actor ! BuildingActor.MapUpdate()
          }
        case Failure(_) =>
          log.warn(s"Virus action failed on target: ${target.Definition.Name}@${target.GUID.guid}")
      }
  }

  /**
   * Check if the state of connected facilities has changed since the hack progress began. It accounts for a friendly facility
   * on the other side of a warpgate as well in case there are no friendly facilities in the same zone
   * @param target the `Hackable` object that has been hacked
   * @param hacker the player performing the action
   */
  def EndHackProgress(target: PlanetSideServerObject, hacker: Player): Boolean = {
    val building = target.asInstanceOf[CaptureTerminal].Owner.asInstanceOf[Building]
    if (building.Faction == PlanetSideEmpire.NEUTRAL || building.BuildingType == StructureType.Tower
        || building.Faction == hacker.Faction) {
      false
    }
    else {
      val stopHackingCount = building.Neighbours match {
        case Some(neighbors) =>
          neighbors.count {
            case wg: WarpGate if wg.Faction == hacker.Faction =>
              true
            case wg: WarpGate =>
              val friendlyBaseOpt = for {
                otherWg <- wg.Neighbours.flatMap(_.find(_.isInstanceOf[WarpGate]))
                friendly <- otherWg.Neighbours.flatMap(_.collectFirst { case b: Building if !b.isInstanceOf[WarpGate] => b })
              } yield friendly
              friendlyBaseOpt.exists { fb =>
                fb.Faction == hacker.Faction &&
                  !fb.CaptureTerminalIsHacked &&
                  fb.NtuLevel > 0
              }
            case b =>
              b.Faction == hacker.Faction &&
                !b.CaptureTerminalIsHacked &&
                b.NtuLevel > 0
          }
        case None => 0
      }
      stopHackingCount == 0
    }
  }
}
