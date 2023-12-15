package net.psforever.tools.decodePackets

import java.io.{BufferedWriter, File, FileWriter, IOException}
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
import scala.util.{Try, Using}

case class Config(
    inDir: String = System.getProperty("user.dir"),
    outDir: String = System.getProperty("user.dir"),
    preprocessed: Boolean = false,
    skipExisting: Boolean = false,
    files: Seq[File] = Seq()
)

object DecodePackets {
  private val decoder = Codec.UTF8.decoder.onMalformedInput(CodingErrorAction.REPORT)

  def main(args: Array[String]): Unit = {
    val builder = OParser.builder[Config]
    val parser = {
      import builder._
      OParser.sequence(
        programName("psforever-decode-packets"),
        opt[String]('o', "out-dir")
          .action((x, c) => c.copy(outDir = x))
          .text("Output directory"),
        opt[String]('i', "in-dir")
          .action { (x, c) =>
            getAllFilesFromDirectory(x, c).copy(inDir = x)
          }
          .text("Input directory"),
        opt[Unit]('p', "preprocessed")
          .action((_, c) => c.copy(preprocessed = true))
          .text("Files are already preprocessed gcapy ascii files (do not call gcapy)"),
        opt[Unit]('s', "skip-existing")
          .action((_, c) => c.copy(skipExisting = true))
          .text("Skip files that already exist in out-dir"),
        opt[File]('f', "file")
          .unbounded()
          .action((x, c) => c.copy(files = c.files :+ x))
      )
    }

    val opts = OParser.parse(parser, args, Config()) match {
      case Some(config) =>
        config
      case _ =>
        sys.exit(1)
    }

    val outDir = new File(opts.outDir)
    if (!outDir.exists()) {
      outDir.mkdirs()
    } else if (outDir.isFile) {
      println(s"error: out-dir is file")
      sys.exit(1)
    }

    if (opts.files.isEmpty) {
      println("error: input files not defined; set directory or indicate files")
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

    println(s"${opts.files.size} files found")
    if (opts.preprocessed) {
      decodeFilesUsing(opts.files, extension=".txt", tmpFolder, opts.outDir, opts.skipExisting, preprocessed)
    } else {
      decodeFilesUsing(opts.files, extension=".gcap", tmpFolder, opts.outDir, opts.skipExisting, gcapy)
    }
    FileUtils.forceDelete(tmpFolder)
  }

  private def getAllFilesFromDirectory(directory: String, opts: Config): Config = {
    val inDir = Paths.get(directory)
    if (Files.exists(inDir) && Files.isDirectory(inDir)) {
      var outOpts = opts
      Files.list(inDir).forEach(file =>
        outOpts = outOpts.copy(files = outOpts.files :+ file.toFile)
      )
      outOpts
    } else if (!Files.exists(inDir)) {
      println(s"error: in-dir does not exist")
      opts
    } else {
      println(s"error: in-dir is file")
      opts
    }
  }

  private def decodeFilesUsing(
                                files: Seq[File],
                                extension: String,
                                temporaryDirectory: File,
                                outDirectory: String,
                                skipExisting: Boolean,
                                decodingFunc: (File,BufferedWriter)=>Try[List[String]]
                              ): Unit = {
    files.par.foreach { file =>
      val fileName = file.getName.split(extension)(0)
      val outFilePath = outDirectory + "/" + fileName + ".txt"
      val outFile = new File(outFilePath)
      if (skipExisting && outFile.exists()) {
        println(s"file $fileName skipped due to params")
      } else {
        val tmpFilePath = temporaryDirectory.getAbsolutePath + "/" + fileName + ".txt"
        val writer = new BufferedWriter(new FileWriter(new File(tmpFilePath), false))
        try {
          decodingFunc(file, writer).collect { lines =>
            println(s"${lines.size} lines read from file $fileName")
            processAndRewriteFileContents(writer, lines)
          }
          writer.close()
          Files.move(Paths.get(tmpFilePath), Paths.get(outFilePath), StandardCopyOption.REPLACE_EXISTING)
        } catch {
          case e: Throwable =>
            println(s"File ${file.getName} threw an exception because ${e.getMessage}")
            writer.close()
            e.printStackTrace()
        }
      }
    }
  }

  private def preprocessed(file: File, writer: BufferedWriter): Try[List[String]] = {
    Using(Source.fromFile(file.getAbsolutePath)(decoder)) { source =>
      source.getLines().toList
    }
  }

  private def gcapy(file: File, writer: BufferedWriter): Try[List[String]] = {
    Using(Source.fromString(s"gcapy -xa '${file.getAbsolutePath}'" !!)) { source =>
      source.getLines().toList
    }
  }

  @throws(classOf[IOException])
  private def processAndRewriteFileContents(writer: BufferedWriter, lines: List[String]): Unit = {
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
  }

  /** Traverse down any nested packets such as SlottedMetaPacket, MultiPacket and MultiPacketEx and add indent for each layer down
    * The number of lines to skip will be returned so duplicate lines following SlottedMetaPackets in the gcapy output can be filtered out
    */
  private def recursivelyHandleNestedPacket(decodedLine: String, writer: BufferedWriter, depth: Int = 0): Int = {
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

  private def shortGcapyString(line: String): String = {
    val regex = "Game record ([0-9]+) at ([0-9.]+s) is from ([S|C]).* to ([S|C]).*contents (.*)".r
    line match {
      case regex(index, time, from, _, contents) =>
        val direction = if (from == "S") "<<<" else ">>>"
        s"#$index @ $time C $direction S ($contents)"
    }
  }

  private def isNestedPacket(decodedLine: String): Boolean = {
    // Also matches MultiPacketEx
    decodedLine.indexOf("MultiPacket") >= 0 || decodedLine.indexOf("SlottedMetaPacket") >= 0
  }

  private def decodePacket(hexString: String): String = {
    PacketCoding.decodePacket(ByteVector.fromValidHex(hexString)) match {
      case Successful(value) => value.toString
      case Failure(cause)    => s"Decoding error '${cause.toString}'"
    }
  }
}
