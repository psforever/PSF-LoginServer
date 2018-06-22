// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.ActorContext
import net.psforever.objects._
import net.psforever.objects.definition.{SeatDefinition, ToolDefinition}
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
  private val seats : Map[Int, Chair] = Map(0 -> Chair(new SeatDefinition() { ControlledWeapon = Some(1) }))
  private var weapons : Map[Int, EquipmentSlot] = Map.empty
  private val inventory : GridInventory = GridInventory()
  private var upgradePath : TurretUpgrade.Value = TurretUpgrade.None
  private var jammered : Boolean = false

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
    Definition.Weapons.foreach({ case(index , upgradePaths) =>
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
  def apply(tDef : MannedTurretDefinition) : MannedTurret = {
    new MannedTurret(tDef)
  }

  import net.psforever.objects.equipment.EquipmentSize
  /**
    * Use the `*Definition` that was provided to this object to initialize its fields and settings.
    * @param turret the `MannedTurret` being initialized
    * @see `{object}.LoadDefinition`
    */
  def LoadDefinition(turret : MannedTurret) : MannedTurret = {
    val tdef : MannedTurretDefinition = turret.Definition
    //general stuff
    turret.Health = tdef.MaxHealth
    //create weapons
    turret.weapons = tdef.Weapons.map({case (num, upgradePaths) =>
      val slot = EquipmentSlot(EquipmentSize.BaseTurretWeapon)
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

  private class MannedTurretWeapon(mdef : MannedTurretDefinition, udefs : Map[TurretUpgrade.Value, ToolDefinition], default : TurretUpgrade.Value = TurretUpgrade.None)
    extends Tool(udefs(default)) {
    private var upgradePath : TurretUpgrade.Value = default

    def Upgrade : TurretUpgrade.Value = {
      /*
      Must check `not null` due to how this object's `Definition` will be called during `Tool`'s constructor
      before the internal value can be set to default value `None`
       */
      if(upgradePath == null) { default } else { upgradePath }
    }

    def Upgrade_=(upgrade : TurretUpgrade.Value) : TurretUpgrade.Value = {
      if(udefs.contains(upgrade)) {
        val beforeUpgrade = upgradePath
        upgradePath = upgrade
        if(beforeUpgrade != upgradePath) {
          Tool.LoadDefinition(this) //rebuild weapon internal structure
          FireModeIndex = 0
        }
      }
      Upgrade
    }

    override def Definition = udefs(Upgrade)
  }

  import net.psforever.objects.definition.AmmoBoxDefinition
  private class TurretAmmoBox(private val adef : AmmoBoxDefinition) extends AmmoBox(adef, Some(65535)) {
    import net.psforever.objects.inventory.InventoryTile
    override def Tile = InventoryTile.Tile11

    override def Capacity_=(toCapacity : Int) = Capacity
  }

  def Constructor(tdef : MannedTurretDefinition)(id : Int, context : ActorContext) : MannedTurret = {
    import akka.actor.Props
    val obj = MannedTurret(tdef)
    obj.Actor = context.actorOf(Props(classOf[MannedTurretControl], obj), s"${obj.Definition.Name}_$id")
    obj
  }
}

object TurretUpgrade extends Enumeration {
  val
  None,
  AVCombo,
  FlakCombo
  = Value
}
