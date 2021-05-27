// Copyright (c) 2021 PSForever
package net.psforever.objects.ce

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects.guid.GUIDTask
import net.psforever.objects._
import net.psforever.objects.definition.DeployAnimation
import net.psforever.objects.zones.Zone
import net.psforever.packet.game._
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.PlanetSideEmpire

import scala.concurrent.duration._

/**
  * A `trait` mixin to manage the basic lifecycle of `Deployable` entities.<br>
  * <br>
  * Two parts of the deployable lifecycle are supported - building/deployment and dismissal/deconstruction.
  * Furthermore, both parts of the lifecycle can also be broken down into two parts for the purposes of sequencing.
  * The former part can be referred to as "preparation" which, at the least, queues the future part.
  * This latter part can be referred to as "execution" where the the actual process takes place.
  * Internal messaging protocol permits the lifecycle to transition.
  * "Building" of the deployable starts when a `Setup` request is received during the appropriate window of opportunity
  * and queues up a the formal construction event and its packets for a later period (usually a few seconds).
  * After being constructed, the deployable can be deconstructed by receiving such a `Deconstruct` message.
  * As deployables are capable of being owned by the player,
  * in between two two states of being created and deconstructed,
  * deployables may also recognize that their ownership has been changed and go through appropriate element shuffling.
  * That recognition is much easier before having their construction finalized, however.<br>
  * <br>
  * Interaction with the major zone deployable management service is crucial.
  * @see `OwnableByPlayer`
  * @see `ZoneDeployableActor`
  */
trait DeployableBehavior {
  _: Actor =>
  def DeployableObject: Deployable

  /** the timer for the construction process */
  var setup: Cancellable = Default.Cancellable
  /** the timer for the deconstruction process */
  var decay: Cancellable = Default.Cancellable
  /** used to manage the internal knowledge of the construction process;
    * `None` means "never constructed",
    * `Some(false)` means "deconstructed" or a state that is unresponsive to certain messaging input,
    * `Some(true)` means "constructed" */
  private var constructed: Option[Boolean] = None
  /** a value that is utilized by the `ObjectDeleteMessage` packet, affecting animation */
  var deletionType: Int = 2

  def deployableBehaviorPostStop(): Unit = {
    setup.cancel()
    decay.cancel()
  }

  def isConstructed: Option[Boolean] = constructed

  val deployableBehavior: Receive = {
    case Zone.Deployable.Setup()
      if constructed.isEmpty && setup.isCancelled =>
      setupDeployable(sender())

    case DeployableBehavior.Finalize(callback) =>
      finalizeDeployable(callback)

    case Deployable.Ownership(None)
      if DeployableObject.Owner.nonEmpty =>
      val obj = DeployableObject
      if (constructed.contains(true)) {
        loseOwnership(obj.Faction)
      } else {
        obj.Owner = None
      }

    case Deployable.Ownership(Some(player))
      if !DeployableObject.Destroyed && DeployableObject.Owner.isEmpty =>
      if (constructed.contains(true)) {
        gainOwnership(player)
      } else {
        DeployableObject.AssignOwnership(player)
      }

    case Deployable.Deconstruct(time)
      if constructed.contains(true) =>
      deconstructDeployable(time)

    case DeployableBehavior.FinalizeElimination() =>
      dismissDeployable()

    case Zone.Deployable.IsDismissed(obj)
      if (obj eq DeployableObject) && (constructed.isEmpty || constructed.contains(false)) =>
      unregisterDeployable(obj)
  }

  /**
    * Losing ownership involves updating map screen UI, to remove management rights from the now-previous owner,
    * and may involve concealing the deployable from the map screen for the entirety of the previous owner's faction.
    * Displaying the deployable on the map screen of another faction may be required.
    * @param toFaction the faction to which to set the deployable to be visualized on the map and in the game world;
    *                  may also affect deployable operation
    */
  def loseOwnership(toFaction: PlanetSideEmpire.Value): Unit = {
    val obj = DeployableObject
    val guid = obj.GUID
    val zone = obj.Zone
    val localEvents = zone.LocalEvents
    val originalFaction = obj.Faction
    val changeFaction = originalFaction != toFaction
    val info = DeployableInfo(guid, DeployableIcon.Boomer, obj.Position, Service.defaultPlayerGUID)
    if (changeFaction) {
      obj.Faction = toFaction
      //visual tells in regards to ownership by faction
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        AvatarAction.SetEmpire(Service.defaultPlayerGUID, guid, toFaction)
      )
      //remove knowledge by the previous owner's faction
      localEvents ! LocalServiceMessage(
        originalFaction.toString,
        LocalAction.DeployableMapIcon(Service.defaultPlayerGUID, DeploymentAction.Dismiss, info)
      )
      //display to the given faction
      localEvents ! LocalServiceMessage(
        toFaction.toString,
        LocalAction.DeployableMapIcon(Service.defaultPlayerGUID, DeploymentAction.Build, info)
      )
    }
    startOwnerlessDecay()
  }

  def startOwnerlessDecay(): Unit = {
    val obj = DeployableObject
    if (obj.Owner.nonEmpty && decay.isCancelled) {
      //without an owner, this deployable should begin to decay and will deconstruct later
      import scala.concurrent.ExecutionContext.Implicits.global
      decay = context.system.scheduler.scheduleOnce(Deployable.decay, self, Deployable.Deconstruct())
    }
    obj.Owner = None //OwnerName should remain set
  }

  /**
    * na
    * @see `gainOwnership(Player, PlanetSideEmpire.Value)`
    * @param player the player being given ownership of this deployable
    */
  def gainOwnership(player: Player): Unit = {
    gainOwnership(player, player.Faction)
  }

  /**
    * na
    * @param player the player being given ownership of this deployable
    * @param toFaction the faction to which the deployable is being assigned;
    *                  usually matches the `player`'s own faction
    */
  def gainOwnership(player: Player, toFaction: PlanetSideEmpire.Value): Unit = {
    val obj = DeployableObject
    obj.AssignOwnership(player)
    decay.cancel()

    val guid = obj.GUID
    val zone = obj.Zone
    val originalFaction = obj.Faction
    val info = DeployableInfo(guid, DeployableIcon.Boomer, obj.Position, obj.Owner.get)
    if (originalFaction != toFaction) {
      obj.Faction = toFaction
      val localEvents = zone.LocalEvents
      zone.AvatarEvents ! AvatarServiceMessage(
        zone.id,
        AvatarAction.SetEmpire(Service.defaultPlayerGUID, guid, toFaction)
      )
      localEvents ! LocalServiceMessage(
        originalFaction.toString,
        LocalAction.DeployableMapIcon(Service.defaultPlayerGUID, DeploymentAction.Dismiss, info)
      )
      localEvents ! LocalServiceMessage(
        toFaction.toString,
        LocalAction.DeployableMapIcon(Service.defaultPlayerGUID, DeploymentAction.Build, info)
      )
    }
  }

  /**
    * The first stage of the deployable build process, to put the formal process in motion.
    * Deployables, upon construction, may display an animation effect.
    * Parameters are required to be passed onto the next stage of the build process and are not used here.
    * @see `DeployableDefinition.deployAnimation`
    * @see `DeployableDefinition.DeployTime`
    * @see `LocalAction.TriggerEffectLocation`
    * @param callback an `ActorRef` used for confirming the deployable's completion of the process
    */
  def setupDeployable(callback: ActorRef): Unit = {
    val obj = DeployableObject
    constructed = Some(false)
    if (obj.Definition.deployAnimation == DeployAnimation.Standard) {
      val zone = obj.Zone
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        LocalAction.TriggerEffectLocation(
          obj.Owner.getOrElse(Service.defaultPlayerGUID),
          "spawn_object_effect",
          obj.Position,
          obj.Orientation
        )
      )
    }
    import scala.concurrent.ExecutionContext.Implicits.global
    setup = context.system.scheduler.scheduleOnce(
      obj.Definition.DeployTime milliseconds,
      self,
      DeployableBehavior.Finalize(callback)
    )
  }

  /**
    * The second stage of the deployable build process, to complete the formal process.
    * If no owner is assigned, the deployable must immediately begin suffering decay.
    * Nothing dangerous happens if it does not begin to decay, but, because it is not under a player's management,
    * the deployable will not properly transition to a decay state for another reason
    * and can linger in the zone ownerless for as long as it is not destroyed.
    * @see `AvatarAction.DeployItem`
    * @see `DeploymentAction`
    * @see `DeployableInfo`
    * @see `LocalAction.DeployableMapIcon`
    * @see `Zone.Deployable.IsBuilt`
    * @param callback an `ActorRef` used for confirming the deployable's completion of the process
    */
  def finalizeDeployable(callback: ActorRef): Unit = {
    setup.cancel()
    constructed = Some(true)
    val obj = DeployableObject
    val zone       = obj.Zone
    val localEvents = zone.LocalEvents
    val owner     = obj.Owner.getOrElse(Service.defaultPlayerGUID)
    obj.OwnerName match {
      case Some(_) =>
      case None    =>
        import scala.concurrent.ExecutionContext.Implicits.global
        decay = context.system.scheduler.scheduleOnce(
          Deployable.decay,
          self,
          Deployable.Deconstruct()
        )
    }
    //zone build
    zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.DeployItem(Service.defaultPlayerGUID, obj))
    //zone map icon
    localEvents ! LocalServiceMessage(
      obj.Faction.toString,
      LocalAction.DeployableMapIcon(
        Service.defaultPlayerGUID,
        DeploymentAction.Build, DeployableInfo(obj.GUID, Deployable.Icon(obj.Definition.Item), obj.Position, owner)
      )
    )
    //local build management
    callback ! Zone.Deployable.IsBuilt(obj)
  }

  /**
    * The first stage of the deployable dismissal process, to put the formal process in motion.
    * @param time an optional duration that overrides the deployable's usual duration
    */
  def deconstructDeployable(time: Option[FiniteDuration]): Unit = {
    constructed = Some(false)
    val duration = time.getOrElse(Deployable.cleanup)
    import scala.concurrent.ExecutionContext.Implicits.global
    setup.cancel()
    decay.cancel()
    decay = context.system.scheduler.scheduleOnce(duration, self, DeployableBehavior.FinalizeElimination())
  }

  /**
    * The task for unregistering this deployable.
    * Most deployables are monolithic entities, requiring only a single unique identifier.
    * @param obj the deployable
    */
  def unregisterDeployable(obj: Deployable): Unit = {
    val zone = obj.Zone
    zone.tasks ! GUIDTask.UnregisterObjectTask(obj)(zone.GUID)
  }

  /**
    * The second stage of the deployable build process, to complete the formal process.
    * Queue up final deployable unregistering, separate from the zone's deployable governance,
    * and instruct all clients in this zone that the deployable is to be deconstructed.
    */
  def dismissDeployable(): Unit = {
    setup.cancel()
    decay.cancel()
    val obj = DeployableObject
    val zone = obj.Zone
    //there's no special meaning behind directing any replies from from zone governance straight back to zone governance
    //this deployable control agency, however, will be expiring and can not be a recipient
    zone.Deployables ! Zone.Deployable.Dismiss(obj)
    zone.LocalEvents ! LocalServiceMessage(
      zone.id,
      LocalAction.EliminateDeployable(obj, obj.GUID, obj.Position, deletionType)
    )
    //when the deployable is being destroyed, certain functions will already have been invoked
    //as destruction will instigate deconstruction, skip these invocations to avoid needless message passing
    if (!obj.Destroyed) {
      Deployables.AnnounceDestroyDeployable(obj)
    }
    obj.OwnerName = None
  }
}

object DeployableBehavior {
  /** internal message for progressing the build process */
  private case class Finalize(callback: ActorRef)

  /** internal message for progresisng the deconstruction process */
  private case class FinalizeElimination()
}
