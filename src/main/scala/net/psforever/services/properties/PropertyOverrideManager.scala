package net.psforever.services.properties

import org.apache.pekko.actor.Actor
import net.psforever.packet.game.{GamePropertyTarget, PropertyOverrideMessage}
import net.psforever.packet.game.PropertyOverrideMessage.GamePropertyScope
import net.psforever.packet.game.objectcreate.ObjectClass
import net.psforever.zones.Zones

import scala.collection.mutable.ListBuffer

class PropertyOverrideManager extends Actor {
  private[this] val log = org.log4s.getLogger("PropertyOverrideManager")

  private var overrides: Map[Int, Map[String, List[(String, String)]]]            = Map()
  private var gamePropertyScopes: List[PropertyOverrideMessage.GamePropertyScope] = List()

  override def preStart(): Unit = {
    LoadOverridesFromFile(zoneId = 0) // Global overrides
    for (zoneId <- Zones.zones.map(_.Number)) {
      LoadOverridesFromFile(zoneId)
    }
    ProcessGamePropertyScopes()
  }

  override def receive: Receive = {
    case PropertyOverrideManager.GetOverridesMessage =>
      sender() ! gamePropertyScopes

    case _ => ()
  }

  private def LoadOverridesFromFile(zoneId: Int): Unit = {
    val zoneOverrides = if (zoneId > 13 && zoneId < 23) {
      LoadFile(s"overrides/game_objectstz.adb.lst")
    } else {
      LoadFile(s"overrides/game_objects$zoneId.adb.lst")
    }
    if (zoneOverrides == null) {
      log.debug(s"PropertyOverride: no overrides found for zone $zoneId using filename game_objects$zoneId.adb.lst")
    } else {
      val grouped = zoneOverrides.groupBy(_._1).view.mapValues(_.map(x => (x._2, x._3)).toList).toMap
      log.debug(s"PropertyOverride: loaded property overrides for zone $zoneId: ${grouped.toString}")
      overrides += (zoneId -> grouped)
    }
  }

  private def ProcessGamePropertyScopes(): Unit = {
    val scopesBuffer: ListBuffer[GamePropertyScope] = ListBuffer()
    for (over <- overrides) {
      val zoneId      = over._1
      val overrideMap = over._2
      val gamePropertyTargets: ListBuffer[PropertyOverrideMessage.GamePropertyTarget] = ListBuffer()
      for (propOverride <- overrideMap) {
        val objectId = ObjectClass.ByName(propOverride._1)
        val props    = GamePropertyTarget(objectId, propOverride._2)
        gamePropertyTargets += props
      }
      val scope = GamePropertyScope(zoneId, gamePropertyTargets.toList)
      scopesBuffer += scope
    }
    gamePropertyScopes = scopesBuffer.toList
  }

  def LoadFile(path: String): List[(String, String, String)] = {
    val stream = getClass.getClassLoader.getResourceAsStream(path)
    if (stream == null) {
      List.empty[(String, String, String)]
    } else {
      val content = scala.io.Source.fromInputStream(stream).getLines()
      val data = content
        .filter(_.startsWith("add_property"))
        .map { line => (line, line.split("\\s+")) }
        .filter(_._2.length > 2) //n >= 3
        .map { case (line, tokens) =>
          val objectName = tokens(1)
          val property = tokens(2)
          val propertyValue = line.drop(objectName.length + property.length + 15) //"add_property" (12) + spaces (3)
          (objectName, property, propertyValue)
        }
        .toList
      stream.close()
      data
    }
  }
}

object PropertyOverrideManager {
  final case object GetOverridesMessage
}
