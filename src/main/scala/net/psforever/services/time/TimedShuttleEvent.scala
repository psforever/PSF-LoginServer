// Copyright (c) 2021 PSForever
package net.psforever.services.time

trait TimedShuttleEvent {
  def d: Long //for how long this event goes on
  def t: Long //starting time on the clock
  def lockedDoors : Boolean = true
}

object TimedShuttleEvent {
  final val fullTime: Long = 225000L //ms

  case object Boarding extends TimedShuttleEvent {
    def d: Long = 60000
    def t: Long = 60000
    override def lockedDoors: Boolean = false
  }
  case object Takeoff extends TimedShuttleEvent {
    def t: Long = fullTime //225000ms
    def d: Long = 8000
  }
  case object Event1 extends TimedShuttleEvent {
    def t: Long = 217000
    def d: Long = 13300
  } //217000ms
  case object Event2 extends TimedShuttleEvent {
    def t: Long = 203700
    def d: Long = 180000
  }
  case object Event3 extends TimedShuttleEvent {
    def t: Long = 23700
    def d: Long = 15700
  }
  case object Event4 extends TimedShuttleEvent {
    def t: Long = 8000
    def d: Long = 8000
  }
  case object Blanking extends TimedShuttleEvent {
    def t: Long = Int.MaxValue.toLong
    def d: Long = 1 //for how long?
  }

  final val eventTimeDeltas: Seq[TimedShuttleEvent] = Seq(
    Boarding,
    Takeoff,
    Event1,
    Event2,
    Event3,
    Event4,
    Blanking
  )
}
