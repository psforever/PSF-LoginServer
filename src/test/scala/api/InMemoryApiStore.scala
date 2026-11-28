// Copyright (c) 2026 PSForever
package api

import com.github.t3hnar.bcrypt._
import net.psforever.api.{ApiAccount, ApiStore, StoredSession, StoredTotp}

import scala.collection.mutable
import scala.concurrent.Future

/** [[ApiStore]] in memory, for testing session logic without PostgreSQL. */
final class InMemoryApiStore extends ApiStore {
  val accounts: mutable.Map[Int, ApiAccount]       = mutable.Map()
  val sessions: mutable.Map[String, StoredSession] = mutable.Map()
  val totps: mutable.Map[Int, StoredTotp]          = mutable.Map()

  /** Add an account whose password hash is cheap to check. */
  def addAccount(id: Int, username: String, password: String, gm: Boolean = false): ApiAccount = {
    val account = ApiAccount(id, username, password.bcryptBounded(4), "", inactive = false, gm = gm)
    synchronized(accounts(id) = account)
    account
  }

  def update(id: Int)(change: ApiAccount => ApiAccount): Unit = synchronized(accounts(id) = change(accounts(id)))

  def sessionsOf(accountId: Int): Seq[StoredSession] =
    synchronized(sessions.values.filter(_.accountId == accountId).toSeq)

  private def done[T](value: => T): Future[T] = Future.successful(synchronized(value))

  override def findAccount(username: String): Future[Option[ApiAccount]] =
    done(
      accounts.values
        .find(_.username == username)
        .orElse(accounts.values.find(_.username.toLowerCase == username.toLowerCase))
    )

  override def createSession(session: StoredSession): Future[Unit] = done(sessions(session.tokenDigest) = session)

  override def session(tokenDigest: String): Future[Option[(StoredSession, ApiAccount)]] =
    done(sessions.get(tokenDigest).flatMap(s => accounts.get(s.accountId).map(a => (s, a))))

  override def touchSession(tokenDigest: String, lastSeenAtMs: Long): Future[Unit] =
    done(sessions.get(tokenDigest).foreach(s => sessions(tokenDigest) = s.copy(lastSeenAtMs = lastSeenAtMs)))

  override def setSessionMfa(tokenDigest: String, mfa: Boolean): Future[Unit] =
    done(sessions.get(tokenDigest).foreach(s => sessions(tokenDigest) = s.copy(mfa = mfa)))

  override def clearAccountMfa(accountId: Int): Future[Unit] =
    done(sessions.mapValuesInPlace {
      case (_, s) if s.accountId == accountId => s.copy(mfa = false)
      case (_, s)                             => s
    })

  override def deleteSession(tokenDigest: String): Future[Unit] = done(sessions.remove(tokenDigest))

  override def deleteAccountSessions(accountId: Int): Future[Unit] =
    done(sessions.filterInPlace { case (_, s) => s.accountId != accountId })

  override def deleteExpiredSessions(idleCutoffMs: Long, createdCutoffMs: Long): Future[Long] =
    done {
      val before = sessions.size
      sessions.filterInPlace { case (_, s) => s.lastSeenAtMs >= idleCutoffMs && s.createdAtMs >= createdCutoffMs }
      (before - sessions.size).toLong
    }

  override def totp(accountId: Int): Future[Option[StoredTotp]] = done(totps.get(accountId))

  override def savePendingTotp(accountId: Int, secret: String): Future[Boolean] =
    done(totps.get(accountId) match {
      case Some(t) if t.enabled => false
      case _ =>
        totps(accountId) = StoredTotp(accountId, secret, enabled = false, lastUsedStep = 0L)
        true
    })

  override def enableTotp(accountId: Int, secret: String, step: Long): Future[Boolean] =
    done(totps.get(accountId) match {
      case Some(t) if !t.enabled && t.secret == secret && t.lastUsedStep < step =>
        totps(accountId) = t.copy(enabled = true, lastUsedStep = step)
        true
      case _ => false
    })

  override def deleteTotp(accountId: Int, secret: String): Future[Boolean] =
    done(totps.get(accountId) match {
      case Some(t) if t.enabled && t.secret == secret =>
        totps.remove(accountId)
        true
      case _ => false
    })

  override def useTotpStep(accountId: Int, step: Long): Future[Boolean] =
    done(totps.get(accountId) match {
      case Some(t) if t.lastUsedStep < step =>
        totps(accountId) = t.copy(lastUsedStep = step)
        true
      case _ => false
    })
}
