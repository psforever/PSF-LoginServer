package net.psforever.objects.serverobject.zipline

import net.psforever.types.Vector3

class ZipLinePath(
    private val pathId: Integer,
    private val isTeleporter: Boolean,
    private val zipLinePoints: List[Vector3]
) {
  def PathId: Integer              = pathId
  def IsTeleporter: Boolean        = isTeleporter
  def ZipLinePoints: List[Vector3] = zipLinePoints
}
