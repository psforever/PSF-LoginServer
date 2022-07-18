// Copyright (c) 2019-2022 PSForever
package net.psforever.services.teamwork

import akka.actor.{Actor, ActorRef, Cancellable}
import org.log4s.Logger

import scala.concurrent.duration._
import net.psforever.objects.{Default, Player}
import net.psforever.objects.avatar.Certification
import net.psforever.objects.definition.converter.StatConverter
import net.psforever.objects.loadouts.SquadLoadout
import net.psforever.objects.teamwork.{Member, Squad, SquadFeatures, WaypointData}
import net.psforever.packet.game.{PlanetSideZoneID, SquadDetail, SquadInfo, SquadPositionDetail, SquadPositionEntry, WaypointEventAction, WaypointInfo, SquadAction => SquadRequestAction}
import net.psforever.services.teamwork.SquadService.SwapMemberPosition
import net.psforever.types.{PlanetSideGUID, SquadWaypoint, Vector3, WaypointSubtype}

/**
  * The dedicated messaging switchboard for members and observers of a given squad.
  * It almost always dispatches messages to `SessionActor` instances, much like any other `Service`.
  * The sole purpose of this `ActorBus` container is to manage a subscription model
  * that can involuntarily drop subscribers without informing them explicitly
  * or can just vanish without having to properly clean itself up.
  * @param features squad and associated information about the squad
  * @param subscriptions individually-connected subscription service
  */
class SquadSwitchboard(
                        features: SquadFeatures,
                        subscriptions: SquadSubscriptionEntity
                      ) extends Actor {
  private[this] val log = org.log4s.getLogger(context.self.path.name)

  private var lazeWaypoints: Seq[SquadSwitchboard.LazeWaypointData] = Seq.empty

  /**
    * The periodic clearing of laze pointer waypoints.
    */
  private var lazeIndexBlanking: Cancellable = Default.Cancellable

  override def postStop() : Unit = {
    lazeIndexBlanking.cancel()
    lazeIndexBlanking = Default.Cancellable
    lazeWaypoints = Nil
  }

  def receive: Receive = {
    case SquadSwitchboard.Join(player, position, sendTo) =>
      val charId = player.CharId
      if (!features.Squad.Membership.exists { _.CharId == charId }) {
        //joining this squad for the first time
        JoinSquad(player, position, sendTo)
      } else {
        //potential relog
        val squad     = features.Squad
        val guid      = squad.GUID
        val toChannel = s"/${features.ToChannel}/Squad"
        val indices   = squad.Membership
          .zipWithIndex
          .collect { case (member, index) if member.CharId != 0 => index }
          .toList
        subscriptions.Publish(charId, SquadResponse.AssociateWithSquad(guid))
        subscriptions.Publish(charId, SquadResponse.Join(squad, indices, toChannel, self))
        subscriptions.InitSquadDetail(guid, Seq(charId), squad)
        InitWaypoints(charId, features)
      }

    case SquadSwitchboard.Leave(char_id) =>
      LeaveSquad(char_id)

    case SquadSwitchboard.To(member, msg) =>
      if (features.Squad.Membership.exists(_.CharId == member)) {
        subscriptions.UserEvents.get(member) match {
          case Some(actor) =>
            actor ! msg
          case None => ;
        }
      }

    case SquadSwitchboard.ToAll(msg) =>
      features.Squad.Membership
        .map { member => subscriptions.UserEvents(member.CharId) }
        .foreach { actor =>
          actor ! msg
        }

    case SquadSwitchboard.Except(excluded, msg) =>
      features.Squad.Membership
        .collect { case member if member.CharId != excluded => subscriptions.UserEvents(member.CharId) }
        .foreach { actor =>
          actor ! msg
        }

    case SquadServiceMessage(tplayer, _, squad_action) =>
      squad_action match {
        case SquadAction.Definition(guid, line, action) =>
          SquadActionDefinition(tplayer, guid, line, action, sender())

        case SquadAction.Waypoint(_, wtype, _, info) =>
          SquadActionWaypoint(tplayer, wtype, info)

        case SquadAction.Update(char_id, guid, health, max_health, armor, max_armor, pos, zone_number) =>
          SquadActionUpdate(char_id, guid, health, max_health, armor, max_armor, pos, zone_number, sender())

        case _ => ;
      }

    case SquadSwitchboard.BlankLazeWaypoints =>
      TryBlankLazeWaypoints()

    case msg =>
      log.warn(s"Unhandled message $msg from ${sender()}")
  }

  /**
    * Behaviors and exchanges necessary to complete the fulfilled recruitment process for the squad role.<br>
    * <br>
    * This operation is fairly safe to call whenever a player is to be inducted into a squad.
    * The aforementioned player must have a callback retained in `subs.UserEvents`
    * and conditions imposed by both the role and the player must be satisfied.
    * @see `InitialAssociation`
    * @see `InitSquadDetail`
    * @see `InitWaypoints`
    * @see `Publish`
    * @see `CleanUpAllInvitesWithPlayer`
    * @see `SquadDetail`
    * @see `SquadInfo`
    * @see `SquadPositionDetail`
    * @see `SquadPositionEntry`
    * @see `SquadResponse.Join`
    * @see `StatConverter.Health`
    * @see `UpdateSquadListWhenListed`
    * @param player the new squad member;
    *               this player is NOT the squad leader
    * @param position the squad member role that the player will be filling
    * @return `true`, if the player joined the squad in some capacity;
    *         `false`, if the player did not join the squad or is already a squad member
    */
  def JoinSquad(player: Player, position: Int, sendTo: ActorRef): Unit = {
    val charId = player.CharId
    val squad  = features.Squad
    val role   = squad.Membership(position)
    log.info(s"${player.Name}-${player.Faction} joins position ${position+1} of squad #${squad.GUID.guid} - ${squad.Task}")
    role.Name = player.Name
    role.CharId = charId
    role.Health = StatConverter.Health(player.Health, player.MaxHealth, min = 1, max = 64)
    role.Armor = StatConverter.Health(player.Armor, player.MaxArmor, min = 1, max = 64)
    role.Position = player.Position
    role.ZoneId = player.Zone.Number

    val toChannel = features.ToChannel
    val size = squad.Size
    subscriptions.MonitorSquadDetails.subtractOne(charId)
    subscriptions.Publish(charId, SquadResponse.AssociateWithSquad(squad.GUID))
    if (size == 2) {
      //first squad member after leader; both members fully initialize
      subscriptions.InitialAssociation(features)
      val (memberCharIds, indices) = squad.Membership.zipWithIndex
        .filterNot { case (member, _) => member.CharId == 0 }
        .toList
        .unzip { case (member, index) => (member.CharId, index) }
      val organizedIndices = indices.filterNot(_ == position) :+ position
      memberCharIds
        .map { id => (id, subscriptions.UserEvents.get(id)) }
        .collect { case (id, Some(sub)) =>
          subscriptions.InitSquadDetail(squad.GUID, Seq(id), squad)
          subscriptions.Publish(id, SquadResponse.Join(squad, organizedIndices, toChannel, self))
          InitWaypoints(id, features)
          subscriptions.SquadEvents.subscribe(sub, s"/$toChannel/Squad")
        }
      //update for new user
    } else {
      //joining an active squad; different people update differently
      //new member gets full squad UI updates
      subscriptions.InitSquadDetail(squad.GUID, Seq(charId), squad)
      subscriptions.Publish(
        charId,
        SquadResponse.Join(
          squad,
          position +: squad.Membership.zipWithIndex
            .collect({ case (member, index) if member.CharId > 0 => index })
            .filterNot(_ == position)
            .toList,
          toChannel,
          self
        )
      )
      InitWaypoints(charId, features)
      //other squad members see new member joining the squad
      subscriptions.Publish(toChannel, SquadResponse.Join(squad, List(position), "", self), Seq(charId))
      subscriptions.UpdateSquadDetail(
        squad.GUID,
        toChannel,
        Seq(charId),
        SquadDetail().Members(
          List(SquadPositionEntry(position, SquadPositionDetail().Player(charId, player.Name)))
        )
      )
      subscriptions.SquadEvents.subscribe(sendTo, s"/$toChannel/Squad")
    }
    context.parent ! SquadService.UpdateSquadListWhenListed(
      features,
      SquadInfo().Leader(squad.Leader.Name).Size(size)
    )
  }

  /**
    * Behaviors and exchanges necessary to undo the recruitment process for the squad role.
    * @see `PanicLeaveSquad`
    * @see `Publish`
    * @param charId the player
    * @return `true`, if the player, formerly a normal member of the squad, has been ejected from the squad;
    *        `false`, otherwise
    */
  def LeaveSquad(charId: Long): Boolean = {
    val squad = features.Squad
    val membership = squad.Membership.zipWithIndex
    membership.find { case (_member, _) => _member.CharId == charId } match {
      case data @ Some((us, index)) if squad.Leader.CharId != charId =>
        SquadSwitchboard.PanicLeaveSquad(charId, features, data, subscriptions, context.parent, log)
        //member leaves the squad completely (see PanicSquadLeave)
        subscriptions.Publish(
          charId,
          SquadResponse.Leave(
            squad,
            (charId, index) +: membership.collect {
              case (_member, _index) if _member.CharId > 0 && _member.CharId != charId => (_member.CharId, _index)
            }.toList
          )
        )
        subscriptions.SquadEvents.unsubscribe(subscriptions.UserEvents(charId), s"/${features.ToChannel}/Squad")
        log.info(s"${us.Name} has left squad #${squad.GUID.guid}")
        true
      case _ =>
        false
    }
  }

  def SquadActionDefinition(
                             tplayer: Player,
                             guid: PlanetSideGUID,
                             line: Int,
                             action: SquadRequestAction,
                             sendTo: ActorRef
                           ): Unit = {
    import net.psforever.packet.game.SquadAction._
    //the following actions can only be performed by a squad's leader
    action match {
      case SaveSquadFavorite() =>
        SquadActionDefinitionSaveSquadFavorite(tplayer, line, sendTo)

      case LoadSquadFavorite() =>
        SquadActionDefinitionLoadSquadFavorite(tplayer, line, sendTo)

      case DeleteSquadFavorite() =>
        SquadActionDefinitionDeleteSquadFavorite(tplayer, line, sendTo)

      case ChangeSquadPurpose(purpose) =>
        SquadActionDefinitionChangeSquadPurpose(tplayer, purpose)

      case ChangeSquadZone(zone_id) =>
        SquadActionDefinitionChangeSquadZone(tplayer, zone_id, sendTo)

      case CloseSquadMemberPosition(position) =>
        SquadActionDefinitionCloseSquadMemberPosition(tplayer, position)

      case AddSquadMemberPosition(position) =>
        SquadActionDefinitionAddSquadMemberPosition(tplayer, position)

      case ChangeSquadMemberRequirementsRole(position, role) =>
        SquadActionDefinitionChangeSquadMemberRequirementsRole(tplayer, position, role)

      case ChangeSquadMemberRequirementsDetailedOrders(position, orders) =>
        SquadActionDefinitionChangeSquadMemberRequirementsDetailedOrders(tplayer, position, orders)

      case ChangeSquadMemberRequirementsCertifications(position, certs) =>
        SquadActionDefinitionChangeSquadMemberRequirementsCertifications(tplayer, position, certs)

      case LocationFollowsSquadLead(state) =>
        SquadActionDefinitionLocationFollowsSquadLead(tplayer, state)

      case AutoApproveInvitationRequests(state) =>
        SquadActionDefinitionAutoApproveInvitationRequests(tplayer, state)

      case RequestListSquad() =>
        SquadActionDefinitionRequestListSquad(tplayer, sendTo)

      case StopListSquad() =>
        SquadActionDefinitionStopListSquad(tplayer, sendTo)

      case ResetAll() =>
        SquadActionDefinitionResetAll(tplayer)

      //the following action can be performed by the squad leader and maybe an unaffiliated player
      case SelectRoleForYourself(position) =>
        SquadActionDefinitionSelectRoleForYourself(tplayer, position)

      case AssignSquadMemberToRole(position, char_id) =>
        SquadActionDefinitionAssignSquadMemberToRole(char_id, position)

      case DisplaySquad() =>
        SquadActionDefinitionDisplaySquad(sendTo)

      //the following message is feedback from a specific client, awaiting proper initialization
      // how to respond?
      case SquadMemberInitializationIssue() => ;

      case msg =>
        log.warn(s"Unsupported squad definition behavior: $msg")
    }
  }

  def SquadActionDefinitionSaveSquadFavorite(
                                              tplayer: Player,
                                              line: Int,
                                              sendTo: ActorRef
                                            ): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId && squad.Task.nonEmpty && squad.ZoneId > 0) {
      val squad = features.Squad
      tplayer.squadLoadouts.SaveLoadout(squad, squad.Task, line)
      subscriptions.Publish(sendTo, SquadResponse.ListSquadFavorite(line, squad.Task))
    }
  }

  def SquadActionDefinitionLoadSquadFavorite(
                                              tplayer: Player,
                                              line: Int,
                                              sendTo: ActorRef
                                            ): Unit = {
    //TODO seems all wrong
    val squad = features.Squad
    tplayer.squadLoadouts.LoadLoadout(line) match {
      case Some(loadout: SquadLoadout) if squad.Size == 1 =>
        SquadSwitchboard.LoadSquadDefinition(squad, loadout)
        context.parent ! SquadService.UpdateSquadListWhenListed(features, SquadService.PublishFullListing(squad))
        subscriptions.Publish(sendTo, SquadResponse.AssociateWithSquad(PlanetSideGUID(0)))
        subscriptions.InitSquadDetail(PlanetSideGUID(0), Seq(tplayer.CharId), squad)
        subscriptions.UpdateSquadDetail(features)
        subscriptions.Publish(sendTo, SquadResponse.AssociateWithSquad(squad.GUID))
      case _ => ;
    }
  }

  def SquadActionDefinitionDeleteSquadFavorite(tplayer: Player, line: Int, sendTo: ActorRef): Unit = {
    tplayer.squadLoadouts.DeleteLoadout(line)
    subscriptions.Publish(sendTo, SquadResponse.ListSquadFavorite(line, ""))
  }

  def SquadActionDefinitionChangeSquadPurpose(
                                               tplayer: Player,
                                               purpose: String
                                             ): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId) {
      val squad = features.Squad
      squad.Task = purpose
      context.parent ! SquadService.UpdateSquadListWhenListed(features, SquadInfo().Task(purpose))
      subscriptions.UpdateSquadDetail(features, SquadDetail().Task(purpose))
    }
  }

  def SquadActionDefinitionChangeSquadZone(
                                            tplayer: Player,
                                            zone_id: PlanetSideZoneID,
                                            sendTo: ActorRef
                                          ): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId) {
      squad.ZoneId = zone_id.zoneId.toInt
      context.parent ! SquadService.UpdateSquadListWhenListed(features, SquadInfo().ZoneId(zone_id))
      subscriptions.InitialAssociation(features)
      subscriptions.Publish(sendTo, SquadResponse.Detail(squad.GUID, SquadService.PublishFullDetails(squad)))
      subscriptions.UpdateSquadDetail(
        squad.GUID,
        features.ToChannel,
        Seq(squad.Leader.CharId),
        SquadDetail().ZoneId(zone_id)
      )
    }
  }

  def SquadActionDefinitionCloseSquadMemberPosition(
                                                     tplayer: Player,
                                                     position: Int
                                                   ): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId) {
      squad.Availability.lift(position) match {
        case Some(true) if position > 0 => //do not close squad leader position; undefined behavior
          squad.Availability.update(position, false)
          context.parent ! SquadService.UpdateSquadListWhenListed(features, SquadInfo().Capacity(squad.Capacity))
          subscriptions.UpdateSquadDetail(
            features,
            SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail.Closed)))
          )
        case Some(_) | None => ;
      }
    }
  }

  def SquadActionDefinitionAddSquadMemberPosition(
                                                   tplayer: Player,
                                                   position: Int
                                                 ): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId) {
      squad.Availability.lift(position) match {
        case Some(false) =>
          squad.Availability.update(position, true)
          context.parent ! SquadService.UpdateSquadListWhenListed(features, SquadInfo().Capacity(squad.Capacity))
          subscriptions.UpdateSquadDetail(
            features,
            SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail.Open)))
          )
        case Some(true) | None => ;
      }
    }
  }

  def SquadActionDefinitionChangeSquadMemberRequirementsRole(
                                                              tplayer: Player,
                                                              position: Int,
                                                              role: String
                                                            ): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId) {
      squad.Availability.lift(position) match {
        case Some(true) =>
          squad.Membership(position).Role = role
          subscriptions.UpdateSquadDetail(
            features,
            SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail().Role(role))))
          )
        case Some(false) | None => ;
      }
    }
  }

  def SquadActionDefinitionChangeSquadMemberRequirementsDetailedOrders(
                                                                        tplayer: Player,
                                                                        position: Int,
                                                                        orders: String
                                                                      ): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId) {
      squad.Availability.lift(position) match {
        case Some(true) =>
          squad.Membership(position).Orders = orders
          subscriptions.UpdateSquadDetail(
            features,
            SquadDetail().Members(
              List(SquadPositionEntry(position, SquadPositionDetail().DetailedOrders(orders)))
            )
          )
        case Some(false) | None => ;
      }
    }
  }

  def SquadActionDefinitionChangeSquadMemberRequirementsCertifications(
                                                                        tplayer: Player,
                                                                        position: Int,
                                                                        certs: Set[Certification]
                                                                      ): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId) {
      squad.Availability.lift(position) match {
        case Some(true) =>
          squad.Membership(position).Requirements = certs
          subscriptions.UpdateSquadDetail(
            features,
            SquadDetail().Members(List(SquadPositionEntry(position, SquadPositionDetail().Requirements(certs))))
          )
        case Some(false) | None => ;
      }
    }
  }

  def SquadActionDefinitionLocationFollowsSquadLead(
                                                     tplayer: Player,
                                                     state: Boolean
                                                   ): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId) {
      features.LocationFollowsSquadLead = state
    }
  }

  def SquadActionDefinitionAutoApproveInvitationRequests(
                                                          tplayer: Player,
                                                          state: Boolean
                                                        ): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId) {
      features.AutoApproveInvitationRequests = state
    }
  }

  def SquadActionDefinitionRequestListSquad(
                                             tplayer: Player,
                                             sendTo: ActorRef
                                           ): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId) {
      if (!features.Listed && squad.Task.nonEmpty && squad.ZoneId > 0) {
        features.Listed = true
        subscriptions.InitialAssociation(features)
        subscriptions.Publish(sendTo, SquadResponse.SetListSquad(squad.GUID))
        context.parent ! SquadService.UpdateSquadList(features, None)
      }
    }
  }

  def SquadActionDefinitionStopListSquad(tplayer: Player, sendTo: ActorRef): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId) {
      if (features.Listed) {
        features.Listed = false
        subscriptions.Publish(sendTo, SquadResponse.SetListSquad(PlanetSideGUID(0)))
        context.parent ! SquadService.UpdateSquadList(features, None)
      }
    }
  }

  def SquadActionDefinitionResetAll(tplayer: Player): Unit = {
    val squad = features.Squad
    if (squad.Leader.CharId == tplayer.CharId) {
      squad.Task = ""
      squad.ZoneId = None
      squad.Availability.indices.foreach { i =>
        squad.Availability.update(i, true)
      }
      squad.Membership.foreach(position => {
        position.Role = ""
        position.Orders = ""
        position.Requirements = Set()
      })
      features.LocationFollowsSquadLead = true
      features.AutoApproveInvitationRequests = true
      if (features.Listed) {
        //unlist the squad
        features.Listed = false
        subscriptions.Publish(features.ToChannel, SquadResponse.SetListSquad(PlanetSideGUID(0)))
        context.parent ! SquadService.UpdateSquadList(features, None)
      }
      subscriptions.UpdateSquadDetail(features)
      subscriptions.InitialAssociation(features)
      features.InitialAssociation = true
    }
  }

  def SquadActionDefinitionSelectRoleForYourself(tplayer: Player, position: Int): Unit = {
    //no swap to squad leader position, ever
    val squad = features.Squad
    if (position != 0 && position < squad.Size && squad.Availability(position)) {
      //this may give someone a position that they might not otherwise be able to fulfill for free
      val toMember: Member = squad.Membership(position)
      squad.Membership.indexWhere { p => p.CharId == tplayer.CharId } match {
        case -1 =>
          //no swap
        case 0 =>
          //the squad leader must stay the squad leader, position 0
          //don't swap between the actual positions, just swap details of the position
          val fromMember = squad.Membership.head
          val fromMemberPositionRole = fromMember.Role
          val fromMemberPositionOrders = fromMember.Orders
          val fromMemberPositionReq = fromMember.Requirements
          fromMember.Role = toMember.Role
          fromMember.Orders = toMember.Orders
          fromMember.Requirements = toMember.Requirements
          toMember.Role = fromMemberPositionRole
          toMember.Orders = fromMemberPositionOrders
          toMember.Requirements = fromMemberPositionReq
          subscriptions.UpdateSquadDetail(
            features,
            SquadDetail().Members(
              List(
                SquadPositionEntry(0, SquadPositionDetail()
                  .Role(fromMemberPositionRole)
                  .DetailedOrders(fromMemberPositionOrders)
                  .Requirements(fromMemberPositionReq)
                ),
                SquadPositionEntry(position, SquadPositionDetail()
                  .Role(toMember.Role)
                  .DetailedOrders(toMember.Orders)
                  .Requirements(toMember.Requirements)
                )
              )
            )
          )
        case index
          if index != position && squad.isAvailable(position, tplayer.avatar.certifications) =>
          //only validate if the requesting player is qualified to swap between these positions
          val fromMember = squad.Membership(index)
          SwapMemberPosition(toMember, fromMember)
          subscriptions.Publish(
            features.ToChannel,
            SquadResponse.AssignMember(squad, index, position)
          )
          subscriptions.UpdateSquadDetail(
            features,
            SquadDetail().Members(List(
              SquadPositionEntry(index, SquadPositionDetail().Player(fromMember.CharId, fromMember.Name)),
              SquadPositionEntry(position, SquadPositionDetail().Player(toMember.CharId, toMember.Name))
            ))
          )
        case _ => ;
          //no swap
      }
    }
  }

  def SquadActionDefinitionAssignSquadMemberToRole(
                                                    char_id: Long,
                                                    position: Int
                                                  ): Unit = {
    val squad = features.Squad
    val membership = squad.Membership.zipWithIndex
    (membership.find({ case (member, _) => member.CharId == char_id }), membership(position)) match {
      //TODO squad leader currently disallowed
      case (Some((fromMember, fromPosition)), (toMember, _)) if fromPosition != 0 =>
        val name = fromMember.Name
        SwapMemberPosition(toMember, fromMember)
        subscriptions.Publish(features.ToChannel, SquadResponse.AssignMember(squad, fromPosition, position))
        subscriptions.UpdateSquadDetail(
          features,
          SquadDetail().Members(
            List(
              SquadPositionEntry(position, SquadPositionDetail().Player(fromMember.CharId, fromMember.Name)),
              SquadPositionEntry(fromPosition, SquadPositionDetail().Player(char_id, name))
            )
          )
        )
      case _ => ;
    }
  }

  def SquadActionDefinitionDisplaySquad(sendTo: ActorRef): Unit = {
    val squad = features.Squad
    subscriptions.Publish(sendTo, SquadResponse.Detail(squad.GUID, SquadService.PublishFullDetails(squad)))
  }

  def SquadActionWaypoint(tplayer: Player, waypointType: SquadWaypoint, info: Option[WaypointInfo]): Unit = {
    val playerCharId = tplayer.CharId
    (if (waypointType.subtype == WaypointSubtype.Laze) {
      info match {
        case Some(winfo) =>
          //laze rally can be updated by any squad member
          //the laze-indicated target waypoint is not retained
          val curr = System.currentTimeMillis()
          val clippedLazes = {
            val index = lazeWaypoints.indexWhere { _.charId == playerCharId }
            if (index > -1) {
              lazeWaypoints.take(index) ++ lazeWaypoints.drop(index + 1)
            }
            else {
              lazeWaypoints
            }
          }
          if (lazeWaypoints.isEmpty || clippedLazes.headOption != lazeWaypoints.headOption) {
            //reason to retime blanking
            lazeIndexBlanking.cancel()
            import scala.concurrent.ExecutionContext.Implicits.global
            lazeIndexBlanking = lazeWaypoints.headOption match {
              case Some(data) =>
                context.system.scheduler.scheduleOnce(math.min(0, data.endTime - curr).milliseconds, self, SquadSwitchboard.BlankLazeWaypoints)
              case None =>
                context.system.scheduler.scheduleOnce(15.seconds, self, SquadSwitchboard.BlankLazeWaypoints)
            }
          }
          lazeWaypoints = clippedLazes :+ SquadSwitchboard.LazeWaypointData(playerCharId, waypointType.value, curr + 15000)
          Some(WaypointData(winfo.zone_number, winfo.pos))
        case _ =>
          None
      }
    } else if (playerCharId == features.Squad.Leader.CharId) {
      //only the squad leader may update other squad waypoints
      info match {
        case Some(winfo) =>
          features.AddWaypoint(features.Squad.GUID, waypointType, winfo)
        case _ =>
          features.RemoveWaypoint(features.Squad.GUID, waypointType)
          None
      }
    }) match {
      case Some(_) =>
        //waypoint added or updated
        subscriptions.Publish(
          features.ToChannel,
          SquadResponse.WaypointEvent(WaypointEventAction.Add, playerCharId, waypointType, None, info, 1),
          Seq(playerCharId)
        )
      case None =>
        //waypoint removed
        subscriptions.Publish(
          features.ToChannel,
          SquadResponse.WaypointEvent(WaypointEventAction.Remove, playerCharId, waypointType, None, None, 0),
          Seq(playerCharId)
        )
    }
  }

  def TryBlankLazeWaypoints(): Unit = {
    lazeIndexBlanking.cancel()
    val curr = System.currentTimeMillis()
    val blank = lazeWaypoints.takeWhile { data => curr >= data.endTime }
    lazeWaypoints = lazeWaypoints.drop(blank.size)
    blank.foreach { data =>
      subscriptions.Publish(
        features.ToChannel,
        SquadResponse.WaypointEvent(WaypointEventAction.Remove, data.charId, SquadWaypoint(data.waypointType), None, None, 0),
        Seq()
      )
    }
    //retime
    lazeWaypoints match {
      case Nil => ;
      case x :: _ =>
        import scala.concurrent.ExecutionContext.Implicits.global
        lazeIndexBlanking = context.system.scheduler.scheduleOnce(
          math.min(0, x.endTime - curr).milliseconds,
          self,
          SquadSwitchboard.BlankLazeWaypoints
        )
    }
  }

  /**
    * Dispatch all of the information about a given squad's waypoints to a user.
    * @param toCharId the player to whom the waypoint data will be dispatched
    * @param features the squad
    */
  def InitWaypoints(toCharId: Long, features: SquadFeatures): Unit = {
    val squad = features.Squad
    val vz1   = Vector3.z(value = 1)
    val list  = features.Waypoints
    subscriptions.Publish(
      toCharId,
      SquadResponse.InitWaypoints(
        squad.Leader.CharId,
        list.zipWithIndex.collect {
          case (point, index) if point.pos != vz1 =>
            (SquadWaypoint(index), WaypointInfo(point.zone_number, point.pos), 1)
        }
      )
    )
  }

  def SquadActionUpdate(
                         charId: Long,
                         guid: PlanetSideGUID,
                         health: Int,
                         maxHealth: Int,
                         armor: Int,
                         maxArmor: Int,
                         pos: Vector3,
                         zoneNumber: Int,
                         sendTo: ActorRef
                       ): Unit = {
    val squad = features.Squad
    squad.Membership.find(_.CharId == charId) match {
      case Some(member) =>
        member.GUID = guid
        member.Health = StatConverter.Health(health, maxHealth, min = 1, max = 64)
        member.Armor = StatConverter.Health(armor, maxArmor, min = 1, max = 64)
        member.Position = pos
        member.ZoneId = zoneNumber
        subscriptions.Publish(
          sendTo,
          SquadResponse.UpdateMembers(
            squad,
            squad.Membership
              .filterNot {
                _.CharId == 0
              }
              .map { member =>
                SquadAction.Update(member.CharId, PlanetSideGUID(0), member.Health, 0, member.Armor, 0, member.Position, member.ZoneId)
              }
              .toList
          )
        )
      case _ => ;
    }
  }
}

object SquadSwitchboard {
  private case class LazeWaypointData(charId: Long, waypointType: Int, endTime: Long)

  private case object BlankLazeWaypoints

  final case class Join(player: Player, position: Int, replyTo: ActorRef)

  final case class Leave(charId: Long)

  final case class To(member: Long, msg: SquadServiceResponse)

  final case class ToAll(msg: SquadServiceResponse)

  final case class Except(excluded_member: Long, msg: SquadServiceResponse)

  /**
    * Behaviors and exchanges necessary to undo the recruitment process for the squad role.<br>
    * <br>
    * The complement of the prior `LeaveSquad` method.
    * This method deals entirely with other squad members observing the given squad member leaving the squad
    * while the other method handles messaging only for the squad member who is leaving.
    * The distinction is useful when unsubscribing from this service,
    * as the `ActorRef` object used to message the player's client is no longer reliable
    * and has probably ceased to exist.
    * @see `LeaveSquad`
    * @see `Publish`
    * @see `SquadDetail`
    * @see `SquadInfo`
    * @see `SquadPositionDetail`
    * @see `SquadPositionEntry`
    * @see `SquadResponse.Leave`
    * @see `UpdateSquadDetail`
    * @see `UpdateSquadListWhenListed`
    * @param charId the player
    * @param features the squad
    * @param entry a paired membership role with its index in the squad
    * @return if a role/index pair is provided
    */
  def PanicLeaveSquad(
                       charId: Long,
                       features: SquadFeatures,
                       entry: Option[(Member, Int)],
                       subscriptions: SquadSubscriptionEntity,
                       squadDetailActorRef: ActorRef,
                       log: Logger
                     ): Boolean = {
    val squad = features.Squad
    entry match {
      case Some((member, index)) =>
        log.info(s"${member.Name}-${squad.Faction} has left squad #${squad.GUID.guid} - ${squad.Task}")
        val entry = (charId, index)
        //member leaves the squad completely
        //memberToSquad.remove(charId)
        member.Name = ""
        member.CharId = 0
        //other squad members see the member leaving
        subscriptions.Publish(features.ToChannel, SquadResponse.Leave(squad, List(entry)), Seq(charId))
        subscriptions.UpdateSquadDetail(
          features,
          SquadDetail().Members(List(SquadPositionEntry(index, SquadPositionDetail().Player(char_id = 0, name = ""))))
        )
        squadDetailActorRef ! SquadService.UpdateSquadListWhenListed(features, SquadInfo().Size(squad.Size))
        true
      case _ =>
        false
    }
  }

  /**
    * Clear the current detail about a squad's membership and replace it with a previously stored details.
    * @param squad the squad
    * @param favorite the loadout object
    */
  def LoadSquadDefinition(squad: Squad, favorite: SquadLoadout): Unit = {
    squad.Task = favorite.task
    squad.ZoneId = favorite.zone_id.getOrElse(squad.ZoneId)
    squad.Availability.indices.foreach { index => squad.Availability.update(index, false) }
    squad.Membership.foreach { position =>
      position.Role = ""
      position.Orders = ""
      position.Requirements = Set()
    }
    favorite.members.foreach { position =>
      squad.Availability.update(position.index, true)
      val member = squad.Membership(position.index)
      member.Role = position.role
      member.Orders = position.orders
      member.Requirements = position.requirements
    }
  }
}
