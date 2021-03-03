// Copyright (c) 2021 PSForever
package net.psforever.services.time

object ShuttleState extends Enumeration {
  val State13 = Value(13)
  val State14 = Value(14)
  val State10 = Value(10)
  val State11 = Value(11)
  val State12 = Value(12)
  val State15 = Value(15)
}

trait TimedShuttleEvent {
  def u1: Int //?
  def u2: Int //??
  def duration: Long //for how long this event goes on
  def t: Long //starting time on the clock
  def lockedDoors: Boolean = true
  def shuttleState: Option[Int]
  def docked: Option[Boolean]
}

object TimedShuttleEvent {
  final val fullTime: Long = 225000L //ms

  case object Boarding extends TimedShuttleEvent {
    def u1: Int = 0
    def u2: Int = 0
    def duration: Long = 60000
    def t: Long = 60000
    override def lockedDoors: Boolean = false
    def shuttleState: Option[Int] = Some(10)
    def docked: Option[Boolean] = Some(true)
  }
  case object RaiseShuttlePlatform extends TimedShuttleEvent {
    def u1: Int = 1
    def u2: Int = 1
    def t: Long = fullTime //225000ms
    def duration: Long = 8000
    def shuttleState: Option[Int] = Some(11)
    def docked: Option[Boolean] = Some(true)
  }
  case object Takeoff extends TimedShuttleEvent {
    def u1: Int = 2
    def u2: Int = 2
    def t: Long = 217000
    def duration: Long = 13300
    def shuttleState: Option[Int] = Some(12)
    def docked: Option[Boolean] = Some(false)
  }
  case object Event2 extends TimedShuttleEvent {
    def u1: Int = 7
    def u2: Int = 3
    def t: Long = 203700
    def duration: Long = 180000
    def shuttleState: Option[Int] = Some(15)
    def docked: Option[Boolean] = None
  }
  case object Event3 extends TimedShuttleEvent {
    def u1: Int = 3
    def u2: Int = 4
    def t: Long = 23700
    def duration: Long = 15700
    def shuttleState: Option[Int] = Some(13)
    def docked: Option[Boolean] = None
  }
  case object Docking extends TimedShuttleEvent {
    def u1: Int = 4
    def u2: Int = 5
    def t: Long = 8000
    def duration: Long = 8000
    def shuttleState: Option[Int] = Some(14)
    def docked: Option[Boolean] = Some(true)
  }
  case object Blanking extends TimedShuttleEvent {
    def u1: Int = 0
    def u2: Int = 5
    def t: Long = 0
    def duration: Long = 1 //for how long?
    def shuttleState: Option[Int] = None
    def docked: Option[Boolean] = Some(true)
  }
}
