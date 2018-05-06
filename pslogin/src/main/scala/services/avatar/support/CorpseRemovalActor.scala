// Copyright (c) 2017 PSForever
package services.avatar.support

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.guid.TaskResolver
import net.psforever.objects.{DefaultCancellable, Player}
import net.psforever.objects.zones.Zone
import net.psforever.types.Vector3
import services.{Service, ServiceManager}
import services.ServiceManager.Lookup
import services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.annotation.tailrec
import scala.concurrent.duration._

class CorpseRemovalActor extends Actor {
  private var burial : Cancellable = DefaultCancellable.obj
  private var corpses : List[CorpseRemovalActor.Entry] = List()

  private var decomposition : Cancellable = DefaultCancellable.obj
  private var buriedCorpses : List[CorpseRemovalActor.Entry] = List()

  private var taskResolver : ActorRef = Actor.noSender

  private[this] val log = org.log4s.getLogger

  override def postStop() = {
    //Cart Master: See you on Thursday.
    super.postStop()
    burial.cancel
    decomposition.cancel

    corpses.foreach(corpse => {
      BurialTask(corpse)
      LastRitesTask(corpse)
    })
    buriedCorpses.foreach { LastRitesTask }
  }

  def receive : Receive = {
    case "startup" =>
      ServiceManager.serviceManager ! Lookup("taskResolver") //ask for a resolver to deal with the GUID system

    case ServiceManager.LookupResult("taskResolver", endpoint) =>
      //Cart Master: Bring out your dead!
      taskResolver = endpoint
      context.become(Processing)

    case _ => ;
  }

  def Processing : Receive = {
    case CorpseRemovalActor.AddCorpse(corpse, zone, time) =>
      if(corpse.isBackpack) {
        if(corpses.isEmpty) {
          //we were the only entry so the event must be started from scratch
          corpses = List(CorpseRemovalActor.Entry(corpse, zone, time))
          RetimeFirstTask()
        }
        else {
          //unknown number of entries; append, sort, then re-time tasking
          val oldHead = corpses.head
          corpses = (corpses :+ CorpseRemovalActor.Entry(corpse, zone, time)).sortBy(_.timeAlive)
          if(oldHead != corpses.head) {
            RetimeFirstTask()
          }
        }
      }
      else {
        //Cart Master: 'Ere. He says he's not dead!
        log.warn(s"$corpse does not qualify as a corpse; ignored queueing request")
      }

    case AvatarServiceMessage.RemoveSpecificCorpse(targets) =>
      if(targets.nonEmpty) {
        //Cart Master: No, I've got to go to the Robinsons'. They've lost nine today.
        burial.cancel
        if(targets.size == 1) {
          log.debug(s"a target corpse submitted for early cleanup: ${targets.head}")
          //simple selection
          CorpseRemovalActor.recursiveFindCorpse(corpses.iterator, targets.head) match {
            case None => ;
            case Some(index) =>
              decomposition.cancel
              BurialTask(corpses(index))
              buriedCorpses = buriedCorpses :+ corpses(index)
              corpses = corpses.take(index) ++ corpses.drop(index+1)
              import scala.concurrent.ExecutionContext.Implicits.global
              decomposition = context.system.scheduler.scheduleOnce(500 milliseconds, self, CorpseRemovalActor.TryDelete())
          }
        }
        else {
          log.debug(s"multiple target corpses submitted for early cleanup: $targets")
          decomposition.cancel
          //cumbersome partition
          //a - find targets from corpses
          val locatedTargets = for {
            a <- targets
            b <- corpses
            if b.corpse == a &&
              b.corpse.Continent.equals(a.Continent) &&
              b.corpse.HasGUID && a.HasGUID && b.corpse.GUID == a.GUID
          } yield b
          locatedTargets.foreach { BurialTask }
          buriedCorpses = locatedTargets ++ buriedCorpses
          //b - corpses after the found targets are removed (note: cull any non-GUID entries while at it)
          corpses = (for {
            a <- locatedTargets.map { _.corpse }
            b <- corpses
            if b.corpse.HasGUID && a.HasGUID &&
              (b.corpse != a ||
                !b.corpse.Continent.equals(a.Continent) ||
                !b.corpse.HasGUID || !a.HasGUID || b.corpse.GUID != a.GUID)
          } yield b).sortBy(_.timeAlive)
          import scala.concurrent.ExecutionContext.Implicits.global
          decomposition = context.system.scheduler.scheduleOnce(500 milliseconds, self, CorpseRemovalActor.TryDelete())
        }
        RetimeFirstTask()
      }

    case CorpseRemovalActor.StartDelete() =>
      burial.cancel
      decomposition.cancel
      val now : Long = System.nanoTime
      val (buried, rotting) = corpses.partition(entry => { now - entry.time >= entry.timeAlive })
      corpses = rotting
      buriedCorpses = buriedCorpses ++ buried
      buried.foreach { BurialTask }
      RetimeFirstTask()
      if(buriedCorpses.nonEmpty) {
        import scala.concurrent.ExecutionContext.Implicits.global
        burial = context.system.scheduler.scheduleOnce(500 milliseconds, self, CorpseRemovalActor.TryDelete())
      }

    case CorpseRemovalActor.TryDelete() =>
      decomposition.cancel
      val (decomposed, rotting) = buriedCorpses.partition(entry => { !entry.zone.Corpses.contains(entry.corpse) })
      buriedCorpses = rotting
      decomposed.foreach { LastRitesTask }
      if(rotting.nonEmpty) {
        import scala.concurrent.ExecutionContext.Implicits.global
        decomposition = context.system.scheduler.scheduleOnce(500 milliseconds, self, CorpseRemovalActor.TryDelete())
      }

    case CorpseRemovalActor.FailureToWork(target, zone, ex) =>
      //Cart Master: Oh, I can't take him like that. It's against regulations.
      log.error(s"corpse $target from $zone not properly unregistered - $ex")

    case _ => ;
  }

  def RetimeFirstTask(now : Long = System.nanoTime) : Unit = {
    //Cart Master: Thursday.
    burial.cancel
    if(corpses.nonEmpty) {
      val short_timeout : FiniteDuration = math.max(1, corpses.head.timeAlive - (now - corpses.head.time)) nanoseconds
      import scala.concurrent.ExecutionContext.Implicits.global
      burial = context.system.scheduler.scheduleOnce(short_timeout, self, CorpseRemovalActor.StartDelete())
    }
  }

  def BurialTask(entry : CorpseRemovalActor.Entry) : Unit = {
    val target = entry.corpse
    entry.zone.Population ! Zone.Corpse.Remove(target)
    context.parent ! AvatarServiceMessage(entry.zone.Id, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, target.GUID))
  }

  def LastRitesTask(entry : CorpseRemovalActor.Entry) : Unit = {
    //Cart master: Nine pence.
    val target = entry.corpse
    target.Position = Vector3.Zero //somewhere it will not disturb anything
    taskResolver ! LastRitesTask(target, entry.zone)
  }

  def LastRitesTask(corpse : Player, zone : Zone) : TaskResolver.GiveTask = {
    import net.psforever.objects.guid.{GUIDTask, Task}
    TaskResolver.GiveTask (
      new Task() {
        private val localCorpse = corpse
        private val localZone = zone
        private val localAnnounce = self

        override def isComplete : Task.Resolution.Value = if(!localCorpse.HasGUID) {
          Task.Resolution.Success
        }
        else {
          Task.Resolution.Incomplete
        }

        def Execute(resolver : ActorRef) : Unit = {
          resolver ! scala.util.Success(this)
        }

        override def onFailure(ex : Throwable): Unit = {
          localAnnounce ! CorpseRemovalActor.FailureToWork(localCorpse, localZone, ex)
        }
      }, List(GUIDTask.UnregisterPlayer(corpse)(zone.GUID))
    )
  }
}

object CorpseRemovalActor {
  final val time : Long = 180000000000L //3 min (180s)

  final case class AddCorpse(corpse : Player, zone : Zone, time : Long = CorpseRemovalActor.time)

  final case class Entry(corpse : Player, zone : Zone, timeAlive : Long = CorpseRemovalActor.time, time : Long = System.nanoTime())

  private final case class FailureToWork(corpse : Player, zone : Zone, ex : Throwable)

  private final case class StartDelete()

  private final case class TryDelete()

  /**
    * A recursive function that finds and removes a specific player from a list of players.
    * @param iter an `Iterator` of `CorpseRemovalActor.Entry` objects
    * @param player the target `Player`
    * @param index the index of the discovered `Player` object
    * @return the index of the `Player` object in the list to be removed;
    *         `None`, otherwise
    */
  @tailrec final def recursiveFindCorpse(iter : Iterator[CorpseRemovalActor.Entry], player : Player, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val corpse = iter.next.corpse
      if(corpse == player && corpse.Continent.equals(player.Continent) && corpse.GUID == player.GUID) {
        Some(index)
      }
      else {
        recursiveFindCorpse(iter, player, index + 1)
      }
    }
  }
}
