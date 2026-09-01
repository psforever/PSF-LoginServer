// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.types.{DriveState, PlanetSideEmpire, VehicleFormat}
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of the high altitude rapid transport (HART) shuttle that ferries the player into battle.
  * This `Codec` is different depending on whether the shuttle is the child of a parent or independent.<br>
  * <br>
  * Three HART shuttles dock with the three HART buildings in the three sanctuaries for short periods on a timed schedule.
  * When one is landed, players may board the shuttle using designated hallways in the lobbies of the HART building.
  * After the shuttle leaves the sanctuary, it transports those players up into orbit above the continents.
  * The shuttle docks again, this time with space stations that orbit the planet.
  * It allows infantry to use droppods to land on the continents by pinpointing locations on that continent's tactical map.<br>
  * <br>
  * The previous explanation is smoke and mirrors nonsense.
  * Lore-wise, the separation of Auraxis during the Bending rendered it impossible for the shuttle to visit all of the continents.
  * The orbital stations - even if they multiplied one per planet - don't really exist.
  * (They almost existed but all assets for them were cut from the game.)
  * The HART shuttle also isn't a traditional vehicle.
  * It isn't even tangible.
  * The game just treats it like a vehicle for the purpose of allowing players to access the controllable droppod system.<br>
  * <br>
  * When accessible to the player, the shuttle has an access point called a "trunk."
  * Trying to access it yields the brief message "OSMustBeDockedToMount."
  * @param faction empire the object is affiliated with
  * @param pos optional;
  *            where and how the object is oriented
  * @see `DroppodLaunchRequestMessage`
  * @see `DroppodLaunchResponseMessage`
  * @see `OrbitalShuttleTimeMsg`
  */
final case class OrbitalShuttleData(faction: PlanetSideEmpire.Value, pos: Option[PlacementData] = None)
    extends ConstructorData {
  override def bitsize: Long = {
    //common field data (normal) = 23
    //vehicle pattern data = 23
    pos match {
      case Some(position) =>
        //vehicle variant = 8
        54L + position.bitsize
      case None =>
        46L
    }
  }
}

object OrbitalShuttleData extends Marshallable[OrbitalShuttleData] {
  /**
    * Overloaded constructor that requires defining a position.
    * The fields are arranged in the standard order for most vehicles (position data first).
    * @param pos where and how the object is oriented
    * @param faction empire the object is affiliated with
    * @return an `OrbitalShuttleData` object
    */
  def apply(pos: PlacementData, faction: PlanetSideEmpire.Value): OrbitalShuttleData =
    OrbitalShuttleData(faction, Some(pos))

  private val base: VehiclePatternData = VehiclePatternData(
    boostMaxHealth = false,
    health = 31,
    unk4 = true,
    no_mount_points = true,
    DriveState.OrbitalShuttleDocked,
    unk5 = true,
    unk6 = true,
    cloak = false,
    None,
    None
  )(VehicleFormat.Normal)

  private val base_variant: VehiclePatternData = VehiclePatternData(
    boostMaxHealth = false,
    health = 255,
    unk4 = false,
    no_mount_points = false,
    DriveState.State127,
    unk5 = true,
    unk6 = false,
    cloak = false,
    Some(VariantVehicleData(15)),
    None
  )(VehicleFormat.Variant)

  private val base_vehicle_codec: Codec[VehiclePatternData] = VehiclePatternData.codec(velocity_data = false, VehicleFormat.Normal)

  implicit val codec: Codec[OrbitalShuttleData] = (
    CommonFieldData.codec ::
      base_vehicle_codec
    ).exmap[OrbitalShuttleData](
    {
      case data :: _ :: HNil =>
        Attempt.successful(OrbitalShuttleData(data.faction))

      case data =>
        Attempt.failure(Err(s"invalid shuttle data format - $data"))
    },
    {
      case OrbitalShuttleData(faction, _) =>
        Attempt.successful(CommonFieldData(faction) :: base :: HNil)
    }
  )

  /**
   * Used when the shuttle is not attached to something else.
   */
  val codec_pos: Codec[OrbitalShuttleData] = (
    CommonFieldDataWithPlacement.codec >>:~ { data =>
      VehiclePatternData.codec(data.pos.vel.isDefined, VehicleFormat.Variant).hlist
    }
    ).exmap[OrbitalShuttleData](
    {
      case cdata :: _:: HNil =>
        Attempt.successful(OrbitalShuttleData(cdata.data.faction, Some(cdata.pos)))
    },
    {
      case OrbitalShuttleData(faction, Some(pos)) =>
        Attempt.successful(CommonFieldDataWithPlacement(pos, CommonFieldData(faction)) :: base_variant :: HNil)
    }
  )
}
