// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ce.{SimpleDeployable, TelepadLike}
import net.psforever.objects.definition.SimpleDeployableDefinition

class TelepadDeployable(ddef: SimpleDeployableDefinition) extends SimpleDeployable(ddef) with TelepadLike
