// Copyright (c) 2022 PSForever
package net.psforever.objects.serverobject.terminals.tabs

import akka.actor.ActorRef
import net.psforever.objects.Player
import net.psforever.objects.avatar.Certification
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.ItemTransactionMessage

/**
  * The tab used to select a certification to be utilized by the player.
  * Only certifications may be returned to the interface defined by this page.
  * @see `CertificationType`
  * @param stock the key is always a `String` value as defined from `ItemTransationMessage` data;
  *              the value is a `CertificationType` value
  */
final case class CertificationPage(stock: Seq[Certification]) extends Tab {
  override def Buy(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    stock.find(_.name == msg.item_name) match {
      case Some(cert: Certification) =>
        Terminal.LearnCertification(cert)
      case _ =>
        Terminal.NoDeal()
    }
  }

  override def Sell(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    stock.find(_.name == msg.item_name) match {
      case Some(cert: Certification) =>
        Terminal.SellCertification(cert)
      case None =>
        Terminal.NoDeal()
    }
  }

  def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    sender ! msg
  }
}
