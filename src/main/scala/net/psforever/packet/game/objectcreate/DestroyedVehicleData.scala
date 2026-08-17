// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.codecs._
import scodec.Codec

/**
  * A representation of the charred husk of a destroyed vehicle.<br>
  * <br>
  * This is a hand-crafted `Codec` and was not based on something found on Gemini Live.
  * @param pos where and how the object is oriented;
  *            `pos.vel` existing is fine
  */
final case class DestroyedVehicleData(pos: PlacementData) extends ConstructorData {
  override def bitsize: Long = pos.bitsize
}

object DestroyedVehicleData extends Marshallable[DestroyedVehicleData] {
  implicit val codec: Codec[DestroyedVehicleData] = ("pos" | PlacementData.codec).as[DestroyedVehicleData]
}
