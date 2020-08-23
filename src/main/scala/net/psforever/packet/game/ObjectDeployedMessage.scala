// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * An `Enumeration` for the forms of the event chat message produced by this packet.
  */
object DeployOutcome extends Enumeration(1) {
  type Type = Value

  val Failure = Value(2)
  //3 produces a Success message, but 4 is common
  val Success = Value(4)

  val codec = PacketHelpers.createLongEnumerationCodec(this, uint32L)
}

/**
  * Dispatched by the server to generate a message in the events chat when placing deployables.<br>
  * <br>
  * This packet does not actually modify anything in regards to deployables.
  * The most common form of the generated message is:<br>
  *   `"You have placed x of a possible y thing s."`<br>
  * ... where `x` is the current count of objects of this type that have been deployed;
  * `y` is the (reported) maximum amount of objects of this type that can be deployed;
  * and, `thing` is the token for objects of this type.
  * If the `thing` is a valid string token, it will be replaced by language-appropriate descriptive text in the message.
  * Otherwise, that text is placed directly into the message, with an obvious space between the text and the "s".
  * "boomer," for example, is replaced by "Boomer Heavy Explosives" in the message for English language.
  * "bullet_9mm_AP," however, is just "bullet_9mm_AP s."<br>
  * <br>
  * When the `action` is `Success`, the message in the chat will be shown as above.
  * When the `action` is `Failure`, the message will be:<br>
  *   `"thing failed to deploy and was destroyed."`<br>
  * ... where, again, `thing` is a valid string token.
  * @param unk na;
  *             usually 0?
  * @param desc descriptive text of what kind of object is being deployed;
  *             string token of the object, at best
  * @param action the form the message will take
  * @param count the current number of this type of object deployed
  * @param max the maximum number of this type of object that can be deployed
  */
final case class ObjectDeployedMessage(unk: Int, desc: String, action: DeployOutcome.Value, count: Long, max: Long)
    extends PlanetSideGamePacket {
  type Packet = ObjectDeployedMessage
  def opcode = GamePacketOpcode.ObjectDeployedMessage
  def encode = ObjectDeployedMessage.encode(this)
}

object ObjectDeployedMessage extends Marshallable[ObjectDeployedMessage] {

  /**
    * Overloaded constructor for when the guid is not required.
    * @param desc descriptive text of what kind of object is being deployed
    * @param action na
    * @param count the number of this type of object deployed
    * @param max the maximum number of this type of object that can be deployed
    * @return an `ObjectDeployedMessage` object
    */
  def apply(desc: String, action: DeployOutcome.Value, count: Long, max: Long): ObjectDeployedMessage =
    new ObjectDeployedMessage(0, desc, action, count, max)

  /**
    * na
    * @param desc descriptive text of what kind of object is being deployed
    * @param count the number of this type of object deployed
    * @param max the maximum number of this type of object that can be deployed
    * @return an `ObjectDeployedMessage` object
    */
  def Success(desc: String, count: Int, max: Int): ObjectDeployedMessage =
    new ObjectDeployedMessage(0, desc, DeployOutcome.Success, count, max)

  /**
    * na
    * @param desc descriptive text of what kind of object failed to be deployed
    * @return an `ObjectDeployedMessage` object
    */
  def Failure(desc: String): ObjectDeployedMessage =
    new ObjectDeployedMessage(0, desc, DeployOutcome.Failure, 0, 0)

  implicit val codec: Codec[ObjectDeployedMessage] = (
    ("unk" | uint16L) ::
      ("desc" | PacketHelpers.encodedString) ::
      ("action" | DeployOutcome.codec) ::
      ("count" | uint32L) ::
      ("max" | uint32L)
  ).xmap[ObjectDeployedMessage](
    {
      case guid :: str :: unk :: cnt :: mx :: HNil =>
        ObjectDeployedMessage(guid, str, unk, cnt, mx)
    },
    {
      case ObjectDeployedMessage(guid, str, unk, cnt, mx) =>
        //truncate string length to 100 characters; raise no warnings
        val limitedStr: String = if (str.length() > 100) { str.substring(0, 100) }
        else { str }
        guid :: limitedStr :: unk :: cnt :: mx :: HNil
    }
  )
}
