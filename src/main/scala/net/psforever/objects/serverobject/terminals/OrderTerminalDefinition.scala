// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import akka.actor.{ActorContext, ActorRef}
import net.psforever.objects.{Default, Player}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.ItemTransactionMessage
import net.psforever.objects.serverobject.terminals.tabs.Tab
import net.psforever.types.TransactionType

import scala.collection.mutable

/**
  * The definition for any `Terminal` from which specifications can be altered.
  * These specification alternations involve three classifications:
  * the exchange of denominations of in-game hardware, i.e., `Equipment`,
  * the modification of lists of personal statistics, e.g., `Certifications`,
  * and saving and loading of preset configurations, i.e., `Loadouts`.
  * This hardware is organized as "stock," occasionally supplemented.
  * Terminals have tabs (visually) that are organized by different stock (internally)
  * that determines the behavior available from that tab
  * and what stock can be drawn or returned.<br>
  * <br>
  * Equipment terminals are the property of bases and vehicles ("amenities").
  * To bases, the `Terminal` object is coupled loosely and may be allowed to diverge.
  * To vehicles, the `Terminal` object is coupled directly to the faction affiliation of the vehicle.
  * @see `Amenity`
  * @see `Terminal`
  * @see `Utility`
  */
class OrderTerminalDefinition(objId: Int) extends TerminalDefinition(objId) {

  /** An internal object organizing the different specification options found on a terminal's UI. */
  private val tabs: mutable.HashMap[Int, Tab] =
    new mutable.HashMap[Int, Tab]()

  /** Disconnect the ability to return stock back to the terminal
    * from the type of stock available from the terminal in general
    * or the type of stock available from its denoted page.
    * Will always return a message of type `SellEquipment`.
    */
  private var sellEquipmentDefault: Boolean = false

  def Tab: mutable.HashMap[Int, Tab] = tabs

  def SellEquipmentByDefault: Boolean = sellEquipmentDefault

  def SellEquipmentByDefault_=(sell: Boolean): Boolean = {
    sellEquipmentDefault = sell
    SellEquipmentByDefault
  }

  def Request(player: Player, msg: Any): Terminal.Exchange =
    msg match {
      case message: ItemTransactionMessage =>
        message.transaction_type match {
          case TransactionType.Buy | TransactionType.Learn =>
            Buy(player, message)
          case TransactionType.Loadout =>
            Loadout(player, message)
          case TransactionType.Sell =>
            Sell(player, message)
          case _ =>
            Terminal.NoDeal()
        }
      case _ =>
        Terminal.NoDeal()
    }

  private def Buy(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    tabs.get(msg.item_page) match {
      case Some(page) =>
        page.Buy(player, msg)
      case _ =>
        Terminal.NoDeal()
    }
  }

  private def Loadout(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = Buy(player, msg)

  private def Sell(player: Player, msg: ItemTransactionMessage): Terminal.Exchange = {
    if (sellEquipmentDefault) {
      Terminal.SellEquipment()
    } else {
      tabs.get(msg.item_page) match {
        case Some(page) =>
          page.Sell(player, msg)
        case _ =>
          Terminal.NoDeal()
      }
    }
  }

  override def Dispatch(sender: ActorRef, terminal: Terminal, msg: Terminal.TerminalMessage): Unit = {
    tabs.get(msg.msg.item_page) match {
      case Some(page) =>
        page.Dispatch(sender, terminal, msg)
      case _ => ;
    }
  }
}

object OrderTerminalDefinition {
  /**
    * Assemble some logic for a provided object.
    * @param obj an `Amenity` object;
    *            anticipating a `Terminal` object using this same definition
    * @param context hook to the local `Actor` system
    */
  def Setup(obj: Amenity, context: ActorContext): Unit = {
    import akka.actor.Props
    if (obj.Actor == Default.Actor) {
      obj.Actor = context.actorOf(Props(classOf[TerminalControl], obj), PlanetSideServerObject.UniqueActorName(obj))
    }
  }
}
