// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.types.PlanetSideGUID
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of the player-mountable large field turrets deployed using an advanced adaptive construction engine.<br>
  * <br>
  * Field turrets are divided into the turret base, the mounted turret weapon, and the turret's ammunition.
  * The ammunition is always the same regardless of which faction owns the turret.
  * Turret bases and turret weapons are generally paired by the faction.<br>
  * <br>
  * If the turret has no `health`, it is rendered as destroyed.
  * If the turret has no internal weapon, it is safest rendered as destroyed.
  * Trying to fire a turret with no internal weapon will soft-lock the PlanetSide client.
  * @param deploy data common to objects spawned by the (advanced) adaptive construction engine
  * @param health the amount of health the object has, as a percentage of a filled bar
  * @param internals data regarding the mountable weapon
  */
final case class OneMannedFieldTurretData(
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
    37L + deploySize + internalSize //16u + 1u + 8u + 5u + 4u + 2u + 1u
  }
}

object OneMannedFieldTurretData extends Marshallable[OneMannedFieldTurretData] {
  /**
    * Overloaded constructor that mandates information about the internal weapon of the field turret.
    * @param deploy data common to objects spawned by the (advanced) adaptive construction engine
    * @param health the amount of health the object has, as a percentage of a filled bar
    * @param internals data regarding the mountable weapon
    * @return a `OneMannedFieldTurretData` object
    */
  def apply(deploy: CommonFieldDataWithPlacement, health: Int, internals: InventoryData): OneMannedFieldTurretData =
    new OneMannedFieldTurretData(deploy, health, Some(internals))

  private def structureIfAlive(inHealth: Int, inInternals: Option[InventoryData]): (Int, Option[InventoryData]) = {
    if (inHealth > 0 && inInternals.exists(_.contents.nonEmpty)) {
      (inHealth, inInternals)
    } else {
      (0, None)
    }
  }

  implicit val codec: Codec[OneMannedFieldTurretData] = (
    ("deploy" | CommonFieldDataWithPlacement.codec_extra) ::
      PlanetSideGUID.codec :: //hoist/extract with the deploy.owner_guid in field above
      bool ::
      ("health" | uint8L) ::
      uint(bits = 5) ::
      uint4 ::
      uint2 ::
      ("internals" | optional(bool, InventoryData.codec))
  ).exmap[OneMannedFieldTurretData](
    {
      case deploy :: player :: false :: health :: 0 :: 0xf :: 0 :: internals :: HNil =>
        val (newHealth, newInternals) = structureIfAlive(health, internals)
        Attempt.successful(
          OneMannedFieldTurretData(
            CommonFieldDataWithPlacement(
              deploy.pos,
              deploy.data.copy(guid = player)
            ),
            newHealth,
            newInternals
          )
        )

      case data =>
        Attempt.failure(Err(s"invalid field turret data format - $data"))
    },
    {
      case OneMannedFieldTurretData(CommonFieldDataWithPlacement(pos, data), health, internals) =>
        val (newHealth, newInternals) = structureIfAlive(health, internals)
        Attempt.successful(
          CommonFieldDataWithPlacement(
            pos,
            data.copy(guid = PlanetSideGUID(0))
          ) :: data.guid :: false :: newHealth :: 0 :: 0xf :: 0 :: newInternals :: HNil
        )
    }
  )
}
