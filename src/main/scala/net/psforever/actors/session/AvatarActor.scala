package net.psforever.actors.session

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.Cancellable
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, StashBuffer}
import akka.actor.typed.{ActorRef, Behavior, PostStop, SupervisorStrategy}
import net.psforever.objects.avatar._
import net.psforever.objects.definition.converter.CharacterSelectConverter
import net.psforever.objects.definition._
import net.psforever.objects.inventory.Container
import net.psforever.objects.equipment.{Equipment, EquipmentSlot}
import net.psforever.objects.inventory.InventoryItem
import net.psforever.objects.loadouts.{InfantryLoadout, Loadout, VehicleLoadout}
import net.psforever.objects._
import net.psforever.objects.locker.LockerContainer
import net.psforever.packet.game.objectcreate.ObjectClass
import net.psforever.packet.game._
import net.psforever.types._
import net.psforever.util.Database._
import net.psforever.persistence
import net.psforever.util.{Config, DefinitionUtil}
import org.joda.time.{LocalDateTime, Seconds}
import net.psforever.services.ServiceManager
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.collection.mutable
import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import net.psforever.services.Service

object AvatarActor {
  def apply(sessionActor: ActorRef[SessionActor.Command]): Behavior[Command] =
    Behaviors
      .supervise[Command] {
        Behaviors.withStash(100) { buffer =>
          Behaviors.setup(context => new AvatarActor(context, buffer, sessionActor).start())
        }
      }
      .onFailure[Exception](SupervisorStrategy.restart)

  sealed trait Command

  /** Subscribe to avatar updates */
  final case class Subscribe(actor: ActorRef[Avatar]) extends Command

  /** Unsubscribe from avatar updates */
  final case class Unsubscribe(actor: ActorRef[Avatar]) extends Command

  // TODO temporary solution for passing session from SessionActor, should use Topic
  final case class SetSession(session: Session) extends Command

  /** Set account this avatar belongs to. Required for handling most other messages. */
  final case class SetAccount(account: Account) extends Command

  /** Create avatar */
  final case class CreateAvatar(
      name: String,
      head: Int,
      voice: CharacterVoice.Value,
      gender: CharacterSex,
      empire: PlanetSideEmpire.Value
  ) extends Command

  /** Delete avatar */
  final case class DeleteAvatar(charId: Int) extends Command

  /** Load basic avatar info */
  final case class SelectAvatar(charId: Int, replyTo: ActorRef[AvatarResponse]) extends Command

  /** Log in the currently selected avatar. Must have first sent SelectAvatar. */
  final case class LoginAvatar(replyTo: ActorRef[AvatarLoginResponse]) extends Command

  /** Send implants to client - TODO this can be done better using a event system on SessionActor */
  final case class CreateImplants() extends Command

  /** Replace avatar instance with the provided one */
  final case class ReplaceAvatar(avatar: Avatar) extends Command

  /** Add a first time event (help text) */
  final case class AddFirstTimeEvent(event: String) extends Command

  /** Add a certification using a terminal */
  final case class LearnCertification(terminalGuid: PlanetSideGUID, certification: Certification) extends Command

  /** Remove a certification using a terminal */
  final case class SellCertification(terminalGuid: PlanetSideGUID, certification: Certification) extends Command

  /** Force-set certifications */
  final case class SetCertifications(certifications: Set[Certification]) extends Command

  /** Save a loadout */
  final case class SaveLoadout(player: Player, loadoutType: LoadoutType.Value, label: Option[String], number: Int)
      extends Command

  /** Delete a loadout */
  final case class DeleteLoadout(player: Player, loadoutType: LoadoutType.Value, number: Int) extends Command

  /** Refresh the client's loadouts, excluding empty entries */
  final case class InitialRefreshLoadouts() extends Command

  /** Refresh all of the client's loadouts */
  final case class RefreshLoadouts() extends Command

  /** Take all the entries in the player's locker and write it to the database */
  final case class SaveLocker() extends Command

  /** Set purchase time for the use of calculating cooldowns */
  final case class UpdatePurchaseTime(definition: BasicDefinition, time: LocalDateTime = LocalDateTime.now())
      extends Command

  /** Set use time for the use of calculating cooldowns */
  final case class UpdateUseTime(definition: BasicDefinition, time: LocalDateTime = LocalDateTime.now()) extends Command

  /** Force refresh the client's item purchase times */
  final case class RefreshPurchaseTimes() extends Command

  /** Set vehicle */
  final case class SetVehicle(vehicle: Option[PlanetSideGUID]) extends Command

  /** Add a implant using a terminal */
  final case class LearnImplant(terminalGuid: PlanetSideGUID, definition: ImplantDefinition) extends Command

  /** Remove a implant using a terminal */
  final case class SellImplant(terminalGuid: PlanetSideGUID, definition: ImplantDefinition) extends Command

  /** Activate an implant (must already be initialized) */
  final case class ActivateImplant(implantType: ImplantType) extends Command

  /** Deactivate an implant */
  final case class DeactivateImplant(implantType: ImplantType) extends Command

  /** Deactivate all non-passive implants that are in use */
  final case class DeactivateActiveImplants() extends Command

  /** Start implant initialization timers (after zoning or respawn) */
  final case class InitializeImplants() extends Command

  /** Deinitialize implants (before zoning or respawning) */
  final case class DeinitializeImplants() extends Command

  /** Shorthand for DeinitializeImplants and InitializeImplants */
  final case class ResetImplants() extends Command

  /** Set the avatar's lookingForSquad */
  final case class SetLookingForSquad(lfs: Boolean) extends Command

  /** Restore up to the given stamina amount */
  final case class RestoreStamina(stamina: Int) extends Command

  /** Consume up to the given stamina amount */
  final case class ConsumeStamina(stamina: Int) extends Command

  /** Suspend stamina regeneration for a given time */
  final case class SuspendStaminaRegeneration(duration: FiniteDuration) extends Command

  /** Award battle experience points */
  final case class AwardBep(bep: Long) extends Command

  /** Set total battle experience points */
  final case class SetBep(bep: Long) extends Command

  /** Award command experience points */
  final case class AwardCep(bep: Long) extends Command

  /** Set total command experience points */
  final case class SetCep(bep: Long) extends Command

  /** Set cosmetics. Only allowed for BR24 or higher. */
  final case class SetCosmetics(personalStyles: Set[Cosmetic]) extends Command

  private case class ServiceManagerLookupResult(result: ServiceManager.LookupResult) extends Command

  private case class SetStamina(stamina: Int) extends Command

  private case class SetImplantInitialized(implantType: ImplantType) extends Command

  final case class AvatarResponse(avatar: Avatar)

  final case class AvatarLoginResponse(avatar: Avatar)

}

class AvatarActor(
    context: ActorContext[AvatarActor.Command],
    buffer: StashBuffer[AvatarActor.Command],
    sessionActor: ActorRef[SessionActor.Command]
) {

  import AvatarActor._

  implicit val ec: ExecutionContextExecutor = context.executionContext

  private[this] val log                            = org.log4s.getLogger
  var account: Option[Account]                     = None
  var session: Option[Session]                     = None
  val implantTimers: mutable.Map[Int, Cancellable] = mutable.Map()
  var staminaRegenTimer: Cancellable               = Cancellable.alreadyCancelled
  var _avatar: Option[Avatar]                      = None
  var saveLockerFunc: () => Unit                   = storeNewLocker
  //val topic: ActorRef[Topic.Command[Avatar]]       = context.spawnAnonymous(Topic[Avatar]("avatar"))

  def avatar: Avatar = _avatar.get

  def avatar_=(avatar: Avatar): Unit = {
    _avatar = Some(avatar)
    //topic ! Topic.Publish(avatar)
    sessionActor ! SessionActor.SetAvatar(avatar)
  }

  def start(): Behavior[Command] = {
    Behaviors
      .receiveMessage[Command] {
        case SetAccount(newAccount) =>
          account = Some(newAccount)
          import ctx._
          ctx.run(query[persistence.Account].filter(_.id == lift(newAccount.id)).map(_.id)).onComplete {
            case Success(accounts) =>
              accounts.headOption match {
                case Some(_) => sendAvatars(newAccount)
                case None    => log.error(s"data for account ${newAccount.name} not found")
              }
            case Failure(e) => log.error(e)("db failure")
          }
          postStartBehaviour()

        case SetSession(newSession) =>
          session = Some(newSession)
          postStartBehaviour()

        case other =>
          buffer.stash(other)
          Behaviors.same
      }
  }

  def postStartBehaviour(): Behavior[Command] = {
    account match {
      case Some(_account) =>
        buffer.unstashAll(active(_account))
      case _ =>
        Behaviors.same
    }
  }

  def active(account: Account): Behavior[Command] = {
    Behaviors
      .receiveMessagePartial[Command] {
        case SetSession(newSession) =>
          session = Some(newSession)
          Behaviors.same

        case SetLookingForSquad(lfs) =>
          avatar = avatar.copy(lookingForSquad = lfs)
          sessionActor ! SessionActor.SendResponse(PlanetsideAttributeMessage(session.get.player.GUID, 53, 0))
          session.get.zone.AvatarEvents ! AvatarServiceMessage(
            avatar.faction.toString,
            AvatarAction.PlanetsideAttribute(session.get.player.GUID, 53, if (lfs) 1 else 0)
          )
          Behaviors.same

        case CreateAvatar(name, head, voice, gender, empire) =>
          import ctx._

          ctx.run(query[persistence.Avatar].filter(_.name ilike lift(name)).filter(!_.deleted)).onComplete {
            case Success(characters) =>
              characters.headOption match {
                case None =>
                  val result = for {
                    id <- ctx.run(
                      query[persistence.Avatar]
                        .insert(
                          _.name      -> lift(name),
                          _.accountId -> lift(account.id),
                          _.factionId -> lift(empire.id),
                          _.headId    -> lift(head),
                          _.voiceId   -> lift(voice.id),
                          _.genderId  -> lift(gender.value),
                          _.bep       -> lift(Config.app.game.newAvatar.br.experience),
                          _.cep       -> lift(Config.app.game.newAvatar.cr.experience)
                        )
                        .returningGenerated(_.id)
                    )
                    _ <- ctx.run(
                      liftQuery(
                        List(
                          persistence.Certification(Certification.MediumAssault.value, id),
                          persistence.Certification(Certification.ReinforcedExoSuit.value, id),
                          persistence.Certification(Certification.ATV.value, id),
                          persistence.Certification(Certification.Harasser.value, id)
                        )
                      ).foreach(c => query[persistence.Certification].insert(c))
                    )
                  } yield ()

                  result.onComplete {
                    case Success(_) =>
                      log.debug(s"AvatarActor: created character $name for account ${account.name}")
                      sessionActor ! SessionActor.SendResponse(ActionResultMessage.Pass)
                      sendAvatars(account)
                    case Failure(e) => log.error(e)("db failure")
                  }
                case Some(_) =>
                  // send "char already exists"
                  sessionActor ! SessionActor.SendResponse(ActionResultMessage.Fail(1))
              }
            case Failure(e) =>
              log.error(e)("db failure")
              sessionActor ! SessionActor.SendResponse(ActionResultMessage.Fail(4))
              sendAvatars(account)
          }
          Behaviors.same

        case DeleteAvatar(id) =>
          import ctx._
          val result = for {
            _ <- ctx.run(query[persistence.Implant].filter(_.avatarId == lift(id)).delete)
            _ <- ctx.run(query[persistence.Loadout].filter(_.avatarId == lift(id)).delete)
            _ <- ctx.run(query[persistence.Certification].filter(_.avatarId == lift(id)).delete)
            r <- ctx.run(query[persistence.Avatar].filter(_.id == lift(id)).delete)
          } yield r

          result.onComplete {
            case Success(_) =>
              log.debug(s"AvatarActor: avatar $id deleted")
              sessionActor ! SessionActor.SendResponse(ActionResultMessage.Pass)
              sendAvatars(account)
            case Failure(e) => log.error(e)("db failure")
          }
          Behaviors.same

        case SelectAvatar(charId, replyTo) =>
          import ctx._
          ctx.run(query[persistence.Avatar].filter(_.id == lift(charId))).onComplete {
            case Success(characters) =>
              characters.headOption match {
                case Some(character) =>
                  avatar = character.toAvatar
                  replyTo ! AvatarResponse(avatar)
                case None => log.error(s"selected character $charId not found")
              }
            case Failure(e) => log.error(e)("db failure")
          }
          Behaviors.same

        case LoginAvatar(replyTo) =>
          import ctx._

          val result = for {
            _ <- ctx.run(
              query[persistence.Avatar]
                .filter(_.id == lift(avatar.id))
                .update(_.lastLogin -> lift(LocalDateTime.now()))
            )
            loadouts <- initializeAllLoadouts()
            implants <- ctx.run(query[persistence.Implant].filter(_.avatarId == lift(avatar.id)))
            certs    <- ctx.run(query[persistence.Certification].filter(_.avatarId == lift(avatar.id)))
            locker   <- loadLocker()
          } yield (loadouts, implants, certs, locker)

          result.onComplete {
            case Success((loadouts, implants, certs, locker)) =>
              avatar = avatar.copy(
                loadouts = loadouts,
                // make sure we always have the base certifications
                certifications =
                  certs.map(cert => Certification.withValue(cert.id)).toSet ++ Config.app.game.baseCertifications,
                implants = implants.map(implant => Some(Implant(implant.toImplantDefinition))).padTo(3, None),
                locker = locker
              )

              staminaRegenTimer.cancel()
              staminaRegenTimer = defaultStaminaRegen()
              replyTo ! AvatarLoginResponse(avatar)
            case Failure(e) => log.error(e)("db failure")
          }
          Behaviors.same

        case ReplaceAvatar(newAvatar) =>
          replaceAvatar(newAvatar)
          Behaviors.same

        case AddFirstTimeEvent(event) =>
          avatar = avatar.copy(firstTimeEvents = avatar.firstTimeEvents ++ Set(event))
          Behaviors.same

        case LearnCertification(terminalGuid, certification) =>
          import ctx._

          if (avatar.certifications.contains(certification)) {
            sessionActor ! SessionActor.SendResponse(
              ItemTransactionResultMessage(terminalGuid, TransactionType.Learn, success = false)
            )
          } else {
            val replace = certification.replaces.intersect(avatar.certifications)
            Future
              .sequence(replace.map(cert => {
                ctx
                  .run(
                    query[persistence.Certification]
                      .filter(_.avatarId == lift(avatar.id))
                      .filter(_.id == lift(cert.value))
                      .delete
                  )
                  .map(_ => cert)
              }))
              .onComplete {
                case Failure(exception) =>
                  log.error(exception)("db failure")
                  sessionActor ! SessionActor.SendResponse(
                    ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = false)
                  )
                case Success(_replace) =>
                  _replace.foreach { cert =>
                    sessionActor ! SessionActor.SendResponse(
                      PlanetsideAttributeMessage(session.get.player.GUID, 25, cert.value)
                    )
                  }
                  ctx
                    .run(
                      query[persistence.Certification]
                        .insert(_.id -> lift(certification.value), _.avatarId -> lift(avatar.id))
                    )
                    .onComplete {
                      case Failure(exception) =>
                        log.error(exception)("db failure")
                        sessionActor ! SessionActor.SendResponse(
                          ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = false)
                        )

                      case Success(_) =>
                        sessionActor ! SessionActor.SendResponse(
                          PlanetsideAttributeMessage(session.get.player.GUID, 24, certification.value)
                        )
                        replaceAvatar(
                          avatar.copy(certifications = avatar.certifications.diff(replace) + certification)
                        )
                        sessionActor ! SessionActor.SendResponse(
                          ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = true)
                        )
                    }

              }
          }
          Behaviors.same

        case SellCertification(terminalGuid, certification) =>
          import ctx._

          if (!avatar.certifications.contains(certification)) {
            sessionActor ! SessionActor.SendResponse(
              ItemTransactionResultMessage(terminalGuid, TransactionType.Learn, success = false)
            )
          } else {
            var requiredByCert: Set[Certification] = Set(certification)
            var removeThese: Set[Certification]    = Set(certification)
            val allCerts: Set[Certification]       = Certification.values.toSet
            do {
              removeThese = allCerts.filter { testingCert =>
                testingCert.requires.intersect(removeThese).nonEmpty
              }
              requiredByCert = requiredByCert ++ removeThese
            } while (removeThese.nonEmpty)

            Future
              .sequence(
                avatar.certifications
                  .intersect(requiredByCert)
                  .map(cert => {
                    ctx
                      .run(
                        query[persistence.Certification]
                          .filter(_.avatarId == lift(avatar.id))
                          .filter(_.id == lift(cert.value))
                          .delete
                      )
                      .map(_ => cert)
                  })
              )
              .onComplete {
                case Failure(exception) =>
                  log.error(exception)("db failure")
                  sessionActor ! SessionActor.SendResponse(
                    ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = false)
                  )
                case Success(certs) =>
                  val player = session.get.player
                  replaceAvatar(avatar.copy(certifications = avatar.certifications.diff(certs)))
                  certs.foreach { cert =>
                    sessionActor ! SessionActor.SendResponse(
                      PlanetsideAttributeMessage(player.GUID, 25, cert.value)
                    )
                  }
                  sessionActor ! SessionActor.SendResponse(
                    ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = true)
                  )
                  //wearing invalid armor?
                  if (
                    if (certification == Certification.ReinforcedExoSuit) player.ExoSuit == ExoSuitType.Reinforced
                    else if (certification == Certification.InfiltrationSuit) player.ExoSuit == ExoSuitType.Infiltration
                    else if (player.ExoSuit == ExoSuitType.MAX) {
                      lazy val subtype = InfantryLoadout.DetermineSubtypeA(ExoSuitType.MAX, player.Slot(slot = 0).Equipment)
                      if (certification == Certification.UniMAX) true
                      else if (certification == Certification.AAMAX) subtype == 1
                      else if (certification == Certification.AIMAX) subtype == 2
                      else if (certification == Certification.AVMAX) subtype == 3
                      else false
                    } else false
                  ) {
                    player.Actor ! PlayerControl.SetExoSuit(ExoSuitType.Standard, 0)
                  }
              }
          }
          Behaviors.same

        case SetCertifications(certifications) =>
          import ctx._
          Future
            .sequence(
              avatar.certifications
                .diff(certifications)
                .map(cert => {
                  sessionActor ! SessionActor.SendResponse(
                    PlanetsideAttributeMessage(session.get.player.GUID, 25, cert.value)
                  )
                  ctx
                    .run(
                      query[persistence.Certification]
                        .filter(_.avatarId == lift(avatar.id))
                        .filter(_.id == lift(cert.value))
                        .delete
                    )
                }) ++
                certifications
                  .diff(avatar.certifications)
                  .map(cert => {
                    sessionActor ! SessionActor.SendResponse(
                      PlanetsideAttributeMessage(session.get.player.GUID, 24, cert.value)
                    )
                    ctx
                      .run(
                        query[persistence.Certification]
                          .insert(_.id -> lift(cert.value), _.avatarId -> lift(avatar.id))
                      )
                  })
            )
            .onComplete {
              case Success(_) =>
                replaceAvatar(avatar.copy(certifications = certifications))
              case Failure(exception) =>
                log.error(exception)("db failure")
            }

          Behaviors.same

        case CreateImplants() =>
          avatar.implants.zipWithIndex.foreach {
            case (Some(implant), index) =>
              sessionActor ! SessionActor.SendResponse(
                AvatarImplantMessage(
                  session.get.player.GUID,
                  ImplantAction.Add,
                  index,
                  implant.definition.implantType.value
                )
              )
            case _ => ;
          }
          deinitializeImplants()
          Behaviors.same

        case LearnImplant(terminalGuid, definition) =>
          // TODO there used to be a terminal check here, do we really need it?
          val index = avatar.implants.zipWithIndex.collectFirst {
            case (Some(implant), _index) if implant.definition.implantType == definition.implantType => _index
            case (None, _index) if _index < avatar.br.implantSlots                                    => _index
          }
          index match {
            case Some(_index) =>
              import ctx._
              ctx
                .run(query[persistence.Implant].insert(_.name -> lift(definition.Name), _.avatarId -> lift(avatar.id)))
                .onComplete {
                  case Success(_) =>
                    replaceAvatar(avatar.copy(implants = avatar.implants.updated(_index, Some(Implant(definition)))))
                    sessionActor ! SessionActor.SendResponse(
                      AvatarImplantMessage(
                        session.get.player.GUID,
                        ImplantAction.Add,
                        _index,
                        definition.implantType.value
                      )
                    )
                    sessionActor ! SessionActor.SendResponse(
                      ItemTransactionResultMessage(terminalGuid, TransactionType.Learn, success = true)
                    )
                    context.self ! ResetImplants()
                  case Failure(exception) => log.error(exception)("db failure")
                }

            case None =>
              log.warn("attempted to learn implant but could not find slot")
              sessionActor ! SessionActor.SendResponse(
                ItemTransactionResultMessage(terminalGuid, TransactionType.Learn, success = false)
              )
          }
          Behaviors.same

        case SellImplant(terminalGuid, definition) =>
          // TODO there used to be a terminal check here, do we really need it?
          val index = avatar.implants.zipWithIndex.collectFirst {
            case (Some(implant), _index) if implant.definition.implantType == definition.implantType => _index
          }
          index match {
            case Some(_index) =>
              import ctx._
              ctx
                .run(
                  query[persistence.Implant]
                    .filter(_.name == lift(definition.Name))
                    .filter(_.avatarId == lift(avatar.id))
                    .delete
                )
                .onComplete {
                  case Success(_) =>
                    replaceAvatar(avatar.copy(implants = avatar.implants.updated(_index, None)))
                    sessionActor ! SessionActor.SendResponse(
                      AvatarImplantMessage(session.get.player.GUID, ImplantAction.Remove, _index, 0)
                    )
                    sessionActor ! SessionActor.SendResponse(
                      ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = true)
                    )
                    context.self ! ResetImplants()
                  case Failure(exception) => log.error(exception)("db failure")
                }

            case None =>
              log.warn("attempted to sell implant but could not find slot")
              sessionActor ! SessionActor.SendResponse(
                ItemTransactionResultMessage(terminalGuid, TransactionType.Sell, success = false)
              )
          }
          Behaviors.same

        case SaveLoadout(player, loadoutType, label, number) =>
          log.info(s"${player.Name} wishes to save a favorite $loadoutType loadout as #${number+1}")
          val name = label.getOrElse(s"missing_loadout_${number + 1}")
          val (lineNo, result): (Int, Future[Loadout]) = loadoutType match {
            case LoadoutType.Infantry =>
              (
                number,
                storeLoadout(player, name, number)
              )

            case LoadoutType.Vehicle =>
              (
                number + 10,
                player.Zone.GUID(avatar.vehicle) match {
                  case Some(vehicle: Vehicle) =>
                    storeVehicleLoadout(player, name, number, vehicle)
                  case _ =>
                    throwLoadoutFailure(s"no owned vehicle found for ${player.Name}")
                }
              )
          }
          result.onComplete {
            case Success(loadout) =>
              replaceAvatar(avatar.copy(loadouts = avatar.loadouts.updated(lineNo, Some(loadout))))
              refreshLoadout(lineNo)
            case Failure(exception) =>
              log.error(exception)("db failure (?)")
          }
          Behaviors.same

        case DeleteLoadout(player, loadoutType, number) =>
          log.info(s"${player.Name} wishes to delete a favorite $loadoutType loadout - #${number+1}")
          import ctx._
          val (lineNo, result) = loadoutType match {
            case LoadoutType.Infantry if avatar.loadouts(number).nonEmpty =>
              (
                number,
                ctx.run(
                  query[persistence.Loadout]
                    .filter(_.avatarId == lift(avatar.id))
                    .filter(_.loadoutNumber == lift(number))
                    .delete
                )
              )
            case LoadoutType.Vehicle if avatar.loadouts(number + 10).nonEmpty =>
              val lineNo = number + 10
              (
                lineNo,
                ctx.run(
                  query[persistence.Vehicleloadout]
                    .filter(_.avatarId == lift(avatar.id))
                    .filter(_.loadoutNumber == lift(number))
                    .delete
                )
              )
            case _ =>
              (number, throwLoadoutFailure("unhandled loadout type or no loadout"))
          }
          result.onComplete {
            case Success(_) =>
              replaceAvatar(avatar.copy(loadouts = avatar.loadouts.updated(lineNo, None)))
              sessionActor ! SessionActor.SendResponse(FavoritesMessage(loadoutType, player.GUID, number, ""))
            case Failure(exception) =>
              log.error(exception)("db failure (?)")
          }
          Behaviors.same

        case SaveLocker() =>
          saveLockerFunc()
          Behaviors.same

        case InitialRefreshLoadouts() =>
          refreshLoadouts(avatar.loadouts.zipWithIndex)
          Behaviors.same

        case RefreshLoadouts() =>
          refreshLoadouts(avatar.loadouts.zipWithIndex.collect { case out @ (Some(_), _) => out })
          Behaviors.same

        case UpdatePurchaseTime(definition, time) =>
          // TODO save to db
          var newTimes = avatar.purchaseTimes
          resolveSharedPurchaseTimeNames(resolvePurchaseTimeName(avatar.faction, definition)).foreach {
            case (item, name) =>
              Avatar.purchaseCooldowns.get(item) match {
                case Some(cooldown) =>
                  //only send for items with cooldowns
                  newTimes = newTimes.updated(item.Name, time)
                  updatePurchaseTimer(name, cooldown.toSeconds, unk1 = true)
                case _ => ;
              }
          }
          avatar = avatar.copy(purchaseTimes = newTimes)
          Behaviors.same

        case UpdateUseTime(definition, time) =>
          if (!Avatar.useCooldowns.contains(definition)) {
            log.warn(s"UpdateUseTime message for item '${definition.Name}' without cooldown")
          }
          avatar = avatar.copy(useTimes = avatar.useTimes.updated(definition.Name, time))
          Behaviors.same

        case RefreshPurchaseTimes() =>
          refreshPurchaseTimes(avatar.purchaseTimes.keys.toSet)
          Behaviors.same

        case SetVehicle(vehicle) =>
          avatar = avatar.copy(vehicle = vehicle)
          Behaviors.same

        case ActivateImplant(implantType) =>
          avatar.implants.zipWithIndex.collectFirst {
            case (Some(implant), index) if implant.definition.implantType == implantType => (implant, index)
          } match {
            case Some((implant, slot)) =>
              if (!implant.initialized) {
                log.warn(s"requested activation of uninitialized implant $implantType")
              } else if (
                !consumeStamina(implant.definition.ActivationStaminaCost) ||
                avatar.stamina < implant.definition.StaminaCost
              ) {
                // not enough stamina to activate
              } else if (implant.definition.implantType.disabledFor.contains(session.get.player.ExoSuit)) {
                // TODO can this really happen? can we prevent it?
              } else {
                avatar = avatar.copy(
                  implants = avatar.implants.updated(slot, Some(implant.copy(active = true)))
                )
                sessionActor ! SessionActor.SendResponse(
                  AvatarImplantMessage(
                    session.get.player.GUID,
                    ImplantAction.Activation,
                    slot,
                    1
                  )
                )

                // Activation sound / effect
                session.get.zone.AvatarEvents ! AvatarServiceMessage(
                  session.get.zone.id,
                  AvatarAction.PlanetsideAttribute(
                    session.get.player.GUID,
                    28,
                    implant.definition.implantType.value * 2 + 1
                  )
                )

                implantTimers.get(slot).foreach(_.cancel())

                val interval = implant.definition.GetCostIntervalByExoSuit(session.get.player.ExoSuit).milliseconds
                // TODO costInterval should be an option ^
                if (interval.toMillis > 0) {
                  implantTimers(slot) = context.system.scheduler.scheduleWithFixedDelay(interval, interval)(() => {
                    if (!consumeStamina(implant.definition.StaminaCost)) {
                      context.self ! DeactivateImplant(implantType)
                    }
                  })
                }
              }

            case None => log.error(s"requested activation of unknown implant $implantType")
          }
          Behaviors.same

        case SetImplantInitialized(implantType) =>
          avatar.implants.zipWithIndex.collectFirst {
            case (Some(implant), index) if implant.definition.implantType == implantType => index
          } match {
            case Some(index) =>
              sessionActor ! SessionActor.SendResponse(
                AvatarImplantMessage(session.get.player.GUID, ImplantAction.Initialization, index, 1)
              )
              avatar = avatar.copy(implants = avatar.implants.map {
                case Some(implant) if implant.definition.implantType == implantType =>
                  Some(implant.copy(initialized = true))
                case other => other
              })

            case None => log.error(s"set initialized called for unknown implant $implantType")
          }

          Behaviors.same

        case DeactivateImplant(implantType) =>
          deactivateImplant(implantType)
          Behaviors.same

        case DeactivateActiveImplants() =>
          avatar.implants.indices.foreach { index =>
            avatar.implants(index).foreach { implant =>
              if (implant.active && implant.definition.GetCostIntervalByExoSuit(session.get.player.ExoSuit) > 0) {
                deactivateImplant(implant.definition.implantType)
              }
            }
          }
          Behaviors.same

        case RestoreStamina(stamina) =>
          assert(stamina > 0)
          if (session.get.player.HasGUID) {
            val totalStamina = math.min(avatar.maxStamina, avatar.stamina + stamina)
            val fatigued = if (avatar.fatigued && totalStamina >= 20) {
              avatar.implants.zipWithIndex.foreach {
                case (Some(_), slot) =>
                  sessionActor ! SessionActor.SendResponse(
                    AvatarImplantMessage(session.get.player.GUID, ImplantAction.OutOfStamina, slot, 0)
                  )
                case _ => ()
              }
              false
            } else {
              avatar.fatigued
            }
            avatar = avatar.copy(stamina = totalStamina, fatigued = fatigued)
            sessionActor ! SessionActor.SendResponse(
              PlanetsideAttributeMessage(session.get.player.GUID, 2, avatar.stamina)
            )
          }
          Behaviors.same

        case ConsumeStamina(stamina) =>
          assert(stamina > 0, s"consumed stamina must be larger than 0, but is: $stamina")
          consumeStamina(stamina)
          Behaviors.same

        case SuspendStaminaRegeneration(duration) =>
          // TODO suspensions can overwrite each other with different durations
          staminaRegenTimer.cancel()
          staminaRegenTimer = context.system.scheduler.scheduleOnce(
            duration,
            () => {
              staminaRegenTimer = defaultStaminaRegen()
            }
          )
          Behaviors.same

        case InitializeImplants() =>
          initializeImplants()
          Behaviors.same

        case DeinitializeImplants() =>
          deinitializeImplants()
          Behaviors.same

        case ResetImplants() =>
          deinitializeImplants()
          initializeImplants()
          Behaviors.same

        case AwardBep(bep) =>
          context.self ! SetBep(avatar.bep + bep)
          Behaviors.same

        case SetBep(bep) =>
          import ctx._

          val result = for {
            _ <-
              if (BattleRank.withExperience(bep).value < BattleRank.BR24.value) setCosmetics(Set())
              else Future.successful(())
            r <- ctx.run(query[persistence.Avatar].filter(_.id == lift(avatar.id)).update(_.bep -> lift(bep)))
          } yield r
          result.onComplete {
            case Success(_) =>
              sessionActor ! SessionActor.SendResponse(BattleExperienceMessage(session.get.player.GUID, bep, 0))
              session.get.zone.AvatarEvents ! AvatarServiceMessage(
                session.get.zone.id,
                AvatarAction.PlanetsideAttributeToAll(session.get.player.GUID, 17, bep)
              )
              // when the level is reduced, take away any implants over the implant slot limit
              val implants = avatar.implants.zipWithIndex.map {
                case (implant, index) =>
                  if (index >= BattleRank.withExperience(bep).implantSlots && implant.isDefined) {
                    ctx
                      .run(
                        query[persistence.Implant]
                          .filter(_.name == lift(implant.get.definition.Name))
                          .filter(_.avatarId == lift(avatar.id))
                          .delete
                      )
                      .onComplete {
                        case Success(_) =>
                          sessionActor ! SessionActor.SendResponse(
                            AvatarImplantMessage(session.get.player.GUID, ImplantAction.Remove, index, 0)
                          )
                        case Failure(exception) => log.error(exception)("db failure")
                      }
                    None
                  } else {
                    implant
                  }
              }
              avatar = avatar.copy(bep = bep, implants = implants)
            case Failure(exception) => log.error(exception)("db failure")
          }
          Behaviors.same

        case AwardCep(cep) =>
          context.self ! SetCep(avatar.cep + cep)
          Behaviors.same

        case SetCep(cep) =>
          import ctx._
          ctx.run(query[persistence.Avatar].filter(_.id == lift(avatar.id)).update(_.cep -> lift(cep))).onComplete {
            case Success(_) =>
              avatar = avatar.copy(cep = cep)
              session.get.zone.AvatarEvents ! AvatarServiceMessage(
                session.get.zone.id,
                AvatarAction.PlanetsideAttributeToAll(session.get.player.GUID, 18, cep)
              )
            case Failure(exception) => log.error(exception)("db failure")
          }
          Behaviors.same

        case SetCosmetics(cosmetics) =>
          setCosmetics(cosmetics)
          Behaviors.same
      }
      .receiveSignal {
        case (_, PostStop) =>
          staminaRegenTimer.cancel()
          implantTimers.values.foreach(_.cancel())
          saveLockerFunc()
          Behaviors.same
      }
  }

  def throwLoadoutFailure(msg: String): Future[Loadout] = {
    throwLoadoutFailure(new Exception(msg))
  }

  def throwLoadoutFailure(ex: Throwable): Future[Loadout] = {
    Future.failed(ex).asInstanceOf[Future[Loadout]]
  }

  def replaceAvatar(newAvatar: Avatar): Unit = {
    avatar = newAvatar
    avatar.deployables.UpdateMaxCounts(avatar.certifications)
    updateDeployableUIElements(
      avatar.deployables.UpdateUI()
    )
  }

  def setCosmetics(cosmetics: Set[Cosmetic]): Future[Unit] = {
    val p = Promise[Unit]()

    import ctx._
    ctx
      .run(
        query[persistence.Avatar]
          .filter(_.id == lift(avatar.id))
          .update(_.cosmetics -> lift(Some(Cosmetic.valuesToObjectCreateValue(cosmetics)): Option[Int]))
      )
      .onComplete {
        case Success(_) =>
          avatar = avatar.copy(cosmetics = Some(cosmetics))
          session.get.zone.AvatarEvents ! AvatarServiceMessage(
            session.get.zone.id,
            AvatarAction
              .PlanetsideAttributeToAll(session.get.player.GUID, 106, Cosmetic.valuesToAttributeValue(cosmetics))
          )
          p.success(())
        case Failure(exception) =>
          p.failure(exception)
      }

    p.future
  }

  /** Consumes given stamina and returns false if current stamina was too low to consume the full amount */
  def consumeStamina(stamina: Int): Boolean = {
    if (stamina == 0) return true
    if (!session.get.player.HasGUID) return false
    val consumed     = (avatar.stamina - stamina) >= 0
    val totalStamina = math.max(0, avatar.stamina - stamina)
    val fatigued = if (!avatar.fatigued && totalStamina == 0) {
      context.self ! DeactivateActiveImplants()
      true
    } else {
      totalStamina == 0
    }
    if (!avatar.fatigued && fatigued) {
      avatar.implants.zipWithIndex.foreach {
        case (Some(implant), slot) =>
          if (implant.active) {
            deactivateImplant(implant.definition.implantType)
          }
          sessionActor ! SessionActor.SendResponse(
            AvatarImplantMessage(session.get.player.GUID, ImplantAction.OutOfStamina, slot, 1)
          )
        case _ => ()
      }
    }

    avatar = avatar.copy(stamina = totalStamina, fatigued = fatigued)
    sessionActor ! SessionActor.SendResponse(PlanetsideAttributeMessage(session.get.player.GUID, 2, avatar.stamina))
    consumed
  }

  def initializeImplants(): Unit = {
    avatar.implants.zipWithIndex.foreach {
      case (Some(implant), slot) =>
        // TODO if this implant is Installed but does not have shortcut, add to a free slot or write over slot 61/62/63
        // for now, just write into slots 2, 3 and 4
        sessionActor ! SessionActor.SendResponse(
          CreateShortcutMessage(
            session.get.player.GUID,
            slot + 2,
            0,
            addShortcut = true,
            Some(implant.definition.implantType.shortcut)
          )
        )

        implantTimers.get(slot).foreach(_.cancel())
        implantTimers(slot) = context.scheduleOnce(
          implant.definition.InitializationDuration.seconds,
          context.self,
          SetImplantInitialized(implant.definition.implantType)
        )

        // Start client side initialization timer, visible on the character screen
        // Progress accumulates according to the client's knowledge of the implant initialization time
        // What is normally a 60s timer that is set to 120s on the server will still visually update as if 60s\
        session.get.zone.AvatarEvents ! AvatarServiceMessage(
          avatar.name,
          AvatarAction.SendResponse(Service.defaultPlayerGUID, ActionProgressMessage(slot + 6, 0))
        )

      case (None, _) => ;
    }
  }

  def deinitializeImplants(): Unit = {
    avatar = avatar.copy(implants = avatar.implants.zipWithIndex.map {
      case (Some(implant), slot) =>
        if (implant.active) {
          deactivateImplant(implant.definition.implantType)
        }
        session.get.zone.AvatarEvents ! AvatarServiceMessage(
          session.get.zone.id,
          AvatarAction.SendResponse(
            Service.defaultPlayerGUID,
            AvatarImplantMessage(session.get.player.GUID, ImplantAction.Initialization, slot, 0)
          )
        )
        Some(implant.copy(initialized = false, active = false))
      case (None, _) => None
    })
  }

  def deactivateImplant(implantType: ImplantType): Unit = {
    avatar.implants.zipWithIndex.collectFirst {
      case (Some(implant), index) if implant.definition.implantType == implantType => (implant, index)
    } match {
      case Some((implant, slot)) =>
        implantTimers(slot).cancel()
        avatar = avatar.copy(
          implants = avatar.implants.updated(slot, Some(implant.copy(active = false)))
        )

        // Deactivation sound / effect
        session.get.zone.AvatarEvents ! AvatarServiceMessage(
          session.get.zone.id,
          AvatarAction.PlanetsideAttribute(session.get.player.GUID, 28, implant.definition.implantType.value * 2)
        )

        sessionActor ! SessionActor.SendResponse(
          AvatarImplantMessage(
            session.get.player.GUID,
            ImplantAction.Activation,
            slot,
            0
          )
        )
      case None => log.error(s"requested deactivation of unknown implant $implantType")
    }
  }

  /** Send list of avatars to client (show character selection screen) */
  def sendAvatars(account: Account): Unit = {
    import ctx._
    val result = ctx.run(query[persistence.Avatar].filter(_.accountId == lift(account.id)))
    result.onComplete {
      case Success(avatars) =>
        val gen       = new AtomicInteger(1)
        val converter = new CharacterSelectConverter

        avatars.filter(!_.deleted) foreach { a =>
          val secondsSinceLastLogin = Seconds.secondsBetween(a.lastLogin, LocalDateTime.now()).getSeconds
          val avatar                = a.toAvatar
          val player                = new Player(avatar)

          player.ExoSuit = ExoSuitType.Reinforced
          player.Slot(0).Equipment = Tool(GlobalDefinitions.StandardPistol(player.Faction))
          player.Slot(1).Equipment = Tool(GlobalDefinitions.MediumPistol(player.Faction))
          player.Slot(2).Equipment = Tool(GlobalDefinitions.HeavyRifle(player.Faction))
          player.Slot(3).Equipment = Tool(GlobalDefinitions.AntiVehicularLauncher(player.Faction))
          player.Slot(4).Equipment = Tool(GlobalDefinitions.katana)

          /** After a client has connected to the server, their account is used to generate a list of characters.
            * On the character selection screen, each of these characters is made to exist temporarily when one is selected.
            * This "character select screen" is an isolated portion of the client, so it does not have any external constraints.
            * Temporary global unique identifiers are assigned to the underlying `Player` objects so that they can be turned into packets.
            */
          player
            .Holsters()
            .foreach(holster =>
              holster.Equipment match {
                case Some(tool: Tool) =>
                  tool.AmmoSlots.foreach(slot => {
                    slot.Box.GUID = PlanetSideGUID(gen.getAndIncrement)
                  })
                  tool.GUID = PlanetSideGUID(gen.getAndIncrement)
                case Some(item: Equipment) =>
                  item.GUID = PlanetSideGUID(gen.getAndIncrement)
                case _ => ;
              }
            )
          player.GUID = PlanetSideGUID(gen.getAndIncrement)

          player.Spawn()

          sessionActor ! SessionActor.SendResponse(
            ObjectCreateDetailedMessage(
              ObjectClass.avatar,
              player.GUID,
              converter.DetailedConstructorData(player).get
            )
          )

          sessionActor ! SessionActor.SendResponse(
            CharacterInfoMessage(
              15,
              PlanetSideZoneID(4),
              avatar.id,
              player.GUID,
              finished = false,
              secondsSinceLastLogin
            )
          )

          /** After the user has selected a character to load from the "character select screen,"
            * the temporary global unique identifiers used for that screen are stripped from the underlying `Player` object that was selected.
            * Characters that were not selected may  be destroyed along with their temporary GUIDs.
            */
          player
            .Holsters()
            .foreach(holster =>
              holster.Equipment match {
                case Some(item: Tool) =>
                  item.AmmoSlots.foreach(slot => {
                    slot.Box.Invalidate()
                  })
                  item.Invalidate()
                case Some(item: Equipment) =>
                  item.Invalidate()
                case _ => ;
              }
            )
          player.Invalidate()
        }

        sessionActor ! SessionActor.SendResponse(
          CharacterInfoMessage(15, PlanetSideZoneID(0), 0, PlanetSideGUID(0), finished = true, 0)
        )

      case Failure(e) => log.error(e)("db failure")
    }
  }

  def storeLoadout(owner: Player, label: String, line: Int): Future[Loadout] = {
    import ctx._
    val items: String = {
      val clobber: StringBuilder = new StringBuilder()
      //encode holsters
      owner
        .Holsters()
        .zipWithIndex
        .collect {
          case (slot, index) if slot.Equipment.nonEmpty =>
            clobber.append(encodeLoadoutClobFragment(slot.Equipment.get, index))
        }
      //encode inventory
      owner.Inventory.Items.foreach {
        case InventoryItem(obj, index) =>
          clobber.append(encodeLoadoutClobFragment(obj, index))
      }
      clobber.mkString.drop(1)
    }

    for {
      loadouts <- ctx.run(
        query[persistence.Loadout].filter(_.avatarId == lift(owner.CharId)).filter(_.loadoutNumber == lift(line))
      )
      _ <- loadouts.headOption match {
        case Some(loadout) =>
          ctx.run(
            query[persistence.Loadout]
              .filter(_.id == lift(loadout.id))
              .update(_.exosuitId -> lift(owner.ExoSuit.id), _.name -> lift(label), _.items -> lift(items))
          )
        case None =>
          ctx.run(
            query[persistence.Loadout].insert(
              _.exosuitId     -> lift(owner.ExoSuit.id),
              _.name          -> lift(label),
              _.items         -> lift(items),
              _.avatarId      -> lift(owner.avatar.id),
              _.loadoutNumber -> lift(line)
            )
          )
      }
    } yield Loadout.Create(owner, label)
  }

  def storeVehicleLoadout(owner: Player, label: String, line: Int, vehicle: Vehicle): Future[Loadout] = {
    import ctx._
    val items: String = {
      val clobber: StringBuilder = new StringBuilder()
      //encode holsters
      vehicle
        .Weapons
        .collect {
          case (index, slot: EquipmentSlot) if slot.Equipment.nonEmpty =>
            clobber.append(encodeLoadoutClobFragment(slot.Equipment.get, index))
        }
      //encode inventory
      vehicle.Inventory.Items.foreach {
        case InventoryItem(obj, index) =>
          clobber.append(encodeLoadoutClobFragment(obj, index))
      }
      clobber.mkString.drop(1)
    }

    for {
      loadouts <- ctx.run(
        query[persistence.Vehicleloadout]
          .filter(_.avatarId == lift(owner.CharId))
          .filter(_.loadoutNumber == lift(line))
      )
      _ <- loadouts.headOption match {
        case Some(loadout) =>
          ctx.run(
            query[persistence.Vehicleloadout]
              .filter(_.id == lift(loadout.id))
              .update(_.name -> lift(label), _.vehicle -> lift(vehicle.Definition.ObjectId), _.items -> lift(items))
          )
        case None =>
          ctx.run(
            query[persistence.Vehicleloadout].insert(
              _.avatarId      -> lift(owner.avatar.id),
              _.loadoutNumber -> lift(line),
              _.name          -> lift(label),
              _.vehicle       -> lift(vehicle.Definition.ObjectId),
              _.items         -> lift(items)
            )
          )
      }
    } yield Loadout.Create(vehicle, label)
  }

  def storeNewLocker(): Unit = {
    if (_avatar.nonEmpty) {
      val items : String = {
        val clobber : StringBuilder = new StringBuilder()
        avatar.locker.Inventory.Items.foreach {
          case InventoryItem(obj, index) =>
            clobber.append(encodeLoadoutClobFragment(obj, index))
        }
        clobber.mkString.drop(1)
      }
      if (items.nonEmpty) {
        saveLockerFunc = storeLocker
        import ctx._
        ctx.run(
          query[persistence.Locker].insert(
            _.avatarId -> lift(avatar.id),
            _.items -> lift(items)
          )
        ).onComplete {
          case Success(_) =>
            log.debug(s"saving locker contents belonging to ${avatar.name}")
          case Failure(e) =>
            saveLockerFunc = doNotStoreLocker
            log.error(e)("db failure")
        }
      }
    }
  }

  def doNotStoreLocker(): Unit = {
    /* most likely the database encountered an error; don't do anything with it until the restart */
  }

  def storeLocker(): Unit = {
    import ctx._
    val items : String = {
      val clobber : StringBuilder = new StringBuilder()
      avatar.locker.Inventory.Items.foreach {
        case InventoryItem(obj, index) =>
          clobber.append(encodeLoadoutClobFragment(obj, index))
      }
      clobber.mkString.drop(1)
    }
    log.debug(s"saving locker contents belonging to ${avatar.name}")
    ctx.run(
      query[persistence.Locker]
        .filter(_.avatarId == lift(avatar.id))
        .update(_.items -> lift(items))
    )
  }

  def encodeLoadoutClobFragment(equipment: Equipment, index: Int): String = {
    val ammoInfo: String = equipment match {
      case tool: Tool =>
        tool.AmmoSlots.zipWithIndex.collect {
          case (ammoSlot, index2) if ammoSlot.AmmoTypeIndex != 0 =>
            s"_$index2-${ammoSlot.AmmoTypeIndex}-${ammoSlot.AmmoType.id}"
        }.mkString
      case _ =>
        ""
    }
    s"/${equipment.getClass.getSimpleName},$index,${equipment.Definition.ObjectId},$ammoInfo"
  }

  def initializeAllLoadouts(): Future[Seq[Option[Loadout]]] = {
    for {
      infantry <- loadLoadouts().andThen {
        case out @ Success(_) => out
        case Failure(_) => Future(Array.fill[Option[Loadout]](10)(None).toSeq)
      }
      vehicles <- loadVehicleLoadouts().andThen {
        case out @ Success(_) => out
        case Failure(_) => Future(Array.fill[Option[Loadout]](5)(None).toSeq)
      }
    } yield infantry ++ vehicles
  }

  def loadLoadouts(): Future[Seq[Option[Loadout]]] = {
    import ctx._
    ctx
      .run(query[persistence.Loadout].filter(_.avatarId == lift(avatar.id)))
      .map { loadouts =>
        loadouts.map { loadout =>
          val doll = new Player(Avatar(0, "doll", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
          doll.ExoSuit = ExoSuitType(loadout.exosuitId)
          buildContainedEquipmentFromClob(doll, loadout.items)

          val result = (loadout.loadoutNumber, Loadout.Create(doll, loadout.name))
          (0 until 4).foreach(index => {
            doll.Slot(index).Equipment = None
          })
          doll.Inventory.Clear()
          result
        }
      }
      .map { loadouts => (0 until 10).map { index => loadouts.find(_._1 == index).map(_._2) } }
  }

  def loadVehicleLoadouts(): Future[Seq[Option[Loadout]]] = {
    import ctx._
    ctx
      .run(query[persistence.Vehicleloadout].filter(_.avatarId == lift(avatar.id)))
      .map { loadouts =>
        loadouts.map { loadout =>
          val toy = new Vehicle(DefinitionUtil.idToDefinition(loadout.vehicle).asInstanceOf[VehicleDefinition])
          buildContainedEquipmentFromClob(toy, loadout.items)

          val result = (loadout.loadoutNumber, Loadout.Create(toy, loadout.name))
          toy.Weapons.values.foreach(slot => {
            slot.Equipment = None
          })
          toy.Inventory.Clear()
          result
        }
      }
      .map { loadouts => (0 until 5).map { index => loadouts.find(_._1 == index).map(_._2) } }
  }

  def refreshLoadouts(loadouts: Iterable[(Option[Loadout], Int)]): Unit = {
    loadouts.map {
      case (Some(loadout: InfantryLoadout), index) =>
        FavoritesMessage(
          LoadoutType.Infantry,
          session.get.player.GUID,
          index,
          loadout.label,
          InfantryLoadout.DetermineSubtypeB(loadout.exosuit, loadout.subtype)
        )
      case (Some(loadout: VehicleLoadout), index) =>
        FavoritesMessage(
          LoadoutType.Vehicle,
          session.get.player.GUID,
          index - 10,
          loadout.label,
          0
        )
      case (_, index) =>
        val (mtype, lineNo) = if (index < 10) {
          (LoadoutType.Infantry, index)
        } else {
          (LoadoutType.Vehicle, index - 10)
        }
        FavoritesMessage(
          mtype,
          session.get.player.GUID,
          lineNo,
          "",
          0
        )
    }.foreach { sessionActor ! SessionActor.SendResponse(_) }
  }

  def refreshLoadout(line: Int): Unit = {
    avatar.loadouts.lift(line) match {
      case Some(Some(loadout: InfantryLoadout)) =>
        sessionActor ! SessionActor.SendResponse(
          FavoritesMessage(
            LoadoutType.Infantry,
            session.get.player.GUID,
            line,
            loadout.label,
            InfantryLoadout.DetermineSubtypeB(loadout.exosuit, loadout.subtype)
          )
        )
      case Some(Some(loadout: VehicleLoadout)) =>
        sessionActor ! SessionActor.SendResponse(
          FavoritesMessage(
            LoadoutType.Vehicle,
            session.get.player.GUID,
            line - 10,
            loadout.label,
            0
          )
        )
      case Some(None) =>
        val (mtype, lineNo) = if (line < 10) {
          (LoadoutType.Infantry, line)
        } else {
          (LoadoutType.Vehicle, line - 10)
        }
        sessionActor ! SessionActor.SendResponse(
          FavoritesMessage(
            mtype,
            session.get.player.GUID,
            lineNo,
            "",
            0
          )
        )
      case _ => ;
    }
  }

  def loadLocker(): Future[LockerContainer] = {
    val locker = Avatar.makeLocker()
    import ctx._
    ctx
      .run(query[persistence.Locker].filter(_.avatarId == lift(avatar.id)))
      .map { entry =>
        saveLockerFunc = storeLocker
        entry.foreach { contents => buildContainedEquipmentFromClob(locker, contents.items) }
      }
      .map { _ => locker }
  }

  def buildContainedEquipmentFromClob(container: Container, clob: String): Unit = {
    clob.split("/").foreach {
      value =>
        val (objectType, objectIndex, objectId, toolAmmo) = value.split(",") match {
          case Array(a, b: String, c: String)    => (a, b.toInt, c.toInt, None)
          case Array(a, b: String, c: String, d) => (a, b.toInt, c.toInt, Some(d))
        }

        objectType match {
          case "Tool" =>
            container.Slot(objectIndex).Equipment =
              Tool(DefinitionUtil.idToDefinition(objectId).asInstanceOf[ToolDefinition])
          case "AmmoBox" =>
            container.Slot(objectIndex).Equipment =
              AmmoBox(DefinitionUtil.idToDefinition(objectId).asInstanceOf[AmmoBoxDefinition])
          case "ConstructionItem" =>
            container.Slot(objectIndex).Equipment = ConstructionItem(
              DefinitionUtil.idToDefinition(objectId).asInstanceOf[ConstructionItemDefinition]
            )
          case "SimpleItem" =>
            container.Slot(objectIndex).Equipment =
              SimpleItem(DefinitionUtil.idToDefinition(objectId).asInstanceOf[SimpleItemDefinition])
          case "Kit" =>
            container.Slot(objectIndex).Equipment =
              Kit(DefinitionUtil.idToDefinition(objectId).asInstanceOf[KitDefinition])
          case "Telepad" | "BoomerTrigger" => ;
          //special types of equipment that are not actually loaded
          case name =>
            log.error(s"failing to add unknown equipment to a locker - $name")
        }

        toolAmmo foreach { toolAmmo =>
          toolAmmo.toString.split("_").drop(1).foreach { value =>
            val (ammoSlots, ammoTypeIndex, ammoBoxDefinition) = value.split("-") match {
              case Array(a: String, b: String, c: String) => (a.toInt, b.toInt, c.toInt)
            }
            container.Slot(objectIndex).Equipment.get.asInstanceOf[Tool].AmmoSlots(ammoSlots).AmmoTypeIndex =
              ammoTypeIndex
            container.Slot(objectIndex).Equipment.get.asInstanceOf[Tool].AmmoSlots(ammoSlots).Box =
              AmmoBox(AmmoBoxDefinition(ammoBoxDefinition))
          }
        }
    }
  }

  def defaultStaminaRegen(): Cancellable = {
    context.system.scheduler.scheduleWithFixedDelay(0.5 seconds, 0.5 seconds)(() => {
      (session, _avatar) match {
        case (Some(_session), Some(_)) =>
          if (
            !avatar.staminaFull && (_session.player.VehicleSeated.nonEmpty || !_session.player.isMoving && !_session.player.Jumping)
          ) {
            context.self ! RestoreStamina(1)
          }
        case _ => ;
      }
    })
  }

  // same as in SA, this really doesn't belong here
  def updateDeployableUIElements(list: List[(Int, Int, Int, Int)]): Unit = {
    val guid = PlanetSideGUID(0)
    list.foreach({
      case (currElem, curr, maxElem, max) =>
        //fields must update in ordered pairs: max, curr
        sessionActor ! SessionActor.SendResponse(PlanetsideAttributeMessage(guid, maxElem, max))
        sessionActor ! SessionActor.SendResponse(PlanetsideAttributeMessage(guid, currElem, curr))
    })
  }

  def resolvePurchaseTimeName(faction: PlanetSideEmpire.Value, item: BasicDefinition): (BasicDefinition, String) = {
    val factionName : String = faction.toString.toLowerCase
    val name = item match {
      case GlobalDefinitions.trhev_dualcycler |
           GlobalDefinitions.nchev_scattercannon |
           GlobalDefinitions.vshev_quasar   =>
        s"${factionName}hev_antipersonnel"
      case GlobalDefinitions.trhev_pounder |
           GlobalDefinitions.nchev_falcon |
           GlobalDefinitions.vshev_comet    =>
        s"${factionName}hev_antivehicular"
      case GlobalDefinitions.trhev_burster |
           GlobalDefinitions.nchev_sparrow |
           GlobalDefinitions.vshev_starfire =>
        s"${factionName}hev_antiaircraft"
      case _                                =>
        item.Name
    }
    (item, name)
  }

  def resolveSharedPurchaseTimeNames(pair: (BasicDefinition, String)): Seq[(BasicDefinition, String)] = {
    val (_, name) = pair
    if (name.matches("(tr|nc|vs)hev_.+") && Config.app.game.sharedMaxCooldown) {
      val faction = name.take(2)
      (if (faction.equals("nc")) {
        Seq(GlobalDefinitions.nchev_scattercannon, GlobalDefinitions.nchev_falcon, GlobalDefinitions.nchev_sparrow)
      }
      else if (faction.equals("vs")) {
        Seq(GlobalDefinitions.vshev_quasar, GlobalDefinitions.vshev_comet, GlobalDefinitions.vshev_starfire)
      }
      else {
        Seq(GlobalDefinitions.trhev_dualcycler, GlobalDefinitions.trhev_pounder, GlobalDefinitions.trhev_burster)
      }).zip(
        Seq(s"${faction}hev_antipersonnel", s"${faction}hev_antivehicular", s"${faction}hev_antiaircraft")
      )
    } else {
      Seq(pair)
    }
  }

  def refreshPurchaseTimes(keys: Set[String]): Unit = {
    var keysToDrop: Seq[String] = Nil
    keys.foreach { key =>
      avatar.purchaseTimes.find { case (name, _) => name.equals(key) } match {
        case Some((name, purchaseTime)) =>
          val secondsSincePurchase = Seconds.secondsBetween(purchaseTime, LocalDateTime.now()).getSeconds
          Avatar.purchaseCooldowns.find(_._1.Name == name) match {
            case Some((obj, cooldown)) if cooldown.toSeconds - secondsSincePurchase > 0 =>
              val (_, name) = resolvePurchaseTimeName(avatar.faction, obj)
              updatePurchaseTimer(name, cooldown.toSeconds - secondsSincePurchase, unk1 = true)

            case _ =>
              keysToDrop = keysToDrop :+ key  //key has timed-out
          }
        case _ => ;
      }
    }
    if (keysToDrop.nonEmpty) {
      avatar = avatar.copy(purchaseTimes = avatar.purchaseTimes.removedAll(keysToDrop))
    }
  }

  def updatePurchaseTimer(name: String, time: Long, unk1: Boolean): Unit = {
    //TODO? unk1 is: vehicles = true, everything else = false
    sessionActor ! SessionActor.SendResponse(
      AvatarVehicleTimerMessage(session.get.player.GUID, name, time, unk1 = true)
    )
  }
}
