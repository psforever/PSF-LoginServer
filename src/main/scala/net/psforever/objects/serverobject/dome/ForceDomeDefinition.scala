// Copyright (c) 2025 PSForever
package net.psforever.objects.serverobject.dome

import net.psforever.objects.geometry.d3.{Sphere, VolumetricGeometry}
import net.psforever.objects.serverobject.structures.AmenityDefinition
import net.psforever.types.Vector3

class ForceDomeDefinition(objectId: Int)
  extends AmenityDefinition(objectId) {
  Name = "force_dome"
  Geometry = ForceDomeDefinition.representBy

  /** offsets that define the perimeter of the pyramidal force "dome" barrier;
   * these points are the closest to where the dome interacts with the ground at a corner;
   * should be sequential, either clockwise or counterclockwise */
  private var perimeter: List[Vector3] = List()
  /** offset of the physical location of the force dome generator stalk */
  private var genOffset: Vector3 = Vector3.Zero

  def PerimeterOffsets: List[Vector3] = perimeter

  def PerimeterOffsets_=(points: List[Vector3]): List[Vector3] = {
    perimeter = points
    PerimeterOffsets
  }

  private var protects: List[AmenityDefinition] = List()

  def ApplyProtectionTo: List[AmenityDefinition] = protects

  def ApplyProtectionTo_=(protect: AmenityDefinition): List[AmenityDefinition] = {
    ApplyProtectionTo_=(List(protect))
  }

  def ApplyProtectionTo_=(protect: List[AmenityDefinition]): List[AmenityDefinition] = {
    protects = protect
    ApplyProtectionTo
  }

  def GeneratorOffset: Vector3 = genOffset

  def GeneratorOffset_=(offset: Vector3): Vector3 = {
    genOffset = offset
    GeneratorOffset
  }
}

object ForceDomeDefinition {
  /**
   * Transform a capitol force dome into a bounded geometric representation.
   * @param o any entity from which to produce a geometric representation
   * @return geometric representation
   */
  def representBy(o: Any): VolumetricGeometry = {
    o match {
      case fdp: ForceDomePhysics =>
        Sphere(fdp.Position, fdp.Definition.UseRadius)
      case _ =>
        net.psforever.objects.geometry.GeometryForm.invalidPoint
    }
  }
}
