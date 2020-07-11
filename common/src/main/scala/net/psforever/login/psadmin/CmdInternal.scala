package net.psforever.login.psadmin

import net.psforever.util.Config
import scala.collection.mutable
import scala.jdk.CollectionConverters._

object CmdInternal {

  def cmdDumpConfig(args: Array[String]) = {
    val config =
      Config.config.root.keySet.asScala.map(key => key -> Config.config.getAnyRef(key).asInstanceOf[Any]).toMap
    CommandGoodResponse(s"Dump of WorldConfig", mutable.Map(config.toSeq: _*))
  }

  def cmdThreadDump(args: Array[String]) = {

    var data       = mutable.Map[String, Any]()
    val traces     = Thread.getAllStackTraces().asScala
    var traces_fmt = List[String]()

    for ((thread, trace) <- traces) {
      val info = s"Thread ${thread.getId} - ${thread.getName}\n"
      traces_fmt = traces_fmt ++ List(info + trace.mkString("\n"))
    }

    data { "trace" } = traces_fmt

    CommandGoodResponse(s"Dump of ${traces.size} threads", data)
  }
}
