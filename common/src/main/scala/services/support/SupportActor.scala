// Copyright (c) 2017 PSForever
package services.support

import akka.actor.Actor
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.zones.Zone

import scala.annotation.tailrec

abstract class SupportActor[A <: SupportActor.Entry] extends Actor {
  private[this] val log = org.log4s.getLogger
  def info(msg : String) : Unit = log.info(msg)
  def trace(msg : String) : Unit = log.trace(msg)
  def debug(msg : String) : Unit = log.debug(msg)

  def sameEntryComparator : SimilarityComparator[A]

  /**
    * Determine whether or not the resulting entry is valid for this process.
    * The primary purpose of this function should be to determine if the appropriate type of object is being submitted.
    * Override.
    * @param entry the entry
    * @return `true`, if it can be processed; `false`, otherwise
    */
  def InclusionTest(entry : A) : Boolean

  def entryManagementBehaviors : Receive = {
    case SupportActor.HurrySpecific(targets, zone) =>
      HurrySpecific(targets, zone)

    case SupportActor.HurryAll() =>
      HurryAll()

    case SupportActor.ClearSpecific(targets, zone) =>
      ClearSpecific(targets, zone)

    case SupportActor.ClearAll() =>
      ClearAll()
  }

  def HurrySpecific(targets : List[PlanetSideGameObject], zone : Zone) : Unit

  def HurryAll()

  def ClearSpecific(targets : List[PlanetSideGameObject], zone : Zone) : Unit

  def ClearAll() : Unit

  /*
    * Search the first pool of entries awaiting removal processing.
    * If any entry has the same object as one of the targets and belongs to the same zone, remove it from the first pool.
    * If no targets are selected (an empty list), all discovered targets within the appropriate zone are removed.
    * @param targets a list of objects to pick
    * @param zone the zone in which these objects must be discovered;
    *             all targets must be in this zone, with the assumption that this is the zone where they were registered
    * @return all of the discovered entries
    */
  def PartitionTargetsFromList(list : List[A], targets : List[A], zone : Zone, comparator : SimilarityComparator[A] = sameEntryComparator) : (List[A], List[A]) = {
    if(targets.nonEmpty) {
      if(targets.size == 1) {
        debug(s"a target submitted: ${targets.head}")
        //simple selection
        SupportActor.recursiveFind(comparator)(list.iterator, targets.head) match {
          case None => ;
            (Nil, list)
          case Some(index) =>
            (List(list(index)), list.take(index) ++ list.drop(index + 1))
        }
      }
      else {
        debug(s"multiple targets submitted: $targets")
        //cumbersome partition
        //a - find targets from entries
        val locatedTargets = for {
          a <- targets
          b <- list
          if b.obj.HasGUID && a.obj.HasGUID && comparator.Test(b, a)
        } yield b
        if(locatedTargets.nonEmpty) {
          //b - entries, after the found targets are removed (cull any non-GUID entries while at it)
          (locatedTargets, list filterNot locatedTargets.toSet)
        }
        else {
          (Nil, list)
        }
      }
    }
    else {
      list.partition(entry => entry.zone == zone)
    }
  }
}

object SupportActor {
  class Entry(val obj : PlanetSideGameObject, val zone : Zone, val duration : Long) {
    val time : Long = System.nanoTime
  }

  /**
    * "Hurrying" shifts entries with the discovered objects (in the same `zone`)
    * through their first task and into the second pool.
    * If the list of targets is empty, all discovered objects in the given zone will be considered targets.
    * @param targets a list of objects to match
    * @param zone the zone in which these objects exist;
    *             the assumption is that all these target objects are registered to this zone
    */
  case class HurrySpecific(targets : List[PlanetSideGameObject], zone : Zone)
  /**
    * "Hurrying" shifts all entries through their first task and into the second pool.
    */
  case class HurryAll()

  /**
    * "Clearing" cancels entries with the discovered objects (in the same `zone`)
    * if they are discovered in the first pool of objects.
    * Those entries will no longer be affected by any actions performed by the removal process until re-submitted.
    * If the list of targets is empty, all discovered objects in the given zone will be considered targets.
    * @param targets a list of objects to match
    * @param zone the zone in which these objects exist;
    *             the assumption is that all these target objects are registered to this zone
    */
  case class ClearSpecific(targets : List[PlanetSideGameObject], zone : Zone)
  /**
    * "Clearing" cancels all entries if they are discovered in the first pool of objects.
    * Those entries will no longer be affected by any actions performed by the removal process until re-submitted.
    */
  case class ClearAll()

  /**
    * Get the index of an entry in the list of entries.
    * @param iter an `Iterator` of entries
    * @param target the specific entry to be found
    * @param index the incrementing index value
    * @return the index of the entry in the list, if a match to the target is found
    */
  @tailrec private def recursiveFind[A <: SupportActor.Entry](comparator : SimilarityComparator[A])(iter : Iterator[A], target : A, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val entry = iter.next
      if(entry.obj.HasGUID && target.obj.HasGUID && comparator.Test(entry, target)) {
        Some(index)
      }
      else {
        recursiveFind(comparator)(iter, target, index + 1)
      }
    }
  }
}
