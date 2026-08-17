// Copyright (c) 2026 PSForever
package net.psforever.objects.avatar

import net.psforever.objects.avatar.interaction.{InteractWithForceDomeProtection, TriggerOnPlayerRule, WithEntrance, WithGantry, WithLava, WithWater}
import net.psforever.objects.{GlobalDefinitions, Player, OffhandEquipmentSlot}
import net.psforever.objects.ballistics.InteractWithRadiationClouds
import net.psforever.objects.ce.{InteractWithMines, InteractWithTurrets}
import net.psforever.objects.definition.{AvatarBotDefinition, ExoSuitDefinition}
import net.psforever.objects.equipment.{EquipmentSize, EquipmentSlot, JammableUnit}
import net.psforever.objects.inventory.{Container, GridInventory, InventoryItem}
import net.psforever.objects.serverobject.{PlanetSideServerObject, environment}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.aura.AuraContainer
import net.psforever.objects.serverobject.environment.interaction.common.{WithDeath, WithMovementTrigger}
import net.psforever.objects.serverobject.interior.InteriorAwareFromInteraction
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.damage.DamageProfile
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.objects.vital.resolution.DamageResistanceModel
import net.psforever.objects.zones.blockmap.BlockMapEntity
import net.psforever.objects.zones.interaction.InteractsWithZone
import net.psforever.objects.zones.ZoneAware
import net.psforever.packet.game.objectcreate.BasicCharacterData
import net.psforever.types._

/**
  * A stripped down combination of the `Avatar` and `Player` classes.
  * This acts as a base class for NPC avatars, and is used for VR Shooting Range infantry targets.
  * @see `Avatar`
  * @see `Player`
  */
case class AvatarBot(
    basic: BasicCharacterData,
    definition: AvatarBotDefinition,
    bep: Long = 0,
    cep: Long = 0,
    certifications: Set[Certification] = Set(),
    implants: Seq[Option[Implant]] = Seq(None, None, None),
    decoration: ProgressDecoration = ProgressDecoration()
) extends PlanetSideServerObject
  with BlockMapEntity
  with InteractsWithZone
  with FactionAffinity
  with Vitality
  with ResistanceProfile
  with Container
  with JammableUnit
  with ZoneAware
  with InteriorAwareFromInteraction
  with AuraContainer {
  interaction(new InteractWithForceDomeProtection())
  interaction(environment.interaction.InteractWithEnvironment(Seq(
    new WithEntrance(),
    new WithWater(Name),
    new WithLava(),
    new WithDeath(),
    new WithGantry(Name),
    new WithMovementTrigger()
  )))
  interaction(new InteractWithMines(range = 10, TriggerOnPlayerRule))
  interaction(new InteractWithTurrets())
  interaction(new InteractWithRadiationClouds(range = 10f, None))

  val br: BattleRank  = BattleRank.withExperience(bep)
  val cr: CommandRank = CommandRank.withExperience(cep)

  private var armor: Int                     = 0

  private var exosuit: ExoSuitDefinition     = GlobalDefinitions.Standard
  private val freeHand: EquipmentSlot        = new OffhandEquipmentSlot(EquipmentSize.Inventory)
  private val holsters: Array[EquipmentSlot] = Array.fill[EquipmentSlot](5)(new EquipmentSlot)
  private val inventory: GridInventory       = GridInventory()
  private var drawnSlot: Int                 = Player.HandsDownSlot
  private var lastDrawnSlot: Int             = Player.HandsDownSlot

  private var facingYawUpper: Float          = 0f
  private var crouching: Boolean             = false
  private var jumping: Boolean               = false
  private var cloaked: Boolean               = false

  /** The maximum stamina amount */
  val maxStamina: Int                        = 100

  var fatigued: Boolean                      = false
  var stamina: Int                           = 100

  //init
  Health = 0       //bot health is artificially managed as a part of their lifecycle; start entity as dead
  Destroyed = true //see isAlive
  AvatarBot.SuitSetup(this, exosuit)

  def Definition: AvatarBotDefinition = definition

  def Name: String = basic.name

  def Faction: PlanetSideEmpire.Value = basic.faction

  def Sex: CharacterSex = basic.sex

  def Head: Int = basic.head

  def Voice: CharacterVoice.Value = basic.voice

  def isAlive: Boolean = !Destroyed

  def Spawn(): Boolean = {
    if (!isAlive) {
      Destroyed = false
      Health = Definition.DefaultHealth
      Armor = MaxArmor
    }
    isAlive
  }

  def Die: Boolean = {
    Destroyed = true
    Health = 0
    false
  }

  def Armor: Int = armor

  def Armor_=(assignArmor: Int): Int = {
    armor = math.min(math.max(0, assignArmor), MaxArmor)
    Armor
  }

  def MaxArmor: Int = exosuit.MaxArmor

  override def Slot(slot: Int): EquipmentSlot = {
    if (inventory.Offset <= slot && slot <= inventory.LastIndex) {
      inventory.Slot(slot)
    } else if (slot > -1 && slot < 5) {
      holsters(slot)
    } else if (slot == 5) {
      OffhandEquipmentSlot.BlockedSlot
    } else if (slot == Player.FreeHandSlot) {
      freeHand
    } else {
      OffhandEquipmentSlot.BlockedSlot
    }
  }

  def VisibleSlots: Set[Int] =
  if (exosuit.SuitType == ExoSuitType.MAX) {
    Set(0)
  } else {
    (0 to 4).filterNot(index => holsters(index).Size == EquipmentSize.Blocked).toSet
  }

  def Holsters(): Array[EquipmentSlot] = holsters

  /**
    * Transform the holster equipment slots
    * into a list of the kind of item wrapper found in an inventory.
    * @see `GridInventory`
    * @see `InventoryItem`
    * @return a list of items that would be found in a proper inventory
    */
  def HolsterItems(): List[InventoryItem] = holsters
    .zipWithIndex
    .collect {
      case (slot: EquipmentSlot, index: Int) =>
        slot.Equipment match {
          case Some(item) => Some(InventoryItem(item, index))
          case None => None
        }
    }.flatten.toList

  def Inventory: GridInventory = inventory

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
    AvatarBot.SuitSetup(this, eSuit)
  }

  def Subtract: DamageProfile = exosuit.Subtract

  def ResistanceDirectHit: Int = exosuit.ResistanceDirectHit

  def ResistanceSplash: Int = exosuit.ResistanceSplash

  def ResistanceAggravated: Int = exosuit.ResistanceAggravated

  def RadiationShielding: Float = exosuit.RadiationShielding

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

  override def CanDamage: Boolean = {
    isAlive && super.CanDamage
  }

  def DamageModel: DamageResistanceModel = exosuit.asInstanceOf[DamageResistanceModel]

  /** Return true if the stamina is at the maximum amount */
  def staminaFull: Boolean = {
    stamina == maxStamina
  }

  override def toString: String = {
    val guid = if (HasGUID) {
      s" $Continent-${GUID.guid}"
    } else {
      ""
    }
    s"${basic.name}$guid ${basic.faction} H: $Health/$MaxHealth A: $Armor/$MaxArmor"
  }
}

object AvatarBot {
  final case class Die(reason: Option[DamageInteraction])

  final case class Release()

  final case class Spawn()

  object Die {
    def apply(): Die = Die(None)

    def apply(reason: DamageInteraction): Die = {
      Die(Some(reason))
    }
  }

  def apply(name: String, faction: PlanetSideEmpire.Value, sex: CharacterSex, head: Int, voice: CharacterVoice.Value, definition: AvatarBotDefinition): AvatarBot = {
    AvatarBot(BasicCharacterData(name, faction, sex, head, voice), definition)
  }

  private def SuitSetup(bot: AvatarBot, eSuit: ExoSuitDefinition): Unit = {
    //inventory
    bot.Inventory.Clear()
    bot.Inventory.Resize(eSuit.InventoryScale.Width, eSuit.InventoryScale.Height)
    bot.Inventory.Offset = eSuit.InventoryOffset
    //holsters
    (0 until 5).foreach { index => bot.Slot(index).Size = eSuit.Holster(index) }
  }
}
