package net.psforever.login.psadmin

import akka.actor.typed.receptionist.Receptionist
import akka.actor.{Actor, ActorRef}
import services.{InterstellarClusterService, ServiceManager}
import scala.collection.mutable.Map
import akka.actor.typed.scaladsl.adapter._

class CmdListPlayers(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  override def preStart() = {
    ServiceManager.receptionist ! Receptionist.Find(
      InterstellarClusterService.InterstellarClusterServiceKey,
      context.self
    )
  }

  override def receive = {
    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      listings.head ! InterstellarClusterService.GetPlayers(context.self)

    case InterstellarClusterService.PlayersResponse(players) =>
      val data = Map[String, Any]()
      data {
        "player_count"
      } = players.size
      data {
        "player_list"
      } = Array[String]()

      if (players.isEmpty) {
        context.parent ! CommandGoodResponse("No players currently online!", data)
      } else {
        data {
          "player_list"
        } = players
        context.parent ! CommandGoodResponse(s"${players.length} players online\n", data)
      }
    case default => log.error(s"Unexpected message $default")
  }
}
