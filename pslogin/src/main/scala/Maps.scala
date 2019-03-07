// Copyright (c) 2017 PSForever
import net.psforever.objects.LocalProjectile
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.zones.ZoneMap
import zonemaps._

object Maps {

  val map1 = Map01.ZoneMap
  val map2 = Map02.ZoneMap
  val map3 = Map03.ZoneMap
  val map4 = Map04.ZoneMap
  val map5 = Map05.ZoneMap
  val map6 = Map06.ZoneMap
  val map7 = Map07.ZoneMap
  val map8 = Map08.ZoneMap
  val map9 = Map09.ZoneMap
  val map10 = Map10.ZoneMap
  val map11 = Map11.ZoneMap
  val map12 = Map12.ZoneMap
  val map13 = Map13.ZoneMap

  Projectiles(map1)
  Projectiles(map2)
  Projectiles(map3)
  Projectiles(map4)
  Projectiles(map5)
  Projectiles(map6)
  Projectiles(map7)
  Projectiles(map8)
  Projectiles(map9)
  Projectiles(map10)
  Projectiles(map11)
  Projectiles(map12)
  Projectiles(map13)


  val map14 = new ZoneMap("map14") {
    Projectiles(this)
  }

  val map15 = new ZoneMap("map15") {
    Projectiles(this)
  }

  val map16 = new ZoneMap("map16") {
    Projectiles(this)
  }

  val ugd01 = new ZoneMap("ugd01") {
    Projectiles(this)
  }

  val ugd02 = new ZoneMap("ugd02") {
    Projectiles(this)
  }

  val ugd03 = new ZoneMap("ugd03") {
    Projectiles(this)
  }

  val ugd04 = new ZoneMap("ugd04") {
    Projectiles(this)
  }

  val ugd05 = new ZoneMap("ugd05") {
    Projectiles(this)
  }

  val ugd06 = new ZoneMap("ugd06") {
    Projectiles(this)
  }

  val map96 = Map96.ZoneMap
  val map97 = Map97.ZoneMap
  val map98 = Map98.ZoneMap
  val map99 = Map99.ZoneMap

  Projectiles(map96)
  Projectiles(map97)
  Projectiles(map98)
  Projectiles(map99)

  def Projectiles(zmap: ZoneMap): Unit = {
    (Projectile.BaseUID until Projectile.RangeUID) foreach {
      zmap.LocalObject(_, LocalProjectile.Constructor)
    }
  }
}
