package net.psforever.objects.zones

import net.psforever.types.Vector3

trait FillLine {
  def altitude: Float

  def submerged(pos: Vector3, headHeight: Float): Boolean

  def breakSurface(pos: Vector3, previousPos: Vector3, headHeight: Float): Option[Boolean] =
    FillLine.breakSurface(fluid = this, pos, previousPos, headHeight)
}

final case class Sealevel(altitude: Float)
  extends FillLine {
  def submerged(pos: Vector3, headHeight: Float): Boolean = {
    pos.z + headHeight <= altitude
  }
}

final case class FloorIsLava(altitude: Float)
  extends FillLine {
  def submerged(pos: Vector3, headHeight: Float): Boolean = {
    pos.z <= altitude
  }
}

final case class Pool(altitude: Float, north: Float, east: Float, south: Float, west: Float)
  extends FillLine {
  assert(north > south, s"latitudinal coordinates should be north > south, but n=$north, s=$south")
  assert(east > west, s"longitudinal coordinates should be east > west, but e=$east, w=$west")

  def submerged(pos: Vector3, headHeight: Float): Boolean = {
    pos.z + headHeight <= altitude && north > pos.y && pos.y > south && east > pos.x && pos.x > west
  }
}

final case class LavaPool(altitude: Float, north: Float, east: Float, south: Float, west: Float)
  extends FillLine {
  assert(north > south, s"latitudinal coordinates should be north > south, but n=$north, s=$south")
  assert(east > west, s"longitudinal coordinates should be east > west, but e=$east, w=$west")

  def submerged(pos: Vector3, headHeight: Float): Boolean = {
    pos.z + headHeight <= altitude && north > pos.y && pos.y > south && east > pos.x && pos.x > west
  }
}

final case class RoundLavaPool(origin: Vector3, radius: Float)
  extends FillLine {
  def altitude: Float = origin.z

  def submerged(pos: Vector3, headHeight: Float): Boolean = {
    pos.z + headHeight <= origin.z && Vector3.DistanceSquared(pos.xy, origin.xy) < radius * radius
  }
}

object FillLine {
  final val Sealevel0 = Sealevel(0)

  final val Sealevel35 = Sealevel(35)

  def breakSurface(fluid: FillLine, pos: Vector3, previousPos: Vector3, headHeight: Float): Option[Boolean] = {
    if (fluid.submerged(pos, headHeight) && !fluid.submerged(previousPos, headHeight)) {
      Some(true)
    } else if (!fluid.submerged(pos, headHeight) && fluid.submerged(previousPos, headHeight)) {
      Some(false)
    }
    else {
      None
    }
  }
}