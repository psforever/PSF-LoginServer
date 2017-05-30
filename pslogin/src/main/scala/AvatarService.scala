// Copyright (c) 2016 PSForever.net to present
import akka.actor.Actor
import akka.event.{ActorEventBus, SubchannelClassification}
import akka.util.Subclassification
import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game.objectcreate.ConstructorData
import net.psforever.types.ExoSuitType
import net.psforever.packet.game.{PlanetSideGUID, PlayerStateMessageUpstream}
import net.psforever.types.Vector3

sealed trait Action

sealed trait Response

final case class Join(channel : String)
final case class Leave()
final case class LeaveAll()

object AvatarAction {
  final case class ArmorChanged(player_guid : PlanetSideGUID, suit : ExoSuitType.Value, subtype : Int) extends Action
  //final case class DropItem(pos : Vector3, orient : Vector3, item : PlanetSideGUID) extends Action
  final case class EquipmentInHand(player_guid : PlanetSideGUID, slot : Int, item : Equipment) extends Action
  final case class EquipmentOnGround(player_guid : PlanetSideGUID, pos : Vector3, orient : Vector3, item : Equipment) extends Action
  final case class LoadPlayer(player_guid : PlanetSideGUID, pdata : ConstructorData) extends Action
//  final case class LoadMap(msg : PlanetSideGUID) extends Action
//  final case class unLoadMap(msg : PlanetSideGUID) extends Action
  final case class ObjectDelete(player_guid : PlanetSideGUID, item_guid : PlanetSideGUID, unk : Int = 0) extends Action
  final case class ObjectHeld(player_guid : PlanetSideGUID, slot : Int) extends Action
  final case class PlanetsideAttribute(player_guid : PlanetSideGUID, attribute_type : Int, attribute_value : Long) extends Action
  final case class PlayerState(player_guid : PlanetSideGUID, msg : PlayerStateMessageUpstream, spectator : Boolean, weaponInHand : Boolean) extends Action
  final case class Reload(player_guid : PlanetSideGUID, mag : Int) extends Action
//  final case class PlayerStateShift(killer : PlanetSideGUID, victim : PlanetSideGUID) extends Action
//  final case class DestroyDisplay(killer : PlanetSideGUID, victim : PlanetSideGUID) extends Action
//  final case class HitHintReturn(killer : PlanetSideGUID, victim : PlanetSideGUID) extends Action
//  final case class ChangeWeapon(unk1 : Int, sessionId : Long) extends Action
}

object AvatarServiceResponse {
  final case class ArmorChanged(suit : ExoSuitType.Value, subtype : Int) extends Response
  //final case class DropItem(pos : Vector3, orient : Vector3, item : PlanetSideGUID) extends Response
  final case class EquipmentInHand(slot : Int, item : Equipment) extends Response
  final case class EquipmentOnGround(pos : Vector3, orient : Vector3, item : Equipment) extends Response
  final case class LoadPlayer(pdata : ConstructorData) extends Response
//  final case class unLoadMap() extends Response
//  final case class LoadMap() extends Response
  final case class ObjectDelete(item_guid : PlanetSideGUID, unk : Int) extends Response
  final case class ObjectHeld(slot : Int) extends Response
  final case class PlanetSideAttribute(attribute_type : Int, attribute_value : Long) extends Response
  final case class PlayerState(msg : PlayerStateMessageUpstream, spectator : Boolean, weaponInHand : Boolean) extends Response
  final case class Reload(mag : Int) extends Response
//  final case class PlayerStateShift(itemID : PlanetSideGUID) extends Response
//  final case class DestroyDisplay(itemID : PlanetSideGUID) extends Response
//  final case class HitHintReturn(itemID : PlanetSideGUID) extends Response
//  final case class ChangeWeapon(facingYaw : Int) extends Response
}

final case class AvatarServiceMessage(forChannel : String, actionMessage : Action)

final case class AvatarServiceResponse(toChannel : String, avatar_guid : PlanetSideGUID, replyMessage : Response)

/*
   /avatar/
 */

class AvatarEventBus extends ActorEventBus with SubchannelClassification {
  type Event = AvatarServiceResponse
  type Classifier = String

  protected def classify(event: Event): Classifier = event.toChannel

  protected def subclassification = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier) = x == y
    def isSubclass(x: Classifier, y: Classifier) = x.startsWith(y)
  }

  protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event
  }
}

class AvatarService extends Actor {
  //import AvatarServiceResponse._
  private [this] val log = org.log4s.getLogger

  override def preStart = {
    log.info("Starting...")
  }

  val AvatarEvents = new AvatarEventBus

  /*val channelMap = Map(
    AvatarMessageType.CMT_OPEN -> AvatarPath("local")
  )*/

  def receive = {
    case Join(channel) =>
      val path = "/Avatar/" + channel
      val who = sender()

      log.info(s"$who has joined $path")

      AvatarEvents.subscribe(who, path)
    case Leave() =>
      AvatarEvents.unsubscribe(sender())
    case LeaveAll() =>
      AvatarEvents.unsubscribe(sender())

    case AvatarServiceMessage(forChannel, action) =>
      action match {
        case AvatarAction.ArmorChanged(player_guid, suit, subtype) =>
          AvatarEvents.publish(
            AvatarServiceResponse("/Avatar/" + forChannel, player_guid, AvatarServiceResponse.ArmorChanged(suit, subtype))
          )
        case AvatarAction.EquipmentInHand(player_guid, slot, obj) =>
          AvatarEvents.publish(
            AvatarServiceResponse("/Avatar/" + forChannel, player_guid, AvatarServiceResponse.EquipmentInHand(slot, obj))
          )
        case AvatarAction.EquipmentOnGround(player_guid, pos, orient, obj) =>
          AvatarEvents.publish(
            AvatarServiceResponse("/Avatar/" + forChannel, player_guid, AvatarServiceResponse.EquipmentOnGround(pos, orient, obj))
          )
        case AvatarAction.LoadPlayer(player_guid, pdata) =>
          AvatarEvents.publish(
            AvatarServiceResponse("/Avatar/" + forChannel, player_guid, AvatarServiceResponse.LoadPlayer(pdata))
          )
        case AvatarAction.ObjectDelete(player_guid, item_guid, unk) =>
          AvatarEvents.publish(
            AvatarServiceResponse("/Avatar/" + forChannel, player_guid, AvatarServiceResponse.ObjectDelete(item_guid, unk))
          )
        case AvatarAction.ObjectHeld(player_guid, slot) =>
          AvatarEvents.publish(
            AvatarServiceResponse("/Avatar/" + forChannel, player_guid, AvatarServiceResponse.ObjectHeld(slot))
          )
        case AvatarAction.PlanetsideAttribute(guid, attribute_type, attribute_value) =>
          AvatarEvents.publish(
            AvatarServiceResponse("/Avatar/" + forChannel, guid, AvatarServiceResponse.PlanetSideAttribute(attribute_type, attribute_value))
          )
        case AvatarAction.PlayerState(guid, msg, spectator, weapon) =>
          AvatarEvents.publish(
            AvatarServiceResponse("/Avatar/" + forChannel, guid, AvatarServiceResponse.PlayerState(msg, spectator, weapon))
          )
        case AvatarAction.Reload(player_guid, mag) =>
          AvatarEvents.publish(
            AvatarServiceResponse("/Avatar/" + forChannel, player_guid, AvatarServiceResponse.Reload(mag))
          )
        case _ => ;
    }

      /*
    case AvatarService.PlayerStateMessage(msg) =>
      //      log.info(s"NEW: ${m}")
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(msg.avatar_guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, msg.avatar_guid,
          AvatarServiceReply.PlayerStateMessage(msg.pos, msg.vel, msg.facingYaw, msg.facingPitch, msg.facingYawUpper, msg.is_crouching, msg.is_jumping, msg.jump_thrust, msg.is_cloaked)
        ))

      }
    case AvatarService.LoadMap(msg) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(msg.guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, PlanetSideGUID(msg.guid),
          AvatarServiceReply.LoadMap()
        ))
      }
    case AvatarService.unLoadMap(msg) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(msg.guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, PlanetSideGUID(msg.guid),
          AvatarServiceReply.unLoadMap()
        ))
      }
    case AvatarService.ObjectHeld(msg) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(msg.guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, PlanetSideGUID(msg.guid),
          AvatarServiceReply.ObjectHeld()
        ))
      }
    case AvatarService.PlanetsideAttribute(guid, attribute_type, attribute_value) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, guid,
          AvatarServiceReply.PlanetSideAttribute(attribute_type, attribute_value)
        ))
      }
    case AvatarService.PlayerStateShift(killer, guid) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, guid,
          AvatarServiceReply.PlayerStateShift(killer)
        ))
      }
    case AvatarService.DestroyDisplay(killer, victim) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(victim)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, victim,
          AvatarServiceReply.DestroyDisplay(killer)
        ))
      }
    case AvatarService.HitHintReturn(source_guid,victim_guid) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(source_guid)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, victim_guid,
          AvatarServiceReply.DestroyDisplay(source_guid)
        ))
      }
    case AvatarService.ChangeWeapon(unk1, sessionId) =>
      val playerOpt: Option[PlayerAvatar] = PlayerMasterList.getPlayer(sessionId)
      if (playerOpt.isDefined) {
        val player: PlayerAvatar = playerOpt.get
        AvatarEvents.publish(AvatarMessage("/Avatar/" + player.continent, PlanetSideGUID(player.guid),
          AvatarServiceReply.ChangeWeapon(unk1)
        ))
      }
      */
    case msg =>
      log.info(s"Unhandled message $msg from $sender")
  }
}
