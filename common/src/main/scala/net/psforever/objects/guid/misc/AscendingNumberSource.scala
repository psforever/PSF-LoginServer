// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.misc

/**
  * This class is just a proof of concept model of a self-contained system.
  */
class AscendingNumberSource {
  val pool : Array[Int] = Array.ofDim[Int](65536)
  (0 to 65535).foreach(x => { pool(x) = x })
  var head : Int = 0

  def Get() : Int = {
    val start : Int = head
    if(pool(head) == -1) {
      do {
        head = (head + 1) % pool.length
      }
      while(pool(head) == -1 && head != start)
    }
    if(head == start) {
      import net.psforever.objects.entity.NoGUIDException
      throw NoGUIDException("no unused numbers available")
    }
    val outNumber : Int = head
    pool(head) = -1
    outNumber
  }

  def Return(number : Int) : Unit = {
    pool(number) = number
  }
}
