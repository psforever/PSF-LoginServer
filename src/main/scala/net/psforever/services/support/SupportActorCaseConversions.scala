// Copyright (c) 2017 PSForever
package net.psforever.services.support

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.zones.Zone

trait SupportActorCaseConversions {

  /**
    * A mask for converting between a class local and `SupportActor.HurrySpecific`.
    *
    * @param targets a list of objects to match
    * @param zone    the zone in which these objects exist
    * @return a `SupportActor.HurrySpecific` object
    */
  def HurrySpecific(targets: List[PlanetSideGameObject], zone: Zone): SupportActor.HurrySpecific =
    SupportActor.HurrySpecific(targets, zone)

  /**
    * A mask for converting between a class local and `SupportActor.HurryAll`.
    * @return a `SupportActor.HurryAll` object
    */
  def HurryAll(): SupportActor.HurryAll = SupportActor.HurryAll()

  /**
    * A mask for converting between a class local and `SupportActor.ClearSpecific`.
    * @param targets a list of objects to match
    * @param zone the zone in which these objects exist
    * @return a `SupportActor.ClearSpecific` object
    */
  def ClearSpecific(targets: List[PlanetSideGameObject], zone: Zone): SupportActor.ClearSpecific =
    SupportActor.ClearSpecific(targets, zone)

  /**
    * A mask for converting between a class local and `SupportActor.ClearAll`.
    * @return a `SupportActor.ClearAll` object
    */
  def ClearAll(): SupportActor.ClearAll = SupportActor.ClearAll()
}
