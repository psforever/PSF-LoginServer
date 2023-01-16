package net.psforever.actors.session.support

import net.psforever.objects.avatar.Avatar
import net.psforever.objects.zones.Zone
import net.psforever.objects.{Account, Player, Session}
import net.psforever.packet.PlanetSideGamePacket
import org.log4s.Logger

trait CommonSessionInterfacingFuncs {
  def sessionData: SessionData

  def session: Session = sessionData.session

  def session_=(newsession: Session): Unit = sessionData.session_=(newsession)

  def account: Account = sessionData.account

  def continent: Zone = sessionData.continent

  def player: Player = sessionData.player

  def avatar: Avatar = sessionData.avatar

  def log: Logger = sessionData.log

  def sendResponse(pkt: PlanetSideGamePacket): Unit = sessionData.sendResponse(pkt)
}
