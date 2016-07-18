// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import scodec.{Attempt, Codec, DecodeResult, Err}
import scodec.bits.BitVector
import scodec.codecs._

/**
  * The master list of Game packet opcodes that have been discovered in the PlanetSide client.
  *
  * UnknownMessage* means that there, to the best of our knowledge, was no opcode of this value.
  * This was double checked by extracting out the master case statement in PlanetsideComm::OnReceive
  * and by parsing NetMessage RTTI.
  *
  * Keep http://psforever.net/wiki/Game_Packets up-to-date with the decoding progress of each packet
  */
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
  HitMessage,

  // OPCODE 10
  HitHint,
  DamageMessage,
  DestroyMessage,
  ReloadMessage,
  MountVehicleMsg,
  DismountVehicleMsg,
  UseItemMessage,
  MoveItemMessage,
  ChatMsg,
  CharacterNoRecordMessage,

  // OPCODE 20
  CharacterInfoMessage,
  UnknownMessage21,
  BindPlayerMessage,
  ObjectCreateMessage_Duplicate,
  ObjectCreateMessage,
  ObjectDeleteMessage,
  PingMsg,
  VehicleStateMessage,
  FrameVehicleStateMessage,
  GenericObjectStateMsg,

  // OPCODE 30
  ChildObjectStateMessage,
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
  RequestDestroyMessage,
  UnknownMessage46,
  CharacterCreateRequestMessage,
  CharacterRequestMessage,
  LoadMapMessage,

  // OPCODE 50
  SetCurrentAvatarMessage,
  ObjectHeldMessage,
  WeaponFireMessage,
  AvatarJumpMessage,
  PickupItemMessage,
  DropItemMessage,
  InventoryStateMessage,
  ChangeFireStateMessage_Duplicate,
  ChangeFireStateMessage,
  UnknownMessage59,

  // OPCODE 60
  GenericCollisionMsg,
  QuantityUpdateMessage,
  ArmorChangedMessage,
  ProjectileStateMessage,
  MountVehicleCargoMsg,
  DismountVehicleCargoMsg,
  CargoMountPointStatusMessage,
  BeginZoningMessage,
  ItemTransactionMessage,
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
  LashMessage,

  // OPCODE 80
  TargetingInfoMessage,
  TriggerEffectMessage,
  WeaponDryFireMessage,
  DroppodLaunchRequestMessage,
  HackMessage,
  DroppodLaunchResponseMessage,
  GenericObjectActionMessage,
  AvatarVehicleTimerMessage,
  AvatarImplantMessage,
  UnknownMessage89,

  // OPCODE 90
  DelayedPathMountMsg,
  OrbitalShuttleTimeMsg,
  AIDamage,
  DeployObjectMessage,
  FavoritesRequest,
  FavoritesResponse,
  FavoritesMessage,
  ObjectDetectedMessage,
  SplashHitMessage,
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
  LootItemMessage,
  VehicleSubStateMessage,

  // OPCODE 110
  SquadMembershipRequest,
  SquadMembershipResponse,
  SquadMemberEvent,
  PlatoonEvent,
  FriendsRequest,
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
  DismountBuildingMsg,
  UnknownMessage125,
  UnknownMessage126,
  AvatarStatisticsMessage,
  GenericObjectAction2Message,
  DestroyDisplayMessage,

  // OPCODE 130
  TriggerBotAction,
  SquadWaypointRequest,
  SquadWaypointEvent,
  OffshoreVehicleMessage,
  ObjectDeployedMessage,
  ObjectDeployedCountMessage,
  WeaponDelayFireMessage,
  BugReportMessage,
  PlayerStasisMessage,
  UnknownMessage139,

  // OPCODE 140
  OutfitMembershipRequest,
  OutfitMembershipResponse,
  OutfitRequest,
  OutfitEvent,
  OutfitMemberEvent,
  OutfitMemberUpdate,
  PlanetsideStringAttributeMessage,
  DataChallengeMessage,
  DataChallengeMessageResp,
  WeatherMessage,

  // OPCODE 150
  SimDataChallenge,
  SimDataChallengeResp,
  OutfitListEvent,
  EmpireIncentivesMessage,
  InvalidTerrainMessage,
  SyncMessage,
  DebugDrawMessage,
  SoulMarkMessage,
  UplinkPositionEvent,
  HotSpotUpdateMessage,

  // OPCODE 160
  BuildingInfoUpdateMessage,
  FireHintMessage,
  UplinkRequest,
  UplinkResponse,
  WarpgateRequest,
  WarpgateResponse,
  DamageWithPositionMessage,
  GenericActionMessage,
  ContinentalLockUpdateMessage,
  AvatarGrenadeStateMessage,

  // OPCODE 170
  UnknownMessage170,
  UnknownMessage171,
  ReleaseAvatarRequestMessage,
  AvatarDeadStateMessage,
  CSAssistMessage,
  CSAssistCommentMessage,
  VoiceHostRequest,
  VoiceHostKill,
  VoiceHostInfo,
  BattleplanMessage,

  // OPCODE 180
  BattleExperienceMessage,
  TargetingImplantRequest,
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
  FacilityBenefitShieldChargeRequestMessage,
  ProximityTerminalUseMessage,
  QuantityDeltaUpdateMessage,
  ChainLashMessage,
  ZoneInfoMessage,
  LongRangeProjectileInfoMessage,

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
  GenericObjectActionAtPositionMessage,
  PropertyOverrideMessage,
  WarpgateLinkOverrideMessage,
  EmpireBenefitsMessage,
  ForceEmpireMessage,
  BroadcastWarpgateUpdateMessage,
  UnknownMessage218,
  SquadMainTerminalMessage,

  // OPCODE 220
  SquadMainTerminalResponseMessage,
  SquadOrderMessage,
  SquadOrderResponse,
  ZoneLockInfoMessage,
  SquadBindInfoMessage,
  AudioSequenceMessage,
  SquadFacilityBindInfoMessage,
  ZoneForcedCavernConnectionsMessage,
  MissionActionMessage,
  MissionKillTriggerMessage,

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
  OrderTerminalBugMessage,

  // OPCODE 240
  QueueTimedHelpMessage,
  MailMessage,
  GameVarUpdate,
  ClientCheatedMessage // last known message type (243, 0xf3)
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
      case HitMessage => noDecoder(opcode)

      // OPCODE 10
      case HitHint => noDecoder(opcode)
      case DamageMessage => noDecoder(opcode)
      case DestroyMessage => noDecoder(opcode)
      case ReloadMessage => noDecoder(opcode)
      case MountVehicleMsg => noDecoder(opcode)
      case DismountVehicleMsg => noDecoder(opcode)
      case UseItemMessage => noDecoder(opcode)
      case MoveItemMessage => noDecoder(opcode)
      case ChatMsg => game.ChatMsg.decode
      case CharacterNoRecordMessage => noDecoder(opcode)

      // OPCODE 20
      case CharacterInfoMessage => game.CharacterInfoMessage.decode
      case UnknownMessage21 => noDecoder(opcode)
      case BindPlayerMessage => noDecoder(opcode)
      case ObjectCreateMessage_Duplicate => noDecoder(opcode)
      case ObjectCreateMessage => game.ObjectCreateMessage.decode
      case ObjectDeleteMessage => noDecoder(opcode)
      case PingMsg => noDecoder(opcode)
      case VehicleStateMessage => noDecoder(opcode)
      case FrameVehicleStateMessage => noDecoder(opcode)
      case GenericObjectStateMsg => noDecoder(opcode)

      // OPCODE 30
      case ChildObjectStateMessage => noDecoder(opcode)
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
      case RequestDestroyMessage => noDecoder(opcode)
      case UnknownMessage46 => noDecoder(opcode)
      case CharacterCreateRequestMessage => game.CharacterCreateRequestMessage.decode
      case CharacterRequestMessage => game.CharacterRequestMessage.decode
      case LoadMapMessage => noDecoder(opcode)

      // OPCODE 50
      case SetCurrentAvatarMessage => game.SetCurrentAvatarMessage.decode
      case ObjectHeldMessage => noDecoder(opcode)
      case WeaponFireMessage => noDecoder(opcode)
      case AvatarJumpMessage => noDecoder(opcode)
      case PickupItemMessage => noDecoder(opcode)
      case DropItemMessage => noDecoder(opcode)
      case InventoryStateMessage => noDecoder(opcode)
      case ChangeFireStateMessage_Duplicate => noDecoder(opcode)
      case ChangeFireStateMessage => noDecoder(opcode)
      case UnknownMessage59 => noDecoder(opcode)

      // OPCODE 60
      case GenericCollisionMsg => noDecoder(opcode)
      case QuantityUpdateMessage => noDecoder(opcode)
      case ArmorChangedMessage => noDecoder(opcode)
      case ProjectileStateMessage => noDecoder(opcode)
      case MountVehicleCargoMsg => noDecoder(opcode)
      case DismountVehicleCargoMsg => noDecoder(opcode)
      case CargoMountPointStatusMessage => noDecoder(opcode)
      case BeginZoningMessage => noDecoder(opcode)
      case ItemTransactionMessage => noDecoder(opcode)
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
      case LashMessage => noDecoder(opcode)

      // OPCODE 80
      case TargetingInfoMessage => noDecoder(opcode)
      case TriggerEffectMessage => noDecoder(opcode)
      case WeaponDryFireMessage => noDecoder(opcode)
      case DroppodLaunchRequestMessage => noDecoder(opcode)
      case HackMessage => noDecoder(opcode)
      case DroppodLaunchResponseMessage => noDecoder(opcode)
      case GenericObjectActionMessage => noDecoder(opcode)
      case AvatarVehicleTimerMessage => noDecoder(opcode)
      case AvatarImplantMessage => noDecoder(opcode)
      case UnknownMessage89 => noDecoder(opcode)

      // OPCODE 90
      case DelayedPathMountMsg => noDecoder(opcode)
      case OrbitalShuttleTimeMsg => noDecoder(opcode)
      case AIDamage => noDecoder(opcode)
      case DeployObjectMessage => noDecoder(opcode)
      case FavoritesRequest => noDecoder(opcode)
      case FavoritesResponse => noDecoder(opcode)
      case FavoritesMessage => noDecoder(opcode)
      case ObjectDetectedMessage => noDecoder(opcode)
      case SplashHitMessage => noDecoder(opcode)
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
      case LootItemMessage => noDecoder(opcode)
      case VehicleSubStateMessage => noDecoder(opcode)

      // OPCODE 110
      case SquadMembershipRequest => noDecoder(opcode)
      case SquadMembershipResponse => noDecoder(opcode)
      case SquadMemberEvent => noDecoder(opcode)
      case PlatoonEvent => noDecoder(opcode)
      case FriendsRequest => noDecoder(opcode)
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
      case DismountBuildingMsg => noDecoder(opcode)
      case UnknownMessage125 => noDecoder(opcode)
      case UnknownMessage126 => noDecoder(opcode)
      case AvatarStatisticsMessage => noDecoder(opcode)
      case GenericObjectAction2Message => noDecoder(opcode)
      case DestroyDisplayMessage => noDecoder(opcode)

      // OPCODE 130
      case TriggerBotAction => noDecoder(opcode)
      case SquadWaypointRequest => noDecoder(opcode)
      case SquadWaypointEvent => noDecoder(opcode)
      case OffshoreVehicleMessage => noDecoder(opcode)
      case ObjectDeployedMessage => noDecoder(opcode)
      case ObjectDeployedCountMessage => noDecoder(opcode)
      case WeaponDelayFireMessage => noDecoder(opcode)
      case BugReportMessage => noDecoder(opcode)
      case PlayerStasisMessage => noDecoder(opcode)
      case UnknownMessage139 => noDecoder(opcode)

      // OPCODE 140
      case OutfitMembershipRequest => noDecoder(opcode)
      case OutfitMembershipResponse => noDecoder(opcode)
      case OutfitRequest => noDecoder(opcode)
      case OutfitEvent => noDecoder(opcode)
      case OutfitMemberEvent => noDecoder(opcode)
      case OutfitMemberUpdate => noDecoder(opcode)
      case PlanetsideStringAttributeMessage => noDecoder(opcode)
      case DataChallengeMessage => noDecoder(opcode)
      case DataChallengeMessageResp => noDecoder(opcode)
      case WeatherMessage => noDecoder(opcode)

      // OPCODE 150
      case SimDataChallenge => noDecoder(opcode)
      case SimDataChallengeResp => noDecoder(opcode)
      case OutfitListEvent => noDecoder(opcode)
      case EmpireIncentivesMessage => noDecoder(opcode)
      case InvalidTerrainMessage => noDecoder(opcode)
      case SyncMessage => noDecoder(opcode)
      case DebugDrawMessage => noDecoder(opcode)
      case SoulMarkMessage => noDecoder(opcode)
      case UplinkPositionEvent => noDecoder(opcode)
      case HotSpotUpdateMessage => noDecoder(opcode)

      // OPCODE 160
      case BuildingInfoUpdateMessage => noDecoder(opcode)
      case FireHintMessage => noDecoder(opcode)
      case UplinkRequest => noDecoder(opcode)
      case UplinkResponse => noDecoder(opcode)
      case WarpgateRequest => noDecoder(opcode)
      case WarpgateResponse => noDecoder(opcode)
      case DamageWithPositionMessage => noDecoder(opcode)
      case GenericActionMessage => noDecoder(opcode)
      case ContinentalLockUpdateMessage => noDecoder(opcode)
      case AvatarGrenadeStateMessage => noDecoder(opcode)

      // OPCODE 170
      case UnknownMessage170 => noDecoder(opcode)
      case UnknownMessage171 => noDecoder(opcode)
      case ReleaseAvatarRequestMessage => noDecoder(opcode)
      case AvatarDeadStateMessage => noDecoder(opcode)
      case CSAssistMessage => noDecoder(opcode)
      case CSAssistCommentMessage => noDecoder(opcode)
      case VoiceHostRequest => noDecoder(opcode)
      case VoiceHostKill => noDecoder(opcode)
      case VoiceHostInfo => noDecoder(opcode)
      case BattleplanMessage => noDecoder(opcode)

      // OPCODE 180
      case BattleExperienceMessage => noDecoder(opcode)
      case TargetingImplantRequest => noDecoder(opcode)
      case ZonePopulationUpdateMessage => noDecoder(opcode)
      case DisconnectMessage => noDecoder(opcode)
      case ExperienceAddedMessage => noDecoder(opcode)
      case OrbitalStrikeWaypointMessage => noDecoder(opcode)
      case KeepAliveMessage => game.KeepAliveMessage.decode
      case MapObjectStateBlockMessage => noDecoder(opcode)
      case SnoopMsg => noDecoder(opcode)
      case PlayerStateMessageUpstream => game.PlayerStateMessageUpstream.decode

      // OPCODE 190
      case PlayerStateShiftMessage => noDecoder(opcode)
      case ZipLineMessage => noDecoder(opcode)
      case CaptureFlagUpdateMessage => noDecoder(opcode)
      case VanuModuleUpdateMessage => noDecoder(opcode)
      case FacilityBenefitShieldChargeRequestMessage => noDecoder(opcode)
      case ProximityTerminalUseMessage => noDecoder(opcode)
      case QuantityDeltaUpdateMessage => noDecoder(opcode)
      case ChainLashMessage => noDecoder(opcode)
      case ZoneInfoMessage => noDecoder(opcode)
      case LongRangeProjectileInfoMessage => noDecoder(opcode)

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
      case GenericObjectActionAtPositionMessage => noDecoder(opcode)
      case PropertyOverrideMessage => noDecoder(opcode)
      case WarpgateLinkOverrideMessage => noDecoder(opcode)
      case EmpireBenefitsMessage => noDecoder(opcode)
      case ForceEmpireMessage => noDecoder(opcode)
      case BroadcastWarpgateUpdateMessage => noDecoder(opcode)
      case UnknownMessage218 => noDecoder(opcode)
      case SquadMainTerminalMessage => noDecoder(opcode)

      // OPCODE 220
      case SquadMainTerminalResponseMessage => noDecoder(opcode)
      case SquadOrderMessage => noDecoder(opcode)
      case SquadOrderResponse => noDecoder(opcode)
      case ZoneLockInfoMessage => noDecoder(opcode)
      case SquadBindInfoMessage => noDecoder(opcode)
      case AudioSequenceMessage => noDecoder(opcode)
      case SquadFacilityBindInfoMessage => noDecoder(opcode)
      case ZoneForcedCavernConnectionsMessage => noDecoder(opcode)
      case MissionActionMessage => noDecoder(opcode)
      case MissionKillTriggerMessage => noDecoder(opcode)

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
      case OrderTerminalBugMessage => noDecoder(opcode)

      // OPCODE 240
      case QueueTimedHelpMessage => noDecoder(opcode)
      case MailMessage => noDecoder(opcode)
      case GameVarUpdate => noDecoder(opcode)
      case ClientCheatedMessage => noDecoder(opcode)
      case default => noDecoder(opcode)
  }

  implicit val codec: Codec[this.Value] = PacketHelpers.createEnumerationCodec(this, uint8L)
}
