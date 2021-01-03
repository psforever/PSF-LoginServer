package net.psforever.actors.net

import akka.actor.Cancellable
import akka.actor.typed.{ActorRef, ActorTags, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.io.Udp
import java.net.InetSocketAddress
import java.security.{SecureRandom, Security}
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.jce.provider.BouncyCastleProvider
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration._
import scodec.Attempt
import scodec.Attempt.{Failure, Successful}
import scodec.bits.{BitVector, ByteVector, HexStringSyntax}
import scodec.interop.akka.EnrichedByteVector

import net.psforever.objects.Default
import net.psforever.packet.{
  CryptoPacketOpcode,
  PacketCoding,
  PlanetSideControlPacket,
  PlanetSideCryptoPacket,
  PlanetSideGamePacket,
  PlanetSidePacket
}
import net.psforever.packet.control.{
  ClientStart,
  ConnectionClose,
  ControlSync,
  ControlSyncResp,
  HandleGamePacket,
  MultiPacket,
  MultiPacketEx,
  RelatedA,
  RelatedB,
  ServerStart,
  SlottedMetaPacket,
  TeardownConnection
}
import net.psforever.packet.crypto.{ClientChallengeXchg, ClientFinished, ServerChallengeXchg, ServerFinished}
import net.psforever.packet.game.{ChangeFireModeMessage, CharacterInfoMessage, KeepAliveMessage, PingMsg}
import net.psforever.packet.PacketCoding.CryptoCoding
import net.psforever.util.{DiffieHellman, Md5Mac}

/**
  * `MiddlewareActor` sits between the raw UDP socket and the "main" actors
  * (either `LoginActor` or `SessionActor`)
  * and handles crypto and control packets.
  * The former side is called the outbound network (the clients); the former is called the inbound local (server).
  * This service sets up cryptography, it decodes incoming packets, it encodes, bundles, or splits outgoing packets,
  * and it handles things like requesting/resending lost packets.
  * Accurate operation of the service is mostly a product of the network being agreeable
  * and allowing packets to arrive correctly.
  * Various subroutines are used to keep track of the predicted offending packets.<br>
  * <br>
  * The cryptographic aspect of the service resolves itself first,
  * exchanging keys and passcodes and challeneges between local and the network.
  * Most of this process occurs without any prior cryptographic setup,
  * relying on the validation of its own exchange to operate.
  * Afterwards its completion,
  * all raw data arriving from the network or leaving to the network will require a cipher
  * based on the previously exchanged data.
  * <br>
  * As packets arrive from local with the intention of being sent out towards the network terminus,
  * they will be pooled into a buffer.
  * Periodically, the buffer contents will be evaluated,
  * and packet data of enough quantity and combined length will be assembled into a singular packet bundle.
  * This bundle will be queued and dispatched towards the network.
  * If the outbound packets do not arrive at the network terminus correctly,
  * the network has a method of dispatching requests for identified missing packets.
  * This side of the communication will keep track of its previously dispatched packets for a "reasonable" amount of time
  * and will respond to those requests if possible by searching its catalog.<br>
  * <br>
  * If the inbound packets do not arrive correctly the first time,
  * after a while, requests for identified mising packets from the network source will occur.
  * Virtually all important packets have a sequence number that bestows on each packet an absolute delivery order.
  * Bundled packets have a subslot number that indicates the total number of subslot packets dispatched to this client
  * as well as the order in which the packets should have been received.
  * If a packet is out of sequence - a future packet, compared to what is being expected - it is buffered.
  * Should the expected packets show up out of order before the buffered is cleared,
  * everything sorts itself out.
  * Unresolved missing sequence entries will often lead to requests for missing packets with anticipated subslots.
  * If these requests do not resolve, there is unfortuantely not much that can be done except grin and bear with it.
  */
object MiddlewareActor {
  Security.addProvider(new BouncyCastleProvider)

  /** Maximum packet size in bytes */
  //final val MTU: Int = 467
  final val MTU: Int = 440

  def apply(
      socket: ActorRef[Udp.Command],
      sender: InetSocketAddress,
      next: (ActorRef[Command], InetSocketAddress, String) => Behavior[PlanetSidePacket],
      connectionId: String
  ): Behavior[Command] =
    Behaviors.setup(context => new MiddlewareActor(context, socket, sender, next, connectionId).start())

  sealed trait Command

  /** Receive incoming packet */
  final case class Receive(msg: ByteVector) extends Command

  /** Send outgoing packet */
  final case class Send(msg: PlanetSidePacket) extends Command

  /** Teardown connection */
  final case class Teardown() extends Command

  /** Close connection */
  final case class Close() extends Command

  /** ... */
  private case class ProcessQueue() extends Command

  /** Log inbound packets that are yet to be in proper order by sequence number */
  private case class InReorderEntry(packet: PlanetSidePacket, sequence: Int, time: Long)

  /**
    * All packets are bundled by themselves.
    * May as well just waste all of the cycles on your CPU, eh?
    */
  def allPacketGuard(packet: PlanetSidePacket): Boolean = true

  /**
    * `CharacterInfoMessage` packets are bundled by themselves.<br>
    * <br>
    * Super awkward special case.
    * Bundling `CharacterInfoMessage` with its corresponding `ObjectCreateDetailedMesssage`,
    * which can occur during otherwise careless execution of the character select screen,
    * causes the character options to show blank slots and be unusable.
    */
  def characterInfoMessageGuard(packet: PlanetSidePacket): Boolean = {
    packet.isInstanceOf[CharacterInfoMessage]
  }

  /**
    * `KeepAliveMessage` packets are bundled by themselves.
    * They're special.
    */
  def keepAliveMessageGuard(packet: PlanetSidePacket): Boolean = {
    packet.isInstanceOf[KeepAliveMessage]
  }

  /**
    * A function for blanking tasks related to inbound packet resolution.
    * Do nothing.
    * Wait to be told to do something.
    */
  private def neutral(): Unit = { }
}

/**
  * MiddlewareActor sits between the raw UDP socket and the "main" actors (either login or session) and handles
  * crypto and control packets. This means it sets up cryptography, it decodes incoming packets,
  * it encodes, bundles and splits outgoing packets, it handles things like requesting/resending lost packets and more.
  */
class MiddlewareActor(
    context: ActorContext[MiddlewareActor.Command],
    socket: ActorRef[Udp.Command],
    sender: InetSocketAddress,
    next: (ActorRef[MiddlewareActor.Command], InetSocketAddress, String) => Behavior[PlanetSidePacket],
    connectionId: String
) {

  import MiddlewareActor._

  implicit val ec: ExecutionContextExecutor = context.executionContext
  implicit val executor: ExecutionContext   = context.executionContext

  private[this] val log = org.log4s.getLogger

  var clientNonce: Long = 0

  var serverMACBuffer: ByteVector = ByteVector.empty

  val random = new SecureRandom()

  var crypto: Option[CryptoCoding] = None

  val nextActor: ActorRef[PlanetSidePacket] =
    context.spawnAnonymous(next(context.self, sender, connectionId), ActorTags(s"id=$connectionId"))

  /** Queue of incoming packets (plus sequence numbers and timestamps) that arrived in the wrong order */
  private val inReorderQueue: mutable.Queue[InReorderEntry] = mutable.Queue()

  /** Latest incoming sequence number */
  var inSequence = 0

  /** Latest incoming subslot number */
  var inSubslot = 0

  /** List of missing subslot numbers and attempts counter */
  var inSubslotsMissing: mutable.Map[Int, Int] = mutable.Map()

  /** Queue of outgoing packets used for bundling and splitting */
  val outQueue: mutable.Queue[(PlanetSidePacket, BitVector)] = mutable.Queue()

  /** Queue of outgoing packets ready for sending */
  val outQueueBundled: mutable.Queue[PlanetSidePacket] = mutable.Queue()

  /** Latest outbound sequence number;
    * the current sequence is one less than this number */
  var outSequence = 0

  /**
    * Increment the outbound sequence number.
    * The previous sequence number is returned.
    * The fidelity of the sequence field in packets is 16 bits, so wrap back to 0 after 65535.
    * @return
    */
  def nextSequence: Int = {
    val r = outSequence
    if (outSequence == 0xffff) {
      outSequence = 0
    } else {
      outSequence += 1
    }
    r
  }

  /** Latest outbound subslot number;
    * the current subslot is one less than this number */
  var outSubslot = 0

  /**
    * Increment the outbound subslot number.
    * The previous subslot number is returned.
    * The fidelity of the subslot field in `SlottedMetapacket`'s is 16 bits, so wrap back to 0 after 65535.
    * @return
    */
  def nextSubslot: Int = {
    val r = outSubslot
    if (outSubslot == 0xffff) {
      outSubslot = 0
    } else {
      outSubslot += 1
    }
    r
  }

  /**
    * Do not bundle these packets together with other packets
    */
  val packetsBundledByThemselves: List[PlanetSidePacket=>Boolean] = List(
    MiddlewareActor.keepAliveMessageGuard,
    MiddlewareActor.characterInfoMessageGuard
  )

  val smpHistoryLength: Int = 100
  /** History of created `SlottedMetaPacket`s.
    * In case the client does not register receiving a packet by checking against packet subslot index numbers,
    * it will dispatch a `RelatedA` packet,
    * and the server will hopefully locate the packet where it has been backlogged.
    * The client will also dispatch a `RelatedB` packet to indicate the packet with the highest subslot received.
    * All packets with subslots less than that number have been received or will no longer be requested.
    * The client and server supposedly maintain reciprocating mechanisms.
    */
  val preparedSlottedMetaPackets: Array[SlottedMetaPacket] = new Array[SlottedMetaPacket](smpHistoryLength)
  var nextSmpIndex: Int = 0
  var acceptedSmpSubslot: Int = 0


  var timesInReorderQueue: Int = 0
  var timesSubslotMissing: Int = 0
  /** Delay between runs of the packet bundler/resolver timer (ms);
    * 250ms per network update (client upstream), so 10 runs of this bundling code every update */
  val packetProcessorHz = 25.milliseconds
  /** Timer that handles the bundling and throttling of outgoing packets and resolves disorganized inbound packets */
  var packetProcessor: Cancellable = Default.Cancellable

//  val inboundProcessorHz = 10.milliseconds
//  var inboundProcessor: Cancellable = Default.Cancellable

//formerly, CryptoSessionActor

  def start(): Behavior[Command] = {
    Behaviors.receiveMessagePartial {
      case Receive(msg) =>
        PacketCoding.unmarshalPacket(msg) match {
          case Successful(packet) =>
            packet match {
              case (ClientStart(nonce), _) =>
                clientNonce = nonce
                val serverNonce = Math.abs(random.nextInt())
                send(ServerStart(nonce, serverNonce), None, None)
                cryptoSetup()

              // TODO ResetSequence
              case _ =>
                log.warn(s"Unexpected packet type $packet in start (before crypto)")
                Behaviors.same
            }
          case Failure(_) =>
            // There is a special case where no crypto is being used.
            // The only packet coming through looks like PingMsg. This is a hardcoded
            // feature of the client @ 0x005FD618
            PacketCoding.decodePacket(msg) match {
              case Successful(packet) =>
                packet match {
                  case ping: PingMsg =>
                    // reflect the packet back to the sender
                    send(ping)
                    Behaviors.same

                  case _: ChangeFireModeMessage =>
                    log.trace(s"What is this packet that just arrived? ${msg.toString}")
                    //ignore
                    Behaviors.same

                  case _ =>
                    log.error(s"Unexpected non-crypto packet type $packet in start")
                    Behaviors.same
                }
              case Failure(e) =>
                log.error(s"Could not decode packet in start: $e")
                Behaviors.same
            }
        }

      case other =>
        log.error(s"Invalid message '$other' received in start")
        Behaviors.same
    }

  }

  def cryptoSetup(): Behavior[Command] = {
    Behaviors
      .receiveMessagePartial[Command] {
        case Receive(msg) =>
          PacketCoding.unmarshalPacket(msg, None, CryptoPacketOpcode.ClientChallengeXchg) match {
            case Successful(packet) =>
              packet match {
                case (ClientChallengeXchg(time, challenge, p, g), Some(_)) =>
                  serverMACBuffer ++= msg.drop(3)

                  val dh = DiffieHellman(p.toArray, g.toArray)

                  val clientChallenge = ServerChallengeXchg.getCompleteChallenge(time, challenge)
                  val serverTime      = System.currentTimeMillis() / 1000L
                  val randomChallenge = randomBytes(0xc)
                  val serverChallenge = ServerChallengeXchg.getCompleteChallenge(serverTime, randomChallenge)

                  serverMACBuffer ++= send(
                    ServerChallengeXchg(serverTime, randomChallenge, ByteVector.view(dh.publicKey))
                  ).drop(3)

                  cryptoFinish(dh, clientChallenge, serverChallenge)

                case _ =>
                  log.error(s"Unexpected packet type $packet in cryptoSetup")
                  connectionClose()
              }
            case Failure(e) =>
              log.error(s"Could not decode packet in cryptoSetup: $e")
              connectionClose()
          }
        case other =>
          log.error(s"Invalid message '$other' received in cryptoSetup")
          connectionClose()
      }
      .receiveSignal(onSignal)
  }

  def cryptoFinish(dh: DiffieHellman, clientChallenge: ByteVector, serverChallenge: ByteVector): Behavior[Command] = {
    Behaviors
      .receiveMessagePartial[Command] {
        case Receive(msg) =>
          PacketCoding.unmarshalPacket(msg, None, CryptoPacketOpcode.ClientFinished) match {
            case Successful(packet) =>
              packet match {
                case (ClientFinished(clientPubKey, _), Some(_)) =>
                  serverMACBuffer ++= msg.drop(3)

                  val agreedKey = dh.agree(clientPubKey.toArray)
                  val agreedMessage = ByteVector("master secret".getBytes) ++ clientChallenge ++
                    hex"00000000" ++ serverChallenge ++ hex"00000000"

                  val masterSecret = new Md5Mac(ByteVector.view(agreedKey)).updateFinal(agreedMessage)
                  val mac          = new Md5Mac(masterSecret)

                  // To do? verify client challenge. The code below has always been commented out, so it probably never
                  // worked and it surely doesn't work now. The whole cryptography is flawed because
                  // of the 128bit p values for DH, so implementing security features is probably not worth it.
                  /*
                  val clientChallengeExpanded = mac.updateFinal(
                    ByteVector(
                      "client finished".getBytes
                    ) ++ serverMACBuffer ++ hex"01" ++ clientChallengeResult ++ hex"01",
                    0xc
                  )
                   */

                  val serverChallengeResult = mac
                    .updateFinal(ByteVector("server finished".getBytes) ++ serverMACBuffer ++ hex"01", 0xc)

                  val encExpansion = ByteVector.view("server expansion".getBytes) ++ hex"0000" ++ serverChallenge ++
                    hex"00000000" ++ clientChallenge ++ hex"00000000"
                  val decExpansion = ByteVector.view("client expansion".getBytes) ++ hex"0000" ++ serverChallenge ++
                    hex"00000000" ++ clientChallenge ++ hex"00000000"

                  val expandedEncKey = mac.updateFinal(encExpansion, 64)
                  val expandedDecKey = mac.updateFinal(decExpansion, 64)

                  crypto = Some(
                    CryptoCoding(
                      new SecretKeySpec(expandedEncKey.take(20).toArray, "RC5"),
                      new SecretKeySpec(expandedDecKey.take(20).toArray, "RC5"),
                      expandedEncKey.slice(20, 36),
                      expandedDecKey.slice(20, 36)
                    )
                  )

                  send(ServerFinished(serverChallengeResult))
                  //start the queue processor loop
                  packetProcessor =
                    context.system.scheduler.scheduleWithFixedDelay(packetProcessorHz, packetProcessorHz)(()=> {
                      context.self ! ProcessQueue()
                    })
                  active()

                case other =>
                  log.error(s"Unexpected packet '$other' in cryptoFinish")
                  connectionClose()
              }
            case Failure(e) =>
              log.error(s"Could not decode packet in cryptoFinish: $e")
              connectionClose()
          }
        case other =>
          log.error(s"Invalid message '$other' received in cryptoFinish")
          connectionClose()
      }
      .receiveSignal(onSignal)
  }

//formerly, PacketCodingActor

  def active(): Behavior[Command] = {
    Behaviors
      .receiveMessage[Command] {
        case Receive(msg) =>
          PacketCoding.unmarshalPacket(msg, crypto) match {
            case Successful((packet, Some(sequence))) =>
              if (sequence == inSequence + 1) {
                inSequence = sequence
                in(packet)
              } else if(sequence < inSequence) { //expedite this packet
                inReorderQueue.filterInPlace(_.sequence == sequence)
                inReorderQueueFunc = inReorderQueueTest
                in(packet)
              } else {
                var insertAtIndex = 0
                val length = inReorderQueue.length
                while (insertAtIndex < length && sequence >= inReorderQueue(insertAtIndex).sequence) {
                  insertAtIndex += 1
                }
                inReorderQueue.insert(insertAtIndex, InReorderEntry(packet, sequence, System.currentTimeMillis()))
                inReorderQueueFunc = processInReorderQueue
              }
            case Successful((packet, None)) =>
              in(packet)
            case Failure(e)                 =>
              log.error(s"could not decode packet: $e")
          }
          Behaviors.same

        case Send(packet) =>
          out(packet)
          Behaviors.same

        case ProcessQueue() =>
          processQueue()
          Behaviors.same

        case Teardown() =>
          send(TeardownConnection(clientNonce))
          context.self ! Close()
          Behaviors.same

        case Close() =>
          outQueue
            .dequeueAll(_ => true)
            .foreach(p => send(smp(slot = 0, p._2.bytes), Some(nextSequence), crypto))
          connectionClose()
      }
      .receiveSignal(onSignal)
  }

  val onSignal: PartialFunction[(ActorContext[Command], Signal), Behavior[Command]] = {
    case (_, PostStop) =>
      context.stop(nextActor)
      if(timesInReorderQueue > 0 || timesSubslotMissing > 0) {
        log.info(s"End of life note: out of sequence checks: $timesInReorderQueue, subslot missing checks: $timesSubslotMissing")
      }
      packetProcessor.cancel()
      Behaviors.same
  }

  /** Handle incoming packet */
  def in(packet: PlanetSidePacket): Behavior[Command] = {
    packet match {
      case packet: PlanetSideGamePacket =>
        nextActor ! packet
        Behaviors.same

      case packet: PlanetSideControlPacket =>
        packet match {
          case SlottedMetaPacket(slot, subslot, inner) =>
            //also send a confirmation packet after all requested packets are handled
            if (subslot > inSubslot + 1) {
              ((inSubslot + 1) until subslot).foreach { s => inSubslotsMissing.addOne((s, 0)) } //request missing SMP's
              inSubslot = subslot
              inSubslotsMissingFunc = processInSubslotsMissing
            } else if (subslot < inSubslot) {
              inSubslotsMissing.remove(subslot) //expedite this SMP
              inSubslotsMissingFunc = inSubslotsMissingTest
              if (inSubslotsMissing.isEmpty) {
                send(RelatedB(slot, inSubslot))
              }
            } else {
              send(RelatedB(slot, subslot))
              inSubslot = subslot
            }
            in(PacketCoding.decodePacket(inner))
            Behaviors.same

          case MultiPacket(packets) =>
            packets.foreach(p => in(PacketCoding.decodePacket(p)))
            Behaviors.same

          case MultiPacketEx(packets) =>
            packets.foreach(p => in(PacketCoding.decodePacket(p)))
            Behaviors.same

          case RelatedA(slot, subslot) =>
            val requestedSubslot = subslot - 1
            preparedSlottedMetaPackets.find(_.subslot == requestedSubslot) match {
              case Some(_packet) =>
                outQueueBundled.enqueue(_packet)
              case None if requestedSubslot < acceptedSmpSubslot =>
                log.warn(s"Client indicated an smp of slot $slot prior to $subslot that is no longer in the backlog")
              case None =>
                log.warn(s"Client indicated an smp of slot $slot prior to $subslot that is not in the backlog")
            }
            Behaviors.same

          case RelatedB(_, subslot) =>
            acceptedSmpSubslot = subslot
            Behaviors.same

          case ControlSync(diff, _, _, _, _, _, fa, fb) =>
            // TODO: figure out what this is what what it does for the PS client
            // I believe it has something to do with reliable packet transmission and resending

            // Work around the 2038 problem
            // TODO can we just start at 0 again? what is this for?
            val serverTick = math.min(System.currentTimeMillis(), 4294967295L)
            val nextDiff   = if (diff == 65535) 0 else diff + 1
            send(ControlSyncResp(nextDiff, serverTick, fa, fb, fb, fa))
            Behaviors.same

          case ConnectionClose() =>
            Behaviors.stopped

          case TeardownConnection(_) =>
            Behaviors.stopped

          case ClientStart(_) =>
            start()

          case other =>
            log.warn(s"Unhandled control packet '$other'")
            Behaviors.same
        }

      case packet: PlanetSideCryptoPacket =>
        log.error(s"Unexpected crypto packet '$packet'")
        Behaviors.same
    }
  }

  def in(packet: Attempt[PlanetSidePacket]): Unit = {
    packet match {
      case Successful(_packet) => in(_packet)
      case Failure(cause)      => log.error(cause.message)
    }
  }

  /** Handle outgoing packet */
  def out(packet: PlanetSidePacket): Unit = {
    packet match {
      case packet: KeepAliveMessage =>
        send(packet)

      case _ =>
        PacketCoding.encodePacket(packet) match {
          case Successful(payload) => outQueue.enqueue((packet, payload))
          case Failure(cause)      => log.error(cause.message)
        }
    }
  }

  /**
    * Periodically deal with these concerns:
    * the bundling and throttling of outgoing packets,
    * the reordering of out-of-sequence incoming packets,
    * and the resolution of missing incoming packets.
    * The latter two of these tasks are managed by function literals that may have no tasking assigned at the time.
    */
  def processQueue(): Unit = {
    processOutQueueBundle()
    inReorderQueueFunc()
    inSubslotsMissingFunc()
  }

//  def startInboundProcessor(): Unit = {
//    inboundProcessor =
//      context.system.scheduler.scheduleWithFixedDelay(inboundProcessorHz, inboundProcessorHz)(()=> {
//        context.self ! ProcessQueue()
//      })
//  }

  /**
    * Outbound packets that have not been caught by other guards need to be bundled
    * and placed into `SlottedMetaPacket` messages.
    * Multiple packets in one bundle have to be placed into a `MultiPacketEx` message wrapper
    * before being placed into the `SlottedMetaPacket`.
    * Packets that are too big for the MTU must go on to be split into smaller portions that will be wrapped individually.
    * Once queued, the first bundle is dispatched to the network.
    */
  def processOutQueueBundle(): Unit = {
    try {
      if (outQueueBundled.nonEmpty) {
        sendFirstBundle()
      } else if (outQueue.nonEmpty) {
        val bundle = {
          var length = 0L
          val (_, bundle) = outQueue
            .dequeueWhile {
              case (packet, payload) =>
                // packet length + MultiPacketEx header length
                val packetLength = payload.length + (
                  if (payload.length < 2048) { 8L } //256 * 8; 1L * 8
                  else if (payload.length < 524288) { 16L } //65536 * 8; 2L * 8
                  else { 32L } //4L * 8
                  )
                length += packetLength

                if (packetsBundledByThemselves.exists { _(packet) }) {
                  if (length == packetLength) {
                    length += MTU
                    true
                  } else {
                    false
                  }
                } else {
                  // Some packets may be larger than the MTU limit, in that case we dequeue anyway and split later
                  // We deduct some bytes to leave room for SlottedMetaPacket (4 bytes) and MultiPacketEx (2 bytes + prefix per packet)
                  length == packetLength || length <= (MTU - 6) * 8
                }
            }
            .unzip
          bundle
        }

        if (bundle.length == 1) {
          splitPacket(bundle.head) match {
            case Seq() =>
            //TODO is oversized packet recovery possible?
            case data =>
              outQueueBundled.enqueueAll(data)
              sendFirstBundle()
          }
        } else {
          PacketCoding.encodePacket(MultiPacketEx(bundle.toVector.map(_.bytes))) match {
            case Successful(data) =>
              outQueueBundled.enqueue(smp(slot = 0, data.bytes))
              sendFirstBundle()
            case Failure(cause)   =>
              log.error(cause.message)
              //to avoid packets being lost, unwrap bundle and queue the packets individually
              bundle.foreach { packet =>
                outQueueBundled.enqueue(smp(slot = 0, packet.bytes))
              }
              sendFirstBundle()
          }
        }
      }
    } catch {
      case e: Throwable =>
        log.error(s"outbound queue processing error - ${Option(e.getMessage).getOrElse(e.getClass.getSimpleName)}")
    }
  }

  /** what to periodically do about the inbound reorder queue */
  private var inReorderQueueFunc: ()=>Unit = MiddlewareActor.neutral

  /**
    * Examine inbound packets that need to be reordered by sequence number and
    * pass all packets that are now in the correct order and
    * pass packets that have been kept waiting for too long in the queue.
    * Set the recorded inbound sequence number to belong to the greatest packet removed from the queue.
    */
  def processInReorderQueue(): Unit = {
    timesInReorderQueue += 1
    var currentSequence  = inSequence
    val currentTime      = System.currentTimeMillis()
    val takenPackets = (inReorderQueue.indexWhere { currentTime - _.time > 50 } match {
      case -1 =>
        inReorderQueue
          .takeWhile { entry =>
            // Forward packet if next in sequence order
            if (entry.sequence == currentSequence + 1) {
              currentSequence = entry.sequence //already ordered by sequence, stays in order during traversal
              true
            } else {
              false
            }
          }
      case index =>
        // Forward all packets ahead of any packet that has been in the queue for 50ms
        val entries = inReorderQueue.take(index + 1)
        currentSequence = entries.last.sequence
        entries
    }).map(_.packet)
    inReorderQueue.dropInPlace(takenPackets.length)
    inSequence = currentSequence
    takenPackets.foreach { p =>
      inReorderQueueFunc = inReorderQueueTest
      in(p)
    }
  }

  /**
    * If the inbound reorder queue is empty, do no more work on it until work is available.
    * Otherwise, do work on whatever is at the front of the queue, and do no test again until explicitly requested.
    * Test the queue for contents after removing content from it.
    */
  def inReorderQueueTest(): Unit = {
    if (inReorderQueue.isEmpty) {
      inReorderQueueFunc = MiddlewareActor.neutral
    } else {
      inReorderQueueFunc = processInReorderQueue
      processInReorderQueue()
    }
  }

  /** what to periodically do about the missing inbound subslot map */
  private var inSubslotsMissingFunc: ()=>Unit = MiddlewareActor.neutral

  def processInSubslotsMissing(): Unit = {
    timesSubslotMissing += 1
    inSubslotsMissing.foreach {
      case (subslot, attempts) =>
        if (attempts <= 20) {
          // Send RelatedA less frequently, might want to put this on a separate timer
          if (attempts % 5 == 0) {
            send(RelatedA(0, subslot))
          }
          inSubslotsMissing(subslot) += 1
        } else {
          log.trace(s"requesting subslot $subslot from client failed")
          inSubslotsMissing.remove(subslot)
          inSubslotsMissingFunc = inSubslotsMissingTest
        }
    }
  }

  /**
    * If the inbound missing subslot map is empty, do no more work on it until work is available.
    * Otherwise, do work on whatever is in the map, and do no test again until explicitly requested.
    * Test the map for contents after removing content from it.
    */
  def inSubslotsMissingTest(): Unit = {
    if (inSubslotsMissing.isEmpty) {
      inSubslotsMissingFunc = MiddlewareActor.neutral
    } else {
      inSubslotsMissingFunc = processInSubslotsMissing
      processInSubslotsMissing()
    }
  }

  /**
    * Split packet into multiple chunks (if necessary).
    * Split packets are wrapped in a `HandleGamePacket` and sent as `SlottedMetaPacket4`.
    * The purpose of `SlottedMetaPacket4` may or may not be to indicate a split packet.
    */
  def splitPacket(packet: BitVector): Seq[PlanetSideControlPacket] = {
    if (packet.length > (MTU - 4) * 8) {
      PacketCoding.encodePacket(HandleGamePacket(packet.bytes)) match {
        case Successful(data) =>
          data.grouped((MTU - 8) * 8).map(vec => smp(slot = 4, vec.bytes)).toSeq
        case Failure(cause) =>
          log.error(cause.message)
          Seq()
      }
    } else {
      Seq(smp(slot = 0, packet.bytes))
    }
  }

  /**
    * Create a new `SlottedMetaPacket` with the sequence number filled in and the packet added to the history.
    * @param slot the slot for this packet, which influences the type of SMP
    * @param data hexadecimal data, the encoded packets to be placed in the SMP
    * @return the packet
    */
  def smp(slot: Int, data: ByteVector): SlottedMetaPacket = {
    val packet = SlottedMetaPacket(slot, nextSubslot, data)
    preparedSlottedMetaPackets.update(nextSmpIndex, packet)
    nextSmpIndex = (nextSmpIndex + 1) % smpHistoryLength
    packet
  }

  /**
    * Take the first fully-prepared packet from the queue of prepared packets and send it to the client.
    * Assign the dispatched packet the latest sequence number.
    * Do not call unless confident the the queue has at least one element.
    * @throws NoSuchElementException if there is no packet to dequeue
    * @see `SlottedMetaPacket`
    */
  def sendFirstBundle(): Unit = {
    send(outQueueBundled.dequeue(), Some(nextSequence), crypto)
  }

  def send(packet: PlanetSideControlPacket): ByteVector = {
    send(packet, if (crypto.isDefined) Some(nextSequence) else None, crypto)
  }

  def send(packet: PlanetSideCryptoPacket): ByteVector = {
    send(packet, Some(nextSequence), crypto)
  }

  def send(packet: PlanetSideGamePacket): ByteVector = {
    send(packet, Some(nextSequence), crypto)
  }

  def send(packet: PlanetSidePacket, sequence: Option[Int], crypto: Option[CryptoCoding]): ByteVector = {
    PacketCoding.marshalPacket(packet, sequence, crypto) match {
      case Successful(bits) =>
        val bytes = bits.toByteVector
        socket ! Udp.Send(bytes.toByteString, sender)
        bytes
      case Failure(e) =>
        log.error(s"Failed to encode packet ${packet.getClass.getSimpleName}: $e")
        ByteVector.empty
    }
  }

  /**
    * A random series of bytes used as a challenge value.
    * @param amount the number of bytes
    * @return a random series of bytes
    */
  def randomBytes(amount: Int): ByteVector = {
    val array = Array.ofDim[Byte](amount)
    random.nextBytes(array)
    ByteVector.view(array)
  }

  /**
    * End client-server ops.
    * End messaging capabilities.
    */
  def connectionClose(): Behavior[Command] = {
    send(ConnectionClose())
    Behaviors.stopped
  }
}
