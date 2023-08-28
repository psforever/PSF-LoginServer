// Copyright (c) 2022 PSForever
package net.psforever.objects.zones.exp

import scala.concurrent.ExecutionContext.Implicits.global

import net.psforever.objects.avatar.scoring.EquipmentStat
import net.psforever.objects.serverobject.hackable.Hackable.HackInfo
import net.psforever.objects.sourcing.VehicleSource
import net.psforever.persistence
import net.psforever.types.Vector3
import net.psforever.util.Database.ctx
import net.psforever.util.Database.ctx._

import scala.util.Success

object ToDatabase {
  /**
   * Insert an entry into the database's `killactivity` table.
   * One player just died and some other player is at fault.
   */
  def reportKillBy(
                    killerId: Long,
                    victimId: Long,
                    victimExoSuitId: Int,
                    victimMounted: Int,
                    weaponId: Int,
                    zoneId: Int,
                    position: Vector3,
                    exp: Long
                  ): Unit = {
    ctx.run(query[persistence.Killactivity]
      .insert(
        _.victimId      -> lift(victimId),
        _.killerId      -> lift(killerId),
        _.victimExosuit -> lift(victimExoSuitId),
        _.victimMounted -> lift(victimMounted),
        _.weaponId      -> lift(weaponId),
        _.zoneId        -> lift(zoneId),
        _.px            -> lift((position.x * 1000).toInt),
        _.py            -> lift((position.y * 1000).toInt),
        _.pz            -> lift((position.z * 1000).toInt),
        _.exp           -> lift(exp)
      )
    )
  }

  /**
   * Insert an entry into the database's `assistactivity` table.
   * One player just died and some other player tried to take credit.
   * (They are actually an accomplice.)
   */
  def reportKillAssistBy(
                          avatarId: Long,
                          victimId: Long,
                          weaponId: Int,
                          zoneId: Int,
                          position: Vector3,
                          exp: Long
                        ): Unit = {
    ctx.run(query[persistence.Assistactivity]
      .insert(
        _.killerId -> lift(avatarId),
        _.victimId -> lift(victimId),
        _.weaponId -> lift(weaponId),
        _.zoneId   -> lift(zoneId),
        _.px       -> lift((position.x * 1000).toInt),
        _.py       -> lift((position.y * 1000).toInt),
        _.pz       -> lift((position.z * 1000).toInt),
        _.exp      -> lift(exp)
      )
    )
  }

  /**
   * Insert an entry into the database's `supportactivity` table.
   * One player did something for some other player and
   * that other player was able to kill a third player.
   */
  def reportSupportBy(
                       user: Long,
                       target: Long,
                       exosuit: Int,
                       interaction: Int,
                       intermediate: Int,
                       implement: Int,
                       experience: Long
                     ): Unit = {
    ctx.run(query[persistence.Supportactivity]
      .insert(
        _.userId           -> lift(user),
        _.targetId         -> lift(target),
        _.targetExosuit    -> lift(exosuit),
        _.interactionType  -> lift(interaction),
        _.implementType    -> lift(implement),
        _.intermediateType -> lift(intermediate),
        _.exp              -> lift(experience)
      )
    )
  }

  /**
   * Attempt to update the database's `weaponstatsession` table and,
   * if no existing entries can be found,
   * insert a new entry into the table.
   * Shots fired.
   */
  def reportToolDischarge(avatarId: Long, stats: EquipmentStat): Unit = {
    val result = for {
      res <- ctx.run(
        query[persistence.Weaponstatsession]
          .filter(_.avatarId == lift(avatarId))
          .filter(_.weaponId == lift(stats.objectId))
          .update(
            _.shotsFired -> lift(stats.shotsFired),
            _.shotsLanded -> lift(stats.shotsLanded)
          )
      )
    } yield res
    result.onComplete {
      case Success(rowCount) if rowCount.longValue > 0 => ()
      case _ =>
        ctx.run(query[persistence.Weaponstatsession]
          .insert(
            _.avatarId -> lift(avatarId),
            _.weaponId -> lift(stats.objectId),
            _.shotsFired -> lift(stats.shotsFired),
            _.shotsLanded -> lift(stats.shotsLanded),
            _.kills -> lift(0),
            _.assists -> lift(0),
            _.sessionId -> lift(-1L)
          )
        )
    }
  }

  /**
   * Insert an entry into the database's `machinedestroyed` table.
   * Just as stated, something that was not a player was destroyed.
   * Valid entity types include: vehicles, amenities, and various turrets.
   */
  def reportMachineDestruction(
                                avatarId: Long,
                                machine: VehicleSource,
                                hackState: Option[HackInfo],
                                isCargo: Boolean,
                                weaponId: Int,
                                zoneNumber: Int
                              ): Unit = {
    import net.psforever.util.Database.ctx
    import net.psforever.util.Database.ctx._
    val normalFaction = machine.Faction.id
    val hackedToFaction = hackState.map { _.player.Faction.id }.getOrElse(normalFaction)
    val machinePosition = machine.Position
    ctx.run(query[persistence.Machinedestroyed]
      .insert(
        _.avatarId -> lift(avatarId),
        _.weaponId -> lift(weaponId),
        _.machineType -> lift(machine.Definition.ObjectId),
        _.machineFaction -> lift(normalFaction),
        _.hackedFaction -> lift(hackedToFaction),
        _.asCargo -> lift(isCargo),
        _.zoneNum -> lift(zoneNumber),
        _.px -> lift((machinePosition.x * 1000).toInt),
        _.py -> lift((machinePosition.y * 1000).toInt),
        _.pz -> lift((machinePosition.z * 1000).toInt)
      )
    )
  }
}
