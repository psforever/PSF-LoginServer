package net.psforever.zones

import net.psforever.objects.LocalProjectile
import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.zones.ZoneMap
import net.psforever.zones.zonemaps._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Maps {
  import scala.concurrent.ExecutionContext.Implicits.global
  /*

  val maps = Await.result(
    Seq(
      Future { Map01.zoneMap },
      Future { ("z02", Map02.zoneMap) },
      Future { ("z03", Map03.zoneMap) },
      Future { ("z04", Map04.zoneMap) },
      Future { ("z05", Map05.zoneMap) },
      Future { ("z06", Map06.zoneMap) },
      Future { ("z07", Map07.zoneMap) },
      Future { ("z08", Map08.zoneMap) },
      Future { ("z09", Map09.zoneMap) },
      Future { ("z10", Map10.zoneMap) },
      Future { ("home1", Map11.zoneMap) },
      Future { ("home2", Map12.zoneMap) },
      Future { ("home3", Map13.zoneMap) },
      Future { new ZoneMap("map14", ZoneMapType.VirtualReality) { projectiles(this) } },
      Future { new ZoneMap("map15", ZoneMapType.VirtualReality) { projectiles(this) } },
      Future { new ZoneMap("map16", ZoneMapType.VirtualReality) { projectiles(this) } },
      Future { Ugd01.zoneMap },
      Future { Ugd02.zoneMap },
      Future { Ugd03.zoneMap },
      Future { Ugd04.zoneMap },
      Future { Ugd05.zoneMap },
      Future { Ugd06.zoneMap },
      Future { Map96.zoneMap },
      Future { Map97.zoneMap },
      Future { Map98.zoneMap },
      Future { Map99.zoneMap }
    ).flatMap {
      case (_, zonemap) =>
        (Projectile.BaseUID until Projectile.RangeUID) foreach {
          zonemap.LocalObject(_, LocalProjectile.Constructor)
        }
        map
    },
    30 seconds
  )
   */

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
