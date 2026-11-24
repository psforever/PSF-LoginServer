// Copyright (c) 2025 PSForever
// Copyright (c) 2026 PSForever (modifications and additions)
// SPDX-License-Identifier: GPL-3.0-only AND AGPL-3.0-or-later

package net.psforever.actors.session.support

import io.getquill.{Action, ActionReturning, EntityQuery, Insert, PostgresJAsyncContext, Query, Quoted, SnakeCase}
import net.psforever.objects.avatar.PlayerControl
import net.psforever.objects.zones.Zone
import net.psforever.objects.Player
import net.psforever.packet.game.packets.OutfitEventAction.{Initial, Leaving, OutfitInfo, OutfitRankNames, ListEnd, FormationPersistedNotify, MembershipPersistedNotify, Update, UpdateMemberCount}
import net.psforever.packet.game.packets.OutfitMembershipResponse.Error
import net.psforever.packet.game.packets.OutfitMembershipResponse.PacketType.CreateResponse
import net.psforever.packet.game.packets.{ChatMsg, OutfitEvent, OutfitEventAction, OutfitListEvent, OutfitListEventAction, OutfitMemberEvent, OutfitMemberEventAction, OutfitMemberUpdate, OutfitMembershipResponse}
import net.psforever.services.avatar.AvatarAction
import net.psforever.services.base.envelope.{BundledEnvelope, MessageEnvelope}
import net.psforever.services.base.message.{PlanetsideAttribute, SendResponse}
import net.psforever.services.chat.OutfitChannel
import net.psforever.types.ChatMessageType
import net.psforever.util.Config

import java.time.{LocalDateTime, ZoneId}
import java.util.concurrent.Executors
import scala.util.{Failure, Success}

object SessionOutfitHandlers {

  case class Avatar(id: Long, name: String, faction_id: Int, last_login: java.time.LocalDateTime)
  case class Outfit(
    id: Long,
    name: String,
    faction: Int,
    owner_id: Long,
    motd: Option[String],
    created: java.time.LocalDateTime,
    deleted: Boolean,
    decal: Int,
    rank0: Option[String],
    rank1: Option[String],
    rank2: Option[String],
    rank3: Option[String],
    rank4: Option[String],
    rank5: Option[String],
    rank6: Option[String],
    rank7: Option[String]
  )
  case class Outfitmember(id: Long, outfit_id: Long, avatar_id: Long, rank: Int)
  case class Outfitpoint(id: Long, outfit_id: Long, avatar_id: Option[Long], points: Long)
  case class OutfitpointMv(outfit_id: Long, points: Long)

  val ctx = new PostgresJAsyncContext(SnakeCase, Config.config.getConfig("database"))
  import ctx._

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future

  def HandleOutfitForm(outfitName: String, player: Player, session: SessionData): Unit = {
    val zone = player.Zone
    val zoneid = zone.id
    val charid = player.CharId
    val pname = player.Name
    val cleanedName = sanitizeOutfitName(outfitName)

    cleanedName match {
      case Some(validName) =>
        ctx.run(findOutfitByName(validName)).flatMap {
          case existing if existing.nonEmpty =>
            // the client displays the localized "@OutfitErrorNameAlreadyTaken" notification
            PlayerControl.sendResponse(zone, pname, OutfitMembershipResponse(
              CreateResponse, Error.NameAlreadyTaken, 0, charid, 0, validName, "", to_actor = true))
            Future.successful(())

          case _ =>
            createNewOutfit(validName, player.Faction.id, charid).map { outfit =>
              val outfitId = outfit.id
              val outfitName = outfit.name
              val seconds: Long = outfit.created.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli / 1000
              player.outfit_id = outfitId
              player.outfit_name = outfitName
              zone.AvatarEvents ! BundledEnvelope(
                MessageEnvelope(pname, SendResponse(
                  OutfitEvent(outfitId, Update(
                    OutfitInfo(
                      outfitName,
                      outfit_points1 = 0,
                      outfit_points2 = 0,
                      member_count = 1,
                      outfit_rank_names = OutfitRankNames("", "", "", "", "", "", "", ""),
                      motd = "",
                      outfit.decal,
                      formation_completed = true,
                      formation_deadline = 0,
                      created_timestamp = seconds,
                      owned_base_zone_id = 0,
                      owned_base_facility_id = 0,
                      owned_base_benefit_timeout = 0
                    )
                  )),
                  OutfitMemberUpdate(outfitId, charid, 7, flag = true),
                  ChatMsg(ChatMessageType.UNK_227, "@OutfitCreateSuccess"),
                  OutfitMembershipResponse(CreateResponse, Error.NoError, 0, charid, 0, "", "", to_actor = true),
                )),
                MessageEnvelope(
                  zoneid,
                  PlanetsideAttribute(player.GUID, 39, outfitId)
                ),
                MessageEnvelope(
                  zoneid,
                  player.GUID,
                  AvatarAction.PlanetsideStringAttribute(0, outfitName)
                )
              )
              session.chat.JoinChannel(OutfitChannel(outfitId))
              // the trailing database-persistence echo of the outfit creation
              scheduleOutfitPersistenceNotify(outfitId, FormationPersistedNotify(), zone, pname)
            }
              .recover { case e =>
                e.printStackTrace()
                PlayerControl.sendResponse(zone, pname, OutfitMembershipResponse(
                  CreateResponse, Error.DatabaseError, 0, charid, 0, validName, "", to_actor = true))
              }
        }
      case None =>
        PlayerControl.sendResponse(zone, pname, OutfitMembershipResponse(
          CreateResponse, Error.NameIsInvalid, 0, charid, 0,
          outfitName, "", to_actor = true))
    }
  }

  def HandleOutfitInvite(zones: Seq[Zone], invitedName: String, sentFrom: Player): Unit = {
    findPlayerByNameForOutfitAction(zones, invitedName, sentFrom).foreach { invitedPlayer =>

      PlayerControl.sendResponse(invitedPlayer.Zone, invitedPlayer.Name,
        OutfitMembershipResponse(OutfitMembershipResponse.PacketType.Invite, Error.NoError, 0,
          sentFrom.CharId, sentFrom.CharId, sentFrom.Name, sentFrom.outfit_name, to_actor = false))

      PlayerControl.sendResponse(sentFrom.Zone, sentFrom.Name,
        OutfitMembershipResponse(OutfitMembershipResponse.PacketType.Invite, Error.NoError, 0,
          sentFrom.CharId, invitedPlayer.CharId, invitedPlayer.Name, sentFrom.outfit_name, to_actor = true))

      // show the pending invitation in the inviter's outfit window:
      // a member row with `joined = false` and the inviter's char id is only
      // rendered on the inviter's client (client row logic 0x4c8b40/0x4c97b0)
      PlayerControl.sendResponse(sentFrom.Zone, sentFrom.Name,
        OutfitMemberEvent(sentFrom.outfit_id, invitedPlayer.CharId,
          OutfitMemberEventAction.Update(
            invitedPlayer.Name,
            rank = 0,
            points = 0,
            last_online = 0,
            joined = false,
            invited_by = Some(sentFrom.CharId)
          )))

      val outfitInvite = OutfitInvite(invitedPlayer, sentFrom)
      OutfitInviteManager.addOutfitInvite(outfitInvite)
    }
  }

  def HandleOutfitInviteAccept(zones: Seq[Zone], invited: Player, session: SessionData): Unit = {
    val toName = invited.Name
    val toCharId = invited.CharId
    val toZone = invited.Zone
    val toZoneId = toZone.id
    val toGuid = invited.GUID
    OutfitInviteManager.getOutfitInvite(toCharId) match {
      case Some(outfitInvite) =>
        val fromName = outfitInvite.sentFrom.Name
        val fromCharId = outfitInvite.sentFrom.CharId
        val fromZone = outfitInvite.sentFrom.Zone
        val outfitId = outfitInvite.sentFrom.outfit_id
        (for {
          _           <- addMemberToOutfit(outfitId, toCharId)
          outfitOpt   <- ctx.run(getOutfitById(outfitId)).map(_.headOption)
          memberCount <- ctx.run(getOutfitMemberCount(outfitId))
          points      <- ctx.run(getOutfitPoints(outfitId)).map(_.headOption.map(_.points).getOrElse(0L))
        } yield (outfitOpt, memberCount, points))
          .map {
          case (Some(outfit), memberCount, points) =>
            val outfitName = outfit.name
            val seconds: Long = outfit.created.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli / 1000
            fromZone.AvatarEvents ! BundledEnvelope(
              MessageEnvelope(fromName, SendResponse(
                OutfitMembershipResponse(
                  OutfitMembershipResponse.PacketType.InviteAccepted, Error.NoError, 0,
                  toCharId, fromCharId, toName, outfitName, to_actor = false
                ),
                OutfitEvent(outfitId, UpdateMemberCount(memberCount)),
                OutfitMemberEvent(outfitId, toCharId,
                  OutfitMemberEventAction.Update(toName, rank = 0, points = 0, last_online = 0, joined = true, invited_by = None)
                )
              ))
            )
            toZone.AvatarEvents ! BundledEnvelope(
              MessageEnvelope(toName, SendResponse(
                OutfitMembershipResponse(
                  OutfitMembershipResponse.PacketType.InviteAccepted, Error.NoError, 0,
                  toCharId, fromCharId, toName, outfitName, to_actor = true
                ),
                OutfitEvent(outfitId, Initial(OutfitInfo(
                  outfitName, points, points, memberCount,
                  OutfitRankNames("", "", "", "", "", "", "", ""),
                  outfit.motd.getOrElse(""),
                  outfit.decal,
                  formation_completed = true,
                  formation_deadline = 0,
                  created_timestamp = seconds,
                  owned_base_zone_id = 0,
                  owned_base_facility_id = 0,
                  owned_base_benefit_timeout = 0
                ))),
                OutfitMemberUpdate(outfitId, toCharId, 0, flag=true)
              )),
              MessageEnvelope(
                toZoneId,
                PlanetsideAttribute(toGuid, 39, outfitId)
              ),
              MessageEnvelope(
                toZoneId,
                toGuid,
                AvatarAction.PlanetsideStringAttribute(0, outfitName)
              )
            )
            OutfitInviteManager.removeOutfitInvite(toCharId)
            session.chat.JoinChannel(OutfitChannel(outfitId))
            invited.outfit_id = outfitId
            invited.outfit_name = outfitName
            // the trailing database-persistence echo of the member join
            scheduleOutfitPersistenceNotify(outfitId, MembershipPersistedNotify(), toZone, toName)
          case (None, _, _) => ()
            PlayerControl.sendResponse(toZone, toName,
              ChatMsg(ChatMessageType.UNK_227, "Failed to join outfit")
            )
          }
          .recover { case _ =>
            PlayerControl.sendResponse(toZone, toName,
              ChatMsg(ChatMessageType.UNK_227, "Failed to join outfit")
            )
          }
      case None => ()
    }
  }

  def HandleOutfitInviteReject(invited: Player): Unit = {
    OutfitInviteManager.getOutfitInvite(invited.CharId) match {
      case Some(outfitInvite) =>

        PlayerControl.sendResponse(outfitInvite.sentFrom.Zone, outfitInvite.sentFrom.Name,
          OutfitMembershipResponse(
            OutfitMembershipResponse.PacketType.InviteRejected, Error.NoError, 0,
            invited.CharId, outfitInvite.sentFrom.CharId, invited.Name, "", to_actor = false))

        PlayerControl.sendResponse(invited.Zone, invited.Name,
          OutfitMembershipResponse(
            OutfitMembershipResponse.PacketType.InviteRejected, Error.NoError, 0,
            invited.CharId, outfitInvite.sentFrom.CharId, invited.Name, "", to_actor = true))

        // clear the pending-invitation row in the inviter's outfit window
        PlayerControl.sendResponse(outfitInvite.sentFrom.Zone, outfitInvite.sentFrom.Name,
          OutfitMemberEvent(outfitInvite.sentFrom.outfit_id, invited.CharId, OutfitMemberEventAction.Kicked()))

        OutfitInviteManager.removeOutfitInvite(invited.CharId)
      case None =>
    }
  }

  /**
    * A player withdrew an outfit invitation they had previously extended.
    * The client offers this through the outfit window's member list (and the
    * `/outfitcancel` chat command) - the `OutfitMembershipRequest`'s `CancelInvite` action.
    * Only the player who extended the invitation may cancel it; both sides receive the
    * `InviteCancelled` response family, the invited player's dialog being withdrawn with
    * `flag = false` and the inviter seeing the confirmation with `flag = true`.
    */
  def HandleOutfitInviteCancel(zones: Seq[Zone], cancelledId: Long, cancelledName: String, cancelledBy: Player): Unit = {
    val cancelledOpt = findPlayerByNameForOutfitAction(zones, cancelledName, cancelledBy)
      .orElse(findPlayerByIdForOutfitAction(zones, cancelledId, cancelledBy))
    cancelledOpt match {
      case Some(cancelled) =>
        OutfitInviteManager.getOutfitInvite(cancelled.CharId) match {
          case Some(invite) if invite.sentFrom.CharId == cancelledBy.CharId =>
            PlayerControl.sendResponse(cancelled.Zone, cancelled.Name,
              OutfitMembershipResponse(
                OutfitMembershipResponse.PacketType.InviteCancelled, Error.NoError, 0,
                cancelledBy.CharId, cancelled.CharId, cancelledBy.Name, "", to_actor = false))

            PlayerControl.sendResponse(cancelledBy.Zone, cancelledBy.Name,
              OutfitMembershipResponse(
                OutfitMembershipResponse.PacketType.InviteCancelled, Error.NoError, 0,
                cancelledBy.CharId, cancelled.CharId, cancelled.Name, "", to_actor = true))

            // clear the pending-invitation row in the inviter's outfit window
            PlayerControl.sendResponse(cancelledBy.Zone, cancelledBy.Name,
              OutfitMemberEvent(cancelledBy.outfit_id, cancelled.CharId, OutfitMemberEventAction.Kicked()))

            OutfitInviteManager.removeOutfitInvite(cancelled.CharId)
          case _ => ()
        }
      case None =>
        // the client displays the localized "@OutfitErrorNoPlayer" notification
        PlayerControl.sendResponse(cancelledBy.Zone, cancelledBy.Name, OutfitMembershipResponse(
          OutfitMembershipResponse.PacketType.InviteCancelled, Error.NoPlayer, 0,
          cancelledBy.CharId, cancelledId, cancelledName, "", to_actor = true))
    }
  }

  def HandleOutfitKick(zones: Seq[Zone], kickedId: Long, kickedBy: Player, session: SessionData): Unit = {
    val outfit_id = kickedBy.outfit_id
    // if same id, player has left the outfit by their own choice
    if (kickedId == kickedBy.CharId) {
      // store outfit_id since it will be nulled soon
      removeMemberFromOutfit(outfit_id, kickedId).map {
        case (deleted, _) =>
          if (deleted > 0) {
            kickedBy.Zone.AvatarEvents ! BundledEnvelope(
              MessageEnvelope(kickedBy.Name, SendResponse(OutfitEvent(outfit_id, Leaving()))),
              MessageEnvelope(
                kickedBy.Zone.id,
                PlanetsideAttribute(kickedBy.GUID, 39, 0)
              ),
              MessageEnvelope(
                kickedBy.Zone.id,
                kickedBy.GUID,
                AvatarAction.PlanetsideStringAttribute(0, "")
              )
            )
            session.chat.LeaveChannel(OutfitChannel(outfit_id))
            kickedBy.outfit_name = ""
            kickedBy.outfit_id = 0
            // the trailing database-persistence echo of the member removal
            scheduleOutfitPersistenceNotify(outfit_id, MembershipPersistedNotify(), kickedBy.Zone, kickedBy.Name)

            zones
              .filter(z => z.AllPlayers.nonEmpty)
              .flatMap(_.AllPlayers)
              .filter(p => p.outfit_id == outfit_id)
              .foreach(outfitMember =>
                PlayerControl.sendResponse(outfitMember.Zone, outfitMember.Name,
                  OutfitMemberEvent(outfit_id, kickedId, OutfitMemberEventAction.Kicked())
                )
              )
          }
      }.recover { case e =>
        e.printStackTrace()
      }
    }
    else {
      removeMemberFromOutfit(outfit_id, kickedId).map {
        case (deleted, _) =>
          if (deleted > 0) {
            // the trailing database-persistence confirmation of the kicker's action
            scheduleOutfitPersistenceNotify(outfit_id, MembershipPersistedNotify(), kickedBy.Zone, kickedBy.Name)
            findPlayerByIdForOutfitAction(zones, kickedId, kickedBy).foreach { kicked =>
              kicked.Zone.AvatarEvents ! BundledEnvelope(
                MessageEnvelope(kicked.Name, SendResponse(
                  OutfitEvent(outfit_id, Leaving()),
                  // type `Kicked` with `flag = false` makes the client display
                  // its "@OutfitResponseMemberKicked" notification to the kicked player
                  OutfitMembershipResponse(
                    OutfitMembershipResponse.PacketType.Kicked, Error.NoError, 1,
                    kickedBy.CharId, kicked.CharId, kickedBy.Name, kicked.Name, to_actor = false
                  )
                )),
                MessageEnvelope(kicked.Zone.id,
                  PlanetsideAttribute(kicked.GUID, 39, 0)
                ),
                MessageEnvelope(
                  kicked.Zone.id,
                  kicked.GUID,
                  AvatarAction.PlanetsideStringAttribute(0, "")
                ),
                MessageEnvelope(kicked.Name,
                  AvatarAction.RemoveFromOutfitChat(outfit_id)
                ),
                MessageEnvelope(kicked.Name, SendResponse(
                  OutfitMemberEvent(outfit_id, kickedId, OutfitMemberEventAction.Kicked())
                ))
              )
              kicked.outfit_id = 0
              kicked.outfit_name = ""
            }
            val avatarName: Future[Option[String]] =
            ctx.run(quote { query[Avatar].filter(_.id == lift(kickedId)).map(_.name) }).map(_.headOption)

            avatarName.foreach {
              case Some(name) => PlayerControl.sendResponse(kickedBy.Zone, kickedBy.Name,
                OutfitMembershipResponse(OutfitMembershipResponse.PacketType.Kicked, Error.NoError, 1, kickedBy.CharId, kickedId, name, "", to_actor = true))

              case None => PlayerControl.sendResponse(kickedBy.Zone, kickedBy.Name,
                OutfitMembershipResponse(OutfitMembershipResponse.PacketType.Kicked, Error.NoError, 1, kickedBy.CharId, kickedId, "NameNotFound", "", to_actor = true))
            }
            zones
              .filter(z => z.AllPlayers.nonEmpty)
              .flatMap(_.AllPlayers)
              .filter(p => p.outfit_id == kickedBy.outfit_id)
              .foreach(outfitMember =>
                PlayerControl.sendResponse(outfitMember.Zone, outfitMember.Name,
                  OutfitMemberEvent(kickedBy.outfit_id, kickedId, OutfitMemberEventAction.Kicked())
                )
            )
            // this needs to be the kicked player
            // session.chat.LeaveChannel(OutfitChannel(kickedBy.outfit_id))
            // new number of outfit members?
            //PlayerControl.sendResponse(kickedBy.Zone, kickedBy.Name, OutfitEvent(kickedBy.outfit_id, OutfitEventAction.Unk5(34)))
          }
      }.recover { case e =>
        e.printStackTrace()
      }
    }
  }

  def HandleOutfitPromote(zones: Seq[Zone], promotedId: Long, newRank: Int, promoter: Player): Unit = {
    val outfit_id = promoter.outfit_id
    findPlayerByIdForOutfitAction(zones, promotedId, promoter).foreach { promoted =>
      if (newRank == 7) {
        // demote owner to rank 6
        // promote promoted to rank 7
        // update outfit
        updateOutfitOwner(outfit_id, promoter.avatar.id, promoted.avatar.id)
        // TODO: does every member get the notification like this?
        getOutfitMemberPoints(outfit_id, promoter.avatar.id).map {
          owner_points =>
            // announce owner rank change
            zones.foreach(zone => {
              zone.AllPlayers
                .filter(_.outfit_id == outfit_id)
                .foreach(outfitMember => {
                  PlayerControl.sendResponse(
                    zone, outfitMember.Name,
                    OutfitMemberEvent(outfit_id, promoter.avatar.id,
                      OutfitMemberEventAction.Update(promoter.Name, 6, owner_points, last_online = 0, joined = true, invited_by = None)))
                })
            })
        }
        // update promoter rank
        PlayerControl.sendResponse(
          promoter.Zone, promoter.Name,
          OutfitMemberUpdate(outfit_id, promoter.avatar.id, rank = 6, flag = true))
      }
      else {
        // promote promoted
        updateOutfitMemberRank(outfit_id, promoted.avatar.id, rank = newRank)
      }

      // TODO: does every member get the notification like this?
      getOutfitMemberPoints(outfit_id, promoted.avatar.id).map {
        member_points =>
          zones.foreach { zone =>
            // tell everyone about the new rank of the promoted member
            val messages = zone.AllPlayers
              .filter(_.outfit_id == outfit_id)
              .map { player =>
                MessageEnvelope(player.Name, SendResponse(
                  OutfitMemberEvent(outfit_id, promoted.avatar.id,
                    OutfitMemberEventAction.Update(promoted.Name, newRank, member_points, last_online = 0, joined = true, invited_by = None)
                  )
                ))
              }
            zone.AvatarEvents ! BundledEnvelope(messages)
          }
      }

      // update promoted rank
      PlayerControl.sendResponse(
        promoted.Zone, promoted.Name,
        OutfitMemberUpdate(outfit_id, promoted.avatar.id, rank = newRank, flag = true))
    }
  }

  def HandleViewOutfitWindow(zones: Seq[Zone], player: Player, outfitId: Long): Unit = {
    val outfitDetailsF = for {
      outfitOpt <- ctx.run(getOutfitById(outfitId)).map(_.headOption)
      memberCount <- ctx.run(query[Outfitmember].filter(_.outfit_id == lift(outfitId)).size)
      pointsTotal <- ctx.run(querySchema[OutfitpointMv]("outfitpoint_mv").filter(_.outfit_id == lift(outfitId)))
    } yield (outfitOpt, memberCount, pointsTotal.headOption.map(_.points).getOrElse(0L))
    val membersF = ctx.run(getOutfitMembersWithDetails(outfitId))

    for {
      (outfitOpt, memberCount, totalPoints) <- outfitDetailsF
      members                              <- membersF
    } yield {
      outfitOpt.foreach { outfit =>
        val seconds: Long = outfit.created.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli / 1000
        val outfitEventInitial = OutfitEvent(outfit.id, Initial(OutfitInfo(
          outfit.name,
          totalPoints,
          totalPoints,
          memberCount,
          OutfitRankNames(
            outfit.rank0.getOrElse(""),
            outfit.rank1.getOrElse(""),
            outfit.rank2.getOrElse(""),
            outfit.rank3.getOrElse(""),
            outfit.rank4.getOrElse(""),
            outfit.rank5.getOrElse(""),
            outfit.rank6.getOrElse(""),
            outfit.rank7.getOrElse(""),
          ),
          outfit.motd.getOrElse(""),
          outfit.decal,
          formation_completed = true,
          formation_deadline = 0,
          created_timestamp = seconds,
          owned_base_zone_id = 0,
          owned_base_facility_id = 0,
          owned_base_benefit_timeout = 0
        )))
        val memberEventList = members.map { case (avatarId, avatarName, points, rank, login) =>
          val lastLogin = findPlayerByIdForOutfitAction(zones, avatarId, player) match {
            case Some(_) => 0L
            case None if player.Name == avatarName => 0L
            case None =>  (System.currentTimeMillis() - login.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli) / 1000
          }
          OutfitMemberEvent(outfit.id, avatarId,
            OutfitMemberEventAction.Update(
              avatarName,
              rank,
              points,
              lastLogin,
              joined = true,
              invited_by = None)
          )
        }
        player.Zone.AvatarEvents ! MessageEnvelope(player.Name, SendResponse(
          outfitEventInitial +: memberEventList :+ OutfitEvent(outfit.id, ListEnd())
        ))
      }
    }
  }

  def HandleGetOutfitList(player: Player): Unit = {
    val q = getOutfitsByEmpire(player.Faction.id)
    val futureResult = ctx.run(q)

    futureResult.onComplete {
      case Success(rows) =>
        player.Zone.AvatarEvents ! MessageEnvelope(player.Name, SendResponse(
          // the client only clears its outfit-browser rows when a `ListBegin` (action 0)
          // arrives first (handler 0x4c7db0); without it, every browser opening would
          // append to the rows already collected
          OutfitListEvent(OutfitListEventAction.ListBegin()) +:
            rows.map { case (outfitId, points, name, leaderName, memberCount) =>
              OutfitListEvent(
                OutfitListEventAction.ListElementOutfit(outfitId, points, memberCount, name, leaderName)
              )
            } :+
            OutfitListEvent(OutfitListEventAction.ListEnd())
        ))

      case Failure(_) =>
        PlayerControl.sendResponse(player.Zone, player.Name,
          ChatMsg(ChatMessageType.UNK_227, "Outfit list failed to return")
        )
    }
  }

  def HandleOutfitMotd(zones: Seq[Zone], message: String, player: Player): Unit = {
    val outfit_id = player.outfit_id
    // normalize: the outfit table stores the message of the day in a VARCHAR(256);
    // the client's message editor does not enforce a length below its own cap.
    // The clamp is the client's own: its editor submit path truncates the input to
    // 256 wide characters (client 3.15.84)
    // Emptying the message clears the stored value.
    val motd: Option[String] = message.trim match {
      case ""  => None
      case msg => Some(msg.take(256))
    }
    val outfitDetails = for {
      _           <- updateOutfitMotd(outfit_id, motd)
      outfitOpt   <- ctx.run(getOutfitById(outfit_id)).map(_.headOption)
      memberCount <- ctx.run(query[Outfitmember].filter(_.outfit_id == lift(outfit_id)).size)
      pointsTotal <- ctx.run(querySchema[OutfitpointMv]("outfitpoint_mv").filter(_.outfit_id == lift(outfit_id)))
    } yield (outfitOpt, memberCount, pointsTotal.headOption.map(_.points).getOrElse(0L))

    (for {
      (outfitOpt, memberCount, totalPoints) <- outfitDetails
    } yield {
      outfitOpt.foreach { outfit =>
        // send to all online players in outfit with the window open
        val outfit_event = SendResponse(OutfitEvent(
          outfit_id,
          Update(
            OutfitInfo(
              outfit_name = outfit.name,
              outfit_points1 = totalPoints,
              outfit_points2 = totalPoints,
              member_count = memberCount,
              outfit_rank_names = OutfitRankNames(
                outfit.rank0.getOrElse(""),
                outfit.rank1.getOrElse(""),
                outfit.rank2.getOrElse(""),
                outfit.rank3.getOrElse(""),
                outfit.rank4.getOrElse(""),
                outfit.rank5.getOrElse(""),
                outfit.rank6.getOrElse(""),
                outfit.rank7.getOrElse(""),
              ),
              motd = outfit.motd.getOrElse(""),
              decal_id = outfit.decal,
              formation_completed = true,
              formation_deadline = 0,
              created_timestamp = outfit.created.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli / 1000,
              owned_base_zone_id = 0,
              owned_base_facility_id = 0,
              owned_base_benefit_timeout = 0
            )
          )
        ))

        zones.foreach { zone =>
          zone.AvatarEvents ! BundledEnvelope(
            zone.AllPlayers
              .filter { p => p.outfit_id == outfit_id && p.outfit_window_open }
              .map(p => MessageEnvelope(p.Name, outfit_event))
          )
        }
      }
    }) recoverWith {
      case e =>
        e.printStackTrace()
        // the client displays its localized "@OutfitErrorDatabaseError" notification
        PlayerControl.sendResponse(
          player.Zone,
          player.Name,
          OutfitMembershipResponse(
            OutfitMembershipResponse.PacketType.CreateResponse,
            Error.DatabaseError,
            variant = 0,
            actor_id = outfit_id,
            target_id = player.CharId,
            actor_name = player.Name,
            target_name = "",
            to_actor = true
          )
        )
        Future.failed(e)
    }

    // C >> S OutfitRequest(41593365, Motd(Vanu outfit for the planetside forever project!                                                                                      -find out more about the PSEMU project at PSforever.net))
    // S >> C OutfitEvent(Unk2, 529744, Unk2(OutfitInfo(PlanetSide_Forever_Vanu, 0, 0, 3, OutfitRankNames(, , , , , , , ), Vanu outfit for the planetside forever project!                                                                                      -find out more about the PSEMU project at PSforever.net, 0, 1, 0, 1458331641, 0, 0, 0)))
  }

  def HandleOutfitRank(zones: Seq[Zone], list: List[Option[String]], player: Player): Unit = {
    val outfit_id = player.outfit_id
    val outfitDetails = for {
      _           <- updateOutfitRanks(outfit_id, list)
      outfitOpt   <- ctx.run(getOutfitById(outfit_id)).map(_.headOption)
      memberCount <- ctx.run(query[Outfitmember].filter(_.outfit_id == lift(outfit_id)).size)
      pointsTotal <- ctx.run(querySchema[OutfitpointMv]("outfitpoint_mv").filter(_.outfit_id == lift(outfit_id)))
    } yield (outfitOpt, memberCount, pointsTotal.headOption.map(_.points).getOrElse(0L))

    (for {
      (outfitOpt, memberCount, totalPoints) <- outfitDetails
    } yield {
      outfitOpt.foreach { outfit =>
        // send to all online players in outfit with window open
        val outfit_event = SendResponse(OutfitEvent(
          outfit_id,
          Update(
            OutfitInfo(
              outfit_name = outfit.name,
              outfit_points1 = totalPoints,
              outfit_points2 = totalPoints,
              member_count = memberCount,
              outfit_rank_names = OutfitRankNames(
                outfit.rank0.getOrElse(""),
                outfit.rank1.getOrElse(""),
                outfit.rank2.getOrElse(""),
                outfit.rank3.getOrElse(""),
                outfit.rank4.getOrElse(""),
                outfit.rank5.getOrElse(""),
                outfit.rank6.getOrElse(""),
                outfit.rank7.getOrElse(""),
              ),
              motd = outfit.motd.getOrElse(""),
              decal_id = outfit.decal,
              formation_completed = true,
              formation_deadline = 0,
              created_timestamp = outfit.created.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli / 1000,
              owned_base_zone_id = 0,
              owned_base_facility_id = 0,
              owned_base_benefit_timeout = 0
            )
          )
        ))

        zones.foreach { zone =>
          zone.AvatarEvents ! BundledEnvelope(
            zone.AllPlayers
              .filter { p => p.outfit_id == outfit_id && p.outfit_window_open }
              .map(p => MessageEnvelope(p.Name, outfit_event))
          )
        }
      }
    }) recoverWith {
      case e =>
        e.printStackTrace()
        // the client displays its localized "@OutfitErrorDatabaseError" notification
        PlayerControl.sendResponse(
          player.Zone,
          player.Name,
          OutfitMembershipResponse(
            OutfitMembershipResponse.PacketType.CreateResponse,
            error = Error.DatabaseError,
            variant = 0,
            actor_id = outfit_id,
            target_id = player.CharId,
            actor_name = player.Name,
            target_name = "",
            to_actor = true
          )
        )
        Future.failed(e)
    }
  }

  private var outfitPointSheduleStarted = false

  /**
    * Re-emits the trailing outfit events found in stock-server packet logs:
    * roughly 15-20 seconds after an outfit was created, or after a member
    * joined, left, or was kicked, a second `OutfitEvent` naming that outfit
    * reached the player who had *performed* the change - action `Unk6` for
    * the creation of the outfit row, action `Unk7` for changes of the
    * outfitmember row. Only that one player received it, never the rest of
    * the outfit, which reads as a "your action is persisted" confirmation of
    * that client's own request rather than a general notification: an
    * asynchronous database-persistence echo of the committed row, with the
    * 15-20 second scatter matching what a background commit-then-refresh
    * chain added on the stock servers. The shipped client's outfit-event
    * handler drops both actions without effect. Carried over here so live
    * traffic matches the captured baseline as closely as possible; scheduled
    * at the observed floor of the window.
    */
  private val outfitPersistenceNotifyExecutor: java.util.concurrent.ScheduledExecutorService =
    Executors.newSingleThreadScheduledExecutor()

  /**
    * Schedules the "your action is persisted" echo for the player who
    * performed the change and no one else.
    */
  private def scheduleOutfitPersistenceNotify(
                                               outfit_id: Long,
                                               action: OutfitEventAction,
                                               zone: Zone,
                                               playerName: String
                                             ): Unit = {
    outfitPersistenceNotifyExecutor.schedule(
      new Runnable {
        override def run(): Unit =
          PlayerControl.sendResponse(zone, playerName, OutfitEvent(outfit_id, action))
      },
      15,
      java.util.concurrent.TimeUnit.SECONDS
    )
  }

  def HandleLoginOutfitCheck(player: Player, session: SessionData): Unit = {

    // TODO: implement this the proper way, please
    // start the shedule on first run of the function
    if (!outfitPointSheduleStarted) {
      outfitPointSheduleStarted = true

      Executors.newSingleThreadScheduledExecutor.scheduleAtFixedRate(
        () => {
          ctx.run(updateOutfitPointMV())
        },
        0,
        5,
        java.util.concurrent.TimeUnit.MINUTES
      )
    }

    ctx.run(getOutfitOnLogin(player.avatar.id)).flatMap { memberships =>
      memberships.headOption match {
        case Some(membership) =>
          val outfitId = membership.outfit_id
          (for {
            outfitOpt   <- ctx.run(getOutfitById(outfitId)).map(_.headOption)
            memberCount <- ctx.run(getOutfitMemberCount(outfitId))
            points      <- ctx.run(getOutfitPoints(outfitId)).map(_.headOption.map(_.points).getOrElse(0L))
          } yield (outfitOpt, memberCount, points))
            .map {
              case (Some(outfit), memberCount, points) =>
                val seconds: Long = outfit.created.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli / 1000
                player.Zone.AvatarEvents ! BundledEnvelope(
                  MessageEnvelope(player.Name, SendResponse(
                    OutfitEvent(outfitId, Update(OutfitInfo(
                      outfit.name, points, points, memberCount,
                      OutfitRankNames(
                        outfit.rank0.getOrElse(""), outfit.rank1.getOrElse(""), outfit.rank2.getOrElse(""),
                        outfit.rank3.getOrElse(""), outfit.rank4.getOrElse(""), outfit.rank5.getOrElse(""),
                        outfit.rank6.getOrElse(""), outfit.rank7.getOrElse("")
                      ),
                      outfit.motd.getOrElse(""),
                      outfit.decal, formation_completed = true, 0, seconds, 0, 0, 0
                    ))),
                    OutfitMemberUpdate(outfit.id, player.CharId, membership.rank, flag = true)
                  )),
                  MessageEnvelope(
                    player.Zone.id,
                    PlanetsideAttribute(player.GUID, 39, outfit.id)
                  ),
                  MessageEnvelope(
                    player.Zone.id,
                    player.GUID,
                    AvatarAction.PlanetsideStringAttribute(0, outfit.name)
                  )
                )
                session.chat.JoinChannel(OutfitChannel(outfit.id))
                player.outfit_id = outfit.id
                player.outfit_name = outfit.name

              case (None, _, _) =>
                PlayerControl.sendResponse(player.Zone, player.Name,
                  ChatMsg(ChatMessageType.UNK_227, "Failed to load outfit"))
            }
            .recover { case _ =>
              PlayerControl.sendResponse(player.Zone, player.Name,
                ChatMsg(ChatMessageType.UNK_227, "Failed to load outfit"))
            }
        case None =>
          Future.successful(())
      }
    }
  }

  /* supporting functions */

  def sanitizeOutfitName(name: String): Option[String] = {
    val cleaned = name
      .replaceAll("""[^A-Za-z0-9\-="\;\[\]\(\)\. ]""", "") // Remove disallowed chars
      .replaceAll(" +", " ") // Collapse multiple spaces to one
      .trim // Remove leading/trailing spaces
    if (cleaned.length >= 2 && cleaned.length <= 32) Some(cleaned) else None
  }

  def findPlayerByNameForOutfitAction(zones: Iterable[Zone], targetName: String, inviter: Player): Option[Player] = {
    zones
      .flatMap(_.AllPlayers)
      .find(p =>
        p.Name.equalsIgnoreCase(targetName) && p.Name != inviter.Name &&
          p.Faction == inviter.Faction && p.outfit_id == 0
      )
  }

  def findPlayerByIdForOutfitAction(zones: Iterable[Zone], targetId: Long, initiator: Player): Option[Player] = {
    zones
      .flatMap(_.AllPlayers)
      .find(p =>
        p.CharId == targetId && p.Name != initiator.Name &&
          p.Faction == initiator.Faction && p.outfit_id == initiator.outfit_id
      )
  }

  /* db actions */

  def findOutfitByName(name: String): Quoted[EntityQuery[Outfit]] = quote {
    query[Outfit].filter(outfit => lift(name).toLowerCase == outfit.name.toLowerCase)
  }

  def insertNewOutfit(name: String, faction: Int, owner_id: Long): Quoted[ActionReturning[Outfit, Outfit]] = quote {
    query[Outfit]
      .insert(_.name -> lift(name), _.faction -> lift(faction), _.owner_id -> lift(owner_id))
      .returning(outfit => outfit)
  }

  def insertOutfitMember(outfit_id: Long, avatar_id: Long, rank: Int): Quoted[Insert[Outfitmember]] = quote {
    query[Outfitmember].insert(
      _.outfit_id -> lift(outfit_id),
      _.avatar_id -> lift(avatar_id),
      _.rank      -> lift(rank)
    )
  }

  def insertOutfitPoint(outfit_id: Long, avatar_id: Long): Quoted[Insert[Outfitpoint]] = quote {
    query[Outfitpoint].insert(
      _.outfit_id -> lift(outfit_id),
      _.avatar_id -> lift(Some(avatar_id): Option[Long])
    )
  }

  def createNewOutfit(name: String, faction: Int, owner_id: Long): Future[Outfit] = {
    ctx.transaction { implicit ec =>
      for {
        outfit <- ctx.run(insertNewOutfit(name, faction, owner_id))
        _      <- ctx.run(insertOutfitMember(outfit.id, owner_id, rank=7))
        _      <- ctx.run(insertOutfitPoint(outfit.id, owner_id))
      } yield outfit
    }
  }

  def addMemberToOutfit(outfit_id: Long, avatar_id: Long): Future[Unit] = {
    ctx.transaction { implicit ec =>
      for {
        _ <- ctx.run(insertOutfitMember(outfit_id, avatar_id, rank=0))
        _ <- ctx.run(insertOutfitPoint(outfit_id, avatar_id))
      } yield ()
    }
  }

  def removeMemberFromOutfit(outfit_id: Long, avatar_id: Long): Future[(Long, Long)] = {
    val avatarOpt: Option[Long] = Some(avatar_id)
    ctx.transaction { _ =>
      for {
        deleted <- ctx.run(
          query[Outfitmember]
            .filter(_.outfit_id == lift(outfit_id))
            .filter(_.avatar_id == lift(avatar_id))
            .delete
        )
        updated <- ctx.run(
          query[Outfitpoint]
            .filter(_.outfit_id == lift(outfit_id))
            .filter(_.avatar_id == lift(avatarOpt))
            .update(_.avatar_id -> None)
        )
      } yield (deleted, updated)
    }
  }

  def getOutfitById(id: Long): Quoted[EntityQuery[Outfit]] = quote {
    query[Outfit].filter(_.id == lift(id))
  }

  def getOutfitMemberCount(id: Long): Quoted[Long] = quote {
    query[Outfitmember].filter(_.outfit_id == lift(id)).size
  }

  def getOutfitMemberPoints(outfit_id: Long, avatar_id: Long): Future[Long] = {
    val avatarOpt: Option[Long] = Some(avatar_id)
    for {
      points <- ctx.run(
        query[Outfitpoint]
          .filter(_.outfit_id == lift(outfit_id))
          .filter(_.avatar_id == lift(avatarOpt))
          .map(_.points)
      )
    } yield (points.headOption.getOrElse(0))
  }

  def getOutfitPoints(id: Long): Quoted[EntityQuery[OutfitpointMv]] = quote {
    querySchema[OutfitpointMv]("outfitpoint_mv").filter(_.outfit_id == lift(id))
  }

  def getOutfitOnLogin(avatarId: Long): Quoted[EntityQuery[Outfitmember]] = quote {
    query[Outfitmember].filter(_.avatar_id == lift(avatarId))
  }

  def getOutfitMembersWithDetails(outfitId: Long): Quoted[Query[(Long, String, Long, Int, LocalDateTime)]] = quote {
    query[Outfitmember]
      .filter(_.outfit_id == lift(outfitId))
      .join(query[Avatar]).on(_.avatar_id == _.id)
      .leftJoin(query[Outfitpoint]).on {
      case ((member, _), points) =>
        points.outfit_id == member.outfit_id && points.avatar_id.getOrElse(0L) == member.avatar_id
    }
      .map {
        case ((member, avatar), pointsOpt) =>
          (member.avatar_id, avatar.name, pointsOpt.map(_.points).getOrElse(0L), member.rank, avatar.last_login)
      }
  }

  def getOutfitsByEmpire(playerEmpireId: Int): Quoted[Query[(Long, Long, String, String, Long)]] = quote {
    query[Outfit]
      .filter(_.faction == lift(playerEmpireId))
      .join(query[Avatar]).on((outfit, avatar) => outfit.owner_id == avatar.id)
      .leftJoin(
        query[Outfitmember]
          .groupBy(_.outfit_id)
          .map { case (oid, members) => (oid, members.size) }
      ).on { case ((outfit, _), (oid, _)) => oid == outfit.id }
      .leftJoin(querySchema[OutfitpointMv]("outfitpoint_mv")).on {
      case (((outfit, _), _), points) => points.outfit_id == outfit.id
    }
      .map {
        case (((outfit, leader), memberCounts), points) =>
          (outfit.id, points.map(_.points).getOrElse(0L), outfit.name, leader.name, memberCounts.map(_._2).getOrElse(0L))
      }
  }

  def updateMemberRankById(outfit_id: Long, avatar_id: Long, rank: Int): Quoted[io.getquill.Update[Outfitmember]] = quote {
    query[Outfitmember]
      .filter(_.outfit_id == lift(outfit_id))
      .filter(_.avatar_id == lift(avatar_id))
      .update(_.rank -> lift(rank))
  }

  def updateOutfitMemberRank(outfit_id: Long, avatar_id: Long, rank: Int): Future[Unit] = {
    ctx.transaction { implicit ec =>
      for {
        _ <- ctx.run(updateMemberRankById(outfit_id, avatar_id, rank))
      } yield ()
    }
  }

  def updateOutfitOwnerById(outfit_id: Long, owner_id: Long): Quoted[io.getquill.Update[Outfit]] = quote {
    query[Outfit]
      .filter(_.id == lift(outfit_id))
      .update(_.owner_id -> lift(owner_id))
  }

  def updateOutfitOwner(outfit_id: Long, owner_id: Long, new_owner_id: Long): Future[Unit] = {
    ctx.transaction { implicit ec =>
      for {
        _ <- ctx.run(updateMemberRankById(outfit_id, owner_id, 6))
        _ <- ctx.run(updateMemberRankById(outfit_id, new_owner_id, 7))
        _ <- ctx.run(updateOutfitOwnerById(outfit_id, new_owner_id))
      } yield ()
    }
  }

  def updateOutfitMotdById(outfit_id: Long, motd: Option[String]): Quoted[io.getquill.Update[Outfit]] = quote {
    query[Outfit]
      .filter(_.id == lift(outfit_id))
      .update(_.motd -> lift(motd))
  }

  def updateOutfitMotd(outfit_id: Long, motd: Option[String]): Future[Unit] = {
    ctx.transaction { implicit ec =>
      for {
        _ <- ctx.run(updateOutfitMotdById(outfit_id, motd))
      } yield ()
    }
  }

  def updateOutfitRanksById(outfit_id: Long, list: List[Option[String]]): Quoted[io.getquill.Update[Outfit]] = {

    // Normalize: turn empty strings into None, and clamp to the outfit table's
    // VARCHAR(32) rank-title column - an over-long title would fail the
    // database update silently
    val normalized = list.map {
      case Some(value) if value.trim.nonEmpty => Some(value.take(32))
      case _ => None
    }

    // Colorize: prepend \ in front of # if it is the fist character
    val colorized = normalized.map {
      case Some(s) if s.startsWith("#") => Some("\\" + s)
      case other                        => other
    }

    query[Outfit]
      .filter(_.id == lift(outfit_id))
      .update(
        _.rank0 -> lift(colorized.head),
        _.rank1 -> lift(colorized(1)),
        _.rank2 -> lift(colorized(2)),
        _.rank3 -> lift(colorized(3)),
        _.rank4 -> lift(colorized(4)),
        _.rank5 -> lift(colorized(5)),
        _.rank6 -> lift(colorized(6)),
        _.rank7 -> lift(colorized(7))
      )
  }

  def updateOutfitRanks(outfit_id: Long, list: List[Option[String]]): Future[Unit] = {
    ctx.transaction { _ =>
      for {
        _ <- ctx.run(updateOutfitRanksById(outfit_id, list))
      } yield ()
    }
  }

  def updateOutfitPointMV(): Quoted[Action[Unit]] = quote(
    infix"REFRESH MATERIALIZED VIEW CONCURRENTLY outfitpoint_mv".as[Action[Unit]]
  )
}
