// Copyright (c) 2021 PSForever
package net.psforever.objects.guid.uns

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.NumberPoolHub

final case class UnregisteredEntity(
                                     obj: IdentifiableEntity,
                                     pool_name: String,
                                     guid_system: NumberPoolHub,
                                     number: Int
                                   )

final case class AlreadyUnregisteredEntity(msg: UnregisteredEntity)
