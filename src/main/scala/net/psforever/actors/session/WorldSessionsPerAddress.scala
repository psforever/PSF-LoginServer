// Copyright (c) 2026 PSForever
package net.psforever.actors.session

import java.net.InetAddress

import scala.collection.mutable

/**
  * How many world sessions each client address has been admitted, so one address cannot hold more than its share.
  * A session is counted from the time it is admitted until it stops.
  * Loopback addresses are never limited; that is the server's own machine (local testing, bots).
  */
object WorldSessionsPerAddress {
  private val admitted: mutable.Map[InetAddress, Int] = mutable.Map()

  /**
    * Admit one more world session from this address, unless it already has the limit.
    * @param address the client address
    * @param limit the most sessions an address may have; 0 or less is no limit
    * @return `true`, if the session was admitted and counted, and must be released later;
    *        `false`, if the address is at its limit
    */
  def tryAdmit(address: InetAddress, limit: Int): Boolean = synchronized {
    val count = admitted.getOrElse(address, 0)
    if (limit <= 0 || address.isLoopbackAddress || count < limit) {
      admitted(address) = count + 1
      true
    } else {
      false
    }
  }

  /**
    * Stop counting one admitted world session from this address.
    * @param address the client address
    */
  def release(address: InetAddress): Unit = synchronized {
    admitted.get(address).foreach { count =>
      if (count <= 1) {
        admitted.remove(address)
      } else {
        admitted(address) = count - 1
      }
    }
  }
}
