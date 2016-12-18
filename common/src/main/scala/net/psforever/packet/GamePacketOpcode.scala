// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import scodec.{Attempt, Codec, DecodeResult, Err}
import scodec.bits.BitVector
import scodec.codecs._

import scala.annotation.switch

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
  // OPCODES 0x00-0f
  Unknown0,
  LoginMessage,
  LoginRespMessage,
  ConnectToWorldRequestMessage, // found by searching for 83 F8 03 89 in IDA
  ConnectToWorldMessage,
  VNLWorldStatusMessage,
  UnknownMessage6,
  UnknownMessage7,
  // 0x08
  PlayerStateMessage,
  HitMessage,
  HitHint,
  DamageMessage,
  DestroyMessage,
  ReloadMessage,
  MountVehicleMsg,
  DismountVehicleMsg,

  // OPCODES 0x10-1f
  UseItemMessage,
  MoveItemMessage,
  ChatMsg,
  CharacterNoRecordMessage,
  CharacterInfoMessage,
  UnknownMessage21,
  BindPlayerMessage,
  ObjectCreateMessage_Duplicate,
  // 0x18
  ObjectCreateMessage,
  ObjectDeleteMessage,
  PingMsg,
  VehicleStateMessage,
  FrameVehicleStateMessage,
  GenericObjectStateMsg,
  ChildObjectStateMessage,
  ActionResultMessage,

  // OPCODES 0x20-2f
  UnknownMessage32,
  ActionProgressMessage,
  ActionCancelMessage,
  ActionCancelAcknowledgeMessage,
  SetEmpireMessage,
  EmoteMsg,
  UnuseItemMessage,
  ObjectDetachMessage,
  // 0x28
  CreateShortcutMessage,
  ChangeShortcutBankMessage,
  ObjectAttachMessage,
  UnknownMessage43,
  PlanetsideAttributeMessage,
  RequestDestroyMessage,
  UnknownMessage46,
  CharacterCreateRequestMessage,

  // OPCODES 0x30-3f
  CharacterRequestMessage,
  LoadMapMessage,
  SetCurrentAvatarMessage,
  ObjectHeldMessage,
  WeaponFireMessage,
  AvatarJumpMessage,
  PickupItemMessage,
  DropItemMessage,
  // 0x38
  InventoryStateMessage,
  ChangeFireStateMessage_Start,
  ChangeFireStateMessage_Stop,
  UnknownMessage59,
  GenericCollisionMsg,
  QuantityUpdateMessage,
  ArmorChangedMessage,
  ProjectileStateMessage,

  // OPCODES 0x40-4f
  MountVehicleCargoMsg,
  DismountVehicleCargoMsg,
  CargoMountPointStatusMessage,
  BeginZoningMessage,
  ItemTransactionMessage,
  ItemTransactionResultMessage,
  ChangeFireModeMessage,
  ChangeAmmoMessage,
  // 0x48
  TimeOfDayMessage,
  UnknownMessage73,
  SpawnRequestMessage,
  DeployRequestMessage,
  UnknownMessage76,
  RepairMessage,
  ServerVehicleOverrideMsg,
  LashMessage,

  // OPCODES 0x50-5f
  TargetingInfoMessage,
  TriggerEffectMessage,
  WeaponDryFireMessage,
  DroppodLaunchRequestMessage,
  HackMessage,
  DroppodLaunchResponseMessage,
  GenericObjectActionMessage,
  AvatarVehicleTimerMessage,
  // 0x58
  AvatarImplantMessage,
  UnknownMessage89,
  DelayedPathMountMsg,
  OrbitalShuttleTimeMsg,
  AIDamage,
  DeployObjectMessage,
  FavoritesRequest,
  FavoritesResponse,

  // OPCODES 0x60-6f
  FavoritesMessage,
  ObjectDetectedMessage,
  SplashHitMessage,
  SetChatFilterMessage,
  AvatarSearchCriteriaMessage,
  AvatarSearchResponse,
  WeaponJammedMessage,
  LinkDeadAwarenessMsg,
  // 0x68
  DroppodFreefallingMessage,
  AvatarFirstTimeEventMessage,
  AggravatedDamageMessage,
  TriggerSoundMessage,
  LootItemMessage,
  VehicleSubStateMessage,
  SquadMembershipRequest,
  SquadMembershipResponse,

  // OPCODES 0x70-7f
  SquadMemberEvent,
  PlatoonEvent,
  FriendsRequest,
  FriendsResponse,
  TriggerEnvironmentalDamageMessage,
  TrainingZoneMessage,
  DeployableObjectsInfoMessage,
  SquadState,
  // 0x78
  OxygenStateMessage,
  TradeMessage,
  UnknownMessage122,
  DamageFeedbackMessage,
  DismountBuildingMsg,
  UnknownMessage125,
  UnknownMessage126,
  AvatarStatisticsMessage,

  // OPCODES 0x80-8f
  GenericObjectAction2Message,
  DestroyDisplayMessage,
  TriggerBotAction,
  SquadWaypointRequest,
  SquadWaypointEvent,
  OffshoreVehicleMessage,
  ObjectDeployedMessage,
  ObjectDeployedCountMessage,
  // 0x88
  WeaponDelayFireMessage,
  BugReportMessage,
  PlayerStasisMessage,
  UnknownMessage139,
  OutfitMembershipRequest,
  OutfitMembershipResponse,
  OutfitRequest,
  OutfitEvent,

  // OPCODES 0x90-9f
  OutfitMemberEvent,
  OutfitMemberUpdate,
  PlanetsideStringAttributeMessage,
  DataChallengeMessage,
  DataChallengeMessageResp,
  WeatherMessage,
  SimDataChallenge,
  SimDataChallengeResp,
  // 0x98
  OutfitListEvent,
  EmpireIncentivesMessage,
  InvalidTerrainMessage,
  SyncMessage,
  DebugDrawMessage,
  SoulMarkMessage,
  UplinkPositionEvent,
  HotSpotUpdateMessage,

  // OPCODES 0xa0-af
  BuildingInfoUpdateMessage,
  FireHintMessage,
  UplinkRequest,
  UplinkResponse,
  WarpgateRequest,
  WarpgateResponse,
  DamageWithPositionMessage,
  GenericActionMessage,
  // 0xa8
  ContinentalLockUpdateMessage,
  AvatarGrenadeStateMessage,
  UnknownMessage170,
  UnknownMessage171,
  ReleaseAvatarRequestMessage,
  AvatarDeadStateMessage,
  CSAssistMessage,
  CSAssistCommentMessage,

  // OPCODES 0xb0-bf
  VoiceHostRequest,
  VoiceHostKill,
  VoiceHostInfo,
  BattleplanMessage,
  BattleExperienceMessage,
  TargetingImplantRequest,
  ZonePopulationUpdateMessage,
  DisconnectMessage,
  // 0xb8
  ExperienceAddedMessage,
  OrbitalStrikeWaypointMessage,
  KeepAliveMessage,
  MapObjectStateBlockMessage,
  SnoopMsg,
  PlayerStateMessageUpstream,
  PlayerStateShiftMessage,
  ZipLineMessage,

  // OPCODES 0xc0-cf
  CaptureFlagUpdateMessage,
  VanuModuleUpdateMessage,
  FacilityBenefitShieldChargeRequestMessage,
  ProximityTerminalUseMessage,
  QuantityDeltaUpdateMessage,
  ChainLashMessage,
  ZoneInfoMessage,
  LongRangeProjectileInfoMessage,
  // 0xc8
  WeaponLazeTargetPositionMessage,
  ModuleLimitsMessage,
  OutfitBenefitMessage,
  EmpireChangeTimeMessage,
  ClockCalibrationMessage,
  DensityLevelUpdateMessage,
  ActOfGodMessage,
  AvatarAwardMessage,

  // OPCODES 0xd0-df
  UnknownMessage208,
  DisplayedAwardMessage,
  RespawnAMSInfoMessage,
  ComponentDamageMessage,
  GenericObjectActionAtPositionMessage,
  PropertyOverrideMessage,
  WarpgateLinkOverrideMessage,
  EmpireBenefitsMessage,
  // 0xd8
  ForceEmpireMessage,
  BroadcastWarpgateUpdateMessage,
  UnknownMessage218,
  SquadMainTerminalMessage,
  SquadMainTerminalResponseMessage,
  SquadOrderMessage,
  SquadOrderResponse,
  ZoneLockInfoMessage,

  // OPCODES 0xe0-ef
  SquadBindInfoMessage,
  AudioSequenceMessage,
  SquadFacilityBindInfoMessage,
  ZoneForcedCavernConnectionsMessage,
  MissionActionMessage,
  MissionKillTriggerMessage,
  ReplicationStreamMessage,
  SquadDefinitionActionMessage,
  // 0xe8
  SquadDetailDefinitionUpdateMessage,
  TacticsMessage,
  RabbitUpdateMessage,
  SquadInvitationRequestMessage,
  CharacterKnowledgeMessage,
  GameScoreUpdateMessage,
  UnknownMessage238,
  OrderTerminalBugMessage,

  // OPCODES 0xf0-f3
  QueueTimedHelpMessage,
  MailMessage,
  GameVarUpdate,
  ClientCheatedMessage // last known message type (243, 0xf3)
  = Value

  private def noDecoder(opcode : GamePacketOpcode.Type) = (a : BitVector) =>
    Attempt.failure(Err(s"Could not find a marshaller for game packet ${opcode}"))

  /// Mapping of packet IDs to decoders. Notice that we are using the @switch annotation which ensures that the Scala
  /// compiler will be able to optimize this as a lookup table (switch statement). Microbenchmarks show a nearly 400x
  /// speedup when using a switch (given the worst case of not finding a decoder)
  def getPacketDecoder(opcode : GamePacketOpcode.Type) : (BitVector) => Attempt[DecodeResult[PlanetSideGamePacket]] = (opcode.id : @switch) match {
    // OPCODES 0x00-0f
    case 0x00 => noDecoder(Unknown0)
    case 0x01 => game.LoginMessage.decode
    case 0x02 => game.LoginRespMessage.decode
    case 0x03 => game.ConnectToWorldRequestMessage.decode
    case 0x04 => game.ConnectToWorldMessage.decode
    case 0x05 => game.VNLWorldStatusMessage.decode
    case 0x06 => noDecoder(UnknownMessage6)
    case 0x07 => noDecoder(UnknownMessage7)
    // 0x08
    case 0x08 => noDecoder(PlayerStateMessage)
    case 0x09 => game.HitMessage.decode
    case 0x0a => noDecoder(HitHint)
    case 0x0b => noDecoder(DamageMessage)
    case 0x0c => noDecoder(DestroyMessage)
    case 0x0d => game.ReloadMessage.decode
    case 0x0e => noDecoder(MountVehicleMsg)
    case 0x0f => noDecoder(DismountVehicleMsg)

    // OPCODES 0x10-1f
    case 0x10 => game.UseItemMessage.decode
    case 0x11 => game.MoveItemMessage.decode
    case 0x12 => game.ChatMsg.decode
    case 0x13 => noDecoder(CharacterNoRecordMessage)
    case 0x14 => game.CharacterInfoMessage.decode
    case 0x15 => noDecoder(UnknownMessage21)
    case 0x16 => noDecoder(BindPlayerMessage)
    case 0x17 => noDecoder(ObjectCreateMessage_Duplicate)
    // 0x18
    case 0x18 => game.ObjectCreateMessage.decode
    case 0x19 => game.ObjectDeleteMessage.decode
    case 0x1a => game.PingMsg.decode
    case 0x1b => noDecoder(VehicleStateMessage)
    case 0x1c => noDecoder(FrameVehicleStateMessage)
    case 0x1d => game.GenericObjectStateMsg.decode
    case 0x1e => noDecoder(ChildObjectStateMessage)
    case 0x1f => game.ActionResultMessage.decode

    // OPCODES 0x20-2f
    case 0x20 => noDecoder(UnknownMessage32)
    case 0x21 => noDecoder(ActionProgressMessage)
    case 0x22 => noDecoder(ActionCancelMessage)
    case 0x23 => noDecoder(ActionCancelAcknowledgeMessage)
    case 0x24 => game.SetEmpireMessage.decode
    case 0x25 => game.EmoteMsg.decode
    case 0x26 => noDecoder(UnuseItemMessage)
    case 0x27 => noDecoder(ObjectDetachMessage)
    // 0x28
    case 0x28 => noDecoder(CreateShortcutMessage)
    case 0x29 => noDecoder(ChangeShortcutBankMessage)
    case 0x2a => noDecoder(ObjectAttachMessage)
    case 0x2b => noDecoder(UnknownMessage43)
    case 0x2c => noDecoder(PlanetsideAttributeMessage)
    case 0x2d => game.RequestDestroyMessage.decode
    case 0x2e => noDecoder(UnknownMessage46)
    case 0x2f => game.CharacterCreateRequestMessage.decode

    // OPCODES 0x30-3f
    case 0x30 => game.CharacterRequestMessage.decode
    case 0x31 => game.LoadMapMessage.decode
    case 0x32 => game.SetCurrentAvatarMessage.decode
    case 0x33 => game.ObjectHeldMessage.decode
    case 0x34 => game.WeaponFireMessage.decode
    case 0x35 => game.AvatarJumpMessage.decode
    case 0x36 => noDecoder(PickupItemMessage)
    case 0x37 => game.DropItemMessage.decode
    // 0x38
    case 0x38 => noDecoder(InventoryStateMessage)
    case 0x39 => game.ChangeFireStateMessage_Start.decode
    case 0x3a => game.ChangeFireStateMessage_Stop.decode
    case 0x3b => noDecoder(UnknownMessage59)
    case 0x3c => noDecoder(GenericCollisionMsg)
    case 0x3d => game.QuantityUpdateMessage.decode
    case 0x3e => game.ArmorChangedMessage.decode
    case 0x3f => noDecoder(ProjectileStateMessage)

    // OPCODES 0x40-4f
    case 0x40 => noDecoder(MountVehicleCargoMsg)
    case 0x41 => noDecoder(DismountVehicleCargoMsg)
    case 0x42 => noDecoder(CargoMountPointStatusMessage)
    case 0x43 => noDecoder(BeginZoningMessage)
    case 0x44 => game.ItemTransactionMessage.decode
    case 0x45 => noDecoder(ItemTransactionResultMessage)
    case 0x46 => game.ChangeFireModeMessage.decode
    case 0x47 => game.ChangeAmmoMessage.decode
    // 0x48
    case 0x48 => game.TimeOfDayMessage.decode
    case 0x49 => noDecoder(UnknownMessage73)
    case 0x4a => noDecoder(SpawnRequestMessage)
    case 0x4b => noDecoder(DeployRequestMessage)
    case 0x4c => noDecoder(UnknownMessage76)
    case 0x4d => noDecoder(RepairMessage)
    case 0x4e => noDecoder(ServerVehicleOverrideMsg)
    case 0x4f => noDecoder(LashMessage)

    // OPCODES 0x50-5f
    case 0x50 => noDecoder(TargetingInfoMessage)
    case 0x51 => noDecoder(TriggerEffectMessage)
    case 0x52 => game.WeaponDryFireMessage.decode
    case 0x53 => noDecoder(DroppodLaunchRequestMessage)
    case 0x54 => noDecoder(HackMessage)
    case 0x55 => noDecoder(DroppodLaunchResponseMessage)
    case 0x56 => noDecoder(GenericObjectActionMessage)
    case 0x57 => noDecoder(AvatarVehicleTimerMessage)
    // 0x58
    case 0x58 => noDecoder(AvatarImplantMessage)
    case 0x59 => noDecoder(UnknownMessage89)
    case 0x5a => noDecoder(DelayedPathMountMsg)
    case 0x5b => noDecoder(OrbitalShuttleTimeMsg)
    case 0x5c => noDecoder(AIDamage)
    case 0x5d => noDecoder(DeployObjectMessage)
    case 0x5e => noDecoder(FavoritesRequest)
    case 0x5f => noDecoder(FavoritesResponse)

    // OPCODES 0x60-6f
    case 0x60 => noDecoder(FavoritesMessage)
    case 0x61 => noDecoder(ObjectDetectedMessage)
    case 0x62 => noDecoder(SplashHitMessage)
    case 0x63 => noDecoder(SetChatFilterMessage)
    case 0x64 => noDecoder(AvatarSearchCriteriaMessage)
    case 0x65 => noDecoder(AvatarSearchResponse)
    case 0x66 => game.WeaponJammedMessage.decode
    case 0x67 => noDecoder(LinkDeadAwarenessMsg)
    // 0x68
    case 0x68 => noDecoder(DroppodFreefallingMessage)
    case 0x69 => game.AvatarFirstTimeEventMessage.decode
    case 0x6a => noDecoder(AggravatedDamageMessage)
    case 0x6b => noDecoder(TriggerSoundMessage)
    case 0x6c => noDecoder(LootItemMessage)
    case 0x6d => noDecoder(VehicleSubStateMessage)
    case 0x6e => noDecoder(SquadMembershipRequest)
    case 0x6f => noDecoder(SquadMembershipResponse)

    // OPCODES 0x70-7f
    case 0x70 => noDecoder(SquadMemberEvent)
    case 0x71 => noDecoder(PlatoonEvent)
    case 0x72 => noDecoder(FriendsRequest)
    case 0x73 => noDecoder(FriendsResponse)
    case 0x74 => noDecoder(TriggerEnvironmentalDamageMessage)
    case 0x75 => noDecoder(TrainingZoneMessage)
    case 0x76 => noDecoder(DeployableObjectsInfoMessage)
    case 0x77 => noDecoder(SquadState)
    // 0x78
    case 0x78 => noDecoder(OxygenStateMessage)
    case 0x79 => noDecoder(TradeMessage)
    case 0x7a => noDecoder(UnknownMessage122)
    case 0x7b => noDecoder(DamageFeedbackMessage)
    case 0x7c => noDecoder(DismountBuildingMsg)
    case 0x7d => noDecoder(UnknownMessage125)
    case 0x7e => noDecoder(UnknownMessage126)
    case 0x7f => noDecoder(AvatarStatisticsMessage)

    // OPCODES 0x80-8f
    case 0x80 => noDecoder(GenericObjectAction2Message)
    case 0x81 => noDecoder(DestroyDisplayMessage)
    case 0x82 => noDecoder(TriggerBotAction)
    case 0x83 => noDecoder(SquadWaypointRequest)
    case 0x84 => noDecoder(SquadWaypointEvent)
    case 0x85 => noDecoder(OffshoreVehicleMessage)
    case 0x86 => noDecoder(ObjectDeployedMessage)
    case 0x87 => noDecoder(ObjectDeployedCountMessage)
    // 0x88
    case 0x88 => game.WeaponDelayFireMessage.decode
    case 0x89 => noDecoder(BugReportMessage)
    case 0x8a => noDecoder(PlayerStasisMessage)
    case 0x8b => noDecoder(UnknownMessage139)
    case 0x8c => noDecoder(OutfitMembershipRequest)
    case 0x8d => noDecoder(OutfitMembershipResponse)
    case 0x8e => noDecoder(OutfitRequest)
    case 0x8f => noDecoder(OutfitEvent)

    // OPCODES 0x90-9f
    case 0x90 => noDecoder(OutfitMemberEvent)
    case 0x91 => noDecoder(OutfitMemberUpdate)
    case 0x92 => noDecoder(PlanetsideStringAttributeMessage)
    case 0x93 => noDecoder(DataChallengeMessage)
    case 0x94 => noDecoder(DataChallengeMessageResp)
    case 0x95 => noDecoder(WeatherMessage)
    case 0x96 => noDecoder(SimDataChallenge)
    case 0x97 => noDecoder(SimDataChallengeResp)
    // 0x98
    case 0x98 => noDecoder(OutfitListEvent)
    case 0x99 => noDecoder(EmpireIncentivesMessage)
    case 0x9a => noDecoder(InvalidTerrainMessage)
    case 0x9b => noDecoder(SyncMessage)
    case 0x9c => noDecoder(DebugDrawMessage)
    case 0x9d => noDecoder(SoulMarkMessage)
    case 0x9e => noDecoder(UplinkPositionEvent)
    case 0x9f => noDecoder(HotSpotUpdateMessage)

    // OPCODES 0xa0-af
    case 0xa0 => game.BuildingInfoUpdateMessage.decode
    case 0xa1 => noDecoder(FireHintMessage)
    case 0xa2 => noDecoder(UplinkRequest)
    case 0xa3 => noDecoder(UplinkResponse)
    case 0xa4 => noDecoder(WarpgateRequest)
    case 0xa5 => noDecoder(WarpgateResponse)
    case 0xa6 => noDecoder(DamageWithPositionMessage)
    case 0xa7 => noDecoder(GenericActionMessage)
    // 0xa8
    case 0xa8 => game.ContinentalLockUpdateMessage.decode
    case 0xa9 => noDecoder(AvatarGrenadeStateMessage)
    case 0xaa => noDecoder(UnknownMessage170)
    case 0xab => noDecoder(UnknownMessage171)
    case 0xac => noDecoder(ReleaseAvatarRequestMessage)
    case 0xad => noDecoder(AvatarDeadStateMessage)
    case 0xae => noDecoder(CSAssistMessage)
    case 0xaf => noDecoder(CSAssistCommentMessage)

    // OPCODES 0xb0-bf
    case 0xb0 => noDecoder(VoiceHostRequest)
    case 0xb1 => noDecoder(VoiceHostKill)
    case 0xb2 => noDecoder(VoiceHostInfo)
    case 0xb3 => noDecoder(BattleplanMessage)
    case 0xb4 => noDecoder(BattleExperienceMessage)
    case 0xb5 => noDecoder(TargetingImplantRequest)
    case 0xb6 => game.ZonePopulationUpdateMessage.decode
    case 0xb7 => noDecoder(DisconnectMessage)
    // 0xb8
    case 0xb8 => noDecoder(ExperienceAddedMessage)
    case 0xb9 => noDecoder(OrbitalStrikeWaypointMessage)
    case 0xba => game.KeepAliveMessage.decode
    case 0xbb => noDecoder(MapObjectStateBlockMessage)
    case 0xbc => noDecoder(SnoopMsg)
    case 0xbd => game.PlayerStateMessageUpstream.decode
    case 0xbe => game.PlayerStateShiftMessage.decode
    case 0xbf => noDecoder(ZipLineMessage)

    // OPCODES 0xc0-cf
    case 0xc0 => noDecoder(CaptureFlagUpdateMessage)
    case 0xc1 => noDecoder(VanuModuleUpdateMessage)
    case 0xc2 => noDecoder(FacilityBenefitShieldChargeRequestMessage)
    case 0xc3 => noDecoder(ProximityTerminalUseMessage)
    case 0xc4 => game.QuantityDeltaUpdateMessage.decode
    case 0xc5 => noDecoder(ChainLashMessage)
    case 0xc6 => noDecoder(ZoneInfoMessage)
    case 0xc7 => noDecoder(LongRangeProjectileInfoMessage)
    // 0xc8
    case 0xc8 => noDecoder(WeaponLazeTargetPositionMessage)
    case 0xc9 => noDecoder(ModuleLimitsMessage)
    case 0xca => noDecoder(OutfitBenefitMessage)
    case 0xcb => noDecoder(EmpireChangeTimeMessage)
    case 0xcc => noDecoder(ClockCalibrationMessage)
    case 0xcd => noDecoder(DensityLevelUpdateMessage)
    case 0xce => noDecoder(ActOfGodMessage)
    case 0xcf => noDecoder(AvatarAwardMessage)

    // OPCODES 0xd0-df
    case 0xd0 => noDecoder(UnknownMessage208)
    case 0xd1 => noDecoder(DisplayedAwardMessage)
    case 0xd2 => noDecoder(RespawnAMSInfoMessage)
    case 0xd3 => noDecoder(ComponentDamageMessage)
    case 0xd4 => noDecoder(GenericObjectActionAtPositionMessage)
    case 0xd5 => noDecoder(PropertyOverrideMessage)
    case 0xd6 => noDecoder(WarpgateLinkOverrideMessage)
    case 0xd7 => noDecoder(EmpireBenefitsMessage)
    // 0xd8
    case 0xd8 => noDecoder(ForceEmpireMessage)
    case 0xd9 => game.BroadcastWarpgateUpdateMessage.decode
    case 0xda => noDecoder(UnknownMessage218)
    case 0xdb => noDecoder(SquadMainTerminalMessage)
    case 0xdc => noDecoder(SquadMainTerminalResponseMessage)
    case 0xdd => noDecoder(SquadOrderMessage)
    case 0xde => noDecoder(SquadOrderResponse)
    case 0xdf => noDecoder(ZoneLockInfoMessage)

    // OPCODES 0xe0-ef
    case 0xe0 => noDecoder(SquadBindInfoMessage)
    case 0xe1 => noDecoder(AudioSequenceMessage)
    case 0xe2 => noDecoder(SquadFacilityBindInfoMessage)
    case 0xe3 => noDecoder(ZoneForcedCavernConnectionsMessage)
    case 0xe4 => noDecoder(MissionActionMessage)
    case 0xe5 => noDecoder(MissionKillTriggerMessage)
    case 0xe6 => noDecoder(ReplicationStreamMessage)
    case 0xe7 => noDecoder(SquadDefinitionActionMessage)
    // 0xe8
    case 0xe8 => noDecoder(SquadDetailDefinitionUpdateMessage)
    case 0xe9 => noDecoder(TacticsMessage)
    case 0xea => noDecoder(RabbitUpdateMessage)
    case 0xeb => noDecoder(SquadInvitationRequestMessage)
    case 0xec => noDecoder(CharacterKnowledgeMessage)
    case 0xed => noDecoder(GameScoreUpdateMessage)
    case 0xee => noDecoder(UnknownMessage238)
    case 0xef => noDecoder(OrderTerminalBugMessage)

    // OPCODES 0xf0-f3
    case 0xf0 => noDecoder(QueueTimedHelpMessage)
    case 0xf1 => noDecoder(MailMessage)
    case 0xf2 => noDecoder(GameVarUpdate)
    case 0xf3 => noDecoder(ClientCheatedMessage)
    case default => noDecoder(opcode)
  }

  implicit val codec: Codec[this.Value] = PacketHelpers.createEnumerationCodec(this, uint8L)
}
