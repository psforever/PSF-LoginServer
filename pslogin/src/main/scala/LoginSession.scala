import java.net.InetSocketAddress

import akka.actor.ActorRef
import scodec.bits.{BitVector, ByteVector}

class LoginSession(id : Long, socket : ActorRef, address : InetSocketAddress) {

  def send(msg : BitVector) = {
    socket ! SendPacket(msg.toByteVector, address)
  }
}
