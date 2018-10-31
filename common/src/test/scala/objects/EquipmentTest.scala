// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects._
import net.psforever.objects.equipment._
import net.psforever.objects.inventory.InventoryTile
import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects.ce.{DeployedItem, TelepadLike}
import net.psforever.objects.definition._
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.CertificationType
import org.specs2.mutable._

class EquipmentTest extends Specification {
  "EquipmentSize" should {
    "equal" in {
      //basic equality
      EquipmentSize.isEqual(EquipmentSize.Pistol, EquipmentSize.Pistol) mustEqual true
      EquipmentSize.isEqual(EquipmentSize.Pistol, EquipmentSize.Rifle) mustEqual false
      //Inventory is always allowed
      EquipmentSize.isEqual(EquipmentSize.Inventory, EquipmentSize.Rifle) mustEqual true
      EquipmentSize.isEqual(EquipmentSize.Pistol, EquipmentSize.Inventory) mustEqual true
      //Blocked is never allowed
      EquipmentSize.isEqual(EquipmentSize.Blocked, EquipmentSize.Rifle) mustEqual false
      EquipmentSize.isEqual(EquipmentSize.Pistol, EquipmentSize.Blocked) mustEqual false
      EquipmentSize.isEqual(EquipmentSize.Blocked, EquipmentSize.Inventory) mustEqual false
    }
  }

  "AmmoBox" should {
    "define" in {
      val obj = AmmoBoxDefinition(86)
      obj.Capacity = 300
      obj.Tile = InventoryTile.Tile44

      obj.AmmoType mustEqual Ammo.aphelion_immolation_cannon_ammo
      obj.Capacity mustEqual 300
      obj.Tile.Width mustEqual InventoryTile.Tile44.Width
      obj.Tile.Height mustEqual InventoryTile.Tile44.Height
      obj.ObjectId mustEqual 86
    }

    "construct" in {
      val obj = AmmoBox(bullet_9mm)
      obj.AmmoType mustEqual Ammo.bullet_9mm
      obj.Capacity mustEqual 50
    }

    "construct (2)" in {
      val obj = AmmoBox(bullet_9mm, 150)
      obj.AmmoType mustEqual Ammo.bullet_9mm
      obj.Capacity mustEqual 150
    }

    "vary capacity" in {
      val obj = AmmoBox(bullet_9mm, 0)
      obj.Capacity mustEqual 1 //can not be initialized to 0
      obj.Capacity = 75
      obj.Capacity mustEqual 75
    }

    "limit capacity" in {
      val obj = AmmoBox(bullet_9mm)
      obj.Capacity mustEqual 50
      obj.Capacity = -1
      obj.Capacity mustEqual 0
      obj.Capacity = 65536
      obj.Capacity mustEqual 65535
    }

    "split (0)" in {
      val obj = AmmoBox(bullet_9mm)
      obj.Capacity = 0
      val list = AmmoBox.Split(obj)
      list.size mustEqual 0
    }

    "split (1)" in {
      val obj = AmmoBox(bullet_9mm)
      obj.Capacity = 50
      val list = AmmoBox.Split(obj)
      list.size mustEqual 1
      list.head.Capacity mustEqual 50
    }

    "split (2)" in {
      val obj = AmmoBox(bullet_9mm)
      obj.Capacity = 75
      val list = AmmoBox.Split(obj)
      list.size mustEqual 2
      list.head.Capacity mustEqual 50
      list(1).Capacity mustEqual 25
    }

    "split (4)" in {
      val obj = AmmoBox(bullet_9mm)
      obj.Capacity = 165
      val list = AmmoBox.Split(obj)
      list.size mustEqual 4
      list.head.Capacity mustEqual 50
      list(1).Capacity mustEqual 50
      list(2).Capacity mustEqual 50
      list(3).Capacity mustEqual 15
    }
  }

  "Tool" should {
    "define" in {
      val obj = ToolDefinition(1076)
      obj.Name = "sample_weapon"
      obj.Size = EquipmentSize.Rifle
      obj.AmmoTypes += GlobalDefinitions.shotgun_shell
      obj.AmmoTypes += GlobalDefinitions.shotgun_shell_AP
      obj.FireModes += new FireModeDefinition
      obj.FireModes.head.AmmoTypeIndices += 0
      obj.FireModes.head.AmmoTypeIndices += 1
      obj.FireModes.head.AmmoSlotIndex = 0
      obj.FireModes.head.Magazine = 18
      obj.FireModes += new FireModeDefinition
      obj.FireModes(1).AmmoTypeIndices += 0
      obj.FireModes(1).AmmoTypeIndices += 1
      obj.FireModes(1).AmmoSlotIndex = 1
      obj.FireModes(1).Chamber = 3
      obj.FireModes(1).Magazine = 18
      obj.Tile = InventoryTile.Tile93
      obj.ObjectId mustEqual 1076

      obj.Name mustEqual "sample_weapon"
      obj.AmmoTypes.head mustEqual GlobalDefinitions.shotgun_shell
      obj.AmmoTypes(1) mustEqual GlobalDefinitions.shotgun_shell_AP
      obj.FireModes.head.AmmoTypeIndices.head mustEqual 0
      obj.FireModes.head.AmmoTypeIndices(1) mustEqual 1
      obj.FireModes.head.AmmoSlotIndex mustEqual 0
      obj.FireModes.head.Chamber mustEqual 1
      obj.FireModes.head.Magazine mustEqual 18
      obj.FireModes(1).AmmoTypeIndices.head mustEqual 0
      obj.FireModes(1).AmmoTypeIndices(1) mustEqual 1
      obj.FireModes(1).AmmoSlotIndex mustEqual 1
      obj.FireModes(1).Chamber mustEqual 3
      obj.FireModes(1).Magazine mustEqual 18
      obj.Tile.Width mustEqual InventoryTile.Tile93.Width
      obj.Tile.Height mustEqual InventoryTile.Tile93.Height
    }

    "construct" in {
      val obj : Tool = Tool(fury_weapon_systema)
      obj.Definition.ObjectId mustEqual fury_weapon_systema.ObjectId
    }

    "fire mode" in {
      //explanation: fury_weapon_systema has one fire mode and that fire mode is our only option
      val obj : Tool = Tool(fury_weapon_systema)
      obj.Magazine = obj.MaxMagazine
      obj.Magazine mustEqual obj.Definition.FireModes.head.Magazine
      //fmode = 0
      obj.FireModeIndex mustEqual 0
      obj.FireMode.Magazine mustEqual 2
      obj.AmmoType mustEqual Ammo.hellfire_ammo
      //fmode -> 1 (0)
      obj.FireModeIndex = 1
      obj.FireModeIndex mustEqual 0
      obj.FireMode.Magazine mustEqual 2
      obj.AmmoType mustEqual Ammo.hellfire_ammo
    }

    "multiple fire modes" in {
      //explanation: sample_weapon has two fire modes; each fire mode has a different ammunition type
      val obj : Tool = Tool(punisher)
      //fmode = 0
      obj.FireModeIndex mustEqual 0
      obj.FireMode.Magazine mustEqual 30
      obj.AmmoType mustEqual Ammo.bullet_9mm
      //fmode -> 1
      obj.NextFireMode
      obj.FireModeIndex mustEqual 1
      obj.FireMode.Magazine mustEqual 1
      obj.AmmoType mustEqual Ammo.rocket
      //fmode -> 0
      obj.NextFireMode
      obj.FireModeIndex mustEqual 0
      obj.FireMode.Magazine mustEqual 30
      obj.AmmoType mustEqual Ammo.bullet_9mm
    }

    "multiple types of ammunition" in {
      //explanation: obj has one fire mode and two ammunitions; adjusting the AmmoType changes between them
      val obj : Tool = Tool(flechette)
      //ammo = 0
      obj.AmmoTypeIndex mustEqual 0
      obj.AmmoType mustEqual Ammo.shotgun_shell
      //ammo -> 1
      obj.NextAmmoType
      obj.AmmoTypeIndex mustEqual 1
      obj.AmmoType mustEqual Ammo.shotgun_shell_AP
      //ammo -> 2 (0)
      obj.NextAmmoType
      obj.AmmoTypeIndex mustEqual 0
      obj.AmmoType mustEqual Ammo.shotgun_shell
    }

    "multiple ammo types and multiple fire modes, split (Punisher)" in {
      val obj = Tool(GlobalDefinitions.punisher)
      //ammo = 0, fmode = 0
      obj.FireModeIndex mustEqual 0
      obj.AmmoTypeIndex mustEqual 0
      obj.AmmoType mustEqual Ammo.bullet_9mm
      //ammo = 2, fmode = 1
      obj.NextFireMode
      obj.FireModeIndex mustEqual 1
      obj.AmmoTypeIndex mustEqual 2
      obj.AmmoType mustEqual Ammo.rocket
      //ammo = 3, fmode = 1
      obj.NextAmmoType
      obj.AmmoTypeIndex mustEqual 3
      obj.AmmoType mustEqual Ammo.frag_cartridge
      //ammo = 4, fmode = 1
      obj.NextAmmoType
      obj.AmmoTypeIndex mustEqual 4
      obj.AmmoType mustEqual Ammo.jammer_cartridge
      //ammo = 0, fmode = 0
      obj.NextFireMode
      obj.FireModeIndex mustEqual 0
      obj.AmmoTypeIndex mustEqual 0
      obj.AmmoType mustEqual Ammo.bullet_9mm
      //ammo = 1, fmode = 0
      obj.NextAmmoType
      obj.AmmoTypeIndex mustEqual 1
      obj.AmmoType mustEqual Ammo.bullet_9mm_AP
      //ammo = 5, fmode = 1
      obj.NextFireMode
      obj.NextAmmoType
      obj.FireModeIndex mustEqual 1
      obj.AmmoTypeIndex mustEqual 5
      obj.AmmoType mustEqual Ammo.plasma_cartridge
      //ammo = 2, fmode = 1
      obj.NextAmmoType
      obj.AmmoTypeIndex mustEqual 2
      obj.AmmoType mustEqual Ammo.rocket
    }

    "projectile types and ammo types" in {
      val suppressor_wep = Tool(suppressor)
      suppressor_wep.ProjectileType mustEqual bullet_9mm_projectile.ProjectileType
      suppressor_wep.NextAmmoType
      suppressor_wep.ProjectileType mustEqual bullet_9mm_AP_projectile.ProjectileType
      suppressor_wep.NextAmmoType
      suppressor_wep.ProjectileType mustEqual bullet_9mm_projectile.ProjectileType
    }

    "projectile types and fire modes" in {
      val pulsar_wep = Tool(pulsar)
      pulsar_wep.ProjectileType mustEqual pulsar_projectile.ProjectileType
      pulsar_wep.NextFireMode
      pulsar_wep.ProjectileType mustEqual pulsar_ap_projectile.ProjectileType
      pulsar_wep.NextFireMode
      pulsar_wep.ProjectileType mustEqual pulsar_projectile.ProjectileType
    }

    "projectile types and fire modes / ammo types" in {
      val punisher_wep = Tool(punisher)
      punisher_wep.ProjectileType mustEqual bullet_9mm_projectile.ProjectileType
      punisher_wep.NextAmmoType
      punisher_wep.ProjectileType mustEqual bullet_9mm_AP_projectile.ProjectileType

      punisher_wep.NextFireMode
      punisher_wep.ProjectileType mustEqual rocket_projectile.ProjectileType
      punisher_wep.NextAmmoType
      punisher_wep.ProjectileType mustEqual frag_cartridge_projectile.ProjectileType
      punisher_wep.NextAmmoType
      punisher_wep.ProjectileType mustEqual jammer_cartridge_projectile.ProjectileType

      punisher_wep.NextFireMode
      punisher_wep.ProjectileType mustEqual bullet_9mm_AP_projectile.ProjectileType
      punisher_wep.NextAmmoType
      punisher_wep.ProjectileType mustEqual bullet_9mm_projectile.ProjectileType

      punisher_wep.NextFireMode
      punisher_wep.ProjectileType mustEqual jammer_cartridge_projectile.ProjectileType
      punisher_wep.NextAmmoType
      punisher_wep.ProjectileType mustEqual plasma_cartridge_projectile.ProjectileType
      punisher_wep.NextAmmoType
      punisher_wep.ProjectileType mustEqual rocket_projectile.ProjectileType
    }

    "discharge (1)" in {
      val obj = Tool(GlobalDefinitions.punisher)
      obj.Magazine mustEqual 30
      obj.Discharge
      obj.Magazine mustEqual 29
      obj.Discharge
      obj.Discharge
      obj.Magazine mustEqual 27
    }

    "chamber" in {
      val obj = Tool(GlobalDefinitions.flechette)
      obj.Magazine mustEqual 12
      obj.AmmoSlot.Chamber mustEqual 8

      obj.Discharge
      obj.Magazine mustEqual 12
      obj.AmmoSlot.Chamber mustEqual 7
      obj.Discharge
      obj.Discharge
      obj.Magazine mustEqual 12
      obj.AmmoSlot.Chamber mustEqual 5
      obj.Discharge
      obj.Discharge
      obj.Discharge
      obj.Discharge
      obj.Magazine mustEqual 12
      obj.AmmoSlot.Chamber mustEqual 1
      obj.Discharge
      obj.Magazine mustEqual 11
      obj.AmmoSlot.Chamber mustEqual 8
    }
  }

  "Kit" should {
    "define" in {
      val sample = KitDefinition(Kits.medkit)
      sample.ObjectId mustEqual medkit.ObjectId
      sample.Tile.Width mustEqual medkit.Tile.Width
      sample.Tile.Height mustEqual medkit.Tile.Height
    }

    "construct" in {
      val obj : Kit = Kit(medkit)
      obj.Definition.ObjectId mustEqual medkit.ObjectId
    }
  }

  "ConstructionItem" should {
    "construct" in {
      val obj : ConstructionItem = ConstructionItem(GlobalDefinitions.ace)
      obj.Definition.ObjectId mustEqual GlobalDefinitions.ace.ObjectId
    }

    "fire modes" in {
      val obj : ConstructionItem = ConstructionItem(GlobalDefinitions.ace)
      obj.AmmoType mustEqual DeployedItem.boomer
      obj.NextFireMode
      obj.AmmoType mustEqual DeployedItem.he_mine
      obj.NextFireMode
      obj.AmmoType mustEqual DeployedItem.spitfire_turret
      obj.NextFireMode
      obj.AmmoType mustEqual DeployedItem.motionalarmsensor
      obj.NextFireMode
      obj.AmmoType mustEqual DeployedItem.boomer
    }

    "ammo types" in {
      val obj : ConstructionItem = ConstructionItem(GlobalDefinitions.ace)
      obj.NextFireMode
      obj.AmmoType mustEqual DeployedItem.he_mine
      obj.NextAmmoType
      obj.AmmoType mustEqual DeployedItem.jammer_mine
      obj.NextAmmoType
      obj.AmmoType mustEqual DeployedItem.he_mine
    }

    "when switching fire modes, ammo mode resets to the first entry" in {
      val obj : ConstructionItem = ConstructionItem(GlobalDefinitions.ace)
      obj.NextFireMode
      obj.AmmoType mustEqual DeployedItem.he_mine
      obj.NextAmmoType
      obj.AmmoType mustEqual DeployedItem.jammer_mine
      obj.NextFireMode //spitfire_turret
      obj.NextFireMode //motionalarmsensor
      obj.NextFireMode //boomer
      obj.NextFireMode
      obj.AmmoType mustEqual DeployedItem.he_mine
    }

    "qualify certifications that must be met before ammo types may be used" in {
      val obj : ConstructionItem = ConstructionItem(GlobalDefinitions.ace)
      obj.AmmoType mustEqual DeployedItem.boomer
      obj.ModePermissions mustEqual Set(CertificationType.CombatEngineering)
      obj.NextFireMode
      obj.AmmoType mustEqual DeployedItem.he_mine
      obj.ModePermissions mustEqual Set(CertificationType.CombatEngineering)
      obj.NextAmmoType
      obj.AmmoType mustEqual DeployedItem.jammer_mine
      obj.ModePermissions mustEqual Set(CertificationType.AssaultEngineering)
    }
  }

  "SimpleItem" should {
    "define" in {
      val sample = SimpleItemDefinition(SItem.remote_electronics_kit)
      sample.ObjectId mustEqual remote_electronics_kit.ObjectId
    }

    "construct" in {
      val obj : SimpleItem = SimpleItem(remote_electronics_kit)
      obj.Definition.ObjectId mustEqual remote_electronics_kit.ObjectId
    }
  }

  "BoomerTrigger" should {
    "construct" in {
      val obj : BoomerTrigger = new BoomerTrigger
      obj.Definition.ObjectId mustEqual boomer_trigger.ObjectId
      obj.Companion mustEqual None
    }

    "boomer trigger has a companion object referenced by GUID" in {
      val obj : BoomerTrigger = new BoomerTrigger
      obj.Companion mustEqual None
      obj.Companion = PlanetSideGUID(1)
      obj.Companion.contains(PlanetSideGUID(1)) mustEqual true
      obj.Companion = None
      obj.Companion mustEqual None
    }
  }
}
