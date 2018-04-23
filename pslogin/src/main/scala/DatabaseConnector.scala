// Copyright (c) 2017 PSForever
import com.github.mauricio.async.db.mysql.MySQLConnection
import com.github.mauricio.async.db.Configuration

object DatabaseConnector {
  def getAccountsConnection = new MySQLConnection(Configuration(
    "root",
    "localhost",
    3306,
    Some("psforever"), // password
    Some("psforever")  // database
  ))
}
