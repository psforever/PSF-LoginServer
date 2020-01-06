// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.types.PlanetSideGUID

/**
  * A representation of an object that can be interacted with when using a variety of terminals.
  * This object is generally invisible.
  */
object TerminalData {
  /**
    * Overloaded constructor for a type of common terminal.
    * @param cls the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param parentSlot a parent-defined slot identifier that explains where the child is to be attached to the parent
    * @param terminal the `TerminalData`
    * @return an `InternalSlot` object
    */
  def apply(cls : Int, guid : PlanetSideGUID, parentSlot : Int, terminal : CommonFieldData) : InternalSlot =
    InternalSlot(cls, guid, parentSlot, terminal)
}
