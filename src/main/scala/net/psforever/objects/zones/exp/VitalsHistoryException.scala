// Copyright (c) 2023 PSForever
package net.psforever.objects.zones.exp

import net.psforever.objects.vital.InGameActivity

final case class VitalsHistoryException(
                                         head: InGameActivity, //InGameActivity might be more suitable?
                                         private val message: String = "",
                                         private val cause: Throwable = None.orNull
                                       ) extends Exception(message, cause)
