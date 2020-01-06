// Copyright (c) 2017 PSForever
package objects.number

import akka.actor.Actor
import net.psforever.objects.guid.actor.Register
import org.specs2.mutable.Specification

class RegisterTest extends Specification {
  val obj = new net.psforever.objects.entity.IdentifiableEntity() {}

  "Register" should {
    "construct (object)" in {
      val reg = Register(obj)
      reg.obj mustEqual obj
      reg.number.isEmpty mustEqual true
      reg.name.isEmpty mustEqual true
      reg.callback.isEmpty mustEqual true
    }

    "construct (object, callback)" in {
      val reg = Register(obj, Actor.noSender)
      reg.obj mustEqual obj
      reg.number.isEmpty mustEqual true
      reg.name.isEmpty mustEqual true
      reg.callback.contains(Actor.noSender) mustEqual true
    }

    "construct (object, suggested number)" in {
      val reg = Register(obj, 5)
      reg.obj mustEqual obj
      reg.number.contains(5) mustEqual true
      reg.name.isEmpty mustEqual true
      reg.callback.isEmpty mustEqual true
    }

    "construct (object, suggested number, callback)" in {
      val reg = Register(obj, 5, Actor.noSender)
      reg.obj mustEqual obj
      reg.number.contains(5) mustEqual true
      reg.name.isEmpty mustEqual true
      reg.callback.contains(Actor.noSender) mustEqual true
    }

    "construct (object, pool name)" in {
      val reg = Register(obj, "pool")
      reg.obj mustEqual obj
      reg.number.isEmpty mustEqual true
      reg.name.contains("pool") mustEqual true
      reg.callback.isEmpty mustEqual true
    }

    "construct (object, pool name, callback)" in {
      val reg = Register(obj, "pool", Actor.noSender)
      reg.obj mustEqual obj
      reg.number.isEmpty mustEqual true
      reg.name.contains("pool") mustEqual true
      reg.callback.contains(Actor.noSender) mustEqual true
    }
  }
}
