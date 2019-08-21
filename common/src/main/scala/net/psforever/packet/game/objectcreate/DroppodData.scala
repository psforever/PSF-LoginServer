// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of a droppod that is dropped from the HART shuttle and ferries the player into battle.
  * Droppods are also used when a player has activated Instant Action.<br>
  * <br>
  * When the server first spawns the droppod, it will be placed at the world ceiling - 1024.0f.
  * It is placed under control via another packet that sends it hurtling to the ground.
  * Upon hitting the ground, it opens up, releasing the player, and despawns.<br>
  * <br>
  * Although the droppod is not technically a vehicle, it is treated as such by the game.
  * A spawned and unoccupied droppod can be entered and exited, as expected (the seat is 0).
  * There is no entry animation.
  * The exit animation is the droppod flowering open as usual.
  * Even in its spread open state, the droppod can be re-entered, though it will remain spread open.
  * The player's character will disappear once "inside."
  * Upon exiting again, the droppod will snap shut and spread open.<br>
  * <br>
  * Exploration:
  * When `basic.player_guid` is defined, the droppod will not be at the world ceiling anymore and its boosters will be activate.
  * Does this `basic.player_guid` actually represent the player who is in the pod?
  * @param basic data common to objects
  * @param health the amount of health the object has, as a percentage of a filled bar
  * @param burn whether the boosters are ignited
  * @see `DroppodLaunchRequestMessage`
  * @see `DroppodLaunchResponseMessage`
  */
final case class DroppodData(basic : CommonFieldDataWithPlacement,
                             health : Int,
                             burn : Boolean,
                             unk : Boolean
                            ) extends ConstructorData {
  override def bitsize : Long = {
    val basicSize = basic.bitsize
    29L + basicSize
  }
}

object DroppodData extends Marshallable[DroppodData] {
  def apply(basic : CommonFieldDataWithPlacement) : DroppodData = DroppodData(basic, 255, burn = false, unk = false)

  implicit val  codec : Codec[DroppodData] = (
    ("basic" | CommonFieldDataWithPlacement.codec) ::
      bool ::
      ("health" | uint8L) :: //health
      uintL(5) :: //0x0
      uint4L :: //0xF
      uintL(6) :: //0x0
      ("boosters" | uint4L) :: //0x9 on standby, 0x0 when burning and occupied (basic.player_guid?)
      ("unk" | bool)
    ).exmap[DroppodData] (
    {
      case basic :: false :: health :: 0 :: 0xF :: 0 :: boosters :: unk :: HNil =>
        val burn : Boolean = boosters == 0
        Attempt.successful(DroppodData(basic, health, burn, unk))

      case data =>
        Attempt.failure(Err(s"invalid droppod data format - $data"))
    },
    {
      case DroppodData(basic, health, burn, unk) =>
        val boosters : Int = if(burn) { 0 } else { 9 }
        Attempt.successful(basic :: false :: health :: 0 :: 0xF :: 0 :: boosters :: unk :: HNil)
    }
  )
}
