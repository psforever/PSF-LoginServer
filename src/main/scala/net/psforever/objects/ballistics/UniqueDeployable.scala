// Copyright (c) 2023 PSForever
package net.psforever.objects.ballistics

final case class UniqueDeployable(
                                   spawnTime: Long,
                                   originalOwnerName: String
                                 ) extends SourceUniqueness
