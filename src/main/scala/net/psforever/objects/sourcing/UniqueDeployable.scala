// Copyright (c) 2023 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.ce.Deployable

final case class UniqueDeployable(
    spawnTime: Long,
    originalOwnerName: String
) extends SourceUniqueness

object UniqueDeployable {
  def apply(obj: Deployable): UniqueDeployable = {
    UniqueDeployable(
      obj.History.headOption match {
        case Some(entry) => entry.time
        case None => 0L
      },
      obj.OriginalOwnerName.getOrElse("none")
    )
  }
}
