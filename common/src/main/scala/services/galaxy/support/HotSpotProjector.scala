// Copyright (c) 2019 PSForever
package services.galaxy.support

import akka.actor.Actor
import net.psforever.objects.zones.{MapScale, Zone, HotSpotInfo => ZoneHotSpotInfo}
import net.psforever.packet.game.{HotSpotInfo => GameHotSpotInfo}
import net.psforever.types.Vector3
import services.galaxy.{GalaxyAction, GalaxyServiceMessage}

class HotSpotProjector extends Actor {
  private val sectorDivs : Int = 64
  private val sectorFunc : (MapScale, Vector3, Int, Int)=>Vector3 = HotSpotProjector.Sector

  def receive : Receive = {
    case Zone.HotSpot.Activity(zone, defender, attacker, location) =>
      val originalNumber = zone.HotSpots.size
      val hotSpot = {
        zone.TryHotSpot(
          Seq(attacker, defender),
          sectorFunc(zone.Map.Scale, location, sectorDivs, sectorDivs)
        )
      }
      val updatedHotSpots = zone.HotSpots
      if(originalNumber != updatedHotSpots.size) {
        UpdateHotSpots(zone, updatedHotSpots)
      }

    case _ => ;
  }

  def UpdateHotSpots(zone : Zone, hotSpotInfos : List[ZoneHotSpotInfo]) : Unit = {
    //TODO combined hotspot map; separate by and distribute to empires involved
    context.parent ! GalaxyServiceMessage(
      GalaxyAction.HotSpotUpdate(
        zone.Number,
        0,
        hotSpotInfos.map { spot => GameHotSpotInfo(spot.DisplayLocation.x, spot.DisplayLocation.y, 40) }
      )
    )
  }
}

object HotSpotProjector {
  def Sector(scale : MapScale, pos : Vector3, longDivNum : Int, latDivNum : Int) : Vector3 = {
    val (posx, posy) = (pos.x, pos.y)
    val width = scale.width
    val height = scale.height
    val divWidth : Float = width / longDivNum
    val divHeight : Float = height / latDivNum
    Vector3(
      //x
      if(posx >= width - divWidth) {
        width - divWidth
      }
      else if(posx >= divWidth) {
        val sector : Float = (posx * longDivNum / width).toInt * divWidth
        val nextSector : Float = sector + divWidth
        if(posx - sector < nextSector - posx) {
          sector
        }
        else {
          nextSector
        }
      }
      else {
        divWidth
      },
      //y
      if(posy >= height - divHeight) {
        height - divHeight
      }
      else if(posy >= divHeight) {
        val sector : Float = (posy * latDivNum / height).toInt * divHeight
        val nextSector : Float = sector + divHeight
        if(posy - sector < nextSector - posy) {
          sector
        }
        else {
          nextSector
        }
      }
      else {
        divHeight
      },
      //z
      0
    )
  }
}
