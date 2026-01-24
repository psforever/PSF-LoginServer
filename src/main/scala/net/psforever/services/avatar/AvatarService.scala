// Copyright (c) 2017 PSForever
package net.psforever.services.avatar

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.zones.Zone
import net.psforever.services.avatar.support.{CorpseRemovalActor, DroppedItemRemover}
import net.psforever.services.base.{EventServiceSupport, GenericEventServiceWithSupport, GenericMessageEnvelope}

case object CorpseRemovalSupport
  extends EventServiceSupport {
  def label: String = "undertaker"
  def constructor(context: ActorContext): ActorRef = {
    context.actorOf(Props[CorpseRemovalActor](), name = "CorpseRemoval")
  }
}

case object ItemRemoverSupport
  extends EventServiceSupport {
  def label: String = "janitor"
  def constructor(context: ActorContext): ActorRef = {
    context.actorOf(Props[DroppedItemRemover](), name = "ItemRemover")
  }
}

class AvatarService(zone: Zone)
  extends GenericEventServiceWithSupport[AvatarServiceResponse](
    busName = "Avatar",
    eventSupportServices = List(CorpseRemovalSupport, ItemRemoverSupport)
  ) {
  protected def compose(msg: GenericMessageEnvelope): AvatarServiceResponse = {
    AvatarServiceResponse(formatChannelOnBusName(msg.channel), msg.filter, msg.msg.response())
  }
}
