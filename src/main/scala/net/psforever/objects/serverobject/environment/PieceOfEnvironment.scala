package net.psforever.objects.serverobject.environment

import enumeratum.{Enum, EnumEntry}
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.vital.Vitality
import net.psforever.types.Vector3

trait PieceOfEnvironment {
  def attribute: EnvironmentTrait

  def collision: EnvironmentCollision

  def testInteraction(pos: Vector3, varDepth: Float): Boolean = collision.testInteraction(pos, varDepth)

  def passThrough(pos: Vector3, previousPos: Vector3, varDepth: Float): Option[Boolean] =
    PieceOfEnvironment.passThrough(body = this, pos, previousPos, varDepth)
}

sealed abstract class EnvironmentTrait extends EnumEntry {
  def canInteractWith(obj: PlanetSideGameObject): Boolean
}

object EnvironmentAttribute extends Enum[EnvironmentTrait] {
  val values: IndexedSeq[EnvironmentTrait] = findValues

  case object Water extends EnvironmentTrait {
    def canInteractWith(obj: PlanetSideGameObject): Boolean = {
      obj.Definition.DrownAtMaxDepth || obj.Definition.DisableAtMaxDepth
    }
  }

  case object Lava extends EnvironmentTrait {
    def canInteractWith(obj: PlanetSideGameObject): Boolean = obj.isInstanceOf[Vitality]
  }

  case object Death extends EnvironmentTrait {
    def canInteractWith(obj: PlanetSideGameObject): Boolean = obj.isInstanceOf[Vitality]
  }
}

trait EnvironmentCollision {
  def altitude: Float

  def testInteraction(pos: Vector3, waterline: Float): Boolean
}

final case class DeepPlane(altitude: Float)
  extends EnvironmentCollision {
  def testInteraction(pos: Vector3, waterline: Float): Boolean = {
    pos.z + waterline < altitude
  }
}

final case class DeepSquare(altitude: Float, north: Float, east: Float, south: Float, west: Float)
  extends EnvironmentCollision {
  def testInteraction(pos: Vector3, waterline: Float): Boolean = {
    pos.z + waterline < altitude && north > pos.y && pos.y >= south && east > pos.x && pos.x >= west
  }
}

final case class DeepSurface(altitude: Float, north: Float, east: Float, south: Float, west: Float)
  extends EnvironmentCollision {
  def testInteraction(pos: Vector3, waterline: Float): Boolean = {
    pos.z < altitude && north > pos.y && pos.y >= south && east > pos.x && pos.x >= west
  }
}

final case class DeepCircularSurface(center: Vector3, radius: Float)
  extends EnvironmentCollision {
  def altitude: Float = center.z

  def testInteraction(pos: Vector3, waterline: Float): Boolean = {
    pos.z < center.z && Vector3.DistanceSquared(pos.xy, center.xy) < radius * radius
  }
}

final case class SeaLevel(attribute: EnvironmentTrait, altitude: Float)
  extends PieceOfEnvironment {
  private val planar = DeepPlane(altitude)

  def collision : EnvironmentCollision = planar
}

final case class Pool(attribute: EnvironmentTrait, collision: EnvironmentCollision)
  extends PieceOfEnvironment

object Pool {
  def apply(attribute: EnvironmentTrait, altitude: Float, north: Float, east: Float, south: Float, west: Float): Pool =
    Pool(attribute, DeepSquare(altitude, north, east, south, west))
}

object PieceOfEnvironment {
  def passThrough(body: PieceOfEnvironment, pos: Vector3, previousPos: Vector3, varDepth: Float): Option[Boolean] = {
    val isSubmerged = body.collision.testInteraction(pos, varDepth)
    val wasSubmerged = body.collision.testInteraction(previousPos, varDepth)
    if (isSubmerged != wasSubmerged) {
      Some(isSubmerged)
    } else {
      None
    }
  }
}
