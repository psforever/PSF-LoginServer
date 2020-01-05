// Copyright (c) 2017 PSForever
package net.psforever.objects

class Account(private val accountId : Int, private val username : String, private val gm : Boolean = false)  {
  def AccountId : Int = accountId
  def Username : String = username
  def GM : Boolean = gm
}
