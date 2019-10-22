// Copyright (c) 2017 PSForever
package services.chat

final case class ChatServiceMessage(forChannel : String, actionMessage : ChatAction.Action)