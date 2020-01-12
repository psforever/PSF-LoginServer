// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.generator

import net.psforever.objects.serverobject.structures.Amenity

class Generator(private val gdef : GeneratorDefinition) extends Amenity {
  //TODO should have Vitality, to indicate damaged/destroyed property
  def Definition : GeneratorDefinition = gdef
}

object Generator {
  def apply(gdef : GeneratorDefinition) : Generator = {
    new Generator(gdef)
  }

  import akka.actor.ActorContext
  def Constructor(id : Int, context : ActorContext) : Generator = {
    import akka.actor.Props
    import net.psforever.objects.GlobalDefinitions

    val obj = Generator(GlobalDefinitions.generator)
    obj.Actor = context.actorOf(Props(classOf[GeneratorControl], obj), s"${obj.Definition.Name}_$id")
    obj
  }

  import net.psforever.types.Vector3
  def Constructor(pos : Vector3)(id : Int, context : ActorContext) : Generator = {
    import akka.actor.Props
    import net.psforever.objects.GlobalDefinitions

    val obj = Generator(GlobalDefinitions.generator)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[GeneratorControl], obj), s"${obj.Definition.Name}_$id")
    obj
  }
}
