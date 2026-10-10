package net.psforever.actors.api

import io.getquill.Query
import net.psforever.util.Database._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Who is calling the PSF-HTTP API, established from the caller's own session rather than asserted.
  *
  * The portal forwards the browser session id it already holds, as `X-PSF-Session`. This process owns
  * the session table, so it can resolve that id to an account itself and read the account's real
  * privileges out of the database. Nothing about the caller is taken on trust from a header: the
  * header carries an opaque id, and every fact about the identity behind it comes from a row here.
  *
  * That distinction is the whole point. `X-Admin-User` -- which the audit log still records -- is a
  * caller-supplied LABEL and always was; it says who the portal believes is acting, and a caller may
  * write anything in it. `X-PSF-Session` is a bearer credential: holding it is equivalent to holding
  * the browser cookie it came from, and it is worth exactly what that cookie is worth.
  *
  * An expired session resolves to nobody. The lookup filters on the expiry the same way the session
  * store's read does, so a session that has aged out cannot authorise anything even though its row is
  * still present until the next sweep.
  */
object CallerAuth {
  import ctx._

  /** The header carrying the caller's session id. */
  val SessionHeader: String = "X-PSF-Session"

  /**
    * An authenticated caller.
    *
    * @param accountId the account the session is bound to
    * @param username  its name, used to attribute audited actions to a VERIFIED identity
    * @param gm        whether that account holds game-master rights
    * @param inactive  whether the account is banned -- a banned account authenticates but may do nothing
    */
  case class Caller(accountId: Int, username: String, gm: Boolean, inactive: Boolean)

  private case class SessionAccount(account_id: Int, username: String, gm: Boolean, inactive: Boolean)

  /**
    * Resolve a session id to the account behind it, or None.
    *
    * One query rather than two: the session row is joined straight to its account, so a session
    * naming an account that has since been deleted resolves to nobody instead of a dangling id.
    *
    * `account_id` is read from the session JSON, which is written ONLY by this process -- see
    * [[PortalQueries.sessionSet]], which preserves the stored value and refuses to let a caller
    * assert one. Were that not true, this whole mechanism would reduce to trusting a header again.
    */
  def resolve(sessionId: String): Future[Option[Caller]] = {
    val q = quote(
      infix"""SELECT a.id AS account_id, a.username AS username, a.gm AS gm, a.inactive AS inactive
              FROM session s
              JOIN account a ON a.id = (s.sess ->> 'account_id')::int
              WHERE s.sid = ${lift(sessionId)} AND s.expire > NOW()""".as[Query[SessionAccount]]
    )
    ctx.run(q).map(_.headOption.map(r => Caller(r.account_id, r.username, r.gm, r.inactive)))
  }

  /**
    * Does this caller own the character?
    *
    * Asked of the database rather than inferred, because the portal must not be the one deciding.
    * A deleted character answers false: it is not "yours" in any sense a caller should be able to
    * read, and treating it as owned would expose a deleted character's inventory.
    */
  def ownsAvatar(accountId: Int, avatarId: Int): Future[Boolean] =
    PortalQueries.avatarOwner(avatarId).map {
      case row :: _ => row.account_id == accountId && !row.deleted
      case Nil      => false
    }
}
