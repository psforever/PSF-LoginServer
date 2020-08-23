// Copyright (c) 2017 PSForever
package control

import org.specs2.mutable._
import net.psforever.packet.control._
import org.specs2.specification.core.Fragment
import scodec.bits._

class MultiPacketExTest extends Specification {
  val strings = Vector(
    hex"00",
    hex"01 41",
    hex"01 41" ++ hex"02 4142",
    hex"fe" ++ ByteVector.fill(0xfe)(0x41),
    hex"ffff00" ++ ByteVector.fill(0xff)(0x41),
    hex"ff0001" ++ ByteVector.fill(0x100)(0x41),
    hex"ff ffff ffff 0000" ++ ByteVector.fill(0x0000ffff)(0x41),
    hex"ff ffff 0000 0100" ++ ByteVector.fill(0x00010000)(0x41)
  )

  val packets = Vector(
    MultiPacketEx(Vector(ByteVector.empty)),
    MultiPacketEx(Vector(hex"41")),
    MultiPacketEx(Vector(hex"41", hex"4142")),
    MultiPacketEx(Vector(ByteVector.fill(0xfe)(0x41))),
    MultiPacketEx(Vector(ByteVector.fill(0xff)(0x41))),
    MultiPacketEx(Vector(ByteVector.fill(0x100)(0x41))),
    MultiPacketEx(Vector(ByteVector.fill(0x0000ffff)(0x41))),
    MultiPacketEx(Vector(ByteVector.fill(0x00010000)(0x41)))
  )

  "decode" in {
    Fragment.foreach(strings.indices) { i =>
      "test " + i ! { MultiPacketEx.decode(strings { i }.bits).require.value mustEqual packets { i } }
    }
  }

  "encode" in {
    Fragment.foreach(packets.indices) { i =>
      "test " + i ! { MultiPacketEx.encode(packets { i }).require.toByteVector mustEqual strings { i } }
    }
  }

  "sizeCodec description" in {
    MultiPacketEx.sizeCodec.toString mustEqual "variable-bit unsigned integer"
  }
}
