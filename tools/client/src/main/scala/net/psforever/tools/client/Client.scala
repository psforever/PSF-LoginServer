package net.psforever.tools.client

import java.net.{DatagramPacket, DatagramSocket, InetSocketAddress}
import java.security.{SecureRandom, Security}
import akka.actor.typed.ActorRef
import akka.io.Udp
import net.psforever.packet.{
  CryptoPacketOpcode,
  PacketCoding,
  PlanetSideControlPacket,
  PlanetSideCryptoPacket,
  PlanetSideGamePacket,
  PlanetSidePacket
}
import net.psforever.packet.PacketCoding.CryptoCoding
import net.psforever.packet.control.{
  ClientStart,
  ConnectionClose,
  HandleGamePacket,
  MultiPacketEx,
  ServerStart,
  SlottedMetaPacket
}
import net.psforever.packet.crypto.{ClientChallengeXchg, ClientFinished, ServerChallengeXchg, ServerFinished}
import net.psforever.packet.game.{
  BeginZoningMessage,
  CharacterInfoMessage,
  CharacterRequestAction,
  CharacterRequestMessage,
  ConnectToWorldRequestMessage,
  KeepAliveMessage,
  LoadMapMessage,
  LoginMessage,
  LoginRespMessage,
  PlayerStateMessageUpstream,
  VNLWorldStatusMessage,
  WorldInformation
}
import net.psforever.tools.client.State.Connection
import net.psforever.util.{DiffieHellman, Md5Mac}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import scodec.{Attempt, Err}
import scodec.Attempt.{Failure, Successful}
import scodec.bits._

import javax.crypto.spec.SecretKeySpec
import scala.collection.mutable
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.reflect.ClassTag
import java.util.concurrent.{Executors, TimeUnit}

object Client {
  Security.addProvider(new BouncyCastleProvider)

  private[this] val log = org.log4s.getLogger

  def main(args: Array[String]): Unit = {
    val client = new Client("test", "test")
    client.login(new InetSocketAddress("localhost", 51000))
    client.joinWorld(client.state.worlds.head)
    client.selectCharacter(client.state.characters.head.charId)
    client.startTasks()

    while (true) {
      client.updateAvatar(client.state.avatar.copy(crouching = !client.state.avatar.crouching))
      Thread.sleep(2000)
      //Thread.sleep(Int.MaxValue)
    }
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

  private var _state = State()
  def state: State   = _state

  private[this] val log = org.log4s.getLogger

  private var socket: Option[DatagramSocket]     = None
  private var ref: Option[ActorRef[Udp.Message]] = None
  private var crypto: Option[CryptoCoding]       = None
  private val buffer                             = new Array[Byte](65535)
  val random                                     = new SecureRandom()

  private val inQueue: mutable.Queue[PlanetSidePacket]            = mutable.Queue()
  private val splitPackets: mutable.ArrayDeque[(Int, ByteVector)] = mutable.ArrayDeque()

  private val scheduler = Executors.newScheduledThreadPool(2)

  /** Establish encrypted connection */
  private def setupConnection(): Unit = {
    assert(state.connection == Connection.Disconnected)
    var macBuffer: ByteVector = ByteVector.empty

    send(ClientStart(0)).require
    val serverStart = waitFor[ServerStart]().require
    assert(serverStart.clientNonce == 0)

    val time            = System.currentTimeMillis() / 1000
    val randomChallenge = randomBytes(12)
    val clientChallenge = ServerChallengeXchg.getCompleteChallenge(time, randomChallenge)
    val p               = randomBytes(16)
    val g               = ByteVector(1.toByte).reverse.padTo(16).reverse
    val dh              = DiffieHellman(p.toArray, g.toArray)
    send(ClientChallengeXchg(time, randomChallenge, p, g)).require
    val serverChallengeMsg = waitFor[ServerChallengeXchg](CryptoPacketOpcode.ServerChallengeXchg).require

    val serverChallenge =
      ServerChallengeXchg.getCompleteChallenge(serverChallengeMsg.time, serverChallengeMsg.challenge)
    val agreedKey = dh.agree(serverChallengeMsg.pubKey.toArray)

    val agreedMessage = ByteVector("master secret".getBytes) ++ clientChallenge ++
      hex"00000000" ++ serverChallenge ++ hex"00000000"
    val masterSecret = new Md5Mac(ByteVector.view(agreedKey)).updateFinal(agreedMessage)
    val mac          = new Md5Mac(masterSecret)
    val serverExpansion = ByteVector.view("server expansion".getBytes) ++ hex"0000" ++ serverChallenge ++
      hex"00000000" ++ clientChallenge ++ hex"00000000"
    val clientExpansion = ByteVector.view("client expansion".getBytes) ++ hex"0000" ++ serverChallenge ++
      hex"00000000" ++ clientChallenge ++ hex"00000000"
    val serverKey = mac.updateFinal(serverExpansion, 64)
    val clientKey = mac.updateFinal(clientExpansion, 64)

    send(ClientFinished(16, ByteVector.view(dh.publicKey), ByteVector.empty)).require
    crypto = Some(
      CryptoCoding(
        new SecretKeySpec(clientKey.take(20).toArray, "RC5"),
        new SecretKeySpec(serverKey.take(20).toArray, "RC5"),
        clientKey.slice(20, 36),
        serverKey.slice(20, 36)
      )
    )
    waitFor[ServerFinished](CryptoPacketOpcode.ServerFinished).require
  }

  /** Login using given host address */
  def login(host: InetSocketAddress): Unit = {
    val sock = new DatagramSocket()
    sock.setSoTimeout(10000)
    sock.connect(host)
    socket = Some(sock)
    login()
  }

  /** Login using given actor ref */
  def login(ref: ActorRef[Udp.Message]): Unit = {
    this.ref = Some(ref)
    login()
  }

  private def login(): Unit = {
    setupConnection()
    send(LoginMessage(0, 0, "", username, Some(password), None, 0)).require
    waitFor[LoginRespMessage]().require
    waitFor[VNLWorldStatusMessage]().require
    assert(state.connection == Connection.WorldSelection)
    disconnect()
  }

  def disconnect(): Unit = {
    send(ConnectionClose()).require
    socket match {
      case Some(socket) => socket.disconnect()
      case _            => ???
    }
    crypto = None
    // Server does not send any confirmation for ConnectionClose
    _state = state.copy(connection = Connection.Disconnected)
  }

  /** Join world */
  def joinWorld(world: WorldInformation): Unit = {
    socket match {
      case Some(_) =>
        val sock = new DatagramSocket()
        sock.setSoTimeout(60000)
        log.info(s"joinWorld ${world.connections.head.address}")
        sock.connect(world.connections.head.address)
        socket = Some(sock)
      case _ => ???
    }
    setupConnection()
    send(ConnectToWorldRequestMessage("", state.token.get, 0, 0, 0, "", 0)).require
    waitFor[CharacterInfoMessage]().require
  }

  def selectCharacter(charId: Long): Unit = {
    assert(state.connection == Connection.AvatarSelection)
    send(CharacterRequestMessage(charId, CharacterRequestAction.Select)).require
    waitFor[LoadMapMessage](timeout = 15.seconds).require
  }

  def createCharacter(): Unit = {
    ???
  }

  def deleteCharacter(charId: Long): Unit = {
    ??? // never been tested
    assert(state.connection == Connection.AvatarSelection)
    send(CharacterRequestMessage(charId, CharacterRequestAction.Delete)).require
  }

  def updateAvatar(avatar: State.Avatar): Unit = {
    this._state = this.state.copy(avatar = avatar)
  }

  /** Start processing tasks. Must be run after login/joinWorld. */
  def startTasks(): Unit = {
    scheduler.scheduleAtFixedRate(new Runnable() { override def run(): Unit = tick() }, 0, 250, TimeUnit.MILLISECONDS)

    scheduler.scheduleAtFixedRate(
      new Runnable() {
        override def run(): Unit = {
          receive().foreach {
            case Failure(cause) => log.error(s"receive error: ${cause}")
            case _              => ()
          }
          while (inQueue.nonEmpty) {
            process()
          }
        }
      },
      0,
      10,
      TimeUnit.MILLISECONDS
    )
  }

  /** Stop auto processing tasks. */
  def stopTasks(): Unit = {
    scheduler.shutdown()
  }

  /** recurring task used for keep alive and state updates */
  private def tick(): Unit = {
    send(KeepAliveMessage())
    (state.avatar.guid, state.avatar.position) match {
      case (Some(guid), Some(pos)) =>
        send(
          PlayerStateMessageUpstream(
            guid,
            pos,
            state.avatar.velocity,
            state.avatar.yaw,
            state.avatar.pitch,
            state.avatar.yawUpper,
            0,
            0,
            state.avatar.crouching,
            state.avatar.jumping,
            jump_thrust = false,
            state.avatar.cloaked,
            0,
            0
          )
        )
      case _ =>
        log.warn("not ready, skipping PlayerStateMessageUpstream")
    }
  }

  /** Process next queued packet */
  def process(): (State, Option[PlanetSidePacket]) = {
    if (inQueue.nonEmpty) {
      val packet = inQueue.dequeue()
      _process(packet)
      (state, Some(packet))
    } else {
      (state, None)
    }
  }

  /** Process next queued packet matching predicate */
  def processFirst(p: PlanetSidePacket => Boolean): (State, Option[PlanetSidePacket]) = {
    if (inQueue.nonEmpty) {
      val packet = inQueue.dequeueFirst(p)
      if (packet.isDefined) {
        _process(packet.get)
      }
      (state, packet)
    } else {
      (state, None)
    }
  }

  private def _process(packet: PlanetSidePacket): Unit = {
    packet match {
      case _: KeepAliveMessage => ()
      case _: LoadMapMessage =>
        log.info(s"process: ${packet}")
        send(BeginZoningMessage()).require
        _state = state.update(packet)
      case packet: PlanetSideGamePacket =>
        _state = state.update(packet)
        log.info(s"process: ${packet}")
        ()
      case _ => ()
    }
  }

  private def waitFor[T <: PlanetSidePacket: ClassTag](
      cryptoState: CryptoPacketOpcode.Type = CryptoPacketOpcode.Ignore,
      timeout: FiniteDuration = 5.seconds
  ): Attempt[T] = {
    val time            = System.currentTimeMillis()
    var res: Attempt[T] = Failure(Err("timeout"))
    while (res.isFailure && System.currentTimeMillis() - time < timeout.toMillis) {
      receive(cryptoState).foreach {
        case Failure(cause) =>
          res = Failure(cause)
        case _ => ()
      }
      processFirst {
        case packet if implicitly[ClassTag[T]].runtimeClass.isInstance(packet) => true
        case _                                                                 => false
      } match {
        case (_, Some(packet: T)) =>
          res = Successful(packet)
        case _ => ()
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
    packet match {
      case _: KeepAliveMessage => ()
      case _                   => log.info(s"send: ${packet}")
    }
    PacketCoding.marshalPacket(packet, sequence, crypto) match {
      case Successful(payload) =>
        send(payload.toByteArray)
        Successful(payload)
      case f: Failure =>
        f
    }
  }

  private def send(payload: Array[Byte]): Unit = {
    (socket, ref) match {
      case (Some(socket), _) =>
        socket.send(new DatagramPacket(payload, payload.length))
      case (_, Some(ref)) =>
        // ref ! Udp.Received(ByteString(payload), new InetSocketAddress(socket.getInetAddress, socket.getPort))
        ???
      case _ => ???
    }
  }

  def receive(
      cryptoState: CryptoPacketOpcode.Type = CryptoPacketOpcode.Ignore
  ): Seq[Attempt[PlanetSidePacket]] = {
    (socket, ref) match {
      case (Some(socket), _) =>
        try {
          val p = new DatagramPacket(buffer, buffer.length)
          socket.receive(p)
          val data = ByteVector.view(p.getData).drop(p.getOffset).take(p.getLength)
          PacketCoding.unmarshalPacket(data, crypto, cryptoState) match {
            case Successful((packet, sequence)) =>
              unwrapPacket(packet, sequence).map {
                case Successful(packet) =>
                  inQueue.enqueue(packet)
                  Successful(packet)
                case Failure(cause) =>
                  Failure(cause)
              }
            case Failure(cause) =>
              Seq(Failure(cause))
          }

        } catch {
          case e: Throwable => Seq(Failure(Err(e.getMessage)))
        }
      case _ => ???
    }
  }

  private def unwrapPacket(packet: PlanetSidePacket, sequence: Option[Int]): Seq[Attempt[PlanetSidePacket]] = {
    packet match {
      case SlottedMetaPacket(slot, _, data) if slot != 4 =>
        PacketCoding.decodePacket(data) match {
          case Successful(packet) => unwrapPacket(packet, sequence)
          case Failure(cause)     => Seq(Failure(cause))
        }
      // SMP4 should be split packet
      case SlottedMetaPacket(slot, _, data) if slot == 4 =>
        PacketCoding.decodePacket(data) match {
          case Successful(HandleGamePacket(_, _, _)) =>
            splitPackets += ((sequence.get, data))
            tryMergePackets()
            Seq()
          case Successful(packet) => unwrapPacket(packet, sequence)
          case Failure(_) if sequence.isDefined =>
            splitPackets += ((sequence.get, data))
            tryMergePackets()
            Seq()
          case Failure(cause) => Seq(Failure(cause))
        }
      case MultiPacketEx(data) =>
        data.flatMap { data =>
          PacketCoding.decodePacket(data) match {
            case Successful(packet) => unwrapPacket(packet, sequence)
            case Failure(cause)     => Seq(Failure(cause))
          }
        }
      case p => Seq(Successful(p))
    }
  }

  private def tryMergePackets(): Unit = {
    splitPackets.foreach {
      case (sequence, data) =>
        PacketCoding.decodePacket(data) match {
          case Successful(HandleGamePacket(len, bytes, _)) =>
            val data =
              ByteVector.view(bytes.toArray ++ splitPackets.filter(_._1 > sequence).sortBy(_._1).flatMap(_._2.toArray))
            if (data.length == len) {
              PacketCoding.decodePacket(data) match {
                case Successful(packet) =>
                  inQueue.enqueue(packet)
                  // may silently remove old incomplete packets but there is no proper solution here
                  splitPackets.removeAll()
                case Failure(cause) => ???
              }
            }
          case _ => ()
        }
    }
  }

  private def randomBytes(amount: Int): ByteVector = {
    val array = Array.ofDim[Byte](amount)
    random.nextBytes(array)
    ByteVector.view(array)
  }
}
