// Copyright (c) 2023 PSForever
package net.psforever.actors.session.support

import akka.actor.{ActorContext, ActorRef}
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.zones.Zone
import net.psforever.objects.{Account, Player, Session}
import net.psforever.packet.PlanetSideGamePacket
import org.log4s.Logger

trait CommonSessionInterfacingFunctionality {
  /**
   * Hardwire an implicit `sender` to be the same as `context.self` of the `SessionActor` actor class
   * for which this support class was initialized.
   * Allows for proper use for `ActorRef.tell` or an actor's `!` in the support class,
   * one where the result is always directed back to the same `SessionActor` instance, if applicable.
   * If there is a different packet "sender" that has to be respected by a given method,
   * pass that `ActorRef` into the method as a parameter instead.
   */
  protected implicit val sender: ActorRef = context.self

  def context: ActorContext

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
