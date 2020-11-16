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
    autoRepairTimer.cancel()
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
        PerformRepairs(obj, repair.amount)
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
    retimeAutoRepair()
    autoRepairStartFunc = startAutoRepairIfStopped
  }

  /**
    * Cancel any attempts at auto-repair
    * by stopping any currently processing repair timer
    * and ensuring that otherwise trigger-able conditions (damages) do not instigate auto-repair operations.
    * @see `stopAutoRepair`
    */
  private def stopAutoRepairFunctionality(): Unit = {
    autoRepairTimer.cancel()
    autoRepairStartFunc = ()=>{}
  }

  /**
    * Attempt to start auto-repair operation
    * only if no operation is currently being processed.
    */
  private def startAutoRepairIfStopped(): Unit = {
    if(autoRepairTimer.isCancelled) {
      retimeAutoRepair()
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
    val before = autoRepairTimer.isCancelled
    autoRepairStartFunc()
    !(before || autoRepairTimer.isCancelled)
  }

/**
  * Cancel any attempts at auto-repair
  * by stopping any currently processing repair timer
  * The operation can be resumed.
  * @see `stopAutoRepairFunctionality`
  */
  final def stopAutoRepair(): Unit = {
    autoRepairTimer.cancel()
  }

  /**
    * As long as setup information regarding the auto-repair process can be discovered in the amenity's definition
    * and the amenity actually requires to be performed,
    * perform the setup for the auto-repair operation.
    */
  private def retimeAutoRepair(): Unit = {
    val obj = AutoRepairObject
    obj.Definition.autoRepair match {
      case Some(AutoRepairStats(_, start, interval, drain)) if obj.Health < obj.Definition.MaxHealth =>
        retimeAutoRepair(start, interval, drain)
      case _ => ;
    }
  }

  /**
    * As long as setup information regarding the auto-repair process can be provided,
    * perform the setup for the auto-repair operation.
    * @see `BuildingActor.Ntu`
    * @see `NtuCommand.Request`
    * @see `scheduleWithFixedDelay`
    * @param initialDelay the delay before the first message
    * @param delay the delay between subsequent messages, after the first
    * @param drain the amount of NTU being levied as a cost for auto-repair operation
    *              (the responding entity determines how to satisfy the cost)
    */
  private def retimeAutoRepair(initialDelay: Long, delay: Long, drain: Float): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    autoRepairTimer.cancel()
    autoRepairTimer = if(AutoRepairObject.Owner == Building.NoBuilding) {
      //without an owner, auto-repair freely
      context.system.scheduler.scheduleWithFixedDelay(
        initialDelay milliseconds,
        delay milliseconds,
        self,
        NtuCommand.Grant(null, drain)
      )
    } else {
      //ask
      context.system.scheduler.scheduleWithFixedDelay(
        initialDelay milliseconds,
        delay milliseconds,
        AutoRepairObject.Owner.Actor,
        BuildingActor.Ntu(NtuCommand.Request(drain, ntuGrantActorRef))
      )
    }
  }
}
