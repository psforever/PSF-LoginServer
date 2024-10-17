// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import net.psforever.actors.session.SessionActor
import net.psforever.actors.session.support.{ChatFunctions, GalaxyHandlerFunctions, GeneralFunctions, LocalHandlerFunctions, ModeLogic, MountHandlerFunctions, PlayerMode, SessionData, SquadHandlerFunctions, TerminalHandlerFunctions, VehicleFunctions, VehicleHandlerFunctions, WeaponAndProjectileFunctions}
import net.psforever.objects.{Deployables, Session, Vehicle}
import net.psforever.objects.avatar.Certification
import net.psforever.packet.PlanetSidePacket
import net.psforever.packet.game.{ChatMsg, ObjectCreateDetailedMessage}
import net.psforever.packet.game.objectcreate.{ObjectCreateMessageParent, RibbonBars}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.chat.{CustomerServiceChannel, SpectatorChannel}
import net.psforever.types.{ChatMessageType, MeritCommendation}

class CustomerServiceRepresentativeMode(data: SessionData) extends ModeLogic {
  val avatarResponse: AvatarHandlerLogic = AvatarHandlerLogic(data.avatarResponse)
  val chat: ChatFunctions = ChatLogic(data.chat)
  val galaxy: GalaxyHandlerFunctions = net.psforever.actors.session.normal.GalaxyHandlerLogic(data.galaxyResponseHandlers)
  val general: GeneralFunctions = GeneralLogic(data.general)
  val local: LocalHandlerFunctions = net.psforever.actors.session.normal.LocalHandlerLogic(data.localResponse)
  val mountResponse: MountHandlerFunctions = MountHandlerLogic(data.mountResponse)
  val shooting: WeaponAndProjectileFunctions = WeaponAndProjectileLogic(data.shooting)
  val squad: SquadHandlerFunctions = SquadHandlerLogic(data.squad)
  val terminals: TerminalHandlerFunctions = TerminalHandlerLogic(data.terminals)
  val vehicles: VehicleFunctions = VehicleLogic(data.vehicles)
  val vehicleResponse: VehicleHandlerFunctions = net.psforever.actors.session.normal.VehicleHandlerLogic(data.vehicleResponseOperations)

  private var oldRibbons: RibbonBars = RibbonBars()
  private var oldCertifications : Set[Certification] = Set()

  override def switchTo(session: Session): Unit = {
    val player = session.player
    val avatar = session.avatar
    val continent = session.zone
    val sendResponse: PlanetSidePacket=>Unit = data.sendResponse
    //
    if (oldCertifications.isEmpty) {
      oldCertifications = avatar.certifications
      oldRibbons = avatar.decoration.ribbonBars
      val newAvatar = avatar.copy(
        certifications = Certification.values.toSet,
        decoration = avatar.decoration.copy(ribbonBars = RibbonBars(
          MeritCommendation.CSAppreciation,
          MeritCommendation.Loser,
          MeritCommendation.Loser,
          MeritCommendation.CSAppreciation
        ))
      )
      player.avatar = newAvatar
      data.context.self ! SessionActor.SetAvatar(newAvatar)
      Deployables.InitializeDeployableQuantities(newAvatar)
    }
    val vehicleAndSeat = data.vehicles.GetMountableAndSeat(None, player, continent) match {
      case (Some(obj: Vehicle), Some(seatNum)) =>
        Some(ObjectCreateMessageParent(obj.GUID, seatNum))
      case _ =>
        None
    }
    //
    val pguid = player.GUID
    val definition = player.Definition
    val objectClass = definition.ObjectId
    val packet = definition.Packet
    sendResponse(ObjectCreateDetailedMessage(
      0L,
      objectClass,
      pguid,
      vehicleAndSeat,
      packet.DetailedConstructorData(player).get
    ))
    data.zoning.spawn.HandleSetCurrentAvatar(player)
    continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.LoadPlayer(
      pguid,
      objectClass,
      pguid,
      packet.ConstructorData(player).get,
      vehicleAndSeat
    ))
    if (player.silenced) {
      data.chat.commandIncomingSilence(session, ChatMsg(ChatMessageType.CMT_SILENCE, "player 0"))
    }
    data.chat.JoinChannel(SpectatorChannel)
    data.chat.JoinChannel(CustomerServiceChannel)
    sendResponse(ChatMsg(ChatMessageType.UNK_225, "CSR MODE ON"))
  }

  override def switchFrom(session: Session): Unit = {
    val player = session.player
    val avatar = session.avatar
    val continent = session.zone
    val sendResponse: PlanetSidePacket => Unit = data.sendResponse
    //
    val newAvatar = avatar.copy(
      certifications = oldCertifications,
      decoration = avatar.decoration.copy(ribbonBars = oldRibbons)
    )
    oldCertifications = Set()
    oldRibbons = RibbonBars()
    player.avatar = newAvatar
    data.context.self ! SessionActor.SetAvatar(newAvatar)
    val vehicleAndSeat = data.vehicles.GetMountableAndSeat(None, player, continent) match {
      case (Some(obj: Vehicle), Some(seatNum)) =>
        Some(ObjectCreateMessageParent(obj.GUID, seatNum))
      case _ =>
        None
    }
    Deployables.InitializeDeployableQuantities(newAvatar)
    //
    val pguid = player.GUID
    val definition = player.Definition
    val objectClass = definition.ObjectId
    val packet = definition.Packet
    sendResponse(ObjectCreateDetailedMessage(
      0L,
      objectClass,
      pguid,
      vehicleAndSeat,
      packet.DetailedConstructorData(player).get
    ))
    data.zoning.spawn.HandleSetCurrentAvatar(player)
    continent.AvatarEvents ! AvatarServiceMessage(continent.id, AvatarAction.LoadPlayer(
      pguid,
      objectClass,
      pguid,
      packet.ConstructorData(player).get,
      vehicleAndSeat
    ))
    data.chat.LeaveChannel(SpectatorChannel)
    data.chat.LeaveChannel(CustomerServiceChannel)
    sendResponse(ChatMsg(ChatMessageType.UNK_225, "CSR MODE OFF"))
  }
}

case object CustomerServiceRepresentativeMode extends PlayerMode {
  def setup(data: SessionData): ModeLogic = {
    new CustomerServiceRepresentativeMode(data)
  }
}
