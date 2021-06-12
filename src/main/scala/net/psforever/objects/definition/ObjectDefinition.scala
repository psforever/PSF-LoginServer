// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.converter.{ObjectCreateConverter, PacketConverter}
import net.psforever.objects.geometry.GeometryForm
import net.psforever.objects.geometry.d3.VolumetricGeometry
import net.psforever.types.OxygenState

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
abstract class ObjectDefinition(private val objectId: Int) extends BasicDefinition {

  /** a data converter for this type of object */
  protected var packet: PacketConverter = new ObjectCreateConverter[PlanetSideGameObject]() {}
  Name = "object definition"

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

  def ObjectId: Int = objectId
}
