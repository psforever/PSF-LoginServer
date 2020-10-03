// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.resourcesilo

import akka.actor.{ActorContext, Props}
import net.psforever.objects.{CommonNtuContainer, GlobalDefinitions, Player}
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.UseItemMessage
import net.psforever.types.Vector3

class ResourceSilo extends Amenity with CommonNtuContainer {

  // For the flashing red light on top of the NTU silo on.
  // Default to true until charge level can be persisted across restarts as default charge level is 0
  private var lowNtuWarningOn: Boolean = true

  // For the NTU display bar
  private var capacitorDisplay: Long = 0

  def MaxNtuCapacitor : Float = Definition.MaxNtuCapacitor

  def LowNtuWarningOn: Boolean = lowNtuWarningOn
  def LowNtuWarningOn_=(enabled: Boolean): Boolean = {
    lowNtuWarningOn = enabled
    LowNtuWarningOn
  }

  def CapacitorDisplay : Long = scala.math.ceil((NtuCapacitor / MaxNtuCapacitor) * 10).toInt

  def Definition: ResourceSiloDefinition = GlobalDefinitions.resource_silo

  def Use(player: Player, msg: UseItemMessage): ResourceSilo.Exchange = {
    ResourceSilo.ChargeEvent()
  }
}

object ResourceSilo {
  final case class UpdateChargeLevel(amount: Float)
  final case class LowNtuWarning(enabled: Boolean)
  sealed trait Exchange
  final case class ChargeEvent() extends Exchange
  final case class ResourceSiloMessage(player: Player, msg: UseItemMessage, response: Exchange)

  /**
    * Overloaded constructor.
    * @return the `Resource Silo` object
    */
  def apply(): ResourceSilo = {
    new ResourceSilo()
  }

  /**
    * Instantiate and configure a `Resource Silo` object
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality;
    *                not necessary for this object, but required by signature
    * @return the `ResourceSilo` object
    */
  def Constructor(pos: Vector3)(id: Int, context: ActorContext): ResourceSilo = {
    val obj = ResourceSilo()
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[ResourceSiloControl], obj), s"${obj.Definition.Name}_$id")
    obj
  }
}
