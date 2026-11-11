// Copyright (c) 2026 PSForever
package net.psforever.packet

import org.specs2.mutable._
import scodec.bits._

import scala.util.Random

class FastBitsTest extends Specification {
  /** A small leaf in one of the shapes scodec builds: whole bytes, a prefix, a suffix, or deferred. */
  private def leaf(r: Random): BitVector = {
    val bytes = new Array[Byte](r.nextInt(6))
    r.nextBytes(bytes)
    val whole = BitVector(bytes)
    val bits  = whole.size.toInt
    r.nextInt(5) match {
      case 0 => whole
      case 1 => whole.take(r.nextInt(bits + 1).toLong)
      case 2 => whole.drop(r.nextInt(bits + 1).toLong)
      case 3 =>
        // unfold builds lazily evaluated (Suspend) nodes
        val dropped = whole.drop(r.nextInt(bits + 1).toLong)
        BitVector.unfold(0)(i => if (i < 2) Some((dropped, i + 1)) else None)
      case _ => BitVector.fromInt(r.nextInt(), 1 + r.nextInt(32), ByteOrdering.LittleEndian)
    }
  }

  /** A random tree, mixing concatenation with the slicing and buffering an encoder does. */
  private def tree(r: Random, depth: Int): BitVector = {
    if (depth == 0 || r.nextInt(5) == 0) leaf(r)
    else {
      val joined = tree(r, depth - 1) ++ tree(r, depth - 1)
      val size   = joined.size.toInt
      r.nextInt(5) match {
        case 0 => joined.drop(r.nextInt(size + 1).toLong)
        case 1 => joined.take(r.nextInt(size + 1).toLong)
        case 2 => BitVector.unfold(0)(i => if (i == 0) Some((joined, 1)) else None) ++ leaf(r)
        case _ => joined
      }
    }
  }

  "FastBits" should {
    "match scodec on random trees, without falling back" in {
      val r = new Random(0x50465346L)
      val trees = (1 to 5000).map(i => tree(r, 1 + i % 10))
      forall(trees) { v =>
        (FastBits.toByteArray(v) != null) must beTrue
        FastBits.compact(v) mustEqual v
        FastBits.toByteVector(v) mustEqual v.toByteVector
      }
    }

    "match scodec on encoded packets" in {
      // from PlayerStateMessageTest, BuildingInfoUpdateMessageTest and CharacterDataTest (a player)
      val packets = Seq(
        hex"08 A006 DFD17 B5AEB 380B 0F80002990",
        hex"08 A006 4DD47 CDB1B 0C0B A8C1A5000403008014A4",
        hex"a0040009001600000000800000001700000000000040",
        hex"1773070000bc83e0f6c2d765535ca160000099741e4f804000000234530063007200610077006e00790052006f006e006e0069006500220b7e67b540404001000000000022b50100268042006c00610063006b002000420065007200650074002000410072006d006f007500720065006400200043006f007200700073001700e0030050040003bc00000234040001a0040003fff67a8fa0a5424e0e800000000080952a9c3a03000001081103e040000000a023782f1080c0000016244108200000000808382403a030000014284c3a0c0000000202512f00b80c00000578f80f840000000280838b3c320300000080"
      ).map(bytes => PacketCoding.decodePacket(bytes).require)
      forall(packets) { packet =>
        val encoded = PacketCoding.encodePacket(packet).require
        (FastBits.toByteArray(encoded) != null) must beTrue
        FastBits.compact(encoded) mustEqual encoded
        FastBits.toByteVector(encoded) mustEqual encoded.toByteVector
      }
    }

    "handle the empty vector" in {
      FastBits.compact(BitVector.empty) mustEqual BitVector.empty
      FastBits.toByteVector(BitVector.empty) mustEqual ByteVector.empty
    }
  }
}
