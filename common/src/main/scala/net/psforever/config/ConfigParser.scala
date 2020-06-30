// Copyright (c) 2019 PSForever
package net.psforever.config

import org.ini4j
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.TypeTag
import scala.annotation.implicitNotFound
import scala.concurrent.duration._
import scala.collection.mutable.Map

case class ConfigValueMapper[T](name: String)(f: (String => Option[T])) {
  def apply(t: String): Option[T] = f(t)
}

object ConfigValueMapper {
  import scala.language.implicitConversions

  implicit val toInt: ConfigValueMapper[Int] = ConfigValueMapper[Int]("toInt") { e =>
    try {
      Some(e.toInt)
    } catch {
      case e: Exception => None
    }
  }

  // TypeTag is necessary to be able to retrieve an instance of the Enum class
  // at runtime as it is usually erased at runtime. This is low cost as its only
  // used during the config parsing
  implicit def toEnum[E <: Enumeration#Value](implicit m: TypeTag[E]): ConfigValueMapper[E] =
    ConfigValueMapper[E]("toEnum") { e =>
      try {
        Some(EnumReflector.withName[E](e))
      } catch {
        case e: Exception => None
      }
    }

  implicit val toBool: ConfigValueMapper[Boolean] = ConfigValueMapper[Boolean]("toBool") { e =>
    if (e == "yes") {
      Some(true)
    } else if (e == "no") {
      Some(false)
    } else {
      None
    }
  }

  implicit val toFloat: ConfigValueMapper[Float] = ConfigValueMapper[Float]("toFloat") { e =>
    try {
      Some(e.toFloat)
    } catch {
      case e: Exception => None
    }
  }

  implicit val toDuration: ConfigValueMapper[Duration] = ConfigValueMapper[Duration]("toDuration") { e =>
    try {
      Some(Duration(e))
    } catch {
      case e: Exception => None
    }
  }

  implicit val toStr: ConfigValueMapper[String] = ConfigValueMapper[String]("toString") { e =>
    Some(e)
  }
}

sealed trait ConfigEntry {
  type Value
  val key: String
  val default: Value
  def getType: String
  val constraints: Seq[Constraint[Value]]
  def read(v: String): Option[Value]
}

final case class ConfigEntryString(key: String, default: String, constraints: Constraint[String]*) extends ConfigEntry {
  type Value = String
  def getType         = "String"
  def read(v: String) = ConfigValueMapper.toStr(v)
}

final case class ConfigEntryInt(key: String, default: Int, constraints: Constraint[Int]*) extends ConfigEntry {
  type Value = Int
  def getType         = "Int"
  def read(v: String) = ConfigValueMapper.toInt(v)
}

final case class ConfigEntryBool(key: String, default: Boolean, constraints: Constraint[Boolean]*) extends ConfigEntry {
  type Value = Boolean
  def getType         = "Bool"
  def read(v: String) = ConfigValueMapper.toBool(v)
}

final case class ConfigEntryEnum[E <: Enumeration](key: String, default: E#Value)(
    implicit m: TypeTag[E#Value],
    implicit val m2: TypeTag[E#ValueSet]
) extends ConfigEntry {
  type Value = E#Value
  val constraints: Seq[Constraint[E#Value]] = Seq()
  def getType                               = EnumReflector.values[E#ValueSet](m2).mkString(", ")
  def read(v: String)                       = ConfigValueMapper.toEnum[E#Value](m)(v)
}

final case class ConfigEntryFloat(key: String, default: Float, constraints: Constraint[Float]*) extends ConfigEntry {
  type Value = Float
  def getType         = "Float"
  def read(v: String) = ConfigValueMapper.toFloat(v)
}

final case class ConfigEntryTime(key: String, default: Duration, constraints: Constraint[Duration]*)
    extends ConfigEntry {
  type Value = Duration
  def getType         = "Time"
  def read(v: String) = ConfigValueMapper.toDuration(v)
}

case class ConfigSection(name: String, entries: ConfigEntry*)

@implicitNotFound("Nothing was inferred")
sealed trait ConfigTypeRequired[-T]

object ConfigTypeRequired {
  implicit object cfgTypeRequired extends ConfigTypeRequired[Any]
  //We do not want Nothing to be inferred, so make an ambiguous implicit
  implicit object `\n The Get[T] call needs a type T matching the corresponding ConfigEntry`
      extends ConfigTypeRequired[Nothing]
}

trait ConfigParser {
  protected var config_map: Map[String, Any] = Map()
  protected val config_template: Seq[ConfigSection]

  // Misuse of this function can lead to run time exceptions when the types don't match
  // ClassTag is needed due to type erasure on T
  // https://dzone.com/articles/scala-classtag-a-simple-use-case
  def Get[T: ConfigTypeRequired](key: String)(implicit m: ClassTag[T]): T = {
    config_map.get(key) match {
      case Some(value: T) => value
      case None =>
        throw new NoSuchElementException(s"Config key '${key}' not found")
      case Some(value: Any) =>
        throw new ClassCastException(
          s"Incorrect type T = ${m.runtimeClass.getSimpleName} passed to Get[T]: needed ${value.getClass.getSimpleName}"
        )
    }
  }

  def Load(filename: String): ValidationResult = {
    var map: Map[String, Any] = scala.collection.mutable.Map()

    LoadMap(filename, map) match {
      case i: Invalid =>
        i
      case Valid =>
        ReplaceConfig(map)
        // run post-parse validation only if we successfully parsed
        postParseChecks match {
          case i: Invalid =>
            i
          case Valid =>
            Valid
        }
    }
  }

  def LoadMap(filename: String, map: Map[String, Any]): ValidationResult = {
    val ini = new org.ini4j.Ini()

    try {
      ini.load(new java.io.File(filename))
    } catch {
      case e: org.ini4j.InvalidFileFormatException =>
        return Invalid(e.getMessage)
      case e: java.io.FileNotFoundException =>
        return Invalid(e.getMessage)
    }

    val result: Seq[ValidationResult] = config_template
      .map { section =>
        val sectionIni = ini.get(section.name)

        if (sectionIni == null)
          Seq(Invalid("section.missing", section.name))
        else
          section.entries.map(parseSection(sectionIni, _, map))
      }
      .reduceLeft((x, y) => x ++ y)

    val errors: Seq[Invalid] = result.collect { case iv: Invalid => iv }

    if (errors.length > 0)
      errors.reduceLeft((x, y) => x ++ y)
    else
      Valid
  }

  def ReplaceConfig(map: Map[String, Any]): Unit = {
    config_map = map
  }

  def GetRawConfig: Map[String, Any] = {
    config_map
  }

  def FormatErrors(invalidResult: Invalid): Seq[String] = {
    var count = 0;

    invalidResult.errors.map((error) => {
      var message = error.message;

      if (error.args.length > 0)
        message += " (" + error.args(0) + ")"

      count += 1;
      s"Error ${count}: ${message}"
    });
  }

  protected def postParseChecks: ValidationResult = {
    Valid
  }

  protected def parseSection(
      sectionIni: org.ini4j.Profile.Section,
      entry: ConfigEntry,
      map: Map[String, Any]
  ): ValidationResult = {
    var rawValue         = sectionIni.get(entry.key, 0)
    val full_key: String = sectionIni.getName + "." + entry.key

    val value = if (rawValue == null) {
      println(s"config warning: missing key '${entry.key}', using default value '${entry.default}'")
      entry.default
    } else {
      rawValue = rawValue.stripPrefix("\"").stripSuffix("\"")

      entry.read(rawValue) match {
        case Some(v) => v
        case None =>
          return Invalid(
            ValidationError(String.format("%s: value format error (expected: %s)", full_key, entry.getType))
          )
      }
    }

    map += (full_key -> value)

    ParameterValidator(entry.constraints, Some(value)) match {
      case v @ Valid => v
      case i @ Invalid(errors) => {
        Invalid(errors.map(x => ValidationError(x.messages.map(full_key + ": " + _), x.args: _*)))
      }
    }
  }
}
