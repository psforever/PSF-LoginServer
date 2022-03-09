// Copyright (c) 2021 PSForever
package net.psforever.objects.zones

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.guid.{GUIDTask, StraightforwardTask, TaskBundle, TaskWorkflow}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.types.PlanetSideGUID

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Synchronize management of the list of some `Projectile`s maintained by a zone.
  * @param zone the zone being represented
  * @param projectileList the zone's projectile list
  */
class ZoneProjectileActor(
                           zone: Zone,
                           projectileList: mutable.ListBuffer[Projectile]
                         ) extends Actor {
  /** a series of timers matched against projectile unique identifiers,
    * marking the maximum lifespan of the projectile */
  val projectileLifespan: mutable.HashMap[PlanetSideGUID, Cancellable] = new mutable.HashMap[PlanetSideGUID, Cancellable]

  override def postStop() : Unit = {
    projectileLifespan.values.foreach { _.cancel() }
    projectileList.iterator.filter(_.HasGUID).foreach { p => cleanUpRemoteProjectile(p.GUID, p) }
    projectileList.clear()
  }

  def receive: Receive = {
    case ZoneProjectile.Add(filterGuid, projectile) =>
      if (projectile.Definition.ExistsOnRemoteClients) {
        if (projectile.HasGUID) {
          cleanUpRemoteProjectile(projectile.GUID, projectile)
          TaskWorkflow.execute(reregisterProjectile(filterGuid, projectile))
        } else {
          TaskWorkflow.execute(registerProjectile(filterGuid, projectile))
        }
      }

    case ZoneProjectile.Remove(guid) =>
      projectileList.find(_.GUID == guid) match {
        case Some(projectile) =>
          cleanUpRemoteProjectile(guid, projectile)
          TaskWorkflow.execute(unregisterProjectile(projectile))
        case _ =>
          projectileLifespan.remove(guid)
          //if we can't find this projectile by guid, remove any projectiles that are unregistered
          val (in, out) = projectileList.filter(_.HasGUID).partition { p => zone.GUID(p.GUID).nonEmpty }
          projectileList.clear()
          projectileList.addAll(in)
          out.foreach { p =>
            cleanUpRemoteProjectile(p.GUID, p)
          }
      }

    case _ => ;
  }

  /**
    * Construct tasking that adds a completed but unregistered projectile into the scene.
    * After the projectile is registered to the curent zone's global unique identifier system,
    * all connected clients save for the one that registered it will be informed about the projectile's "creation."
    * @param obj the projectile to be registered
    * @return a `TaskBundle` message
    */
  private def registerProjectile(filterGuid: PlanetSideGUID, obj: Projectile): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        private val filter = filterGuid
        private val globalProjectile = obj
        private val func: (PlanetSideGUID, PlanetSideGUID, Projectile) => Unit = loadedRemoteProjectile

        override def description(): String = s"register a ${globalProjectile.profile.Name}"

        def action(): Future[Any] = {
          func(filter, globalProjectile.GUID, globalProjectile)
          Future(true)
        }
      },
      List(GUIDTask.registerObject(zone.GUID, obj))
    )
  }

  /**
    * Construct tasking that removes a formerly complete and currently registered projectile from the scene.
    * After the projectile is unregistered from the curent zone's global unique identifier system,
    * all connected clients save for the one that registered it will be informed about the projectile's "destruction."
    * @param obj the projectile to be unregistered
    * @return a `TaskBundle` message
    */
  private def unregisterProjectile(obj: Projectile): TaskBundle = GUIDTask.unregisterObject(zone.GUID, obj)

  /**
    * If the projectile object is unregistered, register it.
    * If the projectile object is already registered, unregister it and then register it again.
    * @see `registerProjectile(Projectile)`
    * @see `unregisterProjectile(Projectile)`
    * @param obj the projectile to be registered (a second time?)
    * @return a `TaskBundle` message
    */
  def reregisterProjectile(filterGuid: PlanetSideGUID, obj: Projectile): TaskBundle = {
    val reg = registerProjectile(filterGuid, obj)
    if (obj.HasGUID) {
      TaskBundle(
        reg.mainTask,
        TaskBundle(
          reg.subTasks(0).mainTask,
          unregisterProjectile(obj)
        )
      )
    } else {
      reg
    }
  }

  /**
    * For a given registered remote projectile,
    * perform all the actions necessary to properly integrate it into the management system.<br>
    * <br>
    * Those actions involve:<br>
    * - determine whether or not the default filter needs to be applied,<br>
    * - add the projectile to the zone managing list,<br>
    * - if the projectile is a radiation cloud, add it to the zone blockmap<br>
    * - set up the internal disposal timer, and<br>
    * - dispatch a message to introduce the projectile to the game world.
    * @param filterGuid a unique identifier filtering messages from a certain recipient
    * @param projectileGuid the projectile unique identifier that was assigned by the zone's unique number system
    * @param projectile the projectile being included
    */
  def loadedRemoteProjectile(
                              filterGuid: PlanetSideGUID,
                              projectileGuid: PlanetSideGUID,
                              projectile: Projectile
                            ): Unit = {
    val definition = projectile.Definition
    projectileList.addOne(projectile)
    val (clarifiedFilterGuid, duration) = if (definition.radiation_cloud) {
      zone.blockMap.addTo(projectile)
      (Service.defaultPlayerGUID, projectile.profile.Lifespan seconds)
    } else if (definition.RemoteClientData == (0,0)) {
      //remote projectiles that are not radiation clouds have lifespans controlled by the controller (user)
      //this projectile has defaulted remote client data
      (Service.defaultPlayerGUID, projectile.profile.Lifespan * 1.5f seconds)
    } else {
      //remote projectiles that are not radiation clouds have lifespans controlled by the controller (user)
      //if the controller fails, the projectile has a bit more than its normal lifespan before automatic clean up
      (filterGuid, projectile.profile.Lifespan * 1.5f seconds)
    }
    projectileLifespan.put(
      projectileGuid,
      context.system.scheduler.scheduleOnce(duration, self, ZoneProjectile.Remove(projectileGuid))
    )
    zone.AvatarEvents ! AvatarServiceMessage(
      zone.id,
      AvatarAction.LoadProjectile(
        clarifiedFilterGuid,
        definition.ObjectId,
        projectileGuid,
        definition.Packet.ConstructorData(projectile).get
      )
    )
  }

  /**
    * For a given registered remote projectile, perform all the actions necessary to properly dispose of it.
    * The projectile doesn't have to be registered at the moment,
    * but you do need to know it's (previous) globally unique identifier.<br>
    * <br>
    * Those actions involve:<br>
    * - remove and cancel the internal disposal timer,<br>
    * - if the projectile is a radiation cloud, remove it from the zone blockmap<br>
    * - remove the projectile from the zone managing list, and<br>
    * - dispatch messages to eliminate the projectile from the game world.
    * @param projectile_guid the globally unique identifier of the projectile
    * @param projectile the projectile
    */
  def cleanUpRemoteProjectile(projectile_guid: PlanetSideGUID, projectile: Projectile): Unit = {
    projectileLifespan.remove(projectile_guid) match {
      case Some(c) => c.cancel()
      case _ => ;
    }
    projectileList.remove(projectileList.indexOf(projectile))
    if (projectile.Definition.radiation_cloud) {
      zone.blockMap.removeFrom(projectile)
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        AvatarAction.ObjectDelete(PlanetSideGUID(0), projectile_guid, 2)
      )
    } else if (projectile.Definition.RemoteClientData == (0,0)) {
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        AvatarAction.ObjectDelete(PlanetSideGUID(0), projectile_guid, 2)
      )
    } else {
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        AvatarAction.ProjectileExplodes(PlanetSideGUID(0), projectile_guid, projectile)
      )
    }
  }
}

object ZoneProjectile {
  /**
    * Start monitoring the projectile.
    * @param filterGuid a unique identifier filtering messages from a certain recipient
    * @param projectile the projectile being included
    */
  final case class Add(filterGuid: PlanetSideGUID, projectile: Projectile)

  object Add {
    /**
      * Overloaded constructor for `Add` which onyl requires the projectile
      * and defaults the filtering.
      * @param projectile the projectile being included
      * @return an `Add` message
      */
    def apply(projectile: Projectile): Add = Add(PlanetSideGUID(0), projectile)
  }

  /**
    * Stop the projectile from being monitored.
    * @param guid the projectile assigned global unique identifier;
    *             not the same as the client local unique identifier (40100 to 40125)
    */
  final case class Remove(guid: PlanetSideGUID)
}
