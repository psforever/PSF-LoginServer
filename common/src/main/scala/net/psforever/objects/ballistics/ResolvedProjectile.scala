// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.types.Vector3

final case class ResolvedProjectile(resolution : ProjectileResolution.Value,
                                    projectile : Projectile,
                                    target : SourceEntry,
                                    hit_pos : Vector3,
                                    hit_time : Long = System.nanoTime)
