package net.psforever.objects.definition.converter

object StatConverter {
  /**
    * Takes a measure of a value against the maximum possible value and
    * transforms it to a scaled number that can be written within a specific domain.<br>
    * <br>
    * The default (and absolutely common) situation writes a scaled number that can be represented by an unsigned `8u`.
    * The maximum value is 255, or 2^8^ - 1.
    * The minimum value is 0;
    * but, due to how game models are represented at various health,
    * the representable minimum value is allowed to plateau at 3.
    * Any result less than 3 creates the same situation as if the result were 0.
    */
  def Health(health : Int, maxHealth : Int, min : Int = 3, max : Int = 255) : Int =
    if(health < 1) 0
    else if(health <= min || min >= max) min
    else if(health >= maxHealth) max
    else math.floor(max * health / maxHealth).toInt
}
