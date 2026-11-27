package net.psforever.api

import java.nio.charset.StandardCharsets
import java.security.{MessageDigest, SecureRandom}
import java.util.Base64

import com.github.t3hnar.bcrypt._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * What a route may require of its caller. Every route names the permission it needs, so finer roles can
  * be added later without touching the routes.
  */
sealed abstract class Permission(val name: String)

object Permission {

  /** Any signed-in account. */
  case object SignedIn extends Permission("signed-in")

  /** A GM whose session carries GM rights; see [[Caller.gmActive]]. */
  case object Gm extends Permission("gm")
}

/**
  * The signed-in caller of one request.
  *
  * @param gmActive whether this session may use GM rights: the account is a GM now, was a GM when the
  *                 session was issued (so a newly promoted GM signs in again), and, if GMs must pass a
  *                 TOTP check, this session passed one
  */
final case class Caller(account: ApiAccount, session: StoredSession, gmActive: Boolean) {
  def has(permission: Permission): Boolean =
    permission match {
      case Permission.SignedIn => true
      case Permission.Gm       => gmActive
    }
}

/** What `/v1/me` reports. */
final case class Me(
    id: Int,
    username: String,
    gm: Boolean,
    gmActive: Boolean,
    totpEnabled: Boolean,
    totpEnrollmentRequired: Boolean
)

/** A started TOTP enrolment: the secret to type in, and the same as a URI for a QR code. */
final case class TotpEnrollment(secret: String, provisioningUri: String)

sealed trait SignInResult

object SignInResult {
  final case class SignedIn(token: String, session: StoredSession) extends SignInResult

  /** Unknown username, wrong password or banned account; deliberately indistinguishable. */
  case object InvalidCredentials extends SignInResult

  /** The password was right, but the account uses TOTP and no code was sent. */
  case object TotpRequired extends SignInResult

  /** The password was right, but the code was wrong or already used. */
  case object TotpInvalid extends SignInResult

  /** Too many password checks are already running; try again shortly. */
  case object Busy extends SignInResult
}

/**
  * @param idleTimeoutMs  a session ends after this long unused
  * @param maxLifetimeMs  and never outlives this long after sign-in
  * @param gmTotpRequired whether a GM session carries GM rights only after a TOTP check at sign-in
  * @param totpIssuer     the name authenticator apps show beside the code
  * @param bcryptRounds   the cost of the stand-in hash checked for unknown usernames; the game login's
  *                       cost, so a missing account takes as long to refuse as a wrong password
  */
final case class SessionSettings(
    idleTimeoutMs: Long,
    maxLifetimeMs: Long,
    gmTotpRequired: Boolean,
    totpIssuer: String,
    bcryptRounds: Int = 12
)

/**
  * Sign-in sessions issued by this server (decisions Q3 and Q7 to Q12).
  *
  * A session token is 32 random bytes; only its SHA-256 is stored. Every request re-reads the account, so
  * a change made anywhere, including by the 1.x portal writing to the database directly, takes effect at
  * once: a ban, a password change (detected through a fingerprint of the password hash) or losing GM ends
  * every session the account has. Sessions also end after `idleTimeoutMs` unused and `maxLifetimeMs` after
  * sign-in.
  *
  * @param clock epoch milliseconds, replaceable in tests
  */
final class SessionService(
    store: ApiStore,
    passwords: PasswordChecks,
    settings: SessionSettings,
    clock: () => Long = () => System.currentTimeMillis(),
    random: SecureRandom = new SecureRandom()
)(implicit ec: ExecutionContext) {
  import SessionService._

  /** Checked in place of a real hash when the username is unknown, so the refusal takes as long. */
  private val standInHash: String = "psf-http-api-stand-in".bcryptBounded(settings.bcryptRounds)

  def signIn(username: String, password: String, totpCode: Option[String]): Future[SignInResult] =
    store
      .findAccount(username)
      .flatMap { found =>
        val hash = found.map(_.passhash).filter(_.nonEmpty).getOrElse(standInHash)
        passwords.run(Try(password.isBcryptedBounded(hash)).getOrElse(false)).flatMap { matches =>
          found match {
            case Some(account) if matches && !account.inactive && account.passhash.nonEmpty =>
              secondFactor(account, totpCode)
            case _ =>
              Future.successful(SignInResult.InvalidCredentials)
          }
        }
      }
      .recover { case PasswordChecks.Busy => SignInResult.Busy }

  private def secondFactor(account: ApiAccount, totpCode: Option[String]): Future[SignInResult] =
    store.totp(account.id).flatMap {
      case Some(totp) if totp.enabled =>
        totpCode match {
          case None => Future.successful(SignInResult.TotpRequired)
          case Some(code) =>
            useCode(totp, code).flatMap {
              case true  => issue(account, mfa = true)
              case false => Future.successful(SignInResult.TotpInvalid)
            }
        }
      case _ => issue(account, mfa = false)
    }

  /** Check a code against a stored secret and record its step, refusing a replay. */
  private def useCode(totp: StoredTotp, code: String): Future[Boolean] =
    Base32.decode(totp.secret).flatMap(Totp.verify(_, code, clock() / 1000L, totp.lastUsedStep)) match {
      case Some(step) => store.useTotpStep(totp.accountId, step)
      case None       => Future.successful(false)
    }

  private def issue(account: ApiAccount, mfa: Boolean): Future[SignInResult] = {
    val bytes = new Array[Byte](TokenBytes)
    random.nextBytes(bytes)
    val token   = Base64.getUrlEncoder.withoutPadding.encodeToString(bytes)
    val now     = clock()
    val session = StoredSession(digest(token), account.id, now, now, fingerprint(account), account.gm, mfa)
    store.createSession(session).map(_ => SignInResult.SignedIn(token, session))
  }

  /** The caller a bearer token belongs to, if its session is still valid. */
  def authenticate(token: String): Future[Option[Caller]] =
    if (!TokenPattern.matches(token)) Future.successful(None)
    else {
      val tokenDigest = digest(token)
      store.session(tokenDigest).flatMap {
        case None => Future.successful(None)
        case Some((session, account)) =>
          val now = clock()
          val revoked =
            account.inactive || session.credentialFingerprint != fingerprint(account) ||
              (session.gmAtIssue && !account.gm)
          val expired =
            now - session.lastSeenAtMs >= settings.idleTimeoutMs ||
              now - session.createdAtMs >= settings.maxLifetimeMs
          if (revoked) store.deleteAccountSessions(account.id).map(_ => None)
          else if (expired) store.deleteSession(tokenDigest).map(_ => None)
          else {
            val touched = now - session.lastSeenAtMs >= TouchIntervalMs
            val touch   = if (touched) store.touchSession(tokenDigest, now) else Future.unit
            touch.map { _ =>
              val current  = if (touched) session.copy(lastSeenAtMs = now) else session
              val gmActive = account.gm && session.gmAtIssue && (!settings.gmTotpRequired || session.mfa)
              Some(Caller(account, current, gmActive))
            }
          }
      }
    }

  /** When a session expires if it goes unused from now on, in epoch milliseconds. */
  def expiresAt(session: StoredSession): Long =
    math.min(session.lastSeenAtMs + settings.idleTimeoutMs, session.createdAtMs + settings.maxLifetimeMs)

  def signOut(token: String): Future[Unit] =
    if (TokenPattern.matches(token)) store.deleteSession(digest(token)) else Future.unit

  def signOutEverywhere(caller: Caller): Future[Unit] = store.deleteAccountSessions(caller.account.id)

  def me(caller: Caller): Future[Me] =
    store.totp(caller.account.id).map { totp =>
      val enabled = totp.exists(_.enabled)
      Me(
        caller.account.id,
        caller.account.username,
        caller.account.gm,
        caller.gmActive,
        enabled,
        totpEnrollmentRequired = caller.account.gm && settings.gmTotpRequired && !enabled
      )
    }

  /** Start enrolling in TOTP, replacing any unconfirmed secret; None if TOTP is already on. */
  def beginTotp(caller: Caller): Future[Option[TotpEnrollment]] =
    store.totp(caller.account.id).flatMap {
      case Some(totp) if totp.enabled => Future.successful(None)
      case _ =>
        val secret = Totp.newSecret(random)
        val text   = Base32.encode(secret)
        store
          .savePendingTotp(caller.account.id, text)
          .map(_ =>
            Some(TotpEnrollment(text, Totp.provisioningUri(settings.totpIssuer, caller.account.username, secret)))
          )
    }

  /**
    * Confirm enrolment with a first code. This session then counts as TOTP-checked, so a GM does not have
    * to sign in again.
    */
  def confirmTotp(caller: Caller, code: String): Future[Boolean] =
    store.totp(caller.account.id).flatMap {
      case Some(totp) if !totp.enabled =>
        Base32.decode(totp.secret).flatMap(Totp.verify(_, code, clock() / 1000L, totp.lastUsedStep)) match {
          case Some(step) =>
            for {
              _ <- store.enableTotp(caller.account.id, step)
              _ <- store.setSessionMfa(caller.session.tokenDigest, mfa = true)
            } yield true
          case None => Future.successful(false)
        }
      case _ => Future.successful(false)
    }

  /** Turn TOTP off with a current code. No session counts as TOTP-checked afterwards. */
  def disableTotp(caller: Caller, code: String): Future[Boolean] =
    store.totp(caller.account.id).flatMap {
      case Some(totp) if totp.enabled =>
        useCode(totp, code).flatMap {
          case true =>
            for {
              _ <- store.deleteTotp(caller.account.id)
              _ <- store.clearAccountMfa(caller.account.id)
            } yield true
          case false => Future.successful(false)
        }
      case _ => Future.successful(false)
    }

  /** Delete sessions that have expired; returns how many. */
  def purgeExpired(): Future[Long] = {
    val now = clock()
    store.deleteExpiredSessions(now - settings.idleTimeoutMs, now - settings.maxLifetimeMs)
  }
}

object SessionService {

  /** Random bytes per token: 256 bits. */
  val TokenBytes = 32

  /** A session's last-seen time is written at most this often, not on every request. */
  val TouchIntervalMs: Long = 60L * 1000L

  /** What a token looks like: unpadded base64url of [[TokenBytes]] bytes. */
  private val TokenPattern = "[A-Za-z0-9_-]{43}".r

  def digest(text: String): String =
    MessageDigest
      .getInstance("SHA-256")
      .digest(text.getBytes(StandardCharsets.UTF_8))
      .map(b => f"${b & 0xff}%02x")
      .mkString

  /**
    * Changes whenever the account's password hash changes, which ends its sessions. Only `passhash` counts:
    * the launcher's `password` column is filled in by the game login the first time an account logs in,
    * which is not a password change.
    */
  def fingerprint(account: ApiAccount): String = digest(account.passhash)
}
