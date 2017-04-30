// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.PacketHelpers
import net.psforever.packet.game.PlanetSideGUID
import scodec.{Attempt, Codec, DecodeResult, Err}
import scodec.bits.BitVector
import scodec.codecs.{bool, either, uintL}
import shapeless.{::, HNil}
import scodec.codecs._

/**
  * The parent information of a created object.<br>
  * <br>
  * In normal packet data order, there are two ways the parent object can be assigned.
  * The first is an implicit association between a parent object and a child object that are both created at the same time.
  * A player character object, for example, is initialized in the same breath as the objects in his inventory are initialized.
  * A weapon object is constructed with an ammunition object already included within itself.
  * The second is an explicit association between the child and the parent where the parent exists before the child is created.
  * When a new inventory object is produced, it is usually assigned to some other existing object's inventory.
  * That is the relationship to the role of "parent" that this object defines.
  * As such, only its current unique identifier needs to be provided.
  * If the parent can not be found, the child object is not created.<br>
  * <br>
  * A third form of parent object to child object association involves the impromptu assignment of an existing child to an existing parent.
  * Since no objects are being created, that is unrelated to `ObjectCreateMessage`.
  * Refer to `ObjectAttachMessage`, `MountVehicleMsg`, and `MountVehicleCargoMsg`.<br>
  * <br>
  * When associated, the child object is "attached" to the parent object at a specific location called a "slot."
  * "Slots" are internal to the object and are (typically) invisible to the player.
  * Any game object can possess any number of "slots" that serve specific purposes.
  * Player objects have equipment holsters and grid inventory capacity.
  * Weapon objects have magazine feed positions.
  * Vehicle objects have seating for players and trunk inventory capacity.
  * @param guid the GUID of the parent object
  * @param slot a parent-defined slot identifier that explains where the child is to be attached to the parent;
  *             encoded as the length field of a Pascal string
  */
final case class ObjectCreateMessageParent(guid : PlanetSideGUID,
                                           slot : Int)

object ObjectCreateBase {
  private type basePattern = Long :: Int :: PlanetSideGUID :: Option[ObjectCreateMessageParent] :: BitVector :: HNil
  private type parentPattern = Int :: PlanetSideGUID :: Option[ObjectCreateMessageParent] :: HNil

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
  def streamLen(parentInfo : Option[ObjectCreateMessageParent], data : StreamBitSize) : Long = {
    //knowable length
    val base : Long = if(parentInfo.isDefined) {
      if(parentInfo.get.slot > 127) 92L else 84L //(32u + 1u + 11u + 16u) ?+ (16u + (8u | 16u))
    }
    else {
      60L
    }
    base + data.bitsize
  }

  /**
    * Take bit data and transform it into an object that expresses the important information of a game piece.
    * This function is fail-safe because it catches errors involving bad parsing of the bitstream data.
    * Generally, the `Exception` messages themselves are not useful here.
    * The important parts are what the packet thought the object class should be and what it actually processed.
    * @param objectClass the code for the type of object being constructed
    * @param data the bitstream data
    * @param getCodecFunc a lookup function that returns a `Codec` for this object class
    * @return the optional constructed object
    * @see `ObjectClass`
    */
  def decodeData(objectClass : Int, data : BitVector, getCodecFunc : (Int) => Codec[ConstructorData.genericPattern]) : Option[ConstructorData] = {
    var out : Option[ConstructorData] = None
    try {
      val outOpt : Option[DecodeResult[_]] = getCodecFunc(objectClass).decode(data).toOption
      if(outOpt.isDefined)
        out = outOpt.get.value.asInstanceOf[ConstructorData.genericPattern]
    }
    catch {
      case _ : Exception =>
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
    * @param getCodecFunc a lookup function that returns a `Codec` for this object class
    * @return the bitstream data
    * @see `ObjectClass`
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

  /**
    * `Codec` for formatting around the lack of parent data in the stream.
    */
  private val noParent : Codec[parentPattern] = (
    ("objectClass" | uintL(0xb)) :: //11u
      ("guid" | PlanetSideGUID.codec) //16u
    ).xmap[parentPattern](
    {
      case cls :: guid :: HNil =>
        cls :: guid :: None :: HNil
    }, {
      case cls :: guid :: None :: HNil =>
        cls :: guid :: HNil
    }
  )
  /**
    * `Codec` for reading and formatting parent data from the stream.
    */
  private val parent : Codec[parentPattern] = (
    ("parentGuid" | PlanetSideGUID.codec) :: //16u
      ("objectClass" | uintL(0xb)) :: //11u
      ("guid" | PlanetSideGUID.codec) :: //16u
      ("parentSlotIndex" | PacketHelpers.encodedStringSize) //8u or 16u
    ).xmap[parentPattern](
    {
      case pguid :: cls :: guid :: slot :: HNil =>
        cls :: guid :: Some(ObjectCreateMessageParent(pguid, slot)) :: HNil
    }, {
      case cls :: guid :: Some(ObjectCreateMessageParent(pguid, slot)) :: HNil =>
        pguid :: cls :: guid :: slot :: HNil
    }
  )

  /**
    * `Codec` for handling the primary fields of both `ObjectCreateMessage` packets and `ObjectCreateDetailedMessage` packets.
    */
  val baseCodec : Codec[basePattern] =
    ("streamLength" | uint32L) ::
      (either(bool, parent, noParent).exmap[parentPattern] (
        {
          case Left(a :: b :: Some(c) :: HNil) =>
            Attempt.successful(a :: b :: Some(c) :: HNil) //true, _, _, Some(c)
          case Right(a :: b :: None :: HNil) =>
            Attempt.successful(a :: b :: None :: HNil) //false, _, _, None
          // failure cases
          case Left(_ :: _ :: None :: HNil) =>
            Attempt.failure(Err("missing parent structure")) //true, _, _, None
          case Right(_ :: _ :: Some(_) :: HNil) =>
            Attempt.failure(Err("unexpected parent structure")) //false, _, _, Some(c)
        }, {
          case a :: b :: Some(c) :: HNil =>
            Attempt.successful(Left(a :: b :: Some(c) :: HNil))
          case a :: b :: None :: HNil =>
            Attempt.successful(Right(a :: b :: None :: HNil))
        }
      ) :+
        ("data" | bits)) //greed is good
}
