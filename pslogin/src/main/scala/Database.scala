// Copyright (c) 2017 PSForever
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.{Configuration, QueryResult, RowData, SSLConfiguration}
import scala.concurrent._
import ExecutionContext.Implicits.global

import scala.concurrent.Future

object Database {
  val UNKNOWN_ERROR : Int = -1
  val EMPTY_RESULT : Int = -2

  // TODO Will probably want to use the ConnectionPool, although I don't know the implications for multithreaded apps
  def getConnection = new PostgreSQLConnection(Configuration(
    "psforever", // user
    "198.50.147.114", // host
    5432, // port
    Some("PSFDB"), // password
    Some("psforever"),  // database
    SSLConfiguration(SSLConfiguration.Mode.Require)
  ))

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
