// Copyright (c) 2025 PSForever
package net.psforever.actors.api

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.definition.VehicleDefinition

/**
  * One weapon's share of the kills.
  *
  * Carries the object id and no name: the portal already resolves ids to the client's own display
  * names, taken from `game_objects.adb` and `english.str`, which are better names than anything this
  * side has. Naming it here as well would be a second source to keep in step with the first.
  */
case class WeaponKills(weaponId: Int, kills: Int)

/** One vehicle's share of the kills, summed over every weapon it mounts. */
case class VehicleKills(vehicle: String, name: String, kills: Int, weapons: Int)

/**
  * Which vehicle a weapon belongs to, so kills can be credited to the thing that carried the gun.
  *
  * ==Why this mapping has to exist==
  * `killactivity` records the weapon that made the kill and nothing about what the killer was riding
  * in. That is enough for "top guns" and not enough for "top vehicles": a Liberator kill is recorded
  * against `cannon_liberator_bomb`, a Magrider's against `particle_beam_magrider`, and no column
  * anywhere says those belong to a Liberator or a Magrider. The vehicles themselves do know -- every
  * `VehicleDefinition` carries the `ToolDefinition`s it mounts -- so the map is built by asking each
  * vehicle what it is armed with and inverting the answer.
  *
  * Built from the definitions rather than written out by hand, so a vehicle whose armament changes
  * stays correctly attributed without anybody remembering this file exists. What DOES have to be
  * remembered is the list below: there is no registry of every `VehicleDefinition`, so the vehicles
  * to ask are named explicitly, the same way `CmdPlaceVehicle.Placeable` names them.
  *
  * ==What this cannot tell you==
  * A weapon that a vehicle mounts AND a player can carry on foot would be credited to the vehicle.
  * In practice the two sets do not overlap -- vehicle armaments are their own object classes -- but
  * it is worth knowing that the classification is by armament, not by how the kill actually happened.
  */
object TopWeapons {

  /**
    * Every vehicle whose kills should be attributed to it.
    *
    * Ordered as the game groups them: ground assault, ground support, air, then the battleframes.
    * A vehicle missing from here does not break anything -- its weapons simply fall through to the
    * gun leaderboard instead, which is visible rather than silent.
    */
  private val Vehicles: List[(String, VehicleDefinition)] = List(
    "Lightning"              -> GlobalDefinitions.lightning,
    "Prowler"                -> GlobalDefinitions.prowler,
    "Vanguard"               -> GlobalDefinitions.vanguard,
    "Magrider"               -> GlobalDefinitions.magrider,
    "Harasser"               -> GlobalDefinitions.two_man_assault_buggy,
    "Enforcer"               -> GlobalDefinitions.twomanheavybuggy,
    "Marauder"               -> GlobalDefinitions.threemanheavybuggy,
    "Thresher"               -> GlobalDefinitions.twomanhoverbuggy,
    "Basilisk"               -> GlobalDefinitions.quadassault,
    "Wraith"                 -> GlobalDefinitions.quadstealth,
    "Skyguard"               -> GlobalDefinitions.skyguard,
    "Switchblade"            -> GlobalDefinitions.switchblade,
    "Flail"                  -> GlobalDefinitions.flail,
    "Deliverer"              -> GlobalDefinitions.mediumtransport,
    "Thunderer"              -> GlobalDefinitions.thunderer,
    "Raider"                 -> GlobalDefinitions.battlewagon,
    "Aurora"                 -> GlobalDefinitions.aurora,
    "Router"                 -> GlobalDefinitions.router,
    "Mosquito"               -> GlobalDefinitions.mosquito,
    "Reaver"                 -> GlobalDefinitions.lightgunship,
    "Liberator"              -> GlobalDefinitions.liberator,
    "Vulture"                -> GlobalDefinitions.vulture,
    "Wasp"                   -> GlobalDefinitions.wasp,
    "Galaxy"                 -> GlobalDefinitions.dropship,
    "Galaxy Gunship"         -> GlobalDefinitions.galaxy_gunship,
    "Lodestar"               -> GlobalDefinitions.lodestar,
    "Phantasm"               -> GlobalDefinitions.phantasm,
    "Aphelion (Flight)"      -> GlobalDefinitions.aphelion_flight,
    "Aphelion (Gunner)"      -> GlobalDefinitions.aphelion_gunner,
    "Colossus (Flight)"      -> GlobalDefinitions.colossus_flight,
    "Colossus (Gunner)"      -> GlobalDefinitions.colossus_gunner,
    "Peregrine (Flight)"     -> GlobalDefinitions.peregrine_flight,
    "Peregrine (Gunner)"     -> GlobalDefinitions.peregrine_gunner
  )

  /**
    * weapon object id -> the vehicle that mounts it.
    *
    * Where two vehicles share a weapon -- and several do, the Basilisk's chaingun among them -- the
    * first in the list above wins. Splitting a shared weapon's kills between them would need to know
    * what the killer was actually driving, which the kill record does not say, so a stable and
    * documented choice beats an invented one.
    */
  lazy val weaponToVehicle: Map[Int, (String, String)] = {
    val pairs = for {
      (label, definition) <- Vehicles
      tool                <- definition.Weapons.values
    } yield tool.ObjectId -> (definition.Name, label)
    // `toMap` keeps the LAST binding for a duplicate key, so reverse first to keep the first.
    pairs.reverse.toMap
  }

  /** How many distinct weapons each vehicle contributes, for the UI to say what it is summing. */
  lazy val weaponCountByVehicle: Map[String, Int] =
    weaponToVehicle.values.groupBy(_._1).map { case (k, v) => k -> v.size }

  /**
    * Split kills-per-weapon into a gun board and a vehicle board.
    *
    * @param rows  weapon object id -> kills, as counted from `killactivity`
    * @param limit how many rows each board keeps
    */
  def split(rows: Seq[(Int, Int)], limit: Int): (Seq[WeaponKills], Seq[VehicleKills]) = {
    val (mounted, handheld) = rows.partition { case (id, _) => weaponToVehicle.contains(id) }

    val guns = handheld
      .sortBy { case (_, kills) => -kills }
      .take(limit)
      .map { case (id, kills) => WeaponKills(id, kills) }

    val vehicles = mounted
      .groupBy { case (id, _) => weaponToVehicle(id) }
      .map {
        case ((internal, label), group) =>
          VehicleKills(internal, label, group.map(_._2).sum, weaponCountByVehicle.getOrElse(internal, 0))
      }
      .toSeq
      .sortBy(-_.kills)
      .take(limit)

    (guns, vehicles)
  }
}
