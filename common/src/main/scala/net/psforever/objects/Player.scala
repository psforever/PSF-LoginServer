// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.{AvatarDefinition, ImplantDefinition}
import net.psforever.objects.equipment.{Equipment, EquipmentSize}
import net.psforever.objects.inventory.{GridInventory, InventoryItem}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types._

import scala.annotation.tailrec
import scala.collection.mutable

class Player(private val name : String,
             private val faction : PlanetSideEmpire.Value,
             private val sex : CharacterGender.Value,
             private val head : Int,
             private val voice : Int
            ) extends PlanetSideGameObject {
  private var alive : Boolean = false
  private var backpack : Boolean = false
  private var health : Int = 0
  private var stamina : Int = 0
  private var armor : Int = 0
  private var maxHealth : Int = 100 //TODO affected by empire benefits, territory benefits, and bops
  private var maxStamina : Int = 100 //does anything affect this?

  private var exosuit : ExoSuitType.Value = ExoSuitType.Standard
  private val freeHand : EquipmentSlot = new OffhandEquipmentSlot(EquipmentSize.Any)
  private val holsters : Array[EquipmentSlot] = Array.fill[EquipmentSlot](5)(new EquipmentSlot)
  private val fifthSlot : EquipmentSlot = new OffhandEquipmentSlot(EquipmentSize.Inventory)
  private val inventory : GridInventory = GridInventory()
  private var drawnSlot : Int = Player.HandsDownSlot
  private var lastDrawnSlot : Int = 0

  private val loadouts : Array[Option[Loadout]] = Array.fill[Option[Loadout]](10)(None)

  private var bep : Long = 0
  private var cep : Long = 0
  private val certifications : mutable.Set[CertificationType.Value] = mutable.Set[CertificationType.Value]()
  /**
    * Unlike other objects, the maximum number of `ImplantSlots` are built into the `Player`.
    * Additionally, "implants" do not have tightly-coupled "`Definition` objects" that explain a formal implant object.
    * The `ImplantDefinition` objects themselves are moved around as if they were the implants.
    * The term internally used for this process is "installed" and "uninstalled."
    * @see `ImplantSlot`
    * @see `DetailedCharacterData.implants`
    * @see `AvatarConverter.MakeImplantEntries`
    */
  private val implants : Array[ImplantSlot] = Array.fill[ImplantSlot](3)(new ImplantSlot)

//  private var tosRibbon : MeritCommendation.Value = MeritCommendation.None
//  private var upperRibbon : MeritCommendation.Value = MeritCommendation.None
//  private var middleRibbon : MeritCommendation.Value = MeritCommendation.None
//  private var lowerRibbon : MeritCommendation.Value = MeritCommendation.None

  private var facingYawUpper : Float = 0f
  private var crouching : Boolean  = false
  private var jumping : Boolean = false
  private var cloaked : Boolean = false
  private var backpackAccess : Option[PlanetSideGUID] = None

  private var sessionId : Long = 0
  private var admin : Boolean = false
  private var spectator : Boolean = false

  private var vehicleSeated : Option[PlanetSideGUID] = None
  private var vehicleOwned : Option[PlanetSideGUID] = None

  private var continent : String = "home2" //actually, the zoneId
  private var playerDef : AvatarDefinition = Player.definition

  //SouNourS things
  /** Last medkituse. */
  var lastMedkit : Long = 0
  var death_by : Int = 0
  var lastSeenStreamMessage : Array[Long] = Array.fill[Long](65535)(0L)
  var lastShotSeq_time : Int = -1
  /** The player is shooting. */
  var shooting : Boolean = false
  /** From PlanetsideAttributeMessage */
  var PlanetsideAttribute : Array[Long] = Array.ofDim(120)

  Player.SuitSetup(this, ExoSuit)
  fifthSlot.Equipment = new LockerContainer() //the fifth slot is the player's "locker"

  def Name : String = name

  def Faction : PlanetSideEmpire.Value = faction

  def Sex : CharacterGender.Value = sex

  def Voice : Int = voice

  def Head : Int = head

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

  def MaxArmor : Int = ExoSuitDefinition.Select(exosuit).MaxArmor

  def Slot(slot : Int) : EquipmentSlot = {
    if(inventory.Offset <= slot && slot <= inventory.LastIndex) {
      inventory.Slot(slot)
    }
    else if(slot > -1 && slot < 5) {
      holsters(slot)
    }
    else if(slot == 5) {
      new OffhandEquipmentSlot(EquipmentSize.Inventory) {
        Equipment = fifthSlot.Equipment
      }
    }
    else if(slot == Player.FreeHandSlot) {
      freeHand
    }
    else {
      new OffhandEquipmentSlot(EquipmentSize.Blocked)
    }
  }

  def Holsters() : Array[EquipmentSlot] = holsters

  def Inventory : GridInventory = inventory

  def Fit(obj : Equipment) : Option[Int] = {
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

  def Equip(slot : Int, obj : Equipment) : Boolean = {
    if(-1 < slot && slot < 5) {
      holsters(slot).Equipment = obj
      true
    }
    else if(slot == Player.FreeHandSlot) {
      freeHand.Equipment = obj
      true
    }
    else {
      inventory += slot -> obj
    }
  }

  def FreeHand = freeHand

  def FreeHand_=(item : Option[Equipment]) : Option[Equipment] = {
    if(freeHand.Equipment.isEmpty || item.isEmpty) {
      freeHand.Equipment = item
    }
    FreeHand.Equipment
  }

  def SaveLoadout(label : String, line : Int) : Unit = {
    loadouts(line) = Some(new Loadout(this, label))
  }

  def LoadLoadout(line : Int) : Option[Loadout] = loadouts(line)

  def DeleteLoadout(line : Int) : Unit = {
    loadouts(line) = None
  }

  def Find(obj : Equipment) : Option[Int] = Find(obj.GUID)

  def Find(guid : PlanetSideGUID) : Option[Int] = {
    findInHolsters(holsters.iterator, guid) match {
      case Some(index) =>
        Some(index)
      case None =>
        findInInventory(inventory.Items.values.iterator, guid) match {
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

  @tailrec private def findInInventory(iter : Iterator[InventoryItem], guid : PlanetSideGUID) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val item = iter.next
      if(item.obj.GUID == guid) {
        Some(item.start)
      }
      else {
        findInInventory(iter, guid)
      }
    }
  }

  def DrawnSlot : Int = drawnSlot

  def DrawnSlot_=(slot : Int = Player.HandsDownSlot) : Int = {
    if(slot != drawnSlot) {
      val origDrawnSlot : Int = drawnSlot
      if(slot == Player.HandsDownSlot) {
        drawnSlot = slot
      }
      else if(-1 < slot && slot < 5 && holsters(slot).Equipment.isDefined) {
        drawnSlot = slot
      }
      lastDrawnSlot = if(-1 < origDrawnSlot && origDrawnSlot < 5) { origDrawnSlot } else { lastDrawnSlot }
    }
    DrawnSlot
  }

  def LastDrawnSlot : Int = lastDrawnSlot

  def ExoSuit : ExoSuitType.Value = exosuit

  def ExoSuit_=(suit : ExoSuitType.Value) : Unit = {
    exosuit = suit
  }

  def BEP : Long = bep

  def BEP_=(battleExperiencePoints : Long) : Long = {
    bep = math.max(0L, math.min(battleExperiencePoints, 4294967295L))
    BEP
  }

  def CEP : Long = cep

  def CEP_=(commandExperiencePoints : Long) : Long = {
    cep = math.max(0L, math.min(commandExperiencePoints, 4294967295L))
    CEP
  }

  def Certifications : mutable.Set[CertificationType.Value] = certifications

  /**
    * Retrieve the three implant slots for this player.
    * @return an `Array` of `ImplantSlot` objects
    */
  def Implants : Array[ImplantSlot] = implants

  /**
    * What kind of implant is installed into the given slot number?
    * @see `ImplantType`
    * @param slot the slot number
    * @return the tye of implant
    */
  def Implant(slot : Int) : ImplantType.Value = {
    if(-1 < slot && slot < implants.length) { implants(slot).Implant } else { ImplantType.None }
  }

  /**
    * Given a new implant, assign it into a vacant implant slot on this player.<br>
    * <br>
    * The implant must be unique in terms of which implants have already been assigned to this player.
    * Multiple of a type of implant being assigned at once is not supported.
    * Additionally, the implant is inserted into the earliest yet-unknown but vacant slot.
    * Implant slots are vacant by just being unlocked or by having their previous implant uninstalled.
    * @param implant the implant being installed
    * @return the index of the `ImplantSlot` where the implant was installed
    */
  def InstallImplant(implant : ImplantDefinition) : Option[Int] = {
    implants.find({p => p.Installed.contains(implant)}) match { //try to find the installed implant
      case None =>
        recursiveFindImplantInSlot(implants.iterator, ImplantType.None) match { //install in a free slot
          case Some(slot) =>
            implants(slot).Implant = implant
            Some(slot)
          case None =>
            None
        }
      case Some(_) =>
        None
    }
  }

  /**
    * Remove a specific implant from a player's allocated installed implants.<br>
    * <br>
    * Due to the exclusiveness of installed implants,
    * any implant slot with a matching `Definition` can be uninstalled safely.
    * (There will never be any doubles.)
    * This operation can lead to an irregular pattern of installed and uninstalled `ImplantSlot` objects.
    * Despite that breach of pattern, the logic here is consistent as demonstrated by the client and by packets.
    * The client also assigns and removes implants based on slot numbers that only express availability of a "slot."
    * @see `AvatarImplantMessage.implantSlot`
    * @param implantType the type of implant being uninstalled
    * @return the index of the `ImplantSlot` where the implant was found and uninstalled
    */
  def UninstallImplant(implantType : ImplantType.Value) : Option[Int] = {
    recursiveFindImplantInSlot(implants.iterator, implantType) match {
      case Some(slot) =>
        implants(slot).Implant = None
      Some(slot)
      case None =>
        None
    }
  }

  /**
    * Locate the index of the encountered implant type.
    * Functional implants may be exclusive in as far as the input `Iterator`'s source is concerned,
    * but any number of `ImplantType.None` values are alway allowed in the source in any order.
    * @param iter an `Iterator` of `ImplantSlot` objects
    * @param implantType the target implant being sought
    * @param index a defaulted index value representing the structure underlying the `Iterator` param
    * @return the index where the target implant is installed
    */
  @tailrec private def recursiveFindImplantInSlot(iter : Iterator[ImplantSlot], implantType : ImplantType.Value, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val slot = iter.next
      if(slot.Unlocked && slot.Implant == implantType) {
        Some(index)
      }
      else {
        recursiveFindImplantInSlot(iter, implantType, index + 1)
      }
    }
  }

  def ResetAllImplants() : Unit = {
    implants.foreach(slot => {
      slot.Installed match {
        case Some(_) =>
          slot.Initialized = false
        case None => ;
      }
    })
  }

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

  def SessionId : Long = sessionId

  def Admin : Boolean = admin

  def Spectator : Boolean = spectator

  def Continent : String = continent

  def VehicleSeated : Option[PlanetSideGUID] = vehicleSeated

  def VehicleSeated_=(vehicle : Vehicle) : Option[PlanetSideGUID] = {
   vehicleSeated = Some(vehicle.GUID)
    VehicleSeated
  }

  def VehicleSeated_=(guid : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    vehicleSeated = guid
    VehicleSeated
  }

  def VehicleOwned : Option[PlanetSideGUID] = vehicleOwned

  def VehicleOwned_=(vehicle : Vehicle) : Option[PlanetSideGUID] = {
    vehicleOwned = Some(vehicle.GUID)
    VehicleOwned
  }

  def VehicleOwned_=(guid : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    vehicleOwned = guid
    VehicleOwned
  }

  def Continent_=(zoneId : String) : String = {
    continent = zoneId
    Continent
  }

  def Definition : AvatarDefinition = playerDef

  override def toString : String = {
    Player.toString(this)
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Player]

  override def equals(other : Any) : Boolean = other match {
    case that: Player =>
      (that canEqual this) &&
        name == that.name &&
        faction == that.faction &&
        sex == that.sex &&
        voice == that.voice &&
        head == that.head
    case _ =>
      false
  }

  override def hashCode() : Int = {
    val state = Seq(name, faction, sex, voice, head)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object Player {
  final private val definition : AvatarDefinition = new AvatarDefinition(121)
  final val FreeHandSlot : Int = 250
  final val HandsDownSlot : Int = 255

  def apply(name : String, faction : PlanetSideEmpire.Value, sex : CharacterGender.Value, head : Int, voice : Int) : Player = {
    new Player(name, faction, sex, head, voice)
  }

  def apply(guid : PlanetSideGUID, name : String, faction : PlanetSideEmpire.Value, sex : CharacterGender.Value, head : Int, voice : Int) : Player = {
    val obj = new Player(name, faction, sex, voice, head)
    obj.GUID = guid
    obj
  }

  /**
    * Change the type of `AvatarDefinition` is used to define the player.
    * @param player the player
    * @param avatarDef the player's new definition entry
    * @return the changed player
    */
  def apply(player : Player, avatarDef : AvatarDefinition) : Player = {
    player.playerDef = avatarDef
    player
  }

  def apply(player : Player, sessId : Long) : Player = {
    player.sessionId = sessId
    player
  }

  def SuitSetup(player : Player, eSuit : ExoSuitType.Value) : Unit = {
    val esuitDef : ExoSuitDefinition = ExoSuitDefinition.Select(eSuit)
    //exosuit
    player.ExoSuit = eSuit
    //inventory
    player.Inventory.Clear()
    player.Inventory.Resize(esuitDef.InventoryScale.Width, esuitDef.InventoryScale.Height)
    player.Inventory.Offset = esuitDef.InventoryOffset
    //holsters
    (0 until 5).foreach(index => { player.Slot(index).Size = esuitDef.Holster(index) })
  }

  def ChangeSessionId(player : Player, session : Long) : Long = {
    player.sessionId = session
    player.SessionId
  }

  def Administrate(player : Player, isAdmin : Boolean) : Player = {
    player.admin = isAdmin
    player
  }

  def Spectate(player : Player, isSpectator : Boolean) : Player = {
    player.spectator = isSpectator
    player
  }

  def Release(player : Player) : Player = {
    if(player.Release) {
      val obj = new Player(player.Name, player.Faction, player.Sex, player.Voice, player.Head)
      obj.VehicleOwned = player.VehicleOwned
      obj.Continent = player.Continent
      //hand over loadouts
      (0 until 10).foreach(index => {
        obj.loadouts(index) = player.loadouts(index)
      })
      //hand over implants
      (0 until 3).foreach(index => {
        if(obj.Implants(index).Unlocked = player.Implants(index).Unlocked) {
          obj.Implants(index).Implant = player.Implants(index).Installed
        }
      })
      //hand over knife
      obj.Slot(4).Equipment = player.Slot(4).Equipment
      player.Slot(4).Equipment = None
      //hand over ???
      obj.fifthSlot.Equipment = player.fifthSlot.Equipment
      player.fifthSlot.Equipment = None
      obj
    }
    else {
      player
    }
  }

  def toString(obj : Player) : String = {
    val name : String = if(obj.VehicleSeated.isDefined) { s"[${obj.name}, ${obj.VehicleSeated.get.guid}]" } else { obj.Name }
    s"[player $name, ${obj.Faction} (${obj.Health}/${obj.MaxHealth})(${obj.Armor}/${obj.MaxArmor})]"
  }
}
