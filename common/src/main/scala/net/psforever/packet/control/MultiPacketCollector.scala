// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.PlanetSidePacket

/**
  * Message for holding a series of packets being moved through the system (server),
  * eventually be bundled into a `MultiPacketEx` and dispatched to the client.
  * Invalid packets are eliminated at the time of creation.
  * At least one packet is necessary.
  * @param packets a series of packets to be bundled together;
  *                this list is effectively immutable;
  *                the only way to access these packets is through pattern matching
  */
final case class MultiPacketBundle(private var packets : List[PlanetSidePacket]) {
  packets match {
    case Nil =>
      throw new IllegalArgumentException("can not create with zero packets")
    case _ =>
      packets = MultiPacketBundle.collectValidPackets(packets)
  }
}

object MultiPacketBundle {
  /**
    * Accept a series of packets of a specific supertype (`PlanetSidePacket`)
    * and filter out subtypes that should be excluded.
    * Show a generic disclaimer if any packets were filtered.
    * Two of the four subclasses of `PlanetSidePacket` are accepted - `PlanetSideGamePacket` and `PlanetSideControlPacket`.
    * @param packets a series of packets
    * @return the accepted packets from the original group
    */
  def collectValidPackets(packets : List[PlanetSidePacket]) : List[PlanetSidePacket] = {
    import net.psforever.packet.{PlanetSideGamePacket, PlanetSideControlPacket}
    val (good, bad) = packets.partition( {
      case _ : PlanetSideGamePacket => true
      case _ : PlanetSideControlPacket => true
      case _ => false
    })
    if(bad.nonEmpty) {
      org.log4s.getLogger("MultiPacketBundle")
        .warn(s"attempted to include packet types that are on the whitelist; ${bad.size} items have been excluded")
    }
    good
  }
}

/**
  * Accumulator for packets that will eventually be bundled and submitted for composing a `MultiPacketEx` packet.<br>
  * <br>
  * The accumulator is intended to be a disposable convenience class to incrementally construct a `MultiPacketBundle`.
  * Packets can only be added on top of the existing internal collection and can not be removed.
  * (Overloaded methods for adding packets from various sources also exist.)
  * Additionally, retrieving a copy of the collection via a `MultiPacketBundle` does not empty the collection.
  */
class MultiPacketCollector() {
  private var bundle : List[PlanetSidePacket] = List.empty

  def Add(t : PlanetSidePacket) : Unit = Add(List(t))

  def Add(t : MultiPacketBundle) : Unit = t match {
    case MultiPacketBundle(list) =>
      Add(list)
  }

  def Add(t : List[PlanetSidePacket]) : Unit = {
    bundle = bundle ++ t
  }

  /**
    * Retrieve the internal collection of packets.
    * @return a loaded `MultiPacketBundle` object
    */
  def Bundle : MultiPacketBundle = MultiPacketBundle(bundle)

  /**
    * A safer `Bundle` that consumes any` Exceptions` that might be thrown in the process of producing output.
    * @see `Bundle`
    * @return a loaded `MultiPacketBundle` object, or `None`
    */
  def BundleOption : Option[MultiPacketBundle] = {
    try {
      Some(Bundle)
    }
    catch {
      case _ : Exception =>
        None
    }
  }
}

object MultiPacketCollector {
  /**
    * Overload constructor that accepts a initial packets.
    * @param packets a series of packets
    * @return a `MultiPacketCollector` object
    */
  def apply(packets : List[PlanetSidePacket]) : MultiPacketCollector = {
    val obj = new MultiPacketCollector()
    obj.Add(packets)
    obj
  }
}
