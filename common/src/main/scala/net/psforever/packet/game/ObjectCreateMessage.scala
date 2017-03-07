// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.game.objectcreate.{ConstructorData, ObjectClass, StreamBitSize}
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.{Attempt, Codec, DecodeResult, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * The parent information of a created object.<br>
  * <br>
  * Rather than a created-parent with a created-child relationship, the whole of the packet still only creates the child.
  * The parent is a pre-existing object into which the (created) child is attached.
  * The slot is encoded as a string length integer, following PlanetSide Classic convention for slot numbering.
  * It is either a 0-127 eight bit number, or a 128-32767 sixteen bit number.
  * @param guid the GUID of the parent object
  * @param slot a parent-defined slot identifier that explains where the child is to be attached to the parent
  */
final case class ObjectCreateMessageParent(guid : PlanetSideGUID,
                                           slot : Int)

/**
  * Communicate with the client that a certain object with certain properties is to be created.
  * The object may also have primitive assignment (attachment) properties.<br>
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
  * Knowing the object's class is essential for parsing the specific information passed by the `data` parameter.
  * @param streamLength the total length of the data that composes this packet in bits, excluding the opcode and end padding
  * @param objectClass the code for the type of object being constructed
  * @param guid the GUID this object will be assigned
  * @param parentInfo if defined, the relationship between this object and another object (its parent)
  * @param data the data used to construct this type of object;
  *             on decoding, set to `None` if the process failed
  * @see ObjectClass.selectDataCodec
  */
final case class ObjectCreateMessage(streamLength : Long,
                                     objectClass : Int,
                                     guid : PlanetSideGUID,
                                     parentInfo : Option[ObjectCreateMessageParent],
                                     data : Option[ConstructorData])
  extends PlanetSideGamePacket {
  def opcode = GamePacketOpcode.ObjectCreateMessage
  def encode = ObjectCreateMessage.encode(this)
}

object ObjectCreateMessage extends Marshallable[ObjectCreateMessage] {
  /**
    * An abbreviated constructor for creating `ObjectCreateMessages`, ignoring the optional aspect of some fields.
    * @param streamLength the total length of the data that composes this packet in bits, excluding the opcode and end padding
    * @param objectClass the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param parentInfo the relationship between this object and another object (its parent)
    * @param data the data used to construct this type of object
    * @return an ObjectCreateMessage
    */
  def apply(streamLength : Long, objectClass : Int, guid : PlanetSideGUID, parentInfo : ObjectCreateMessageParent, data : ConstructorData) : ObjectCreateMessage =
    ObjectCreateMessage(streamLength, objectClass, guid, Some(parentInfo), Some(data))

  /**
    * An abbreviated constructor for creating `ObjectCreateMessages`, ignoring `parentInfo`.
    * @param streamLength the total length of the data that composes this packet in bits, excluding the opcode and end padding
    * @param objectClass the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param data the data used to construct this type of object
    * @return an ObjectCreateMessage
    */
  def apply(streamLength : Long, objectClass : Int, guid : PlanetSideGUID, data : ConstructorData) : ObjectCreateMessage =
    ObjectCreateMessage(streamLength, objectClass, guid, None, Some(data))

  type Pattern = Int :: PlanetSideGUID :: Option[ObjectCreateMessageParent] :: HNil
  type outPattern = Long :: Int :: PlanetSideGUID :: Option[ObjectCreateMessageParent] :: Option[ConstructorData] :: HNil
  /**
    * Codec for formatting around the lack of parent data in the stream.
    */
  private val noParent : Codec[Pattern] = (
    ("objectClass" | uintL(0xb)) :: //11u
      ("guid" | PlanetSideGUID.codec) //16u
    ).xmap[Pattern](
    {
      case cls :: guid :: HNil =>
        cls :: guid :: None :: HNil
    }, {
      case cls :: guid :: None :: HNil =>
        cls :: guid :: HNil
    }
  )
  /**
    * Codec for reading and formatting parent data from the stream.
    */
  private val parent : Codec[Pattern] = (
    ("parentGuid" | PlanetSideGUID.codec) :: //16u
      ("objectClass" | uintL(0xb)) :: //11u
      ("guid" | PlanetSideGUID.codec) :: //16u
      ("parentSlotIndex" | PacketHelpers.encodedStringSize) //8u or 16u
    ).xmap[Pattern](
    {
      case pguid :: cls :: guid :: slot :: HNil =>
        cls :: guid :: Some(ObjectCreateMessageParent(pguid, slot)) :: HNil
    }, {
      case cls :: guid :: Some(ObjectCreateMessageParent(pguid, slot)) :: HNil =>
        pguid :: cls :: guid :: slot :: HNil
    }
  )

  /**
    * Take bit data and transform it into an object that expresses the important information of a game piece.
    * This function is fail-safe because it catches errors involving bad parsing of the bitstream data.
    * Generally, the `Exception` messages themselves are not useful here.
    * The important parts are what the packet thought the object class should be and what it actually processed.
    * @param objectClass the code for the type of object being constructed
    * @param data the bitstream data
    * @return the optional constructed object
    */
  private def decodeData(objectClass : Int, data : BitVector) : Option[ConstructorData] = {
    var out : Option[ConstructorData] = None
    try {
      val outOpt : Option[DecodeResult[_]] = ObjectClass.selectDataCodec(objectClass).decode(data).toOption
      if(outOpt.isDefined)
        out = outOpt.get.value.asInstanceOf[ConstructorData.genericPattern]
    }
    catch {
      case ex : Exception =>
        //catch and release, any sort of parse error
    }
    out
  }

  /**
    * Take the important information of a game piece and transform it into bit data.
    * This function is fail-safe because it catches errors involving bad parsing of the object data.
    * Generally, the `Exception` messages themselves are not useful here.
    * @param objClass the code for the type of object being deconstructed
    * @param obj the object data
    * @return the bitstream data
    */
  private def encodeData(objClass : Int, obj : ConstructorData) : BitVector = {
    var out = BitVector.empty
    try {
      val outOpt : Option[BitVector] = ObjectClass.selectDataCodec(objClass).encode(Some(obj.asInstanceOf[ConstructorData])).toOption
      if(outOpt.isDefined)
        out = outOpt.get
    }
    catch {
      case ex : Exception =>
        //catch and release, any sort of parse error
    }
    out
  }

  /**
    * Calculate the stream length in number of bits by factoring in the whole message in two portions.
    * This process automates for: object encoding.<br>
    * <br>
    * Ignoring the parent data, constant field lengths have already been factored into the results.
    * That includes:
    * the length of the stream length field (32u),
    * the object's class (11u),
    * the object's GUID (16u),
    * and the bit to determine if there will be parent data.
    * In total, these fields form a known fixed length of 60u.
    * @param parentInfo if defined, the relationship between this object and another object (its parent);
    *                   information about the parent adds either 24u or 32u
    * @param data if defined, the data used to construct this type of object;
    *             the data length is indeterminate until it is walked-through;
    *             note: the type is `StreamBitSize` as opposed to `ConstructorData`
    * @return the total length of the resulting data stream in bits
    */
  private def streamLen(parentInfo : Option[ObjectCreateMessageParent], data : StreamBitSize) : Long = {
    //knowable length
    val base : Long = if(parentInfo.isDefined) {
      if(parentInfo.get.slot > 127) 92L else 84L //(32u + 1u + 11u + 16u) ?+ (16u + (8u | 16u))
    }
    else {
      60L
    }
    base + data.bitsize
  }

  implicit val codec : Codec[ObjectCreateMessage] = (
    ("streamLength" | uint32L) ::
      (either(bool, parent, noParent).exmap[Pattern] (
        {
          case Left(a :: b :: Some(c) :: HNil) =>
            Attempt.successful(a :: b :: Some(c) :: HNil) //true, _, _, Some(c)
          case Right(a :: b :: None :: HNil) =>
            Attempt.successful(a :: b :: None :: HNil) //false, _, _, None
          // failure cases
          case Left(a :: b :: None :: HNil) =>
            Attempt.failure(Err("missing parent structure")) //true, _, _, None
          case Right(a :: b :: Some(c) :: HNil) =>
            Attempt.failure(Err("unexpected parent structure")) //false, _, _, Some(c)
        }, {
          case a :: b :: Some(c) :: HNil =>
            Attempt.successful(Left(a :: b :: Some(c) :: HNil))
          case a :: b :: None :: HNil =>
            Attempt.successful(Right(a :: b :: None :: HNil))
        }
      ) :+
        ("data" | bits)) //greed is good
    ).exmap[outPattern] (
    {
      case _ :: _ :: _ :: _ :: BitVector.empty :: HNil =>
        Attempt.failure(Err("no data to decode"))
      case len :: cls :: guid :: par :: data :: HNil =>
        Attempt.successful(len :: cls :: guid :: par :: decodeData(cls, data) :: HNil)
    },
    {
      case _ :: _ :: _ :: _ :: None :: HNil =>
        Attempt.failure(Err("no object to encode"))
      case _ :: cls :: guid :: par :: Some(obj) :: HNil =>
        Attempt.successful(streamLen(par, obj) :: cls :: guid :: par :: encodeData(cls, obj) :: HNil)
    }
  ).xmap[ObjectCreateMessage] (
    {
      case len :: cls :: guid :: par :: obj :: HNil =>
        ObjectCreateMessage(len, cls, guid, par, obj)
    },
    {
      case ObjectCreateMessage(len, cls, guid, par, obj) =>
        len :: cls :: guid :: par :: obj :: HNil
    }
  ).as[ObjectCreateMessage]
}
