// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.locks

import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage}

object IFFLocks {

  /**
    * The process of resecuring an IFF lock is finished
    * Clear the hack state and send to clients
    *
    * @param lock the `IFFLock` object that has been resecured
    */
  def FinishResecuringIFFLock(lock: IFFLock)(): Unit = {
    val zone = lock.Zone
    lock.Zone.LocalEvents ! LocalServiceMessage(
      zone.id,
      LocalAction.ClearTemporaryHack(Service.defaultPlayerGUID, lock)
    )
  }
}
