// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

import scala.collection.mutable

/**
  * The server instructs clients to render a certain avatar not operated by its player to move in a certain way.<br>
  * <br>
  * The avatar model normally moves from where it currently is to `pos`.
  * When `vel` is defined, `pos` is treated as where the avatar model starts its animation;
  * and, from there, it moves a certain distance as according to the values.
  * The repositioning always takes the same amount of time and the player model is left in running animation (in place).
  * The coordinates evaluate between -256.0 and 256.0.<br>
  * <br>
  * facingYaw:<br>
  * `0x00` -- E<br>
  * `0x10` -- NE<br>
  * `0x20` -- N<br>
  * `0x30` -- NW<br>
  * `0x40` -- W<br>
  * `0x50` -- SW<br>
  * `0x60` -- S<br>
  * `0x70` -- SE<br>
  * `0x80` -- E<br>
  * <br>
  * facingPitch:<br>
  * `0x00`-`0x20` -- downwards-facing angles, with `0x00` as forwards-facing<br>
  * `0x21`-`0x40` -- downwards-facing<br>
  * `0x41`-`0x59` -- upwards-facing<br>
  * `0x60`-`0x80` -- upwards-facing angles, with `0x80` as forwards-facing<br>
  * <br>
  * facingYawUpper:<br>
  * `0x00`-`0x20` -- turning to left, with `0x00` being forward-facing<br>
  * `0x21`-`0x40` -- facing leftwards<br>
  * `0x41`-`0x59` -- facing rightwards<br>
  * `0x60`-`0x80` -- turning to right, with `0x80` being forward-facing
  *
  * @param guid the avatar's guid
  * @param pos the position of the avatar in the world environment (in three coordinates)
  * @param vel an optional velocity
  * @param facingYaw the angle with respect to the horizon towards which the avatar is looking;
  *                  every `0x1` is about 2.8125 degrees;
  *                  measurements are counter-clockwise from East
  * @param facingPitch the angle with respect to the sky and the ground towards which the avatar is looking;
  *                    every `0x1` is about 2.8125 degrees
  * @param facingYawUpper the angle of the avatar's upper body with respect to its forward-facing direction;
  *                       every `0x1` is about 2.8125 degrees
  * @param unk1 na
  * @param fourBools set to `false` to parse the following four fields, otherwise those values will be ignored
  * @param isCrouching avatar is crouching;
  *                    must remain flagged for crouch to maintain animation;
  *                    turn off to stand up
  * @param isJumping avatar is jumping;
  *                  must remain flagged for jump to maintain animation;
  *                  turn off to land(?)
  * @param unk2 na
  * @param unk3 na
  */
final case class PlayerStateMessage(guid : PlanetSideGUID,
                                    pos : Vector3,
                                    vel : Option[Vector3],
                                    facingYaw : Int,
                                    facingPitch : Int,
                                    facingYawUpper : Int,
                                    unk1 : Int,
                                    fourBools : Boolean,
                                    isCrouching : Boolean = false,
                                    isJumping : Boolean = false,
                                    unk2 : Boolean = false,
                                    unk3 : Boolean = false)
  extends PlanetSideGamePacket {
  type Packet = PlayerStateMessage
  def opcode = GamePacketOpcode.PlayerStateMessage
  def encode = PlayerStateMessage.encode(this)
}

object PlayerStateMessage extends Marshallable[PlayerStateMessage] {
  type fourBoolPattern = Boolean :: Boolean :: Boolean :: Boolean :: HNil

  /**
    * A `Codec` for reading out the four `Boolean` values near the end of the formal packet.
    */
  val booleanCodec : Codec[fourBoolPattern] = (
    ("isCrouching" | bool) ::
      ("isJumping" | bool) ::
      ("unk2" | bool) ::
      ("unk3" | bool)
    ).as[fourBoolPattern]

  /**
    * A `Codec` for ignoring the four values at the end of the formal packet (all set to `false`).
    */
  val defaultCodec : Codec[fourBoolPattern] = ignore(0).xmap[fourBoolPattern] (
    {
      case _ =>
        false :: false :: false :: false :: HNil
    },
    {
      case _ =>
        ()
    }
  ).as[fourBoolPattern]

  implicit val codec : Codec[PlayerStateMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos) ::
      (bool >>:~ { b1 =>
        conditional(b1, "unk1" | Vector3.codec_vel) ::
          ("facingYaw" | uint8L) ::
          ("facingPitch" | uint8L) ::
          ("facingYawUpper" | uint8L) ::
          ("unk1" | uintL(10)) ::
          ("fourBools" | bool >>:~ { b2 =>
            ignore(0) ::
              newcodecs.binary_choice(!b2, booleanCodec, defaultCodec)
          })
      })
    ).xmap[PlayerStateMessage] (
    {
      case uid :: p :: true :: Some(extra) :: f1 :: f2 :: f3 :: u :: b :: _ :: b1 :: b2 :: b3 :: b4 :: HNil =>
        PlayerStateMessage(uid, p, Some(extra), f1, f2, f3, u, b, b1, b2, b3, b4)
      case uid :: p :: false :: None :: f1 :: f2 :: f3 :: u :: b :: _ :: b1 :: b2 :: b3 :: b4 :: HNil =>
        PlayerStateMessage(uid, p, None, f1, f2, f3, u, b, b1, b2, b3, b4)
    },
    {
      case PlayerStateMessage(uid, p, Some(extra), f1, f2, f3, u, b, b1, b2, b3, b4) =>
        uid :: p :: true :: Some(extra) :: f1 :: f2 :: f3 :: u :: b :: () :: b1 :: b2 :: b3 :: b4 :: HNil
      case PlayerStateMessage(uid, p, None, f1, f2, f3, u, b, b1, b2, b3, b4) =>
        uid :: p :: false :: None :: f1 :: f2 :: f3 :: u :: b :: () :: b1 :: b2 :: b3 :: b4 :: HNil
    }
  )
}

//TODO the following logic is unimplemented
/*
There is a bool that is currently unhandled that determines if the packet is aware that this code would run.
If it passes, the first 8-bit value is the number of times the data will be iterated over.
On each pass, a 4-bit value is extracted from the packet and compared against 15.
When 15 is read, an 8-bit value is read on that same turn.
On each subsequent turn, 8-bit values will be read until the number of iterations or there is an exception.
I have no clue what any of this is supposed to do.
 */
/**
  * na
  * @param size a length to be applied to the next list, but not necessarily the length of that list
  * @param data a list of data that comes in either a single 8-bit value, or a 4-bit value and, maybe, an 8-bit value
  */
final case class Extra1(size : Int,
                        data : List[Extra2])

/**
  * na
  * @param unk1 na;
  *             the first 8-bit value in one-value form or the first 4-bit value in two-value form;
  *             in two-value form, when equal to 15, the second value is read
  * @param unk2 na;
  *             the potential second 8-bit value in two-value form
  * @param more the next data in the sequence
  */
final case class Extra2(unk1 : Int,
                        unk2 : Option[Int],
                        more : Option[Extra2] = None)

object Extra1 {
  /**
    * Take a chain of `Extra2` objects produced from decoding and compress it into a `List`.
    * @param lst the list in which the `Extra2` data will be stored
    * @param nesting the current link in the chain of `Extra2` objects
    */
  private def packExtraList(lst : mutable.ListBuffer[Extra2], nesting : Option[Extra2]) : Unit = {
    if(nesting.isEmpty) { //escape case
      return
    }
    val elem : Extra2 = nesting.get
    lst += Extra2(elem.unk1, elem.unk2)
    packExtraList(lst, elem.more) //tail recursion
  }

  /**
    * Take a `List` of `Extra2` objects for encoding and expand it into a chain.
    * @param iter the iterator for a `List` of `Extra2` data
    * @return the head of a chain of `Extra2` objects
    */
  private def unpackExtraList(iter : Iterator[Extra2]) : Option[Extra2] = {
    //TODO as I don't think I can use tail recursion, how do I do this iteratively?
    if(!iter.hasNext)
      return None
    val elem : Extra2 = iter.next
    Some(Extra2(elem.unk1, elem.unk2, unpackExtraList(iter)))
  }

  implicit val codec : Codec[Extra1] = (
    ("size" | uint8L) >>:~ { sz =>
      ignore(0) ::
        //external logic: the client checks sz < dword_D33D38 before decoding beyond this point
        conditional(sz != 0, "data" | Extra2.processData(sz))
    }
    ).xmap[Extra1] (
    {
      case a :: _ :: None :: HNil =>
        Extra1(a, List.empty) //it's okay if a != 0
      case a :: _ :: b :: HNil =>
        val list = mutable.ListBuffer[Extra2]()
        packExtraList(list, b)
        Extra1(a, list.toList)
    },
    {
      case Extra1(a, b) =>
        if(b.isEmpty)
          a :: () :: None :: HNil
        else
          a :: () :: unpackExtraList(b.iterator) :: HNil
    }
  )
}

object Extra2 {
  /**
    * An abbreviated constructor for the one-value form.
    * @param a na
    * @return an `Extra2` object
    */
  def apply(a : Int) : Extra2 = {
    Extra2(a, None)
  }

  /**
    * An abbreviated constructor for the two-value form.
    * @param a na
    * @param b na
    * @return an `Extra2` object
    */
  def apply(a : Int, b : Int) : Extra2 = {
    Extra2(a, Some(b))
  }

  /**
    * A `Codec` for reading a single value.
    */
  private val oneValueCodec : Codec[Extra2] = (
    ignore(0) ::
      ("unk2" | uint8L)
    ).xmap[Extra2] (
    {
      case _ :: a :: HNil =>
        Extra2(a, None, None)
    },
    {
      case Extra2(a, None, _) =>
        () :: a :: HNil
    }
  )

  /**
    * A `Codec` for reading potentially two values.
    */
  private val twoValueCodec : Codec[Extra2] = (
    ("unk1" | uint4L) >>:~ { unk =>
      ignore(0) ::
        conditional(unk == 15, "unk2" | uint8L)
    }
    ).xmap[Extra2] (
    {
      case a :: _ :: b :: HNil =>
        Extra2(a, b, None)
    },
    {
      case Extra2(a, b, _) =>
        a :: () :: b :: HNil
    }
  )

  /**
    * A recursive `Codec` that allows for swapping between different `Codec`s to account for two ways to parse the next element.
    * The function calls itself to process each element in the sequence of data in the same manner until complete.
    * The `Extra2` object that is recovered from the first choice of `Codec`s is merely an intermediary object.
    * Due to immutability, the initial object is repackaged to append the chain of `Extra2` in an `Extra2` object.
    * @param size the number of iterations of the looping process left to perform, including this one
    * @param form determine whether we use `oneValueCodec` or `twoValueCodec`;
    *             should be set to `false` at first and set to `true` when two values are read in one pass;
    *             it will stay as `false` until set to `true`, whereupon it will always be `true`
    * @return a `Codec` the translates a chain of `Extra2` data
    */
  def processData(size : Int, form : Boolean = false) : Codec[Extra2] = (
    newcodecs.binary_choice(form, Extra2.oneValueCodec, Extra2.twoValueCodec) >>:~ { elem =>
      ignore(0) ::
        conditional(size > 0, newcodecs.binary_choice(form || elem.unk2.isDefined,
          Extra2.processData(size - 1, true),
          Extra2.processData(size - 1))
        )
    }
    ).xmap[Extra2] (
    {
      case a :: _ :: b :: HNil =>
        Extra2(a.unk1, a.unk2, b)
    },
    {
      case Extra2(a, b, c) =>
        Extra2(a, b) :: () :: c :: HNil
    }
  )
}
