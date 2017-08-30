// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

abstract class BasicDefinition {
  private var name : String = "definition"

  def Name : String = name

  def Name_=(name : String) : String = {
    this.name = name
    Name
  }
}
