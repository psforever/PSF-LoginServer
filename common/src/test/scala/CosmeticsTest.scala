// Copyright (c) 2019 PSForever
import net.psforever.packet.game.objectcreate.{Cosmetics, PersonalStyle}
import org.specs2.mutable._
import net.psforever.types.Vector3

class CosmeticsTest extends Specification {
  "Cosmetics" should {
    "construct" in {
      Cosmetics()
      Cosmetics(3)
      Cosmetics(PersonalStyle.NoHelmet)
      Cosmetics(Set(PersonalStyle.NoHelmet))
      Cosmetics(true, false, false, false, false)
      ok
    }

    "translate into a numeric value" in {
      Cosmetics().pstyles mustEqual 0
      Cosmetics(3).pstyles mustEqual 3
      Cosmetics(PersonalStyle.NoHelmet).pstyles mustEqual PersonalStyle.NoHelmet.id
      Cosmetics(Set(PersonalStyle.NoHelmet, PersonalStyle.Earpiece)).pstyles mustEqual PersonalStyle.NoHelmet.id + PersonalStyle.Earpiece.id
      Cosmetics(true, false, false, false, false).pstyles mustEqual PersonalStyle.NoHelmet.id
    }

    "translate into a list of cosmetic style tokens" in {
      Cosmetics().Styles mustEqual Set()
      Cosmetics(3).Styles mustEqual Set(PersonalStyle.BrimmedCap, PersonalStyle.Earpiece)
      Cosmetics(PersonalStyle.NoHelmet).Styles mustEqual Set(PersonalStyle.NoHelmet)
      Cosmetics(Set(PersonalStyle.NoHelmet)).Styles mustEqual Set(PersonalStyle.NoHelmet)
      Cosmetics(true, false, false, false, false).Styles mustEqual Set(PersonalStyle.NoHelmet)
    }

    "report containing specific values only" in {
      val cos = Cosmetics(Set(PersonalStyle.NoHelmet, PersonalStyle.Earpiece))
      cos.contains(PersonalStyle.NoHelmet) mustEqual true
      cos.contains(PersonalStyle.Beret) mustEqual false
    }

    "add values" in {
      val cos = Cosmetics()
      cos.Styles mustEqual Set()
      val cos1 = cos + PersonalStyle.NoHelmet
      cos1.Styles mustEqual Set(PersonalStyle.NoHelmet)
      cos1.Styles mustNotEqual cos.Styles
      val cos2 = cos1 + PersonalStyle.Beret
      cos2.Styles mustEqual Set(PersonalStyle.NoHelmet, PersonalStyle.Beret)
      cos2.Styles mustNotEqual cos.Styles
      cos2.Styles mustNotEqual cos1.Styles
    }

    "can not add already included values" in {
      val cos = Cosmetics(Set(PersonalStyle.NoHelmet, PersonalStyle.Beret))
      cos.Styles mustEqual Set(PersonalStyle.NoHelmet, PersonalStyle.Beret)
      val cos1 = cos + PersonalStyle.Beret
      cos1.Styles mustEqual Set(PersonalStyle.NoHelmet, PersonalStyle.Beret)
      cos ne cos1 mustEqual true
    }

    "remove values" in {
      val cos = Cosmetics(Set(PersonalStyle.NoHelmet, PersonalStyle.Beret))
      cos.Styles mustEqual Set(PersonalStyle.NoHelmet, PersonalStyle.Beret)
      val cos1 = cos - PersonalStyle.NoHelmet
      cos1.Styles mustEqual Set(PersonalStyle.Beret)
      cos1.Styles mustNotEqual cos.Styles
      val cos2 = cos1 - PersonalStyle.Beret
      cos2.Styles mustEqual Set()
      cos2.Styles mustNotEqual cos.Styles
      cos2.Styles mustNotEqual cos1.Styles
    }

    "can not remove un-included or already excluded values" in {
      val cos = Cosmetics(Set(PersonalStyle.NoHelmet, PersonalStyle.Beret))
      cos.Styles mustEqual Set(PersonalStyle.NoHelmet, PersonalStyle.Beret)
      val cos1 = cos - PersonalStyle.Beret
      cos1.Styles mustEqual Set(PersonalStyle.NoHelmet)

      val cos2 = cos - PersonalStyle.Beret //again
      cos2.Styles mustEqual Set(PersonalStyle.NoHelmet)

      val cos3 = cos1 - PersonalStyle.Earpiece
      cos3.Styles mustEqual Set(PersonalStyle.NoHelmet)
    }
  }
}
