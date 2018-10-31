// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ce.{SimpleDeployable, TelepadLike}
import net.psforever.objects.definition.DeployableDefinition

class TelepadDeployable(ddef : DeployableDefinition) extends SimpleDeployable(ddef)
  with TelepadLike
