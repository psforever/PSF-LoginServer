package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.bits._
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless._

import scala.annotation.switch
import scala.util.Try

abstract class ConstructorData

case class AmmoBoxData(magazine : Int) extends ConstructorData

object AmmoBoxData extends Marshallable[AmmoBoxData] {
  implicit val codec : Codec[AmmoBoxData] = (
    ("code" | uintL(23)) ::
      ("magazine" | uint16L)
    ).exmap[AmmoBoxData] (
      {
        case 0xC8 :: mag :: HNil =>
          Attempt.successful(AmmoBoxData(mag))
        case x :: _ :: HNil =>
          Attempt.failure(Err("code wrong - looking for 200, found "+x))
      },
      {
        case AmmoBoxData(mag) =>
          Attempt.successful(0xC8 :: mag :: HNil)
      }
    ).as[AmmoBoxData]
}

case class Mold(objectClass : Int,
                data : BitVector) {
  private var obj : Option[ConstructorData] = Mold.selectMold(objectClass, data)

  def isDefined : Boolean = this.obj.isDefined

  def get : ConstructorData = this.obj.get

  def set(data : ConstructorData) : Boolean = {
    var ret = false
    if(Some(data).isDefined) {
      obj = Some(data)
      ret = true
    }
    ret
  }
}

object Mold {
  def apply(objectClass : Int, obj : ConstructorData) : Mold =
    new Mold( objectClass, Mold.serialize(objectClass, obj) )

  private def selectMold(objClass : Int, data : BitVector) : Option[ConstructorData] = {
    var out : Option[ConstructorData] = None
    if(!data.isEmpty) {
      (objClass : @switch) match {
        case 0x1C =>
          val opt = AmmoBoxData.codec.decode(data).toOption
          if(opt.isDefined) {
            out = Some(opt.get.value)
          }
        case _ =>
          out = None
      }
    }
    out
  }

  private def serialize(objClass : Int, obj : ConstructorData) : BitVector = {
    var out = BitVector.empty
    try {
      (objClass : @switch) match {
        case 0x1C =>
          val opt = AmmoBoxData.codec.encode(obj.asInstanceOf[AmmoBoxData]).toOption
          if(opt.isDefined) {
            out = opt.get
          }
      }
    }
    catch {
      case ex : ClassCastException => {
        //TODO generate and log wrong class error message
      }
    }
    out
  }
}

/**
  * The parent information of a created object.<br>
  * <br>
  * Rather than a created-parent with a created-child relationship, the whole of the packet still only creates the child.
  * The parent is a pre-existing object into which the (created) child is attached.<br>
  * <br>
  * The slot is encoded as a string length integer commonly used by PlanetSide.
  * It is either a 0-127 eight bit number (0 = 0x80), or a 128-32767 sixteen bit number (128 = 0x0080).
  * @param guid the GUID of the parent object
  * @param slot a parent-defined slot identifier that explains where the child is to be attached to the parent
  */
case class ObjectCreateMessageParent(guid : PlanetSideGUID,
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
  * Knowing the object's class is essential for parsing the specific information passed by the `data` parameter.<br>
  * <br>
  * Exploration:<br>
  * Can we build a `case class` "foo" that can accept the `objectClass` and the `data` and construct any valid object automatically?
  * @param streamLength the total length of the data that composes this packet in bits, excluding the opcode and end padding
  * @param objectClass the code for the type of object being constructed
  * @param guid the GUID this object will be assigned
  * @param parentInfo if defined, the relationship between this object and another object (its parent)
  * @param mold the data used to construct this type of object;
  *             requires further object-specific processing
  */
case class ObjectCreateMessage(streamLength : Long,
                               objectClass : Int,
                               guid : PlanetSideGUID,
                               parentInfo : Option[ObjectCreateMessageParent],
                               mold : Mold)
  extends PlanetSideGamePacket {
  def opcode = GamePacketOpcode.ObjectCreateMessage
  def encode = ObjectCreateMessage.encode(this)
}

object ObjectCreateMessage extends Marshallable[ObjectCreateMessage] {
  type Pattern = Int :: PlanetSideGUID :: Option[ObjectCreateMessageParent] :: HNil
  /**
    * Codec for formatting around the lack of parent data in the stream.
    */
  val noParent : Codec[Pattern] = (
    ("objectClass" | uintL(0xb)) :: //11u
      ("guid" | PlanetSideGUID.codec) //16u
    ).xmap[Pattern] (
    {
      case cls :: guid :: HNil =>
        cls :: guid :: None :: HNil
    },
    {
      case cls :: guid :: None :: HNil =>
        cls :: guid :: HNil
    }
  )

  /**
    * Codec for reading and formatting parent data from the stream.
    */
  val parent : Codec[Pattern] = (
    ("parentGuid" | PlanetSideGUID.codec) :: //16u
      ("objectClass" | uintL(0xb)) :: //11u
      ("guid" | PlanetSideGUID.codec) :: //16u
      ("parentSlotIndex" | PacketHelpers.encodedStringSize) //8u or 16u
    ).xmap[Pattern] (
    {
      case pguid :: cls :: guid :: slot :: HNil =>
        cls :: guid :: Some(ObjectCreateMessageParent(pguid, slot)) :: HNil
    },
    {
      case cls :: guid :: Some(ObjectCreateMessageParent(pguid, slot)) :: HNil =>
        pguid :: cls :: guid :: slot :: HNil
    }
  )

  /**
    * Calculate the stream length in number of bits by factoring in the two variable fields.<br>
    * <br>
    * Constant fields have already been factored into the results.
    * That includes:
    * the length of the stream length field (32u),
    * the object's class (11u),
    * the object's GUID (16u),
    * and the bit to determine if there will be parent data.
    * In total, these fields form a known fixed length of 60u.
    * @param parentInfo if defined, the parentInfo adds either 24u or 32u
    * @param data the data length is indeterminate until it is read
    * @return the total length of the stream in bits
    */
  private def streamLen(parentInfo : Option[ObjectCreateMessageParent], data : BitVector) : Long = {
    //known length
    val first : Long = if(parentInfo.isDefined) {
      if(parentInfo.get.slot > 127) 92L else 84L //60u + 16u + (8u or 16u)
    }
    else {
      60L
    }
    //variant length
    var second : Long = data.size
    val secondMod4 : Long = second % 4L
    if(secondMod4 > 0L) { //pad to include last whole nibble
      second += 4L - secondMod4
    }
    first + second
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
        },
        {
          case a :: b :: Some(c) :: HNil =>
            Attempt.successful(Left(a :: b :: Some(c) :: HNil))
          case a :: b :: None :: HNil =>
            Attempt.successful(Right(a :: b :: None :: HNil))
        }
      ) :+
        ("data" | bits) )
    ).xmap[ObjectCreateMessage] (
    {
      case len :: cls :: guid :: info :: data :: HNil =>
        ObjectCreateMessage(len, cls, guid, info, Mold(cls, data))
    },
    {
      //the user should not have to manually supply a proper stream length, that's a restrictive requirement
      case ObjectCreateMessage(_, cls, guid, info, mold) =>
        streamLen(info, mold.data) :: cls :: guid :: info :: mold.data :: HNil
    }
  ).as[ObjectCreateMessage]
}
