package net.psforever.objects.zones

import enumeratum.{Enum, EnumEntry}
import net.psforever.types.Vector3

trait FillLine {
  def attribute: FillLineTrait

  def collision: FillLineCollision

  def submerged(pos: Vector3, waterline: Float): Boolean = collision.submerged(pos, waterline)

  def breakSurface(pos: Vector3, previousPos: Vector3, waterline: Float): Option[Boolean] =
    FillLine.breakSurface(fluid = this, pos, previousPos, waterline)
}

sealed abstract class FillLineTrait extends EnumEntry {}

object FilledWith extends Enum[FillLineTrait] {
  val values: IndexedSeq[FillLineTrait] = findValues

  case object Water extends FillLineTrait

  case object Lava extends FillLineTrait

  case object Death extends FillLineTrait
}

trait FillLineCollision {
  def altitude: Float

  def submerged(pos: Vector3, waterline: Float): Boolean
}

final case class DeepPlane(altitude: Float)
  extends FillLineCollision {
  def submerged(pos: Vector3, waterline: Float): Boolean = {
    pos.z + waterline < altitude
  }
}

final case class DeepSquare(altitude: Float, north: Float, east: Float, south: Float, west: Float)
  extends FillLineCollision {
  def submerged(pos: Vector3, waterline: Float): Boolean = {
    pos.z + waterline < altitude && north > pos.y && pos.y >= south && east > pos.x && pos.x >= west
  }
}

final case class DeepSurface(altitude: Float, north: Float, east: Float, south: Float, west: Float)
  extends FillLineCollision {
  def submerged(pos: Vector3, waterline: Float): Boolean = {
    pos.z < altitude && north > pos.y && pos.y >= south && east > pos.x && pos.x >= west
  }
}

final case class DeepCircularSurface(center: Vector3, radius: Float)
  extends FillLineCollision {
  def altitude: Float = center.z

  def submerged(pos: Vector3, waterline: Float): Boolean = {
    pos.z < center.z && Vector3.DistanceSquared(pos.xy, center.xy) < radius * radius
  }
}

final case class SeaLevel(attribute: FillLineTrait, altitude: Float)
  extends FillLine {
  private val planar = DeepPlane(altitude)

  def collision : FillLineCollision = planar
}

final case class Pool(attribute: FillLineTrait, collision: FillLineCollision)
  extends FillLine

object Pool {
  def apply(attribute: FillLineTrait, altitude: Float, north: Float, east: Float, south: Float, west: Float): Pool =
    Pool(attribute, DeepSquare(altitude, north, east, south, west))
}

object FillLine {
  def breakSurface(fluid: FillLine, pos: Vector3, previousPos: Vector3, waterline: Float): Option[Boolean] = {
    val isSubmerged = fluid.collision.submerged(pos, waterline)
    val wasSubmerged = fluid.collision.submerged(previousPos, waterline)
    if (isSubmerged != wasSubmerged) {
      Some(isSubmerged)
    } else {
      None
    }
  }
}
