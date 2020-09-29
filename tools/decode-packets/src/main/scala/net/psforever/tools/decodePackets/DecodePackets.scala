package net.psforever.tools.decodePackets

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.charset.CodingErrorAction
import java.nio.file.{Files, Paths, StandardCopyOption}

import net.psforever.packet.PacketCoding
import org.apache.commons.io.FileUtils
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import scopt.OParser

import scala.collection.parallel.CollectionConverters._
import scala.io.{Codec, Source}
import scala.sys.process._
import scala.util.Using

case class Config(
    outDir: String = System.getProperty("user.dir"),
    preprocessed: Boolean = false,
    skipExisting: Boolean = false,
    files: Seq[File] = Seq()
)

object DecodePackets {
  def main(args: Array[String]): Unit = {

    val builder = OParser.builder[Config]

    val parser = {
      import builder._
      OParser.sequence(
        programName("psforever-decode-packets"),
        opt[String]('o', "out-dir")
          .action((x, c) => c.copy(outDir = x))
          .text("Output directory"),
        opt[Unit]('p', "preprocessed")
          .action((x, c) => c.copy(preprocessed = true))
          .text("Files are already preprocessed gcapy ascii files (do not call gcapy)"),
        opt[Unit]('s', "skip-existing")
          .action((x, c) => c.copy(skipExisting = true))
          .text("Skip files that already exist in out-dir"),
        arg[File]("<file>...")
          .unbounded()
          .required()
          .action((x, c) => c.copy(files = c.files :+ x))
      )
    }

    val opts = OParser.parse(parser, args, Config()) match {
      case Some(config) =>
        config
      case _ =>
        sys.exit(1)
    }

    val outDir = new File(opts.outDir);
    if (!outDir.exists()) {
      outDir.mkdirs()
    } else if (outDir.isFile) {
      println(s"error: out-dir is file")
      sys.exit(1)
    }

    opts.files.foreach { file =>
      if (!file.exists) {
        println(s"file ${file.getAbsolutePath} does not exist")
        sys.exit(1)
      }
    }

    val tmpFolder = new File(System.getProperty("java.io.tmpdir") + "/psforever-decode-packets")
    if (!tmpFolder.exists()) {
      tmpFolder.mkdirs()
    }

    opts.files.par.foreach { file =>
      val outFilePath = opts.outDir + "/" + file.getName.split(".gcap")(0) + ".txt"
      val outFile     = new File(outFilePath);

      if (outFile.exists() && opts.skipExisting) {
        return
      }

      val tmpFilePath = tmpFolder.getAbsolutePath + "/" + file.getName.split(".gcap")(0) + ".txt"
      val writer      = new BufferedWriter(new FileWriter(new File(tmpFilePath), false))

      try {
        val lines = if (opts.preprocessed) {
          val decoder = Codec.UTF8.decoder.onMalformedInput(CodingErrorAction.REPORT)
          Using(Source.fromFile(file.getAbsolutePath)(decoder)) { source => source.getLines() }.get
        } else {
          Using(Source.fromString(s"gcapy -xa '${file.getAbsolutePath}'" !!)) { source => source.getLines() }.get
        }

        var linesToSkip = 0
        for (line <- lines.drop(1)) {
          if (linesToSkip > 0) {
            linesToSkip -= 1
          } else {
            val decodedLine = decodePacket(line.drop(line.lastIndexOf(' ')))
            writer.write(s"${shortGcapyString(line)}")
            writer.newLine()

            if (!isNestedPacket(decodedLine)) {
              // Standard line, output as is with a bit of extra whitespace for readability
              writer.write(decodedLine.replace(",", ", "))
              writer.newLine()
            } else {
              // Packet with nested packets, including possibly other nested packets within e.g. SlottedMetaPacket containing a MultiPacketEx
              writer.write(s"${decodedLine.replace(",", ", ")}")
              writer.newLine()
              val nestedLinesToSkip = recursivelyHandleNestedPacket(decodedLine, writer)

              // Gcapy output has duplicated lines for SlottedMetaPackets, so we can skip over those if found to reduce noise
              // The only difference between the original and duplicate lines is a slight difference in timestamp of when the packet was processed
              linesToSkip = decodedLine.indexOf("SlottedMetaPacket") match {
                case pos if pos >= 0 && nestedLinesToSkip > 0 =>
                  writer.write(s"Skipping $nestedLinesToSkip duplicate lines")
                  writer.newLine()
                  nestedLinesToSkip
                case _ => 0
              }
            }

            writer.newLine()
          }
        }
        writer.close()
        Files.move(Paths.get(tmpFilePath), Paths.get(outFilePath), StandardCopyOption.REPLACE_EXISTING)
      } catch {
        case e: Throwable =>
          println(s"File ${file.getName} threw an exception")
          e.printStackTrace()
      }
    }

    FileUtils.forceDelete(tmpFolder)
  }

  /** Traverse down any nested packets such as SlottedMetaPacket, MultiPacket and MultiPacketEx and add indent for each layer down
    * The number of lines to skip will be returned so duplicate lines following SlottedMetaPackets in the gcapy output can be filtered out
    */
  def recursivelyHandleNestedPacket(decodedLine: String, writer: BufferedWriter, depth: Int = 0): Int = {
    if (decodedLine.indexOf("Failed to parse") >= 0) return depth
    val regex   = "(0x[a-f0-9]+)".r
    val matches = regex.findAllIn(decodedLine)

    var linesToSkip = 0
    while (matches.hasNext) {
      val packet = matches.next()

      for (i <- depth to 0 by -1) {
        if (i == 0) writer.write("> ")
        else writer.write("-")
      }

      val nextDecodedLine = decodePacket(packet)
      writer.write(s"${nextDecodedLine.replace(",", ", ")}")
      writer.newLine()

      if (isNestedPacket(nextDecodedLine)) {
        linesToSkip += recursivelyHandleNestedPacket(nextDecodedLine, writer, depth + 1)
      }

      linesToSkip += 1
    }

    linesToSkip
  }

  def shortGcapyString(line: String): String = {
    val regex = "Game record ([0-9]+) at ([0-9.]+s) is from ([S|C]).* to ([S|C]).*contents (.*)".r
    line match {
      case regex(index, time, from, to, contents) => {
        val direction = if (from == "S") "<<<" else ">>>"
        s"#$index @ $time C $direction S ($contents)"
      }
    }
  }

  def isNestedPacket(decodedLine: String): Boolean = {
    // Also matches MultiPacketEx
    decodedLine.indexOf("MultiPacket") >= 0 || decodedLine.indexOf("SlottedMetaPacket") >= 0
  }

  def decodePacket(hexString: String): String = {
    PacketCoding.decodePacket(ByteVector.fromValidHex(hexString)) match {
      case Successful(value) => value.toString
      case Failure(cause)    => s"Decoding error '${cause.toString}' for data ${hexString}"
    }
  }
}
