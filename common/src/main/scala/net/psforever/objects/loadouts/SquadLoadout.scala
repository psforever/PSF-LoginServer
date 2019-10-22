// Copyright (c) 2019 PSForever
package net.psforever.objects.loadouts

import net.psforever.types.CertificationType

final case class SquadPositionLoadout(index : Int, role : String, orders : String, requirements : Set[CertificationType.Value])

final case class SquadLoadout(task : String,
                              zone_id : Option[Int],
                              members : List[SquadPositionLoadout]) extends Loadout(task)
