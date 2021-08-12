//Copyright (c) 2021 PSForever
package net.psforever.objects.vital

class CollisionData() {
  var xy: CollisionXYData = CollisionXYData()
  var z: CollisionZData = CollisionZData()
  var collisionDamageMultiplier: Float = 1f
}

class ExosuitCollisionData() {
  var forceFactor: Float = 1f
  var massFactor: Float = 1f
}

class AdvancedCollisionData() extends CollisionData() {
  var avatarCollisionDamageMax: Int = Int.MaxValue

  //I don't know what to do with these, so they will go here for now
  var minHp: Float = 1f
  var maxHp: Float = 10f
  var minForce: Float = 15f
  var maxForce: Float = 50f
}

trait CollisionDoesDamage {
  def hp(): List[Int]

  def hp(d: Int): Int = {
    val _hp = hp()
    _hp.lift(d) match {
      case Some(n) => n
      case None    => _hp.last
    }
  }
}

final case class CollisionZData(data: Iterable[(Float, Int)])
  extends CollisionDoesDamage {
  assert(data.nonEmpty, "some collision data must be defined")

  def height(): List[Float] = data.unzip._1.toList

  override def hp(): List[Int] = data.unzip._2.toList

  def height(z: Float): Int = {
    val n = data.toArray.indexWhere { case (h, _) => h > z }
    if (n == -1) {
      data.size - 1
    } else {
      n
    }
  }
}

object CollisionZData {
  def apply(): CollisionZData = CollisionZData(Array((0f,0)))
}

final case class CollisionXYData(data: Iterable[(Float, Int)])
  extends CollisionDoesDamage {
  assert(data.nonEmpty, "some collision data must be defined")

  def throttle(): List[Float] = data.unzip._1.toList

  override def hp(): List[Int] = data.unzip._2.toList

  def throttle(z: Float): Int = {
    val n = data.toArray.indexWhere { case (h, _) => h > z }
    if (n == -1) {
      data.size - 1
    } else {
      n
    }
  }
}

object CollisionXYData {
  def apply(): CollisionXYData = CollisionXYData(Array((0f,0)))
}
