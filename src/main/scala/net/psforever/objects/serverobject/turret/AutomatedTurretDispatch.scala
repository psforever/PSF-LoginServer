// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.ActorRef
import net.psforever.objects.serverobject.turret.AutomatedTurret.Target
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.{ChangeFireStateMessage_Start, ChangeFireStateMessage_Stop, ObjectDetectedMessage}
import net.psforever.services.Service
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.PlanetSideGUID

/**
 * Dispatch messages from an `AutomatedTurret` entity's control agency
 * with respects to the kind of entity which is the target.
 * The main sticking point is that the message bus destination matches the type of message envelope.
 * The packet messages utilized are the same either way and are tied to the action rather than the transmission process.
 * @see `ChangeFireStateMessage_Start`
 * @see `ChangeFireStateMessage_Stop`
 * @see `ObjectDetectedMessage`
 * @see `PlanetSideGamePacket`
 * @see `Zone`
 */
trait AutomatedTurretDispatch {
  /**
   * The event bus should be accessible from the target's knowledge of their zone.
   * @param target something the turret can potentially shoot at
   * @return event bus to use
   */
  def getEventBus(target: Target): ActorRef

  /**
   * The event bus should be accessible from the target's knowledge of their zone.
   * @param channel the scope of the message transmission
   * @param msg the packet to be dispatched
   * @return messaging envelope to use
   */
  def composeMessageEnvelope(channel: String, msg: PlanetSideGamePacket): Any

  /**
   * Are we tracking an entity?
   */
  def startTracking(target: Target, channel: String, turretGuid: PlanetSideGUID, list: List[PlanetSideGUID]): Unit = {
    getEventBus(target) ! composeMessageEnvelope(channel, startTrackingMsg(turretGuid, list))
  }

  /**
   * Are we no longer tracking an entity?
   */
  def stopTracking(target: Target, channel: String, turretGuid: PlanetSideGUID): Unit = {
    getEventBus(target) ! composeMessageEnvelope(channel, stopTrackingMsg(turretGuid))
  }

  /**
   * Are we shooting at an entity?
   */
  def startShooting(target: Target, channel: String, weaponGuid: PlanetSideGUID): Unit = {
    getEventBus(target) ! composeMessageEnvelope(channel, startShootingMsg(weaponGuid))
  }

  /**
   * Are we no longer shooting at an entity?
   */
  def stopShooting(target: Target, channel: String, weaponGuid: PlanetSideGUID): Unit = {
    getEventBus(target) ! composeMessageEnvelope(channel, stopShootingMsg(weaponGuid))
  }

  /**
   * Will we be shooting at an entity?
   */
  def testNewDetected(target: Target, channel: String, turretGuid: PlanetSideGUID, weaponGuid: PlanetSideGUID): Unit = {
    startTracking(target, channel, turretGuid, List(target.GUID))
    startShooting(target, channel, weaponGuid)
    stopShooting(target, channel, weaponGuid)
  }

  private def startTrackingMsg(guid: PlanetSideGUID, list: List[PlanetSideGUID]): PlanetSideGamePacket = {
    ObjectDetectedMessage(guid, guid, 0, list)
  }

  private def stopTrackingMsg(turretGuid: PlanetSideGUID): PlanetSideGamePacket = {
    ObjectDetectedMessage(turretGuid, turretGuid, 0, AutomatedTurretDispatch.noTargets)
  }

  private def startShootingMsg(weaponGuid: PlanetSideGUID): PlanetSideGamePacket = {
    ChangeFireStateMessage_Start(weaponGuid)
  }

  private def stopShootingMsg(weaponGuid: PlanetSideGUID): PlanetSideGamePacket = {
    ChangeFireStateMessage_Stop(weaponGuid)
  }
}

object AutomatedTurretDispatch {
  private val noTargets: List[PlanetSideGUID] = List(Service.defaultPlayerGUID)

  object Generic extends AutomatedTurretDispatch {
    def getEventBus(target: Target): ActorRef = {
      target.Zone.LocalEvents
    }

    def composeMessageEnvelope(channel: String, msg: PlanetSideGamePacket): Any = {
      LocalServiceMessage(channel, LocalAction.SendResponse(msg))
    }
  }

  object Vehicle extends AutomatedTurretDispatch {
    def getEventBus(target: Target): ActorRef = {
      target.Zone.VehicleEvents
    }

    def composeMessageEnvelope(channel: String, msg: PlanetSideGamePacket): Any = {
      VehicleServiceMessage(channel, VehicleAction.SendResponse(Service.defaultPlayerGUID, msg))
    }
  }
}
