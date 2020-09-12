package net.psforever.objects.avatar

import net.psforever.objects.definition.{AvatarDefinition, BasicDefinition}
import net.psforever.objects.equipment.{EquipmentSize, EquipmentSlot}
import net.psforever.objects.inventory.LocallyRegisteredInventory
import net.psforever.objects.loadouts.{Loadout, SquadLoadout}
import net.psforever.objects.locker.{LockerContainer, LockerEquipment}
import net.psforever.objects.{GlobalDefinitions, OffhandEquipmentSlot}
import net.psforever.types._
import org.joda.time.{Duration, LocalDateTime, Seconds}

import scala.collection.immutable.Seq
import scala.concurrent.duration._

object Avatar {
  val purchaseCooldowns: Map[BasicDefinition, FiniteDuration] = Map(
    GlobalDefinitions.ams                   -> 5.minutes,
    GlobalDefinitions.ant                   -> 5.minutes,
    GlobalDefinitions.apc_nc                -> 5.minutes,
    GlobalDefinitions.apc_tr                -> 5.minutes,
    GlobalDefinitions.apc_vs                -> 5.minutes,
    GlobalDefinitions.aurora                -> 5.minutes,
    GlobalDefinitions.battlewagon           -> 5.minutes,
    GlobalDefinitions.dropship              -> 5.minutes,
    GlobalDefinitions.flail                 -> 5.minutes,
    GlobalDefinitions.fury                  -> 5.minutes,
    GlobalDefinitions.galaxy_gunship        -> 10.minutes,
    GlobalDefinitions.lodestar              -> 5.minutes,
    GlobalDefinitions.liberator             -> 5.minutes,
    GlobalDefinitions.lightgunship          -> 5.minutes,
    GlobalDefinitions.lightning             -> 5.minutes,
    GlobalDefinitions.magrider              -> 5.minutes,
    GlobalDefinitions.mediumtransport       -> 5.minutes,
    GlobalDefinitions.mosquito              -> 5.minutes,
    GlobalDefinitions.phantasm              -> 5.minutes,
    GlobalDefinitions.prowler               -> 5.minutes,
    GlobalDefinitions.quadassault           -> 5.minutes,
    GlobalDefinitions.quadstealth           -> 5.minutes,
    GlobalDefinitions.router                -> 5.minutes,
    GlobalDefinitions.switchblade           -> 5.minutes,
    GlobalDefinitions.skyguard              -> 5.minutes,
    GlobalDefinitions.threemanheavybuggy    -> 5.minutes,
    GlobalDefinitions.thunderer             -> 5.minutes,
    GlobalDefinitions.two_man_assault_buggy -> 5.minutes,
    GlobalDefinitions.twomanhoverbuggy      -> 5.minutes,
    GlobalDefinitions.twomanheavybuggy      -> 5.minutes,
    GlobalDefinitions.vanguard              -> 5.minutes,
    GlobalDefinitions.vulture               -> 5.minutes,
    GlobalDefinitions.wasp                  -> 5.minutes,
    GlobalDefinitions.flamethrower          -> 3.minutes,
    GlobalDefinitions.nchev_sparrow         -> 5.minutes,
    GlobalDefinitions.nchev_falcon          -> 5.minutes,
    GlobalDefinitions.nchev_scattercannon   -> 5.minutes,
    GlobalDefinitions.vshev_comet           -> 5.minutes,
    GlobalDefinitions.vshev_quasar          -> 5.minutes,
    GlobalDefinitions.vshev_starfire        -> 5.minutes,
    GlobalDefinitions.trhev_burster         -> 5.minutes,
    GlobalDefinitions.trhev_dualcycler      -> 5.minutes,
    GlobalDefinitions.trhev_pounder         -> 5.minutes
  )

  val useCooldowns: Map[BasicDefinition, FiniteDuration] = Map(
    GlobalDefinitions.medkit           -> 5.seconds,
    GlobalDefinitions.super_armorkit   -> 20.minutes,
    GlobalDefinitions.super_medkit     -> 20.minutes,
    GlobalDefinitions.super_staminakit -> 20.minutes
  )
}

case class Avatar(
    /** unique identifier corresponding to a database table row index */
    id: Int,
    name: String,
    faction: PlanetSideEmpire.Value,
    sex: CharacterGender.Value,
    head: Int,
    voice: CharacterVoice.Value,
    bep: Long = 0,
    cep: Long = 0,
    stamina: Int = 100,
    fatigued: Boolean = false,
    cosmetics: Option[Set[Cosmetic]] = None,
    certifications: Set[Certification] = Set(),
    loadouts: Seq[Option[Loadout]] = Seq.fill(15)(None),
    squadLoadouts: Seq[Option[SquadLoadout]] = Seq.fill(10)(None),
    implants: Seq[Option[Implant]] = Seq(None, None, None),
    locker: LockerContainer = new LockerContainer({
      val inv = new LocallyRegisteredInventory(numbers = 40150 until 40450) // TODO var bad
      inv.Resize(30,20)
      inv
    }),
    deployables: DeployableToolbox = new DeployableToolbox(), // TODO var bad
    lookingForSquad: Boolean = false,
    var vehicle: Option[PlanetSideGUID] = None, // TODO var bad
    firstTimeEvents: Set[String] =
      FirstTimeEvents.Maps ++ FirstTimeEvents.Monoliths ++
        FirstTimeEvents.Standard.All ++ FirstTimeEvents.Cavern.All ++
        FirstTimeEvents.TR.All ++ FirstTimeEvents.NC.All ++ FirstTimeEvents.VS.All ++
        FirstTimeEvents.Generic,
    /** Timestamps of when a vehicle or equipment was last purchased */
    purchaseTimes: Map[String, LocalDateTime] = Map(),
    /** Timestamps of when a vehicle or equipment was last purchased */
    useTimes: Map[String, LocalDateTime] = Map()
) {
  assert(bep >= 0)
  assert(cep >= 0)

  val br: BattleRank  = BattleRank.withExperience(bep)
  val cr: CommandRank = CommandRank.withExperience(cep)

  private def cooldown(
      times: Map[String, LocalDateTime],
      cooldowns: Map[BasicDefinition, FiniteDuration],
      definition: BasicDefinition
  ): Option[Duration] = {
    times.get(definition.Name) match {
      case Some(purchaseTime) =>
        val secondsSincePurchase = Seconds.secondsBetween(purchaseTime, LocalDateTime.now())
        cooldowns.get(definition) match {
          case Some(cooldown) if (cooldown.toSeconds - secondsSincePurchase.getSeconds) > 0 =>
            Some(Seconds.seconds((cooldown.toSeconds - secondsSincePurchase.getSeconds).toInt).toStandardDuration)
          case _ => None
        }
      case None =>
        None
    }
  }

  /** Returns the remaining purchase cooldown or None if an object is not on cooldown */
  def purchaseCooldown(definition: BasicDefinition): Option[Duration] = {
    cooldown(purchaseTimes, Avatar.purchaseCooldowns, definition)
  }

  /** Returns the remaining use cooldown or None if an object is not on cooldown */
  def useCooldown(definition: BasicDefinition): Option[Duration] = {
    cooldown(useTimes, Avatar.useCooldowns, definition)
  }

  def fifthSlot(): EquipmentSlot = {
    new OffhandEquipmentSlot(EquipmentSize.Inventory) {
      val obj = new LockerEquipment(locker)
      Equipment = obj
    }
  }

  val definition: AvatarDefinition = GlobalDefinitions.avatar

  /** Returns numerical value from 0-3 that is the hacking skill level representation in packets */
  def hackingSkillLevel(): Int = {
    if (
      certifications.contains(Certification.ExpertHacking) || certifications.contains(Certification.ElectronicsExpert)
    ) {
      3
    } else if (certifications.contains(Certification.AdvancedHacking)) {
      2
    } else if (certifications.contains(Certification.Hacking)) {
      1
    } else {
      0
    }
  }

  /** The maximum stamina amount */
  val maxStamina: Int = 100

  /** Return true if the stamina is at the maximum amount */
  def staminaFull: Boolean = {
    stamina == maxStamina
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Avatar]

  override def equals(other: Any): Boolean =
    other match {
      case that: Avatar =>
        (that canEqual this) &&
          id == that.id
      case _ =>
        false
    }

  /** Avatar assertions
    * These protect against programming errors by asserting avatar properties have correct values
    * They may or may not be disabled for live applications
    */
  assert(stamina <= maxStamina && stamina >= 0)
  assert(head >= 0) // TODO what's the max value?
  assert(implants.length <= 3)
  assert(implants.flatten.map(_.definition.implantType).distinct.length == implants.flatten.length)
  assert(br.implantSlots >= implants.flatten.length)
}
