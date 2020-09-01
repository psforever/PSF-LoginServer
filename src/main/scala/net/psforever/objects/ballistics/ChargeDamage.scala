// Copyright (c) 2020 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.vital.StandardDamageProfile

final case class ChargeDamage(
                               effect_count: Int,
                               min: StandardDamageProfile
                             )
