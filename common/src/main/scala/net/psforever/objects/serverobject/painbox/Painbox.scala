package net.psforever.objects.serverobject.painbox

import akka.actor.{ActorContext, Props}
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.types.Vector3


class Painbox(tdef : PainboxDefinition) extends Amenity {
  def Definition = tdef
}

object Painbox {
  final case class Start()
  final case class Tick()
  final case class Stop()

  def apply(tdef : PainboxDefinition) : Painbox = {
    new Painbox(tdef)
  }

  def Constructor(pos : Vector3, tdef : PainboxDefinition)(id : Int, context : ActorContext) : Painbox = {
    val obj = Painbox(tdef)
    obj.Position = pos + tdef.SphereOffset
    obj.Actor = context.actorOf(Props(classOf[PainboxControl], obj), s"${obj.Definition.Name}_$id")
    obj
  }
}