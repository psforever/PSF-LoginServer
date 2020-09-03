// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.definition.ToolDefinition
import net.psforever.objects.{GlobalDefinitions, Tool}
import net.psforever.objects.equipment._
import org.specs2.mutable._

class FireModeTest extends Specification {
  "FireModeDefinition" should {
    "construct" in {
      val obj = new FireModeDefinition
      obj.AmmoTypeIndices mustEqual Nil
      obj.AmmoSlotIndex mustEqual 0
      obj.Magazine mustEqual 1
      obj.RoundsPerShot mustEqual 1
      obj.Chamber mustEqual 1
    }

    "test configurations" in {
      val tdef = ToolDefinition(1076) //fake object id
      tdef.Size = EquipmentSize.Rifle
      tdef.AmmoTypes += GlobalDefinitions.bullet_9mm
      tdef.AmmoTypes += GlobalDefinitions.shotgun_shell
      tdef.FireModes += new FireModeDefinition
      tdef.FireModes.head.AmmoTypeIndices += 0
      tdef.FireModes.head.AmmoSlotIndex = 0
      tdef.FireModes.head.Magazine = 18
      tdef.FireModes.head.RoundsPerShot = 18
      tdef.FireModes.head.Chamber = 2
      tdef.FireModes += new FireModeDefinition
      tdef.FireModes(1).AmmoTypeIndices += 1
      tdef.FireModes(1).AmmoTypeIndices += 2
      tdef.FireModes(1).AmmoSlotIndex = 1
      tdef.FireModes(1).Magazine = 9
      tdef.FireModes(1).RoundsPerShot = 2
      tdef.FireModes(1).Chamber = 8

      tdef.AmmoTypes.toList mustEqual List(GlobalDefinitions.bullet_9mm, GlobalDefinitions.shotgun_shell)
      tdef.FireModes.size mustEqual 2
      tdef.FireModes.head.AmmoTypeIndices.toList mustEqual List(0)
      tdef.FireModes.head.AmmoSlotIndex mustEqual 0
      tdef.FireModes.head.Magazine mustEqual 18
      tdef.FireModes.head.RoundsPerShot mustEqual 18
      tdef.FireModes.head.Chamber mustEqual 2
      tdef.FireModes(1).AmmoTypeIndices.toList mustEqual List(1, 2)
      tdef.FireModes(1).AmmoSlotIndex mustEqual 1
      tdef.FireModes(1).Magazine mustEqual 9
      tdef.FireModes(1).RoundsPerShot mustEqual 2
      tdef.FireModes(1).Chamber mustEqual 8
    }

    "discharge" in {
      val obj = Tool(GlobalDefinitions.beamer) //see EquipmentTest
      obj.FireMode.isInstanceOf[FireModeDefinition] mustEqual true
      obj.Magazine mustEqual 16
      obj.FireMode.RoundsPerShot mustEqual 1
      obj.FireMode.Chamber mustEqual 1

      obj.Magazine mustEqual 16
      obj.Discharge()
      obj.Magazine mustEqual 15
      obj.Discharge()
      obj.Discharge()
      obj.Magazine mustEqual 13
    }
  }

  "PelletFireModeDefinition" should {
    "construct" in {
      val obj = new PelletFireModeDefinition
      obj.AmmoTypeIndices mustEqual Nil
      obj.AmmoSlotIndex mustEqual 0
      obj.Magazine mustEqual 1
      obj.RoundsPerShot mustEqual 1
      obj.Chamber mustEqual 1
    }

    "discharge" in {
      val obj = Tool(GlobalDefinitions.flechette) //see EquipmentTest
      obj.FireMode.isInstanceOf[PelletFireModeDefinition] mustEqual true
      obj.Magazine mustEqual 12
      obj.FireMode.RoundsPerShot mustEqual 1
      obj.FireMode.Chamber mustEqual 8

      obj.Magazine mustEqual 12
      obj.Discharge() //1
      obj.Magazine mustEqual 12
      obj.Discharge() //2
      obj.Discharge() //3
      obj.Magazine mustEqual 12
      obj.Discharge() //4
      obj.Discharge() //5
      obj.Discharge() //6
      obj.Discharge() //7
      obj.Magazine mustEqual 12
      obj.Discharge() //8
      obj.Magazine mustEqual 11
    }
  }

  "InfiniteFireModeDefinition" should {
    "construct" in {
      val obj = new InfiniteFireModeDefinition
      obj.AmmoTypeIndices mustEqual Nil
      obj.AmmoSlotIndex mustEqual 0
      obj.Magazine mustEqual 1
      obj.RoundsPerShot mustEqual 1
      obj.Chamber mustEqual 1
    }

    "discharge" in {
      val obj = Tool(GlobalDefinitions.magcutter) //see EquipmentTest
      obj.FireMode.isInstanceOf[InfiniteFireModeDefinition] mustEqual true
      obj.Magazine mustEqual 1
      obj.FireMode.RoundsPerShot mustEqual 1
      obj.FireMode.Chamber mustEqual 1

      obj.Magazine mustEqual 1
      obj.Discharge()
      obj.Magazine mustEqual 1
      obj.Discharge()
      obj.Discharge()
      obj.Magazine mustEqual 1
    }
  }

  "ChargeFireModeDefinition" should {
    "construct" in {
      val obj = new ChargeFireModeDefinition(1000, 500)
      obj.AmmoTypeIndices mustEqual Nil
      obj.AmmoSlotIndex mustEqual 0
      obj.Magazine mustEqual 1
      obj.RoundsPerShot mustEqual 1
      obj.Chamber mustEqual 1
      obj.Time mustEqual 1000L
      obj.DrainInterval mustEqual 500L
    }

    "discharge" in {
      val obj = Tool(GlobalDefinitions.spiker)
      obj.FireMode.isInstanceOf[ChargeFireModeDefinition] mustEqual true
      obj.Magazine mustEqual 25
      obj.FireMode.RoundsPerShot mustEqual 1
      obj.FireMode.Chamber mustEqual 1

      obj.Magazine mustEqual 25
      obj.Discharge()
      obj.Magazine mustEqual 24
      obj.Discharge()
      obj.Discharge()
      obj.Magazine mustEqual 22
    }
  }
}
