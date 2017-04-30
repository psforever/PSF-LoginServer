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
final case class DroppedItemData[T <: ConstructorData](pos : PlacementData, obj : T) extends ConstructorData {
  override def bitsize : Long = pos.bitsize + obj.bitsize
}

object DroppedItemData {
  /**
    * Transform `DroppedItemData[T]` for object type `T` into `ConstructorData.genericPattern`.<br>
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
    * @return `ConstructorData.genericPattern`
    * @see `ConstructorData.genericPattern` (function)
    */
  def genericCodec[T <: ConstructorData](objCodec : Codec[T], objType : String = "object") : Codec[ConstructorData.genericPattern] = (
    ("pos" | PlacementData.codec) ::
      ("obj" | objCodec)
    ).xmap[DroppedItemData[T]] (
    {
      case pos :: obj :: HNil =>
        DroppedItemData[T](pos, obj)
    },
    {
      case DroppedItemData(pos, obj) =>
        pos :: obj :: HNil
    }
  ).exmap[ConstructorData.genericPattern] (
    {
      case x =>
        Attempt.successful(Some(x.asInstanceOf[ConstructorData]))
    },
    {
      case Some(x) =>
        Attempt.successful(x.asInstanceOf[DroppedItemData[T]])
      case _ =>
        Attempt.failure(Err(s"can not encode dropped $objType data"))
    }
  )
}
