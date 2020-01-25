// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.{AmmoBoxDefinition, ProjectileDefinition, ToolDefinition}
import net.psforever.objects.equipment._
import net.psforever.objects.ballistics.Projectiles

import scala.annotation.tailrec

/**
  * A type of `Equipment` that can be wielded and loaded with certain other game elements.<br>
  * <br>
  * "Tool" is a very mechanical name while this class is intended for various weapons and support items.
  * The primary trait of a `Tool` is that it has something that counts as an "ammunition,"
  * depleted as the `Tool` is used, replaceable as long as one has an appropriate type of `AmmoBox` object.
  * (The former is always called "consuming;" the latter, "reloading.")
  * Some weapons Chainblade have ammunition but do not consume it.
  * @param toolDef the `ObjectDefinition` that constructs this item and maintains some of its immutable fields
  */
class Tool(private val toolDef : ToolDefinition) extends Equipment
  with FireModeSwitch[FireModeDefinition]
  with JammableUnit {
  /** index of the current fire mode on the `ToolDefinition`'s list of fire modes */
  private var fireModeIndex : Int = toolDef.DefaultFireModeIndex
  /** current ammunition slot being used by this fire mode */
  private var ammoSlots : List[Tool.FireModeSlot] = List.empty
  var lastDischarge : Long = 0

  Tool.LoadDefinition(this)

  def FireModeIndex : Int = fireModeIndex

  def FireModeIndex_=(index : Int) : Int = {
    fireModeIndex = index % Definition.FireModes.length
    FireModeIndex
  }

  def FireMode : FireModeDefinition = Definition.FireModes(fireModeIndex)

  def NextFireMode : FireModeDefinition = {
    FireModeIndex = Definition.NextFireModeIndex(FireModeIndex)
    AmmoSlot.Chamber = FireMode.Chamber
    FireMode
  }

  def ToFireMode : Int = Definition.NextFireModeIndex(FireModeIndex)

  def ToFireMode_=(index : Int) : FireModeDefinition = {
    FireModeIndex = index
    AmmoSlot.Chamber = FireMode.Chamber
    FireMode
  }

  def AmmoTypeIndex : Int = FireMode.AmmoTypeIndices(AmmoSlot.AmmoTypeIndex)

  def AmmoTypeIndex_=(index : Int) : Int = {
    AmmoSlot.AmmoTypeIndex = index % FireMode.AmmoTypeIndices.length
    AmmoTypeIndex
  }

  def AmmoType : Ammo.Value = Definition.AmmoTypes(AmmoTypeIndex).AmmoType

  def NextAmmoType : Ammo.Value = {
    AmmoSlot.AmmoTypeIndex = AmmoSlot.AmmoTypeIndex + 1
    AmmoType
  }

  def Projectile : ProjectileDefinition = {
    Definition.ProjectileTypes({
      val projIndices = FireMode.ProjectileTypeIndices
      if(projIndices.isEmpty) {
        AmmoTypeIndex //e.g., bullet_9mm -> bullet_9mm_projectile, bullet_9mm_AP -> bullet_9mm_AP_projectile
      }
      else {
        projIndices(AmmoSlot.AmmoTypeIndex) //e.g., pulsar: f.mode1 -> pulsar_projectile, f.mode2 = pulsar_ap_projectile
      }
    })
  }

  def ProjectileType : Projectiles.Value = Projectile.ProjectileType

  def Magazine : Int = AmmoSlot.Magazine

  def Magazine_=(mag : Int) : Int = {
    //AmmoSlot.Magazine = Math.min(Math.max(0, mag), MaxMagazine)
    AmmoSlot.Magazine = Math.max(0, mag)
    Magazine
  }

  def MaxMagazine : Int = {
    val fmode = FireMode
    fmode.CustomMagazine.get(AmmoType) match {
      case Some(magSize) =>
        magSize
      case None =>
        fmode.Magazine
    }
  }

  def Discharge : Int = {
    lastDischarge = System.nanoTime()
    Magazine = FireMode.Discharge(this)
  }

  def LastDischarge : Long = {
    lastDischarge
  }

  def AmmoSlot : Tool.FireModeSlot = ammoSlots(FireMode.AmmoSlotIndex)

  def AmmoSlots : List[Tool.FireModeSlot] = ammoSlots

  def MaxAmmoSlot : Int = ammoSlots.length

  def Definition : ToolDefinition = toolDef

  override def toString : String = Tool.toString(this)
}

//AmmoType = Definition.AmmoTypes( (Definition.FireModes(fireModeIndex)).AmmoTypeIndices( (ammoSlots((Definition.FireModes(fireModeIndex)).AmmoSlotIndex)).AmmoTypeIndex) ).AmmoType

object Tool {
  def apply(toolDef : ToolDefinition) : Tool = {
    new Tool(toolDef)
  }

  /**
    * Use the `*Definition` that was provided to this object to initialize its fields and settings.
    * @param tool the `Tool` being initialized
    */
  def LoadDefinition(tool : Tool) : Unit = {
    val tdef : ToolDefinition = tool.Definition
    val maxSlot = tdef.FireModes.maxBy(fmode => fmode.AmmoSlotIndex).AmmoSlotIndex
    tool.ammoSlots = buildFireModes(tdef, (0 to maxSlot).iterator, tdef.FireModes.toList)
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

    def Tool : ToolDefinition = tdef

    def Definition : FireModeDefinition = fdef
  }
}
