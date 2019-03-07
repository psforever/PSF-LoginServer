// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

/**
  * An `Enumeration` of common equipment sizes in the game.
  * Check the comments for originating use.
  */
object EquipmentSize extends Enumeration {
  val
  Blocked,
  Melee, //special
  Pistol, //2x2 and 3x3
  Rifle, //6x3 and 9x3
  Max, //max weapon only
  VehicleWeapon, //vehicle-mounted weapons
  BaseTurretWeapon, //common phalanx cannons, and cavern turrets
  BFRArmWeapon, //duel arm weapons for bfr
  BFRGunnerWeapon, //gunner seat for bfr
  Inventory //reserved
  = Value

  /**
    * Perform custom size comparison.<br>
    * <br>
    * In almost all cases, the only time two sizes are equal is if they are the same size.
    * If either size is `Blocked`, however, they will never be equal.
    * If either size is `Inventory` or `Any`, however, they will always be equal.
    * Size comparison is important for putting `Equipment` in size-fitted slots, but not for much else.
    * @param type1 the first size
    * @param type2 the second size
    * @return `true`, if they are equal; `false`, otherwise
    */
   def isEqual(type1 : EquipmentSize.Value, type2 : EquipmentSize.Value) : Boolean = {
    if(type1 == Blocked || type2 == Blocked) {
      false
    }
    else if(type1 == Inventory || type2 == Inventory) {
      true
    }
    else {
      type1 == type2
    }
  }
}
