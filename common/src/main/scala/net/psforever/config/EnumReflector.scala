// Copyright (c) 2019 PSForever
package net.psforever.config

import scala.reflect.runtime.universe._

/**
 * Scala [[Enumeration]] helpers implementing Scala versions of
 * Java's [[java.lang.Enum.valueOf(Class[Enum], String)]].
 * @author Dmitriy Yefremov (http://yefremov.net/blog/scala-enum-by-name/)
 */
object EnumReflector {

  private val mirror: Mirror = runtimeMirror(getClass.getClassLoader)

  /**
   * Returns a value of the specified enumeration with the given name.
   * @param name value name
   * @tparam T enumeration type
   * @return enumeration value, see [[scala.Enumeration.withName(String)]]
   */
  def withName[T <: Enumeration#Value: TypeTag](name: String): T = {
    typeOf[T] match {
      case valueType @ TypeRef(enumType, _, _) =>
        val methodSymbol = factoryMethodSymbol(enumType, "withName")
        val moduleSymbol = enumType.termSymbol.asModule
        reflect(moduleSymbol, methodSymbol)(name).asInstanceOf[T]
    }
  }

  /**
   * Returns the set of values of an enumeration
   * @tparam T enumeration type
   * @return possible enumeration values, see [[scala.Enumeration.values()]]
   */
  def values[T <: Enumeration#ValueSet: TypeTag]: T = {
    typeOf[T] match {
      case valueType @ TypeRef(enumType, _, _) =>
        val methodSymbol = factoryMethodSymbol(enumType, "values")
        val moduleSymbol = enumType.termSymbol.asModule
        reflect(moduleSymbol, methodSymbol)().asInstanceOf[T]
    }
  }

  private def factoryMethodSymbol(enumType: Type, name : String): MethodSymbol = {
    enumType.member(TermName(name)).asMethod
  }

  private def reflect(module: ModuleSymbol, method: MethodSymbol)(args: Any*): Any = {
    val moduleMirror = mirror.reflectModule(module)
    val instanceMirror = mirror.reflect(moduleMirror.instance)
    instanceMirror.reflectMethod(method)(args:_*)
  }

}
