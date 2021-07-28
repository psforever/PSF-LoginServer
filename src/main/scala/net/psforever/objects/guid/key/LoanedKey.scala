// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.key

import net.psforever.objects.entity.IdentifiableEntity

/**
  * The only indirect public access a queued number monitor object (`Key`) is allowed.
  * @param guid the GUID represented by this indirect key
  * @param key a private reference to the original key
  */
class LoanedKey(private val guid: Int, private val key: Monitor) {
  def GUID: Int = guid

  def Policy: AvailabilityPolicy = key.policy

  def Object: Option[IdentifiableEntity] = key.obj

  /**
    * na
    * @param obj the object that should hold this GUID
    * @return `true`, if the assignment worked; `false`, otherwise
    */
  def Object_=(obj: IdentifiableEntity): Option[IdentifiableEntity] = Object_=(Some(obj))

  /**
    * na
    * @param obj the object that should hold this GUID
    * @return `true`, if the assignment worked; `false`, otherwise
    */
  def Object_=(obj: Option[IdentifiableEntity]): Option[IdentifiableEntity] = {
    if (key.policy == AvailabilityPolicy.Leased) {
      if (key.obj.isDefined) {
        key.obj.get.Invalidate()
        key.obj = None
      }
      key.obj = obj
      if (obj.isDefined) {
        import net.psforever.types.PlanetSideGUID
        obj.get.GUID = PlanetSideGUID(guid)
      }
    }
    key.obj
  }
}
