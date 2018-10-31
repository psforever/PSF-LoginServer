// Copyright (c) 2017 PSForever
package net.psforever.objects.avatar

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ce.{Deployable, DeployableCategory, DeployedItem}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.CertificationType

import scala.collection.mutable

/**
  * A class that keeps track - "manages" - deployables that are owned by the avatar.<br>
  * <br>
  * Deployables belong to the Engineering certification line of certifications.
  * `CombatEngineering` and above certifications include permissions for different types of deployables,
  * and one unique type of deployable is available through the `GroundSupport`
  * and one that also requires `AdvancedHacking`.
  * (They are collectively called "ce" for that reason.)
  * Not only does the level of certification change the maximum number of deployables that can be managed by type
  * but it also influences the maximum number of deployables that can be managed by category.
  * Individual deployables are counted by type and category individually in special data structures
  * to avoid having to probe the primary list of deployable references whenever a question of quantity is asked.
  * As deployables are added and removed, and tracked certifications are added and removed,
  * these structures are updated to reflect proper count.
  */
class DeployableToolbox {
  /**
    * a map of bins for keeping track of the quantities of deployables in a category
    * keys: categories, values: quantity storage object
    */
  private val categoryCounts = DeployableCategory.values.toSeq.map(value => { value -> new DeployableToolbox.Bin }).toMap
  categoryCounts(DeployableCategory.Telepads).Max = 1024
  /**
    * a map of bins for keeping track of the quantities of individual deployables
    * keys: deployable types, values: quantity storage object
    */
  private val deployableCounts = DeployedItem.values.toSeq.map(value => { value -> new DeployableToolbox.Bin }).toMap
  deployableCounts(DeployedItem.router_telepad_deployable).Max = 1024
  /**
    * a map of tracked/owned individual deployables
    * keys: categories, values: deployable objects
    */
  private val deployableLists =
    DeployableCategory.values.toSeq.map(value => { value -> mutable.ListBuffer[DeployableToolbox.AcceptableDeployable]() }).toMap
  /**
    * can only be initialized once
    * set during the `Initialization` method primarily, and in `Add` and in `Remove` if not
    */
  private var initialized : Boolean = false

  /**
    * Set up the initial deployable counts by providing certification values to be used in category and unit selection.
    * @param certifications a group of certifications for the initial values
    * @return `true`, if this is the first time and actual "initialization" is performed;
    *        `false`, otherwise
    */
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

  /**
    * Change the count of deployable units that can be tracked by providing a new certification.
    * If the given certification is already factored into the quantities, no changes will occur.
    * @param certification the new certification
    * @param certificationSet the group of previous certifications being tracked;
    *                         occasionally, important former certification values are required for additional configuration;
    *                         the new certification should already have been added to this group
    */
  def AddToDeployableQuantities(certification : CertificationType.Value, certificationSet : Set[CertificationType.Value]) : Unit = {
    initialized = true
    DeployableToolbox.AddToDeployableQuantities(deployableCounts, categoryCounts, certification, certificationSet)
  }

  /**
    * Change the count of deployable units that can be tracked
    * by designating a certification whose deployables will be removed.
    * If the given certification is already factored out of the quantities, no changes will occur.
    * @param certification the old certification
    * @param certificationSet the group of previous certifications being tracked;
    *                         occasionally, important former certification values are required for additional configuration;
    *                         the new certification should already have been excluded from this group
    */
  def RemoveFromDeployableQuantities(certification : CertificationType.Value, certificationSet : Set[CertificationType.Value]) : Unit = {
    initialized = true
    DeployableToolbox.RemoveFromDeployablesQuantities(deployableCounts, categoryCounts, certification, certificationSet)
  }

  /**
    * Determine if the given deployable can be managed by this toolbox.
    * @see `Valid`
    * @see `Available`
    * @see `Contains`
    * @param obj the deployable
    * @return `true`, if it can be managed under the current conditions;
    *        `false`, otherwise
    */
  def Accept(obj : DeployableToolbox.AcceptableDeployable) : Boolean = {
    Valid(obj) && Available(obj) && !Contains(obj)
  }

  /**
    * Determine if the given deployable can be managed by this toolbox
    * by testing if the specific deployable maximum and the deployable category maximum is non-zero
    * @param obj the deployable
    * @return `true`, if both category maximum and deployable type maximum are positive non-zero integers;
    *        `false`, otherwise
    */
  def Valid(obj : DeployableToolbox.AcceptableDeployable) : Boolean = {
    deployableCounts(DeployableToolbox.UnifiedType(obj.Definition.Item)).Max > 0 &&
      categoryCounts(obj.Definition.DeployCategory).Max > 0
  }

  /**
    * Determine if the given deployable can be managed by this toolbox
    * by testing if the specific deployable list and the deployable category list have available slots.
    * In this case, a "slot" is merely the difference between the current count is less than the maximum count.
    * @param obj the deployable
    * @return `true`, if the deployable can be added to the support lists and counted;
    *        `false`, otherwise
    */
  def Available(obj : DeployableToolbox.AcceptableDeployable) : Boolean = {
      deployableCounts(DeployableToolbox.UnifiedType(obj.Definition.Item)).Available &&
      categoryCounts(obj.Definition.DeployCategory).Available
  }

  /**
    * Check if this deployable is already being managed by the toolbox
    * by determining whether or not it is already being managed by this toolbox.
    * @param obj the deployable
    * @return `true`, if the deployable can be found in one of the lists;
    *        `false`, otherwise
    */
  def Contains(obj : DeployableToolbox.AcceptableDeployable) : Boolean = {
    deployableLists(obj.Definition.DeployCategory).contains(obj)
  }

  /**
    * Manage the provided deployable.<br>
    * <br>
    * Although proper testing should be performed prior to attempting to add the deployable to this toolbox,
    * three tests are administered to determine whether space is available prior to insertion.
    * The first two tests check for available space in the category count and in the unit count
    * and the third test checks whether the deployable is already being managed by this toolbox.
    * No changes should occur if the deployable is not properly added.
    * @param obj the deployable
    * @return `true`, if the deployable is added;
    *        `false`, otherwise
    */
  def Add(obj : DeployableToolbox.AcceptableDeployable) : Boolean = {
    val category = obj.Definition.DeployCategory
    val dCategory = categoryCounts(category)
    val dType = deployableCounts(DeployableToolbox.UnifiedType(obj.Definition.Item))
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

  /**
    * Stop managing the provided deployable.<br>
    * <br>
    * Although proper testing should be performed prior to attempting to remove the deployable to this toolbox,
    * a single test is administered to determine whether the removal can take place.
    * If the deployable is found to currently being managed by this toolbox, then it is properly removed.
    * No changes should occur if the deployable is not properly removed.
    * @param obj the deployable
    * @return `true`, if the deployable is added;
    *        `false`, otherwise
    */
  def Remove(obj : DeployableToolbox.AcceptableDeployable) : Boolean = {
    val category = obj.Definition.DeployCategory
    val deployables = deployableLists(category)
    if(deployables.contains(obj)) {
      categoryCounts(category).Current -= 1
      deployableCounts(DeployableToolbox.UnifiedType(obj.Definition.Item)).Current -= 1
      deployables -= obj
      true
    }
    else {
      false
    }
  }

  /**
    * Remove the first managed deployable that matches the same type of deployable as the example.
    * The explicit tests is defined to find the first deployable whose type matches.
    * @param obj the example deployable
    * @return any deployable that is found
    */
  def DisplaceFirst(obj : DeployableToolbox.AcceptableDeployable) : Option[DeployableToolbox.AcceptableDeployable] = {
    DisplaceFirst(obj, { d => d.Definition.Item == obj.Definition.Item })
  }

  /**
    * Remove the first managed deployable that satisfies a test and belongs to the same category as the example.
    * The test in question is used to pinpoint the first qualifying deployable;
    * but, if the test fails to find any valid targets,
    * the first deployable in the list of managed deployables for that category is selected to be removed.
    * The only test performed is whether there is any valid deployable managed for the category.
    * @param obj the example deployable
    * @param rule the testing rule for determining a valid deployable
    * @return any deployable that is found
    */
  def DisplaceFirst(obj : DeployableToolbox.AcceptableDeployable, rule : (Deployable)=> Boolean) : Option[DeployableToolbox.AcceptableDeployable] = {
    val definition = obj.Definition
    val category = definition.DeployCategory
    val categoryList = deployableLists(category)
    if(categoryList.nonEmpty) {
      val found = categoryList.find(rule) match {
        case Some(target) =>
          categoryList.remove(categoryList.indexOf(target))
        case None =>
          categoryList.remove(0)
      }
      categoryCounts(category).Current -= 1
      deployableCounts(DeployableToolbox.UnifiedType(found.Definition.Item)).Current -= 1
      Some(found)
    }
    else {
      None
    }
  }

  /**
    * Remove the first managed deployable from a category.
    * The only test performed is whether there is any valid deployable managed for the category.
    * @param category the target category
    * @return any deployable that is found
    */
  def DisplaceFirst(category : DeployableCategory.Value) : Option[DeployableToolbox.AcceptableDeployable] = {
    val categoryList = deployableLists(category)
    if(categoryList.nonEmpty) {
      val found = categoryList.remove(0)
      categoryCounts(category).Current -= 1
      deployableCounts(DeployableToolbox.UnifiedType(found.Definition.Item)).Current -= 1
      Some(found)
    }
    else {
      None
    }
  }

  /**
    * Reference all managed deployables of the same type as an example deployable.
    * @param filter the example deployable
    * @return a list of globally unique identifiers that should be valid for the current zone
    */
  def Deployables(filter : DeployableToolbox.AcceptableDeployable) : List[PlanetSideGUID] = {
    Deployables(filter.Definition.Item)
  }

  /**
    * Reference all managed deployables of the same type.
    * @param filter the type of deployable
    * @return a list of globally unique identifiers that should be valid for the current zone
    */
  def Deployables(filter : DeployedItem.Value) : List[PlanetSideGUID] = {
    deployableLists(Deployable.Category.Of(filter))
      .filter(entry => { entry.Definition.Item == filter })
      .map(_.GUID).toList
  }

  /**
    * Reference all managed deployables in the same category as an example deployable.
    * @param filter the example deployable
    * @return a list of globally unique identifiers that should be valid for the current zone
    */
  def Category(filter : DeployableToolbox.AcceptableDeployable) : List[PlanetSideGUID] = {
    Category(filter.Definition.DeployCategory)
  }

  /**
    * Reference all managed deployables in the same category.
    * @param filter the type of deployable
    * @return a list of globally unique identifiers that should be valid for the current zone
    */
  def Category(filter : DeployableCategory.Value) : List[PlanetSideGUID] = {
    deployableLists(filter).map(_.GUID).toList
  }

  /**
    * Check the current capacity for the same type of deployable as the example.
    * @param item the example deployable
    * @return the current quantity of deployables and the maximum number
    */
  def CountDeployable(item : DeployedItem.Value) : (Int, Int) = {
    val dType = deployableCounts(DeployableToolbox.UnifiedType(item))
    (dType.Current, dType.Max)
  }

  /**
    * Check the current capacity for the same category of deployable as the example.
    * @param item the example deployable
    * @return the current quantity of deployables and the maximum number
    */
  def CountCategory(item : DeployedItem.Value) : (Int, Int) = {
    val dCat = categoryCounts(Deployable.Category.Of(DeployableToolbox.UnifiedType(item)))
    (dCat.Current, dCat.Max)
  }

  def UpdateUIElement(entry : DeployedItem.Value) : List[(Int,Int,Int,Int)] = {
    val toEntry = DeployableToolbox.UnifiedType(entry)
    val (curr, max) = Deployable.UI(toEntry)
    val dType = deployableCounts(toEntry)
    List((curr, dType.Current, max, dType.Max))
  }

  def UpdateUI() : List[(Int,Int,Int,Int)] = DeployedItem.values flatMap UpdateUIElement toList

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

      case _ =>
        Nil
    }
  }

  def UpdateUI(certifications : List[CertificationType.Value]) : List[(Int,Int,Int,Int)] = {
    certifications flatMap UpdateUI
  }

  /**
    * Remove all managed deployables that are the same type.
    * @param item the deployable type
    * @return a list of globally unique identifiers that should be valid for the current zone
    */
  def ClearDeployable(item : DeployedItem.Value) : List[PlanetSideGUID] = {
    val uitem = DeployableToolbox.UnifiedType(item)
    val category = Deployable.Category.Of(uitem)
    val categoryList = deployableLists(category)
    val (out, in) = categoryList.partition(_.Definition.Item == item)

    categoryList.clear()
    categoryList ++= in
    categoryCounts(category).Current = in.size
    deployableCounts(uitem).Current = 0
    out.map(_.GUID).toList
  }

  /**
    * Remove all managed deployables that belong to the same category.
    * @param item the deployable type belonging to a category
    * @return a list of globally unique identifiers that should be valid for the current zone
    */
  def ClearCategory(item : DeployedItem.Value) : List[PlanetSideGUID] = {
    val category = Deployable.Category.Of(DeployableToolbox.UnifiedType(item))
    val out = deployableLists(category).map(_.GUID).toList
    deployableLists(category).clear()
    categoryCounts(category).Current = 0
    (Deployable.Category.Includes(category) map DeployableToolbox.UnifiedType toSet)
      .foreach({item : DeployedItem.Value => deployableCounts(item).Current = 0 })
    out
  }

  /**
    * Remove all managed deployables.
    * @return a list of globally unique identifiers that should be valid for the current zone
    */
  def Clear() : List[PlanetSideGUID] = {
    val out = deployableLists.values.flatten.map(_.GUID).toList
    deployableLists.values.foreach(_.clear())
    deployableCounts.values.foreach(_.Current = 0)
    categoryCounts.values.foreach(_.Current = 0)
    out
  }
}

object DeployableToolbox {
  /**
    * A `type` intended to properly define the minimum acceptable conditions for a `Deployable` object.
    */
  type AcceptableDeployable = PlanetSideGameObject with Deployable

  /**
    * An internal class to keep track of the quantity of deployables managed for a certain set of criteria.
    * There are deployable numbers organized by deploybale type and by deployable category.
    */
  private class Bin {
    /** the maximum number of deployables for this criteria that can be managed */
    private var max : Int = 0
    /** the current number of deployables for this criteria that are being managed */
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

  /**
    * Some deployable types, though unique themselves,
    * resolve to the same deployable type for the purposes of categorization.
    * @param item the type of deployable
    * @return the corrected deployable type
    */
  def UnifiedType(item : DeployedItem.Value) : DeployedItem.Value = item match {
    case DeployedItem.portable_manned_turret_nc | DeployedItem.portable_manned_turret_tr | DeployedItem.portable_manned_turret_vs =>
      DeployedItem.portable_manned_turret
    case _ =>
      item
  }

  /**
    * Hardcoded maximum values for the category and type initialization.
    * @param counts a reference to the type `Bin` object
    * @param categories a reference to the category `Bin` object
    * @param certifications a group of certifications for the initial values
    */
  private def Initialize(counts : Map[DeployedItem.Value, DeployableToolbox.Bin], categories : Map[DeployableCategory.Value, DeployableToolbox.Bin], certifications : Set[CertificationType.Value]) : Unit = {
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
      counts(DeployedItem.portable_manned_turret).Max = 1 //the below turret types are unified
      //counts(DeployedItem.portable_manned_turret_nc).Max = 1
      //counts(DeployedItem.portable_manned_turret_tr).Max = 1
      //counts(DeployedItem.portable_manned_turret_vs).Max = 1
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
        counts(DeployedItem.portable_manned_turret).Max = 1 //the below turret types are unified
        //counts(DeployedItem.portable_manned_turret_nc).Max = 1
        //counts(DeployedItem.portable_manned_turret_tr).Max = 1
        //counts(DeployedItem.portable_manned_turret_vs).Max = 1
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
      counts(DeployedItem.router_telepad_deployable).Max = 1024
      categories(DeployableCategory.Telepads).Max = 1024
    }
  }

  /**
    * Hardcoded maximum values for the category and type initialization upon providing a new certification.
    * @param counts a reference to the type `Bin` object
    * @param categories a reference to the category `Bin` object
    * @param certification the new certification
    * @param certificationSet the group of previous certifications being tracked
    */
  def AddToDeployableQuantities(counts : Map[DeployedItem.Value, DeployableToolbox.Bin], categories : Map[DeployableCategory.Value, DeployableToolbox.Bin], certification : CertificationType.Value, certificationSet : Set[CertificationType.Value]) : Unit = {
    import CertificationType._
    if(certificationSet contains certification) {
      certification match {
        case AdvancedHacking =>
          if(certificationSet contains CombatEngineering) {
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
          if(certificationSet contains AdvancedHacking) {
            counts(DeployedItem.sensor_shield).Max = 20
          }

        case AssaultEngineering =>
          counts(DeployedItem.jammer_mine).Max = 20
          counts(DeployedItem.portable_manned_turret).Max = 1 //the below turret types are unified
          //counts(DeployedItem.portable_manned_turret_nc).Max = 1
          //counts(DeployedItem.portable_manned_turret_tr).Max = 1
          //counts(DeployedItem.portable_manned_turret_vs).Max = 1
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
          if(!certificationSet.contains(AssaultEngineering)) {
            AddToDeployableQuantities(counts, categories, AssaultEngineering, certificationSet ++ Set(AssaultEngineering))
          }
          if(!certificationSet.contains(FortificationEngineering)) {
            AddToDeployableQuantities(counts, categories, FortificationEngineering, certificationSet ++ Set(FortificationEngineering))
          }

//        case GroundSupport =>
//          counts(DeployedItem.router_telepad_deployable).Max = 1024
//          categories(DeployableCategory.Telepads).Max = 1024

        case _ => ;
      }
    }
  }

  /**
    * Hardcoded zero'd values for the category and type initialization upon ignoring a previous certification.
    * @param counts a reference to the type `Bin` object
    * @param categories a reference to the category `Bin` object
    * @param certification the new certification
    * @param certificationSet the group of previous certifications being tracked
    */
  def RemoveFromDeployablesQuantities(counts : Map[DeployedItem.Value, DeployableToolbox.Bin], categories : Map[DeployableCategory.Value, DeployableToolbox.Bin], certification : CertificationType.Value, certificationSet : Set[CertificationType.Value]) : Unit = {
    import CertificationType._
    if(!certificationSet.contains(certification)) {
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
          counts(DeployedItem.portable_manned_turret).Max = 0  //the below turret types are unified
          //counts(DeployedItem.portable_manned_turret_nc).Max = 0
          //counts(DeployedItem.portable_manned_turret_tr).Max = 0
          //counts(DeployedItem.portable_manned_turret_vs).Max = 0
          counts(DeployedItem.deployable_shield_generator).Max = 0
          categories(DeployableCategory.Sensors).Max = if(certificationSet contains CombatEngineering) 20 else 0
          categories(DeployableCategory.FieldTurrets).Max = 0
          categories(DeployableCategory.ShieldGenerators).Max = 0

        case FortificationEngineering =>
          val ce : Int = if(certificationSet contains CombatEngineering) 1 else 0 //true = 1, false = 0
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
          if(!certificationSet.contains(AssaultEngineering)) {
            RemoveFromDeployablesQuantities(counts, categories, AssaultEngineering, certificationSet)
          }
          if(!certificationSet.contains(FortificationEngineering)) {
            RemoveFromDeployablesQuantities(counts, categories, FortificationEngineering, certificationSet)
          }

//        case GroundSupport =>
//          counts(DeployedItem.router_telepad_deployable).Max = 0
//          categories(DeployableCategory.Telepads).Max = 0

        case _ => ;
      }
    }
  }
}
