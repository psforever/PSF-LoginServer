package net.psforever.actors.net

import java.net.InetSocketAddress
import java.security.{SecureRandom, Security}

import akka.actor.Cancellable
import akka.actor.typed.{ActorRef, ActorTags, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.io.Udp
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
import scodec.Attempt.{Failure, Successful}
import scodec.bits.{BitVector, ByteVector, HexStringSyntax}
import scodec.interop.akka.EnrichedByteVector
import javax.crypto.spec.SecretKeySpec
import net.psforever.packet.PacketCoding.CryptoCoding
import net.psforever.util.{DiffieHellman, Md5Mac}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import scodec.Attempt

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/** MiddlewareActor sits between the raw UDP socket and the "main" actors (either login or session) and handles
  * crypto and control packets. This means it sets up cryptography, it decodes incoming packets,
  * it encodes, bundles and splits outgoing packets, it handles things like requesting/resending lost packets and more.
  */
object MiddlewareActor {
  Security.addProvider(new BouncyCastleProvider)

  /** Maximum packet size in bytes */
  //final val MTU: Int = 467
  final val MTU: Int = 440

  def apply(
      socket: ActorRef[Udp.Command],
      sender: InetSocketAddress,
      next: (ActorRef[Command], String) => Behavior[PlanetSidePacket],
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
}

class MiddlewareActor(
    context: ActorContext[MiddlewareActor.Command],
    socket: ActorRef[Udp.Command],
    sender: InetSocketAddress,
    next: (ActorRef[MiddlewareActor.Command], String) => Behavior[PlanetSidePacket],
    connectionId: String
) {

  import MiddlewareActor._

  implicit val ec: ExecutionContextExecutor = context.executionContext

  private[this] val log = org.log4s.getLogger

  var clientNonce: Long = 0

  var serverMACBuffer: ByteVector = ByteVector.empty

  val random = new SecureRandom()

  var crypto: Option[CryptoCoding] = None

  val nextActor: ActorRef[PlanetSidePacket] =
    context.spawnAnonymous(next(context.self, connectionId), ActorTags(s"id=${connectionId}"))

  /** Queue of incoming packets (plus sequence numbers and timestamps) that arrived in the wrong order */
  val inReorderQueue: ListBuffer[(PlanetSidePacket, Int, Long)] = ListBuffer()

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

  /** Latest outgoing sequence number */
  var outSequence = 0

  def nextSequence: Int = {
    val r = outSequence
    if (outSequence == 0xffff) {
      outSequence = 0
    } else {
      outSequence += 1
    }
    r
  }

  /** Latest outgoing subslot number */
  var outSubslot = 0

  def nextSubslot: Int = {
    val r = outSubslot
    if (outSubslot == 0xffff) {
      outSubslot = 0
    } else {
      outSubslot += 1
    }
    r
  }

  /** Create a new SlottedMetaPacket with the sequence number filled in and the packet added to the history */
  def smp(slot: Int, data: ByteVector): SlottedMetaPacket = {
    if (outSlottedMetaPackets.length > 100) {
      outSlottedMetaPackets = outSlottedMetaPackets.takeRight(100)
    }
    val packet = SlottedMetaPacket(slot, nextSubslot, data)
    outSlottedMetaPackets += packet
    packet
  }

  /** History of sent SlottedMetaPackets in case the client requests missing SMP packets via a RelatedA packet. */
  var outSlottedMetaPackets: ListBuffer[SlottedMetaPacket] = ListBuffer()

  /** Timer that handles the bundling and throttling of outgoing packets and the reordering of incoming packets */
  val queueProcessor: Cancellable = {
    context.system.scheduler.scheduleWithFixedDelay(10.milliseconds, 10.milliseconds)(() => {
      try {
        if (outQueue.nonEmpty && outQueueBundled.isEmpty) {
          var length = 0L
          val bundle = outQueue
            .dequeueWhile {
              case (packet, payload) =>
                // packet length + MultiPacketEx prefix length
                val packetLength = payload.length + (if (payload.length < 256 * 8) { 1L * 8 }
                                                     else if (payload.length < 65536 * 8) { 2L * 8 }
                                                     else { 4L * 8 })
                length += packetLength

                packet match {
                  // Super awkward special case: Bundling CharacterInfoMessage with OCDM causes the character selection
                  // to show blank lines and be broken. So we make sure CharacterInfoMessage is always sent as the only
                  // packet in a bundle.
                  case _: CharacterInfoMessage =>
                    if (length == packetLength) {
                      length += MTU
                      true
                    } else {
                      false
                    }
                  case _ =>
                    // Some packets may be larger than the MTU limit, in that case we dequeue anyway and split later
                    // We deduct some bytes to leave room for SlottedMetaPacket (4 bytes) and MultiPacketEx (2 bytes + prefix per packet)
                    length == packetLength || length <= (MTU - 6) * 8
                }
            }
            .map(_._2)

          if (bundle.length == 1) {
            outQueueBundled.enqueueAll(splitPacket(bundle.head))
          } else {
            PacketCoding.encodePacket(MultiPacketEx(bundle.toVector.map(_.bytes))) match {
              case Successful(data) => outQueueBundled.enqueue(smp(0, data.bytes))
              case Failure(cause)   => log.error(cause.message)
            }
          }
        }

        outQueueBundled.dequeueFirst(_ => true) match {
          case Some(packet) => send(packet, Some(nextSequence), crypto)
          case None         => ()
        }

        if (inReorderQueue.nonEmpty) {
          var currentSequence = inSequence
          val currentTime     = System.currentTimeMillis()
          inReorderQueue
            .sortBy(_._2)
            .dropWhile {
              case (_, sequence, time) =>
                // Forward packet if next in sequence order or older than 20ms
                if (sequence == currentSequence + 1 || currentTime - time > 20) {
                  currentSequence += 1
                  true
                } else {
                  false
                }
            }
            .foreach {
              case (packet, sequence, _) =>
                if (sequence > inSequence) {
                  inSequence = sequence
                }
                in(packet)
            }
        }

        if (inSubslotsMissing.nonEmpty) {
          inSubslotsMissing.foreach {
            case (subslot, attempts) =>
              if (attempts <= 50) {
                // Slight hack to send RelatedA less frequently, might want to put this on a separate timer
                if (attempts % 10 == 0) send(RelatedA(0, subslot))
                inSubslotsMissing(subslot) += 1
              } else {
                log.warn(s"Requesting subslot '$subslot' from client failed")
                inSubslotsMissing.remove(subslot)
              }
          }
        }
      } catch {
        case e: Throwable => log.error(e)("Queue processing error")
      }
    })
  }

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
                log.error(s"Unexpected packet type $packet in init")
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
                    // ignore
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
              log.error(s"Could not decode packet in cryptoSetup: ${e}")
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

  def active(): Behavior[Command] = {
    Behaviors
      .receiveMessage[Command] {
        case Receive(msg) =>
          PacketCoding.unmarshalPacket(msg, crypto) match {
            case Successful((packet, Some(sequence))) =>
              if (sequence == inSequence + 1) {
                inSequence = sequence
                in(packet)
              } else {
                inReorderQueue.addOne((packet, sequence, System.currentTimeMillis()))
              }
            case Successful((packet, None)) => in(packet)
            case Failure(e)                 => log.error(s"Could not decode packet: ${e}")
          }
          Behaviors.same

        case Send(packet) =>
          out(packet)
          Behaviors.same

        case Teardown() =>
          send(TeardownConnection(clientNonce))
          context.self ! Close()
          Behaviors.same

        case Close() =>
          outQueue
            .dequeueAll(_ => true)
            .foreach(p => send(smp(0, p._2.bytes), Some(nextSequence), crypto))
          connectionClose()
      }
      .receiveSignal(onSignal)
  }

  val onSignal: PartialFunction[(ActorContext[Command], Signal), Behavior[Command]] = {
    case (_, PostStop) =>
      context.stop(nextActor)
      queueProcessor.cancel()
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
            if (subslot > inSubslot + 1) {
              ((inSubslot + 1) until subslot).foreach(s => inSubslotsMissing.addOne((s, 0)))
            } else if (inSubslotsMissing.contains(subslot)) {
              inSubslotsMissing.remove(subslot)
            } else if (inSubslotsMissing.isEmpty) {
              send(RelatedB(slot, subslot))
            }
            if (subslot > inSubslot) {
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
            log.info(s"Client indicated a packet is missing prior to slot '$slot' and subslot '$subslot'")
            outSlottedMetaPackets.find(_.subslot == subslot - 1) match {
              case Some(packet) => outQueueBundled.enqueue(packet)
              case None         => log.warn(s"Client requested unknown subslot '$subslot'")
            }
            Behaviors.same

          case RelatedB(_, subslot) =>
            outSlottedMetaPackets = outSlottedMetaPackets.filter(_.subslot > subslot)
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
      case Successful(packet) => in(packet)
      case Failure(cause)     => log.error(cause.message)
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
        log.error(s"Failed to encode packet ${packet.getClass.getName}: $e")
        ByteVector.empty
    }
  }

  def randomBytes(amount: Int): ByteVector = {
    val array = Array.ofDim[Byte](amount)
    random.nextBytes(array)
    ByteVector.view(array)
  }

  def connectionClose(): Behavior[Command] = {
    send(ConnectionClose())
    Behaviors.stopped
  }

  /** Split packet into multiple chunks (if necessary)
    * Split packets are wrapped in a HandleGamePacket and sent as SlottedMetaPacket4
    * The purpose of SlottedMetaPacket4 may or may not be to indicate a split packet
    */
  def splitPacket(packet: BitVector): Seq[PlanetSideControlPacket] = {
    if (packet.length > (MTU - 4) * 8) {
      PacketCoding.encodePacket(HandleGamePacket(packet.bytes)) match {
        case Successful(data) =>
          data.grouped((MTU - 8) * 8).map(vec => smp(4, vec.bytes)).toSeq
        case Failure(cause) =>
          log.error(cause.message)
          Seq()
      }
    } else {
      Seq(smp(0, packet.bytes))
    }
  }

}
