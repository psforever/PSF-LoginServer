// Copyright (c) 2017 PSForever
package net.psforever.services.avatar

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.zones.Zone
import net.psforever.services.avatar.support.{CorpseRemovalActor, DroppedItemRemover}
import net.psforever.services.base.{EventServiceSupport, GenericEventServiceWithCacheAndSupport, GenericMessageEnvelope}

case object CorpseRemovalSupport
  extends EventServiceSupport {
  def label: String = "undertaker"
  def constructor(context: ActorContext): ActorRef = {
    context.actorOf(Props[CorpseRemovalActor](), name = "CorpseRemoval")
  }
}

case object LitterRemovalSupport
  extends EventServiceSupport {
  def label: String = "janitor"
  def constructor(context: ActorContext): ActorRef = {
    context.actorOf(Props[DroppedItemRemover](), name = "DroppedItemRemover")
  }
}

class AvatarService(zone: Zone)
  extends GenericEventServiceWithCacheAndSupport[AvatarServiceResponse](
    busName = "Avatar",
    eventSupportServices = List(CorpseRemovalSupport, LitterRemovalSupport)
  ) {
  protected def composeResponseEnvelope(msg: GenericMessageEnvelope): AvatarServiceResponse = {
    AvatarServiceResponse(formatChannelOnBusName(msg.channel), msg.filter, msg.msg.response())
  }
}
