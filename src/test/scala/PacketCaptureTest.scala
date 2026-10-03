// Copyright (c) 2025 PSForever
import net.psforever.actors.api.PacketCapture
import org.specs2.mutable._
import scodec.bits._

/**
  * The properties Packet Review depends on being true, rather than a tour of its methods.
  *
  * Three of these are load-bearing for reasons outside this file: capture must cost a live server
  * nothing when nobody is watching, it must stop on its own when the last watcher goes away, and it
  * must never keep a packet carrying a credential. The rest of the feature is an admin convenience;
  * these three are the ones that would be a problem to get wrong.
  *
  * Tests run in sequence because `PacketCapture` is a single shared object -- as it must be, since it
  * is read from every connection's packet path -- so its state is global.
  */
class PacketCaptureTest extends Specification {
  sequential

  private val Watcher = "test-viewer"

  private def offerOne(space: String, opcode: Int): Unit =
    PacketCapture.offer(
      space = space,
      opcode = opcode,
      name = "TestPacket",
      tier = PacketCapture.Tiers.Encrypted,
      direction = "in",
      session = "test",
      raw = hex"0102030405",
      decoded = Some("TestPacket(1,2,3)"),
      error = None,
      charted = true,
      fields = None
    )

  private def reset(): Unit = PacketCapture.stopAll()

  "Packet capture" should {

    "capture nothing at all until somebody is watching" in {
      reset()
      PacketCapture.idle mustEqual true
      PacketCapture.armedFor(PacketCapture.Spaces.Game, 8) mustEqual false
      offerOne(PacketCapture.Spaces.Game, 8)
      PacketCapture.state.retained mustEqual 0
    }

    "arm only what a watcher asked for" in {
      reset()
      PacketCapture.subscribe(Watcher, "tester", Set(8), Set.empty)
      PacketCapture.idle mustEqual false
      PacketCapture.armedFor(PacketCapture.Spaces.Game, 8) mustEqual true
      PacketCapture.armedFor(PacketCapture.Spaces.Game, 9) mustEqual false
      // Same number in the other opcode table is a different packet entirely.
      PacketCapture.armedFor(PacketCapture.Spaces.Control, 8) mustEqual false
      reset()
      ok
    }

    "never arm an opcode carrying a credential, however it is asked" in {
      reset()
      val (state, refused) = PacketCapture.subscribe(Watcher, "tester", PacketCapture.Redacted.keySet, Set.empty)
      refused mustEqual PacketCapture.Redacted.keySet
      state.game must beEmpty
      PacketCapture.Redacted.keys.foreach { opcode =>
        PacketCapture.armedFor(PacketCapture.Spaces.Game, opcode) mustEqual false
      }
      reset()
      ok
    }

    "refuse a credential opcode even when it is offered directly" in {
      reset()
      // Arming everything else must not sweep a redacted opcode along with it.
      PacketCapture.subscribe(Watcher, "tester", (0 to 255).toSet, Set.empty)
      PacketCapture.Redacted.keys.foreach { opcode =>
        PacketCapture.armedFor(PacketCapture.Spaces.Game, opcode) mustEqual false
        offerOne(PacketCapture.Spaces.Game, opcode)
      }
      PacketCapture.since(Watcher, 0L, 100).exists(p => PacketCapture.Redacted.contains(p.opcode)) mustEqual false
      reset()
      ok
    }

    "let several watchers read the same capture without consuming it" in {
      reset()
      PacketCapture.subscribe("a", "alice", Set(8), Set.empty)
      PacketCapture.subscribe("b", "bob", Set(8), Set.empty)
      offerOne(PacketCapture.Spaces.Game, 8)
      offerOne(PacketCapture.Spaces.Game, 8)

      val first  = PacketCapture.since("a", 0L, 100)
      val second = PacketCapture.since("b", 0L, 100)
      first.size mustEqual 2
      // Reading did not take them away from the other watcher.
      second.size mustEqual 2
      // Each cursor moves on its own.
      PacketCapture.since("a", first.last.id, 100) must beEmpty
      PacketCapture.since("b", 0L, 100).size mustEqual 2
      reset()
      ok
    }

    "arm the union of what the watchers asked for, and show each only its own" in {
      reset()
      PacketCapture.subscribe("a", "alice", Set(8), Set.empty)
      PacketCapture.subscribe("b", "bob", Set(9), Set.empty)
      PacketCapture.armedFor(PacketCapture.Spaces.Game, 8) mustEqual true
      PacketCapture.armedFor(PacketCapture.Spaces.Game, 9) mustEqual true

      offerOne(PacketCapture.Spaces.Game, 8)
      offerOne(PacketCapture.Spaces.Game, 9)

      // One admin widening their selection does not put traffic in another's table.
      PacketCapture.since("a", 0L, 100).map(_.opcode) mustEqual Seq(8)
      PacketCapture.since("b", 0L, 100).map(_.opcode) mustEqual Seq(9)
      reset()
      ok
    }

    "stop capturing, and drop what it held, when the last watcher leaves" in {
      reset()
      PacketCapture.subscribe("a", "alice", Set(8), Set.empty)
      PacketCapture.subscribe("b", "bob", Set(8), Set.empty)
      offerOne(PacketCapture.Spaces.Game, 8)
      PacketCapture.state.retained mustEqual 1

      PacketCapture.leave("a")
      // Still one watcher, so capture continues.
      PacketCapture.idle mustEqual false

      PacketCapture.leave("b")
      PacketCapture.idle mustEqual true
      PacketCapture.state.retained mustEqual 0
      offerOne(PacketCapture.Spaces.Game, 8)
      PacketCapture.state.retained mustEqual 0
      ok
    }

    "show nothing to a caller that never said what it wanted" in {
      reset()
      PacketCapture.subscribe("a", "alice", Set(8), Set.empty)
      offerOne(PacketCapture.Spaces.Game, 8)
      PacketCapture.since("someone-else", 0L, 100) must beEmpty
      reset()
      ok
    }

    "hold no more than the window, oldest first" in {
      reset()
      PacketCapture.subscribe(Watcher, "tester", Set(8), Set.empty)
      // More than the window, but within one second's render budget so the cap under test is the
      // window rather than the budget.
      val n = math.min(PacketCapture.MaxRetained + 50, PacketCapture.MaxPerSecond)
      (1 to n).foreach(_ => offerOne(PacketCapture.Spaces.Game, 8))
      PacketCapture.state.retained must beLessThanOrEqualTo(PacketCapture.MaxRetained)
      reset()
      ok
    }

    "never render more than the per-second budget" in {
      reset()
      PacketCapture.subscribe(Watcher, "tester", Set(8), Set.empty)
      val attempts = PacketCapture.MaxPerSecond * 3
      (1 to attempts).foreach(_ => offerOne(PacketCapture.Spaces.Game, 8))
      val state = PacketCapture.state
      // Whatever arrived, the work actually done is bounded -- which is what keeps an armed chatty
      // opcode off the critical path for players.
      state.seen must beLessThanOrEqualTo(PacketCapture.MaxPerSecond.toLong * 2L)
      state.dropped must beGreaterThan(0L)
      reset()
      ok
    }

    "know which opcodes have a layout and which do not" in {
      // Opcode 8 is PlayerStateMessage, which is implemented; 0 is a stub that never had one. Read
      // from the opcode table itself rather than a list kept beside it, so this cannot drift.
      val catalogue = PacketCapture.catalogue.filter(_.space == PacketCapture.Spaces.Game)
      catalogue.find(_.opcode == 8).map(_.charted) mustEqual Some(true)
      catalogue.find(_.opcode == 0).map(_.charted) mustEqual Some(false)
      catalogue.size mustEqual 256
    }

    "name every opcode as `Name (ID)`" in {
      val entry = PacketCapture.catalogue.find(e => e.space == PacketCapture.Spaces.Game && e.opcode == 8)
      entry.map(_.label) mustEqual Some("PlayerStateMessage (8)")
    }

    "take a decoded packet's real field names from its own class" in {
      // The whole point of this: seeding the field designer with `flying`/`vel` beats `unk1`/`unk2`.
      // Names and types come from the case class, which is exact; widths live in the codec and are
      // deliberately not claimed here.
      val packet = net.psforever.packet.game.packets.PlayerStateMessage(
        net.psforever.types.PlanetSideGUID(1),
        net.psforever.types.Vector3(1.0f, 2.0f, 3.0f),
        Some(net.psforever.types.Vector3(4.0f, 5.0f, 6.0f)),
        facingYaw = 10.0f,
        facingPitch = 11.0f,
        facingYawUpper = 12.0f,
        timestamp = 13,
        is_crouching = true
      )
      val fields = PacketCapture.fieldsOf(packet)
      fields must beSome
      val names = fields.get.map(_.name)
      names must contain("is_crouching")
      names must contain("vel")
      // Arity matches the class, so nothing is silently dropped.
      fields.get.size mustEqual packet.productArity
      // Types are read off the runtime values rather than guessed from the name.
      fields.get.find(_.name == "is_crouching").map(_.scalaType) mustEqual Some("Boolean")
      fields.get.find(_.name == "vel").map(_.scalaType) mustEqual Some("Option[Vector3]")
    }

    "offer no field names for something that never decoded" in {
      // An uncharted opcode has no class, so there is nothing to read names from -- and that is the
      // case the designer exists to serve.
      PacketCapture.fieldsOf("not a packet") must beNone
    }

    "mark the credential opcodes as never selectable" in {
      val catalogue = PacketCapture.catalogue.filter(_.space == PacketCapture.Spaces.Game)
      PacketCapture.Redacted.keys.foreach { opcode =>
        catalogue.find(_.opcode == opcode).map(_.redacted) mustEqual Some(true)
      }
      // And nothing else is locked, so the list stays honest about what it covers.
      catalogue.count(_.redacted) mustEqual PacketCapture.Redacted.size
    }
  }
}
