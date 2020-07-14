// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

abstract class BasicDefinition {
  private var name: String               = "definition"
  private var descriptor: Option[String] = None

  def Name: String = name

  def Name_=(name: String): String = {
    this.name = name
    Name
  }

  def Descriptor: String = descriptor.getOrElse(Name)

  def Descriptor_=(description: String): String = Descriptor_=(Some(description))

  def Descriptor_=(description: Option[String]): String = {
    descriptor = description
    Descriptor
  }
}
