// Copyright (c) 2019 PSForever
package net.psforever.actors.session

import java.util.concurrent.atomic.AtomicInteger
import akka.actor.Cancellable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.{ActorRef, Behavior, PostStop, SupervisorStrategy}
import net.psforever.objects.vital.{DamagingActivity, HealingActivity}
import org.joda.time.{LocalDateTime, Seconds}
//import org.log4s.Logger
import scala.collection.mutable
import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.{Failure, Success}
import scala.concurrent.duration._
//
import net.psforever.objects.avatar.{Friend => AvatarFriend, Ignored => AvatarIgnored, Shortcut => AvatarShortcut, _}
import net.psforever.objects.definition.converter.CharacterSelectConverter
import net.psforever.objects.definition._
import net.psforever.objects.inventory.Container
import net.psforever.objects.equipment.{Equipment, EquipmentSlot}
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.loadouts.{InfantryLoadout, Loadout, VehicleLoadout}
import net.psforever.objects._
import net.psforever.objects.ballistics.PlayerSource
import net.psforever.objects.locker.LockerContainer
import net.psforever.objects.vital.HealFromImplant
import net.psforever.packet.game.objectcreate.{ObjectClass, RibbonBars}
import net.psforever.packet.game.{Friend => GameFriend, _}
import net.psforever.types.{MemberAction, PlanetSideEmpire, _}
import net.psforever.util.Database._
import net.psforever.persistence
import net.psforever.util.{Config, Database, DefinitionUtil}
import net.psforever.services.{Service, ServiceManager}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

object AvatarActor {
  def apply(sessionActor: ActorRef[SessionActor.Command]): Behavior[Command] =
    Behaviors
      .supervise[Command] {
        Behaviors.withStash(100) { buffer =>
          Behaviors.setup(context => new AvatarActor(context, buffer, sessionActor).start())
        }
      }
      .onFailure[Exception](SupervisorStrategy.restart)

  sealed trait Command

  /** Subscribe to avatar updates */
  final case class Subscribe(actor: ActorRef[Avatar]) extends Command

  /** Unsubscribe from avatar updates */
  final case class Unsubscribe(actor: ActorRef[Avatar]) extends Command

  // TODO temporary solution for passing session from SessionActor, should use Topic
  final case class SetSession(session: Session) extends Command

  /** Set account this avatar belongs to. Required for handling most other messages. */
  final case class SetAccount(account: Account) extends Command

  /** Create avatar */
  final case class CreateAvatar(
      name: String,
      head: Int,
      voice: CharacterVoice.Value,
      gender: CharacterSex,
      empire: PlanetSideEmpire.Value
  ) extends Command

  /** Delete avatar */
  final case class DeleteAvatar(charId: Int) extends Command

  /** Load basic avatar info */
  final case class SelectAvatar(charId: Int, replyTo: ActorRef[AvatarResponse]) extends Command

  /** Log in the currently selected avatar. Must have first sent SelectAvatar. */
  final case class LoginAvatar(replyTo: ActorRef[AvatarLoginResponse]) extends Command

  /** Send implants to client - TODO this can be done better using a event system on SessionActor */
  final case class CreateImplants() extends Command

  /** Replace avatar instance with the provided one */
  final case class ReplaceAvatar(avatar: Avatar) extends Command

  /** Add a first time event (help text) */
  final case class AddFirstTimeEvent(event: String) extends Command

  /** Add a certification using a terminal */
  final case class LearnCertification(terminalGuid: PlanetSideGUID, certification: Certification) extends Command

  /** Remove a certification using a terminal */
  final case class SellCertification(terminalGuid: PlanetSideGUID, certification: Certification) extends Command

  /** Force-set certifications */
  final case class SetCertifications(certifications: Set[Certification]) extends Command

  /** Save a loadout */
  final case class SaveLoadout(player: Player, loadoutType: LoadoutType.Value, label: Option[String], number: Int)
      extends Command

  /** Delete a loadout */
  final case class DeleteLoadout(player: Player, loadoutType: LoadoutType.Value, number: Int) extends Command

  /** Refresh the client's loadouts, excluding empty entries */
  final case class InitialRefreshLoadouts() extends Command

  /** Refresh all of the client's loadouts */
  final case class RefreshLoadouts() extends Command

  /** Take all the entries in the player's locker and write it to the database */
  final case class SaveLocker() extends Command

  /** Set purchase time for the use of calculating cooldowns */
  final case class UpdatePurchaseTime(definition: BasicDefinition, time: LocalDateTime = LocalDateTime.now())
      extends Command

  /** Set use time for the use of calculating cooldowns */
  final case class UpdateUseTime(definition: BasicDefinition, time: LocalDateTime = LocalDateTime.now()) extends Command

  /** Force refresh the client's item purchase times */
  final case class RefreshPurchaseTimes() extends Command

  /** Set vehicle */
  final case class SetVehicle(vehicle: Option[PlanetSideGUID]) extends Command

  /** Add a implant using a terminal */
  final case class LearnImplant(terminalGuid: PlanetSideGUID, definition: ImplantDefinition) extends Command

  /** Remove a implant using a terminal */
  final case class SellImplant(terminalGuid: PlanetSideGUID, definition: ImplantDefinition) extends Command

  /** Activate an implant (must already be initialized) */
  final case class ActivateImplant(implantType: ImplantType) extends Command

  /** Deactivate an implant */
  final case class DeactivateImplant(implantType: ImplantType) extends Command

  /** Deactivate all non-passive implants that are in use */
  final case class DeactivateActiveImplants() extends Command

  /** Start implant initialization timers (after zoning or respawn) */
  final case class InitializeImplants() extends Command

  /** Deinitialize implants (before zoning or respawning) */
  final case class DeinitializeImplants() extends Command

  /** Deinitialize a certain implant, then initialize it again */
  final case class ResetImplant(implant: ImplantType) extends Command

  /** Shorthand for DeinitializeImplants and InitializeImplants */
  final case class ResetImplants() extends Command

  /** Set the avatar's lookingForSquad */
  final case class SetLookingForSquad(lfs: Boolean) extends Command

  /** Restore up to the given stamina amount due to natural recharge */
  private case class RestoreStaminaPeriodically(stamina: Int) extends Command

  /** Restore up to the given stamina amount for some reason */
  final case class RestoreStamina(stamina: Int) extends Command

  /** Consume up to the given stamina amount */
  final case class ConsumeStamina(stamina: Int) extends Command

  /** Suspend stamina regeneration for a given time */
  final case class SuspendStaminaRegeneration(duration: FiniteDuration) extends Command

  /** Award battle experience points */
  final case class AwardBep(bep: Long) extends Command

  /** Set total battle experience points */
  final case class SetBep(bep: Long) extends Command

  /** Award command experience points */
  final case class AwardCep(bep: Long) extends Command

  /** Set total command experience points */
  final case class SetCep(bep: Long) extends Command

  /** Set cosmetics. Only allowed for BR24 or higher. */
  final case class SetCosmetics(personalStyles: Set[Cosmetic]) extends Command

  final case class SetRibbon(ribbon: MeritCommendation.Value, bar: RibbonBarSlot.Value) extends Command

  private case class ServiceManagerLookupResult(result: ServiceManager.LookupResult) extends Command

  final case class SetStamina(stamina: Int) extends Command

  private case class SetImplantInitialized(implantType: ImplantType) extends Command

  final case class MemberListRequest(action: MemberAction.Value, name: String) extends Command

  final case class AddShortcut(slot: Int, shortcut: Shortcut) extends Command

  final case class RemoveShortcut(slot: Int) extends Command

  final case class AvatarResponse(avatar: Avatar)

  final case class AvatarLoginResponse(avatar: Avatar)

  /**
    * A player loadout represents all of the items in the player's hands (equipment slots)
    * and all of the items in the player's backpack (inventory)
    * with items separated by meaningful punctuation marks.
    * The CLOB - character large object - is a string of such item data
    * that can be translated back into the original items
    * and placed back in the same places in the inventory from which they were extracted.
    * Together, these are occasionally referred to as an "inventory".
    * @param owner the player whose inventory is being transcribed
    * @return the resulting text data that represents an inventory
    */
  def buildClobFromPlayerLoadout(owner: Player): String = {
    val clobber: mutable.StringBuilder = new mutable.StringBuilder()
    //encode holsters
    owner
      .Holsters()
      .zipWithIndex
      .collect {
        case (slot, index) if slot.Equipment.nonEmpty =>
          clobber.append(encodeLoadoutClobFragment(slot.Equipment.get, index))
      }
    //encode inventory
    owner.Inventory.Items.foreach {
      case InventoryItem(obj, index) =>
        clobber.append(encodeLoadoutClobFragment(obj, index))
    }
    clobber.mkString.drop(1) //drop leading punctuation
  }

  /**
    * Transform from encoded inventory data as a CLOB - character large object - into individual items.
    * Install those items into positions in a target container
    * in the same positions in which they were previously recorded.<br>
    * <br>
    * There is no guarantee that the structure of the retained container data encoded in the CLOB
    * will fit the current dimensions of the container.
    * No tests are performed.
    * A partial decompression of the CLOB may occur.
    * @param container the container in which to place the pieces of equipment produced from the CLOB
    * @param clob the inventory data in string form
    * @param log a reference to a logging context
    */
  def buildContainedEquipmentFromClob(container: Container, clob: String, log: org.log4s.Logger): Unit = {
    clob.split("/").filter(_.trim.nonEmpty).foreach { value =>
      val (objectType, objectIndex, objectId, toolAmmo) = value.split(",") match {
        case Array(a, b: String, c: String)    => (a, b.toInt, c.toInt, None)
        case Array(a, b: String, c: String, d) => (a, b.toInt, c.toInt, Some(d))
        case _ =>
          log.warn(s"ignoring invalid item string: '$value'")
          return
      }

      objectType match {
        case "Tool" =>
          container.Slot(objectIndex).Equipment =
            Tool(DefinitionUtil.idToDefinition(objectId).asInstanceOf[ToolDefinition])
        case "AmmoBox" =>
          container.Slot(objectIndex).Equipment =
            AmmoBox(DefinitionUtil.idToDefinition(objectId).asInstanceOf[AmmoBoxDefinition])
        case "ConstructionItem" =>
          container.Slot(objectIndex).Equipment = ConstructionItem(
            DefinitionUtil.idToDefinition(objectId).asInstanceOf[ConstructionItemDefinition]
          )
        case "SimpleItem" =>
          container.Slot(objectIndex).Equipment =
            SimpleItem(DefinitionUtil.idToDefinition(objectId).asInstanceOf[SimpleItemDefinition])
        case "Kit" =>
          container.Slot(objectIndex).Equipment =
            Kit(DefinitionUtil.idToDefinition(objectId).asInstanceOf[KitDefinition])
        case "Telepad" | "BoomerTrigger" => ;
        //special types of equipment that are not actually loaded
        case name =>
          log.error(s"failing to add unknown equipment to a container - $name")
      }

      toolAmmo foreach { toolAmmo =>
        toolAmmo.split("_").drop(1).foreach { value =>
          val (ammoSlots, ammoTypeIndex, ammoBoxDefinition) = value.split("-") match {
            case Array(a: String, b: String, c: String) => (a.toInt, b.toInt, c.toInt)
          }
          container.Slot(objectIndex).Equipment.get.asInstanceOf[Tool].AmmoSlots(ammoSlots).AmmoTypeIndex =
            ammoTypeIndex
          container.Slot(objectIndex).Equipment.get.asInstanceOf[Tool].AmmoSlots(ammoSlots).Box =
            AmmoBox(AmmoBoxDefinition(ammoBoxDefinition))
        }
      }
    }
  }

  /**
    * Transform the encoded object to time data
    * into proper object to proper time references
    * and filter out mappings that have exceeded the sample duration.
    * @param clob the entity to time data in string form
    * @param cooldownDurations a base reference for entity to time comparison
    * @param log a reference to a logging context
    * @return the resulting text data that represents object to time mappings
    */
  def buildCooldownsFromClob(
                              clob: String,
                              cooldownDurations: Map[BasicDefinition,FiniteDuration],
                              log: org.log4s.Logger
                            ): Map[String, LocalDateTime] = {
    val now = LocalDateTime.now()
    val cooldowns: mutable.Map[String, LocalDateTime] = mutable.Map()
    clob.split("/").filter(_.trim.nonEmpty).foreach { value =>
      value.split(",") match {
        case Array(name: String, b: String) =>
          try {
            val cooldown = LocalDateTime.parse(b)
            cooldownDurations.get(DefinitionUtil.fromString(name)) match {
              case Some(duration) if now.compareTo(cooldown.plusMillis(duration.toMillis.toInt)) == -1 =>
                cooldowns.put(name, cooldown)
              case _ => ;
            }
          } catch {
            case _: Exception => ;
          }
        case _ =>
          log.warn(s"ignoring invalid cooldown string: '$value'")
      }
    }
    cooldowns.toMap
  }

  /**
    * Transform the proper object to proper time references
    * into encoded object to time data in a string format
    * and filter out mappings that have exceeded the current time.
    * @param cooldowns a base reference for entity to time comparison
    * @return the resulting map that represents object to time string data
    */
  def buildClobfromCooldowns(cooldowns: Map[String, LocalDateTime]): String = {
    val now = LocalDateTime.now()
    cooldowns
      .filter { case (_, cd) => cd.compareTo(now) == -1 }
      .map { case (name, cd) => s"$name,$cd" }
      .mkString("/")
  }

  def resolvePurchaseTimeName(faction: PlanetSideEmpire.Value, item: BasicDefinition): (BasicDefinition, String) = {
    val factionName: String = faction.toString.toLowerCase
    val name = item match {
      case GlobalDefinitions.trhev_dualcycler | GlobalDefinitions.nchev_scattercannon |
           GlobalDefinitions.vshev_quasar =>
        s"${factionName}hev_antipersonnel"
      case GlobalDefinitions.trhev_pounder | GlobalDefinitions.nchev_falcon | GlobalDefinitions.vshev_comet =>
        s"${factionName}hev_antivehicular"
      case GlobalDefinitions.trhev_burster | GlobalDefinitions.nchev_sparrow | GlobalDefinitions.vshev_starfire =>
        s"${factionName}hev_antiaircraft"
      case _ =>
        item.Name
    }
    (item, name)
  }

  def resolveSharedPurchaseTimeNames(pair: (BasicDefinition, String)): Seq[(BasicDefinition, String)] = {
    val (definition, name) = pair
    if (name.matches("(tr|nc|vs)hev_.+") && Config.app.game.sharedMaxCooldown) {
      val faction = name.take(2)
      (if (faction.equals("nc")) {
        Seq(GlobalDefinitions.nchev_scattercannon, GlobalDefinitions.nchev_falcon, GlobalDefinitions.nchev_sparrow)
      } else if (faction.equals("vs")) {
        Seq(GlobalDefinitions.vshev_quasar, GlobalDefinitions.vshev_comet, GlobalDefinitions.vshev_starfire)
      } else {
        Seq(GlobalDefinitions.trhev_dualcycler, GlobalDefinitions.trhev_pounder, GlobalDefinitions.trhev_burster)
      }).zip(
        Seq(s"${faction}hev_antipersonnel", s"${faction}hev_antivehicular", s"${faction}hev_antiaircraft")
      )
    } else {
      definition match {
        case vdef: VehicleDefinition if GlobalDefinitions.isBattleFrameFlightVehicle(vdef) =>
          val bframe = name.substring(0, name.indexOf('_'))
          val gunner = bframe + "_gunner"
          Seq((DefinitionUtil.fromString(gunner), gunner), (vdef, name))

        case vdef: VehicleDefinition if GlobalDefinitions.isBattleFrameGunnerVehicle(vdef) =>
          val bframe = name.substring(0, name.indexOf('_'))
          val flight = bframe + "_flight"
          Seq((vdef, name), (DefinitionUtil.fromString(flight), flight))

        case _ =>
          Seq(pair)
      }
    }
  }

  def encodeLockerClob(container: Container): String = {
    val clobber: mutable.StringBuilder = new StringBuilder()
    container.Inventory.Items.foreach {
      case InventoryItem(obj, index) =>
        clobber.append(encodeLoadoutClobFragment(obj, index))
    }
    clobber.mkString.drop(1)
  }

  def encodeLoadoutClobFragment(equipment: Equipment, index: Int): String = {
    val ammoInfo: String = equipment match {
      case tool: Tool =>
        tool.AmmoSlots.zipWithIndex.collect {
          case (ammoSlot, index2) if ammoSlot.AmmoTypeIndex != 0 =>
            s"_$index2-${ammoSlot.AmmoTypeIndex}-${ammoSlot.AmmoType.id}"
        }.mkString
      case _ =>
        ""
    }
    s"/${equipment.getClass.getSimpleName},$index,${equipment.Definition.ObjectId},$ammoInfo"
  }

  def changeRibbons(ribbons: RibbonBars, ribbon: MeritCommendation.Value, bar: RibbonBarSlot.Value): RibbonBars = {
    bar match {
      case RibbonBarSlot.Top           => ribbons.copy(upper = ribbon)
      case RibbonBarSlot.Middle        => ribbons.copy(middle = ribbon)
      case RibbonBarSlot.Bottom        => ribbons.copy(lower = ribbon)
      case RibbonBarSlot.TermOfService => ribbons.copy(tos = ribbon)
    }
  }

  /**
    * Check for an avatar being online at the moment by matching against their name.
    * If discovered, run a function based on the avatar's characteristics.
    * @param name name of a character being sought
    * @param func functionality that is called upon discovery of the character
    * @return if found, the discovered avatar, the avatar's account id, and the avatar's faction affiliation
    */
  def getLiveAvatarForFunc(name: String, func: (Long,String,Int)=>Unit): Option[(Avatar, Long, PlanetSideEmpire.Value)] = {
    if (name.nonEmpty) {
      LivePlayerList.WorldPopulation({ case (_, a) => a.name.equals(name) }).headOption match {
        case Some(otherAvatar) =>
          func(otherAvatar.id, name, otherAvatar.faction.id)
          Some((otherAvatar, otherAvatar.id.toLong, otherAvatar.faction))
        case None =>
          None
      }
    } else {
      None
    }
  }

  /**
    * Check for an avatar existing the database of avatars by matching against their name.
    * If discovered, run a function based on the avatar's characteristics.
    * @param name name of a character being sought
    * @param func functionality that is called upon discovery of the character
    * @return if found online, the discovered avatar, the avatar's account id, and the avatar's faction affiliation;
    *         otherwise, always returns `None` as if no avatar was discovered
    *         (the query is probably still in progress)
    */
  def getAvatarForFunc(name: String, func: (Long,String,Int)=>Unit): Option[(Avatar, Long, PlanetSideEmpire.Value)] = {
    getLiveAvatarForFunc(name, func).orElse {
      if (name.nonEmpty) {
        import ctx._
        import scala.concurrent.ExecutionContext.Implicits.global
        ctx.run(query[persistence.Avatar].filter { _.name.equals(lift(name)) }).onComplete {
          case Success(otherAvatar) =>
            otherAvatar.headOption match {
              case Some(a) =>
                func(a.id, a.name, a.factionId)
              case _ => ;
            }
          case _ => ;
        }
      }
      None //satisfy the orElse
    }
  }

  /**
    * Transform a `(Long, String, PlanetSideEmpire.Value)` function call
    * into a `(Long, String)` function call.
    * @param func replacement `(Long, String)` function call
    * @param charId unique account identifier
    * @param name unique character name
    * @param faction the faction affiliation
    */
  def formatForOtherFunc(func: (Long,String)=>Unit)(charId: Long, name: String, faction: Int): Unit = {
    func(charId, name)
  }

  /**
    * Determine if one player considered online to the other person.
    * @param onlinePlayerName name of a player to be determined if they are online
    * @param observerName name of a player who might see the former and be seen by the former
    * @return `true`, if one player is visible to the other
    *         `false`, otherwise
    */
  def onlineIfNotIgnored(onlinePlayerName: String, observerName: String): Boolean = {
    LivePlayerList.WorldPopulation({ case (_, a) => a.name.equals(onlinePlayerName) }).headOption match {
      case Some(onlinePlayer) => onlineIfNotIgnored(onlinePlayer, observerName)
      case _ => false
    }
  }

  /**
    * Determine if one player considered online to the other person.
    * Neither player can be ignoring the other.
    * @param onlinePlayerName name of a player to be determined if they are online
    * @param observer player who might see the former and be seen by the former
    * @return `true`, if one player is visible to the other
    *         `false`, otherwise
    */
  def onlineIfNotIgnoredEitherWay(observer: Avatar, onlinePlayerName: String): Boolean = {
    LivePlayerList.WorldPopulation({ case (_, a) => a.name.equals(onlinePlayerName) }) match {
      case Nil => false //weird case, but ...
      case onlinePlayer :: Nil => onlineIfNotIgnoredEitherWay(onlinePlayer, observer)
      case _ => throw new Exception("only trying to find two players, but too many matching search results!")
    }
  }

  /**
    * Determine if one player considered online to the other person.
    * Neither player can be ignoring the other.
    * @param onlinePlayer player who is online
    * @param observer player who might see the former
    * @return `true`, if the other person is not ignoring us;
    *         `false`, otherwise
    */
  def onlineIfNotIgnoredEitherWay(onlinePlayer: Avatar, observer: Avatar): Boolean = {
    onlineIfNotIgnored(onlinePlayer, observer.name) && onlineIfNotIgnored(observer, onlinePlayer.name)
  }

  /**
    * Determine if one player is considered online to the other person.
    * The question is whether first player is ignoring the other player.
    * @param onlinePlayer player who is online
    * @param observedName name of the player who may be seen
    * @return `true`, if the other person is visible;
    *         `false`, otherwise
    */
  def onlineIfNotIgnored(onlinePlayer: Avatar, observedName: String): Boolean = {
    !onlinePlayer.people.ignored.exists { f => f.name.equals(observedName) }
  }

  /**
    * Query the database on information retained in regards to a certain character
    * when that character had last logged out of the game.
    * Dummy the data if no entries are found.
    * @param avatarId the unique character identifier number
    * @return when completed, a copy of data on that character from the database
    */
  def loadSavedPlayerData(avatarId: Long): Future[persistence.Savedplayer] = {
    import ctx._
    import scala.concurrent.ExecutionContext.Implicits.global
    val out: Promise[persistence.Savedplayer] = Promise()
    val queryResult = ctx.run(query[persistence.Savedplayer].filter { _.avatarId == lift(avatarId) })
    queryResult.onComplete {
      case Success(data) if data.nonEmpty =>
        out.completeWith(Future(data.head))
      case _ =>
        ctx.run(query[persistence.Savedplayer]
          .insert(
            _.avatarId -> lift(avatarId),
            _.px -> lift(0),
            _.py -> lift(0),
            _.pz -> lift(0),
            _.orientation -> lift(0),
            _.zoneNum -> lift(0),
            _.health -> lift(0),
            _.armor -> lift(0),
            _.exosuitNum -> lift(0),
            _.loadout -> lift("")
          )
        )
        out.completeWith(Future(persistence.Savedplayer(avatarId, 0, 0, 0, 0, 0, 0, 0, 0, "")))
    }
    out.future
  }
//TODO should return number of rows inserted?
  /**
    * Query the database on information retained in regards to a certain character
    * when that character had last logged out of the game.
    * If that character is found in the database, update the data for that character.
    * @param player the player character
    * @return when completed, return the number of rows updated
    */
  def savePlayerData(player: Player): Future[Int] = {
    savePlayerData(player, player.Health)
  }

  /**
    * Query the database on information retained in regards to a certain character
    * when that character had last logged out of the game.
    * If that character is found in the database, update the data for that character.
    * Determine if the player's previous health information is valid
    * by comparing historical information about the player character's campaign.
    * (This ignored the official health value attached to the character.)
    * @param player the player character
    * @return when completed, return the number of rows updated
    */
  def finalSavePlayerData(player: Player): Future[Int] = {
    val health = (
      player.History.find(_.isInstanceOf[DamagingActivity]),
      player.History.find(_.isInstanceOf[HealingActivity])
    ) match {
      case (Some(damage), Some(heal)) =>
        //between damage and potential healing, which came last?
        if (damage.time < heal.time) {
          heal.asInstanceOf[HealingActivity].amount % player.MaxHealth
        } else {
          damage.asInstanceOf[DamagingActivity].data.targetAfter.asInstanceOf[PlayerSource].health
        }
      case (Some(damage), None) =>
        damage.asInstanceOf[DamagingActivity].data.targetAfter.asInstanceOf[PlayerSource].health
      case (None, Some(heal)) =>
        heal.asInstanceOf[HealingActivity].amount % player.MaxHealth
      case _ =>
        player.MaxHealth
    }
    savePlayerData(player, health)
  }

  /**
    * Query the database on information retained in regards to a certain character
    * when that character had last logged out of the game.
    * If that character is found in the database, update the data for that character.
    * If no entries for that character are found, insert a new default-data entry.
    * @param player the player character
    * @param health a custom health value to assign the player character's information in the database
    * @return when completed, return the number of rows updated
    */
  def savePlayerData(player: Player, health: Int): Future[Int] = {
    import ctx._
    import scala.concurrent.ExecutionContext.Implicits.global
    val out: Promise[Int] = Promise()
    val avatarId = player.avatar.id
    val position = player.Position
    val queryResult = ctx.run(query[persistence.Savedplayer].filter { _.avatarId == lift(avatarId) })
    queryResult.onComplete {
      case Success(results) if results.nonEmpty =>
        ctx.run(query[persistence.Savedplayer]
          .filter { _.avatarId == lift(avatarId) }
          .update(
            _.px -> lift((position.x * 1000).toInt),
            _.py -> lift((position.y * 1000).toInt),
            _.pz -> lift((position.z * 1000).toInt),
            _.orientation -> lift((player.Orientation.z * 1000).toInt),
            _.zoneNum -> lift(player.Zone.Number),
            _.health -> lift(health),
            _.armor -> lift(player.Armor),
            _.exosuitNum -> lift(player.ExoSuit.id),
            _.loadout -> lift(buildClobFromPlayerLoadout(player))
          )
        )
        out.completeWith(Future(1))
      case _ =>
        out.completeWith(Future(0))
    }
    out.future
  }

  /**
    * Query the database on information retained in regards to a certain character
    * when that character had last logged out of the game.
    * If that character is found in the database, update only specific fields for that character
    * related to the character's physical location in the game world.
    * @param player the player character
    * @return when completed, return the number of rows updated
    */
  def savePlayerLocation(player: Player): Future[Int] = {
    import ctx._
    import scala.concurrent.ExecutionContext.Implicits.global
    val out: Promise[Int] = Promise()
    val avatarId = player.avatar.id
    val position = player.Position
    val queryResult = ctx.run(query[persistence.Savedplayer].filter { _.avatarId == lift(avatarId) })
    queryResult.onComplete {
      case Success(results) if results.nonEmpty =>
        val res=ctx.run(query[persistence.Savedplayer]
          .filter { _.avatarId == lift(avatarId) }
          .update(
            _.px -> lift((position.x * 1000).toInt),
            _.py -> lift((position.y * 1000).toInt),
            _.pz -> lift((position.z * 1000).toInt),
            _.orientation -> lift((player.Orientation.z * 1000).toInt),
            _.zoneNum -> lift(player.Zone.Number)
          )
        )
        out.completeWith(Future(1))
      case _ =>
        out.completeWith(Future(0))
    }
    out.future
  }

  /**
    * Query the database on information retained in regards to a certain player avatar
    * when a character associated with the avatar had last logged out of the game.
    * If that player avatar is found in the database, recover the retained information.
    * If no entries for that avatar are found, insert a new default-data entry and dummy an entry for use.
    * Useful mainly for player avatar login evaluations.
    * @param avatarId a unique identifier number associated with the player avatar
    * @return when completed, return the persisted data
    */
  def loadSavedAvatarData(avatarId: Long): Future[persistence.Savedavatar] = {
    import ctx._
    import scala.concurrent.ExecutionContext.Implicits.global
    val out: Promise[persistence.Savedavatar] = Promise()
    val queryResult = ctx.run(query[persistence.Savedavatar].filter { _.avatarId == lift(avatarId) })
    queryResult.onComplete {
      case Success(data) if data.nonEmpty =>
        out.completeWith(Future(data.head))
      case _ =>
        val now = LocalDateTime.now()
        ctx.run(query[persistence.Savedavatar]
          .insert(
            _.avatarId -> lift(avatarId),
            _.forgetCooldown -> lift(now),
            _.purchaseCooldowns -> lift(""),
            _.useCooldowns -> lift("")
          )
        )
        out.completeWith(Future(persistence.Savedavatar(avatarId, now, "", "")))
    }
    out.future
  }

  /**
    * Query the database on information retained in regards to a certain player avatar
    * when a character associated with the avatar had last logged out of the game.
    * If that player avatar is found in the database, update important information.
    * Useful mainly for player avatar login evaluations.
    * @param avatar a unique player avatar
    * @return when completed, return the number of rows updated
    */
  def saveAvatarData(avatar: Avatar): Future[Int] = {
    import ctx._
    import scala.concurrent.ExecutionContext.Implicits.global
    val out: Promise[Int] = Promise()
    val avatarId = avatar.id
    val queryResult = ctx.run(query[persistence.Savedavatar].filter { _.avatarId == lift(avatarId) })
    queryResult.onComplete {
      case Success(results) if results.nonEmpty =>
        ctx.run(query[persistence.Savedavatar]
          .filter { _.avatarId == lift(avatarId) }
          .update(
            _.purchaseCooldowns -> lift(buildClobfromCooldowns(avatar.cooldowns.purchase)),
            _.useCooldowns -> lift(buildClobfromCooldowns(avatar.cooldowns.use))
          )
        )
        out.completeWith(Future(1))
      case _ =>
        out.completeWith(Future(0))
    }
    out.future
  }
}

class AvatarActor(
    context: ActorContext[AvatarActor.Command],
    buffer: StashBuffer[AvatarActor.Command],
    sessionActor: ActorRef[SessionActor.Command]
) {

  import AvatarActor._

  implicit val ec: ExecutionContextExecutor = context.executionContext

  private[this] val log                            = org.log4s.getLogger
  var account: Option[Account]                     = None
  var session: Option[Session]                     = None
  val implantTimers: mutable.Map[Int, Cancellable] = mutable.Map()
  var staminaRegenTimer: Cancellable               = Default.Cancellable
  var _avatar: Option[Avatar]                      = None
  var saveLockerFunc: () => Unit                   = storeNewLocker
  //val topic: ActorRef[Topic.Command[Avatar]]       = context.spawnAnonymous(Topic[Avatar]("avatar"))

  def avatar: Avatar = _avatar.get

  def avatar_=(avatar: Avatar): Unit = {
    _avatar = Some(avatar)
    //topic ! Topic.Publish(avatar)
    sessionActor ! SessionActor.SetAvatar(avatar)
  }

  def start(): Behavior[Command] = {
    Behaviors
      .receiveMessage[Command] {
        case SetAccount(newAccount) =>
          account = Some(newAccount)
          import ctx._
          ctx.run(query[persistence.Account].filter(_.id == lift(newAccount.id)).map(_.id)).onComplete {
            case Success(accounts) =>
              accounts.headOption match {
                case Some(_) => sendAvatars(newAccount)
                case None    => log.error(s"data for account ${newAccount.name} not found")
              }
            case Failure(e) => log.error(e)("db failure")
          }
          postStartBehaviour()

        case SetSession(newSession) =>
          session = Some(newSession)
          postStartBehaviour()

        case other =>
          buffer.stash(other)
          Behaviors.same
      }
  }

  def postStartBehaviour(): Behavior[Command] = {
    account match {
      case Some(_account) =>
        buffer.unstashAll(active(_account))
      case _ =>
        Behaviors.same
    }
  }

  def active(account: Account): Behavior[Command] = {
    Behaviors
      .receiveMessagePartial[Command] {
        case SetSession(newSession) =>
          session = Some(newSession)
          Behaviors.same

        case SetLookingForSquad(lfs) =>
          avatarCopy(avatar.copy(lookingForSquad = lfs))
          sessionActor ! SessionActor.SendResponse(PlanetsideAttributeMessage(session.get.player.GUID, 53, 0))
          session.get.zone.AvatarEvents ! AvatarServiceMessage(
            avatar.faction.toString,
            AvatarAction.PlanetsideAttribute(session.get.player.GUID, 53, if (lfs) 1 else 0)
          )
          Behaviors.same

        case CreateAvatar(name, head, voice, gender, empire) =>
          import ctx._

          ctx.run(query[persistence.Avatar].filter(_.name ilike lift(name)).filter(!_.deleted)).onComplete {
            case Success(characters) =>
              characters.headOption match {
                case None =>
                  val result = for {
                    _ <- ctx.run(
                      query[persistence.Avatar]
                        .insert(
                          _.name      -> lift(name),
                          _.accountId -> lift(account.id),
                          _.factionId -> lift(empire.id),
                          _.headId    -> lift(head),
                          _.voiceId   -> lift(voice.id),
                          _.genderId  -> lift(gender.value),
                          _.bep       -> lift(Config.app.game.newAvatar.br.experience),
                          _.cep       -> lift(Config.app.game.newAvatar.cr.experience)
                        )
                    )
                  } yield ()

                  result.onComplete {
                    case Success(_) =>
                      log.debug(s"AvatarActor: created character $name for account ${account.name}")
                      sessionActor ! SessionActor.SendResponse(ActionResultMessage.Pass)
                      sendAvatars(account)
                    case Failure(e) => log.error(e)("db failure")
                  }
                case Some(_) =>
                  // send "char already exists"
                  sessionActor ! SessionActor.SendResponse(ActionResultMessage.Fail(1))
              }
            case Failure(e) =>
              log.error(e)("db failure")
              sessionActor ! SessionActor.SendResponse(ActionResultMessage.Fail(4))
              sendAvatars(account)
          }
          Behaviors.same

        case DeleteAvatar(id) =>
          import ctx._
          val result = for {
            _ <- ctx.run(query[persistence.Implant].filter(_.avatarId == lift(id)).delete)
            _ <- ctx.run(query[persistence.Loadout].filter(_.avatarId == lift(id)).delete)
            _ <- ctx.run(query[persistence.Locker].filter(_.avatarId == lift(id)).delete)
            _ <- ctx.run(query[persistence.Certification].filter(_.avatarId == lift(id)).delete)
            _ <- ctx.run(query[persistence.Friend].filter(_.avatarId == lift(id)).delete)
            _ <- ctx.run(query[persistence.Ignored].filter(_.avatarId == lift(id)).delete)
            _ <- ctx.run(query[persistence.Savedavatar].filter(_.avatarId == lift(id)).delete)
            _ <- ctx.run(query[persistence.Savedplayer].filter(_.avatarId == lift(id)).delete)
            r <- ctx.run(query[persistence.Avatar].filter(_.id == lift(id)))
          } yield r

          result.onComplete {
            case Success(deleted) =>
              deleted.headOption match {
                case Some(a) if !a.deleted =>
                  ctx.run(query[persistence.Avatar]
                    .filter(_.id == lift(id))
                    .update(
                      _.deleted -> lift(true),
                      _.lastModified -> lift(LocalDateTime.now())
                    )
                  )
                  log.debug(s"AvatarActor: avatar $id deleted")
                  sessionActor ! SessionActor.SendResponse(ActionResultMessage.Pass)
                case _ => ;
              }
              sendAvatars(account)
            case Failure(e) => log.error(e)("db failure")
          }
          Behaviors.same

        case SelectAvatar(charId, replyTo) =>
          import ctx._
          ctx.run(query[persistence.Avatar].filter(_.id == lift(charId))).onComplete {
            case Success(characters) =>
              characters.headOption match {
                case Some(character) =>
                  avatar = character.toAvatar
                  replyTo ! AvatarResponse(avatar)
                case None => log.error(s"selected character $charId not found")
              }
            case Failure(e) => log.error(e)("db failure")
          }
          Behaviors.same

        case LoginAvatar(replyTo) =>
          import ctx._
          val avatarId = avatar.id
          ctx.run(
            query[persistence.Avatar]
              .filter(_.id == lift(avatarId))
              .map { c => (c.created, c.lastLogin) }
          )
            .onComplete {
              case Success(value) if value.nonEmpty =>
                val (created, lastLogin) = value.head
                if (created.equals(lastLogin)) {
                  //first login
                  //initialize default values that would be compromised during login if blank
                  val inits = for {
                    _ <- ctx.run(
                      liftQuery(
                        List(
                          persistence.Certification(Certification.StandardExoSuit.value, avatarId),
                          persistence.Certification(Certification.AgileExoSuit.value, avatarId),
                          persistence.Certification(Certification.ReinforcedExoSuit.value, avatarId),
                          persistence.Certification(Certification.StandardAssault.value, avatarId),
                          persistence.Certification(Certification.MediumAssault.value, avatarId),
                          persistence.Certification(Certification.ATV.value, avatarId),
                          persistence.Certification(Certification.Harasser.value, avatarId)
                        )
                      ).foreach(c => query[persistence.Certification].insert(c))
                    )
                    _ <- ctx.run(
                      liftQuery(
                        List(persistence.Shortcut(avatarId, 0, 0, "medkit"))
                      ).foreach(c => query[persistence.Shortcut].insert(c))
                    )
                  } yield true
                  inits.onComplete {
                    case Success(_) => performAvatarLogin(avatarId, account.id, replyTo)
                    case Failure(e) => log.error(e)("db failure")
                  }
                } else {
                  performAvatarLogin(avatarId, account.id, replyTo)
                }
              case Failure(e) => log.error(e)("db failure")
            }
          Behaviors.same

        case ReplaceAvatar(newAvatar) =>
          replaceAvatar(newAvatar)
          startIfStoppedStaminaRegen(initialDelay = 0.5f seconds)
          Behaviors.same

        case AddFirstTimeEvent(event) =>
          val decor = avatar.decoration
          avatarCopy(avatar.copy(decoration = decor.copy(firstTimeEvents = decor.firstTimeEvents ++ Set(event))))
          Behaviors.same

        case LearnCertification(terminalGuid, certification) =>
          import ctx._

          if (avatar.certifications.contains(certification)) {
            sessionActor ! SessionActor.SendResponse(
              ItemTransactionResultMessage(terminalGuid, TransactionType.Learn, success = false)
            )
          } else {
            val replace = certification.replaces.intersect(avatar.certifications)
            Future
              .sequence(replace.map(cert => {
                ctx.run(
                  query[persistence.Certification]
                    .filter(_.avatarId == lift(avatar.id))
                    .filter(_.id == lift(cert.value))
                    .delete
                )
                .map(_ => cert)
              }))
              .onComplete {
                case Failure(exception) =>
                  log.error(exception)("db failure")
                  sessionActor ! SessionActor.SendResponse(
                    ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = false)
                  )
                case Success(_replace) =>
                  _replace.foreach { cert =>
                    sessionActor ! SessionActor.SendResponse(
                      PlanetsideAttributeMessage(session.get.player.GUID, 25, cert.value)
                    )
                  }
                  ctx.run(
                    query[persistence.Certification]
                      .insert(_.id -> lift(certification.value), _.avatarId -> lift(avatar.id))
                  )
                    .onComplete {
                      case Failure(exception) =>
                        log.error(exception)("db failure")
                        sessionActor ! SessionActor.SendResponse(
                          ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = false)
                        )

                      case Success(_) =>
                        sessionActor ! SessionActor.SendResponse(
                          PlanetsideAttributeMessage(session.get.player.GUID, 24, certification.value)
                        )
                        replaceAvatar(
                          avatar.copy(certifications = avatar.certifications.diff(replace) + certification)
                        )
                        sessionActor ! SessionActor.SendResponse(
                          ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = true)
                        )
                        sessionActor ! SessionActor.CharSaved
                    }

              }
          }
          Behaviors.same

        case SellCertification(terminalGuid, certification) =>
          import ctx._

          if (!avatar.certifications.contains(certification)) {
            sessionActor ! SessionActor.SendResponse(
              ItemTransactionResultMessage(terminalGuid, TransactionType.Learn, success = false)
            )
          } else {
            var requiredByCert: Set[Certification] = Set(certification)
            var removeThese: Set[Certification]    = Set(certification)
            val allCerts: Set[Certification]       = Certification.values.toSet
            do {
              removeThese = allCerts.filter { testingCert =>
                testingCert.requires.intersect(removeThese).nonEmpty
              }
              requiredByCert = requiredByCert ++ removeThese
            } while (removeThese.nonEmpty)

            Future
              .sequence(
                avatar.certifications
                  .intersect(requiredByCert)
                  .map(cert => {
                    ctx.run(
                      query[persistence.Certification]
                        .filter(_.avatarId == lift(avatar.id))
                        .filter(_.id == lift(cert.value))
                        .delete
                    )
                    .map(_ => cert)
                  })
              )
              .onComplete {
                case Failure(exception) =>
                  log.error(exception)("db failure")
                  sessionActor ! SessionActor.SendResponse(
                    ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = false)
                  )
                case Success(certs) =>
                  val player = session.get.player
                  replaceAvatar(avatar.copy(certifications = avatar.certifications.diff(certs)))
                  certs.foreach { cert =>
                    sessionActor ! SessionActor.SendResponse(
                      PlanetsideAttributeMessage(player.GUID, 25, cert.value)
                    )
                  }
                  sessionActor ! SessionActor.SendResponse(
                    ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = true)
                  )
                  sessionActor ! SessionActor.CharSaved
                  //wearing invalid armor?
                  if (
                    if (certification == Certification.ReinforcedExoSuit) player.ExoSuit == ExoSuitType.Reinforced
                    else if (certification == Certification.InfiltrationSuit) player.ExoSuit == ExoSuitType.Infiltration
                    else if (player.ExoSuit == ExoSuitType.MAX) {
                      lazy val subtype =
                        InfantryLoadout.DetermineSubtypeA(ExoSuitType.MAX, player.Slot(slot = 0).Equipment)
                      if (certification == Certification.UniMAX) true
                      else if (certification == Certification.AAMAX) subtype == 1
                      else if (certification == Certification.AIMAX) subtype == 2
                      else if (certification == Certification.AVMAX) subtype == 3
                      else false
                    } else false
                  ) {
                    player.Actor ! PlayerControl.SetExoSuit(ExoSuitType.Standard, 0)
                  }
              }
          }
          Behaviors.same

        case SetCertifications(certifications) =>
          import ctx._
          Future
            .sequence(
              avatar.certifications
                .diff(certifications)
                .map(cert => {
                  sessionActor ! SessionActor.SendResponse(
                    PlanetsideAttributeMessage(session.get.player.GUID, 25, cert.value)
                  )
                  ctx.run(
                    query[persistence.Certification]
                      .filter(_.avatarId == lift(avatar.id))
                      .filter(_.id == lift(cert.value))
                      .delete
                  )
                }) ++
                certifications
                  .diff(avatar.certifications)
                  .map(cert => {
                    sessionActor ! SessionActor.SendResponse(
                      PlanetsideAttributeMessage(session.get.player.GUID, 24, cert.value)
                    )
                    ctx
                      .run(
                        query[persistence.Certification]
                          .insert(_.id -> lift(cert.value), _.avatarId -> lift(avatar.id))
                      )
                  })
            )
            .onComplete {
              case Success(_) =>
                replaceAvatar(avatar.copy(certifications = certifications))
                sessionActor ! SessionActor.CharSaved
              case Failure(exception) =>
                log.error(exception)("db failure")
            }

          Behaviors.same

        case CreateImplants() =>
          avatar.implants.zipWithIndex.foreach {
            case (Some(implant), index) =>
              sessionActor ! SessionActor.SendResponse(
                AvatarImplantMessage(
                  session.get.player.GUID,
                  ImplantAction.Add,
                  index,
                  implant.definition.implantType.value
                )
              )
            case _ => ;
          }
          deinitializeImplants()
          Behaviors.same

        case LearnImplant(terminalGuid, definition) =>
          // TODO there used to be a terminal check here, do we really need it?
          val index = avatar.implants.zipWithIndex.collectFirst {
            case (Some(implant), _index) if implant.definition.implantType == definition.implantType => _index
            case (None, _index) if _index < avatar.br.implantSlots                                   => _index
          }
          index match {
            case Some(_index) =>
              import ctx._
              ctx
                .run(query[persistence.Implant].insert(_.name -> lift(definition.Name), _.avatarId -> lift(avatar.id)))
                .onComplete {
                  case Success(_) =>
                    replaceAvatar(avatar.copy(implants = avatar.implants.updated(_index, Some(Implant(definition)))))
                    sessionActor ! SessionActor.SendResponse(
                      AvatarImplantMessage(
                        session.get.player.GUID,
                        ImplantAction.Add,
                        _index,
                        definition.implantType.value
                      )
                    )
                    sessionActor ! SessionActor.SendResponse(
                      ItemTransactionResultMessage(terminalGuid, TransactionType.Learn, success = true)
                    )
                    context.self ! ResetImplants()
                    sessionActor ! SessionActor.CharSaved
                  case Failure(exception) => log.error(exception)("db failure")
                }

            case None =>
              log.warn("attempted to learn implant but could not find slot")
              sessionActor ! SessionActor.SendResponse(
                ItemTransactionResultMessage(terminalGuid, TransactionType.Learn, success = false)
              )
          }
          Behaviors.same

        case SellImplant(terminalGuid, definition) =>
          // TODO there used to be a terminal check here, do we really need it?
          val index = avatar.implants.zipWithIndex.collectFirst {
            case (Some(implant), _index) if implant.definition.implantType == definition.implantType => _index
          }
          index match {
            case Some(_index) =>
              import ctx._
              ctx.run(
                query[persistence.Implant]
                  .filter(_.name == lift(definition.Name))
                  .filter(_.avatarId == lift(avatar.id))
                  .delete
              )
              .onComplete {
                case Success(_) =>
                  replaceAvatar(avatar.copy(implants = avatar.implants.updated(_index, None)))
                  sessionActor ! SessionActor.SendResponse(
                    AvatarImplantMessage(session.get.player.GUID, ImplantAction.Remove, _index, 0)
                  )
                  sessionActor ! SessionActor.SendResponse(
                    ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = true)
                  )
                  context.self ! ResetImplants()
                  sessionActor ! SessionActor.CharSaved
                case Failure(exception) => log.error(exception)("db failure")
              }

            case None =>
              log.warn("attempted to sell implant but could not find slot")
              sessionActor ! SessionActor.SendResponse(
                ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = false)
              )
          }
          Behaviors.same

        case SaveLoadout(player, loadoutType, label, number) =>
          log.info(s"${player.Name} wishes to save a favorite $loadoutType loadout as #${number + 1}")
          val name = label.getOrElse(s"missing_loadout_${number + 1}")
          val (lineNo, result): (Int, Future[Loadout]) = loadoutType match {
            case LoadoutType.Infantry =>
              (
                number,
                storeLoadout(player, name, number)
              )

            case LoadoutType.Vehicle =>
              (
                number + 10,
                player.Zone.GUID(avatar.vehicle) match {
                  case Some(vehicle: Vehicle) =>
                    storeVehicleLoadout(player, name, number, vehicle)
                  case _ =>
                    throwLoadoutFailure(s"no owned vehicle found for ${player.Name}")
                }
              )

            case LoadoutType.Battleframe =>
              (
                number + 15,
                player.Zone.GUID(avatar.vehicle) match {
                  case Some(vehicle: Vehicle) if GlobalDefinitions.isBattleFrameVehicle(vehicle.Definition) =>
                    storeVehicleLoadout(player, name, number + 5, vehicle)
                  case _ =>
                    throwLoadoutFailure(s"no owned battleframe found for ${player.Name}")
                }
              )
          }
          result.onComplete {
            case Success(loadout) =>
              val ldouts = avatar.loadouts
              replaceAvatar(avatar.copy(loadouts = ldouts.copy(suit = ldouts.suit.updated(lineNo, Some(loadout)))))
              sessionActor ! SessionActor.CharSaved
              refreshLoadout(lineNo)
            case Failure(exception) =>
              log.error(exception)("db failure (?)")
          }
          Behaviors.same

        case DeleteLoadout(player, loadoutType, number) =>
          log.info(s"${player.Name} wishes to delete a favorite $loadoutType loadout - #${number + 1}")
          import ctx._
          val (lineNo: Int, result) = loadoutType match {
            case LoadoutType.Infantry if avatar.loadouts.suit(number).nonEmpty =>
              (
                number,
                ctx.run(
                  query[persistence.Loadout]
                    .filter(_.avatarId == lift(avatar.id))
                    .filter(_.loadoutNumber == lift(number))
                    .delete
                )
              )
            case LoadoutType.Vehicle if avatar.loadouts.suit(number + 10).nonEmpty =>
              (
                number + 10,
                ctx.run(
                  query[persistence.Vehicleloadout]
                    .filter(_.avatarId == lift(avatar.id))
                    .filter(_.loadoutNumber == lift(number))
                    .delete
                )
              )
            case LoadoutType.Battleframe if avatar.loadouts.suit(number + 15).nonEmpty =>
              (
                number + 15,
                ctx.run(
                  query[persistence.Vehicleloadout]
                    .filter(_.avatarId == lift(avatar.id))
                    .filter(_.loadoutNumber == lift(number + 5))
                    .delete
                )
              )
            case _ =>
              (number, throwLoadoutFailure(msg = "unhandled loadout type or no loadout"))
          }
          result.onComplete {
            case Success(_) =>
              val ldouts = avatar.loadouts
              avatarCopy(avatar.copy(loadouts = ldouts.copy(suit = ldouts.suit.updated(lineNo, None))))
              sessionActor ! SessionActor.CharSaved
              sessionActor ! SessionActor.SendResponse(FavoritesMessage(loadoutType, player.GUID, number, ""))
            case Failure(exception) =>
              log.error(exception)("db failure (?)")
          }
          Behaviors.same

        case SaveLocker() =>
          saveLockerFunc()
          Behaviors.same

        case InitialRefreshLoadouts() =>
          refreshLoadouts(avatar.loadouts.suit.zipWithIndex)
          Behaviors.same

        case RefreshLoadouts() =>
          refreshLoadouts(avatar.loadouts.suit.zipWithIndex.collect { case out @ (Some(_), _) => out })
          Behaviors.same

        case UpdatePurchaseTime(definition, time) =>
          var newTimes = avatar.cooldowns.purchase
          AvatarActor.resolveSharedPurchaseTimeNames(AvatarActor.resolvePurchaseTimeName(avatar.faction, definition)).foreach {
            case (item, name) =>
              Avatar.purchaseCooldowns.get(item) match {
                case Some(cooldown) =>
                  //only send for items with cooldowns
                  newTimes = newTimes.updated(name, time)
                  updatePurchaseTimer(
                    name,
                    cooldown.toSeconds,
                    item.isInstanceOf[VehicleDefinition]
                  )
                case _ => ;
              }
          }
          avatarCopy(avatar.copy(cooldowns = avatar.cooldowns.copy(purchase = newTimes)))
          Behaviors.same

        case UpdateUseTime(definition, time) =>
          if (!Avatar.useCooldowns.contains(definition)) {
            log.warn(s"${avatar.name} is updating a use time for item '${definition.Name}' that has no cooldown")
          }
          val cdowns = avatar.cooldowns
          avatarCopy(avatar.copy(cooldowns = cdowns.copy(use = cdowns.use.updated(definition.Name, time))))
          sessionActor ! SessionActor.UseCooldownRenewed(definition, time)
          Behaviors.same

        case RefreshPurchaseTimes() =>
          refreshPurchaseTimes(avatar.cooldowns.purchase.keys.toSet)
          Behaviors.same

        case SetVehicle(vehicle) =>
          avatarCopy(avatar.copy(vehicle = vehicle))
          Behaviors.same

        case ActivateImplant(implantType) =>
          avatar.implants.zipWithIndex.collectFirst {
            case (Some(implant), index) if implant.definition.implantType == implantType => (implant, index)
          } match {
            case Some((implant, slot)) =>
              if (!implant.initialized) {
                log.warn(s"requested activation of uninitialized implant $implantType")
              } else if (
                !consumeThisMuchStamina(implant.definition.ActivationStaminaCost) ||
                avatar.stamina < implant.definition.StaminaCost
              ) {
                // not enough stamina to activate
              } else if (implant.definition.implantType.disabledFor.contains(session.get.player.ExoSuit)) {
                // TODO can this really happen? can we prevent it?
              } else {
                avatarCopy(
                  avatar.copy(
                    implants = avatar.implants.updated(slot, Some(implant.copy(active = true)))
                  )
                )
                sessionActor ! SessionActor.SendResponse(
                  AvatarImplantMessage(session.get.player.GUID, ImplantAction.Activation, slot, 1)
                )
                // Activation sound / effect
                session.get.zone.AvatarEvents ! AvatarServiceMessage(
                  session.get.zone.id,
                  AvatarAction.PlanetsideAttribute(
                    session.get.player.GUID,
                    28,
                    implant.definition.implantType.value * 2 + 1
                  )
                )
                implantTimers.get(slot).foreach(_.cancel())
                val interval = implant.definition.GetCostIntervalByExoSuit(session.get.player.ExoSuit).milliseconds
                // TODO costInterval should be an option ^
                if (interval.toMillis > 0) {
                  implantTimers(slot) = context.system.scheduler.scheduleWithFixedDelay(interval, interval)(() => {
                    val player = session.get.player
                    if (
                      implantType match {
                        case ImplantType.AdvancedRegen =>
                          //for every 1hp: 2sp (running), 1.5sp (standing), 1sp (crouched)
                          // to simulate '1.5sp (standing)', find if 0.0...1.0 * 100 is an even number
                          val cost = implant.definition.StaminaCost -
                            (if (player.Crouching || (!player.isMoving && (math.random() * 100) % 2 == 1)) 1 else 0)
                          val aliveAndWounded = player.isAlive && player.Health < player.MaxHealth
                          if (aliveAndWounded && consumeThisMuchStamina(cost)) {
                            //heal
                            val originalHealth = player.Health
                            val zone           = player.Zone
                            val events         = zone.AvatarEvents
                            val guid           = player.GUID
                            val newHealth      = player.Health = originalHealth + 1
                            player.History(HealFromImplant(PlayerSource(player), 1, implantType))
                            events ! AvatarServiceMessage(
                              zone.id,
                              AvatarAction.PlanetsideAttributeToAll(guid, 0, newHealth)
                            )
                            false
                          } else {
                            !aliveAndWounded
                          }
                        case _ =>
                          !player.isAlive || !consumeThisMuchStamina(implant.definition.StaminaCost)
                      }
                    ) {
                      context.self ! DeactivateImplant(implantType)
                    }
                  })
                }
              }

            case None => log.error(s"requested activation of unknown implant $implantType")
          }
          Behaviors.same

        case SetImplantInitialized(implantType) =>
          avatar.implants.zipWithIndex.collectFirst {
            case (Some(implant), index) if implant.definition.implantType == implantType => index
          } match {
            case Some(index) =>
              sessionActor ! SessionActor.SendResponse(
                AvatarImplantMessage(session.get.player.GUID, ImplantAction.Initialization, index, 1)
              )
              avatarCopy(avatar.copy(implants = avatar.implants.map {
                case Some(implant) if implant.definition.implantType == implantType =>
                  Some(implant.copy(initialized = true))
                case other => other
              }))

            case None => log.error(s"set initialized called for unknown implant $implantType")
          }

          Behaviors.same

        case DeactivateImplant(implantType) =>
          deactivateImplant(implantType)
          Behaviors.same

        case DeactivateActiveImplants() =>
          avatar.implants.indices.foreach { index =>
            avatar.implants(index).foreach { implant =>
              if (implant.active && implant.definition.GetCostIntervalByExoSuit(session.get.player.ExoSuit) > 0) {
                deactivateImplant(implant.definition.implantType)
              }
            }
          }
          Behaviors.same

        case RestoreStamina(stamina) =>
          tryRestoreStaminaForSession(stamina) match {
            case Some(sess) =>
              actuallyRestoreStamina(stamina, sess)
            case _ => ;
          }
          Behaviors.same

        case RestoreStaminaPeriodically(stamina) =>
          restoreStaminaPeriodically(stamina)
          Behaviors.same

        case ConsumeStamina(stamina) =>
          if (stamina > 0) {
            consumeThisMuchStamina(stamina)
          } else {
            log.warn(s"consumed stamina must be larger than 0, but is: $stamina")
          }
          Behaviors.same

        case SuspendStaminaRegeneration(duration) =>
          // TODO suspensions can overwrite each other with different durations
          defaultStaminaRegen(duration)
          Behaviors.same

        case InitializeImplants() =>
          initializeImplants()
          Behaviors.same

        case DeinitializeImplants() =>
          deinitializeImplants()
          Behaviors.same

        case ResetImplant(implantType) =>
          resetAnImplant(implantType)
          Behaviors.same

        case ResetImplants() =>
          deinitializeImplants()
          initializeImplants()
          Behaviors.same

        case AwardBep(bep) =>
          context.self ! SetBep(avatar.bep + bep)
          Behaviors.same

        case SetBep(bep) =>
          import ctx._

          val result = for {
            _ <-
              if (BattleRank.withExperience(bep).value < BattleRank.BR24.value) setCosmetics(Set())
              else Future.successful(())
            r <- ctx.run(query[persistence.Avatar].filter(_.id == lift(avatar.id)).update(_.bep -> lift(bep)))
          } yield r
          result.onComplete {
            case Success(_) =>
              sessionActor ! SessionActor.SendResponse(BattleExperienceMessage(session.get.player.GUID, bep, 0))
              session.get.zone.AvatarEvents ! AvatarServiceMessage(
                session.get.zone.id,
                AvatarAction.PlanetsideAttributeToAll(session.get.player.GUID, 17, bep)
              )
              // when the level is reduced, take away any implants over the implant slot limit
              val implants = avatar.implants.zipWithIndex.map {
                case (implant, index) =>
                  if (index >= BattleRank.withExperience(bep).implantSlots && implant.isDefined) {
                    ctx.run(
                      query[persistence.Implant]
                        .filter(_.name == lift(implant.get.definition.Name))
                        .filter(_.avatarId == lift(avatar.id))
                        .delete
                    )
                      .onComplete {
                        case Success(_) =>
                          sessionActor ! SessionActor.SendResponse(
                            AvatarImplantMessage(session.get.player.GUID, ImplantAction.Remove, index, 0)
                          )
                        case Failure(exception) => log.error(exception)("db failure")
                      }
                    None
                  } else {
                    implant
                  }
              }
              avatar = avatar.copy(bep = bep, implants = implants)
            case Failure(exception) => log.error(exception)("db failure")
          }
          Behaviors.same

        case AwardCep(cep) =>
          context.self ! SetCep(avatar.cep + cep)
          Behaviors.same

        case SetCep(cep) =>
          import ctx._
          ctx.run(query[persistence.Avatar].filter(_.id == lift(avatar.id)).update(_.cep -> lift(cep))).onComplete {
            case Success(_) =>
              avatar = avatar.copy(cep = cep)
              session.get.zone.AvatarEvents ! AvatarServiceMessage(
                session.get.zone.id,
                AvatarAction.PlanetsideAttributeToAll(session.get.player.GUID, 18, cep)
              )
            case Failure(exception) => log.error(exception)("db failure")
          }
          Behaviors.same

        case SetCosmetics(cosmetics) =>
          setCosmetics(cosmetics)
          Behaviors.same

        case SetRibbon(ribbon, bar) =>
          val decor = avatar.decoration
          val previousRibbonBars = decor.ribbonBars
          val useRibbonBars = Seq(previousRibbonBars.upper, previousRibbonBars.middle, previousRibbonBars.lower)
            .indexWhere { _ == ribbon } match {
            case -1 => previousRibbonBars
            case n  => AvatarActor.changeRibbons(previousRibbonBars, MeritCommendation.None, RibbonBarSlot(n))
          }
          replaceAvatar(avatar.copy(decoration = decor.copy(ribbonBars = AvatarActor.changeRibbons(useRibbonBars, ribbon, bar))))
          val player = session.get.player
          val zone   = player.Zone
          zone.AvatarEvents ! AvatarServiceMessage(
            zone.id,
            AvatarAction.SendResponse(Service.defaultPlayerGUID, DisplayedAwardMessage(player.GUID, ribbon, bar))
          )
          Behaviors.same

        case MemberListRequest(action, name) =>
          memberListAction(action, name)
          Behaviors.same

        case AddShortcut(slot, shortcut) =>
          import ctx._
          if (slot > -1) {
            val targetShortcut = avatar.shortcuts.lift(slot).flatten
            //short-circuit if the shortcut already exists at the given location
            val isMacroShortcut = shortcut.isInstanceOf[Shortcut.Macro]
            val isDifferentShortcut = !(targetShortcut match {
              case Some(target) => AvatarShortcut.equals(shortcut, target)
              case _            => false
            })
            if (isDifferentShortcut) {
              if (!isMacroShortcut && avatar.shortcuts.flatten.exists {
                a => AvatarShortcut.equals(shortcut, a)
              }) {
                //duplicate implant or medkit found
                if (shortcut.isInstanceOf[Shortcut.Implant]) {
                  //duplicate implant
                  targetShortcut match {
                    case Some(existingShortcut) =>
                      //redraw redundant shortcut slot with existing shortcut
                      sessionActor ! SessionActor.SendResponse(
                        CreateShortcutMessage(session.get.player.GUID, slot + 1, Some(AvatarShortcut.convert(existingShortcut)))
                      )
                    case _ =>
                      //blank shortcut slot
                      sessionActor ! SessionActor.SendResponse(CreateShortcutMessage(session.get.player.GUID, slot + 1, None))
                  }
                }
              } else {
                //macro, or implant or medkit
                val (optEffect1, optEffect2, optShortcut) = shortcut match {
                  case Shortcut.Macro(acro, msg) =>
                    (
                      acro,
                      msg,
                      Some(AvatarShortcut(shortcut.code, shortcut.tile, acro, msg))
                    )
                  case _ =>
                    (null, null, Some(AvatarShortcut(shortcut.code, shortcut.tile)))
                }
                targetShortcut match {
                  case Some(_) =>
                    ctx.run(
                      query[persistence.Shortcut]
                        .filter(_.avatarId == lift(avatar.id.toLong))
                        .filter(_.slot == lift(slot))
                        .update(
                          _.purpose -> lift(shortcut.code),
                          _.tile -> lift(shortcut.tile),
                          _.effect1 -> Option(lift(optEffect1)),
                          _.effect2 -> Option(lift(optEffect2))
                        )
                    )
                  case None =>
                    ctx.run(
                      query[persistence.Shortcut].insert(
                        _.avatarId -> lift(avatar.id.toLong),
                        _.slot -> lift(slot),
                        _.purpose -> lift(shortcut.code),
                        _.tile -> lift(shortcut.tile),
                        _.effect1 -> Option(lift(optEffect1)),
                        _.effect2 -> Option(lift(optEffect2))
                      )
                    )
                }
                avatar.shortcuts.update(slot, optShortcut)
              }
            }
          }
          Behaviors.same

        case RemoveShortcut(slot) =>
          import ctx._
          avatar.shortcuts.lift(slot).flatten match {
            case None => ;
            case Some(_) =>
              ctx.run(query[persistence.Shortcut]
                .filter(_.avatarId == lift(avatar.id.toLong))
                .filter(_.slot == lift(slot))
                .delete
              )
              avatar.shortcuts.update(slot, None)
          }
          Behaviors.same
      }
      .receiveSignal {
        case (_, PostStop) =>
          AvatarActor.saveAvatarData(avatar)
          staminaRegenTimer.cancel()
          implantTimers.values.foreach(_.cancel())
          saveLockerFunc()
          Behaviors.same
      }
  }

  def throwLoadoutFailure(msg: String): Future[Loadout] = {
    throwLoadoutFailure(new Exception(msg))
  }

  def throwLoadoutFailure(ex: Throwable): Future[Loadout] = {
    Future.failed(ex).asInstanceOf[Future[Loadout]]
  }

  def performAvatarLogin(avatarId: Long, accountId: Long, replyTo: ActorRef[AvatarLoginResponse]): Unit = {
    import ctx._

    val result = for {
      //log this login
      _ <- ctx.run(query[persistence.Avatar].filter(_.id == lift(avatarId))
        .update(_.lastLogin -> lift(LocalDateTime.now()))
      )
      //log this choice of faction (no empire switching)
      _ <- ctx.run(query[persistence.Account].filter(_.id == lift(accountId))
        .update(_.lastFactionId -> lift(avatar.faction.id))
      )
      //retrieve avatar data
      loadouts  <- initializeAllLoadouts()
      implants  <- ctx.run(query[persistence.Implant].filter(_.avatarId == lift(avatarId)))
      certs     <- ctx.run(query[persistence.Certification].filter(_.avatarId == lift(avatarId)))
      locker    <- loadLocker(avatarId)
      friends   <- loadFriendList(avatarId)
      ignored   <- loadIgnoredList(avatarId)
      shortcuts <- loadShortcuts(avatarId)
      saved     <- AvatarActor.loadSavedAvatarData(avatarId)
    } yield (loadouts, implants, certs, locker, friends, ignored, shortcuts, saved)
    result.onComplete {
      case Success((_loadouts, implants, certs, locker, friendsList, ignoredList, shortcutList, saved)) =>
        //shortcuts must have a hotbar option for each implant
//        val implantShortcuts = shortcutList.filter {
//          case Some(e) => e.purpose == 0
//          case None    => false
//        }
//        implants.filterNot { implant =>
//          implantShortcuts.exists {
//            case Some(a) => a.tile.equals(implant.name)
//            case None    => false
//          }
//        }.foreach { c =>
//          shortcutList.indexWhere { _.isEmpty } match {
//            case -1 => ;
//            case index =>
//              shortcutList.update(index, Some(AvatarShortcut(2, c.name)))
//          }
//        }
        //
        avatarCopy(
          avatar.copy(
            loadouts = avatar.loadouts.copy(suit = _loadouts),
            certifications =
              certs.map(cert => Certification.withValue(cert.id)).toSet ++ Config.app.game.baseCertifications,
            implants = implants.map(implant => Some(Implant(implant.toImplantDefinition))).padTo(3, None),
            shortcuts = shortcutList,
            locker = locker,
            people = MemberLists(
              friend = friendsList,
              ignored = ignoredList
            ),
            cooldowns = Cooldowns(
              purchase = AvatarActor.buildCooldownsFromClob(saved.purchaseCooldowns, Avatar.purchaseCooldowns, log),
              use = AvatarActor.buildCooldownsFromClob(saved.useCooldowns, Avatar.useCooldowns, log)
            )
          )
        )
        // if we need to start stamina regeneration
        tryRestoreStaminaForSession(stamina = 1) match {
          case Some(_) =>
            defaultStaminaRegen(initialDelay = 0.5f seconds)
          case _ => ;
        }
        replyTo ! AvatarLoginResponse(avatar)
      case Failure(e) =>
        log.error(e)("db failure")
    }
  }

  /**
    * na
    * @see `avatarCopy(Avatar)`
    * @param newAvatar na
    */
  def replaceAvatar(newAvatar: Avatar): Unit = {
    avatarCopy(newAvatar)
    avatar.deployables.UpdateMaxCounts(avatar.certifications)
    updateDeployableUIElements(
      avatar.deployables.UpdateUI()
    )
  }

  def setCosmetics(cosmetics: Set[Cosmetic]): Future[Unit] = {
    val p = Promise[Unit]()

    import ctx._
    ctx.run(
      query[persistence.Avatar]
        .filter(_.id == lift(avatar.id))
        .update(_.cosmetics -> lift(Some(Cosmetic.valuesToObjectCreateValue(cosmetics)): Option[Int]))
    )
      .onComplete {
        case Success(_) =>
          avatarCopy(avatar.copy(decoration = avatar.decoration.copy(cosmetics = Some(cosmetics))))
          session.get.zone.AvatarEvents ! AvatarServiceMessage(
            session.get.zone.id,
            AvatarAction.PlanetsideAttributeToAll(
              session.get.player.GUID,
              106,
              Cosmetic.valuesToAttributeValue(cosmetics)
            )
          )
          p.success(())
        case Failure(exception) =>
          p.failure(exception)
      }
    p.future
  }

  def tryRestoreStaminaForSession(stamina: Int): Option[Session] = {
    (session, _avatar) match {
      case (out @ Some(_), Some(a)) if !a.staminaFull && stamina > 0 => out
      case _                                                         => None
    }
  }

  def actuallyRestoreStaminaIfStationary(stamina: Int, session: Session): Unit = {
    val player = session.player
    if (player.VehicleSeated.nonEmpty || !(player.isMoving || player.Jumping)) {
      actuallyRestoreStamina(stamina, session)
    }
  }

  def actuallyRestoreStamina(stamina: Int, session: Session): Unit = {
    val originalStamina = avatar.stamina
    val maxStamina      = avatar.maxStamina
    val totalStamina    = math.min(maxStamina, originalStamina + stamina)
    if (originalStamina < totalStamina) {
      val originalFatigued = avatar.fatigued
      val isFatigued       = totalStamina < 20
      avatar = avatar.copy(stamina = totalStamina, fatigued = isFatigued)
      if (totalStamina == maxStamina) {
        staminaRegenTimer.cancel()
        staminaRegenTimer = Default.Cancellable
      }
      if (session.player.HasGUID) {
        val guid = session.player.GUID
        if (originalFatigued && !isFatigued) {
          avatar.implants.zipWithIndex.foreach {
            case (Some(_), slot) =>
              sessionActor ! SessionActor.SendResponse(AvatarImplantMessage(guid, ImplantAction.OutOfStamina, slot, 0))
            case _ => ;
          }
        }
        sessionActor ! SessionActor.SendResponse(PlanetsideAttributeMessage(guid, 2, totalStamina))
      }
    }
  }

  def restoreStaminaPeriodically(stamina: Int): Unit = {
    tryRestoreStaminaForSession(stamina) match {
      case Some(sess) =>
        actuallyRestoreStaminaIfStationary(stamina, sess)
      case _ => ;
    }
    startIfStoppedStaminaRegen(initialDelay = 0.5f seconds)
  }

  /**
    * Drain at most a given amount of stamina from the player's pool of stamina.
    * If the player's reserves become zero in the act, inform the player that he is fatigued
    * meaning that he will only be able to walk, all implants will deactivate,
    * and all exertion that require stamina use will become impossible until a threshold of stamina is regained.
    * @param stamina an amount to drain
    * @return `true`, as long as the requested amount of stamina can be drained in total;
    *        `false`, otherwise
    */
  def consumeThisMuchStamina(stamina: Int): Boolean = {
    if (stamina < 1) {
      true
    } else {
      val resultingStamina = avatar.stamina - stamina
      val totalStamina     = math.max(0, resultingStamina)
      val alreadyFatigued  = avatar.fatigued
      val becomeFatigued   = !alreadyFatigued && totalStamina == 0
      avatarCopy(avatar.copy(stamina = totalStamina, fatigued = alreadyFatigued || becomeFatigued))
      startIfStoppedStaminaRegen(initialDelay = 0.5f seconds)
      val player = session.get.player
      if (player.HasGUID) {
        if (becomeFatigued) {
          avatar.implants.zipWithIndex.foreach {
            case (Some(implant), slot) =>
              if (implant.active) {
                deactivateImplant(implant.definition.implantType)
              }
              sessionActor ! SessionActor.SendResponse(
                AvatarImplantMessage(player.GUID, ImplantAction.OutOfStamina, slot, 1)
              )
            case _ => ;
          }
        }
        sessionActor ! SessionActor.SendResponse(PlanetsideAttributeMessage(player.GUID, 2, totalStamina))
      } else if (becomeFatigued) {
        avatarCopy(avatar.copy(implants = avatar.implants.zipWithIndex.collect {
          case (Some(implant), slot) if implant.active =>
            implantTimers.get(slot).foreach(_.cancel())
            Some(implant.copy(active = false))
          case (out, _) =>
            out
        }))
      }
      resultingStamina >= 0
    }
  }

  def initializeImplants(): Unit = {
    avatar.implants.zipWithIndex.foreach {
      case (Some(implant), slot) =>
        // TODO if this implant is Installed but does not have shortcut, add to a free slot or write over slot 61/62/63
        // for now, just write into slots 2, 3 and 4
        sessionActor ! SessionActor.SendResponse(
          CreateShortcutMessage(
            session.get.player.GUID,
            slot + 2,
            Some(implant.definition.implantType.shortcut)
          )
        )

        implantTimers.get(slot).foreach(_.cancel())
        implantTimers(slot) = context.scheduleOnce(
          implant.definition.InitializationDuration.seconds,
          context.self,
          SetImplantInitialized(implant.definition.implantType)
        )

        // Start client side initialization timer, visible on the character screen
        // Progress accumulates according to the client's knowledge of the implant initialization time
        // What is normally a 60s timer that is set to 120s on the server will still visually update as if 60s\
        session.get.zone.AvatarEvents ! AvatarServiceMessage(
          avatar.name,
          AvatarAction.SendResponse(Service.defaultPlayerGUID, ActionProgressMessage(slot + 6, 0))
        )

      case (None, _) => ;
    }
  }

  def deinitializeImplants(): Unit = {
    avatarCopy(avatar.copy(implants = avatar.implants.zipWithIndex.map {
      case (Some(implant), slot) =>
        if (implant.active) {
          deactivateImplant(implant.definition.implantType)
        }
        session.get.zone.AvatarEvents ! AvatarServiceMessage(
          session.get.zone.id,
          AvatarAction.SendResponse(
            Service.defaultPlayerGUID,
            AvatarImplantMessage(session.get.player.GUID, ImplantAction.Initialization, slot, 0)
          )
        )
        Some(implant.copy(initialized = false, active = false))
      case (None, _) => None
    }))
  }

  def resetAnImplant(implantType: ImplantType): Unit = {
    avatar.implants.zipWithIndex.find {
      case (Some(imp), _) => imp.definition.implantType == implantType
      case (None, _)      => false
    } match {
      case Some((Some(imp), index)) =>
        //deactivate
        if (imp.active) {
          deactivateImplant(implantType)
        }
        //deinitialize
        session.get.zone.AvatarEvents ! AvatarServiceMessage(
          session.get.zone.id,
          AvatarAction.SendResponse(
            Service.defaultPlayerGUID,
            AvatarImplantMessage(session.get.player.GUID, ImplantAction.Initialization, index, 0)
          )
        )
        avatarCopy(
          avatar.copy(
            implants = avatar.implants.updated(index, Some(imp.copy(initialized = false, active = false)))
          )
        )
        //restart initialization process
        implantTimers.get(index).foreach(_.cancel())
        implantTimers(index) = context.scheduleOnce(
          imp.definition.InitializationDuration.seconds,
          context.self,
          SetImplantInitialized(implantType)
        )
        session.get.zone.AvatarEvents ! AvatarServiceMessage(
          avatar.name,
          AvatarAction.SendResponse(Service.defaultPlayerGUID, ActionProgressMessage(index + 6, 0))
        )
      case _ => ;
    }
  }

  def deactivateImplant(implantType: ImplantType): Unit = {
    avatar.implants.zipWithIndex.collectFirst {
      case (Some(implant), index) if implant.definition.implantType == implantType => (implant, index)
    } match {
      case Some((implant, slot)) =>
        implantTimers.get(slot).foreach(_.cancel())
        avatarCopy(
          avatar.copy(
            implants = avatar.implants.updated(slot, Some(implant.copy(active = false)))
          )
        )
        // Deactivation sound / effect
        session.get.zone.AvatarEvents ! AvatarServiceMessage(
          session.get.zone.id,
          AvatarAction.PlanetsideAttribute(session.get.player.GUID, 28, implant.definition.implantType.value * 2)
        )
        sessionActor ! SessionActor.SendResponse(
          AvatarImplantMessage(session.get.player.GUID, ImplantAction.Activation, slot, 0)
        )
      case None => log.error(s"requested deactivation of unknown implant $implantType")
    }
  }

  /** Send list of avatars to client (show character selection screen) */
  def sendAvatars(account: Account): Unit = {
    import ctx._
    val result = ctx.run(query[persistence.Avatar].filter(_.accountId == lift(account.id)))
    result.onComplete {
      case Success(avatars) =>
        val gen       = new AtomicInteger(1)
        val converter = new CharacterSelectConverter

        avatars.filter(!_.deleted) foreach { a =>
          val secondsSinceLastLogin = Seconds.secondsBetween(a.lastLogin, LocalDateTime.now()).getSeconds
          val avatar                = a.toAvatar
          val player                = new Player(avatar)

          player.ExoSuit = ExoSuitType.Reinforced
          player.Slot(0).Equipment = Tool(GlobalDefinitions.StandardPistol(player.Faction))
          player.Slot(1).Equipment = Tool(GlobalDefinitions.MediumPistol(player.Faction))
          player.Slot(2).Equipment = Tool(GlobalDefinitions.HeavyRifle(player.Faction))
          player.Slot(3).Equipment = Tool(GlobalDefinitions.AntiVehicularLauncher(player.Faction))
          player.Slot(4).Equipment = Tool(GlobalDefinitions.katana)

          /** After a client has connected to the server, their account is used to generate a list of characters.
            * On the character selection screen, each of these characters is made to exist temporarily when one is selected.
            * This "character select screen" is an isolated portion of the client, so it does not have any external constraints.
            * Temporary global unique identifiers are assigned to the underlying `Player` objects so that they can be turned into packets.
            */
          player
            .Holsters()
            .foreach(holster =>
              holster.Equipment match {
                case Some(tool: Tool) =>
                  tool.AmmoSlots.foreach(slot => {
                    slot.Box.GUID = PlanetSideGUID(gen.getAndIncrement)
                  })
                  tool.GUID = PlanetSideGUID(gen.getAndIncrement)
                case Some(item: Equipment) =>
                  item.GUID = PlanetSideGUID(gen.getAndIncrement)
                case _ => ;
              }
            )
          player.GUID = PlanetSideGUID(gen.getAndIncrement)
          player.Spawn()
          sessionActor ! SessionActor.SendResponse(
            ObjectCreateDetailedMessage(
              ObjectClass.avatar,
              player.GUID,
              converter.DetailedConstructorData(player).get
            )
          )
          sessionActor ! SessionActor.SendResponse(
            CharacterInfoMessage(
              15,
              PlanetSideZoneID(4),
              avatar.id,
              player.GUID,
              finished = false,
              secondsSinceLastLogin
            )
          )
          /** After the user has selected a character to load from the "character select screen,"
            * the temporary global unique identifiers used for that screen are stripped from the underlying `Player` object that was selected.
            * Characters that were not selected may  be destroyed along with their temporary GUIDs.
            */
          player
            .Holsters()
            .foreach(holster =>
              holster.Equipment match {
                case Some(item: Tool) =>
                  item.AmmoSlots.foreach(slot => {
                    slot.Box.Invalidate()
                  })
                  item.Invalidate()
                case Some(item: Equipment) =>
                  item.Invalidate()
                case _ => ;
              }
            )
          player.Invalidate()
        }
        sessionActor ! SessionActor.SendResponse(
          CharacterInfoMessage(15, PlanetSideZoneID(0), 0, PlanetSideGUID(0), finished = true, 0)
        )

      case Failure(e) => log.error(e)("db failure")
    }
  }

  def storeLoadout(owner: Player, label: String, line: Int): Future[Loadout] = {
    import ctx._
    sessionActor ! SessionActor.CharSaved
    val items: String = AvatarActor.buildClobFromPlayerLoadout(owner)
    for {
      loadouts <- ctx.run(
        query[persistence.Loadout].filter(_.avatarId == lift(owner.CharId)).filter(_.loadoutNumber == lift(line))
      )
      _ <- loadouts.headOption match {
        case Some(loadout) =>
          ctx.run(
            query[persistence.Loadout]
              .filter(_.id == lift(loadout.id))
              .update(_.exosuitId -> lift(owner.ExoSuit.id), _.name -> lift(label), _.items -> lift(items))
          )
        case None =>
          ctx.run(
            query[persistence.Loadout].insert(
              _.exosuitId     -> lift(owner.ExoSuit.id),
              _.name          -> lift(label),
              _.items         -> lift(items),
              _.avatarId      -> lift(owner.avatar.id),
              _.loadoutNumber -> lift(line)
            )
          )
      }
    } yield Loadout.Create(owner, label)
  }

  def storeVehicleLoadout(owner: Player, label: String, line: Int, vehicle: Vehicle): Future[Loadout] = {
    import ctx._
    val items: String = {
      val clobber: mutable.StringBuilder = new StringBuilder()
      //encode holsters
      vehicle.Weapons
        .collect {
          case (index, slot: EquipmentSlot) if slot.Equipment.nonEmpty =>
            clobber.append(AvatarActor.encodeLoadoutClobFragment(slot.Equipment.get, index))
        }
      //encode inventory
      vehicle.Inventory.Items.foreach {
        case InventoryItem(obj, index) =>
          clobber.append(AvatarActor.encodeLoadoutClobFragment(obj, index))
      }
      clobber.mkString.drop(1)
    }

    sessionActor ! SessionActor.CharSaved
    for {
      loadouts <- ctx.run(
        query[persistence.Vehicleloadout]
          .filter(_.avatarId == lift(owner.CharId))
          .filter(_.loadoutNumber == lift(line))
      )
      _ <- loadouts.headOption match {
        case Some(loadout) =>
          ctx.run(
            query[persistence.Vehicleloadout]
              .filter(_.id == lift(loadout.id))
              .update(_.name -> lift(label), _.vehicle -> lift(vehicle.Definition.ObjectId), _.items -> lift(items))
          )
        case None =>
          ctx.run(
            query[persistence.Vehicleloadout].insert(
              _.avatarId      -> lift(owner.avatar.id),
              _.loadoutNumber -> lift(line),
              _.name          -> lift(label),
              _.vehicle       -> lift(vehicle.Definition.ObjectId),
              _.items         -> lift(items)
            )
          )
      }
    } yield Loadout.Create(vehicle, label)
  }

  def storeNewLocker(): Unit = {
    if (_avatar.nonEmpty) {
      pushLockerClobToDataBase(AvatarActor.encodeLockerClob(avatar.locker))
        .onComplete {
          case Success(_) =>
            saveLockerFunc = storeLocker
            log.debug(s"saving locker contents belonging to ${avatar.name}")
          case Failure(e) =>
            saveLockerFunc = doNotStoreLocker
            log.error(e)("db failure")
        }
    }
  }

  def doNotStoreLocker(): Unit = {
    /* most likely the database encountered an error; don't do anything with it until the restart */
  }

  def storeLocker(): Unit = {
    log.debug(s"saving locker contents belonging to ${avatar.name}")
    pushLockerClobToDataBase(AvatarActor.encodeLockerClob(avatar.locker))
  }

  def pushLockerClobToDataBase(items: String): Database.ctx.Result[Database.ctx.RunActionResult] = {
    import ctx._
    sessionActor ! SessionActor.CharSaved
    ctx.run(
      query[persistence.Locker]
        .filter(_.avatarId == lift(avatar.id))
        .update(_.items -> lift(items))
    )
  }

  def initializeAllLoadouts(): Future[Seq[Option[Loadout]]] = {
    for {
      infantry <- loadLoadouts().andThen {
        case out @ Success(_) => out
        case Failure(_)       => Future(Array.fill[Option[Loadout]](10)(None).toSeq)
      }
      vehicles <- loadVehicleLoadouts().andThen {
        case out @ Success(_) => out
        case Failure(_)       => Future(Array.fill[Option[Loadout]](10)(None).toSeq)
      }
    } yield infantry ++ vehicles
  }

  def loadLoadouts(): Future[Seq[Option[Loadout]]] = {
    import ctx._
    ctx
      .run(query[persistence.Loadout].filter(_.avatarId == lift(avatar.id)))
      .map { loadouts =>
        loadouts.map { loadout =>
          val doll = new Player(Avatar(0, "doll", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
          doll.ExoSuit = ExoSuitType(loadout.exosuitId)
          AvatarActor.buildContainedEquipmentFromClob(doll, loadout.items, log)

          val result = (loadout.loadoutNumber, Loadout.Create(doll, loadout.name))
          (0 until 4).foreach(index => {
            doll.Slot(index).Equipment = None
          })
          doll.Inventory.Clear()
          result
        }
      }
      .map { loadouts => (0 until 10).map { index => loadouts.find(_._1 == index).map(_._2) } }
  }

  def loadVehicleLoadouts(): Future[Seq[Option[Loadout]]] = {
    import ctx._
    ctx
      .run(query[persistence.Vehicleloadout].filter(_.avatarId == lift(avatar.id)))
      .map { loadouts =>
        loadouts.map { loadout =>
          val definition = DefinitionUtil.idToDefinition(loadout.vehicle).asInstanceOf[VehicleDefinition]
          val toy        = new Vehicle(definition)
          AvatarActor.buildContainedEquipmentFromClob(toy, loadout.items, log)

          val result = (loadout.loadoutNumber, Loadout.Create(toy, loadout.name))
          toy.Weapons.values.foreach(slot => {
            slot.Equipment = None
          })
          toy.Inventory.Clear()
          result
        }
      }
      .map { loadouts => (0 until 10).map { index => loadouts.find(_._1 == index).map(_._2) } }
  }

  def refreshLoadouts(loadouts: Iterable[(Option[Loadout], Int)]): Unit = {
    loadouts
      .map {
        case (Some(loadout: InfantryLoadout), index) =>
          FavoritesMessage.Infantry(
            session.get.player.GUID,
            index,
            loadout.label,
            InfantryLoadout.DetermineSubtypeB(loadout.exosuit, loadout.subtype)
          )
        case (Some(loadout: VehicleLoadout), index)
            if GlobalDefinitions.isBattleFrameVehicle(loadout.vehicle_definition) =>
          FavoritesMessage.Battleframe(
            session.get.player.GUID,
            index - 15,
            loadout.label,
            VehicleLoadout.DetermineBattleframeSubtype(loadout.vehicle_definition)
          )
        case (Some(loadout: VehicleLoadout), index) =>
          FavoritesMessage.Vehicle(
            session.get.player.GUID,
            index - 10,
            loadout.label
          )
        case (_, index) =>
          val (mtype, lineNo) = if (index > 14) {
            (LoadoutType.Battleframe, index - 15)
          } else if (index < 10) {
            (LoadoutType.Infantry, index)
          } else {
            (LoadoutType.Vehicle, index - 10)
          }
          FavoritesMessage(
            mtype,
            session.get.player.GUID,
            lineNo,
            "",
            0
          )
      }
      .foreach { sessionActor ! SessionActor.SendResponse(_) }
  }

  def refreshLoadout(line: Int): Unit = {
    avatar.loadouts.suit.lift(line) match {
      case Some(Some(loadout: InfantryLoadout)) =>
        sessionActor ! SessionActor.SendResponse(
          FavoritesMessage.Infantry(
            session.get.player.GUID,
            line,
            loadout.label,
            InfantryLoadout.DetermineSubtypeB(loadout.exosuit, loadout.subtype)
          )
        )
      case Some(Some(loadout: VehicleLoadout)) if GlobalDefinitions.isBattleFrameVehicle(loadout.vehicle_definition) =>
        sessionActor ! SessionActor.SendResponse(
          FavoritesMessage.Battleframe(
            session.get.player.GUID,
            line - 15,
            loadout.label,
            VehicleLoadout.DetermineBattleframeSubtype(loadout.vehicle_definition)
          )
        )
      case Some(Some(loadout: VehicleLoadout)) =>
        sessionActor ! SessionActor.SendResponse(
          FavoritesMessage.Vehicle(
            session.get.player.GUID,
            line - 10,
            loadout.label
          )
        )
      case Some(None) =>
        val (mtype, lineNo, subtype) = if (line > 14) {
          (LoadoutType.Battleframe, line - 15, Some(0))
        } else if (line < 10) {
          (LoadoutType.Infantry, line, Some(0))
        } else {
          (LoadoutType.Vehicle, line - 10, None)
        }
        sessionActor ! SessionActor.SendResponse(
          FavoritesMessage(
            mtype,
            session.get.player.GUID,
            lineNo,
            "",
            subtype
          )
        )
      case _ => ;
    }
  }

  def loadLocker(charId: Long): Future[LockerContainer] = {
    val locker = Avatar.makeLocker()
    var notLoaded: Boolean = false
    import ctx._
    val out = ctx.run(query[persistence.Locker]
      .filter(_.avatarId == lift(charId)))
      .map { entry =>
        notLoaded = false
        entry.foreach { contents => AvatarActor.buildContainedEquipmentFromClob(locker, contents.items, log) }
      }
      .map { _ => locker }
    out.onComplete {
      case Success(_) =>
        saveLockerFunc = storeLocker
      case Failure(_) =>
        notLoaded = true
    }
    if (notLoaded) {
      //default empty locker
      ctx.run(query[persistence.Locker]
        .insert(_.avatarId -> lift(avatar.id), _.items -> lift("")))
        .onComplete {
          case Success(_) =>
            saveLockerFunc = storeLocker
          case Failure(e) =>
            saveLockerFunc = doNotStoreLocker
            log.error(e)("db failure")
        }
    }
    out
  }



  def loadFriendList(avatarId: Long): Future[List[AvatarFriend]] = {
    import ctx._
    val out: Promise[List[AvatarFriend]] = Promise()

    val queryResult = ctx.run(
      query[persistence.Friend].filter { _.avatarId == lift(avatarId) }
        .join(query[persistence.Avatar])
        .on { case (friend, avatar) => friend.charId == avatar.id }
        .map { case (_, avatar) => (avatar.id, avatar.name, avatar.factionId) }
    )
    queryResult.onComplete {
      case Success(list) =>
        out.completeWith(Future(
          list.map { case (id, name, faction) => AvatarFriend(id, name, PlanetSideEmpire(faction)) }.toList
        ))
      case _ =>
        out.completeWith(Future(List.empty[AvatarFriend]))
    }
    out.future
  }

  def loadIgnoredList(avatarId: Long): Future[List[AvatarIgnored]] = {
    import ctx._
    val out: Promise[List[AvatarIgnored]] = Promise()

    val queryResult = ctx.run(
      query[persistence.Ignored].filter { _.avatarId == lift(avatarId) }
        .join(query[persistence.Avatar])
        .on { case (friend, avatar) => friend.charId == avatar.id }
        .map { case (_, avatar) => (avatar.id, avatar.name) }
    )
    queryResult.onComplete {
      case Success(list) =>
        out.completeWith(Future(
          list.map { case (id, name) => AvatarIgnored(id, name) }.toList
        ))
      case _ =>
        out.completeWith(Future(List.empty[AvatarIgnored]))
    }
    out.future
  }

  def loadShortcuts(avatarId: Long): Future[Array[Option[AvatarShortcut]]] = {
    import ctx._
    val out: Promise[Array[Option[AvatarShortcut]]] = Promise()

    val queryResult = ctx.run(
      query[persistence.Shortcut].filter { _.avatarId == lift(avatarId) }
        .map { shortcut => (shortcut.slot, shortcut.purpose, shortcut.tile, shortcut.effect1, shortcut.effect2) }
    )
    val output = Array.fill[Option[AvatarShortcut]](64)(None)
    queryResult.onComplete {
      case Success(list) =>
        list.foreach { case (slot, purpose, tile, effect1, effect2) =>
          output.update(slot, Some(AvatarShortcut(purpose, tile, effect1.getOrElse(""), effect2.getOrElse(""))))
        }
        out.completeWith(Future(output))
      case Failure(e) =>
        //something went wrong, but we can recover
        log.warn(e)("db failure")
        //output.update(0, Some(AvatarShortcut(0, "medkit")))
        out.completeWith(Future(output))
    }
    out.future
  }

  def startIfStoppedStaminaRegen(initialDelay: FiniteDuration): Unit = {
    if (staminaRegenTimer.isCancelled) {
      defaultStaminaRegen(initialDelay)
    }
  }

  def defaultStaminaRegen(initialDelay: FiniteDuration): Unit = {
    staminaRegenTimer.cancel()
    val restoreStaminaFunc: Int => Unit = restoreStaminaPeriodically
    staminaRegenTimer = context.system.scheduler.scheduleWithFixedDelay(initialDelay, delay = 0.5 seconds)(() => {
      restoreStaminaFunc(1)
    })
  }

  // same as in SA, this really doesn't belong here
  def updateDeployableUIElements(list: List[(Int, Int, Int, Int)]): Unit = {
    val guid = PlanetSideGUID(0)
    list.foreach({
      case (currElem, curr, maxElem, max) =>
        //fields must update in ordered pairs: max, curr
        sessionActor ! SessionActor.SendResponse(PlanetsideAttributeMessage(guid, maxElem, max))
        sessionActor ! SessionActor.SendResponse(PlanetsideAttributeMessage(guid, currElem, curr))
    })
  }

  def refreshPurchaseTimes(keys: Set[String]): Unit = {
    var keysToDrop: Seq[String] = Nil
    keys.foreach { key =>
      avatar.cooldowns.purchase.find { case (name, _) => name.equals(key) } match {
        case Some((name, purchaseTime)) =>
          val secondsSincePurchase = Seconds.secondsBetween(purchaseTime, LocalDateTime.now()).getSeconds
          Avatar.purchaseCooldowns.find(_._1.Name == name) match {
            case Some((obj, cooldown)) if cooldown.toSeconds - secondsSincePurchase > 0 =>
              val (_, name) = AvatarActor.resolvePurchaseTimeName(avatar.faction, obj)
              updatePurchaseTimer(
                name,
                cooldown.toSeconds - secondsSincePurchase,
                DefinitionUtil.fromString(name).isInstanceOf[VehicleDefinition]
              )

            case _ =>
              keysToDrop = keysToDrop :+ key //key has timed-out
          }
        case _ => ;
      }
    }
    if (keysToDrop.nonEmpty) {
      val cdown = avatar.cooldowns
      avatarCopy(avatar.copy(cooldowns = cdown.copy(purchase = cdown.purchase.removedAll(keysToDrop))))
    }
  }

  def updatePurchaseTimer(name: String, time: Long, isActuallyAVehicle: Boolean): Unit = {
    sessionActor ! SessionActor.SendResponse(
      AvatarVehicleTimerMessage(session.get.player.GUID, name, time, isActuallyAVehicle)
    )
  }

  /**
    * na
    * @see `replaceCopy(Avatar)`
    * @param copyAvatar na
    */
  def avatarCopy(copyAvatar: Avatar): Unit = {
    avatar = copyAvatar
    session match {
      case Some(sess) if sess.player != null =>
        sess.player.avatar = copyAvatar
      case _ => ;
    }
  }

  /**
    * Branch based on behavior of the request for the friends list or the ignored people list.
    * @param action nature of the request
    * @param name other player's name (can not be our name)
    */
  def memberListAction(action: MemberAction.Value, name: String): Unit = {
    if (!name.equals(avatar.name)) {
      action match {
        case MemberAction.InitializeFriendList => memberActionListManagement(action, transformFriendsList)
        case MemberAction.InitializeIgnoreList => memberActionListManagement(action, transformIgnoredList)
        case MemberAction.UpdateFriend         => memberActionUpdateFriend(name)
        case MemberAction.AddFriend            => getAvatarForFunc(name, memberActionAddFriend)
        case MemberAction.RemoveFriend         => getAvatarForFunc(name, formatForOtherFunc(memberActionRemoveFriend))
        case MemberAction.AddIgnoredPlayer     => getAvatarForFunc(name, memberActionAddIgnored)
        case MemberAction.RemoveIgnoredPlayer  => getAvatarForFunc(name, formatForOtherFunc(memberActionRemoveIgnored))
        case _ => ;
      }
    }
  }

  /**
    * Transform the friends list in a list of packet entities.
    * @return a list of `Friends` suitable for putting into a packet
    */
  def transformFriendsList(): List[GameFriend] = {
    avatar.people.friend.map { f => GameFriend(f.name, f.online)}
  }
  /**
    * Transform the ignored players list in a list of packet entities.
    * @return a list of `Friends` suitable for putting into a packet
    */
  def transformIgnoredList(): List[GameFriend] = {
    avatar.people.ignored.map { f => GameFriend(f.name, f.online)}
  }
  /**
    * Reload the list of friend players or ignored players for the client.
    * This does not update any player's online status, but merely reloads current states.
    * @param action nature of the request
    *               (either `InitializeFriendList` or `InitializeIgnoreList`, hopefully)
    * @param listFunc transformation function that produces data suitable for a game paket
    */
  def memberActionListManagement(action: MemberAction.Value, listFunc: ()=>List[GameFriend]): Unit = {
    FriendsResponse.packetSequence(action, listFunc()).foreach { msg =>
      sessionActor ! SessionActor.SendResponse(msg)
    }
  }

  /**
    * Add another player's data to the list of friend players and report back whether or not that player is online.
    * Update the database appropriately.
    * @param charId unique account identifier
    * @param name unique character name
    * @param faction a faction affiliation
    */
  def memberActionAddFriend(charId: Long, name: String, faction: Int): Unit = {
    val people = avatar.people
    people.friend.find { _.name.equals(name) } match {
      case Some(_) => ;
      case None =>
        import ctx._
        ctx.run(query[persistence.Friend]
          .insert(
            _.avatarId -> lift(avatar.id.toLong),
            _.charId -> lift(charId)
          )
        )
        val isOnline = onlineIfNotIgnoredEitherWay(avatar, name)
        replaceAvatar(avatar.copy(
          people = people.copy(friend = people.friend :+ AvatarFriend(charId, name, PlanetSideEmpire(faction), isOnline))
        ))
        sessionActor ! SessionActor.SendResponse(FriendsResponse(MemberAction.AddFriend, GameFriend(name, isOnline)))
        sessionActor ! SessionActor.CharSaved
    }
  }

  /**
    * Remove another player's data from the list of friend players.
    * Update the database appropriately.
    * @param charId unique account identifier
    * @param name unique character name
    */
  def memberActionRemoveFriend(charId: Long, name: String): Unit = {
    import ctx._
    val people = avatar.people
    people.friend.find { _.name.equals(name) } match {
      case Some(_) =>
        replaceAvatar(
          avatar.copy(people = people.copy(friend = people.friend.filterNot { _.charId == charId }))
        )
      case None => ;
    }
    ctx.run(query[persistence.Friend]
      .filter(_.avatarId == lift(avatar.id))
      .filter(_.charId == lift(charId))
      .delete
    )
    sessionActor ! SessionActor.SendResponse(FriendsResponse(MemberAction.RemoveFriend, GameFriend(name)))
    sessionActor ! SessionActor.CharSaved
  }

  /**
    *
    * @param name unique character name
    * @return if the avatar is found, that avatar's unique identifier and the avatar's faction affiliation
    */
  def memberActionUpdateFriend(name: String): Option[(Long, PlanetSideEmpire.Value)] = {
    if (name.nonEmpty) {
      val people = avatar.people
      people.friend.find { _.name.equals(name) } match {
        case Some(otherFriend) =>
          val (out, online) = LivePlayerList.WorldPopulation({ case (_, a) => a.name.equals(name) }).headOption match {
            case Some(otherAvatar) =>
              (
                Some((otherAvatar.id.toLong, otherAvatar.faction)),
                onlineIfNotIgnoredEitherWay(otherAvatar, avatar)
              )
            case None =>
              (None, false)
          }
          replaceAvatar(avatar.copy(
            people = people.copy(
              friend = people.friend.filterNot { _.name.equals(name) } :+ otherFriend.copy(online = online)
            )
          ))
          sessionActor ! SessionActor.SendResponse(FriendsResponse(MemberAction.UpdateFriend, GameFriend(name, online)))
          out
        case None =>
          None
      }
    } else {
      None
    }
  }

  /**
    * Add another player's data to the list of ignored players.
    * Update the database appropriately.
    * The change affects not only this player but also the player being ignored
    * by denying online visibility of the former to the latter.
    * @param charId unique account identifier
    * @param name unique character name
    * @param faction a faction affiliation
    */
  def memberActionAddIgnored(charId: Long, name: String, faction: Int): Unit = {
    val people = avatar.people
    people.ignored.find { _.name.equals(name) } match {
      case Some(_) => ;
      case None =>
        import ctx._
        ctx.run(query[persistence.Ignored]
          .insert(
            _.avatarId -> lift(avatar.id.toLong),
            _.charId -> lift(charId)
          )
        )
        replaceAvatar(
          avatar.copy(people = people.copy(ignored = people.ignored :+ AvatarIgnored(charId, name)))
        )
        sessionActor ! SessionActor.UpdateIgnoredPlayers(FriendsResponse(MemberAction.AddIgnoredPlayer, GameFriend(name)))
        sessionActor ! SessionActor.CharSaved
    }
  }

  /**
    * Remove another player's data from the list of ignored players.
    * Update the database appropriately.
    * The change affects not only this player but also the player formerly being ignored
    * by restoring online visibility of the former to the latter.
    * @param charId unique account identifier
    * @param name unique character name
    */
  def memberActionRemoveIgnored(charId: Long, name: String): Unit = {
    import ctx._
    val people = avatar.people
    people.ignored.find { _.name.equals(name) } match {
      case Some(_) =>
        replaceAvatar(
          avatar.copy(people = people.copy(ignored = people.ignored.filterNot { _.charId == charId }))
        )
      case None => ;
    }
    ctx.run(query[persistence.Ignored]
      .filter(_.avatarId == lift(avatar.id.toLong))
      .filter(_.charId == lift(charId))
      .delete
    )
    sessionActor ! SessionActor.UpdateIgnoredPlayers(FriendsResponse(MemberAction.RemoveIgnoredPlayer, GameFriend(name)))
    sessionActor ! SessionActor.CharSaved
  }
}
