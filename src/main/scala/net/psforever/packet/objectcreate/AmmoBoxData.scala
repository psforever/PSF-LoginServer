// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.types.PlanetSideGUID

/**
  * A representation of ammunition that can be created using `ObjectCreateMessage` packet data.
  * This data will help construct a "box" of that type of ammunition when standalone.
  * It can also be constructed directly inside a weapon as its magazine.<br>
  * <br>
  * This ammunition object ompletely ignores the capacity field, normal to detailed ammunition objects.
  * Creating an object of this type directly and picking it up or observing it (in a weapon) will reveals single round.
  * @see `DetailedAmmoBoxData`
  */
object AmmoBoxData {

  /**
    * An abbreviated constructor for creating `AmmoBoxData` while masking use of `InternalSlot`.
    * @param cls the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param parentSlot a parent-defined slot identifier that explains where the child is to be attached to the parent
    * @param ammo the ammunition object
    * @return an `InternalSlot` object that encapsulates `CommonFieldData`
    */
  def apply(cls: Int, guid: PlanetSideGUID, parentSlot: Int, ammo: CommonFieldData): InternalSlot =
    new InternalSlot(cls, guid, parentSlot, ammo)
}
