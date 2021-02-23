// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.mount

trait MountableSpace[A] {
  /**
    * A single mounted entity.
    * @return
    */
  def occupant: Option[A]
  /**
    * A collection of mounted entities.
    * @return
    */
  def occupants: List[A]
  /**
    * Is anything be seated?
    * Do not use this method as a test for "availability".
    */
  def isOccupied: Boolean
  /**
    * Can something be mounted?
    * Use this method as a test for "availability".
    */
  def canBeOccupied: Boolean
  /**
    * Is this specific entity currently mounted?
    */
  def isOccupiedBy(target: A): Boolean
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
  def mount(target: Option[A]): Option[A]
  /**
    * Tests whether the target is allowed to be mounted.
    * @see `MountableSpace[A].canBeOccupiedBy(A)`
    */
  protected def testToMount(target: A): Boolean = canBeOccupiedBy(target)
  /**
    * Attempt to dismount the target entity from this space.
    */
  def unmount(target: A): Option[A] = unmount(Some(target))
  /**
    * Attempt to dismount the target entity from this space.
    */
  def unmount(target: Option[A]): Option[A]
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
  def definition: MountableDefinition[A]
}

trait SingleMountableSpace[A]
  extends MountableSpace[A] {
  private var _occupant: Option[A] = None

  def occupant: Option[A] = _occupant

  def occupants: List[A] = _occupant.toList

  def isOccupied: Boolean = _occupant.nonEmpty

  def canBeOccupied: Boolean = !isOccupied

  def isOccupiedBy(player: A): Boolean = _occupant.contains(player)

  def mount(target: Option[A]): Option[A] = {
    target match {
      case Some(p) if testToMount(p) =>
        _occupant = target
        target
      case _ =>
        _occupant
    }
  }

  override protected def testToMount(target: A): Boolean = _occupant.isEmpty && super.testToMount(target)

  def unmount(target: Option[A]): Option[A] = {
    target match {
      case Some(p) if testToUnmount(p) =>
        _occupant = None
        None
      case None =>
        _occupant = None
        None
      case _ =>
        _occupant
    }
  }
}

trait ManyMountableSpace[A]
  extends MountableSpace[A] {
  private var _occupants: List[A] = List()

  def occupant: Option[A] = _occupants.headOption

  def occupants: List[A] = _occupants

  def isOccupied: Boolean = _occupants.nonEmpty

  def canBeOccupied: Boolean = _occupants.size < definition.occupancy

  def isOccupiedBy(target: A): Boolean = _occupants.contains(target)

  def mount(target: Option[A]): Option[A] = {
    target match {
      case Some(p) if testToMount(p) =>
        _occupants = _occupants :+ p
        target
      case _ =>
        None
    }
  }

  override protected def testToMount(target: A): Boolean = _occupants.isEmpty && super.canBeOccupiedBy(target)

  def unmount(target: Option[A]): Option[A] = {
    target match {
      case Some(p) if testToUnmount(p) =>
        _occupants = _occupants.filterNot(_ == p)
        target
      case _ =>
        None
    }
  }
}
