// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Dispatched by the server when placing deployables to generate a message in the events chat.<br>
  * <br>
  * This packet does not actually modify anything in regards to deployables.
  * It merely generates the message:<br>
  *   `"You have placed x of a possible y thing."`<br>
  * ... where `x` is the current count of objects of this type that have been deployed;
  * `y` is the (reported) maximum amount of objects of this type that can be deployed;
  * and, `thing` is the label for objects of this type.
  * This text is not directly placed into the message's field but, rather, is a token for language-appropriate descriptive text.<br>
  * "boomer," for example, is replaced by "Heavy Explosive Mines" in the message for English language.
  * @param guid na;
  *             usually 0?
  * @param desc descriptive text of what kind of object is being deployed;
  *             matches the `String` description of the object class
  * @param unk na;
  *            usually 4
  * @param count the current number of this type of object deployed
  * @param max the maximum number of this type of object that can be deployed
  * @see `ObjectClass`
  */
final case class ObjectDeployedMessage(guid : PlanetSideGUID,
                                       desc : String,
                                       unk : Long,
                                       count : Long,
                                       max : Long)
  extends PlanetSideGamePacket {
  type Packet = ObjectDeployedMessage
  def opcode = GamePacketOpcode.ObjectDeployedMessage
  def encode = ObjectDeployedMessage.encode(this)
}

object ObjectDeployedMessage extends Marshallable[ObjectDeployedMessage] {
  /**
    * Overloaded constructor for when the guid is not required.
    * @param desc descriptive text of what kind of object is being deployed
    * @param unk na
    * @param count the number of this type of object deployed
    * @param max the maximum number of this type of object that can be deployed
    * @return an `ObjectDeployedMessage` object
    */
  def apply(desc : String, unk : Long, count : Long, max : Long) : ObjectDeployedMessage =
    new ObjectDeployedMessage(PlanetSideGUID(0), desc, unk, count, max)

  implicit val codec : Codec[ObjectDeployedMessage] = (
    ("object_guid" | PlanetSideGUID.codec) ::
      ("desc" | PacketHelpers.encodedString) ::
      ("unk" | uint32L) ::
      ("count" | uint32L) ::
      ("max" | uint32L)
    ).xmap[ObjectDeployedMessage] (
    {
      case guid :: str :: unk :: cnt ::mx :: HNil =>
        ObjectDeployedMessage(guid, str, unk, cnt, mx)
    },
    {
      case ObjectDeployedMessage(guid, str, unk, cnt, mx) =>
        //truncate string length to 100 characters; raise no warnings
        val limitedStr : String =  if(str.length() > 100) { str.substring(0,100) } else { str }
        guid :: limitedStr :: unk :: cnt :: mx :: HNil
    }
  )
}
