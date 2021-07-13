// Copyright (c) 2017 PSForever
package objects.guidtask

import java.util.logging.LogManager

import scala.util.Success
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.actor.{NumberPoolActor, UniqueNumberSystem}
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.actor.Task

import scala.concurrent.Future

object GUIDTaskTest {
  class TestObject extends IdentifiableEntity

  class RegisterTestTask(probe: ActorRef) extends Task {
    def action(): Future[Any] = {
      probe ! Success
      Future(this)(scala.concurrent.ExecutionContext.Implicits.global)
    }
    def undo(): Unit = {}

    override def isSuccessful() : Boolean = false
  }

  def CommonTestSetup(implicit system: ActorSystem): (NumberPoolHub, ActorRef, ActorRef, TestProbe) = {
    import akka.actor.Props
    import akka.routing.RandomPool
    import akka.testkit.TestProbe

    val guid: NumberPoolHub = new NumberPoolHub(new MaxNumberSource(110))
    guid.AddPool("dynamic", (1 to 100).toList).Selector = new RandomSelector //TODO name is hardcoded for now
    val uns = system.actorOf(
      RandomPool(25).props(Props(classOf[UniqueNumberSystem], guid, GUIDTaskTest.AllocateNumberPoolActors(guid))),
      "uns"
    )
    LogManager.getLogManager.reset() //suppresses any internal loggers created by the above elements
    (guid, uns, ActorRef.noSender, TestProbe())
  }

  /**
    * @see `UniqueNumberSystem.AllocateNumberPoolActors(NumberPoolHub)(implicit ActorContext)`
    */
  def AllocateNumberPoolActors(poolSource: NumberPoolHub)(implicit system: ActorSystem): Map[String, ActorRef] = {
    poolSource.Pools
      .map({
        case ((pname, pool)) =>
          pname -> system.actorOf(Props(classOf[NumberPoolActor], pool), pname)
      })
      .toMap
  }
}
