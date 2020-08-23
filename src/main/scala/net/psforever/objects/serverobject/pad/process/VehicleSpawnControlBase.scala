// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.pad.process

import akka.actor.Actor
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.zones.Zone
import org.log4s.Logger

/**
  * Base for all `VehicleSpawnControl`-related `Actor` classes.
  * Provide a common convention for the logging system's name.
  * Additional functionality that recovers the `Zone` of the owned amenity `VehicleSpawnPad`.
  * @param pad a `VehicleSpawnPad` object
  */
abstract class VehicleSpawnControlBase(pad: VehicleSpawnPad) extends Actor {

  /** the log reference */
  private var baseLogger: Option[Logger] = None

  /**
    * Initialize, if appropriate, and provide a log-keeping agent for the requested task.
    * If a consistent logger does not yet exist, initialize one that will be returned this time and for every subsequent request.
    * If the underlying spawn pad has not been registered yet, however, produce a throw-away logger.
    * @param logid a special identifier that distinguishes a logger whose name is built of common features
    * @return a `Logger` object
    */
  private def GetLogger(logid: String): Logger =
    baseLogger match {
      case None =>
        if (!pad.HasGUID || pad.Zone == Zone.Nowhere) {
          org.log4s.getLogger(s"uninitialized_${pad.Definition.Name}$logid")
        } else {
          baseLogger = Some(org.log4s.getLogger(s"${pad.Continent}-${pad.Definition.Name}-${pad.GUID.guid}$logid"))
          baseLogger.get
        }
      case Some(logger) =>
        logger
    }

  /**
    * Implement this to add a suffix to the identifying name of the logger.
    * @return a special identifier that distinguishes a logger whose name is built of common features
    */
  def LogId: String

  /**
    * Act as if a variable for the logging agent.
    * @return a `Logger` object
    */
  def log: Logger = GetLogger(LogId)

  /**
    * A common manner of utilizing the logging agent such that all messages have the same logging level.
    * The default should be `trace`-level comments.
    * No important messages should processed by this agent; only consume general vehicle spawn status.
    * @param msg the message
    */
  def trace(msg: String): Unit = log.trace(msg)
}
