// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, ActorSystem}
import net.psforever.objects.entity.IdentifiableEntity

import scala.util.{Failure, Success}

class ResolutionObject {
  var complete = false
}

/**
  * This is for file NumberPoolActorTest, for its tests.
  * Attempting to define this class in the aforementioned file causes a "can not find constructor" issue.
  */
class Receiver(private val system : ActorSystem, result : ResolutionObject) extends Actor {
  def receive : Receive = {
    case Success(objct : IdentifiableEntity) =>
      objct.GUID //this will throw a NoGUIDException if it fails
      result.complete = true
      system.terminate()
    case Failure(ex) =>
      org.log4s.getLogger.error(s"object did not register - ${ex.getMessage}")
      system.terminate()
  }
}
//TODO Look into whether that was a legitimate issue or whether I (the user) was in error during Actor initialization later.
