// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.Implant
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

  "construct" in {
    val obj = new Implant(sample)
    obj.Definition.Type mustEqual sample.Type
    obj.Active mustEqual false
    obj.Ready mustEqual false
    obj.Timer mustEqual 0
  }

  "reset/init their timer" in {
    val obj = new Implant(sample)
    obj.Timer mustEqual 0
    obj.Reset()
    obj.Timer mustEqual 90000
  }

  "reset/init their readiness condition" in {
    val obj = new Implant(sample)
    obj.Ready mustEqual false
    obj.Timer = 0
    obj.Ready mustEqual true
    obj.Reset()
    obj.Ready mustEqual false
  }

  "not activate until they are ready" in {
    val obj = new Implant(sample)
    obj.Active = true
    obj.Active mustEqual false
    obj.Timer = 0
    obj.Active = true
    obj.Active mustEqual true
  }

  "not cost energy while not active" in {
    val obj = new Implant(sample)
    obj.Charge(ExoSuitType.Reinforced, Stance.Running) mustEqual 0
  }

  "cost energy while active" in {
    val obj = new Implant(sample)
    obj.Timer = 0
    obj.Active = true
    obj.Charge(ExoSuitType.Reinforced, Stance.Running) mustEqual 4
  }
}
