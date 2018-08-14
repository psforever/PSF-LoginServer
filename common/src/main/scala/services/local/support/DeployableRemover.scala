// Copyright (c) 2017 PSForever
package services.local.support

import net.psforever.objects.guid.{GUIDTask, TaskResolver}
import net.psforever.objects.zones.Zone
import net.psforever.objects.{BoomerDeployable, Deployable, PlanetSideGameObject, TurretDeployable}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3
import services.RemoverActor

import scala.concurrent.duration._

class DeployableRemover extends RemoverActor {
  final val FirstStandardDuration : FiniteDuration = 3 minutes

  final val SecondStandardDuration : FiniteDuration = 2 seconds

  def InclusionTest(entry : RemoverActor.Entry) : Boolean = entry.obj.isInstanceOf[Deployable]

  def InitialJob(entry : RemoverActor.Entry) : Unit = { }

  def FirstJob(entry : RemoverActor.Entry) : Unit = {
    entry.zone.Deployables ! Zone.Deployable.Dismiss(entry.obj.asInstanceOf[PlanetSideGameObject with Deployable])
  }

  override def SecondJob(entry : RemoverActor.Entry) : Unit = {
    entry.obj match {
      case obj : BoomerDeployable =>
        SecondJobBoomer(obj, entry)
      case obj : PlanetSideGameObject with Deployable =>
        SecondJobDeployable(obj, entry)
      case _ => ; //impossible case
    }
    super.SecondJob(entry)
  }

  def SecondJobBoomer(obj : BoomerDeployable, entry : RemoverActor.Entry) : Unit = {
    obj.Trigger match {
      case Some(trigger) =>
        obj.Trigger = None
        if(trigger.HasGUID) {
          val guid = trigger.GUID
          Zone.EquipmentIs.Where(trigger, guid, entry.zone) match {
            case Some(Zone.EquipmentIs.InContainer(container, index)) =>
              container.Slot(index).Equipment = None
            case Some(Zone.EquipmentIs.OnGround(_)) =>
              entry.zone.Ground ! Zone.Ground.RemoveItem(guid)
            case _ => ;
          }
          context.parent ! DeployableRemover.DeleteTrigger(guid, entry.zone)
          taskResolver ! GUIDTask.UnregisterObjectTask(trigger)(entry.zone.GUID)
        }
      case None => ;
    }
    SecondJobDeployable(obj, entry)
  }

  def SecondJobDeployable(obj : PlanetSideGameObject with Deployable, entry : RemoverActor.Entry) : Unit = {
    context.parent ! DeployableRemover.EliminateDeployable(obj, obj.GUID, obj.Position, entry.zone)
  }

  def ClearanceTest(entry : RemoverActor.Entry) : Boolean = !entry.zone.DeployableList.contains(entry.obj)

  def DeletionTask(entry : RemoverActor.Entry) : TaskResolver.GiveTask = {
    entry.obj match {
      case turret : TurretDeployable =>
        GUIDTask.UnregisterDeployableTurret(turret)(entry.zone.GUID)
      case obj =>
        GUIDTask.UnregisterObjectTask(obj)(entry.zone.GUID)
    }
  }
}

object DeployableRemover {
  final case class EliminateDeployable(obj : PlanetSideGameObject with Deployable,
                                       guid : PlanetSideGUID,
                                       position : Vector3,
                                       zone : Zone)

  final case class DeleteTrigger(trigger_guid : PlanetSideGUID,
                                 zone_id : Zone)
}
