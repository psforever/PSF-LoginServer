// Copyright (c) 2017 PSForever
package net.psforever.objects.ce

import net.psforever.objects._
import net.psforever.objects.definition.{BaseDeployableDefinition, ObjectDefinition}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.{DamageResistanceModel, Vitality}
import net.psforever.packet.game.{DeployableIcon, PlanetSideGUID}
import net.psforever.types.PlanetSideEmpire

trait Deployable extends FactionAffinity
  with Vitality {
  this : PlanetSideGameObject =>
  private var health : Int = 1
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private var owner : Option[PlanetSideGUID] = None
  private var ownerName : Option[String] = None

  def Health : Int = health

  def Health_=(toHealth : Int) : Int = {
    health = math.min(math.max(0, toHealth), MaxHealth)
    Health
  }

  def MaxHealth : Int

  def Faction : PlanetSideEmpire.Value = faction

  override def Faction_=(toFaction : PlanetSideEmpire.Value) : PlanetSideEmpire.Value = {
    faction = toFaction
    Faction
  }

  def Owner : Option[PlanetSideGUID] = owner

  def Owner_=(owner : PlanetSideGUID) : Option[PlanetSideGUID] = Owner_=(Some(owner))

  def Owner_=(owner : Player) : Option[PlanetSideGUID] = Owner_=(Some(owner.GUID))

  def Owner_=(owner : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    owner match {
      case Some(_) =>
        this.owner = owner
      case None =>
        this.owner = None
    }
    Owner
  }

  def OwnerName : Option[String] = ownerName

  def OwnerName_=(owner : String) : Option[String] = OwnerName_=(Some(owner))

  def OwnerName_=(owner : Player) : Option[String] = OwnerName_=(Some(owner.Name))

  def OwnerName_=(owner : Option[String]) : Option[String] = {
    owner match {
      case Some(_) =>
        ownerName = owner
      case None =>
        ownerName = None
    }
    OwnerName
  }

  def DamageModel : DamageResistanceModel = Definition.asInstanceOf[DamageResistanceModel]

  def Definition : ObjectDefinition with BaseDeployableDefinition
}

object Deployable {
  object Category {
    def Of(item : DeployedItem.Value) : DeployableCategory.Value = deployablesToCategories(item)

    def Includes(category : DeployableCategory.Value) : List[DeployedItem.Value] = {
      (for {
        (ce, cat) <- deployablesToCategories
        if cat == category
      } yield ce) toList
    }

    def OfAll() : Map[DeployedItem.Value, DeployableCategory.Value] = deployablesToCategories

    private val deployablesToCategories : Map[DeployedItem.Value, DeployableCategory.Value] = Map(
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
  }

  object Icon {
    def apply(item : DeployedItem.Value) : DeployableIcon.Value = ceicon(item.id)

    private val ceicon : Map[Int, DeployableIcon.Value] = Map(
      DeployedItem.boomer.id -> DeployableIcon.Boomer,
      DeployedItem.he_mine.id -> DeployableIcon.HEMine,
      DeployedItem.jammer_mine.id -> DeployableIcon.DisruptorMine,
      DeployedItem.spitfire_turret.id -> DeployableIcon.SpitfireTurret,
      DeployedItem.spitfire_cloaked.id -> DeployableIcon.ShadowTurret,
      DeployedItem.spitfire_aa.id -> DeployableIcon.CerebusTurret,
      DeployedItem.motionalarmsensor.id -> DeployableIcon.MotionAlarmSensor,
      DeployedItem.sensor_shield.id -> DeployableIcon.SensorDisruptor,
      DeployedItem.tank_traps.id -> DeployableIcon.TRAP,
      DeployedItem.portable_manned_turret.id -> DeployableIcon.FieldTurret,
      DeployedItem.portable_manned_turret_tr.id -> DeployableIcon.FieldTurret,
      DeployedItem.portable_manned_turret_nc.id -> DeployableIcon.FieldTurret,
      DeployedItem.portable_manned_turret_vs.id -> DeployableIcon.FieldTurret,
      DeployedItem.deployable_shield_generator.id -> DeployableIcon.AegisShieldGenerator,
      DeployedItem.router_telepad_deployable.id -> DeployableIcon.RouterTelepad
    ).withDefaultValue(DeployableIcon.Boomer)
  }

  object UI {
    def apply(item : DeployedItem.Value) : (Int, Int) = planetsideAttribute(item)

    /**
      * The attribute values to be invoked in `PlanetsideAttributeMessage` packets
      * in reference to a particular combat engineering deployable element on the UI.
      * The first number is for the actual count field.
      * The second number is for the maximum count field.
      */
    private val planetsideAttribute : Map[DeployedItem.Value, (Int, Int)] = Map(
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
      DeployedItem.sensor_shield -> (104, 93)
    ).withDefaultValue((0,0))
  }
}
