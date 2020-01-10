// Copyright (c) 2017 PSForever
package services.account

import scala.collection.mutable
import akka.actor.Actor
import net.psforever.objects.Account

/**
  * This actor is essentially a bridge between the LoginSessionActor and WorldSessionActor. When
  * a player connects, the LoginSessionActor handles the account login by querying the database,
  * comparing passwords etc, and then by inserting the account information into this actor by a
  * token that is also sent to the player's client in the LoginRespMessage packet. The token is
  * just like a browser cookie session token.
  *
  * When the player's client has processed the LoginRespMessage packet, it attempts to connect
  * to the game world by sending a ConnectToWorldRequestMessage with the token which is used
  * by the WorldSessionActor to lookup the player's account info from this actor.
  *
  * So this actor is a temporary store of account data for players logging into the login
  * server and crossing over to the world server.
  */

class AccountIntermediaryService extends Actor {
  private val accountsByToken = mutable.Map[String, Account]()
  private val IPAddressBySessionID = mutable.Map[Long, IPAddress]()
  private [this] val log = org.log4s.getLogger

  override def preStart = {
    log.trace("Starting...")
  }

  def receive = {
    // Called by the LoginSessionActor
    case StoreAccountData(token, account) =>
      accountsByToken += (token -> account)
      log.info(s"Storing intermediary account data for ${account.AccountId}")

    // Called by the WorldSessionActor
    case RetrieveAccountData(token) =>
      val account : Option[Account] = accountsByToken.remove(token)
      if(account.nonEmpty) {
        sender() ! ReceiveAccountData(account.get)
        log.info(s"Retrieving intermediary account data for ${account.get.AccountId}")
      } else {
        log.error(s"Unable to retrieve intermediary account data for ${account.get.AccountId}")
      }

    case StoreIPAddress(sessionID, address) =>
      IPAddressBySessionID += (sessionID -> address)
      log.info(s"Storing IP address (${address.Address}) for sessionID : $sessionID")

    case RetrieveIPAddress(sessionID) =>
      val address : Option[IPAddress] = IPAddressBySessionID.remove(sessionID)
      if(address.nonEmpty) {
        sender() ! ReceiveIPAddress(address.get)
        log.info(s"Retrieving IP address data for sessionID : ${sessionID}")
      } else {
        log.error(s"Unable to retrieve IP address data for sessionID : ${sessionID}")
      }

    case msg =>
      log.warn(s"Unhandled message $msg from $sender")
  }
}
