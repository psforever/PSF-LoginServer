package net.psforever.services.properties

import akka.actor.Actor
import net.psforever.packet.game.{GamePropertyTarget, PropertyOverrideMessage}
import net.psforever.packet.game.PropertyOverrideMessage.GamePropertyScope
import net.psforever.packet.game.objectcreate.ObjectClass
import net.psforever.zones.Zones
import scala.collection.mutable.ListBuffer

class PropertyOverrideManager extends Actor {
  private[this] val log = org.log4s.getLogger("PropertyOverrideManager")

  private var overrides: Map[Int, Map[String, List[(String, String)]]]            = Map()
  private var gamePropertyScopes: List[PropertyOverrideMessage.GamePropertyScope] = List()
  lazy private val zoneIds: Iterable[Int]                                         = Zones.zones.map(_.Number)

  override def preStart() = {
    LoadOverridesFromFile(zoneId = 0) // Global overrides
    for (zoneId <- zoneIds) {
      LoadOverridesFromFile(zoneId)
    }

    ProcessGamePropertyScopes()
  }

  override def receive: Receive = {
    case PropertyOverrideManager.GetOverridesMessage => {
      sender() ! gamePropertyScopes
    }
    case _ => ;
  }

  private def LoadOverridesFromFile(zoneId: Int): Unit = {
    val zoneOverrides = LoadFile(s"overrides/game_objects${zoneId}.adb.lst")

    if (zoneOverrides == null) {
      log.debug(s"No overrides found for zone ${zoneId} using filename game_objects${zoneId}.adb.lst")
      return
    }

    val grouped = zoneOverrides.groupBy(_._1).view.mapValues(_.map(x => (x._2, x._3)).toList).toMap

    log.debug(s"Loaded property overrides for zone $zoneId: ${grouped.toString}")
    overrides += (zoneId -> grouped)
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

  def LoadFile(path: String): ListBuffer[(String, String, String)] = {
    val stream = getClass.getClassLoader.getResourceAsStream(path)
    if (stream == null) {
      return null
    }

    val content                                    = scala.io.Source.fromInputStream(stream).getLines().filter(x => x.startsWith("add_property"))
    var data: ListBuffer[(String, String, String)] = ListBuffer()

    for (line <- content) {
      val splitLine = line.split(" ")
      if (splitLine.length >= 3) {
        val objectName = splitLine(1)
        val property   = splitLine(2)

        var propertyValue = ""
        for (i <- 3 to splitLine.length - 1) {
          propertyValue += splitLine(i) + " "
        }
        propertyValue = propertyValue.trim

        data += ((objectName, property, propertyValue))
      }
    }

    stream.close()

    data
  }

}

object PropertyOverrideManager {
  final case class GetOverridesMessage()
}
