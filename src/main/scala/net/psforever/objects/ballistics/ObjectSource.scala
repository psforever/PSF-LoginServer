// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.VitalityDefinition
import net.psforever.objects.vital.resistance.ResistanceProfileMutators
import net.psforever.types.{PlanetSideEmpire, Vector3}

final case class ObjectSource(
    obj: PlanetSideGameObject,
    faction: PlanetSideEmpire.Value,
    position: Vector3,
    orientation: Vector3,
    velocity: Option[Vector3]
) extends SourceEntry {
  private val definition = obj.Definition match {
    case vital : VitalityDefinition => vital
    case genericDefinition => NonvitalDefinition(genericDefinition)
  }
  private val modifiers = definition match {
    case nonvital : NonvitalDefinition => nonvital
    case _ => ObjectSource.FixedResistances
  }
  override def Name    = SourceEntry.NameFormat(obj.Definition.Name)
  override def Faction = faction
  def Definition = definition
  def Position         = position
  def Orientation      = orientation
  def Velocity         = velocity
  def Modifiers = modifiers
}

object ObjectSource {
  final val FixedResistances = new ResistanceProfileMutators() { }

  def apply(obj: PlanetSideGameObject): ObjectSource = {
    ObjectSource(
      obj,
      obj match {
        case aligned: FactionAffinity => aligned.Faction
        case _ => PlanetSideEmpire.NEUTRAL
      },
      obj.Position,
      obj.Orientation,
      obj.Velocity
    )
  }
}

/**
  * A wrapper for a definition that does not represent a `Vitality` object.
  * @param definition the original definition
  */
class NonvitalDefinition(private val definition : ObjectDefinition)
  extends ObjectDefinition(definition.ObjectId)
    with ResistanceProfileMutators
    with VitalityDefinition {
  Name = { definition.Name }
  Packet = { definition.Packet }

  def canEqual(a: Any) : Boolean = a.isInstanceOf[definition.type]

  override def equals(that: Any): Boolean = definition.equals(that)

  override def hashCode: Int = definition.hashCode
}

object NonvitalDefinition {
  //single point of contact for all wrapped definitions
  private val storage: scala.collection.mutable.LongMap[NonvitalDefinition] =
    new scala.collection.mutable.LongMap[NonvitalDefinition]()

  def apply(definition : ObjectDefinition) : NonvitalDefinition = {
    storage.get(definition.ObjectId) match {
      case Some(existing) =>
        existing
      case None =>
        val out = new NonvitalDefinition(definition)
        storage += definition.ObjectId.toLong -> out
        out
    }
  }
}
