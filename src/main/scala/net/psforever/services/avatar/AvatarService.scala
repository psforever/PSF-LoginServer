// Copyright (c) 2017 PSForever
package net.psforever.services.avatar

import akka.actor.Props
import net.psforever.services.avatar.support.{CorpseRemovalSupport, LitterRemovalSupport}
import net.psforever.services.base.{EventSystemStamp, GenericEventServiceWithCacheAndSupport}

case object AvatarStamp extends EventSystemStamp

object AvatarService {
  def apply(): Props = {
    Props(
      classOf[GenericEventServiceWithCacheAndSupport],
      AvatarStamp,
      List(CorpseRemovalSupport, LitterRemovalSupport)
    )
  }
}
