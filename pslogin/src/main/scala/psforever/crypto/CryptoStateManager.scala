// Copyright (c) 2016 PSForever.net to present
package psforever.crypto

import akka.actor.{Actor, ActorLogging, FSM}
import akka.util.ByteString
import scodec.Codec
import scodec.bits.ByteVector

sealed trait CryptoState
final case class ClientStart() extends CryptoState
final case class ServerStart() extends CryptoState
final case class ClientChallengeXchg() extends CryptoState
final case class ServerChallengeXchg() extends CryptoState
final case class ClientFinished() extends CryptoState
final case class ServerFinished() extends CryptoState

sealed trait CryptoData
final case class Uninitialized() extends CryptoData

class CryptoStateManager extends Actor with ActorLogging with FSM[CryptoState, CryptoData] {
  startWith(ClientStart(), Uninitialized())

  when(ClientStart()) {
    /*case Event(RawPacket(msg), _) => {
      val decoded = Codec.decode[psforever.net.ClientStart](msg.bits)
      try {
        val packet = decoded.require.value
        println("Got cNonce: " + packet.clientNonce)
      }
      catch {
        case e : Exception =>
          println("Invalid packet: " + e.getMessage)
      }

      stay
    }*/
    case _ => stay
  }

  initialize()
}
