// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

object CItem {
  object Unit extends Enumeration {
    final val ace = Value(32)
    final val advanced_ace = Value(39) //fdu
    final val router_telepad = Value(743)
  }

  object DeployedItem extends Enumeration {
    final val boomer = Value(148)
    final val deployable_shield_generator = Value(240)
    final val he_mine = Value(388)
    final val jammer_mine = Value(420) //disruptor mine
    final val motionalarmsensor = Value(575)
    final val sensor_shield = Value(752) //sensor disruptor
    final val spitfire_aa = Value(819) //cerebus turret
    final val spitfire_cloaked = Value(825) //shadow turret
    final val spitfire_turret = Value(826)
    final val tank_traps = Value(849) //trap
    final val portable_manned_turret = Value(685)
    final val portable_manned_turret_nc = Value(686)
    final val portable_manned_turret_tr = Value(687)
    final val portable_manned_turret_vs = Value(688)
    final val router_telepad_deployable = Value(744)
  }
}
