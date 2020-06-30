// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of the Spitfire-based small turrets deployed using an adaptive construction engine.<br>
  * <br>
  * The turret may contain substructure defining a weapon is a turret weapon contained within the turret itself.
  * Furthermore, that turret-like weapon is loaded with turret-like ammunition.
  * In other words, this outer turret can be considered a weapons platform for the inner turret weapon.<br>
  * <br>
  * If the turret has no `health`, it is rendered as destroyed.
  * If the turret has no internal weapon, it is safest rendered as destroyed.
  * @param deploy data common to objects spawned by the (advanced) adaptive construction engine
  * @param health the amount of health the object has, as a percentage of a filled bar
  * @param internals data regarding the mounted weapon
  */
final case class SmallTurretData(
    deploy: CommonFieldDataWithPlacement,
    health: Int,
    internals: Option[InventoryData] = None
) extends ConstructorData {
  override def bitsize: Long = {
    val deploySize = deploy.bitsize
    val internalSize = internals match {
      case Some(inv) =>
        inv.bitsize
      case None =>
        0
    }
    22L + deploySize + internalSize //8u + 7u + 4u + 2u + 1u
  }
}

object SmallTurretData extends Marshallable[SmallTurretData] {

  /**
    * Overloaded constructor that mandates information about the internal weapon of the small turret.
    * @param deploy data common to objects spawned by the (advanced) adaptive construction engine
    * @param health the amount of health the object has, as a percentage of a filled bar
    * @param internals data regarding the mounted weapon
    * @return a `SmallTurretData` object
    */
  def apply(deploy: CommonFieldDataWithPlacement, health: Int, internals: InventoryData): SmallTurretData =
    new SmallTurretData(deploy, health, Some(internals))

  implicit val codec: Codec[SmallTurretData] = (
    ("deploy" | CommonFieldDataWithPlacement.codec2) ::
      ("health" | uint8L) ::
      uintL(7) ::
      uint4L ::
      uint2L ::
      optional(bool, "internals" | InventoryData.codec)
  ).exmap[SmallTurretData](
    {
      case deploy :: health :: 0 :: 0xf :: 0 :: internals :: HNil =>
        val (newHealth, newInternals) = if (health == 0 || internals.isEmpty || internals.get.contents.isEmpty) {
          (0, None)
        } else {
          (health, internals)
        }
        Attempt.successful(SmallTurretData(deploy, newHealth, newInternals))

      case data =>
        Attempt.failure(Err(s"invalid small turret data format - $data"))
    },
    {
      case SmallTurretData(deploy, health, internals) =>
        val (newHealth, newInternals) = if (health == 0 || internals.isEmpty || internals.get.contents.isEmpty) {
          (0, None)
        } else {
          (health, internals)
        }
        Attempt.successful(deploy :: newHealth :: 0 :: 0xf :: 0 :: newInternals :: HNil)
    }
  )
}
