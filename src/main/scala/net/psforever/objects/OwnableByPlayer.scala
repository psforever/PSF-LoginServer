// Copyright (c) 2019 PSForever
package net.psforever.objects

import net.psforever.objects.sourcing.UniquePlayer
import net.psforever.types.PlanetSideGUID

trait OwnableByPlayer {
  private var owner: Option[UniquePlayer]       = None
  private var ownerGuid: Option[PlanetSideGUID] = None
  private var originalOwnerName: Option[String] = None

  def Owners: Option[UniquePlayer] = owner

  def OwnerGuid: Option[PlanetSideGUID] = ownerGuid

  def OwnerGuid_=(owner: PlanetSideGUID): Option[PlanetSideGUID] = OwnerGuid_=(Some(owner))

  def OwnerGuid_=(owner: Player): Option[PlanetSideGUID] = OwnerGuid_=(Some(owner.GUID))

  def OwnerGuid_=(owner: Option[PlanetSideGUID]): Option[PlanetSideGUID] = {
    owner match {
      case Some(_) =>
        ownerGuid = owner
      case None =>
        ownerGuid = None
    }
    OwnerGuid
  }

  def OwnerName: Option[String] = owner.map { _.name }

  def OriginalOwnerName: Option[String] = originalOwnerName

  /**
   * na
   * @param player na
   * @return na
   */
  def AssignOwnership(player: Player): OwnableByPlayer = AssignOwnership(Some(player))

  /**
   * na
   * @param playerOpt na
   * @return na
   */
  def AssignOwnership(playerOpt: Option[Player]): OwnableByPlayer = {
    (originalOwnerName, playerOpt) match {
      case (None, Some(player)) =>
        owner = Some(UniquePlayer(player))
        originalOwnerName = originalOwnerName.orElse { Some(player.Name) }
        OwnerGuid = player
      case (_, Some(player)) =>
        owner = Some(UniquePlayer(player))
        OwnerGuid = player
      case (_, None) =>
        owner = None
        OwnerGuid = None
    }
    this
  }

  /**
   * na
   * @param ownable na
   * @return na
   */
  def AssignOwnership(ownable: OwnableByPlayer): OwnableByPlayer = {
    owner = ownable.owner
    originalOwnerName = originalOwnerName.orElse { ownable.originalOwnerName }
    OwnerGuid = ownable.OwnerGuid
    this
  }
}
