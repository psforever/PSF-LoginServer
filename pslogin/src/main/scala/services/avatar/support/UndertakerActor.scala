// Copyright (c) 2017 PSForever
package services.avatar.support

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.guid.TaskResolver
import net.psforever.objects.{DefaultCancellable, Player}
import net.psforever.objects.zones.Zone
import services.{Service, ServiceManager}
import services.ServiceManager.Lookup
import services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.annotation.tailrec
import scala.concurrent.duration._

class UndertakerActor extends Actor {
  private var burial : Cancellable = DefaultCancellable.obj

  private var corpses : List[UndertakerActor.Entry] = List()

  private var taskResolver : ActorRef = Actor.noSender

  private[this] val log = org.log4s.getLogger("Cart Master")

  override def postStop() = {
    corpses.foreach { BurialTask }
  }

  def receive : Receive = {
    case "startup" =>
      ServiceManager.serviceManager ! Lookup("taskResolver") //ask for a resolver to deal with the GUID system

    case ServiceManager.LookupResult("taskResolver", endpoint) =>
      taskResolver = endpoint
      context.become(Processing)

    case _ => ;
  }

  def Processing : Receive = {
    case UndertakerActor.AddCorpse(corpse, zone, time) =>
      if(corpse.isBackpack) {
        corpses = corpses :+ UndertakerActor.Entry(corpse, zone, time)
        if(corpses.size == 1) { //we were the only entry so the event must be started from scratch
          import scala.concurrent.ExecutionContext.Implicits.global
          burial = context.system.scheduler.scheduleOnce(UndertakerActor.timeout, self, UndertakerActor.Dispose())
        }
      }
      else {
        log.warn(s"he's not dead yet - $corpse")
      }

    case UndertakerActor.Dispose() =>
      burial.cancel
      val now : Long = System.nanoTime
      val (buried, rotting) = PartitionEntries(corpses, now)
      corpses = rotting
      buried.foreach { BurialTask }
      if(rotting.nonEmpty) {
        val short_timeout : FiniteDuration = math.max(1, UndertakerActor.timeout_time - (now - rotting.head.time)) nanoseconds
        import scala.concurrent.ExecutionContext.Implicits.global
        burial = context.system.scheduler.scheduleOnce(short_timeout, self, UndertakerActor.Dispose())
      }

    case UndertakerActor.FailureToWork(target, zone, ex) =>
     log.error(s"$target failed to be properly cleaned up from $zone - $ex")

    case _ => ;
  }

  def BurialTask(entry : UndertakerActor.Entry) : Unit = {
    val target = entry.corpse
    val zone = entry.zone
    entry.zone.Population ! Zone.Corpse.Remove(target)
    context.parent ! AvatarServiceMessage(zone.Id, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, target.GUID)) //call up to the main event system
    taskResolver ! BurialTask(target, zone)
  }

  def BurialTask(corpse : Player, zone : Zone) : TaskResolver.GiveTask = {
    import net.psforever.objects.guid.{GUIDTask, Task}
    TaskResolver.GiveTask (
      new Task() {
        private val localCorpse = corpse
        private val localZone = zone
        private val localAnnounce = self

        override def isComplete : Task.Resolution.Value = Task.Resolution.Success

        def Execute(resolver : ActorRef) : Unit = {
          resolver ! scala.util.Success(this)
        }

        override def onFailure(ex : Throwable): Unit = {
          localAnnounce ! UndertakerActor.FailureToWork(localCorpse, localZone, ex)
        }
      }, List(GUIDTask.UnregisterPlayer(corpse)(zone.GUID))
    )
  }

  private def PartitionEntries(list : List[UndertakerActor.Entry], now : Long) : (List[UndertakerActor.Entry], List[UndertakerActor.Entry]) = {
    val n : Int = recursivePartitionEntries(list.iterator, now, UndertakerActor.timeout_time)
    (list.take(n), list.drop(n)) //take and drop so to always return new lists
  }

  /**
    * Mark the index where the `List` of elements can be divided into two:
    * a `List` of elements that have exceeded the time limit,
    * and a `List` of elements that still satisfy the time limit.
    * @param iter the `Iterator` of entries to divide
    * @param now the time right now (in nanoseconds)
    * @param index a persistent record of the index where list division should occur;
    *              defaults to 0
    * @return the index where division will occur
    */
  @tailrec private def recursivePartitionEntries(iter : Iterator[UndertakerActor.Entry], now : Long, duration : Long, index : Int = 0) : Int = {
    if(!iter.hasNext) {
      index
    }
    else {
      val entry = iter.next()
      if(now - entry.time >= duration) {
        recursivePartitionEntries(iter, now, duration, index + 1)
      }
      else {
        index
      }
    }
  }
}

object UndertakerActor {
  /** A `Long` for calculation simplicity */
  private final val timeout_time : Long = 180000000000L //3 min (180s)
  /** A `FiniteDuration` for `Executor` simplicity */
  private final val timeout : FiniteDuration = timeout_time nanoseconds

  final case class AddCorpse(corpse : Player, zone : Zone, time : Long = System.nanoTime())

  final case class Entry(corpse : Player, zone : Zone, time : Long = System.nanoTime())

  final case class FailureToWork(corpse : Player, zone : Zone, ex : Throwable)

  final case class Dispose()

  //TODO design mass disposal cases
}
