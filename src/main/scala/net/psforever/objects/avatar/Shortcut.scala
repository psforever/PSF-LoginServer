// Copyright (c) 2022 PSForever
package net.psforever.objects.avatar

import net.psforever.packet.game.{Shortcut => GameShortcut}

/**
 * The internal respresentation of a shortcut on the hotbar.
 * @param purpose integer value related to the type of shortcut
 * @param tile details how the shortcut is to be used (net.psforever.packet.game.Schortcut)
 * @param effect1 three letters emblazoned on the shortcut icon;
 *                defaults to empty string
 * @param effect2 the message published to text chat;
 *                defaults to empty string
 */
case class Shortcut(
                     purpose: Int,
                     tile: String,
                     effect1: String = "",
                     effect2: String = ""
                   )

object Shortcut {
  /**
   * Transform the internal form of the `Shortcut`
   * into the packet form of the `Shortcut`.
   * @see `net.psforever.packet.game.Shortcut`
   * @param shortcut internal form of the `Shortcut`
   * @return equivalent packet form of the `Shortcut`
   * @throws `AssertionError` if an implant is not named
   */
  def convert(shortcut: Shortcut): GameShortcut = {
    shortcut.tile match {
      case "medkit"         => GameShortcut.Medkit
      case "shortcut_macro" => GameShortcut.Macro(shortcut.effect1, shortcut.effect2)
      case _                => GameShortcut.Implant(shortcut.tile)
    }
  }

  /**
   * Is an internal form of the `Shortcut` equivalent to a packet form of the `Shortcut`?
   * @param a internal form of the `Shortcut`
   * @param b packet form of the `Shortcut`
   * @return `true`, if the forms of `Shortcut` are equivalent;
   *         `false`, otherwise
   */
  def equals(a: Shortcut, b: GameShortcut): Boolean = {
    a.purpose == b.code && typeEquals(a, b)
  }
  /**
   * Is an internal form of the `Shortcut` equivalent to a packet form of the `Shortcut`?
   * @param a internal form of the `Shortcut`
   * @param b packet form of the `Shortcut`
   * @return `true`, if the forms of `Shortcut` are equivalent;
   *         `false`, otherwise
   */
  def equals(b: GameShortcut, a: Shortcut): Boolean = {
    a.purpose == b.code && typeEquals(a, b)
  }

  /**
   * Is an internal form of the `Shortcut` equivalent to a packet form of the `Shortcut`?
   * Test against individual types of packet forms and then the fields associated with that form.
   * @param a internal form of the `Shortcut`
   * @param b packet form of the `Shortcut`
   * @return `true`, if the forms of `Shortcut` are equivalent;
   *         `false`, otherwise
   */
  private def typeEquals(a: Shortcut, b: GameShortcut): Boolean = {
    b match {
      case GameShortcut.Medkit        => true
      case GameShortcut.Macro(x, y)   => x.equals(a.effect1) && y.equals(a.effect2)
      case GameShortcut.Implant(tile) => tile.equals(a.tile)
      case _                          => true
    }
  }
}
