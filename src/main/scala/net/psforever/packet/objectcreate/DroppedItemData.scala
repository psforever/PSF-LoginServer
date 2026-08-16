// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Provide information that positions a given object on the ground in the game world.
  * @param pos where and how the object is oriented
  * @param obj the object on the ground
  * @tparam T a subclass of `ConstructorData` that indicates what type the object is
  */
final case class DroppedItemData[T <: ConstructorData](pos: PlacementData, obj: T) extends ConstructorData {
  override def bitsize: Long = pos.bitsize + obj.bitsize
}

object DroppedItemData {

  /**
    * Transform `DroppedItemData[T]` for object type `T` into `ConstructorData`.<br>
    * <br>
    * This function eliminates the need to have a separate "DroppedFooData" class for every object "Foo."
    * Two functions normally perform this transformation: an `implicit` `codec` used in a `genericCodec`.
    * Since actual Generics are utilized, combining the processes eliminates defining to the type data multiple times.
    * (If that is even possible here.)
    * Knowledge of the object type is still necessary to recover the original object's data through casting.
    * Not having to explicitly cast would have been the main upside of having specialized "DroppedFooData" classes.<br>
    * <br>
    * Use:<br>
    * `DroppedItemCodec.genericCodec(T.codec)`
    * @param objCodec a `Codec` that satisfies the transformation `Codec[T] -> T`
    * @param objType a `String` that explains what the object should be identified as in the log;
    *                defaults to "object"
    * @tparam T a subclass of `ConstructorData` that indicates what type the object is
    * @return `Codec[ConstructorData]`
    * @see `ConstructorData` (function)
    */
  def apply[T <: ConstructorData](objCodec: Codec[T], objType: String = "object"): Codec[ConstructorData] =
    (
      ("pos" | PlacementData.codec) ::
        ("obj" | objCodec)
    ).xmap[DroppedItemData[T]](
      {
        case pos :: obj :: HNil =>
          DroppedItemData[T](pos, obj)
      },
      {
        case DroppedItemData(pos, obj) =>
          pos :: obj :: HNil
      }
    ).exmap[ConstructorData](
      x => {
        try {
          Attempt.successful(x.asInstanceOf[ConstructorData])
        } catch {
          case ex: Exception =>
            Attempt.failure(Err(s"can not cast decode of $x to dropped $objType - $ex"))
        }
      },
      x => {
        try {
          Attempt
            .successful(x.asInstanceOf[DroppedItemData[T]]) //why does this work? shouldn't type erasure be a problem?
        } catch {
          case ex: Exception =>
            Attempt.failure(Err(s"can not cast encode $x to dropped $objType - $ex"))
        }
      }
    )
}
