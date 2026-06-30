// Copyright (c) 2026 PSForever
package net.psforever.actors.zone

import akka.actor.{Actor, Props}
import net.psforever.objects.{Default, GlobalDefinitions, Tool, Vehicle}
import net.psforever.objects.avatar.{AvatarBot, AvatarBotActor}
import net.psforever.objects.guid.{GUIDTask, StraightforwardTask, TaskBundle, TaskWorkflow}
import net.psforever.objects.zones.Zone
import net.psforever.services.base.envelope.MessageEnvelope
import net.psforever.services.local.LocalAction
import net.psforever.services.vehicle.VehicleAction
import net.psforever.types.{CharacterSex, CharacterVoice, ExoSuitType, PlanetSideEmpire, Vector3}
import net.psforever.util.Config

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

object ShootingRangeTargetSpawner {
  final case class InfantryTargetReleased(bot: AvatarBot)

  final case class VehicleTargetDeconstructed(vehicle: Vehicle)
}

class ShootingRangeTargetSpawnerActor(zone: Zone) extends Actor {
  private[this] val log = org.log4s.getLogger

  private val maleOnlyBotNames = Config.app.game.virtualTraining.maleBotNames
  private val femaleOnlyBotNames = Config.app.game.virtualTraining.femaleBotNames
  private val universalBotNames = Config.app.game.virtualTraining.universalBotNames

  private val airVehicleSpawns = List[(Vector3, Float)](
    (Vector3(526.1094f, 517.84375f, 18.65625f), 295.3125f),
    (Vector3(507.9297f, 567.4297f, 20.328125f), 182.8125f),
    (Vector3(533.8516f, 551.28125f, 20.96875f), 278.4375f),
    (Vector3(504.5390f, 491.9922f, 22.015625f), 64.6875f),
    (Vector3(464.3281f, 513.5625f, 22.4375f), 348.75f),
    (Vector3(457.4375f, 546.71094f, 21.90625f), 146.25f),
    (Vector3(502.7734f, 467.5078f, 31.34375f), 334.6875f),
    (Vector3(489.2580f, 608.71094f, 28.046875f), 78.75f),
    (Vector3(570.1562f, 569.4766f, 26.328125f), 230.625f),
    (Vector3(576.2891f, 487.8594f, 28.71875f), 351.5625f),
    (Vector3(419.3906f, 572.8984f, 22.296875f), 137.8125f),
    (Vector3(410.7266f, 493.95312f, 25.578125f), 19.6875f)
  )

  private val groundVehicleSpawns = List[(Vector3, Float)](
    (Vector3(501.6562f, 479.1562f, 12.421875f), 351.5625f),
    (Vector3(463.1484f, 522.72656f, 12.0625f), 59.0625f),
    (Vector3(510.2812f, 573.9844f, 11.453125f), 295.3125f),
    (Vector3(543.1330f, 519.0703f, 11.0625f), 188.4375f),
    (Vector3(547.1406f, 555.09375f, 11.859375f), 315.0f),
    (Vector3(463.9220f, 570.71094f, 12.15625f), 75.9375f)
  )

  private val infantrySpawns = List[(Vector3, Float)](
    (Vector3(499.7969f, 546.6484f, 14.906250f), 191.2500f),
    (Vector3(507.1172f, 547.7188f, 14.906250f), 205.3125f),
    (Vector3(497.9688f, 550.0469f, 14.734375f), 199.6875f),
    (Vector3(519.3438f, 536.0000f, 14.796875f), 275.6250f),
    (Vector3(481.4609f, 539.5000f, 14.421875f), 135.0000f),
    (Vector3(522.1094f, 524.4766f, 13.593750f), 306.5625f),
    (Vector3(481.4453f, 524.0781f, 14.796875f), 84.3750f),
    (Vector3(499.6875f, 557.7578f, 13.375000f), 180.0000f),
    (Vector3(483.3984f, 517.5938f, 14.796875f), 67.5000f),
    (Vector3(487.9531f, 555.8828f, 13.468750f), 157.5000f),
    (Vector3(527.2109f, 546.3984f, 13.765625f), 250.3125f),
    (Vector3(472.9609f, 536.8125f, 13.093750f), 95.6250f),
    (Vector3(476.3984f, 548.1094f, 12.437500f), 126.5625f),
    (Vector3(503.7188f, 503.8047f, 14.296875f), 357.1875f),
    (Vector3(475.7344f, 514.3984f, 14.125000f), 56.2500f),
    (Vector3(492.8750f, 502.5547f, 13.828125f), 5.6250f),
    (Vector3(511.1172f, 501.3047f, 13.812500f), 345.9375f),
    (Vector3(498.2422f, 592.5938f, 13.796875f), 180.0000f),
    (Vector3(448.2500f, 579.2500f, 13.015625f), 120.9375f),
    (Vector3(417.7109f, 511.1719f, 13.828125f), 70.3125f),
    (Vector3(589.2812f, 551.2266f, 13.687500f), 250.3125f),
    (Vector3(505.4062f, 440.4062f, 13.203125f), 19.6875f),
    (Vector3(540.4297f, 464.6406f, 13.593750f), 306.5625f),
    (Vector3(549.6016f, 558.6719f, 13.687500f), 250.3125f)
  )

  private val infantrySpawnsMAX = List[(Vector3, Float)](
    (Vector3(506.6484f, 544.3984f, 14.937500f), 196.8750f),
    (Vector3(503.9219f, 559.7109f, 13.375000f), 188.4375f),
    (Vector3(497.3125f, 510.0312f, 14.640625f), 14.0625f),
    (Vector3(507.9922f, 511.7656f, 14.625000f), 357.1875f),
    (Vector3(511.1875f, 558.5625f, 14.640625f), 199.6875f),
    (Vector3(473.9062f, 517.5156f, 14.265625f), 70.3125f),
    (Vector3(474.9062f, 541.5234f, 12.765625f), 106.8750f),
    (Vector3(530.0000f, 513.5469f, 13.046875f), 303.7500f),
    (Vector3(529.4062f, 538.7891f, 13.390625f), 267.1875f)
  )

  private val activeInfantryTargets = ListBuffer[AvatarBot]()
  private val activeVehicleTargets = ListBuffer[(Vehicle, Vector3)]()
  private var botNamesInUse = List[String]()

  override def preStart() = {
    if (Config.app.game.virtualTraining.shootingRangeTargetsEnabled) {
      //delayed to avoid potential GUID registration errors
      context.system.scheduler.scheduleOnce(
        3.seconds,
        new Runnable() { override def run(): Unit = StartSpawner() }
      )
    }
  }

  override def postStop(): Unit = {
    activeInfantryTargets.foreach{ target => 
      target.Actor ! AvatarBot.Release()
    }
    activeInfantryTargets.clear()
    botNamesInUse = List[String]()
    activeVehicleTargets.foreach{ case (target, pos) =>
      if (target.Actor != Default.Actor) {
        target.Actor ! Vehicle.Deconstruct(None)
      }
    }
    activeVehicleTargets.clear()
  }

  def receive: Receive = {
    case ShootingRangeTargetSpawner.InfantryTargetReleased(bot) =>
      RemoveBot(bot)

    case ShootingRangeTargetSpawner.VehicleTargetDeconstructed(vehicle) =>
      OnVehicleTargetDeconstructed(vehicle)

    case _ => ()
  }

  private def StartSpawner(): Unit = {
    val validZone = zone.id match {
      case "tzshtr" | "tzshnc" | "tzshvs" => true
      case _                              => false
    }
    if (!validZone) {
      log.warn(s"Failed to enable target spawns for zone ${zone.id}; not a valid zone for this behavior")
    } else {
      airVehicleSpawns.foreach{case (pos, yaw) => CreateVehicleTarget(pos, yaw, true)}
      groundVehicleSpawns.foreach{case (pos, yaw) => CreateVehicleTarget(pos, yaw, false)}
      infantrySpawns.foreach{case (pos, yaw) => CreateInfantryTarget(pos, false)}
      infantrySpawnsMAX.foreach{case (pos, yaw) => CreateInfantryTarget(pos, true)}

      log.info(s"Enabled target spawns for zone ${zone.id}")
    }
  }

  /**
   * Creates a new infantry target at the specified coordinates.
   * @param position the position the target will be created at
   * @param facingYaw the direction the target will be facing
   * @param isMAX if this target is a MAX unit
   */
  def CreateInfantryTarget(position: Vector3, isMAX: Boolean): Unit = {
    val definition = if (isMAX) GlobalDefinitions.avatar_bot_max_no_weapon else Random.nextInt(3) match {
      case 0 => GlobalDefinitions.avatar_bot_agile_no_weapon
      case 1 => GlobalDefinitions.avatar_bot_reinforced_no_weapon
      case 2 => GlobalDefinitions.avatar_bot_standard_no_weapon
    }
    val gender = if (Random.nextBoolean()) CharacterSex.Female else CharacterSex.Male
    val name = GetRandomBotName(gender)
    val factionRNG = Random.nextBoolean()
    val faction = zone.id match {
      case "tzshtr" => if (factionRNG) PlanetSideEmpire.VS else PlanetSideEmpire.NC
      case "tzshnc" => if (factionRNG) PlanetSideEmpire.VS else PlanetSideEmpire.TR
      case "tzshvs" => if (factionRNG) PlanetSideEmpire.NC else PlanetSideEmpire.TR
    }
    val head = Random.nextInt(if (gender == CharacterSex.Female) 10 else 11)
    val voiceRNG = Random.nextInt(4)
    val voice = voiceRNG match {
      case 0 => CharacterVoice.Voice1
      case 1 => CharacterVoice.Voice2
      case 2 => CharacterVoice.Voice3
      case 3 => CharacterVoice.Voice4
      case 4 => CharacterVoice.Voice5
    }

    val facingYaw = if (isMAX) infantrySpawnsMAX.find(_._1 == position) match {
        case Some((_, yaw)) => yaw
        case _ => 0
      }
    else infantrySpawns.find(_._1 == position) match {
        case Some((_, yaw)) => yaw
        case _ => 0
      }

    val bot = AvatarBot(name, faction, gender, head, voice, definition)
    bot.Position = position
    bot.Orientation = Vector3(0f, 0f, facingYaw)
    bot.Zone = zone
    bot.ExoSuit = definition match {
      case GlobalDefinitions.avatar_bot_agile | GlobalDefinitions.avatar_bot_agile_no_weapon =>
        ExoSuitType.Agile
      case GlobalDefinitions.avatar_bot_max | GlobalDefinitions.avatar_bot_max_no_weapon =>
        ExoSuitType.MAX
      case GlobalDefinitions.avatar_bot_reinforced | GlobalDefinitions.avatar_bot_reinforced_no_weapon =>
        ExoSuitType.Reinforced
      case GlobalDefinitions.avatar_bot_standard | GlobalDefinitions.avatar_bot_standard_no_weapon =>
        ExoSuitType.Standard
      case _ =>
        ExoSuitType.Standard
    }
    if (bot.ExoSuit == ExoSuitType.MAX) {
      val subtype = 1 + Random.nextInt(3)
      bot.Slot(0).Equipment = Tool(GlobalDefinitions.MAXArms(subtype, faction))
      bot.DrawnSlot = 0 //max arm up
    }

    TaskWorkflow.execute(RegisterAndSpawnBot(bot))
  }

  /**
   * Gets a random name for a bot and removes the name from the name pool.
   * @param gender determines if it should pull from the male or female name pools
   * @return the name as a string
   */
  private def GetRandomBotName(gender: CharacterSex): String = {
    try {
      gender match {
        case CharacterSex.Male =>
          val availableNames = (maleOnlyBotNames ++ universalBotNames).filterNot(n => botNamesInUse.contains(n))
          if (!availableNames.isEmpty) {
            val name = availableNames(Random.nextInt(availableNames.size))
            botNamesInUse = botNamesInUse :+ name
            name
          } else {
            log.warn(s"Male bot name pool in ${zone.id} is empty!")
            "Bot"
          }
         case CharacterSex.Female =>
          val availableNames = (femaleOnlyBotNames ++ universalBotNames).filterNot(n => botNamesInUse.contains(n))
          if (!availableNames.isEmpty) {
            val name = availableNames(Random.nextInt(availableNames.size))
            botNamesInUse = botNamesInUse :+ name
            name
          } else {
            log.warn(s"Female bot name pool in ${zone.id} is empty!")
            "Bot"
          }
      }
    } catch {
      //while the issue that was causing a mutation during iteration exception to be thrown here rarely should hopefully be fixed now,
      //this is still being put here as a fallback to not block the bot from spawning
      case ex: Exception =>
        "Bot"
    }
  }

  /**
   * Registers the `AvatarBot` object and spawns it into the zone.
   * @param bot the `AvatarBot` object
   * @return a `TaskBundle` message
   */
  private def RegisterAndSpawnBot(bot: AvatarBot): TaskBundle = {
    import net.psforever.objects.serverobject.PlanetSideServerObject
    TaskBundle(
      new StraightforwardTask() {
        private val localBot = bot

        override def description(): String = s"register a ${localBot.Definition.Name}"

        def action(): Future[Any] = {
          localBot.Actor = context.actorOf(
            Props(classOf[AvatarBotActor], localBot, context.self),
            PlanetSideServerObject.UniqueActorName(localBot)
          )
          localBot.Actor ! AvatarBot.Spawn()
          activeInfantryTargets.addOne(localBot)
          log.debug(s"Spawned a ${localBot.Faction} bot named ${localBot.Name} in ${zone.id} at ${localBot.Position}")
          Future(true)
        }
      },
      List(GUIDTask.registerBot(zone.GUID, bot))
    )
  }

  /**
   * Removes the specified bot from the scene and unregisters it.
   * @param bot the bot to remove
   */
  private def RemoveBot(bot: AvatarBot): Boolean = {
    activeInfantryTargets.indexOf(bot) match {
      case -1 =>
        log.warn(s"Failed to remove bot with GUID ${bot.GUID} from ${zone.id}'s active targets list! This shouldn't happen... and probably just caused a leak.")
        false
      case index =>
        activeInfantryTargets.remove(index)
    }
    zone.LocalEvents ! MessageEnvelope(
      zone.id,
      LocalAction.TriggerEffectLocation("bot_destroyed_effect", bot.Position, bot.Orientation)
    )
    //spawn a replacement bot
    context.system.scheduler.scheduleOnce(
      5.seconds,
      new Runnable() { override def run(): Unit = CreateInfantryTarget(bot.Position, bot.ExoSuit == ExoSuitType.MAX) }
    )
    //unregister bot (delay is to prevent ValidObjects from complaining if the bot is getting hit too quickly when it is destroyed)
    context.system.scheduler.scheduleOnce(
      1.seconds,
      new Runnable() { override def run(): Unit = TaskWorkflow.execute(GUIDTask.unregisterBot(bot.Zone.GUID, bot)) }
    )
    //return bot name to name pool
    botNamesInUse.indexOf(bot.Name) match {
      case -1    => log.warn(s"Failed to restore bot name `${bot.Name}` to the bot name pool!")
      case index => botNamesInUse = botNamesInUse.filterNot(n => n == bot.Name)
    }
    true
  }

  /**
   * Creates a new vehicle target at the specified coordinates.
   * @param position the position the target will be created at
   * @param facingYaw the direction the target will be facing
   * @param airVehicle if this target is an aircraft
   */
  private def CreateVehicleTarget(position: Vector3, facingYaw: Float, airVehicle: Boolean): Unit = {
    val definition = if (airVehicle) Random.nextBoolean() match {
      case true => GlobalDefinitions.lightgunship
      case false => GlobalDefinitions.mosquito
    } else Random.nextInt(4) match {
      case 0 => GlobalDefinitions.lightning
      case 1 => GlobalDefinitions.quadassault
      case 2 => GlobalDefinitions.quadstealth
      case 3 => GlobalDefinitions.two_man_assault_buggy
    }
    val factionRNG = Random.nextBoolean()
    val faction = zone.id match {
      case "tzshtr" => if (factionRNG) PlanetSideEmpire.VS else PlanetSideEmpire.NC
      case "tzshnc" => if (factionRNG) PlanetSideEmpire.VS else PlanetSideEmpire.TR
      case "tzshvs" => if (factionRNG) PlanetSideEmpire.NC else PlanetSideEmpire.TR
    }

    val vehicle = Vehicle(definition)
    vehicle.Position = position
    vehicle.Orientation = Vector3(0f, 0f, facingYaw)
    vehicle.Faction = faction
    TaskWorkflow.execute(RegisterAndSpawnVehicle(vehicle))
  }

  /**
   * Registers the `Vehicle` object and spawns it into the zone.
   * @param vehicle the `Vehicle` object
   * @return a `TaskBundle` message
   */
  private def RegisterAndSpawnVehicle(vehicle: Vehicle): TaskBundle = {
    TaskBundle(
      new StraightforwardTask() {
        private val localVehicle = vehicle

        override def description(): String = s"register a ${localVehicle.Definition.Name}"

        def action(): Future[Any] = {
          zone.Transport ! Zone.Vehicle.Spawn(localVehicle)
          zone.VehicleEvents ! MessageEnvelope(
            zone.id,
            VehicleAction.LoadVehicle(
              localVehicle,
              localVehicle.Definition.ObjectId,
              localVehicle.GUID,
              localVehicle.Definition.Packet.ConstructorData(localVehicle).get
            )
          )
          activeVehicleTargets.addOne((localVehicle, localVehicle.Position))
          log.debug(s"Spawned a ${localVehicle.Faction} ${localVehicle.Definition.Name} in ${zone.id} at ${localVehicle.Position}")
          Future(true)
        }
      },
      List(GUIDTask.registerVehicle(zone.GUID, vehicle))
    )
  }

  private def OnVehicleTargetDeconstructed(vehicle: Vehicle): Unit = {
    activeVehicleTargets.find(_._1 == vehicle) match {
      case Some((target, pos)) => 
        val index = activeVehicleTargets.indexOf((target, pos))
        activeVehicleTargets.remove(index)
        context.system.scheduler.scheduleOnce(
          5.seconds,
          new Runnable() { override def run(): Unit = CreateVehicleTarget(pos, vehicle.Orientation.z, GlobalDefinitions.isFlightVehicle(vehicle.Definition)) }
        )
      case None =>
    }
  }
}
