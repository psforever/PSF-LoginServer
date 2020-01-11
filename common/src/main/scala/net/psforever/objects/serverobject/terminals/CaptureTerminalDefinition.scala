package net.psforever.objects.serverobject.terminals

import net.psforever.objects.definition.ObjectDefinition

class CaptureTerminalDefinition(objectId : Int) extends ObjectDefinition(objectId) {
  Name = objectId match {
    case 158 => "capture_terminal"
    case 751 => "secondary_capture"
    case 930 => "vanu_control_console"
    case _ => throw new IllegalArgumentException("Not a valid capture terminal object id")
  }
}
