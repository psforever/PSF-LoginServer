// Copyright (c) 2023 PSForever
package net.psforever.objects.serverobject.turret

import akka.actor.Cancellable
import net.psforever.objects.serverobject.ServerObjectControl
import net.psforever.objects.{Default, Player, Tool}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.Vector3

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * A control agency that handles messages being dispatched to a specific `FacilityTurret`.
 * These turrets are installed tangential to cavern facilities but are independent of the facility.
 * @param turret the `FacilityTurret` object being governed
 */
class VanuSentryControl(turret: FacilityTurret)
  extends ServerObjectControl
    with MountableTurretControl {
  def TurretObject: FacilityTurret     = turret
  def FactionObject: FacilityTurret    = turret
  def MountableObject: FacilityTurret  = turret
  def JammableObject: FacilityTurret   = turret
  def DamageableObject: FacilityTurret = turret
  def RepairableObject: FacilityTurret = turret

  // Used for timing ammo recharge for vanu turrets in caves
  private var weaponAmmoRechargeTimer: Cancellable = Default.Cancellable

  private val weaponAmmoRecharge: Receive = {
    case VanuSentry.ChangeFireStart =>
      weaponAmmoRechargeTimer.cancel()
      weaponAmmoRechargeTimer = Default.Cancellable

    case VanuSentry.ChangeFireStop =>
      weaponAmmoRechargeTimer.cancel()
      weaponAmmoRechargeTimer = context.system.scheduler.scheduleWithFixedDelay(
        3 seconds,
        200 milliseconds,
        self,
        VanuSentry.RechargeAmmo
      )

    case VanuSentry.RechargeAmmo =>
      TurretObject.ControlledWeapon(wepNumber = 1).collect {
        case weapon: Tool =>
          // recharge when last shot fired 3s delay, +1, 200ms interval
          if (weapon.Magazine < weapon.MaxMagazine && System.currentTimeMillis() - weapon.LastDischarge > 3000L) {
            weapon.Magazine += 1
            val seat = TurretObject.Seat(0).get
            seat.occupant.collect {
              case player: Player =>
                TurretObject.Zone.LocalEvents ! LocalServiceMessage(
                  TurretObject.Zone.id,
                  player.GUID,
                  LocalAction.RechargeVehicleWeapon(TurretObject.GUID, weapon.GUID)
                )
            }
          }
          else if (weapon.Magazine == weapon.MaxMagazine && weaponAmmoRechargeTimer != Default.Cancellable) {
            weaponAmmoRechargeTimer.cancel()
            weaponAmmoRechargeTimer = Default.Cancellable
          }
      }
  }

  override def postStop(): Unit = {
    super.postStop()
    weaponAmmoRechargeTimer.cancel()
  }

  def receive: Receive =
    commonBehavior
      .orElse(mountBehavior)
      .orElse(weaponAmmoRecharge)
      .orElse {
        case _ => ()
      }

  override def parseAttribute(attribute: Int, value: Long, other: Option[Any]): Unit = { /*intentionally blank*/ }
}

object VanuSentry {
  final case object RechargeAmmo
  final case object ChangeFireStart
  final case object ChangeFireStop

  import akka.actor.ActorContext
  def Constructor(pos: Vector3, tdef: FacilityTurretDefinition)(id: Int, context: ActorContext): FacilityTurret = {
    import akka.actor.Props
    val obj = FacilityTurret(tdef)
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[VanuSentryControl], obj), s"${tdef.Name}_$id")
    obj
  }
}
