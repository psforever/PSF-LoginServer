// Copyright (c) 2019 PSForever
package net.psforever.objects

import net.psforever.objects.definition.{ObjectDefinition, VehicleDefinition}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3

import scala.collection.mutable

trait SpawnPoint {
  psso : PlanetSideServerObject =>
  /**
    * An element of the contract of `PlanetSideServerObject`;
    * but, this makes it visible to a `SpawnPoint` object without casting.
    * @see `Identifiable.GUID`
    */
  def GUID : PlanetSideGUID
  /**
    * An element of the contract of `PlanetSideServerObject`;
    * but, this makes it visible to a `SpawnPoint` object without casting.
    * @see `WorldEntity.GUID`
    * @see `SpecificPoint`
    */
  def Position : Vector3
  /**
    * An element of the contract of `PlanetSideServerObject`;
    * but, this makes it visible to a `SpawnPoint` object without casting.
    * @see `WorldEntity.GUID`
    * @see `SpecificPoint`
    */
  def Orientation : Vector3
  /**
    * An element of an unspoken contract with `Amenity`.
    * While not all `SpawnPoint` objects will be `Amenity` objects, a subclass of the `PlanetSideServerObject` class,
    * they will all promote having an object owner, or "parent."
    * This should generally be themselves.
    * @see `Amenity.Owner`
    */
  def Owner : PlanetSideServerObject
  /**
    * An element of the contract of `PlanetSideServerObject`;
    * but, this makes it visible to a `SpawnPoint` object without casting.
    * @see `PlanetSideGameObject.Definition`
    * @see `SpecificPoint`
    */
  def Definition : ObjectDefinition with SpawnPointDefinition

  /**
    * Determine a specific position and orientation in which to spawn the target.
    * @return a `Tuple` of `Vector3` objects;
    *         the first represents the game world position of spawning;
    *         the second represents the game world direction of spawning
    */
  def SpecificPoint(target : PlanetSideGameObject) : (Vector3, Vector3) = {
    psso.Definition match {
      case d : SpawnPointDefinition =>
        d.SpecificPoint(this, target)
      case _ =>
        SpawnPoint.Default(this, target)
    }
  }
}

object SpawnPoint {
  def Default(obj : SpawnPoint, target : PlanetSideGameObject) : (Vector3, Vector3) = (obj.Position, obj.Orientation)

  def Tube(obj : SpawnPoint, target : PlanetSideGameObject) : (Vector3, Vector3) = (
    obj.Position + Vector3.z(1.5f),
    obj.Orientation.xy + Vector3.z(obj.Orientation.z + 90 % 360)
  )

  def AMS(obj : SpawnPoint, target : PlanetSideGameObject) : (Vector3, Vector3) = {
    //position the player alongside either of the AMS's terminals, facing away from it
    val ori = obj.Orientation
    val side = if(System.currentTimeMillis() % 2 == 0) 1 else -1 //right | left
    val x = ori.x
    val xsin = 3 * side * math.abs(math.sin(math.toRadians(x))).toFloat + 0.5f //sin because 0-degrees is up
    val z = ori.z
    val zrot = (z + 90) % 360
    val zrad = math.toRadians(zrot)
    val shift = Vector3(math.sin(zrad).toFloat, math.cos(zrad).toFloat, 0) * (3 * side) //x=sin, y=cos because compass-0 is East, not North
    (
      obj.Position + shift + (if(x >= 330) { //ams leaning to the left
        Vector3.z(xsin)
      }
      else { //ams leaning to the right
        Vector3.z(-xsin)
      }),
      if(side == 1) {
        Vector3.z(zrot)
      }
      else {
        Vector3.z((z - 90) % 360)
      }
    )
  }

  def Gate(obj : SpawnPoint, target : PlanetSideGameObject) : (Vector3, Vector3) = {
    obj.Definition match {
      case d : SpawnPointDefinition =>
        val ori = target.Orientation
        val zrad = math.toRadians(ori.z)
        val radius = scala.math.random.toFloat * d.UseRadius/2 + 20f //20 is definitely outside of the gating energy field
      val shift = Vector3(math.sin(zrad).toFloat, math.cos(zrad).toFloat, 0) * radius
        val altitudeShift = target.Definition match {
          case vdef : VehicleDefinition if GlobalDefinitions.isFlightVehicle(vdef) =>
            Vector3.z(scala.math.random.toFloat * d.UseRadius/4 + 20f)
          case _ =>
            Vector3.Zero
        }
        (obj.Position + shift + altitudeShift, ori)
      case _ =>
        Default(obj, target)
    }
  }
}

trait SpawnPointDefinition {
  private var radius : Float = 0f //m
  private var delay : Long = 0 //s
  private var noWarp : Option[mutable.Set[VehicleDefinition]] = None
  private var spawningFunc : (SpawnPoint, PlanetSideGameObject) => (Vector3, Vector3) = SpawnPoint.Default

  def UseRadius : Float = radius

  def UseRadius_=(rad : Float) : Float = {
    radius = rad
    UseRadius
  }

  def Delay : Long = delay

  def Delay_=(toDelay : Long) : Long = {
    delay = toDelay
    Delay
  }

  def VehicleAllowance : Boolean = noWarp.isDefined

  def VehicleAllowance_=(allow : Boolean) : Boolean = {
    if(allow && noWarp.isEmpty) {
      noWarp = Some(mutable.Set.empty[VehicleDefinition])
    }
    else if(!allow && noWarp.isDefined) {
      noWarp = None
    }
    VehicleAllowance
  }

  def NoWarp : mutable.Set[VehicleDefinition] = {
    noWarp.getOrElse(mutable.Set.empty[VehicleDefinition])
  }

  def SpecificPointFunc : (SpawnPoint, PlanetSideGameObject) => (Vector3, Vector3) = spawningFunc

  def SpecificPointFunc_=(func : (SpawnPoint, PlanetSideGameObject) => (Vector3, Vector3)) : Unit = {
    spawningFunc = func
  }

  def SpecificPoint(obj : SpawnPoint, target : PlanetSideGameObject) : (Vector3, Vector3) = spawningFunc(obj, target)
}
