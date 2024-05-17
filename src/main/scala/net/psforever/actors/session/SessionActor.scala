// Copyright (c) 2016, 2020, 2024 PSForever
package net.psforever.actors.session

import akka.actor.{Actor, Cancellable, MDCContextAware, typed}
import net.psforever.actors.session.normal.NormalMode
import org.joda.time.LocalDateTime
import org.log4s.MDC

import scala.collection.mutable
//
import net.psforever.actors.net.MiddlewareActor
import net.psforever.actors.session.support.{ModeLogic, PlayerMode, SessionData}
import net.psforever.objects.{Default, Player}
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.definition.BasicDefinition
import net.psforever.packet.PlanetSidePacket
import net.psforever.packet.game.{FriendsResponse, KeepAliveMessage}
import net.psforever.types.Vector3

object SessionActor {
  sealed trait Command

  private[session] final case class ServerLoaded()

  private[session] final case class NewPlayerLoaded(tplayer: Player)

  private[session] final case class PlayerLoaded(tplayer: Player)

  private[session] final case class PlayerFailedToLoad(tplayer: Player)

  private[session] final case class SetCurrentAvatar(tplayer: Player, max_attempts: Int, attempt: Int = 0)

  final case class SendResponse(packet: PlanetSidePacket) extends Command

  final case class SetSpeed(speed: Float) extends Command

  final case class SetFlying(flying: Boolean) extends Command

  final case class SetSpectator(spectator: Boolean) extends Command

  final case class SetZone(zoneId: String, position: Vector3) extends Command

  final case class SetPosition(position: Vector3) extends Command

  final case class SetConnectionState(connectionState: Int) extends Command

  final case class SetSilenced(silenced: Boolean) extends Command

  final case class SetAvatar(avatar: Avatar) extends Command

  final case class Recall() extends Command

  final case class InstantAction() extends Command

  final case class Quit() extends Command

  final case class Suicide() extends Command

  final case class Kick(player: Player, time: Option[Long] = None) extends Command

  final case class UseCooldownRenewed(definition: BasicDefinition, time: LocalDateTime) extends Command

  final case class UpdateIgnoredPlayers(msg: FriendsResponse) extends Command

  final case class AvatarLoadingSync(step: Int) extends Command

  final case object CharSaved extends Command

  private[session] case object CharSavedMsg extends Command

  final case object StartHeartbeat extends Command

  private final case object PokeClient extends Command

  final case class SetMode(mode: PlayerMode) extends Command
}

class SessionActor(middlewareActor: typed.ActorRef[MiddlewareActor.Command], connectionId: String, sessionId: Long)
  extends Actor
    with MDCContextAware {
  MDC("connectionId") = connectionId

  private var clientKeepAlive: Cancellable = Default.Cancellable
  private[this] val buffer: mutable.ListBuffer[Any] = new mutable.ListBuffer[Any]()
  private[this] val data = new SessionData(middlewareActor, context)
  private[this] var mode: PlayerMode = NormalMode
  private[this] var logic: ModeLogic = _

  override def postStop(): Unit = {
    clientKeepAlive.cancel()
    data.stop()
  }

  def receive: Receive = startup

  private def startup: Receive = {
    case msg if !data.assignEventBus(msg) =>
      buffer.addOne(msg)
    case _ if data.whenAllEventBusesLoaded() =>
      context.become(inTheGame)
      logic = mode.setup(data)
      buffer.foreach { self.tell(_, self) } //we forget the original sender, shouldn't be doing callbacks at this point
      buffer.clear()
    case _ => ()
  }

  private def inTheGame: Receive = {
    /* used for the game's heartbeat */
    case SessionActor.StartHeartbeat =>
      startHeartbeat()

    case SessionActor.PokeClient =>
      middlewareActor ! MiddlewareActor.Send(KeepAliveMessage())

    case SessionActor.SetMode(newMode) =>
      if (mode != newMode) {
        logic.switchFrom(data.session)
      }
      mode = newMode
      logic = mode.setup(data)
      logic.switchTo(data.session)

    case packet =>
      logic.parse(sender())(packet)
  }

  private def startHeartbeat(): Unit = {
    import scala.concurrent.duration._
    import scala.concurrent.ExecutionContext.Implicits.global
    clientKeepAlive.cancel()
    clientKeepAlive = context.system.scheduler.scheduleWithFixedDelay(
      initialDelay = 0.seconds,
      delay = 500.milliseconds,
      context.self,
      SessionActor.PokeClient
    )
  }
}
