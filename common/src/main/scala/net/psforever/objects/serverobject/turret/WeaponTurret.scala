// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.{AmmoBox, PlanetSideGameObject, Player, Tool}
import net.psforever.objects.definition.{AmmoBoxDefinition, SeatDefinition, ToolDefinition}
import net.psforever.objects.equipment.{Equipment, EquipmentSlot}
import net.psforever.objects.inventory.{Container, GridInventory}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vehicles.{MountedWeapons, Seat => Chair}

trait WeaponTurret extends FactionAffinity with Mountable with MountedWeapons with Container {
  _: PlanetSideGameObject =>

  /** manned turrets have just one seat; this is just standard interface */
  protected val seats: Map[Int, Chair] = Map(0 -> Chair(new SeatDefinition() { ControlledWeapon = Some(1) }))

  /** turrets have just one weapon; this is just standard interface */
  protected var weapons: Map[Int, EquipmentSlot] = Map.empty

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

  def Weapons: Map[Int, EquipmentSlot] = weapons

  def MountPoints: Map[Int, Int]

  def Seats: Map[Int, Chair] = seats

  def Seat(seatNum: Int): Option[Chair] = seats.get(seatNum)

  /**
    * Given the index of an entry mounting point, return the infantry-accessible `Seat` associated with it.
    * @param mountPoint an index representing the seat position / mounting point
    * @return a seat number, or `None`
    */
  def GetSeatFromMountPoint(mountPoint: Int): Option[Int] = {
    MountPoints.get(mountPoint)
  }

  def PassengerInSeat(user: Player): Option[Int] = {
    if (seats(0).Occupant.contains(user)) {
      Some(0)
    } else {
      None
    }
  }

  def ControlledWeapon(wepNumber: Int): Option[Equipment] = {
    if (VisibleSlots.contains(wepNumber)) {
      weapons(wepNumber).Equipment
    } else {
      None
    }
  }

  def Upgrade: TurretUpgrade.Value = upgradePath

  def Upgrade_=(upgrade: TurretUpgrade.Value): TurretUpgrade.Value = {
    middleOfUpgrade = true //blocking flag; block early
    var updated = false
    //upgrade each weapon as long as that weapon has a valid option for that upgrade
    Definition match {
      case definition: TurretDefinition =>
        definition.Weapons.foreach({
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
    } else {
      middleOfUpgrade = false //reset
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
    turret.weapons = tdef.Weapons
      .map({
        case (num, upgradePaths) =>
          val slot = EquipmentSlot(BaseTurretWeapon)
          slot.Equipment = new TurretWeapon(tdef, upgradePaths.toMap)
          num -> slot
      })
      .toMap
    //special inventory ammunition object(s)
    if (tdef.ReserveAmmunition) {
      val allAmmunitionTypes = tdef.Weapons.values.flatMap { _.values.flatMap { _.AmmoTypes } }.toSet
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
