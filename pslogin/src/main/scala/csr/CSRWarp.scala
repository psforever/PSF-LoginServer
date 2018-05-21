package csr

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.ChatMsg
import net.psforever.types.{ChatMessageType, Vector3}

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

/*
The following is STILL for development and fun.
*/
/**
  * An implementation of the CSR command `/warp`, highly modified to serve the purposes of the testing phases of the server.
  * See `help()` for details.
  */
object CSRWarp {
  /**
    * Accept and confirm that a message sent to a player is a valid `/warp` invocation.
    * If so, parse the message and send the player to whichever destination in this zone was requested.
    * @param traveler the player
    * @param msg      the message the player received
    * @return true, if the player is being transported to another place; false, otherwise
    */
  def read(traveler : Traveler, msg : ChatMsg) : (Boolean, Vector3) = {
    if(!isProperRequest(msg))
      return (false, Vector3.Zero) //we do not handle this message

    val buffer = decomposeMessage(msg.contents)
    if(buffer.length == 0 || buffer(0).equals("") || buffer(0).equals("-help")) {
      CSRWarp.help(traveler) //print usage information to chat
      return (false, Vector3.Zero)
    }
    var destId : String = ""
    var coords : ArrayBuffer[Int] = ArrayBuffer.empty[Int]
    var list : Boolean = false
    var failedCoordInput = false
    for(o <- buffer) {
      val toInt = Try(o.toInt)
      if(toInt.isSuccess) {
        coords += toInt.get
      }
      else if(coords.nonEmpty && coords.size < 3)
        failedCoordInput = true
      if(o.equals("-list"))
        list = true
      else if(destId.equals(""))
        destId = o
    }
    if(failedCoordInput || (coords.nonEmpty && coords.size < 3)) {
      CSRWarp.error(traveler, "Needs three integer components (<x> <y> <z>)")
      return (false, Vector3.Zero)
    }
    else {
      coords.slice(0, 3).foreach(x => {
        if(x < 0 || x > 8191) {
          CSRWarp.error(traveler, "Out of range - 0 < n < 8191, but n = " + x)
          return (false, Vector3.Zero)
        }
      })
    }
    val zone = CSRZoneImpl.get(traveler.zone).get //the traveler is already in the appropriate zone
    if(list && coords.isEmpty && destId.equals("")) {
      CSRWarp.reply(traveler, CSRZoneImpl.listLocations(zone) + "; " + CSRZoneImpl.listWarpgates(zone))
      return (false, Vector3.Zero)
    }
    val dest : Option[Vector3] = if(coords.nonEmpty) Some(Vector3(coords(0), coords(1), coords(2)))
    else CSRZoneImpl.getWarpLocation(zone, destId) //coords before destId
    if(dest.isEmpty) {
      CSRWarp.error(traveler, "Invalid location")
      return (false, Vector3.Zero)
    }
    (true, dest.get)
  }

  /**
    * Check that the incoming message is an appropriate type for this command.
    * @param msg the message
    * @return true, if we will handle it; false, otherwise
    */
  def isProperRequest(msg : ChatMsg) : Boolean = {
    msg.messageType == ChatMessageType.CMT_WARP
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
    * @param msg      the message to be sent
    */
  private def reply(traveler : Traveler, msg : String) : Unit = {
    traveler ! PacketCoding.CreateGamePacket(0, ChatMsg(ChatMessageType.CMT_OPEN, true, "", msg, None))
  }

  /**
    * Print usage information to the `Traveler`'s chat window.<br>
    * <br>
    * The "official" use information for help dictates the command should follow this format:
    * `/warp &lt;x&gt;&lt;y&gt;&lt;z&gt; | to &lt;character&gt; | near &lt;object&gt; | above &lt;object&gt; | waypoint`.
    * In our case, creating fixed coordinate points of interest is not terribly dissimilar from the "near" and "to" aspect.
    * We can not currently implement most of the options for now, however.<br>
    * <br>
    * The destination prioritizes evaluation of the coordinates before the location string.
    * When the user provides coordinates, he must provide all three components of the coordinate at once, else none will be accepted.
    * If the coordinates are invalid, the location string will still be checked.
    * "-list" is accepted while no serious attempt is made to indicate a destination (no location string or not enough coordinates).
    * @param traveler the player
    */
  private def help(traveler : Traveler) : Unit = {
    CSRWarp.reply(traveler, "usage: /warp <location> | <gatename> | <x> <y> <z> | [-list]")
  }

  /**
    * Print error information to the `Traveler`'s chat window.<br>
    * The most common reason for error is the lack of information, or wrong information.
    * @param traveler the player
    */
  private def error(traveler : Traveler, msg : String) : Unit = {
    CSRWarp.reply(traveler, "Error! " + msg)
  }
}
