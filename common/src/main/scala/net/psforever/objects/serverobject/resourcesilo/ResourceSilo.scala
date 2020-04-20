// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.resourcesilo

import akka.actor.{ActorContext, Props}
import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.UseItemMessage

class ResourceSilo extends Amenity {
  private var chargeLevel : Int = 0
  private val maximumCharge : Int = 1000

  // For the flashing red light on top of the NTU silo on.
  // Default to true until charge level can be persisted across restarts as default charge level is 0
  private var lowNtuWarningOn : Boolean = true

  // For the NTU display bar
  private var capacitorDisplay : Long = 0

  def ChargeLevel : Int = chargeLevel

  // Do not call directly. Use ResourceSilo.UpdateChargeLevel message to handle logic such as low ntu warnings
  def ChargeLevel_=(charge: Int) : Int = {
    if(charge < 0 ) {
      chargeLevel = 0
    } else if (charge > maximumCharge) {
      chargeLevel = maximumCharge
    } else {
      chargeLevel = charge
    }
    ChargeLevel
  }

  def MaximumCharge : Int = maximumCharge

  def LowNtuWarningOn : Boolean = lowNtuWarningOn
  def LowNtuWarningOn_=(enabled: Boolean) : Boolean = {
    lowNtuWarningOn = enabled
    LowNtuWarningOn
  }

  def CapacitorDisplay : Long = scala.math.ceil((ChargeLevel.toFloat / MaximumCharge.toFloat) * 10).toInt

  def Definition : ResourceSiloDefinition = GlobalDefinitions.resource_silo

  def Use(player: Player, msg : UseItemMessage) : ResourceSilo.Exchange = {
    ResourceSilo.ChargeEvent()
  }
}


object ResourceSilo {

  final case class Use(player: Player, msg : UseItemMessage)
  final case class UpdateChargeLevel(amount: Int)
  final case class LowNtuWarning(enabled: Boolean)
  sealed trait Exchange
  final case class ChargeEvent() extends Exchange
  final case class ResourceSiloMessage(player: Player, msg : UseItemMessage, response : Exchange)


  /**
    * Overloaded constructor.
    * @return the `Resource Silo` object
    */
  def apply() : ResourceSilo = {
    new ResourceSilo()
  }

  /**
    * Instantiate and configure a `Resource Silo` object
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality;
    *                not necessary for this object, but required by signature
    * @return the `ResourceSilo` object
    */
  def Constructor(id : Int, context : ActorContext) : ResourceSilo = {
    val obj = ResourceSilo()
    obj.Actor = context.actorOf(Props(classOf[ResourceSiloControl], obj), s"${obj.Definition.Name}_$id")
    obj
  }
}
