// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

trait MountableSpace[A] {
  private var _occupants: List[A] = List()

  /**
    * A single mounted entity.
    * Only return the occupant of this mount if the mount is limited to single user.
    * @see `occupants`
    * @return one mounted entity at most, or `None`
    */
  def occupant: Option[A] = {
    if (definition.occupancy == 1) {
      _occupants.headOption
    } else {
      None
    }
  }

  /**
    * A collection of mounted entities.
    * @return all mounted entities
    */
  def occupants: List[A] = _occupants

  /**
    * Is anything be seated?
    * Do not use this method as a test for "availability".
    */
  def isOccupied: Boolean = _occupants.nonEmpty

  /**
    * Can something be mounted?
    * Use this method as a test for "availability".
    */
  def canBeOccupied: Boolean = _occupants.size < definition.occupancy

  /**
    * Is this specific entity currently mounted?
    */
  def isOccupiedBy(target: A): Boolean = _occupants.contains(target)

  /**
    * Is this specific entity allowed to be mounted in this space?
    * Utiltizes restriction tests, but not "availability" tests.
    * @see `MountableDefinition[A].restriction`
    */
  def canBeOccupiedBy(target: A): Boolean = definition.restriction.test(target)

  /**
    * Attempt to mount the target entity in this space.
    */
  def mount(target: A): Option[A] = mount(Some(target))

  /**
    * Attempt to mount the target entity in this space.
    */
  def mount(target: Option[A]): Option[A] = {
    target match {
      case Some(p) if testToMount(p) =>
        _occupants = _occupants :+ p
        target
      case _ =>
        occupant
    }
  }

  /**
    * Tests whether the target is allowed to be mounted.
    * @see `MountableSpace[A].canBeOccupiedBy(A)`
    */
  protected def testToMount(target: A): Boolean = canBeOccupied && canBeOccupiedBy(target)

  /**
    * Attempt to dismount the target entity from this space.
    */
  def unmount(target: A): Option[A] = unmount(Some(target))

  /**
    * Attempt to dismount the target entity from this space.
    */
  def unmount(target: Option[A]): Option[A] = {
    target match {
      case Some(p) if testToUnmount(p) =>
        _occupants = _occupants.filterNot(_ == p)
        None
      case _ =>
        occupant
    }
  }

  /**
    * Tests whether the target is capable of being unmounted from this place.
    * @see `MountableSpace[A].isOccupiedBy(A)`
    */
  protected def testToUnmount(target: A): Boolean = isOccupiedBy(target)

  /**
    * Does this mountable space count as being "bailable",
    * a condition whereupon it can be unmounted under duress?
    * The conditions of the duress do not matter at the moment;
    * this is only a test of possibility.
    */
  def bailable: Boolean = definition.bailable

  /**
    * The information that establishes the underlying characteristics of this mountable space.
    */
  def definition: MountableSpaceDefinition[A]
}
