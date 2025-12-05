package net.psforever.objects.serverobject.environment.interaction

import net.psforever.objects.serverobject.environment.{EnvironmentTrait, PieceOfEnvironment}
import net.psforever.objects.zones.interaction.InteractsWithZone

trait InteractionWith {
  def attribute: EnvironmentTrait

  //noinspection ScalaUnusedSymbol
  def doInteractingWith(
                         obj: InteractsWithZone,
                         body: PieceOfEnvironment,
                         parentInfo: Option[Any]
                       ): Unit

  //noinspection ScalaUnusedSymbol
  def stopInteractingWith(
                           obj: InteractsWithZone,
                           body: PieceOfEnvironment,
                           parentInfo: Option[Any]
                         ): Unit = { /*mainly for overriding*/ }

  //noinspection ScalaUnusedSymbol
  def recoverFromInteracting(obj: InteractsWithZone): Unit = { /*mainly for overriding*/ }
}
