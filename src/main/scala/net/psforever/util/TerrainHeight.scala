package net.psforever.util

import io.circe._
import io.circe.parser._
import net.psforever.objects.zones.MapInfo

import java.io.FileNotFoundException
import java.util.Base64
import scala.io.Source

/**
  * One continent's ground-height grid, exactly as extracted from its terrain mesh by the Raxicore
  * editor's ContinentExport tool -- the same rasterization pass that produces the coastline the
  * portal draws, kept at the byte-quantized resolution it ships at.
  *
  * @param n         cells per axis; `worldSize / n` is one cell's width
  * @param elevation row-major (`j * n + i`, `i` +x, `j` +y) byte grid, one byte per cell, normalised
  *                  0..255 across `[elevMin, elevMax]`
  */
final case class TerrainHeightGrid(
    worldSize: Int,
    n: Int,
    elevMin: Float,
    elevMax: Float,
    elevation: Array[Byte]
) {

  /**
    * Ground height at a world position, by nearest cell (no interpolation -- this is for placing a
    * visual effect, not a physics query, and the grid is already coarse enough that a cell boundary
    * is not something a player could ever perceive).
    *
    * Off-map coordinates are clamped into the grid rather than rejected: an entity standing right at
    * the continent's edge is still a place worth answering for.
    */
  def heightAt(x: Float, y: Float): Float = {
    val cell = worldSize.toFloat / n
    val i    = math.min(n - 1, math.max(0, (x / cell).toInt))
    val j    = math.min(n - 1, math.max(0, (y / cell).toInt))
    val b    = elevation(j * n + i) & 0xff
    elevMin + (b / 255f) * (elevMax - elevMin)
  }
}

/**
  * Ground height, sourced from the world server's OWN copy of the terrain -- never from a caller.
  *
  * This exists because an admin action that places a visual effect at a world position (an orbital
  * strike's beam, say) needs a Z coordinate to draw at, and the only Z a remote caller could offer is
  * an unverified claim: the server has no way to check it against anything, so a wrong or malicious
  * value would place the effect underground, in the sky, or anywhere else with no way to notice. The
  * fix is to never ask a caller for it. The world server already has the real terrain -- the same
  * mesh the reference client renders -- so it can answer the question itself.
  *
  * The grids are the Raxicore editor's own extraction (`ContinentExport --terrain-out`), shipped as a
  * resource per continent under `terrain/<base>.json`, keyed by [[MapInfo]]'s base name and resolved
  * from a live `Zone.id` (e.g. `"z3"`) through [[PointOfInterest]]'s existing zoneId -> base mapping,
  * so nothing here invents a second naming scheme.
  */
object TerrainHeight {
  private implicit val decodeGrid: Decoder[TerrainHeightGrid] = Decoder.forProduct5(
    "worldSize",
    "n",
    "elevMin",
    "elevMax",
    "elevation"
  ) { (worldSize: Int, n: Int, elevMin: Float, elevMax: Float, elevationB64: String) =>
    TerrainHeightGrid(worldSize, n, elevMin, elevMax, Base64.getDecoder.decode(elevationB64))
  }

  /** Every continent that shipped a height grid, keyed by its `MapInfo` base name (e.g. `"map03"`). */
  private lazy val grids: Map[String, TerrainHeightGrid] =
    MapInfo.values.flatMap { info =>
      try {
        val res  = Source.fromResource(s"terrain/${info.value}.json")
        val json = res.mkString
        res.close()
        decode[TerrainHeightGrid](json).toOption.map(info.value -> _)
      } catch {
        case _: FileNotFoundException => None
      }
    }.toMap

  /**
    * Ground height in `zoneId` at `(x, y)`, or `None` if that zone has no height grid (VR
    * training zones and the two Black Ops / faction stations, none of which are the kind of zone an
    * orbital strike is meaningful in anyway).
    */
  def sample(zoneId: String, x: Float, y: Float): Option[Float] =
    PointOfInterest.get(zoneId).flatMap(poi => grids.get(poi.map)).map(_.heightAt(x, y))
}
