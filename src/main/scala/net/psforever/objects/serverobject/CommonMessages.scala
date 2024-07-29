// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject

import net.psforever.objects.{PlanetSideGameObject, Player}
import net.psforever.objects.serverobject.hackable.Hackable

//temporary location for these messages
object CommonMessages {
  final case class Use(player: Player, data: Option[Any] = None)
  final case class Unuse(player: Player, data: Option[Any] = None)
  final case class Hack(player: Player, obj: PlanetSideServerObject with Hackable, data: Option[Any] = None)
  final case class ClearHack()
  final case class EntityHackState(obj: PlanetSideGameObject with Hackable, hackState: Boolean)
  
  /**
   * The message that progresses some form of user-driven activity with a certain eventual outcome
   * and potential feedback per cycle.
   * @param delta how much the progress value changes each tick, which will be treated as a percentage;
   *              must be a positive value
   * @param completionAction a finalizing action performed once the progress reaches 100(%)
   * @param tickAction an action that is performed for each increase of progress
   * @param tickTime how long between each `tickAction` (ms);
   *                 defaults to 250 milliseconds
   */
  final case class ProgressEvent(
                                  delta: Float,
                                  completionAction: () => Unit,
                                  tickAction: Float => Boolean,
                                  tickTime: Long = 250L
                                )

  /**
    * The message that initializes a process -
    * some form of user-driven activity with a certain eventual outcome and potential feedback per cycle.
    * The feedback is usually only known to the individual attempting the process.
    * @param delta how much the progress value changes each tick, which will be treated as a percentage;
    *              must be a positive value
    * @param completionAction a finalizing action performed once the progress reaches 100(%)
    * @param tickAction an action that is performed for each increase of progress
    */
  final case class Progress(delta: Float, completionAction: () => Unit, tickAction: Float => Boolean) {
    assert(delta > 0, s"progress activity change value must be positive number - $delta")
  }

  /**
   * A request has been made to charge this entity's shields.
   * @see `FacilityBenefitShieldChargeRequestMessage`
   * @param amount the number of points to charge
   * @param motivator the element that caused the shield to charge;
   *                  allowed to be `None`;
   *                  most often, a `Building`;
   *                  if the vehicle instigated its own charge (battleframe robotics), specify that
   */
  final case class ChargeShields(amount: Int, motivator: Option[PlanetSideGameObject])
}
