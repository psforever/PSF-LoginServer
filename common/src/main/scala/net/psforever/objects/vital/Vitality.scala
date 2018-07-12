// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ballistics.ResolvedProjectile

abstract class VitalsActivity

final case class ProjectileDamage(data : ResolvedProjectile) extends VitalsActivity

final case class VehicleShieldCharge(amount : Int, time : Long = System.nanoTime) extends VitalsActivity

/**
  * A vital object can be hurt or damaged or healed or repaired (HDHR).
  * The amount of HDHR is controlled by the damage model of this vital object reacting to stimulus.
  * A history of the previous changes in vital statistics of the underlying object is recorded
  * in reverse chronological order.
  * The damage model is also provided.
  */
trait Vitality {
  this : PlanetSideGameObject =>

  /** a reverse-order list of chronological events that have occurred to these vital statistics */
  private var vitalHistory : List[VitalsActivity] = List.empty[VitalsActivity]

  def History(action : VitalsActivity) : List[VitalsActivity] = {
    vitalHistory = action +: vitalHistory
    vitalHistory
  }

  /**
    * Very common example of a `VitalsActivity` event involving weapon discharge.
    * @param projectile the fully-informed entry of discharge of a weapon
    * @return the list of previous changes to this object's vital statistics
    */
  def History(projectile : ResolvedProjectile) : List[VitalsActivity] = {
    vitalHistory = ProjectileDamage(projectile) +: vitalHistory
    vitalHistory
  }

  def LastVitalsActivity(test : (VitalsActivity)=>Boolean) : Option[VitalsActivity] = {
    vitalHistory.find(test(_))
  }

  /**
    * Find, specifically, the last instance of a weapon discharge vital statistics change.
    * @return information about the discharge
    */
  def LastShot : Option[ResolvedProjectile] = {
    LastVitalsActivity({p => p.isInstanceOf[ProjectileDamage]}) match {
      case Some(entry : ProjectileDamage) =>
        Some(entry.data)
      case _ =>
        None
    }
  }

  def ClearHistory() : List[VitalsActivity] = {
    val out = vitalHistory
    vitalHistory = List.empty[VitalsActivity]
    out
  }

  def DamageModel : DamageResistanceModel
}

object Vitality {

  /**
    * Provide the damage model-generated functionality
    * that would properly enact the calculated changes of a vital statistics event
    * upon a given vital object.
    * @param func a function literal
    */
  final case class Damage(func : (Any)=>Unit)

  /**
    * Report that a vitals object must be updated due to damage.
    * @param obj the vital object
    */
  final case class DamageResolution(obj : Vitality)
}
