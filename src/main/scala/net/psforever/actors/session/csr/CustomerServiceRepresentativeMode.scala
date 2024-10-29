// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import net.psforever.actors.session.support.{ChatFunctions, GalaxyHandlerFunctions, GeneralFunctions, LocalHandlerFunctions, ModeLogic, MountHandlerFunctions, PlayerMode, SessionData, SquadHandlerFunctions, TerminalHandlerFunctions, VehicleFunctions, VehicleHandlerFunctions, WeaponAndProjectileFunctions}
import net.psforever.objects.{Deployables, PlanetSideGameObject, Player, Session, Vehicle}
import net.psforever.objects.avatar.Certification
import net.psforever.objects.serverobject.ServerObject
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{ChatMsg, ObjectCreateDetailedMessage, PlanetsideAttributeMessage}
import net.psforever.packet.game.objectcreate.RibbonBars
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.chat.{CustomerServiceChannel, SpectatorChannel}
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types.{ChatMessageType, MeritCommendation, PlanetSideGUID}

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
    //
    data.zoning.displayZoningMessageWhenCancelled = false
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
      data.session = session.copy(avatar = newAvatar, player = player)
      Deployables.InitializeDeployableQuantities(newAvatar)
    }
    requireDismount(continent, player)
    data.keepAlivePersistenceFunc = keepAlivePersistanceCSR
    //
    CustomerServiceRepresentativeMode.renderPlayer(data, continent, player)
    if (player.silenced) {
      data.chat.commandIncomingSilence(session, ChatMsg(ChatMessageType.CMT_SILENCE, "player 0"))
    }
    data.chat.JoinChannel(SpectatorChannel)
    data.chat.JoinChannel(CustomerServiceChannel)
    data.sendResponse(ChatMsg(ChatMessageType.UNK_225, "CSR MODE ON"))
  }

  override def switchFrom(session: Session): Unit = {
    val player = session.player
    val avatar = session.avatar
    val continent = session.zone
    //
    data.zoning.displayZoningMessageWhenCancelled = true
    val newAvatar = avatar.copy(
      certifications = oldCertifications,
      decoration = avatar.decoration.copy(ribbonBars = oldRibbons)
    )
    oldCertifications = Set()
    oldRibbons = RibbonBars()
    player.avatar = newAvatar
    data.session = session.copy(avatar = newAvatar, player = player)
    Deployables.InitializeDeployableQuantities(newAvatar)
    //
    requireDismount(continent, player)
    data.keepAlivePersistenceFunc = data.keepAlivePersistence
    //
    CustomerServiceRepresentativeMode.renderPlayer(data, continent, player)
    data.chat.LeaveChannel(SpectatorChannel)
    data.chat.LeaveChannel(CustomerServiceChannel)
    data.sendResponse(ChatMsg(ChatMessageType.UNK_225, "CSR MODE OFF"))
  }

  private def requireDismount(zone: Zone, player: Player): Unit = {
    data.vehicles.GetMountableAndSeat(None, player, zone) match {
      case (Some(obj: Vehicle), Some(seatNum)) if seatNum == 0 =>
        data.vehicles.ServerVehicleOverrideStop(obj)
        obj.Actor ! ServerObject.AttributeMsg(10, 3) //faction-accessible driver seat
        obj.Actor ! Mountable.TryDismount(player, seatNum)
        player.VehicleSeated = None
      case (Some(obj), Some(seatNum)) =>
        obj.Actor ! Mountable.TryDismount(player, seatNum)
        player.VehicleSeated = None
      case _ =>
        player.VehicleSeated = None
    }
  }

  private def keepAlivePersistanceCSR(): Unit = {
    data.keepAlivePersistence()
    topOffHealthOfPlayer(data.player)
    data.continent.GUID(data.player.VehicleSeated)
      .collect {
        case obj: PlanetSideGameObject with Vitality => topOffHealth(obj)
      }
  }

  private def topOffHealth(obj: PlanetSideGameObject with Vitality): Unit = {
    obj match {
      case p: Player => topOffHealthOfPlayer(p)
      case v: Vehicle => topOffHealthOfVehicle(v)
      case o: PlanetSideGameObject with Vitality => topOffHealthOfGeneric(o)
      case _ => ()
    }
  }

  private def topOffHealthOfPlayer(player: Player): Unit = {
    //driver below half health, full heal
    val maxHealthOfPlayer = player.MaxHealth.toLong
    if (player.Health < maxHealthOfPlayer * 0.5f) {
      player.Health = maxHealthOfPlayer.toInt
      player.LogActivity(player.ClearHistory().head)
      data.sendResponse(PlanetsideAttributeMessage(player.GUID, 0, maxHealthOfPlayer))
      data.continent.AvatarEvents ! AvatarServiceMessage(data.zoning.zoneChannel, AvatarAction.PlanetsideAttribute(player.GUID, 0, maxHealthOfPlayer))
    }
  }

  private def topOffHealthOfVehicle(vehicle: Vehicle): Unit = {
    topOffHealthOfGeneric(vehicle)
    //vehicle shields below half, full shields
    val maxShieldsOfVehicle = vehicle.MaxShields.toLong
    val shieldsUi = vehicle.Definition.shieldUiAttribute
    if (vehicle.Shields < maxShieldsOfVehicle) {
      val guid = vehicle.GUID
      vehicle.Shields = maxShieldsOfVehicle.toInt
      data.sendResponse(PlanetsideAttributeMessage(guid, shieldsUi, maxShieldsOfVehicle))
      data.continent.VehicleEvents ! VehicleServiceMessage(
        data.continent.id,
        VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), guid, shieldsUi, maxShieldsOfVehicle)
      )
    }
  }

  private def topOffHealthOfGeneric(obj: PlanetSideGameObject with Vitality): Unit = {
    //below half health, full heal
    val guid = obj.GUID
    val maxHealthOf = obj.MaxHealth.toLong
    if (obj.Health < maxHealthOf) {
      obj.Health = maxHealthOf.toInt
      data.sendResponse(PlanetsideAttributeMessage(guid, 0, maxHealthOf))
      data.continent.VehicleEvents ! VehicleServiceMessage(
        data.continent.id,
        VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), guid, 0, maxHealthOf)
      )
    }
  }
}

case object CustomerServiceRepresentativeMode extends PlayerMode {
  def setup(data: SessionData): ModeLogic = {
    new CustomerServiceRepresentativeMode(data)
  }

  private[csr] def renderPlayer(data: SessionData, zone: Zone, player: Player): Unit = {
    val pguid = player.GUID
    val definition = player.Definition
    val objectClass = definition.ObjectId
    val packet = definition.Packet
    data.sendResponse(ObjectCreateDetailedMessage(
      objectClass,
      pguid,
      packet.DetailedConstructorData(player).get
    ))
    data.zoning.spawn.HandleSetCurrentAvatar(player)
    zone.AvatarEvents ! AvatarServiceMessage(zone.id, AvatarAction.LoadPlayer(
      pguid,
      objectClass,
      pguid,
      packet.ConstructorData(player).get,
      None
    ))
  }
}
