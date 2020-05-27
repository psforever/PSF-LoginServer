package net.psforever.psadmin

import net.psforever.WorldConfig
import scala.collection.mutable.Map

object CmdInternal {

  def cmdDumpConfig(args : Array[String]) = {
    val config = WorldConfig.GetRawConfig

    CommandGoodResponse(s"Dump of WorldConfig", config)
  }

  def cmdThreadDump(args : Array[String]) = {
    import scala.jdk.CollectionConverters._

    var data = Map[String,Any]()
    val traces = Thread.getAllStackTraces().asScala
    var traces_fmt = List[String]()

    for ((thread, trace) <- traces) {
      val info = s"Thread ${thread.getId} - ${thread.getName}\n"
      traces_fmt = traces_fmt ++ List(info + trace.mkString("\n"))
    }

    data{"trace"} = traces_fmt

    CommandGoodResponse(s"Dump of ${traces.size} threads", data)
  }
}
