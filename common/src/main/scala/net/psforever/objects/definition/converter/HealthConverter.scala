package net.psforever.objects.definition.converter

object HealthConverter {
  def apply(health : Int, maxHealth : Int, min : Int = 3, max : Int = 255) : Int = {
    if(health < 1) {
      0
    }
    else if(health == 1) {
      min
    }
    else if(health >= maxHealth) {
      max
    }
    else {
      math.ceil(max * health / maxHealth).toInt
    }
  }
}
