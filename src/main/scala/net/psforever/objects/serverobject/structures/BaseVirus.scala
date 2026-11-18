// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package net.psforever.objects.serverobject.structures

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.packet.game.packets.HackState7

/**
  * An `IntEnum` representing base viruses that can be installed on a facility's Main Terminal (`main_terminal`)
  * using a T-REK (`trek`).
  *
  * Corresponds to the options in `HUDUploadVirus` (`UploadVirusDialog`, `0x006EF770`) in `planetside.exe`.
  *
  * @param value           the numeric virus ID transmitted in `UseItemMessage.action_context` (0-4, or 8)
  * @param durationSeconds duration of the virus infection in seconds
  * @param hackState       the corresponding `HackState7` used in `HackMessage`
  * @param actionString    the game action / effect string identifier
  */
sealed abstract class BaseVirus(
    val value: Int,
    val durationSeconds: Int,
    val hackState: HackState7,
    val actionString: String
) extends IntEnumEntry {
  def id: Long = value.toLong
}

object BaseVirus extends IntEnum[BaseVirus] {
  val values: IndexedSeq[BaseVirus] = findValues

  /** Virus 0: Unlocks all internal/external IFF door locks in the facility */
  case object UnlockDoors extends BaseVirus(
    value = 0,
    durationSeconds = 3600,
    hackState = HackState7.UnlockDoors,
    actionString = "virus_unlock"
  )

  /** Virus 1: Disables facility lattice benefits and cavern module benefits */
  case object DisableLatticeBenefits extends BaseVirus(
    value = 1,
    durationSeconds = 900,
    hackState = HackState7.DisableLatticeBenefits,
    actionString = "virus_module"
  )

  /** Virus 2: Doubles the NTU drain rate of the facility */
  case object NTUDrain extends BaseVirus(
    value = 2,
    durationSeconds = 3600,
    hackState = HackState7.NTUDrain,
    actionString = "virus_resource"
  )

  /** Virus 3: Disables enemy radar, proximity sensors, and sensor network */
  case object DisableRadar extends BaseVirus(
    value = 3,
    durationSeconds = 900,
    hackState = HackState7.DisableRadar,
    actionString = "virus_radar"
  )

  /** Virus 4: Grants enemy players access to facility equipment terminals */
  case object AccessEquipmentTerms extends BaseVirus(
    value = 4,
    durationSeconds = 120,
    hackState = HackState7.AccessEquipmentTerms,
    actionString = "virus_equipment"
  )

  /** Special value 8: No virus installed or virus has been purged */
  case object NoVirus extends BaseVirus(
    value = 8,
    durationSeconds = 0,
    hackState = HackState7.Unk8,
    actionString = "none"
  )

  def apply(id: Long): Option[BaseVirus] = withValueOpt(id.toInt)
}
