// Copyright (c) 2017 PSForever
package net.psforever.objects.guid

import net.psforever.objects.entity.{IdentifiableEntity, NoGUIDException}
import net.psforever.objects.guid.key.LoanedKey
import net.psforever.objects.guid.pool.{ExclusivePool, GenericPool, NumberPool}
import net.psforever.objects.guid.source.NumberSource
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success, Try}

/**
  * A master object that manages `NumberPool`s when they are applied to a single `NumberSource`.
  * It catalogs the numbers and ensures the pool contents are unique to each other.<br>
  * <br>
  * All globally unique numbers are sorted into user-defined groups called pools.
  * Pools are intended to pre-allocate certain numbers to certain tasks.
  * Two default pools also exist - "generic," for all numbers not formally placed into a pool, and a hidden restricted pool.
  * The former can accept a variety of numbers on the source not known at initialization time loaded into it.
  * The latter can only be set by the `NumberSource` and can not be affected once this object is created.
  * @param source the number source object
  */
class NumberPoolHub(private val source: NumberSource) {
  import scala.collection.mutable
  private val hash: mutable.HashMap[String, NumberPool] = mutable.HashMap[String, NumberPool]()
  private val bigpool: mutable.LongMap[String]          = mutable.LongMap[String]()
  hash += "generic" -> new GenericPool(bigpool, source.size)
  source.finalizeRestrictions.foreach(i =>
    bigpool += i.toLong -> ""
  ) //these numbers can never be pooled; the source can no longer restrict numbers

  /**
    * Given a globally unique identifier, return any object registered to it.<br>
    * <br>
    * Use:<br>
    * For `val obj = new NumberPoolHub(...)` use `obj(number)`.
    * @param number the unique number to attempt to retrieve from the `source`
    * @return the object that is assigned to the number
    */
  def apply(number: PlanetSideGUID): Option[IdentifiableEntity] = this(number.guid)

  /**
    * Given a globally unique identifier, return any object registered to it.<br>
    * <br>
    * Use:<br>
    * For `val obj = new NumberPoolHub(...)` use `obj(number)`.
    * @param number the unique number to attempt to retrieve from the `source`
    * @return the object that is assigned to the number
    */
  def apply(number: Int): Option[IdentifiableEntity] = source.get(number).orElse(return None).get.Object

  def Numbers: List[Int] = bigpool.keys.map(key => key.toInt).toList

  /**
    * Create a new number pool with the given label and the given numbers.<br>
    * <br>
    * Creating number pools is a task that should only be performed at whatever counts as the initialization stage.
    * Nothing technically blocks it being done during runtime;
    * however, stability is best served by doing it only once and while nothing else risk affecting the numbers.
    * Unlike "live" functionality which often returns as `Success` or `Failure`, this is considered a critical operation.
    * As thus, `Exceptions` are permitted since a fault of the pool's creation will disrupt normal operations.
    * @param name the name of the pool
    * @param pool the `List` of numbers that will belong to the pool
    * @return the newly-created number pool
    * @throws IllegalArgumentException if the pool's name is already defined;
    *                                  if the pool is (already) empty;
    *                                  if the pool contains numbers the source does not;
    *                                  if the pool contains numbers from already existing pools
    */
  def AddPool(name: String, pool: List[Int]): NumberPool = {
    if (hash.get(name).isDefined) {
      throw new IllegalArgumentException(s"can not add pool $name - name already known to this hub?")
    }
    if (pool.isEmpty) {
      throw new IllegalArgumentException(s"can not add empty pool $name")
    }
    if (source.max < pool.max) {
      throw new IllegalArgumentException(s"can not add pool $name - pool.max is greater than source.max")
    }
    bigpool.keys.map(n => n.toInt).toSet.intersect(pool.toSet).toSeq match {
      case Nil => ;
      case collisions =>
        throw new IllegalArgumentException(
          s"can not add pool $name - it contains the following redundant numbers: ${collisions.mkString(",")}"
        )
    }
    pool.foreach(i => bigpool += i.toLong -> name)
    hash += name -> new ExclusivePool(pool)
    hash(name)
  }

  /**
    * Remove an existing number pool with the given label from the list of number pools.<br>
    * <br>
    * Removing number pools is a task that should only be performed at whatever counts as the termination stage.
    * All the same reasoning applies as with `AddPool` above.
    * Although an easy operation would move all the assigned numbers in the removing pool to the "generic" pool,
    * doing so is ill-advised both for the reasoning above and because that creates unreliability.
    * @param name the name of the pool
    * @return the `List` of numbers that belonged to the pool
    * @throws IllegalArgumentException if the pool doesn't exist or is not removed (removable)
    */
  def RemovePool(name: String): List[Int] = {
    if (name.equals("generic") || name.equals("")) {
      throw new IllegalArgumentException("can not remove pool - generic or restricted")
    }
    val pool = hash
      .get(name)
      .orElse({
        throw new IllegalArgumentException(s"can not remove pool - $name does not exist")
      })
      .get
    if (pool.Count > 0) {
      throw new IllegalArgumentException(s"can not remove pool - $name is being used")
    }

    hash.remove(name)
    pool.Numbers.foreach(number => bigpool -= number)
    pool.Numbers
  }

  /**
    * Get the number pool known by this name.
    * It will not return correctly for any number that is in the "restricted" pool.
    * @param name the name of the pool
    * @return a reference to the number pool, or `None`
    */
  def GetPool(name: String): Option[NumberPool] =
    if (name.equals("")) { None }
    else { hash.get(name) }

  /**
    * na
    * @return na
    */
  def Pools: mutable.HashMap[String, NumberPool] = hash

  /**
    * Reference a specific number's pool.<br>
    * <br>
    * `WhichPool(Int)` does not require the number to be registered at the time it is used.
    * It does not return anything for an unregistered unpooled number -
    * a number that would be part of the "generic" nonstandard pool.
    * It only reports "generic" if that number is registered.
    * It will not return correctly for any number that is in the "restricted" pool.
    * @param number a number
    * @return the name of the number pool to which this item belongs
    */
  def WhichPool(number: Int): Option[String] = {
    val name = bigpool.get(number)
    if (name.contains("")) { None }
    else { name }
  }

  /**
    * Reference a specific number's pool.<br>
    * <br>
    * `WhichPool(IdentifiableEntity)` does require the object to be registered to be found.
    * It checks that the object is registered, and that it is registered to the local source object.
    * @param obj an object
    * @return the name of the number pool to which this item belongs
    */
  def WhichPool(obj: IdentifiableEntity): Option[String] = {
    try {
      val number: Int = obj.GUID.guid
      val entry       = source.get(number)
      if (entry.isDefined && entry.get.Object.contains(obj)) { WhichPool(number) }
      else { None }
    } catch {
      case _: Exception =>
        None
    }
  }

  /**
    * Register an object to any available selection (of the "generic" number pool).
    * @param obj an object being registered
    * @return the number the was given to the object
    */
  def register(obj: IdentifiableEntity): Try[Int] = register(obj, "generic")

  /**
    * Register an object to a specific number if it is available.
    * @param obj an object being registered
    * @param number the number whose assignment is requested
    * @return the number the was given to the object
    */
  def register(obj: IdentifiableEntity, number: Int): Try[Int] = {
    bigpool.get(number.toLong) match {
      case Some(name) =>
        register_GetSpecificNumberFromPool(name, number) match {
          case Success(key) =>
            key.Object = obj
            Success(obj.GUID.guid)
          case Failure(ex) =>
            Failure(new Exception(s"trying to register an object to a specific number but, ${ex.getMessage}"))
        }
      case None =>
        import net.psforever.objects.guid.selector.SpecificSelector
        hash("generic").Selector.asInstanceOf[SpecificSelector].SelectionIndex = number
        register(obj, "generic")
    }
  }

  /**
    * Asides from using the `name` parameter to find the number pool,
    * this method also removes the `number` from that number pool of its own accord.
    * The "{pool}.Selector = new SpecificSelector" technique is used to safely remove the number.
    * It will disrupt the internal order of the number pool set by its current selector and reset it to a neutral state.
    * @param name the local pool name
    * @param number the number whose assignment is requested
    * @return the number the was given to the object
    * @see `NumberPool.Selector_=(NumberSelector)`
    */
  private def register_GetSpecificNumberFromPool(name: String, number: Int): Try[LoanedKey] = {
    hash.get(name) match {
      case Some(pool) =>
        val slctr = pool.Selector
        import net.psforever.objects.guid.selector.SpecificSelector
        val specific = new SpecificSelector
        pool.Selector = specific
        specific.SelectionIndex = pool.Numbers.indexOf(number)
        pool.Get()
        pool.Selector = slctr
        register_GetAvailableNumberFromSource(number)
      case None =>
        Failure(new Exception(s"number pool $name not defined"))
    }
  }

  private def register_GetAvailableNumberFromSource(number: Int): Try[LoanedKey] = {
    source.getAvailable(number) match {
      case Some(key) =>
        Success(key)
      case None =>
        Failure(new Exception(s"number $number is unavailable"))
    }
  }

  /**
    * Register an object to a specific number pool.
    * @param obj an object being registered
    * @param name the local pool name
    * @return the number the was given to the object
    */
  def register(obj: IdentifiableEntity, name: String): Try[Int] = {
    if (obj.HasGUID) {
      register_CheckNumberAgainstDesiredPool(obj, name, obj.GUID.guid)
    } else {
      register_GetPool(name) match {
        case Success(key) =>
          key.Object = obj
          Success(obj.GUID.guid)
        case Failure(ex) =>
          Failure(new Exception(s"trying to register an object but, ${ex.getMessage}"))
      }
    }
  }

  private def register_CheckNumberAgainstDesiredPool(obj: IdentifiableEntity, name: String, number: Int): Try[Int] = {
    val directKey = source.get(number)
    if (directKey.isEmpty || !directKey.get.Object.contains(obj)) {
      Failure(new Exception("object already registered, but not to this source"))
    } else if (!WhichPool(number).contains(name)) {
      //TODO obj is not registered to the desired pool; is this okay?
      Success(number)
    } else {
      Success(number)
    }
  }

  private def register_GetPool(name: String): Try[LoanedKey] = {
    hash.get(name) match {
      case Some(pool) =>
        register_GetNumberFromDesiredPool(pool)
      case _ =>
        Failure(new Exception(s"number pool $name not defined"))
    }
  }

  private def register_GetNumberFromDesiredPool(pool: NumberPool): Try[LoanedKey] = {
    pool.Get() match {
      case Success(number) =>
        register_GetMonitorFromSource(number)
      case Failure(ex) =>
        Failure(ex)
    }
  }

  private def register_GetMonitorFromSource(number: Int): Try[LoanedKey] = {
    register_GetAvailableNumberFromSource(number) match {
      case Success(key) =>
        Success(key)
      case Failure(_) =>
        throw new NoGUIDException(
          s"a pool gave us a number $number that is actually unavailable"
        ) //stop the show; this is terrible!
    }
  }

  /**
    * Register a specific number.
    * @param number the number whose assignment is requested
    * @return the monitor for a number
    */
  def register(number: Int): Try[LoanedKey] = {
    WhichPool(number) match {
      case None =>
        import net.psforever.objects.guid.selector.SpecificSelector
        hash("generic").Selector.asInstanceOf[SpecificSelector].SelectionIndex = number
        register_GetPool("generic")
      case Some(name) =>
        register_GetSpecificNumberFromPool(name, number)
    }
  }

  /**
    * Register a number selected automatically from the named pool.
    * @param name the local pool name
    * @return the monitor for a number
    */
  def register(name: String): Try[LoanedKey] = register_GetPool(name)

  /**
    * na
    * @param obj an object being registered
    * @param number the number whose assignment is requested
    * @return an object that has been registered
    */
  def latterPartRegister(obj: IdentifiableEntity, number: Int): Try[IdentifiableEntity] = {
    register_GetMonitorFromSource(number) match {
      case Success(monitor) =>
        monitor.Object = obj
        Success(obj)
      case Failure(ex) =>
        Failure(ex)
    }
  }

  /**
    * Unregister a specific object.
    * @param obj an object being unregistered
    * @return the number previously associated with the object
    */
  def unregister(obj: IdentifiableEntity): Try[Int] = {
    unregister_GetPoolFromObject(obj) match {
      case Success(pool) =>
        val number = obj.GUID.guid
        pool.Return(number)
        source.returnNumber(number)
        obj.Invalidate()
        Success(number)
      case Failure(ex) =>
        unregister_GetMonitorFromObject(obj, ex.getMessage)
    }
  }

  /**
    * Unregister a specific object
    * by actually finding the object itself, if it exists.
    * @param obj an object being unregistered
    * @param msg custom error message;
    *            has a vague default
    * @return the number associated with this object
    */
  def unregister_GetMonitorFromObject(
                                       obj: IdentifiableEntity,
                                       msg: String = "can not find this object"
                                     ): Try[Int] = {
    source.get(obj) match {
      case Some(key) =>
        val number = key.GUID
        GetPool(WhichPool(number).get).get.Return(number)
        source.returnNumber(number)
        Success(number)
      case _ =>
        Failure(new Exception(s"can not unregister this $obj - $msg"))
    }
  }

  def unregister_GetPoolFromObject(obj: IdentifiableEntity): Try[NumberPool] = {
    WhichPool(obj) match {
      case Some(name) =>
        unregister_GetPool(name)
      case None =>
        Failure(new Exception(s"can not find a pool for this $obj"))
    }
  }

  private def unregister_GetPool(name: String): Try[NumberPool] = {
    hash.get(name) match {
      case Some(pool) =>
        Success(pool)
      case None =>
        Failure(new Exception(s"no pool by the name of '$name'"))
    }
  }

  /**
    * Unregister a specific number.
    * @param number the number previously assigned(?)
    * @return the object, if any, previous associated with the number
    */
  def unregister(number: Int): Try[Option[IdentifiableEntity]] = {
    if (source.test(number)) {
      unregister_GetObjectFromSource(number)
    } else {
      Failure(new Exception(s"can not unregister a number $number that this source does not own"))
    }
  }

  private def unregister_GetObjectFromSource(number: Int): Try[Option[IdentifiableEntity]] = {
    source.returnNumber(number) match {
      case Some(obj) =>
        unregister_ReturnObjectToPool(obj)
      case None =>
        unregister_ReturnNumberToPool(number) //nothing is wrong, but we'll check the pool
    }
  }

  private def unregister_ReturnObjectToPool(obj: IdentifiableEntity): Try[Option[IdentifiableEntity]] = {
    val number = obj.GUID.guid
    unregister_GetPoolFromNumber(number) match {
      case Success(pool) =>
        pool.Return(number)
        obj.Invalidate()
        Success(Some(obj))
      case Failure(ex) =>
        source.getAvailable(number) //undo
        Failure(new Exception(s"started unregistering, but ${ex.getMessage}"))
    }
  }

  private def unregister_ReturnNumberToPool(number: Int): Try[Option[IdentifiableEntity]] = {
    unregister_GetPoolFromNumber(number) match {
      case Success(pool) =>
        pool.Return(number)
        Success(None)
      case _ => //though everything else went fine, we must still fail if this number was restricted all along
        if (!bigpool.get(number).contains("")) {
          Success(None)
        } else {
          Failure(new Exception(s"can not unregister this number $number"))
        }
    }
  }

  private def unregister_GetPoolFromNumber(number: Int): Try[NumberPool] = {
    WhichPool(number) match {
      case Some(name) =>
        unregister_GetPool(name)
      case None =>
        Failure(new Exception(s"no pool using number $number"))
    }
  }

  /**
    * For accessing the `returnNumber` function of the contained `NumberSource` directly.
    * @param number the number to return.
    * @return any object previously using this number
    */
  def latterPartUnregister(number: Int): Option[IdentifiableEntity] = source.returnNumber(number)

  /**
    * Determines if the object is registered.<br>
    * <br>
    * Three conditions are necessary to determine this condition for objects.
    * (1) A registered object has a globally unique identifier.
    * (2) A registered object is known to the `source` by that identifier.
    * (3) The registered object can be found attached to that entry from the source.
    * @param obj an object
    * @return `true`, if the number is registered; `false`, otherwise
    * @see `isRegistered(Int)`
    */
  def isRegistered(obj: IdentifiableEntity): Boolean = {
    try {
      source.get(obj.GUID.guid) match {
        case Some(monitor) =>
          monitor.Object.contains(obj)
        case None =>
          false
      }
    } catch {
      case _: NoGUIDException =>
        false
    }
  }

  /**
    * Determines if the number is registered.<br>
    * <br>
    * Two conditions are necessary to determine this condition for numbers.
    * (1) A registered number is known to the `source`.
    * (2) A register number is known as `Leased` to the `source`.
    * @param number the number previously assigned(?)
    * @return `true`, if the number is registered; `false`, otherwise
    * @see `isRegistered(IdentifiableEntity)`
    */
  def isRegistered(number: Int): Boolean = {
    source.get(number) match {
      case Some(monitor) =>
        monitor.Policy == AvailabilityPolicy.Leased
      case None =>
        false
    }
  }
}
