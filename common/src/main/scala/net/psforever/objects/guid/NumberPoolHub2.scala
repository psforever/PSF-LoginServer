// Copyright (c) 2017 PSForever
package net.psforever.objects.guid

import net.psforever.objects.entity.{IdentifiableEntity, NoGUIDException}
import net.psforever.objects.guid.key.LoanedKey
import net.psforever.objects.guid.pool.{ExclusivePool, GenericPool, NumberPool}
import net.psforever.objects.guid.source.NumberSource
import net.psforever.packet.game.PlanetSideGUID

import scala.util.{Failure, Success, Try}

class NumberPoolHub2(private val source : NumberSource) {
  import scala.collection.mutable
  private val hash : mutable.HashMap[String, NumberPool] = mutable.HashMap[String, NumberPool]()
  private val bigpool : mutable.LongMap[String] = mutable.LongMap[String]()
  hash += "generic" -> new GenericPool(bigpool, source.Size)
  source.FinalizeRestrictions.foreach(i => bigpool += i.toLong -> "") //these numbers can never be pooled; the source can no longer restrict numbers

  def apply(number : PlanetSideGUID) : Option[IdentifiableEntity] = this(number.guid)

  def apply(number : Int) : Option[IdentifiableEntity] = source.Get(number).orElse(return None).get.Object

  def Numbers : List[Int] = bigpool.keys.map(key => key.toInt).toList

  def AddPool(name : String, pool : List[Int]) : NumberPool = {
    if(hash.get(name).isDefined) {
      throw new IllegalArgumentException(s"can not add pool $name - name already known to this hub?")
    }
    if(source.Size <= pool.max) {
      throw new IllegalArgumentException(s"can not add pool $name - max(pool) is greater than source.size")
    }
    val collision = bigpool.keys.map(n => n.toInt).toSet.intersect(pool.toSet)
    if(collision.nonEmpty) {
      throw new IllegalArgumentException(s"can not add pool $name - it contains the following redundant numbers: ${collision.toString}")
    }
    pool.foreach(i => bigpool += i.toLong -> name)
    hash += name -> new ExclusivePool(pool)
    hash(name)
  }

  def RemovePool(name : String) : List[Int] = {
    if(name.equals("generic") || name.equals("")) {
      throw new IllegalArgumentException("can not remove pool - generic or restricted")
    }
    val pool = hash.get(name).orElse({
      throw new IllegalArgumentException(s"can not remove pool - $name does not exist")
    }).get
    if(pool.Count > 0) {
      throw new IllegalArgumentException(s"can not remove pool - $name is being used")
    }

    hash.remove(name)
    pool.Numbers.foreach(number => bigpool -= number)
    pool.Numbers
  }

  def GetPool(name : String) : Option[NumberPool] = if(name.equals("")) { None } else { hash.get(name) }

  def Pools : mutable.HashMap[String, NumberPool] = hash

  def WhichPool(number : Int) : Option[String] =  {
    val name = bigpool.get(number)
    if(name.contains("")) { None } else { name }
  }

  def WhichPool(obj : IdentifiableEntity) : Option[String] = {
    try {
      val number : Int = obj.GUID.guid
      val entry = source.Get(number)
      if(entry.isDefined && entry.get.Object.contains(obj)) { WhichPool(number) } else { None }
    }
    catch {
      case _ : Exception =>
        None
    }
  }

  def register(obj : IdentifiableEntity) : Try[Int] = register(obj, "generic")

  def register(obj : IdentifiableEntity, number : Int) : Try[Int] = {
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

  private def register_GetSpecificNumberFromPool(name : String, number : Int) : Try[LoanedKey]= {
    hash.get(name) match {
      case Some(pool) =>
        val slctr = pool.Selector
        import net.psforever.objects.guid.selector.SpecificSelector
        val specific = new SpecificSelector
        specific.SelectionIndex = number
        pool.Selector = specific
        pool.Get()
        pool.Selector = slctr
        register_GetAvailableNumberFromSource(number)
      case None =>
        Failure(new Exception(s"number pool $name not defined"))
    }
  }

  private def register_GetAvailableNumberFromSource(number : Int) : Try[LoanedKey] = {
    source.Available(number) match {
      case Some(key) =>
        Success(key)
      case None =>
        Failure(new Exception(s"number $number is unavailable"))
    }
  }

  def register(obj : IdentifiableEntity, name : String) : Try[Int] = {
    try {
      register_CheckNumberAgainstDesiredPool(obj, name, obj.GUID.guid)
    }
    catch {
      case _ : Exception =>
        register_GetPool(name) match {
          case Success(key) =>
            key.Object = obj
            Success(obj.GUID.guid)
          case Failure(ex) =>
            Failure(new Exception(s"trying to register an object but, ${ex.getMessage}"))
        }
    }
  }

  private def register_CheckNumberAgainstDesiredPool(obj : IdentifiableEntity, name : String, number : Int) : Try[Int] = {
    val directKey = source.Get(number)
    if(directKey.isEmpty || !directKey.get.Object.contains(obj)) {
      Failure(new Exception("object already registered, but not to this source"))
    }
    else if(!WhichPool(number).contains(name)) {
      //TODO obj is not registered to the desired pool; is this okay?
      Success(number)
    }
    else {
      Success(number)
    }
  }

  private def register_GetPool(name : String) : Try[LoanedKey] = {
    hash.get(name) match {
      case Some(pool) =>
        register_GetNumberFromDesiredPool(pool)
      case _ =>
        Failure(new Exception(s"number pool $name not defined"))
    }
  }

  private def register_GetNumberFromDesiredPool(pool : NumberPool) : Try[LoanedKey] = {
    pool.Get() match {
      case Success(number) =>
        register_GetMonitorFromSource(number)
      case Failure(ex) =>
        Failure(ex)
    }
  }

  private def register_GetMonitorFromSource(number : Int) : Try[LoanedKey] = {
    source.Available(number) match {
      case Some(key) =>
        Success(key)
      case _ =>
        throw NoGUIDException(s"a pool gave us a number $number that is actually unavailable") //stop the show; this is terrible!
    }
  }

  def register(number : Int) : Try[LoanedKey] = {
    WhichPool(number) match {
      case None =>
        import net.psforever.objects.guid.selector.SpecificSelector
        hash("generic").Selector.asInstanceOf[SpecificSelector].SelectionIndex = number
        register_GetPool("generic")
      case Some(name) =>
        register_GetSpecificNumberFromPool(name, number)
    }
  }

  def register(name : String) : Try[LoanedKey] = register_GetPool(name)

  def latterPartRegister(obj : IdentifiableEntity, number : Int) : Try[IdentifiableEntity] = {
    register_GetMonitorFromSource(number) match {
      case Success(monitor) =>
        monitor.Object = obj
        Success(obj)
      case Failure(ex) =>
        Failure(ex)
    }
  }

  def unregister(obj : IdentifiableEntity) : Try[Int] = {
    unregister_GetPoolFromObject(obj) match {
      case Success(pool) =>
        val number = obj.GUID.guid
        pool.Return(number)
        source.Return(number)
        obj.Invalidate()
        Success(number)
      case Failure(ex) =>
        Failure(new Exception(s"can not unregister this object: ${ex.getMessage}"))
    }
  }

  def unregister_GetPoolFromObject(obj : IdentifiableEntity) : Try[NumberPool] = {
    WhichPool(obj) match {
      case Some(name) =>
        unregister_GetPool(name)
      case None =>
        Failure(throw new Exception("can not find a pool for this object"))
    }
  }

  private def unregister_GetPool(name : String) : Try[NumberPool] = {
    hash.get(name) match {
      case Some(pool) =>
        Success(pool)
      case None =>
        Failure(new Exception(s"no pool by the name of '$name'"))
    }
  }

  def unregister(number : Int) : Try[Option[IdentifiableEntity]] = {
    if(source.Test(number)) {
      unregister_GetObjectFromSource(number)
    }
    else {
      Failure(new Exception(s"can not unregister a number $number that this source does not own") )
    }
  }

  private def unregister_GetObjectFromSource(number : Int) : Try[Option[IdentifiableEntity]] = {
    source.Return(number) match {
      case Some(obj) =>
        unregister_ReturnObjectToPool(obj)
      case None =>
        unregister_ReturnNumberToPool(number) //nothing is wrong, but we'll check the pool
    }
  }

  private def unregister_ReturnObjectToPool(obj : IdentifiableEntity) : Try[Option[IdentifiableEntity]] = {
    val number = obj.GUID.guid
    unregister_GetPoolFromNumber(number) match {
      case Success(pool) =>
        pool.Return(number)
        obj.Invalidate()
        Success(Some(obj))
      case Failure(ex) =>
        source.Available(number) //undo
        Failure(new Exception(s"started unregistering, but ${ex.getMessage}"))
    }
  }

  private def unregister_ReturnNumberToPool(number : Int) : Try[Option[IdentifiableEntity]] = {
    unregister_GetPoolFromNumber(number) match {
      case Success(pool) =>
        pool.Return(number)
        Success(None)
      case _ => //though everything else went fine, we must still fail if this number was restricted all along
        if(!bigpool.get(number).contains("")) {
          Success(None)
        }
        else {
          Failure(new Exception(s"can not unregister this number $number"))
        }
    }
  }

  private def unregister_GetPoolFromNumber(number : Int) : Try[NumberPool] = {
    WhichPool(number) match {
      case Some(name) =>
        unregister_GetPool(name)
      case None =>
        Failure(new Exception(s"no pool using number $number"))
    }
  }

  def latterPartUnregister(number : Int) : Option[IdentifiableEntity] = source.Return(number)

  def isRegistered(obj : IdentifiableEntity) : Boolean =  {
    try {
      source.Get(obj.GUID.guid) match {
        case Some(monitor) =>
          monitor.Object.contains(obj)
        case None =>
          false
      }
    }
    catch {
      case _ : NoGUIDException =>
        false
    }
  }

  def isRegistered(number : Int) : Boolean = {
    source.Get(number) match {
      case Some(monitor) =>
        monitor.Policy == AvailabilityPolicy.Leased
      case None =>
        false
    }
  }
}
