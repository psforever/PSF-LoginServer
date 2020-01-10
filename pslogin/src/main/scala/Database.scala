// Copyright (c) 2017 PSForever
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.{Configuration, QueryResult, RowData, SSLConfiguration}
import scala.util.{Try,Success,Failure}
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import scala.concurrent.Future

object Database {
  private val logger = org.log4s.getLogger

  import WorldConfig.ConfigDatabaseSSL._

  val UNKNOWN_ERROR : Int = -1
  val EMPTY_RESULT : Int = -2
  val config = Configuration(
    WorldConfig.Get[String]("database.Username"),
    WorldConfig.Get[String]("database.Hostname"),
    WorldConfig.Get[Int]("database.Port"),
    Some(WorldConfig.Get[String]("database.Password")),
    Some(WorldConfig.Get[String]("database.Database")),
    WorldConfig.Get[WorldConfig.ConfigDatabaseSSL.Value]("database.SSL") match {
      case Disable => SSLConfiguration(SSLConfiguration.Mode.Disable)
      case Prefer => SSLConfiguration(SSLConfiguration.Mode.Prefer)
      case Require => SSLConfiguration(SSLConfiguration.Mode.Require)
      // not including VerifyCA as full is more secure
      case Verify => SSLConfiguration(SSLConfiguration.Mode.VerifyFull)
    }
  )

  def testConnection : Try[Boolean] = {
    try {
      val connection = Await.result(getConnection.connect, 2 seconds)
      val result = Await.result(query(connection.sendQuery("SELECT 0")), 2 seconds)
      connection.disconnect
      Success(true)
    } catch  {
      case e : com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException =>
        logger.error(e.errorMessage.message)
        Failure(e)
      case e : javax.net.ssl.SSLHandshakeException =>
        logger.error(s"${e.getMessage} (make sure your database supports SSL and the certificate matches)")
        Failure(e)
      case e : Throwable =>
        logger.error(s"Unknown database error: ${e.toString}")
        Failure(e)
    }
  }

  // TODO Will probably want to use the ConnectionPool, although I don't know the implications for multithreaded apps
  def getConnection = new PostgreSQLConnection(config)

  def query(query : Future[QueryResult]) : Future[Any] = {
    query.map(queryResult => queryResult.rows match {
      case Some(resultSet) =>
        if(resultSet.nonEmpty) {
          val row : RowData = resultSet.head
          row
        } else {
          EMPTY_RESULT
        }
      case None =>
        UNKNOWN_ERROR
    })
  }
}
