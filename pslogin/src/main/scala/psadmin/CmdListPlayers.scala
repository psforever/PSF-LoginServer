// Copyright (c) 2020 PSForever
package net.psforever.psadmin

import java.net.InetAddress
import java.net.InetSocketAddress
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.actor.{Actor, Stash}
import akka.io.Tcp
import scodec.bits._
import scodec.interop.akka._
import scala.collection.mutable.Map
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._
import net.psforever.objects.zones.InterstellarCluster

import services.ServiceManager.Lookup
import services._

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
