// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package objects

import net.psforever.objects.serverobject.structures.BaseVirus
import net.psforever.packet.game.packets.HackState7
import org.specs2.mutable.Specification

class BaseVirusTest extends Specification {
  "BaseVirus" should {
    "have correct values and properties" in {
      BaseVirus.UnlockDoors.value mustEqual 0
      BaseVirus.UnlockDoors.durationSeconds mustEqual 3600
      BaseVirus.UnlockDoors.hackState mustEqual HackState7.UnlockDoors
      BaseVirus.UnlockDoors.actionString mustEqual "virus_unlock"

      BaseVirus.DisableLatticeBenefits.value mustEqual 1
      BaseVirus.DisableLatticeBenefits.durationSeconds mustEqual 900
      BaseVirus.DisableLatticeBenefits.hackState mustEqual HackState7.DisableLatticeBenefits
      BaseVirus.DisableLatticeBenefits.actionString mustEqual "virus_module"

      BaseVirus.NTUDrain.value mustEqual 2
      BaseVirus.NTUDrain.durationSeconds mustEqual 3600
      BaseVirus.NTUDrain.hackState mustEqual HackState7.NTUDrain
      BaseVirus.NTUDrain.actionString mustEqual "virus_resource"

      BaseVirus.DisableRadar.value mustEqual 3
      BaseVirus.DisableRadar.durationSeconds mustEqual 900
      BaseVirus.DisableRadar.hackState mustEqual HackState7.DisableRadar
      BaseVirus.DisableRadar.actionString mustEqual "virus_radar"

      BaseVirus.AccessEquipmentTerms.value mustEqual 4
      BaseVirus.AccessEquipmentTerms.durationSeconds mustEqual 120
      BaseVirus.AccessEquipmentTerms.hackState mustEqual HackState7.AccessEquipmentTerms
      BaseVirus.AccessEquipmentTerms.actionString mustEqual "virus_equipment"

      BaseVirus.NoVirus.value mustEqual 8
      BaseVirus.NoVirus.durationSeconds mustEqual 0
      BaseVirus.NoVirus.hackState mustEqual HackState7.Unk8
      BaseVirus.NoVirus.actionString mustEqual "none"
    }

    "look up virus by Long ID" in {
      BaseVirus(0L) must beSome(BaseVirus.UnlockDoors: BaseVirus)
      BaseVirus(1L) must beSome(BaseVirus.DisableLatticeBenefits: BaseVirus)
      BaseVirus(2L) must beSome(BaseVirus.NTUDrain: BaseVirus)
      BaseVirus(3L) must beSome(BaseVirus.DisableRadar: BaseVirus)
      BaseVirus(4L) must beSome(BaseVirus.AccessEquipmentTerms: BaseVirus)
      BaseVirus(8L) must beSome(BaseVirus.NoVirus: BaseVirus)
      BaseVirus(99L) must beNone
      BaseVirus(0xFFFFFFFFL) must beNone
    }
  }
}
