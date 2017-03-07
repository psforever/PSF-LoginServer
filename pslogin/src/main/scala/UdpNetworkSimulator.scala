// Copyright (c) 2017 PSForever
import akka.actor.{Actor, ActorRef}
import akka.io._
import akka.util.ByteString

import scala.util.Random
import scala.collection.mutable
import scala.concurrent.duration._

/** Parameters for the Network simulator
  *
  * @param packetLoss The percentage from [0.0, 1.0] that a packet will be lost
  * @param packetDelay The end-to-end delay (ping) of all packets
  * @param packetReorderingChance The percentage from [0.0, 1.0] that a packet will be reordered
  * @param packetReorderingTime The absolute adjustment in milliseconds that a packet can have (either
  *                             forward or backwards in time)
  */
case class NetworkSimulatorParameters(packetLoss : Double,
                                      packetDelay : Int,
                                      packetReorderingChance : Double,
                                      packetReorderingTime : Int) {
  assert(packetLoss >= 0.0 && packetLoss <= 1.0)
  assert(packetDelay >= 0)
  assert(packetReorderingChance >= 0.0 && packetReorderingChance <= 1.0)
  assert(packetReorderingTime >= 0)
}


class UdpNetworkSimulator(server : ActorRef, params : NetworkSimulatorParameters) extends Actor {
  private val log = org.log4s.getLogger

  import scala.concurrent.ExecutionContext.Implicits.global

  //******* Internal messages
  private final case class ProcessInputQueue()
  private final case class ProcessOutputQueue()

  //******* Variables
  val packetDelayDuration = (params.packetDelay/2).milliseconds

  type QueueItem = (Udp.Message, Long)

  // sort in ascending order (older things get dequeued first)
  implicit val QueueItem = Ordering.by[QueueItem, Long](_._2).reverse

  val inPacketQueue = mutable.PriorityQueue[QueueItem]()
  val outPacketQueue = mutable.PriorityQueue[QueueItem]()

  val chaos = new Random()
  var interface = ActorRef.noSender

  def receive = {
    case ProcessInputQueue() =>
      val time = System.nanoTime()
      var exit = false

      while(inPacketQueue.nonEmpty && !exit) {
        val lastTime = time - inPacketQueue.head._2

        // this packet needs to be sent within 20 milliseconds or more
        if (lastTime >= 20000000) {
          server.tell(inPacketQueue.dequeue._1, interface)
        } else {
          schedule(lastTime.nanoseconds, outbound = false)
          exit = true
        }
      }
    case ProcessOutputQueue() =>
      val time = System.nanoTime()
      var exit = false

      while(outPacketQueue.nonEmpty && !exit) {
        val lastTime = time - outPacketQueue.head._2

        // this packet needs to be sent within 20 milliseconds or more
        if (lastTime >= 20000000) {
          interface.tell(outPacketQueue.dequeue._1, server)
        } else {
          schedule(lastTime.nanoseconds, outbound = true)
          exit = true
        }
      }
    // outbound messages
    case msg @ Udp.Send(payload, target, _) =>
      handlePacket(msg, outPacketQueue, outbound = true)
    // inbound messages
    case msg @ Udp.Received(payload, sender) =>
      handlePacket(msg, inPacketQueue, outbound = false)
    case msg @ Udp.Bound(address) =>
      interface = sender()
      log.info(s"Hooked ${server.path} for network simulation")
      server.tell(msg, self) // make sure the server sends *us* the packets
    case default =>
      val from = sender()

      if(from == server)
        interface.tell(default, server)
      else if(from == interface)
        server.tell(default, interface)
      else
        log.error("Unexpected sending Actor " + from.path)
  }

  def handlePacket(message : Udp.Message, queue : mutable.PriorityQueue[QueueItem], outbound : Boolean) = {
    val name : String = if(outbound) "OUT" else "IN"
    val queue : mutable.PriorityQueue[QueueItem] = if(outbound) outPacketQueue else inPacketQueue

    if(chaos.nextDouble() > params.packetLoss) {
      // if the message queue is empty, then we need to reschedule our task
      if(queue.isEmpty)
        schedule(packetDelayDuration, outbound)

      // perform a reordering
      if(chaos.nextDouble() <= params.packetReorderingChance) {
        // creates the range (-1.0, 1.0)
        // time adjustment to move the packet (forward or backwards in time)
        val adj = (2*(chaos.nextDouble()-0.5)*params.packetReorderingTime).toLong
        queue += ((message, System.nanoTime() + adj*1000000))

        log.debug(s"Reordered $name by ${adj}ms - $message")
      } else { // normal message
        queue += ((message, System.nanoTime()))
      }
    } else {
      log.debug(s"Dropped $name - $message")
    }
  }

  def schedule(duration : FiniteDuration, outbound : Boolean) = context.system.scheduler.scheduleOnce(
    packetDelayDuration,
    self,
    if(outbound) ProcessOutputQueue() else ProcessInputQueue()
  )
}
