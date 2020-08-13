// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.avatar.{
  Avatar,
  LoadoutManager
}
import net.psforever.objects.definition.{
  AvatarDefinition,
  ExoSuitDefinition,
  SpecialExoSuitDefinition
}
import net.psforever.objects.equipment.{Equipment, EquipmentSize, EquipmentSlot, JammableUnit}
import net.psforever.objects.inventory.{Container, GridInventory, InventoryItem}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.aura.AuraContainer
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.objects.vital.{DamageResistanceModel, Vitality}
import net.psforever.objects.zones.ZoneAware
import net.psforever.types.{PlanetSideGUID, _}

import scala.annotation.tailrec
import scala.util.{Success, Try}

class Player(var avatar: Avatar)
    extends PlanetSideServerObject
    with FactionAffinity
    with Vitality
    with ResistanceProfile
    with Container
    with JammableUnit
    with ZoneAware
    with AuraContainer {
  Health = 0       //player health is artificially managed as a part of their lifecycle; start entity as dead
  Destroyed = true //see isAlive
  private var backpack: Boolean = false
  private var armor: Int        = 0

  private var capacitor: Float                         = 0f
  private var capacitorState: CapacitorStateType.Value = CapacitorStateType.Idle
  private var capacitorLastUsedMillis: Long            = 0
  private var capacitorLastChargedMillis: Long         = 0

  private var exosuit: ExoSuitDefinition             = GlobalDefinitions.Standard
  private val freeHand: EquipmentSlot                = new OffhandEquipmentSlot(EquipmentSize.Inventory)
  private val holsters: Array[EquipmentSlot]         = Array.fill[EquipmentSlot](5)(new EquipmentSlot)
  private val inventory: GridInventory               = GridInventory()
  private var drawnSlot: Int                         = Player.HandsDownSlot
  private var lastDrawnSlot: Int                     = Player.HandsDownSlot
  private var backpackAccess: Option[PlanetSideGUID] = None

  private var facingYawUpper: Float = 0f
  private var crouching: Boolean    = false
  private var jumping: Boolean      = false
  private var cloaked: Boolean      = false
  private var afk: Boolean          = false

  private var vehicleSeated: Option[PlanetSideGUID] = None

  Continent = "home2" //the zone id

  var spectator: Boolean                 = false
  var silenced: Boolean                  = false
  var death_by: Int                      = 0
  var lastSeenStreamMessage: Array[Long] = Array.fill[Long](65535)(0L)
  var lastShotSeq_time: Int              = -1

  /** From PlanetsideAttributeMessage */
  var PlanetsideAttribute: Array[Long] = Array.ofDim(120)

  val squadLoadouts = new LoadoutManager(10)

  Player.SuitSetup(this, exosuit)

  def Definition: AvatarDefinition = avatar.definition

  def CharId: Long = avatar.id

  def Name: String = avatar.name

  def Faction: PlanetSideEmpire.Value = avatar.faction

  def Sex: CharacterGender.Value = avatar.sex

  def Head: Int = avatar.head

  def Voice: CharacterVoice.Value = avatar.voice

  def isAlive: Boolean = !Destroyed

  def isBackpack: Boolean = backpack

  def Spawn(): Boolean = {
    if (!isAlive && !isBackpack) {
      Destroyed = false
      Health = Definition.DefaultHealth
      Armor = MaxArmor
      Capacitor = 0
    }
    isAlive
  }

  def Die: Boolean = {
    Destroyed = true
    Health = 0
    false
  }

  def Revive: Boolean = {
    Destroyed = false
    Health = Definition.DefaultHealth
    true
  }

  def Release: Boolean = {
    if (!isAlive) {
      backpack = true
      true
    } else {
      false
    }
  }

  def Armor: Int = armor

  def Armor_=(assignArmor: Int): Int = {
    armor = math.min(math.max(0, assignArmor), MaxArmor)
    Armor
  }

  def MaxArmor: Int = exosuit.MaxArmor

  def Capacitor: Float = capacitor

  def Capacitor_=(value: Float): Float = {
    val newValue = math.min(math.max(0, value), ExoSuitDef.MaxCapacitor.toFloat)

    if (newValue < capacitor) {
      capacitorLastUsedMillis = System.currentTimeMillis()
      capacitorLastChargedMillis = 0
    } else if (newValue > capacitor && newValue < ExoSuitDef.MaxCapacitor) {
      capacitorLastChargedMillis = System.currentTimeMillis()
      capacitorLastUsedMillis = 0
    } else if (newValue > capacitor && newValue == ExoSuitDef.MaxCapacitor) {
      capacitorLastChargedMillis = 0
      capacitorLastUsedMillis = 0
      capacitorState = CapacitorStateType.Idle
    }

    capacitor = newValue
    capacitor
  }

  def CapacitorState: CapacitorStateType.Value = capacitorState
  def CapacitorState_=(value: CapacitorStateType.Value): CapacitorStateType.Value = {
    value match {
      case CapacitorStateType.Charging    => capacitorLastChargedMillis = System.currentTimeMillis()
      case CapacitorStateType.Discharging => capacitorLastUsedMillis = System.currentTimeMillis()
      case _                              => ;
    }

    capacitorState = value
    capacitorState
  }

  def CapacitorLastUsedMillis    = capacitorLastUsedMillis
  def CapacitorLastChargedMillis = capacitorLastChargedMillis

  def VisibleSlots: Set[Int] =
    if (exosuit.SuitType == ExoSuitType.MAX) {
      Set(0)
    } else {
      (0 to 4).filterNot(index => holsters(index).Size == EquipmentSize.Blocked).toSet
    }

  override def Slot(slot: Int): EquipmentSlot = {
    if (inventory.Offset <= slot && slot <= inventory.LastIndex) {
      inventory.Slot(slot)
    } else if (slot > -1 && slot < 5) {
      holsters(slot)
    } else if (slot == 5) {
      avatar.fifthSlot()
    } else if (slot == Player.FreeHandSlot) {
      freeHand
    } else {
      OffhandEquipmentSlot.BlockedSlot
    }
  }

  def Holsters(): Array[EquipmentSlot] = holsters

  def Inventory: GridInventory = inventory

  override def Fit(obj: Equipment): Option[Int] = {
    recursiveHolsterFit(holsters.iterator, obj.Size) match {
      case Some(index) =>
        Some(index)
      case None =>
        inventory.Fit(obj.Definition.Tile) match {
          case Some(index) =>
            Some(index)
          case None =>
            if (freeHand.Equipment.isDefined) { None }
            else { Some(Player.FreeHandSlot) }
        }
    }
  }

  @tailrec private def recursiveHolsterFit(
      iter: Iterator[EquipmentSlot],
      objSize: EquipmentSize.Value,
      index: Int = 0
  ): Option[Int] = {
    if (!iter.hasNext) {
      None
    } else {
      val slot = iter.next()
      if (slot.Equipment.isEmpty && slot.Size.equals(objSize)) {
        Some(index)
      } else {
        recursiveHolsterFit(iter, objSize, index + 1)
      }
    }
  }

  def FreeHand = freeHand

  def FreeHand_=(item: Option[Equipment]): Option[Equipment] = {
    if (freeHand.Equipment.isEmpty || item.isEmpty) {
      freeHand.Equipment = item
    }
    FreeHand.Equipment
  }

  override def Find(guid: PlanetSideGUID): Option[Int] = {
    findInHolsters(holsters.iterator, guid)
      .orElse(inventory.Find(guid)) match {
      case Some(index) =>
        Some(index)
      case None =>
        if (freeHand.Equipment.isDefined && freeHand.Equipment.get.GUID == guid) {
          Some(Player.FreeHandSlot)
        } else {
          None
        }
    }
  }

  @tailrec private def findInHolsters(
      iter: Iterator[EquipmentSlot],
      guid: PlanetSideGUID,
      index: Int = 0
  ): Option[Int] = {
    if (!iter.hasNext) {
      None
    } else {
      val slot = iter.next()
      if (slot.Equipment.isDefined && slot.Equipment.get.GUID == guid) {
        Some(index)
      } else {
        findInHolsters(iter, guid, index + 1)
      }
    }
  }

  override def Collisions(dest: Int, width: Int, height: Int): Try[List[InventoryItem]] = {
    if (-1 < dest && dest < 5) {
      holsters(dest).Equipment match {
        case Some(item) =>
          Success(List(InventoryItem(item, dest)))
        case None =>
          Success(List())
      }
    } else if (dest == Player.FreeHandSlot) {
      freeHand.Equipment match {
        case Some(item) =>
          Success(List(InventoryItem(item, dest)))
        case None =>
          Success(List())
      }
    } else {
      super.Collisions(dest, width, height)
    }
  }

  def DrawnSlot: Int = drawnSlot

  def DrawnSlot_=(slot: Int): Int = {
    if (slot != drawnSlot) {
      if (slot == Player.HandsDownSlot) {
        drawnSlot = slot
      } else if (VisibleSlots.contains(slot) && holsters(slot).Equipment.isDefined) {
        drawnSlot = slot
        lastDrawnSlot = slot
      }
    }
    DrawnSlot
  }

  def LastDrawnSlot: Int = lastDrawnSlot

  def ExoSuit: ExoSuitType.Value    = exosuit.SuitType
  def ExoSuitDef: ExoSuitDefinition = exosuit

  def ExoSuit_=(suit: ExoSuitType.Value): Unit = {
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

  def FacingYawUpper: Float = facingYawUpper

  def FacingYawUpper_=(facing: Float): Float = {
    facingYawUpper = facing
    FacingYawUpper
  }

  def Crouching: Boolean = crouching

  def Crouching_=(crouched: Boolean): Boolean = {
    crouching = crouched
    Crouching
  }

  def Jumping: Boolean = jumping

  def Jumping_=(jumped: Boolean): Boolean = {
    jumping = jumped
    Jumping
  }

  def Cloaked: Boolean = cloaked

  def Cloaked_=(isCloaked: Boolean): Boolean = {
    cloaked = isCloaked
    Cloaked
  }

  def AwayFromKeyboard: Boolean = afk

  def AwayFromKeyboard_=(away: Boolean): Boolean = {
    afk = away
    AwayFromKeyboard
  }

  private var usingSpecial: SpecialExoSuitDefinition.Mode.Value => SpecialExoSuitDefinition.Mode.Value =
    DefaultUsingSpecial

  private var gettingSpecial: () => SpecialExoSuitDefinition.Mode.Value = DefaultGettingSpecial

  private def ChangeSpecialAbility(): Unit = {
    if (ExoSuit == ExoSuitType.MAX) {
      gettingSpecial = MAXGettingSpecial
      usingSpecial = Faction match {
        case PlanetSideEmpire.TR => UsingAnchorsOrOverdrive
        case PlanetSideEmpire.NC => UsingShield
        case _                   => DefaultUsingSpecial
      }
    } else {
      usingSpecial = DefaultUsingSpecial
      gettingSpecial = DefaultGettingSpecial
    }
  }

  def UsingSpecial: SpecialExoSuitDefinition.Mode.Value = { gettingSpecial() }

  def UsingSpecial_=(state: SpecialExoSuitDefinition.Mode.Value): SpecialExoSuitDefinition.Mode.Value =
    usingSpecial(state)

  private def DefaultUsingSpecial(state: SpecialExoSuitDefinition.Mode.Value): SpecialExoSuitDefinition.Mode.Value =
    SpecialExoSuitDefinition.Mode.Normal

  private def UsingAnchorsOrOverdrive(
      state: SpecialExoSuitDefinition.Mode.Value
  ): SpecialExoSuitDefinition.Mode.Value = {
    import SpecialExoSuitDefinition.Mode._
    val curr = UsingSpecial
    val next = if (curr == Normal) {
      if (state == Anchored || state == Overdrive) {
        state
      } else {
        Normal
      }
    } else if (state == Normal) {
      Normal
    } else {
      curr
    }
    MAXUsingSpecial(next)
  }

  private def UsingShield(state: SpecialExoSuitDefinition.Mode.Value): SpecialExoSuitDefinition.Mode.Value = {
    import SpecialExoSuitDefinition.Mode._
    val curr = UsingSpecial
    val next = if (curr == Normal) {
      if (state == Shielded) {
        state
      } else {
        Normal
      }
    } else if (state == Normal) {
      Normal
    } else {
      curr
    }
    MAXUsingSpecial(next)
  }

  private def DefaultGettingSpecial(): SpecialExoSuitDefinition.Mode.Value = SpecialExoSuitDefinition.Mode.Normal

  private def MAXUsingSpecial(state: SpecialExoSuitDefinition.Mode.Value): SpecialExoSuitDefinition.Mode.Value =
    exosuit match {
      case obj: SpecialExoSuitDefinition =>
        obj.UsingSpecial = state
      case _ =>
        SpecialExoSuitDefinition.Mode.Normal
    }

  private def MAXGettingSpecial(): SpecialExoSuitDefinition.Mode.Value =
    exosuit match {
      case obj: SpecialExoSuitDefinition =>
        obj.UsingSpecial
      case _ =>
        SpecialExoSuitDefinition.Mode.Normal
    }

  def isAnchored: Boolean =
    ExoSuit == ExoSuitType.MAX && Faction == PlanetSideEmpire.TR && UsingSpecial == SpecialExoSuitDefinition.Mode.Anchored

  def isOverdrived: Boolean =
    ExoSuit == ExoSuitType.MAX && Faction == PlanetSideEmpire.TR && UsingSpecial == SpecialExoSuitDefinition.Mode.Overdrive

  def isShielded: Boolean =
    ExoSuit == ExoSuitType.MAX && Faction == PlanetSideEmpire.NC && UsingSpecial == SpecialExoSuitDefinition.Mode.Shielded

  def AccessingBackpack: Option[PlanetSideGUID] = backpackAccess

  def AccessingBackpack_=(guid: PlanetSideGUID): Option[PlanetSideGUID] = {
    AccessingBackpack = Some(guid)
  }

  /**
    * Change which player has access to the backpack of this player.
    * A player may only access to the backpack of a dead released player, and only if no one else has access at the moment.
    * @param guid the player who wishes to access the backpack
    * @return the player who is currently allowed to access the backpack
    */
  def AccessingBackpack_=(guid: Option[PlanetSideGUID]): Option[PlanetSideGUID] = {
    guid match {
      case None =>
        backpackAccess = None
      case Some(player) =>
        if (isBackpack && backpackAccess.isEmpty) {
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
  def CanAccessBackpack(player: Player): Boolean = {
    isBackpack && (backpackAccess.isEmpty || backpackAccess.contains(player.GUID))
  }

  def VehicleSeated: Option[PlanetSideGUID] = vehicleSeated

  def VehicleSeated_=(guid: PlanetSideGUID): Option[PlanetSideGUID] = VehicleSeated_=(Some(guid))

  def VehicleSeated_=(guid: Option[PlanetSideGUID]): Option[PlanetSideGUID] = {
    vehicleSeated = guid
    VehicleSeated
  }

  def DamageModel = exosuit.asInstanceOf[DamageResistanceModel]

  def canEqual(other: Any): Boolean = other.isInstanceOf[Player]

  override def equals(other: Any): Boolean =
    other match {
      case that: Player =>
        (that canEqual this) &&
          avatar == that.avatar
      case _ =>
        false
    }

  override def hashCode(): Int = {
    avatar.hashCode()
  }

  override def toString: String = {
    val guid = if (HasGUID) {
      s" ${Continent}-${GUID.guid}"
    } else {
      ""
    }
    s"${avatar.name}$guid ${Health}/${MaxHealth} ${Armor}/${MaxArmor}"
  }
}

object Player {
  final val LockerSlot: Int    = 5
  final val FreeHandSlot: Int  = 250
  final val HandsDownSlot: Int = 255

  final case class Die()

  def apply(core: Avatar): Player = {
    new Player(core)
  }

  private def SuitSetup(player: Player, eSuit: ExoSuitDefinition): Unit = {
    //inventory
    player.Inventory.Clear()
    player.Inventory.Resize(eSuit.InventoryScale.Width, eSuit.InventoryScale.Height)
    player.Inventory.Offset = eSuit.InventoryOffset
    //holsters
    (0 until 5).foreach(index => { player.Slot(index).Size = eSuit.Holster(index) })
  }

  def Respawn(player: Player): Player = {
    if (player.Release) {
      val obj = new Player(player.avatar)
      obj.Continent = player.Continent
      obj
    } else {
      player
    }
  }
}
