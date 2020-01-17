// Copyright (c) 2017 PSForever
import net.psforever.objects.LocalProjectile
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.zones.ZoneMap
import zonemaps._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Maps {
  import scala.concurrent.ExecutionContext.Implicits.global

  val map1f = Future { Map01.ZoneMap }
  val map2f = Future { Map02.ZoneMap }
  val map3f = Future { Map03.ZoneMap }
  val map4f = Future { Map04.ZoneMap }
  val map5f = Future { Map05.ZoneMap }
  val map6f = Future { Map06.ZoneMap }
  val map7f = Future { Map07.ZoneMap }
  val map8f = Future { Map08.ZoneMap }
  val map9f = Future { Map09.ZoneMap }
  val map10f = Future { Map10.ZoneMap }
  val map11f = Future { Map11.ZoneMap }
  val map12f = Future { Map12.ZoneMap }
  val map13f = Future { Map13.ZoneMap }
  val ugd01f = Future { Ugd01.ZoneMap }
  val ugd02f = Future { Ugd02.ZoneMap }
  val ugd03f = Future { Ugd03.ZoneMap }
  val ugd04f = Future { Ugd04.ZoneMap }
  val ugd05f = Future { Ugd05.ZoneMap }
  val ugd06f = Future { Ugd06.ZoneMap }
  val map96f = Future { Map96.ZoneMap }
  val map97f = Future { Map97.ZoneMap }
  val map98f = Future { Map98.ZoneMap }
  val map99f = Future { Map99.ZoneMap }

  val map1 = Await.result(map1f, 30 seconds)
  val map2 = Await.result(map2f, 30 seconds)
  val map3 = Await.result(map3f, 30 seconds)
  val map4 = Await.result(map4f, 30 seconds)
  val map5 = Await.result(map5f, 30 seconds)
  val map6 = Await.result(map6f, 30 seconds)
  val map7 = Await.result(map7f, 30 seconds)
  val map8 = Await.result(map8f, 30 seconds)
  val map9 = Await.result(map9f, 30 seconds)
  val map10 = Await.result(map10f, 30 seconds)
  val map11 = Await.result(map11f, 30 seconds)
  val map12 = Await.result(map12f, 30 seconds)
  val map13 = Await.result(map13f, 30 seconds)

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

  val ugd01 = Await.result(ugd01f, 30 seconds)
  val ugd02 = Await.result(ugd02f, 30 seconds)
  val ugd03 = Await.result(ugd03f, 30 seconds)
  val ugd04 = Await.result(ugd04f, 30 seconds)
  val ugd05 = Await.result(ugd05f, 30 seconds)
  val ugd06 = Await.result(ugd06f, 30 seconds)

  Projectiles(ugd01)
  Projectiles(ugd02)
  Projectiles(ugd03)
  Projectiles(ugd04)
  Projectiles(ugd05)
  Projectiles(ugd06)

  val map96 = Await.result(map96f, 30 seconds)
  val map97 = Await.result(map97f, 30 seconds)
  val map98 = Await.result(map98f, 30 seconds)
  val map99 = Await.result(map99f, 30 seconds)

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
