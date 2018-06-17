package net.psforever.objects.serverobject.terminals

import net.psforever.objects.definition.ObjectDefinition

class CaptureTerminalDefinition(objectId : Int) extends ObjectDefinition(objectId) {
  Name = if(objectId == 158) {
    "capture_terminal"
  } else if (objectId == 751) {
    "secondary_capture"
  } else {
    throw new IllegalArgumentException("Not a valid capture terminal object id")
  }
}
