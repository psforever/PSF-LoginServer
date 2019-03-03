// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.terminals

import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.Vehicle
import net.psforever.objects.loadouts.VehicleLoadout

object VehicleTerminalDefinition {
  import net.psforever.objects.GlobalDefinitions._
  /**
    * A `Map` of operations for producing a ground-based `Vehicle`.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  val groundVehicles : Map[String, () => Vehicle] = Map(
    "quadassault" -> MakeVehicle(quadassault),
    "fury" -> MakeVehicle(fury),
    "quadstealth" -> MakeVehicle(quadstealth),
    "ant" -> MakeVehicle(ant),
    "ams" -> MakeVehicle(ams),
    "mediumtransport" -> MakeVehicle(mediumtransport),
    "two_man_assault_buggy" -> MakeVehicle(two_man_assault_buggy),
    "skyguard" -> MakeVehicle(skyguard),
    "lightning" -> MakeVehicle(lightning),
    "threemanheavybuggy" -> MakeVehicle(threemanheavybuggy),
    "battlewagon" -> MakeVehicle(battlewagon),
    "apc_tr" -> MakeVehicle(apc_tr),
    "prowler" -> MakeVehicle(prowler),
    "twomanheavybuggy" -> MakeVehicle(twomanheavybuggy),
    "thunderer" -> MakeVehicle(thunderer),
    "apc_nc" -> MakeVehicle(apc_nc),
    "vanguard" -> MakeVehicle(vanguard),
    "twomanhoverbuggy" -> MakeVehicle(twomanhoverbuggy),
    "aurora" -> MakeVehicle(aurora),
    "apc_vs" -> MakeVehicle(apc_vs),
    "magrider" -> MakeVehicle(magrider),
    "flail" -> MakeVehicle(flail),
    "switchblade" -> MakeVehicle(switchblade),
    "router" -> MakeVehicle(router)
  )

  /**
    * A `Map` of operations for producing most flight-based `Vehicle`.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  val flight1Vehicles : Map[String, ()=>Vehicle] = Map(
    "mosquito" -> MakeVehicle(mosquito),
    "lightgunship" -> MakeVehicle(lightgunship),
    "wasp" -> MakeVehicle(wasp),
    "phantasm" -> MakeVehicle(phantasm),
    "vulture" -> MakeVehicle(vulture),
    "liberator" -> MakeVehicle(liberator)
  )

  /**
    * A `Map` of operations for producing a flight-based `Vehicle` specific to the dropship terminal.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  val flight2Vehicles : Map[String, ()=>Vehicle] = Map(
    "dropship" -> MakeVehicle(dropship),
    "galaxy_gunship" -> MakeVehicle(galaxy_gunship),
    "lodestar" -> MakeVehicle(lodestar)
  )

  /**
    * A `Map` of operations for producing a ground-based `Vehicle` specific to the bfr terminal.
    * key - an identification string sent by the client
    * value - a curried function that builds the object
    */
  val bfrVehicles : Map[String, ()=>Vehicle] = Map(
    //    "colossus_gunner" -> (()=>Unit),
    //    "colossus_flight" -> (()=>Unit),
    //    "peregrine_gunner" -> (()=>Unit),
    //    "peregrine_flight" -> (()=>Unit),
    //    "aphelion_gunner" -> (()=>Unit),
    //    "aphelion_flight" -> (()=>Unit)
  )

  import net.psforever.objects.loadouts.{Loadout => _Loadout} //distinguish from Terminal.Loadout message
  import _Loadout._
  /**
    * A `Map` of the default contents of a `Vehicle` inventory, called the trunk.
    * key - an identification string sent by the client (for the vehicle)
    * value - a curried function that builds the object
    */
  val trunk : Map[String, _Loadout] = {
    val ammo_12mm = ShorthandAmmoBox(bullet_12mm, bullet_12mm.Capacity)
    val ammo_15mm = ShorthandAmmoBox(bullet_15mm, bullet_15mm.Capacity)
    val ammo_25mm = ShorthandAmmoBox(bullet_25mm, bullet_25mm.Capacity)
    val ammo_35mm = ShorthandAmmoBox(bullet_35mm, bullet_35mm.Capacity)
    val ammo_20mm = ShorthandAmmoBox(bullet_20mm, bullet_20mm.Capacity)
    val ammo_75mm = ShorthandAmmoBox(bullet_75mm, bullet_75mm.Capacity)
    val ammo_mortar = ShorthandAmmoBox(heavy_grenade_mortar, heavy_grenade_mortar.Capacity)
    val ammo_flux = ShorthandAmmoBox(flux_cannon_thresher_battery, flux_cannon_thresher_battery.Capacity)
    val ammo_bomb = ShorthandAmmoBox(liberator_bomb, liberator_bomb.Capacity)
    Map(
      //"quadstealth" -> VehicleLoadout("default_quadstealth", List(), List(), quadstealth),
      "quadassault" -> VehicleLoadout("default_quadassault", List(),
        List(
          SimplifiedEntry(ammo_12mm, 30),
          SimplifiedEntry(ammo_12mm, 34),
          SimplifiedEntry(ammo_12mm, 74),
          SimplifiedEntry(ammo_12mm, 78)
        ),
        quadassault
      ),
      {
        val ammo = ShorthandAmmoBox(hellfire_ammo, hellfire_ammo.Capacity)
        "fury" -> VehicleLoadout("default_fury", List(),
          List(
            SimplifiedEntry(ammo, 30),
            SimplifiedEntry(ammo, 34),
            SimplifiedEntry(ammo, 74),
            SimplifiedEntry(ammo, 78)
          ),
          fury
        )
      },
      //"ant" -> VehicleLoadout("default_ant", List(), List(), ant),
      //"ams" -> VehicleLoadout("default_ams", List(), List(), ams),
      "two_man_assault_buggy" -> VehicleLoadout("default_two_man_assault_buggy", List(),
        List(
          SimplifiedEntry(ammo_12mm, 30),
          SimplifiedEntry(ammo_12mm, 34),
          SimplifiedEntry(ammo_12mm, 38),
          SimplifiedEntry(ammo_12mm, 90),
          SimplifiedEntry(ammo_12mm, 94),
          SimplifiedEntry(ammo_12mm, 98)
        ),
        two_man_assault_buggy
      ),
      {
        val ammo = ShorthandAmmoBox(skyguard_flak_cannon_ammo, skyguard_flak_cannon_ammo.Capacity)
        "skyguard" -> VehicleLoadout("default_skyguard", List(),
          List(
            SimplifiedEntry(ammo_12mm, 30),
            SimplifiedEntry(ammo_12mm, 34),
            SimplifiedEntry(ammo_12mm, 38),
            SimplifiedEntry(ammo, 90),
            SimplifiedEntry(ammo, 94),
            SimplifiedEntry(ammo, 98)
          ),
          skyguard
        )
      },
      "threemanheavybuggy" -> VehicleLoadout("default_threemanheavybuggy", List(),
        List(
          SimplifiedEntry(ammo_12mm, 30),
          SimplifiedEntry(ammo_12mm, 34),
          SimplifiedEntry(ammo_12mm, 38),
          SimplifiedEntry(ammo_mortar, 90),
          SimplifiedEntry(ammo_mortar, 94),
          SimplifiedEntry(ammo_mortar, 98)
        ),
        threemanheavybuggy
      ),
      {
        val ammo = ShorthandAmmoBox(firebird_missile, firebird_missile.Capacity)
        "twomanheavybuggy" -> VehicleLoadout("default_twomanheavybuggy", List(),
          List(
            SimplifiedEntry(ammo, 30),
            SimplifiedEntry(ammo, 34),
            SimplifiedEntry(ammo, 38),
            SimplifiedEntry(ammo, 90),
            SimplifiedEntry(ammo, 94),
            SimplifiedEntry(ammo, 98)
          ),
          twomanheavybuggy
        )
      },
      "twomanhoverbuggy" -> VehicleLoadout("default_twomanhoverbuggy", List(),
        List(
          SimplifiedEntry(ammo_flux, 30),
          SimplifiedEntry(ammo_flux, 34),
          SimplifiedEntry(ammo_flux, 38),
          SimplifiedEntry(ammo_flux, 90),
          SimplifiedEntry(ammo_flux, 94),
          SimplifiedEntry(ammo_flux, 98)
        ),
        twomanhoverbuggy
      ),
      "mediumtransport" -> VehicleLoadout("default_mediumtransport", List(),
        List(
          SimplifiedEntry(ammo_20mm, 30),
          SimplifiedEntry(ammo_20mm, 34),
          SimplifiedEntry(ammo_20mm, 38),
          SimplifiedEntry(ammo_20mm, 90),
          SimplifiedEntry(ammo_20mm, 94),
          SimplifiedEntry(ammo_20mm, 98),
          SimplifiedEntry(ammo_20mm, 150),
          SimplifiedEntry(ammo_20mm, 154),
          SimplifiedEntry(ammo_20mm, 158)
        ),
        mediumtransport
      ),
      "battlewagon" -> VehicleLoadout("default_battlewagon", List(),
        List(
          SimplifiedEntry(ammo_15mm, 30),
          SimplifiedEntry(ammo_15mm, 34),
          SimplifiedEntry(ammo_15mm, 38),
          SimplifiedEntry(ammo_15mm, 90),
          SimplifiedEntry(ammo_15mm, 94),
          SimplifiedEntry(ammo_15mm, 98),
          SimplifiedEntry(ammo_15mm, 150),
          SimplifiedEntry(ammo_15mm, 154),
          SimplifiedEntry(ammo_15mm, 158)
        ),
        battlewagon
      ),
      {
        val ammo = ShorthandAmmoBox(gauss_cannon_ammo, gauss_cannon_ammo.Capacity)
        "thunderer" -> VehicleLoadout("default_thunderer", List(),
          List(
            SimplifiedEntry(ammo, 30),
            SimplifiedEntry(ammo, 34),
            SimplifiedEntry(ammo, 38),
            SimplifiedEntry(ammo, 90),
            SimplifiedEntry(ammo, 94),
            SimplifiedEntry(ammo, 98),
            SimplifiedEntry(ammo, 150),
            SimplifiedEntry(ammo, 154),
            SimplifiedEntry(ammo, 158)
          ),
          thunderer
        )
      },
      {
        val ammo = ShorthandAmmoBox(fluxpod_ammo, fluxpod_ammo.Capacity)
        "aurora" -> VehicleLoadout("default_aurora", List(),
          List(
            SimplifiedEntry(ammo, 30),
            SimplifiedEntry(ammo, 34),
            SimplifiedEntry(ammo, 38),
            SimplifiedEntry(ammo, 90),
            SimplifiedEntry(ammo, 94),
            SimplifiedEntry(ammo, 98),
            SimplifiedEntry(ammo, 150),
            SimplifiedEntry(ammo, 154),
            SimplifiedEntry(ammo, 158)
          ),
          aurora
        )
      },
      "apc_tr" -> VehicleLoadout("default_apc_tr", List(),
        List(
          SimplifiedEntry(ammo_75mm, 30),
          SimplifiedEntry(ammo_75mm, 34),
          SimplifiedEntry(ammo_75mm, 38),
          SimplifiedEntry(ammo_75mm, 42),
          SimplifiedEntry(ammo_75mm, 46),
          SimplifiedEntry(ammo_75mm, 110),
          SimplifiedEntry(ammo_75mm, 114),
          SimplifiedEntry(ammo_75mm, 118),
          SimplifiedEntry(ammo_75mm, 122),
          SimplifiedEntry(ammo_12mm, 126),
          SimplifiedEntry(ammo_12mm, 190),
          SimplifiedEntry(ammo_12mm, 194),
          SimplifiedEntry(ammo_12mm, 198),
          SimplifiedEntry(ammo_12mm, 202),
          SimplifiedEntry(ammo_15mm, 206),
          SimplifiedEntry(ammo_15mm, 270),
          SimplifiedEntry(ammo_15mm, 274),
          SimplifiedEntry(ammo_15mm, 278),
          SimplifiedEntry(ammo_15mm, 282),
          SimplifiedEntry(ammo_15mm, 286)
        ),
        apc_tr
      ),
      "apc_nc" -> VehicleLoadout("default_apc_nc", List(),
        List(
          SimplifiedEntry(ammo_75mm, 30),
          SimplifiedEntry(ammo_75mm, 34),
          SimplifiedEntry(ammo_75mm, 38),
          SimplifiedEntry(ammo_75mm, 42),
          SimplifiedEntry(ammo_75mm, 46),
          SimplifiedEntry(ammo_75mm, 110),
          SimplifiedEntry(ammo_75mm, 114),
          SimplifiedEntry(ammo_75mm, 118),
          SimplifiedEntry(ammo_75mm, 122),
          SimplifiedEntry(ammo_12mm, 126),
          SimplifiedEntry(ammo_12mm, 190),
          SimplifiedEntry(ammo_12mm, 194),
          SimplifiedEntry(ammo_12mm, 198),
          SimplifiedEntry(ammo_12mm, 202),
          SimplifiedEntry(ammo_20mm, 206),
          SimplifiedEntry(ammo_20mm, 270),
          SimplifiedEntry(ammo_20mm, 274),
          SimplifiedEntry(ammo_20mm, 278),
          SimplifiedEntry(ammo_20mm, 282),
          SimplifiedEntry(ammo_20mm, 286)
        ),
        apc_nc
      ),
      "apc_vs" -> VehicleLoadout("default_apc_vs", List(),
        List(
          SimplifiedEntry(ammo_75mm, 30),
          SimplifiedEntry(ammo_75mm, 34),
          SimplifiedEntry(ammo_75mm, 38),
          SimplifiedEntry(ammo_75mm, 42),
          SimplifiedEntry(ammo_75mm, 46),
          SimplifiedEntry(ammo_75mm, 110),
          SimplifiedEntry(ammo_75mm, 114),
          SimplifiedEntry(ammo_75mm, 118),
          SimplifiedEntry(ammo_75mm, 122),
          SimplifiedEntry(ammo_12mm, 126),
          SimplifiedEntry(ammo_12mm, 190),
          SimplifiedEntry(ammo_12mm, 194),
          SimplifiedEntry(ammo_12mm, 198),
          SimplifiedEntry(ammo_12mm, 202),
          SimplifiedEntry(ammo_flux, 206),
          SimplifiedEntry(ammo_flux, 270),
          SimplifiedEntry(ammo_flux, 274),
          SimplifiedEntry(ammo_flux, 278),
          SimplifiedEntry(ammo_flux, 282),
          SimplifiedEntry(ammo_flux, 286)
        ),
        apc_vs
      ),
      "lightning" -> VehicleLoadout("default_lightning", List(),
        List(
          SimplifiedEntry(ammo_12mm, 30),
          SimplifiedEntry(ammo_12mm, 34),
          SimplifiedEntry(ammo_12mm, 38),
          SimplifiedEntry(ammo_75mm, 90),
          SimplifiedEntry(ammo_75mm, 94),
          SimplifiedEntry(ammo_75mm, 98)
        ),
        lightning
      ),
      {
        val ammo = ShorthandAmmoBox(bullet_105mm, bullet_105mm.Capacity)
        "prowler" -> VehicleLoadout("default_prowler", List(),
          List(
            SimplifiedEntry(ammo_15mm, 30),
            SimplifiedEntry(ammo_15mm, 34),
            SimplifiedEntry(ammo_15mm, 38),
            SimplifiedEntry(ammo, 90),
            SimplifiedEntry(ammo, 94),
            SimplifiedEntry(ammo, 98)
          ),
          prowler
        )
      },
      {
        val ammo = ShorthandAmmoBox(bullet_150mm, bullet_150mm.Capacity)
        "vanguard" -> VehicleLoadout("default_vanguard", List(),
          List(
            SimplifiedEntry(ammo_20mm, 30),
            SimplifiedEntry(ammo_20mm, 34),
            SimplifiedEntry(ammo_20mm, 38),
            SimplifiedEntry(ammo, 90),
            SimplifiedEntry(ammo, 94),
            SimplifiedEntry(ammo, 98)
          ),
          vanguard
        )
      },
      {
        val ammo1 = ShorthandAmmoBox(pulse_battery, pulse_battery.Capacity)
        val ammo2 = ShorthandAmmoBox(heavy_rail_beam_battery, heavy_rail_beam_battery.Capacity)
        "magrider" -> VehicleLoadout("default_magrider", List(),
          List(
            SimplifiedEntry(ammo1, 30),
            SimplifiedEntry(ammo1, 34),
            SimplifiedEntry(ammo1, 38),
            SimplifiedEntry(ammo2, 90),
            SimplifiedEntry(ammo2, 94),
            SimplifiedEntry(ammo2, 98)
          ),
          magrider
        )
      },
      //"flail" -> VehicleLoadout("default_flail", List(), List(), flail),
      //"switchblade" -> VehicleLoadout("default_switchblade", List(), List(), switchblade),
      //"router" -> VehicleLoadout("default_router", List(), List(), router),
      "mosquito" -> VehicleLoadout("default_mosquito", List(),
        List(
          SimplifiedEntry(ammo_12mm, 30),
          SimplifiedEntry(ammo_12mm, 34),
          SimplifiedEntry(ammo_12mm, 74),
          SimplifiedEntry(ammo_12mm, 78)
        ),
        mosquito
      ),
      {
        val ammo = ShorthandAmmoBox(reaver_rocket, reaver_rocket.Capacity)
        "lightgunship" -> VehicleLoadout("default_lightgunship", List(),
          List(
            SimplifiedEntry(ammo, 30),
            SimplifiedEntry(ammo, 34),
            SimplifiedEntry(ammo, 38),
            SimplifiedEntry(ammo, 90),
            SimplifiedEntry(ammo_20mm, 94),
            SimplifiedEntry(ammo_20mm, 98)
          ),
          lightgunship
        )
      },
      {
        val ammo1 = ShorthandAmmoBox(wasp_rocket_ammo, wasp_rocket_ammo.Capacity)
        val ammo2 = ShorthandAmmoBox(wasp_gun_ammo, wasp_gun_ammo.Capacity)
        "wasp" -> VehicleLoadout("default_wasp", List(),
          List(
            SimplifiedEntry(ammo1, 30),
            SimplifiedEntry(ammo1, 34),
            SimplifiedEntry(ammo2, 74),
            SimplifiedEntry(ammo2, 78)
          ),
          wasp
        )
      },
      "liberator" -> VehicleLoadout("default_liberator", List(),
        List(
          SimplifiedEntry(ammo_35mm, 30),
          SimplifiedEntry(ammo_35mm, 34),
          SimplifiedEntry(ammo_25mm, 38),
          SimplifiedEntry(ammo_25mm, 90),
          SimplifiedEntry(ammo_bomb, 94),
          SimplifiedEntry(ammo_bomb, 98),
          SimplifiedEntry(ammo_bomb, 150),
          SimplifiedEntry(ammo_bomb, 154),
          SimplifiedEntry(ammo_bomb, 158)
        ),
        liberator
      ),
      "vulture" -> VehicleLoadout("default_vulture", List(),
        List(
          SimplifiedEntry(ammo_35mm, 30),
          SimplifiedEntry(ammo_35mm, 34),
          SimplifiedEntry(ammo_35mm, 38),
          SimplifiedEntry(ammo_25mm, 42),
          SimplifiedEntry(ammo_25mm, 94),
          SimplifiedEntry(ammo_bomb, 98),
          SimplifiedEntry(ammo_bomb, 102),
          SimplifiedEntry(ammo_bomb, 106)
        ), //TODO confirm
        vulture
      ),
      "dropship" -> VehicleLoadout("default_dropship", List(),
        List(
          SimplifiedEntry(ammo_20mm, 30),
          SimplifiedEntry(ammo_20mm, 34),
          SimplifiedEntry(ammo_20mm, 38),
          SimplifiedEntry(ammo_20mm, 42),
          SimplifiedEntry(ammo_20mm, 94),
          SimplifiedEntry(ammo_20mm, 98),
          SimplifiedEntry(ammo_20mm, 102),
          SimplifiedEntry(ammo_20mm, 106),
          SimplifiedEntry(ammo_20mm, 158),
          SimplifiedEntry(ammo_20mm, 162),
          SimplifiedEntry(ammo_20mm, 166),
          SimplifiedEntry(ammo_20mm, 170)
        ),
        dropship
      ),
      "galaxy_gunship" -> VehicleLoadout("galaxy_gunship", List(),
        List(
          SimplifiedEntry(ammo_35mm, 30),
          SimplifiedEntry(ammo_35mm, 34),
          SimplifiedEntry(ammo_35mm, 38),
          SimplifiedEntry(ammo_35mm, 42),
          SimplifiedEntry(ammo_35mm, 102),
          SimplifiedEntry(ammo_35mm, 106),
          SimplifiedEntry(ammo_35mm, 110),
          SimplifiedEntry(ammo_35mm, 114),
          SimplifiedEntry(ammo_mortar, 174),
          SimplifiedEntry(ammo_mortar, 178),
          SimplifiedEntry(ammo_mortar, 182),
          SimplifiedEntry(ammo_mortar, 186)
        ),
        galaxy_gunship
      )
      //"phantasm" -> VehicleLoadout("default_phantasm", List(), List(), phantasm),
      //"lodestar" -> VehicleLoadout("default_lodestar", List(), List(), lodestar),
    )
  }

  /**
    * Create a new `Vehicle` from provided `VehicleDefinition` objects.
    * @param vdef the `VehicleDefinition` object
    * @return a curried function that, when called, creates the `Vehicle`
    * @see `GlobalDefinitions`
    */
  protected def MakeVehicle(vdef : VehicleDefinition)() : Vehicle = Vehicle(vdef)
}
