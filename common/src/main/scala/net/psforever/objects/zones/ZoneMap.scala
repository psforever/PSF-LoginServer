// Copyright (c) 2017 PSForever
package net.psforever.objects.zones

/**
  * The fixed instantiation and relation of a series of server objects.<br>
  * <br>
  * Asides from a `List` of server objects to be built, the operation between any server objects
  * and the connected functionality emerging from more complex data structures is codified by this object.
  * In the former case, all `Terminal` server objects for a `Zone` are to be defined herein.
  * In the latter case, the arrangement of server objects into groups called facilities is also to be defined herein.
  * Much like a `BasicDefinition` to an object, `ZoneMap` should not maintain mutable information for the companion `Zone`.
  * Use it as a blueprint.<br>
  * <br>
  * The "training zones" are the best example of the difference between a `ZoneMap` and a `Zone.`
  * `tzdrtr` is the Terran Republic driving course.
  * `tzdrvs` is the Vanu Sovereignty driving course.
  * While each course can have different objects and object states (`Zone`),
  * both courses have the same basic server objects because they are built from the same blueprint (`ZoneMap`).
  * @param name the privileged name that can be used as the first parameter in the packet `LoadMapMessage`
  * @see `ServerObjectBuilder`<br>
  *      `LoadMapMessage`
  */
class ZoneMap(private val name : String) {
  private var localObjects : List[ServerObjectBuilder[_]] = List()

  def Name : String = name

  /**
    * Append the builder for a server object to the list of builders known to this `ZoneMap`.
    * @param obj the builder for a server object
    */
  def LocalObject(obj : ServerObjectBuilder[_]) : Unit = {
    localObjects = localObjects :+ obj
  }

  /**
    * The list of all server object builder wrappers that have been assigned to this `ZoneMap`.
    * @return the `List` of all `ServerObjectBuilders` known to this `ZoneMap`
    */
  def LocalObjects : List[ServerObjectBuilder[_]] =  {
    localObjects
  }
}
