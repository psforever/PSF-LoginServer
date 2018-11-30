// Copyright (c) 2017 PSForever
package services.vehicle.support

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.ballistics.VehicleSource
import net.psforever.objects.serverobject.terminals.{MedicalTerminalDefinition, ProximityUnit, Terminal}
import net.psforever.objects.vital.RepairFromTerm
import net.psforever.objects.{DefaultCancellable, Vehicle}
import net.psforever.packet.game.PlanetSideGUID
import org.slf4j.Logger
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.collection.mutable
import scala.concurrent.duration._

class SiloRepair extends Actor {
  var task : Cancellable = DefaultCancellable.obj

  var counter : Long = 0

  val terminalsToTargets : mutable.LongMap[(Terminal with ProximityUnit, Vehicle)] =
    new mutable.LongMap[(Terminal with ProximityUnit, Vehicle)]()

  def receive : Receive = {
    case SiloRepair.Start(term, vehicle) =>
      if(term.Targets.contains(vehicle) && !terminalsToTargets.values.exists({ case (x, y) => (x ne term) && (y ne vehicle) })) {
        terminalsToTargets += Counter -> (term, vehicle)
        if(terminalsToTargets.size == 1) {
          val proxDef = term.Definition.asInstanceOf[MedicalTerminalDefinition]
          import scala.concurrent.ExecutionContext.Implicits.global
          task = context.system.scheduler.schedule(0 seconds, proxDef.Delay, self, SiloRepair.TerminalActionCycle())
        }
      }

    case SiloRepair.TerminalActionCycle() =>
      terminalsToTargets.toMap.foreach({ case (index, (term, target)) =>
        val medDef = term.Definition.asInstanceOf[MedicalTerminalDefinition]
        val healAmount = medDef.HealAmount
        if(healAmount != 0 && term.Validate(target) && target.Health < target.MaxHealth) {
          target.Health = target.Health + healAmount
          target.History(RepairFromTerm(VehicleSource(target), healAmount, medDef))
          context.parent ! VehicleServiceMessage(term.Continent, VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), target.GUID, 0, target.Health))
        }
        else {
          terminalsToTargets -= index
          if(terminalsToTargets.isEmpty) {
            task.cancel
          }
        }

      case _ => ;
      })
  }

  def Counter : Long = {
    val current = counter
    if(counter + 1 == Long.MaxValue) {
      counter = 0
    }
    else {
      counter += 1
    }
    current
  }
}

object SiloRepair {
  final case class Start(terminal : Terminal with ProximityUnit, vehicle : Vehicle)

  final case class TerminalActionCycle()
}
