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
                                             data : Option[ConstructorData])
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
  def apply(objectClass : Int, guid : PlanetSideGUID, parentInfo : ObjectCreateMessageParent, data : ConstructorData) : ObjectCreateDetailedMessage =
    ObjectCreateDetailedMessage(0L, objectClass, guid, Some(parentInfo), Some(data))

  /**
    * An abbreviated constructor for creating `ObjectCreateMessages`, ignoring `parentInfo`.
    * @param objectClass the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param data the data used to construct this type of object
    * @return an ObjectCreateMessage
    */
  def apply(objectClass : Int, guid : PlanetSideGUID, data : ConstructorData) : ObjectCreateDetailedMessage =
    ObjectCreateDetailedMessage(0L, objectClass, guid, None, Some(data))

  /**
    * Take the important information of a game piece and transform it into bit data.
    * This function is fail-safe because it catches errors involving bad parsing of the object data.
    * Generally, the `Exception` messages themselves are not useful here.
    * @param objClass the code for the type of object being deconstructed
    * @param obj the object data
    * @return the bitstream data
    * @see ObjectClass.selectDataCodec
    */
  def encodeData(objClass : Int, obj : ConstructorData, getCodecFunc : (Int) => Codec[ConstructorData.genericPattern]) : BitVector = {
    var out = BitVector.empty
    try {
      val outOpt : Option[BitVector] = getCodecFunc(objClass).encode(Some(obj.asInstanceOf[ConstructorData])).toOption
      if(outOpt.isDefined)
        out = outOpt.get
    }
    catch {
      case _ : Exception =>
        //catch and release, any sort of parse error
    }
    out
  }

  implicit val codec : Codec[ObjectCreateDetailedMessage] = ObjectCreateBase.baseCodec.exmap[ObjectCreateDetailedMessage] (
    {
      case _ :: _ :: _ :: _ :: BitVector.empty :: HNil =>
        Attempt.failure(Err("no data to decode"))

      case len :: cls :: guid :: par :: data :: HNil =>
        val obj = ObjectCreateBase.decodeData(cls, data, ObjectClass.selectDataDetailedCodec)
        Attempt.successful(ObjectCreateDetailedMessage(len, cls, guid, par, obj))
    },
    {
      case ObjectCreateDetailedMessage(_ , _ , _, _, None) =>
        Attempt.failure(Err("no object to encode"))

      case ObjectCreateDetailedMessage(_, cls, guid, par, Some(obj)) =>
        val len = ObjectCreateBase.streamLen(par, obj) //even if a stream length has been assigned, it can not be trusted during encoding
        val bitvec = ObjectCreateBase.encodeData(cls, obj, ObjectClass.selectDataDetailedCodec)
        Attempt.successful(len :: cls :: guid :: par :: bitvec :: HNil)
    }
  )
}
