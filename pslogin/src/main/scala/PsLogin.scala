import akka.actor.{Props, ActorSystem}
import psforever.crypto.CryptoInterface

object PsLogin {
  def main(args : Array[String]) : Unit = {
    println("PsLogin v0.1")

    try {
      CryptoInterface.initialize()
      println("Crypto initialized")
    }
    catch {
      case e : UnsatisfiedLinkError =>
        println("Unable to initialize " + CryptoInterface.libName)
        sys.exit(1)
    }

    val system = ActorSystem("PsLogin")
    val session = system.actorOf(Props[SessionRouter], "session-router")
    val listener = system.actorOf(Props(new UdpListener(session)), "udp-listener")
  }
}
