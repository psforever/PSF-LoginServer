// Copyright (c) 2017 PSForever
package net.psforever.types

import enumeratum.values.{IntEnum, IntEnumEntry}

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
  *
  * @see `BindPlayerMessage`
  */
sealed abstract class SpawnGroup(val value: Int) extends IntEnumEntry

object SpawnGroup extends IntEnum[SpawnGroup] {
  val values = findValues

  case object Sanctuary extends SpawnGroup(0)

  case object BoundAMS extends SpawnGroup(1)

  case object AMS extends SpawnGroup(2)

  case object Unknown3 extends SpawnGroup(3)

  case object BoundTower    extends SpawnGroup(4) // unused?
  case object BoundFacility extends SpawnGroup(5)

  case object Tower extends SpawnGroup(6)

  case object Facility extends SpawnGroup(7)

  case object Unknown8 extends SpawnGroup(8)

  case object Unknown9 extends SpawnGroup(9)

  case object Unknown10 extends SpawnGroup(10)

  case object Unknown11 extends SpawnGroup(11)

  case object WarpGate extends SpawnGroup(12)

}
