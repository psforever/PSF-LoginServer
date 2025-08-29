// Copyright (c) 2024 PSForever
package net.psforever.services.chat

import net.psforever.types.PlanetSideGUID

trait ChatChannel

case object DefaultChannel extends ChatChannel

final case class SquadChannel(guid: PlanetSideGUID) extends ChatChannel

case object SpectatorChannel extends ChatChannel

case object CustomerServiceChannel extends ChatChannel

final case class OutfitChannel(id: Long) extends ChatChannel
