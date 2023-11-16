// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.{AmmoBox, PlanetSideGameObject, Tool}
import net.psforever.objects.definition.{AmmoBoxDefinition, ToolDefinition}
import net.psforever.objects.equipment.EquipmentSlot
import net.psforever.objects.inventory.{Container, GridInventory}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.mount.{SeatDefinition, Seat => Chair}
import net.psforever.objects.vehicles.MountableWeapons

trait WeaponTurret
  extends FactionAffinity
  with MountableWeapons
  with Container {
  _: PlanetSideGameObject =>

  /** manned turrets have just one mount; this is just standard interface */
  seats = Map(0 -> new Chair(new SeatDefinition()))

  /** may or may not have inaccessible inventory space
    * see `ReserveAmmunition` in the definition
    */
  protected val inventory: GridInventory = new GridInventory() {
    import net.psforever.types.PlanetSideGUID
    override def Remove(index: Int): Boolean           = false
    override def Remove(guid: PlanetSideGUID): Boolean = false
  }

  /** some turrets can be updated; they all start without updates */
  private var upgradePath: TurretUpgrade.Value = TurretUpgrade.None
  private var middleOfUpgrade: Boolean         = false

  /*
  do not mind what the IDE probably comments about these method prototypes for Health and MaxHealth
  they do not override methods in Vitality, unless overrode in any class that implements this one
  due to the inheritance requirement above, these statements are not required to be implemented or overrode ever
  they are purely for class visibility
   */
  def Health: Int

  def MaxHealth: Int

  def Inventory: GridInventory = inventory

  def VisibleSlots: Set[Int] = Set(1)

  def Upgrade: TurretUpgrade.Value = upgradePath

  def Upgrade_=(upgrade: TurretUpgrade.Value): TurretUpgrade.Value = {
    var updated = false
    //upgrade each weapon as long as that weapon has a valid option for that upgrade
    Definition match {
      case definition: TurretDefinition =>
        definition.WeaponPaths.foreach({
          case (index, upgradePaths) =>
            if (upgradePaths.contains(upgrade)) {
              updated = true
              weapons(index).Equipment.get.asInstanceOf[TurretWeapon].Upgrade = upgrade
            }
        })
      case _ => ;
    }
    if (updated) {
      upgradePath = upgrade
    }
    Upgrade
  }

  def ConfirmUpgrade(upgrade: TurretUpgrade.Value): TurretUpgrade.Value = {
    if (middleOfUpgrade && upgradePath == upgrade) {
      middleOfUpgrade = false
    }
    upgradePath
  }

  def isUpgrading: Boolean = middleOfUpgrade

  def setMiddleOfUpgrade(value: Boolean): Unit = {
    middleOfUpgrade = value
  }

  def Definition: TurretDefinition
}

object WeaponTurret {

  /**
    * Use the `*Definition` that was provided to this object to initialize its fields and settings.
    * @see `{object}.LoadDefinition`
    * @param turret the `MannedTurret` being initialized
    */
  def LoadDefinition(turret: WeaponTurret): WeaponTurret = {
    LoadDefinition(turret, turret.Definition)
  }

  /**
    * Use the `*Definition` that was provided to this object to initialize its fields and settings.
    * A default definition is provided to be used.
    * @see `{object}.LoadDefinition`
    * @param turret the `MannedTurret` being initialized
    * @param tdef the object definition
    */
  def LoadDefinition(turret: WeaponTurret, tdef: TurretDefinition): WeaponTurret = {
    import net.psforever.objects.equipment.EquipmentSize.BaseTurretWeapon
    //create weapons; note the class
    turret.weapons = tdef.WeaponPaths
      .map({
        case (num, upgradePaths) =>
          val slot = EquipmentSlot(BaseTurretWeapon)
          slot.Equipment = new TurretWeapon(tdef, upgradePaths.toMap)
          num -> slot
      })
      .toMap
    //special inventory ammunition object(s)
    if (tdef.ReserveAmmunition) {
      val allAmmunitionTypes = tdef.WeaponPaths.values.flatMap { _.values.flatMap { _.AmmoTypes } }.toSet
      if (allAmmunitionTypes.nonEmpty) {
        turret.inventory.Resize(allAmmunitionTypes.size, 1)
        var i: Int = 0
        allAmmunitionTypes.foreach(ammotype => {
          turret.inventory.InsertQuickly(i, new TurretAmmoBox(ammotype))
          i += 1
        })
      }
    }
    turret
  }
}

class TurretWeapon(
    mdef: TurretDefinition,
    udefs: Map[TurretUpgrade.Value, ToolDefinition],
    default: TurretUpgrade.Value = TurretUpgrade.None
) extends Tool(udefs(default)) {
  private var upgradePath: TurretUpgrade.Value = default

  def Upgrade: TurretUpgrade.Value = {
    /*
    Must check `not null` due to how this object's `Definition` will be called during `Tool`'s constructor
    before the internal value can be set to default value `None`
     */
    Option(upgradePath) match {
      case Some(value) =>
        value
      case None =>
        default
    }
  }

  def Upgrade_=(upgrade: TurretUpgrade.Value): TurretUpgrade.Value = {
    if (udefs.contains(upgrade)) {
      val beforeUpgrade = upgradePath
      upgradePath = upgrade
      if (beforeUpgrade != upgradePath) {
        Tool.LoadDefinition(this) //rebuild weapon internal structure
        FireModeIndex = 0         //reset fire mode; this option is always valid
      }
    }
    Upgrade
  }

  override def Definition = udefs(Upgrade)
}

/**
  * A special type of ammunition box contained within a `MannedTurret` for the purposes of infinite reloads.
  * The original quantity of ammunition does not change.
  * @param adef ammunition definition
  */
class TurretAmmoBox(private val adef: AmmoBoxDefinition) extends AmmoBox(adef, Some(65535)) {
  import net.psforever.objects.inventory.InventoryTile
  override def Tile = InventoryTile.Tile11

  override def Capacity_=(toCapacity: Int) = Capacity
}
