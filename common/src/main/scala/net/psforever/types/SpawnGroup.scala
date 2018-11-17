// Copyright (c) 2017 PSForever
package net.psforever.types

/**
  * The spawn group.<br>
  * <br>
  * The groups `Sanctuary`, `Tower`, and ,`Facility` are typically hard-defined by the client.
  * The groups `AMS` and the `Bound*` spawns can only be displayed on the deployment map
  * by sending a manual `BindPlayerMessage` packet to the client,
  * and the designated spawn group identifier is returned to the server if the spawn point that is created is selected.
  * The sanctuary spawn is also used as a fallback for an unknown spawn point
  * as going back to one's own sanctuary counts as a "safe spawn."<br>
  * <br>
  * The `Sanctuary` spawn is commonly accessible on a smaller map (of the sanctuary continent)
  * off to one side of the greater deployment map.
  * It does not generate an icon when manually set.
  * The icons produced by the normal and the bound tower and facility groups are not detailed.
  * The ones that are not designated as "bound" also do not display icons when manually set.
  * The AMS spawn group icons have an overhead AMS glyph and are smaller in radius, identical otherwise.
  */
object SpawnGroup extends Enumeration {
  type Type = Value

  val
  Sanctuary,
  BoundAMS,
  AMS,
  Unknown3,
  BoundTower, //unused?
  BoundFacility,
  Tower,
  Facility,
  Unknown8,
  Unknown9,
  Unknown10,
  Unknown11,
  Unknown12
  = Value
}
