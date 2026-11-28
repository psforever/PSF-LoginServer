package net.psforever.api

import com.typesafe.config.{Config => TypesafeConfig, ConfigValueFactory}
import io.getquill.{PostgresJAsyncContext, SnakeCase}
import net.psforever.persistence

import scala.concurrent.{ExecutionContext, Future}

/** What the API needs to know about an account. */
final case class ApiAccount(
    id: Int,
    username: String,
    passhash: String,
    password: String,
    inactive: Boolean,
    gm: Boolean
)

/** A sign-in session as stored; times are UTC epoch milliseconds. */
final case class StoredSession(
    tokenDigest: String,
    accountId: Int,
    createdAtMs: Long,
    lastSeenAtMs: Long,
    credentialFingerprint: String,
    gmAtIssue: Boolean,
    mfa: Boolean
)

/** An account's TOTP secret (base32), whether it has been confirmed, and the last step accepted. */
final case class StoredTotp(accountId: Int, secret: String, enabled: Boolean, lastUsedStep: Long)

/**
  * The API's database access, behind a trait so the session logic can be tested without PostgreSQL.
  * Every method is one short statement; none of them holds a connection while waiting on anything else.
  */
trait ApiStore {

  /** The account with this username: an exact match first, then ignoring case, as the game login does. */
  def findAccount(username: String): Future[Option[ApiAccount]]

  def createSession(session: StoredSession): Future[Unit]

  /** A session together with its account's current state. */
  def session(tokenDigest: String): Future[Option[(StoredSession, ApiAccount)]]

  def touchSession(tokenDigest: String, lastSeenAtMs: Long): Future[Unit]

  def setSessionMfa(tokenDigest: String, mfa: Boolean): Future[Unit]

  /** Mark every session of an account as not TOTP-checked. */
  def clearAccountMfa(accountId: Int): Future[Unit]

  def deleteSession(tokenDigest: String): Future[Unit]

  def deleteAccountSessions(accountId: Int): Future[Unit]

  /**
    * Delete an account's sessions issued against a credential other than `credentialFingerprint`, so a
    * token left over from before a password change ends the sessions that change invalidated and leaves
    * the ones signed in after it.
    */
  def deleteStaleSessions(accountId: Int, credentialFingerprint: String): Future[Unit]

  /** Delete sessions idle since before `idleCutoffMs` or created before `createdCutoffMs`; returns how many. */
  def deleteExpiredSessions(idleCutoffMs: Long, createdCutoffMs: Long): Future[Long]

  def totp(accountId: Int): Future[Option[StoredTotp]]

  /** Store a not-yet-confirmed secret, replacing any earlier unconfirmed one. */
  def savePendingTotp(accountId: Int, secret: String): Future[Unit]

  /**
    * Confirm a pending secret, recording the step of the code that confirmed it, but only while that exact
    * secret is still pending and its code has not been used. A secret replaced by another request, or a
    * second confirmation of the same code, then fails instead of enabling a secret nobody checked.
    * @return whether it was confirmed; `false` means the enrolment changed underneath this request
    */
  def enableTotp(accountId: Int, secret: String, step: Long): Future[Boolean]

  /**
    * Delete an account's enabled secret, but only while it is still the one this request checked a code
    * against.
    * @return whether it was deleted; `false` means the enrolment changed underneath this request
    */
  def deleteTotp(accountId: Int, secret: String): Future[Boolean]

  /**
    * Record that a code from `step` was used, only if no code from that step or later was used before.
    * @return whether it was recorded; false means the code was a replay
    */
  def useTotpStep(accountId: Int, step: Long): Future[Boolean]
}

/**
  * [[ApiStore]] on PostgreSQL, through a connection pool of the API's own, separate from the one the game
  * uses, so API load can only ever queue behind other API work.
  */
final class PostgresApiStore(databaseConfig: TypesafeConfig, maxConnections: Int)(implicit ec: ExecutionContext)
    extends ApiStore {
  private val ctx = new PostgresJAsyncContext(
    SnakeCase,
    databaseConfig.withValue("maxActiveConnections", ConfigValueFactory.fromAnyRef(maxConnections))
  )
  import ctx._

  private def toAccount(row: persistence.Account): ApiAccount =
    ApiAccount(row.id, row.username, row.passhash, row.password, row.inactive, row.gm)

  private def toSession(row: persistence.ApiSession): StoredSession =
    StoredSession(
      row.tokenDigest,
      row.accountId,
      row.createdAtMs,
      row.lastSeenAtMs,
      row.credentialFingerprint,
      row.gmAtIssue,
      row.mfa
    )

  override def findAccount(username: String): Future[Option[ApiAccount]] =
    ctx.run(query[persistence.Account].filter(_.username == lift(username))).flatMap { exact =>
      exact.headOption match {
        case Some(found) => Future.successful(Some(toAccount(found)))
        case None =>
          ctx
            .run(query[persistence.Account].filter(_.username.toLowerCase == lift(username.toLowerCase)))
            .map(_.headOption.map(toAccount))
      }
    }

  override def createSession(s: StoredSession): Future[Unit] =
    ctx
      .run(
        query[persistence.ApiSession].insertValue(
          lift(
            persistence.ApiSession(
              s.tokenDigest,
              s.accountId,
              s.createdAtMs,
              s.lastSeenAtMs,
              s.credentialFingerprint,
              s.gmAtIssue,
              s.mfa
            )
          )
        )
      )
      .map(_ => ())

  override def session(tokenDigest: String): Future[Option[(StoredSession, ApiAccount)]] =
    ctx
      .run(
        query[persistence.ApiSession]
          .filter(_.tokenDigest == lift(tokenDigest))
          .join(query[persistence.Account])
          .on(_.accountId == _.id)
      )
      .map(_.headOption.map { case (s, a) => (toSession(s), toAccount(a)) })

  override def touchSession(tokenDigest: String, lastSeenAtMs: Long): Future[Unit] =
    ctx
      .run(
        query[persistence.ApiSession]
          .filter(_.tokenDigest == lift(tokenDigest))
          .update(_.lastSeenAtMs -> lift(lastSeenAtMs))
      )
      .map(_ => ())

  override def setSessionMfa(tokenDigest: String, mfa: Boolean): Future[Unit] =
    ctx
      .run(query[persistence.ApiSession].filter(_.tokenDigest == lift(tokenDigest)).update(_.mfa -> lift(mfa)))
      .map(_ => ())

  override def clearAccountMfa(accountId: Int): Future[Unit] =
    ctx
      .run(query[persistence.ApiSession].filter(_.accountId == lift(accountId)).update(_.mfa -> false))
      .map(_ => ())

  override def deleteSession(tokenDigest: String): Future[Unit] =
    ctx.run(query[persistence.ApiSession].filter(_.tokenDigest == lift(tokenDigest)).delete).map(_ => ())

  override def deleteAccountSessions(accountId: Int): Future[Unit] =
    ctx.run(query[persistence.ApiSession].filter(_.accountId == lift(accountId)).delete).map(_ => ())

  override def deleteStaleSessions(accountId: Int, credentialFingerprint: String): Future[Unit] =
    ctx
      .run(
        query[persistence.ApiSession]
          .filter(s => s.accountId == lift(accountId) && s.credentialFingerprint != lift(credentialFingerprint))
          .delete
      )
      .map(_ => ())

  override def deleteExpiredSessions(idleCutoffMs: Long, createdCutoffMs: Long): Future[Long] =
    ctx.run(
      query[persistence.ApiSession]
        .filter(s => s.lastSeenAtMs < lift(idleCutoffMs) || s.createdAtMs < lift(createdCutoffMs))
        .delete
    )

  override def totp(accountId: Int): Future[Option[StoredTotp]] =
    ctx
      .run(query[persistence.AccountTotp].filter(_.accountId == lift(accountId)))
      .map(_.headOption.map(t => StoredTotp(t.accountId, t.secret, t.enabled, t.lastUsedStep)))

  override def savePendingTotp(accountId: Int, secret: String): Future[Unit] =
    ctx
      .run(
        query[persistence.AccountTotp]
          .insertValue(lift(persistence.AccountTotp(accountId, secret, enabled = false, lastUsedStep = 0L)))
          .onConflictUpdate(_.accountId)(
            (t, e) => t.secret -> e.secret,
            (t, _) => t.enabled -> false,
            (t, _) => t.lastUsedStep -> 0L
          )
      )
      .map(_ => ())

  override def enableTotp(accountId: Int, secret: String, step: Long): Future[Boolean] =
    ctx
      .run(
        query[persistence.AccountTotp]
          .filter(t =>
            t.accountId == lift(accountId) && !t.enabled && t.secret == lift(secret) &&
              t.lastUsedStep < lift(step)
          )
          .update(_.enabled -> true, _.lastUsedStep -> lift(step))
      )
      .map(_ > 0)

  override def deleteTotp(accountId: Int, secret: String): Future[Boolean] =
    ctx
      .run(
        query[persistence.AccountTotp]
          .filter(t => t.accountId == lift(accountId) && t.enabled && t.secret == lift(secret))
          .delete
      )
      .map(_ > 0)

  override def useTotpStep(accountId: Int, step: Long): Future[Boolean] =
    ctx
      .run(
        query[persistence.AccountTotp]
          .filter(t => t.accountId == lift(accountId) && t.lastUsedStep < lift(step))
          .update(_.lastUsedStep -> lift(step))
      )
      .map(_ > 0)
}
