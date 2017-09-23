// Copyright (c) 2017 PSForever
package net.psforever.objects.continent

class ZoneMap(private val name : String) {
  private var localObjects : List[ServerObjectBuilder] = List()

  def Name : String = name

  def LocalObject(obj : ServerObjectBuilder) : Unit = {
    localObjects = localObjects :+ obj
  }

  def LocalObjects : List[ServerObjectBuilder] =  {
    val utilities = localObjects
    localObjects = Nil
    utilities
  }
}
