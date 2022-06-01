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
      enum: IntEnum[A],
      ct: ClassTag[A]
  ): ConfigConvert[A] =
    viaNonEmptyStringOpt[A](
      v =>
        enum.values.toList.collectFirst {
          case e: ServerType if e.name == v            => e.asInstanceOf[A]
          case e: BattleRank if e.value.toString == v  => e.asInstanceOf[A]
          case e: CommandRank if e.value.toString == v => e.asInstanceOf[A]
          case e: Certification if e.name == v         => e.asInstanceOf[A]
        },
      _.value.toString
    )

  implicit def enumeratumConfigConvert[A <: EnumEntry](implicit
      enum: Enum[A],
      ct: ClassTag[A]
  ): ConfigConvert[A] =
    viaNonEmptyStringOpt[A](
      v =>
        enum.values.toList.collectFirst {
          case e if e.toString.toLowerCase == v.toLowerCase => e.asInstanceOf[A]
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
    case Left(failures) => {
      logger.error("Loading config failed")
      failures.toList.foreach { failure =>
        logger.error(failure.toString)
      }
      sys.exit(1)
    }
  }

  // Typed config object
  lazy val app: AppConfig = source.load[AppConfig] match {
    case Right(config) => config
    case Left(failures) => {
      logger.error("Loading config failed")
      failures.toList.foreach { failure =>
        logger.error(failure.toString)
      }
      sys.exit(1)
    }
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
    inReorderTimeout: FiniteDuration,
    inSubslotMissingDelay: FiniteDuration,
    inSubslotMissingAttempts: Int
)

case class SessionConfig(
    inboundGraceTime: FiniteDuration,
    outboundGraceTime: FiniteDuration
)

case class GameConfig(
    instantActionAms: Boolean,
    amenityAutorepairRate: Float,
    amenityAutorepairDrainRate: Float,
    bepRate: Double,
    cepRate: Double,
    newAvatar: NewAvatar,
    hart: HartConfig,
    sharedMaxCooldown: Boolean,
    baseCertifications: Seq[Certification],
    warpGates: WarpGateConfig,
    cavernRotation: CavernRotationConfig
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
