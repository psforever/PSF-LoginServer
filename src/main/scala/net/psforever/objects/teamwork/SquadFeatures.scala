// Copyright (c) 2019 PSForever
package net.psforever.objects.teamwork

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.Default
import net.psforever.packet.game.WaypointInfo
import net.psforever.types.{PlanetSideGUID, SquadWaypoint, Vector3}
import net.psforever.services.teamwork.{SquadSubscriptionEntity, SquadSwitchboard}

class SquadFeatures(val Squad: Squad) {

  /**
    * `initialAssociation` per squad is similar to "Does this squad want to recruit members?"
    * The squad does not have to be flagged.
    * Dispatches an `AssociateWithSquad` `SquadDefinitionActionMessage` packet to the squad leader and ???
    * and then a `SquadDetailDefinitionUpdateMessage` that includes at least the squad owner name and char id.
    * Dispatched only once when a squad is first listed
    * or when the squad leader searches for recruits by proximity or for certain roles or by invite
    * or when a spontaneous squad forms,
    * whatever happens first.
    * Additionally, the packets are also sent when the check is made when the continent is changed (or set).
    */
  private var initialAssociation: Boolean = true

  /**
    * na
    */
  private var switchboard: ActorRef = ActorRef.noSender

  /**
    * Waypoint data.
    * The first four slots are used for squad waypoints.
    * The fifth slot is used for the squad leader experience waypoint.
    * There is a sixth waypoint used for a target that has been indicated by the laze pointer
    * but its id indication does not follow the indexes of the previous waypoints.<br>
    * <br>
    * All of the waypoints constantly exist as long as the squad to which they are attached exists.
    * They are merely "activated" and "deactivated."
    * When "activated," the waypoint knows on which continent to appear
    * and where on the map and in the game world to be positioned.
    * Waypoints manifest in the game world as a (usually far-off) beam of light that extends into the sky
    * and whose ground contact utilizes a downwards pulsating arrow.
    * On the continental map and deployment map, they appear as a diamond, with a different number where applicable.
    * The squad leader experience rally, for example, does not have a number like the preceding four waypoints.<br>
    * <br>
    * Laze waypoints are as numerous as the number of players in a squad and
    * exist only for fifteen seconds at a time.
    * They are not counted in this list.
    * @see `Start`
    */
  private var waypoints: Array[WaypointData] = Array[WaypointData]()

  /**
    * The particular position being recruited right at the moment.
    * When `None`, no highlighted searches have been indicated.
    * When a positive integer or 0, indicates distributed `LookingForSquadRoleInvite` messages as recorded by `proxyInvites`.
    * Only one position may be actively recruited at a time in this case.
    * When -1, indicates distributed `ProximityInvite` messages as recorded by `proxyInvites`.
    * Previous efforts may or may not be forgotten if there is a switch between the two modes.
    */
  private var searchForRole: Option[Int] = None

  /**
    * Handle persistent data related to `ProximityInvite` and `LookingForSquadRoleInvite` messages
    */
  private var proxyInvites: List[Long] = Nil

  /**
    * These users rejected invitation to this squad.
    * For the purposes of wide-searches for membership
    * such as Looking For Squad checks and proximity invitation,
    * the unique character identifier numbers in this list are skipped.
    * Direct invitation requests from the non squad member should remain functional.
    */
  private var refusedPlayers: List[Long]             = Nil
  private var autoApproveInvitationRequests: Boolean = false
  private var locationFollowsSquadLead: Boolean      = true //TODO false

  private var listed: Boolean = false

  private lazy val channel: String = s"${Squad.Faction}-Squad${Squad.GUID.guid}"

  def Start(implicit context: ActorContext, subs: SquadSubscriptionEntity): SquadFeatures = {
    switchboard = context.actorOf(Props(classOf[SquadSwitchboard], this, subs), s"squad_${Squad.GUID.guid}_${System.currentTimeMillis}")
    waypoints = Array.fill[WaypointData](SquadWaypoint.values.size)(new WaypointData())
    this
  }

  def Stop: SquadFeatures = {
    switchboard ! akka.actor.PoisonPill
    switchboard = Default.Actor
    waypoints = Array.empty
    this
  }

  def InitialAssociation: Boolean = initialAssociation

  def InitialAssociation_=(assoc: Boolean): Boolean = {
    initialAssociation = assoc
    InitialAssociation
  }

  def Switchboard: ActorRef = switchboard

  def Waypoints: Array[WaypointData] = waypoints



  /**
    * Display the indicated waypoint.<br>
    * <br>
    * Despite the name, no waypoints are actually "added."
    * All of the waypoints constantly exist as long as the squad to which they are attached exists.
    * They are merely "activated" and "deactivated."
    * No waypoint is ever remembered for the laze-indicated target.
    * @see `SquadWaypointRequest`
    * @see `WaypointInfo`
    * @param guid the squad's unique identifier
    * @param waypointType the type of the waypoint
    * @param info information about the waypoint, as was reported by the client's packet
    * @return the waypoint data, if the waypoint type is changed
    */
  def AddWaypoint(
                   guid: PlanetSideGUID,
                   waypointType: SquadWaypoint,
                   info: WaypointInfo
                 ): Option[WaypointData] = {
    waypoints.lift(waypointType.value) match {
      case Some(point) =>
        point.zone_number = info.zone_number
        point.pos = info.pos
        Some(point)
      case None =>
        None
    }
  }

  /**
    * Hide the indicated waypoint.
    * Unused waypoints are marked by having a non-zero z-coordinate.<br>
    * <br>
    * Despite the name, no waypoints are actually "removed."
    * All of the waypoints constantly exist as long as the squad to which they are attached exists.
    * They are merely "activated" and "deactivated."
    * @param guid the squad's unique identifier
    * @param waypointType the type of the waypoint
    */
  def RemoveWaypoint(guid: PlanetSideGUID, waypointType: SquadWaypoint): Option[WaypointData] = {
    waypoints.lift(waypointType.value) match {
      case Some(point) =>
        val oldWaypoint = WaypointData(point.zone_number, point.pos)
        point.pos = Vector3.z(1)
        Some(oldWaypoint)
      case None =>
        None
    }
  }

  def SearchForRole: Option[Int] = searchForRole

  def SearchForRole_=(role: Int): Option[Int] = SearchForRole_=(Some(role))

  def SearchForRole_=(role: Option[Int]): Option[Int] = {
    searchForRole = role
    SearchForRole
  }

  def ProxyInvites: List[Long] = proxyInvites

  def ProxyInvites_=(list: List[Long]): List[Long] = {
    proxyInvites = list
    ProxyInvites
  }

  def DeniedPlayers(): List[Long] = refusedPlayers

  def DeniedPlayers(charId: Long): List[Long] = {
    DeniedPlayers(List(charId))
  }

  def DeniedPlayers(list: List[Long]): List[Long] = {
    refusedPlayers = list ++ refusedPlayers
    DeniedPlayers()
  }

  def AllowedPlayers(charId: Long): List[Long] = {
    AllowedPlayers(List(charId))
  }

  def AllowedPlayers(list: List[Long]): List[Long] = {
    refusedPlayers = refusedPlayers.filterNot(list.contains)
    DeniedPlayers()
  }

  def LocationFollowsSquadLead: Boolean = locationFollowsSquadLead

  def LocationFollowsSquadLead_=(follow: Boolean): Boolean = {
    locationFollowsSquadLead = follow
    LocationFollowsSquadLead
  }

  def AutoApproveInvitationRequests: Boolean = autoApproveInvitationRequests

  def AutoApproveInvitationRequests_=(autoApprove: Boolean): Boolean = {
    autoApproveInvitationRequests = autoApprove
    AutoApproveInvitationRequests
  }

  def Listed: Boolean = listed

  def Listed_=(announce: Boolean): Boolean = {
    listed = announce
    Listed
  }

  def ToChannel: String = channel
}
