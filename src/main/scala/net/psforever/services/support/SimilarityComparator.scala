// Copyright (c) 2017 PSForever
package net.psforever.services.support

abstract class SimilarityComparator[A <: SupportActor.Entry] {

  /**
    * Match two entries by object and by zone information.
    *
    * @param entry1 the first entry
    * @param entry2 the second entry
    * @return if they match
    */
  def Test(entry1: A, entry2: A): Boolean
}
