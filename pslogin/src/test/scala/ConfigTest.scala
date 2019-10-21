// Copyright (c) 2019 PSForever
import scala.io.Source
import org.specs2.mutable._
import net.psforever.config._
import scala.concurrent.duration._

class ConfigTest extends Specification {
  val testConfig = getClass.getResource("/testconfig.ini").getPath

  "WorldConfig" should {
    "have no errors" in {
      WorldConfig.Load("config/worldserver.ini.dist") mustEqual Valid
    }
  }

  "TestConfig" should {
    "parse" in {
      TestConfig.Load(testConfig) mustEqual Valid
      TestConfig.Get[String]("default.string") mustEqual "a string"
      TestConfig.Get[String]("default.string_quoted") mustEqual "a string"
      TestConfig.Get[Int]("default.int") mustEqual 31
      TestConfig.Get[Duration]("default.time") mustEqual (1 second)
      TestConfig.Get[Duration]("default.time2") mustEqual (100 milliseconds)
      TestConfig.Get[Float]("default.float") mustEqual 0.1f
      TestConfig.Get[Boolean]("default.bool_true") mustEqual true
      TestConfig.Get[Boolean]("default.bool_false") mustEqual false
      TestConfig.Get[Int]("default.missing") mustEqual 1337
    }
  }

  "TestBadConfig" should {
    "not parse" in {
      val error = TestBadConfig.Load(testConfig).asInstanceOf[Invalid]
      val check_errors = List(
        ValidationError("bad.bad_int: value format error (expected: Int)"),
        ValidationError("bad.bad_time: value format error (expected: Time)"),
        ValidationError("bad.bad_float: value format error (expected: Float)"),
        ValidationError("bad.bad_bool: value format error (expected: Bool)"),
        ValidationError("bad.bad_int_range: error.min", 0),
        ValidationError("bad.bad_int_range2: error.max", 2)
      )

      error.errors mustEqual check_errors
    }
  }
}

object TestConfig extends ConfigParser {
  protected var config_map : Map[String, Any] = Map()

  protected val config_template = Seq(
    ConfigSection("default",
      ConfigEntryString("string", ""),
      ConfigEntryString("string_quoted", ""),
      ConfigEntryInt("int", 0),
      ConfigEntryTime("time", 0 seconds),
      ConfigEntryTime("time2", 0 seconds),
      ConfigEntryFloat("float", 0.0f),
      ConfigEntryBool("bool_true", false),
      ConfigEntryBool("bool_false", true),
      ConfigEntryInt("missing", 1337)
    )
  )
}

object TestBadConfig extends ConfigParser {
  protected var config_map : Map[String, Any] = Map()

  protected val config_template = Seq(
    ConfigSection("bad",
      ConfigEntryInt("bad_int", 0),
      ConfigEntryTime("bad_time", 0 seconds),
      ConfigEntryFloat("bad_float", 0.0f),
      ConfigEntryBool("bad_bool", false),
      ConfigEntryInt("bad_int_range", 0, Constraints.min(0)),
      ConfigEntryInt("bad_int_range2", 0, Constraints.min(0), Constraints.max(2))
    )
  )
}
