package net.psforever.actors.api

import com.github.t3hnar.bcrypt._
import io.getquill.{Action, Query}
import net.psforever.persistence
import net.psforever.util.Database._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Everything the web portal used to read out of PostgreSQL itself.
  *
  * The portal used to hold its own connection pool. That made it a second database authority, and the
  * two disagreed in ways that mattered: it read `building.faction_id` to colour a continent map while
  * this process held the faction the world was actually enforcing, and it read `account.passhash` to
  * log people in. Everything it needs is served from here now, so the login server owns the database
  * outright and the portal owns none of it.
  *
  * These are deliberately RAW SQL rather than Quill quotations. The statements are analytic -- window
  * functions, `GROUP BY` over computed columns, chained outer joins -- and they arrived here already
  * tuned on the portal side. Restating them as quotations would be a rewrite of working SQL for no
  * gain, and would risk changing results silently. Quill still decodes the rows into the case classes
  * below, so the shape of every result is checked even though the statements are not.
  *
  * Return types are INFERRED on purpose. Quill's `run` is overloaded between a query
  * (`Quoted[Query[T]] -> Future[List[T]]`) and a single value (`Quoted[T] -> Future[T]`), and an
  * expected type of `Future[List[T]]` matches BOTH -- annotating one picks the wrong overload and
  * fails to compile. Each raw query returns `Future[List[<its row type>]]`.
  *
  * The row classes carry snake_case field names, which is unusual for Scala and deliberate: they are
  * wire DTOs, and one name has to satisfy three contracts at once. Quill decodes a row by matching the
  * field to the column alias, json4s serialises the field name verbatim, and the portal's React
  * components already read `killer_id`, `faction_id` and so on. Every column must therefore be
  * aliased to its field name, in declaration order -- a mismatch fails at runtime, not compile time,
  * which is the price of keeping the SQL verbatim.
  *
  * Timestamps are cast to `text` in SQL rather than decoded as dates and re-encoded. The portal has
  * always passed these straight through to the browser as strings, and going via a temporal type here
  * would only introduce a timezone to get wrong.
  *
  * Redaction happens HERE rather than in the portal. `passhash` and `password` are never selected at
  * all, and a login's IP address is cut to its last two octets before it leaves this process, so the
  * portal is never handed data it has no business holding.
  */
object PortalQueries {
  import ctx._

  /** Cost factor for new password hashes. Matches `LoginActor`, so both paths spend the same work. */
  private val BcryptRounds: Int = 12

  /**
    * The tail of an IP address -- the last two octets, as the portal has always displayed them.
    *
    * Done here so a full address never crosses the wire. An address that is not dotted-quad (IPv6, or
    * a stored hostname) is dropped entirely rather than half-shown: "the last two pieces" has no
    * meaning there, and guessing at it risks revealing more than intended, not less.
    */
  private def maskIp(ip: String): Option[String] = {
    val parts = Option(ip).getOrElse("").split('.')
    if (parts.length == 4) Some(parts.takeRight(2).mkString(".")) else None
  }

  // --- row types ---------------------------------------------------------------------------------

  case class Count(count: Long)

  case class Account(
      id: Int,
      username: String,
      created: String,
      last_modified: String,
      inactive: Boolean,
      gm: Boolean
  )

  case class AccountListing(
      id: Int,
      username: String,
      created: String,
      last_modified: String,
      inactive: Boolean,
      gm: Boolean,
      last_login: Option[String],
      ip_address: Option[String]
  )

  case class Character(
      id: Int,
      account_id: Int,
      name: String,
      faction_id: Int,
      created: String,
      last_login: String,
      avatar_id: Option[Int],
      can_gm: Option[Boolean],
      can_spectate: Option[Boolean]
  )

  case class AccountCharacter(
      id: Int,
      account_id: Int,
      name: String,
      faction_id: Int,
      gender_id: Int,
      head_id: Int,
      created: String,
      last_login: String,
      bep: Long,
      cep: Long,
      deleted: Boolean,
      avatar_id: Option[Int],
      can_gm: Option[Boolean],
      can_spectate: Option[Boolean]
  )

  case class Role(
      avatar_id: Int,
      can_spectate: Boolean,
      can_gm: Boolean,
      id: Int,
      last_login: String,
      account_id: Int,
      name: String
  )

  case class NamedCharacter(
      id: Int,
      account_id: Int,
      name: String,
      faction_id: Int,
      created: String,
      last_login: String
  )

  case class StatCharacter(id: Int, name: String, faction_id: Int, bep: Long, cep: Long)

  case class LastCharacter(id: Int, account_id: Int, name: String, faction_id: Int, created: String)

  case class AvatarDetail(
      id: Int,
      name: String,
      faction_id: Int,
      bep: Long,
      cep: Long,
      gender_id: Int,
      head_id: Int,
      created: String,
      last_login: String,
      outfit_id: Option[Int],
      outfit_name: Option[String]
  )

  case class AvatarOwner(id: Int, account_id: Int, name: String, deleted: Boolean)

  case class WeaponStat(
      avatar_id: Int,
      weapon_id: Int,
      shots_fired: Int,
      shots_landed: Int,
      kills: Int,
      assists: Int
  )

  case class TopKill(
      count: Long,
      killer_id: Int,
      name: String,
      bep: Long,
      cep: Long,
      faction_id: Int,
      gender_id: Int,
      head_id: Int
  )

  case class TopKillByDate(
      kill_count: Int,
      killer_id: Int,
      f_kill_date: String,
      row_num: Int,
      name: String,
      faction_id: Int
  )

  case class KdByDate(date: String, kills: Int, deaths: Int)

  case class TopOutfit(
      outfit_id: Int,
      faction: Int,
      outfit_name: String,
      leader_name: String,
      leader_id: Int,
      members: Int,
      points: Option[Int]
  )

  case class OutfitDetail(
      outfit_id: Int,
      faction: Int,
      outfit_name: String,
      leader_id: Int,
      leader_name: String,
      members: Int,
      points: Option[Int],
      created: String
  )

  case class OutfitMember(
      avatar_id: Int,
      bep: Long,
      cep: Long,
      avatar_name: String,
      rank_num: Int,
      rank_title: Option[String],
      points: Int,
      joined: String
  )

  case class LockerRow(items: Option[String])

  case class LoadoutRow(loadout_number: Int, exosuit_id: Int, name: String, items: String)

  case class VehicleLoadoutRow(loadout_number: Int, name: String, vehicle: Int, items: String)

  case class LoginRow(
      id: Int,
      account_id: Int,
      login_time: String,
      port: Int,
      ip_address: Option[String]
  )

  case class SearchAccount(id: Int, username: String, gm: Boolean, inactive: Boolean)

  case class SearchCharacter(
      id: Int,
      name: String,
      account_id: Int,
      faction_id: Int,
      avatar_id: Option[Int],
      can_spectate: Option[Boolean],
      can_gm: Option[Boolean]
  )

  case class Credentials(id: Int, passhash: String, inactive: Boolean)

  case class SessionRow(sess: String)

  // --- single-row and simple reads ---------------------------------------------------------------

  /**
    * One account, WITHOUT either password column.
    *
    * The portal's `SELECT *` used to drag `passhash`, `password` and `token` across the wire on every
    * authenticated request, and then delete them before responding. Selecting only what is displayed
    * means they never leave the database in the first place.
    */
  def account(id: Int) = {
    val q = quote(
      infix"""SELECT id AS id, username AS username, created::text AS created,
                     last_modified::text AS last_modified, inactive AS inactive, gm AS gm
              FROM account WHERE id = ${lift(id)}""".as[Query[Account]]
    )
    ctx.run(q)
  }

  /** Who owns a character, and whether it still exists. Backs the portal's "is this yours?" check. */
  def avatarOwner(avatarId: Int) = {
    val q = quote(
      infix"""SELECT id AS id, account_id AS account_id, name AS name, deleted AS deleted
              FROM avatar WHERE id = ${lift(avatarId)}""".as[Query[AvatarOwner]]
    )
    ctx.run(q)
  }

  /** A character by name, for the public name lookup. */
  def characterByName(name: String) = {
    val q = quote(
      infix"""SELECT id AS id, account_id AS account_id, name AS name, faction_id AS faction_id,
                     created::text AS created, last_login::text AS last_login
              FROM avatar WHERE name = ${lift(name)} AND deleted = false""".as[Query[NamedCharacter]]
    )
    ctx.run(q)
  }

  /** Every living character on an account, with its GM/spectate grants. */
  def charactersByAccount(accountId: Int) = {
    val q = quote(
      infix"""SELECT a.id AS id, a.account_id AS account_id, a.name AS name, a.faction_id AS faction_id,
                     a.gender_id AS gender_id, a.head_id AS head_id, a.created::text AS created,
                     a.last_login::text AS last_login, a.bep AS bep, a.cep AS cep, a.deleted AS deleted,
                     b.avatar_id AS avatar_id, b.can_gm AS can_gm, b.can_spectate AS can_spectate
              FROM avatar a
              LEFT JOIN avatarmodepermission b ON a.id = b.avatar_id
              WHERE a.account_id = ${lift(accountId)} AND a.deleted = false""".as[Query[AccountCharacter]]
    )
    ctx.run(q)
  }

  /** A character sheet, with the outfit it belongs to if any. */
  def avatar(id: Int) = {
    val q = quote(
      infix"""SELECT a.id AS id, a.name AS name, a.faction_id AS faction_id, a.bep AS bep, a.cep AS cep,
                     a.gender_id AS gender_id, a.head_id AS head_id, a.created::text AS created,
                     a.last_login::text AS last_login, o.id AS outfit_id, o.name AS outfit_name
              FROM avatar a
              LEFT JOIN outfitmember om ON om.avatar_id = a.id
              LEFT JOIN outfit o ON o.id = om.outfit_id
              WHERE a.id = ${lift(id)}""".as[Query[AvatarDetail]]
    )
    ctx.run(q)
  }

  /** Per-weapon totals for one character, deadliest first. */
  def weaponStats(avatarId: Int) = {
    val q = quote(
      infix"""SELECT avatar_id AS avatar_id, weapon_id AS weapon_id, shots_fired AS shots_fired,
                     shots_landed AS shots_landed, kills AS kills, assists AS assists
              FROM weaponstat WHERE avatar_id = ${lift(avatarId)}
              ORDER BY kills DESC""".as[Query[WeaponStat]]
    )
    ctx.run(q)
  }

  /** A character's kills and deaths per calendar day, most recent day first. */
  def avatarKdByDate(id: Int) = {
    val q = quote(
      infix"""SELECT TO_CHAR(timestamp, 'FMMon DD, YYYY') AS date,
                     SUM(CASE WHEN killer_id = ${lift(id)} THEN 1 ELSE 0 END)::int AS kills,
                     SUM(CASE WHEN victim_id = ${lift(id)} THEN 1 ELSE 0 END)::int AS deaths
              FROM killactivity WHERE exp > 0
              GROUP BY date
              HAVING SUM(CASE WHEN killer_id = ${lift(id)} THEN 1 ELSE 0 END) > 0
                  OR SUM(CASE WHEN victim_id = ${lift(id)} THEN 1 ELSE 0 END) > 0
              ORDER BY MIN(timestamp) DESC""".as[Query[KdByDate]]
    )
    ctx.run(q)
  }

  /** A character's locker contents, as the stored inventory blob. */
  def lockerItems(avatarId: Int) = {
    val q = quote(
      infix"""SELECT items AS items FROM locker
              WHERE avatar_id = ${lift(avatarId)}""".as[Query[LockerRow]]
    )
    ctx.run(q)
  }

  /** A character's saved infantry loadouts. */
  def loadouts(avatarId: Int) = {
    val q = quote(
      infix"""SELECT loadout_number AS loadout_number, exosuit_id AS exosuit_id, name AS name,
                     items AS items
              FROM loadout WHERE avatar_id = ${lift(avatarId)}
              ORDER BY loadout_number""".as[Query[LoadoutRow]]
    )
    ctx.run(q)
  }

  /** A character's saved vehicle loadouts. */
  def vehicleLoadouts(avatarId: Int) = {
    val q = quote(
      infix"""SELECT loadout_number AS loadout_number, name AS name, vehicle AS vehicle,
                     items AS items
              FROM vehicleloadout WHERE avatar_id = ${lift(avatarId)}
              ORDER BY loadout_number""".as[Query[VehicleLoadoutRow]]
    )
    ctx.run(q)
  }

  // --- leaderboards ------------------------------------------------------------------------------

  /** Leaderboard: the 500 deadliest characters by scored kills. */
  def topKills() = {
    val q = quote(
      infix"""SELECT COUNT(killactivity.killer_id) AS count,
                     killactivity.killer_id AS killer_id,
                     avatar.name AS name,
                     avatar.bep AS bep,
                     avatar.cep AS cep,
                     avatar.faction_id AS faction_id,
                     avatar.gender_id AS gender_id,
                     avatar.head_id AS head_id
              FROM killactivity
              INNER JOIN avatar ON killactivity.killer_id = avatar.id
              WHERE exp > 0
              GROUP BY killactivity.killer_id, avatar.name, avatar.bep, avatar.cep,
                       avatar.faction_id, avatar.gender_id, avatar.head_id
              ORDER BY COUNT(killer_id) DESC
              LIMIT 500""".as[Query[TopKill]]
    )
    ctx.run(q)
  }

  /** Leaderboard: each character's single best day, and the best 50 of those days. */
  def topKillsByDate() = {
    val q = quote(
      infix"""WITH RankedKills AS (
                SELECT COUNT(*)::int AS kill_count,
                       killer_id,
                       DATE(timestamp) AS kill_date,
                       ROW_NUMBER() OVER (PARTITION BY killer_id ORDER BY COUNT(*) DESC)::int AS row_num
                FROM killactivity WHERE exp > 0
                GROUP BY killer_id, DATE(timestamp)
              )
              SELECT rk.kill_count AS kill_count,
                     rk.killer_id AS killer_id,
                     TO_CHAR(rk.kill_date, 'FMMon DD, YYYY') AS f_kill_date,
                     rk.row_num AS row_num,
                     av.name AS name,
                     av.faction_id AS faction_id
              FROM RankedKills rk
              JOIN avatar av ON rk.killer_id = av.id
              WHERE rk.row_num = 1
              ORDER BY rk.kill_count DESC
              LIMIT 50""".as[Query[TopKillByDate]]
    )
    ctx.run(q)
  }

  /** Leaderboard: outfits by accumulated points. */
  def topOutfits() = {
    val q = quote(
      infix"""WITH OutfitData AS (
                SELECT o.id AS outfit_id, o.faction, o.name AS outfit_name,
                       a.id AS leader_id, a.name AS leader_name,
                       COUNT(om.avatar_id)::int AS members,
                       (op.points / 100.0)::int AS points
                FROM outfit o
                JOIN avatar a ON a.id = o.owner_id
                LEFT JOIN outfitmember om ON om.outfit_id = o.id
                LEFT JOIN outfitpoint_mv op ON op.outfit_id = o.id
                GROUP BY o.id, o.faction, o.name, a.name, a.id, op.points
              )
              SELECT outfit_id AS outfit_id, faction AS faction, outfit_name AS outfit_name,
                     leader_name AS leader_name, leader_id AS leader_id, members AS members,
                     points AS points
              FROM OutfitData
              ORDER BY points DESC""".as[Query[TopOutfit]]
    )
    ctx.run(q)
  }

  /** One outfit's summary. */
  def outfit(id: Int) = {
    val q = quote(
      infix"""WITH OutfitData AS (
                SELECT o.id AS outfit_id, o.faction, o.name AS outfit_name, o.created,
                       a.id AS leader_id, a.name AS leader_name,
                       COUNT(om.avatar_id)::int AS members,
                       (op.points / 100.0)::int AS points
                FROM outfit o
                JOIN avatar a ON a.id = o.owner_id
                LEFT JOIN outfitmember om ON om.outfit_id = o.id
                LEFT JOIN outfitpoint_mv op ON op.outfit_id = o.id
                WHERE o.id = ${lift(id)}
                GROUP BY o.created, o.id, o.faction, o.name, a.id, a.name, op.points
              )
              SELECT outfit_id AS outfit_id, faction AS faction, outfit_name AS outfit_name,
                     leader_id AS leader_id, leader_name AS leader_name, members AS members,
                     points AS points, created::text AS created
              FROM OutfitData""".as[Query[OutfitDetail]]
    )
    ctx.run(q)
  }

  /** One outfit's roster, each member's rank resolved against the outfit's own rank names. */
  def outfitMembers(id: Int) = {
    val q = quote(
      infix"""SELECT av.id AS avatar_id, av.bep AS bep, av.cep AS cep,
                     av.name AS avatar_name,
                     om.rank AS rank_num,
                     CASE om.rank
                       WHEN 0 THEN COALESCE(o.rank0, 'Fodder')
                       WHEN 1 THEN COALESCE(o.rank1, 'Soldier')
                       WHEN 2 THEN COALESCE(o.rank2, 'Commando')
                       WHEN 3 THEN COALESCE(o.rank3, 'Master at Arms')
                       WHEN 4 THEN COALESCE(o.rank4, 'Tactical Officer')
                       WHEN 5 THEN COALESCE(o.rank5, 'Strategic Officer')
                       WHEN 6 THEN COALESCE(o.rank6, 'Chief Officer')
                       WHEN 7 THEN COALESCE(o.rank7, 'Outfit Leader')
                     END AS rank_title,
                     COALESCE((op.points / 100.0)::int, 0) AS points,
                     om.created::text AS joined
              FROM outfitmember om
              JOIN avatar av ON av.id = om.avatar_id
              JOIN outfit o ON o.id = om.outfit_id
              LEFT JOIN outfitpoint op ON op.avatar_id = om.avatar_id
              WHERE om.outfit_id = ${lift(id)}
              ORDER BY points DESC, avatar_name ASC""".as[Query[OutfitMember]]
    )
    ctx.run(q)
  }

  /**
    * A fixed-size page of characters for the statistics tables.
    *
    * The sort arrives as free text, so it is not spliced into the statement -- see
    * [[accountsWithLastLogin]] for why the ordering is written as `CASE` expressions over a lifted key.
    */
  def characterBatch(batch: Int, sort: String, ascending: Boolean) = {
    val key = sort match {
      case "bep" => 2
      case "cep" => 3
      case _     => 1 // id
    }
    val q = quote(
      infix"""SELECT id AS id, name AS name, faction_id AS faction_id, bep AS bep, cep AS cep
              FROM avatar
              ORDER BY
                CASE WHEN ${lift(key)} = 1 AND     ${lift(ascending)} THEN id  END ASC,
                CASE WHEN ${lift(key)} = 1 AND NOT ${lift(ascending)} THEN id  END DESC,
                CASE WHEN ${lift(key)} = 2 AND     ${lift(ascending)} THEN bep END ASC,
                CASE WHEN ${lift(key)} = 2 AND NOT ${lift(ascending)} THEN bep END DESC,
                CASE WHEN ${lift(key)} = 3 AND     ${lift(ascending)} THEN cep END ASC,
                CASE WHEN ${lift(key)} = 3 AND NOT ${lift(ascending)} THEN cep END DESC
              OFFSET ${lift(batch)} * 500 LIMIT 500""".as[Query[StatCharacter]]
    )
    ctx.run(q)
  }

  // --- paginated listings ------------------------------------------------------------------------

  /**
    * Accounts with the time and address of their most recent login.
    *
    * Sort and filter arrive from the portal as free text, so neither is spliced into the statement.
    * The filter becomes two lifted booleans; the sort becomes a lifted key compared inside `CASE`
    * expressions, one per column and direction. Every expression but the selected one evaluates to
    * NULL for every row and so ties, leaving the chosen one to decide the order. It reads oddly, but
    * it keeps the listing as ONE parameterised statement -- the alternative is splicing an identifier
    * into SQL, which is the shape injection takes.
    */
  def accountsWithLastLogin(offset: Int, limit: Int, sort: String, ascending: Boolean, filter: String) = {
    val key = sort match {
      case "id"         => 1
      case "username"   => 3
      case "last_login" => 4
      case _            => 2 // created
    }
    val onlyGm     = filter == "gm"
    val onlyBanned = filter == "banned"
    val q = quote(
      infix"""SELECT account.id AS id, account.username AS username, account.created::text AS created,
                     account.last_modified::text AS last_modified, account.inactive AS inactive,
                     account.gm AS gm,
                     COALESCE(l.lastLogin, TIMESTAMP 'epoch')::text AS last_login,
                     l2.ip_address AS ip_address
              FROM account
              LEFT OUTER JOIN (
                SELECT MAX(id) AS loginId, account_id, MAX(login_time) AS lastLogin
                FROM login GROUP BY account_id
              ) l ON l.account_id = account.id
              LEFT OUTER JOIN login l2 ON l2.id = l.loginId
              WHERE (NOT ${lift(onlyGm)}     OR account.gm = TRUE)
                AND (NOT ${lift(onlyBanned)} OR account.inactive = TRUE)
              ORDER BY
                CASE WHEN ${lift(key)} = 1 AND     ${lift(ascending)} THEN account.id END ASC,
                CASE WHEN ${lift(key)} = 1 AND NOT ${lift(ascending)} THEN account.id END DESC,
                CASE WHEN ${lift(key)} = 2 AND     ${lift(ascending)} THEN account.created END ASC,
                CASE WHEN ${lift(key)} = 2 AND NOT ${lift(ascending)} THEN account.created END DESC,
                CASE WHEN ${lift(key)} = 3 AND     ${lift(ascending)} THEN account.username END ASC,
                CASE WHEN ${lift(key)} = 3 AND NOT ${lift(ascending)} THEN account.username END DESC,
                CASE WHEN ${lift(key)} = 4 AND     ${lift(ascending)} THEN l.lastLogin END ASC,
                CASE WHEN ${lift(key)} = 4 AND NOT ${lift(ascending)} THEN l.lastLogin END DESC
              OFFSET ${lift(offset)} LIMIT ${lift(limit)}""".as[Query[AccountListing]]
    )
    ctx.run(q).map(_.map(r => r.copy(ip_address = r.ip_address.flatMap(maskIp))))
  }

  /** How many accounts a given filter matches, for the pager. */
  def accountCount(filter: String) = {
    val onlyGm     = filter == "gm"
    val onlyBanned = filter == "banned"
    val q = quote(
      infix"""SELECT COUNT(*) AS count FROM account
              WHERE (NOT ${lift(onlyGm)}     OR gm = TRUE)
                AND (NOT ${lift(onlyBanned)} OR inactive = TRUE)""".as[Query[Count]]
    )
    ctx.run(q)
  }

  /** All characters, most recently seen first, with their GM/spectate grants. */
  def characters(offset: Int, limit: Int) = {
    val q = quote(
      infix"""SELECT avatar.id AS id, avatar.account_id AS account_id, avatar.name AS name,
                     avatar.faction_id AS faction_id, avatar.created::text AS created,
                     avatar.last_login::text AS last_login,
                     avatarmodepermission.avatar_id AS avatar_id,
                     avatarmodepermission.can_gm AS can_gm,
                     avatarmodepermission.can_spectate AS can_spectate
              FROM avatar
              LEFT JOIN avatarmodepermission ON avatarmodepermission.avatar_id = avatar.id
              ORDER BY avatar.last_login DESC
              OFFSET ${lift(offset)} LIMIT ${lift(limit)}""".as[Query[Character]]
    )
    ctx.run(q)
  }

  def characterCount() = {
    val q = quote(infix"""SELECT COUNT(*) AS count FROM avatar""".as[Query[Count]])
    ctx.run(q)
  }

  /** Only the characters that actually carry a GM or spectate grant. */
  def roles(offset: Int, limit: Int) = {
    val q = quote(
      infix"""SELECT avatar_id AS avatar_id, can_spectate AS can_spectate, can_gm AS can_gm,
                     id AS id, last_login::text AS last_login, account_id AS account_id, name AS name
              FROM avatarmodepermission
              INNER JOIN avatar ON avatar_id = id
              WHERE can_gm = TRUE OR can_spectate = TRUE
              ORDER BY last_login DESC
              OFFSET ${lift(offset)} LIMIT ${lift(limit)}""".as[Query[Role]]
    )
    ctx.run(q)
  }

  def roleCount() = {
    val q = quote(
      infix"""SELECT COUNT(*) AS count FROM avatarmodepermission
              INNER JOIN avatar ON avatar_id = id
              WHERE can_gm = TRUE OR can_spectate = TRUE""".as[Query[Count]]
    )
    ctx.run(q)
  }

  /** One account's login history, newest first. The address is masked on the way out. */
  def accountLogins(accountId: Int, offset: Int, limit: Int): Future[List[LoginRow]] = {
    val q = quote(
      infix"""SELECT id AS id, account_id AS account_id, login_time::text AS login_time,
                     port AS port, ip_address AS ip_address
              FROM login WHERE account_id = ${lift(accountId)}
              ORDER BY login_time DESC
              OFFSET ${lift(offset)} LIMIT ${lift(limit)}""".as[Query[LoginRow]]
    )
    ctx.run(q).map(_.map(r => r.copy(ip_address = r.ip_address.flatMap(maskIp))))
  }

  def loginCount(accountId: Int) = {
    val q = quote(
      infix"""SELECT COUNT(*) AS count FROM login
              WHERE account_id = ${lift(accountId)}""".as[Query[Count]]
    )
    ctx.run(q)
  }

  // --- search ------------------------------------------------------------------------------------

  /** Accounts whose username contains the term, case-insensitively. */
  def searchAccounts(pattern: String, offset: Int, limit: Int) = {
    val q = quote(
      infix"""SELECT id AS id, username AS username, gm AS gm, inactive AS inactive
              FROM account WHERE UPPER(username) LIKE ${lift(pattern)}
              ORDER BY username
              OFFSET ${lift(offset)} LIMIT ${lift(limit)}""".as[Query[SearchAccount]]
    )
    ctx.run(q)
  }

  /** Characters whose name contains the term, case-insensitively. */
  def searchCharacters(pattern: String, offset: Int, limit: Int) = {
    val q = quote(
      infix"""SELECT a.id AS id, a.name AS name, a.account_id AS account_id,
                     a.faction_id AS faction_id, b.avatar_id AS avatar_id,
                     b.can_spectate AS can_spectate, b.can_gm AS can_gm
              FROM avatar a
              LEFT JOIN avatarmodepermission b ON a.id = b.avatar_id
              WHERE UPPER(a.name) LIKE ${lift(pattern)}
              ORDER BY name
              OFFSET ${lift(offset)} LIMIT ${lift(limit)}""".as[Query[SearchCharacter]]
    )
    ctx.run(q)
  }

  // --- site statistics ---------------------------------------------------------------------------

  def accountTotal() = {
    val q = quote(infix"""SELECT COUNT(*) AS count FROM account""".as[Query[Count]])
    ctx.run(q)
  }

  def newestCharacter() = {
    val q = quote(
      infix"""SELECT id AS id, account_id AS account_id, name AS name, faction_id AS faction_id,
              created::text AS created
              FROM avatar ORDER BY id DESC LIMIT 1""".as[Query[LastCharacter]]
    )
    ctx.run(q)
  }

  // --- credentials -------------------------------------------------------------------------------

  /**
    * A password check, performed here rather than in the portal.
    *
    * The point of moving it is that `passhash` never leaves this process. It also closes a whole class
    * of bug: the portal hashed with Node's bcrypt, which writes `$2b$` revisions that this server's
    * jBCrypt-derived checker rejects outright, so a password could verify in the portal and fail in
    * the game. One implementation now produces and checks every hash.
    *
    * A miss still costs a bcrypt comparison against a throwaway hash, so an unknown username takes the
    * same time as a wrong password and the response cannot be used to enumerate accounts. A banned
    * (`inactive`) account is compared and then refused, for the same reason.
    */
  def validateAccount(username: String, password: String): Future[Option[Int]] = {
    val q = quote(
      infix"""SELECT id AS id, passhash AS passhash, inactive AS inactive
              FROM account WHERE username = ${lift(username)}""".as[Query[Credentials]]
    )
    ctx.run(q).map {
      case creds :: _ =>
        val ok = password.isBcryptedBounded(creds.passhash)
        if (ok && !creds.inactive) Some(creds.id) else None
      case Nil =>
        password.isBcryptedBounded(DummyHash)
        None
    }
  }

  /**
    * Create an account from the portal's registration form.
    *
    * BOTH password columns are written, matching `LoginActor`'s own account creation. The portal used
    * to write `passhash` alone, leaving `password` at its empty default -- such an account could log
    * in from the game client (which sends the password in the clear, checked against `passhash`) but
    * never from the launcher (which sends SHA-256 of username+password, checked against `password`).
    * Writing both makes an account registered here identical to one this server creates for itself.
    *
    * Returns the new id, or None if the username is taken. The uniqueness constraint on `username` is
    * still the real guard -- this pre-check just turns the common case into a clean answer instead of
    * a constraint violation.
    */
  def createAccount(username: String, password: String): Future[Option[Int]] = {
    val taken = quote(
      infix"""SELECT COUNT(*) AS count FROM account
              WHERE LOWER(username) = LOWER(${lift(username)})""".as[Query[Count]]
    )
    ctx.run(taken).flatMap {
      case c :: _ if c.count > 0 => Future.successful(None)
      case _ =>
        val passhash = password.bcryptBounded(BcryptRounds)
        val launcher = launcherPassword(username, password, BcryptRounds)
        ctx
          .run(
            quote(
              query[persistence.Account]
                .insert(
                  _.username -> lift(username),
                  _.passhash -> lift(passhash),
                  _.password -> lift(launcher)
                )
                .returningGenerated(_.id)
            )
          )
          .map(Some(_))
    }
  }

  // --- portal session store ----------------------------------------------------------------------

  /**
    * The Express session table.
    *
    * These five operations are the whole of `connect-pg-simple`'s storage contract. Moving them here
    * is what lets the portal drop its connection pool outright -- with the session table left behind
    * it would still need a pool, and would still be a database client.
    */
  def sessionGet(sid: String) = {
    val q = quote(
      infix"""SELECT sess::text AS sess FROM session
              WHERE sid = ${lift(sid)} AND expire > NOW()""".as[Query[SessionRow]]
    )
    ctx.run(q)
  }

  def sessionSet(sid: String, sess: String, expiresAt: Long): Future[Long] = {
    val q = quote(
      infix"""INSERT INTO session (sid, sess, expire)
              VALUES (${lift(sid)}, ${lift(sess)}::json, TO_TIMESTAMP(${lift(expiresAt)}))
              ON CONFLICT (sid) DO UPDATE
                SET sess = EXCLUDED.sess, expire = EXCLUDED.expire""".as[Action[Long]]
    )
    ctx.run(q)
  }

  def sessionTouch(sid: String, expiresAt: Long): Future[Long] = {
    val q = quote(
      infix"""UPDATE session SET expire = TO_TIMESTAMP(${lift(expiresAt)})
              WHERE sid = ${lift(sid)}""".as[Action[Long]]
    )
    ctx.run(q)
  }

  def sessionDestroy(sid: String): Future[Long] = {
    val q = quote(infix"""DELETE FROM session WHERE sid = ${lift(sid)}""".as[Action[Long]])
    ctx.run(q)
  }

  /** Drop expired rows. `connect-pg-simple` did this on a timer; the portal still asks for it. */
  def sessionReap(): Future[Long] = {
    val q = quote(infix"""DELETE FROM session WHERE expire < NOW()""".as[Action[Long]])
    ctx.run(q)
  }

  /**
    * A bcrypt hash of a value nobody can supply, so an unknown username costs the same work as a real
    * one. It must be a genuine hash: a hand-written constant of the wrong shape is rejected in
    * microseconds and would restore the timing difference it exists to hide.
    */
  private val DummyHash: String =
    java.util.UUID.randomUUID().toString.bcryptBounded(BcryptRounds)

  /**
    * The launcher's password form: bcrypt of the hex SHA-256 of username+password. Kept identical to
    * `LoginActor.generateNewPassword`, the other place an account can come into existence.
    */
  private def launcherPassword(username: String, password: String, rounds: Int): String = {
    val salted = username.concat(password)
    val hashed = java.security.MessageDigest
      .getInstance("SHA-256")
      .digest(salted.getBytes("UTF-8"))
      .map("%02x".format(_))
      .mkString
    hashed.bcryptBounded(rounds)
  }
}
