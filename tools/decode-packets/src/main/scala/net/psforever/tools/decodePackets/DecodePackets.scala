// Copyright (c) 2020 PSForever
package net.psforever.tools.decodePackets

import java.io.{File, IOException, OutputStream, PrintStream}
import java.nio.charset.CodingErrorAction
import java.nio.file.{Files, Path, Paths, StandardCopyOption}
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
    inDir: String = System.getProperty("user.dir"),
    outDir: String = System.getProperty("user.dir"),
    preprocessed: Boolean = false,
    skipExisting: Boolean = false,
    errorLogs: Boolean = false,
    files: Seq[File] = Seq()
)

object DecodePackets {
  private val utf8Decoder = Codec.UTF8.decoder.onMalformedInput(CodingErrorAction.REPORT)
  private val normalSystemOut = System.out
  private val outCapture: PrintStream = new PrintStream(new OutputStream() {
    @Override
    @throws(classOf[Exception])
    def write(arg0: Int): Unit = { }
  })

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
            c.copy(
              files = c.files ++ getAllFilePathsFromDirectory(x).collect { case path => path.toFile },
              inDir = x
            )
          }
          .text("Input directory"),
        opt[Unit]('p', "preprocessed")
          .action((_, c) => c.copy(preprocessed = true))
          .text("Files are already preprocessed gcapy ASCII files"),
        opt[Unit]('s', "skip-existing")
          .action((_, c) => c.copy(skipExisting = true))
          .text("Skip files that already exist in the output directory"),
        opt[Unit]('e', "error-logs")
          .action((_, c) => c.copy(errorLogs = true))
          .text("Write decoding errors to another file in the output directory"),
        opt[File]('f', "file")
          .unbounded()
          .action((x, c) => c.copy(files = c.files :+ x))
          .text("Individual files to decode ...")
      )
    }

    val opts = OParser.parse(parser, args, Config()) match {
      case Some(config) =>
        config
      case _ =>
        sys.exit(1)
    }

    var skipExisting = opts.skipExisting
    val outDir = new File(opts.outDir)
    if (!outDir.exists()) {
      skipExisting = false
      outDir.mkdirs()
    } else if (outDir.isFile) {
      println(s"error: out-dir is file")
      sys.exit(1)
    }

    val files: Seq[File] = {
      val (readable, unreadable) = opts.files.partition(_.exists())
      if (unreadable.nonEmpty) {
        println(s"The following ${unreadable.size} input files may not exist and will be skipped:")
        unreadable.foreach { file => println(s"- ${file.getAbsolutePath}") }
      }
      if (skipExisting) {
        val (skipped, decodable) = filesWithSameNameInDirectory(opts.outDir, readable)
        if (skipped.nonEmpty) {
          println(s"The following ${skipped.size} input files will not be decoded (reason: skip-existing flag set):")
          skipped.foreach { file => println(s"- ${file.getAbsolutePath}") }
        }
        decodable
      } else {
        readable
      }
    }
    if (files.isEmpty) {
      println("No input files are detected.  Please set an input directory with files or indicate individual files.")
      sys.exit(1)
    }
    println(s"${files.size} input file(s) detected.")

    var deleteTempFolderAfterwards: Boolean = false
    val tmpFolderPath = System.getProperty("java.io.tmpdir") + "/psforever-decode-packets"
    val tmpFolder = new File(tmpFolderPath)
    if (!tmpFolder.exists()) {
      deleteTempFolderAfterwards = true
      tmpFolder.mkdirs()
    } else if (getAllFilePathsFromDirectory(tmpFolderPath).isEmpty) {
      deleteTempFolderAfterwards = true
    }

    val bufferedWriter: (String, String) => WriterWrapper = if (opts.errorLogs) {
      errorWriter
    } else {
      normalWriter
    }

    if (opts.preprocessed) {
      decodeFilesUsing(files, extension = ".txt", tmpFolder, opts.outDir, bufferedWriter, preprocessed)
    } else {
      decodeFilesUsing(files, extension = ".gcap", tmpFolder, opts.outDir, bufferedWriter, gcapy)
    }

    if (deleteTempFolderAfterwards) {
      //if the temporary directory only exists because of this script, it should be safe to delete it
      FileUtils.forceDelete(tmpFolder)
    } else {
      //delete just the files that were created (if files were overwrote, nothing we can do)
      val (deleteThese, _) = filesWithSameNameAs(
        files,
        getAllFilePathsFromDirectory(tmpFolder.getAbsolutePath).toIndexedSeq.map(_.toFile)
      )
      deleteThese.foreach(FileUtils.forceDelete)
    }
  }

  /**
   * Separate files between those that
   * can be found in a given directory location by comparing against file names
   * and those that can not.
   * @param directory where the existing files may be found
   * @param files files to test for matching names
   * @see `filesWithSameNameAs`
   * @see `getAllFilePathsFromDirectory`
   * @return a tuple of file lists, comparing the param files against files in the directory;
   *         the first are the files whose names match;
   *         the second are the files whose names do not match
   */
  private def filesWithSameNameInDirectory(directory: String, files: Seq[File]): (Seq[File], Seq[File]) = {
    filesWithSameNameAs(
      getAllFilePathsFromDirectory(directory).toIndexedSeq.map(_.toFile),
      files
    )
  }
  /**
   * Separate files between those that
   * can be found amongst a group of files by comparing against file names
   * and those that can not.
   * @param existingFiles files whose names are to test against
   * @param files files to test for matching names
   * @see `lowercaseFileNameString`
   * @return a tuple of file lists, comparing the param files against files in the directory;
   *         the first are the files whose names match;
   *         the second are the files whose names do not match
   */
  private def filesWithSameNameAs(existingFiles: Seq[File], files: Seq[File]): (Seq[File], Seq[File]) = {
    val existingFileNames = existingFiles.map { path => lowercaseFileNameString(path.toString) }
    files.partition { file => existingFileNames.exists(_.endsWith(lowercaseFileNameString(file.getName))) }
  }

  /**
   * Isolate a file's name from a file's path.
   * The path is recognized as the direstory structure information,
   * everything to the left of the last file separator character.
   * The file extension is included.
   * @param filename file path of questionable content and length, but including the file name
   * @return file name only
   */
  private def lowercaseFileNameString(filename: String): String = {
    (filename.lastIndexOf(File.separator) match {
      case -1 => filename
      case n => filename.substring(n)
    }).toLowerCase()
  }

  /**
   * Enumerate over files found in the given directory for later.
   * @param directory where the files are found
   * @see `Files.isDirectory`
   * @see `Files.exists`
   * @see `Paths.get`
   * @return the discovered file paths
   */
  private def getAllFilePathsFromDirectory(directory: String): Array[Path] = {
    val dir = Paths.get(directory)
    val exists = Files.exists(dir)
    if (exists && Files.isDirectory(dir)) {
      dir.toFile.listFiles().map(_.toPath)
    } else if (!exists) {
      println(s"error: in-dir does not exist")
      Array.empty
    } else {
      println(s"error: in-dir is file")
      Array.empty
    }
  }

  /**
   * The primary entry point into the process of parsing the packet capture files
   * and producing the decoded packet data.
   * Should be configurable for whatever state that the packet capture file can be structured.
   * @param files all of the discovered files for consideration
   * @param extension file extension being concatenated
   * @param temporaryDirectory destination directory where files temporarily exist while being written
   * @param outDirectory destination directory where the files are stored after being written
   * @param readDecodeAndWrite next step of the file decoding process
   * @see `Files.move`
   * @see `Paths.get`
   * @see `System.setOut`
   */
  private def decodeFilesUsing(
                                files: Seq[File],
                                extension: String,
                                temporaryDirectory: File,
                                outDirectory: String,
                                writerConstructor: (String, String)=>WriterWrapper,
                                readDecodeAndWrite: (File,WriterWrapper)=>Unit
                              ): Unit = {
    files.par.foreach { file =>
      val fileName = file.getName.split(extension)(0)
      val outDirPath = outDirectory + File.separator
      val tmpDirPath = temporaryDirectory.getAbsolutePath + File.separator
      val writer = writerConstructor(tmpDirPath, fileName)
      try {
        System.setOut(outCapture)
        readDecodeAndWrite(file, writer)
        System.setOut(normalSystemOut)
        writer.close()
        writer.getFileNames.foreach { fileNameWithExt =>
          Files.move(Paths.get(tmpDirPath + fileNameWithExt), Paths.get(outDirPath + fileNameWithExt), StandardCopyOption.REPLACE_EXISTING)
        }
      } catch {
        case e: Throwable =>
          println(s"File ${file.getName} threw an exception because ${e.getMessage}")
          writer.close()
          e.printStackTrace()
      }
    }
  }

  /**
   * Read data from ASCII transcribed gcapy files.
   * @param file file to read
   * @param writer writer for output
   * @see `decodeFileContents`
   * @see `File.getAbsolutePath`
   * @see `Source.fromFile`
   * @see `Source.getLines`
   * @see `Using`
   */
  private def preprocessed(file: File, writer: WriterWrapper): Unit = {
    Using(Source.fromFile(file.getAbsolutePath)(utf8Decoder)) { source =>
      println(s"${decodeFileContents(writer, source.getLines())} lines read from file ${file.getName}")
    }
  }

  /**
   * Read data from gcapy files.
   * @param file file to read
   * @param writer writer for output
   * @see `decodeFileContents`
   * @see `File.getAbsolutePath`
   * @see `Source.fromFile`
   * @see `Source.getLines`
   * @see `Using`
   */
  private def gcapy(file: File, writer: WriterWrapper): Unit = {
    Using(Source.fromString(s"gcapy -xa '${file.getAbsolutePath}'" !!)) { source =>
      println(s"${decodeFileContents(writer, source.getLines())} lines read from file ${file.getName}")
    }
  }

  /**
   * Decode each line from the original file, decode it, then write it to the output file.
   * @param writer writer for output
   * @param lines raw packet data from the source
   * @throws java.io.IOException if writing data goes incorrectly
   * @see `decodePacket`
   * @see `isNestedPacket`
   * @see `recursivelyHandleNestedPacket`
   * @see `shortGcapyString`
   * @return number of lines read from the source
   */
  @throws(classOf[IOException])
  private def decodeFileContents(writer: WriterWrapper, lines: Iterator[String]): Int = {
    var linesToSkip = 0
    var linesRead: Int = 0
    for (line <- lines.drop(1)) {
      linesRead += 1
      if (linesToSkip > 0) {
        linesToSkip -= 1
      } else {
        val header = shortGcapyString(line)
        val decodedLine = decodePacket(header, line.drop(line.lastIndexOf(' ')))
        writer.write(decodedLine)
        val decodedLineText = decodedLine.text
        if (isNestedPacket(decodedLineText)) {
          // Packet with nested packets, including possibly other nested packets within e.g. SlottedMetaPacket containing a MultiPacketEx
          val nestedLinesToSkip = recursivelyHandleNestedPacket(header, decodedLineText, writer)
          // Gcapy output has duplicated lines for SlottedMetaPackets, so we can skip over those if found to reduce noise
          // The only difference between the original and duplicate lines is a slight difference in timestamp of when the packet was processed
          linesToSkip = decodedLineText.indexOf("SlottedMetaPacket") match {
            case pos if pos >= 0 && nestedLinesToSkip > 0 =>
              writer.write(str = s"Skipping $nestedLinesToSkip duplicate lines")
              writer.newLine()
              nestedLinesToSkip
            case _ => 0
          }
        }
        writer.newLine()
      }
    }
    linesRead
  }

  /**
   * Traverse down any nested packets such as `SlottedMetaPacket`, `MultiPacket`, and `MultiPacketEx`
   * and add indent for each layer down.
   * A number of lines to skip will be returned so duplicate lines following the nested packet can be filtered out.
   * @param decodedLine decoded packet data
   * @param writer writer for output
   * @param depth the number of layers to indent
   * @throws java.io.IOException if writing data goes incorrectly
   * @see `decodePacket`
   * @see `IOException`
   * @see `isNestedPacket`
   * @see `nested`
   * @see `Regex.findAllIn`
   * @return current indent layer
   */
  @throws(classOf[IOException])
  private def recursivelyHandleNestedPacket(
                                             header: String,
                                             decodedLine: String,
                                             writer: WriterWrapper,
                                             depth: Int = 0
                                           ): Int = {
    if (decodedLine.indexOf("Failed to parse") >= 0) return depth
    val regex   = "(0x[a-f0-9]+)".r
    val matches = regex.findAllIn(decodedLine)
    var linesToSkip = 0
    while (matches.hasNext) {
      val packet = matches.next()
      for (_ <- depth until 0 by -1) {
        writer.write(str = "-")
      }
      writer.write(str = "> ")
      val nextDecoded = nested(decodePacket(header, packet))
      val nextDecodedLine = nextDecoded.text
      writer.write(nextDecoded)
      if (isNestedPacket(nextDecodedLine)) {
        linesToSkip += recursivelyHandleNestedPacket(header, nextDecodedLine, writer, depth + 1)
      }
      linesToSkip += 1
    }
    linesToSkip
  }

  /**
   * Reformat data common to gcapy packet data files and their derivation form of ASCII transcription.
   * @param line original string
   * @return transformed string
   */
  private def shortGcapyString(line: String): String = {
    val regex = "Game record ([0-9]+) at ([0-9.]+s) is from ([S|C]).* to ([S|C]).*contents (.*)".r
    line match {
      case regex(index, time, from, _, contents) =>
        val direction = if (from == "S") "<<<" else ">>>"
        s"#$index @ $time C $direction S ($contents)"
    }
  }

  /**
   * A nested packet contains more packets.
   * @param decodedLine decoded packet data
   * @return `true`, if the packet is nested; `false`, otherwise
   */
  private def isNestedPacket(decodedLine: String): Boolean = {
    // Also matches MultiPacketEx
    decodedLine.indexOf("MultiPacket") >= 0 || decodedLine.indexOf("SlottedMetaPacket") >= 0
  }

  /**
   * Actually decode the packet data.
   * @param hexString raw packet data
   * @see `ByteVector.fromValidHex`
   * @see `DecodeError`
   * @see `DecodedPacket`
   * @see `PacketCoding.decodePacket`
   * @return decoded packet data
   */
  private def decodePacket(header: String, hexString: String): PacketOutput = {
    val byteVector = ByteVector.fromValidHex(hexString)
    val result = PacketCoding.decodePacket(byteVector) match {
      case Successful(value) => DecodedPacket(Some(header), value.toString.replace(",", ", "))
      case Failure(cause) => DecodeError(Some(header), s"Decoding error '${cause.toString}'")
    }
    result
  }

  /** produce a wrapper that writes decoded packet data */
  private def normalWriter(directoryPath: String, fileName: String): WriterWrapper = {
    DecodeWriter(directoryPath, fileName)
  }

  /**
   * When nested, a normal properly decoded packet does not print header information.
   * The header usually contains the original encoded hexadecimal string.
   * @param in decoded packet data
   * @return decoded packet data, potentially without header information
   */
  private def nested(in: PacketOutput): PacketOutput = {
    in match {
      case DecodedPacket(_, text) if !in.text.contains("Decoding error") => DecodedPacket(header=None, text)
      case _ => in
    }
  }

  /** produce a wrapper that writes decoded packet data and writes decode errors to a second file */
  private def errorWriter(directoryPath: String, fileName: String): WriterWrapper = {
    DecodeErrorWriter(directoryPath, fileName)
  }
}
