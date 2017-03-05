// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

import scala.collection.mutable

/**
  * The server instructs some clients to render a player (usually not that client's avatar) to move in a certain way.<br>
  * <br>
  * This packet instructs the basic aspects of how the player character is positioned and how the player character moves.
  * Each client keeps track of where a character "currently" is according to that client.
  * `pos` reflects an update in regards to where the character should be moved.
  * Data between this "currently" and "new" are interpolated over a fixed time interval.
  * Position and velocity data is standard to normal PlanetSide ranges.
  * All angles follow the convention that every `0x1` is about 2.8125 degrees; so, `0x10` is 45.0 degrees.<br>
  * <br>
  * The avatar model normally moves from where it "currently" is to `pos`.
  * When `vel` is defined, `pos` is treated as where the avatar model starts its animation.
  * In that case, it sppears to teleport to `pos` to carry out the interpolated movement according to `vel`.
  * After the move, it remains at essentially `pos + vel * t`.
  * The repositioning always takes the same amount of time.
  * The player model is left in a walking/running animation (in place) until directed otherwise.<br>
  * <br>
  * If the model must interact with the environment during a velocity-driven move, it copes with local physics.
  * A demonstration of this is what happens when one player "runs past"/"into" another player running up stairs.
  * The climbing player is frequently reported by the other to appear to bounce over that player's head.
  * If the other player is off the ground, passing too near to the observer can cause a rubber band effect on trajectory.
  * This effect is entirely client-side to the observer and affects the moving player in no way.<br>
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
  *                  the model's whole body is facing this direction;
  *                  measurements are counter-clockwise from East
  * @param facingPitch the angle with respect to the sky and the ground towards which the avatar is looking
  * @param facingYawUpper the angle of the avatar's upper body with respect to its forward-facing direction
  * @param unk1 na
  * @param is_crouching avatar is crouching
  * @param is_jumping avatar is jumping;
  *                   must remain flagged for jump to maintain animation
  * @param unk2 na
  * @param is_cloaked avatar is cloaked by virtue of an Infiltration Suit
  */
final case class PlayerStateMessage(guid : PlanetSideGUID,
                                    pos : Vector3,
                                    vel : Option[Vector3],
                                    facingYaw : Int,
                                    facingPitch : Int,
                                    facingYawUpper : Int,
                                    unk1 : Int,
                                    is_crouching : Boolean = false,
                                    is_jumping : Boolean = false,
                                    unk2 : Boolean = false,
                                    is_cloaked : Boolean = false)
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
    ("is_crouching" | bool) ::
      ("is_jumping" | bool) ::
      ("unk2" | bool) ::
      ("is_cloaked" | bool)
    ).as[fourBoolPattern]

  /**
    * A `Codec` for ignoring the four values at the end of the formal packet (all set to `false`).
    */
  val defaultCodec : Codec[fourBoolPattern] = ignore(0).hlist.xmap[fourBoolPattern] (
    {
      case _ :: HNil =>
        false :: false :: false :: false :: HNil
    },
    {
      case _ :: _ :: _ :: _ :: HNil =>
        () :: HNil
    }
  ).as[fourBoolPattern]

  implicit val codec : Codec[PlayerStateMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos) ::
      optional(bool, "unk1" | Vector3.codec_vel) ::
      ("facingYaw" | uint8L) ::
      ("facingPitch" | uint8L) ::
      ("facingYawUpper" | uint8L) ::
      ("unk1" | uintL(10)) ::
      (bool >>:~ { fourBools =>
        newcodecs.binary_choice(!fourBools, booleanCodec, defaultCodec)
      })
    ).xmap[PlayerStateMessage] (
    {
      case uid :: pos :: vel :: f1 :: f2 :: f3 :: u :: _ :: b1 :: b2 :: b3 :: b4 :: HNil =>
        PlayerStateMessage(uid, pos, vel, f1, f2, f3, u, b1, b2, b3, b4)
    },
    {
      case PlayerStateMessage(uid, pos, vel, f1, f2, f3, u, b1, b2, b3, b4) =>
        val b : Boolean = !(b1 || b2 || b3 || b4)
        uid :: pos :: vel :: f1 :: f2 :: f3 :: u :: b :: b1 :: b2 :: b3 :: b4 :: HNil
    }
  )
}

//TODO the following logic is unimplemented
/*
There is a boolean that is currently unhandled(?) that determines if the packet is aware that this code would run.
If it passes, the first 8-bit value is the number of times the data will be iterated over.
On each pass, a 4-bit value is extracted from the packet and compared against 15.
When 15 is read, an 8-bit value is read on that same turn.
On each subsequent turn, 8-bit values will be read until the number of iterations or until there is an exception.
Until I find a packet that responds somehow, I have no clue what any of this is supposed to do.
 */
/**
  * na
  * @param size a length to be applied to the next list, but not necessarily the length of that list
  *             (if I could prove that size == list.size always then I could eliminate superfluous logic from `Extra1`)
  * @param data a list of data that comes as either an 8-bit value, or as a 4-bit value and, maybe, an 8-bit value
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
        //external logic: the client checks sz < dword_D33D38 before decoding beyond this point
        conditional(sz != 0, "data" | Extra2.processData(sz)).hlist
    }
    ).xmap[Extra1] (
    {
      case a :: None :: HNil =>
        Extra1(a, List.empty) //it's okay if a != 0
      case a :: b :: HNil =>
        val list = mutable.ListBuffer[Extra2]()
        packExtraList(list, b)
        Extra1(a, list.toList)
    },
    {
      case Extra1(a, Nil) =>
        a :: None :: HNil
      case Extra1(a, b) =>
        a :: unpackExtraList(b.iterator) :: HNil
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
  private val oneValueCodec : Codec[Extra2] = ("unk2" | uint8L).hlist.xmap[Extra2] (
    {
      case  a :: HNil =>
        Extra2(a, None, None)
    },
    {
      case Extra2(a, None, _) =>
         a :: HNil
    }
  )

  /**
    * A `Codec` for reading potentially two values.
    */
  private val twoValueCodec : Codec[Extra2] = (
    ("unk1" | uint4L) >>:~ { unk =>
      conditional(unk == 15, "unk2" | uint8L).hlist
    }
    ).xmap[Extra2] (
    {
      case a :: b :: HNil =>
        Extra2(a, b, None)
    },
    {
      case Extra2(a, b, _) =>
        a :: b :: HNil
    }
  )

  /**
    * Half of a recursive `Codec` that allows for swapping between different `Codec`s in between `List` elements.<br>
    * <br>
    * The function calls itself to process each element in the sequence of data in the same manner until complete.
    * The `Extra2` object that is recovered from the first choice of `Codec`s is merely an intermediary object.
    * Due to immutability, the initial object is repackaged to append the chain of `Extra2` in an `Extra2` object.
    * Eventually, `processData` will parse a 4-bit value of 15 and will pass control over to `processDataSingle`.
    * @param size the number of iterations of the looping process left to perform, including this one
    * @return a `Codec` translating a chain of `Extra2` data
    * @see Extra2.processDataSingle
    */
  def processData(size : Int) : Codec[Extra2] = (
    //TODO: without tail recursion, this might cause a stack overflow
    twoValueCodec >>:~ { elem =>
      conditional(size > 0, newcodecs.binary_choice(elem.unk2.isDefined,
        processDataSingle(size - 1),
        processData(size - 1))
      ).hlist
    }
    ).xmap[Extra2] (
    {
      case a :: b :: HNil =>
        Extra2(a.unk1, a.unk2, b)
    },
    {
      case Extra2(a, b, c) =>
        Extra2(a, b) :: c :: HNil
    }
  )

  /**
    * Latter half of a recursive `Codec` that allows for swapping between different `Codec`s in between `List` elements.
    * This `Codec` no longer performs swapping and merely runs out the data.<br>
    * <br>
    * @param size the number of iterations of the looping process left to perform, including this one
    * @return a `Codec` translating a chain of `Extra2` data
    * @see Extra2.processData
    */
  private def processDataSingle(size : Int) : Codec[Extra2] = (
    //TODO: without tail recursion, this might cause a stack overflow
    oneValueCodec >>:~ { elem =>
      conditional(size > 0, processDataSingle(size - 1)).hlist
    }
    ).xmap[Extra2] (
    {
      case a :: b :: HNil =>
        Extra2(a.unk1, a.unk2, b)
    },
    {
      case Extra2(a, b, c) =>
        Extra2(a, b) :: c :: HNil
    }
  )
}
