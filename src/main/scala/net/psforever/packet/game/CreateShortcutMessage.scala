// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{ImplantType, PlanetSideGUID}
import scodec.{Codec, TransformSyntax}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Details regarding this shortcut.<br>
  * <br>
  * The parameters `purpose` and `tile` are closely related.
  * These two fields are consistent for all shortcuts of the same type.
  * `purpose` indicates the purpose of the shortcut.
  * The medkit icon is 0, chat shortcuts are 1, and implants are 2.
  * `tile` is related to what kind of graphic is displayed in this shortcut's slot on the hotbar based on its purpose.
  * The medkit tile use "medkit", chat shortcuts use "shortcut_macro", and implants are the internal name of the implant.<br>
  * <br>
  * The parameters `effect1` and `effect2` are exclusive to text macro shortcuts and are defaulted to empty `String`s.
  * The `shortcut_macro` setting displays a word bubble superimposed by the (first three letters of) `effect1` text.<br>
  * Implants and the medkit should have self-explanatory graphics.
  * The implant second wind does not have a graphic shortcut icon.
  * @param code the primary use of this shortcut
  */
abstract class Shortcut(val code: Int) {
  def tile: String
}

/**
  * Facilitate a quick-use button for the hotbar.<br>
  * <br>
  * The hotbar is the eight quick-use slots along the bottom center of the HUD.
  * Each of these slots is the application of a medkit, or use of an implant, or repetition of a text macro.
  * There are actually sixty-four of these slots, eight bound to the Function keys depending on which set is selected.<br>
  * <br>
  * When `addShortcut` is `true`, the provided `Shortcut` will be defined and attached to the respective hotbar slot indicated by `slot`.
  * If it is `false`, the given `slot` will be unbound.
  * Nothing happens if the `slot` selection is invalid.<br>
  * <br>
  * This packet coordinates the shortcut both as an upstream and as a downstream packet, leaning heavily towards the latter.
  * An interesting application is that, if the user does not already have a medkit or a medkit shortcut;
  * but, if he places a medkit in his inventory, the shortcut will be automatically added to his hotbar.
  * This, in turn, dispatches a packet informing the server.
  * The prior functionality will rarely be appreciated, however, as players rarely never have their medkit shortcut unbound.
  * @param player_guid the player
  * @param slot the hotbar slot number (one-indexed)
  * @param shortcut optional; details about the shortcut to be created
  * @see `ChangeShortcutBankMessage`
  */
final case class CreateShortcutMessage(
    player_guid: PlanetSideGUID,
    slot: Int,
    shortcut: Option[Shortcut]
) extends PlanetSideGamePacket {
  type Packet = CreateShortcutMessage
  def opcode = GamePacketOpcode.CreateShortcutMessage
  def encode = CreateShortcutMessage.encode(this)
}

object Shortcut extends Marshallable[Shortcut] {
  /** Preset for the medkit quick-use option. */
  case object Medkit extends Shortcut(code=0) {
    def tile = "medkit"
  }

  /**
   * Converter for text macro parameters that acts like a preset.
   * @param acronym a three letter acronym displayed in the hotbar
   * @param msg the chat message content
   * @return `Some` shortcut that represents a voice macro command
   */
  final case class Macro(acronym: String, msg: String) extends Shortcut(code=1) {
    override val tile: String = "shortcut_macro"
  }

  /**
   * Converter for an implant name token that acts like a preset.
   * @param tile implant name
   * @return `Some` shortcut that represents an implant
   * @throws `AssertionError` if an implant is not named
   */
  final case class Implant(tile: String) extends Shortcut(code=2) {
    assert(ImplantType.names.exists(_.equals(tile)), s"not an implant - $tile")
  }

  /**
   * Main transcoder for medkit shortcuts.
   */
  val medkitCodec: Codec[Shortcut] = (
    ("tile" | PacketHelpers.encodedStringAligned(adjustment=5)) ::
      ("effect1" | PacketHelpers.encodedWideString) ::
      ("effect2" | PacketHelpers.encodedWideString)
    ).xmap[Shortcut](
    _ => Medkit,
    {
      case Medkit => "medkit" :: "" :: "" :: HNil
    }
  )

  /**
   * Main transcoder for text chat macro shortcuts.
   * All strings transcoders are utilized.
   */
  val macroCodec: Codec[Macro] = (
    ("tile" | PacketHelpers.encodedStringAligned(adjustment=5)) ::
      ("effect1" | PacketHelpers.encodedWideString) ::
      ("effect2" | PacketHelpers.encodedWideString)
    ).xmap[Macro](
    {
      case _ :: acronym :: msg :: HNil => Macro(acronym, msg)
    },
    {
      case Macro(acronym, msg) => "shortcut_macro" :: acronym :: msg :: HNil
    }
  )

  /**
   * Main transcoder for implant quick-use shortcuts.
   */
  val implantCodec: Codec[Implant] = (
    ("tile" | PacketHelpers.encodedStringAligned(adjustment=5)) ::
      ("effect1" | PacketHelpers.encodedWideString) ::
      ("effect2" | PacketHelpers.encodedWideString)
    ).xmap[Implant](
    {
      case implant :: _ :: _ :: HNil => Implant(implant)
    },
    {
      case Implant(implant) => implant :: "" :: "" :: HNil
    }
  )

  /**
   * Convert the numeric flag for a specific kind of shortcut into the transcoder for that kind of shortcut.
   * @param code numeric code for shortcut type
   * @return transcoder for that shortcut type
   * @throws IllegalArgumentException if the numeric code does not map to any valid transcoders
   */
  def shortcutSwitch(code: Int): Codec[Shortcut] = {
    (code match {
      case 0 => medkitCodec
      case 1 => macroCodec
      case 2 => implantCodec
      case _ => throw new IllegalArgumentException(s"code not associated with shortcut type - $code")
    }).asInstanceOf[Codec[Shortcut]]
  }

  implicit val codec: Codec[Shortcut] = (
    uint(bits=2) >>:~ { code =>
      shortcutSwitch(code).hlist
    }).xmap[Shortcut](
    {
      case _ :: shortcut :: HNil => shortcut
    },
    {
      shortcut => shortcut.code :: shortcut :: HNil
    }
  )
}

object CreateShortcutMessage extends Marshallable[CreateShortcutMessage] {
  implicit val codec: Codec[CreateShortcutMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("slot" | uint16L) ::
      ("shortcut" | optional(bool, Shortcut.codec))
  ).as[CreateShortcutMessage]
}
