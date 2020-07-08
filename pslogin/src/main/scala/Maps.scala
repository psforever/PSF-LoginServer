package net.psforever.pslogin

import net.psforever.objects.LocalProjectile
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.zones.ZoneMap
import zonemaps._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Maps {
  import scala.concurrent.ExecutionContext.Implicits.global

  val map01 = InitZoneMap(Future { Map01.ZoneMap })
  val map02 = InitZoneMap(Future { Map02.ZoneMap })
  val map03 = InitZoneMap(Future { Map03.ZoneMap })
  val map04 = InitZoneMap(Future { Map04.ZoneMap })
  val map05 = InitZoneMap(Future { Map05.ZoneMap })
  val map06 = InitZoneMap(Future { Map06.ZoneMap })
  val map07 = InitZoneMap(Future { Map07.ZoneMap })
  val map08 = InitZoneMap(Future { Map08.ZoneMap })
  val map09 = InitZoneMap(Future { Map09.ZoneMap })
  val map10 = InitZoneMap(Future { Map10.ZoneMap })
  val map11 = InitZoneMap(Future { Map11.ZoneMap })
  val map12 = InitZoneMap(Future { Map12.ZoneMap })
  val map13 = InitZoneMap(Future { Map13.ZoneMap })

  val map14 = new ZoneMap("map14") {
    Projectiles(this)
  }

  val map15 = new ZoneMap("map15") {
    Projectiles(this)
  }

  val map16 = new ZoneMap("map16") {
    Projectiles(this)
  }

  val ugd01 = InitZoneMap(Future { Ugd01.ZoneMap })
  val ugd02 = InitZoneMap(Future { Ugd02.ZoneMap })
  val ugd03 = InitZoneMap(Future { Ugd03.ZoneMap })
  val ugd04 = InitZoneMap(Future { Ugd04.ZoneMap })
  val ugd05 = InitZoneMap(Future { Ugd05.ZoneMap })
  val ugd06 = InitZoneMap(Future { Ugd06.ZoneMap })

  val map96 = InitZoneMap(Future { Map96.ZoneMap })
  val map97 = InitZoneMap(Future { Map97.ZoneMap })
  val map98 = InitZoneMap(Future { Map98.ZoneMap })
  val map99 = InitZoneMap(Future { Map99.ZoneMap })

  def Projectiles(zmap: ZoneMap): Unit = {
    (Projectile.BaseUID until Projectile.RangeUID) foreach {
      zmap.LocalObject(_, LocalProjectile.Constructor)
    }
  }

  def InitZoneMap(future: Future[ZoneMap]): Future[ZoneMap] = {
    future onComplete {
      case Success(x) => Projectiles(x)
      case Failure(_) => throw new RuntimeException("Maps: failure when setting up map") //should not fail?
    }
    future
  }
}
