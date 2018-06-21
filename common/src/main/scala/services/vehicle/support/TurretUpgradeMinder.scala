// Copyright (c) 2017 PSForever
package services.vehicle.support

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.{AmmoBox, DefaultCancellable, Tool}
import net.psforever.objects.guid.{GUIDTask, Task, TaskResolver}
import net.psforever.objects.serverobject.turret.{MannedTurret, TurretUpgrade}
import net.psforever.objects.vehicles.MountedWeapons
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import services.vehicle.{VehicleAction, VehicleServiceMessage}
import services.{Service, ServiceManager}

import scala.concurrent.duration._

class TurretUpgradeMinder extends Actor {
  var task : Cancellable = DefaultCancellable.obj

  var list : List[TurretUpgradeMinder.Entry] = List()

  private var taskResolver : ActorRef = Actor.noSender

  private[this] val log = org.log4s.getLogger

  override def preStart() : Unit = {
    super.preStart()
    self ! Service.Startup()
  }

  def receive : Receive = {
    case Service.Startup() =>
      ServiceManager.serviceManager ! ServiceManager.Lookup("taskResolver") //ask for a resolver to deal with the GUID system

    case ServiceManager.LookupResult("taskResolver", endpoint) =>
      taskResolver = endpoint
      context.become(Processing)

    case msg =>
      log.error(s"received message $msg before being properly initialized")
  }

  def Processing : Receive = {
    case TurretUpgradeMinder.AddTask(turret, zone, upgrade, duration) =>
      val lengthOfTime = duration.getOrElse(TurretUpgradeMinder.StandardUpgradeLifetime).toNanos
      if(lengthOfTime > (1 second).toNanos) { //don't even bother if it's too short; it'll revert near instantly
        val entry = TurretUpgradeMinder.Entry(turret, zone, TurretUpgrade.None, lengthOfTime)
        UpgradeTurretAmmo(TurretUpgradeMinder.Entry(turret, zone, upgrade, lengthOfTime))
        if(list.isEmpty) {
          //we were the only entry so the event must be started from scratch
          list = List(entry)
          //trace(s"a remover task has been added: $entry")
          RetimeFirstTask()
        }
        else{
          val oldHead = list.head
          if(!list.exists(test => TurretUpgradeMinder.Similarity(test, entry))) {
            list = (list :+ entry).sortBy(_.duration)
            //trace(s"a remover task has been added: $entry")
            if(oldHead != list.head) {
              RetimeFirstTask()
            }
          }
        }
      }

    case TurretUpgradeMinder.Downgrade() =>
      task.cancel
      val now : Long = System.nanoTime
      val (in, out) = list.partition(entry => { now - entry.time >= entry.duration }) //&& entry.obj.Seats.values.count(_.isOccupied) == 0
      list = out
      in.foreach { UpgradeTurretAmmo }
      RetimeFirstTask()

    case _ => ;
  }

  def RetimeFirstTask(now : Long = System.nanoTime) : Unit = {
    task.cancel
    if(list.nonEmpty) {
      val short_timeout : FiniteDuration = math.max(1, list.head.duration - (now - list.head.time)) nanoseconds
      import scala.concurrent.ExecutionContext.Implicits.global
      task = context.system.scheduler.scheduleOnce(short_timeout, self, TurretUpgradeMinder.Downgrade())
    }
  }

  /**
    * The process of upgrading a turret is nearly complete.
    * After the upgrade status is changed, the internal structure of the turret's weapons change to suit the configuration.
    * Of special importance are internal ammo supplies of the changing weapon,
    * the original ammunition that must be un-registered,
    * and the new boxes that must be registered so the weapon may be introduced into the game world properly.
    * @param entry na
    */
  def UpgradeTurretAmmo(entry : TurretUpgradeMinder.Entry) : Unit = {
    val target = entry.obj
    val zone = entry.zone
    val zoneId = zone.Id
    val upgrade = entry.upgrade
    val guid = zone.GUID
    val turretGUID = target.GUID
    //kick all occupying players for duration of conversion
    target.Seats.values
      .filter { _.isOccupied }
      .foreach({seat =>
        val tplayer = seat.Occupant.get
        seat.Occupant = None
        tplayer.VehicleSeated = None
        if(tplayer.HasGUID) {
          context.parent ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(tplayer.GUID, 4, false, turretGUID))
        }
      })
    log.info(s"Converting manned wall turret weapon to $upgrade")

    val oldBoxesTask = AllMountedWeaponMagazines(target)
      .map(box => GUIDTask.UnregisterEquipment(box)(guid))
      .toList
    target.Upgrade = upgrade //perform upgrade

    val newBoxesTask = TaskResolver.GiveTask(
      new Task() {
        private val localFunc : ()=>Unit = FinishUpgradingTurret(entry)

        override def isComplete = Task.Resolution.Success

        def Execute(resolver : ActorRef) : Unit = {
          localFunc()
          resolver ! scala.util.Success(this)
        }
      }, AllMountedWeaponMagazines(target).map(box => GUIDTask.RegisterEquipment(box)(guid)).toList
    )
    taskResolver ! TaskResolver.GiveTask(
      new Task() {
        def Execute(resolver : ActorRef) : Unit = {
          resolver ! scala.util.Success(this)
        }
      }, oldBoxesTask :+ newBoxesTask
    )
  }

  /**
    * From an object that has mounted weapons, parse all of the internal ammunition loaded into all of the weapons.
    * @param target the object with mounted weaponry
    * @return all of the internal ammunition objects
    */
  def AllMountedWeaponMagazines(target : MountedWeapons) : Iterable[AmmoBox] = {
    target.Weapons
      .values
      .map { _.Equipment }
      .collect { case Some(tool : Tool) => tool.AmmoSlots }
      .flatMap { _.map { _.Box } }
  }

  /**
    * Finish upgrading the turret by announcing to other players that the weapon type has changed.
    * By this point, a prior required action that required that new ammunition objects had to be registered.
    * It is now safe to announce that clients can update to the new weapon.
    * @param entry na
    */
  def FinishUpgradingTurret(entry : TurretUpgradeMinder.Entry)() : Unit = {
    val target = entry.obj
    val zone = entry.zone
    log.info(s"Wall turret finished ${target.Upgrade} upgrade")
    val targetGUID = target.GUID
    target.Weapons
      .map({ case (index, slot) =>  (index, slot.Equipment) })
      .collect { case (index, Some(tool : Tool)) =>
        context.parent ! VehicleServiceMessage(
          zone.Id,
          VehicleAction.EquipmentInSlot(PlanetSideGUID(0), targetGUID, index, tool)
        )
      }
  }
}

object TurretUpgradeMinder {
  private val StandardUpgradeLifetime : FiniteDuration = 30 minutes

  case class Entry(obj : MannedTurret, zone : Zone, upgrade : TurretUpgrade.Value, duration : Long) {
    val time : Long = System.nanoTime
  }

  final case class AddTask(turret : MannedTurret, zone : Zone, upgrade : TurretUpgrade.Value, duration : Option[FiniteDuration] = None)

  final case class Downgrade()

  private def Similarity(entry1 : TurretUpgradeMinder.Entry, entry2 : TurretUpgradeMinder.Entry) : Boolean = {
    entry1.obj == entry2.obj && entry1.zone == entry2.zone && entry1.obj.GUID == entry2.obj.GUID
  }
}
