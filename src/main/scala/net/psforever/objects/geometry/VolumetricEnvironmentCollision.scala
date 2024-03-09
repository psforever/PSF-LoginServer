// Copyright (c) 2024 PSForever
package net.psforever.objects.geometry

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.geometry.d2.Rectangle
import net.psforever.objects.geometry.d3.VolumetricGeometry
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentCollision, EnvironmentTrait, PieceOfEnvironment}
import net.psforever.objects.zones.Zone
import net.psforever.types.Vector3

case class VolumetricEnvironmentCollision(door: Door)
  extends EnvironmentCollision {
  private lazy val geometry = door.Definition.Geometry.apply(door)
  private lazy val bound: Rectangle = {
    val g = geometry
    Rectangle(
      g.pointOnOutside(Vector3(0, 1,0)).y,
      g.pointOnOutside(Vector3(-1,0,0)).x,
      g.pointOnOutside(Vector3(0,-1,0)).y,
      g.pointOnOutside(Vector3( 1,0,0)).x
    )
  }

  def Geometry: VolumetricGeometry = geometry

  def altitude: Float = geometry.pointOnOutside(Vector3(0,0,1)).z

  def testInteraction(obj: PlanetSideGameObject, varDepth: Float): Boolean = {
    Zone.distanceCheck(obj.Definition.Geometry(obj), geometry) <= varDepth
  }

  def bounding: Rectangle = bound
}
