// Copyright (c) 2017 PSForever
import akka.actor.{Actor, ActorRef, MDCContextAware}
import net.psforever.packet._
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import org.log4s.MDC
import MDCContextAware.Implicits._
import net.psforever.packet.control._

import scala.annotation.tailrec
import scala.collection.mutable

/**
  * In between the network side and the higher functioning side of the simulation:
  * accept packets and transform them into a sequence of data (encoding), and
  * accept a sequence of data and transform it into s packet (decoding).<br>
  * <br>
  * Following the standardization of the `SessionRouter` pipeline, the throughput of this `Actor` has directionality.
  * The "network," where the encoded data comes and goes, is assumed to be `leftRef`.
  * The "simulation", where the decoded packets come and go, is assumed to be `rightRef`.
  * `rightRef` can accept a sequence that looks like encoded data but it will merely pass out the same sequence.
  * Likewise, `leftRef` accepts decoded packets but merely ejects the same packets without doing any work on them.
  * The former functionality is anticipated.
  * The latter functionality is deprecated.<br>
  * <br>
  * Encoded data leaving the `Actor` (`leftRef`) is limited by an upper bound capacity.
  * Sequences can not be larger than that bound or else they will be dropped.
  * This maximum transmission unit (MTU) is used to divide the encoded sequence into chunks of encoded data,
  * re-packaged into nested `ControlPacket` units, and each unit encoded.
  * The outer packaging is numerically consistent with a `subslot` that starts counting once the simulation starts.
  * The client is very specific about the `subslot` number and will reject out-of-order packets.
  * It resets to 0 each time this `Actor` starts up and the client reflects this functionality.
  */
class PacketCodingActor extends Actor with MDCContextAware {
  private var sessionId : Long = 0
  private var subslotOutbound : Int = 0
  private var subslotInbound : Int = 0
  private var leftRef : ActorRef = ActorRef.noSender
  private var rightRef : ActorRef = ActorRef.noSender
  private[this] val log = org.log4s.getLogger

  override def postStop() = {
    subslotOutbound = 0 //in case this `Actor` restarts
    super.postStop()
  }

  def receive = Initializing

  def Initializing : Receive = {
    case HelloFriend(sharedSessionId, pipe) =>
      import MDCContextAware.Implicits._
      this.sessionId = sharedSessionId
      leftRef = sender()
      if(pipe.hasNext) {
        rightRef = pipe.next
        rightRef !> HelloFriend(sessionId, pipe)
      }
      else {
        rightRef = sender()
      }
      log.trace(s"Left sender ${leftRef.path.name}")
      context.become(Established)

    case default =>
      log.error("Unknown message " + default)
      context.stop(self)
  }

  def Established : Receive = {
    case RawPacket(msg) =>
      if(sender == rightRef) { //from LSA, WSA, etc., to network - encode
        mtuLimit(msg)
      }
      else {//from network, to LSA, WSA, etc. - decode
        UnmarshalInnerPacket(msg, "a packet")
//        PacketCoding.unmarshalPayload(0, msg) match { //TODO is it safe for this to always be 0?
//          case Successful(packet) =>
//            handlePacketContainer(packet) //sendResponseRight
//          case Failure(ex) =>
//            log.info(s"Failed to marshal a packet: $ex")
//        }
      }
    //known elevated packet type
    case ctrl @ ControlPacket(_, packet) =>
      if(sender == rightRef) { //from LSA, WSA, to network - encode
        PacketCoding.EncodePacket(packet) match {
          case Successful(data) =>
            mtuLimit(data.toByteVector)
          case Failure(ex) =>
            log.error(s"Failed to encode a ControlPacket: $ex")
        }
      }
      else { //deprecated; ControlPackets should not be coming from this direction
        log.warn(s"DEPRECATED CONTROL PACKET SEND: $ctrl")
        MDC("sessionId") = sessionId.toString
        handlePacketContainer(ctrl) //sendResponseRight
      }
    //known elevated packet type
    case game @ GamePacket(_, _, packet) =>
      if(sender == rightRef) { //from LSA, WSA, etc., to network - encode
        PacketCoding.EncodePacket(packet) match {
          case Successful(data) =>
            mtuLimit(data.toByteVector)
          case Failure(ex) =>
            log.error(s"Failed to encode a GamePacket: $ex")
        }
      }
      else { //deprecated; GamePackets should not be coming from this direction
        log.warn(s"DEPRECATED GAME PACKET SEND: $game")
        MDC("sessionId") = sessionId.toString
        sendResponseRight(game)
      }
    //bundling packets into a SlottedMetaPacket0/MultiPacketEx
    case msg @ MultiPacketBundle(list) =>
      log.trace(s"BUNDLE PACKET REQUEST SEND, LEFT (always): $msg")
      handleBundlePacket(list)
    //etc
    case msg =>
      if(sender == rightRef) {
        log.trace(s"BASE CASE PACKET SEND, LEFT: $msg")
        MDC("sessionId") = sessionId.toString
        leftRef !> msg
      }
      else {
        log.trace(s"BASE CASE PACKET SEND, RIGHT: $msg")
        MDC("sessionId") = sessionId.toString
        rightRef !> msg
      }
  }

  /**
    * Retrieve the current subslot number.
    * Increment the `subslot` for the next time it is needed.
    * @return a `16u` number starting at 0
    */
  def Subslot : Int = {
    if(subslotOutbound == 65536) { //TODO what is the actual wrap number?
      subslotOutbound = 0
      subslotOutbound
    } else {
      val curr = subslotOutbound
      subslotOutbound += 1
      curr
    }
  }

  /**
    * Check that an outbound packet is not too big to get stuck by the MTU.
    * If it is larger than the MTU, divide it up and re-package the sections.
    * Otherwise, send the data out like normal.
    * @param msg the encoded packet data
    */
  def mtuLimit(msg : ByteVector) : Unit = {
    if(msg.length > PacketCodingActor.MTU_LIMIT_BYTES) {
      handleSplitPacket(PacketCoding.CreateControlPacket(HandleGamePacket(msg)))
    }
    else {
      sendResponseLeft(msg)
    }
  }

  /**
    * Transform a `ControlPacket` into `ByteVector` data for splitting.
    * @param cont the original `ControlPacket`
    */
  def handleSplitPacket(cont : ControlPacket) : Unit = {
    PacketCoding.getPacketDataForEncryption(cont) match {
      case Successful((_, data)) =>
        handleSplitPacket(data)
      case Failure(ex) =>
        log.error(s"$ex")
    }
  }

  /**
    * Accept `ByteVector` data, representing a `ControlPacket`, and split it into chunks.
    * The chunks should not be blocked by the MTU.
    * Send each chunk (towards the network) as it is converted.
    * @param data `ByteVector` data to be split
    */
  def handleSplitPacket(data : ByteVector) : Unit = {
    val lim = PacketCodingActor.MTU_LIMIT_BYTES - 4 //4 bytes is the base size of SlottedMetaPacket
    data.grouped(lim).foreach(bvec => {
      PacketCoding.EncodePacket(SlottedMetaPacket(4, Subslot, bvec)) match {
        case Successful(bdata) =>
          sendResponseLeft(bdata.toByteVector)
        case f : Failure =>
          log.error(s"$f")
      }
    })
  }

  /**
    * Accept a `List` of packets and sequentially re-package the elements from the list into multiple container packets.<br>
    * <br>
    * The original packets are encoded then paired with their encoding lengths plus extra space to prefix the length.
    * Encodings from these pairs are drawn from the list until into buckets that fit a maximum byte stream length.
    * The size limitation on any bucket is the mtu limit
    * less by the base sizes of `MultiPacketEx` (2) and of `SlottedMetaPacket` (4).
    * @param bundle the packets to be bundled
    */
  def handleBundlePacket(bundle : List[PlanetSidePacket]) : Unit = {
    val packets : List[(ByteVector, Int)] = recursiveEncode(bundle.iterator).map( pkt => {
      pkt -> {
        val len = pkt.length.toInt
        len + (if(len < 256) { 1 } else if(len < 65536) { 2 } else { 4 }) //space for the prefixed length byte(s)
      }
    })
    recursiveFillPacketBuckets(packets.iterator, PacketCodingActor.MTU_LIMIT_BYTES - 6)
      .foreach( list => {
        handleBundlePacket(list.toVector)
      })
  }

  /**
    * Accept a `Vector` of encoded packets and re-package them into a `MultiPacketEx`.
    * @param vec a specific number of byte streams
    */
  def handleBundlePacket(vec : Vector[ByteVector]) : Unit = {
    PacketCoding.EncodePacket(MultiPacketEx(vec)) match {
      case Successful(bdata) =>
        handleBundlePacket(bdata.toByteVector)
      case Failure(e) =>
        log.warn(s"bundling failed on MultiPacketEx creation: - $e")
    }
  }

  /**
    * Accept `ByteVector` data and package it into a `SlottedMetaPacket`.
    * Send it (towards the network) upon successful encoding.
    * @param data an encoded packet
    */
  def handleBundlePacket(data : ByteVector) : Unit = {
    PacketCoding.EncodePacket(SlottedMetaPacket(0, Subslot, data)) match {
      case Successful(bdata) =>
        sendResponseLeft(bdata.toByteVector)
      case Failure(e) =>
        log.warn(s"bundling failed on SlottedMetaPacket creation: - $e")
    }
  }

  /**
    * Encoded sequence of data going towards the network.
    * @param cont the data
    */
  def sendResponseLeft(cont : ByteVector) : Unit = {
    log.trace("PACKET SEND, LEFT: " + cont)
    MDC("sessionId") = sessionId.toString
    leftRef !> RawPacket(cont)
  }

  /**
    * Transform data into a container packet and re-submit that container to the process that handles the packet.
    * @param data the packet data
    * @param description an explanation of the input `data`
    */
  def UnmarshalInnerPacket(data : ByteVector, description : String) : Unit = {
    PacketCoding.unmarshalPayload(0, data) match { //TODO is it safe for this to always be 0?
      case Successful(packet) =>
        handlePacketContainer(packet)
      case Failure(ex) =>
        log.info(s"Failed to unmarshal $description: $ex")
    }
  }

  /**
    *  Sort and redirect a container packet bound for the server by type of contents.
    *  `GamePacket` objects can just onwards without issue.
    *  `ControlPacket` objects may need to be dequeued.
    *  All other container types are invalid.
    * @param container the container packet
    */
  def handlePacketContainer(container : PlanetSidePacketContainer) : Unit = {
    container match {
      case _ : GamePacket =>
        sendResponseRight(container)
      case ControlPacket(_, ctrlPkt) =>
        handleControlPacket(container, ctrlPkt)
      case default =>
        log.warn(s"Invalid packet container class received: ${default.getClass.getName}") //do not spill contents in log
    }
  }

  /**
    * Process a control packet or determine that it does not need to be processed at this level.
    * Primarily, if the packet is of a type that contains another packet that needs be be unmarshalled,
    * that/those packet must be unwound.<br>
    * <br>
    * The subslot information is used to identify these nested packets after arriving at their destination,
    * to establish order for sequential packets and relation between divided packets.
    * @param container the original container packet
    * @param packet the packet that was extracted from the container
    */
  def handleControlPacket(container : PlanetSidePacketContainer, packet : PlanetSideControlPacket) = {
    packet match {
      case SlottedMetaPacket(slot, subslot, innerPacket) =>
        subslotInbound = subslot
        self.tell(PacketCoding.CreateControlPacket(RelatedB(slot, subslot)), rightRef) //will go to the network
        UnmarshalInnerPacket(innerPacket, "the inner packet of a SlottedMetaPacket")

      case MultiPacket(packets) =>
        packets.foreach { UnmarshalInnerPacket(_, "the inner packet of a MultiPacket") }

      case MultiPacketEx(packets) =>
        packets.foreach { UnmarshalInnerPacket(_, "the inner packet of a MultiPacketEx") }

      case RelatedA(slot, subslot) =>
        log.error(s"result $slot: subslot $subslot was in error")

      case RelatedB(slot, subslot) =>
        log.trace(s"result $slot: subslot $subslot accepted")

      case _ =>
        sendResponseRight(container)
    }
  }

  /**
    * Decoded packet going towards the simulation.
    * @param cont the packet
    */
  def sendResponseRight(cont : PlanetSidePacketContainer) : Unit = {
    log.trace("PACKET SEND, RIGHT: " + cont)
    MDC("sessionId") = sessionId.toString
    rightRef !> cont
  }

  /** WIP */

  @tailrec private def recursiveEncode(iter : Iterator[PlanetSidePacket], out : List[ByteVector] = List()) : List[ByteVector] = {
    if(!iter.hasNext) {
      out
    }
    else {
      import net.psforever.packet.{PlanetSideControlPacket, PlanetSideGamePacket}
      iter.next match {
        case msg : PlanetSideGamePacket =>
          PacketCoding.EncodePacket(msg) match {
            case Successful(bytecode) =>
              recursiveEncode(iter, out :+ bytecode.toByteVector)
            case Failure(e) =>
              log.warn(s"game packet $msg, part of a bundle, did not encode - $e")
              recursiveEncode(iter, out)
          }
        case msg : PlanetSideControlPacket =>
          PacketCoding.EncodePacket(msg) match {
            case Successful(bytecode) =>
              recursiveEncode(iter, out :+ bytecode.toByteVector)
            case Failure(e) =>
              log.warn(s"control packet $msg, part of a bundle, did not encode - $e")
              recursiveEncode(iter, out)
          }
        case _ =>
          recursiveEncode(iter, out)
      }
    }
  }

  @tailrec private def recursiveFillPacketBuckets(iter : Iterator[(ByteVector, Int)], lim : Int, currLen : Int = 0, out : List[mutable.ListBuffer[ByteVector]] = List(mutable.ListBuffer())) : List[mutable.ListBuffer[ByteVector]] = {
    if(!iter.hasNext) {
      out
    }
    else {
      val (data, len) = iter.next
      if(currLen + len > lim) {
        recursiveFillPacketBuckets(iter, lim, len, out :+ mutable.ListBuffer(data))
      }
      else {
        out.last += data
        recursiveFillPacketBuckets(iter, lim, currLen + len, out)
      }
    }
  }
}

object PacketCodingActor {
  final val MTU_LIMIT_BYTES : Int = 467
}
