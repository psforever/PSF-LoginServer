// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.ActorRef
import net.psforever.objects.avatar.{Avatar, Certification}

import scala.concurrent.duration._
import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

object Deployables {
  //private val log = org.log4s.getLogger("Deployables")

  object Make {
    def apply(item: DeployedItem.Value): () => Deployable = cemap(item)

    private val cemap: Map[DeployedItem.Value, () => Deployable] = Map(
      DeployedItem.boomer                 -> { () => new BoomerDeployable(GlobalDefinitions.boomer) },
      DeployedItem.he_mine                -> { () => new ExplosiveDeployable(GlobalDefinitions.he_mine) },
      DeployedItem.jammer_mine            -> { () => new ExplosiveDeployable(GlobalDefinitions.jammer_mine) },
      DeployedItem.spitfire_turret        -> { () => new TurretDeployable(GlobalDefinitions.spitfire_turret) },
      DeployedItem.spitfire_cloaked       -> { () => new TurretDeployable(GlobalDefinitions.spitfire_cloaked) },
      DeployedItem.spitfire_aa            -> { () => new TurretDeployable(GlobalDefinitions.spitfire_aa) },
      DeployedItem.motionalarmsensor      -> { () => new SensorDeployable(GlobalDefinitions.motionalarmsensor) },
      DeployedItem.sensor_shield          -> { () => new SensorDeployable(GlobalDefinitions.sensor_shield) },
      DeployedItem.tank_traps             -> { () => new TrapDeployable(GlobalDefinitions.tank_traps) },
      DeployedItem.portable_manned_turret -> { () => new TurretDeployable(GlobalDefinitions.portable_manned_turret) },
      DeployedItem.portable_manned_turret -> { () => new TurretDeployable(GlobalDefinitions.portable_manned_turret) },
      DeployedItem.portable_manned_turret_nc -> { () =>
        new TurretDeployable(GlobalDefinitions.portable_manned_turret_nc)
      },
      DeployedItem.portable_manned_turret_tr -> { () =>
        new TurretDeployable(GlobalDefinitions.portable_manned_turret_tr)
      },
      DeployedItem.portable_manned_turret_vs -> { () =>
        new TurretDeployable(GlobalDefinitions.portable_manned_turret_vs)
      },
      DeployedItem.deployable_shield_generator -> { () =>
        new ShieldGeneratorDeployable(GlobalDefinitions.deployable_shield_generator)
      },
      DeployedItem.router_telepad_deployable -> { () =>
        new TelepadDeployable(GlobalDefinitions.router_telepad_deployable)
      }
    ).withDefaultValue({ () => new ExplosiveDeployable(GlobalDefinitions.boomer) })
  }

  /**
    * Distribute information that a deployable has been destroyed.
    * Additionally, since the player who destroyed the deployable isn't necessarily the owner,
    * and the real owner will still be aware of the existence of the deployable,
    * that player must be informed of the loss of the deployable directly.
    * @see `AnnounceDestroyDeployable(Deployable)`
    * @see `Deployable.Deconstruct`
    * @param target the deployable that is destroyed
    * @param time length of time that the deployable is allowed to exist in the game world;
    *             `None` indicates the normal un-owned existence time (180 seconds)
    */
  def AnnounceDestroyDeployable(target: Deployable, time: Option[FiniteDuration]): Unit = {
    AnnounceDestroyDeployable(target)
    target.Actor ! Deployable.Deconstruct(time)
  }

  /**
    * Distribute information that a deployable has been destroyed.
    * The deployable may not have yet been eliminated from the game world (client or server),
    * but its health is zero and it has entered the conditions where it is nearly irrelevant.<br>
    * <br>
    * The typical use case of this function involves destruction via weapon fire, attributed to a particular player.
    * Contrast this to simply destroying a deployable by being the deployable's owner and using the map icon controls.
    * This function eventually invokes the same routine
    * but mainly goes into effect when the deployable has been destroyed
    * and may still leave a physical component in the game world to be cleaned up later.
    * @see `DeployableInfo`
    * @see `DeploymentAction`
    * @see `LocalAction.DeployableMapIcon`
    * @param target the deployable that is destroyed
    **/
  def AnnounceDestroyDeployable(target: Deployable): Unit = {
    val zone = target.Zone
    val events = zone.LocalEvents
    val item = target.Definition.Item
    target.OwnerName
      .foreach { owner =>
        zone.LivePlayers.find { p => owner.equals(p.Name) }
          .orElse(zone.Corpses.find { c => owner.equals(c.Name) })
          .foreach { p =>
            p.Actor ! Player.LoseDeployable(target)
          }
        target.AssignOwnership(None)
      }
    events ! LocalServiceMessage(
      s"${target.Faction}",
      LocalAction.DeployableMapIcon(DeploymentAction.Dismiss, DeployableInfo(target.GUID, Deployable.Icon(item), target.Position, PlanetSideGUID(0)))
    )
  }

  /**
    * Collect all deployables previously owned by the player,
    * dissociate the avatar's globally unique identifier to remove turnover ownership,
    * and, on top of performing the above manipulations, dispose of any boomers discovered.
    * (`BoomerTrigger` objects, the companions of the boomers, should be handled by an external implementation
    * if they had not already been handled by the time this function is executed.)
    * @return all previously-owned deployables after they have been processed;
    *         boomers are listed before all other deployable types
    */
  //noinspection ScalaUnusedSymbol
  def Disown(zone: Zone, avatar: Avatar, replyTo: ActorRef): List[Deployable] = {
    avatar.deployables
      .Clear()
      .map(zone.GUID)
      .collect {
        case Some(obj: Deployable) =>
          obj.Actor ! Deployable.Ownership(None)
          obj.OwnerGuid = None //fast-forward the effect
          obj
      }
  }

  /**
    * Initialize the deployables backend information.
    * @param avatar the player's core
    */
  def InitializeDeployableQuantities(avatar: Avatar): Boolean = {
    avatar.deployables.Initialize(avatar.certifications)
  }

  /**
    * Initialize the UI elements for deployables.
    * @param avatar the player's core
    */
  def InitializeDeployableUIElements(avatar: Avatar): List[(Int, Int, Int, Int)] = {
    avatar.deployables.UpdateUI()
  }

  /**
    * If the default ammunition mode for the `ConstructionTool` is not supported by the given certifications,
    * find a suitable ammunition mode and switch to it internally.
    * No special complaint is raised if the `ConstructionItem` itself is completely unsupported.
    * The search function will explore every ammo option for every fire mode option
    * and will stop when it finds either a valid option or when arrives back at the original fire mode.
    * @param certs the certification baseline being compared against
    * @param obj the `ConstructionItem` entity
    * @return `true`, if the firemode and ammunition mode of the item is valid;
    *        `false`, otherwise
    */
  def initializeConstructionItem(
                                  certs: Set[Certification],
                                  obj: ConstructionItem
                                ): Boolean = {
    val initialFireModeIndex = obj.FireModeIndex
    if (!Deployables.constructionItemPermissionComparison(certs, obj.ModePermissions)) {
      while (!Deployables.constructionItemPermissionComparison(certs, obj.ModePermissions) &&
             !Deployables.performConstructionItemAmmoChange(certs, obj, obj.AmmoTypeIndex) &&
             {
               obj.NextFireMode
               initialFireModeIndex != obj.FireModeIndex
             }) {
        /* change in fire mode occurs in conditional */
      }
      Deployables.constructionItemPermissionComparison(certs, obj.ModePermissions)
    } else {
      true
    }
  }

  /**
    * The custom behavior responding to the packet `ChangeAmmoMessage` for `ConstructionItem` game objects.
    * Iterate through sub-modes corresponding to a type of "deployable" as ammunition for this fire mode
    * and check each of these sub-modes for their certification requirements to be met before they can be used.
    * Additional effort is exerted to ensure that the requirements for the given ammunition are satisfied.
    * If no satisfactory combination is achieved, the original state will be restored.
    * @see `Certification`
    * @see `ChangeAmmoMessage`
    * @see `ConstructionItem.ModePermissions`
    * @see `Deployables.constructionItemPermissionComparison`
    * @param certs the certification baseline being compared against
    * @param obj the `ConstructionItem` entity
    * @param originalAmmoIndex the starting point ammunition type mode index
    * @return `true`, if the ammunition mode of the item has been changed;
    *        `false`, otherwise
    */
  def performConstructionItemAmmoChange(
                                         certs: Set[Certification],
                                         obj: ConstructionItem,
                                         originalAmmoIndex: Int
                                       ): Boolean = {
    do {
      obj.NextAmmoType
    } while (
      !Deployables.constructionItemPermissionComparison(certs, obj.ModePermissions) &&
      originalAmmoIndex != obj.AmmoTypeIndex
    )
    obj.AmmoTypeIndex != originalAmmoIndex
  }

  /**
    * The custom behavior responding to the message `ChangeFireModeMessage` for `ConstructionItem` game objects.
    * Each fire mode has sub-modes corresponding to a type of "deployable" as ammunition
    * and each of these sub-modes have certification requirements that must be met before they can be used.
    * Additional effort is exerted to ensure that the requirements for the given mode and given sub-mode are satisfied.
    * If no satisfactory combination is achieved, the original state will be restored.
    * @see `Deployables.constructionItemPermissionComparison`
    * @see `Deployables.performConstructionItemAmmoChange`
    * @see `FireModeSwitch.NextFireMode`
    * @param certs the certification baseline being compared against
    * @param obj the `ConstructionItem` entity
    * @param originalModeIndex the starting point fire mode index
    * @return `true`, if the ammunition mode of the item has been changed;
    *        `false`, otherwise
    */
  def performConstructionItemFireModeChange(
                                             certs: Set[Certification],
                                             obj: ConstructionItem,
                                             originalModeIndex: Int
                                           ): Boolean = {
    /*
    if any of the fire modes possess an initial option that is not valid for a given set of certifications,
    but a subsequent option is valid, the do...while loop has to be modified to traverse and compare each option
    */
    do {
      obj.NextFireMode
    } while (
      !Deployables.constructionItemPermissionComparison(certs, obj.ModePermissions) &&
      originalModeIndex != obj.FireModeIndex
    )
    originalModeIndex != obj.FireModeIndex
  }

  /**
    * Compare sets of certifications to determine if
    * the requested `Engineering`-like certification requirements of the one group can be found in a another group.
    * @see `CertificationType`
    * @param sample the certifications to be compared against
    * @param test the desired certifications
    * @return `true`, if the desired certification requirements are met; `false`, otherwise
    */
  def constructionItemPermissionComparison(
                                            sample: Set[Certification],
                                            test: Set[Certification]
                                          ): Boolean = {
    import Certification._
    val engineeringCerts: Set[Certification] = Set(AssaultEngineering, FortificationEngineering)
    val testDiff: Set[Certification]         = test diff (engineeringCerts ++ Set(AdvancedEngineering))
    //substitute `AssaultEngineering` and `FortificationEngineering` for `AdvancedEngineering`
    val sampleIntersect = if (sample contains AdvancedEngineering) {
      engineeringCerts
    } else {
      sample intersect engineeringCerts
    }
    val testIntersect = if (test contains AdvancedEngineering) {
      engineeringCerts
    } else {
      test intersect engineeringCerts
    }
    (sample intersect testDiff equals testDiff) && (sampleIntersect intersect testIntersect equals testIntersect)
  }

  /**
   * Find a player with a given name in this zone.
   * The assumption is the player is the owner of a given deployable entity.
   * If the player can not be found, the deployable entity can stand in as it's own owner.
   * @param zone continent in which the player should be found;
   *             should be the same zone as the deployable, but not required
   * @param nameOpt optional player's name
   * @param deployableSource deployable entity
   * @return discovered player as a reference
   */
  def AssignBlameTo(zone: Zone, nameOpt: Option[String], deployableSource: SourceEntry): SourceEntry = {
    zone
      .Players
      .find(a => nameOpt.contains(a.name))
      .collect { a =>
        val name = a.name
        Deployables.AssignBlameToFrom(name, zone.LivePlayers)
          .orElse(Deployables.AssignBlameToFrom(name, zone.Corpses))
          .getOrElse {
            val player = PlayerSource(name, deployableSource.Faction, deployableSource.Position) //might report minor inconsistencies, e.g., exo-suit type
            player.copy(unique = player.unique.copy(charId = a.id), progress = a.scorecard.CurrentLife)
          }
      }
      .getOrElse(deployableSource)
  }

  /**
   * Find a player with a given name from this list of possible players.
   * If the player is seated, attach a shallow copy of the mounting information.
   * @param name player name
   * @param blameList possible players in which to find the player name
   * @return discovered player as a reference, or `None` if not found
   */
  private def AssignBlameToFrom(name: String, blameList: List[Player]): Option[SourceEntry] = {
    blameList
      .find(_.Name.equals(name))
      .map { player =>
        PlayerSource
          .mountableAndSeat(player)
          .map { case (mount, seat) => PlayerSource.inSeat(player, mount, seat) }
          .getOrElse { PlayerSource(player) }
      }
  }
}
