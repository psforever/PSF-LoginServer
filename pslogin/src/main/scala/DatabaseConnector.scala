// Copyright (c) 2017 PSForever.net to present
import com.github.mauricio.async.db.Connection
import com.github.mauricio.async.db.mysql.MySQLConnection
import com.github.mauricio.async.db.mysql.util.URLParser

object DatabaseConnector {
  val accounts_db = URLParser.parse("jdbc:mysql://localhost:3306/psforever-accounts?user=root&password=PSForever")
  def getAccountsConnection = new MySQLConnection(accounts_db)
}
