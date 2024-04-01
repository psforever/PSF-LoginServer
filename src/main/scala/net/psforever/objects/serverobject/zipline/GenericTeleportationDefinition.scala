// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.zipline

import net.psforever.objects.definition.ObjectDefinition

/**
 * The definition for any generic teleportation entity.
 * Two entities are described by this object definition - zip lines and the teleportation rings, both in the caverns.
 * Entities of these objects are environmental, like facilities, do not get constructed by the user.
 * The said entities also do not require being configured at login or zone load.
 */
class GenericTeleportationDefinition(private val objectId: Int) extends ObjectDefinition(objectId)
