//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import akka.actor.{Actor, ActorRef, Cancellable}
import akka.actor.typed.{ActorRef => TypedActorRef}
import akka.actor.typed.scaladsl.adapter.ClassicActorRefOps
import net.psforever.actors.commands.NtuCommand
import net.psforever.actors.zone.BuildingActor
import net.psforever.objects.{Default, NtuContainer, NtuStorageBehavior}
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.serverobject.structures.{Amenity, AutoRepairStats, Building}
import net.psforever.util.Config

import scala.concurrent.duration._

/**
  * A mixin for handling the automatic repair functionality of facility amenities.
  * Auto-repair is facilitated primarily as a function of nanite transfer unit (NTU) provisions
  * and is prompted by the amenity itself sustaining damage
  * and being, at some level, capable of being repaired.
  * In major facilities - technology plants, bio labs, etc. -
  * this NTU is obtained from that facility's nanite resource silo.
  * The amenity that wishes to be repaired asks the facility for nanite.
  * The reply comes from the NTU source, or from the facility again.
  * In exchange for the automatic repair, the silo looses some of its NTU stockpile
  * and that entails all of the consequences of losing all of the NTU for the base.
  * In smaller field tower bases, the lack of resource silo should not hinder operations
  * as auto-repair is still carried out nonetheless.
  * The consequences of losing NTU do not apply in this case;
  * the field tower is considered to have unlimited, unshared NTU.
  */
trait AmenityAutoRepair
  extends NtuStorageBehavior {
  _: Damageable with RepairableEntity with Actor =>
  /** a dedicated reference to self that facilitates in receiving `NtuCommand.Grant` messages for auto-repair operation */
  private lazy val ntuGrantActorRef: TypedActorRef[NtuCommand.Grant] =
    new ClassicActorRefOps(self).toTyped[NtuCommand.Grant]
  /** the function that initializes auto-repair operations, if those operations have not yet started */
  private var autoRepairStartFunc: ()=>Unit = startAutoRepairIfStopped
  /** the timer for requests for auto-repair-actionable resource deposits (NTU) */
  private var autoRepairTimer: Cancellable  = Default.Cancellable
  /** indicate the current state of the task assignment;
    * `None` means no auto-repair operations;
    * `Some(0L)` means previous auto-repair task completed;
    * `Some(time)` means that an auto-repair task is or was queued to occur at `time` */
  private var autoRepairQueueTask: Option[Long] = None
  /** repair can only occur in integer increments, so any non-integer portion of incremental repairs accumulates;
    * once above a whole number, that number is extracted and applied to the base repair value */
  private var autoRepairOverflow: Float = 0f

  def AutoRepairObject: Amenity

  final val autoRepairBehavior: Receive = storageBehavior.orElse {
    case BuildingActor.SuppliedWithNtu() =>
      withNtuSupplyCallback()

    case BuildingActor.NtuDepleted() =>
      noNtuSupplyCallback()
  }

  //nothing special
  def HandleNtuOffer(sender: ActorRef, src: NtuContainer): Unit = { }

  /**
    * Stop the auto-repair timer.
    */
  def StopNtuBehavior(sender : ActorRef) : Unit = {
    stopAutoRepair()
  }

  //nothing special
  def HandleNtuRequest(sender: ActorRef, min: Float, max: Float): Unit = { }

  /**
    * When reports of an NTU provision is returned to the requesting amenity,
    * the amount of repair that can be performed is obtained
    * and, if the amenity still requires those repairs,
    * auto-repair executes a single tick.
    * @see `RepairableAmenity`
    */
  def HandleNtuGrant(sender : ActorRef, src : NtuContainer, amount : Float) : Unit = {
    val obj = AutoRepairObject
    obj.Definition.autoRepair match {
      case Some(repair : AutoRepairStats) if obj.Health < obj.Definition.MaxHealth =>
        autoRepairTimer.cancel()
        val modifiedRepairAmount = repair.amount * Config.app.game.amenityAutorepairRate
        val wholeRepairAmount = modifiedRepairAmount.toInt
        val overflowRepairAmount = modifiedRepairAmount - wholeRepairAmount
        val finalRepairAmount = if (autoRepairOverflow + overflowRepairAmount < 1) {
          autoRepairOverflow += overflowRepairAmount
          wholeRepairAmount
        } else {
          val totalOverflow = autoRepairOverflow + overflowRepairAmount
          val wholeOverflow = totalOverflow.toInt
          autoRepairOverflow = totalOverflow - wholeOverflow
          wholeRepairAmount + wholeOverflow
        }
        PerformRepairs(obj, finalRepairAmount)
        val currentTime = System.currentTimeMillis()
        val taskTime = currentTime - autoRepairQueueTask.getOrElse(currentTime)
        autoRepairQueueTask = Some(0L)
        trySetupAutoRepairSubsequent(taskTime)
      case _ =>
        StopNtuBehavior(sender)
    }
  }

  /**
    * Confirm that a provision of NTU to the potential requesting amenity is possible.
    * Attempt to start auto-repair operations.
    */
  def withNtuSupplyCallback(): Unit = {
    startAutoRepairFunctionality()
  }

  /**
    * No (further) provisions of NTU to the potential requesting amenity will be forthcoming.
    * Cancel any attempts at auto-repair.
    */
  def noNtuSupplyCallback(): Unit = {
    stopAutoRepairFunctionality()
  }

  /**
    * Attempt to start auto-repair operation if possible,
    * restarting an existing timed operation if necessary.
    * Set a function that will attempt auto-repair operations under specific trigger-able conditions (damage).
    */
  private def startAutoRepairFunctionality(): Unit = {
    trySetupAutoRepairInitial()
    autoRepairStartFunc = startAutoRepairIfStopped
  }

  /**
    * Cancel any attempts at auto-repair
    * by stopping any currently processing repair timer
    * and ensuring that otherwise trigger-able conditions (damages) do not instigate auto-repair operations.
    * @see `stopAutoRepair`
    */
  private def stopAutoRepairFunctionality(): Unit = {
    stopAutoRepair()
    autoRepairStartFunc = ()=>{}
  }

  /**
    * Attempt to start auto-repair operation
    * only if no operation is currently being processed
    * or if the current process has stalled.
    */
  private def startAutoRepairIfStopped(): Unit = {
    if(autoRepairQueueTask.isEmpty || stallDetection(stallTime = 15000L)) {
      trySetupAutoRepairInitial()
    }
  }

  /**
    * Detect if the auto-repair system is in a stalled state where orders are not being dispatched when they should.
    * Not running or not being expected to be running does not count as being stalled.
    * @param stallTime for how long we need to be stalled (ms)
    * @return `true`, if stalled;
    *        `false`, otherwise
    */
  private def stallDetection(stallTime: Long): Boolean = {
    autoRepairQueueTask match {
      case Some(0L) =>
        //the last auto-repair request was completed; did we start the next one?
        autoRepairTimer.isCancelled
      case Some(time) =>
        //waiting for too long on an active auto-repair request
        time + stallTime > System.currentTimeMillis()
      case None =>
        //we've not stalled; we're just not running
        false
    }
  }

  /**
    * Attempt to start auto-repair operation
    * only if no operation is currently being processed.
    * @see `actuallyTryAutoRepair`
    * @return `true`, if the auto-repair process started specifically due to this call;
    *        `false`, if it was already started, or did not start
    */
  def tryAutoRepair(): Boolean = {
    actuallyTryAutoRepair()
  }

  /**
    * Attempt to start auto-repair operation
    * only if no operation is currently being processed.
    * In case that an override to the normals operations of `tryAutoRepair` is necessary,
    * but the superclass can not be invoked,
    * this method is the backup of those operations to initiate auto-repair.
    * @see `tryAutoRepair`
    * @return `true`, if the auto-repair process started specifically due to this call;
    *        `false`, if it was already started, or did not start
    */
  final def actuallyTryAutoRepair(): Boolean = {
    val before = autoRepairQueueTask.isEmpty
    autoRepairStartFunc()
    !(before || autoRepairQueueTask.isEmpty)
  }

/**
  * Cancel any attempts at auto-repair
  * by stopping any currently processing repair timer
  * The operation can be resumed.
  * @see `stopAutoRepairFunctionality`
  */
  final def stopAutoRepair(): Unit = {
    autoRepairTimer.cancel()
    autoRepairOverflow = 0
    autoRepairQueueTask = None
  }

  /**
    * As long as setup information regarding the auto-repair process can be discovered in the amenity's definition
    * and the amenity actually requires repairs to be performed,
    * perform the setup for the auto-repair operation.
    * This is the initial delay before the first repair attempt.
    */
  private def trySetupAutoRepairInitial(): Unit = {
    val obj = AutoRepairObject
    obj.Definition.autoRepair match {
      case Some(AutoRepairStats(_, start, _, drain)) if obj.Health < obj.Definition.MaxHealth =>
        setupAutoRepair(start, drain)
      case _ =>
        stopAutoRepair()
    }
  }

  /**
    * As long as setup information regarding the auto-repair process can be discovered in the amenity's definition
    * and the amenity actually requires repairs to be performed,
    * perform the setup for the auto-repair operation.
    * This is the delay before every subsequent repair attempt.
    * @param delayOffset an adjustment to the normal delay applied to the subsequent operation (ms);
    *                    ideally, some number that's inclusive to 0 and the interval
    */
  private def trySetupAutoRepairSubsequent(delayOffset: Long): Unit = {
    if (autoRepairQueueTask.contains(0L)) {
      val obj = AutoRepairObject
      obj.Definition.autoRepair match {
        case Some(AutoRepairStats(_, _, interval, drain)) if obj.Health < obj.Definition.MaxHealth =>
          setupAutoRepair(
            math.min(interval, math.max(0L, interval - delayOffset)),
            drain
          )
        case _ =>
          stopAutoRepair()
      }
    }
  }

    /**
      * As long as setup information regarding the auto-repair process can be provided,
      * perform the setup for the auto-repair operation.
      * @see `BuildingActor.Ntu`
      * @see `NtuCommand.Request`
      * @see `scheduleOnce`
      * @param delay the delay before the message is sent (ms)
      * @param drain the amount of NTU being levied as a cost for auto-repair operation
      *              (the responding entity determines how to satisfy this cost)
      */
  private def setupAutoRepair(delay: Long, drain: Float): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    autoRepairTimer.cancel()
    autoRepairQueueTask = Some(System.currentTimeMillis() + delay)
    val modifiedDrain = drain * 2 * Config.app.game.amenityAutorepairDrainRate //doubled intentionally
    autoRepairTimer = if(AutoRepairObject.Owner == Building.NoBuilding) {
      //without an owner, auto-repair freely
      context.system.scheduler.scheduleOnce(
        delay milliseconds,
        self,
        NtuCommand.Grant(null, modifiedDrain)
      )
    } else {
      //ask politely
      context.system.scheduler.scheduleOnce(
        delay milliseconds,
        AutoRepairObject.Owner.Actor,
        BuildingActor.Ntu(NtuCommand.Request(modifiedDrain, ntuGrantActorRef))
      )
    }
  }
}
