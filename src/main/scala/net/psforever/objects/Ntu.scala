// Copyright (c) 2020 PSForever
package net.psforever.objects

import akka.actor.{Actor, ActorRef}
import net.psforever.actors.commands.NtuCommand
import net.psforever.objects.serverobject.transfer.{TransferBehavior, TransferContainer}

object Ntu {
  object Nanites extends TransferContainer.TransferMaterial

  /**
    * Message for a `sender` announcing it has nanites it can offer the recipient.
    *
    * @param src the nanite container recognized as the sender
    */
  final case class Offer(src: NtuContainer)

  /**
    * Message for a `sender` asking for nanites from the recipient.
    *
    * @param min a minimum amount of nanites requested;
    *            if 0, the `sender` has no expectations
    * @param max the amount of nanites required to not make further requests;
    *            if 0, the `sender` is full and the message is for clean up operations
    */
  final case class Request(min: Float, max: Float)

  /**
    * Message for transferring nanites to a recipient.
    *
    * @param src    the nanite container recognized as the sender
    * @param amount the nanites transferred in this package
    */
  final case class Grant(src: NtuContainer, amount: Float)
}

trait NtuContainer extends TransferContainer {
  def NtuCapacitor: Float

  def NtuCapacitor_=(value: Float): Float

  def Definition: NtuContainerDefinition
}

trait CommonNtuContainer extends NtuContainer {
  private var ntuCapacitor: Float = 0

  def NtuCapacitor: Float = ntuCapacitor

  def NtuCapacitor_=(value: Float): Float = {
    ntuCapacitor = scala.math.max(0, scala.math.min(value, Definition.MaxNtuCapacitor))
    NtuCapacitor
  }

  def Definition: NtuContainerDefinition
}

trait NtuContainerDefinition {
  private var maxNtuCapacitor: Float = 0

  def MaxNtuCapacitor: Float = maxNtuCapacitor

  def MaxNtuCapacitor_=(max: Float): Float = {
    maxNtuCapacitor = max
    MaxNtuCapacitor
  }
}

trait NtuStorageBehavior extends Actor {
  def NtuStorageObject: NtuContainer = null

  def storageBehavior: Receive = {
    case Ntu.Offer(src) => HandleNtuOffer(sender(), src)

    case Ntu.Grant(_, 0) | Ntu.Request(0, 0) | TransferBehavior.Stopping() => StopNtuBehavior(sender())

    case Ntu.Request(min, max) => HandleNtuRequest(sender(), min, max)

    case NtuCommand.Request(amount, replyTo) =>
      import akka.actor.typed.scaladsl.adapter.TypedActorRefOps
      HandleNtuRequest(new TypedActorRefOps(replyTo).toClassic, amount, amount+1)

    case Ntu.Grant(src, amount)        => HandleNtuGrant(sender(), src, amount)

    case NtuCommand.Grant(src, amount) => HandleNtuGrant(sender(), src, amount)
  }

  def HandleNtuOffer(sender: ActorRef, src: NtuContainer): Unit

  def StopNtuBehavior(sender: ActorRef): Unit

  def HandleNtuRequest(sender: ActorRef, min: Float, max: Float): Unit

  def HandleNtuGrant(sender: ActorRef, src: NtuContainer, amount: Float): Unit
}
