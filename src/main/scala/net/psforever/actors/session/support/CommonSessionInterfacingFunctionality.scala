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
   * one where the result is always directed back to the same `SessionActor` instance.
   * If there is a different packet "sender" that has to be respected by a given method,
   * pass that `ActorRef` into the method as a parameter.
   * @see `ActorRef.!(Any)(ActorRef)`
   * @see `ActorRef.tell(Any)(ActorRef)`
   */
  protected implicit val sender: ActorRef = context.self

  protected def context: ActorContext

  protected def sessionLogic: SessionData

  protected def session: Session = sessionLogic.session

  protected def session_=(newsession: Session): Unit = sessionLogic.session_=(newsession)

  protected def account: Account = sessionLogic.account

  protected def continent: Zone = sessionLogic.continent

  protected def player: Player = sessionLogic.player

  protected def avatar: Avatar = sessionLogic.avatar

  protected def log: Logger = sessionLogic.log

  protected def sendResponse(pkt: PlanetSideGamePacket): Unit = sessionLogic.sendResponse(pkt)

  protected[session] def actionsToCancel(): Unit = { /* to override */ }

  protected[session] def stop(): Unit = { /* to override */ }
}
