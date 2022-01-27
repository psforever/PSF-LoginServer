// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import akka.actor.{ActorContext, Props}
import net.psforever.objects.{Default, NtuContainerDefinition, Vehicle}
import net.psforever.objects.definition.converter.VehicleConverter
import net.psforever.objects.inventory.InventoryTile
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.vehicles.{DestroyedVehicle, MountableWeaponsDefinition, UtilityType, VehicleSubsystemEntry}
import net.psforever.objects.vital._
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.resistance.ResistanceProfileMutators
import net.psforever.objects.vital.resolution.DamageResistanceModel
import net.psforever.types.Vector3

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * An object definition system used to construct and retain the parameters of various vehicles.
  * @param objectId the object id that is associated with this sort of `Vehicle`
  */
class VehicleDefinition(objectId: Int)
    extends ObjectDefinition(objectId)
    with MountableWeaponsDefinition
    with VitalityDefinition
    with NtuContainerDefinition
    with ResistanceProfileMutators
    with DamageResistanceModel {
  /** ... */
  var shieldUiAttribute: Int = 68
  /** how many points of shield the vehicle starts with (should default to 0 if unset through the accessor) */
  private var defaultShields : Option[Int] = None
  /** maximum vehicle shields (generally: 20% of health)
    * for normal vehicles, offered through amp station facility benefits
    * for BFR's, it charges naturally
    **/
  private var maxShields: Int = 0
  /** the minimum amount of time that must elapse in between damage and shield charge activities (ms) */
  private var shieldChargeDamageCooldown : Long = 5000L
  /** the minimum amount of time that must elapse in between distinct shield charge activities (ms) */
  private var shieldChargePeriodicCooldown : Long = 1000L
  /** if the shield recharges on its own, this value will be non-`None` and indicate by how much */
  private var autoShieldRecharge : Option[Int] = None
  private var autoShieldRechargeSpecial : Option[Int] = None
  /** shield drain is what happens to the shield under special conditions, e.g., bfr flight;
    * the drain interval is 250ms which is convenient for us
    * we can skip needing to define is explicitly */
  private var shieldDrain : Option[Int] = None
  private val cargo: mutable.HashMap[Int, CargoDefinition] = mutable.HashMap[Int, CargoDefinition]()
  private var deployment: Boolean                                = false
  private val utilities: mutable.HashMap[Int, UtilityType.Value] = mutable.HashMap()
  private val utilityOffsets: mutable.HashMap[Int, Vector3]      = mutable.HashMap()
  var subsystems: List[VehicleSubsystemEntry]                    = Nil
  private var deploymentTime_Deploy: Int                         = 0 //ms
  private var deploymentTime_Undeploy: Int                       = 0 //ms
  private var trunkSize: InventoryTile                           = InventoryTile.None
  private var trunkOffset: Int                                   = 0
  /* The position offset of the trunk, orientation as East = 0 */
  private var trunkLocation: Vector3                             = Vector3.Zero
  private var canCloak: Boolean                                  = false
  private var canFly: Boolean                                    = false
  /** whether the vehicle gains and/or maintains ownership based on access to the driver seat<br>
    * `Some(true)` - assign ownership upon the driver mount, maintains ownership after the driver dismounts<br>
    * `Some(false)` - assign ownership upon the driver mount, becomes unowned after the driver dismounts<br>
    * `None` - does not assign ownership<br>
    * Be cautious about using `None` as the client tends to equate the driver seat as the owner's seat for many vehicles
    * and breaking from the client's convention either requires additional fields or just doesn't work.
    */
  private var canBeOwned: Option[Boolean]                        = Some(true)
  private var serverVehicleOverrideSpeeds: (Int, Int)            = (0, 0)
  var undergoesDecay: Boolean                                    = true
  private var deconTime: Option[FiniteDuration]                  = None
  private var defaultCapacitor: Int                              = 0
  private var maxCapacitor: Int                                  = 0
  private var capacitorRecharge: Int                             = 0
  private var capacitorDrain: Int                                = 0
  private var capacitorDrainSpecial: Int                         = 0
  /**
    * extend the time of the final scrapping and explosion further beyond when the vehicle is functionally rendered destroyed;
    * see `innateDamage` for explosion information;
    * for BFR's, the ADB field is `death_large_explosion_interval`
    */
  var destructionDelay: Option[Long]                             = None
  private var destroyedModel: Option[DestroyedVehicle.Value]     = None
  Name = "vehicle"
  Packet = VehicleDefinition.converter
  DamageUsing = DamageCalculations.AgainstVehicle
  ResistUsing = StandardVehicleResistance
  Model = VehicleResolutions.calculate
  RepairDistance = 10
  RepairRestoresAt = 1
  registerAs = "vehicles"

  def DefaultShields: Int = defaultShields.getOrElse(0)

  def DefaultShields_=(shield: Int): Int = DefaultShields_=(Some(shield))

  def DefaultShields_=(shield: Option[Int]): Int = {
    defaultShields = shield
    DefaultShields
  }

  def MaxShields: Int = maxShields

  def MaxShields_=(shields: Int): Int = {
    maxShields = shields
    MaxShields
  }

  def ShieldPeriodicDelay : Long = shieldChargePeriodicCooldown

  def ShieldPeriodicDelay_=(cooldown: Long): Long = {
    shieldChargePeriodicCooldown = cooldown
    ShieldPeriodicDelay
  }

  def ShieldDamageDelay: Long = shieldChargeDamageCooldown

  def ShieldDamageDelay_=(cooldown: Long): Long = {
    shieldChargeDamageCooldown = cooldown
    ShieldDamageDelay
  }

  def ShieldAutoRecharge: Option[Int] = autoShieldRecharge

  def ShieldAutoRecharge_=(charge: Int): Option[Int] = ShieldAutoRecharge_=(Some(charge))

  def ShieldAutoRecharge_=(charge: Option[Int]): Option[Int] = {
    autoShieldRecharge = charge
    ShieldAutoRecharge
  }

  def ShieldAutoRechargeSpecial: Option[Int] = autoShieldRechargeSpecial.orElse(ShieldAutoRecharge)

  def ShieldAutoRechargeSpecial_=(charge: Int): Option[Int] = ShieldAutoRechargeSpecial_=(Some(charge))

  def ShieldAutoRechargeSpecial_=(charge: Option[Int]): Option[Int] = {
    autoShieldRechargeSpecial = charge
    ShieldAutoRechargeSpecial
  }

  def ShieldDrain: Option[Int] = shieldDrain

  def ShieldDrain_=(drain: Int): Option[Int] = ShieldDrain_=(Some(drain))

  def ShieldDrain_=(drain: Option[Int]): Option[Int] = {
    shieldDrain = drain
    ShieldDrain
  }

  def Cargo: mutable.HashMap[Int, CargoDefinition] = cargo

  def CanBeOwned: Option[Boolean] = canBeOwned

  def CanBeOwned_=(ownable: Boolean): Option[Boolean] = CanBeOwned_=(Some(ownable))

  def CanBeOwned_=(ownable: Option[Boolean]): Option[Boolean] = {
    canBeOwned = ownable
    CanBeOwned
  }

  def CanCloak: Boolean = canCloak

  def CanCloak_=(cloakable: Boolean): Boolean = {
    canCloak = cloakable
    CanCloak
  }

  def CanFly: Boolean = canFly

  def CanFly_=(flying: Boolean): Boolean = {
    canFly = flying
    CanFly
  }

  def Deployment: Boolean = deployment

  def Deployment_=(deployable: Boolean): Boolean = {
    deployment = deployable
    Deployment
  }

  def Utilities: mutable.HashMap[Int, UtilityType.Value] = utilities

  def UtilityOffset: mutable.HashMap[Int, Vector3] = utilityOffsets

  def DeployTime: Int = deploymentTime_Deploy

  def DeployTime_=(dtime: Int): Int = {
    deploymentTime_Deploy = dtime
    DeployTime
  }

  def DeconstructionTime: Option[FiniteDuration] = deconTime

  def DeconstructionTime_=(time: FiniteDuration): Option[FiniteDuration] = {
    deconTime_=(Some(time))
    DeconstructionTime
  }

  def DeconstructionTime_=(time: Option[FiniteDuration]): Option[FiniteDuration] = {
    deconTime = time
    DeconstructionTime
  }

  def UndeployTime: Int = deploymentTime_Undeploy

  def UndeployTime_=(dtime: Int): Int = {
    deploymentTime_Undeploy = dtime
    UndeployTime
  }

  def TrunkSize: InventoryTile = trunkSize

  def TrunkSize_=(tile: InventoryTile): InventoryTile = {
    trunkSize = tile
    TrunkSize
  }

  def TrunkOffset: Int = trunkOffset

  def TrunkOffset_=(offset: Int): Int = {
    trunkOffset = offset
    TrunkOffset
  }

  def TrunkLocation: Vector3 = trunkLocation

  def TrunkLocation_=(location: Vector3): Vector3 = {
    trunkLocation = location
    TrunkLocation
  }

  def AutoPilotSpeeds: (Int, Int) = serverVehicleOverrideSpeeds

  def AutoPilotSpeeds_=(speeds: (Int, Int)): (Int, Int) = {
    serverVehicleOverrideSpeeds = speeds
    AutoPilotSpeeds
  }

  def AutoPilotSpeed1: Int = serverVehicleOverrideSpeeds._1

  def AutoPilotSpeed2: Int = serverVehicleOverrideSpeeds._2

  def DefaultCapacitor: Int = defaultCapacitor

  def DefaultCapacitor_=(defValue: Int): Int = {
    defaultCapacitor = defValue
    DefaultCapacitor
  }

  def MaxCapacitor : Int = maxCapacitor

  def MaxCapacitor_=(max: Int) : Int = {
    maxCapacitor = max
    MaxCapacitor
  }

  def CapacitorRecharge: Int = capacitorRecharge

  def CapacitorRecharge_=(charge: Int): Int = {
    capacitorRecharge = charge
    CapacitorRecharge
  }

  def CapacitorDrain: Int = capacitorDrain

  def CapacitorDrain_=(charge: Int): Int = {
    capacitorDrain = charge
    CapacitorDrain
  }

  def CapacitorDrainSpecial: Int = capacitorDrainSpecial

  def CapacitorDrainSpecial_=(charge: Int): Int = {
    capacitorDrainSpecial = charge
    CapacitorDrainSpecial
  }

  private var jackDuration        = Array(0, 0, 0, 0)
  def JackingDuration: Array[Int] = jackDuration
  def JackingDuration_=(arr: Array[Int]): Array[Int] = {
    jackDuration = arr
    arr
  }

  def DestroyedModel: Option[DestroyedVehicle.Value] = destroyedModel

  def DestroyedModel_=(model: Option[DestroyedVehicle.Value]): Option[DestroyedVehicle.Value] = {
    destroyedModel = model
    DestroyedModel
  }

  def Initialize(obj: Vehicle, context: ActorContext): Unit = {
    import net.psforever.objects.vehicles.control.VehicleControl
    obj.Actor = context.actorOf(
      Props(classOf[VehicleControl], obj),
      PlanetSideServerObject.UniqueActorName(obj)
    )
  }

  def Uninitialize(obj: Vehicle, context: ActorContext): Unit = {
    obj.Actor ! akka.actor.PoisonPill
    obj.Actor = Default.Actor
  }

  override val collision: AdvancedCollisionData = new AdvancedCollisionData()
}

object VehicleDefinition {
  private val converter = new VehicleConverter

  def apply(objectId: Int): VehicleDefinition = {
    new VehicleDefinition(objectId)
  }

  protected class AmsDefinition(objectId: Int) extends VehicleDefinition(objectId) {
    import net.psforever.objects.vehicles.control.AmsControl
    override def Initialize(obj: Vehicle, context: ActorContext): Unit = {
      obj.Actor = context.actorOf(
        Props(classOf[AmsControl], obj),
        PlanetSideServerObject.UniqueActorName(obj)
      )
    }
  }
  /**
    * Vehicle definition for the advanced mobile spawn (AMS) vehicle.
    * @param objectId the object id that is associated with this sort of `Vehicle`
    */
  def Ams(objectId: Int): VehicleDefinition = new AmsDefinition(objectId)

  protected class AntDefinition(objectId: Int) extends VehicleDefinition(objectId) {
    import net.psforever.objects.vehicles.control.AntControl
    override def Initialize(obj: Vehicle, context: ActorContext): Unit = {
      obj.Actor = context.actorOf(
        Props(classOf[AntControl], obj),
        PlanetSideServerObject.UniqueActorName(obj)
      )
    }
  }
  /**
    * Vehicle definition for the advanced nanite transport (ANT) vehicle.
    * @param objectId the object id that is associated with this sort of `Vehicle`
    */
  def Ant(objectId: Int): VehicleDefinition = new AntDefinition(objectId)

  protected class ApcDefinition(objectId: Int) extends VehicleDefinition(objectId) {
    import net.psforever.objects.vehicles.control.ApcControl
    override def Initialize(obj: Vehicle, context: ActorContext): Unit = {
      obj.Actor = context.actorOf(
        Props(classOf[ApcControl], obj),
        PlanetSideServerObject.UniqueActorName(obj)
      )
    }
  }
  /**
    * Vehicle definition(s) for the armored personnel carrier (`apc*`) vehicles.
    * @param objectId the object id that is associated with this sort of `Vehicle`
    */
  def Apc(objectId: Int): VehicleDefinition = new ApcDefinition(objectId)

  protected class BfrDefinition(objectId: Int) extends VehicleDefinition(objectId) {
    import net.psforever.objects.vehicles.control.BfrControl
    override def Initialize(obj: Vehicle, context: ActorContext): Unit = {
      obj.Actor = context.actorOf(
        Props(classOf[BfrControl], obj),
        PlanetSideServerObject.UniqueActorName(obj)
      )
    }
  }
  /**
    * Vehicle definition(s) for the battle frame robotics vehicles.
    * @param objectId the object id that is associated with this sort of `Vehicle`
    */
  def Bfr(objectId: Int): VehicleDefinition = new BfrDefinition(objectId)

  protected class BfrFlightDefinition(objectId: Int) extends VehicleDefinition(objectId) {
    import net.psforever.objects.vehicles.control.BfrFlightControl
    override def Initialize(obj: Vehicle, context: ActorContext): Unit = {
      obj.Actor = context.actorOf(
        Props(classOf[BfrFlightControl], obj),
        PlanetSideServerObject.UniqueActorName(obj)
      )
    }
  }
  /**
    * Vehicle definition(s) for the flight variant of the battle frame robotics vehicles.
    * @param objectId the object id that is associated with this sort of `Vehicle`
    */
  def BfrFlight(objectId: Int): VehicleDefinition = new BfrFlightDefinition(objectId)

  protected class CarrierDefinition(objectId: Int) extends VehicleDefinition(objectId) {
    import net.psforever.objects.vehicles.control.CargoCarrierControl
    override def Initialize(obj: Vehicle, context: ActorContext): Unit = {
      obj.Actor = context.actorOf(
        Props(classOf[CargoCarrierControl], obj),
        PlanetSideServerObject.UniqueActorName(obj)
      )
    }
  }
  /**
    * Vehicle definition(s) for the vehicles (carriers) that are used to transport other vehicles (cargo).
    * @param objectId the object id that is associated with this sort of `Vehicle`
    */
  def Carrier(objectId: Int): VehicleDefinition = new CarrierDefinition(objectId)

  protected class DeployingDefinition(objectId: Int) extends VehicleDefinition(objectId) {
    import net.psforever.objects.vehicles.control.DeployingVehicleControl
    override def Initialize(obj: Vehicle, context: ActorContext): Unit = {
      obj.Actor = context.actorOf(
        Props(classOf[DeployingVehicleControl], obj),
        PlanetSideServerObject.UniqueActorName(obj)
      )
    }
  }
  /**
    * Vehicle definition(s) for the vehicles that perform significant mode state transitions.
    * @param objectId the object id that is associated with this sort of `Vehicle`
    */
  def Deploying(objectId: Int): VehicleDefinition = new DeployingDefinition(objectId)

  protected class RouterDefinition(objectId: Int) extends VehicleDefinition(objectId) {
    import net.psforever.objects.vehicles.control.RouterControl
    override def Initialize(obj: Vehicle, context: ActorContext): Unit = {
      obj.Actor = context.actorOf(
        Props(classOf[RouterControl], obj),
        PlanetSideServerObject.UniqueActorName(obj)
      )
    }
  }
  /**
    * Vehicle definition for the Router.
    * @param objectId the object id that is associated with this sort of `Vehicle`
    */
  def Router(objectId: Int): VehicleDefinition = new RouterDefinition(objectId)
}
