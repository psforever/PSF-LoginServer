// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

import akka.actor.Actor

class ZonePopulationActor(zone : Zone) extends Actor {
  def receive : Receive = {
    case Zone.Population.Join(avatar) =>
      zone.PopulationJoin(avatar)

    case Zone.Population.Leave(avatar) =>
      zone.PopulationLeave(avatar) match {
        case None => ;
        case player @ Some(_) =>
          sender ! Zone.Population.PlayerHasLeft(zone, player)
      }

    case Zone.Population.Spawn(avatar, player) =>
      zone.PopulationSpawn(avatar, player) match {
        case Some(tplayer) =>
          if(tplayer != player) {
            sender ! Zone.Population.PlayerCanNotSpawn(zone, player)
          }
        case None =>
          sender ! Zone.Population.PlayerCanNotSpawn(zone, player)
      }

    case Zone.Population.Release(avatar) =>
      zone.PopulationRelease(avatar) match {
        case Some(_) => ;
        case None =>
          sender ! Zone.Population.PlayerHasLeft(zone, None)
      }

    case Zone.Corpse.Add(player) =>
      zone.CorpseAdd(player)

    case Zone.Corpse.Remove(player) =>
      zone.CorpseRemove(player)

    case _ => ;
  }
}
