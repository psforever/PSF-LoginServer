// Copyright (c) 2019 PSForever
package net.psforever.objects.teamwork

import net.psforever.objects.avatar.Certification
import net.psforever.types.Vector3

class Member {
  //about the position to be filled
  private var role: String                     = ""
  private var orders: String                   = ""
  private var requirements: Set[Certification] = Set()
  //about the individual filling the position
  private var name: String      = ""
  private var charId: Long      = 0L
  private var health: Int       = 0
  private var armor: Int        = 0
  private var zoneId: Int       = 0
  private var position: Vector3 = Vector3.Zero

  def Role: String = role

  def Role_=(title: String): String = {
    role = title
    Role
  }

  def Orders: String = orders

  def Orders_=(text: String): String = {
    orders = text
    Orders
  }

  def Requirements: Set[Certification] = requirements

  def Requirements_=(req: Set[Certification]): Set[Certification] = {
    requirements = req
    Requirements
  }

  def Name: String = name

  def Name_=(moniker: String): String = {
    name = moniker
    Name
  }

  def CharId: Long = charId

  def CharId_=(id: Long): Long = {
    charId = id
    CharId
  }

  def Health: Int = health

  def Health_=(red: Int): Int = {
    health = red
    Health
  }

  def Armor: Int = armor

  def Armor_=(blue: Int): Int = {
    armor = blue
    Armor
  }

  def ZoneId: Int = zoneId

  def ZoneId_=(id: Int): Int = {
    zoneId = id
    ZoneId
  }

  def Position: Vector3 = position

  def Position_=(pos: Vector3): Vector3 = {
    position = pos
    Position
  }

  def isAvailable: Boolean = {
    charId == 0
  }

  def isAvailable(certs: Set[Certification]): Boolean = {
    isAvailable && certs.intersect(requirements) == requirements
  }
}
