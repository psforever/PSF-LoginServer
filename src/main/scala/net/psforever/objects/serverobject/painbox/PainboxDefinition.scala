package net.psforever.objects.serverobject.painbox

import net.psforever.objects.serverobject.structures.AmenityDefinition
import net.psforever.types.Vector3

class PainboxDefinition(objectId: Int) extends AmenityDefinition(objectId) {
  var alwaysOn: Boolean        = true
  var sphereOffset             = Vector3.Zero
  var hasNearestDoorDependency = false

  def AlwaysOn: Boolean                 = alwaysOn
  def SphereOffset: Vector3             = sphereOffset
  def HasNearestDoorDependency: Boolean = hasNearestDoorDependency
}
