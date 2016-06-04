// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import scodec.{Attempt, Codec, DecodeResult, Err}
import scodec.bits.BitVector
import scodec.codecs._

object GamePacketOpcode extends Enumeration {
  type Type = Value
  val

  // Opcodes should have a marker every 10 (decimal)
  // OPCODE 0
  Unknown0,
  LoginMessage,
  LoginRespMessage,
  ConnectToWorldRequestMessage, // found by searching for 83 F8 03 89 in IDA
  ConnectToWorldMessage,
  VNLWorldStatusMessage,
  UnknownMessage6,
  UnknownMessage7,
  PlayerStateMessage,
  UnknownMessage9,

  // OPCODE 10
  HitHint,
  DamageMessage,
  DestroyMessage,
  ReloadMessage,
  MountVehicleMsg,
  DismountVehicleMsg,
  UseItemMessage,
  UnknownMessage17,
  ChatMsg,
  CharacterNoRecordMessage,

  // OPCODE 20
  CharacterInfoMessage,
  UnknownMessage21,
  BindPlayerMessage,
  UnknownMessage23,
  ObjectCreateMessage,
  ObjectDeleteMessage,
  UnknownMessage26,
  VehicleStateMessage,
  FrameVehicleStateMessage,
  GenericObjectStateMsg,

  // OPCODE 30
  UnknownMessage30,
  ActionResultMessage,
  UnknownMessage32,
  ActionProgressMessage,
  ActionCancelMessage,
  ActionCancelAcknowledgeMessage,
  SetEmpireMessage,
  EmoteMsg,
  UnuseItemMessage,
  ObjectDetachMessage,

  // OPCODE 40
  CreateShortcutMessage,
  ChangeShortcutBankMessage,
  ObjectAttachMessage,
  UnknownMessage43,
  PlanetsideAttributeMessage,
  UnknownMessage45,
  UnknownMessage46,
  CharacterCreateRequestMessage,
  CharacterRequestMessage,
  LoadMapMessage,

  // OPCODE 50
  PlayerAvatarChangedMessage,
  ObjectHeldMessage,
  WeaponFireMessage,
  UnknownMessage53,
  UnknownMessage54,
  UnknownMessage55,
  InventoryStateMessage,
  UnknownMessage57,
  ChangeFireStateMessage,
  UnknownMessage59,

  // OPCODE 60
  UnknownMessage60,
  QuantityUpdateMessage,
  ArmorChangedMessage,
  ProjectileStateMessage,
  MountVehicleCargoMsg,
  DismountVehicleCargoMsg,
  CargoMountPointStatusMessage,
  BeginZoningMessage,
  UnknownMessage68,
  ItemTransactionResultMessage,

  // OPCODE 70
  ChangeFireModeMessage,
  ChangeAmmoMessage,
  TimeOfDayMessage,
  UnknownMessage73,
  SpawnRequestMessage,
  DeployRequestMessage,
  UnknownMessage76,
  RepairMessage,
  ServerVehicleOverrideMsg,
  UnknownMessage79,

  // OPCODE 80
  TargetingInfoMessage,
  TriggerEffectMessage,
  WeaponDryFireMessage,
  UnknownMessage83,
  HackMessage,
  DroppodLaunchResponseMessage,
  GenericObjectActionMessage,
  AvatarVehicleTimerMessage,
  AvatarImplantMessage,
  UnknownMessage89,

  // OPCODE 90
  DelayedPathMountMsg,
  OrbitalShuttleTimeMsg,
  UnknownMessage92,
  UnknownMessage93,
  UnknownMessage94,
  FavoritesResponse,
  FavoritesMessage,
  ObjectDetectedMessage,
  UnknownMessage98,
  SetChatFilterMessage,

  // OPCODE 100
  AvatarSearchCriteriaMessage,
  AvatarSearchResponse,
  WeaponJammedMessage,
  LinkDeadAwarenessMsg,
  DroppodFreefallingMessage,
  AvatarFirstTimeEventMessage,
  AggravatedDamageMessage,
  TriggerSoundMessage,
  UnknownMessage108,
  UnknownMessage109,

  // OPCODE 110
  UnknownMessage110,
  SquadMembershipResponse,
  SquadMemberEvent,
  PlatoonEvent,
  UnknownMessage114,
  FriendsResponse,
  TriggerEnvironmentalDamageMessage,
  TrainingZoneMessage,
  DeployableObjectsInfoMessage,
  SquadState,

  // OPCODE 120
  OxygenStateMessage,
  TradeMessage,
  UnknownMessage122,
  DamageFeedbackMessage,
  UnknownMessage124,
  UnknownMessage125,
  UnknownMessage126,
  AvatarStatisticsMessage,
  GenericObjectAction2Message,
  DestroyDisplayMessage,

  // OPCODE 130
  TriggerBotAction,
  UnknownMessage131,
  SquadWaypointEvent,
  OffshoreVehicleMessage,
  ObjectDeployedMessage,
  ObjectDeployedCountMessage,
  UnknownMessage136,
  UnknownMessage137,
  PlayerStasisMessage,
  UnknownMessage139,

  // OPCODE 140
  UnknownMessage140,
  OutfitMembershipResponse,
  UnknownMessage142,
  OutfitEvent,
  OutfitMemberEvent,
  OutfitMemberUpdate,
  PlanetsideStringAttributeMessage,
  DataChallengeMessage,
  UnknownMessage148,
  WeatherMessage,

  // OPCODE 150
  SimDataChallengeResp,
  UnknownMessage151,
  OutfitListEvent,
  EmpireIncentivesMessage,
  UnknownMessage154,
  SyncMessage,
  DebugDrawMessage,
  SoulMarkMessage,
  UplinkPositionEvent,
  HotSpotUpdateMessage,

  // OPCODE 160
  BuildingInfoUpdateMessage,
  FireHintMessage,
  UnknownMessage162,
  UplinkResponse,
  UnknownMessage164,
  WarpgateResponse,
  DamageWithPositionMessage,
  GenericActionMessage,
  ContinentalLockUpdateMessage,
  AvatarGrenadeStateMessage,

  // OPCODE 170
  UnknownMessage170,
  UnknownMessage171,
  UnknownMessage172,
  AvatarDeadStateMessage,
  CSAssistMessage,
  CSAssistCommentMessage,
  UnknownMessage176,
  UnknownMessage177,
  VoiceHostInfo,
  BattleplanMessage,

  // OPCODE 180
  BattleExperienceMessage,
  UnknownMessage181,
  ZonePopulationUpdateMessage,
  DisconnectMessage,
  ExperienceAddedMessage,
  OrbitalStrikeWaypointMessage,
  KeepAliveMessage,
  MapObjectStateBlockMessage,
  SnoopMsg,
  PlayerStateMessageUpstream,

  // OPCODE 190
  PlayerStateShiftMessage,
  ZipLineMessage,
  CaptureFlagUpdateMessage,
  VanuModuleUpdateMessage,
  UnknownMessage194,
  ProximityTerminalUseMessage,
  QuantityDeltaUpdateMessage,
  ChainLashMessage,
  ZoneInfoMessage,
  UnknownMessage199,

  // OPCODE 200
  WeaponLazeTargetPositionMessage,
  ModuleLimitsMessage,
  OutfitBenefitMessage,
  EmpireChangeTimeMessage,
  ClockCalibrationMessage,
  DensityLevelUpdateMessage,
  ActOfGodMessage,
  AvatarAwardMessage,
  UnknownMessage208,
  DisplayedAwardMessage,

  // OPCODE 210
  RespawnAMSInfoMessage,
  ComponentDamageMessage,
  UnknownMessage212,
  PropertyOverrideMessage,
  WarpgateLinkOverrideMessage,
  EmpireBenefitsMessage,
  ForceEmpireMessage,
  BroadcastWarpgateUpdateMessage,
  UnknownMessage218,
  UnknownMessage219,

  // OPCODE 220
  SquadMainTerminalResponseMessage,
  SquadOrderMessage,
  UnknownMessage222,
  ZoneLockInfoMessage,
  SquadBindInfoMessage,
  AudioSequenceMessage,
  SquadFacilityBindInfoMessage,
  ZoneForcedCavernConnectionsMessage,
  MissionActionMessage,
  UnknownMessage229,

  // OPCODE 230
  ReplicationStreamMessage,
  SquadDefinitionActionMessage,
  SquadDetailDefinitionUpdateMessage,
  TacticsMessage,
  RabbitUpdateMessage,
  SquadInvitationRequestMessage,
  CharacterKnowledgeMessage,
  GameScoreUpdateMessage,
  UnknownMessage238,
  UnknownMessage239,

  // OPCODE 240
  QueueTimedHelpMessage,
  MailMessage,
  UnknownMessage242,
  ClientCheatedMessage,
  UnknownMessage244,
  UnknownMessage245,
  UnknownMessage246,
  UnknownMessage247,
  UnknownMessage248,
  UnknownMessage249,

  // OPCODE 250
  UnknownMessage250,
  UnknownMessage251,
  UnknownMessage252,
  UnknownMessage253,
  UnknownMessage254,
  UnknownMessage255
  = Value

  private def noDecoder(opcode : GamePacketOpcode.Type) = (a : BitVector) =>
    Attempt.failure(Err(s"Could not find a marshaller for game packet ${opcode}"))

  def getPacketDecoder(opcode : GamePacketOpcode.Type) : (BitVector) => Attempt[DecodeResult[PlanetSideGamePacket]] = opcode match {
      // OPCODE 0
      case Unknown0 => noDecoder(opcode)
      case LoginMessage => game.LoginMessage.decode
      case LoginRespMessage => game.LoginRespMessage.decode
      case ConnectToWorldRequestMessage => game.ConnectToWorldRequestMessage.decode
      case ConnectToWorldMessage => game.ConnectToWorldMessage.decode
      case VNLWorldStatusMessage => game.VNLWorldStatusMessage.decode
      case UnknownMessage6 => noDecoder(opcode)
      case UnknownMessage7 => noDecoder(opcode)
      case PlayerStateMessage => noDecoder(opcode)
      case UnknownMessage9 => noDecoder(opcode)

      // OPCODE 10
      case HitHint => noDecoder(opcode)
      case DamageMessage => noDecoder(opcode)
      case DestroyMessage => noDecoder(opcode)
      case ReloadMessage => noDecoder(opcode)
      case MountVehicleMsg => noDecoder(opcode)
      case DismountVehicleMsg => noDecoder(opcode)
      case UseItemMessage => noDecoder(opcode)
      case UnknownMessage17 => noDecoder(opcode)
      case ChatMsg => noDecoder(opcode)
      case CharacterNoRecordMessage => noDecoder(opcode)

      // OPCODE 20
      case CharacterInfoMessage => game.CharacterInfoMessage.decode
      case UnknownMessage21 => noDecoder(opcode)
      case BindPlayerMessage => noDecoder(opcode)
      case UnknownMessage23 => noDecoder(opcode)
      case ObjectCreateMessage => noDecoder(opcode)
      case ObjectDeleteMessage => noDecoder(opcode)
      case UnknownMessage26 => noDecoder(opcode)
      case VehicleStateMessage => noDecoder(opcode)
      case FrameVehicleStateMessage => noDecoder(opcode)
      case GenericObjectStateMsg => noDecoder(opcode)

      // OPCODE 30
      case UnknownMessage30 => noDecoder(opcode)
      case ActionResultMessage => game.ActionResultMessage.decode
      case UnknownMessage32 => noDecoder(opcode)
      case ActionProgressMessage => noDecoder(opcode)
      case ActionCancelMessage => noDecoder(opcode)
      case ActionCancelAcknowledgeMessage => noDecoder(opcode)
      case SetEmpireMessage => noDecoder(opcode)
      case EmoteMsg => noDecoder(opcode)
      case UnuseItemMessage => noDecoder(opcode)
      case ObjectDetachMessage => noDecoder(opcode)

      // OPCODE 40
      case CreateShortcutMessage => noDecoder(opcode)
      case ChangeShortcutBankMessage => noDecoder(opcode)
      case ObjectAttachMessage => noDecoder(opcode)
      case UnknownMessage43 => noDecoder(opcode)
      case PlanetsideAttributeMessage => noDecoder(opcode)
      case UnknownMessage45 => noDecoder(opcode)
      case UnknownMessage46 => noDecoder(opcode)
      case CharacterCreateRequestMessage => game.CharacterCreateRequestMessage.decode
      case CharacterRequestMessage => game.CharacterRequestMessage.decode
      case LoadMapMessage => noDecoder(opcode)

      // OPCODE 50
      case PlayerAvatarChangedMessage => noDecoder(opcode)
      case ObjectHeldMessage => noDecoder(opcode)
      case WeaponFireMessage => noDecoder(opcode)
      case UnknownMessage53 => noDecoder(opcode)
      case UnknownMessage54 => noDecoder(opcode)
      case UnknownMessage55 => noDecoder(opcode)
      case InventoryStateMessage => noDecoder(opcode)
      case UnknownMessage57 => noDecoder(opcode)
      case ChangeFireStateMessage => noDecoder(opcode)
      case UnknownMessage59 => noDecoder(opcode)

      // OPCODE 60
      case UnknownMessage60 => noDecoder(opcode)
      case QuantityUpdateMessage => noDecoder(opcode)
      case ArmorChangedMessage => noDecoder(opcode)
      case ProjectileStateMessage => noDecoder(opcode)
      case MountVehicleCargoMsg => noDecoder(opcode)
      case DismountVehicleCargoMsg => noDecoder(opcode)
      case CargoMountPointStatusMessage => noDecoder(opcode)
      case BeginZoningMessage => noDecoder(opcode)
      case UnknownMessage68 => noDecoder(opcode)
      case ItemTransactionResultMessage => noDecoder(opcode)

      // OPCODE 70
      case ChangeFireModeMessage => noDecoder(opcode)
      case ChangeAmmoMessage => noDecoder(opcode)
      case TimeOfDayMessage => noDecoder(opcode)
      case UnknownMessage73 => noDecoder(opcode)
      case SpawnRequestMessage => noDecoder(opcode)
      case DeployRequestMessage => noDecoder(opcode)
      case UnknownMessage76 => noDecoder(opcode)
      case RepairMessage => noDecoder(opcode)
      case ServerVehicleOverrideMsg => noDecoder(opcode)
      case UnknownMessage79 => noDecoder(opcode)

      // OPCODE 80
      case TargetingInfoMessage => noDecoder(opcode)
      case TriggerEffectMessage => noDecoder(opcode)
      case WeaponDryFireMessage => noDecoder(opcode)
      case UnknownMessage83 => noDecoder(opcode)
      case HackMessage => noDecoder(opcode)
      case DroppodLaunchResponseMessage => noDecoder(opcode)
      case GenericObjectActionMessage => noDecoder(opcode)
      case AvatarVehicleTimerMessage => noDecoder(opcode)
      case AvatarImplantMessage => noDecoder(opcode)
      case UnknownMessage89 => noDecoder(opcode)

      // OPCODE 90
      case DelayedPathMountMsg => noDecoder(opcode)
      case OrbitalShuttleTimeMsg => noDecoder(opcode)
      case UnknownMessage92 => noDecoder(opcode)
      case UnknownMessage93 => noDecoder(opcode)
      case UnknownMessage94 => noDecoder(opcode)
      case FavoritesResponse => noDecoder(opcode)
      case FavoritesMessage => noDecoder(opcode)
      case ObjectDetectedMessage => noDecoder(opcode)
      case UnknownMessage98 => noDecoder(opcode)
      case SetChatFilterMessage => noDecoder(opcode)

      // OPCODE 100
      case AvatarSearchCriteriaMessage => noDecoder(opcode)
      case AvatarSearchResponse => noDecoder(opcode)
      case WeaponJammedMessage => noDecoder(opcode)
      case LinkDeadAwarenessMsg => noDecoder(opcode)
      case DroppodFreefallingMessage => noDecoder(opcode)
      case AvatarFirstTimeEventMessage => noDecoder(opcode)
      case AggravatedDamageMessage => noDecoder(opcode)
      case TriggerSoundMessage => noDecoder(opcode)
      case UnknownMessage108 => noDecoder(opcode)
      case UnknownMessage109 => noDecoder(opcode)

      // OPCODE 110
      case UnknownMessage110 => noDecoder(opcode)
      case SquadMembershipResponse => noDecoder(opcode)
      case SquadMemberEvent => noDecoder(opcode)
      case PlatoonEvent => noDecoder(opcode)
      case UnknownMessage114 => noDecoder(opcode)
      case FriendsResponse => noDecoder(opcode)
      case TriggerEnvironmentalDamageMessage => noDecoder(opcode)
      case TrainingZoneMessage => noDecoder(opcode)
      case DeployableObjectsInfoMessage => noDecoder(opcode)
      case SquadState => noDecoder(opcode)

      // OPCODE 120
      case OxygenStateMessage => noDecoder(opcode)
      case TradeMessage => noDecoder(opcode)
      case UnknownMessage122 => noDecoder(opcode)
      case DamageFeedbackMessage => noDecoder(opcode)
      case UnknownMessage124 => noDecoder(opcode)
      case UnknownMessage125 => noDecoder(opcode)
      case UnknownMessage126 => noDecoder(opcode)
      case AvatarStatisticsMessage => noDecoder(opcode)
      case GenericObjectAction2Message => noDecoder(opcode)
      case DestroyDisplayMessage => noDecoder(opcode)

      // OPCODE 130
      case TriggerBotAction => noDecoder(opcode)
      case UnknownMessage131 => noDecoder(opcode)
      case SquadWaypointEvent => noDecoder(opcode)
      case OffshoreVehicleMessage => noDecoder(opcode)
      case ObjectDeployedMessage => noDecoder(opcode)
      case ObjectDeployedCountMessage => noDecoder(opcode)
      case UnknownMessage136 => noDecoder(opcode)
      case UnknownMessage137 => noDecoder(opcode)
      case PlayerStasisMessage => noDecoder(opcode)
      case UnknownMessage139 => noDecoder(opcode)

      // OPCODE 140
      case UnknownMessage140 => noDecoder(opcode)
      case OutfitMembershipResponse => noDecoder(opcode)
      case UnknownMessage142 => noDecoder(opcode)
      case OutfitEvent => noDecoder(opcode)
      case OutfitMemberEvent => noDecoder(opcode)
      case OutfitMemberUpdate => noDecoder(opcode)
      case PlanetsideStringAttributeMessage => noDecoder(opcode)
      case DataChallengeMessage => noDecoder(opcode)
      case UnknownMessage148 => noDecoder(opcode)
      case WeatherMessage => noDecoder(opcode)

      // OPCODE 150
      case SimDataChallengeResp => noDecoder(opcode)
      case UnknownMessage151 => noDecoder(opcode)
      case OutfitListEvent => noDecoder(opcode)
      case EmpireIncentivesMessage => noDecoder(opcode)
      case UnknownMessage154 => noDecoder(opcode)
      case SyncMessage => noDecoder(opcode)
      case DebugDrawMessage => noDecoder(opcode)
      case SoulMarkMessage => noDecoder(opcode)
      case UplinkPositionEvent => noDecoder(opcode)
      case HotSpotUpdateMessage => noDecoder(opcode)

      // OPCODE 160
      case BuildingInfoUpdateMessage => noDecoder(opcode)
      case FireHintMessage => noDecoder(opcode)
      case UnknownMessage162 => noDecoder(opcode)
      case UplinkResponse => noDecoder(opcode)
      case UnknownMessage164 => noDecoder(opcode)
      case WarpgateResponse => noDecoder(opcode)
      case DamageWithPositionMessage => noDecoder(opcode)
      case GenericActionMessage => noDecoder(opcode)
      case ContinentalLockUpdateMessage => noDecoder(opcode)
      case AvatarGrenadeStateMessage => noDecoder(opcode)

      // OPCODE 170
      case UnknownMessage170 => noDecoder(opcode)
      case UnknownMessage171 => noDecoder(opcode)
      case UnknownMessage172 => noDecoder(opcode)
      case AvatarDeadStateMessage => noDecoder(opcode)
      case CSAssistMessage => noDecoder(opcode)
      case CSAssistCommentMessage => noDecoder(opcode)
      case UnknownMessage176 => noDecoder(opcode)
      case UnknownMessage177 => noDecoder(opcode)
      case VoiceHostInfo => noDecoder(opcode)
      case BattleplanMessage => noDecoder(opcode)

      // OPCODE 180
      case BattleExperienceMessage => noDecoder(opcode)
      case UnknownMessage181 => noDecoder(opcode)
      case ZonePopulationUpdateMessage => noDecoder(opcode)
      case DisconnectMessage => noDecoder(opcode)
      case ExperienceAddedMessage => noDecoder(opcode)
      case OrbitalStrikeWaypointMessage => noDecoder(opcode)
      case KeepAliveMessage => game.KeepAliveMessage.decode
      case MapObjectStateBlockMessage => noDecoder(opcode)
      case SnoopMsg => noDecoder(opcode)
      case PlayerStateMessageUpstream => noDecoder(opcode)

      // OPCODE 190
      case PlayerStateShiftMessage => noDecoder(opcode)
      case ZipLineMessage => noDecoder(opcode)
      case CaptureFlagUpdateMessage => noDecoder(opcode)
      case VanuModuleUpdateMessage => noDecoder(opcode)
      case UnknownMessage194 => noDecoder(opcode)
      case ProximityTerminalUseMessage => noDecoder(opcode)
      case QuantityDeltaUpdateMessage => noDecoder(opcode)
      case ChainLashMessage => noDecoder(opcode)
      case ZoneInfoMessage => noDecoder(opcode)
      case UnknownMessage199 => noDecoder(opcode)

      // OPCODE 200
      case WeaponLazeTargetPositionMessage => noDecoder(opcode)
      case ModuleLimitsMessage => noDecoder(opcode)
      case OutfitBenefitMessage => noDecoder(opcode)
      case EmpireChangeTimeMessage => noDecoder(opcode)
      case ClockCalibrationMessage => noDecoder(opcode)
      case DensityLevelUpdateMessage => noDecoder(opcode)
      case ActOfGodMessage => noDecoder(opcode)
      case AvatarAwardMessage => noDecoder(opcode)
      case UnknownMessage208 => noDecoder(opcode)
      case DisplayedAwardMessage => noDecoder(opcode)

      // OPCODE 210
      case RespawnAMSInfoMessage => noDecoder(opcode)
      case ComponentDamageMessage => noDecoder(opcode)
      case UnknownMessage212 => noDecoder(opcode)
      case PropertyOverrideMessage => noDecoder(opcode)
      case WarpgateLinkOverrideMessage => noDecoder(opcode)
      case EmpireBenefitsMessage => noDecoder(opcode)
      case ForceEmpireMessage => noDecoder(opcode)
      case BroadcastWarpgateUpdateMessage => noDecoder(opcode)
      case UnknownMessage218 => noDecoder(opcode)
      case UnknownMessage219 => noDecoder(opcode)

      // OPCODE 220
      case SquadMainTerminalResponseMessage => noDecoder(opcode)
      case SquadOrderMessage => noDecoder(opcode)
      case UnknownMessage222 => noDecoder(opcode)
      case ZoneLockInfoMessage => noDecoder(opcode)
      case SquadBindInfoMessage => noDecoder(opcode)
      case AudioSequenceMessage => noDecoder(opcode)
      case SquadFacilityBindInfoMessage => noDecoder(opcode)
      case ZoneForcedCavernConnectionsMessage => noDecoder(opcode)
      case MissionActionMessage => noDecoder(opcode)
      case UnknownMessage229 => noDecoder(opcode)

      // OPCODE 230
      case ReplicationStreamMessage => noDecoder(opcode)
      case SquadDefinitionActionMessage => noDecoder(opcode)
      case SquadDetailDefinitionUpdateMessage => noDecoder(opcode)
      case TacticsMessage => noDecoder(opcode)
      case RabbitUpdateMessage => noDecoder(opcode)
      case SquadInvitationRequestMessage => noDecoder(opcode)
      case CharacterKnowledgeMessage => noDecoder(opcode)
      case GameScoreUpdateMessage => noDecoder(opcode)
      case UnknownMessage238 => noDecoder(opcode)
      case UnknownMessage239 => noDecoder(opcode)

      // OPCODE 240
      case QueueTimedHelpMessage => noDecoder(opcode)
      case MailMessage => noDecoder(opcode)
      case UnknownMessage242 => noDecoder(opcode)
      case ClientCheatedMessage => noDecoder(opcode)
      case UnknownMessage244 => noDecoder(opcode)
      case UnknownMessage245 => noDecoder(opcode)
      case UnknownMessage246 => noDecoder(opcode)
      case UnknownMessage247 => noDecoder(opcode)
      case UnknownMessage248 => noDecoder(opcode)
      case UnknownMessage249 => noDecoder(opcode)

      // OPCODE 250
      case UnknownMessage250 => noDecoder(opcode)
      case UnknownMessage251 => noDecoder(opcode)
      case UnknownMessage252 => noDecoder(opcode)
      case UnknownMessage253 => noDecoder(opcode)
      case UnknownMessage254 => noDecoder(opcode)
      case UnknownMessage255 => noDecoder(opcode)
      case default => noDecoder(opcode)
  }

  implicit val codec: Codec[this.Value] = PacketHelpers.createEnumerationCodec(this, uint8L)
}
