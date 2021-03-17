// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

trait MountableSpace[A] {
  private var _occupant: Option[A] = None

  /**
    * A single mounted entity.
    * @return one mounted entity at most, or `None`
    */
  def occupant: Option[A] = _occupant

  /**
    * A collection of any mounted entity.
    * Useful for compiling all seated users using `flatMap`.
    * @return all mounted entities
    */
  def occupants: List[A] = _occupant.toList

  /**
    * Is anything be seated?
    * Do not use this method as a test for "availability".
    */
  def isOccupied: Boolean = _occupant.nonEmpty

  /**
    * Can something be mounted?
    * Use this method as a test for "availability".
    */
  def canBeOccupied: Boolean = _occupant.isEmpty

  /**
    * Is this specific entity currently mounted?
    */
  def isOccupiedBy(target: A): Boolean = _occupant.contains(target)

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
        _occupant = target
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
        _occupant = None
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
