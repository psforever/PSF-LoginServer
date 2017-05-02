// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.packet.game.objectcreate._
import scodec.{Attempt, Codec, Err}
import scodec.bits.BitVector
import shapeless.{::, HNil}

/**
  * Communicate with the client that a certain object with certain properties is to be created.
  * In general, `ObjectCreateMessage` and its counterpart `ObjectCreateDetailedMessage` should look similar.<br>
  * <br>
  * `ObjectCreateMessage` is capable of creating every non-environmental object in the game through the use of encoding patterns.
  * The objects produced by this packet generally do not always fully express all the complexities of the object class.
  * With respect to a client's avatar, all of the items in his inventory are given thorough detail so that the client can account for their interaction.
  * The "shallow" objects produced by this packet are not like that.
  * They express only the essential information necessary for client interaction when the client interacts with them.
  * For example, a weapon defined by this packet may not care internally what fire mode it is in or how much ammunition it has.
  * Such a weapon is not in the client's player's holster or inventory.
  * It is imperceptive information to which he would not currently have access.
  * An `0x17` game object is, therefore, a game object with only the essential data exposed.<br>
  * <br>
  * When interacting with an `0x17` game object, the server will swap back and forth between it and an `0x18` object.
  * (Or it will be removed when it is placed somewhere a given client will no longer be able to see it.)
  * The purpose of this conversion is to control network traffic and object agency.
  * It is not necessary to keep track of all objects on every player on every client individually.
  * This relates to the goal of this packet exposing only "essential data."
  * One player does not need to know how much ammunition remains in a weapon belonging to another player normally.
  * One player also does not need to know how much ammunition is used up when another player reloads their weapon.
  * The only way the first player will know is when the weapon is transferred into his own inventory.
  * All other clients are spared micromanagement of the hypothetical other player's weapon.
  * Updated information is only made available when and where it is needed.<br>
  * <br>
  * Knowing the object's type is necessary for proper parsing.
  * If the object does not have encoding information or is unknown, it will not translate between byte data and a game object.
  * @param streamLength the total length of the data that composes this packet in bits;
  *                     exclude the opcode (1 byte) and end padding (0-7 bits);
  *                     when encoding, it will be calculated automatically
  * @param objectClass the code for the type of object being constructed;
  *                    always an 11-bit LE value
  * @param guid the GUID this object will be assigned
  * @param parentInfo if defined, the relationship between this object and another object (its parent)
  * @param data the data used to construct this type of object;
  *             on decoding, set to `None` if the process failed
  * @see ObjectCreateDetailedMessage
  * @see ObjectCreateMessageParent
  */
final case class ObjectCreateMessage(streamLength : Long,
                                     objectClass : Int,
                                     guid : PlanetSideGUID,
                                     parentInfo : Option[ObjectCreateMessageParent],
                                     data : Option[ConstructorData])
  extends PlanetSideGamePacket {
  type Packet = ObjectCreateMessage
  def opcode = GamePacketOpcode.ObjectCreateMessage_Duplicate
  def encode = ObjectCreateMessage.encode(this)
}

object ObjectCreateMessage extends Marshallable[ObjectCreateMessage] {
  /**
    * An abbreviated constructor for creating `ObjectCreateMessage`s, ignoring the optional aspect of some fields.
    * @param objectClass the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param parentInfo the relationship between this object and another object (its parent)
    * @param data the data used to construct this type of object
    * @return an `ObjectCreateMessage`
    */
  def apply(objectClass : Int, guid : PlanetSideGUID, parentInfo : ObjectCreateMessageParent, data : ConstructorData) : ObjectCreateMessage = {
    val parentInfoOpt : Option[ObjectCreateMessageParent] = Some(parentInfo)
    ObjectCreateMessage(ObjectCreateBase.streamLen(parentInfoOpt, data), objectClass, guid, parentInfoOpt, Some(data))
  }

  /**
    * An abbreviated constructor for creating `ObjectCreateMessage`s, calculating `streamLen` and ignoring `parentInfo`.
    * @param objectClass the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param data the data used to construct this type of object
    * @return an `ObjectCreateMessage`
    */
  def apply(objectClass : Int, guid : PlanetSideGUID, data : ConstructorData) : ObjectCreateMessage = {
    ObjectCreateMessage(ObjectCreateBase.streamLen(None, data), objectClass, guid, None, Some(data))
  }

  implicit val codec : Codec[ObjectCreateMessage] = ObjectCreateBase.baseCodec.exmap[ObjectCreateMessage] (
    {
      case _ :: _ :: _ :: _ :: BitVector.empty :: HNil =>
        Attempt.failure(Err("no data to decode"))

      case len :: cls :: guid :: par :: data :: HNil =>
        val obj = ObjectCreateBase.decodeData(cls, data,
          if(par.isDefined) {
            ObjectClass.selectDataCodec
          }
          else {
            ObjectClass.selectDataDroppedCodec
          }
        )
        Attempt.successful(ObjectCreateMessage(len, cls, guid, par, obj))
    },
    {
      case ObjectCreateMessage(_ , _ , _, _, None) =>
        Attempt.failure(Err("no object to encode"))

      case ObjectCreateMessage(_, cls, guid, par, Some(obj)) =>
        val len = ObjectCreateBase.streamLen(par, obj) //even if a stream length has been assigned, it can not be trusted during encoding
        val bitvec = ObjectCreateBase.encodeData(cls, obj,
          if(par.isDefined) {
            ObjectClass.selectDataCodec
          }
          else {
            ObjectClass.selectDataDroppedCodec
          }
        )
        Attempt.successful(len :: cls :: guid :: par :: bitvec :: HNil)
    }
  )
}
