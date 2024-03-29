package net.psforever.util

import java.nio.file.Paths
import com.typesafe.config.{Config => TypesafeConfig}
import enumeratum.{Enum, EnumEntry}
import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.objects.avatar.{BattleRank, Certification, CommandRank}
import net.psforever.packet.game.ServerType
import net.psforever.types.ChatMessageType
import pureconfig.ConfigConvert.viaNonEmptyStringOpt
import pureconfig.{ConfigConvert, ConfigSource}
import scala.concurrent.duration._
import scala.reflect.ClassTag
import pureconfig.generic.auto._ // intellij: this is not unused

object Config {
  private val logger = org.log4s.getLogger

  // prog.home is defined when we are running from SBT pack
  val directory: String = System.getProperty("prog.home") match {
    case null =>
      Paths.get("config").toAbsolutePath.toString
    case home =>
      Paths.get(home, "config").toAbsolutePath.toString
  }

  implicit def enumeratumIntConfigConvert[A <: IntEnumEntry](implicit
      e: IntEnum[A],
      ct: ClassTag[A]
  ): ConfigConvert[A] =
    viaNonEmptyStringOpt[A](
      v =>
        e.values.toList.collectFirst {
          case e: ServerType if e.name == v            => e.asInstanceOf[A]
          case e: BattleRank if e.value.toString == v  => e.asInstanceOf[A]
          case e: CommandRank if e.value.toString == v => e.asInstanceOf[A]
          case e: Certification if e.name == v         => e.asInstanceOf[A]
        },
      _.value.toString
    )

  implicit def enumeratumConfigConvert[A <: EnumEntry](implicit
      e: Enum[A],
      ct: ClassTag[A]
  ): ConfigConvert[A] =
    viaNonEmptyStringOpt[A](
      v =>
        e.values.toList.collectFirst {
          case e if e.toString.toLowerCase == v.toLowerCase => e
        },
      _.toString
    )

  private val source = {
    val configFile = Paths.get(directory, "psforever.conf").toFile
    if (configFile.exists)
      ConfigSource.file(configFile).withFallback(ConfigSource.default)
    else
      ConfigSource.default
  }

  // Raw config object - prefer app when possible
  lazy val config: TypesafeConfig = source.config() match {
    case Right(config) => config
    case Left(failures) =>
      logger.error("Loading config failed")
      failures.toList.foreach { failure =>
        logger.error(failure.toString)
      }
      sys.exit(1)
  }

  // Typed config object
  lazy val app: AppConfig = source.load[AppConfig] match {
    case Right(config) => config
    case Left(failures) =>
      logger.error("Loading config failed")
      failures.toList.foreach { failure =>
        logger.error(failure.toString)
      }
      sys.exit(1)
  }
}

case class AppConfig(
    bind: String,
    public: String,
    login: LoginConfig,
    world: WorldConfig,
    admin: AdminConfig,
    database: DatabaseConfig,
    game: GameConfig,
    antiCheat: AntiCheatConfig,
    network: NetworkConfig,
    development: DevelopmentConfig,
    kamon: KamonConfig,
    sentry: SentryConfig
)

case class LoginConfig(
    port: Int,
    createMissingAccounts: Boolean
)

case class WorldConfig(
    port: Int,
    serverName: String,
    serverType: ServerType
)

case class AdminConfig(
    port: Int,
    bind: String
)

case class DatabaseConfig(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String,
    sslmode: String
) {
  def toJdbc = s"jdbc:postgresql://$host:$port/$database"
}

case class AntiCheatConfig(
    hitPositionDiscrepancyThreshold: Int
)

case class NetworkConfig(
    session: SessionConfig,
    middleware: MiddlewareConfig
)

case class MiddlewareConfig(
    packetBundlingDelay: FiniteDuration,
    packetBundlingDelayMultiplier: Float,
    inReorderTimeout: FiniteDuration,
    inSubslotMissingDelay: FiniteDuration,
    inSubslotMissingAttempts: Int
)

case class SessionConfig(
    inboundGraceTime: FiniteDuration,
    outboundGraceTime: FiniteDuration
)

case class GameConfig(
    instantAction: InstantActionConfig,
    amenityAutorepairRate: Float,
    amenityAutorepairDrainRate: Float,
    newAvatar: NewAvatar,
    hart: HartConfig,
    sharedMaxCooldown: Boolean,
    sharedBfrCooldown: Boolean,
    baseCertifications: Seq[Certification],
    warpGates: WarpGateConfig,
    cavernRotation: CavernRotationConfig,
    savedMsg: SavedMessageEvents,
    playerDraw: PlayerStateDrawSettings,
    doorsCanBeOpenedByMedAppFromThisDistance: Float,
    experience: Experience,
    maxBattleRank: Int,
    promotion: PromotionSystem
)

case class InstantActionConfig(
    spawnOnAms: Boolean,
    thirdParty: Boolean
)

case class NewAvatar(
    br: BattleRank,
    cr: CommandRank
)

case class HartConfig(
    inFlightDuration: Long,
    boardingDuration: Long
)

case class DevelopmentConfig(
    unprivilegedGmCommands: Seq[ChatMessageType],
    unprivilegedGmBangCommands: Seq[String],
    netSim: NetSimConfig
)

case class NetSimConfig(
    enable: Boolean,
    loss: Double,
    delay: Duration,
    reorderChance: Double,
    reorderTime: Duration
)

case class KamonConfig(
    enable: Boolean
)

case class SentryConfig(
    enable: Boolean,
    dsn: String
)

case class WarpGateConfig(
    defaultToSanctuaryDestination: Boolean,
    broadcastBetweenConflictedFactions: Boolean
)

case class CavernRotationConfig(
    hoursBetweenRotation: Float,
    simultaneousUnlockedZones: Int,
    enhancedRotationOrder: Seq[Int],
    forceRotationImmediately: Boolean
)

case class SavedMessageEvents(
    short: SavedMessageTimings,
    renewal: SavedMessageTimings,
    interruptedByAction: SavedMessageTimings
)

case class SavedMessageTimings(
    fixed: Long,
    variable: Long
)

case class PlayerStateDrawSettings(
    populationThreshold: Int,
    populationStep: Int,
    rangeMin: Int,
    rangeMax: Int,
    rangeStep: Int,
    ranges: Seq[Int],
    delayMax: Long,
    delays: Seq[Long]
) {
  assert(ranges.nonEmpty)
  assert(ranges.size == delays.size)
}

case class Experience(
    shortContributionTime: Long,
    longContributionTime: Long,
    bep: BattleExperiencePoints,
    sep: SupportExperiencePoints,
    cep: CommandExperiencePoints,
    facilityCaptureRate: Float
) {
  assert(shortContributionTime < longContributionTime)
}

case class ThreatAssessment(
    id: Int,
    value: Float
)

case class ThreatLevel(
    id: Int,
    level: Long
)

case class BattleExperiencePoints(
    rate: Float,
    base: BattleExperiencePointsBase,
    lifeSpan: BattleExperiencePointsLifespan,
    revenge: BattleExperiencePointsRevenge
)

case class BattleExperiencePointsBase(
    bopsMultiplier: Long,
    asMax: Long,
    withKills: Long,
    asMounted: Long,
    mature: Long,
    maturityTime: Long
)

case class BattleExperiencePointsLifespan(
    lifeSpanThreatRate: Float,
    threatAssessmentOf: List[ThreatAssessment],
    maxThreatLevel: List[ThreatLevel]
)

case class BattleExperiencePointsRevenge(
    rate: Float,
    defaultExperience: Long,
    maxExperience: Long
)

case class SupportExperiencePoints(
    rate: Float,
    ntuSiloDepositReward: Long,
    canNotFindEventDefaultValue: Long,
    events: Seq[SupportExperienceEvent]
)

case class SupportExperienceEvent(
    name: String,
    base: Long,
    shotsMax: Int = 50,
    shotsCutoff: Int = 50,
    shotsMultiplier: Float = 0f,
    amountMultiplier: Float = 0f
)

case class CommandExperiencePoints(
    rate: Float,
    lluCarrierModifier: Float,
    lluSlayerCreditDuration: Duration,
    lluSlayerCredit: Long,
    maximumPerSquadSize: Seq[Int],
    squadSizeLimitOverflow: Int,
    squadSizeLimitOverflowMultiplier: Float
)

case class PromotionSystem(
    active: Boolean,
    broadcastBattleRank: Int,
    resetBattleRank: Int,
    maxBattleRank: Int,
    battleExperiencePointsModifier: Float,
    supportExperiencePointsModifier: Float,
    captureExperiencePointsModifier: Float
)
