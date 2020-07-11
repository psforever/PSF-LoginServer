package net.psforever.login.psadmin

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.zones.InterstellarCluster

import scala.collection.mutable.Map

class CmdListPlayers(args: Array[String], services: Map[String, ActorRef]) extends Actor {
  private[this] val log = org.log4s.getLogger(self.path.name)

  override def preStart = {
    services { "cluster" } ! InterstellarCluster.ListPlayers()
  }

  override def receive = {
    case InterstellarCluster.PlayerList(players) =>
      val data = Map[String, Any]()
      data { "player_count" } = players.size
      data { "player_list" } = Array[String]()

      if (players.isEmpty) {
        context.parent ! CommandGoodResponse("No players currently online!", data)
      } else {
        data { "player_list" } = players
        context.parent ! CommandGoodResponse(s"${players.length} players online\n", data)
      }
    case default => log.error(s"Unexpected message $default")
  }
}
