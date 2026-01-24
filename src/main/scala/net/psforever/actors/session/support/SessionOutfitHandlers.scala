// Copyright (c) 2025 PSForever
package net.psforever.actors.session.support

import io.getquill.{Action, ActionReturning, EntityQuery, Insert, PostgresJAsyncContext, Query, Quoted, SnakeCase}
import net.psforever.objects.avatar.PlayerControl
import net.psforever.objects.zones.Zone
import net.psforever.objects.Player
import net.psforever.packet.game.OutfitEventAction.{Initial, Leaving, OutfitInfo, OutfitRankNames, Unk1, Update, UpdateMemberCount}
import net.psforever.packet.game.OutfitMembershipResponse.PacketType.CreateResponse
import net.psforever.packet.game._
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
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
    val cleanedName = sanitizeOutfitName(outfitName)

    cleanedName match {
      case Some(validName) =>
        ctx.run(findOutfitByName(validName)).flatMap {
          case existing if existing.nonEmpty =>
            PlayerControl.sendResponse(player.Zone, player.Name,
              ChatMsg(ChatMessageType.UNK_227, "@OutfitErrorNameAlreadyTaken"))
            Future.successful(())

          case _ =>
            createNewOutfit(validName, player.Faction.id, player.CharId).map { outfit =>
              val seconds: Long =
                outfit.created.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli / 1000

              PlayerControl.sendResponse(player.Zone, player.Name,
                OutfitEvent(outfit.id, Update(
                  OutfitInfo(
                    outfit.name, 0, 0, 1,
                    OutfitRankNames("", "", "", "", "", "", "", ""),
                    "",
                    14, unk11 = true, 0, seconds, 0, 0, 0))))

              PlayerControl.sendResponse(player.Zone, player.Name,
                OutfitMemberUpdate(outfit.id, player.CharId, 7, flag = true))

              PlayerControl.sendResponse(player.Zone, player.Name,
                ChatMsg(ChatMessageType.UNK_227, "@OutfitCreateSuccess"))

              PlayerControl.sendResponse(player.Zone, player.Name,
                OutfitMembershipResponse(CreateResponse, 0, 0, player.CharId, 0, "", "", flag = true))

              player.outfit_id = outfit.id
              player.outfit_name = outfit.name

              player.Zone.AvatarEvents ! AvatarServiceMessage(
                player.Zone.id,
                player.GUID,
                AvatarAction.PlanetsideAttributeToAll(39, outfit.id)
              )

              player.Zone.AvatarEvents ! AvatarServiceMessage(
                player.Zone.id,
                player.GUID,
                AvatarAction.PlanetsideStringAttribute(0, outfit.name)
              )

              session.chat.JoinChannel(OutfitChannel(player.outfit_id))
            }
              .recover { case e =>
                e.printStackTrace()
                PlayerControl.sendResponse(player.Zone, player.Name,
                  ChatMsg(ChatMessageType.UNK_227, "@OutfitCreateFailure"))
              }
        }
      case None =>
        PlayerControl.sendResponse(player.Zone, player.Name,
          ChatMsg(ChatMessageType.UNK_227, "@OutfitCreateFailure"))
    }
  }

  def HandleOutfitInvite(zones: Seq[Zone], invitedName: String, sentFrom: Player): Unit = {
    findPlayerByNameForOutfitAction(zones, invitedName, sentFrom).foreach { invitedPlayer =>

      PlayerControl.sendResponse(invitedPlayer.Zone, invitedPlayer.Name,
        OutfitMembershipResponse(OutfitMembershipResponse.PacketType.Invite, 0, 0,
          sentFrom.CharId, sentFrom.CharId, sentFrom.Name, sentFrom.outfit_name, flag = false))

      PlayerControl.sendResponse(sentFrom.Zone, sentFrom.Name,
        OutfitMembershipResponse(OutfitMembershipResponse.PacketType.Invite, 0, 0,
          sentFrom.CharId, invitedPlayer.CharId, invitedPlayer.Name, sentFrom.outfit_name, flag = true))

      val outfitInvite = OutfitInvite(invitedPlayer, sentFrom)
      OutfitInviteManager.addOutfitInvite(outfitInvite)
    }
  }

  def HandleOutfitInviteAccept(invited: Player, session: SessionData): Unit = {
    OutfitInviteManager.getOutfitInvite(invited.CharId) match {
      case Some(outfitInvite) =>
        val outfitId = outfitInvite.sentFrom.outfit_id

        (for {
          _           <- addMemberToOutfit(outfitId, invited.CharId)
          outfitOpt   <- ctx.run(getOutfitById(outfitId)).map(_.headOption)
          memberCount <- ctx.run(getOutfitMemberCount(outfitId))
          points      <- ctx.run(getOutfitPoints(outfitId)).map(_.headOption.map(_.points).getOrElse(0L))
        } yield (outfitOpt, memberCount, points))
          .map {
          case (Some(outfit), memberCount, points) =>

            PlayerControl.sendResponse(outfitInvite.sentFrom.Zone, outfitInvite.sentFrom.Name,
              OutfitMembershipResponse(
                OutfitMembershipResponse.PacketType.InviteAccepted, 0, 0,
                invited.CharId, outfitInvite.sentFrom.CharId, invited.Name, outfit.name, flag = false))

            PlayerControl.sendResponse(invited.Zone, invited.Name,
              OutfitMembershipResponse(
                OutfitMembershipResponse.PacketType.InviteAccepted, 0, 0,
                invited.CharId, outfitInvite.sentFrom.CharId, invited.Name, outfit.name, flag = true))

            PlayerControl.sendResponse(outfitInvite.sentFrom.Zone, outfitInvite.sentFrom.Name,
              OutfitEvent(outfitId, UpdateMemberCount(memberCount)))

            PlayerControl.sendResponse(outfitInvite.sentFrom.Zone, outfitInvite.sentFrom.Name,
              OutfitMemberEvent(outfitId, invited.CharId,
                OutfitMemberEventAction.Update(invited.Name, 0, 0, 0,
                  OutfitMemberEventAction.PacketType.Padding, 0)))

            val seconds: Long = outfit.created.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli / 1000
            PlayerControl.sendResponse(invited.Zone, invited.Name,
              OutfitEvent(outfitId, Initial(OutfitInfo(
                outfit.name, points, points, memberCount,
                OutfitRankNames("", "", "", "", "", "", "", ""),
                outfit.motd.getOrElse(""),
                14, unk11 = true, 0, seconds, 0, 0, 0))))

            PlayerControl.sendResponse(invited.Zone, invited.Name,
              OutfitMemberUpdate(outfit.id, invited.CharId, 0, flag=true))

            OutfitInviteManager.removeOutfitInvite(invited.CharId)

            session.chat.JoinChannel(OutfitChannel(outfit.id))
            invited.outfit_id = outfit.id
            invited.outfit_name = outfit.name

            invited.Zone.AvatarEvents ! AvatarServiceMessage(
              invited.Zone.id,
              invited.GUID,
              AvatarAction.PlanetsideAttributeToAll(39, outfit.id)
            )

            invited.Zone.AvatarEvents ! AvatarServiceMessage(
              invited.Zone.id,
              invited.GUID,
              AvatarAction.PlanetsideStringAttribute(0, outfit.name)
            )
          case (None, _, _) =>

            PlayerControl.sendResponse(invited.Zone, invited.Name,
              ChatMsg(ChatMessageType.UNK_227, "Failed to join outfit"))
        }
          .recover { case _ =>
            PlayerControl.sendResponse(invited.Zone, invited.Name,
              ChatMsg(ChatMessageType.UNK_227, "Failed to join outfit"))
          }
      case None =>
    }
  }

  def HandleOutfitInviteReject(invited: Player): Unit = {
    OutfitInviteManager.getOutfitInvite(invited.CharId) match {
      case Some(outfitInvite) =>

        PlayerControl.sendResponse(outfitInvite.sentFrom.Zone, outfitInvite.sentFrom.Name,
          OutfitMembershipResponse(
            OutfitMembershipResponse.PacketType.InviteRejected, 0, 0,
            invited.CharId, outfitInvite.sentFrom.CharId, invited.Name, "", flag = false))

        PlayerControl.sendResponse(invited.Zone, invited.Name,
          OutfitMembershipResponse(
            OutfitMembershipResponse.PacketType.InviteRejected, 0, 0,
            invited.CharId, outfitInvite.sentFrom.CharId, invited.Name, "", flag = true))

        OutfitInviteManager.removeOutfitInvite(invited.CharId)
      case None =>
    }
  }

  def HandleOutfitKick(zones: Seq[Zone], kickedId: Long, kickedBy: Player, session: SessionData): Unit = {
    // if same id, player has left the outfit by their own choice
    if (kickedId == kickedBy.CharId) {

      // store outfit_id since it will be nulled soon
      val outfit_id = kickedBy.outfit_id

      removeMemberFromOutfit(outfit_id, kickedId).map {
        case (deleted, _) =>
          if (deleted > 0) {

            PlayerControl.sendResponse(kickedBy.Zone, kickedBy.Name,
              OutfitEvent(outfit_id, Leaving())
            )

            session.chat.LeaveChannel(OutfitChannel(outfit_id))
            kickedBy.outfit_name = ""
            kickedBy.outfit_id = 0

            zones.filter(z => z.AllPlayers.nonEmpty).flatMap(_.AllPlayers)
              .filter(p => p.outfit_id == outfit_id).foreach(outfitMember =>
              PlayerControl.sendResponse(outfitMember.Zone, outfitMember.Name,
                OutfitMemberEvent(outfit_id, kickedId, OutfitMemberEventAction.Kicked()))
            )

            kickedBy.Zone.AvatarEvents ! AvatarServiceMessage(
              kickedBy.Zone.id,
              kickedBy.GUID,
              AvatarAction.PlanetsideAttributeToAll(39, 0)
            )

            kickedBy.Zone.AvatarEvents ! AvatarServiceMessage(
              kickedBy.Zone.id,
              kickedBy.GUID,
              AvatarAction.PlanetsideStringAttribute(0, "")
            )
          }
      }.recover { case e =>
        e.printStackTrace()
      }
    }
    else {
      removeMemberFromOutfit(kickedBy.outfit_id, kickedId).map {
        case (deleted, _) =>
          if (deleted > 0) {
            findPlayerByIdForOutfitAction(zones, kickedId, kickedBy).foreach { kicked =>

              PlayerControl.sendResponse(kicked.Zone, kicked.Name,
                OutfitEvent(kickedBy.outfit_id, Leaving())
              )

              PlayerControl.sendResponse(kicked.Zone, kicked.Name,
                OutfitMembershipResponse(OutfitMembershipResponse.PacketType.YouGotKicked, 0, 1,
                  kickedBy.CharId, kicked.CharId, kickedBy.Name, kicked.Name, flag = false))

              kicked.Zone.AvatarEvents ! AvatarServiceMessage(
                kicked.Zone.id,
                kicked.GUID,
                AvatarAction.PlanetsideAttributeToAll(39, 0)
              )

              kicked.Zone.AvatarEvents ! AvatarServiceMessage(
                kicked.Zone.id,
                kicked.GUID,
                AvatarAction.PlanetsideStringAttribute(0, "")
              )

              kicked.Zone.AvatarEvents ! AvatarServiceMessage(
                kicked.Name, AvatarAction.RemoveFromOutfitChat(kickedBy.outfit_id))

              kicked.outfit_id = 0
              kicked.outfit_name = ""
              PlayerControl.sendResponse(kicked.Zone, kicked.Name,
                OutfitMemberEvent(kickedBy.outfit_id, kickedId, OutfitMemberEventAction.Kicked()))
            }
            val avatarName: Future[Option[String]] =
            ctx.run(
              quote { query[Avatar].filter(_.id == lift(kickedId)).map(_.name) }
            ).map(_.headOption)

            avatarName.foreach {
              case Some(name) => PlayerControl.sendResponse(kickedBy.Zone, kickedBy.Name,
                OutfitMembershipResponse(OutfitMembershipResponse.PacketType.YouKicked, 0, 1, kickedBy.CharId, kickedId, name, "", flag = true))

              case None => PlayerControl.sendResponse(kickedBy.Zone, kickedBy.Name,
                OutfitMembershipResponse(OutfitMembershipResponse.PacketType.YouKicked, 0, 1, kickedBy.CharId, kickedId, "NameNotFound", "", flag = true))
            }
            zones.filter(z => z.AllPlayers.nonEmpty).flatMap(_.AllPlayers)
              .filter(p => p.outfit_id == kickedBy.outfit_id).foreach(outfitMember =>
              PlayerControl.sendResponse(outfitMember.Zone, outfitMember.Name,
                OutfitMemberEvent(kickedBy.outfit_id, kickedId, OutfitMemberEventAction.Kicked()))
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
                      OutfitMemberEventAction.Update(promoter.Name, 6, owner_points, 0, OutfitMemberEventAction.PacketType.Padding, 0)))
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
          // tell everyone about the new rank of the promoted member
          zones.foreach(zone => {
            zone.AllPlayers
              .filter(_.outfit_id == outfit_id)
              .foreach(player => {
                PlayerControl.sendResponse(
                  zone, player.Name,
                  OutfitMemberEvent(outfit_id, promoted.avatar.id,
                    OutfitMemberEventAction.Update(promoted.Name, newRank, member_points, 0, OutfitMemberEventAction.PacketType.Padding, 0)))
              })
          })
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

        PlayerControl.sendResponse(player.Zone, player.Name,
          OutfitEvent(outfit.id, Initial(OutfitInfo(
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
            14, unk11 = true, 0, seconds, 0, 0, 0))))

        members.foreach { case (avatarId, avatarName, points, rank, login) =>
          val lastLogin = findPlayerByIdForOutfitAction(zones, avatarId, player) match {
            case Some(_) => 0L
            case None if player.Name == avatarName => 0L
            case None =>  (System.currentTimeMillis() - login.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli) / 1000
          }
          PlayerControl.sendResponse(player.Zone, player.Name,
            OutfitMemberEvent(outfit.id, avatarId,
              OutfitMemberEventAction.Update(
                avatarName,
                rank,
                points,
                lastLogin,
                OutfitMemberEventAction.PacketType.Padding, 0)))
        }
        PlayerControl.sendResponse(player.Zone, player.Name,
          OutfitEvent(outfit.id, Unk1()))
      }
    }
  }

  def HandleGetOutfitList(player: Player): Unit = {
    val q = getOutfitsByEmpire(player.Faction.id)
    val futureResult = ctx.run(q)

    futureResult.onComplete {
      case Success(rows) =>
        rows.foreach { case (outfitId, points, name, leaderName, memberCount) =>
          PlayerControl.sendResponse(player.Zone, player.Name,
            OutfitListEvent(
              OutfitListEventAction.ListElementOutfit(
                outfitId,
                points,
                memberCount,
                name,
                leaderName)))
        }

      case Failure(_) =>
        PlayerControl.sendResponse(player.Zone, player.Name,
          ChatMsg(ChatMessageType.UNK_227, "Outfit list failed to return")
        )
    }
  }

  def HandleOutfitMotd(zones: Seq[Zone], message: String, player: Player): Unit = {

    val outfit_id = player.outfit_id

    val outfitDetails = for {
      _           <- updateOutfitMotd(outfit_id, message)
      outfitOpt <- ctx.run(getOutfitById(outfit_id)).map(_.headOption)
      memberCount <- ctx.run(query[Outfitmember].filter(_.outfit_id == lift(outfit_id)).size)
      pointsTotal <- ctx.run(querySchema[OutfitpointMv]("outfitpoint_mv").filter(_.outfit_id == lift(outfit_id)))
    } yield (outfitOpt, memberCount, pointsTotal.headOption.map(_.points).getOrElse(0L))

    for {
      (outfitOpt, memberCount, totalPoints) <- outfitDetails
    } yield {
      outfitOpt.foreach { outfit =>

        // send to all online players in outfit
        val outfit_event = OutfitEvent(
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
              unk10 = 0,
              unk11 = true,
              unk12 = 0,
              created_timestamp = outfit.created.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli / 1000,
              unk23 = 0,
              unk24 = 0,
              unk25 = 0
            )
          )
        )

        zones.foreach(zone => {
          zone.AllPlayers
            .filter(_.outfit_id == outfit_id)
            .filter(_.outfit_window_open)
            .foreach(player => {
            PlayerControl.sendResponse(
              zone, player.Name,
              outfit_event
            )
          })
        })
      }
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

    for {
      (outfitOpt, memberCount, totalPoints) <- outfitDetails
    } yield {
      outfitOpt.foreach { outfit =>

        // send to all online players in outfit with window open
        val outfit_event = OutfitEvent(
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
              unk10 = 0,
              unk11 = true,
              unk12 = 0,
              created_timestamp = outfit.created.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli / 1000,
              unk23 = 0,
              unk24 = 0,
              unk25 = 0
            )
          )
        )

        zones.foreach(zone => {
          zone.AllPlayers
            .filter(_.outfit_id == outfit_id)
            .filter(_.outfit_window_open)
            .foreach(player => {
            PlayerControl.sendResponse(
              zone, player.Name,
              outfit_event
            )
          })
        })
      }
    }
  }

  private var outfitPointSheduleStarted = false

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

                PlayerControl.sendResponse(player.Zone, player.Name,
                  OutfitEvent(outfitId, Update(OutfitInfo(
                    outfit.name, points, points, memberCount,
                    OutfitRankNames(outfit.rank0.getOrElse(""), outfit.rank1.getOrElse(""), outfit.rank2.getOrElse(""),
                      outfit.rank3.getOrElse(""), outfit.rank4.getOrElse(""), outfit.rank5.getOrElse(""),
                      outfit.rank6.getOrElse(""), outfit.rank7.getOrElse("")),
                    outfit.motd.getOrElse(""),
                    14, unk11 = true, 0, seconds, 0, 0, 0))))

                PlayerControl.sendResponse(player.Zone, player.Name,
                  OutfitMemberUpdate(outfit.id, player.CharId, membership.rank, flag = true))

                session.chat.JoinChannel(OutfitChannel(outfit.id))
                player.outfit_id = outfit.id
                player.outfit_name = outfit.name

                player.Zone.AvatarEvents ! AvatarServiceMessage(
                  player.Zone.id,
                  player.GUID,
                  AvatarAction.PlanetsideAttributeToAll(39, outfit.id)
                )

                player.Zone.AvatarEvents ! AvatarServiceMessage(
                  player.Zone.id,
                  player.GUID,
                  AvatarAction.PlanetsideStringAttribute(0, outfit.name)
                )

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

  def updateOutfitMotd(outfit_id: Long, motd: String): Future[Unit] = {
    ctx.transaction { implicit ec =>
      for {
        _ <- ctx.run(updateOutfitMotdById(outfit_id, Some(motd)))
      } yield ()
    }
  }

  def updateOutfitRanksById(outfit_id: Long, list: List[Option[String]]): Quoted[io.getquill.Update[Outfit]] = {

    // Normalize: turn empty strings into None
    val normalized = list.map {
      case Some(value) if value.trim.nonEmpty => Some(value)
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
        _.rank0 -> lift(colorized(0)),
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
