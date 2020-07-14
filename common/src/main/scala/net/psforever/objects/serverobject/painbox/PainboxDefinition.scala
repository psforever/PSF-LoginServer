package net.psforever.objects.serverobject.painbox

import net.psforever.objects.serverobject.structures.AmenityDefinition
import net.psforever.types.Vector3

class PainboxDefinition(objectId: Int) extends AmenityDefinition(objectId) {
  private var alwaysOn: Boolean        = true
  private var radius: Float            = 0f
  private var damage: Int              = 5
  private var sphereOffset             = Vector3(0f, 0f, -0.4f)
  private var hasNearestDoorDependency = false

  objectId match {
    case 622 =>
      Name = "painbox"
      alwaysOn = false
      damage = 0
    case 623 =>
      Name = "painbox_continuous"
    case 624 =>
      Name = "painbox_door_radius"
      alwaysOn = false
      radius = 10f * 0.6928f
      hasNearestDoorDependency = true
      damage = 0
    case 625 =>
      Name = "painbox_door_radius_continuous"
      radius = 10f * 0.6928f
      hasNearestDoorDependency = true
    case 626 =>
      Name = "painbox_radius"
      alwaysOn = false
      radius = 10f * 0.6928f
      damage = 0
    case 627 =>
      Name = "painbox_radius_continuous"
      radius = 8.55f
      sphereOffset = Vector3.Zero
    case _ =>
      throw new IllegalArgumentException(s"$objectId is not a valid painbox object id")
  }

  def Radius: Float                     = radius
  def AlwaysOn: Boolean                 = alwaysOn
  def Damage: Int                       = damage
  def SphereOffset: Vector3             = sphereOffset
  def HasNearestDoorDependency: Boolean = hasNearestDoorDependency
}
