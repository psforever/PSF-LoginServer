// Copyright (c) 2017 PSForever
package objects.terminal

import akka.actor.{ActorSystem, Props}
import base.ActorTest
import net.psforever.objects.avatar.{Avatar, Certification}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.structures.{BaseVirus, Building, StructureType}
import net.psforever.objects.serverobject.terminals.{Terminal, TerminalControl, TerminalDefinition}
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.objects.{GlobalDefinitions, Player, Tool}
import net.psforever.packet.game.packets.{HackState1, ItemTransactionMessage}
import net.psforever.types._

import scala.concurrent.duration.Duration

class TerminalControl1Test extends ActorTest {
  "TerminalControl" should {
    "construct (cert terminal)" in {
      val terminal = Terminal(GlobalDefinitions.cert_terminal)
      terminal.Actor = system.actorOf(Props(classOf[TerminalControl], terminal), "test-cert-term")
    }
  }
}

class TerminalControl2Test extends ActorTest {
  "TerminalControl can not process wrong messages" in {
    val (_, terminal) = TerminalControlTest.SetUpAgents(GlobalDefinitions.cert_terminal, PlanetSideEmpire.TR)

    terminal.Actor ! "hello"
    expectNoMessage(Duration.create(500, "ms"))
  }
}

//terminal control is mostly a pass-through actor for Terminal.Exchange messages, wrapped in Terminal.TerminalMessage protocol
//test for Cert_Terminal messages (see CertTerminalTest)
class CertTerminalControl1Test extends ActorTest {
  "TerminalControl can be used to learn a certification ('medium_assault')" in {
    val (player, terminal) = TerminalControlTest.SetUpAgents(GlobalDefinitions.cert_terminal, PlanetSideEmpire.TR)
    val msg =
      ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Learn, 0, "medium_assault", 0, PlanetSideGUID(0))

    terminal.Actor ! Terminal.Request(player, msg)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player)
    assert(reply2.msg == msg)
    assert(reply2.response == Terminal.LearnCertification(Certification.MediumAssault))
  }
}

class CertTerminalControl2Test extends ActorTest {
  "TerminalControl can be used to warn about not learning a fake certification ('juggling')" in {
    val (player, terminal) = TerminalControlTest.SetUpAgents(GlobalDefinitions.cert_terminal, PlanetSideEmpire.TR)
    val msg                = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Learn, 0, "juggling", 0, PlanetSideGUID(0))

    terminal.Actor ! Terminal.Request(player, msg)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player)
    assert(reply2.msg == msg)
    assert(reply2.response == Terminal.NoDeal())
  }
}

class CertTerminalControl3Test extends ActorTest {
  "TerminalControl can be used to forget a certification ('medium_assault')" in {
    val (player, terminal) = TerminalControlTest.SetUpAgents(GlobalDefinitions.cert_terminal, PlanetSideEmpire.TR)
    val msg                = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Sell, 0, "medium_assault", 0, PlanetSideGUID(0))

    terminal.Actor ! Terminal.Request(player, msg)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player)
    assert(reply2.msg == msg)
    assert(reply2.response == Terminal.SellCertification(Certification.MediumAssault))
  }
}

class VehicleTerminalControl1Test extends ActorTest {
  "TerminalControl can be used to buy a vehicle ('two_man_assault_buggy')" in {
    val (player, terminal) =
      TerminalControlTest.SetUpAgents(GlobalDefinitions.ground_vehicle_terminal, PlanetSideEmpire.TR)
    val msg = ItemTransactionMessage(
      PlanetSideGUID(1),
      TransactionType.Buy,
      46769,
      "two_man_assault_buggy",
      0,
      PlanetSideGUID(0)
    )

    terminal.Actor ! Terminal.Request(player, msg)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player)
    assert(reply2.msg == msg)
    assert(reply2.response.isInstanceOf[Terminal.BuyVehicle])
    val reply3 = reply2.response.asInstanceOf[Terminal.BuyVehicle]
    assert(reply3.vehicle.Definition == GlobalDefinitions.two_man_assault_buggy)
    assert(reply3.weapons == Nil)
    assert(reply3.inventory.length == 6) //TODO
    assert(reply3.inventory.head.obj.Definition == GlobalDefinitions.bullet_12mm)
    assert(reply3.inventory(1).obj.Definition == GlobalDefinitions.bullet_12mm)
    assert(reply3.inventory(2).obj.Definition == GlobalDefinitions.bullet_12mm)
    assert(reply3.inventory(3).obj.Definition == GlobalDefinitions.bullet_12mm)
    assert(reply3.inventory(4).obj.Definition == GlobalDefinitions.bullet_12mm)
    assert(reply3.inventory(5).obj.Definition == GlobalDefinitions.bullet_12mm)
  }
}

class VehicleTerminalControl2Test extends ActorTest {
  "TerminalControl can be used to warn about not buy a vehicle ('harasser')" in {
    val (player, terminal) =
      TerminalControlTest.SetUpAgents(GlobalDefinitions.ground_vehicle_terminal, PlanetSideEmpire.TR)
    val msg = ItemTransactionMessage(PlanetSideGUID(1), TransactionType.Buy, 0, "harasser", 0, PlanetSideGUID(0))

    terminal.Actor ! Terminal.Request(player, msg)
    val reply = receiveOne(Duration.create(500, "ms"))
    assert(reply.isInstanceOf[Terminal.TerminalMessage])
    val reply2 = reply.asInstanceOf[Terminal.TerminalMessage]
    assert(reply2.player == player)
    assert(reply2.msg == msg)
    assert(reply2.response == Terminal.NoDeal())
  }
}

object TerminalControlTest {
  def SetUpAgents(tdef: TerminalDefinition, faction: PlanetSideEmpire.Value)(implicit
      system: ActorSystem
  ): (Player, Terminal) = {
    val terminal = Terminal(tdef)
    terminal.Actor = system.actorOf(Props(classOf[TerminalControl], terminal), "test-term")
    terminal.Owner = new Building(
      "Building",
      building_guid = 0,
      map_id = 0,
      Zone.Nowhere,
      StructureType.Building,
      GlobalDefinitions.building
    )
    terminal.Owner.Faction = faction
    (Player(Avatar(0, "test", faction, CharacterSex.Male, 0, CharacterVoice.Mute)), terminal)
  }
}

class TerminalControlVirusCancellationTest extends ActorTest {
  private case class VirusFixture(
      zone: Zone,
      terminal: Terminal,
      building: Building,
      hacker1: Player,
      hacker2: Player,
      trek1: Tool,
      trek2: Tool
  )

  private def SetUpVirusRace(): VirusFixture = {
    val testZone = new Zone("test-zone", new ZoneMap("test-zone"), 99)
    testZone.AvatarEvents = testActor
    //gives unobserved install-path events (sounds, virus alert scheduling) a safe sink
    testZone.LocalEvents = system.deadLetters

    val terminal = Terminal(GlobalDefinitions.main_terminal)
    terminal.Actor = system.actorOf(Props(classOf[TerminalControl], terminal))
    val building = new Building(
      "Building",
      building_guid = 10,
      map_id = 10,
      testZone,
      StructureType.Building,
      GlobalDefinitions.building
    )
    building.Faction = PlanetSideEmpire.NC
    terminal.Owner = building

    val hacker1 = Player(Avatar(1, "hacker1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute).copy(certifications = Set(Certification.ElectronicsExpert)))
    hacker1.GUID = PlanetSideGUID(101)

    val hacker2 = Player(Avatar(2, "hacker2", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute).copy(certifications = Set(Certification.ElectronicsExpert)))
    hacker2.GUID = PlanetSideGUID(102)

    val trek1 = Tool(GlobalDefinitions.trek)
    trek1.GUID = PlanetSideGUID(201)
    val trek2 = Tool(GlobalDefinitions.trek)
    trek2.GUID = PlanetSideGUID(202)
    terminal.GUID = PlanetSideGUID(301)

    VirusFixture(testZone, terminal, building, hacker1, hacker2, trek1, trek2)
  }

  "TerminalControl" should {
    "track multiple active virus uploaders and cancel the other hacker when one wins by upload-state" in {
      val f = SetUpVirusRace()

      assert(TerminalControl.CanUploadVirus(f.hacker1, f.terminal))

      // Hacker 1 starts uploading virus
      f.terminal.Actor ! CommonMessages.UploadVirus(f.hacker1, Some(f.trek1), BaseVirus.UnlockDoors.id)
      val reply1 = receiveOne(Duration.create(500, "ms"))
      assert(reply1.isInstanceOf[CommonMessages.Progress])
      assert(f.terminal.activeVirusUploaders.contains(f.hacker1.GUID))

      // Hacker 2 starts uploading virus
      f.terminal.Actor ! CommonMessages.UploadVirus(f.hacker2, Some(f.trek2), BaseVirus.DisableRadar.id)
      val reply2 = receiveOne(Duration.create(500, "ms"))
      assert(reply2.isInstanceOf[CommonMessages.Progress])
      assert(f.terminal.activeVirusUploaders.contains(f.hacker2.GUID))
      assert(f.terminal.activeVirusUploaders.size == 2)

      // EndVirusUploadProgress should be false for both while terminal is in valid state
      assert(!TerminalControl.EndVirusUploadProgress(f.terminal, f.hacker1))
      assert(!TerminalControl.EndVirusUploadProgress(f.terminal, f.hacker2))

      // Now hacker 1 completes virus upload (building has active virus with cooldown)
      f.building.virusId = BaseVirus.UnlockDoors.id
      f.building.virusInstallTime = Some(System.currentTimeMillis())
      f.building.virusInstalledBy = Some(PlanetSideEmpire.TR.id)

      // Hacker 2 should now be cancelled by EndVirusUploadProgress
      assert(TerminalControl.EndVirusUploadProgress(f.terminal, f.hacker2))

      // Also verify HackingTickAction cancels hacker 2 and sends HackState.Cancelled
      val tickCancelled = GenericHackables.HackingTickAction(
        HackState1.Unk1,
        f.hacker2,
        f.terminal,
        f.trek2.GUID,
        TerminalControl.EndVirusUploadProgress
      )(100f)
      // Even at 100% progress, should return false (cancelled)
      assert(!tickCancelled)

      // Verify that HackState.Cancelled was sent to hacker2
      val cancelMsg = receiveOne(Duration.create(500, "ms"))
      assert(cancelMsg.isInstanceOf[net.psforever.services.base.envelope.MessageEnvelope])
      val envelope = cancelMsg.asInstanceOf[net.psforever.services.base.envelope.MessageEnvelope]
      assert(envelope.channel == f.hacker2.Name)
      val hackMessage = envelope.msg.asInstanceOf[net.psforever.services.base.message.SendResponse].pkts.head.asInstanceOf[net.psforever.packet.game.packets.HackMessage]
      assert(hackMessage.hack_state == net.psforever.packet.game.packets.HackState.Cancelled)
      assert(hackMessage.player_guid == f.hacker2.GUID)
      assert(hackMessage.target_guid == f.terminal.GUID)
    }

    "deliver HackState.Cancelled to the concurrent hacker when the winning upload completes end-to-end" in {
      val f = SetUpVirusRace()

      // both hackers start real uploads through the terminal actor
      f.terminal.Actor ! CommonMessages.UploadVirus(f.hacker1, Some(f.trek1), BaseVirus.UnlockDoors.id)
      val reply1 = receiveOne(Duration.create(500, "ms"))
      assert(reply1.isInstanceOf[CommonMessages.Progress], "winner upload did not produce Progress reply")
      val progress1 = reply1.asInstanceOf[CommonMessages.Progress]

      f.terminal.Actor ! CommonMessages.UploadVirus(f.hacker2, Some(f.trek2), BaseVirus.DisableRadar.id)
      val reply2 = receiveOne(Duration.create(500, "ms"))
      assert(reply2.isInstanceOf[CommonMessages.Progress])
      assert(f.terminal.activeVirusUploaders.size == 2)

      // no virus is installed yet and no cooldown is running, so the completion is valid to install
      f.building.Actor = system.deadLetters

      // winner reaches 100% - the session actor invokes exactly this completion action
      progress1.completionAction()

      // FinishVirusAction asks the terminal actor, then on completion evicts all other uploaders,
      // and each of them is notified with a cancelled hack state on their avatar channel
      val cancelMsg = receiveOne(Duration.create(3, "seconds"))
      assert(cancelMsg.isInstanceOf[net.psforever.services.base.envelope.MessageEnvelope], "no cancellation was broadcast to AvatarEvents")
      val envelope = cancelMsg.asInstanceOf[net.psforever.services.base.envelope.MessageEnvelope]
      assert(envelope.channel == f.hacker2.Name)
      val hackMessage = envelope.msg.asInstanceOf[net.psforever.services.base.message.SendResponse].pkts.head.asInstanceOf[net.psforever.packet.game.packets.HackMessage]
      assert(hackMessage.hack_state == net.psforever.packet.game.packets.HackState.Cancelled)
      assert(hackMessage.player_guid == f.hacker2.GUID)
      assert(hackMessage.target_guid == f.terminal.GUID)

      // the winner and the loser were both deregistered by the completion
      assert(f.terminal.activeVirusUploaders.isEmpty)

      // the loser's next progress tick must terminate its upload loop
      val progress2 = reply2.asInstanceOf[CommonMessages.Progress]
      assert(!progress2.tickAction(50f))
    }
  }
}
