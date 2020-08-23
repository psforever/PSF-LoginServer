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
  * Either of the first two parameters - `lock_accelerator` or `lock_wheel` - constitutes any vehicle being overrode.
  * Either of the latter two parameters - `lock_thrust` or `lock_strafe` - constitutes a flight vehicle being overrode.
  * No message is displayed if the vehicle is placed under any form of server control.
  * During server control, this is an acceleration value (?);
  * during cancellable auto-drive, a constant velocity value.
  * Vertical thrust control for aircraft is either on or off;
  * the amount of that thrust can not be controlled.<br>
  * <br>
  * After being controlled, when the vehicle is no longer under control,
  * it will transition into a state of constant speed auto-drive.
  * The message regarding the vehicle being back in the driver's control will display,
  * unless one of the aforementioned `lock_*` parameters is still set to `true`.
  * When dismounting a bailable vehicle while it is under the server's control,
  * the player will behave like they are bailing from it.
  * (The vehicle actually has to be "bailable" first, of course.)<br>
  * <br>
  * "Something like speed:"<br>
  * For ground vehicles, for `n`, the calculated in-game speed for the value in this packet will be at least `3.45 x n`.
  * For flight vehicles, for `n`, the forward air speed for the value in this packet will be at least `1.18 * n`.
  * This approximation is not always going to be accurate but serves as a good rule of thumb.
  * @param lock_accelerator driver has no control over vehicle acceleration
  * @param lock_wheel driver has no control over vehicle turning
  * @param reverse move in reverse
  *                0 = forward
  *                1 = reverse
  * @param unk4 na;
  *             something to do with vehicle bailable speed
  * @param lock_vthrust pilot has no control over vertical thrust;
  *                     asserts a constant positive vertical thrust;
  *                     the only valid setting appears to be 1
  * @param lock_strafe pilot has no control over strafing thrust;
  *                    0 = not locked
  *                    1 = no strafing
  *                    2 = strafe left automatically
  *                    3 = strafe right automatically
  * @param movement_speed "something like speed"
  * @param unk8 na;
  *             set `lock_wheel` to `true` to expose this value
  */
final case class ServerVehicleOverrideMsg(
    lock_accelerator: Boolean,
    lock_wheel: Boolean,
    reverse: Boolean,
    unk4: Boolean,
    lock_vthrust: Int,
    lock_strafe: Int,
    movement_speed: Int,
    unk8: Option[Long]
) extends PlanetSideGamePacket {
  type Packet = ServerVehicleOverrideMsg
  def opcode = GamePacketOpcode.ServerVehicleOverrideMsg
  def encode = ServerVehicleOverrideMsg.encode(this)
}

object ServerVehicleOverrideMsg extends Marshallable[ServerVehicleOverrideMsg] {
  implicit val codec: Codec[ServerVehicleOverrideMsg] = (
    ("lock_accelerator" | bool) ::
      (("lock_wheel" | bool) >>:~ { test =>
      ("reverse" | bool) ::
        ("unk4" | bool) ::
        ("lock_vthrust" | uint2L) ::
        ("lock_strafe" | uint2L) ::
        ("movement_speed" | uintL(9)) ::
        conditional(test, "unk8" | uint32L)
    })
  ).as[ServerVehicleOverrideMsg]
}
