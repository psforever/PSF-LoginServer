// Copyright (c) 2019 PSForever
package net.psforever.objects.teamwork

import akka.actor.{Actor, ActorContext, ActorRef, Cancellable, Props}
import net.psforever.objects.DefaultCancellable
import net.psforever.types.SquadWaypoints
import services.teamwork.SquadService.WaypointData
import services.teamwork.SquadSwitchboard

class SquadFeatures(val Squad : Squad) {
  /**
    * `initialAssociation` per squad is similar to "Does this squad want to recruit members?"
    * The squad does not have to be flagged.
    * Dispatches an `AssociateWithSquad` `SDAM` to the squad leader and ???
    * and then a `SDDUM` that includes at least the squad owner name and char id.
    * Dispatched only once when a squad is first listed
    * or when the squad leader searches for recruits by proximity or for certain roles or by invite
    * or when a spontaneous squad forms,
    * whatever happens first.
    * Additionally, the packets are also sent when the check is made when the continent is changed (or set).
    */
  private var initialAssociation : Boolean = true
  /**
    * na
    */
  private var switchboard : ActorRef = ActorRef.noSender
  /**
    * Waypoint data.
    * The first four slots are used for squad waypoints.
    * The fifth slot is used for the squad leader experience waypoint.<br>
    * <br>
    * All of the waypoints constantly exist as long as the squad to which they are attached exists.
    * They are merely "activated" and "deactivated."
    * When "activated," the waypoint knows on which continent to appear and where on the map and in the game world to be positioned.
    * Waypoints manifest in the game world as a far-off beam of light that extends into the sky
    * and whose ground contact utilizes a downwards pulsating arrow.
    * On the continental map and deployment map, they appear as a diamond, with a differentiating number where applicable.
    * The squad leader experience rally, for example, does not have a number like the preceding four waypoints.
    * @see `Start`
    */
  private var waypoints : Array[WaypointData] = Array[WaypointData]()
  /**
    * The particular position being recruited right at the moment.
    * When `None`. no highlighted searches have been indicated.
    * When a positive integer or 0, indicates distributed `LookingForSquadRoleInvite` messages as recorded by `proxyInvites`.
    * Only one position may bne actively recruited at a time in this case.
    * When -1, indicates distributed `ProximityIvite` messages as recorded by `proxyInvites`.
    * Previous efforts may or may not be forgotten if there is a switch between the two modes.
    */
  private var searchForRole : Option[Int] = None
  /**
    * Handle persistent data related to `ProximityInvite` and `LookingForSquadRoleInvite` messages
    */
  private var proxyInvites : List[Long] = Nil

  private var requestInvitePrompt : Cancellable = DefaultCancellable.obj
  /**
    * These useres rejected invitation to this squad.
    * For the purposes of wide-searches for membership
    * such as Looking For Squad checks and proximity invitation,
    * the unique character identifier numbers in this list are skipped.
    * Direct invitation requests from the non sqad member should remain functional.
    */
  private var refusedPlayers : List[Long] = Nil
  private var autoApproveInvitationRequests : Boolean = true
  private var locationFollowsSquadLead : Boolean = true

  private var listed : Boolean = false

  private lazy val channel : String = s"${Squad.Faction}-Squad${Squad.GUID.guid}"

  def Start(implicit context : ActorContext) : SquadFeatures = {
    switchboard = context.actorOf(Props[SquadSwitchboard], s"squad${Squad.GUID.guid}")
    waypoints = Array.fill[WaypointData](SquadWaypoints.values.size)(new WaypointData())
    this
  }

  def Stop : SquadFeatures = {
    switchboard ! akka.actor.PoisonPill
    switchboard = Actor.noSender
    waypoints = Array.empty
    requestInvitePrompt.cancel
    this
  }

  def InitialAssociation : Boolean = initialAssociation

  def InitialAssociation_=(assoc : Boolean) : Boolean = {
    initialAssociation = assoc
    InitialAssociation
  }

  def Switchboard : ActorRef = switchboard

  def Waypoints : Array[WaypointData] = waypoints

  def SearchForRole : Option[Int] = searchForRole

  def SearchForRole_=(role : Int) : Option[Int] = SearchForRole_=(Some(role))

  def SearchForRole_=(role : Option[Int]) : Option[Int] = {
    searchForRole = role
    SearchForRole
  }

  def ProxyInvites : List[Long] = proxyInvites

  def ProxyInvites_=(list : List[Long]) : List[Long] = {
    proxyInvites = list
    ProxyInvites
  }

  def Refuse : List[Long] = refusedPlayers

  def Refuse_=(charId : Long) : List[Long] = {
    Refuse_=(List(charId))
  }

  def Refuse_=(list : List[Long]) : List[Long] = {
    refusedPlayers = list ++ refusedPlayers
    Refuse
  }

  def LocationFollowsSquadLead : Boolean = locationFollowsSquadLead

  def LocationFollowsSquadLead_=(follow : Boolean) : Boolean = {
    locationFollowsSquadLead = follow
    LocationFollowsSquadLead
  }

  def AutoApproveInvitationRequests : Boolean = autoApproveInvitationRequests

  def AutoApproveInvitationRequests_=(autoApprove : Boolean) : Boolean = {
    autoApproveInvitationRequests = autoApprove
    AutoApproveInvitationRequests
  }

  def Listed : Boolean = listed

  def Listed_=(announce : Boolean) : Boolean = {
    listed = announce
    Listed
  }

  def ToChannel : String = channel

  def Prompt : Cancellable = requestInvitePrompt

  def Prompt_=(callback: Cancellable) : Cancellable = {
    if(requestInvitePrompt.isCancelled) {
      requestInvitePrompt = callback
    }
    Prompt
  }
}
