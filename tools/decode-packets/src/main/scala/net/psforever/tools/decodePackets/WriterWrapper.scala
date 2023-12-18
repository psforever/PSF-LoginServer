// Copyright (c) 2023 PSForever
package net.psforever.tools.decodePackets

import java.io.{BufferedWriter, File, FileWriter}

trait WriterWrapper {
  def write(str: String): Unit
  def write(str: PacketOutput): Unit
  def newLine(): Unit
  def close(): Unit
  def getFileNames: Seq[String]
}

final case class DecodeWriter(directoryPath: String, fileName: String) extends WriterWrapper {
  private val log: BufferedWriter = new BufferedWriter(
    new FileWriter(new File(directoryPath + fileName + ".txt"), false)
  )

  def write(str: String): Unit = log.write(str)

  def write(data: PacketOutput): Unit = {
    log.write(data.header)
    log.newLine()
    log.write(data.text)
    log.newLine()
  }

  def newLine(): Unit = log.newLine()

  def close(): Unit = log.close()

  def getFileNames: Seq[String] = Seq(fileName + ".txt")
}

final case class DecodeErrorWriter(directoryPath: String, fileName: String)
  extends WriterWrapper {
  private val log: DecodeWriter = DecodeWriter(directoryPath, fileName)
  private val errorLog: DecodeWriter = DecodeWriter(directoryPath, fileName+".error")

  def write(str: String): Unit = log.write(str)

  def write(data: PacketOutput): Unit = {
    log.write(data)
    log.newLine()
    data match {
      case error: DecodeError =>
        errorLog.write(error)
        errorLog.newLine()
      case _ => ()
    }
  }

  def newLine(): Unit = log.newLine()

  def close(): Unit = {
    log.close()
    errorLog.close()
  }

  def getFileNames: Seq[String] = log.getFileNames ++ errorLog.getFileNames
}
