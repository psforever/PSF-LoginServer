package net.psforever.actors.api

import akka.actor.ActorRef
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.etc.SuicideReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.zones.Zone
import net.psforever.objects.{OrbitalStrike, Player, Vehicle}
import net.psforever.packet.game.packets.{TriggerEffectMessage, TriggeredEffectLocation}
import net.psforever.services.account.AccountPersistenceService
import net.psforever.services.base.envelope.MessageEnvelope
import net.psforever.services.base.message.SendResponse
import net.psforever.services.vehicle.VehicleAction
import net.psforever.types.{PlanetSideGUID, ValidPlanetSideGUID, Vector3}
import net.psforever.util.TerrainHeight

import scala.concurrent.duration._

/**
  * The handful of world mutations the administration API performs, shared by the single-entity
  * command and the whole-continent one so both behave identically.
  *
  * Nothing here is novel behaviour -- each is the same thing the game already does to itself, reached
  * from outside a session.
  */
object AdminWorldOps {
  private val log = org.log4s.getLogger("AdminWorldOps")

  /**
    * Kill an entity through the ordinary damage path, so the world reacts exactly as it would to a
    * fatal shot: wreckage, explosion, death message, respawn timer.
    *
    * [[SuicideReason]] is a guaranteed-lethal profile that reports no adversary, so nothing is
    * credited with the kill -- an administrator clearing a continent should not appear on anyone's
    * scoreboard.
    */
  def destroy(target: PlanetSideServerObject with Vitality): Unit = {
    target.Actor ! Vitality.Damage(
      DamageInteraction(SourceEntry(target), SuicideReason(), target.Position).calculate()
    )
  }

  /**
    * Disconnect a player, the same way an in-game administrator's kick does.
    *
    * Both halves are required: the sentinel makes the player's own session send the disconnect, and
    * the persistence service records a logout rather than treating it as a dropped connection. They
    * are also taken out of any seat first, so they are not kicked while still mounted.
    */
  def kick(zone: Zone, p: Player, accountPersistence: ActorRef): Unit = {
    p.death_by = -1
    accountPersistence ! AccountPersistenceService.Kick(p.Name)
    dismount(zone, p)
  }

  /** Take one player out of whatever they are sitting in. */
  def dismount(zone: Zone, p: Player): Unit = {
    p.VehicleSeated
      .flatMap(zone.GUID)
      .collect { case v: Vehicle =>
        v.Seats.find { case (_, seat) => seat.occupant.contains(p) }.foreach { case (index, seat) =>
          seat.unmount(p)
          p.VehicleSeated = None
          zone.VehicleEvents ! MessageEnvelope(
            zone.id,
            p.GUID,
            VehicleAction.KickPassenger(index, unk2 = false, v.GUID)
          )
        }
      }
  }

  /** Turf every seated player out of a vehicle without harming them. */
  def evictOccupants(zone: Zone, v: Vehicle): Unit = {
    v.Seats.foreach { case (index, seat) =>
      seat.occupant.collect { case p: Player =>
        seat.unmount(p)
        p.VehicleSeated = None
        zone.VehicleEvents ! MessageEnvelope(
          zone.id,
          p.GUID,
          VehicleAction.KickPassenger(index, unk2 = false, v.GUID)
        )
      }
    }
  }

  /**
    * Call in a real orbital strike at a map position.
    *
    * This is the game's own strike, not an imitation of one: the client already knows how to play it,
    * so the server broadcasts the same [[TriggerEffectMessage]] the Command Uplink Device path sends
    * and then applies [[OrbitalStrike]] damage through the game's own target selection
    * ([[Zone.findOrbitalStrikeTargets]]), including its rule that riders inside vehicles are spared
    * and the vehicle is hit instead. Everyone in the zone sees and hears the strike land.
    *
    * The five-second gap between the effect starting and the damage landing is the game's, kept so
    * the beam is on screen before anything dies, exactly as players experience it. The waypoint
    * marker phase is skipped: that is the commander's own aiming reticle, and there is no commander.
    *
    * The black-ops effect variant is used because no empire called this in.
    *
    * @param radius 10 for the CR4 strike, 20 for CR5 -- the only two the game defines
    *
    * There is deliberately no way for a caller to supply the target's height. A caller aiming from a
    * two-dimensional map has no real height to offer, only a claim the server cannot check against
    * anything -- so instead of trusting one, the height is read from [[TerrainHeight]], the world
    * server's own copy of the terrain. Height only affects where the effect is drawn; targeting is
    * measured across the ground and does not need it at all.
    */
  def orbitalStrike(
      zone: Zone,
      x: Float,
      y: Float,
      radius: Float,
      scheduler: akka.actor.Scheduler
  )(implicit ec: scala.concurrent.ExecutionContext): Int = {
    val profile = if (radius >= 20f) OrbitalStrike.cr5_os else OrbitalStrike.cr4_os
    val flat = Vector3(x, y, 0f)

    def targets(): List[PlanetSideServerObject with Vitality] =
      Zone
        .findOrbitalStrikeTargets(zone, flat, profile.DamageRadius, Zone.getOrbitbalStrikeTargets)
        .filter(t => Zone.orbitalStrikeDistanceCheck(flat, t.Position, profile.DamageRadius))

    val standing = targets()
    val sampled = TerrainHeight.sample(zone.id, x, y)
    val height = sampled.getOrElse(0f)
    val at = Vector3(x, y, height)
    log.info(
      s"orbital strike in ${zone.id} at ($x, $y): terrain height ${sampled.getOrElse("unavailable, using 0")}"
    )

    val effect = if (profile.DamageRadius >= 20f) "explosion_bluedeath_bo_lrg" else "explosion_bluedeath_bo"
    zone.LocalEvents ! MessageEnvelope(
      zone.id,
      PlanetSideGUID(-1),
      SendResponse(
        TriggerEffectMessage(
          ValidPlanetSideGUID(0),
          effect,
          None,
          Some(TriggeredEffectLocation(at, Vector3(0, 0, 90)))
        )
      )
    )

    // Re-resolved when it lands, not reused from above: five seconds is long enough to run out of.
    scheduler.scheduleOnce(5.seconds) {
      targets().foreach { t =>
        t.Actor ! Vitality.Damage(
          DamageInteraction(SourceEntry(t), OrbitalStrike(None), t.Position).calculate()
        )
      }
    }

    standing.size
  }
}
