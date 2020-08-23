// Copyright (c) 2019 PSForever
package net.psforever.services.teamwork

import akka.actor.{Actor, ActorRef, Terminated}

import scala.collection.mutable

/**
  * The dedicated messaging switchboard for members and observers of a given squad.
  * It almost always dispatches messages to `WorldSessionActor` instances, much like any other `Service`.
  * The sole purpose of this `ActorBus` container is to manage a subscription model
  * that can involuntarily drop subscribers without informing them explicitly
  * or can just vanish without having to properly clean itself up.
  */
class SquadSwitchboard extends Actor {

  /**
    * This collection contains the message-sending contact reference for squad members.
    * Users are added to this collection via the `SquadSwitchboard.Join` message, or a
    * combination of the `SquadSwitchboard.DelayJoin` message followed by a
    * `SquadSwitchboard.Join` message with or without an `ActorRef` hook.
    * The message `SquadSwitchboard.Leave` removes the user from this collection.
    * key - unique character id; value - `Actor` reference for that character
    */
  val UserActorMap: mutable.LongMap[ActorRef] = mutable.LongMap[ActorRef]()

  /**
    * This collection contains the message-sending contact information for would-be squad members.
    * Users are added to this collection via the `SquadSwitchboard.DelayJoin` message
    * and are promoted to an actual squad member through a `SquadSwitchboard.Join` message.
    * The message `SquadSwitchboard.Leave` removes the user from this collection.
    * key - unique character id; value - `Actor` reference for that character
    */
  val DelayedJoin: mutable.LongMap[ActorRef] = mutable.LongMap[ActorRef]()

  /**
    * This collection contains the message-sending contact information for squad observers.
    * Squad observers only get "details" messages as opposed to the sort of messages squad members receive.
    * Squad observers are promoted to an actual squad member through a `SquadSwitchboard.Watch` message.
    * The message `SquadSwitchboard.Leave` removes the user from this collection.
    * The message `SquadSwitchboard.Unwatch` also removes the user from this collection.
    * key - unique character id; value - `Actor` reference for that character
    */
  val Watchers: mutable.LongMap[ActorRef] = mutable.LongMap[ActorRef]()

  override def postStop(): Unit = {
    UserActorMap.clear()
    DelayedJoin.clear()
    Watchers.clear()
  }

  def receive: Receive = {
    case SquadSwitchboard.Join(char_id, Some(actor)) =>
      UserActorMap(char_id) = DelayedJoin.remove(char_id).orElse(Watchers.remove(char_id)) match {
        case Some(_actor) =>
          context.watch(_actor)
          _actor
        case None =>
          context.watch(actor)
          actor
      }

    case SquadSwitchboard.Join(char_id, None) =>
      DelayedJoin.remove(char_id).orElse(Watchers.remove(char_id)) match {
        case Some(actor) =>
          UserActorMap(char_id) = actor
        case None => ;
      }

    case SquadSwitchboard.DelayJoin(char_id, actor) =>
      context.watch(actor)
      DelayedJoin(char_id) = actor

    case SquadSwitchboard.Leave(char_id) =>
      UserActorMap
        .find { case (charId, _) => charId == char_id }
        .orElse(DelayedJoin.find { case (charId, _) => charId == char_id })
        .orElse(Watchers.find { case (charId, _) => charId == char_id }) match {
        case Some((member, actor)) =>
          context.unwatch(actor)
          UserActorMap.remove(member)
          DelayedJoin.remove(member)
          Watchers.remove(member)
        case None => ;
      }

    case SquadSwitchboard.Watch(char_id, actor) =>
      context.watch(actor)
      Watchers(char_id) = actor

    case SquadSwitchboard.Unwatch(char_id) =>
      Watchers.remove(char_id)

    case SquadSwitchboard.To(member, msg) =>
      UserActorMap.find { case (char_id, _) => char_id == member } match {
        case Some((_, actor)) =>
          actor ! msg
        case None => ;
      }

    case SquadSwitchboard.ToAll(msg) =>
      UserActorMap
        .foreach {
          case (_, actor) =>
            actor ! msg
        }

    case SquadSwitchboard.Except(excluded, msg) =>
      UserActorMap
        .filterNot { case (char_id, _) => char_id == excluded }
        .foreach {
          case (_, actor) =>
            actor ! msg
        }

    case Terminated(actorRef) =>
      UserActorMap
        .find { case (_, ref) => ref == actorRef }
        .orElse(DelayedJoin.find { case (_, ref) => ref == actorRef })
        .orElse(Watchers.find { case (_, ref) => ref == actorRef }) match {
        case Some((member, actor)) =>
          context.unwatch(actor)
          UserActorMap.remove(member)
          DelayedJoin.remove(member)
          Watchers.remove(member)
        case None => ;
      }

    case _ => ;
  }
}

object SquadSwitchboard {
  final case class Join(char_id: Long, actor: Option[ActorRef])

  final case class DelayJoin(char_id: Long, actor: ActorRef)

  final case class Leave(char_id: Long)

  final case class Watch(char_id: Long, actor: ActorRef)

  final case class Unwatch(char_id: Long)

  final case class To(member: Long, msg: SquadServiceResponse)

  final case class ToAll(msg: SquadServiceResponse)

  final case class Except(excluded_member: Long, msg: SquadServiceResponse)
}
