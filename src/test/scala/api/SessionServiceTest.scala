// Copyright (c) 2026 PSForever
package api

import com.github.t3hnar.bcrypt._
import net.psforever.api._
import org.specs2.mutable._
import org.specs2.specification.AfterAll

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class SessionServiceTest extends Specification with AfterAll {
  // specs2 runs the examples of a specification in parallel and most of them check a password through this
  // pool, so it needs room for all of them at once. The refusal when the pool is full is tested on a pool
  // of one thread and no queue of its own.
  private val passwords = new PasswordChecks(4, 64)

  override def afterAll(): Unit = passwords.shutdown()

  private val Day  = 24L * 60L * 60L * 1000L
  private val Week = 7L * Day

  private def await[T](f: Future[T]): T = Await.result(f, 10.seconds)

  private final class Fixture(gmTotpRequired: Boolean = false) {
    var now: Long = 1700000000000L
    val store     = new InMemoryApiStore
    val service = new SessionService(
      store,
      passwords,
      SessionSettings(Day, Week, gmTotpRequired, "PSForever", bcryptRounds = 4),
      () => now
    )

    def signIn(username: String, password: String, totp: Option[String] = None): SignInResult =
      await(service.signIn(username, password, totp))

    def token(username: String, password: String, totp: Option[String] = None): String =
      signIn(username, password, totp) match {
        case SignInResult.SignedIn(token, _) => token
        case other                           => throw new AssertionError(s"sign-in failed: $other")
      }

    def caller(token: String): Option[Caller] = await(service.authenticate(token))

    def advance(ms: Long): Unit = now += ms

    /** The code an authenticator app would show now for this account's stored secret. */
    def code(accountId: Int, stepOffset: Long = 0): String =
      Totp.code(Base32.decode(store.totps(accountId).secret).get, Totp.stepAt(now / 1000L) + stepOffset)

    def enrol(token: String): Unit = {
      val c = caller(token).get
      await(service.beginTotp(c)) must beSome
      await(service.confirmTotp(c, code(c.account.id))) must beTrue
    }
  }

  "signing in" should {
    "issue a session for the right password, storing only the token's digest" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val token = f.token("player", "hunter2")
      f.store.sessions.keys must contain(exactly(SessionService.digest(token)))
      f.store.sessions.values.exists(_.tokenDigest == token) must beFalse
      f.caller(token).map(_.account.username) must beSome("player")
    }

    "match the username regardless of case, as the game login does" in {
      val f = new Fixture()
      f.store.addAccount(1, "Player", "hunter2")
      f.signIn("player", "hunter2") must beAnInstanceOf[SignInResult.SignedIn]
    }

    "refuse a wrong password, an unknown username and a banned account identically" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      f.store.addAccount(2, "banned", "hunter2")
      f.store.update(2)(_.copy(inactive = true))
      Seq(f.signIn("player", "wrong"), f.signIn("nobody", "hunter2"), f.signIn("banned", "hunter2")) mustEqual
        Seq.fill(3)(SignInResult.InvalidCredentials)
      f.store.sessions must beEmpty
    }

    "refuse an account with no password hash" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      f.store.update(1)(_.copy(passhash = ""))
      f.signIn("player", "") mustEqual SignInResult.InvalidCredentials
    }

    "report Busy when the password-check threads are full" in {
      val busy  = new PasswordChecks(1, 0)
      val store = new InMemoryApiStore
      store.addAccount(1, "player", "hunter2")
      val service = new SessionService(store, busy, SessionSettings(Day, Week, gmTotpRequired = false, "PSF", 4))
      val gate    = new java.util.concurrent.CountDownLatch(1)
      try {
        busy.run(gate.await())
        Thread.sleep(50)
        await(service.signIn("player", "hunter2", None)) mustEqual SignInResult.Busy
      } finally {
        gate.countDown()
        busy.shutdown()
      }
    }
  }

  "a session" should {
    "end after a day unused" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val token = f.token("player", "hunter2")
      f.advance(Day - 1000)
      f.caller(token) must beSome
      f.advance(Day)
      f.caller(token) must beNone
      f.store.sessions must beEmpty
    }

    "stay alive while used, but never past seven days" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val token = f.token("player", "hunter2")
      // Used every 23 h 59 min, it is never idle for a day...
      (1 to 7).foreach { _ =>
        f.advance(Day - 60000)
        f.caller(token) must beSome
      }
      // ...but once seven days have passed since sign-in it ends anyway.
      f.advance(7 * 60000)
      f.caller(token) must beNone
    }

    "end, with every other session of the account, when the account is banned" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val (a, b) = (f.token("player", "hunter2"), f.token("player", "hunter2"))
      f.store.update(1)(_.copy(inactive = true))
      f.caller(a) must beNone
      f.store.sessionsOf(1) must beEmpty
      f.caller(b) must beNone
    }

    "end when the password changes" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val token = f.token("player", "hunter2")
      f.store.update(1)(_.copy(passhash = "correct horse".bcryptBounded(4)))
      f.caller(token) must beNone
    }

    "end the sessions signed in before a password change without ending the ones signed in after it" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val (first, second) = (f.token("player", "hunter2"), f.token("player", "hunter2"))
      f.store.update(1)(_.copy(passhash = "correct horse".bcryptBounded(4)))
      val fresh = f.token("player", "correct horse")
      // The old tab's request ends both of its own sessions...
      f.caller(first) must beNone
      f.caller(second) must beNone
      // ...and never the one signed in with the new password, however often that old token comes back.
      f.caller(fresh).map(_.account.username) must beSome("player")
      f.caller(first) must beNone
      f.caller(fresh).map(_.account.username) must beSome("player")
    }

    "end only the stale token that was presented, leaving other stale sessions alone" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val stale = f.token("player", "hunter2")
      // A session issued against an older credential must not be swept up by this token's request.
      val other = SessionService.digest("other")
      f.store.sessions(other) = StoredSession(other, 1, f.now, f.now, "0" * 64, gmAtIssue = false, mfa = false)
      f.store.update(1)(_.copy(passhash = "correct horse".bcryptBounded(4)))
      f.caller(stale) must beNone
      f.store.sessions.contains(other) must beTrue
    }

    "end a session that loses GM rights without ending a session of the same account that never had them" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val plain = f.token("player", "hunter2")
      f.store.update(1)(_.copy(gm = true))
      val gm = f.token("player", "hunter2")
      f.store.update(1)(_.copy(gm = false))
      f.caller(gm) must beNone
      f.caller(plain).isDefined must beTrue
    }

    "survive the game login filling in the launcher password column" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val token = f.token("player", "hunter2")
      f.store.update(1)(_.copy(password = "launcher-hash"))
      f.caller(token) must beSome
    }

    "end when the account loses GM, and carry no GM rights for a GM promoted after sign-in" in {
      val f = new Fixture()
      f.store.addAccount(1, "gm", "hunter2", gm = true)
      f.store.addAccount(2, "player", "hunter2")
      val gm     = f.token("gm", "hunter2")
      val player = f.token("player", "hunter2")
      f.caller(gm).map(_.has(Permission.Gm)) must beSome(true)
      f.store.update(1)(_.copy(gm = false))
      f.caller(gm) must beNone
      f.store.update(2)(_.copy(gm = true))
      f.caller(player).map(_.has(Permission.Gm)) must beSome(false)
      f.caller(f.token("player", "hunter2")).map(_.has(Permission.Gm)) must beSome(true)
    }

    "sign out alone, or everywhere" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val (a, b, c) = (f.token("player", "hunter2"), f.token("player", "hunter2"), f.token("player", "hunter2"))
      await(f.service.signOut(a))
      (f.caller(a), f.caller(b).isDefined) mustEqual ((None, true))
      await(f.service.signOutEverywhere(f.caller(b).get))
      (f.caller(b), f.caller(c)) mustEqual ((None, None))
    }

    "refuse tokens that aren't shaped like one without asking the database" in {
      val f = new Fixture()
      f.caller("short") must beNone
      f.caller("x" * 200) must beNone
    }

    "be purged once expired" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      f.token("player", "hunter2")
      f.advance(Day / 2)
      f.token("player", "hunter2")
      f.advance(Day / 2 + 1)
      await(f.service.purgeExpired()) mustEqual 1L
      f.store.sessions.size mustEqual 1
    }
  }

  "TOTP" should {
    "be asked for at sign-in once enrolled, and refuse a wrong or replayed code" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      f.enrol(f.token("player", "hunter2"))
      f.advance(31000)
      f.signIn("player", "hunter2") mustEqual SignInResult.TotpRequired
      f.signIn("player", "hunter2", Some("000000")) mustEqual SignInResult.TotpInvalid
      val code = f.code(1)
      f.signIn("player", "hunter2", Some(code)) must beAnInstanceOf[SignInResult.SignedIn]
      f.signIn("player", "hunter2", Some(code)) mustEqual SignInResult.TotpInvalid
    }

    "still refuse a wrong password before ever mentioning TOTP" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      f.enrol(f.token("player", "hunter2"))
      f.signIn("player", "wrong") mustEqual SignInResult.InvalidCredentials
    }

    "withhold GM rights from a GM who hasn't enrolled when it is required, and grant them on enrolment" in {
      val f = new Fixture(gmTotpRequired = true)
      f.store.addAccount(1, "gm", "hunter2", gm = true)
      val token = f.token("gm", "hunter2")
      f.caller(token).map(_.has(Permission.Gm)) must beSome(false)
      await(f.service.me(f.caller(token).get)).totpEnrollmentRequired must beTrue
      f.enrol(token)
      f.caller(token).map(_.has(Permission.Gm)) must beSome(true)
      await(f.service.me(f.caller(token).get)).totpEnrollmentRequired must beFalse
    }

    "grant GM rights without TOTP when it isn't required" in {
      val f = new Fixture(gmTotpRequired = false)
      f.store.addAccount(1, "gm", "hunter2", gm = true)
      f.caller(f.token("gm", "hunter2")).map(_.has(Permission.Gm)) must beSome(true)
    }

    "refuse a second enrolment while one is on, and allow restarting an unconfirmed one" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val caller = f.caller(f.token("player", "hunter2")).get
      val first  = await(f.service.beginTotp(caller)).get
      val second = await(f.service.beginTotp(caller)).get
      first.secret must not(beEqualTo(second.secret))
      await(f.service.confirmTotp(caller, f.code(1))) must beTrue
      await(f.service.beginTotp(caller)) must beNone
    }

    "confirm an enrolment once, and not twice with the same code" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val caller = f.caller(f.token("player", "hunter2")).get
      await(f.service.beginTotp(caller)) must beSome
      await(f.service.confirmTotp(caller, f.code(1))) must beTrue
      f.store.totps(1).enabled must beTrue
      // A second request that read the row before the first one enabled it confirms nothing, and grants
      // this session no fresh TOTP check.
      await(f.service.confirmTotp(caller, f.code(1))) must beFalse
    }

    "not replace an enrolment that another request has already confirmed" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val caller = f.caller(f.token("player", "hunter2")).get
      await(f.service.beginTotp(caller)) must beSome
      await(f.service.confirmTotp(caller, f.code(1))) must beTrue
      // A begin whose read saw a pending row must not turn the confirmed secret back off.
      await(f.store.savePendingTotp(1, "JBSWY3DPEHPK3PXP")) must beFalse
      f.store.totps(1).enabled must beTrue
      await(f.service.beginTotp(caller)) must beNone
    }

    "enable only the pending secret that was checked, and delete only the enabled one" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val caller = f.caller(f.token("player", "hunter2")).get
      val first  = await(f.service.beginTotp(caller)).get
      val second = await(f.service.beginTotp(caller)).get
      second.secret must not(beEqualTo(first.secret))
      val step = Totp.stepAt(f.now / 1000L)
      // A confirm whose read is overtaken by a restarted enrolment has to fail rather than enable a secret
      // nobody verified, which would leave the app and the database holding different secrets.
      await(f.store.enableTotp(1, first.secret, step)) must beFalse
      f.store.totps(1).enabled must beFalse
      await(f.store.enableTotp(1, second.secret, step)) must beTrue
      await(f.store.enableTotp(1, second.secret, step + 1)) must beFalse
      await(f.store.deleteTotp(1, first.secret)) must beFalse
      f.store.totps(1).enabled must beTrue
      await(f.store.deleteTotp(1, second.secret)) must beTrue
      f.store.totps must beEmpty
    }

    "mark a session checked only while the secret is still enabled" in {
      val f = new Fixture()
      f.store.addAccount(1, "player", "hunter2")
      val late       = f.token("player", "hunter2")
      val caller     = f.caller(f.token("player", "hunter2")).get
      val enrollment = await(f.service.beginTotp(caller)).get
      await(f.service.confirmTotp(caller, f.code(1))) must beTrue
      // A disable between the confirmation and the session write must leave the other session unchecked.
      await(f.store.deleteTotp(1, enrollment.secret)) must beTrue
      await(f.store.setSessionMfaIfTotpEnabled(SessionService.digest(late), 1, enrollment.secret)) must beFalse
      f.store.sessions(SessionService.digest(late)).mfa must beFalse
    }

    "turn off only with a current code, and leave no session counting as checked" in {
      val f = new Fixture(gmTotpRequired = true)
      f.store.addAccount(1, "gm", "hunter2", gm = true)
      val token = f.token("gm", "hunter2")
      f.enrol(token)
      f.advance(31000)
      val caller = f.caller(token).get
      await(f.service.disableTotp(caller, "000000")) must beFalse
      await(f.service.disableTotp(caller, f.code(1))) must beTrue
      f.store.totps must beEmpty
      f.caller(token).map(_.has(Permission.Gm)) must beSome(false)
    }
  }
}
