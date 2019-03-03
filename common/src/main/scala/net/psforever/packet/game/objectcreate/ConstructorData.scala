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
    * Transform a `Codec[T]` for object type `T` into `ConstructorData`.
    * @param objCodec a `Codec` that satisfies the transformation `Codec[T] -> T`
    * @param objType a `String` that explains what the object should be identified as in the `Err` message;
    *                defaults to "object"
    * @tparam T a subclass of `ConstructorData` that indicates what type the object is
    * @return `Codec[ConstructorData]`
    */
  def apply[T <: ConstructorData](objCodec : Codec[T], objType : String = "object") : Codec[ConstructorData] =
    objCodec.exmap[ConstructorData] (
      x => {
        try {
          Attempt.successful(x.asInstanceOf[ConstructorData])
        }
        catch {
          case ex : Exception =>
            Attempt.failure(Err(s"can not cast decode of $x to $objType - $ex"))
        }
      },
      x => {
        try {
          Attempt.successful(x.asInstanceOf[T]) //why does this work? shouldn't type erasure be a problem?
        }
        catch {
          case ex : Exception =>
            Attempt.failure(Err(s"can not cast encode $x to $objType - $ex"))
        }
      }
    )
}
