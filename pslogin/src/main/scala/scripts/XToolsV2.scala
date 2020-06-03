package scripts

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Files, Paths, StandardCopyOption}
import java.nio.charset.CodingErrorAction

import net.psforever.packet._
import scodec.bits._
import scodec.Attempt.{Failure, Successful}

import scala.io.{Codec, Source}
import util.control.Breaks._
import scala.collection.parallel.CollectionConverters._

object XToolsV2 {
  def main(args: Array[String]): Unit = {

    // Replace the below directories with the correct locations before running

    // Directory containing gcapy ASCII output files
    val dirToProcess = "C:\\xtools\\in"

    // Directory for final decoded packet logs
    val dirForDecoded = "C:\\xtools\\out"

    // Temporary directory to write current log before moving to final directory
    val tempDir = "C:\\xtools\\temp"

    val files = new File(dirToProcess).listFiles

    files.par.foreach { f =>
      val file = new File(f.toString)
      val FileToWrite = tempDir + "/" + file.getName().split(".gcapy")(0) + ".txt"
      val FileToMoveTo = dirForDecoded + "/" + file.getName().split(".gcapy")(0) + ".txt"

      if (new File(FileToMoveTo).exists()) {
        println(s"File ${file.getName} exists - skipping")
        return
      } else {
        println(s"${FileToMoveTo} doesn't exist - Got new file ${file.getName}")
      }

      val FileToRead = file.toString
      val fw = new BufferedWriter(new FileWriter(FileToWrite, false))
      val decoder = Codec.UTF8.decoder.onMalformedInput(CodingErrorAction.REPORT)

      try {
        var linesToSkip = 0
        for (line <- Source.fromFile(FileToRead)(decoder).getLines().drop(1)) {
          breakable {
            if(linesToSkip > 0) {
              linesToSkip -= 1
              break
            }

            val decodedLine = DecodePacket(line.drop(line.lastIndexOf(' ')))
            fw.write(s"${ShortGcapyString(line)}")
            fw.newLine()

            if(!IsNestedPacket(decodedLine)) {
              // Standard line, output as is with a bit of extra whitespace for readability
              fw.write(decodedLine.replace(",", ", "))
              fw.newLine()
            } else {
              // Packet with nested packets, including possibly other nested packets within e.g. SlottedMetaPacket containing a MultiPacketEx
              fw.write(s"${decodedLine.replace(",", ", ")}")
              fw.newLine()
              val nestedLinesToSkip = RecursivelyHandleNestedPacket(decodedLine, fw)

              // Gcapy output has duplicated lines for SlottedMetaPackets, so we can skip over those if found to reduce noise
              // The only difference between the original and duplicate lines is a slight difference in timestamp of when the packet was processed
              linesToSkip = decodedLine.indexOf("SlottedMetaPacket") match {
                case pos if pos >= 0 && nestedLinesToSkip > 0 =>
                  fw.write(s"Skipping $nestedLinesToSkip duplicate lines")
                  fw.newLine()
                  nestedLinesToSkip
                case _ => 0
              }
            }

            fw.newLine()
          }
        }
      }
      catch {
        case e: Throwable =>
          println(s"File ${file.getName} threw an exception")
          e.printStackTrace()
      }
      finally {
        fw.close()
        MoveFile(FileToWrite, FileToMoveTo)
      }
    }
  }

  /*
    Traverse down any nested packets such as SlottedMetaPacket, MultiPacket and MultiPacketEx and add indent for each layer down
    The number of lines to skip will be returned so duplicate lines following SlottedMetaPackets in the gcapy output can be filtered out
   */
  def RecursivelyHandleNestedPacket(decodedLine : String, fw : BufferedWriter, depth : Int = 0): Int = {
    if(decodedLine.indexOf("Failed to parse") >= 0) return depth
    val regex = "(0x[a-f0-9]+)".r
    val matches = regex.findAllIn(decodedLine)

    var linesToSkip = 0
    while(matches.hasNext) {
      val packet = matches.next

      for(i <- depth to 0 by -1) {
        if(i == 0) fw.write("> ")
        else fw.write("-")
      }

      val nextDecodedLine = DecodePacket(packet)
      fw.write(s"${nextDecodedLine.replace(",", ", ")}")
      fw.newLine()

      if(IsNestedPacket(nextDecodedLine)) {
        linesToSkip += RecursivelyHandleNestedPacket(nextDecodedLine, fw, depth + 1)
      }

      linesToSkip += 1
    }

    linesToSkip
  }

  def ShortGcapyString(line : String): String = {
    val regex = "Game record ([0-9]+) at ([0-9.]+s) is from ([S|C]).* to ([S|C]).*contents (.*)".r
    line match {
      case regex(index, time, from, to, contents) => s"#$index @ $time $from -> $to ($contents)"
    }
  }

  def IsNestedPacket(decodedLine : String) : Boolean = {
    // Also matches MultiPacketEx
    decodedLine.indexOf("MultiPacket") >= 0 || decodedLine.indexOf("SlottedMetaPacket") >= 0
  }

  def DecodePacket(hexString: String) : String = {
    PacketCoding.DecodePacket(ByteVector.fromValidHex(hexString)) match {
      case Successful(value) => value.toString
      case Failure(cause) => cause.toString
    }
  }

  def MoveFile(sourcePath: String, targetPath: String) : Boolean = {
    var success = true
    try
      Files.move(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING)
    catch {
      case e: Exception =>
        success = false
        e.printStackTrace()
    }
    success
  }
}
