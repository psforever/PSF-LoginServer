// Copyright (c) 2017 PSForever
package net.psforever

class ObjectFinalizedException(msg: String) extends Exception(msg)

trait IFinalizable {
  var closed = false

  def close = {
    closed = true
  }

  def assertNotClosed = {
    if (closed)
      throw new ObjectFinalizedException(
        this.getClass.getCanonicalName + ": already finalized. Cannot interact with object"
      )
  }

  override def finalize() = {
    if (!closed)
      println(this.getClass.getCanonicalName + ": class not closed. memory leaked")
  }
}
