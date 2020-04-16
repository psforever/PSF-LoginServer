// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.ImplantSlot
import net.psforever.objects.definition.ImplantDefinition
import net.psforever.types.{ExoSuitType, ImplantType}
import org.specs2.mutable._

class ImplantTest extends Specification {
  val sample = new ImplantDefinition(8) //variant of sensor shield/silent run
      sample.InitializationDuration = 90 //1:30
      sample.ActivationStaminaCost = 3
      sample.StaminaCost = 1
      sample.CostIntervalDefault = 1000
      sample.CostIntervalByExoSuitHashMap += ExoSuitType.Agile -> 500

  "ImplantDefinition" should {
    "define" in {
      sample.InitializationDuration mustEqual 90
      sample.ActivationStaminaCost mustEqual 3
      sample.StaminaCost mustEqual 1
      sample.GetCostIntervalByExoSuit(ExoSuitType.Reinforced) mustEqual 1000 // Default value
      sample.GetCostIntervalByExoSuit(ExoSuitType.Agile) mustEqual 500 // Overridden value
      sample.Type mustEqual ImplantType.SilentRun
    }
  }

  "ImplantSlot" should {
    "construct" in {
      val obj = new ImplantSlot
      obj.Unlocked mustEqual false
      obj.Initialized mustEqual false
      obj.Active mustEqual false
      obj.Implant mustEqual ImplantType.None
      obj.Installed mustEqual None
    }

    "load an implant when locked" in {
      val obj = new ImplantSlot
      obj.Unlocked mustEqual false
      obj.Implant mustEqual ImplantType.None

      obj.Implant = sample
      obj.Implant mustEqual ImplantType.None
    }

    "load an implant when unlocked" in {
      val obj = new ImplantSlot
      obj.Unlocked mustEqual false
      obj.Implant mustEqual ImplantType.None
      sample.Type mustEqual ImplantType.SilentRun

      obj.Unlocked = true
      obj.Implant = sample
      obj.Implant mustEqual ImplantType.SilentRun
    }

    "can not re-lock an unlocked implant slot" in {
      val obj = new ImplantSlot
      obj.Unlocked mustEqual false

      obj.Unlocked = false
      obj.Unlocked mustEqual false
      obj.Unlocked = true
      obj.Unlocked mustEqual true
      obj.Unlocked = false
      obj.Unlocked mustEqual true
    }

    "can not initialize without an implant" in {
      val obj = new ImplantSlot
      obj.Initialized mustEqual false
      obj.Initialized = true
      obj.Initialized mustEqual false
    }

    "can initialize an implant" in {
      val obj = new ImplantSlot
      obj.Initialized mustEqual false

      obj.Unlocked = true
      obj.Implant = sample
      obj.Initialized = true
      obj.Initialized mustEqual true
    }

    "can not activate an uninitialized implant" in {
      val obj = new ImplantSlot
      obj.Unlocked = true
      obj.Implant = sample
      obj.Initialized mustEqual false
      obj.Active mustEqual false

      obj.Active = true
      obj.Active mustEqual false
    }

    "can activate an initialized implant" in {
      val obj = new ImplantSlot
      obj.Unlocked = true
      obj.Implant = sample
      obj.Initialized mustEqual false
      obj.Active mustEqual false

      obj.Initialized = true
      obj.Active = true
      obj.Active mustEqual true
    }

    "not cost energy while not active" in {
      val obj = new ImplantSlot
      obj.Unlocked = true
      obj.Implant = sample
      obj.Initialized = true
      obj.Active mustEqual false
      obj.ActivationCharge mustEqual 0
      obj.Charge(ExoSuitType.Reinforced) mustEqual 0
    }

    "cost energy while active" in {
      val obj = new ImplantSlot
      obj.Unlocked = true
      obj.Implant = sample
      obj.Initialized = true
      obj.Active = true
      obj.Active mustEqual true
      obj.ActivationCharge mustEqual 3
      obj.Charge(ExoSuitType.Reinforced) mustEqual 1
    }
  }
}
