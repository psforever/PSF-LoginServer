// Copyright (c) 2024 PSForever
package net.psforever.objects.global

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.ballistics.{AggravatedDamage, AggravatedInfo, AggravatedTiming, ChargeDamage}
import net.psforever.objects.definition.ProjectileDefinition
import net.psforever.objects.definition.converter.{
  LittleBuddyProjectileConverter,
  ProjectileConverter,
  RadiationCloudConverter
}
import net.psforever.objects.equipment.{ArmorSiphonRepairHost, EffectTarget, TargetValidation}
import net.psforever.objects.serverobject.aura.Aura
import net.psforever.objects.vital.base.DamageType
import net.psforever.objects.vital.damage.{RadialDegrade, SameHit, StandardDamageProfile}
import net.psforever.objects.vital.etc.{
  ArmorSiphonMaxDistanceCutoff,
  ExplosionDamagesOnlyAbove,
  InfantryAggravatedRadiation,
  InfantryAggravatedRadiationBurn
}
import net.psforever.objects.vital.projectile._

object GlobalDefinitionsProjectile {
  import GlobalDefinitions._

  /**
   * Initialize `ProjectileDefinition` globals.
   */
  def init(): Unit = {
    init_standard_projectile()
    init_bfr_projectile()
  }

  /**
    * Initialize `ProjectileDefinition` globals for most projectiles.
    */
  private def init_standard_projectile(): Unit = {
    val projectileConverter: ProjectileConverter   = new ProjectileConverter
    val radCloudConverter: RadiationCloudConverter = new RadiationCloudConverter

    no_projectile.Name = "no_projectile"
    no_projectile.DamageRadiusMin = 0f
    ProjectileDefinition.CalculateDerivedFields(no_projectile)
    no_projectile.Modifiers = Nil

    bullet_105mm_projectile.Name = "105mmbullet_projectile"
    bullet_105mm_projectile.Damage0 = 150
    bullet_105mm_projectile.Damage1 = 300
    bullet_105mm_projectile.Damage2 = 300
    bullet_105mm_projectile.Damage3 = 300
    bullet_105mm_projectile.Damage4 = 180
    bullet_105mm_projectile.DamageAtEdge = 0.1f
    bullet_105mm_projectile.DamageRadius = 7f
    bullet_105mm_projectile.ProjectileDamageType = DamageType.Splash
    bullet_105mm_projectile.InitialVelocity = 100
    bullet_105mm_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(bullet_105mm_projectile)

    bullet_12mm_projectile.Name = "12mmbullet_projectile"
    bullet_12mm_projectile.Damage0 = 25
    bullet_12mm_projectile.Damage1 = 10
    bullet_12mm_projectile.Damage2 = 25
    bullet_12mm_projectile.Damage3 = 10
    bullet_12mm_projectile.Damage4 = 7
    bullet_12mm_projectile.ProjectileDamageType = DamageType.Direct
    bullet_12mm_projectile.DegradeDelay = .015f
    bullet_12mm_projectile.DegradeMultiplier = 0.5f
    bullet_12mm_projectile.InitialVelocity = 500
    bullet_12mm_projectile.Lifespan = 0.5f
    bullet_12mm_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(bullet_12mm_projectile)

    bullet_12mm_projectileb.Name = "12mmbullet_projectileb"
    // TODO for later, maybe : set_resource_parent 12mmbullet_projectileb game_objects 12mmbullet_projectile
    bullet_12mm_projectileb.Damage0 = 25
    bullet_12mm_projectileb.Damage1 = 10
    bullet_12mm_projectileb.Damage2 = 25
    bullet_12mm_projectileb.Damage3 = 10
    bullet_12mm_projectileb.Damage4 = 7
    bullet_12mm_projectileb.ProjectileDamageType = DamageType.Direct
    bullet_12mm_projectileb.DegradeDelay = .015f
    bullet_12mm_projectileb.DegradeMultiplier = 0.5f
    bullet_12mm_projectileb.InitialVelocity = 500
    bullet_12mm_projectileb.Lifespan = 0.5f
    bullet_12mm_projectileb.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(bullet_12mm_projectileb)

    bullet_150mm_projectile.Name = "150mmbullet_projectile"
    bullet_150mm_projectile.Damage0 = 150
    bullet_150mm_projectile.Damage1 = 450
    bullet_150mm_projectile.Damage2 = 450
    bullet_150mm_projectile.Damage3 = 450
    bullet_150mm_projectile.Damage4 = 400
    bullet_150mm_projectile.DamageAtEdge = 0.10f
    bullet_150mm_projectile.DamageRadius = 8f
    bullet_150mm_projectile.ProjectileDamageType = DamageType.Splash
    bullet_150mm_projectile.InitialVelocity = 100
    bullet_150mm_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(bullet_150mm_projectile)
    bullet_150mm_projectile.Modifiers = RadialDegrade

    bullet_15mm_apc_projectile.Name = "15mmbullet_apc_projectile"
    // TODO for later, maybe : set_resource_parent 15mmbullet_apc_projectile game_objects 15mmbullet_projectile
    bullet_15mm_apc_projectile.Damage0 = 12
    bullet_15mm_apc_projectile.Damage1 = 20
    bullet_15mm_apc_projectile.Damage2 = 30
    bullet_15mm_apc_projectile.Damage3 = 20
    bullet_15mm_apc_projectile.Damage4 = 16
    bullet_15mm_apc_projectile.ProjectileDamageType = DamageType.Direct
    bullet_15mm_apc_projectile.DegradeDelay = .015f
    bullet_15mm_apc_projectile.DegradeMultiplier = 0.5f
    bullet_15mm_apc_projectile.InitialVelocity = 500
    bullet_15mm_apc_projectile.Lifespan = 0.5f
    ProjectileDefinition.CalculateDerivedFields(bullet_15mm_apc_projectile)

    bullet_15mm_projectile.Name = "15mmbullet_projectile"
    bullet_15mm_projectile.Damage0 = 21
    bullet_15mm_projectile.Damage1 = 18
    bullet_15mm_projectile.Damage2 = 25
    bullet_15mm_projectile.Damage3 = 18
    bullet_15mm_projectile.Damage4 = 11
    bullet_15mm_projectile.ProjectileDamageType = DamageType.Direct
    bullet_15mm_projectile.DegradeDelay = .015f
    bullet_15mm_projectile.DegradeMultiplier = 0.5f
    bullet_15mm_projectile.InitialVelocity = 500
    bullet_15mm_projectile.Lifespan = 0.5f
    ProjectileDefinition.CalculateDerivedFields(bullet_15mm_projectile)

    bullet_20mm_apc_projectile.Name = "20mmbullet_apc_projectile"
    // TODO for later, maybe : set_resource_parent 20mmbullet_apc_projectile game_objects 20mmbullet_projectile
    bullet_20mm_apc_projectile.Damage0 = 24
    bullet_20mm_apc_projectile.Damage1 = 40
    bullet_20mm_apc_projectile.Damage2 = 60
    bullet_20mm_apc_projectile.Damage3 = 40
    bullet_20mm_apc_projectile.Damage4 = 32
    bullet_20mm_apc_projectile.ProjectileDamageType = DamageType.Direct
    bullet_20mm_apc_projectile.DegradeDelay = .015f
    bullet_20mm_apc_projectile.DegradeMultiplier = 0.5f
    bullet_20mm_apc_projectile.InitialVelocity = 500
    bullet_20mm_apc_projectile.Lifespan = 0.5f
    ProjectileDefinition.CalculateDerivedFields(bullet_20mm_apc_projectile)

    bullet_20mm_projectile.Name = "20mmbullet_projectile"
    bullet_20mm_projectile.Damage0 = 20
    bullet_20mm_projectile.Damage1 = 20
    bullet_20mm_projectile.Damage2 = 40
    bullet_20mm_projectile.Damage3 = 20
    bullet_20mm_projectile.Damage4 = 16
    bullet_20mm_projectile.ProjectileDamageType = DamageType.Direct
    bullet_20mm_projectile.DegradeDelay = .015f
    bullet_20mm_projectile.DegradeMultiplier = 0.5f
    bullet_20mm_projectile.InitialVelocity = 500
    bullet_20mm_projectile.Lifespan = 0.5f
    ProjectileDefinition.CalculateDerivedFields(bullet_20mm_projectile)

    bullet_25mm_projectile.Name = "25mmbullet_projectile"
    bullet_25mm_projectile.Damage0 = 25
    bullet_25mm_projectile.Damage1 = 35
    bullet_25mm_projectile.Damage2 = 50
    bullet_25mm_projectile.ProjectileDamageType = DamageType.Direct
    bullet_25mm_projectile.DegradeDelay = .02f
    bullet_25mm_projectile.DegradeMultiplier = 0.5f
    bullet_25mm_projectile.InitialVelocity = 500
    bullet_25mm_projectile.Lifespan = 0.6f
    ProjectileDefinition.CalculateDerivedFields(bullet_25mm_projectile)

    bullet_35mm_projectile.Name = "35mmbullet_projectile"
    bullet_35mm_projectile.Damage0 = 40
    bullet_35mm_projectile.Damage1 = 50
    bullet_35mm_projectile.Damage2 = 60
    bullet_35mm_projectile.ProjectileDamageType = DamageType.Direct
    bullet_35mm_projectile.DegradeDelay = .015f
    bullet_35mm_projectile.DegradeMultiplier = 0.5f
    bullet_35mm_projectile.InitialVelocity = 200
    bullet_35mm_projectile.Lifespan = 1.5f
    ProjectileDefinition.CalculateDerivedFields(bullet_35mm_projectile)

    bullet_75mm_apc_projectile.Name = "75mmbullet_apc_projectile"
    // TODO for later, maybe : set_resource_parent 75mmbullet_apc_projectile game_objects 75mmbullet_projectile
    bullet_75mm_apc_projectile.Damage0 = 85
    bullet_75mm_apc_projectile.Damage1 = 155
    bullet_75mm_apc_projectile.DamageAtEdge = 0.1f
    bullet_75mm_apc_projectile.DamageRadius = 5f
    bullet_75mm_apc_projectile.ProjectileDamageType = DamageType.Splash
    bullet_75mm_apc_projectile.InitialVelocity = 100
    bullet_75mm_apc_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(bullet_75mm_apc_projectile)
    bullet_75mm_apc_projectile.Modifiers = RadialDegrade

    bullet_75mm_projectile.Name = "75mmbullet_projectile"
    bullet_75mm_projectile.Damage0 = 75
    bullet_75mm_projectile.Damage1 = 125
    bullet_75mm_projectile.DamageAtEdge = 0.1f
    bullet_75mm_projectile.DamageRadius = 5f
    bullet_75mm_projectile.ProjectileDamageType = DamageType.Splash
    bullet_75mm_projectile.InitialVelocity = 100
    bullet_75mm_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(bullet_75mm_projectile)
    bullet_75mm_projectile.Modifiers = RadialDegrade

    bullet_9mm_AP_projectile.Name = "9mmbullet_AP_projectile"
    // TODO for later, maybe : set_resource_parent 9mmbullet_AP_projectile game_objects 9mmbullet_projectile
    bullet_9mm_AP_projectile.Damage0 = 10
    bullet_9mm_AP_projectile.Damage1 = 15
    bullet_9mm_AP_projectile.ProjectileDamageType = DamageType.Direct
    bullet_9mm_AP_projectile.DegradeDelay = 0.15f
    bullet_9mm_AP_projectile.DegradeMultiplier = 0.25f
    bullet_9mm_AP_projectile.InitialVelocity = 500
    bullet_9mm_AP_projectile.Lifespan = 0.4f
    bullet_9mm_AP_projectile.UseDamage1Subtract = true
    bullet_9mm_AP_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(bullet_9mm_AP_projectile)

    bullet_9mm_projectile.Name = "9mmbullet_projectile"
    bullet_9mm_projectile.Damage0 = 18
    bullet_9mm_projectile.Damage1 = 10
    bullet_9mm_projectile.ProjectileDamageType = DamageType.Direct
    bullet_9mm_projectile.DegradeDelay = 0.15f
    bullet_9mm_projectile.DegradeMultiplier = 0.25f
    bullet_9mm_projectile.InitialVelocity = 500
    bullet_9mm_projectile.Lifespan = 0.4f
    bullet_9mm_projectile.UseDamage1Subtract = true
    bullet_9mm_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(bullet_9mm_projectile)

    anniversary_projectilea.Name = "anniversary_projectilea"
    anniversary_projectilea.Damage0 = 30
    anniversary_projectilea.Damage1 = 15
    anniversary_projectilea.Damage2 = 15
    anniversary_projectilea.Damage3 = 45
    anniversary_projectilea.Damage4 = 15
    anniversary_projectilea.ProjectileDamageType = DamageType.Direct
    anniversary_projectilea.DegradeDelay = 0.04f
    anniversary_projectilea.DegradeMultiplier = 0.2f
    anniversary_projectilea.InitialVelocity = 500
    anniversary_projectilea.Lifespan = 0.5f
    anniversary_projectilea.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(anniversary_projectilea)

    anniversary_projectileb.Name = "anniversary_projectileb"
    // TODO for later, maybe : set_resource_parent anniversary_projectileb game_objects anniversary_projectilea
    anniversary_projectileb.Damage0 = 30
    anniversary_projectileb.Damage1 = 15
    anniversary_projectileb.Damage2 = 15
    anniversary_projectileb.Damage3 = 45
    anniversary_projectileb.Damage4 = 15
    anniversary_projectileb.ProjectileDamageType = DamageType.Direct
    anniversary_projectileb.DegradeDelay = 0.04f
    anniversary_projectileb.DegradeMultiplier = 0.2f
    anniversary_projectileb.InitialVelocity = 500
    anniversary_projectileb.Lifespan = 0.5f
    anniversary_projectileb.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(anniversary_projectileb)

    bolt_projectile.Name = "bolt_projectile"
    bolt_projectile.Damage0 = 100
    bolt_projectile.Damage1 = 50
    bolt_projectile.Damage2 = 50
    bolt_projectile.Damage3 = 50
    bolt_projectile.Damage4 = 75
    bolt_projectile.ProjectileDamageType = DamageType.Splash
    bolt_projectile.InitialVelocity = 500
    bolt_projectile.Lifespan = 1.0f
    bolt_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(bolt_projectile)
    //TODO bolt_projectile.Modifiers = DistanceDegrade?

    burster_projectile.Name = "burster_projectile"
    burster_projectile.Damage0 = 18
    burster_projectile.Damage1 = 25
    burster_projectile.Damage2 = 50
    burster_projectile.DamageAtEdge = 0.25f
    burster_projectile.DamageRadius = 10f
    burster_projectile.ProjectileDamageType = DamageType.Direct
    burster_projectile.ProjectileDamageTypeSecondary = DamageType.Splash
    burster_projectile.InitialVelocity = 125
    burster_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(burster_projectile)
    burster_projectile.Modifiers = List(
      //FlakHit,
      FlakBurst,
      MaxDistanceCutoff
    )

    chainblade_projectile.Name = "chainblade_projectile"
    // TODO for later, maybe : set_resource_parent chainblade_projectile game_objects melee_ammo_projectile
    chainblade_projectile.Damage0 = 50
    chainblade_projectile.Damage1 = 0
    chainblade_projectile.ProjectileDamageType = DamageType.Direct
    chainblade_projectile.InitialVelocity = 100
    chainblade_projectile.Lifespan = .03f //.02f
    ProjectileDefinition.CalculateDerivedFields(chainblade_projectile)
    chainblade_projectile.Modifiers = List(MeleeBoosted, MaxDistanceCutoff)

    comet_projectile.Name = "comet_projectile"
    comet_projectile.Damage0 = 15
    comet_projectile.Damage1 = 60
    comet_projectile.Damage2 = 60
    comet_projectile.Damage3 = 38
    comet_projectile.Damage4 = 64
    comet_projectile.Acceleration = 10
    comet_projectile.AccelerationUntil = 2f
    comet_projectile.DamageAtEdge = 0.45f
    comet_projectile.DamageRadius = 1.0f
    comet_projectile.ProjectileDamageType = DamageType.Aggravated
    comet_projectile.Aggravated = AggravatedDamage(
      AggravatedInfo(DamageType.Direct, 0.25f, 500), //originally, .2
      Aura.Comet,
      AggravatedTiming(2000, 3),
      10f,
      List(
        TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player),
        TargetValidation(EffectTarget.Category.Vehicle, EffectTarget.Validation.Vehicle),
        TargetValidation(EffectTarget.Category.Turret, EffectTarget.Validation.Turret)
      )
    )
    comet_projectile.InitialVelocity = 80
    comet_projectile.Lifespan = 3.1f
    ProjectileDefinition.CalculateDerivedFields(comet_projectile)
    comet_projectile.Modifiers = List(
      CometAggravated,
      CometAggravatedBurn
    )

    dualcycler_projectile.Name = "dualcycler_projectile"
    dualcycler_projectile.Damage0 = 18
    dualcycler_projectile.Damage1 = 10
    dualcycler_projectile.ProjectileDamageType = DamageType.Direct
    dualcycler_projectile.DegradeDelay = .025f
    dualcycler_projectile.DegradeMultiplier = .5f
    dualcycler_projectile.InitialVelocity = 500
    dualcycler_projectile.Lifespan = 0.5f
    dualcycler_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(dualcycler_projectile)

    dynomite_projectile.Name = "dynomite_projectile"
    // TODO for later, maybe : set_resource_parent dynomite_projectile game_objects frag_grenade_projectile_enh
    dynomite_projectile.Damage0 = 75
    dynomite_projectile.Damage1 = 175
    dynomite_projectile.DamageAtEdge = 0.1f
    dynomite_projectile.DamageRadius = 10f
    dynomite_projectile.GrenadeProjectile = true
    dynomite_projectile.ProjectileDamageType = DamageType.Splash
    dynomite_projectile.InitialVelocity = 30
    dynomite_projectile.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(dynomite_projectile)
    dynomite_projectile.Modifiers = RadialDegrade

    energy_cell_projectile.Name = "energy_cell_projectile"
    energy_cell_projectile.Damage0 = 18
    energy_cell_projectile.Damage1 = 10
    energy_cell_projectile.ProjectileDamageType = DamageType.Direct
    energy_cell_projectile.DegradeDelay = 0.05f
    energy_cell_projectile.DegradeMultiplier = 0.4f
    energy_cell_projectile.InitialVelocity = 500
    energy_cell_projectile.Lifespan = .4f
    energy_cell_projectile.UseDamage1Subtract = true
    energy_cell_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(energy_cell_projectile)

    energy_gun_nc_projectile.Name = "energy_gun_nc_projectile"
    energy_gun_nc_projectile.Damage0 = 10
    energy_gun_nc_projectile.Damage1 = 13
    energy_gun_nc_projectile.ProjectileDamageType = DamageType.Direct
    energy_gun_nc_projectile.InitialVelocity = 500
    energy_gun_nc_projectile.Lifespan = 0.5f
    energy_gun_nc_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(energy_gun_nc_projectile)

    energy_gun_tr_projectile.Name = "energy_gun_tr_projectile"
    energy_gun_tr_projectile.Damage0 = 14
    energy_gun_tr_projectile.Damage1 = 18
    energy_gun_tr_projectile.ProjectileDamageType = DamageType.Direct
    energy_gun_tr_projectile.DegradeDelay = .025f
    energy_gun_tr_projectile.DegradeMultiplier = .5f
    energy_gun_tr_projectile.InitialVelocity = 500
    energy_gun_tr_projectile.Lifespan = 0.5f
    energy_gun_tr_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(energy_gun_tr_projectile)

    energy_gun_vs_projectile.Name = "energy_gun_vs_projectile"
    energy_gun_vs_projectile.Damage0 = 25
    energy_gun_vs_projectile.Damage1 = 35
    energy_gun_vs_projectile.ProjectileDamageType = DamageType.Direct
    energy_gun_vs_projectile.DegradeDelay = 0.045f
    energy_gun_vs_projectile.DegradeMultiplier = 0.5f
    energy_gun_vs_projectile.InitialVelocity = 500
    energy_gun_vs_projectile.Lifespan = .5f
    energy_gun_vs_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(energy_gun_vs_projectile)

    enhanced_energy_cell_projectile.Name = "enhanced_energy_cell_projectile"
    // TODO for later, maybe : set_resource_parent enhanced_energy_cell_projectile game_objects energy_cell_projectile
    enhanced_energy_cell_projectile.Damage0 = 7
    enhanced_energy_cell_projectile.Damage1 = 15
    enhanced_energy_cell_projectile.ProjectileDamageType = DamageType.Direct
    enhanced_energy_cell_projectile.DegradeDelay = 0.05f
    enhanced_energy_cell_projectile.DegradeMultiplier = 0.4f
    enhanced_energy_cell_projectile.InitialVelocity = 500
    enhanced_energy_cell_projectile.Lifespan = .4f
    enhanced_energy_cell_projectile.UseDamage1Subtract = true
    enhanced_energy_cell_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(enhanced_energy_cell_projectile)

    enhanced_quasar_projectile.Name = "enhanced_quasar_projectile"
    // TODO for later, maybe : set_resource_parent enhanced_quasar_projectile game_objects quasar_projectile
    enhanced_quasar_projectile.Damage1 = 12
    enhanced_quasar_projectile.Damage0 = 10
    enhanced_quasar_projectile.ProjectileDamageType = DamageType.Direct
    enhanced_quasar_projectile.DegradeDelay = 0.045f
    enhanced_quasar_projectile.DegradeMultiplier = 0.5f
    enhanced_quasar_projectile.InitialVelocity = 500
    enhanced_quasar_projectile.Lifespan = .4f
    enhanced_quasar_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(enhanced_quasar_projectile)

    falcon_projectile.Name = "falcon_projectile"
    falcon_projectile.Damage0 = 35
    falcon_projectile.Damage1 = 132
    falcon_projectile.Damage2 = 132
    falcon_projectile.Damage3 = 83
    falcon_projectile.Damage4 = 144
    falcon_projectile.Acceleration = 10
    falcon_projectile.AccelerationUntil = 2f
    falcon_projectile.DamageAtEdge = 0.2f
    falcon_projectile.DamageRadius = 1f
    falcon_projectile.ProjectileDamageType = DamageType.Splash
    falcon_projectile.InitialVelocity = 120
    falcon_projectile.Lifespan = 2.1f
    ProjectileDefinition.CalculateDerivedFields(falcon_projectile)
    falcon_projectile.Modifiers = RadialDegrade

    firebird_missile_projectile.Name = "firebird_missile_projectile"
    firebird_missile_projectile.Damage0 = 125
    firebird_missile_projectile.Damage1 = 220
    firebird_missile_projectile.Damage2 = 220
    firebird_missile_projectile.Damage3 = 200
    firebird_missile_projectile.Damage4 = 181
    firebird_missile_projectile.Acceleration = 20
    firebird_missile_projectile.AccelerationUntil = 2f
    firebird_missile_projectile.DamageAtEdge = .1f
    firebird_missile_projectile.DamageRadius = 5f
    firebird_missile_projectile.ProjectileDamageType = DamageType.Splash
    firebird_missile_projectile.InitialVelocity = 75
    firebird_missile_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(firebird_missile_projectile)
    firebird_missile_projectile.Modifiers = RadialDegrade

    flail_projectile.Name = "flail_projectile"
    flail_projectile.Damage0 = 75
    flail_projectile.Damage1 = 200
    flail_projectile.Damage2 = 200
    flail_projectile.Damage3 = 200
    flail_projectile.Damage4 = 300
    flail_projectile.DamageAtEdge = 0.1f
    flail_projectile.DamageRadius = 15f
    flail_projectile.ProjectileDamageType = DamageType.Splash
    flail_projectile.DegradeDelay = 1.5f
    //a DegradeDelay of 1.5s equals a DistanceNoDegrade of 112.5m
    flail_projectile.DegradeMultiplier = 5f
    flail_projectile.InitialVelocity = 75
    flail_projectile.Lifespan = 40f
    ProjectileDefinition.CalculateDerivedFields(flail_projectile)
    flail_projectile.Modifiers = List(
      FlailDistanceDamageBoost,
      RadialDegrade
    )

    flamethrower_fire_cloud.Name = "flamethrower_fire_cloud"
    flamethrower_fire_cloud.Damage0 = 2
    flamethrower_fire_cloud.Damage1 = 0
    flamethrower_fire_cloud.Damage2 = 0
    flamethrower_fire_cloud.Damage3 = 1
    flamethrower_fire_cloud.Damage4 = 0
    flamethrower_fire_cloud.DamageAtEdge = 0.1f
    flamethrower_fire_cloud.DamageRadius = 5f
    flamethrower_fire_cloud.ProjectileDamageType = DamageType.Aggravated
    flamethrower_fire_cloud.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, -1.5f, 500), AggravatedInfo(DamageType.Splash, -4.0f, 500)),
      Aura.Fire,
      AggravatedTiming(5000, 10),
      2.5f,
      false,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    flamethrower_fire_cloud.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(flamethrower_fire_cloud)
    flamethrower_fire_cloud.Modifiers = List(
      InfantryAggravatedDirect,
      InfantryAggravatedSplash,
      RadialDegrade,
      FireballAggravatedBurn
    )

    flamethrower_fireball.Name = "flamethrower_fireball"
    flamethrower_fireball.Damage0 = 30
    flamethrower_fireball.Damage1 = 0
    flamethrower_fireball.Damage2 = 0
    flamethrower_fireball.Damage3 = 20
    flamethrower_fireball.Damage4 = 0
    flamethrower_fireball.DamageToHealthOnly = true
    flamethrower_fireball.DamageAtEdge = 0.15f
    flamethrower_fireball.DamageRadius = 5f
    flamethrower_fireball.ProjectileDamageType = DamageType.Aggravated
    flamethrower_fireball.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, 0.9f, 500), AggravatedInfo(DamageType.Splash, 0.9f, 500)),
      Aura.Fire,
      AggravatedTiming(5000, 10),
      0.1f,
      false,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    flamethrower_fireball.InitialVelocity = 15
    flamethrower_fireball.Lifespan = 1.2f
    ProjectileDefinition.CalculateDerivedFields(flamethrower_fireball)
    flamethrower_fireball.Modifiers = List(
      InfantryAggravatedDirect,
      InfantryAggravatedSplash,
      RadialDegrade,
      FireballAggravatedBurn
    )

    flamethrower_projectile.Name = "flamethrower_projectile"
    flamethrower_projectile.Damage0 = 10
    flamethrower_projectile.Damage1 = 0
    flamethrower_projectile.Damage2 = 0
    flamethrower_projectile.Damage3 = 4
    flamethrower_projectile.Damage4 = 0
    flamethrower_projectile.DamageToHealthOnly = true
    flamethrower_projectile.Acceleration = -5
    flamethrower_projectile.AccelerationUntil = 2f
    flamethrower_projectile.ProjectileDamageType = DamageType.Aggravated
    flamethrower_projectile.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, 0.5f, 500)),
      Aura.Fire,
      AggravatedTiming(5000, 10),
      0.5f,
      false,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    flamethrower_projectile.DegradeDelay = 1.0f
    flamethrower_projectile.DegradeMultiplier = 0.5f
    flamethrower_projectile.InitialVelocity = 10
    flamethrower_projectile.Lifespan = 2.0f
    ProjectileDefinition.CalculateDerivedFields(flamethrower_projectile)
    flamethrower_projectile.Modifiers = List(
      InfantryAggravatedDirect,
      FireballAggravatedBurn,
      MaxDistanceCutoff
    )

    flux_cannon_apc_projectile.Name = "flux_cannon_apc_projectile"
    // TODO for later, maybe : set_resource_parent flux_cannon_apc_projectile game_objects flux_cannon_thresher_projectile
    flux_cannon_apc_projectile.Damage0 = 14
    flux_cannon_apc_projectile.Damage1 = 23
    flux_cannon_apc_projectile.Damage2 = 35
    flux_cannon_apc_projectile.Damage3 = 23
    flux_cannon_apc_projectile.Damage4 = 18
    flux_cannon_apc_projectile.DamageAtEdge = .5f
    flux_cannon_apc_projectile.DamageRadius = 4f
    flux_cannon_apc_projectile.ProjectileDamageType = DamageType.Direct
    flux_cannon_apc_projectile.InitialVelocity = 300
    flux_cannon_apc_projectile.Lifespan = 1f
    ProjectileDefinition.CalculateDerivedFields(flux_cannon_apc_projectile)

    flux_cannon_thresher_projectile.Name = "flux_cannon_thresher_projectile"
    flux_cannon_thresher_projectile.Damage0 = 30
    flux_cannon_thresher_projectile.Damage1 = 44
    flux_cannon_thresher_projectile.Damage2 = 44
    flux_cannon_thresher_projectile.Damage3 = 40
    flux_cannon_thresher_projectile.Damage4 = 37
    flux_cannon_thresher_projectile.DamageAtEdge = .5f
    flux_cannon_thresher_projectile.DamageRadius = 4f
    flux_cannon_thresher_projectile.ProjectileDamageType = DamageType.Splash
    flux_cannon_thresher_projectile.InitialVelocity = 75
    flux_cannon_thresher_projectile.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(flux_cannon_thresher_projectile)
    flux_cannon_thresher_projectile.Modifiers = RadialDegrade

    fluxpod_projectile.Name = "fluxpod_projectile"
    fluxpod_projectile.Damage0 = 110
    fluxpod_projectile.Damage1 = 80
    fluxpod_projectile.Damage2 = 125
    fluxpod_projectile.Damage3 = 80
    fluxpod_projectile.Damage4 = 52
    fluxpod_projectile.DamageAtEdge = .3f
    fluxpod_projectile.DamageRadius = 3f
    fluxpod_projectile.ProjectileDamageType = DamageType.Splash
    fluxpod_projectile.InitialVelocity = 80
    fluxpod_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(fluxpod_projectile)
    fluxpod_projectile.Modifiers = RadialDegrade

    forceblade_projectile.Name = "forceblade_projectile"
    // TODO for later, maybe : set_resource_parent forceblade_projectile game_objects melee_ammo_projectile
    forceblade_projectile.Damage0 = 50
    forceblade_projectile.Damage1 = 0
    forceblade_projectile.ProjectileDamageType = DamageType.Direct
    forceblade_projectile.InitialVelocity = 100
    forceblade_projectile.Lifespan = .03f //.02f
    ProjectileDefinition.CalculateDerivedFields(forceblade_projectile)
    forceblade_projectile.Modifiers = List(MeleeBoosted, MaxDistanceCutoff)

    frag_cartridge_projectile.Name = "frag_cartridge_projectile"
    // TODO for later, maybe : set_resource_parent frag_cartridge_projectile game_objects frag_grenade_projectile
    frag_cartridge_projectile.Damage0 = 75
    frag_cartridge_projectile.Damage1 = 100
    frag_cartridge_projectile.DamageAtEdge = 0.1f
    frag_cartridge_projectile.DamageRadius = 7f
    frag_cartridge_projectile.GrenadeProjectile = true
    frag_cartridge_projectile.ProjectileDamageType = DamageType.Splash
    frag_cartridge_projectile.InitialVelocity = 30
    frag_cartridge_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(frag_cartridge_projectile)
    frag_cartridge_projectile.Modifiers = RadialDegrade

    frag_cartridge_projectile_b.Name = "frag_cartridge_projectile_b"
    // TODO for later, maybe : set_resource_parent frag_cartridge_projectile_b game_objects frag_grenade_projectile_enh
    frag_cartridge_projectile_b.Damage0 = 75
    frag_cartridge_projectile_b.Damage1 = 100
    frag_cartridge_projectile_b.DamageAtEdge = 0.1f
    frag_cartridge_projectile_b.DamageRadius = 5f
    frag_cartridge_projectile_b.GrenadeProjectile = true
    frag_cartridge_projectile_b.ProjectileDamageType = DamageType.Splash
    frag_cartridge_projectile_b.InitialVelocity = 30
    frag_cartridge_projectile_b.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(frag_cartridge_projectile_b)
    frag_cartridge_projectile_b.Modifiers = RadialDegrade

    frag_grenade_projectile.Name = "frag_grenade_projectile"
    frag_grenade_projectile.Damage0 = 75
    frag_grenade_projectile.Damage1 = 100
    frag_grenade_projectile.DamageAtEdge = 0.1f
    frag_grenade_projectile.DamageRadius = 7f
    frag_grenade_projectile.GrenadeProjectile = true
    frag_grenade_projectile.ProjectileDamageType = DamageType.Splash
    frag_grenade_projectile.InitialVelocity = 30
    frag_grenade_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(frag_grenade_projectile)
    frag_grenade_projectile.Modifiers = RadialDegrade

    frag_grenade_projectile_enh.Name = "frag_grenade_projectile_enh"
    // TODO for later, maybe : set_resource_parent frag_grenade_projectile_enh game_objects frag_grenade_projectile
    frag_grenade_projectile_enh.Damage0 = 75
    frag_grenade_projectile_enh.Damage1 = 100
    frag_grenade_projectile_enh.DamageAtEdge = 0.1f
    frag_grenade_projectile_enh.DamageRadius = 7f
    frag_grenade_projectile_enh.GrenadeProjectile = true
    frag_grenade_projectile_enh.ProjectileDamageType = DamageType.Splash
    frag_grenade_projectile_enh.InitialVelocity = 30
    frag_grenade_projectile_enh.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(frag_grenade_projectile_enh)
    frag_grenade_projectile_enh.Modifiers = RadialDegrade

    galaxy_gunship_gun_projectile.Name = "galaxy_gunship_gun_projectile"
    // TODO for later, maybe : set_resource_parent galaxy_gunship_gun_projectile game_objects 35mmbullet_projectile
    galaxy_gunship_gun_projectile.Damage0 = 40
    galaxy_gunship_gun_projectile.Damage1 = 50
    galaxy_gunship_gun_projectile.Damage2 = 80
    galaxy_gunship_gun_projectile.ProjectileDamageType = DamageType.Direct
    galaxy_gunship_gun_projectile.DegradeDelay = 0.4f
    galaxy_gunship_gun_projectile.DegradeMultiplier = 0.6f
    galaxy_gunship_gun_projectile.InitialVelocity = 400
    galaxy_gunship_gun_projectile.Lifespan = 0.8f
    ProjectileDefinition.CalculateDerivedFields(galaxy_gunship_gun_projectile)

    gauss_cannon_projectile.Name = "gauss_cannon_projectile"
    gauss_cannon_projectile.Damage0 = 190
    gauss_cannon_projectile.Damage1 = 370
    gauss_cannon_projectile.Damage2 = 370
    gauss_cannon_projectile.Damage3 = 370
    gauss_cannon_projectile.Damage4 = 240
    gauss_cannon_projectile.DamageAtEdge = 0.3f
    gauss_cannon_projectile.DamageRadius = 1.5f
    gauss_cannon_projectile.ProjectileDamageType = DamageType.Splash
    gauss_cannon_projectile.InitialVelocity = 150
    gauss_cannon_projectile.Lifespan = 2.67f
    ProjectileDefinition.CalculateDerivedFields(gauss_cannon_projectile)
    gauss_cannon_projectile.Modifiers = RadialDegrade

    grenade_projectile.Name = "grenade_projectile"
    grenade_projectile.Damage0 = 50
    grenade_projectile.DamageAtEdge = 0.2f
    grenade_projectile.DamageRadius = 100f
    grenade_projectile.ProjectileDamageType = DamageType.Splash
    grenade_projectile.InitialVelocity = 15
    grenade_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(grenade_projectile)
    grenade_projectile.Modifiers = RadialDegrade

    heavy_grenade_projectile.Name = "heavy_grenade_projectile"
    heavy_grenade_projectile.Damage0 = 50
    heavy_grenade_projectile.Damage1 = 82
    heavy_grenade_projectile.Damage2 = 82
    heavy_grenade_projectile.Damage3 = 75
    heavy_grenade_projectile.Damage4 = 66
    heavy_grenade_projectile.DamageAtEdge = 0.1f
    heavy_grenade_projectile.DamageRadius = 5f
    heavy_grenade_projectile.GrenadeProjectile = true
    heavy_grenade_projectile.ProjectileDamageType = DamageType.Splash
    heavy_grenade_projectile.InitialVelocity = 75
    heavy_grenade_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(heavy_grenade_projectile)
    heavy_grenade_projectile.Modifiers = RadialDegrade

    heavy_rail_beam_projectile.Name = "heavy_rail_beam_projectile"
    heavy_rail_beam_projectile.Damage0 = 75
    heavy_rail_beam_projectile.Damage1 = 215
    heavy_rail_beam_projectile.Damage2 = 215
    heavy_rail_beam_projectile.Damage3 = 215
    heavy_rail_beam_projectile.Damage4 = 120
    heavy_rail_beam_projectile.DamageAtEdge = 0.30f
    heavy_rail_beam_projectile.DamageRadius = 5f
    heavy_rail_beam_projectile.ProjectileDamageType = DamageType.Splash
    heavy_rail_beam_projectile.InitialVelocity = 600
    heavy_rail_beam_projectile.Lifespan = .5f
    ProjectileDefinition.CalculateDerivedFields(heavy_rail_beam_projectile)
    heavy_rail_beam_projectile.Modifiers = RadialDegrade

    heavy_sniper_projectile.Name = "heavy_sniper_projectile"
    heavy_sniper_projectile.Damage0 = 55
    heavy_sniper_projectile.Damage1 = 28
    heavy_sniper_projectile.Damage2 = 28
    heavy_sniper_projectile.Damage3 = 28
    heavy_sniper_projectile.Damage4 = 42
    heavy_sniper_projectile.ProjectileDamageType = DamageType.Splash
    heavy_sniper_projectile.InitialVelocity = 500
    heavy_sniper_projectile.Lifespan = 1.0f
    heavy_sniper_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(heavy_sniper_projectile)
    heavy_sniper_projectile.Modifiers = RadialDegrade

    hellfire_projectile.Name = "hellfire_projectile"
    hellfire_projectile.Damage0 = 50
    hellfire_projectile.Damage1 = 250
    hellfire_projectile.Damage2 = 250
    hellfire_projectile.Damage3 = 125
    hellfire_projectile.Damage4 = 250
    hellfire_projectile.Acceleration = 10
    hellfire_projectile.AccelerationUntil = 2f
    hellfire_projectile.DamageAtEdge = .25f
    hellfire_projectile.DamageRadius = 3f
    hellfire_projectile.ProjectileDamageType = DamageType.Splash
    hellfire_projectile.InitialVelocity = 125
    hellfire_projectile.Lifespan = 1.5f
    ProjectileDefinition.CalculateDerivedFields(hellfire_projectile)
    hellfire_projectile.Modifiers = RadialDegrade

    hunter_seeker_missile_dumbfire.Name = "hunter_seeker_missile_dumbfire"
    hunter_seeker_missile_dumbfire.Damage0 = 50
    hunter_seeker_missile_dumbfire.Damage1 = 350
    hunter_seeker_missile_dumbfire.Damage2 = 250
    hunter_seeker_missile_dumbfire.Damage3 = 250
    hunter_seeker_missile_dumbfire.Damage4 = 525
    hunter_seeker_missile_dumbfire.DamageAtEdge = 0.1f
    hunter_seeker_missile_dumbfire.DamageRadius = 1.5f
    hunter_seeker_missile_dumbfire.ProjectileDamageType = DamageType.Splash
    hunter_seeker_missile_dumbfire.InitialVelocity = 40
    hunter_seeker_missile_dumbfire.Lifespan = 6.3f
    ProjectileDefinition.CalculateDerivedFields(hunter_seeker_missile_dumbfire)
    hunter_seeker_missile_dumbfire.Modifiers = RadialDegrade

    hunter_seeker_missile_projectile.Name = "hunter_seeker_missile_projectile"
    hunter_seeker_missile_projectile.Damage0 = 50
    hunter_seeker_missile_projectile.Damage1 = 350
    hunter_seeker_missile_projectile.Damage2 = 250
    hunter_seeker_missile_projectile.Damage3 = 250
    hunter_seeker_missile_projectile.Damage4 = 525
    hunter_seeker_missile_projectile.DamageAtEdge = 0.1f
    hunter_seeker_missile_projectile.DamageRadius = 1.5f
    hunter_seeker_missile_projectile.ProjectileDamageType = DamageType.Splash
    hunter_seeker_missile_projectile.InitialVelocity = 40
    hunter_seeker_missile_projectile.Lifespan = 6.3f
    hunter_seeker_missile_projectile.registerAs = "rc-projectiles"
    hunter_seeker_missile_projectile.ExistsOnRemoteClients = true
    hunter_seeker_missile_projectile.RemoteClientData = (39577, 201)
    hunter_seeker_missile_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(hunter_seeker_missile_projectile)
    hunter_seeker_missile_projectile.Modifiers = RadialDegrade

    jammer_cartridge_projectile.Name = "jammer_cartridge_projectile"
    // TODO for later, maybe : set_resource_parent jammer_cartridge_projectile game_objects jammer_grenade_projectile
    jammer_cartridge_projectile.Damage0 = 0
    jammer_cartridge_projectile.Damage1 = 0
    jammer_cartridge_projectile.DamageAtEdge = 1.0f
    jammer_cartridge_projectile.DamageRadius = 10f
    jammer_cartridge_projectile.GrenadeProjectile = true
    jammer_cartridge_projectile.ProjectileDamageType = DamageType.Splash
    jammer_cartridge_projectile.InitialVelocity = 30
    jammer_cartridge_projectile.Lifespan = 15f
    jammer_cartridge_projectile.AdditionalEffect = true
    jammer_cartridge_projectile.JammerProjectile = true
    jammer_cartridge_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Player,
      EffectTarget.Validation.Player
    ) -> 1000
    jammer_cartridge_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.AMS
    ) -> 5000
    jammer_cartridge_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.MotionSensor
    ) -> 30000
    jammer_cartridge_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.Spitfire
    ) -> 30000
    jammer_cartridge_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Turret,
      EffectTarget.Validation.Turret
    ) -> 30000
    jammer_cartridge_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.VehicleNotAMS
    ) -> 10000
    ProjectileDefinition.CalculateDerivedFields(jammer_cartridge_projectile)
    jammer_cartridge_projectile.Modifiers = MaxDistanceCutoff

    jammer_cartridge_projectile_b.Name = "jammer_cartridge_projectile_b"
    // TODO for later, maybe : set_resource_parent jammer_cartridge_projectile_b game_objects jammer_grenade_projectile_enh
    jammer_cartridge_projectile_b.Damage0 = 0
    jammer_cartridge_projectile_b.Damage1 = 0
    jammer_cartridge_projectile_b.DamageAtEdge = 1.0f
    jammer_cartridge_projectile_b.DamageRadius = 10f
    jammer_cartridge_projectile_b.GrenadeProjectile = true
    jammer_cartridge_projectile_b.ProjectileDamageType = DamageType.Splash
    jammer_cartridge_projectile_b.InitialVelocity = 30
    jammer_cartridge_projectile_b.Lifespan = 2f
    jammer_cartridge_projectile_b.AdditionalEffect = true
    jammer_cartridge_projectile_b.JammerProjectile = true
    jammer_cartridge_projectile_b.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Player,
      EffectTarget.Validation.Player
    ) -> 1000
    jammer_cartridge_projectile_b.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.AMS
    ) -> 5000
    jammer_cartridge_projectile_b.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.MotionSensor
    ) -> 30000
    jammer_cartridge_projectile_b.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.Spitfire
    ) -> 30000
    jammer_cartridge_projectile_b.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Turret,
      EffectTarget.Validation.Turret
    ) -> 30000
    jammer_cartridge_projectile_b.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.VehicleNotAMS
    ) -> 10000
    ProjectileDefinition.CalculateDerivedFields(jammer_cartridge_projectile_b)
    jammer_cartridge_projectile_b.Modifiers = MaxDistanceCutoff

    jammer_grenade_projectile.Name = "jammer_grenade_projectile"
    jammer_grenade_projectile.Damage0 = 0
    jammer_grenade_projectile.Damage1 = 0
    jammer_grenade_projectile.DamageAtEdge = 1.0f
    jammer_grenade_projectile.DamageRadius = 10f
    jammer_grenade_projectile.GrenadeProjectile = true
    jammer_grenade_projectile.ProjectileDamageType = DamageType.Splash
    jammer_grenade_projectile.InitialVelocity = 30
    jammer_grenade_projectile.Lifespan = 15f
    jammer_grenade_projectile.AdditionalEffect = true
    jammer_grenade_projectile.JammerProjectile = true
    jammer_grenade_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Player,
      EffectTarget.Validation.Player
    ) -> 1000
    jammer_grenade_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.AMS
    ) -> 5000
    jammer_grenade_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.MotionSensor
    ) -> 30000
    jammer_grenade_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.Spitfire
    ) -> 30000
    jammer_grenade_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Turret,
      EffectTarget.Validation.Turret
    ) -> 30000
    jammer_grenade_projectile.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.VehicleNotAMS
    ) -> 10000
    ProjectileDefinition.CalculateDerivedFields(jammer_grenade_projectile)
    jammer_grenade_projectile.Modifiers = MaxDistanceCutoff

    jammer_grenade_projectile_enh.Name = "jammer_grenade_projectile_enh"
    // TODO for later, maybe : set_resource_parent jammer_grenade_projectile_enh game_objects jammer_grenade_projectile
    jammer_grenade_projectile_enh.Damage0 = 0
    jammer_grenade_projectile_enh.Damage1 = 0
    jammer_grenade_projectile_enh.DamageAtEdge = 1.0f
    jammer_grenade_projectile_enh.DamageRadius = 10f
    jammer_grenade_projectile_enh.GrenadeProjectile = true
    jammer_grenade_projectile_enh.ProjectileDamageType = DamageType.Splash
    jammer_grenade_projectile_enh.InitialVelocity = 30
    jammer_grenade_projectile_enh.Lifespan = 3f
    jammer_grenade_projectile_enh.AdditionalEffect = true
    jammer_grenade_projectile_enh.JammerProjectile = true
    jammer_grenade_projectile_enh.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Player,
      EffectTarget.Validation.Player
    ) -> 1000
    jammer_grenade_projectile_enh.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.AMS
    ) -> 5000
    jammer_grenade_projectile_enh.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.MotionSensor
    ) -> 30000
    jammer_grenade_projectile_enh.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.Spitfire
    ) -> 30000
    jammer_grenade_projectile_enh.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Turret,
      EffectTarget.Validation.Turret
    ) -> 30000
    jammer_grenade_projectile_enh.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.VehicleNotAMS
    ) -> 10000
    ProjectileDefinition.CalculateDerivedFields(jammer_grenade_projectile_enh)
    jammer_grenade_projectile_enh.Modifiers = MaxDistanceCutoff

    katana_projectile.Name = "katana_projectile"
    katana_projectile.Damage0 = 25
    katana_projectile.Damage1 = 0
    katana_projectile.ProjectileDamageType = DamageType.Direct
    katana_projectile.InitialVelocity = 100
    katana_projectile.Lifespan = .04f //.03f
    ProjectileDefinition.CalculateDerivedFields(katana_projectile)

    katana_projectileb.Name = "katana_projectileb"
    // TODO for later, maybe : set_resource_parent katana_projectileb game_objects katana_projectile
    katana_projectileb.Damage0 = 25
    katana_projectileb.Damage1 = 0
    katana_projectileb.ProjectileDamageType = DamageType.Direct
    katana_projectileb.InitialVelocity = 100
    katana_projectileb.Lifespan = .03f
    ProjectileDefinition.CalculateDerivedFields(katana_projectileb)

    lancer_projectile.Name = "lancer_projectile"
    lancer_projectile.Damage0 = 25
    lancer_projectile.Damage1 = 175
    lancer_projectile.Damage2 = 125
    lancer_projectile.Damage3 = 125
    lancer_projectile.Damage4 = 263
    lancer_projectile.ProjectileDamageType = DamageType.Direct
    lancer_projectile.InitialVelocity = 500
    lancer_projectile.Lifespan = 0.6f
    ProjectileDefinition.CalculateDerivedFields(lancer_projectile)

    lasher_projectile.Name = "lasher_projectile"
    lasher_projectile.Damage0 = 30
    lasher_projectile.Damage1 = 15
    lasher_projectile.Damage2 = 15
    lasher_projectile.Damage3 = 12
    lasher_projectile.Damage4 = 12
    lasher_projectile.ProjectileDamageType = DamageType.Direct
    lasher_projectile.DegradeDelay = 0.012f
    lasher_projectile.DegradeMultiplier = 0.3f
    lasher_projectile.InitialVelocity = 120
    lasher_projectile.LashRadius = 2.5f
    lasher_projectile.Lifespan = 0.75f
    lasher_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(lasher_projectile)
    lasher_projectile.Modifiers = List(
      DistanceDegrade,
      Lash
    )

    lasher_projectile_ap.Name = "lasher_projectile_ap"
    lasher_projectile_ap.Damage0 = 12
    lasher_projectile_ap.Damage1 = 25
    lasher_projectile_ap.Damage2 = 25
    lasher_projectile_ap.Damage3 = 28
    lasher_projectile_ap.Damage4 = 28
    lasher_projectile_ap.ProjectileDamageType = DamageType.Direct
    lasher_projectile_ap.DegradeDelay = 0.012f
    lasher_projectile_ap.DegradeMultiplier = 0.3f
    lasher_projectile_ap.InitialVelocity = 120
    lasher_projectile_ap.LashRadius = 2.5f
    lasher_projectile_ap.Lifespan = 0.75f
    lasher_projectile_ap.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(lasher_projectile_ap)
    lasher_projectile_ap.Modifiers = List(
      DistanceDegrade,
      Lash
    )

    liberator_bomb_cluster_bomblet_projectile.Name = "liberator_bomb_cluster_bomblet_projectile"
    liberator_bomb_cluster_bomblet_projectile.Damage0 = 75
    liberator_bomb_cluster_bomblet_projectile.Damage1 = 100
    liberator_bomb_cluster_bomblet_projectile.DamageAtEdge = 0.25f
    liberator_bomb_cluster_bomblet_projectile.DamageRadius = 3f
    liberator_bomb_cluster_bomblet_projectile.ProjectileDamageType = DamageType.Splash
    liberator_bomb_cluster_bomblet_projectile.InitialVelocity = 0
    liberator_bomb_cluster_bomblet_projectile.Lifespan = 30f
    ProjectileDefinition.CalculateDerivedFields(liberator_bomb_cluster_bomblet_projectile)
    liberator_bomb_cluster_bomblet_projectile.Modifiers = RadialDegrade

    liberator_bomb_cluster_projectile.Name = "liberator_bomb_cluster_projectile"
    liberator_bomb_cluster_projectile.Damage0 = 75
    liberator_bomb_cluster_projectile.Damage1 = 100
    liberator_bomb_cluster_projectile.DamageAtEdge = 0.25f
    liberator_bomb_cluster_projectile.DamageRadius = 3f
    liberator_bomb_cluster_projectile.ProjectileDamageType = DamageType.Direct
    liberator_bomb_cluster_projectile.InitialVelocity = 0
    liberator_bomb_cluster_projectile.Lifespan = 30f
    ProjectileDefinition.CalculateDerivedFields(liberator_bomb_cluster_projectile)

    liberator_bomb_projectile.Name = "liberator_bomb_projectile"
    liberator_bomb_projectile.Damage0 = 250
    liberator_bomb_projectile.Damage1 = 1000
    liberator_bomb_projectile.Damage2 = 1000
    liberator_bomb_projectile.Damage3 = 1000
    liberator_bomb_projectile.Damage4 = 600
    liberator_bomb_projectile.DamageAtEdge = 0.1f
    liberator_bomb_projectile.DamageRadius = 10f
    liberator_bomb_projectile.ProjectileDamageType = DamageType.Splash
    liberator_bomb_projectile.InitialVelocity = 0
    liberator_bomb_projectile.Lifespan = 30f
    ProjectileDefinition.CalculateDerivedFields(liberator_bomb_projectile)
    liberator_bomb_projectile.Modifiers = RadialDegrade

    maelstrom_grenade_damager.Name = "maelstrom_grenade_damager"
    maelstrom_grenade_damager.ProjectileDamageType = DamageType.Direct
    //the maelstrom_grenade_damage will be treated as a broken entity

    maelstrom_grenade_projectile.Name = "maelstrom_grenade_projectile"
    maelstrom_grenade_projectile.Damage0 = 32
    maelstrom_grenade_projectile.Damage1 = 60
    maelstrom_grenade_projectile.DamageRadius = 20f
    maelstrom_grenade_projectile.LashRadius = 5f
    maelstrom_grenade_projectile.GrenadeProjectile = true
    maelstrom_grenade_projectile.ProjectileDamageType = DamageType.Direct
    maelstrom_grenade_projectile.DamageThroughWalls = true
    maelstrom_grenade_projectile.InitialVelocity = 30
    maelstrom_grenade_projectile.Lifespan = 2f
    maelstrom_grenade_projectile.DamageProxy = 464 //maelstrom_grenade_damager
    ProjectileDefinition.CalculateDerivedFields(maelstrom_grenade_projectile)
    maelstrom_grenade_projectile.Modifiers = SameHit

    maelstrom_grenade_projectile_contact.Name = "maelstrom_grenade_projectile_contact"
    // TODO for later, maybe : set_resource_parent maelstrom_grenade_projectile_contact game_objects maelstrom_grenade_projectile
    maelstrom_grenade_projectile_contact.Damage0 = 32
    maelstrom_grenade_projectile_contact.Damage1 = 60
    maelstrom_grenade_projectile_contact.DamageRadius = 20f
    maelstrom_grenade_projectile_contact.LashRadius = 5f
    maelstrom_grenade_projectile_contact.GrenadeProjectile = true
    maelstrom_grenade_projectile_contact.ProjectileDamageType = DamageType.Direct
    maelstrom_grenade_projectile_contact.DamageThroughWalls = true
    maelstrom_grenade_projectile_contact.InitialVelocity = 30
    maelstrom_grenade_projectile_contact.Lifespan = 15f
    maelstrom_grenade_projectile_contact.DamageProxy = 464 //maelstrom_grenade_damager
    ProjectileDefinition.CalculateDerivedFields(maelstrom_grenade_projectile_contact)
    maelstrom_grenade_projectile_contact.Modifiers = SameHit

    maelstrom_stream_projectile.Name = "maelstrom_stream_projectile"
    maelstrom_stream_projectile.Damage0 = 15
    maelstrom_stream_projectile.Damage1 = 6
    maelstrom_stream_projectile.ProjectileDamageType = DamageType.Direct
    maelstrom_stream_projectile.DegradeDelay = .075f
    maelstrom_stream_projectile.DegradeMultiplier = 0.5f
    maelstrom_stream_projectile.InitialVelocity = 200
    maelstrom_stream_projectile.Lifespan = 0.2f
    ProjectileDefinition.CalculateDerivedFields(maelstrom_stream_projectile)
    maelstrom_stream_projectile.Modifiers = MaxDistanceCutoff

    magcutter_projectile.Name = "magcutter_projectile"
    // TODO for later, maybe : set_resource_parent magcutter_projectile game_objects melee_ammo_projectile
    magcutter_projectile.Damage0 = 50
    magcutter_projectile.Damage1 = 0
    magcutter_projectile.ProjectileDamageType = DamageType.Direct
    magcutter_projectile.InitialVelocity = 100
    magcutter_projectile.Lifespan = .03f //.02f
    ProjectileDefinition.CalculateDerivedFields(magcutter_projectile)
    magcutter_projectile.Modifiers = List(MeleeBoosted, MaxDistanceCutoff)

    melee_ammo_projectile.Name = "melee_ammo_projectile"
    melee_ammo_projectile.Damage0 = 25
    melee_ammo_projectile.Damage1 = 0
    melee_ammo_projectile.ProjectileDamageType = DamageType.Direct
    melee_ammo_projectile.InitialVelocity = 100
    melee_ammo_projectile.Lifespan = .02f
    ProjectileDefinition.CalculateDerivedFields(melee_ammo_projectile)
    melee_ammo_projectile.Modifiers = List(MeleeBoosted, MaxDistanceCutoff)

    meteor_common.Name = "meteor_common"
    meteor_common.DamageAtEdge = .1f
    meteor_common.ProjectileDamageType = DamageType.Splash
    meteor_common.InitialVelocity = 0
    meteor_common.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_common)
    meteor_common.Modifiers = RadialDegrade

    meteor_projectile_b_large.Name = "meteor_projectile_b_large"
    // TODO for later, maybe : set_resource_parent meteor_projectile_b_large game_objects meteor_common
    meteor_projectile_b_large.Damage0 = 2500
    meteor_projectile_b_large.Damage1 = 5000
    meteor_projectile_b_large.DamageRadius = 15f
    meteor_projectile_b_large.DamageAtEdge = .1f
    meteor_projectile_b_large.ProjectileDamageType = DamageType.Splash
    meteor_projectile_b_large.InitialVelocity = 0
    meteor_projectile_b_large.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_projectile_b_large)
    meteor_projectile_b_large.Modifiers = RadialDegrade

    meteor_projectile_b_medium.Name = "meteor_projectile_b_medium"
    // TODO for later, maybe : set_resource_parent meteor_projectile_b_medium game_objects meteor_common
    meteor_projectile_b_medium.Damage0 = 1250
    meteor_projectile_b_medium.Damage1 = 2500
    meteor_projectile_b_medium.DamageRadius = 10f
    meteor_projectile_b_medium.DamageAtEdge = .1f
    meteor_projectile_b_medium.ProjectileDamageType = DamageType.Splash
    meteor_projectile_b_medium.InitialVelocity = 0
    meteor_projectile_b_medium.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_projectile_b_medium)
    meteor_projectile_b_medium.Modifiers = RadialDegrade

    meteor_projectile_b_small.Name = "meteor_projectile_b_small"
    // TODO for later, maybe : set_resource_parent meteor_projectile_b_small game_objects meteor_common
    meteor_projectile_b_small.Damage0 = 625
    meteor_projectile_b_small.Damage1 = 1250
    meteor_projectile_b_small.DamageRadius = 5f
    meteor_projectile_b_small.DamageAtEdge = .1f
    meteor_projectile_b_small.ProjectileDamageType = DamageType.Splash
    meteor_projectile_b_small.InitialVelocity = 0
    meteor_projectile_b_small.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_projectile_b_small)
    meteor_projectile_b_small.Modifiers = RadialDegrade

    meteor_projectile_large.Name = "meteor_projectile_large"
    // TODO for later, maybe : set_resource_parent meteor_projectile_large game_objects meteor_common
    meteor_projectile_large.Damage0 = 2500
    meteor_projectile_large.Damage1 = 5000
    meteor_projectile_large.DamageRadius = 15f
    meteor_projectile_large.DamageAtEdge = .1f
    meteor_projectile_large.ProjectileDamageType = DamageType.Splash
    meteor_projectile_large.InitialVelocity = 0
    meteor_projectile_large.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_projectile_large)
    meteor_projectile_large.Modifiers = RadialDegrade

    meteor_projectile_medium.Name = "meteor_projectile_medium"
    // TODO for later, maybe : set_resource_parent meteor_projectile_medium game_objects meteor_common
    meteor_projectile_medium.Damage0 = 1250
    meteor_projectile_medium.Damage1 = 2500
    meteor_projectile_medium.DamageRadius = 10f
    meteor_projectile_medium.DamageAtEdge = .1f
    meteor_projectile_medium.ProjectileDamageType = DamageType.Splash
    meteor_projectile_medium.InitialVelocity = 0
    meteor_projectile_medium.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_projectile_medium)
    meteor_projectile_medium.Modifiers = RadialDegrade

    meteor_projectile_small.Name = "meteor_projectile_small"
    // TODO for later, maybe : set_resource_parent meteor_projectile_small game_objects meteor_common
    meteor_projectile_small.Damage0 = 625
    meteor_projectile_small.Damage1 = 1250
    meteor_projectile_small.DamageRadius = 5f
    meteor_projectile_small.DamageAtEdge = .1f
    meteor_projectile_small.ProjectileDamageType = DamageType.Splash
    meteor_projectile_small.InitialVelocity = 0
    meteor_projectile_small.Lifespan = 40
    ProjectileDefinition.CalculateDerivedFields(meteor_projectile_small)
    meteor_projectile_small.Modifiers = RadialDegrade

    mine_projectile.Name = "mine_projectile"
    mine_projectile.Lifespan = 0.01f
    mine_projectile.InitialVelocity = 300
    ProjectileDefinition.CalculateDerivedFields(mine_projectile)

    mine_sweeper_projectile.Name = "mine_sweeper_projectile"
    mine_sweeper_projectile.Damage0 = 0
    mine_sweeper_projectile.Damage1 = 0
    mine_sweeper_projectile.DamageAtEdge = .33f
    mine_sweeper_projectile.DamageRadius = 25f
    mine_sweeper_projectile.GrenadeProjectile = true
    mine_sweeper_projectile.ProjectileDamageType = DamageType.Splash
    mine_sweeper_projectile.InitialVelocity = 30
    mine_sweeper_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(mine_sweeper_projectile)
    mine_sweeper_projectile.Modifiers = RadialDegrade

    mine_sweeper_projectile_enh.Name = "mine_sweeper_projectile_enh"
    mine_sweeper_projectile_enh.Damage0 = 0
    mine_sweeper_projectile_enh.Damage1 = 0
    mine_sweeper_projectile_enh.DamageAtEdge = 0.33f
    mine_sweeper_projectile_enh.DamageRadius = 25f
    mine_sweeper_projectile_enh.GrenadeProjectile = true
    mine_sweeper_projectile_enh.ProjectileDamageType = DamageType.Splash
    mine_sweeper_projectile_enh.InitialVelocity = 30
    mine_sweeper_projectile_enh.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(mine_sweeper_projectile_enh)
    mine_sweeper_projectile_enh.Modifiers = RadialDegrade

    oicw_projectile.Name = "oicw_projectile"
    oicw_projectile.Damage0 = 50
    oicw_projectile.Damage1 = 50
    oicw_projectile.Acceleration = 15
    oicw_projectile.AccelerationUntil = 5f
    oicw_projectile.DamageAtEdge = 0.1f
    oicw_projectile.DamageRadius = 10f
    oicw_projectile.ProjectileDamageType = DamageType.Splash
    oicw_projectile.InitialVelocity = 5
    oicw_projectile.Lifespan = 6.1f
    oicw_projectile.DamageProxy = List(601, 601, 601, 601, 601) //5 x oicw_little_buddy
    oicw_projectile.registerAs = "rc-projectiles"
    oicw_projectile.ExistsOnRemoteClients = true
    oicw_projectile.RemoteClientData = (13107, 195)
    oicw_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(oicw_projectile)
    oicw_projectile.Modifiers = List(
      //ExplodingRadialDegrade,
      RadialDegrade
    )

    oicw_little_buddy.Name = "oicw_little_buddy"
    oicw_little_buddy.Damage0 = 75
    oicw_little_buddy.Damage1 = 75
    oicw_little_buddy.DamageAtEdge = 0.1f
    oicw_little_buddy.DamageRadius = 7.5f
    oicw_little_buddy.ProjectileDamageType = DamageType.Splash
    oicw_little_buddy.InitialVelocity = 40
    oicw_little_buddy.Lifespan = 0.5f
    oicw_little_buddy.registerAs = "rc-projectiles"
    oicw_little_buddy.ExistsOnRemoteClients = true //does not use RemoteClientData
    oicw_little_buddy.Packet = new LittleBuddyProjectileConverter
    //add_property oicw_little_buddy multi_stage_spawn_server_side true ...
    ProjectileDefinition.CalculateDerivedFields(oicw_little_buddy)
    oicw_little_buddy.Modifiers = List(
      ExplosionDamagesOnlyAbove
    )

    pellet_gun_projectile.Name = "pellet_gun_projectile"
    // TODO for later, maybe : set_resource_parent pellet_gun_projectile game_objects shotgun_shell_projectile
    pellet_gun_projectile.Damage0 = 12
    pellet_gun_projectile.Damage1 = 8
    pellet_gun_projectile.ProjectileDamageType = DamageType.Direct
    pellet_gun_projectile.InitialVelocity = 400
    pellet_gun_projectile.Lifespan = 0.1875f
    pellet_gun_projectile.UseDamage1Subtract = false
    ProjectileDefinition.CalculateDerivedFields(pellet_gun_projectile)

    phalanx_av_projectile.Name = "phalanx_av_projectile"
    phalanx_av_projectile.Damage0 = 60
    phalanx_av_projectile.Damage1 = 140
    phalanx_av_projectile.DamageAtEdge = 0.1f
    phalanx_av_projectile.DamageRadius = 5f
    phalanx_av_projectile.ProjectileDamageType = DamageType.Splash
    phalanx_av_projectile.InitialVelocity = 100
    phalanx_av_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(phalanx_av_projectile)
    phalanx_av_projectile.Modifiers = RadialDegrade

    phalanx_flak_projectile.Name = "phalanx_flak_projectile"
    phalanx_flak_projectile.Damage0 = 15
    phalanx_flak_projectile.Damage1 = 25
    phalanx_flak_projectile.Damage2 = 70
    phalanx_flak_projectile.DamageAtEdge = 1f
    phalanx_flak_projectile.DamageRadius = 10f
    phalanx_flak_projectile.ProjectileDamageType = DamageType.Direct
    phalanx_flak_projectile.ProjectileDamageTypeSecondary = DamageType.Splash
    phalanx_flak_projectile.InitialVelocity = 100
    phalanx_flak_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(phalanx_flak_projectile)
    phalanx_flak_projectile.Modifiers = List(
      //FlakHit,
      FlakBurst,
      MaxDistanceCutoff
    )

    phalanx_projectile.Name = "phalanx_projectile"
    phalanx_projectile.Damage0 = 20
    phalanx_projectile.Damage1 = 30
    phalanx_projectile.Damage2 = 30
    phalanx_projectile.Damage3 = 30
    phalanx_projectile.Damage4 = 18
    phalanx_projectile.ProjectileDamageType = DamageType.Direct
    phalanx_projectile.DegradeDelay = 0f
    phalanx_projectile.DegradeMultiplier = 0.25f
    phalanx_projectile.InitialVelocity = 400
    phalanx_projectile.Lifespan = 1f
    phalanx_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(phalanx_projectile)

    phoenix_missile_guided_projectile.Name = "phoenix_missile_guided_projectile"
    // TODO for later, maybe : set_resource_parent phoenix_missile_guided_projectile game_objects phoenix_missile_projectile
    phoenix_missile_guided_projectile.Damage0 = 80
    phoenix_missile_guided_projectile.Damage1 = 400
    phoenix_missile_guided_projectile.Damage2 = 400
    phoenix_missile_guided_projectile.Damage3 = 300
    phoenix_missile_guided_projectile.Damage4 = 600
    phoenix_missile_guided_projectile.Acceleration = 60
    phoenix_missile_guided_projectile.AccelerationUntil = 2.5f
    phoenix_missile_guided_projectile.DamageAtEdge = 0.3f
    phoenix_missile_guided_projectile.DamageRadius = 1.5f
    phoenix_missile_guided_projectile.ProjectileDamageType = DamageType.Splash
    phoenix_missile_guided_projectile.InitialVelocity = 0
    phoenix_missile_guided_projectile.Lifespan = 3f
    //not naturally a remote projectile, but being governed as one for convenience
    phoenix_missile_guided_projectile.registerAs = "rc-projectiles"
    phoenix_missile_guided_projectile.ExistsOnRemoteClients = true
    phoenix_missile_guided_projectile.RemoteClientData = (0, 63)
    phoenix_missile_guided_projectile.Packet = projectileConverter
    //
    ProjectileDefinition.CalculateDerivedFields(phoenix_missile_guided_projectile)
    phoenix_missile_guided_projectile.Modifiers = RadialDegrade

    phoenix_missile_projectile.Name = "phoenix_missile_projectile"
    phoenix_missile_projectile.Damage0 = 80
    phoenix_missile_projectile.Damage1 = 400
    phoenix_missile_projectile.Damage2 = 400
    phoenix_missile_projectile.Damage3 = 300
    phoenix_missile_projectile.Damage4 = 600
    phoenix_missile_projectile.Acceleration = 60
    phoenix_missile_projectile.AccelerationUntil = 2.5f
    phoenix_missile_projectile.DamageAtEdge = 0.3f
    phoenix_missile_projectile.DamageRadius = 1.5f
    phoenix_missile_projectile.ProjectileDamageType = DamageType.Splash
    phoenix_missile_projectile.InitialVelocity = 0
    phoenix_missile_projectile.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(phoenix_missile_projectile)
    phoenix_missile_projectile.Modifiers = RadialDegrade

    plasma_cartridge_projectile.Name = "plasma_cartridge_projectile"
    // TODO for later, maybe : set_resource_parent plasma_cartridge_projectile game_objects plasma_grenade_projectile
    plasma_cartridge_projectile.Damage0 = 20
    plasma_cartridge_projectile.Damage1 = 15
    plasma_cartridge_projectile.DamageAtEdge = 0.2f
    plasma_cartridge_projectile.DamageRadius = 7f
    plasma_cartridge_projectile.GrenadeProjectile = true
    plasma_cartridge_projectile.ProjectileDamageType = DamageType.Aggravated
    plasma_cartridge_projectile.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, 0.25f, 750), AggravatedInfo(DamageType.Splash, 0.25f, 1000)),
      Aura.Plasma,
      AggravatedTiming(3000),
      1.5f,
      true,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    plasma_cartridge_projectile.InitialVelocity = 30
    plasma_cartridge_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(plasma_cartridge_projectile)
    plasma_cartridge_projectile.Modifiers = List(
      InfantryAggravatedDirect,
      InfantryAggravatedDirectBurn,
      InfantryAggravatedSplash,
      InfantryAggravatedSplashBurn,
      RadialDegrade
    )

    plasma_cartridge_projectile_b.Name = "plasma_cartridge_projectile_b"
    // TODO for later, maybe : set_resource_parent plasma_cartridge_projectile_b game_objects plasma_grenade_projectile_B
    plasma_cartridge_projectile_b.Damage0 = 20
    plasma_cartridge_projectile_b.Damage1 = 15
    plasma_cartridge_projectile_b.DamageAtEdge = 0.2f
    plasma_cartridge_projectile_b.DamageRadius = 7f
    plasma_cartridge_projectile_b.GrenadeProjectile = true
    plasma_cartridge_projectile_b.ProjectileDamageType = DamageType.Aggravated
    plasma_cartridge_projectile_b.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, 0.25f, 750), AggravatedInfo(DamageType.Splash, 0.25f, 1000)),
      Aura.Plasma,
      AggravatedTiming(3000),
      1.5f,
      true,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    plasma_cartridge_projectile_b.InitialVelocity = 30
    plasma_cartridge_projectile_b.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(plasma_cartridge_projectile_b)
    plasma_cartridge_projectile_b.Modifiers = List(
      InfantryAggravatedDirect,
      InfantryAggravatedDirectBurn,
      InfantryAggravatedSplash,
      InfantryAggravatedSplashBurn,
      RadialDegrade
    )

    plasma_grenade_projectile.Name = "plasma_grenade_projectile"
    plasma_grenade_projectile.Damage0 = 40
    plasma_grenade_projectile.Damage1 = 30
    plasma_grenade_projectile.DamageAtEdge = 0.1f
    plasma_grenade_projectile.DamageRadius = 7f
    plasma_grenade_projectile.GrenadeProjectile = true
    plasma_grenade_projectile.ProjectileDamageType = DamageType.Aggravated
    plasma_grenade_projectile.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, 0.25f, 750), AggravatedInfo(DamageType.Splash, 0.25f, 1000)),
      Aura.Plasma,
      AggravatedTiming(3000),
      1.5f,
      true,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    plasma_grenade_projectile.InitialVelocity = 30
    plasma_grenade_projectile.Lifespan = 15f
    ProjectileDefinition.CalculateDerivedFields(plasma_grenade_projectile)
    plasma_grenade_projectile.Modifiers = List(
      InfantryAggravatedDirect,
      InfantryAggravatedDirectBurn,
      InfantryAggravatedSplash,
      InfantryAggravatedSplashBurn,
      RadialDegrade
    )

    plasma_grenade_projectile_B.Name = "plasma_grenade_projectile_B"
    // TODO for later, maybe : set_resource_parent plasma_grenade_projectile_B game_objects plasma_grenade_projectile
    plasma_grenade_projectile_B.Damage0 = 40
    plasma_grenade_projectile_B.Damage1 = 30
    plasma_grenade_projectile_B.DamageAtEdge = 0.1f
    plasma_grenade_projectile_B.DamageRadius = 7f
    plasma_grenade_projectile_B.GrenadeProjectile = true
    plasma_grenade_projectile_B.ProjectileDamageType = DamageType.Aggravated
    plasma_grenade_projectile_B.Aggravated = AggravatedDamage(
      List(AggravatedInfo(DamageType.Direct, 0.25f, 750), AggravatedInfo(DamageType.Splash, 0.25f, 1000)),
      Aura.Plasma,
      AggravatedTiming(3000),
      1.5f,
      true,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    plasma_grenade_projectile_B.InitialVelocity = 30
    plasma_grenade_projectile_B.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(plasma_grenade_projectile_B)
    plasma_grenade_projectile_B.Modifiers = List(
      InfantryAggravatedDirect,
      InfantryAggravatedDirectBurn,
      InfantryAggravatedSplash,
      InfantryAggravatedSplashBurn,
      RadialDegrade
    )

    pounder_projectile.Name = "pounder_projectile"
    pounder_projectile.Damage0 = 31
    pounder_projectile.Damage1 = 120
    pounder_projectile.Damage2 = 120
    pounder_projectile.Damage3 = 75
    pounder_projectile.Damage4 = 132
    pounder_projectile.DamageAtEdge = 0.1f
    pounder_projectile.DamageRadius = 1f
    pounder_projectile.GrenadeProjectile = true
    pounder_projectile.ProjectileDamageType = DamageType.Splash
    pounder_projectile.InitialVelocity = 120
    pounder_projectile.Lifespan = 2.5f
    ProjectileDefinition.CalculateDerivedFields(pounder_projectile)
    pounder_projectile.Modifiers = RadialDegrade

    pounder_projectile_enh.Name = "pounder_projectile_enh"
    // TODO for later, maybe : set_resource_parent pounder_projectile_enh game_objects pounder_projectile
    pounder_projectile_enh.Damage0 = 31
    pounder_projectile_enh.Damage1 = 120
    pounder_projectile_enh.Damage2 = 120
    pounder_projectile_enh.Damage3 = 75
    pounder_projectile_enh.Damage4 = 132
    pounder_projectile_enh.DamageAtEdge = 0.1f
    pounder_projectile_enh.DamageRadius = 1f
    pounder_projectile_enh.GrenadeProjectile = true
    pounder_projectile_enh.ProjectileDamageType = DamageType.Splash
    pounder_projectile_enh.InitialVelocity = 120
    pounder_projectile_enh.Lifespan = 3.2f
    ProjectileDefinition.CalculateDerivedFields(pounder_projectile_enh)
    pounder_projectile_enh.Modifiers = RadialDegrade

    ppa_projectile.Name = "ppa_projectile"
    ppa_projectile.Damage0 = 20
    ppa_projectile.Damage1 = 20
    ppa_projectile.Damage2 = 40
    ppa_projectile.Damage3 = 20
    ppa_projectile.Damage4 = 13
    ppa_projectile.ProjectileDamageType = DamageType.Direct
    ppa_projectile.InitialVelocity = 400
    ppa_projectile.Lifespan = .5f
    ProjectileDefinition.CalculateDerivedFields(ppa_projectile)

    pulsar_ap_projectile.Name = "pulsar_ap_projectile"
    // TODO for later, maybe : set_resource_parent pulsar_ap_projectile game_objects pulsar_projectile
    pulsar_ap_projectile.Damage0 = 7
    pulsar_ap_projectile.Damage1 = 15
    pulsar_ap_projectile.ProjectileDamageType = DamageType.Direct
    pulsar_ap_projectile.DegradeDelay = 0.1f
    pulsar_ap_projectile.DegradeMultiplier = 0.5f
    pulsar_ap_projectile.InitialVelocity = 500
    pulsar_ap_projectile.Lifespan = .4f
    pulsar_ap_projectile.UseDamage1Subtract = true
    pulsar_ap_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(pulsar_ap_projectile)

    pulsar_projectile.Name = "pulsar_projectile"
    pulsar_projectile.Damage0 = 20
    pulsar_projectile.Damage1 = 10
    pulsar_projectile.ProjectileDamageType = DamageType.Direct
    pulsar_projectile.DegradeDelay = 0.1f
    pulsar_projectile.DegradeMultiplier = 0.4f
    pulsar_projectile.InitialVelocity = 500
    pulsar_projectile.Lifespan = .4f
    pulsar_projectile.UseDamage1Subtract = true
    pulsar_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(pulsar_projectile)

    quasar_projectile.Name = "quasar_projectile"
    quasar_projectile.Damage0 = 18
    quasar_projectile.Damage1 = 8
    quasar_projectile.ProjectileDamageType = DamageType.Direct
    quasar_projectile.DegradeDelay = 0.045f
    quasar_projectile.DegradeMultiplier = 0.5f
    quasar_projectile.InitialVelocity = 500
    quasar_projectile.Lifespan = .4f
    quasar_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(quasar_projectile)

    radiator_cloud.Name = "radiator_cloud"
    radiator_cloud.Damage0 = 1 //2
    radiator_cloud.DamageAtEdge = 1.0f
    radiator_cloud.DamageRadius = 5f
    radiator_cloud.DamageToHealthOnly = true
    radiator_cloud.radiation_cloud = true
    radiator_cloud.ProjectileDamageType = DamageType.Radiation
    radiator_cloud.DamageThroughWalls = true
    //custom aggravated information
    radiator_cloud.ProjectileDamageTypeSecondary = DamageType.Aggravated
    radiator_cloud.Aggravated = AggravatedDamage(
      AggravatedInfo(DamageType.Splash, 1f, 80),
      Aura.None,
      AggravatedTiming(250, 2),
      0f,
      false,
      List(TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player))
    )
    radiator_cloud.Lifespan = 10.0f
    ProjectileDefinition.CalculateDerivedFields(radiator_cloud)
    radiator_cloud.registerAs = "rc-projectiles"
    radiator_cloud.ExistsOnRemoteClients = true
    radiator_cloud.Packet = radCloudConverter
    //radiator_cloud.Geometry = GeometryForm.representProjectileBySphere()
    radiator_cloud.Modifiers = List(
      MaxDistanceCutoff,
      InfantryAggravatedRadiation,
      InfantryAggravatedRadiationBurn,
      ShieldAgainstRadiation
    )

    radiator_grenade_projectile.Name = "radiator_grenade_projectile" // Todo : Radiator damages ?
    radiator_grenade_projectile.GrenadeProjectile = true             //not really, but technically yes
    radiator_grenade_projectile.ProjectileDamageType = DamageType.Direct
    radiator_grenade_projectile.InitialVelocity = 30
    radiator_grenade_projectile.Lifespan = 3f
    radiator_grenade_projectile.DamageProxy = 717 //radiator_cloud
    ProjectileDefinition.CalculateDerivedFields(radiator_grenade_projectile)

    radiator_sticky_projectile.Name = "radiator_sticky_projectile"
    // TODO for later, maybe : set_resource_parent radiator_sticky_projectile game_objects radiator_grenade_projectile
    radiator_sticky_projectile.GrenadeProjectile = true //not really, but technically yes
    radiator_sticky_projectile.ProjectileDamageType = DamageType.Direct
    radiator_sticky_projectile.InitialVelocity = 30
    radiator_sticky_projectile.Lifespan = 4f
    radiator_sticky_projectile.DamageProxy = 717 //radiator_cloud
    ProjectileDefinition.CalculateDerivedFields(radiator_sticky_projectile)

    reaver_rocket_projectile.Name = "reaver_rocket_projectile"
    reaver_rocket_projectile.Damage0 = 25
    reaver_rocket_projectile.Damage1 = 88
    reaver_rocket_projectile.Damage2 = 75
    reaver_rocket_projectile.Damage3 = 75
    reaver_rocket_projectile.Damage4 = 88
    reaver_rocket_projectile.Acceleration = 50
    reaver_rocket_projectile.AccelerationUntil = 1f
    reaver_rocket_projectile.DamageAtEdge = 0.1f
    reaver_rocket_projectile.DamageRadius = 3f
    reaver_rocket_projectile.ProjectileDamageType = DamageType.Splash
    reaver_rocket_projectile.InitialVelocity = 100
    reaver_rocket_projectile.Lifespan = 2.1f
    ProjectileDefinition.CalculateDerivedFields(reaver_rocket_projectile)
    reaver_rocket_projectile.Modifiers = RadialDegrade

    rocket_projectile.Name = "rocket_projectile"
    rocket_projectile.Damage0 = 50
    rocket_projectile.Damage1 = 105
    rocket_projectile.Damage2 = 75
    rocket_projectile.Damage3 = 75
    rocket_projectile.Damage4 = 75
    rocket_projectile.Acceleration = 10
    rocket_projectile.AccelerationUntil = 2f
    rocket_projectile.DamageAtEdge = .5f
    rocket_projectile.DamageRadius = 3f
    rocket_projectile.ProjectileDamageType = DamageType.Splash
    rocket_projectile.InitialVelocity = 50
    rocket_projectile.Lifespan = 8f
    ProjectileDefinition.CalculateDerivedFields(rocket_projectile)
    rocket_projectile.Modifiers = RadialDegrade

    rocklet_flak_projectile.Name = "rocklet_flak_projectile"
    rocklet_flak_projectile.Damage0 = 20
    rocklet_flak_projectile.Damage1 = 30
    rocklet_flak_projectile.Damage2 = 57
    rocklet_flak_projectile.Damage3 = 30
    rocklet_flak_projectile.Damage4 = 50
    rocklet_flak_projectile.DamageAtEdge = 0.25f
    rocklet_flak_projectile.DamageRadius = 8f
    rocklet_flak_projectile.ProjectileDamageType = DamageType.Direct
    rocklet_flak_projectile.ProjectileDamageTypeSecondary = DamageType.Splash
    rocklet_flak_projectile.InitialVelocity = 60
    rocklet_flak_projectile.Lifespan = 3.2f
    ProjectileDefinition.CalculateDerivedFields(rocklet_flak_projectile)
    rocklet_flak_projectile.Modifiers = List(
      //FlakHit,
      FlakBurst,
      MaxDistanceCutoff
    )

    rocklet_jammer_projectile.Name = "rocklet_jammer_projectile"
    rocklet_jammer_projectile.Damage0 = 0
    rocklet_jammer_projectile.Acceleration = 10
    rocklet_jammer_projectile.AccelerationUntil = 2f
    rocklet_jammer_projectile.DamageAtEdge = 1.0f
    rocklet_jammer_projectile.DamageRadius = 10f
    rocklet_jammer_projectile.ProjectileDamageType = DamageType.Splash
    rocklet_jammer_projectile.InitialVelocity = 50
    rocklet_jammer_projectile.Lifespan = 8f
    ProjectileDefinition.CalculateDerivedFields(rocklet_jammer_projectile)
    //TODO rocklet_jammer_projectile.Modifiers = RadialDegrade?

    scattercannon_projectile.Name = "scattercannon_projectile"
    scattercannon_projectile.Damage0 = 11
    scattercannon_projectile.Damage1 = 5
    scattercannon_projectile.ProjectileDamageType = DamageType.Direct
    scattercannon_projectile.InitialVelocity = 400
    scattercannon_projectile.Lifespan = 0.25f
    scattercannon_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(scattercannon_projectile)

    scythe_projectile.Name = "scythe_projectile"
    scythe_projectile.Damage0 = 30
    scythe_projectile.Damage1 = 20
    scythe_projectile.ProjectileDamageType = DamageType.Direct
    scythe_projectile.DegradeDelay = .015f
    scythe_projectile.DegradeMultiplier = 0.35f
    scythe_projectile.InitialVelocity = 60
    scythe_projectile.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(scythe_projectile)

    scythe_projectile_slave.Name = "scythe_projectile_slave" // Todo how does it work ?
    scythe_projectile_slave.InitialVelocity = 30
    scythe_projectile_slave.Lifespan = 3f
    ProjectileDefinition.CalculateDerivedFields(scythe_projectile_slave)

    shotgun_shell_AP_projectile.Name = "shotgun_shell_AP_projectile"
    // TODO for later, maybe : set_resource_parent shotgun_shell_AP_projectile game_objects shotgun_shell_projectile
    shotgun_shell_AP_projectile.Damage0 = 5
    shotgun_shell_AP_projectile.Damage1 = 10
    shotgun_shell_AP_projectile.ProjectileDamageType = DamageType.Direct
    shotgun_shell_AP_projectile.InitialVelocity = 400
    shotgun_shell_AP_projectile.Lifespan = 0.25f
    shotgun_shell_AP_projectile.UseDamage1Subtract = true
    shotgun_shell_AP_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(shotgun_shell_AP_projectile)

    shotgun_shell_projectile.Name = "shotgun_shell_projectile"
    shotgun_shell_projectile.Damage0 = 12
    shotgun_shell_projectile.Damage1 = 5
    shotgun_shell_projectile.ProjectileDamageType = DamageType.Direct
    shotgun_shell_projectile.InitialVelocity = 400
    shotgun_shell_projectile.Lifespan = 0.25f
    shotgun_shell_projectile.UseDamage1Subtract = true
    shotgun_shell_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(shotgun_shell_projectile)

    six_shooter_projectile.Name = "six_shooter_projectile"
    // TODO for later, maybe : set_resource_parent six_shooter_projectile game_objects 9mmbullet_projectile
    six_shooter_projectile.Damage0 = 22
    six_shooter_projectile.Damage1 = 20
    six_shooter_projectile.ProjectileDamageType = DamageType.Direct
    six_shooter_projectile.DegradeDelay = 0.15f
    six_shooter_projectile.DegradeMultiplier = 0.25f
    six_shooter_projectile.InitialVelocity = 500
    six_shooter_projectile.Lifespan = 0.4f
    six_shooter_projectile.UseDamage1Subtract = false
    ProjectileDefinition.CalculateDerivedFields(six_shooter_projectile)

    skyguard_flak_cannon_projectile.Name = "skyguard_flak_cannon_projectile"
    skyguard_flak_cannon_projectile.Damage0 = 15
    skyguard_flak_cannon_projectile.Damage1 = 25
    skyguard_flak_cannon_projectile.Damage2 = 50
    skyguard_flak_cannon_projectile.DamageAtEdge = 1f
    skyguard_flak_cannon_projectile.DamageRadius = 10f
    skyguard_flak_cannon_projectile.ProjectileDamageType = DamageType.Direct
    skyguard_flak_cannon_projectile.ProjectileDamageTypeSecondary = DamageType.Splash
    skyguard_flak_cannon_projectile.InitialVelocity = 100
    skyguard_flak_cannon_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(skyguard_flak_cannon_projectile)
    skyguard_flak_cannon_projectile.Modifiers = List(
      //FlakHit,
      FlakBurst,
      MaxDistanceCutoff
    )

    sparrow_projectile.Name = "sparrow_projectile"
    sparrow_projectile.Damage0 = 35
    sparrow_projectile.Damage1 = 50
    sparrow_projectile.Damage2 = 125
    sparrow_projectile.Acceleration = 12
    sparrow_projectile.AccelerationUntil = 5f
    sparrow_projectile.DamageAtEdge = 0.1f
    sparrow_projectile.DamageRadius = 3f
    sparrow_projectile.ProjectileDamageType = DamageType.Splash
    sparrow_projectile.InitialVelocity = 60
    sparrow_projectile.Lifespan = 5.85f
    sparrow_projectile.registerAs = "rc-projectiles"
    sparrow_projectile.ExistsOnRemoteClients = true
    sparrow_projectile.RemoteClientData = (13107, 187)
    sparrow_projectile.AutoLock = true
    sparrow_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(sparrow_projectile)
    sparrow_projectile.Modifiers = RadialDegrade

    sparrow_secondary_projectile.Name = "sparrow_secondary_projectile"
    // TODO for later, maybe : set_resource_parent sparrow_secondary_projectile game_objects sparrow_projectile
    sparrow_secondary_projectile.Damage0 = 35
    sparrow_secondary_projectile.Damage1 = 50
    sparrow_secondary_projectile.Damage2 = 125
    sparrow_secondary_projectile.Acceleration = 12
    sparrow_secondary_projectile.AccelerationUntil = 5f
    sparrow_secondary_projectile.DamageAtEdge = 0.1f
    sparrow_secondary_projectile.DamageRadius = 3f
    sparrow_secondary_projectile.ProjectileDamageType = DamageType.Splash
    sparrow_secondary_projectile.InitialVelocity = 60
    sparrow_secondary_projectile.Lifespan = 5.85f
    sparrow_secondary_projectile.registerAs = "rc-projectiles"
    sparrow_secondary_projectile.ExistsOnRemoteClients = true
    sparrow_secondary_projectile.RemoteClientData = (13107, 187)
    sparrow_secondary_projectile.AutoLock = true
    sparrow_secondary_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(sparrow_secondary_projectile)
    sparrow_secondary_projectile.Modifiers = RadialDegrade

    spiker_projectile.Name = "spiker_projectile"
    spiker_projectile.Charging = ChargeDamage(4, StandardDamageProfile(damage0 = Some(20), damage1 = Some(20)))
    spiker_projectile.Damage0 = 75
    spiker_projectile.Damage1 = 75
    spiker_projectile.DamageAtEdge = 0.1f
    spiker_projectile.DamageRadius = 5f
    spiker_projectile.DamageRadiusMin = 1f
    spiker_projectile.ProjectileDamageType = DamageType.Splash
    spiker_projectile.InitialVelocity = 40
    spiker_projectile.Lifespan = 5f
    spiker_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(spiker_projectile)
    spiker_projectile.Modifiers = List(
      SpikerChargeDamage,
      RadialDegrade
    )

    spitfire_aa_ammo_projectile.Name = "spitfire_aa_ammo_projectile"
    spitfire_aa_ammo_projectile.Damage0 = 5
    spitfire_aa_ammo_projectile.Damage1 = 15
    spitfire_aa_ammo_projectile.Damage2 = 12
    spitfire_aa_ammo_projectile.Damage3 = 5
    spitfire_aa_ammo_projectile.Damage4 = 15
    spitfire_aa_ammo_projectile.DamageAtEdge = 1f
    spitfire_aa_ammo_projectile.DamageRadius = 10f
    spitfire_aa_ammo_projectile.ProjectileDamageType = DamageType.Direct
    spitfire_aa_ammo_projectile.ProjectileDamageTypeSecondary = DamageType.Splash
    spitfire_aa_ammo_projectile.InitialVelocity = 100
    spitfire_aa_ammo_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(spitfire_aa_ammo_projectile)
    spitfire_aa_ammo_projectile.Modifiers = List(
      CerberusTurretWrongTarget,
      FlakBurst,
      MaxDistanceCutoff
    )

    spitfire_ammo_projectile.Name = "spitfire_ammo_projectile"
    spitfire_ammo_projectile.Damage0 = 15
    spitfire_ammo_projectile.Damage1 = 10
    spitfire_ammo_projectile.ProjectileDamageType = DamageType.Direct
    spitfire_ammo_projectile.DegradeDelay = .01f
    spitfire_ammo_projectile.DegradeMultiplier = 0.5f
    spitfire_ammo_projectile.InitialVelocity = 100
    spitfire_ammo_projectile.Lifespan = .5f
    spitfire_ammo_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(spitfire_ammo_projectile)

    starfire_projectile.Name = "starfire_projectile"
    starfire_projectile.Damage0 = 16
    starfire_projectile.Damage1 = 20
    starfire_projectile.Damage2 = 58
    starfire_projectile.Acceleration = 12
    starfire_projectile.AccelerationUntil = 5f
    starfire_projectile.ProjectileDamageType = DamageType.Aggravated
    starfire_projectile.Aggravated = AggravatedDamage(
      AggravatedInfo(DamageType.Direct, 0.25f, 250),
      Aura.Comet,
      2000,
      0f,
      true,
      List(
        TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player),
        TargetValidation(EffectTarget.Category.Vehicle, EffectTarget.Validation.Vehicle),
        TargetValidation(EffectTarget.Category.Turret, EffectTarget.Validation.Turret)
      )
    )
    starfire_projectile.InitialVelocity = 45
    starfire_projectile.Lifespan = 7.8f
    starfire_projectile.registerAs = "rc-projectiles"
    starfire_projectile.ExistsOnRemoteClients = true
    starfire_projectile.RemoteClientData = (39577, 249)
    starfire_projectile.AutoLock = true
    starfire_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(starfire_projectile)
    starfire_projectile.Modifiers = List(
      StarfireAggravatedBurn,
      RadialDegrade
    )

    striker_missile_projectile.Name = "striker_missile_projectile"
    striker_missile_projectile.Damage0 = 35
    striker_missile_projectile.Damage1 = 175
    striker_missile_projectile.Damage2 = 125
    striker_missile_projectile.Damage3 = 125
    striker_missile_projectile.Damage4 = 263
    striker_missile_projectile.Acceleration = 20
    striker_missile_projectile.AccelerationUntil = 2f
    striker_missile_projectile.DamageAtEdge = 0.1f
    striker_missile_projectile.DamageRadius = 1.5f
    striker_missile_projectile.ProjectileDamageType = DamageType.Splash
    striker_missile_projectile.InitialVelocity = 30
    striker_missile_projectile.Lifespan = 4.2f
    ProjectileDefinition.CalculateDerivedFields(striker_missile_projectile)
    striker_missile_projectile.Modifiers = RadialDegrade

    striker_missile_targeting_projectile.Name = "striker_missile_targeting_projectile"
    // TODO for later, maybe : set_resource_parent striker_missile_targeting_projectile game_objects striker_missile_projectile
    striker_missile_targeting_projectile.Damage0 = 35
    striker_missile_targeting_projectile.Damage1 = 175
    striker_missile_targeting_projectile.Damage2 = 125
    striker_missile_targeting_projectile.Damage3 = 125
    striker_missile_targeting_projectile.Damage4 = 263
    striker_missile_targeting_projectile.Acceleration = 20
    striker_missile_targeting_projectile.AccelerationUntil = 2f
    striker_missile_targeting_projectile.DamageAtEdge = 0.1f
    striker_missile_targeting_projectile.DamageRadius = 1.5f
    striker_missile_targeting_projectile.ProjectileDamageType = DamageType.Splash
    striker_missile_targeting_projectile.InitialVelocity = 30
    striker_missile_targeting_projectile.Lifespan = 4.2f
    striker_missile_targeting_projectile.registerAs = "rc-projectiles"
    striker_missile_targeting_projectile.ExistsOnRemoteClients = true
    striker_missile_targeting_projectile.RemoteClientData = (26214, 134)
    striker_missile_targeting_projectile.AutoLock = true
    striker_missile_targeting_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(striker_missile_targeting_projectile)
    striker_missile_targeting_projectile.Modifiers = RadialDegrade

    trek_projectile.Name = "trek_projectile"
    trek_projectile.Damage0 = 0
    trek_projectile.Damage1 = 0
    trek_projectile.Damage2 = 0
    trek_projectile.Damage3 = 0
    trek_projectile.Damage4 = 0
    trek_projectile.Acceleration = -20
    trek_projectile.AccelerationUntil = 1f
    trek_projectile.ProjectileDamageType = DamageType.Direct
    trek_projectile.InitialVelocity = 40
    trek_projectile.Lifespan = 7f
    trek_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(trek_projectile)
    trek_projectile.Modifiers = MaxDistanceCutoff

    vanu_sentry_turret_projectile.Name = "vanu_sentry_turret_projectile"
    vanu_sentry_turret_projectile.Damage0 = 25
    vanu_sentry_turret_projectile.Damage1 = 35
    vanu_sentry_turret_projectile.Damage2 = 100
    vanu_sentry_turret_projectile.DamageAtEdge = 0.1f
    vanu_sentry_turret_projectile.DamageRadius = 3f
    vanu_sentry_turret_projectile.ProjectileDamageType = DamageType.Splash
    vanu_sentry_turret_projectile.InitialVelocity = 240
    vanu_sentry_turret_projectile.Lifespan = 1.3f
    ProjectileDefinition.CalculateDerivedFields(vanu_sentry_turret_projectile)
    vanu_sentry_turret_projectile.Modifiers = RadialDegrade

    vulture_bomb_projectile.Name = "vulture_bomb_projectile"
    vulture_bomb_projectile.Damage0 = 175
    vulture_bomb_projectile.Damage1 = 1750
    vulture_bomb_projectile.Damage2 = 1000
    vulture_bomb_projectile.Damage3 = 400
    vulture_bomb_projectile.Damage4 = 1500
    vulture_bomb_projectile.DamageAtEdge = 0.1f
    vulture_bomb_projectile.DamageRadius = 10f
    vulture_bomb_projectile.ProjectileDamageType = DamageType.Splash
    vulture_bomb_projectile.InitialVelocity = 0
    vulture_bomb_projectile.Lifespan = 30f
    ProjectileDefinition.CalculateDerivedFields(vulture_bomb_projectile)
    vulture_bomb_projectile.Modifiers = RadialDegrade

    vulture_nose_bullet_projectile.Name = "vulture_nose_bullet_projectile"
    vulture_nose_bullet_projectile.Damage0 = 12
    vulture_nose_bullet_projectile.Damage1 = 15
    vulture_nose_bullet_projectile.Damage2 = 10
    vulture_nose_bullet_projectile.Damage3 = 10
    vulture_nose_bullet_projectile.Damage4 = 15
    vulture_nose_bullet_projectile.ProjectileDamageType = DamageType.Direct
    vulture_nose_bullet_projectile.DegradeDelay = .4f
    vulture_nose_bullet_projectile.DegradeMultiplier = 0.7f
    vulture_nose_bullet_projectile.InitialVelocity = 500
    vulture_nose_bullet_projectile.Lifespan = 0.46f
    ProjectileDefinition.CalculateDerivedFields(vulture_nose_bullet_projectile)

    vulture_tail_bullet_projectile.Name = "vulture_tail_bullet_projectile"
    vulture_tail_bullet_projectile.Damage0 = 25
    vulture_tail_bullet_projectile.Damage1 = 35
    vulture_tail_bullet_projectile.Damage2 = 50
    vulture_tail_bullet_projectile.ProjectileDamageType = DamageType.Direct
    vulture_tail_bullet_projectile.DegradeDelay = .02f
    vulture_tail_bullet_projectile.DegradeMultiplier = 0.5f
    vulture_tail_bullet_projectile.InitialVelocity = 500
    vulture_tail_bullet_projectile.Lifespan = 0.6f
    ProjectileDefinition.CalculateDerivedFields(vulture_tail_bullet_projectile)

    wasp_gun_projectile.Name = "wasp_gun_projectile"
    wasp_gun_projectile.Damage0 = 10
    wasp_gun_projectile.Damage1 = 15
    wasp_gun_projectile.Damage2 = 25
    wasp_gun_projectile.Damage3 = 17
    wasp_gun_projectile.Damage4 = 7
    wasp_gun_projectile.ProjectileDamageType = DamageType.Direct
    wasp_gun_projectile.DegradeDelay = .015f
    wasp_gun_projectile.DegradeMultiplier = 0.5f
    wasp_gun_projectile.InitialVelocity = 500
    wasp_gun_projectile.Lifespan = 0.5f
    wasp_gun_projectile.DamageToBattleframeOnly = true
    ProjectileDefinition.CalculateDerivedFields(wasp_gun_projectile)

    wasp_rocket_projectile.Name = "wasp_rocket_projectile"
    wasp_rocket_projectile.Damage0 = 35
    wasp_rocket_projectile.Damage1 = 50
    wasp_rocket_projectile.Damage2 = 300
    wasp_rocket_projectile.Acceleration = 10
    wasp_rocket_projectile.AccelerationUntil = 5f
    wasp_rocket_projectile.DamageAtEdge = 0.1f
    wasp_rocket_projectile.DamageRadius = 3f
    wasp_rocket_projectile.ProjectileDamageType = DamageType.Splash
    wasp_rocket_projectile.InitialVelocity = 60
    wasp_rocket_projectile.Lifespan = 6.5f
    wasp_rocket_projectile.registerAs = "rc-projectiles"
    wasp_rocket_projectile.ExistsOnRemoteClients = true
    wasp_rocket_projectile.RemoteClientData = (0, 208)
    wasp_rocket_projectile.AutoLock = true
    wasp_rocket_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(wasp_rocket_projectile)
    wasp_rocket_projectile.Modifiers = RadialDegrade

    winchester_projectile.Name = "winchester_projectile"
    // TODO for later, maybe : set_resource_parent winchester_projectile game_objects bolt_projectile
    winchester_projectile.Damage0 = 80
    winchester_projectile.Damage1 = 40
    winchester_projectile.Damage2 = 50
    winchester_projectile.Damage3 = 50
    winchester_projectile.Damage4 = 75
    winchester_projectile.ProjectileDamageType = DamageType.Direct
    winchester_projectile.InitialVelocity = 500
    winchester_projectile.Lifespan = 0.6f
    ProjectileDefinition.CalculateDerivedFields(winchester_projectile)
  }

  /**
    * Initialize `ProjectileDefinition` globals for projectiles utilized by battleframe robotics.
    */
  private def init_bfr_projectile(): Unit = {
    val projectileConverter: ProjectileConverter   = new ProjectileConverter
    val radCloudConverter: RadiationCloudConverter = new RadiationCloudConverter

    aphelion_immolation_cannon_projectile.Name = "aphelion_immolation_cannon_projectile"
    aphelion_immolation_cannon_projectile.Damage0 = 55
    aphelion_immolation_cannon_projectile.Damage1 = 225
    aphelion_immolation_cannon_projectile.Damage2 = 210
    aphelion_immolation_cannon_projectile.Damage3 = 135
    aphelion_immolation_cannon_projectile.Damage4 = 140
    aphelion_immolation_cannon_projectile.DamageAtEdge = 0.1f
    aphelion_immolation_cannon_projectile.DamageRadius = 2.0f
    aphelion_immolation_cannon_projectile.ProjectileDamageType = DamageType.Splash
    aphelion_immolation_cannon_projectile.InitialVelocity = 250
    aphelion_immolation_cannon_projectile.Lifespan = 1.4f
    ProjectileDefinition.CalculateDerivedFields(aphelion_immolation_cannon_projectile)
    aphelion_immolation_cannon_projectile.Modifiers = RadialDegrade

    aphelion_laser_projectile.Name = "aphelion_laser_projectile"
    aphelion_laser_projectile.Damage0 = 3
    aphelion_laser_projectile.Damage1 = 5
    aphelion_laser_projectile.Damage2 = 5
    aphelion_laser_projectile.Damage3 = 4
    aphelion_laser_projectile.Damage4 = 5
    aphelion_laser_projectile.ProjectileDamageType = DamageType.Direct
    aphelion_laser_projectile.DegradeDelay = .05f
    aphelion_laser_projectile.DegradeMultiplier = 0.5f
    aphelion_laser_projectile.InitialVelocity = 500
    aphelion_laser_projectile.Lifespan = 0.35f
    ProjectileDefinition.CalculateDerivedFields(aphelion_laser_projectile)

    aphelion_plasma_cloud.Name = "aphelion_plasma_cloud"
    aphelion_plasma_cloud.Damage0 = 3
    aphelion_plasma_cloud.DamageAtEdge = 1.0f
    aphelion_plasma_cloud.DamageRadius = 3f
    aphelion_plasma_cloud.radiation_cloud = true
    aphelion_plasma_cloud.ProjectileDamageType = DamageType.Aggravated
    //aphelion_plasma_cloud.DamageThroughWalls = true
    aphelion_plasma_cloud.Aggravated = AggravatedDamage(
      AggravatedInfo(DamageType.Splash, 0.5f, 1000),
      Aura.Napalm,
      AggravatedTiming(10000, 2), //10000
      10f,                        //aphelion_plasma_rocket_projectile.aggravated_damage_max_factor
      true,
      List(
        TargetValidation(EffectTarget.Category.Player, EffectTarget.Validation.Player)
      )
    )
    aphelion_plasma_cloud.Lifespan = 10.0f
    ProjectileDefinition.CalculateDerivedFields(aphelion_plasma_cloud)
    aphelion_plasma_cloud.registerAs = "rc-projectiles"
    aphelion_plasma_cloud.ExistsOnRemoteClients = true
    aphelion_plasma_cloud.Packet = radCloudConverter
    //aphelion_plasma_cloud.Geometry = GeometryForm.representProjectileBySphere()
    aphelion_plasma_cloud.Modifiers = List( //TODO placeholder values
      MaxDistanceCutoff,
      InfantryAggravatedRadiation,
      InfantryAggravatedRadiationBurn,
      ShieldAgainstRadiation
    )

    aphelion_plasma_rocket_projectile.Name = "aphelion_plasma_rocket_projectile"
    //has property aggravated_damage_max_factor, but it's the aphelion_plasma_cloud that performs aggravated damage
    aphelion_plasma_rocket_projectile.Damage0 = 38
    aphelion_plasma_rocket_projectile.Damage1 = 70
    aphelion_plasma_rocket_projectile.Damage2 = 95
    aphelion_plasma_rocket_projectile.Damage3 = 55
    aphelion_plasma_rocket_projectile.Damage4 = 60
    aphelion_plasma_rocket_projectile.Acceleration = 20
    aphelion_plasma_rocket_projectile.AccelerationUntil = 2f
    aphelion_plasma_rocket_projectile.DamageAtEdge = .1f
    aphelion_plasma_rocket_projectile.DamageRadius = 3f
    aphelion_plasma_rocket_projectile.ProjectileDamageType = DamageType.Splash
    //aphelion_plasma_rocket_projectile.DamageProxy = 96 //aphelion_plama_cloud
    aphelion_plasma_rocket_projectile.InitialVelocity = 75
    aphelion_plasma_rocket_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(aphelion_plasma_rocket_projectile)
    aphelion_plasma_rocket_projectile.Modifiers = RadialDegrade

    aphelion_ppa_projectile.Name = "aphelion_ppa_projectile"
    // TODO for later, maybe : set_resource_parent aphelion_ppa_projectile game_objects ppa_projectile
    aphelion_ppa_projectile.Damage0 = 31
    aphelion_ppa_projectile.Damage1 = 84
    aphelion_ppa_projectile.Damage2 = 58
    aphelion_ppa_projectile.Damage3 = 57
    aphelion_ppa_projectile.Damage4 = 60
    aphelion_ppa_projectile.DamageAtEdge = 0.10f
    aphelion_ppa_projectile.DamageRadius = 1f
    aphelion_ppa_projectile.ProjectileDamageType = DamageType.Splash
    aphelion_ppa_projectile.DegradeDelay = .5f
    aphelion_ppa_projectile.DegradeMultiplier = 0.55f
    aphelion_ppa_projectile.InitialVelocity = 350
    aphelion_ppa_projectile.Lifespan = .7f
    ProjectileDefinition.CalculateDerivedFields(aphelion_ppa_projectile)
    aphelion_ppa_projectile.Modifiers = RadialDegrade

    aphelion_starfire_projectile.Name = "aphelion_starfire_projectile"
    // TODO for later, maybe : set_resource_parent aphelion_starfire_projectile game_objects starfire_projectile
    aphelion_starfire_projectile.Damage0 = 12
    aphelion_starfire_projectile.Damage1 = 20
    aphelion_starfire_projectile.Damage2 = 15
    aphelion_starfire_projectile.Damage3 = 19
    aphelion_starfire_projectile.Damage4 = 17
    aphelion_starfire_projectile.Acceleration = 11
    aphelion_starfire_projectile.AccelerationUntil = 5f
    aphelion_starfire_projectile.InitialVelocity = 45
    aphelion_starfire_projectile.Lifespan = 7f
    aphelion_starfire_projectile.ProjectileDamageType = DamageType.Aggravated
    aphelion_starfire_projectile.Aggravated = AggravatedDamage(
      AggravatedInfo(DamageType.Direct, 0.25f, 250),
      Aura.None,
      2000,
      0f,
      true,
      List(TargetValidation(EffectTarget.Category.Aircraft, EffectTarget.Validation.Aircraft))
    )
    aphelion_starfire_projectile.registerAs = "rc-projectiles"
    aphelion_starfire_projectile.ExistsOnRemoteClients = true
    aphelion_starfire_projectile.RemoteClientData = (39577, 249) //starfire_projectile data
    aphelion_starfire_projectile.AutoLock = true
    aphelion_starfire_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(aphelion_starfire_projectile)
    aphelion_starfire_projectile.Modifiers = List(
      StarfireAggravated,
      StarfireAggravatedBurn
    )

    colossus_100mm_projectile.Name = "colossus_100mm_projectile"
    colossus_100mm_projectile.Damage0 = 58
    colossus_100mm_projectile.Damage1 = 330
    colossus_100mm_projectile.Damage2 = 300
    colossus_100mm_projectile.Damage3 = 165
    colossus_100mm_projectile.Damage4 = 190
    colossus_100mm_projectile.DamageAtEdge = 0.1f
    colossus_100mm_projectile.DamageRadius = 5f
    colossus_100mm_projectile.ProjectileDamageType = DamageType.Splash
    colossus_100mm_projectile.InitialVelocity = 100
    colossus_100mm_projectile.Lifespan = 4f
    ProjectileDefinition.CalculateDerivedFields(colossus_100mm_projectile)
    colossus_100mm_projectile.Modifiers = RadialDegrade

    colossus_burster_projectile.Name = "colossus_burster_projectile"
    // TODO for later, maybe : set_resource_parent colossus_burster_projectile game_objects burster_projectile
    colossus_burster_projectile.Damage0 = 18
    colossus_burster_projectile.Damage1 = 26
    colossus_burster_projectile.Damage2 = 18
    colossus_burster_projectile.Damage3 = 22
    colossus_burster_projectile.Damage4 = 20
    colossus_burster_projectile.DamageAtEdge = 0.1f
    colossus_burster_projectile.DamageRadius = 7f
    colossus_burster_projectile.ProjectileDamageType = DamageType.Direct
    colossus_burster_projectile.ProjectileDamageTypeSecondary = DamageType.Splash
    colossus_burster_projectile.InitialVelocity = 175
    colossus_burster_projectile.Lifespan = 2.5f
    ProjectileDefinition.CalculateDerivedFields(colossus_burster_projectile)
    colossus_burster_projectile.Modifiers = List(
      //FlakHit,
      FlakBurst,
      MaxDistanceCutoff
    )

    colossus_chaingun_projectile.Name = "colossus_chaingun_projectile"
    // TODO for later, maybe : set_resource_parent colossus_chaingun_projectile game_objects 35mmbullet_projectile
    colossus_chaingun_projectile.Damage0 = 15
    colossus_chaingun_projectile.Damage1 = 14
    colossus_chaingun_projectile.Damage2 = 15
    colossus_chaingun_projectile.Damage3 = 13
    colossus_chaingun_projectile.Damage4 = 11
    colossus_chaingun_projectile.ProjectileDamageType = DamageType.Direct
    colossus_chaingun_projectile.DegradeDelay = .100f
    colossus_chaingun_projectile.DegradeMultiplier = 0.44f
    colossus_chaingun_projectile.InitialVelocity = 500
    colossus_chaingun_projectile.Lifespan = .50f
    ProjectileDefinition.CalculateDerivedFields(colossus_chaingun_projectile)

    colossus_cluster_bomb_projectile.Name = "colossus_cluster_bomb_projectile"
    colossus_cluster_bomb_projectile.Damage0 = 40
    colossus_cluster_bomb_projectile.Damage1 = 88
    colossus_cluster_bomb_projectile.Damage2 = 100
    colossus_cluster_bomb_projectile.Damage3 = 83
    colossus_cluster_bomb_projectile.Damage4 = 88
    colossus_cluster_bomb_projectile.DamageAtEdge = 0.1f
    colossus_cluster_bomb_projectile.DamageRadius = 8f
    colossus_cluster_bomb_projectile.ProjectileDamageType = DamageType.Splash
    colossus_cluster_bomb_projectile.InitialVelocity = 75
    colossus_cluster_bomb_projectile.Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(colossus_cluster_bomb_projectile)
    colossus_cluster_bomb_projectile.Modifiers = RadialDegrade

    colossus_tank_cannon_projectile.Name = "colossus_tank_cannon_projectile"
    // TODO for later, maybe : set_resource_parent colossus_tank_cannon_projectile game_objects 75mmbullet_projectile
    colossus_tank_cannon_projectile.Damage0 = 33
    colossus_tank_cannon_projectile.Damage1 = 90
    colossus_tank_cannon_projectile.Damage2 = 95
    colossus_tank_cannon_projectile.Damage3 = 71
    colossus_tank_cannon_projectile.Damage4 = 66
    colossus_tank_cannon_projectile.DamageAtEdge = 0.1f
    colossus_tank_cannon_projectile.DamageRadius = 2f
    colossus_tank_cannon_projectile.ProjectileDamageType = DamageType.Splash
    colossus_tank_cannon_projectile.InitialVelocity = 165
    colossus_tank_cannon_projectile.Lifespan = 2f
    ProjectileDefinition.CalculateDerivedFields(colossus_tank_cannon_projectile)
    colossus_tank_cannon_projectile.Modifiers = RadialDegrade

    peregrine_dual_machine_gun_projectile.Name = "peregrine_dual_machine_gun_projectile"
    // TODO for later, maybe : set_resource_parent peregrine_dual_machine_gun_projectile game_objects 35mmbullet_projectile
    peregrine_dual_machine_gun_projectile.Damage0 = 16
    peregrine_dual_machine_gun_projectile.Damage1 = 44
    peregrine_dual_machine_gun_projectile.Damage2 = 30
    peregrine_dual_machine_gun_projectile.Damage3 = 27
    peregrine_dual_machine_gun_projectile.Damage4 = 32
    peregrine_dual_machine_gun_projectile.ProjectileDamageType = DamageType.Direct
    peregrine_dual_machine_gun_projectile.DegradeDelay = .25f
    peregrine_dual_machine_gun_projectile.DegradeMultiplier = 0.65f
    peregrine_dual_machine_gun_projectile.InitialVelocity = 250
    peregrine_dual_machine_gun_projectile.Lifespan = 1.1f
    ProjectileDefinition.CalculateDerivedFields(peregrine_dual_machine_gun_projectile)

    peregrine_mechhammer_projectile.Name = "peregrine_mechhammer_projectile"
    peregrine_mechhammer_projectile.Damage0 = 5
    peregrine_mechhammer_projectile.Damage1 = 4
    peregrine_mechhammer_projectile.Damage2 = 4
    peregrine_mechhammer_projectile.Damage3 = 5
    peregrine_mechhammer_projectile.Damage4 = 3
    peregrine_mechhammer_projectile.ProjectileDamageType = DamageType.Direct
    peregrine_mechhammer_projectile.InitialVelocity = 500
    peregrine_mechhammer_projectile.Lifespan = 0.4f
    ProjectileDefinition.CalculateDerivedFields(peregrine_mechhammer_projectile)

    peregrine_particle_cannon_projectile.Name = "peregrine_particle_cannon_projectile"
    peregrine_particle_cannon_projectile.Damage0 = 70
    peregrine_particle_cannon_projectile.Damage1 = 525
    peregrine_particle_cannon_projectile.Damage2 = 350
    peregrine_particle_cannon_projectile.Damage3 = 318
    peregrine_particle_cannon_projectile.Damage4 = 310
    peregrine_particle_cannon_projectile.DamageAtEdge = 0.1f
    peregrine_particle_cannon_projectile.DamageRadius = 3f
    peregrine_particle_cannon_projectile.ProjectileDamageType = DamageType.Splash
    //peregrine_particle_cannon_projectile.DamageProxy = 655 //peregrine_particle_cannon_radiation_cloud
    peregrine_particle_cannon_projectile.InitialVelocity = 500
    peregrine_particle_cannon_projectile.Lifespan = .6f
    ProjectileDefinition.CalculateDerivedFields(peregrine_particle_cannon_projectile)
    peregrine_particle_cannon_projectile.Modifiers = RadialDegrade

    peregrine_particle_cannon_radiation_cloud.Name = "peregrine_particle_cannon_radiation_cloud"
    peregrine_particle_cannon_radiation_cloud.Damage0 = 1
    peregrine_particle_cannon_radiation_cloud.DamageAtEdge = 1.0f
    peregrine_particle_cannon_radiation_cloud.DamageRadius = 3f
    peregrine_particle_cannon_radiation_cloud.radiation_cloud = true
    peregrine_particle_cannon_radiation_cloud.ProjectileDamageType = DamageType.Radiation
    peregrine_particle_cannon_radiation_cloud.DamageThroughWalls = true
    peregrine_particle_cannon_radiation_cloud.Lifespan = 5.0f
    ProjectileDefinition.CalculateDerivedFields(peregrine_particle_cannon_radiation_cloud)
    peregrine_particle_cannon_radiation_cloud.registerAs = "rc-projectiles"
    peregrine_particle_cannon_radiation_cloud.ExistsOnRemoteClients = true
    peregrine_particle_cannon_radiation_cloud.Packet = radCloudConverter
    //peregrine_particle_cannon_radiation_cloud.Geometry = GeometryForm.representProjectileBySphere()
    peregrine_particle_cannon_radiation_cloud.Modifiers = List(
      MaxDistanceCutoff,
      ShieldAgainstRadiation
    )

    peregrine_rocket_pod_projectile.Name = "peregrine_rocket_pod_projectile"
    peregrine_rocket_pod_projectile.Damage0 = 30
    peregrine_rocket_pod_projectile.Damage1 = 50
    peregrine_rocket_pod_projectile.Damage2 = 50
    peregrine_rocket_pod_projectile.Damage3 = 45
    peregrine_rocket_pod_projectile.Damage4 = 40
    peregrine_rocket_pod_projectile.Acceleration = 10
    peregrine_rocket_pod_projectile.AccelerationUntil = 2f
    peregrine_rocket_pod_projectile.DamageAtEdge = 0.1f
    peregrine_rocket_pod_projectile.DamageRadius = 3f
    peregrine_rocket_pod_projectile.ProjectileDamageType = DamageType.Splash
    peregrine_rocket_pod_projectile.InitialVelocity = 200
    peregrine_rocket_pod_projectile.Lifespan = 1.85f
    ProjectileDefinition.CalculateDerivedFields(peregrine_rocket_pod_projectile)
    peregrine_rocket_pod_projectile.Modifiers = RadialDegrade

    peregrine_sparrow_projectile.Name = "peregrine_sparrow_projectile"
    // TODO for later, maybe : set_resource_parent peregrine_sparrow_projectile game_objects sparrow_projectile
    peregrine_sparrow_projectile.Damage0 = 20
    peregrine_sparrow_projectile.Damage1 = 40
    peregrine_sparrow_projectile.Damage2 = 30
    peregrine_sparrow_projectile.Damage3 = 30
    peregrine_sparrow_projectile.Damage4 = 31
    peregrine_sparrow_projectile.Acceleration = 12
    peregrine_sparrow_projectile.AccelerationUntil = 5f
    peregrine_sparrow_projectile.DamageAtEdge = 0.1f
    peregrine_sparrow_projectile.DamageRadius = 2f
    peregrine_sparrow_projectile.ProjectileDamageType = DamageType.Splash
    peregrine_sparrow_projectile.InitialVelocity = 45
    peregrine_sparrow_projectile.Lifespan = 7.5f
    peregrine_sparrow_projectile.registerAs = "rc-projectiles"
    peregrine_sparrow_projectile.ExistsOnRemoteClients = true
    peregrine_sparrow_projectile.RemoteClientData = (13107, 187) //sparrow_projectile data
    peregrine_sparrow_projectile.AutoLock = true
    peregrine_sparrow_projectile.Packet = projectileConverter
    ProjectileDefinition.CalculateDerivedFields(peregrine_sparrow_projectile)
    peregrine_sparrow_projectile.Modifiers = RadialDegrade

    armor_siphon_projectile.Name = "armor_siphon_projectile"
    armor_siphon_projectile.Damage0 = 0
    armor_siphon_projectile.Damage1 = 20 //ground vehicles, siphon drain
    armor_siphon_projectile.Damage2 = 20 //aircraft, siphon drain
    armor_siphon_projectile.Damage3 = 0
    armor_siphon_projectile.Damage4 = 20 //bfr's, siphon drain
    armor_siphon_projectile.DamageToVehicleOnly = true
    armor_siphon_projectile.DamageToBattleframeOnly = true
    armor_siphon_projectile.DamageRadius = 35f
    armor_siphon_projectile.ProjectileDamageType = DamageType.Siphon
    ProjectileDefinition.CalculateDerivedFields(armor_siphon_projectile)
    armor_siphon_projectile.Modifiers = List(
      ArmorSiphonMaxDistanceCutoff,
      ArmorSiphonRepairHost
    )

    ntu_siphon_emp.Name = "ntu_siphon_emp"
    ntu_siphon_emp.Damage0 = 1
    ntu_siphon_emp.Damage1 = 1
    ntu_siphon_emp.DamageAtEdge = 0.1f
    ntu_siphon_emp.DamageRadius = 50f
    ntu_siphon_emp.ProjectileDamageType = DamageType.Splash
    ntu_siphon_emp.AdditionalEffect = true
    ntu_siphon_emp.SympatheticExplosion = true
    ntu_siphon_emp.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Player,
      EffectTarget.Validation.Player
    ) -> 1000
    ntu_siphon_emp.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.AMS
    ) -> 5000
    ntu_siphon_emp.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.MotionSensor
    ) -> 30000
    ntu_siphon_emp.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.Spitfire
    ) -> 30000
    ntu_siphon_emp.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Turret,
      EffectTarget.Validation.Turret
    ) -> 30000
    ntu_siphon_emp.JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.VehicleNotAMS
    ) -> 10000
    ProjectileDefinition.CalculateDerivedFields(ntu_siphon_emp)
    ntu_siphon_emp.Modifiers = MaxDistanceCutoff
  }
}
