// Copyright (c) 2017 PSForever
package services.local

final case class LocalServiceMessage(forChannel : String, actionMessage : LocalAction.Action)

object LocalServiceMessage {
  final case class Deployables(msg : Any)

  final case class Telepads(msg : Any)
}
