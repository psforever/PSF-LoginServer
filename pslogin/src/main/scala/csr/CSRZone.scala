package csr

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.ChatMsg
import net.psforever.types.{ChatMessageType, Vector3}

/*
The following is STILL for development and fun.
*/
/**
  * An implementation of the CSR command `/zone`, slightly modified to serve the purposes of the testing phases of the server.
  */
object CSRZone {
  /**
    * Accept and confirm that a message sent to a player is a valid `/zone` invocation.
    * If so, parse the message and send the player to whichever zone was requested.
    * @param traveler the player
    * @param msg the message the player received
    * @return true, if the player is being transported to another zone; false, otherwise
    */
  def read(traveler : Traveler, msg : ChatMsg) : (Boolean, String , Vector3 ) = {
    if(!isProperRequest(msg))
      return (false,"", Vector3.Zero)  //we do not handle this message

    val buffer = decomposeMessage(msg.contents)
    if(buffer.length == 0 || buffer(0).equals("-help")) {
      CSRZone.help(traveler) //print usage information to chat
      return (false,"", Vector3.Zero)
    }

    var zoneId = ""
    var gateId = "" //the user can define which warpgate they may visit (actual keyword protocol missing)
    var list = false //if the user wants a printed list of destination locations
    for(o <- buffer) {
      if(o.equals("-list")) {
        if(zoneId.equals("") || gateId.equals("")) {
          list = true
        }
      }
      else if(zoneId.equals(""))
        zoneId = o
      else if(gateId.equals(""))
        gateId = o
    }

    val zoneOpt = CSRZoneImpl.get(zoneId)
    if(zoneOpt.isEmpty) {
      if(list)
        CSRZone.reply(traveler, CSRZoneImpl.list)
      else
        CSRZone.error(traveler, "Give a valid zonename (use '/zone -list')")
      return (false,"", Vector3.Zero)
    }
    val zone = zoneOpt.get
    var destination : Vector3 = CSRZoneImpl.selectRandom(zone) //the destination in the new zone starts as random

    if(!gateId.equals("")) { //if we've defined a warpgate, and can find that warpgate, we re-assign the destination
    val gateOpt = CSRZoneImpl.getWarpgate(zone, gateId)
      if(gateOpt.isDefined)
        destination = gateOpt.get
      else
        CSRZone.error(traveler, "Gate id not defined (use '/zone <zone> -list')")
    }
    else if(list) {
      CSRZone.reply(traveler, CSRZoneImpl.listWarpgates(zone))
      return (false,"",Vector3.Zero)
    }
    (true, zone.zonename, destination)
  }

  /**
    * Check that the incoming message is an appropriate type for this command.
    * @param msg the message
    * @return true, if we will handle it; false, otherwise
    */
  def isProperRequest(msg : ChatMsg) : Boolean ={
    msg.messageType == ChatMessageType.CMT_ZONE
  }

  /**
    * Break the message in the packet down for parsing.
    * @param msg the contents portion of the message, a space-separated `String`
    * @return the contents portion of the message, transformed into an `Array`
    */
  private def decomposeMessage(msg : String) : Array[String] = {
    msg.trim.toLowerCase.split("\\s+")
  }

  /**
    * Send a message back to the `Traveler` that will be printed into his chat window.
    * @param traveler the player
    * @param msg the message to be sent
    */
  private def reply(traveler : Traveler, msg : String) : Unit = {
    traveler ! PacketCoding.CreateGamePacket(0, ChatMsg(ChatMessageType.CMT_OPEN,true,"", msg, None))
  }

  /**
    * Print usage information to the `Traveler`'s chat window.
    * @param traveler the player
    */
  private def help(traveler : Traveler) : Unit = {
    CSRZone.reply(traveler, "usage: /zone <zone> [gatename] | [-list]")
  }

  /**
    * Print error information to the `Traveler`'s chat window.<br>
    * The most common reason for error is the lack of information, or wrong information.
    * @param traveler the player
    */
  private def error(traveler : Traveler, msg : String) : Unit = {
    CSRZone.reply(traveler, "Error! "+msg)
  }
}
