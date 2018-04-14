// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.Actor
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.zones.Zone
import org.log4s.Logger

/**
  * Base for all `VehicleSpawnControl`-related `Actor` classes.
  * The primary purpose of this superclass is to provide a common convention for the logging system's name.
  * Additional functionality that ewcovers the `Zone` of the owned amenity `VehcileSpawnPad` is also included.
  * @param pad a `VehicleSpawnPad` object
  */
abstract class VehicleSpawnControlBase(pad : VehicleSpawnPad) extends Actor {
  /** the log reference */
  private var baseLogger : Option[Logger] = None

  /**
    * Initialize, if appropriate, and provide a log-keeping agent for the requested task.
    * If a consistent logger does not yet exist, initialize one that will be returned this time and for every subsequent request.
    * If the underlying spawn pad has not been registered yet, however, produce a throw-away logger.
    * @param logid a special identifier that distinguishes a logger whose name is built of common features
    * @return a `Logger` object
    */
  private def GetLogger(logid : String) : Logger = baseLogger match {
    case None =>
      if(!pad.HasGUID || Continent == Zone.Nowhere) {
        org.log4s.getLogger(s"uninitialized_${pad.Definition.Name}$logid")
      }
      else {
        baseLogger = Some(org.log4s.getLogger(s"${Continent.Id}-${pad.Definition.Name}-${pad.GUID.guid}$logid"))
        baseLogger.get
      }
    case Some(logger) =>
      logger
  }

  /**
    * Implement this to add a suffix to the identifying name of the logger.
    * @return a special identifier that distinguishes a logger whose name is built of common features
    */
  def LogId : String

  /**
    * Act as if a variable for the logging agent.
    * @return a `Logger` object
    */
  def log : Logger = GetLogger(LogId)

  /**
    * A common manner of utilizing the logging agent such that all messages have the same logging level.
    * The default should be set to `trace`.
    * No important messages should processed by this agent; only consume general vehicle spawn status.
    * @param msg the message
    */
  def trace(msg : String) : Unit = log.trace(msg)

  protected def Pad : VehicleSpawnPad = pad

  /**
    * The continent the pad recognizes as a place of installation will change as its `Owner` changes.
    * Originally, it belongs to a default non-`Building` object that is owned by a default non-`Zone` object called "nowhere."
    * Eventually, it will belong to an active `Building` object that belongs to an active `Zone` object with an identifier.
    * With respect to `GetLogger(String)`, the active `Zone` object will be valid shortly after the object is registered,
    * but will still be separated from being owned by a valid `Building` object by a few validation checks.
    * @return the (current) `Zone` object
    */
  def Continent : Zone = Pad.Owner.asInstanceOf[Building].Zone
}
