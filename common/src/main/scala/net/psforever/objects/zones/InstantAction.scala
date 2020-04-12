package net.psforever.objects.zones

import net.psforever.objects.SpawnPoint
import net.psforever.types.{PlanetSideEmpire, Vector3}

object InstantAction {

  /**
    * The current state of the instant action process.
    * The process of instant action transport exists in between a successful request message
    * and the ramp-up time before the instant action occurs.<br>
    * `None` should be the default condition
    * and always be the condition unless some other part of the process is being executed.
    * Before the instant action request is sent: `None`.
    * If the instant action request is denied or cancelled: `None`.
    * After the instant action is accomplished (via reconstruction or droppod use): `None`.<br>
    * `Request` is used to denote a request has been submitted and the initial result is being awaited.<br>
    * `Countdown` is to indicate the ramp-up time before transportation.
    * Some other mechanism must be used to monitor the ramp-up time.<br>
    * `Droppod` flags that the user is using a droppod to conduct instant action transportation.
    * It should be cleared to `None` after the user dismounts the pod.
    */
  object Status extends Enumeration {
    type Type = Value

    val
    None,
    Request,
    Countdown,
    Droppod
    = Value
  }

  /**
    * How many seconds the user has to wait until instant action occurs.
    * The primary method of determination involves the comparison of faction affinity.
    */
  object Time {
    final val Sanctuary : Int = 10
    final val Friendly : Int = 10
    final val Neutral : Int = 20
    final val Enemy : Int = 30
  }

  final case class Request(faction : PlanetSideEmpire.Value)

  final case class Located(zone : Zone, hotspot : Vector3, spawn_pos : Vector3, spawn_point : Option[SpawnPoint])

  final case class NotLocated()
}
