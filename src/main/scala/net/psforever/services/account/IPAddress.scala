// Copyright (c) 2017 PSForever
package net.psforever.services.account

import java.net.InetSocketAddress

class IPAddress(private val address: InetSocketAddress) {
  def Address: String = address.getAddress.getHostAddress

  def CanonicalHostName: String = address.getAddress.getCanonicalHostName

  def HostName: String = address.getAddress.getHostName

  def Port: Int = address.getPort
}
