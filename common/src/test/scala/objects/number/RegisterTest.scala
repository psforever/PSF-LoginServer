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
      reg.number mustEqual None
      reg.name mustEqual None
      reg.callback mustEqual None
    }

    "construct (object, callback)" in {
      val reg = Register(obj, Actor.noSender)
      reg.obj mustEqual obj
      reg.number mustEqual None
      reg.name mustEqual None
      reg.callback mustEqual Some(Actor.noSender)
    }

    "construct (object, suggested number)" in {
      val reg = Register(obj, 5)
      reg.obj mustEqual obj
      reg.number mustEqual Some(5)
      reg.name mustEqual None
      reg.callback mustEqual None
    }

    "construct (object, suggested number, callback)" in {
      val reg = Register(obj, 5, Actor.noSender)
      reg.obj mustEqual obj
      reg.number mustEqual Some(5)
      reg.name mustEqual None
      reg.callback mustEqual Some(Actor.noSender)
    }

    "construct (object, pool name)" in {
      val reg = Register(obj, "pool")
      reg.obj mustEqual obj
      reg.number mustEqual None
      reg.name mustEqual Some("pool")
      reg.callback mustEqual None
    }

    "construct (object, pool name, callback)" in {
      val reg = Register(obj, "pool", Actor.noSender)
      reg.obj mustEqual obj
      reg.number mustEqual None
      reg.name mustEqual Some("pool")
      reg.callback mustEqual Some(Actor.noSender)
    }
  }
}
