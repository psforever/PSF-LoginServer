// Copyright (c) 2017 PSForever
package services.local

import net.psforever.packet.game.{PlanetSideGUID, TriggeredSound}
import net.psforever.types.Vector3
import services.GenericEventBusMsg

final case class LocalServiceResponse(toChannel : String,
                                      avatar_guid : PlanetSideGUID,
                                      replyMessage : LocalServiceResponse.Response
                                     ) extends GenericEventBusMsg

object LocalServiceResponse {
  trait Response

  final case class DoorOpens(door_guid : PlanetSideGUID) extends Response
  final case class DoorCloses(door_guid : PlanetSideGUID) extends Response
  final case class HackClear(target_guid : PlanetSideGUID, unk1 : Long, unk2 : Long) extends Response
  final case class HackObject(target_guid : PlanetSideGUID, unk1 : Long, unk2 : Long) extends Response
  final case class TriggerSound(sound : TriggeredSound.Value, pos : Vector3, unk : Int, volume : Float) extends Response
}
