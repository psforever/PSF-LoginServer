package net.psforever.persistence

import net.psforever.objects.avatar
import net.psforever.objects.avatar.Cosmetic
import org.joda.time.LocalDateTime
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire}

case class Avatar(
    id: Int,
    name: String,
    accountId: Int,
    factionId: Int,
    genderId: Int,
    headId: Int,
    voiceId: Int,
    bep: Long = 0,
    cep: Long = 0,
    cosmetics: Option[Int] = None,
    created: LocalDateTime = LocalDateTime.now(),
    lastLogin: LocalDateTime = LocalDateTime.now(),
    lastModified: LocalDateTime = LocalDateTime.now(),
    deleted: Boolean = false
) {

  def toAvatar: avatar.Avatar =
    avatar.Avatar(
      id,
      name,
      PlanetSideEmpire(factionId),
      CharacterGender(genderId),
      headId,
      CharacterVoice(voiceId),
      bep,
      cep,
      cosmetics = cosmetics.map(c => Cosmetic.valuesFromObjectCreateValue(c))
    )
}
