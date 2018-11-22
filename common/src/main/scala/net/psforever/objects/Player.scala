// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.AvatarDefinition
import net.psforever.objects.equipment.{Equipment, EquipmentSize}
import net.psforever.objects.inventory.{Container, GridInventory, InventoryItem}
import net.psforever.objects.loadouts.Loadout
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.objects.vital.{DamageResistanceModel, Vitality}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types._

import scala.annotation.tailrec
import scala.util.{Success, Try}

class Player(private val core : Avatar) extends PlanetSideGameObject
  with FactionAffinity
  with Vitality
  with ResistanceProfile
  with Container {
  private var alive : Boolean = false
  private var backpack : Boolean = false
  private var health : Int = 0
  private var stamina : Int = 0
  private var armor : Int = 0
  private var maxHealth : Int = 100 //TODO affected by empire benefits, territory benefits, and bops
  private var maxStamina : Int = 100 //does anything affect this?

  private var exosuit : ExoSuitDefinition = ExoSuitDefinition.Standard
  private val freeHand : EquipmentSlot = new OffhandEquipmentSlot(EquipmentSize.Inventory)
  private val holsters : Array[EquipmentSlot] = Array.fill[EquipmentSlot](5)(new EquipmentSlot)
  private val inventory : GridInventory = GridInventory()
  private var drawnSlot : Int = Player.HandsDownSlot
  private var lastDrawnSlot : Int = Player.HandsDownSlot

  private var facingYawUpper : Float = 0f
  private var crouching : Boolean  = false
  private var jumping : Boolean = false
  private var cloaked : Boolean = false
  private var backpackAccess : Option[PlanetSideGUID] = None

  private var vehicleSeated : Option[PlanetSideGUID] = None
  private var vehicleOwned : Option[PlanetSideGUID] = None

  private var continent : String = "home2" //the zone id

  //SouNourS things
  /** Last medkituse. */
  var lastMedkit : Long = 0
  var lastSeenStreamMessage : Array[Long] = Array.fill[Long](65535)(0L)
  var lastShotSeq_time : Int = -1
  /** From PlanetsideAttributeMessage */
  var PlanetsideAttribute : Array[Long] = Array.ofDim(120)

  Player.SuitSetup(this, exosuit)

  def Name : String = core.name

  def Faction : PlanetSideEmpire.Value = core.faction

  def Sex : CharacterGender.Value = core.sex

  def Head : Int = core.head

  def Voice : CharacterVoice.Value = core.voice

  def isAlive : Boolean = alive

  def isBackpack : Boolean = backpack

  def Spawn : Boolean = {
    if(!isAlive && !isBackpack) {
      alive = true
      Health = MaxHealth
      Stamina = MaxStamina
      Armor = MaxArmor
      ResetAllImplants()
    }
    isAlive
  }

  def Die : Boolean = {
    alive = false
    Health = 0
    Stamina = 0
    false
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

  def Health : Int = health

  def Health_=(assignHealth : Int) : Int = {
    health = if(isAlive) { math.min(math.max(0, assignHealth), MaxHealth) } else { 0 }
    Health
  }

  def MaxHealth : Int = maxHealth

  def MaxHealth_=(max : Int) : Int = {
    maxHealth = math.min(math.max(0, max), 65535)
    MaxHealth
  }

  def Stamina : Int = stamina

  def Stamina_=(assignEnergy : Int) : Int = {
    stamina = if(isAlive) { math.min(math.max(0, assignEnergy), MaxStamina) } else { 0 }
    Stamina
  }

  def MaxStamina : Int = maxStamina

  def MaxStamina_=(max : Int) : Int = {
    maxStamina = math.min(math.max(0, max), 65535)
    MaxStamina
  }

  def Armor : Int = armor

  def Armor_=(assignArmor : Int) : Int = {
    armor = if(isAlive) { math.min(math.max(0, assignArmor), MaxArmor) } else { 0 }
    Armor
  }

  def MaxArmor : Int = exosuit.MaxArmor

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

  def ExoSuit_=(suit : ExoSuitType.Value) : Unit = {
    val eSuit = ExoSuitDefinition.Select(suit)
    exosuit = eSuit
    Player.SuitSetup(this, eSuit)
    ChangeSpecialAbility()
  }

  def Subtract = exosuit.Subtract

  def ResistanceDirectHit = exosuit.ResistanceDirectHit

  def ResistanceSplash = exosuit.ResistanceSplash

  def ResistanceAggravated = exosuit.ResistanceAggravated

  def RadiationShielding = exosuit.RadiationShielding

  def LoadLoadout(line : Int) : Option[Loadout] = core.LoadLoadout(line)

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

  def Cloaked : Boolean = jumping

  def Cloaked_=(isCloaked : Boolean) : Boolean = {
    cloaked = isCloaked
    Cloaked
  }

  private var usingSpecial : (SpecialExoSuitDefinition.Mode.Value)=>SpecialExoSuitDefinition.Mode.Value = DefaultUsingSpecial

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

  def isAnchored : Boolean = ExoSuit == ExoSuitType.MAX && Faction == PlanetSideEmpire.NC && UsingSpecial == SpecialExoSuitDefinition.Mode.Anchored

  def isOverdrived : Boolean = ExoSuit == ExoSuitType.MAX && Faction == PlanetSideEmpire.NC && UsingSpecial == SpecialExoSuitDefinition.Mode.Overdrive

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

  def VehicleOwned : Option[PlanetSideGUID] = vehicleOwned

  def VehicleOwned_=(guid : PlanetSideGUID) : Option[PlanetSideGUID] = VehicleOwned_=(Some(guid))

  def VehicleOwned_=(guid : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    vehicleOwned = guid
    VehicleOwned
  }

  def Continent : String = continent

  def Continent_=(zoneId : String) : String = {
    continent = zoneId
    Continent
  }

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
      obj.VehicleOwned = player.VehicleOwned
      obj.Continent = player.Continent
      obj
    }
    else {
      player
    }
  }

  def toString(obj : Player) : String = {
    val guid = if(obj.HasGUID) { s" ${obj.Continent}-${obj.GUID.guid}" } else { "" }
    s"${obj.core}$guid ${obj.Health}/${obj.MaxHealth} ${obj.Armor}/${obj.MaxArmor}"
  }
}
