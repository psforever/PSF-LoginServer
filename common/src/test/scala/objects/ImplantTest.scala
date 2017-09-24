// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.ImplantSlot
import net.psforever.objects.definition.{ImplantDefinition, Stance}
import net.psforever.types.{ExoSuitType, ImplantType}
import org.specs2.mutable._

class ImplantTest extends Specification {
  val sample = new ImplantDefinition(8) //variant of sensor shield/silent run
      sample.Initialization = 90000 //1:30
      sample.ActivationCharge = 3
      sample.DurationChargeBase = 1
      sample.DurationChargeByExoSuit += ExoSuitType.Agile -> 2
      sample.DurationChargeByExoSuit += ExoSuitType.Reinforced -> 2
      sample.DurationChargeByExoSuit += ExoSuitType.Standard -> 1
      sample.DurationChargeByStance += Stance.Running -> 1

  "ImplantDefinition" should {
    "define" in {
      sample.Initialization mustEqual 90000
      sample.ActivationCharge mustEqual 3
      sample.DurationChargeBase mustEqual 1
      sample.DurationChargeByExoSuit(ExoSuitType.Agile) mustEqual 2
      sample.DurationChargeByExoSuit(ExoSuitType.Reinforced) mustEqual 2
      sample.DurationChargeByExoSuit(ExoSuitType.Standard) mustEqual 1
      sample.DurationChargeByExoSuit(ExoSuitType.Infiltration) mustEqual 0 //default value
      sample.DurationChargeByStance(Stance.Running) mustEqual 1
      sample.DurationChargeByStance(Stance.Crouching) mustEqual 0 //default value
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

    "initialize without an implant" in {
      val obj = new ImplantSlot
      obj.Initialized mustEqual false
      obj.Initialized = true
      obj.Initialized mustEqual false
    }

    "initialize an implant" in {
      val obj = new ImplantSlot
      obj.Initialized mustEqual false

      obj.Unlocked = true
      obj.Implant = sample
      obj.Initialized = true
      obj.Initialized mustEqual true
    }

    "activate an uninitialized implant" in {
      val obj = new ImplantSlot
      obj.Unlocked = true
      obj.Implant = sample
      obj.Initialized mustEqual false
      obj.Active mustEqual false

      obj.Active = true
      obj.Active mustEqual false
    }

    "activate an initialized implant" in {
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
      obj.Charge(ExoSuitType.Reinforced, Stance.Running) mustEqual 0
    }

    "cost energy while active" in {
      val obj = new ImplantSlot
      obj.Unlocked = true
      obj.Implant = sample
      obj.Initialized = true
      obj.Active = true
      obj.Active mustEqual true
      obj.ActivationCharge mustEqual 3
      obj.Charge(ExoSuitType.Reinforced, Stance.Running) mustEqual 4
    }
  }
}
