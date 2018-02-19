// Copyright (c) 2017 PSForever

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import net.psforever.packet.control.ControlSync
import net.psforever.packet.game.objectcreate.ObjectClass
import net.psforever.packet.{ControlPacket, GamePacket, PacketCoding}
import net.psforever.packet.game._
import scodec.bits._

import scala.concurrent.duration._

class PacketCodingActor1Test extends ActorTest {
  "PacketCodingActor" should {
    "construct" in {
      system.actorOf(Props[PacketCodingActor], "pca")
      //just construct without failing
    }
  }
}

class PacketCodingActor2Test extends ActorTest {
  "PacketCodingActor" should {
    "initialize (no r-neighbor)" in {
      val pca : ActorRef = system.actorOf(Props[PacketCodingActor], "pca")
      within(200 millis) {
        pca ! HelloFriend(135, List.empty[ActorRef].iterator)
        expectNoMsg
      }
    }
  }
}

class PacketCodingActor3Test extends ActorTest {
  "PacketCodingActor" should {
    "initialize (an r-neighbor)" in {
      val probe1 = TestProbe()
      val probe2 = system.actorOf(Props(classOf[ActorTest.MDCTestProbe], probe1), "mdc-probe")
      val pca : ActorRef = system.actorOf(Props[PacketCodingActor], "pca")
      val iter = List(probe2).iterator
      val msg = HelloFriend(135, iter)

      assert(iter.hasNext)
      pca ! msg
      probe1.expectMsg(msg) //pca will pass message directly; a new HelloFriend would be an unequal different object
      assert(!iter.hasNext)
    }
  }
}

class PacketCodingActor4Test extends ActorTest {
  val string_hex = RawPacket(hex"2A 9F05 D405 86")
  val string_obj = ObjectAttachMessage(PlanetSideGUID(1439), PlanetSideGUID(1492), 6)

  "PacketCodingActor" should {
    "translate r-originating game packet into l-facing hexadecimal data" in {
      val probe1 = TestProbe()
      val probe2 = system.actorOf(Props(classOf[ActorTest.MDCTestProbe], probe1), "mdc-probe")
      val pca : ActorRef = system.actorOf(Props[PacketCodingActor], "pca")
      pca ! HelloFriend(135, List(probe2).iterator)
      probe1.receiveOne(100 milli) //consume

      val msg = ActorTest.MDCGamePacket(PacketCoding.CreateGamePacket(0, string_obj))
      probe2 ! msg
      val reply1 = receiveOne(100 milli) //we get a MdcMsg message back
      probe2 ! reply1 //by feeding the MdcMsg into the actor, we get normal output on the probe
      probe1.expectMsg(string_hex)
    }
  }
}

class PacketCodingActor5Test extends ActorTest {
  val string_hex = RawPacket(hex"2A 9F05 D405 86")
  val string_obj = ObjectAttachMessage(PlanetSideGUID(1439), PlanetSideGUID(1492), 6)

  "PacketCodingActor" should {
    "translate l-originating hexadecimal data into r-facing game packet" in {
      val probe1 = TestProbe()
      val probe2 = system.actorOf(Props(classOf[ActorTest.MDCTestProbe], probe1), "mdc-probe")
      val pca : ActorRef = system.actorOf(Props[PacketCodingActor], "pca")
      pca ! HelloFriend(135, List(probe2).iterator)
      probe1.receiveOne(100 milli) //consume

      pca ! string_hex
      val reply = probe1.receiveOne(100 milli)
      reply match {
        case GamePacket(_, _, msg) => ;
          assert(msg == string_obj)
        case _ =>
          assert(false)
      }
    }
  }
}

class PacketCodingActor6Test extends ActorTest {
  val string_obj = ObjectAttachMessage(PlanetSideGUID(1439), PlanetSideGUID(1492), 6)

  "PacketCodingActor" should {
    "permit l-originating game packet to pass through as an r-facing game packet" in {
      val probe1 = TestProbe()
      val probe2 = system.actorOf(Props(classOf[ActorTest.MDCTestProbe], probe1), "mdc-probe")
      val pca : ActorRef = system.actorOf(Props[PacketCodingActor], "pca")
      pca ! HelloFriend(135, List(probe2).iterator)
      probe1.receiveOne(100 milli) //consume

      val msg = PacketCoding.CreateGamePacket(0, string_obj)
      pca ! msg
      probe1.expectMsg(msg)
    }
  }
}

class PacketCodingActor7Test extends ActorTest {
  val string_hex = RawPacket(hex"0007 5268 0000004D 00000052 0000004D 0000007C 0000004D 0000000000000276 0000000000000275")
  val string_obj = ControlSync(21096, 0x4d, 0x52, 0x4d, 0x7c, 0x4d, 0x276, 0x275)

  "PacketCodingActor" should {
    "translate r-originating control packet into l-facing hexadecimal data" in {
      val probe1 = TestProbe()
      val probe2 = system.actorOf(Props(classOf[ActorTest.MDCTestProbe], probe1), "mdc-probe")
      val pca : ActorRef = system.actorOf(Props[PacketCodingActor], "pca")
      pca ! HelloFriend(135, List(probe2).iterator)
      probe1.receiveOne(100 milli) //consume

      val msg = ActorTest.MDCControlPacket(PacketCoding.CreateControlPacket(string_obj))
      probe2 ! msg
      val reply1 = receiveOne(100 milli) //we get a MdcMsg message back
      probe2 ! reply1 //by feeding the MdcMsg into the actor, we get normal output on the probe
      probe1.expectMsg(string_hex)
    }
  }
}

class PacketCodingActor8Test extends ActorTest {
  val string_hex = RawPacket(hex"0007 5268 0000004D 00000052 0000004D 0000007C 0000004D 0000000000000276 0000000000000275")
  val string_obj = ControlSync(21096, 0x4d, 0x52, 0x4d, 0x7c, 0x4d, 0x276, 0x275)

  "PacketCodingActor" should {
    "translate l-originating hexadecimal data into r-facing control packet" in {
      val probe1 = TestProbe()
      val probe2 = system.actorOf(Props(classOf[ActorTest.MDCTestProbe], probe1), "mdc-probe")
      val pca : ActorRef = system.actorOf(Props[PacketCodingActor], "pca")
      pca ! HelloFriend(135, List(probe2).iterator)
      probe1.receiveOne(100 milli) //consume

      pca ! string_hex
      val reply = probe1.receiveOne(100 milli)
      reply match {
        case ControlPacket(_, msg) => ;
          assert(msg == string_obj)
        case _ =>
          assert(false)
      }
    }
  }
}

class PacketCodingActor9Test extends ActorTest {
  val string_obj = ControlSync(21096, 0x4d, 0x52, 0x4d, 0x7c, 0x4d, 0x276, 0x275)

  "PacketCodingActor" should {
    "permit l-originating control packet to pass through as an r-facing control packet" in {
      val probe1 = TestProbe()
      val probe2 = system.actorOf(Props(classOf[ActorTest.MDCTestProbe], probe1), "mdc-probe")
      val pca : ActorRef = system.actorOf(Props[PacketCodingActor], "pca")
      pca ! HelloFriend(135, List(probe2).iterator)
      probe1.receiveOne(100 milli) //consume

      val msg = PacketCoding.CreateControlPacket(string_obj)
      pca ! msg
      probe1.expectMsg(msg)
    }
  }
}

class PacketCodingActorATest extends ActorTest {
  "PacketCodingActor" should {
    "permit l-originating unhandled message to pass through as an r-facing unhandled message" in {
      val probe1 = TestProbe()
      val probe2 = system.actorOf(Props(classOf[ActorTest.MDCTestProbe], probe1), "mdc-probe")
      val pca : ActorRef = system.actorOf(Props[PacketCodingActor], "pca")
      pca ! HelloFriend(135, List(probe2).iterator)
      probe1.receiveOne(100 milli) //consume

      pca ! "unhandled message"
      probe1.expectMsg("unhandled message")
    }
  }
}

class PacketCodingActorBTest extends ActorTest {
  "PacketCodingActor" should {
    "permit r-originating unhandled message to pass through as an l-facing unhandled message" in {
      val probe1 = TestProbe()
      val probe2 = system.actorOf(Props(classOf[ActorTest.MDCTestProbe], probe1), "mdc-probe")
      val pca : ActorRef = system.actorOf(Props[PacketCodingActor], "pca")
      pca ! HelloFriend(135, List(probe2).iterator)
      probe1.receiveOne(100 milli) //consume

      probe2 ! "unhandled message"
      val reply1 = receiveOne(100 milli) //we get a MdcMsg message back
      probe2 ! reply1 //by feeding the MdcMsg into the actor, we get normal output on the probe
      probe1.expectMsg("unhandled message")
    }
  }
}

class PacketCodingActorCTest extends ActorTest {
  val string_hex = hex"D5 0B 00 00 00 01 0A E4  0C 02 48 70 75 72 63 68 61 73 65 5F 65 78 65 6D  70 74 5F 76 73 80 92 70 75 72 63 68 61 73 65 5F  65 78 65 6D 70 74 5F 74 72 80 92 70 75 72 63 68  61 73 65 5F 65 78 65 6D 70 74 5F 6E 63 80 11 00  01 14 A4 04 02 1C 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 12 00 01 14 A4 04 02 1C 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 13 00 01 14 A4 04 02 1C  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 14 00 01  14 A4 04 02 1C 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 15 00 01 14 A4 04 02 1C 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 16 00 01 14 A4 04 02 1C 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 1D 00 15 0A  60 04 02 1C 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 54 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 76 00 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 87 00 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 C7 00 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 C8 00 20 10 E0 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 26 20 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 52 20 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 AD  20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 B0 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 B9 20 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 CE 20 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 D6 20 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 2C 40 20 10 E0 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 82 40 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 83 40 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 B9  40 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 CA 40 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 61 60 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 9B 60 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 DA 60 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 1E 00 15 0A 60 04 02  1C 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 54 00  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  76 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 87 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 C7 00 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 C8 00 20 10 E0 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 26 20 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 52 20 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 AD 20 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 B0 20  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  B9 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 CE 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 D6 20 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 2C 40 20 10 E0 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 82 40 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 83 40 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 B9 40 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 CA 40  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  61 60 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 9B 60 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 DA 60 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 1F 00 15 0A 60 04 02 1C 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 54 00 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 76 00 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 87  00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 C7 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 C8 00 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 26 20 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 52 20 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 AD 20 20 10 E0 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 B0 20 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 B9 20 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 CE  20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 D6 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 2C 40 20 10 E0 61 6C 6C 6F 77 65 64 85  66 61 6C 73 65 82 40 20 10 E0 61 6C 6C 6F 77 65  64 85 66 61 6C 73 65 83 40 20 10 E0 61 6C 6C 6F  77 65 64 85 66 61 6C 73 65 B9 40 20 10 E0 61 6C  6C 6F 77 65 64 85 66 61 6C 73 65 CA 40 20 10 E0  61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 61 60 20  10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 9B  60 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73  65 DA 60 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61  6C 73 65 20 00 15 0A 60 04 02 1C 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 54 00 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 76 00 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 87 00 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 C7 00  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  C8 00 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 26 20 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 52 20 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 AD 20 20 10 E0 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 B0 20 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 B9 20 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 CE 20 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 D6 20  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65  2C 40 20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C  73 65 82 40 20 10 E0 61 6C 6C 6F 77 65 64 85 66  61 6C 73 65 83 40 20 10 E0 61 6C 6C 6F 77 65 64  85 66 61 6C 73 65 B9 40 20 10 E0 61 6C 6C 6F 77  65 64 85 66 61 6C 73 65 CA 40 20 10 E0 61 6C 6C  6F 77 65 64 85 66 61 6C 73 65 61 60 20 10 E0 61  6C 6C 6F 77 65 64 85 66 61 6C 73 65 9B 60 20 10  E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65 DA 60  20 10 E0 61 6C 6C 6F 77 65 64 85 66 61 6C 73 65"

  "PacketCodingActor" should {
    "should split r-originating hexadecimal data if it is larger than the MTU limit" in {
      val probe1 = TestProbe()
      val probe2 = system.actorOf(Props(classOf[ActorTest.MDCTestProbe], probe1), "mdc-probe")
      val pca : ActorRef = system.actorOf(Props[PacketCodingActor], "pca")
      pca ! HelloFriend(135, List(probe2).iterator)
      probe1.receiveOne(100 milli) //consume

      val msg = RawPacket(string_hex)
      probe2 ! msg
      receiveN(4)
      //msg4.foreach(str => { probe2 ! str })
      //probe1.receiveN(4)
    }
  }
}

class PacketCodingActorDTest extends ActorTest {
  val string_obj = PropertyOverrideMessage(
    List(
      GamePropertyScope(0,
        GamePropertyTarget(GamePropertyTarget.game_properties, List(
          "purchase_exempt_vs" -> "",
          "purchase_exempt_tr" -> "",
          "purchase_exempt_nc" -> ""
        )
        )),
      GamePropertyScope(17,
        GamePropertyTarget(ObjectClass.katana, "allowed" -> "false")
      ),
      GamePropertyScope(18,
        GamePropertyTarget(ObjectClass.katana, "allowed" -> "false")
      ),
      GamePropertyScope(19,
        GamePropertyTarget(ObjectClass.katana, "allowed" -> "false")
      ),
      GamePropertyScope(20,
        GamePropertyTarget(ObjectClass.katana, "allowed" -> "false")
      ),
      GamePropertyScope(21,
        GamePropertyTarget(ObjectClass.katana, "allowed" -> "false")
      ),
      GamePropertyScope(22,
        GamePropertyTarget(ObjectClass.katana, "allowed" -> "false")
      ),
      GamePropertyScope(29, List(
        GamePropertyTarget(ObjectClass.aphelion_flight, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.aphelion_gunner, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.aurora, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.battlewagon, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.colossus_flight, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.colossus_gunner, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.flail, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.galaxy_gunship, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.lasher, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.liberator, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.lightgunship, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.maelstrom, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.magrider, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.mini_chaingun, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.peregrine_flight, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.peregrine_gunner, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.prowler, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.r_shotgun, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.thunderer, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.vanguard, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.vulture, "allowed" -> "false")
      )),
      GamePropertyScope(30, List(
        GamePropertyTarget(ObjectClass.aphelion_flight, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.aphelion_gunner, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.aurora, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.battlewagon, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.colossus_flight, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.colossus_gunner, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.flail, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.galaxy_gunship, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.lasher, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.liberator, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.lightgunship, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.maelstrom, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.magrider, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.mini_chaingun, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.peregrine_flight, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.peregrine_gunner, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.prowler, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.r_shotgun, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.thunderer, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.vanguard, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.vulture, "allowed" -> "false")
      )),
      GamePropertyScope(31, List(
        GamePropertyTarget(ObjectClass.aphelion_flight, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.aphelion_gunner, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.aurora, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.battlewagon, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.colossus_flight, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.colossus_gunner, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.flail, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.galaxy_gunship, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.lasher, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.liberator, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.lightgunship, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.maelstrom, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.magrider, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.mini_chaingun, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.peregrine_flight, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.peregrine_gunner, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.prowler, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.r_shotgun, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.thunderer, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.vanguard, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.vulture, "allowed" -> "false")
      )),
      GamePropertyScope(32, List(
        GamePropertyTarget(ObjectClass.aphelion_flight, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.aphelion_gunner, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.aurora, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.battlewagon, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.colossus_flight, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.colossus_gunner, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.flail, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.galaxy_gunship, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.lasher, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.liberator, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.lightgunship, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.maelstrom, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.magrider, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.mini_chaingun, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.peregrine_flight, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.peregrine_gunner, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.prowler, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.r_shotgun, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.thunderer, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.vanguard, "allowed" -> "false"),
        GamePropertyTarget(ObjectClass.vulture, "allowed" -> "false")
      ))
    )
  )

  "PacketCodingActor" should {
    "should split r-originating game packet if it is larger than the MTU limit" in {
      val probe1 = TestProbe()
      val probe2 = system.actorOf(Props(classOf[ActorTest.MDCTestProbe], probe1), "mdc-probe")
      val pca : ActorRef = system.actorOf(Props[PacketCodingActor], "pca")
      pca ! HelloFriend(135, List(probe2).iterator)
      probe1.receiveOne(100 milli) //consume

      val msg = ActorTest.MDCGamePacket(PacketCoding.CreateGamePacket(0, string_obj))
      probe2 ! msg
      receiveN(4)
    }
  }
}

object PacketCodingActorTest {
  //decoy
}
