package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.DecodeResult
import net.psforever.types.Vector3
import scodec.bits._
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless._

import scala.annotation.switch

abstract class ConstructorData

case class AmmoBoxData(magazine : Int) extends ConstructorData

object AmmoBoxData extends Marshallable[AmmoBoxData] {
  implicit val codec : Codec[AmmoBoxData] = (
    uintL(8) ::
      ignore(15) ::
      ("magazine" | uint16L)
    ).exmap[AmmoBoxData] (
    {
      case 0xC8 :: _ :: mag :: HNil =>
        Attempt.successful(AmmoBoxData(mag))
      case x :: _ :: _ :: HNil =>
        Attempt.failure(Err("looking for 200, found "+x))
    },
    {
      case AmmoBoxData(mag) =>
        Attempt.successful(0xC8 :: () :: mag :: HNil)
    }
  ).as[AmmoBoxData]
}

case class WeaponData(unk : Int,
                      ammo : InternalSlot) extends ConstructorData

object WeaponData extends Marshallable[WeaponData] {
  def apply(unk : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : AmmoBoxData) : WeaponData =
    new WeaponData(unk, InternalSlot(cls, guid, parentSlot, Some(ammo)))

  implicit val codec : Codec[WeaponData] = (
    ("unk" | uint4L) ::
      uint4L ::
      ignore(20) ::
      uint4L ::
      ignore(16) ::
      uintL(11) ::
      ("ammo" | InternalSlot.codec)
    ).exmap[WeaponData] (
    {
      case code :: 8 :: _ :: 2 :: _ :: 0x2C0 :: ammo :: HNil =>
        Attempt.successful(WeaponData(code, ammo))
      case _ :: x :: _ ::  y :: _ :: z :: _ :: HNil =>
        Attempt.failure(Err("looking for 8-2-704 pattern, found %d-%d-%d".format(x,y,z))) //TODO I actually don't know what of this is actually important
    },
    {
      case WeaponData(code, ammo) =>
        Attempt.successful(code :: 8 :: () :: 2 :: () :: 0x2C0 :: ammo :: HNil)
    }
  ).as[WeaponData]
}

case class RibbonBars(upper : Int, //0xFFFF means no merit (for all ...)
                      middle : Int,
                      lower : Int,
                      tos : Int)

object RibbonBars extends Marshallable[RibbonBars] {
  implicit val codec : Codec[RibbonBars] = (
    ("upper" | uint16L) ::
      ("middle" | uint16L) ::
      ("lower" | uint16L) ::
      ("tos" | uint16L)
    ).as[RibbonBars]
}

case class CharacterData(pos : Vector3,
                         objYaw : Int,
                         faction : Int,
                         bops : Boolean,
                         name : String,
                         exosuit : Int,
                         sex : Int,
                         face1 : Int,
                         face2 : Int,
                         voice : Int,
                         unk1 : Int, //0x8080
                         unk2 : Int, //0xFFFF or 0x0
                         unk3 : Int, //2
                         viewPitch : Int,
                         viewYaw : Int,
                         upperMerit : Int, //0xFFFF means no merit (for all ...)
                         middleMerit : Int,
                         lowerMerit : Int,
                         termOfServiceMerit : Int,
//                         healthMax : Int,
//                         health : Int,
//                         armor : Int,
//                         unk4 : Int, //1
//                         unk5 : Int, //7
//                         unk6 : Int, //7
//                         staminaMax : Int,
//                         stamina : Int,
//                         unk7 : Int, // 192
//                         unk8 : Int, //66
//                         unk9 : Int, //197
//                         unk10 : Int, //70
//                         unk11 : Int, //134
//                         unk12 : Int, //199
//                         firstTimeEvent_length : Long,
//                         firstEntry : Option[String],
//                         firstTimeEvent_list : List[String],
//                         tutorial_list : List[String],
                         inventory : BitVector
                        ) extends ConstructorData

object CharacterData extends Marshallable[CharacterData] {
  val ribbonBars : Codec[RibbonBars] = (
    ("upper" | uint16L) ::
      ("middle" | uint16L) ::
      ("lower" | uint16L) ::
      ("tos" | uint16L)
    ).as[RibbonBars]

  implicit val codec : Codec[CharacterData] = (
    ("pos" | Vector3.codec_pos) ::
      ignore(16) ::
      ("objYaw" | uint8L) ::
      ignore(1) ::
      ("faction" | uintL(2)) ::
      ("bops" | bool) ::
      ignore(20) ::
      ("name" | PacketHelpers.encodedWideStringAligned(4)) ::
      ("exosuit" | uintL(3)) ::
      ignore(2) ::
      ("sex" | uintL(2)) ::
      ("face1" | uint4L) ::
      ("face2" | uint4L) ::
      ("voice" | uintL(3)) ::
      ignore(22) ::
      ("unk1" | uint16L) ::
      ignore(42) ::
      ("unk2" | uint16L) ::
      ignore(30) ::
      ("unk3" | uintL(4)) ::
      ignore(24) ::
      ("viewPitch" | uint8L) ::
      ("viewYaw" | uint8L) ::
      ignore(10) ::
      ("upperMerit" | uint16L) ::
      ("middleMerit" | uint16L) ::
      ("lowerMerit" | uint16L) ::
      ("termOfServiceMerit" | uint16L) ::
//      ignore(160) ::
//      ("healthMax" | uint16L) ::
//      ("health" | uint16L) ::
//      ignore(1) ::
//      ("armor" | uint16L) ::
//      ignore(9) ::
//      ("unk4" | uint8L) ::
//      ignore(8) ::
//      ("unk5" | uint4L) ::
//      ("unk6" | uintL(3)) ::
//      ("staminaMax" | uint16L) ::
//      ("stamina" | uint16L) ::
//      ignore(152) ::
//      ("unk7" | uint16L) ::
//      ("unk8" | uint8L) ::
//      ("unk9" | uint8L) ::
//      ("unk10" | uint8L) ::
//      ("unk11" | uint8L) ::
//      ("unk12" | uintL(12)) ::
//      ignore(3) ::
//      (("firstTimeEvent_length" | uint32L) >>:~ { len =>
//        conditional(len > 0, "firstEntry" | PacketHelpers.encodedStringAligned(5)) ::
//          ("firstTimeEvent_list" | PacketHelpers.listOfNSized(len - 1, PacketHelpers.encodedString)) ::
//          ("tutorial_list" | PacketHelpers.listOfNAligned(uint32L, 0, PacketHelpers.encodedString)) ::
//          ignore(207) ::
          ("inventory" | bits)
//      })
  ).as[CharacterData]
}

/**
  * The same kind of data as required for a formal ObjectCreateMessage but with a required and implicit parent relationship.
  * Data preceding this entry will define the existence of the parent.
  * @param objectClass na
  * @param guid na
  * @param parentSlot na
  * @param obj na
  */
case class InternalSlot(objectClass : Int,
                        guid : PlanetSideGUID,
                        parentSlot : Int,
                        obj : Option[ConstructorData])

object InternalSlot extends Marshallable[InternalSlot] {
  type objPattern = Int :: PlanetSideGUID :: Int :: Option[ConstructorData] :: HNil

  implicit val codec : Codec[InternalSlot] = (
    ignore(1) :: //TODO determine what this bit does
      ("objectClass" | uintL(11)) ::
      ("guid" | PlanetSideGUID.codec) ::
      ("parentSlot" | PacketHelpers.encodedStringSize) ::
      bits
    ).exmap[objPattern] (
    {
      case _ :: cls :: guid :: slot :: data :: HNil =>
        Attempt.successful(cls :: guid :: slot :: Mold.decode(cls, data) :: HNil)
    },
    {
      case cls :: guid :: slot :: None :: HNil =>
        Attempt.failure(Err("no constuctor data could be found"))
      case cls :: guid :: slot :: mold :: HNil =>
        Attempt.successful(() :: cls :: guid :: slot :: Mold.encode(cls, mold.get) :: HNil)
    }
    ).exmap[objPattern] (
    {
      case cls :: guid :: slot :: None :: HNil =>
        Attempt.failure(Err("no decoded constructor data"))
      case cls :: guid :: slot :: mold :: HNil =>
        Attempt.successful(cls :: guid :: slot :: mold :: HNil)
    },
    {
      case cls :: guid :: slot :: BitVector.empty :: HNil =>
        Attempt.failure(Err("no encoded constructor data"))
      case cls :: guid :: slot :: data :: HNil =>
        Attempt.successful(cls :: guid :: slot :: data :: HNil)
    }
  ).as[InternalSlot]
}

case class Mold(objectClass : Int,
                data : BitVector) {
  private var obj : Option[ConstructorData] = Mold.decode(objectClass, data)

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
    new Mold( objectClass, Mold.encode(objectClass, obj) )

  def decode(objClass : Int, data : BitVector) : Option[ConstructorData] = {
    var out : Option[ConstructorData] = None
    if(!data.isEmpty) {
      var outOpt : Option[DecodeResult[_]] = None
      try {
        (objClass : @switch) match {
          case 0x79 => //avatars
            outOpt = CharacterData.codec.decode(data).toOption
          case 0x1C => //9mm
            outOpt = AmmoBoxData.codec.decode(data).toOption
          case 0x46 => //beamer
            outOpt = WeaponData.codec.decode(data).toOption
          case 0x159 => //gauss
            outOpt = WeaponData.codec.decode(data).toOption
          case _ =>
        }
        if(outOpt.isDefined)
          out = Some(outOpt.get.value.asInstanceOf[ConstructorData])
      }
      catch {
        case ex : ClassCastException =>
        //TODO generate and log wrong class error message
        case ex : Exception =>
        //TODO generic error
      }
    }
    out
  }

  def encode(objClass : Int, obj : ConstructorData) : BitVector = {
    var out = BitVector.empty
    try {
      var outOpt : Option[BitVector] = None
      (objClass : @switch) match {
        case 0x1C => //9mm
          outOpt = AmmoBoxData.codec.encode(obj.asInstanceOf[AmmoBoxData]).toOption
        case 0x46 => //beamer
          outOpt = WeaponData.codec.encode(obj.asInstanceOf[WeaponData]).toOption
        case 0x159 => //gauss
          outOpt = WeaponData.codec.encode(obj.asInstanceOf[WeaponData]).toOption
        case _ =>
          throw new ClassCastException("cannot find object code - "+objClass)
      }
      if(outOpt.isDefined)
        out = outOpt.get
    }
    catch {
      case ex : ClassCastException =>
      //TODO generate and log wrong class error message
      case ex : Exception =>
      //TODO generic error
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
      case ObjectCreateMessage(_, cls, guid, info, mold) =>
        streamLen(info, mold.data) :: cls :: guid :: info :: mold.data :: HNil
    }
  ).as[ObjectCreateMessage]
}
