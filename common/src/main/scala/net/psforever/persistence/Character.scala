package net.psforever.persistence

import org.joda.time.LocalDateTime
import net.psforever.objects.Avatar
import net.psforever.types.{PlanetSideEmpire, CharacterGender, CharacterVoice}

case class Character(
    id: Int,
    name: String,
    accountId: Int,
    factionId: Int,
    genderId: Int,
    headId: Int,
    voiceId: Int,
    created: LocalDateTime = LocalDateTime.now(),
    lastLogin: LocalDateTime = LocalDateTime.now(),
    lastModified: LocalDateTime = LocalDateTime.now(),
    deleted: Boolean = false
) {

  def toAvatar(): Avatar =
    new Avatar(id, name, PlanetSideEmpire(factionId), CharacterGender(genderId), headId, CharacterVoice(voiceId))
}
