// Copyright (c) 2018 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of the construction item portion of `ObjectCreateDetailedMessage` packet data.
  * This creates what is known as a construction tool item (or, `ConstructionItem`)
  * which is a game world object that is manipulated by the player
  * to construct other game world objects which are known as combat engineering deployables (or, just `Deployable`s).
  * None of the information about the `Deployable` objects are maintained here and
  * are instead implicit to the type of `ConstructionItem`.
  * That aspect of the entity is adjusted through fire modes and ammunition types
  * much like conventional weaponry (`Tool`s), though the initial fire mode can be indicated.
  * @see `ConstructionItem`
  * @see `Deployable`
  * @see `FireModeSwitch`
  */
final case class DetailedConstructionToolData(data: CommonFieldData, mode: Int) extends ConstructorData {
  override def bitsize: Long = 28L + data.bitsize
}

object DetailedConstructionToolData extends Marshallable[DetailedConstructionToolData] {
  def apply(data: CommonFieldData): DetailedConstructionToolData = DetailedConstructionToolData(data, 0)

  implicit val codec: Codec[DetailedConstructionToolData] = (
    ("data" | CommonFieldData.codec(false)) ::
      uint8 :: //n > 1 produces a stack of construction items (tends to crash the client)
      ("mode" | uint16) ::
      uint2 ::
      uint2
  ).exmap[DetailedConstructionToolData](
    {
      case data :: 1 :: mode :: 1 :: _ :: HNil =>
        Attempt.successful(DetailedConstructionToolData(data, mode))
      case data =>
        Attempt.failure(Err(s"invalid detailed construction tool data format - $data"))
    },
    {
      case DetailedConstructionToolData(data, mode) =>
        Attempt.successful(data :: 1 :: mode :: 1 :: 0 :: HNil)
    }
  )
}
