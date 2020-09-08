// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.key

import net.psforever.objects.guid.AvailabilityPolicy

/**
  * An unmodifiable reference to an active number monitor object (`Key`).
  * @param guid the number (globally unique identifier)
  * @param key a reference to the monitor
  */
final class SecureKey(private val guid: Int, private val key: Monitor) {
  def GUID: Int = guid

  def Policy: AvailabilityPolicy.Value = key.policy

  import net.psforever.objects.entity.IdentifiableEntity
  def Object: Option[IdentifiableEntity] = key.obj
}
