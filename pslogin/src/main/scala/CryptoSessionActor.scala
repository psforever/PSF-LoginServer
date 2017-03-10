// Copyright (c) 2017 PSForever
import java.net.{InetAddress, InetSocketAddress}

import akka.actor.{Actor, ActorLogging, ActorRef, DiagnosticActorLogging, Identify, MDCContextAware}
import net.psforever.crypto.CryptoInterface.{CryptoState, CryptoStateWithMAC}
import net.psforever.crypto.CryptoInterface
import net.psforever.packet._
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import scodec.{Attempt, Codec, Err}
import scodec.codecs.{bytes, uint16L, uint8L}
import java.security.SecureRandom

import net.psforever.packet.control.{ClientStart, ServerStart, TeardownConnection}
import net.psforever.packet.crypto._
import net.psforever.packet.game.PingMsg
import org.log4s.MDC
import MDCContextAware.Implicits._

sealed trait CryptoSessionAPI
final case class DropCryptoSession() extends CryptoSessionAPI

/**
  * Actor that stores crypto state for a connection, appropriately encrypts and decrypts packets,
  * and passes packets along to the next hop once processed.
  */
class CryptoSessionActor extends Actor with MDCContextAware {
  private[this] val log = org.log4s.getLogger

  var sessionId : Long = 0
  var leftRef : ActorRef = ActorRef.noSender
  var rightRef : ActorRef = ActorRef.noSender

  var cryptoDHState : Option[CryptoInterface.CryptoDHState] = None
  var cryptoState : Option[CryptoInterface.CryptoStateWithMAC] = None
  val random = new SecureRandom()

  // crypto handshake state
  var serverChallenge = ByteVector.empty
  var serverChallengeResult = ByteVector.empty
  var serverMACBuffer = ByteVector.empty

  var clientPublicKey = ByteVector.empty
  var clientChallenge = ByteVector.empty
  var clientChallengeResult = ByteVector.empty

  var clientNonce : Long = 0
  var serverNonce : Long = 0

  // Don't leak crypto object memory even on an exception
  override def postStop() = {
    cleanupCrypto()
  }

  def receive = Initializing

  def Initializing : Receive = {
    case HelloFriend(sessionId, right) =>
      import MDCContextAware.Implicits._
      this.sessionId = sessionId
      leftRef = sender()
      rightRef = right.asInstanceOf[ActorRef]

      // who ever we send to has to send something back to us
      rightRef !> HelloFriend(sessionId, self)

      log.trace(s"Left sender ${leftRef.path.name}")

      context.become(NewClient)
    case default =>
      log.error("Unknown message " + default)
      context.stop(self)
  }

  def NewClient : Receive = {
    case RawPacket(msg) =>
      PacketCoding.UnmarshalPacket(msg) match {
        case Successful(p) =>
          log.trace("Initializing -> NewClient")

          p match {
            case ControlPacket(_, ClientStart(nonce)) =>
              clientNonce = nonce
              serverNonce = Math.abs(random.nextInt())
              sendResponse(PacketCoding.CreateControlPacket(ServerStart(nonce, serverNonce)))

              context.become(CryptoExchange)
            case default =>
              log.error(s"Unexpected packet type ${p} in state NewClient")
          }
        case Failure(e) =>
          // There is a special case where no crypto is being used.
          // The only packet coming through looks like PingMsg. This is a hardcoded
          // feature of the client @ 0x005FD618
          PacketCoding.DecodePacket(msg) match {
            case Successful(packet) =>
              packet match {
                case ping @ PingMsg(_, _) =>
                  // reflect the packet back to the sender
                  sendResponse(ping)
                case default => log.error(s"Unexpected non-crypto packet type ${packet} in state NewClient")
              }
            case Failure(e) =>
              log.error("Could not decode packet: " + e + s" in state NewClient")
          }
      }
    case default => log.error(s"Invalid message '$default' received in state NewClient")
  }

  def CryptoExchange : Receive = {
    case RawPacket(msg) =>
      PacketCoding.UnmarshalPacket(msg, CryptoPacketOpcode.ClientChallengeXchg) match {
        case Failure(e) => log.error("Could not decode packet in state CryptoExchange: " + e)
        case Successful(p) =>
          log.trace("NewClient -> CryptoExchange")

          p match {
            case CryptoPacket(seq, ClientChallengeXchg(time, challenge, p, g)) =>
              cryptoDHState = Some(new CryptoInterface.CryptoDHState())

              val dh = cryptoDHState.get

              // initialize our crypto state from the client's P and G
              dh.start(p, g)

              // save the client challenge
              clientChallenge = ServerChallengeXchg.getCompleteChallenge(time, challenge)

              // save the packet we got for a MAC check later. drop the first 3 bytes
              serverMACBuffer ++= msg.drop(3)

              val serverTime = System.currentTimeMillis() / 1000L
              val randomChallenge = getRandBytes(0xc)

              // store the complete server challenge for later
              serverChallenge = ServerChallengeXchg.getCompleteChallenge(serverTime, randomChallenge)

              val packet = PacketCoding.CreateCryptoPacket(seq,
                ServerChallengeXchg(serverTime, randomChallenge, dh.getPublicKey))

              val sentPacket = sendResponse(packet)

              // save the sent packet a MAC check
              serverMACBuffer ++= sentPacket.drop(3)

              context.become(CryptoSetupFinishing)
            case default => log.error(s"Unexpected packet type $p in state CryptoExchange")
          }
      }
    case default => log.error(s"Invalid message '$default' received in state CryptoExchange")
  }

  def CryptoSetupFinishing : Receive = {
    case RawPacket(msg) =>
      PacketCoding.UnmarshalPacket(msg, CryptoPacketOpcode.ClientFinished) match {
        case Failure(e) => log.error("Could not decode packet in state CryptoSetupFinishing: " + e)
        case Successful(p) =>
          log.trace("CryptoExchange -> CryptoSetupFinishing")

          p match {
            case CryptoPacket(seq, ClientFinished(clientPubKey, clientChalResult)) =>
              clientPublicKey = clientPubKey
              clientChallengeResult = clientChalResult

              // save the packet we got for a MAC check later
              serverMACBuffer ++= msg.drop(3)

              val dh = cryptoDHState.get
              val agreedValue = dh.agree(clientPublicKey)

              // we are now done with the DH crypto object
              dh.close

              /*println("Agreed: " + agreedValue)
              println(s"Client challenge: $clientChallenge")*/
              val agreedMessage = ByteVector("master secret".getBytes) ++ clientChallenge ++
                hex"00000000" ++ serverChallenge ++ hex"00000000"

              //println("In message: " + agreedMessage)

              val masterSecret = CryptoInterface.MD5MAC(agreedValue,
                agreedMessage,
                20)

              //println("Master secret: " + masterSecret)

              serverChallengeResult = CryptoInterface.MD5MAC(masterSecret,
                ByteVector("server finished".getBytes) ++ serverMACBuffer ++ hex"01",
                0xc)

              val clientChallengeResultCheck = CryptoInterface.MD5MAC(masterSecret,
                ByteVector("client finished".getBytes) ++ serverMACBuffer ++ hex"01" ++ clientChallengeResult ++ hex"01",
                0xc)

              //println("Check result: " + CryptoInterface.verifyMAC(clientChallenge, clientChallengeResult))

              val decExpansion = ByteVector("client expansion".getBytes) ++ hex"0000" ++ serverChallenge ++
                hex"00000000" ++ clientChallenge ++ hex"00000000"

              val encExpansion = ByteVector("server expansion".getBytes) ++ hex"0000" ++ serverChallenge ++
                hex"00000000" ++ clientChallenge ++ hex"00000000"

              /*println("DecExpansion: " + decExpansion)
              println("EncExpansion: " + encExpansion)*/

              // expand the encryption and decryption keys
              // The first 20 bytes are for RC5, and the next 16 are for the MAC'ing keys
              val expandedDecKey = CryptoInterface.MD5MAC(masterSecret,
                decExpansion,
                0x40) // this is what is visible in IDA

              val expandedEncKey = CryptoInterface.MD5MAC(masterSecret,
                encExpansion,
                0x40)

              val decKey = expandedDecKey.take(20)
              val encKey = expandedEncKey.take(20)
              val decMACKey = expandedDecKey.drop(20).take(16)
              val encMACKey = expandedEncKey.drop(20).take(16)

              /*println("**** DecKey: " + decKey)
              println("**** EncKey: " + encKey)
              println("**** DecMacKey: " + decMACKey)
              println("**** EncMacKey: " + encMACKey)*/

              // spin up our encryption program
              cryptoState = Some(new CryptoStateWithMAC(decKey, encKey, decMACKey, encMACKey))

              val packet = PacketCoding.CreateCryptoPacket(seq,
                ServerFinished(serverChallengeResult))

              sendResponse(packet)

              context.become(Established)
            case default => failWithError(s"Unexpected packet type $default in state CryptoSetupFinished")
          }
      }
    case default => failWithError(s"Invalid message '$default' received in state CryptoSetupFinished")
  }

  def Established : Receive = {
    case RawPacket(msg) =>
      if(sender() == rightRef) {
        val packet = PacketCoding.encryptPacket(cryptoState.get, 0, msg).require
        sendResponse(packet)
      } else {
        PacketCoding.UnmarshalPacket(msg) match {
          case Successful(p) =>
            p match {
              case encPacket @ EncryptedPacket(seq, _) =>
                PacketCoding.decryptPacket(cryptoState.get, encPacket) match {
                  case Successful(packet) =>
                    self !> packet
                  case Failure(e) =>
                    log.error("Failed to decode encrypted packet: " + e)
                }
              case default => failWithError(s"Unexpected packet type $default in state Established")

            }
          case Failure(e) => log.error("Could not decode raw packet: " + e)
        }
      }
    case api : CryptoSessionAPI =>
      api match {
        case DropCryptoSession() =>
          handleEstablishedPacket(
            sender(),
            PacketCoding.CreateControlPacket(TeardownConnection(clientNonce))
          )
      }
    case ctrl @ ControlPacket(_, _) =>
      val from = sender()

      handleEstablishedPacket(from, ctrl)
    case game @ GamePacket(_, _, _) =>
      val from = sender()

      handleEstablishedPacket(from, game)
    case sessionAPI : SessionRouterAPI =>
      leftRef !> sessionAPI
    case default => failWithError(s"Invalid message '$default' received in state Established")
  }

  def failWithError(error : String) = {
    log.error(error)
  }

  def cleanupCrypto() = {
    if(cryptoDHState.isDefined) {
      cryptoDHState.get.close
      cryptoDHState = None
    }

    if(cryptoState.isDefined) {
      cryptoState.get.close
      cryptoState = None
    }
  }

  def resetState() : Unit = {
    context.become(receive)

    // reset the crypto primitives
    cleanupCrypto()

    serverChallenge = ByteVector.empty
    serverChallengeResult = ByteVector.empty
    serverMACBuffer = ByteVector.empty
    clientPublicKey = ByteVector.empty
    clientChallenge = ByteVector.empty
    clientChallengeResult = ByteVector.empty
  }

  def handleEstablishedPacket(from : ActorRef, cont : PlanetSidePacketContainer) = {
    // we are processing a packet we decrypted
    if(from == self) {
      rightRef !> cont
    } else if(from == rightRef) { // processing a completed packet from the right. encrypt
      val packet = PacketCoding.encryptPacket(cryptoState.get, cont).require
      sendResponse(packet)
    } else {
      log.error(s"Invalid sender when handling a message in Established ${from}")
    }
  }

  def sendResponse(cont : PlanetSidePacketContainer) : ByteVector = {
    log.trace("CRYPTO SEND: " + cont)
    val pkt = PacketCoding.MarshalPacket(cont)

    pkt match {
      case Failure(e) =>
        log.error(s"Failed to marshal packet ${cont.getClass.getName} when sending response")
        ByteVector.empty
      case Successful(v) =>
        val bytes = v.toByteVector

        MDC("sessionId") = sessionId.toString
        leftRef !> ResponsePacket(bytes)

        bytes
    }
  }

  def sendResponse(pkt : PlanetSideGamePacket) : ByteVector = {
    log.trace("CRYPTO SEND GAME: " + pkt)
    val pktEncoded = PacketCoding.EncodePacket(pkt)

    pktEncoded match {
      case Failure(e) =>
        log.error(s"Failed to encode packet ${pkt.getClass.getName} when sending response")
        ByteVector.empty
      case Successful(v) =>
        val bytes = v.toByteVector

        MDC("sessionId") = sessionId.toString
        leftRef !> ResponsePacket(bytes)

        bytes
    }
  }

  def getRandBytes(amount : Int) : ByteVector = {
    val array = Array.ofDim[Byte](amount)
    random.nextBytes(array)
    ByteVector.view(array)
  }
}
