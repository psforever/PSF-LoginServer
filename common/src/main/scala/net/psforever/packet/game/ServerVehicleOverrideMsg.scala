// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by server to assert control of a player's vehicle, usually temporarily, and to relinquish that control.<br>
  * <br>
  * The "vehicle" counts as any mobile platform where the user's character is currently sitting.
  * If the player is not sitting in what the game considers a "vehicle," the packet is wasted.
  * Either of the first two parameters - `lock_accelerator` or `lock_wheel` - constitutes the vehicle being overrode.
  * No message is displayed if the vehicle is placed under server control.
  * The vehicle will operate as if accelerating.<br>
  * <br>
  * After being controlled, when the vehicle is no longer under control,
  * it will transition into a state of constant speed auto-drive.
  * The message regarding the vehicle being back in the driver's control will display,
  * unless one of the aforementioned `lock_*` parameters is still set to `true`.
  * When dismounting a bailable vehicle while it is under the server's control,
  * the player will behave like they are bailing from it.
  * (The vehicle actually has to be "bailable" first, of course.)<br>
  * <br>
  * Speed samples follow (from AMS):<br>
  * 1 -> 3<br>
  * 2 -> 7<br>
  * 3 -> 10<br>
  * 10 -> 35<br>
  * 15 -> 52<br>
  * 20 -> 68
  * @param lock_accelerator driver has no control over whether vehicle accelerates
  * @param lock_wheel driver has no control over whether the vehicle turns
  * @param reverse drive in reverse
  * @param unk4 na
  * @param unk5 na
  * @param unk6 na
  * @param speed "something like speed;"
  *              for `n`, the pattern to calculate a constant in-game speed is `floor(3.5 x n)`;
  *              during server control, an acceleration value (?);
  *              during auto-drive, a velocity value
  * @param unk8 na;
  *             set `lock_wheel` to `true` to expose value
  */
final case class ServerVehicleOverrideMsg(lock_accelerator : Boolean,
                                          lock_wheel : Boolean,
                                          reverse : Boolean,
                                          unk4 : Boolean,
                                          unk5 : Int,
                                          unk6 : Int,
                                          speed : Int,
                                          unk8 : Option[Long]
                                         ) extends PlanetSideGamePacket {
  type Packet = ServerVehicleOverrideMsg
  def opcode = GamePacketOpcode.ServerVehicleOverrideMsg
  def encode = ServerVehicleOverrideMsg.encode(this)
}

object ServerVehicleOverrideMsg extends Marshallable[ServerVehicleOverrideMsg] {
  /**
    * Common assert control packet format.
    * @param speed "something like speed"
    * @return a `ServerVehicleOverrideMsg` packet
    */
  def On(speed : Int) : ServerVehicleOverrideMsg = {
    ServerVehicleOverrideMsg(true, true, false, false, 0, 0, speed, Some(0))
  }

  /**
    * Common relinquish control packet format.
    * @param speed "something like speed"
    * @return a `ServerVehicleOverrideMsg` packet
    */
  def Off(speed : Int) : ServerVehicleOverrideMsg = {
    ServerVehicleOverrideMsg(false, false, false, true, 0, 0, speed, None)
  }

  implicit val codec: Codec[ServerVehicleOverrideMsg] = (
    ("lock_accelerator" | bool) ::
      (("lock_wheel" | bool) >>:~ { test =>
        ("reverse" | bool) ::
          ("unk4" | bool) ::
          ("unk5" | uint2L) ::
          ("unk6" | uint2L) ::
          ("speed" | uintL(9)) ::
          conditional(test, "unk8" | uint32L)
      })
    ).as[ServerVehicleOverrideMsg]
}
