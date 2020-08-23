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
final case class MultiPacketBundle(private var packets: List[PlanetSidePacket]) {
  MultiPacketBundle.collectValidPackets(packets) match {
    case Nil =>
      throw new IllegalArgumentException("can not create with zero packets")
    case list =>
      packets = list
  }

  def +(t: MultiPacketBundle): MultiPacketBundle =
    t match {
      case MultiPacketBundle(list) =>
        MultiPacketBundle(packets ++ list)
      case _ =>
        MultiPacketBundle(packets)
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
  def collectValidPackets(packets: List[PlanetSidePacket]): List[PlanetSidePacket] = {
    import net.psforever.packet.{PlanetSideGamePacket, PlanetSideControlPacket}
    val (good, bad) = packets.partition({
      case _: PlanetSideGamePacket    => true
      case _: PlanetSideControlPacket => true
      case _                          => false
    })
    if (bad.nonEmpty) {
      org.log4s
        .getLogger("MultiPacketBundle")
        .warn(s"attempted to include packet types that are not in the whitelist; ${bad.size} items have been excluded")
    }
    good
  }
}

/**
  * Accumulator for packets that will eventually be bundled and submitted for composing a `MultiPacketEx` packet.
  */
class MultiPacketCollector() {
  private var bundle: List[PlanetSidePacket] = List.empty

  def Add(t: PlanetSidePacket): Unit = Add(List(t))

  def Add(t: MultiPacketBundle): Unit =
    t match {
      case MultiPacketBundle(list) =>
        Add(list)
    }

  def Add(t: List[PlanetSidePacket]): Unit = {
    if (t.nonEmpty) {
      bundle = bundle ++ t
    }
  }

  /**
    * Retrieve the internal collection of packets.
    * Reset the internal list of packets by clearing it.
    * @return a loaded `MultiPacketBundle` object, or `None`
    */
  def Bundle: Option[MultiPacketBundle] = {
    bundle match {
      case Nil =>
        None
      case list =>
        val out = MultiPacketBundle(list)
        bundle = List.empty
        Some(out)
    }
  }
}

object MultiPacketCollector {

  /**
    * Overload constructor that accepts initial packets.
    * @param bundle previously bundled packets
    * @return a `MultiPacketCollector` object
    */
  def apply(bundle: MultiPacketBundle): MultiPacketCollector = {
    val obj = new MultiPacketCollector()
    obj.Add(bundle)
    obj
  }

  /**
    * Overload constructor that accepts initial packets.
    * @param packets a series of packets
    * @return a `MultiPacketCollector` object
    */
  def apply(packets: List[PlanetSidePacket]): MultiPacketCollector = {
    val obj = new MultiPacketCollector()
    obj.Add(packets)
    obj
  }
}
