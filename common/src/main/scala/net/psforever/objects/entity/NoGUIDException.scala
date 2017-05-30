// Copyright (c) 2017 PSForever
package net.psforever.objects.entity

case class NoGUIDException(private val message: String = "",
                           private val cause: Throwable = None.orNull
                          ) extends RuntimeException(message, cause)
