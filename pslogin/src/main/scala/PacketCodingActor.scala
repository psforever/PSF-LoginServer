// Copyright (c) 2017 PSForever
import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware}
import net.psforever.packet._
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import org.log4s.MDC
import MDCContextAware.Implicits._
import net.psforever.objects.DefaultCancellable
import net.psforever.packet.control.{HandleGamePacket, _}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

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

  /*
    Since the client can indicate missing packets when sending SlottedMetaPackets we should keep a history of them to resend to the client when requested with a RelatedA packet
    Since the subslot counter can wrap around, we need to use a LinkedHashMap to maintain the order packets are inserted, then we can drop older entries as required
    For example when a RelatedB packet arrives we can remove any entries to the left of the received ones without risking removing newer entries if the subslot counter wraps around back to 0
  */
  private var slottedPacketLog : mutable.LinkedHashMap[Int, ByteVector] = mutable.LinkedHashMap()

  // Due to the fact the client can send `RelatedA` packets out of order, we need to keep a buffer of which subslots arrived correctly, order them
  // and then act accordingly to send the missing subslot packet after a specified timeout
  private var relatedALog : ArrayBuffer[Int] = ArrayBuffer()
  private var relatedABufferTimeout : Cancellable = DefaultCancellable.obj

  def AddSlottedPacketToLog(subslot: Int, packet : ByteVector): Unit = {
    val log_limit = 500 // Number of SlottedMetaPackets to keep in history
    if(slottedPacketLog.size > log_limit) {
      slottedPacketLog = slottedPacketLog.drop(slottedPacketLog.size - log_limit)
    }

    slottedPacketLog{subslot} = packet
  }

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
    case PacketCodingActor.SubslotResend() => {
      log.trace(s"Subslot resend timeout reached, session: ${sessionId}")
      relatedABufferTimeout.cancel()
      log.trace(s"Client indicated successful subslots ${relatedALog.sortBy(x => x).mkString(" ")}")

      // If a non-contiguous range of RelatedA packets were received we may need to send multiple missing packets, thus split the array into contiguous ranges
      val sorted_log = relatedALog.sortBy(x => x)

      val split_logs : ArrayBuffer[ArrayBuffer[Int]] = new ArrayBuffer[ArrayBuffer[Int]]()
      var curr : ArrayBuffer[Int] = ArrayBuffer()
      for(i <- 0 to sorted_log.size - 1) {
        if(i == 0 || (sorted_log(i) != sorted_log(i-1)+1)) {
          curr = new ArrayBuffer()
          split_logs.append(curr)
        }
        curr.append(sorted_log(i))
      }

      if(split_logs.size > 1) log.trace(s"Split successful subslots into ${split_logs.size} contiguous chunks")

      for (range <- split_logs) {
        log.trace(s"Processing chunk ${range.mkString(" ")}")
        val first_accepted_subslot = range.min
        val missing_subslot = first_accepted_subslot - 1
        slottedPacketLog.get(missing_subslot) match {
          case Some(packet: ByteVector) =>
            log.info(s"Resending packet with subslot: $missing_subslot to session: ${sessionId}")
            sendResponseLeft(packet)
          case None =>
            log.error(s"Couldn't find packet with subslot: ${missing_subslot} to resend to session ${sessionId}.")
        }
      }

      relatedALog.clear()
    }
    case RawPacket(msg) =>
      if(sender == rightRef) { //from LSA, WSA, etc., to network - encode
        mtuLimit(msg)
      }
      else {//from network, to LSA, WSA, etc. - decode
        UnmarshalInnerPacket(msg, "a packet")
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
      val subslot = Subslot
      PacketCoding.EncodePacket(SlottedMetaPacket(4, subslot, bvec)) match {
        case Successful(bdata) =>
          AddSlottedPacketToLog(subslot, bdata.toByteVector)
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
    * The size limitation on any bucket is the MTU limit.
    * less by the base sizes of `MultiPacketEx` (2) and of `SlottedMetaPacket` (4).
    * @param bundle the packets to be bundled
    */
  def handleBundlePacket(bundle : List[PlanetSidePacket]) : Unit = {
    val packets : List[ByteVector] = recursiveEncode(bundle.iterator)
    recursiveFillPacketBuckets(packets.iterator, PacketCodingActor.MTU_LIMIT_BYTES - 6)
      .foreach( list => {
        handleBundlePacket(list.toVector)
      })
  }

  /**
    * Accept a `Vector` of encoded packets and re-package them.
    * The normal order is to package the elements of the vector into a `MultiPacketEx`.
    * If the vector only has one element, it will get packaged by itself in a `SlottedMetaPacket`.
    * If that one element risks being too big for the MTU, however, it will be handled off to be split.
    * Splitting should preserve `Subslot` ordering with the rest of the bundling.
    * @param vec a specific number of byte streams
    */
  def handleBundlePacket(vec : Vector[ByteVector]) : Unit = {
    if(vec.size == 1) {
      val elem = vec.head
      if(elem.length > PacketCodingActor.MTU_LIMIT_BYTES - 4) {
        handleSplitPacket(PacketCoding.CreateControlPacket(HandleGamePacket(elem)))
      }
      else {
        handleBundlePacket(elem)
      }
    }
    else {
      PacketCoding.EncodePacket(MultiPacketEx(vec)) match {
        case Successful(bdata) =>
          handleBundlePacket(bdata.toByteVector)
        case Failure(e) =>
          log.warn(s"bundling failed on MultiPacketEx creation: - $e")
      }
    }
  }

  /**
    * Accept `ByteVector` data and package it into a `SlottedMetaPacket`.
    * Send it (towards the network) upon successful encoding.
    * @param data an encoded packet
    */
  def handleBundlePacket(data : ByteVector) : Unit = {
    val subslot = Subslot
    PacketCoding.EncodePacket(SlottedMetaPacket(0, subslot, data)) match {
      case Successful(bdata) =>
        AddSlottedPacketToLog(subslot, bdata.toByteVector)
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
        log.info(s"Failed to unmarshal $description: $ex. Data : $data")
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
        log.trace(s"Client indicated a packet is missing prior to slot: $slot subslot: $subslot, session: ${sessionId}")

        relatedALog += subslot

        // (re)start the timeout period, if no more RelatedA packets are sent before the timeout period elapses the missing packet(s) will be resent
        import scala.concurrent.ExecutionContext.Implicits.global
        relatedABufferTimeout.cancel()
        relatedABufferTimeout = context.system.scheduler.scheduleOnce(100 milliseconds, self, PacketCodingActor.SubslotResend())

      case RelatedB(slot, subslot) =>
        log.trace(s"result $slot: subslot $subslot accepted, session: ${sessionId}")

        // The client has indicated it's received up to a certain subslot, that means we can purge the log of any subslots prior to and including the confirmed subslot
        // Find where this subslot is stored in the packet log (if at all) and drop anything to the left of it, including itself
        if(relatedABufferTimeout.isCancelled || relatedABufferTimeout == DefaultCancellable.obj) {
          val pos = slottedPacketLog.keySet.toArray.indexOf(subslot)
          if(pos != -1) {
            slottedPacketLog = slottedPacketLog.drop(pos+1)
            log.trace(s"Subslots left in log: ${slottedPacketLog.keySet.toString()}")
          }
        }
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

  /**
    * Accept a series of packets and transform it into a series of packet encodings.
    * Packets that do not encode properly are simply excluded from the product.
    * This is not treated as an error or exception; a warning will merely be logged.
    * @param iter the `Iterator` for a series of packets
    * @param out updated series of byte stream data produced through successful packet encoding;
    *            defaults to an empty list
    * @return a series of byte stream data produced through successful packet encoding
    */
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

  /**
    * Accept a series of byte stream data and sort into sequential size-limited buckets of the same byte streams.
    * Note that elements that exceed `lim` by themselves are always sorted into their own buckets.
    * @param iter an `Iterator` of a series of byte stream data
    * @param lim the maximum stream length permitted
    * @param curr the stream length of the current bucket
    * @param out updated series of byte stream data stored in buckets
    * @return a series of byte stream data stored in buckets
    */
  @tailrec private def recursiveFillPacketBuckets(iter : Iterator[ByteVector], lim : Int, curr : Int = 0, out : List[mutable.ListBuffer[ByteVector]] = List(mutable.ListBuffer())) : List[mutable.ListBuffer[ByteVector]] = {
    if(!iter.hasNext) {
      out
    }
    else {
      val data = iter.next
      var len = data.length.toInt
      len = len + (if(len < 256) { 1 } else if(len < 65536) { 2 } else { 4 }) //space for the prefixed length byte(s)
      if(curr + len > lim && out.last.nonEmpty) { //bucket must have something in it before swapping
        recursiveFillPacketBuckets(iter, lim, len, out :+ mutable.ListBuffer(data))
      }
      else {
        out.last += data
        recursiveFillPacketBuckets(iter, lim, curr + len, out)
      }
    }
  }
}

object PacketCodingActor {
  final val MTU_LIMIT_BYTES : Int = 467

  private final case class SubslotResend()
}
