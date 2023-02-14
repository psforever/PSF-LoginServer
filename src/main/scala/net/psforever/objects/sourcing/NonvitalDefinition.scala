// Copyright (c) 2017-2023 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.vital.VitalityDefinition
import net.psforever.objects.vital.resistance.ResistanceProfileMutators

/**
 * A wrapper for a definition that does not represent a `Vitality` object
 * but needs to look like one internally to satisfy type requirements.
 * @param definition the original definition
 */
class NonvitalDefinition(private val definition : ObjectDefinition)
  extends ObjectDefinition(definition.ObjectId)
    with ResistanceProfileMutators
    with VitalityDefinition {
  Name = definition.Name
  Packet = definition.Packet

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