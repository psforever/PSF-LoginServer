// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.generator

import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.types.PlanetSideGeneratorState

/**
  * The generator is a big feature of all major facilities.
  * It takes nanites from the NTU Silo and transforms it into power for the other amenities in the facility
  * as well as distributing nanites for self-repair mechanisms.
  * The only exception
  * (in that the "exception" is something that does not require the generator to power it)
  * is the capture console / control console.
  * The generator is capable of self-repair from a completely destroyed state, as long as it has an supply of nanites.
  * @param gdef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class Generator(private val gdef: GeneratorDefinition) extends Amenity {
  private var condition: PlanetSideGeneratorState.Value = PlanetSideGeneratorState.Normal

  def Condition: PlanetSideGeneratorState.Value = condition

  def Condition_=(state: PlanetSideGeneratorState.Value): PlanetSideGeneratorState.Value = {
    condition = state
    Condition
  }

  override def Destroyed_=(state : Boolean) : Boolean = {
    val isDestroyed = super.Destroyed_=(state)
    condition = if (isDestroyed) {
      PlanetSideGeneratorState.Destroyed
    } else {
      PlanetSideGeneratorState.Normal
    }
    isDestroyed
  }

  def Definition: GeneratorDefinition = gdef
}

object Generator {
  def apply(gdef: GeneratorDefinition): Generator = {
    new Generator(gdef)
  }

  import akka.actor.ActorContext
  import net.psforever.types.Vector3
  def Constructor(pos: Vector3)(id: Int, context: ActorContext): Generator = {
    import akka.actor.Props
    import net.psforever.objects.GlobalDefinitions

    val obj = Generator(GlobalDefinitions.generator)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[GeneratorControl], obj), s"${obj.Definition.Name}_$id")
    obj
  }
}
