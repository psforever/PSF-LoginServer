package net.psforever.objects.vital.etc

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.base.{DamageReason, DamageResolution}
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.DamageAndResistance

case class OicwLilBuddyReason(
                               entity: SourceEntry,
                               projectileId: Long,
                               damageModel: DamageAndResistance
                             ) extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Explosion

  def same(test: DamageReason): Boolean = test match {
    case eer: OicwLilBuddyReason => eer.projectileId == projectileId
    case _                       => false
  }

  def adversary: Option[SourceEntry] = Some(entity)

  def source: DamageProperties = GlobalDefinitions.oicw_little_buddy

  override def attribution: Int = GlobalDefinitions.oicw.ObjectId
}
