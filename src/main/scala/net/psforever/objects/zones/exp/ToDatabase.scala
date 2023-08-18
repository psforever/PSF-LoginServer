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

  def reportAssistKills(avatarId: Long, weaponId: Int, assists: Int): Unit = {
    val result = for {
      res <- ctx.run(
        query[persistence.Weaponstatsession]
          .filter(_.avatarId == lift(avatarId))
          .filter(_.weaponId == lift(weaponId))
          .update(
            _.assists -> lift(assists)
          )
      )
    } yield res
    result.onComplete {
      case Success(rowCount) if rowCount.longValue > 0 => ()
      case _ =>
        ctx.run(query[persistence.Weaponstatsession]
          .insert(
            _.avatarId -> lift(avatarId),
            _.weaponId -> lift(weaponId),
            _.assists -> lift(assists),
            _.shotsFired -> lift(0),
            _.shotsLanded -> lift(0),
            _.kills -> lift(0),
            _.sessionId -> lift(-1L)
          )
        )
    }
  }

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
    val hackedFaction = hackState.map { _.player.Faction }.getOrElse(normalFaction)
    val machinePosition = machine.Position
    ctx.run(query[persistence.Machinedestroyedinstance]
      .insert(
        _.avatarId -> lift(avatarId),
        _.weaponId -> lift(weaponId),
        _.machineType -> lift(machine.Definition.ObjectId),
        _.machineFaction -> lift(normalFaction),
        _.hackedFaction -> lift(hackedFaction),
        _.asCargo -> lift(isCargo),
        _.zoneNum -> lift(zoneNumber),
        _.px -> lift((machinePosition.x * 1000).toInt),
        _.py -> lift((machinePosition.y * 1000).toInt),
        _.pz -> lift((machinePosition.z * 1000).toInt)
      )
    )
  }
}
