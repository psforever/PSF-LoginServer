// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.game.objectcreate.{ConstructorData, ObjectClass, ObjectCreateBase, ObjectCreateMessageParent}
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * Communicate with the client that a certain object with certain properties is to be created.
  * In general, `ObjectCreateMessage` and its counterpart `ObjectCreateDetailedMessage` should look similar.<br>
  * <br>
  * In normal packet data order, the parent object is specified before the actual object is specified.
  * This is most likely a method of early correction.
  * "Does this parent object exist?"
  * "Is this new object something that can be attached to this parent?"
  * "Does the parent have the appropriate attachment slot?"
  * There is no fail-safe method for any of these circumstances being false, however, and the object will simply not be created.
  * In instance where the parent data does not exist, the object-specific data is immediately encountered.<br>
  * <br>
  * The object's GUID is assigned by the server.
  * The clients are required to adhere to this new GUID referring to the object.
  * There is no fail-safe for a conflict between what the server thinks is a new GUID and what any client thinks is an already-assigned GUID.
  * Likewise, there is no fail-safe between a client failing or refusing to create an object and the server thinking an object has been created.
  * (The GM-level command `/sync` tests for objects that "do not match" between the server and the client.
  * It's implementation and scope are undefined.)<br>
  * <br>
  * Knowing the object's type is essential for parsing the specific information passed by the `data` parameter.
  * If the object does not have encoding information or is unknown, it will not translate between byte data and a game object.
  * @param streamLength the total length of the data that composes this packet in bits, excluding the opcode and end padding
  * @param objectClass the code for the type of object being constructed
  * @param guid the GUID this object will be assigned
  * @param parentInfo if defined, the relationship between this object and another object (its parent)
  * @param data the data used to construct this type of object;
  *             on decoding, set to `None` if the process failed
  */
final case class ObjectCreateDetailedMessage(streamLength : Long,
                                             objectClass : Int,
                                             guid : PlanetSideGUID,
                                             parentInfo : Option[ObjectCreateMessageParent],
                                             data : ConstructorData)
  extends PlanetSideGamePacket {
  type Packet = ObjectCreateDetailedMessage
  def opcode = GamePacketOpcode.ObjectCreateMessage
  def encode = ObjectCreateDetailedMessage.encode(this)
}

object ObjectCreateDetailedMessage extends Marshallable[ObjectCreateDetailedMessage] {
  /**
    * An abbreviated constructor for creating `ObjectCreateMessages`, ignoring the optional aspect of some fields.
    * @param objectClass the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param parentInfo the relationship between this object and another object (its parent)
    * @param data the data used to construct this type of object
    * @return an ObjectCreateMessage
    */
  def apply(objectClass : Int, guid : PlanetSideGUID, parentInfo : ObjectCreateMessageParent, data : ConstructorData) : ObjectCreateDetailedMessage = {
    val parentInfoOpt : Option[ObjectCreateMessageParent] = Some(parentInfo)
    ObjectCreateDetailedMessage(ObjectCreateBase.streamLen(parentInfoOpt, data), objectClass, guid, parentInfoOpt, data)
  }

  /**
    * An abbreviated constructor for creating `ObjectCreateMessages`, ignoring `parentInfo`.
    * @param objectClass the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param data the data used to construct this type of object
    * @return an ObjectCreateMessage
    */
  def apply(objectClass : Int, guid : PlanetSideGUID, data : ConstructorData) : ObjectCreateDetailedMessage = {
    ObjectCreateDetailedMessage(ObjectCreateBase.streamLen(None, data), objectClass, guid, None, data)
  }

  implicit val codec : Codec[ObjectCreateDetailedMessage] = ObjectCreateBase.baseCodec.exmap[ObjectCreateDetailedMessage] (
    {
      case _ :: _ :: _ :: _ :: BitVector.empty :: HNil =>
        Attempt.failure(Err("no data to decode"))

      case len :: cls :: guid :: par :: data :: HNil =>
        ObjectCreateBase.decodeData(cls, data,
          if(par.isDefined) {
            ObjectClass.selectDataDetailedCodec
          }
          else {
            ObjectClass.selectDataDroppedDetailedCodec
          }
        ) match {
          case Attempt.Successful(obj) =>
            Attempt.successful(ObjectCreateDetailedMessage(len, cls, guid, par, obj))
          case Attempt.Failure(err) =>
            Attempt.failure(err)
        }
    },
    {
      case ObjectCreateDetailedMessage(_, cls, guid, par, obj) =>
        val len = ObjectCreateBase.streamLen(par, obj) //even if a stream length has been assigned, it can not be trusted during encoding
        ObjectCreateBase.encodeData(cls, obj,
          if(par.isDefined) {
            ObjectClass.selectDataDetailedCodec
          }
          else {
            ObjectClass.selectDataDroppedDetailedCodec
          }
        ) match {
          case Attempt.Successful(bvec) =>
            Attempt.successful(len :: cls :: guid :: par :: bvec :: HNil)
          case Attempt.Failure(err) =>
            Attempt.failure(err)
        }
    }
  )
}
