// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.converter.{ObjectCreateConverter, PacketConverter}
import net.psforever.objects.geometry.GeometryForm
import net.psforever.objects.geometry.d3.VolumetricGeometry
import net.psforever.types.OxygenState

/*
Line  1316: add_property ams interference_range 125
Line  1392: add_property ams shared_interference_range 30
Line  5977: add_property boomer interference_range 0.2
Line  8735: add_property deployable_shield_generator deployable_interference_range 2.0
Line  8775: add_property deployable_shield_generator interference_range 125
Line  8798: add_property deployable_shield_generator shared_interference_range 60
Line 13684: add_property he_mine deployable_interference_range 0.1
Line 13695: add_property he_mine interference_range 7
Line 13709: add_property he_mine shared_interference_range 7
Line 14634: add_property jammer_mine deployable_interference_range 0.1
Line 14645: add_property jammer_mine interference_range 7
Line 14661: add_property jammer_mine shared_interference_range 7
Line 21038: add_property motion_alarm_sensor_dest interference_range 15
Line 21057: add_property motionalarmsensor deployable_interference_range 0.1
Line 21072: add_property motionalarmsensor interference_range 25
Line 24898: add_property portable_manned_turret deployable_interference_range 2.5
Line 24915: add_property portable_manned_turret interference_range 60
Line 24981: add_property portable_manned_turret shared_interference_range 40
Line 28196: add_property sensor_shield deployable_interference_range 0.1
Line 28205: add_property sensor_shield interference_range 20
Line 29572: add_property spitfire_aa deployable_interference_range 0.1
Line 29584: add_property spitfire_aa interference_range 25
Line 29608: add_property spitfire_aa shared_interference_range 25
Line 29819: add_property spitfire_cloaked deployable_interference_range 0.1
Line 29831: add_property spitfire_cloaked interference_range 25
Line 29855: add_property spitfire_cloaked shared_interference_range 25
Line 29939: add_property spitfire_turret deployable_interference_range 0.1
Line 29951: add_property spitfire_turret interference_range 25
Line 29975: add_property spitfire_turret shared_interference_range 25
Line 30231: add_property stationaryteleportpad deployable_interference_range 5.5
Line 30933: add_property tank_traps deployable_interference_range 3.0
Line 30943: add_property tank_traps interference_range 3.5
Line 30944: add_property tank_traps interference_range2 60
Line 30969: add_property tank_traps shared_interference_range 60
Line 37319: add_property zipline deployable_interference_range 5.5
 */
/**
 * Block the deployment of certain entities within a certain distance.
 * Deployable vehicles and combat engineer entities both have a deployment condition that can be influenced by these ranges.
 * Vehicles of an object type block other vehicles of that object type.
 * Combat engineering entities block combat engineering entities of the same category.
 * @param main distance between which similar deployable entities block one another (m)
 * @param shared na
 * @param deployable distance between which this entity may block deployment of other combat engineering entities (m);
 *                   defaults to 0
 */
final case class InterferenceRange(main: Float, shared: Float, deployable: Float = 0f)

object InterferenceRange {
  val None: InterferenceRange = InterferenceRange(0f, 0f)
}

/**
  * Associate an object's canned in-game representation with its basic game identification unit.
  * The extension of this `class` would identify the common data necessary to construct such a given game object.<br>
  * <br>
  * The converter transforms a game object that is created by this `ObjectDefinition` into packet data through method-calls.
  * The field for this converter is a `PacketConverter`, the superclass for `ObjectCreateConverter`;
  * the type of the mutator's parameter is `ObjectCreateConverter` of a wildcard `tparam`;
  * and, the accessor return type is `ObjectCreateConverter[PlanetSideGameObject]`, a minimum-true statement.
  * The actual type of the converter at a given point, casted or otherwise, is mostly meaningless.
  * Casting the external object does not mutate any of the types used by the methods within that object.
  * So long as it is an `ObjectCreatePacket`, those methods can be called correctly for a game object of the desired type.
  * @param objectId the object's identifier number
  */
abstract class ObjectDefinition(private val objectId: Int)
  extends BasicDefinition {
  var registerAs: String = "generic"

  /** a data converter for this type of object */
  protected var packet: PacketConverter = new ObjectCreateConverter[PlanetSideGameObject]() {}
  Name = "object_definition"

  private var useRadius: Float = 0f

  def UseRadius: Float = useRadius

  def UseRadius_=(radius: Float): Float = {
    useRadius = radius
    UseRadius
  }

  /**
    * Get the conversion object.
    * @return
    */
  final def Packet: ObjectCreateConverter[PlanetSideGameObject] =
    packet.asInstanceOf[ObjectCreateConverter[PlanetSideGameObject]]

  /**
    * Assign this definition a conversion object.
    * @param pkt the new converter
    * @return the current converter, after assignment
    */
  final def Packet_=(pkt: ObjectCreateConverter[_]): PacketConverter = {
    packet = pkt
    Packet
  }

  private var maxDepth: Float = 0 //water_maxdragdepth
  private var disableAtMaxDepth: Boolean = false
  private var drownAtMaxDepth: Boolean = false
  private var underwaterLifespan: Map[OxygenState, Long] = Map.empty //water_underwaterlifespan and water_underwaterlifespanrecovery

  def MaxDepth: Float = maxDepth

  def MaxDepth_=(height: Float): Float = {
    maxDepth = height
    MaxDepth
  }

  def DisableAtMaxDepth: Boolean = disableAtMaxDepth

  def DisableAtMaxDepth_=(drowns: Boolean): Boolean = {
    disableAtMaxDepth = drowns
    DisableAtMaxDepth
  }

  def DrownAtMaxDepth: Boolean = drownAtMaxDepth

  def DrownAtMaxDepth_=(drowns: Boolean): Boolean = {
    drownAtMaxDepth = drowns
    DrownAtMaxDepth
  }

  def UnderwaterLifespan(): Map[OxygenState, Long] = underwaterLifespan

  def UnderwaterLifespan(key: OxygenState): Long = underwaterLifespan.getOrElse(key, 1L)

  def UnderwaterLifespan(suffocation: Long, recovery: Long): Map[OxygenState, Long] = {
    underwaterLifespan = Map(OxygenState.Suffocation -> suffocation, OxygenState.Recovery -> recovery)
    UnderwaterLifespan()
  }

  private var serverSplashTargetsCentroid: Boolean = false

  def ServerSplashTargetsCentroid: Boolean = serverSplashTargetsCentroid

  def ServerSplashTargetsCentroid_=(splash: Boolean): Boolean = {
    serverSplashTargetsCentroid = splash
    ServerSplashTargetsCentroid
  }

  private var serverGeometry: Any => VolumetricGeometry = GeometryForm.representByPoint()

  def Geometry: Any => VolumetricGeometry = if (ServerSplashTargetsCentroid) {
    GeometryForm.representByPoint()
  } else {
    serverGeometry
  }

  def Geometry_=(func: Any => VolumetricGeometry): Any => VolumetricGeometry = {
    serverGeometry = func
    Geometry
  }

  /**
    * The maximum forward speed that can be expected to be achieved by this unit.
    * Faster speeds are not discounted due to conditions of the motion or game environment
    * but speeds too far beyond this measure should be considered suspicious.
    * For ground vehicles, this field is called `maxForward` in the ADB.
    * For flight vehicles, this field is called `MaxSpeed` and `flightmaxspeed` in the ADB,
    * and it does not factor in the afterburner.
    */
  var maxForwardSpeed: Float = 0f

  /**
   * na
   */
  var interference: InterferenceRange = InterferenceRange.None

  def ObjectId: Int = objectId
}
