// Copyright (c) 2017-2019 PSForever
package net.psforever.types

import scodec.codecs.uint16L

abstract class PlanetSideGUID {
  def guid: Int

  /* overriding equals and hashCode to benefit the case class subclasses through inheritance;
   * essentially, if not for these overrides, each case class would implement its own equivalence methods
   * */
  /**
    * All subclasses of `PlanetSideGUID` are equivalent through being subclasses of `PlanetSideGUID`.
    * @param o an entity
    * @return whether that entity is a `PlanetSideGUID` object
    */
  def canEqual(o: Any): Boolean = o.isInstanceOf[PlanetSideGUID]

  override def equals(o: Any): Boolean =
    o match {
      case that: PlanetSideGUID => that.canEqual(this) && that.guid == this.guid
      case _                    => false
    }

  override def hashCode: Int = java.util.Objects.hashCode(guid)
}

final case class ValidPlanetSideGUID(guid: Int) extends PlanetSideGUID

final case class StalePlanetSideGUID(guid: Int) extends PlanetSideGUID

object PlanetSideGUID {
  def apply(guid: Int): PlanetSideGUID = ValidPlanetSideGUID(guid)

  def unapply(n: PlanetSideGUID): Option[Int] = Some(n.guid)

  implicit val codec = uint16L.xmap[PlanetSideGUID](
    n => PlanetSideGUID(n),
    n => n.guid
  )
}
