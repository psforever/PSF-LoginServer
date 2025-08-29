// Copyright (c) 2025 PSForever
package net.psforever.actors.session.support

import io.getquill.{ActionReturning, EntityQuery, Insert, PostgresJAsyncContext, Query, Quoted, SnakeCase}
import net.psforever.objects.avatar.PlayerControl
import net.psforever.objects.zones.Zone
import net.psforever.objects.Player
import net.psforever.packet.game.OutfitEventAction.{OutfitInfo, OutfitRankNames, Unk0, Unk1, Unk2}
import net.psforever.packet.game.OutfitMembershipResponse.PacketType.CreateResponse
import net.psforever.packet.game._
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.chat.OutfitChannel
import net.psforever.types.ChatMessageType
import net.psforever.util.Config

import java.time.LocalDateTime
import scala.util.{Failure, Success}

object SessionOutfitHandlers {

  case class Avatar(id: Long, name: String, faction_id: Int, last_login: java.time.LocalDateTime)
  case class Outfit(id: Long, name: String, faction: Int, owner_id: Long, motd: Option[String], created: java.time.LocalDateTime,
                    rank0: Option[String],
                    rank1: Option[String],
                    rank2: Option[String],
                    rank3: Option[String],
                    rank4: Option[String],
                    rank5: Option[String],
                    rank6: Option[String],
                    rank7: Option[String])
  case class Outfitmember(id: Long, outfit_id: Long, avatar_id: Long, rank: Int)
  case class Outfitpoint(id: Long, outfit_id: Long, avatar_id: Long, points: Long)
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
                outfit.created.atZone(java.time.ZoneOffset.UTC).toInstant.toEpochMilli / 1000

              PlayerControl.sendResponse(player.Zone, player.Name,
                OutfitEvent(outfit.id, Unk2(
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
                OutfitMembershipResponse.PacketType.Unk2, 0, 0,
                invited.CharId, outfitInvite.sentFrom.CharId, invited.Name, outfit.name, flag = false))

            PlayerControl.sendResponse(invited.Zone, invited.Name,
              OutfitMembershipResponse(
                OutfitMembershipResponse.PacketType.Unk2, 0, 0,
                invited.CharId, outfitInvite.sentFrom.CharId, invited.Name, outfit.name, flag = true))

            PlayerControl.sendResponse(outfitInvite.sentFrom.Zone, outfitInvite.sentFrom.Name,
              OutfitEvent(outfitId, OutfitEventAction.Unk5(memberCount)))

            PlayerControl.sendResponse(outfitInvite.sentFrom.Zone, outfitInvite.sentFrom.Name,
              OutfitMemberEvent(outfitId, invited.CharId,
                OutfitMemberEventAction.Unk0(invited.Name, 0, 0, 0,
                  OutfitMemberEventAction.PacketType.Padding, 0)))

            val seconds: Long = outfit.created.atZone(java.time.ZoneOffset.UTC).toInstant.toEpochMilli / 1000
            PlayerControl.sendResponse(invited.Zone, invited.Name,
              OutfitEvent(outfitId, Unk0(OutfitInfo(
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

  def HandleOutfitKick(zones: Seq[Zone], kickedId: Long, kickedBy: Player): Unit = {
    // if same id, player has left the outfit by their own choice
    if (kickedId == kickedBy.CharId) {
      // db stuff first
      PlayerControl.sendResponse(kickedBy.Zone, kickedBy.Name, OutfitMemberEvent(kickedBy.outfit_id, kickedId, OutfitMemberEventAction.Unk1()))
    }
    else {
      // db stuff first
      // tell player they've been kicked (if online)
      findPlayerByIdForOutfitAction(zones, kickedId, kickedBy).foreach { kicked =>
        PlayerControl.sendResponse(kicked.Zone, kicked.Name, OutfitMembershipResponse(OutfitMembershipResponse.PacketType.Kick, 0, 1, kickedBy.CharId, kicked.CharId, kicked.Name, kickedBy.Name, flag = false))
        kicked.Zone.AvatarEvents ! AvatarServiceMessage(kicked.Zone.id, AvatarAction.PlanetsideAttributeToAll(kicked.GUID, 39, 0))
        //kicked.Zone.AvatarEvents ! AvatarServiceMessage(kicked.Zone.id, AvatarAction.PlanetsideStringAttributeMessage(kicked.GUID, 0, ""))
        kicked.outfit_id = 0
        kicked.outfit_name = ""
        PlayerControl.sendResponse(kicked.Zone, kicked.Name, OutfitMemberEvent(kickedBy.outfit_id, kickedId, OutfitMemberEventAction.Unk1()))

        // move this out of foreach - db will provide kicked char details
        PlayerControl.sendResponse(kickedBy.Zone, kickedBy.Name, OutfitMembershipResponse(OutfitMembershipResponse.PacketType.Kick, 0, 1, kickedBy.CharId, kicked.CharId, kicked.Name, "", flag = true))
        PlayerControl.sendResponse(kickedBy.Zone, kickedBy.Name, OutfitMemberEvent(kickedBy.outfit_id, kickedId, OutfitMemberEventAction.Unk1()))
        // new number of outfit members?
        PlayerControl.sendResponse(kickedBy.Zone, kickedBy.Name, OutfitEvent(kickedBy.outfit_id, OutfitEventAction.Unk5(34)))
      }
    }
  }

  def HandleOutfitPromote(zones: Seq[Zone], promotedId: Long, newRank: Int, promoter: Player): Unit = {
    // send to all online players in outfit
    findPlayerByIdForOutfitAction(zones, promotedId, promoter).foreach { promoted =>
      PlayerControl.sendResponse(promoted.Zone, promoted.Name, OutfitMemberEvent(6418, promotedId, OutfitMemberEventAction.Unk0(promoted.Name, newRank, 1032432, 0, OutfitMemberEventAction.PacketType.Padding, 0)))
      PlayerControl.sendResponse(promoter.Zone, promoter.Name, OutfitMemberEvent(6418, promotedId, OutfitMemberEventAction.Unk0(promoted.Name, newRank, 1032432, 0, OutfitMemberEventAction.PacketType.Padding, 0)))
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
        val seconds: Long = outfit.created.atZone(java.time.ZoneOffset.UTC).toInstant.toEpochMilli / 1000

        PlayerControl.sendResponse(player.Zone, player.Name,
          OutfitEvent(outfit.id, Unk0(OutfitInfo(
            outfit.name,
            totalPoints,
            totalPoints,
            memberCount,
            OutfitRankNames("", "", "", "", "", "", "", ""),
            outfit.motd.getOrElse(""),
            14, unk11 = true, 0, seconds, 0, 0, 0))))

        members.foreach { case (avatarId, avatarName, points, rank, login) =>
          val lastLogin = findPlayerByIdForOutfitAction(zones, avatarId, player) match {
            case Some(_) => 0L
            case None if player.Name == avatarName => 0L
            case None =>  (System.currentTimeMillis() - login.atZone(java.time.ZoneOffset.UTC).toInstant.toEpochMilli) / 1000
          }
          PlayerControl.sendResponse(player.Zone, player.Name,
            OutfitMemberEvent(outfit.id, avatarId,
              OutfitMemberEventAction.Unk0(
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
      .flatMap(_.LivePlayers)
      .find(p =>
        p.Name.equalsIgnoreCase(targetName) && p.Name != inviter.Name &&
          p.Faction == inviter.Faction && p.outfit_id == 0
      )
  }

  def findPlayerByIdForOutfitAction(zones: Iterable[Zone], targetId: Long, initiator: Player): Option[Player] = {
    zones
      .flatMap(_.LivePlayers)
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
      _.avatar_id -> lift(avatar_id)
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

  def getOutfitById(id: Long): Quoted[EntityQuery[Outfit]] = quote {
    query[Outfit].filter(_.id == lift(id))
  }

  def getOutfitMemberCount(id: Long): Quoted[Long] = quote {
    query[Outfitmember].filter(_.outfit_id == lift(id)).size
  }

  def getOutfitPoints(id: Long): Quoted[EntityQuery[OutfitpointMv]] = quote {
    querySchema[OutfitpointMv]("outfitpoint_mv").filter(_.outfit_id == lift(id))
  }

  def getOutfitMembersWithDetails(outfitId: Long): Quoted[Query[(Long, String, Long, Int, LocalDateTime)]] = quote {
    query[Outfitmember]
      .filter(_.outfit_id == lift(outfitId))
      .join(query[Avatar]).on(_.avatar_id == _.id)
      .leftJoin(query[Outfitpoint]).on {
      case ((member, _), points) =>
        points.outfit_id == member.outfit_id && points.avatar_id == member.avatar_id
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
}
