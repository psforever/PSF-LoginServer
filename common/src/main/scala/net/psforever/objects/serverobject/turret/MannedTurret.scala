// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.{EquipmentSlot, Player}
import net.psforever.objects.definition.SeatDefinition
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.inventory.{Container, GridInventory}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.serverobject.turret.MannedTurret.MannedTurretWeapon
import net.psforever.objects.vehicles.{MountedWeapons, Seat => Chair}

class MannedTurret(tDef : MannedTurretDefinition) extends Amenity
  with FactionAffinity
  with Mountable
  with MountedWeapons
  with Container {
  private var health : Int = 1
  private var jammered : Boolean = false
  /** manned turrets have just one seat; this is just standard interface */
  private val seats : Map[Int, Chair] = Map(0 -> Chair(new SeatDefinition() { ControlledWeapon = Some(1) }))
  /** manned turrets have just one weapon; this is just standard interface */
  private var weapons : Map[Int, EquipmentSlot] = Map.empty
  /** may or may not have inaccessible inventory space
    * see `ReserveAmmunition` in the definition */
  private val inventory : GridInventory = new GridInventory() {
    import net.psforever.packet.game.PlanetSideGUID
    override def Remove(index : Int) : Boolean = false
    override def Remove(guid : PlanetSideGUID) : Boolean = false
  }
  /** some turrets can be updated; they all start without updates */
  private var upgradePath : TurretUpgrade.Value = TurretUpgrade.None

  MannedTurret.LoadDefinition(this)

  def Health : Int = {
    health
  }

  def Health_=(toHealth : Int) : Int = {
    health = toHealth
    health
  }

  def MaxHealth : Int = {
    Definition.MaxHealth
  }

  def Seats : Map[Int, Chair] = seats

  def Seat(seatNum : Int) : Option[Chair] = seats.get(seatNum)

  /**
    * Given the index of an entry mounting point, return the infantry-accessible `Seat` associated with it.
    * @param mountPoint an index representing the seat position / mounting point
    * @return a seat number, or `None`
    */
  def GetSeatFromMountPoint(mountPoint : Int) : Option[Int] = {
    Definition.MountPoints.get(mountPoint)
  }

  def MountPoints : Map[Int, Int] = Definition.MountPoints.toMap

  def PassengerInSeat(user : Player) : Option[Int] = {
    if(seats(0).Occupant.contains(user)) {
      Some(0)
    }
    else {
      None
    }
  }

  def Weapons : Map[Int, EquipmentSlot] = weapons.filter({ case(index, _) => index < 2 })

  def ControlledWeapon(wepNumber : Int) : Option[Equipment] = {
    if(VisibleSlots.contains(wepNumber)) {
      weapons(wepNumber).Equipment
    }
    else {
      None
    }
  }

  def Inventory : GridInventory = inventory

  def VisibleSlots : Set[Int] = Set(1)

  def Upgrade : TurretUpgrade.Value = upgradePath

  def Upgrade_=(upgrade : TurretUpgrade.Value) : TurretUpgrade.Value = {
    upgradePath = upgrade
    //upgrade each weapon as long as that weapon has a valid option for that upgrade
    Definition.Weapons.foreach({ case(index, upgradePaths) =>
      if(upgradePaths.contains(upgrade)) {
        weapons(index).Equipment.get.asInstanceOf[MannedTurretWeapon].Upgrade = upgrade
      }
    })
    Upgrade
  }

  def Jammered : Boolean = jammered

  def Jammered_=(jamState : Boolean) : Boolean = {
    jammered = jamState
    Jammered
  }

  def Definition : MannedTurretDefinition = tDef
}

object MannedTurret {
  /**
    * Overloaded constructor.
    * @param tDef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    * @return a `MannedTurret` object
    */
  def apply(tDef : MannedTurretDefinition) : MannedTurret = {
    new MannedTurret(tDef)
  }

  /**
    * Use the `*Definition` that was provided to this object to initialize its fields and settings.
    * @param turret the `MannedTurret` being initialized
    * @see `{object}.LoadDefinition`
    */
  def LoadDefinition(turret : MannedTurret) : MannedTurret = {
    import net.psforever.objects.equipment.EquipmentSize.BaseTurretWeapon
    val tdef : MannedTurretDefinition = turret.Definition
    //general stuff
    turret.Health = tdef.MaxHealth
    //create weapons; note the class
    turret.weapons = tdef.Weapons.map({case (num, upgradePaths) =>
      val slot = EquipmentSlot(BaseTurretWeapon)
      slot.Equipment = new MannedTurretWeapon(tdef, upgradePaths.toMap)
      num -> slot
    }).toMap
    //special inventory ammunition object(s)
    if(tdef.ReserveAmmunition) {
      val allAmmunitionTypes = tdef.Weapons.values.flatMap{ _.values.flatMap { _.AmmoTypes } }.toSet
      if(allAmmunitionTypes.nonEmpty) {
        turret.inventory.Resize(allAmmunitionTypes.size, 1)
        var i : Int = 0
        allAmmunitionTypes.foreach(ammotype => {
          turret.inventory.InsertQuickly(i, new TurretAmmoBox(ammotype))
          i += 1
        })
      }
    }
    turret
  }

  import net.psforever.objects.definition.ToolDefinition
  import net.psforever.objects.Tool
  /**
    * A stateful weapon that is mounted in `MannedTurrets`
    * and may maintains a group of upgraded forms that can by swapped
    * without reconstructing the weapon object itself or managing object registration.
    * @param mdef the turret's definition
    * @param udefs a map of turret upgrades to tool definitions that would be constructed by this weapon
    * @param default the default upgrade state;
    *                defaults to `None`
    */
  private class MannedTurretWeapon(mdef : MannedTurretDefinition, udefs : Map[TurretUpgrade.Value, ToolDefinition], default : TurretUpgrade.Value = TurretUpgrade.None)
    extends Tool(udefs(default)) {
    private var upgradePath : TurretUpgrade.Value = default

    def Upgrade : TurretUpgrade.Value = {
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

    def Upgrade_=(upgrade : TurretUpgrade.Value) : TurretUpgrade.Value = {
      if(udefs.contains(upgrade)) {
        val beforeUpgrade = upgradePath
        upgradePath = upgrade
        if(beforeUpgrade != upgradePath) {
          Tool.LoadDefinition(this) //rebuild weapon internal structure
          FireModeIndex = 0 //reset fire mode; this option is always valid
        }
      }
      Upgrade
    }

    override def Definition = udefs(Upgrade)
  }

  import net.psforever.objects.definition.AmmoBoxDefinition
  import net.psforever.objects.AmmoBox

  /**
    * A special type of ammunition box contained within a `MannedTurret` for the purposes of infinite reloads.
    * The original quantity of ammunition does not change.
    * @param adef ammunition definition
    */
  private class TurretAmmoBox(private val adef : AmmoBoxDefinition) extends AmmoBox(adef, Some(65535)) {
    import net.psforever.objects.inventory.InventoryTile
    override def Tile = InventoryTile.Tile11

    override def Capacity_=(toCapacity : Int) = Capacity
  }

  import akka.actor.ActorContext
  /**
    * Instantiate an configure a `MannedTurret` object
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `MannedTurret` object
    */
  def Constructor(tdef : MannedTurretDefinition)(id : Int, context : ActorContext) : MannedTurret = {
    import akka.actor.Props
    val obj = MannedTurret(tdef)
    obj.Actor = context.actorOf(Props(classOf[MannedTurretControl], obj), s"${tdef.Name}_$id")
    obj
  }
}
