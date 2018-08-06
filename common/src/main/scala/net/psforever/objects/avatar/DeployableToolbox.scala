// Copyright (c) 2017 PSForever
package net.psforever.objects.avatar

import net.psforever.objects.{Deployable, DeployableCategory, PlanetSideGameObject}
import net.psforever.objects.equipment.CItem.DeployedItem
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.CertificationType

import scala.collection.mutable

class DeployableToolbox {
  private val categoryCounts = collection.mutable.Map(
    DeployableCategory.values.toSeq.map(value => { value -> new DeployableToolbox.Bin }) : _*
  )
  private val deployableCounts = collection.mutable.Map(
    DeployedItem.values.toSeq.map(value => { value -> new DeployableToolbox.Bin }) : _*
  )
  private val deployableLists = collection.mutable.Map(
    DeployableCategory.values.toSeq.map(value => { value -> mutable.ListBuffer[DeployableToolbox.AcceptableDeployable]() }) : _*
  )
  private var initialized : Boolean = false

  def Initialize(certifications : Set[CertificationType.Value]) : Boolean = {
    if(!initialized) {
      DeployableToolbox.Initialize(deployableCounts, categoryCounts, certifications)
      initialized = true
      true
    }
    else {
      false
    }
  }

  def AddToDeployableQuantities(certification : CertificationType.Value, certificationSet : Set[CertificationType.Value]) : Unit = {
    initialized = true
    DeployableToolbox.AddToDeployableQuantities(deployableCounts, categoryCounts, certification, certificationSet)
  }

  def RemoveFromDeployablesQuantities(certification : CertificationType.Value, certificationSet : Set[CertificationType.Value]) : Unit = {
    initialized = true
    DeployableToolbox.RemoveFromDeployablesQuantities(deployableCounts, categoryCounts, certification, certificationSet)
  }

  def Accept(obj : DeployableToolbox.AcceptableDeployable) : Boolean = {
    deployableCounts(DeployableToolbox.UnifiedTurret(obj.Definition.Item)).Available &&
      categoryCounts(obj.Definition.DeployCategory).Available &&
      !deployableLists(obj.Definition.DeployCategory).contains(obj)
  }

  def Add(obj : DeployableToolbox.AcceptableDeployable) : Boolean = {
    val category = obj.Definition.DeployCategory
    val dCategory = categoryCounts(category)
    val dType = deployableCounts(DeployableToolbox.UnifiedTurret(obj.Definition.Item))
    val dList = deployableLists(category)
    if(dCategory.Available() && dType.Available() && !dList.contains(obj)) {
      dCategory.Current += 1
      dType.Current += 1
      dList += obj
      true
    }
    else {
      false
    }
  }

  def Remove(obj : DeployableToolbox.AcceptableDeployable) : Boolean = {
    val category = obj.Definition.DeployCategory
    val deployables = deployableLists(category)
    if(deployables.contains(obj)) {
      categoryCounts(category).Current -= 1
      deployableCounts(DeployableToolbox.UnifiedTurret(obj.Definition.Item)).Current -= 1
      deployables -= obj
      true
    }
    else {
      false
    }
  }

  def DisplaceFirst(obj : DeployableToolbox.AcceptableDeployable) : Option[DeployableToolbox.AcceptableDeployable] = {
    DisplaceFirst(obj.Definition.DeployCategory)
  }

  def DisplaceFirst(category : DeployableCategory.Value) : Option[DeployableToolbox.AcceptableDeployable] = {
    val categoryBin = categoryCounts(category)
    val categoryList = deployableLists(category)
    if(categoryList.nonEmpty) {
      val temp = categoryList.remove(0)
      categoryBin.Current -= 1
      deployableCounts(DeployableToolbox.UnifiedTurret(temp.Definition.Item)).Current -= 1
      Some(temp)
    }
    else {
      None
    }
  }

  def Deployables(filter : DeployableToolbox.AcceptableDeployable) : List[PlanetSideGUID] = {
    Deployables(filter.Definition.Item)
  }

  def Deployables(filter : DeployedItem.Value) : List[PlanetSideGUID] = {
    deployableLists(DeployableToolbox.DeployablesToCategories(filter))
      .filter(entry => { entry.Definition.Item == filter })
      .map(_.GUID).toList
  }

  def Category(filter : DeployableToolbox.AcceptableDeployable) : List[PlanetSideGUID] = {
    Category(filter.Definition.DeployCategory)
  }

  def Category(filter : DeployableCategory.Value) : List[PlanetSideGUID] = {
    deployableLists(filter).map(_.GUID).toList
  }

  def UpdateUI() : List[(Int,Int,Int,Int)] = DeployedItem.values flatMap UpdateUIElement toList

  def UpdateUIElement(entry : DeployedItem.Value) : List[(Int,Int,Int,Int)] = {
    val toEntry = DeployableToolbox.UnifiedTurret(entry)
    val (curr, max) = DeployableToolbox.PlanetsideAttribute(toEntry)
    val dType = deployableCounts(toEntry)
    List((curr, dType.Current, max, dType.Max))
  }

  def UpdateUI(entry : CertificationType.Value) : List[(Int,Int,Int,Int)] = {
    import CertificationType._
    entry match {
      case AdvancedHacking =>
        UpdateUIElement(DeployedItem.sensor_shield)

      case CombatEngineering =>
        List(
          DeployedItem.boomer, DeployedItem.he_mine, DeployedItem.spitfire_turret, DeployedItem.motionalarmsensor
        ) flatMap UpdateUIElement

      case AssaultEngineering =>
        List(
          DeployedItem.jammer_mine, DeployedItem.portable_manned_turret, DeployedItem.deployable_shield_generator
        ) flatMap UpdateUIElement

      case FortificationEngineering =>
        List(
          DeployedItem.boomer,
          DeployedItem.he_mine,
          DeployedItem.spitfire_turret, DeployedItem.spitfire_cloaked, DeployedItem.spitfire_aa,
          DeployedItem.motionalarmsensor,
          DeployedItem.tank_traps
        ) flatMap UpdateUIElement

      case AdvancedEngineering =>
        List(AssaultEngineering, FortificationEngineering) flatMap UpdateUI

      case GroundSupport =>
        UpdateUIElement(DeployedItem.router_telepad_deployable)

      case _ =>
        Nil
    }
  }

  def UpdateUI(certifications : Set[CertificationType.Value]) : List[(Int,Int,Int,Int)] = {
    certifications flatMap UpdateUI toList
  }

  def Clear() : List[PlanetSideGUID] = {
    val out = deployableLists.values.flatten.map(_.GUID).toList
    deployableLists.values.foreach(_.clear())
    deployableCounts.values.foreach(_.Current = 0)
    categoryCounts.values.foreach(_.Current = 0)
    out
  }
}

object DeployableToolbox {
  type AcceptableDeployable = PlanetSideGameObject with Deployable

  private class Bin {
    private var max : Int = 0
    private var current : Int = 0

    def Current : Int = current

    def Current_=(curr : Int) : Int = {
      current = curr
      Current
    }

    def Max : Int = max

    def Max_=(mx : Int) : Int = {
      max = mx
      Max
    }

    def Available() : Boolean = current < max
  }

  val DeployablesToCategories : Map[DeployedItem.Value, DeployableCategory.Value] = Map(
    DeployedItem.boomer -> DeployableCategory.Boomers,
    DeployedItem.he_mine -> DeployableCategory.Mines,
    DeployedItem.jammer_mine -> DeployableCategory.Mines,
    DeployedItem.spitfire_turret -> DeployableCategory.SmallTurrets,
    DeployedItem.motionalarmsensor -> DeployableCategory.Sensors,
    DeployedItem.spitfire_cloaked -> DeployableCategory.SmallTurrets,
    DeployedItem.spitfire_aa -> DeployableCategory.SmallTurrets,
    DeployedItem.deployable_shield_generator -> DeployableCategory.ShieldGenerators,
    DeployedItem.tank_traps -> DeployableCategory.TankTraps,
    DeployedItem.portable_manned_turret -> DeployableCategory.FieldTurrets,
    DeployedItem.portable_manned_turret_nc -> DeployableCategory.FieldTurrets,
    DeployedItem.portable_manned_turret_tr -> DeployableCategory.FieldTurrets,
    DeployedItem.portable_manned_turret_vs -> DeployableCategory.FieldTurrets,
    DeployedItem.sensor_shield -> DeployableCategory.Sensors,
    DeployedItem.router_telepad_deployable -> DeployableCategory.Telepads
  )

  /**
    * The attribute values to be invoked in `PlanetsideAttributeMessage` packets
    * in reference to a particular combat engineering deployable element on the UI.
    * The first number is for the actual count field.
    * The second number is for the maximum count field.
    */
  val PlanetsideAttribute : Map[DeployedItem.Value, (Int, Int)] = Map(
    DeployedItem.boomer -> (94, 83),
    DeployedItem.he_mine -> (95, 84),
    DeployedItem.jammer_mine -> (96, 85),
    DeployedItem.spitfire_turret -> (97, 86),
    DeployedItem.motionalarmsensor -> (98, 87),
    DeployedItem.spitfire_cloaked -> (99, 88),
    DeployedItem.spitfire_aa -> (100, 89),
    DeployedItem.deployable_shield_generator -> (101, 90),
    DeployedItem.tank_traps -> (102, 91),
    DeployedItem.portable_manned_turret -> (103, 92),
    DeployedItem.portable_manned_turret_nc -> (103, 92),
    DeployedItem.portable_manned_turret_tr -> (103, 92),
    DeployedItem.portable_manned_turret_vs -> (103, 92),
    DeployedItem.sensor_shield -> (104, 93),
    DeployedItem.router_telepad_deployable -> (0, 0) //TODO what is the attribute pair for the deployable_router_telepad
  )

  def UnifiedTurret(value : DeployedItem.Value) : DeployedItem.Value = value match {
    case DeployedItem.portable_manned_turret_nc | DeployedItem.portable_manned_turret_tr | DeployedItem.portable_manned_turret_vs =>
      DeployedItem.portable_manned_turret
    case _ =>
      value
  }

  private def Initialize(counts : mutable.Map[DeployedItem.Value, DeployableToolbox.Bin], categories : mutable.Map[DeployableCategory.Value, DeployableToolbox.Bin], certifications : Set[CertificationType.Value]) : Unit = {
    import CertificationType._
    if(certifications.contains(AdvancedEngineering)) {
      counts(DeployedItem.boomer).Max = 25
      counts(DeployedItem.he_mine).Max = 25
      counts(DeployedItem.jammer_mine).Max = 20
      counts(DeployedItem.spitfire_turret).Max = 15
      counts(DeployedItem.spitfire_cloaked).Max = 5
      counts(DeployedItem.spitfire_aa).Max = 5
      counts(DeployedItem.motionalarmsensor).Max = 25
      counts(DeployedItem.tank_traps).Max = 5
      counts(DeployedItem.portable_manned_turret).Max = 1
      //      counts(DeployedItem.portable_manned_turret_nc).Max = 1
      //      counts(DeployedItem.portable_manned_turret_tr).Max = 1
      //      counts(DeployedItem.portable_manned_turret_vs).Max = 1
      counts(DeployedItem.deployable_shield_generator).Max = 1
      categories(DeployableCategory.Boomers).Max = 25
      categories(DeployableCategory.Mines).Max = 25
      categories(DeployableCategory.SmallTurrets).Max = 15
      categories(DeployableCategory.Sensors).Max = 25
      categories(DeployableCategory.TankTraps).Max = 5
      categories(DeployableCategory.FieldTurrets).Max = 1
      categories(DeployableCategory.ShieldGenerators).Max = 1

      if(certifications.contains(AdvancedHacking)) {
        counts(DeployedItem.sensor_shield).Max = 25
      }
    }
    else if(certifications.contains(CombatEngineering)) {
      if(certifications.contains(AssaultEngineering)) {
        counts(DeployedItem.jammer_mine).Max = 20
        counts(DeployedItem.portable_manned_turret).Max = 1
        //        counts(DeployedItem.portable_manned_turret_nc).Max = 1
        //        counts(DeployedItem.portable_manned_turret_tr).Max = 1
        //        counts(DeployedItem.portable_manned_turret_vs).Max = 1
        counts(DeployedItem.deployable_shield_generator).Max = 1
        categories(DeployableCategory.FieldTurrets).Max = 1
        categories(DeployableCategory.ShieldGenerators).Max = 1
      }
      if(certifications.contains(FortificationEngineering)) {
        counts(DeployedItem.boomer).Max = 25
        counts(DeployedItem.he_mine).Max = 25
        counts(DeployedItem.spitfire_turret).Max = 15
        counts(DeployedItem.spitfire_cloaked).Max = 5
        counts(DeployedItem.spitfire_aa).Max = 5
        counts(DeployedItem.motionalarmsensor).Max = 25
        counts(DeployedItem.tank_traps).Max = 5
        categories(DeployableCategory.Boomers).Max = 25
        categories(DeployableCategory.Mines).Max = 25
        categories(DeployableCategory.SmallTurrets).Max = 15
        categories(DeployableCategory.Sensors).Max = 25
        categories(DeployableCategory.TankTraps).Max = 5
      }
      else {
        counts(DeployedItem.boomer).Max = 20
        counts(DeployedItem.he_mine).Max = 20
        counts(DeployedItem.spitfire_turret).Max = 10
        counts(DeployedItem.motionalarmsensor).Max = 20
        categories(DeployableCategory.Boomers).Max = 20
        categories(DeployableCategory.Mines).Max = 20
        categories(DeployableCategory.SmallTurrets).Max = 10
        categories(DeployableCategory.Sensors).Max = 20
      }

      if(certifications.contains(AdvancedHacking)) {
        counts(DeployedItem.sensor_shield).Max = 20
      }
    }
    if(certifications.contains(CertificationType.GroundSupport)) {
      counts(DeployedItem.router_telepad_deployable).Max = 1
      categories(DeployableCategory.Telepads).Max = 1
    }
  }

  def AddToDeployableQuantities(counts : mutable.Map[DeployedItem.Value, DeployableToolbox.Bin], categories : mutable.Map[DeployableCategory.Value, DeployableToolbox.Bin], certification : CertificationType.Value, certifictionSet : Set[CertificationType.Value]) : Unit = {
    import CertificationType._
    if(certifictionSet contains certification) {
      certification match {
        case AdvancedHacking =>
          if(certifictionSet contains CombatEngineering) {
            counts(DeployedItem.sensor_shield).Max = 20
          }

        case CombatEngineering =>
          counts(DeployedItem.boomer).Max = 20
          counts(DeployedItem.he_mine).Max = 20
          counts(DeployedItem.spitfire_turret).Max = 10
          counts(DeployedItem.motionalarmsensor).Max = 20
          categories(DeployableCategory.Boomers).Max = 20
          categories(DeployableCategory.Mines).Max = 20
          categories(DeployableCategory.SmallTurrets).Max = 10
          categories(DeployableCategory.Sensors).Max = 20
          if(certifictionSet contains AdvancedHacking) {
            counts(DeployedItem.sensor_shield).Max = 20
          }

        case AssaultEngineering =>
          counts(DeployedItem.jammer_mine).Max = 20
          counts(DeployedItem.portable_manned_turret).Max = 1
          //          counts(DeployedItem.portable_manned_turret_nc).Max = 1
          //          counts(DeployedItem.portable_manned_turret_tr).Max = 1
          //          counts(DeployedItem.portable_manned_turret_vs).Max = 1
          counts(DeployedItem.deployable_shield_generator).Max = 1
          categories(DeployableCategory.FieldTurrets).Max = 1
          categories(DeployableCategory.ShieldGenerators).Max = 1

        case FortificationEngineering =>
          counts(DeployedItem.boomer).Max = 25
          counts(DeployedItem.he_mine).Max = 25
          counts(DeployedItem.spitfire_turret).Max = 15
          counts(DeployedItem.motionalarmsensor).Max = 25
          counts(DeployedItem.spitfire_cloaked).Max = 5
          counts(DeployedItem.spitfire_aa).Max = 5
          counts(DeployedItem.tank_traps).Max = 5
          categories(DeployableCategory.Boomers).Max = 25
          categories(DeployableCategory.Mines).Max = 25
          categories(DeployableCategory.SmallTurrets).Max = 15
          categories(DeployableCategory.Sensors).Max = 25
          categories(DeployableCategory.TankTraps).Max = 5

        case AdvancedEngineering =>
          if(!certifictionSet.contains(AssaultEngineering)) {
            AddToDeployableQuantities(counts, categories, AssaultEngineering, certifictionSet ++ Set(AssaultEngineering))
          }
          if(!certifictionSet.contains(FortificationEngineering)) {
            AddToDeployableQuantities(counts, categories, FortificationEngineering, certifictionSet ++ Set(FortificationEngineering))
          }

        case GroundSupport =>
          counts(DeployedItem.router_telepad_deployable).Max = 1
          categories(DeployableCategory.Telepads).Max = 1

        case _ => ;
      }
    }
  }

  def RemoveFromDeployablesQuantities(counts : mutable.Map[DeployedItem.Value, DeployableToolbox.Bin], categories : mutable.Map[DeployableCategory.Value, DeployableToolbox.Bin], certification : CertificationType.Value, certifictionSet : Set[CertificationType.Value]) : Unit = {
    import CertificationType._
    if(!certifictionSet.contains(certification)) {
      certification match {
        case AdvancedHacking =>
          counts(DeployedItem.sensor_shield).Max = 0

        case CombatEngineering =>
          counts(DeployedItem.boomer).Max = 0
          counts(DeployedItem.he_mine).Max = 0
          counts(DeployedItem.spitfire_turret).Max = 0
          counts(DeployedItem.motionalarmsensor).Max = 0
          counts(DeployedItem.sensor_shield).Max = 0
          categories(DeployableCategory.Boomers).Max = 0
          categories(DeployableCategory.Mines).Max = 0
          categories(DeployableCategory.SmallTurrets).Max = 0
          categories(DeployableCategory.Sensors).Max = 0

        case AssaultEngineering =>
          counts(DeployedItem.jammer_mine).Max = 0
          counts(DeployedItem.portable_manned_turret).Max = 0
          //          counts(DeployedItem.portable_manned_turret_nc).Max = 0
          //          counts(DeployedItem.portable_manned_turret_tr).Max = 0
          //          counts(DeployedItem.portable_manned_turret_vs).Max = 0
          counts(DeployedItem.deployable_shield_generator).Max = 0
          categories(DeployableCategory.Sensors).Max = if(certifictionSet contains CombatEngineering) 20 else 0
          categories(DeployableCategory.FieldTurrets).Max = 0
          categories(DeployableCategory.ShieldGenerators).Max = 0

        case FortificationEngineering =>
          val ce : Int = if(certifictionSet contains CombatEngineering) 1 else 0 //true = 1, false = 0
          counts(DeployedItem.boomer).Max = ce * 20
          counts(DeployedItem.he_mine).Max = ce * 20
          counts(DeployedItem.spitfire_turret).Max = ce * 10
          counts(DeployedItem.motionalarmsensor).Max = ce * 20
          counts(DeployedItem.spitfire_cloaked).Max = 0
          counts(DeployedItem.spitfire_aa).Max = 0
          counts(DeployedItem.tank_traps).Max = 0
          categories(DeployableCategory.Boomers).Max = ce * 20
          categories(DeployableCategory.Mines).Max = ce * 20
          categories(DeployableCategory.SmallTurrets).Max = ce * 10
          categories(DeployableCategory.Sensors).Max = ce * 20
          categories(DeployableCategory.TankTraps).Max = 0

        case AdvancedEngineering =>
          if(!certifictionSet.contains(AssaultEngineering)) {
            RemoveFromDeployablesQuantities(counts, categories, AssaultEngineering, certifictionSet)
          }
          if(!certifictionSet.contains(FortificationEngineering)) {
            RemoveFromDeployablesQuantities(counts, categories, FortificationEngineering, certifictionSet)
          }

        case GroundSupport =>
          counts(DeployedItem.router_telepad_deployable).Max = 0
          categories(DeployableCategory.Telepads).Max = 0

        case _ => ;
      }
    }
  }
}
