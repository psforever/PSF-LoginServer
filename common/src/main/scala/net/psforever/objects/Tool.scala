// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.{AmmoBoxDefinition, ToolDefinition}
import net.psforever.objects.equipment.{Ammo, Equipment, FireModeDefinition, FireModeSwitch}
import net.psforever.packet.game.PlanetSideGUID

import scala.annotation.tailrec

/**
  * A type of utility that can be wielded and loaded with certain other game elements.<br>
  * <br>
  * "Tool" is a very mechanical name while this class is intended for various weapons and support items.
  * The primary trait of a `Tool` is that it has something that counts as an "ammunition,"
  * depleted as the `Tool` is used, replaceable as long as one has an appropriate type of `AmmoBox` object.
  * (The former is always called "consuming;" the latter, "reloading.")<br>
  * <br>
  * Some weapons Chainblade have ammunition but do not consume it.
  * @param toolDef the `ObjectDefinition` that constructs this item and maintains some of its immutable fields
  */
class Tool(private val toolDef : ToolDefinition) extends Equipment with FireModeSwitch[FireModeDefinition] {
  private var fireModeIndex : Int = 0
  private val ammoSlot : List[Tool.FireModeSlot] = Tool.LoadDefinition(this)

  def FireModeIndex : Int = fireModeIndex

  def FireModeIndex_=(index : Int) : Int = {
    fireModeIndex = index % toolDef.FireModes.length
    FireModeIndex
  }

  def FireMode : FireModeDefinition = toolDef.FireModes(fireModeIndex)

  def NextFireMode : FireModeDefinition = {
    FireModeIndex = FireModeIndex + 1
    FireMode
  }

  def AmmoTypeIndex : Int = ammoSlot(fireModeIndex).AmmoTypeIndex

  def AmmoTypeIndex_=(index : Int) : Int = {
    ammoSlot(fireModeIndex).AmmoTypeIndex = index % FireMode.AmmoTypeIndices.length
    AmmoTypeIndex
  }

  def AmmoType : Ammo.Value = toolDef.AmmoTypes(AmmoTypeIndex)

  def NextAmmoType : Ammo.Value = {
    AmmoTypeIndex = AmmoTypeIndex + 1
    AmmoType
  }

  def Magazine : Int = ammoSlot(fireModeIndex).Magazine

  def Magazine_=(mag : Int) : Int = {
    ammoSlot(fireModeIndex).Magazine = Math.min(Math.max(0, mag), MaxMagazine)
    Magazine
  }

  def MaxMagazine : Int = FireMode.Magazine

  def NextDischarge : Int = math.min(Magazine, FireMode.Chamber)

  def AmmoSlots : List[Tool.FireModeSlot] = ammoSlot

  def MaxAmmoSlot : Int = ammoSlot.length

  def Definition : ToolDefinition = toolDef

  override def toString : String = {
    Tool.toString(this)
  }
}

object Tool {
  def apply(toolDef : ToolDefinition) : Tool = {
    new Tool(toolDef)
  }

  def apply(guid : PlanetSideGUID, toolDef : ToolDefinition) : Tool = {
    val obj = new Tool(toolDef)
    obj.GUID = guid
    obj
  }

  /**
    * Use the `*Definition` that was provided to this object to initialize its fields and settings.
    * @param tool the `Tool` being initialized
    */
  def LoadDefinition(tool : Tool) : List[FireModeSlot] = {
    val tdef : ToolDefinition = tool.Definition
    val maxSlot = tdef.FireModes.maxBy(fmode => fmode.AmmoSlotIndex).AmmoSlotIndex
    buildFireModes(tool, (0 to maxSlot).iterator, tdef.FireModes.toList)
  }

  @tailrec private def buildFireModes(tool : Tool, iter : Iterator[Int], fmodes : List[FireModeDefinition], list : List[FireModeSlot] = Nil) : List[FireModeSlot] = {
    if(!iter.hasNext) {
      list
    }
    else {
      val index = iter.next
      fmodes.filter(fmode => fmode.AmmoSlotIndex == index) match {
        case fmode :: _ =>
          buildFireModes(tool, iter, fmodes, list :+ new FireModeSlot(tool, fmode))
        case Nil =>
          throw new IllegalArgumentException(s"tool ${tool.Definition.Name} ammo slot #$index is missing a fire mode specification; do not skip")
      }
    }
  }

  def toString(obj : Tool) : String = {
    s"${obj.Definition.Name} (mode=${obj.FireModeIndex}-${obj.AmmoType})(${obj.Magazine}/${obj.MaxMagazine})"
  }

  /**
    * A hidden class that manages the specifics of the given ammunition for the current fire mode of this tool.
    * It operates much closer to an "ammunition feed" rather than a fire mode.
    * The relationship to fire modes is at least one-to-one and at most one-to-many.
    */
  class FireModeSlot(private val tool : Tool, private val fdef : FireModeDefinition) {
    /*
    By way of demonstration:
    Suppressors have one fire mode, two types of ammunition, one slot (2)
    MA Pistols have two fire modes, one type of ammunition, one slot (1)
    Jackhammers have two fire modes, two types of ammunition, one slot (2)
    Punishers have two fire modes, five types of ammunition, two slots (2, 3)
     */

    /** if this fire mode has multiple types of ammunition */
    private var ammoTypeIndex : Int = fdef.AmmoTypeIndices.head
    /** a reference to the actual `AmmoBox` of this slot; will not synch up with `AmmoType` immediately */
    private var box : AmmoBox = AmmoBox(AmmoBoxDefinition(AmmoType)) //defaults to box of one round of the default type for this slot

    def AmmoTypeIndex : Int = ammoTypeIndex

    def AmmoTypeIndex_=(index : Int) : Int =  {
      ammoTypeIndex = index
      AmmoTypeIndex
    }

    def AmmoType : Ammo.Value = tool.Definition.AmmoTypes(ammoTypeIndex)

    def Magazine : Int = box.Capacity

    def Magazine_=(mag : Int) : Int = {
      box.Capacity = mag
      Magazine
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

    def Tool : Tool = tool

    def Definition : FireModeDefinition = fdef
  }
}
