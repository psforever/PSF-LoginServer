// Copyright (c) 2025 PSForever
package net.psforever.objects.serverobject.dome

import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminalAware
import net.psforever.types.Vector3

class ForceDomePhysics(private val cfddef: ForceDomeDefinition)
  extends Amenity
    with CaptureTerminalAware {
  /** whether the dome is active or not */
  private var energized: Boolean = false
  /** defined perimeter of this force dome on the floor;
   * the walls created by this perimeter are angled inwards towards the facility vertically, but that's not a consideration here */
  private var perimeter: List[(Vector3, Vector3)] = List()

  override def Position: Vector3 = Owner.Position

  override def Position_=(vec: Vector3): Vector3 = Owner.Position

  override def Orientation: Vector3 = Owner.Orientation

  override def Orientation_=(vec: Vector3): Vector3 = Owner.Orientation

  def Energized: Boolean = energized

  def Energized_=(state: Boolean): Boolean = {
    energized = state
    Energized
  }

  def Perimeter: List[(Vector3, Vector3)] = perimeter

  def Perimeter_=(list: List[(Vector3, Vector3)]): List[(Vector3, Vector3)] = {
    perimeter = list
    Perimeter
  }

  def Definition: ForceDomeDefinition = cfddef
}

object ForceDomePhysics {
  import akka.actor.ActorContext

  /**
   * Instantiate and configure a `CapitolForceDome` object.
   * @param fddef specific type of force dome
   * @param id the unique id that will be assigned to this entity
   * @param context a context to allow the object to properly set up `ActorSystem` functionality
   * @return the `CapitolForceDome` object
   */
  def Constructor(fddef: ForceDomeDefinition)(id: Int, context: ActorContext): ForceDomePhysics = {
    import akka.actor.Props

    val obj = new ForceDomePhysics(fddef)
    obj.Actor = context.actorOf(Props(classOf[ForceDomeControl], obj), name = s"${fddef.Name}_$id")
    obj
  }
}
