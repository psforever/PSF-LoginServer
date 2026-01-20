// Copyright (c) 2022 PSForever
package net.psforever.services.teamwork

import akka.actor.ActorRef

import scala.collection.mutable
import net.psforever.objects.teamwork.{Squad, SquadFeatures}
import net.psforever.packet.game.SquadDetail
import net.psforever.services.base.GenericEventBus
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

class SquadSubscriptionEntity {
  private[this] val log = org.log4s.getLogger(name="SquadService")

  /**
    * This is a formal `ActorEventBus` object that is reserved for faction-wide messages and squad-specific messages.
    * When the user joins the `SquadService` with a `Service.Join` message
    * that includes a confirmed faction affiliation identifier,
    * the origin `ActorRef` is added as a subscription.
    * Squad channels are produced when a squad is created,
    * and are subscribed to as users join the squad,
    * and unsubscribed from as users leave the squad.<br>
    * key - a `PlanetSideEmpire` value; value - `ActorRef` reference<br>
    * key - a consistent squad channel name; value - `ActorRef` reference
    * @see `CloseSquad`
    * @see `JoinSquad`
    * @see `LeaveSquad`
    * @see `Service.Join`
    * @see `Service.Leave`
    */
  val SquadEvents = new GenericEventBus[SquadServiceResponse]

  /**
    * This collection contains the message-sending contact reference for individuals.
    * When the user joins the `SquadService` with a `Service.Join` message
    * that includes their unique character identifier,
    * the origin `ActorRef` is added as a subscription.
    * It is maintained until they disconnect entirely.
    * The subscription is anticipated to belong to an instance of `SessionActor`.<br>
    * key - unique character identifier number; value - `ActorRef` reference for that character
    * @see `Service.Join`
    */
  val UserEvents: mutable.LongMap[ActorRef] = mutable.LongMap[ActorRef]()

  /**
    * Players who are interested in updated details regarding a certain squad
    * though they may not be a member of the squad.<br>
    * key - unique character identifier number; value - a squad identifier number
    */
  val MonitorSquadDetails: mutable.LongMap[SquadSubscriptionEntity.MonitorEntry] = mutable.LongMap[SquadSubscriptionEntity.MonitorEntry]()

  def postStop(): Unit = {
    MonitorSquadDetails.clear()
    UserEvents.foreach {
      case (_, actor) =>
        SquadEvents.unsubscribe(actor)
    }
    UserEvents.clear()
  }

  /**
    * Overloaded message-sending operation.
    * The `Actor` version wraps around the expected `!` functionality.
    * @param to an `ActorRef` which to send the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    */
  def Publish(to: ActorRef, msg: SquadResponse.Response): Unit = {
    Publish(to, msg, Nil)
  }

  /**
    * Overloaded message-sending operation.
    * The `Actor` version wraps around the expected `!` functionality.
    * @param to an `ActorRef` which to send the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    * @param excluded a group of character identifier numbers who should not receive the message
    *                 (resolved at destination)
    */
  def Publish(to: ActorRef, msg: SquadResponse.Response, excluded: Iterable[Long]): Unit = {
    to ! SquadServiceResponse("", excluded, msg)
  }

  /**
    * Overloaded message-sending operation.
    * Always publishes on the `SquadEvents` object.
    * @param to a faction affiliation used as the channel for the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    */
  def Publish(to: PlanetSideEmpire.Type, msg: SquadResponse.Response): Unit = {
    Publish(to, msg, Nil)
  }

  /**
    * Overloaded message-sending operation.
    * Always publishes on the `SquadEvents` object.
    * @param to a faction affiliation used as the channel for the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    * @param excluded a group of character identifier numbers who should not receive the message
    *                 (resolved at destination)
    */
  def Publish(to: PlanetSideEmpire.Type, msg: SquadResponse.Response, excluded: Iterable[Long]): Unit = {
    SquadEvents.publish(SquadServiceResponse(s"/$to/Squad", excluded, msg))
  }

  /**
    * Overloaded message-sending operation.
    * Strings come in three accepted patterns.
    * The first resolves into a faction name, as determined by `PlanetSideEmpire` when transformed into a string.
    * The second resolves into a squad's dedicated channel, a name that is formulaic.
    * The third resolves as a unique character identifier number.
    * @param to a string used as the channel for the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    */
  def Publish(to: String, msg: SquadResponse.Response): Unit = {
    Publish(to, msg, Nil)
  }

  /**
    * Overloaded message-sending operation.
    * Strings come in three accepted patterns.
    * The first resolves into a faction name, as determined by `PlanetSideEmpire` when transformed into a string.
    * The second resolves into a squad's dedicated channel, a name that is formulaic.
    * The third resolves as a unique character identifier number.
    * @param to a string used as the channel for the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    * @param excluded a group of character identifier numbers who should not receive the message
    *                 (resolved at destination, usually)
    */
  def Publish(to: String, msg: SquadResponse.Response, excluded: Iterable[Long]): Unit = {
    to match {
      case str if "TRNCVS".indexOf(str) > -1 || str.matches("(TR|NC|VS)-Squad\\d+") =>
        SquadEvents.publish(SquadServiceResponse(s"/$str/Squad", excluded, msg))
      case str if str.matches("\\d+") =>
        Publish(to.toLong, msg, excluded)
      case _ =>
        log.warn(s"Publish(String): subscriber information is an unhandled format - $to; message $msg dropped")
    }
  }

  /**
    * Overloaded message-sending operation.
    * Always publishes on the `ActorRef` objects retained by the `UserEvents` object.
    * @param to a unique character identifier used as the channel for the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    */
  def Publish(to: Long, msg: SquadResponse.Response): Unit = {
    UserEvents.get(to) match {
      case Some(user) =>
        user ! SquadServiceResponse("", msg)
      case None =>
        log.warn(s"Publish(Long): subscriber information can not be found - $to; message $msg dropped")
    }
  }

  /**
    * Overloaded message-sending operation.
    * Always publishes on the `ActorRef` objects retained by the `UserEvents` object.
    * @param to a unique character identifier used as the channel for the message
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    * @param excluded a group of character identifier numbers who should not receive the message
    */
  def Publish(to: Long, msg: SquadResponse.Response, excluded: Iterable[Long]): Unit = {
    if (!excluded.exists(_ == to)) {
      Publish(to, msg)
    }
  }

  /**
    * Overloaded message-sending operation.
    * No message can be sent using this distinction.
    * Log a warning.
    * @param to something that was expected to be used as the channel for the message
    *           but is not handled as such
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    */
  def Publish[ANY >: Any](to: ANY, msg: SquadResponse.Response): Unit = {
    log.warn(s"Publish(Any): subscriber information is an unhandled format - $to; message $msg dropped")
  }

  /**
    * Overloaded message-sending operation.
    * No message can be sent using this distinction.
    * Log a warning.
    * @param to something that was expected to be used as the channel for the message
    *           but is not handled as such
    * @param msg a message that can be stored in a `SquadServiceResponse` object
    * @param excluded a group of character identifier numbers who should not receive the message
    */
  def Publish[ANY >: Any](to: ANY, msg: SquadResponse.Response, excluded: Iterable[Long]): Unit = {
    log.warn(s"Publish(Any): subscriber information is an unhandled format - $to; message $msg dropped")
  }

  /* The following functions are related to common communications of squad information, mainly detail. */

  /**
    * Dispatch a message entailing the composition of this squad.
    * This is considered the first time this information will be dispatched to any relevant observers
    * so the details of the squad will be updated in full and be sent to all relevant observers,
    * namely, all the occupants of the squad.
    * External observers are ignored.
    * @see `InitSquadDetail(PlanetSideGUID, Iterable[Long], Squad)`
    * @param features the squad
    */
  def InitSquadDetail(features: SquadFeatures): Unit = {
    val squad = features.Squad
    InitSquadDetail(
      squad.GUID,
      squad.Membership.collect { case member if member.CharId > 0 => member.CharId },
      squad
    )
  }

  /**
    * Dispatch an initial message entailing the strategic information and the composition of this squad.
    * The details of the squad will be updated in full and be sent to all indicated observers.
    * @see `SquadService.PublishFullDetails`
    * @param guid the unique squad identifier to be used when composing the details for this message
    * @param to the unique character identifier numbers of the players who will receive this message
    * @param squad the squad from which the squad details shall be composed
    */
  def InitSquadDetail(guid: PlanetSideGUID, to: Iterable[Long], squad: Squad): Unit = {
    val output = SquadResponse.Detail(guid, SquadService.PublishFullDetails(squad))
    to.foreach { Publish(_, output) }
  }

  /**
    * Send a message entailing the strategic information and the composition of the squad to the existing members of the squad.
    * @see `SquadService.PublishFullDetails`
    * @see `UpdateSquadDetail(PlanetSideGUID, PlanetSideGUID, List[Long], SquadDetail)`
    * @param features the squad
    */
  def UpdateSquadDetail(features: SquadFeatures): Unit = {
    val squad = features.Squad
    UpdateSquadDetail(
      squad.GUID,
      features.ToChannel,
      Nil,
      SquadService.PublishFullDetails(squad)
    )
  }

  /**
    * Send a message entailing some of the strategic information and the composition to the existing members of the squad.
    * @see `SquadResponse.Detail`
    * @see `UpdateSquadDetail(PlanetSideGUID, PlanetSideGUID, List[Long], SquadDetail)`
    * @param features information about the squad
    * @param details the squad details to be included in the message
    */
  def UpdateSquadDetail(features: SquadFeatures, details: SquadDetail): Unit = {
    UpdateSquadDetail(
      features.Squad.GUID,
      features.ToChannel,
      Nil,
      details
    )
  }

  /**
    * Send a message entailing some of the strategic information and the composition to the existing members of the squad.
    * Also send the same information to any users who are watching the squad, potentially for want to join it.
    * The squad-specific message is contingent on finding the squad's features using the unique identifier number
    * and, from that, reporting to the specific squad's messaging channel.
    * Anyone watching the squad will always be updated the given details.
    * @see `DisplaySquad`
    * @see `Publish`
    * @see `SquadDetail`
    * @see `SquadResponse.Detail`
    * @param guid the unique squad identifier number to be used for the squad detail message
    * @param toChannel the squad broadcast channel name
    * @param excluding the explicit unique character identifier numbers of individuals who should not receive the message
    * @param details the squad details to be included in the message
    */
  def UpdateSquadDetail(
                         guid: PlanetSideGUID,
                         toChannel: String,
                         excluding: Iterable[Long],
                         details: SquadDetail
                       ): Unit = {
    val output = SquadResponse.Detail(guid, details)
    Publish(toChannel, output, excluding)
    PublishToMonitorTargets(guid, excluding).foreach { charId => Publish(charId, output, Nil) }
  }

  /**
    * na
    * @see `LongMap.subtractOne`
    * @see `SquadSubscriptionEntity.MonitorEntry`
    * @param guid the unique squad identifier number to be used for the squad detail message
    * @param excluding the explicit unique character identifier numbers of individuals who should not receive the message
    */
  def PublishToMonitorTargets(
                               guid: PlanetSideGUID,
                               excluding: Iterable[Long]
                             ): Iterable[Long] = {
    val curr = System.currentTimeMillis()
    MonitorSquadDetails
      .toSeq
      .collect {
        case out @ (charId: Long, entry: SquadSubscriptionEntity.MonitorEntry)
          if entry.squadGuid == guid && !excluding.exists(_ == charId) =>
          if (curr - entry.time < 300000L) {
            Some(out._1)
          } else {
            MonitorSquadDetails.subtractOne(charId)
            None
          }
      }
      .flatten
  }
}

object SquadSubscriptionEntity {
  private[teamwork] case class MonitorEntry(squadGuid: PlanetSideGUID) {
    val time: Long = System.currentTimeMillis()
  }
}
