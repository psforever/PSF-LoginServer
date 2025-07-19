// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

abstract class BasicDefinition {
  private var name: String               = "definition"
  private var descriptor: Option[String] = None
  private var mapRevealId: Int           = 0

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

  def MapRevealId: Int = mapRevealId //for vehicle IDs used by reveal friendlies and enemies with a CUD

  def MapRevealId_=(mapRevealId: Int): Int = {
    this.mapRevealId = mapRevealId
    MapRevealId
  }
}
