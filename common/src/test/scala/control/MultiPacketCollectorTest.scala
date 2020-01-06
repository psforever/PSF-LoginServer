// Copyright (c) 2017 PSForever
package control

import org.specs2.mutable._
import net.psforever.packet.control.{ControlSync, MultiPacketBundle, MultiPacketCollector}
import net.psforever.packet.crypto.{ClientFinished, ServerFinished}
import net.psforever.packet.game.ObjectDeleteMessage
import net.psforever.types.PlanetSideGUID

class MultiPacketCollectorTest extends Specification {
  val packet1 = ObjectDeleteMessage(PlanetSideGUID(1103), 2)

  "MultiPacketBundle" should {
    import scodec.bits._
    val packet2 = ControlSync(21096, 0x4d, 0x52, 0x4d, 0x7c, 0x4d, 0x276, 0x275)

    "construct" in {
      MultiPacketBundle(List(packet1))
      ok
    }

    "fail to construct if not initialized with PlanetSidePackets" in {
      MultiPacketBundle(Nil) must throwA[IllegalArgumentException]
    }

    "concatenate bundles into a new bundle" in {
      val obj1 = MultiPacketBundle(List(packet1))
      val obj2 = MultiPacketBundle(List(packet2))
      val obj3 = obj1 + obj2
      obj3 match {
        case MultiPacketBundle(list) =>
          list.size mustEqual 2
          list.head mustEqual packet1
          list(1) mustEqual packet2
        case _ =>
          ko
      }
    }

    "accept PlanetSideGamePackets and PlanetSideControlPackets" in {
      MultiPacketBundle(List(packet2, packet1)) match {
        case MultiPacketBundle(list) =>
          list.size mustEqual 2
          list.head mustEqual packet2
          list(1) mustEqual packet1
        case _ =>
          ko
      }
    }

    "ignore other types of PlanetSideContainerPackets" in {
      val param = List(packet2, ClientFinished(hex"", hex""), packet1, ServerFinished(hex""))
      MultiPacketBundle(param) match { //warning message will display in log
        case MultiPacketBundle(list) =>
          list.size mustEqual 2
          list.head mustEqual param.head
          list(1) mustEqual param(2)
        case _ =>
          ko
      }
    }
  }

  "MultiPacketCollector" should {
    val packet2 = ObjectDeleteMessage(PlanetSideGUID(1105), 2)
    val packet3 = ObjectDeleteMessage(PlanetSideGUID(1107), 2)

    "construct" in {
      new MultiPacketCollector()
      ok
    }

    "construct with initial packets" in {
      MultiPacketCollector(List(packet1, packet2))
      ok
    }

    "can retrieve a bundle packets" in {
      val obj = MultiPacketCollector(List(packet1, packet2))
      obj.Bundle match {
        case MultiPacketBundle(list) =>
          list.size mustEqual 2
          list.head mustEqual packet1
          list(1) mustEqual packet2
        case _ =>
          ko
      }
    }

    "can not retrieve a bundle of non-existent packets" in {
      val obj = new MultiPacketCollector()
      obj.Bundle must throwA[RuntimeException]
    }

    "can safely retrieve a bundle of potential packets" in {
      val obj1 = new MultiPacketCollector()
      obj1.BundleOption match {
        case Some(_) =>
          ko
        case _ => ;
      }

      val obj2 = MultiPacketCollector(List(packet1, packet2))
      obj2.BundleOption match {
        case None =>
          ko
        case Some(MultiPacketBundle(list)) =>
          list.size mustEqual 2
          list.head mustEqual packet1
          list(1) mustEqual packet2
      }
    }

    "clear packets after being asked to bundle" in {
      val list = List(packet1, packet2)
      val obj = MultiPacketCollector(list)
      obj.Bundle mustEqual MultiPacketBundle(list)
      obj.Bundle must throwA[RuntimeException]
    }

    "add a packet" in {
      val obj = new MultiPacketCollector()
      obj.Add(packet1)
      obj.Bundle match {
        case MultiPacketBundle(list) =>
          list.size mustEqual 1
          list.head mustEqual packet1
        case _ =>
          ko
      }
    }

    "add packets" in {
      val obj = new MultiPacketCollector()
      obj.Add(List(packet1, packet2))
      obj.Bundle match {
        case MultiPacketBundle(list) =>
          list.size mustEqual 2
          list.head mustEqual packet1
          list(1) mustEqual packet2
        case _ =>
          ko
      }
    }

    "concatenate bundles (1)" in {

      val obj1 = new MultiPacketCollector()
      obj1.Add(List(packet1, packet2))
      val bundle1 = obj1.Bundle

      val obj2 = MultiPacketCollector(bundle1)
      obj2.Add(packet3)
      obj2.Bundle match {
        case MultiPacketBundle(list) =>
          list.size mustEqual 3
          list.head mustEqual packet1
          list(1) mustEqual packet2
          list(2) mustEqual packet3
        case _ =>
          ko
      }
    }

    "concatenate bundles (2)" in {
      val obj1 = new MultiPacketCollector()
      obj1.Add(List(packet1, packet2))
      val bundle1 = obj1.Bundle

      val obj2 = new MultiPacketCollector()
      obj2.Add(packet3)
      obj2.Add(bundle1)
      obj2.Bundle match {
        case MultiPacketBundle(list) =>
          list.size mustEqual 3
          list.head mustEqual packet3
          list(1) mustEqual packet1
          list(2) mustEqual packet2
        case _ =>
          ko
      }
    }
  }
}
