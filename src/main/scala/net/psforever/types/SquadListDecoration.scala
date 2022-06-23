// Copyright (c) 2022 PSForever
package net.psforever.types

import scodec.codecs.uint

object SquadListDecoration extends Enumeration {
  type Type = Value

  val NotAvailable  = Value(0)
  val Available     = Value(1)
  val CertQualified = Value(2)
  val SearchResult  = Value(3)

  implicit val codec = uint(bits = 3).xmap[SquadListDecoration.Value](
    {
      value =>
        if (value < 4) {
          SquadListDecoration(value)
        } else if (value < 7) {
          SquadListDecoration.Available
        } else {
          SquadListDecoration.NotAvailable
        }
    },
    _.id
  )
}
