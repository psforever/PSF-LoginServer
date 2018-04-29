// Copyright (c) 2017 PSForever
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.Configuration

object DatabaseConnector {
  def getAccountsConnection = new PostgreSQLConnection(Configuration(
    "postgres",
    "localhost",
    5432,
    Some("psforever"), // password
    Some("psforever")  // database
  ))
}
