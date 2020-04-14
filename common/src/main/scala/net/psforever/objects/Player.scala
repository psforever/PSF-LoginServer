// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.ActorRef
import net.psforever.objects.avatar.LoadoutManager
import net.psforever.objects.definition.{AvatarDefinition, ExoSuitDefinition, SpecialExoSuitDefinition}
import net.psforever.objects.equipment.{Equipment, EquipmentSize, EquipmentSlot, JammableUnit}
import net.psforever.objects.inventory.{Container, GridInventory, InventoryItem}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.objects.vital.{DamageResistanceModel, Vitality}
import net.psforever.objects.zones.ZoneAware
import net.psforever.packet.game.objectcreate.{Cosmetics, DetailedCharacterData, PersonalStyle}
import net.psforever.types.{PlanetSideGUID, _}

import scala.annotation.tailrec
import scala.util.{Success, Try}

class Player(private val core : Avatar) extends PlanetSideServerObject
  with FactionAffinity
  with Vitality
  with ResistanceProfile
  with Container
  with JammableUnit
  with ZoneAware {
  Health = 0 //player health is artificially managed as a part of their lifecycle; start entity as dead
  Destroyed = true //see isAlive
  private var backpack : Boolean = false
  private var stamina : Int = 0
  private var armor : Int = 0

  private var capacitor : Float = 0f
  private var capacitorState : CapacitorStateType.Value = CapacitorStateType.Idle
  private var capacitorLastUsedMillis : Long = 0
  private var capacitorLastChargedMillis : Long = 0

  private var maxStamina : Int = 100 //does anything affect this?

  private var exosuit : ExoSuitDefinition = GlobalDefinitions.Standard
  private val freeHand : EquipmentSlot = new OffhandEquipmentSlot(EquipmentSize.Inventory)
  private val holsters : Array[EquipmentSlot] = Array.fill[EquipmentSlot](5)(new EquipmentSlot)
  private val inventory : GridInventory = GridInventory()
  private var drawnSlot : Int = Player.HandsDownSlot
  private var lastDrawnSlot : Int = Player.HandsDownSlot
  private var backpackAccess : Option[PlanetSideGUID] = None

  private var facingYawUpper : Float = 0f
  private var crouching : Boolean  = false
  private var jumping : Boolean = false
  private var cloaked : Boolean = false
  private var fatigued : Boolean = false // If stamina drops to 0, player is fatigued until regenerating at least 20 stamina

  private var vehicleSeated : Option[PlanetSideGUID] = None

  Continent = "home2" //the zone id

  var silenced : Boolean = false
  var firstLoad : Boolean = false
  def FirstLoad : Boolean = firstLoad
  def FirstLoad_=(status : Boolean) : Boolean = {
    firstLoad = status
    FirstLoad
  }
  var death_by : Int = 0
  var lastSeenStreamMessage : Array[Long] = Array.fill[Long](65535)(0L)
  var lastShotSeq_time : Int = -1
  /** From PlanetsideAttributeMessage */
  var PlanetsideAttribute : Array[Long] = Array.ofDim(120)
  var skipStaminaRegenForTurns : Int = 0

  Player.SuitSetup(this, exosuit)

  def CharId : Long = core.CharId

  def Name : String = core.name

  def Faction : PlanetSideEmpire.Value = core.faction

  def Sex : CharacterGender.Value = core.sex

  def Head : Int = core.head

  def Voice : CharacterVoice.Value = core.voice

  def LFS : Boolean = core.LFS

  def isAlive : Boolean = !Destroyed

  def isBackpack : Boolean = backpack

  def Spawn : Boolean = {
    if(!isAlive && !isBackpack) {
      Destroyed = false
      Health = Definition.DefaultHealth
      Stamina = MaxStamina
      Armor = MaxArmor
      Capacitor = 0
      ResetAllImplants()
    }
    isAlive
  }

  def Die : Boolean = {
    Destroyed = true
    Health = 0
    Stamina = 0
    false
  }

  def Revive : Boolean = {
    Destroyed = false
    Health = Definition.DefaultHealth
    true
  }

  def Release : Boolean = {
    if(!isAlive) {
      backpack = true
      true
    }
    else {
      false
    }
  }

  def Stamina : Int = stamina

  def Stamina_=(assignStamina : Int) : Int = {
    stamina = if(isAlive) { math.min(math.max(0, assignStamina), MaxStamina) } else { 0 }

    if(Actor != ActorRef.noSender) {
      Actor ! Player.StaminaChanged(Stamina)
    }

    Stamina
  }

  def MaxStamina : Int = maxStamina

  def MaxStamina_=(max : Int) : Int = {
    maxStamina = math.min(math.max(0, max), 65535)
    MaxStamina
  }

  def Armor : Int = armor

  def Armor_=(assignArmor : Int) : Int = {
    armor = math.min(math.max(0, assignArmor), MaxArmor)
    Armor
  }

  def MaxArmor : Int = exosuit.MaxArmor

  def Capacitor : Float = capacitor

  def Capacitor_=(value : Float) : Float = {
    val newValue = math.min(math.max(0, value), ExoSuitDef.MaxCapacitor)

    if(newValue < capacitor) {
      capacitorLastUsedMillis = System.currentTimeMillis()
      capacitorLastChargedMillis = 0
    }
    else if(newValue > capacitor && newValue < ExoSuitDef.MaxCapacitor) {
      capacitorLastChargedMillis = System.currentTimeMillis()
      capacitorLastUsedMillis = 0
    }
    else if(newValue > capacitor && newValue == ExoSuitDef.MaxCapacitor) {
      capacitorLastChargedMillis = 0
      capacitorLastUsedMillis = 0
      capacitorState = CapacitorStateType.Idle
    }

    capacitor = newValue
    capacitor
  }

  def CapacitorState : CapacitorStateType.Value = capacitorState
  def CapacitorState_=(value : CapacitorStateType.Value) : CapacitorStateType.Value = {
    value match {
      case CapacitorStateType.Charging => capacitorLastChargedMillis = System.currentTimeMillis()
      case CapacitorStateType.Discharging => capacitorLastUsedMillis = System.currentTimeMillis()
      case _ => ;
    }

    capacitorState = value
    capacitorState
  }

  def CapacitorLastUsedMillis = capacitorLastUsedMillis
  def CapacitorLastChargedMillis = capacitorLastChargedMillis

  def VisibleSlots : Set[Int] = if(exosuit.SuitType == ExoSuitType.MAX) {
    Set(0)
  }
  else {
    (0 to 4).filterNot(index => holsters(index).Size == EquipmentSize.Blocked).toSet
  }

  override def Slot(slot : Int) : EquipmentSlot = {
    if(inventory.Offset <= slot && slot <= inventory.LastIndex) {
      inventory.Slot(slot)
    }
    else if(slot > -1 && slot < 5) {
      holsters(slot)
    }
    else if(slot == 5) {
      core.FifthSlot
    }
    else if(slot == Player.FreeHandSlot) {
      freeHand
    }
    else {
      OffhandEquipmentSlot.BlockedSlot
    }
  }

  def Holsters() : Array[EquipmentSlot] = holsters

  def Inventory : GridInventory = inventory

  def Locker : LockerContainer = core.Locker

  override def Fit(obj : Equipment) : Option[Int] = {
    recursiveHolsterFit(holsters.iterator, obj.Size) match {
      case Some(index) =>
        Some(index)
      case None =>
        inventory.Fit(obj.Definition.Tile) match {
          case Some(index) =>
            Some(index)
          case None =>
            if(freeHand.Equipment.isDefined) { None } else { Some(Player.FreeHandSlot) }
        }
    }
  }

  @tailrec private def recursiveHolsterFit(iter : Iterator[EquipmentSlot], objSize : EquipmentSize.Value, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val slot = iter.next
      if(slot.Equipment.isEmpty && slot.Size.equals(objSize)) {
        Some(index)
      }
      else {
        recursiveHolsterFit(iter, objSize, index + 1)
      }
    }
  }

  def FreeHand = freeHand

  def FreeHand_=(item : Option[Equipment]) : Option[Equipment] = {
    if(freeHand.Equipment.isEmpty || item.isEmpty) {
      freeHand.Equipment = item
    }
    FreeHand.Equipment
  }

  override def Find(guid : PlanetSideGUID) : Option[Int] = {
    findInHolsters(holsters.iterator, guid)
      .orElse(inventory.Find(guid)) match {
      case Some(index) =>
        Some(index)
      case None =>
        if(freeHand.Equipment.isDefined && freeHand.Equipment.get.GUID == guid) {
          Some(Player.FreeHandSlot)
        }
        else {
          None
        }
    }
  }

  @tailrec private def findInHolsters(iter : Iterator[EquipmentSlot], guid : PlanetSideGUID, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val slot = iter.next
      if(slot.Equipment.isDefined && slot.Equipment.get.GUID == guid) {
        Some(index)
      }
      else {
        findInHolsters(iter, guid, index + 1)
      }
    }
  }

  override def Collisions(dest : Int, width : Int, height : Int) : Try[List[InventoryItem]] = {
    if(-1 < dest && dest < 5) {
      holsters(dest).Equipment match {
        case Some(item) =>
          Success(List(InventoryItem(item, dest)))
        case None =>
          Success(List())
      }
    }
    else if(dest == Player.FreeHandSlot) {
      freeHand.Equipment match {
        case Some(item) =>
          Success(List(InventoryItem(item, dest)))
        case None =>
          Success(List())
      }
    }
    else {
      super.Collisions(dest, width, height)
    }
  }

  def DrawnSlot : Int = drawnSlot

  def DrawnSlot_=(slot : Int) : Int = {
    if(slot != drawnSlot) {
      if(slot == Player.HandsDownSlot) {
        drawnSlot = slot
      }
      else if(VisibleSlots.contains(slot) && holsters(slot).Equipment.isDefined) {
        drawnSlot = slot
        lastDrawnSlot = slot
      }
    }
    DrawnSlot
  }

  def LastDrawnSlot : Int = lastDrawnSlot

  def ExoSuit : ExoSuitType.Value = exosuit.SuitType
  def ExoSuitDef : ExoSuitDefinition = exosuit

  def ExoSuit_=(suit : ExoSuitType.Value) : Unit = {
    val eSuit = ExoSuitDefinition.Select(suit, Faction)
    exosuit = eSuit
    Player.SuitSetup(this, eSuit)
    ChangeSpecialAbility()
  }

  def Subtract = exosuit.Subtract

  def ResistanceDirectHit = exosuit.ResistanceDirectHit

  def ResistanceSplash = exosuit.ResistanceSplash

  def ResistanceAggravated = exosuit.ResistanceAggravated

  def RadiationShielding = exosuit.RadiationShielding

  def EquipmentLoadouts : LoadoutManager = core.EquipmentLoadouts

  def SquadLoadouts : LoadoutManager = core.SquadLoadouts

  def BEP : Long = core.BEP

  def CEP : Long = core.CEP

  def Certifications : Set[CertificationType.Value] = core.Certifications.toSet

  /**
    * What kind of implant is installed into the given slot number?
    * @see `ImplantType`
    * @param slot the slot number
    * @return the tye of implant
    */
  def Implant(slot : Int) : ImplantType.Value = core.Implant(slot)

  def ImplantSlot(slot: Int) : ImplantSlot = core.Implants(slot)

  /**
    * A read-only `Array` of tuples representing important information about all unlocked implant slots.
    * @return a maximum of three implant types, initialization times, and active flags
    */
  def Implants : Array[(ImplantType.Value, Long, Boolean)] = {
    core.Implants.takeWhile(_.Unlocked).map( implant => { (implant.Implant, implant.MaxTimer, implant.Active) })
  }

  def ResetAllImplants() : Unit = core.ResetAllImplants()

  def FacingYawUpper : Float = facingYawUpper

  def FacingYawUpper_=(facing : Float) : Float = {
    facingYawUpper = facing
    FacingYawUpper
  }

  def Crouching : Boolean = crouching

  def Crouching_=(crouched : Boolean) : Boolean = {
    crouching = crouched
    Crouching
  }

  def Jumping : Boolean = jumping

  def Jumping_=(jumped : Boolean) : Boolean = {
    jumping = jumped
    Jumping
  }

  def Cloaked : Boolean = cloaked

  def Cloaked_=(isCloaked : Boolean) : Boolean = {
    cloaked = isCloaked
    Cloaked
  }

  def Fatigued : Boolean = fatigued
  def Fatigued_=(isFatigued : Boolean) : Boolean = {
    fatigued = isFatigued
    Fatigued
  }

  def PersonalStyleFeatures : Option[Cosmetics] = core.PersonalStyleFeatures

  def AddToPersonalStyle(value : PersonalStyle.Value) : (Option[Cosmetics], Option[Cosmetics]) = {
    val original = core.PersonalStyleFeatures
    if(DetailedCharacterData.isBR24(core.BEP)) {
      core.PersonalStyleFeatures = original match {
        case Some(cosmetic) =>
          cosmetic + value
        case None =>
          Cosmetics(value)
      }
      (original, core.PersonalStyleFeatures)
    }
    else {
      (None, None)
    }
  }

  def RemoveFromPersonalStyle(value : PersonalStyle.Value) : (Option[Cosmetics], Option[Cosmetics]) = {
    val original = core.PersonalStyleFeatures
      original match {
        case Some(cosmetics) =>
          (original, core.PersonalStyleFeatures = cosmetics - value)
        case None =>
          (None, None)
      }
  }

  private def BasicFeatureToggle(feature : PersonalStyle.Value) : (Option[Cosmetics], Option[Cosmetics]) = core.PersonalStyleFeatures match {
    case Some(c : Cosmetics) =>
      if(c.Styles.contains(feature)) {
        RemoveFromPersonalStyle(feature)
      }
      else {
        AddToPersonalStyle(feature)
      }
    case None =>
      AddToPersonalStyle(feature)
  }

  def ToggleHelmet : (Option[Cosmetics], Option[Cosmetics]) = BasicFeatureToggle(PersonalStyle.NoHelmet)

  def ToggleShades : (Option[Cosmetics], Option[Cosmetics]) = BasicFeatureToggle(PersonalStyle.Sunglasses)

  def ToggleEarpiece : (Option[Cosmetics], Option[Cosmetics]) = BasicFeatureToggle(PersonalStyle.Earpiece)

  def ToggleHat : (Option[Cosmetics], Option[Cosmetics]) = {
    core.PersonalStyleFeatures match {
      case Some(c : Cosmetics) =>
        if(c.Styles.contains(PersonalStyle.BrimmedCap)) {
          (RemoveFromPersonalStyle(PersonalStyle.BrimmedCap)._1,
            AddToPersonalStyle(PersonalStyle.Beret)._2)
        }
        else if(c.Styles.contains(PersonalStyle.Beret)) {
          RemoveFromPersonalStyle(PersonalStyle.Beret)
        }
        else {
          AddToPersonalStyle(PersonalStyle.BrimmedCap)
        }
      case None =>
        AddToPersonalStyle(PersonalStyle.BrimmedCap)
    }
  }

  private var usingSpecial : SpecialExoSuitDefinition.Mode.Value=>SpecialExoSuitDefinition.Mode.Value = DefaultUsingSpecial

  private var gettingSpecial : ()=>SpecialExoSuitDefinition.Mode.Value = DefaultGettingSpecial

  private def ChangeSpecialAbility() : Unit = {
    if(ExoSuit == ExoSuitType.MAX) {
      gettingSpecial = MAXGettingSpecial
      usingSpecial = Faction match {
        case PlanetSideEmpire.TR => UsingAnchorsOrOverdrive
        case PlanetSideEmpire.NC => UsingShield
        case _ => DefaultUsingSpecial
      }
    }
    else {
      usingSpecial = DefaultUsingSpecial
      gettingSpecial = DefaultGettingSpecial
    }
  }

  def UsingSpecial : SpecialExoSuitDefinition.Mode.Value = { gettingSpecial() }

  def UsingSpecial_=(state : SpecialExoSuitDefinition.Mode.Value) : SpecialExoSuitDefinition.Mode.Value = usingSpecial(state)

  private def DefaultUsingSpecial(state : SpecialExoSuitDefinition.Mode.Value) : SpecialExoSuitDefinition.Mode.Value = SpecialExoSuitDefinition.Mode.Normal

  private def UsingAnchorsOrOverdrive(state : SpecialExoSuitDefinition.Mode.Value) : SpecialExoSuitDefinition.Mode.Value = {
    import SpecialExoSuitDefinition.Mode._
    val curr = UsingSpecial
    val next = if(curr == Normal) {
      if(state == Anchored || state == Overdrive) {
        state
      }
      else {
        Normal
      }
    }
    else if(state == Normal) {
      Normal
    }
    else {
      curr
    }
    MAXUsingSpecial(next)
  }

  private def UsingShield(state : SpecialExoSuitDefinition.Mode.Value) : SpecialExoSuitDefinition.Mode.Value = {
    import SpecialExoSuitDefinition.Mode._
    val curr = UsingSpecial
    val next = if(curr == Normal) {
      if(state == Shielded) {
        state
      }
      else {
        Normal
      }
    }
    else if(state == Normal) {
      Normal
    }
    else {
      curr
    }
    MAXUsingSpecial(next)
  }

  private def DefaultGettingSpecial() : SpecialExoSuitDefinition.Mode.Value = SpecialExoSuitDefinition.Mode.Normal

  private def MAXUsingSpecial(state : SpecialExoSuitDefinition.Mode.Value) : SpecialExoSuitDefinition.Mode.Value = exosuit match {
    case obj : SpecialExoSuitDefinition =>
      obj.UsingSpecial = state
    case _ =>
      SpecialExoSuitDefinition.Mode.Normal
  }

  private def MAXGettingSpecial() : SpecialExoSuitDefinition.Mode.Value = exosuit match {
    case obj : SpecialExoSuitDefinition =>
      obj.UsingSpecial
    case _ =>
      SpecialExoSuitDefinition.Mode.Normal
  }

  def isAnchored : Boolean = ExoSuit == ExoSuitType.MAX && Faction == PlanetSideEmpire.TR && UsingSpecial == SpecialExoSuitDefinition.Mode.Anchored

  def isOverdrived : Boolean = ExoSuit == ExoSuitType.MAX && Faction == PlanetSideEmpire.TR && UsingSpecial == SpecialExoSuitDefinition.Mode.Overdrive

  def isShielded : Boolean = ExoSuit == ExoSuitType.MAX && Faction == PlanetSideEmpire.NC && UsingSpecial == SpecialExoSuitDefinition.Mode.Shielded

  def AccessingBackpack : Option[PlanetSideGUID] = backpackAccess

  def AccessingBackpack_=(guid : PlanetSideGUID) : Option[PlanetSideGUID] = {
    AccessingBackpack = Some(guid)
  }

  /**
    * Change which player has access to the backpack of this player.
    * A player may only access to the backpack of a dead released player, and only if no one else has access at the moment.
    * @param guid the player who wishes to access the backpack
    * @return the player who is currently allowed to access the backpack
    */
  def AccessingBackpack_=(guid : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    guid match {
      case None =>
        backpackAccess = None
      case Some(player) =>
        if(isBackpack && backpackAccess.isEmpty) {
          backpackAccess = Some(player)
        }
    }
    AccessingBackpack
  }

  /**
    * Can the other `player` access the contents of this `Player`'s backpack?
    * @param player a player attempting to access this backpack
    * @return `true`, if the `player` is permitted access; `false`, otherwise
    */
  def CanAccessBackpack(player : Player) : Boolean = {
    isBackpack && (backpackAccess.isEmpty || backpackAccess.contains(player.GUID))
  }

  def VehicleSeated : Option[PlanetSideGUID] = vehicleSeated

  def VehicleSeated_=(guid : PlanetSideGUID) : Option[PlanetSideGUID] = VehicleSeated_=(Some(guid))

  def VehicleSeated_=(guid : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    vehicleSeated = guid
    VehicleSeated
  }

  def VehicleOwned : Option[PlanetSideGUID] = core.VehicleOwned

  def VehicleOwned_=(guid : PlanetSideGUID) : Option[PlanetSideGUID] = core.VehicleOwned_=(Some(guid))

  def VehicleOwned_=(guid : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = core.VehicleOwned_=(guid)

  def DamageModel = exosuit.asInstanceOf[DamageResistanceModel]

  def Definition : AvatarDefinition = core.Definition

  def canEqual(other: Any): Boolean = other.isInstanceOf[Player]

  override def equals(other : Any) : Boolean = other match {
    case that: Player =>
      (that canEqual this) &&
        core == that.core
    case _ =>
      false
  }

  override def hashCode() : Int = {
    core.hashCode()
  }

  override def toString : String = Player.toString(this)
}

object Player {
  final val LockerSlot : Int = 5
  final val FreeHandSlot : Int = 250
  final val HandsDownSlot : Int = 255

  final case class Die()
  final case class ImplantActivation(slot : Int, status : Int)
  final case class ImplantInitializationStart(slot : Int)
  final case class UninitializeImplant(slot : Int)
  final case class ImplantInitializationComplete(slot : Int)
  final case class DrainStamina(amount : Int)
  final case class StaminaChanged(currentStamina : Int)

  def apply(core : Avatar) : Player = {
    new Player(core)
  }

  private def SuitSetup(player : Player, eSuit : ExoSuitDefinition) : Unit = {
    //inventory
    player.Inventory.Clear()
    player.Inventory.Resize(eSuit.InventoryScale.Width, eSuit.InventoryScale.Height)
    player.Inventory.Offset = eSuit.InventoryOffset
    //holsters
    (0 until 5).foreach(index => { player.Slot(index).Size = eSuit.Holster(index) })
  }

  def Respawn(player : Player) : Player = {
    if(player.Release) {
      val obj = new Player(player.core)
      obj.Continent = player.Continent
      obj
    }
    else {
      player
    }
  }

  def GetHackLevel(player : Player): Int = {
    if(player.Certifications.contains(CertificationType.ExpertHacking) || player.Certifications.contains(CertificationType.ElectronicsExpert)) {
      3
    }
    else if(player.Certifications.contains(CertificationType.AdvancedHacking)) {
      2
    }
    else if (player.Certifications.contains(CertificationType.Hacking)) {
      1
    }
    else {
      0
    }
  }

  def toString(obj : Player) : String = {
    val guid = if(obj.HasGUID) { s" ${obj.Continent}-${obj.GUID.guid}" } else { "" }
    s"${obj.core}$guid ${obj.Health}/${obj.MaxHealth} ${obj.Armor}/${obj.MaxArmor}"
  }
}
