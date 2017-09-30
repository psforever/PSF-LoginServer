// Copyright (c) 2017 PSForever
import akka.actor.Actor
import net.psforever.packet.game.PlanetSideGUID

object LocalAction {
  trait Action

  final case class Door(player_guid : PlanetSideGUID) extends Action
}

object LocalServiceResponse {
  trait Response

  final case class Door(player_guid : PlanetSideGUID) extends Response
}

final case class LocalServiceMessage(forChannel : String, actionMessage : LocalAction.Action)

final case class LocalServiceResponse(toChannel : String, avatar_guid : PlanetSideGUID, replyMessage : LocalServiceResponse.Response) extends GenericEventBusMsg

/*
   /LocalEnvironment/
 */

class LocalService extends Actor {
  //import LocalService._
  private [this] val log = org.log4s.getLogger

  override def preStart = {
    log.info("Starting...")
  }

  val LocalEvents = new GenericEventBus[LocalServiceResponse]

  def receive = {
    case Service.Join(channel) =>
      val path = s"/$channel/LocalEnvironment"
      val who = sender()
      log.info(s"$who has joined $path")
      LocalEvents.subscribe(who, path)
    case Service.Leave() =>
      LocalEvents.unsubscribe(sender())
    case Service.LeaveAll() =>
      LocalEvents.unsubscribe(sender())

    case LocalServiceMessage(forChannel, action) =>
      action match {
        case LocalAction.Door(player_guid) =>
          LocalEvents.publish(
            LocalServiceResponse(s"/$forChannel/LocalEnvironment" + forChannel, player_guid, LocalServiceResponse.Door(player_guid))
          )
        case _ => ;
      }

    case msg =>
      log.info(s"Unhandled message $msg from $sender")
  }
}
