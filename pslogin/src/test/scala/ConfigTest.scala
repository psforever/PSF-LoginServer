// Copyright (c) 2019 PSForever
import java.io._
import scala.io.Source
import org.specs2.mutable._
import net.psforever.config._
import scala.concurrent.duration._

class ConfigTest extends Specification {
  sequential

  val testConfig = getClass.getResource("/testconfig.ini").getPath

  "WorldConfig" should {
    "have no errors" in {
      WorldConfig.Load("config/worldserver.ini.dist") mustEqual Valid
    }

    "be formatted correctly" in {
      var lineno = 1
      for (line <- Source.fromFile("config/worldserver.ini.dist").getLines) {
        val linee :String = line
        val ctx = s"worldserver.ini.dist:${lineno}"
        val maxLen = 100
        val lineLen = line.length

        lineLen aka s"${ctx} - line length" must beLessThan(maxLen)
        line.slice(0, 1) aka s"${ctx} - leading whitespace found" mustNotEqual " "
        line.slice(line.length-1, line.length) aka s"${ctx} - trailing whitespace found" mustNotEqual " "

        lineno += 1
      }

      ok
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
      TestConfig.Get[TestEnum.Value]("default.enum_dog") mustEqual TestEnum.Dog
      TestConfig.Get[Int]("default.missing") mustEqual 1337
    }

    "throw when getting non-existant keys" in {
      TestConfig.Load(testConfig) mustEqual Valid
      TestConfig.Get[Int]("missing.key") must throwA[NoSuchElementException](message = "Config key 'missing.key' not found")
      TestConfig.Get[String]("missing.key") must throwA[NoSuchElementException](message = "Config key 'missing.key' not found")
    }

    "throw when Get is not passed the right type parameter" in {
      TestConfig.Load(testConfig) mustEqual Valid
      TestConfig.Get[Duration]("default.string") must throwA[ClassCastException](message = "Incorrect type T = Duration passed to Get\\[T\\]: needed String")
      TestConfig.Get[String]("default.int") must throwA[ClassCastException](message = "Incorrect type T = String passed to Get\\[T\\]: needed Int")
      ok
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
        ValidationError("bad.bad_int_range2: error.max", 2),
        ValidationError("bad.bad_enum: value format error (expected: Animal, Dog, Cat)")
      )

      error.errors mustEqual check_errors
    }
  }
}

object TestEnum extends Enumeration {
  val Animal, Dog, Cat = Value
}

object TestConfig extends ConfigParser {
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
      ConfigEntryInt("missing", 1337),
      ConfigEntryEnum[TestEnum.type]("enum_dog", TestEnum.Dog)
    )
  )
}

object TestBadConfig extends ConfigParser {
  protected val config_template = Seq(
    ConfigSection("bad",
      ConfigEntryInt("bad_int", 0),
      ConfigEntryTime("bad_time", 0 seconds),
      ConfigEntryFloat("bad_float", 0.0f),
      ConfigEntryBool("bad_bool", false),
      ConfigEntryInt("bad_int_range", 0, Constraints.min(0)),
      ConfigEntryInt("bad_int_range2", 0, Constraints.min(0), Constraints.max(2)),
      ConfigEntryEnum[TestEnum.type]("bad_enum", TestEnum.Animal)
    )
  )
}
