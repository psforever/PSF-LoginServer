// Copyright (c) 2024 PSForever
package net.psforever.objects.geometry

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.geometry.d2.Rectangle
import net.psforever.objects.geometry.d3.VolumetricGeometry
import net.psforever.objects.serverobject.environment.EnvironmentCollision
import net.psforever.objects.zones.Zone
import net.psforever.types.Vector3

final case class VolumetricEnvironmentCollision(g: VolumetricGeometry)
  extends EnvironmentCollision {
  private lazy val bound: Rectangle = {
    Rectangle(
      g.pointOnOutside(Vector3(0, 1,0)).y,
      g.pointOnOutside(Vector3(-1,0,0)).x,
      g.pointOnOutside(Vector3(0,-1,0)).y,
      g.pointOnOutside(Vector3( 1,0,0)).x
    )
  }

  def Geometry: VolumetricGeometry = g

  def altitude: Float = g.pointOnOutside(Vector3(0,0,1)).z

  def testInteraction(obj: PlanetSideGameObject, varDepth: Float): Boolean = {
    Zone.distanceCheck(obj.Definition.Geometry(obj), g) <= varDepth
  }

  def bounding: Rectangle = bound
}
