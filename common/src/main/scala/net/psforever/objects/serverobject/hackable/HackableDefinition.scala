// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.hackable

class HackableDefinition {
  private var hackable : Boolean = false
  private var magicNumber : Long = 0

  def Hackable : Boolean = hackable

  def Hackable_=(state : Boolean) : Boolean = {
    hackable = state
    Hackable
  }

  def MagicNumber : Long = magicNumber

  def MagicNumber_=(magic : Long) : Long = {
    magicNumber = magic
    MagicNumber
  }
}
