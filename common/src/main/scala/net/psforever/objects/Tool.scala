// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.{AmmoBoxDefinition, ProjectileDefinition, ToolDefinition}
import net.psforever.objects.equipment._

import scala.annotation.tailrec

/**
  * A type of utility that can be wielded and loaded with certain other game elements.<br>
  * <br>
  * "Tool" is a very mechanical name while this class is intended for various weapons and support items.
  * The primary trait of a `Tool` is that it has something that counts as an "ammunition,"
  * depleted as the `Tool` is used, replaceable as long as one has an appropriate type of `AmmoBox` object.
  * (The former is always called "consuming;" the latter, "reloading.")
  * Some weapons Chainblade have ammunition but do not consume it.
  * @param toolDef the `ObjectDefinition` that constructs this item and maintains some of its immutable fields
  */
class Tool(private val toolDef : ToolDefinition) extends Equipment with FireModeSwitch[FireModeDefinition] {
  /** index of the current fire mode on the `ToolDefinition`'s list of fire modes */
  private var fireModeIndex : Int = 0
  /** current ammunition slot being used by this fire mode */
  private val ammoSlots : List[Tool.FireModeSlot] = Tool.LoadDefinition(this)
//  private val projectileSlots : List[Tool.FireModeSlot] = Tool.LoadDefinition(this)

  def FireModeIndex : Int = fireModeIndex

  def FireModeIndex_=(index : Int) : Int = {
    fireModeIndex = index % toolDef.FireModes.length
    FireModeIndex
  }

  def FireMode : FireModeDefinition = toolDef.FireModes(fireModeIndex)

  def NextFireMode : FireModeDefinition = {
    FireModeIndex = FireModeIndex + 1
    AmmoSlot.Chamber = FireMode.Chamber
    FireMode
  }

  def AmmoTypeIndex : Int = FireMode.AmmoTypeIndices(AmmoSlot.AmmoTypeIndex)

  def AmmoTypeIndex_=(index : Int) : Int = {
    AmmoSlot.AmmoTypeIndex = index % FireMode.AmmoTypeIndices.length
    AmmoTypeIndex
  }

  def AmmoType : Ammo.Value = toolDef.AmmoTypes(AmmoTypeIndex).AmmoType

  def NextAmmoType : Ammo.Value = {
    AmmoSlot.AmmoTypeIndex = AmmoSlot.AmmoTypeIndex + 1
    AmmoType
  }

  def ProjectileType : Projectiles.Value = toolDef.ProjectileTypes(AmmoTypeIndex).ProjectileType

  def Damage0 : Int = toolDef.ProjectileTypes(AmmoTypeIndex).Damage0
  def Damage1 : Int = if (toolDef.ProjectileTypes(AmmoTypeIndex).Damage1 == -1) {Damage0} else {toolDef.ProjectileTypes(AmmoTypeIndex).Damage1}
  def Damage2 : Int = if (toolDef.ProjectileTypes(AmmoTypeIndex).Damage2 == -1) {Damage1} else {toolDef.ProjectileTypes(AmmoTypeIndex).Damage2}
  def Damage3 : Int = if (toolDef.ProjectileTypes(AmmoTypeIndex).Damage3 == -1) {Damage2} else {toolDef.ProjectileTypes(AmmoTypeIndex).Damage3}
  def Damage4 : Int = if (toolDef.ProjectileTypes(AmmoTypeIndex).Damage4 == -1) {Damage3} else {toolDef.ProjectileTypes(AmmoTypeIndex).Damage4}

  def DamageAtEdge : Float = toolDef.ProjectileTypes(AmmoTypeIndex).DamageAtEdge

  def DamageRadius : Float = toolDef.ProjectileTypes(AmmoTypeIndex).DamageRadius

  def DamageType = toolDef.ProjectileTypes(AmmoTypeIndex).ProjectileDamageType

  def DegradeDelay : Float = toolDef.ProjectileTypes(AmmoTypeIndex).DegradeDelay
  def DegradeMultiplier : Float = toolDef.ProjectileTypes(AmmoTypeIndex).DegradeMultiplier
  def InitialVelocity : Int = toolDef.ProjectileTypes(AmmoTypeIndex).InitialVelocity
  def Lifespan : Float = toolDef.ProjectileTypes(AmmoTypeIndex).Lifespan

  def Magazine : Int = AmmoSlot.Magazine

  def Magazine_=(mag : Int) : Int = {
    AmmoSlot.Magazine = Math.min(Math.max(0, mag), MaxMagazine)
    Magazine
  }

  def MaxMagazine : Int = FireMode.Magazine

  def Discharge : Int = {
    Magazine = FireMode.Discharge(this)
  }

  def AmmoSlot : Tool.FireModeSlot = ammoSlots(FireMode.AmmoSlotIndex)

//  def ProjectileSlot : Tool.FireModeSlot = projectileSlots(FireMode.AmmoSlotIndex)

  def AmmoSlots : List[Tool.FireModeSlot] = ammoSlots

  def MaxAmmoSlot : Int = ammoSlots.length

  def Definition : ToolDefinition = toolDef

  override def toString : String = Tool.toString(this)
}

object Tool {
  def apply(toolDef : ToolDefinition) : Tool = {
    new Tool(toolDef)
  }

  /**
    * Use the `*Definition` that was provided to this object to initialize its fields and settings.
    * @param tool the `Tool` being initialized
    */
  def LoadDefinition(tool : Tool) : List[FireModeSlot] = {
    val tdef : ToolDefinition = tool.Definition
    val maxSlot = tdef.FireModes.maxBy(fmode => fmode.AmmoSlotIndex).AmmoSlotIndex
    buildFireModes(tdef, (0 to maxSlot).iterator, tdef.FireModes.toList)
  }

  @tailrec private def buildFireModes(tdef : ToolDefinition, iter : Iterator[Int], fmodes : List[FireModeDefinition], list : List[FireModeSlot] = Nil) : List[FireModeSlot] = {
    if(!iter.hasNext) {
      list
    }
    else {
      val index = iter.next
      fmodes.filter(fmode => fmode.AmmoSlotIndex == index) match {
        case fmode :: _ =>
          buildFireModes(tdef, iter, fmodes, list :+ new FireModeSlot(tdef, fmode))
        case Nil =>
          throw new IllegalArgumentException(s"tool ${tdef.Name} ammo slot #$index is missing a fire mode specification; do not skip")
      }
    }
  }

  def toString(obj : Tool) : String = {
    s"${obj.Definition.Name} (mode=${obj.FireModeIndex}-${obj.AmmoType})(${obj.Magazine}/${obj.MaxMagazine})"
  }

  /**
    * The `FireModeSlot` can be called the "magazine feed," an abstracted "ammunition slot."
    * Most weapons will have only one ammunition slot and swap different ammunition into it as needed.
    * In general to swap ammunition means to unload the onld ammunition and load the new ammunition.
    * Many weapons also have one ammunition slot and multiple fire modes using the same list of ammunition
    * This slot manages either of two ammunitions where one does not need to unload to be swapped to the other;
    * however, the fire mod has most likely been changed.
    * The Punisher -
    * six ammunition types in total,
    * two uniquely different types without unloading,
    * two exclusive groups of ammunition divided into 2 cycled types and 4 cycled types -
    * is an example of a weapon that benefits from this implementation.
    */
  class FireModeSlot(private val tdef : ToolDefinition, private val fdef : FireModeDefinition) {
    /**
      * if this fire mode has multiple types of ammunition
      * this is the index of the fire mode's ammo List, not a reference to the tool's ammo List
      */
    private var ammoTypeIndex : Int = 0
    /** a reference to the actual `AmmoBox` of this slot */
    private var box : AmmoBox = AmmoBox(AmmoDefinition, fdef.Magazine)
    private var chamber = fdef.Chamber

    def AmmoTypeIndex : Int = ammoTypeIndex

    def AmmoTypeIndex_=(index : Int) : Int =  {
      ammoTypeIndex = index % fdef.AmmoTypeIndices.length
      AmmoTypeIndex
    }

    private def AmmoDefinition : AmmoBoxDefinition = {
      tdef.AmmoTypes(fdef.AmmoTypeIndices(ammoTypeIndex))
    }

//    private def ProjectileDefinition : ProjectileDefinition = {
//      tdef.ProjectileTypes(fdef.AmmoTypeIndices(ammoTypeIndex))
//    }

    /**
      * This is a reference to the `Ammo.Value` whose `AmmoBoxDefinition` should be loaded into `box`.
      * It may not be the correct `Ammo.Value` whose `AmmoBoxDefinition` is loaded into `box` such as is the case during ammunition swaps.
      * Generally, convert from this index, to the index in the fire mode's ammunition list, to the index in the `ToolDefinition`'s ammunition list.
      * @return the `Ammo` type that should be loaded into the magazine right now
      */
    def AmmoType : Ammo.Value = AmmoDefinition.AmmoType

    def AllAmmoTypes : List[Ammo.Value] = {
      fdef.AmmoTypeIndices.map(index => tdef.AmmoTypes(fdef.AmmoTypeIndices(index)).AmmoType).toList
    }

    def Magazine : Int = box.Capacity

    def Magazine_=(mag : Int) : Int = {
      box.Capacity = mag
      Magazine
    }

    def Chamber : Int = chamber

    def Chamber_=(chmbr : Int) : Int =  {
      chamber = math.min(math.max(0, chmbr), fdef.Chamber)
      Chamber
    }

    def Box : AmmoBox = box

    def Box_=(toBox : AmmoBox) : Option[AmmoBox] = {
      if(toBox.AmmoType == AmmoType) {
        box = toBox
        Some(Box)
      }
      else {
        None
      }
    }

//    def Damage0 : Int = ProjectileDefinition.Damage0
//    def Damage1 : Int = if (ProjectileDefinition.Damage1==0) {Damage0} else {ProjectileDefinition.Damage1}
//    def Damage2 : Int = if (ProjectileDefinition.Damage2==0) {Damage1} else {ProjectileDefinition.Damage2}
//    def Damage3 : Int = if (ProjectileDefinition.Damage3==0) {Damage2} else {ProjectileDefinition.Damage3}
//    def Damage4 : Int = if (ProjectileDefinition.Damage4==0) {Damage3} else {ProjectileDefinition.Damage4}

//    def DamageType : DamageType.Value = ProjectileDefinition.AmmoDamageType

//    def DegradeDelay : Float = ProjectileDefinition.DegradeDelay
//    def DegradeMultiplier : Float = ProjectileDefinition.DegradeMultiplier
//    def InitialVelocity : Int = ProjectileDefinition.InitialVelocity
//    def Lifespan : Float = ProjectileDefinition.Lifespan



    def Tool : ToolDefinition = tdef

    def Definition : FireModeDefinition = fdef
  }
}
