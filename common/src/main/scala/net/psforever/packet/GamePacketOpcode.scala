// Copyright (c) 2017 PSForever
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
  */
object GamePacketOpcode extends Enumeration {
  type Type = Value
  val
  // OPCODES 0x00-0f
  Unknown0, // PPT_NULL in beta client
  LoginMessage,
  LoginRespMessage,
  ConnectToWorldRequestMessage, // found by searching for 83 F8 03 89 in IDA
  ConnectToWorldMessage,
  VNLWorldStatusMessage,
  UnknownMessage6, // PPT_TRANSFERTOWORLDREQUEST
  UnknownMessage7, // PPT_TRANSFERTOWORLDRESPONSE
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
  UnknownMessage21, // PPT_DISCONNECT
  BindPlayerMessage,
  ObjectCreateMessage_Duplicate, // PPT_OBJECTCREATE
  // 0x18
  ObjectCreateMessage, // PPT_OBJECTCREATEDETAILED
  ObjectDeleteMessage,
  PingMsg,
  VehicleStateMessage,
  FrameVehicleStateMessage,
  GenericObjectStateMsg,
  ChildObjectStateMessage,
  ActionResultMessage,

  // OPCODES 0x20-2f
  UnknownMessage32, // PPT_ACTIONBEGIN
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
  UnknownMessage43, // PPT_OBJECTEMPTY
  PlanetsideAttributeMessage,
  RequestDestroyMessage,
  UnknownMessage46, // PPT_EQUIPITEM
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
  UnknownMessage73, // PPT_PROJECTILE_EVENT_BLOCK
  SpawnRequestMessage,
  DeployRequestMessage,
  UnknownMessage76, // PPT_BUILDINGSTATECHANGED
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
  UnknownMessage89, // PPT_SEARCHMESSAGE
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
  UnknownMessage125, // PPT_MOUNTBUILDING
  UnknownMessage126, // PPT_INTENDEDDROPZONE
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

  private def noDecoder(opcode : GamePacketOpcode.Type) = (_ : BitVector) =>
    Attempt.failure(Err(s"Could not find a marshaller for game packet $opcode"))

  /// Mapping of packet IDs to decoders. Notice that we are using the @switch annotation which ensures that the Scala
  /// compiler will be able to optimize this as a lookup table (switch statement). Microbenchmarks show a nearly 400x
  /// speedup when using a switch (given the worst case of not finding a decoder)
  def getPacketDecoder(opcode : GamePacketOpcode.Type) : BitVector => Attempt[DecodeResult[PlanetSideGamePacket]] = (opcode.id : @switch) match {
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
    case 0x08 => game.PlayerStateMessage.decode
    case 0x09 => game.HitMessage.decode
    case 0x0a => game.HitHint.decode
    case 0x0b => game.DamageMessage.decode
    case 0x0c => game.DestroyMessage.decode
    case 0x0d => game.ReloadMessage.decode
    case 0x0e => game.MountVehicleMsg.decode
    case 0x0f => game.DismountVehicleMsg.decode

    // OPCODES 0x10-1f
    case 0x10 => game.UseItemMessage.decode
    case 0x11 => game.MoveItemMessage.decode
    case 0x12 => game.ChatMsg.decode
    case 0x13 => game.CharacterNoRecordMessage.decode
    case 0x14 => game.CharacterInfoMessage.decode
    case 0x15 => noDecoder(UnknownMessage21)
    case 0x16 => game.BindPlayerMessage.decode
    case 0x17 => game.ObjectCreateMessage.decode
    // 0x18
    case 0x18 => game.ObjectCreateDetailedMessage.decode
    case 0x19 => game.ObjectDeleteMessage.decode
    case 0x1a => game.PingMsg.decode
    case 0x1b => game.VehicleStateMessage.decode
    case 0x1c => noDecoder(FrameVehicleStateMessage)
    case 0x1d => game.GenericObjectStateMsg.decode
    case 0x1e => game.ChildObjectStateMessage.decode
    case 0x1f => game.ActionResultMessage.decode

    // OPCODES 0x20-2f
    case 0x20 => noDecoder(UnknownMessage32)
    case 0x21 => game.ActionProgressMessage.decode
    case 0x22 => game.ActionCancelMessage.decode
    case 0x23 => noDecoder(ActionCancelAcknowledgeMessage)
    case 0x24 => game.SetEmpireMessage.decode
    case 0x25 => game.EmoteMsg.decode
    case 0x26 => game.UnuseItemMessage.decode
    case 0x27 => game.ObjectDetachMessage.decode
    // 0x28
    case 0x28 => game.CreateShortcutMessage.decode
    case 0x29 => game.ChangeShortcutBankMessage.decode
    case 0x2a => game.ObjectAttachMessage.decode
    case 0x2b => noDecoder(UnknownMessage43)
    case 0x2c => game.PlanetsideAttributeMessage.decode
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
    case 0x36 => game.PickupItemMessage.decode
    case 0x37 => game.DropItemMessage.decode
    // 0x38
    case 0x38 => game.InventoryStateMessage.decode
    case 0x39 => game.ChangeFireStateMessage_Start.decode
    case 0x3a => game.ChangeFireStateMessage_Stop.decode
    case 0x3b => noDecoder(UnknownMessage59)
    case 0x3c => game.GenericCollisionMsg.decode
    case 0x3d => game.QuantityUpdateMessage.decode
    case 0x3e => game.ArmorChangedMessage.decode
    case 0x3f => game.ProjectileStateMessage.decode

    // OPCODES 0x40-4f
    case 0x40 => game.MountVehicleCargoMsg.decode
    case 0x41 => game.DismountVehicleCargoMsg.decode
    case 0x42 => game.CargoMountPointStatusMessage.decode
    case 0x43 => game.BeginZoningMessage.decode
    case 0x44 => game.ItemTransactionMessage.decode
    case 0x45 => game.ItemTransactionResultMessage.decode
    case 0x46 => game.ChangeFireModeMessage.decode
    case 0x47 => game.ChangeAmmoMessage.decode
    // 0x48
    case 0x48 => game.TimeOfDayMessage.decode
    case 0x49 => noDecoder(UnknownMessage73)
    case 0x4a => game.SpawnRequestMessage.decode
    case 0x4b => game.DeployRequestMessage.decode
    case 0x4c => noDecoder(UnknownMessage76)
    case 0x4d => game.RepairMessage.decode
    case 0x4e => game.ServerVehicleOverrideMsg.decode
    case 0x4f => game.LashMessage.decode

    // OPCODES 0x50-5f
    case 0x50 => game.TargetingInfoMessage.decode
    case 0x51 => game.TriggerEffectMessage.decode
    case 0x52 => game.WeaponDryFireMessage.decode
    case 0x53 => noDecoder(DroppodLaunchRequestMessage)
    case 0x54 => game.HackMessage.decode
    case 0x55 => noDecoder(DroppodLaunchResponseMessage)
    case 0x56 => game.GenericObjectActionMessage.decode
    case 0x57 => game.AvatarVehicleTimerMessage.decode
    // 0x58
    case 0x58 => game.AvatarImplantMessage.decode
    case 0x59 => noDecoder(UnknownMessage89)
    case 0x5a => game.DelayedPathMountMsg.decode
    case 0x5b => noDecoder(OrbitalShuttleTimeMsg)
    case 0x5c => noDecoder(AIDamage)
    case 0x5d => game.DeployObjectMessage.decode
    case 0x5e => game.FavoritesRequest.decode
    case 0x5f => noDecoder(FavoritesResponse)

    // OPCODES 0x60-6f
    case 0x60 => game.FavoritesMessage.decode
    case 0x61 => game.ObjectDetectedMessage.decode
    case 0x62 => game.SplashHitMessage.decode
    case 0x63 => game.SetChatFilterMessage.decode
    case 0x64 => game.AvatarSearchCriteriaMessage.decode
    case 0x65 => noDecoder(AvatarSearchResponse)
    case 0x66 => game.WeaponJammedMessage.decode
    case 0x67 => noDecoder(LinkDeadAwarenessMsg)
    // 0x68
    case 0x68 => noDecoder(DroppodFreefallingMessage)
    case 0x69 => game.AvatarFirstTimeEventMessage.decode
    case 0x6a => noDecoder(AggravatedDamageMessage)
    case 0x6b => game.TriggerSoundMessage.decode
    case 0x6c => game.LootItemMessage.decode
    case 0x6d => game.VehicleSubStateMessage.decode
    case 0x6e => game.SquadMembershipRequest.decode
    case 0x6f => game.SquadMembershipResponse.decode

    // OPCODES 0x70-7f
    case 0x70 => game.SquadMemberEvent.decode
    case 0x71 => noDecoder(PlatoonEvent)
    case 0x72 => game.FriendsRequest.decode
    case 0x73 => game.FriendsResponse.decode
    case 0x74 => game.TriggerEnvironmentalDamageMessage.decode
    case 0x75 => game.TrainingZoneMessage.decode
    case 0x76 => game.DeployableObjectsInfoMessage.decode
    case 0x77 => game.SquadState.decode
    // 0x78
    case 0x78 => game.OxygenStateMessage.decode
    case 0x79 => noDecoder(TradeMessage)
    case 0x7a => noDecoder(UnknownMessage122)
    case 0x7b => game.DamageFeedbackMessage.decode
    case 0x7c => game.DismountBuildingMsg.decode
    case 0x7d => noDecoder(UnknownMessage125)
    case 0x7e => noDecoder(UnknownMessage126)
    case 0x7f => game.AvatarStatisticsMessage.decode

    // OPCODES 0x80-8f
    case 0x80 => noDecoder(GenericObjectAction2Message)
    case 0x81 => game.DestroyDisplayMessage.decode
    case 0x82 => noDecoder(TriggerBotAction)
    case 0x83 => game.SquadWaypointRequest.decode
    case 0x84 => game.SquadWaypointEvent.decode
    case 0x85 => noDecoder(OffshoreVehicleMessage)
    case 0x86 => game.ObjectDeployedMessage.decode
    case 0x87 => noDecoder(ObjectDeployedCountMessage)
    // 0x88
    case 0x88 => game.WeaponDelayFireMessage.decode
    case 0x89 => game.BugReportMessage.decode
    case 0x8a => game.PlayerStasisMessage.decode
    case 0x8b => noDecoder(UnknownMessage139)
    case 0x8c => noDecoder(OutfitMembershipRequest)
    case 0x8d => noDecoder(OutfitMembershipResponse)
    case 0x8e => noDecoder(OutfitRequest)
    case 0x8f => noDecoder(OutfitEvent)

    // OPCODES 0x90-9f
    case 0x90 => noDecoder(OutfitMemberEvent)
    case 0x91 => noDecoder(OutfitMemberUpdate)
    case 0x92 => game.PlanetsideStringAttributeMessage.decode
    case 0x93 => noDecoder(DataChallengeMessage)
    case 0x94 => noDecoder(DataChallengeMessageResp)
    case 0x95 => game.WeatherMessage.decode
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
    case 0x9f => game.HotSpotUpdateMessage.decode

    // OPCODES 0xa0-af
    case 0xa0 => game.BuildingInfoUpdateMessage.decode
    case 0xa1 => game.FireHintMessage.decode
    case 0xa2 => noDecoder(UplinkRequest)
    case 0xa3 => noDecoder(UplinkResponse)
    case 0xa4 => game.WarpgateRequest.decode
    case 0xa5 => noDecoder(WarpgateResponse)
    case 0xa6 => game.DamageWithPositionMessage.decode
    case 0xa7 => game.GenericActionMessage.decode
    // 0xa8
    case 0xa8 => game.ContinentalLockUpdateMessage.decode
    case 0xa9 => game.AvatarGrenadeStateMessage.decode
    case 0xaa => noDecoder(UnknownMessage170)
    case 0xab => noDecoder(UnknownMessage171)
    case 0xac => game.ReleaseAvatarRequestMessage.decode
    case 0xad => game.AvatarDeadStateMessage.decode
    case 0xae => noDecoder(CSAssistMessage)
    case 0xaf => noDecoder(CSAssistCommentMessage)

    // OPCODES 0xb0-bf
    case 0xb0 => game.VoiceHostRequest.decode
    case 0xb1 => game.VoiceHostKill.decode
    case 0xb2 => game.VoiceHostInfo.decode
    case 0xb3 => game.BattleplanMessage.decode
    case 0xb4 => game.BattleExperienceMessage.decode
    case 0xb5 => game.TargetingImplantRequest.decode
    case 0xb6 => game.ZonePopulationUpdateMessage.decode
    case 0xb7 => game.DisconnectMessage.decode
    // 0xb8
    case 0xb8 => game.ExperienceAddedMessage.decode
    case 0xb9 => game.OrbitalStrikeWaypointMessage.decode
    case 0xba => game.KeepAliveMessage.decode
    case 0xbb => noDecoder(MapObjectStateBlockMessage)
    case 0xbc => noDecoder(SnoopMsg)
    case 0xbd => game.PlayerStateMessageUpstream.decode
    case 0xbe => game.PlayerStateShiftMessage.decode
    case 0xbf => game.ZipLineMessage.decode

    // OPCODES 0xc0-cf
    case 0xc0 => noDecoder(CaptureFlagUpdateMessage)
    case 0xc1 => noDecoder(VanuModuleUpdateMessage)
    case 0xc2 => game.FacilityBenefitShieldChargeRequestMessage.decode
    case 0xc3 => game.ProximityTerminalUseMessage.decode
    case 0xc4 => game.QuantityDeltaUpdateMessage.decode
    case 0xc5 => noDecoder(ChainLashMessage)
    case 0xc6 => game.ZoneInfoMessage.decode
    case 0xc7 => noDecoder(LongRangeProjectileInfoMessage)
    // 0xc8
    case 0xc8 => game.WeaponLazeTargetPositionMessage.decode
    case 0xc9 => noDecoder(ModuleLimitsMessage)
    case 0xca => noDecoder(OutfitBenefitMessage)
    case 0xcb => noDecoder(EmpireChangeTimeMessage)
    case 0xcc => noDecoder(ClockCalibrationMessage)
    case 0xcd => game.DensityLevelUpdateMessage.decode
    case 0xce => noDecoder(ActOfGodMessage)
    case 0xcf => noDecoder(AvatarAwardMessage)

    // OPCODES 0xd0-df
    case 0xd0 => noDecoder(UnknownMessage208)
    case 0xd1 => game.DisplayedAwardMessage.decode
    case 0xd2 => game.RespawnAMSInfoMessage.decode
    case 0xd3 => noDecoder(ComponentDamageMessage)
    case 0xd4 => noDecoder(GenericObjectActionAtPositionMessage)
    case 0xd5 => game.PropertyOverrideMessage.decode
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
    case 0xdf => game.ZoneLockInfoMessage.decode

    // OPCODES 0xe0-ef
    case 0xe0 => noDecoder(SquadBindInfoMessage)
    case 0xe1 => noDecoder(AudioSequenceMessage)
    case 0xe2 => noDecoder(SquadFacilityBindInfoMessage)
    case 0xe3 => game.ZoneForcedCavernConnectionsMessage.decode
    case 0xe4 => noDecoder(MissionActionMessage)
    case 0xe5 => noDecoder(MissionKillTriggerMessage)
    case 0xe6 => game.ReplicationStreamMessage.decode
    case 0xe7 => game.SquadDefinitionActionMessage.decode
    // 0xe8
    case 0xe8 => game.SquadDetailDefinitionUpdateMessage.decode
    case 0xe9 => noDecoder(TacticsMessage)
    case 0xea => noDecoder(RabbitUpdateMessage)
    case 0xeb => game.SquadInvitationRequestMessage.decode
    case 0xec => game.CharacterKnowledgeMessage.decode
    case 0xed => noDecoder(GameScoreUpdateMessage)
    case 0xee => noDecoder(UnknownMessage238)
    case 0xef => noDecoder(OrderTerminalBugMessage)

    // OPCODES 0xf0-f3
    case 0xf0 => noDecoder(QueueTimedHelpMessage)
    case 0xf1 => game.MailMessage.decode
    case 0xf2 => noDecoder(GameVarUpdate)
    case 0xf3 => noDecoder(ClientCheatedMessage)
    case _ => noDecoder(opcode)
  }

  implicit val codec: Codec[this.Value] = PacketHelpers.createEnumerationCodec(this, uint8L)
}
