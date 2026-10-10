// Copyright (c) 2025 PSForever
package net.psforever.actors.api

import net.psforever.packet.{
  ControlPacketOpcode,
  GamePacketOpcode,
  PacketHelpers,
  PlanetSideControlPacket,
  PlanetSideCryptoPacket,
  PlanetSideGamePacket,
  PlanetSidePacket,
  PlanetSideResetSequencePacket
}
import scodec.Attempt
import scodec.bits.{BitVector, ByteVector}

/**
  * One field of a decoded packet, as the class that decoded it declares it.
  *
  * @param name      the field's name in the case class
  * @param scalaType its declared type, as far as a runtime value reveals it
  * @param value     this specimen's value, rendered
  */
case class PacketField(name: String, scalaType: String, value: String)

/**
  * One packet kept for review: what arrived on the wire, and what the server made of it.
  *
  * @param id        monotonic within a run; also the cursor viewers read forward from
  * @param at        when it was seen (epoch millis)
  * @param space     which opcode table names it -- `game` or `control`
  * @param opcode    the opcode's number, 0-255
  * @param name      the opcode's name in that table
  * @param label     `name (opcode)`, the one rendering the server and the portal both use
  * @param tier      `control`, `plaintext` or `encrypted`; see [[PacketCapture.Tiers]]
  * @param direction `in` (client to server) or `out`
  * @param session   the connection it belongs to, so one client's traffic can be read on its own
  * @param length    payload length in bytes, BEFORE any truncation applied to `hex`
  * @param hex       the raw payload, opcode byte first, capped at [[PacketCapture.MaxHexBytes]]
  * @param truncated whether `hex` is shorter than `length` because of that cap
  * @param decoded   the packet as the server's own class renders it, when a layout exists
  * @param error     why decoding did not happen, when it did not
  * @param charted   whether a layout for this opcode exists at all
  * @param fields    the decoded packet's own fields, when it has a class to have any
  */
case class CapturedPacket(
    id: Long,
    at: Long,
    space: String,
    opcode: Int,
    name: String,
    label: String,
    tier: String,
    direction: String,
    session: String,
    length: Int,
    hex: String,
    truncated: Boolean,
    decoded: Option[String],
    error: Option[String],
    charted: Boolean,
    fields: Option[List[PacketField]]
)

/** One opcode as offered to the admin choosing what to capture. */
case class OpcodeEntry(
    space: String,
    opcode: Int,
    name: String,
    label: String,
    charted: Boolean,
    redacted: Boolean,
    armed: Boolean
)

/** One admin watching the capture, and what they asked to see. */
case class CaptureViewer(
    viewer: String,
    who: String,
    joinedAt: Long,
    expiresAt: Long,
    game: Set[Int],
    control: Set[Int]
)

/** The shared capture as it stands: who is watching, what that adds up to, and how it is doing. */
case class CaptureState(
    active: Boolean,
    startedAt: Long,
    watchers: Seq[String],
    game: Set[Int],
    control: Set[Int],
    seen: Long,
    dropped: Long,
    retained: Int,
    oldestId: Long,
    newestId: Long
)

/**
  * Which opcodes are being captured, and the recent specimens every watching admin reads from.
  *
  * ==Nothing is captured unless somebody is watching==
  * Capture belongs to the set of admins currently sitting on the Packet Review page with collection
  * running. Each one keeps its place by saying so periodically; stop collecting, close the tab, lose
  * the network, and that watcher lapses, and when the last watcher lapses capture stops and the
  * buffer is dropped. There is deliberately no way to arm capture and walk away, because the failure
  * mode of that is a world server quietly accumulating player traffic for as long as nobody notices.
  *
  * ==One capture, many readers==
  * Two admins watching the same opcode do not cause it to be captured twice. There is a single
  * buffer; watchers read forward through it by [[CapturedPacket.id]], each holding its own cursor, so
  * nobody's read consumes anybody else's data and the cost of a second watcher is a second cursor
  * rather than a second copy. What is armed is the *union* of what the watchers asked for, and a
  * watcher only reads back the opcodes it asked for -- so one admin widening their selection cannot
  * quietly fill another's table with traffic they did not choose.
  *
  * ==The buffer is a window, not an archive==
  * At most [[MaxRetained]] specimens are held, oldest evicted first, each capped at
  * [[MaxHexBytes]]. A watcher that stops reading falls off the back of the window and is told so
  * (its cursor is older than `oldestId`) rather than silently missing packets. The page keeps the
  * history it has collected and exports from that; the server keeps only enough for a reader to
  * catch up over a normal polling interval.
  *
  * ==What can never be captured==
  * [[Redacted]] lists the game opcodes carrying a credential. `LoginMessage` carries the account
  * password; `LoginRespMessage` and `ConnectToWorldRequestMessage` carry the session token, which
  * logs a client in *without* the password and so is worth exactly as much. Arming them is not a
  * setting an admin can reach and get wrong -- [[subscribe]] drops them from whatever it is handed
  * and reports them back, and [[armedFor]] refuses them a second time on the packet path, so nothing
  * short of editing this list puts a credential in the buffer.
  *
  * ==Cost to the running game==
  * This sits on the packet path of a live world server, so the cost of it being switched off is the
  * number that matters: one volatile boolean read and a branch that is not taken. Nothing is
  * allocated, no lock is taken, no clock is read, and the packet is not rendered or re-encoded.
  *
  * With capture running, an unarmed opcode costs that plus one array load. An armed one is bounded
  * three ways so that arming something chatty cannot degrade play: [[MaxPerSecond]] caps how many
  * packets per second may be rendered at all, [[MaxHexBytes]] caps how much of any one packet is
  * kept, and [[MaxRetained]] caps how many are held. Work refused by the budget is counted as
  * dropped and reported, so the page says it is sampling rather than appearing to show everything.
  *
  * ==Concurrency==
  * The packet path reads only `@volatile` values that are replaced wholesale, so reads are lock-free
  * and always see a consistent snapshot. The watcher list takes a lock, but only on subscribe, leave
  * and sweep -- never per packet. The buffer takes a lock only for packets that are actually being
  * kept, and holds it for an append and at most one eviction.
  */
object PacketCapture {

  /** The two opcode tables a captured packet can be named by. */
  object Spaces {
    val Game    = "game"
    val Control = "control"
  }

  /**
    * How a packet is grouped in the review UI.
    *
    * `Control` is the transport itself -- bundling, sequencing, the crypto handshake -- and is a
    * different concern from anything the game says. The remaining two split the game packets by
    * whether they were encrypted on the wire, which is the honest division: everything before the
    * crypto handshake completes is readable by anyone watching the connection, and everything after
    * it is not.
    */
  object Tiers {
    val Control   = "control"
    val Plaintext = "plaintext"
    val Encrypted = "encrypted"
  }

  /** Game opcodes carrying a password or a session token. Never capturable. */
  val Redacted: Map[Int, String] = Map(
    0x01 -> "carries the account password",
    0x02 -> "carries the session token issued at login",
    0x03 -> "carries the session token used to reach the world"
  )

  /** Most of a packet is enough to read its shape; a stray huge one should not be held whole. */
  val MaxHexBytes: Int = 2048

  /** How many specimens the shared window holds. Enough for a reader to catch up, no more. */
  val MaxRetained: Int = 1000

  /**
    * The most packets that may be rendered for review in any one second, across the whole server.
    *
    * The point of this is that an admin cannot accidentally make the game worse. Arm
    * `PlayerStateMessage` on a busy continent and the honest rate is thousands per second, each one
    * costing a `toString` of a case class and a hex rendering -- work done on the connection's own
    * thread, ahead of the packet reaching the session. Past this many in a second the rest are
    * skipped without being rendered at all, and counted, so what the page shows is a sample and says
    * so. Reviewing a packet layout needs specimens, not every specimen.
    */
  val MaxPerSecond: Int = 200

  /**
    * How long a watcher outlives its last sign of life.
    *
    * Long enough that an ordinary collection interval (a few seconds) never trips it, short enough
    * that a closed tab stops capture in about the time it takes to notice. The page also says when it
    * is leaving -- this is the backstop for when it cannot.
    */
  val LeaseMillis: Long = 20000L

  private val counter = new java.util.concurrent.atomic.AtomicLong(0L)

  private val watchers = scala.collection.mutable.LinkedHashMap[String, CaptureViewer]()
  private val buffer   = scala.collection.mutable.ArrayDeque[CapturedPacket]()

  /**
    * ==The packet path==
    * Everything below is read for every packet the server handles, on every connection, so its cost
    * when nobody is capturing is the thing that actually matters -- and when nobody is capturing that
    * cost is one volatile boolean read and a not-taken branch.
    *
    * [[capturing]] is that boolean. It exists so the common case never has to ask a collection
    * anything: `armedGame.isEmpty && armedControl.isEmpty` would have been two volatile reads and two
    * virtual calls to answer a question whose answer is nearly always "no".
    *
    * When capture IS running, [[gameArmed]] / [[controlArmed]] answer "is this opcode wanted" with an
    * array load rather than a hash lookup. An opcode is a byte, so a 256-entry mask covers the space
    * exactly, with no hashing and no boxing of the `Int`. Both are immutable once published and
    * replaced wholesale, so a reader sees one complete mask or the previous one.
    */
  @volatile private var capturing: Boolean = false

  private val NoneArmed: Array[Boolean] = new Array[Boolean](256)

  @volatile private var gameArmed: Array[Boolean]    = NoneArmed
  @volatile private var controlArmed: Array[Boolean] = NoneArmed

  @volatile private var startedAt: Long = 0L

  @volatile private var seenCount: Long    = 0L
  @volatile private var droppedCount: Long = 0L

  // The budget window: capture never costs the server more than MaxPerSecond packets' worth of work,
  // whatever gets armed. Guarded by the buffer lock, checked before any rendering is done.
  private var windowSecond: Long = 0L
  private var windowCount: Int   = 0

  /** Build a 256-entry mask from the union of what the watchers asked for. */
  private def maskOf(sets: Iterable[Set[Int]]): Array[Boolean] = {
    val mask  = new Array[Boolean](256)
    var armed = false
    sets.foreach(_.foreach { o =>
      if (o >= 0 && o < 256) { mask(o) = true; armed = true }
    })
    if (armed) mask else NoneArmed
  }

  /** Recompute the masks. Caller holds the watchers lock. */
  private def refreshArmed(): Unit = {
    val g = maskOf(watchers.values.map(_.game))
    val c = maskOf(watchers.values.map(_.control))
    gameArmed = g
    controlArmed = c
    // Published last, so a reader that sees `capturing` also sees the masks behind it.
    capturing = (g ne NoneArmed) || (c ne NoneArmed)
    if (watchers.isEmpty) {
      // The last watcher went away: capture stops, and what nobody collected goes with it.
      buffer.synchronized { buffer.clear() }
      counter.set(0L)
      startedAt = 0L
      seenCount = 0L
      droppedCount = 0L
    } else if (startedAt == 0L) {
      startedAt = System.currentTimeMillis()
    }
  }

  /** Drop watchers whose lease has run out. Caller holds the watchers lock. */
  private def sweepLocked(now: Long): Boolean = {
    val lapsed = watchers.collect { case (k, v) if v.expiresAt <= now => k }.toVector
    if (lapsed.isEmpty) false
    else {
      lapsed.foreach(watchers.remove)
      refreshArmed()
      true
    }
  }

  /**
    * True when nothing is being captured. The first thing the packet path asks, and on a server with
    * nobody on the Packet Review page it is the only thing: one volatile read, then nothing.
    */
  def idle: Boolean = !capturing

  /** Whether this opcode in this space is being captured. Also the second refusal of [[Redacted]]. */
  def armedFor(space: String, opcode: Int): Boolean =
    capturing && opcode >= 0 && opcode < 256 && (space match {
      case Spaces.Game    => gameArmed(opcode) && !Redacted.contains(opcode)
      case Spaces.Control => controlArmed(opcode)
      case _              => false
    })

  /** The armed opcodes, read back out of the masks for reporting. Not for the packet path. */
  private def armedSet(mask: Array[Boolean]): Set[Int] =
    (0 until 256).filter(mask(_)).toSet

  /**
    * Join the capture, or renew a place already held, with this watcher's selection.
    *
    * The page calls this both to start collecting and to keep collecting -- one call, carrying the
    * selection each time, so a selection changed in the UI takes effect on the next poll without a
    * separate round trip and without any chance of the two drifting apart.
    *
    * Redacted game opcodes are dropped rather than the call rejected: the portal offers them as
    * unselectable, so their presence here means a hand-written call, and the useful answer to that is
    * to arm everything else and say plainly what was refused.
    *
    * @param viewer stable id for this page (one browser tab), used as the cursor's owner
    * @param who    the admin's name, for the benefit of the other watchers
    * @return the shared state as it now stands, and the redacted opcodes that were dropped
    */
  def subscribe(viewer: String, who: String, game: Set[Int], control: Set[Int]): (CaptureState, Set[Int]) = {
    val refused = game.filter(Redacted.contains)
    val now     = System.currentTimeMillis()
    watchers.synchronized {
      sweepLocked(now)
      val existing = watchers.get(viewer)
      watchers.update(
        viewer,
        CaptureViewer(
          viewer = viewer,
          who = who,
          joinedAt = existing.map(_.joinedAt).getOrElse(now),
          expiresAt = now + LeaseMillis,
          game = (game -- refused).filter(o => o >= 0 && o <= 255),
          control = control.filter(o => o >= 0 && o <= 255)
        )
      )
      refreshArmed()
    }
    (state, refused)
  }

  /** Give up a place, promptly, rather than waiting for the lease to lapse. */
  def leave(viewer: String): Unit = watchers.synchronized {
    if (watchers.remove(viewer).isDefined) refreshArmed()
  }

  /** End the capture for everyone. */
  def stopAll(): Unit = watchers.synchronized {
    watchers.clear()
    refreshArmed()
  }

  /** The shared capture as it stands, with lapsed watchers already swept. */
  def state: CaptureState = {
    watchers.synchronized { sweepLocked(System.currentTimeMillis()) }
    val (retained, oldest, newest) = buffer.synchronized {
      (buffer.size, buffer.headOption.map(_.id).getOrElse(0L), buffer.lastOption.map(_.id).getOrElse(0L))
    }
    val people = watchers.synchronized { watchers.values.map(_.who).toVector }
    CaptureState(
      active = !idle,
      startedAt = startedAt,
      watchers = people,
      game = armedSet(gameArmed),
      control = armedSet(controlArmed),
      seen = seenCount,
      dropped = droppedCount,
      retained = retained,
      oldestId = oldest,
      newestId = newest
    )
  }

  /**
    * Specimens after `since`, oldest first, narrowed to what this watcher asked to see.
    *
    * Reading does not consume: every watcher walks the same buffer at its own pace, which is the
    * point -- two admins reviewing the same opcode share one capture rather than each provoking their
    * own. Pass `since = 0` to start from whatever the window still holds.
    *
    * A watcher whose cursor has fallen off the back of the window gets what remains; the `oldestId`
    * in [[state]] is how the page knows it missed something rather than guessing.
    */
  def since(viewer: String, cursor: Long, limit: Int): Seq[CapturedPacket] = {
    val selection = watchers.synchronized { watchers.get(viewer) }
    val wanted: CapturedPacket => Boolean = selection match {
      case Some(v) =>
        p =>
          (p.space == Spaces.Game && v.game.contains(p.opcode)) ||
            (p.space == Spaces.Control && v.control.contains(p.opcode))
      // Not a registered watcher: show nothing rather than everything. Reading is gated on having
      // joined, so a caller that never said what it wanted does not get to see what others chose.
      case None => _ => false
    }
    buffer.synchronized {
      buffer.iterator.filter(p => p.id > cursor && wanted(p)).take(math.max(1, limit)).toVector
    }
  }

  /**
    * Keep one packet, if its opcode is armed by somebody watching.
    *
    * Called from the packet path, so it does as little as possible when nobody is watching: `idle` is
    * a single volatile read, and `armedFor` adds a set lookup, both ahead of any hex rendering or
    * decoding. `decoded` and `error` are by-name for the same reason -- rendering a packet to a
    * string is not worth doing for traffic nobody asked to see.
    */
  def offer(
      space: String,
      opcode: Int,
      name: String,
      tier: String,
      direction: String,
      session: String,
      raw: ByteVector,
      decoded: => Option[String],
      error: => Option[String],
      charted: Boolean,
      fields: => Option[List[PacketField]]
  ): Unit = {
    if (idle || !armedFor(space, opcode)) return
    // Budget first: `decoded` and `error` are by-name, and `hex` is a fresh string twice the length
    // of the packet, so everything expensive about this method happens after this check.
    if (!withinBudget()) {
      droppedCount += 1
      return
    }
    val length = raw.size.toInt
    val kept   = if (length > MaxHexBytes) raw.take(MaxHexBytes.toLong) else raw
    val entry = CapturedPacket(
      id = counter.incrementAndGet(),
      at = System.currentTimeMillis(),
      space = space,
      opcode = opcode,
      name = name,
      label = s"$name ($opcode)",
      tier = tier,
      direction = direction,
      session = session,
      length = length,
      hex = kept.toHex,
      truncated = length > MaxHexBytes,
      decoded = decoded,
      error = error,
      charted = charted,
      fields = fields
    )
    var lost = 0L
    buffer.synchronized {
      buffer.append(entry)
      while (buffer.size > MaxRetained) {
        buffer.removeHead()
        lost += 1
      }
    }
    // Counting is best-effort: a lost increment matters far less than holding the packet path up.
    seenCount += 1
    if (lost > 0) droppedCount += lost
  }

  /**
    * Whether this second still has room to render a packet.
    *
    * Shares the buffer's lock rather than taking one of its own: anything that passes this check is
    * about to take that lock anyway, so this adds no new contention, and anything that fails it is
    * leaving immediately.
    */
  private def withinBudget(): Boolean = buffer.synchronized {
    val second = System.currentTimeMillis() / 1000L
    if (second != windowSecond) {
      windowSecond = second
      windowCount = 1
      true
    } else if (windowCount < MaxPerSecond) {
      windowCount += 1
      true
    } else {
      false
    }
  }

  /** A rendered field value never grows past this; some packets carry large nested structures. */
  private val MaxValueChars: Int = 200

  /** No packet has this many fields; the cap is only here so a pathological one cannot surprise us. */
  private val MaxFields: Int = 64

  /**
    * The fields of a decoded packet, taken from the case class that decoded it.
    *
    * Every packet class in the project is a case class, so it is a `Product`, and `productElementNames`
    * gives the declared field names -- generated by the compiler, not looked up reflectively, so this
    * costs a list traversal rather than a reflective call. Pairing those with `productIterator` gives
    * name, runtime type and value together.
    *
    * This is what lets Packet Review seed the field designer with a packet's REAL names instead of
    * `unk1, unk2, unk3`. What it cannot recover is bit widths: the widths live in the codec, not in
    * the class, and a `uint4` and a `uint16L` are both an `Int` by the time they reach here. So the
    * names and types are exact and the widths are the designer's problem -- which is the right split,
    * because the names are the part that is tedious to retype and easy to get wrong.
    *
    * Only the top level is walked. A nested structure reports its type and renders its value, which
    * is enough to tell you a composite is there without this trying to model the whole tree.
    */
  def fieldsOf(packet: Any): Option[List[PacketField]] = packet match {
    case p: Product =>
      val names  = p.productElementNames.toList
      val values = p.productIterator.toList
      // Belt and braces: these are the same arity for any case class, but a bespoke Product could
      // disagree, and half a field list would be worse than none.
      if (names.size != values.size || names.isEmpty) None
      else
        Some(
          names
            .zip(values)
            .take(MaxFields)
            .map { case (name, value) => PacketField(name, typeNameOf(value), renderValue(value)) }
        )
    case _ => None
  }

  /** The type of a runtime value, named the way the source would name it. */
  private def typeNameOf(value: Any): String = value match {
    case null       => "Null"
    case _: Boolean => "Boolean"
    case _: Int     => "Int"
    case _: Long    => "Long"
    case _: Float   => "Float"
    case _: Double  => "Double"
    case _: String  => "String"
    case Some(v)    => s"Option[${typeNameOf(v)}]"
    case None       => "Option"
    case xs: Seq[_] => s"Seq[${xs.headOption.map(typeNameOf).getOrElse("_")}]"
    case other      => other.getClass.getSimpleName.stripSuffix("$")
  }

  private def renderValue(value: Any): String = {
    val rendered = String.valueOf(value)
    if (rendered.length > MaxValueChars) rendered.take(MaxValueChars) + "…" else rendered
  }

  /** The opcode's name in the game table, or a placeholder for numbers the table does not assign. */
  def gameName(opcode: Int): String =
    if (opcode >= 0 && opcode < GamePacketOpcode.maxId) GamePacketOpcode(opcode).toString
    else s"Unassigned$opcode"

  /** The opcode's name in the control table, or a placeholder. */
  def controlName(opcode: Int): String =
    if (opcode >= 0 && opcode < ControlPacketOpcode.maxId) ControlPacketOpcode(opcode).toString
    else s"Unassigned$opcode"

  /**
    * Whether a layout exists for an opcode.
    *
    * Asked of the opcode table itself rather than kept as a second list beside it. `getPacketDecoder`
    * answers with a function, and the stub it answers with for an uncharted opcode fails with
    * [[PacketHelpers.NoDecoderMessage]] whatever it is fed -- so feeding it nothing separates "no
    * layout" from "a layout that wants more bytes than this". A list maintained by hand would drift
    * away from the table the first time somebody implemented a packet and forgot it existed.
    */
  private def gameCharted(opcode: Int): Boolean =
    opcode >= 0 && opcode < GamePacketOpcode.maxId && {
      GamePacketOpcode.getPacketDecoder(GamePacketOpcode(opcode))(BitVector.empty) match {
        case Attempt.Failure(err) => !PacketHelpers.isUnimplemented(err)
        case _                    => true
      }
    }

  private def controlCharted(opcode: Int): Boolean =
    opcode >= 0 && opcode < ControlPacketOpcode.maxId && {
      ControlPacketOpcode.getPacketDecoder(ControlPacketOpcode(opcode))(BitVector.empty) match {
        case Attempt.Failure(err) => !PacketHelpers.isUnimplemented(err)
        case _                    => true
      }
    }

  // Probing costs a failed decode per opcode, so the answers are worked out once and held. The armed
  // flag is read fresh each time, because that is the part that changes.
  private lazy val chartedGame: Map[Int, Boolean]    = (0 to 255).map(o => o -> gameCharted(o)).toMap
  private lazy val chartedControl: Map[Int, Boolean] = (0 to 255).map(o => o -> controlCharted(o)).toMap

  /** Every opcode in both tables, with its name and whether it is charted, armed or refused. */
  def catalogue: Seq[OpcodeEntry] = {
    val g = armedSet(gameArmed)
    val c = armedSet(controlArmed)
    val game = (0 to 255).map { o =>
      val n = gameName(o)
      OpcodeEntry(Spaces.Game, o, n, s"$n ($o)", chartedGame(o), Redacted.contains(o), g.contains(o))
    }
    val control = (0 to 255).map { o =>
      val n = controlName(o)
      OpcodeEntry(Spaces.Control, o, n, s"$n ($o)", chartedControl(o), redacted = false, c.contains(o))
    }
    game ++ control
  }

  /** Which tier a decoded packet belongs to, given whether its frame was encrypted. */
  def tierOf(packet: PlanetSidePacket, secured: Boolean): String = packet match {
    case _: PlanetSideControlPacket       => Tiers.Control
    case _: PlanetSideCryptoPacket        => Tiers.Control
    case _: PlanetSideResetSequencePacket => Tiers.Control
    case _: PlanetSideGamePacket          => if (secured) Tiers.Encrypted else Tiers.Plaintext
    case _                                => Tiers.Plaintext
  }
}
