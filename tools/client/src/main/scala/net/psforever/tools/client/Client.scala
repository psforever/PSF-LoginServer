package net.psforever.tools.client

import java.net.{DatagramPacket, DatagramSocket, InetSocketAddress}
import java.security.{SecureRandom, Security}

import akka.actor.typed.ActorRef
import akka.io.Udp
import enumeratum.{Enum, EnumEntry}
import net.psforever.packet.{
  CryptoPacketOpcode,
  PacketCoding,
  PlanetSideControlPacket,
  PlanetSideCryptoPacket,
  PlanetSideGamePacket,
  PlanetSidePacket
}
import net.psforever.packet.PacketCoding.CryptoCoding
import net.psforever.packet.control.{ClientStart, ServerStart}
import net.psforever.packet.crypto.{ClientChallengeXchg, ServerChallengeXchg}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import scodec.{Attempt, Err}
import scodec.Attempt.{Failure, Successful}
import scodec.bits._

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Client {
  Security.addProvider(new BouncyCastleProvider)

  def main(args: Array[String]): Unit = {
    val client = new Client("test", "test")
    client.login(new InetSocketAddress("localhost", 51000))
  }

  sealed trait ClientState extends EnumEntry

  object ClientState extends Enum[ClientState] {

    case object Disconnected    extends ClientState
    case object WorldSelection  extends ClientState
    case object AvatarSelection extends ClientState
    case object AvatarCreation  extends ClientState

    val values: IndexedSeq[ClientState] = findValues

  }
}

class Client(username: String, password: String) {
  import Client._

  private var sequence = 0
  private def nextSequence = {
    val r = sequence
    sequence += 1
    r
  }

  private val socket = new DatagramSocket()
  socket.setSoTimeout(1000)
  private var host: Option[InetSocketAddress]    = None
  private var ref: Option[ActorRef[Udp.Message]] = None
  private var crypto: Option[CryptoCoding]       = None
  private val buffer                             = new Array[Byte](65535)
  val random                                     = new SecureRandom()

  private var _state: ClientState = ClientState.Disconnected
  def state: ClientState          = _state

  /** Login using given host address */
  def login(host: InetSocketAddress): Unit = {
    this.host = Some(host)
    login()
  }

  /** Login using given actor ref */
  /*
  def login(ref: ActorRef[Udp.Message]): Unit = {
    this.ref = Some(ref)
    login()
  }
   */

  private def login() = {
    assert(state == ClientState.Disconnected)
    var macBuffer: ByteVector = ByteVector.empty

    send(ClientStart(0))
    val serverStart = waitFor[ServerStart]().require
    assert(serverStart.clientNonce == 0)

    val time      = System.currentTimeMillis()
    val challenge = randomBytes(12)
    val p         = randomBytes(16)
    val g         = ByteVector(1.toByte).reverse.padTo(16).reverse
    send(ClientChallengeXchg(time, challenge, p, g))

    val serverKey = waitFor[ServerChallengeXchg]().require.pubKey
  }

  private def waitFor[T](
      cryptoState: CryptoPacketOpcode.Type = CryptoPacketOpcode.Ignore,
      timeout: FiniteDuration = 5.seconds
  ): Attempt[T] = {
    val time            = System.currentTimeMillis()
    var res: Attempt[T] = Failure(Err("timeout"))
    while (res.isFailure && System.currentTimeMillis() - time < timeout.toMillis) {
      receive(cryptoState) match {
        case Successful((packet, sequence)) =>
          packet match {
            case packet: T => res = Successful(packet)
            case p =>
              println(s"receive: ${p}")
              ()
          }
        case Failure(cause) => ???

      }
    }
    res
  }

  def send(packet: PlanetSideControlPacket): Attempt[BitVector] = {
    send(packet, if (crypto.isDefined) Some(nextSequence) else None, crypto)
  }

  def send(packet: PlanetSideCryptoPacket): Attempt[BitVector] = {
    send(packet, Some(nextSequence), crypto)
  }

  def send(packet: PlanetSideGamePacket): Attempt[BitVector] = {
    send(packet, Some(nextSequence), crypto)
  }

  private def send(
      packet: PlanetSidePacket,
      sequence: Option[Int],
      crypto: Option[CryptoCoding]
  ): Attempt[BitVector] = {
    PacketCoding.marshalPacket(packet, sequence, crypto) match {
      case Successful(payload) =>
        send(payload.toByteArray)
        Successful(payload)
      case f: Failure =>
        f
    }
  }

  private def send(payload: Array[Byte]): Unit = {
    (host, ref) match {
      case (Some(host), None) =>
        socket.send(new DatagramPacket(payload, payload.length, host))
      case (None, Some(ref)) =>
      // ref ! Udp.Received(ByteString(payload), new InetSocketAddress(socket.getInetAddress, socket.getPort))
      case _ => ;
    }
  }

  private def receive(
      cryptoState: CryptoPacketOpcode.Type = CryptoPacketOpcode.Ignore
  ): Attempt[(PlanetSidePacket, Option[Int])] = {
    try {
      val p = new DatagramPacket(buffer, buffer.length)
      socket.receive(p)
      PacketCoding.unmarshalPacket(ByteVector.view(p.getData), crypto, cryptoState)
    } catch {
      case e: Throwable => Failure(Err(e.getMessage))
    }
  }

  private def randomBytes(amount: Int): ByteVector = {
    val array = Array.ofDim[Byte](amount)
    random.nextBytes(array)
    ByteVector.view(array)
  }
}
