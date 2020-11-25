package net.psforever.actors.session

import java.util.concurrent.atomic.AtomicLong

/**
  * The authority for session identifier numbers for any given server runtime.
  */
object Session {
  /** the session id accumulator */
  private val masterSessionIdKey: AtomicLong = new AtomicLong(0L)

  /**
    * Poll the next session id.
    * @return a session id
    */
  def nextSessionId(): Long = masterSessionIdKey.get()

  /**
    * Get the next available session id.
    * Increment the session counter.
    * @return a session id
    */
  def newSessionId(): Long = {
    val oldId = masterSessionIdKey.getAndIncrement()
    oldId
  }
}
