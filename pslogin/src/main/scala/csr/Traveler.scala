package csr

// Copyright (c) 2017 PSForever
import akka.actor.ActorRef
import net.psforever.packet.PlanetSidePacketContainer

/*
The following is STILL for development and fun.
*/
/**
  * The traveler is synonymous with the player.
  * The primary purpose of the object is to keep track of but not expose the player's session so that packets may be relayed back to him.
  * csr.Traveler also keeps track of which zone the player currently occupies.
  * @param session the player's session
  */
class Traveler(private val session : ActorRef, var zone : String) {
  /**
    * `sendToSelf` is a call that permits the session to gain access to its internal `rightRef` so that it can dispatch a packet.
    * @param msg the byte-code translation of a packet
    */
  def sendToSelf(msg : PlanetSidePacketContainer) : Unit = {
    //    this.session.sendResponse(msg)
  }

  def !(msg : Any) : Unit = {
    session ! msg
  }
}

object Traveler {
  /**
    * An abbreviated constructor for creating `csr.Traveler`s without invocation of `new`.
    * @param session the player's session
    * @return a traveler object for this player
    */
  def apply(session : ActorRef, zoneId : String) : Traveler = new Traveler(session, zoneId)
}
