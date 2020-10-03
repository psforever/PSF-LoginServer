package net.psforever.actors.commands

import akka.actor.typed.ActorRef
import net.psforever.objects.NtuContainer

object NtuCommand {

  trait Command

  /** Message for announcing it has nanites it can offer the recipient.
    *
    * @param source the nanite container recognized as the sender
    */
  final case class Offer(source: NtuContainer, replyTo: ActorRef[Request]) extends Command

  /** Message for asking for nanites from the recipient.
    *
    * @param amount the amount of nanites requested
    */
  final case class Request(amount: Float, replyTo: ActorRef[Grant]) extends Command

  /** Response for transferring nanites to a recipient.
    *
    * @param source the nanite container recognized as the sender
    * @param amount the nanites transferred in this package
    */
  final case class Grant(source: NtuContainer, amount: Float) extends Command

}
