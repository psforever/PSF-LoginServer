package net.psforever.actors.api

import com.github.t3hnar.bcrypt._
import io.getquill.{Action, Ord, Query}
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
  * Most of these are Quill QUOTATIONS against `persistence.*`, the way the rest of this codebase
  * queries the database. Raw SQL is the EXCEPTION here, kept only where a quotation cannot reasonably
  * express the statement:
  *
  *   - the leaderboards and outfit summaries (window functions, CTEs, `GROUP BY` over computed
  *     columns);
  *   - the accounts listing, which joins a grouped subquery back to the row that produced it;
  *   - the session store, which manipulates a JSON column.
  *
  * Prefer a quotation for anything new. It is checked at compile time, it cannot be broken by a typo
  * in a column alias, and it sidesteps a trap peculiar to this driver: a literal question mark
  * anywhere in a raw statement -- including inside a SQL comment, and including the `?` jsonb
  * operator -- is counted as a bind placeholder, and the query is rejected for having more parameters
  * than it was given.
  *
  * Return types are INFERRED on purpose. Quill's `run` is overloaded between a query
  * (`Quoted[Query[T]] -> Future[List[T]]`) and a single value (`Quoted[T] -> Future[T]`), and an
  * expected type of `Future[List[T]]` matches BOTH -- annotating one picks the wrong overload and
  * fails to compile.
  *
  * The row classes carry snake_case field names, which is unusual for Scala and deliberate: they are
  * wire DTOs whose field names ARE the JSON keys the portal's React components already read
  * (`killer_id`, `faction_id`). For the remaining raw statements they double as the column aliases
  * Quill decodes on, so there a mismatch fails at runtime rather than compile time -- one more reason
  * to prefer a quotation.
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

  /**
    * A `LocalDateTime` in the exact text form Postgres produces, which is what the portal has always
    * received: `2026-07-25 06:27:38.089649`.
    *
    * The raw statements this file grew from cast timestamps with `::text` and let Postgres format
    * them. A quotation decodes a real `LocalDateTime` instead, and `toString` would render
    * `2026-07-25T06:27:38.089649` -- a `T` where the browser has always seen a space, and, when the
    * seconds and nanoseconds are both zero, no seconds field at all. Reproducing the database's own
    * format keeps the wire bytes identical across the conversion.
    *
    * Postgres trims trailing zeros, so `.847000` prints as `.847` and an exactly-zero fraction is
    * omitted entirely.
    *
    * One deliberate loss: the project's persistence classes use Joda `LocalDateTime`, which carries
    * MILLISECONDS. Postgres stores microseconds, so a timestamp that used to arrive as
    * `.089649` now reads `.089`. These values are only ever displayed as dates, and using the
    * project's own temporal type matters more than three digits nothing renders.
    */
  private val SecondsFormat = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  private def pgTimestamp(at: org.joda.time.LocalDateTime): String = {
    val whole = at.toString(SecondsFormat)
    val millis = at.getMillisOfSecond
    if (millis == 0) whole
    else whole + "." + "%03d".format(millis).reverse.dropWhile(_ == '0').reverse
  }

  // --- row types ---------------------------------------------------------------------------------

  case class AccountRow(
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
    * The projection is part of the query, not something applied after: Quill emits a SELECT of just
    * these columns, so `passhash` and `password` are never read out of the table at all -- they do not
    * reach this process, let alone the portal.
    */
  def account(id: Int) = {
    ctx
      .run(
        query[persistence.Account]
          .filter(_.id == lift(id))
          .map(a => (a.id, a.username, a.created, a.lastModified, a.inactive, a.gm))
      )
      .map(_.map { case (accId, username, created, modified, inactive, gm) =>
        AccountRow(accId, username, pgTimestamp(created), pgTimestamp(modified), inactive, gm)
      })
  }


  /** Who owns a character, and whether it still exists. Backs the portal's "is this yours?" check. */
  def avatarOwner(avatarId: Int) = {
    ctx
      .run(query[persistence.Avatar].filter(_.id == lift(avatarId)).map(a => (a.id, a.accountId, a.name, a.deleted)))
      .map(_.map { case (id, accountId, name, deleted) => AvatarOwner(id, accountId, name, deleted) })
  }


  /** A character by name, for the public name lookup. */
  def characterByName(name: String) = {
    ctx
      .run(
        query[persistence.Avatar]
          .filter(a => a.name == lift(name) && !a.deleted)
          .map(a => (a.id, a.accountId, a.name, a.factionId, a.created, a.lastLogin))
      )
      .map(_.map { case (id, accountId, n, faction, created, lastLogin) =>
        NamedCharacter(id, accountId, n, faction, pgTimestamp(created), pgTimestamp(lastLogin))
      })
  }


  /** Every living character on an account, with its GM/spectate grants. */
  def charactersByAccount(accountId: Int) = {
    ctx
      .run(
        query[persistence.Avatar]
          .filter(a => a.accountId == lift(accountId) && !a.deleted)
          .leftJoin(query[persistence.Avatarmodepermission])
          .on((a, p) => a.id == p.avatarId)
      )
      .map(_.map { case (a, perm) =>
        AccountCharacter(
          a.id, a.accountId, a.name, a.factionId, a.genderId, a.headId,
          pgTimestamp(a.created), pgTimestamp(a.lastLogin), a.bep, a.cep, a.deleted,
          perm.map(_.avatarId), perm.map(_.canGm), perm.map(_.canSpectate)
        )
      })
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
    ctx
      .run(query[persistence.Locker].filter(_.avatarId == lift(avatarId)).map(_.items))
      .map(_.map(items => LockerRow(Option(items))))
  }


  /** A character's saved infantry loadouts. */
  def loadouts(avatarId: Int) = {
    ctx
      .run(
        query[persistence.Loadout]
          .filter(_.avatarId == lift(avatarId))
          .sortBy(_.loadoutNumber)
          .map(l => (l.loadoutNumber, l.exosuitId, l.name, l.items))
      )
      .map(_.map { case (number, exosuit, name, items) => LoadoutRow(number, exosuit, name, items) })
  }


  /** A character's saved vehicle loadouts. */
  def vehicleLoadouts(avatarId: Int) = {
    ctx
      .run(
        query[persistence.Vehicleloadout]
          .filter(_.avatarId == lift(avatarId))
          .sortBy(_.loadoutNumber)
          .map(l => (l.loadoutNumber, l.name, l.vehicle, l.items))
      )
      .map(_.map { case (number, name, vehicle, items) => VehicleLoadoutRow(number, name, vehicle, items) })
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
    * The sort arrives as free text and is matched here against the three columns the statistics pages
    * offer. As a quotation the ordering is simply a different `sortBy` per case -- the raw version had
    * to express it as `CASE` expressions over a lifted key, because splicing a column name into SQL is
    * how injection happens. Composing quoted queries removes the question entirely.
    */
  def characterBatch(batch: Int, sort: String, ascending: Boolean) = {
    val base = quote(query[persistence.Avatar])
    val sorted = (sort, ascending) match {
      case ("bep", true)  => quote(base.sortBy(_.bep)(Ord.asc))
      case ("bep", false) => quote(base.sortBy(_.bep)(Ord.desc))
      case ("cep", true)  => quote(base.sortBy(_.cep)(Ord.asc))
      case ("cep", false) => quote(base.sortBy(_.cep)(Ord.desc))
      case (_, false)     => quote(base.sortBy(_.id)(Ord.desc))
      case _              => quote(base.sortBy(_.id)(Ord.asc))
    }
    ctx
      .run(sorted.drop(lift(batch * 500)).take(500).map(a => (a.id, a.name, a.factionId, a.bep, a.cep)))
      .map(_.map { case (id, name, faction, bep, cep) => StatCharacter(id, name, faction, bep, cep) })
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
    val base = quote(query[persistence.Account])
    val filtered = filter match {
      case "gm"     => quote(base.filter(_.gm))
      case "banned" => quote(base.filter(_.inactive))
      case _        => base
    }
    ctx.run(filtered.size)
  }


  /** All characters, most recently seen first, with their GM/spectate grants. */
  def characters(offset: Int, limit: Int) = {
    ctx
      .run(
        query[persistence.Avatar]
          .leftJoin(query[persistence.Avatarmodepermission])
          .on((a, p) => a.id == p.avatarId)
          .sortBy { case (a, _) => a.lastLogin }(Ord.desc)
          .drop(lift(offset))
          .take(lift(limit))
      )
      .map(_.map { case (a, perm) =>
        Character(
          a.id, a.accountId, a.name, a.factionId,
          pgTimestamp(a.created), pgTimestamp(a.lastLogin),
          perm.map(_.avatarId), perm.map(_.canGm), perm.map(_.canSpectate)
        )
      })
  }


  def characterCount() = ctx.run(query[persistence.Avatar].size)


  /** Only the characters that actually carry a GM or spectate grant. */
  def roles(offset: Int, limit: Int) = {
    ctx
      .run(
        query[persistence.Avatarmodepermission]
          .filter(p => p.canGm || p.canSpectate)
          .join(query[persistence.Avatar])
          .on((p, a) => p.avatarId == a.id)
          .sortBy { case (_, a) => a.lastLogin }(Ord.desc)
          .drop(lift(offset))
          .take(lift(limit))
      )
      .map(_.map { case (p, a) =>
        Role(p.avatarId, p.canSpectate, p.canGm, a.id, pgTimestamp(a.lastLogin), a.accountId, a.name)
      })
  }


  def roleCount() =
    ctx.run(query[persistence.Avatarmodepermission].filter(p => p.canGm || p.canSpectate).size)


  /** One account's login history, newest first. The address is masked on the way out. */
  def accountLogins(accountId: Int, offset: Int, limit: Int): Future[List[LoginRow]] = {
    ctx
      .run(
        query[persistence.Login]
          .filter(_.accountId == lift(accountId))
          .sortBy(_.loginTime)(Ord.desc)
          .drop(lift(offset))
          .take(lift(limit))
          .map(l => (l.id, l.accountId, l.loginTime, l.port, l.ipAddress))
      )
      .map(_.map { case (id, accId, at, port, ip) =>
        LoginRow(id, accId, pgTimestamp(at), port, maskIp(ip))
      })
  }


  def loginCount(accountId: Int) =
    ctx.run(query[persistence.Login].filter(_.accountId == lift(accountId)).size)


  // --- search ------------------------------------------------------------------------------------

  /**
    * Accounts whose username contains the term, case-insensitively.
    *
    * `ilike` is the project's own helper from `net.psforever.util.Database`, which is exactly what it
    * exists for -- the raw version reached for `UPPER(username) LIKE UPPER(?)` instead.
    */
  def searchAccounts(pattern: String, offset: Int, limit: Int) = {
    ctx
      .run(
        query[persistence.Account]
          .filter(a => a.username.ilike(lift(pattern)))
          .sortBy(_.username)
          .drop(lift(offset))
          .take(lift(limit))
          .map(a => (a.id, a.username, a.gm, a.inactive))
      )
      .map(_.map { case (id, username, gm, inactive) => SearchAccount(id, username, gm, inactive) })
  }


  /** Characters whose name contains the term, case-insensitively. */
  def searchCharacters(pattern: String, offset: Int, limit: Int) = {
    ctx
      .run(
        query[persistence.Avatar]
          .filter(a => a.name.ilike(lift(pattern)))
          .leftJoin(query[persistence.Avatarmodepermission])
          .on((a, p) => a.id == p.avatarId)
          .sortBy { case (a, _) => a.name }
          .drop(lift(offset))
          .take(lift(limit))
      )
      .map(_.map { case (a, perm) =>
        SearchCharacter(a.id, a.name, a.accountId, a.factionId,
                        perm.map(_.avatarId), perm.map(_.canSpectate), perm.map(_.canGm))
      })
  }


  // --- site statistics ---------------------------------------------------------------------------

  def accountTotal() = ctx.run(query[persistence.Account].size)


  def newestCharacter() = {
    ctx
      .run(
        query[persistence.Avatar]
          .sortBy(_.id)(Ord.desc)
          .take(1)
          .map(a => (a.id, a.accountId, a.name, a.factionId, a.created))
      )
      .map(_.map { case (id, accountId, name, faction, created) =>
        LastCharacter(id, accountId, name, faction, pgTimestamp(created))
      })
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
      query[persistence.Account]
        .filter(_.username == lift(username))
        .map(a => (a.id, a.passhash, a.inactive))
    )
    ctx.run(q).map(_.map { case (id, passhash, inactive) => Credentials(id, passhash, inactive) }).map {
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
    // Case-insensitive, matching how LoginActor resolves an existing account.
    val taken = quote(
      query[persistence.Account].filter(_.username.toLowerCase == lift(username).toLowerCase).size
    )
    ctx.run(taken).flatMap {
      case existing if existing > 0 => Future.successful(None)
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

  /**
    * Store a session -- but never let the caller decide WHOSE it is.
    *
    * `account_id` inside the session body is what [[CallerAuth]] reads to decide who is calling and
    * whether they are a game master, so if a caller could write it, it could mint itself an
    * administrator and every other check on this API would be decoration. The incoming body is
    * therefore stripped of `account_id` and the stored value is carried across untouched; only
    * [[sessionBindAccount]], called from the login route after a password has actually been checked,
    * can set it.
    *
    * Everything else in the body is the portal's to own -- flash messages, CSRF tokens, whatever
    * express-session is keeping -- and passes through unread.
    *
    * Done in SQL rather than by reading the row and writing it back, so a session being written
    * concurrently cannot lose its binding to a racing update.
    *
    * Note `jsonb_exists(...)` rather than the natural `sess ? 'account_id'`. The driver scans the
    * statement for bind placeholders and counts every literal question mark as one -- including any
    * inside a SQL comment -- so the query arrives claiming more parameters than it was given and is
    * rejected before it reaches Postgres. Keep question marks out of this file's SQL entirely.
    */
  def sessionSet(sid: String, sess: String, expiresAt: Long): Future[Long] = {
    val q = quote(
      infix"""INSERT INTO session (sid, sess, expire)
              VALUES (
                ${lift(sid)},
                (${lift(sess)}::jsonb - 'account_id')::json,
                TO_TIMESTAMP(${lift(expiresAt)})
              )
              ON CONFLICT (sid) DO UPDATE
                SET sess = CASE
                             WHEN jsonb_exists(session.sess::jsonb, 'account_id')
                               THEN (
                                 (EXCLUDED.sess::jsonb - 'account_id')
                                 || jsonb_build_object('account_id', session.sess::jsonb -> 'account_id')
                               )::json
                             ELSE EXCLUDED.sess
                           END,
                    expire = EXCLUDED.expire""".as[Action[Long]]
    )
    ctx.run(q)
  }

  /**
    * Bind a session to an account, after its password has been verified.
    *
    * The single writer of `account_id`. Splitting it out from [[sessionSet]] is what lets that method
    * refuse the field outright: authentication happens here, in the same call that checked the
    * credential, rather than being asserted later by whoever holds the session id.
    *
    * Creates the row if the portal has not written the session yet, which is the ordinary case --
    * express-session does not persist an anonymous session (`saveUninitialized: false`), so a user's
    * first stored session is usually the one created by logging in.
    */
  def sessionBindAccount(sid: String, accountId: Int, expiresAt: Long): Future[Long] = {
    val q = quote(
      infix"""INSERT INTO session (sid, sess, expire)
              VALUES (
                ${lift(sid)},
                jsonb_build_object('account_id', ${lift(accountId)})::json,
                TO_TIMESTAMP(${lift(expiresAt)})
              )
              ON CONFLICT (sid) DO UPDATE
                SET sess = (
                      session.sess::jsonb || jsonb_build_object('account_id', ${lift(accountId)})
                    )::json,
                    expire = TO_TIMESTAMP(${lift(expiresAt)})""".as[Action[Long]]
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
