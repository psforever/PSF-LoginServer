import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

import scala.util.parsing.json._
import scodec.bits._
import scodec.bits.ByteVector._

object PSUtil{
    val ViableChecksums: List[String] = List("1014c2a973f9ccd2a310039eb484dc27")
    val ErrorMessage: String = "Invalid installation, please use an Vanilla Installation without any mods"
    def IsPSUtilPacket (msg:ByteVector) : Boolean ={
        val firstbyte = msg.take(1)
        val f4 = 244
        if(firstbyte.toInt(false) == f4){
            return true
        }
        return false
    }
    def getResponse (msg:ByteVector) : ByteVector ={
        val Json = new String(msg.toArray)
        val json:Option[Any] = JSON.parseFull(Json)
        val valuemap:Map[String,Any] = json.get.asInstanceOf[Map[String,Any]]
        val Checksum:String = valuemap.get("Checksum").get.asInstanceOf[String]
        var response = new String()
        if(ViableChecksums.contains(Checksum)){
            response = "OK!"
        }else{
            response = ErrorMessage
        }
        val responseUTF:ByteVector = ByteVector.apply(response.getBytes("utf8"))
        return responseUTF
    }
}