// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import scodec.{Attempt, Codec, Err}

/**
  * The base type for the representation of any data used to produce objects from `ObjectCreateMessage` packet data.
  * There is no reason to instantiate this class as-is.
  * Children of this class are expected to be able to translate through `scodec` operations into packet data.<br>
  * <br>
  * The object data is uncoupled from the object class as multiple classes use the same format for their data.
  * For example, both the Suppressor and the Gauss will use a "weapon data" format.
  * For example, both 9mm bullets and energy cells will use an "ammunition data" format.
  */
abstract class ConstructorData extends StreamBitSize

object ConstructorData {
  /**
    * This pattern is intended to provide common conversion between all of the `Codec`s of the children of this class.
    * The casting will be performed through use of `exmap` in the child class.
    */
  type genericPattern = Option[ConstructorData]

  /**
    * Transform a `Codec[T]` for object type `T` into `ConstructorData.genericPattern`.
    * @param objCodec a `Codec` that satisfies the transformation `Codec[T] -> T`
    * @param objType a `String` that explains what the object should be identified as in the `Err` message;
    *                defaults to "object"
    * @tparam T a subclass of `ConstructorData` that indicates what type the object is
    * @return `ConstructorData.genericPattern`
    */
  def genericCodec[T <: ConstructorData](objCodec : Codec[T], objType : String = "object") : Codec[ConstructorData.genericPattern] =
    objCodec.exmap[ConstructorData.genericPattern] (
    {
      case x =>
        Attempt.successful(Some(x.asInstanceOf[ConstructorData]))
    },
    {
      case Some(x) =>
        Attempt.successful(x.asInstanceOf[T]) //why does this work? shouldn't type erasure be a problem?
      case _ =>
        Attempt.failure(Err(s"can not encode as $objType data"))
    }
  )
}
