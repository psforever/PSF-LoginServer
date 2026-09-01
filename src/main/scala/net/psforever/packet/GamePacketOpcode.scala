// Copyright (c) 2017-2025 PSForever
package net.psforever.packet

import net.psforever.packet.game.packets
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
  Unknown0,                     // PPT_NULL in beta client
  LoginMessage,                 //
  LoginRespMessage,             //
  ConnectToWorldRequestMessage, // found by searching for 83 F8 03 89 in IDA
  ConnectToWorldMessage,        //
  VNLWorldStatusMessage,        //
  UnknownMessage6,              // PPT_TRANSFERTOWORLDREQUEST
  UnknownMessage7,              // PPT_TRANSFERTOWORLDRESPONSE
  // 0x08
  PlayerStateMessage, //
  HitMessage,         //
  HitHint,            //
  DamageMessage,      //
  DestroyMessage,     //
  ReloadMessage,      //
  MountVehicleMsg,    //
  DismountVehicleMsg, //
  // OPCODES 0x10-1f
  UseItemMessage,                //
  MoveItemMessage,               //
  ChatMsg,                       //
  CharacterNoRecordMessage,      //
  CharacterInfoMessage,          //
  UnknownMessage21,              // PPT_DISCONNECT
  BindPlayerMessage,             //
  ObjectCreateMessage_Duplicate, // PPT_OBJECTCREATE
  // 0x18
  ObjectCreateMessage,      // PPT_OBJECTCREATEDETAILED
  ObjectDeleteMessage,      //
  PingMsg,                  //
  VehicleStateMessage,      //
  FrameVehicleStateMessage, //
  GenericObjectStateMsg,    //
  ChildObjectStateMessage,  //
  ActionResultMessage,      //
  // OPCODES 0x20-2f
  UnknownMessage32,               // PPT_ACTIONBEGIN
  ActionProgressMessage,          //
  ActionCancelMessage,            //
  ActionCancelAcknowledgeMessage, //
  SetEmpireMessage,               //
  EmoteMsg,                       //
  UnuseItemMessage,               //
  ObjectDetachMessage,            //
  // 0x28
  CreateShortcutMessage,         //
  ChangeShortcutBankMessage,     //
  ObjectAttachMessage,           //
  UnknownMessage43,              // PPT_OBJECTEMPTY
  PlanetsideAttributeMessage,    //
  RequestDestroyMessage,         //
  UnknownMessage46,              // PPT_EQUIPITEM
  CharacterCreateRequestMessage, //
  // OPCODES 0x30-3f
  CharacterRequestMessage, //
  LoadMapMessage,          //
  SetCurrentAvatarMessage, //
  ObjectHeldMessage,       //
  WeaponFireMessage,       //
  AvatarJumpMessage,       //
  PickupItemMessage,       //
  DropItemMessage,         //
  // 0x38
  InventoryStateMessage,        //
  ChangeFireStateMessage_Start, //
  ChangeFireStateMessage_Stop,  //
  UnknownMessage59,             //
  GenericCollisionMsg,          //
  QuantityUpdateMessage,        //
  ArmorChangedMessage,          //
  ProjectileStateMessage,       //
  // OPCODES 0x40-4f
  MountVehicleCargoMsg,         //
  DismountVehicleCargoMsg,      //
  CargoMountPointStatusMessage, //
  BeginZoningMessage,           //
  ItemTransactionMessage,       //
  ItemTransactionResultMessage, //
  ChangeFireModeMessage,        //
  ChangeAmmoMessage,            //
  // 0x48
  TimeOfDayMessage,         //
  UnknownMessage73,         // PPT_PROJECTILE_EVENT_BLOCK
  SpawnRequestMessage,      //
  DeployRequestMessage,     //
  UnknownMessage76,         // PPT_BUILDINGSTATECHANGED
  RepairMessage,            //
  ServerVehicleOverrideMsg, //
  LashMessage,
  // OPCODES 0x50-5f
  TargetingInfoMessage,         //
  TriggerEffectMessage,         //
  WeaponDryFireMessage,         //
  DroppodLaunchRequestMessage,  //
  HackMessage,                  //
  DroppodLaunchResponseMessage, //
  GenericObjectActionMessage,   //
  AvatarVehicleTimerMessage,    //
  // 0x58
  AvatarImplantMessage,  //
  UnknownMessage89,      // PPT_SEARCHMESSAGE
  DelayedPathMountMsg,   //
  OrbitalShuttleTimeMsg, //
  AIDamage,              //
  DeployObjectMessage,   //
  FavoritesRequest,      //
  FavoritesResponse,     //
  // OPCODES 0x60-6f
  FavoritesMessage,            //
  ObjectDetectedMessage,       //
  SplashHitMessage,            //
  SetChatFilterMessage,        //
  AvatarSearchCriteriaMessage, //
  AvatarSearchResponse,        //
  WeaponJammedMessage,         //
  LinkDeadAwarenessMsg,        //
  // 0x68
  DroppodFreefallingMessage,   //
  AvatarFirstTimeEventMessage, //
  AggravatedDamageMessage,     //
  TriggerSoundMessage,         //
  LootItemMessage,             //
  VehicleSubStateMessage,      //
  SquadMembershipRequest,      //
  SquadMembershipResponse,     //
  // OPCODES 0x70-7f
  SquadMemberEvent,                  //
  PlatoonEvent,                      //
  FriendsRequest,                    //
  FriendsResponse,                   //
  TriggerEnvironmentalDamageMessage, //
  TrainingZoneMessage,               //
  DeployableObjectsInfoMessage,      //
  SquadState,
  // 0x78
  OxygenStateMessage,      //
  TradeMessage,            //
  UnknownMessage122,       //
  DamageFeedbackMessage,   //
  DismountBuildingMsg,     //
  UnknownMessage125,       // PPT_MOUNTBUILDING
  UnknownMessage126,       // PPT_INTENDEDDROPZONE
  AvatarStatisticsMessage, //
  // OPCODES 0x80-8f
  GenericObjectAction2Message, //
  DestroyDisplayMessage,       //
  TriggerBotAction,            //
  SquadWaypointRequest,        //
  SquadWaypointEvent,          //
  OffshoreVehicleMessage,      //
  ObjectDeployedMessage,       //
  ObjectDeployedCountMessage,  //
  // 0x88
  WeaponDelayFireMessage,   //
  BugReportMessage,         //
  PlayerStasisMessage,      //
  UnknownMessage139,        //
  OutfitMembershipRequest,  //
  OutfitMembershipResponse, //
  OutfitRequest,            //
  OutfitEvent,              //
  // OPCODES 0x90-9f
  OutfitMemberEvent,                //
  OutfitMemberUpdate,               //
  PlanetsideStringAttributeMessage, //
  DataChallengeMessage,             //
  DataChallengeMessageResp,         //
  WeatherMessage,                   //
  SimDataChallenge,                 //
  SimDataChallengeResp,             //
  // 0x98
  OutfitListEvent,         //
  EmpireIncentivesMessage, //
  InvalidTerrainMessage,   //
  SyncMessage,             //
  DebugDrawMessage,        //
  SoulMarkMessage,         //
  UplinkPositionEvent,     //
  HotSpotUpdateMessage,    //
  // OPCODES 0xa0-af
  BuildingInfoUpdateMessage, //
  FireHintMessage,           //
  UplinkRequest,             //
  UplinkResponse,            //
  WarpgateRequest,           //
  WarpgateResponse,          //
  DamageWithPositionMessage, //
  GenericActionMessage,      //
  // 0xa8
  ContinentalLockUpdateMessage, //
  AvatarGrenadeStateMessage,    //
  UnknownMessage170,            //
  UnknownMessage171,            //
  ReleaseAvatarRequestMessage,  //
  AvatarDeadStateMessage,       //
  CSAssistMessage,              //
  CSAssistCommentMessage,       //
  // OPCODES 0xb0-bf
  VoiceHostRequest,            //
  VoiceHostKill,               //
  VoiceHostInfo,               //
  BattleplanMessage,           //
  BattleExperienceMessage,     //
  TargetingImplantRequest,     //
  ZonePopulationUpdateMessage, //
  DisconnectMessage,           //
  // 0xb8
  ExperienceAddedMessage,       //
  OrbitalStrikeWaypointMessage, //
  KeepAliveMessage,             //
  MapObjectStateBlockMessage,   //
  SnoopMsg,                     //
  PlayerStateMessageUpstream,   //
  PlayerStateShiftMessage,      //
  ZipLineMessage,               //
  // OPCODES 0xc0-cf
  CaptureFlagUpdateMessage,                  //
  VanuModuleUpdateMessage,                   //
  FacilityBenefitShieldChargeRequestMessage, //
  ProximityTerminalUseMessage,               //
  QuantityDeltaUpdateMessage,                //
  ChainLashMessage,                          //
  ZoneInfoMessage,                           //
  LongRangeProjectileInfoMessage,            //
  // 0xc8
  WeaponLazeTargetPositionMessage, //
  ModuleLimitsMessage,             //
  OutfitBenefitMessage,            //
  EmpireChangeTimeMessage,         //
  ClockCalibrationMessage,         //
  DensityLevelUpdateMessage,       //
  ActOfGodMessage,                 //
  AvatarAwardMessage,              //
  // OPCODES 0xd0-df
  UnknownMessage208,                    //
  DisplayedAwardMessage,                //
  RespawnAMSInfoMessage,                //
  ComponentDamageMessage,               //
  GenericObjectActionAtPositionMessage, //
  PropertyOverrideMessage,              //
  WarpgateLinkOverrideMessage,          //
  EmpireBenefitsMessage,                //
  // 0xd8
  ForceEmpireMessage,               //
  BroadcastWarpgateUpdateMessage,   //
  UnknownMessage218,                //
  SquadMainTerminalMessage,         //
  SquadMainTerminalResponseMessage, //
  SquadOrderMessage,                //
  SquadOrderResponse,               //
  ZoneLockInfoMessage,              //
  // OPCODES 0xe0-ef
  SquadBindInfoMessage,               //
  AudioSequenceMessage,               //
  SquadFacilityBindInfoMessage,       //
  ZoneForcedCavernConnectionsMessage, //
  MissionActionMessage,               //
  MissionKillTriggerMessage,          //
  ReplicationStreamMessage,           //
  SquadDefinitionActionMessage,       //
  // 0xe8
  SquadDetailDefinitionUpdateMessage, //
  TacticsMessage,                     //
  RabbitUpdateMessage,                //
  SquadInvitationRequestMessage,      //
  CharacterKnowledgeMessage,          //
  GameScoreUpdateMessage,             //
  UnknownMessage238,                  //
  OrderTerminalBugMessage,            //
  // OPCODES 0xf0-f3
  QueueTimedHelpMessage, //
  MailMessage,           //
  GameVarUpdate,         //
  ClientCheatedMessage   // last known message type (243, 0xf3)
  = Value

  /* The message names the opcode only, which is what identifies the missing marshaller.
     Keep it cheap to build: Err takes its message by value, so anything interpolated here is
     constructed eagerly for every packet that lands on an unimplemented opcode, and a great
     many opcodes are still stubs. Rendering the payload would cost a string twice the
     packet's length each time. */
  private def noDecoder(opcode: GamePacketOpcode.Type) =
    (_: BitVector) => Attempt.failure(Err(s"Could not find a marshaller for game packet $opcode"))

  /// Mapping of packet IDs to decoders. Notice that we are using the @switch annotation which ensures that the Scala
  /// compiler will be able to optimize this as a lookup table (switch statement). Microbenchmarks show a nearly 400x
  /// speedup when using a switch (given the worst case of not finding a decoder)
  def getPacketDecoder(opcode: GamePacketOpcode.Type): BitVector => Attempt[DecodeResult[PlanetSideGamePacket]] =
    (opcode.id: @switch) match {
      // OPCODES 0x00-0f
      case 0x00 => noDecoder(Unknown0)
      case 0x01 => packets.LoginMessage.decode
      case 0x02 => packets.LoginRespMessage.decode
      case 0x03 => packets.ConnectToWorldRequestMessage.decode
      case 0x04 => packets.ConnectToWorldMessage.decode
      case 0x05 => packets.VNLWorldStatusMessage.decode
      case 0x06 => noDecoder(UnknownMessage6)
      case 0x07 => noDecoder(UnknownMessage7)
      // 0x08
      case 0x08 => packets.PlayerStateMessage.decode
      case 0x09 => packets.HitMessage.decode
      case 0x0a => packets.HitHint.decode
      case 0x0b => packets.DamageMessage.decode
      case 0x0c => packets.DestroyMessage.decode
      case 0x0d => packets.ReloadMessage.decode
      case 0x0e => packets.MountVehicleMsg.decode
      case 0x0f => packets.DismountVehicleMsg.decode

      // OPCODES 0x10-1f
      case 0x10 => packets.UseItemMessage.decode
      case 0x11 => packets.MoveItemMessage.decode
      case 0x12 => packets.ChatMsg.decode
      case 0x13 => packets.CharacterNoRecordMessage.decode
      case 0x14 => packets.CharacterInfoMessage.decode
      case 0x15 => noDecoder(UnknownMessage21)
      case 0x16 => packets.BindPlayerMessage.decode
      case 0x17 => packets.ObjectCreateMessage.decode
      // 0x18
      case 0x18 => packets.ObjectCreateDetailedMessage.decode
      case 0x19 => packets.ObjectDeleteMessage.decode
      case 0x1a => packets.PingMsg.decode
      case 0x1b => packets.VehicleStateMessage.decode
      case 0x1c => packets.FrameVehicleStateMessage.decode
      case 0x1d => packets.GenericObjectStateMsg.decode
      case 0x1e => packets.ChildObjectStateMessage.decode
      case 0x1f => packets.ActionResultMessage.decode

      // OPCODES 0x20-2f
      case 0x20 => noDecoder(UnknownMessage32)
      case 0x21 => packets.ActionProgressMessage.decode
      case 0x22 => packets.ActionCancelMessage.decode
      case 0x23 => noDecoder(ActionCancelAcknowledgeMessage)
      case 0x24 => packets.SetEmpireMessage.decode
      case 0x25 => packets.EmoteMsg.decode
      case 0x26 => packets.UnuseItemMessage.decode
      case 0x27 => packets.ObjectDetachMessage.decode
      // 0x28
      case 0x28 => packets.CreateShortcutMessage.decode
      case 0x29 => packets.ChangeShortcutBankMessage.decode
      case 0x2a => packets.ObjectAttachMessage.decode
      case 0x2b => noDecoder(UnknownMessage43)
      case 0x2c => packets.PlanetsideAttributeMessage.decode
      case 0x2d => packets.RequestDestroyMessage.decode
      case 0x2e => noDecoder(UnknownMessage46)
      case 0x2f => packets.CharacterCreateRequestMessage.decode

      // OPCODES 0x30-3f
      case 0x30 => packets.CharacterRequestMessage.decode
      case 0x31 => packets.LoadMapMessage.decode
      case 0x32 => packets.SetCurrentAvatarMessage.decode
      case 0x33 => packets.ObjectHeldMessage.decode
      case 0x34 => packets.WeaponFireMessage.decode
      case 0x35 => packets.AvatarJumpMessage.decode
      case 0x36 => packets.PickupItemMessage.decode
      case 0x37 => packets.DropItemMessage.decode
      // 0x38
      case 0x38 => packets.InventoryStateMessage.decode
      case 0x39 => packets.ChangeFireStateMessage_Start.decode
      case 0x3a => packets.ChangeFireStateMessage_Stop.decode
      case 0x3b => noDecoder(UnknownMessage59)
      case 0x3c => packets.GenericCollisionMsg.decode
      case 0x3d => packets.QuantityUpdateMessage.decode
      case 0x3e => packets.ArmorChangedMessage.decode
      case 0x3f => packets.ProjectileStateMessage.decode

      // OPCODES 0x40-4f
      case 0x40 => packets.MountVehicleCargoMsg.decode
      case 0x41 => packets.DismountVehicleCargoMsg.decode
      case 0x42 => packets.CargoMountPointStatusMessage.decode
      case 0x43 => packets.BeginZoningMessage.decode
      case 0x44 => packets.ItemTransactionMessage.decode
      case 0x45 => packets.ItemTransactionResultMessage.decode
      case 0x46 => packets.ChangeFireModeMessage.decode
      case 0x47 => packets.ChangeAmmoMessage.decode
      // 0x48
      case 0x48 => packets.TimeOfDayMessage.decode
      case 0x49 => noDecoder(UnknownMessage73)
      case 0x4a => packets.SpawnRequestMessage.decode
      case 0x4b => packets.DeployRequestMessage.decode
      case 0x4c => noDecoder(UnknownMessage76)
      case 0x4d => packets.RepairMessage.decode
      case 0x4e => packets.ServerVehicleOverrideMsg.decode
      case 0x4f => packets.LashMessage.decode

      // OPCODES 0x50-5f
      case 0x50 => packets.TargetingInfoMessage.decode
      case 0x51 => packets.TriggerEffectMessage.decode
      case 0x52 => packets.WeaponDryFireMessage.decode
      case 0x53 => packets.DroppodLaunchRequestMessage.decode
      case 0x54 => packets.HackMessage.decode
      case 0x55 => packets.DroppodLaunchResponseMessage.decode
      case 0x56 => packets.GenericObjectActionMessage.decode
      case 0x57 => packets.AvatarVehicleTimerMessage.decode
      // 0x58
      case 0x58 => packets.AvatarImplantMessage.decode
      case 0x59 => noDecoder(UnknownMessage89)
      case 0x5a => packets.DelayedPathMountMsg.decode
      case 0x5b => packets.OrbitalShuttleTimeMsg.decode
      case 0x5c => packets.AIDamage.decode
      case 0x5d => packets.DeployObjectMessage.decode
      case 0x5e => packets.FavoritesRequest.decode
      case 0x5f => noDecoder(FavoritesResponse)

      // OPCODES 0x60-6f
      case 0x60 => packets.FavoritesMessage.decode
      case 0x61 => packets.ObjectDetectedMessage.decode
      case 0x62 => packets.SplashHitMessage.decode
      case 0x63 => packets.SetChatFilterMessage.decode
      case 0x64 => packets.AvatarSearchCriteriaMessage.decode
      case 0x65 => noDecoder(AvatarSearchResponse)
      case 0x66 => packets.WeaponJammedMessage.decode
      case 0x67 => noDecoder(LinkDeadAwarenessMsg)
      // 0x68
      case 0x68 => packets.DroppodFreefallingMessage.decode
      case 0x69 => packets.AvatarFirstTimeEventMessage.decode
      case 0x6a => packets.AggravatedDamageMessage.decode
      case 0x6b => packets.TriggerSoundMessage.decode
      case 0x6c => packets.LootItemMessage.decode
      case 0x6d => packets.VehicleSubStateMessage.decode
      case 0x6e => packets.SquadMembershipRequest.decode
      case 0x6f => packets.SquadMembershipResponse.decode

      // OPCODES 0x70-7f
      case 0x70 => packets.SquadMemberEvent.decode
      case 0x71 => noDecoder(PlatoonEvent)
      case 0x72 => packets.FriendsRequest.decode
      case 0x73 => packets.FriendsResponse.decode
      case 0x74 => packets.TriggerEnvironmentalDamageMessage.decode
      case 0x75 => packets.TrainingZoneMessage.decode
      case 0x76 => packets.DeployableObjectsInfoMessage.decode
      case 0x77 => packets.SquadState.decode
      // 0x78
      case 0x78 => packets.OxygenStateMessage.decode
      case 0x79 => packets.TradeMessage.decode
      case 0x7a => noDecoder(UnknownMessage122)
      case 0x7b => packets.DamageFeedbackMessage.decode
      case 0x7c => packets.DismountBuildingMsg.decode
      case 0x7d => noDecoder(UnknownMessage125)
      case 0x7e => noDecoder(UnknownMessage126)
      case 0x7f => packets.AvatarStatisticsMessage.decode

      // OPCODES 0x80-8f
      case 0x80 => packets.GenericObjectAction2Message.decode
      case 0x81 => packets.DestroyDisplayMessage.decode
      case 0x82 => noDecoder(TriggerBotAction)
      case 0x83 => packets.SquadWaypointRequest.decode
      case 0x84 => packets.SquadWaypointEvent.decode
      case 0x85 => packets.OffshoreVehicleMessage.decode
      case 0x86 => packets.ObjectDeployedMessage.decode
      case 0x87 => noDecoder(ObjectDeployedCountMessage)
      // 0x88
      case 0x88 => packets.WeaponDelayFireMessage.decode
      case 0x89 => packets.BugReportMessage.decode
      case 0x8a => packets.PlayerStasisMessage.decode
      case 0x8b => noDecoder(UnknownMessage139)
      case 0x8c => packets.OutfitMembershipRequest.decode
      case 0x8d => packets.OutfitMembershipResponse.decode
      case 0x8e => packets.OutfitRequest.decode
      case 0x8f => packets.OutfitEvent.decode

      // OPCODES 0x90-9f
      case 0x90 => packets.OutfitMemberEvent.decode
      case 0x91 => packets.OutfitMemberUpdate.decode
      case 0x92 => packets.PlanetsideStringAttributeMessage.decode
      case 0x93 => packets.DataChallengeMessage.decode
      case 0x94 => packets.DataChallengeMessageResp.decode
      case 0x95 => packets.WeatherMessage.decode
      case 0x96 => packets.SimDataChallenge.decode
      case 0x97 => packets.SimDataChallengeResp.decode
      // 0x98
      case 0x98 => packets.OutfitListEvent.decode
      case 0x99 => noDecoder(EmpireIncentivesMessage)
      case 0x9a => packets.InvalidTerrainMessage.decode
      case 0x9b => noDecoder(SyncMessage)
      case 0x9c => packets.DebugDrawMessage.decode
      case 0x9d => noDecoder(SoulMarkMessage)
      case 0x9e => packets.UplinkPositionEvent.decode
      case 0x9f => packets.HotSpotUpdateMessage.decode

      // OPCODES 0xa0-af
      case 0xa0 => packets.BuildingInfoUpdateMessage.decode
      case 0xa1 => packets.FireHintMessage.decode
      case 0xa2 => packets.UplinkRequest.decode
      case 0xa3 => packets.UplinkResponse.decode
      case 0xa4 => packets.WarpgateRequest.decode
      case 0xa5 => noDecoder(WarpgateResponse)
      case 0xa6 => packets.DamageWithPositionMessage.decode
      case 0xa7 => packets.GenericActionMessage.decode
      // 0xa8
      case 0xa8 => packets.ContinentalLockUpdateMessage.decode
      case 0xa9 => packets.AvatarGrenadeStateMessage.decode
      case 0xaa => noDecoder(UnknownMessage170)
      case 0xab => noDecoder(UnknownMessage171)
      case 0xac => packets.ReleaseAvatarRequestMessage.decode
      case 0xad => packets.AvatarDeadStateMessage.decode
      case 0xae => noDecoder(CSAssistMessage)
      case 0xaf => noDecoder(CSAssistCommentMessage)

      // OPCODES 0xb0-bf
      case 0xb0 => packets.VoiceHostRequest.decode
      case 0xb1 => packets.VoiceHostKill.decode
      case 0xb2 => packets.VoiceHostInfo.decode
      case 0xb3 => packets.BattleplanMessage.decode
      case 0xb4 => packets.BattleExperienceMessage.decode
      case 0xb5 => packets.TargetingImplantRequest.decode
      case 0xb6 => packets.ZonePopulationUpdateMessage.decode
      case 0xb7 => packets.DisconnectMessage.decode
      // 0xb8
      case 0xb8 => packets.ExperienceAddedMessage.decode
      case 0xb9 => packets.OrbitalStrikeWaypointMessage.decode
      case 0xba => packets.KeepAliveMessage.decode
      case 0xbb => noDecoder(MapObjectStateBlockMessage)
      case 0xbc => noDecoder(SnoopMsg)
      case 0xbd => packets.PlayerStateMessageUpstream.decode
      case 0xbe => packets.PlayerStateShiftMessage.decode
      case 0xbf => packets.ZipLineMessage.decode

      // OPCODES 0xc0-cf
      case 0xc0 => packets.CaptureFlagUpdateMessage.decode
      case 0xc1 => noDecoder(VanuModuleUpdateMessage)
      case 0xc2 => packets.FacilityBenefitShieldChargeRequestMessage.decode
      case 0xc3 => packets.ProximityTerminalUseMessage.decode
      case 0xc4 => packets.QuantityDeltaUpdateMessage.decode
      case 0xc5 => packets.ChainLashMessage.decode
      case 0xc6 => packets.ZoneInfoMessage.decode
      case 0xc7 => packets.LongRangeProjectileInfoMessage.decode
      // 0xc8
      case 0xc8 => packets.WeaponLazeTargetPositionMessage.decode
      case 0xc9 => noDecoder(ModuleLimitsMessage)
      case 0xca => noDecoder(OutfitBenefitMessage)
      case 0xcb => noDecoder(EmpireChangeTimeMessage)
      case 0xcc => noDecoder(ClockCalibrationMessage)
      case 0xcd => packets.DensityLevelUpdateMessage.decode
      case 0xce => noDecoder(ActOfGodMessage)
      case 0xcf => packets.AvatarAwardMessage.decode

      // OPCODES 0xd0-df
      case 0xd0 => noDecoder(UnknownMessage208)
      case 0xd1 => packets.DisplayedAwardMessage.decode
      case 0xd2 => packets.RespawnAMSInfoMessage.decode
      case 0xd3 => packets.ComponentDamageMessage.decode
      case 0xd4 => packets.GenericObjectActionAtPositionMessage.decode
      case 0xd5 => packets.PropertyOverrideMessage.decode
      case 0xd6 => packets.WarpgateLinkOverrideMessage.decode
      case 0xd7 => packets.EmpireBenefitsMessage.decode
      // 0xd8
      case 0xd8 => noDecoder(ForceEmpireMessage)
      case 0xd9 => packets.BroadcastWarpgateUpdateMessage.decode
      case 0xda => noDecoder(UnknownMessage218)
      case 0xdb => noDecoder(SquadMainTerminalMessage)
      case 0xdc => noDecoder(SquadMainTerminalResponseMessage)
      case 0xdd => noDecoder(SquadOrderMessage)
      case 0xde => noDecoder(SquadOrderResponse)
      case 0xdf => packets.ZoneLockInfoMessage.decode

      // OPCODES 0xe0-ef
      case 0xe0 => noDecoder(SquadBindInfoMessage)
      case 0xe1 => noDecoder(AudioSequenceMessage)
      case 0xe2 => noDecoder(SquadFacilityBindInfoMessage)
      case 0xe3 => packets.ZoneForcedCavernConnectionsMessage.decode
      case 0xe4 => noDecoder(MissionActionMessage)
      case 0xe5 => noDecoder(MissionKillTriggerMessage)
      case 0xe6 => packets.ReplicationStreamMessage.decode
      case 0xe7 => packets.SquadDefinitionActionMessage.decode
      // 0xe8
      case 0xe8 => packets.SquadDetailDefinitionUpdateMessage.decode
      case 0xe9 => noDecoder(TacticsMessage)
      case 0xea => noDecoder(RabbitUpdateMessage)
      case 0xeb => packets.SquadInvitationRequestMessage.decode
      case 0xec => packets.CharacterKnowledgeMessage.decode
      case 0xed => noDecoder(GameScoreUpdateMessage)
      case 0xee => noDecoder(UnknownMessage238)
      case 0xef => noDecoder(OrderTerminalBugMessage)

      // OPCODES 0xf0-f3
      case 0xf0 => noDecoder(QueueTimedHelpMessage)
      case 0xf1 => packets.MailMessage.decode
      case 0xf2 => noDecoder(GameVarUpdate)
      case 0xf3 => noDecoder(ClientCheatedMessage)
      case _    => noDecoder(opcode)
    }

  implicit val codec: Codec[this.Value] = PacketHelpers.createEnumerationCodec(this, uint8L)
}
