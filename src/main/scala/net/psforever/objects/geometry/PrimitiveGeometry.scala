// Copyright (c) 2021 PSForever
package net.psforever.objects.geometry

import net.psforever.types.Vector3

/**
  * Basic interface for all geometry.
  */
trait PrimitiveGeometry {
  /**
    * The centroid of the geometry.
    * @return a point
    */
  def center: Point

  /**
    * Move the centroid of the shape to the given point
    * @param point the new center point
    * @return geometry centered on the new point;
    *         ideally, should be the same type of geometry as the original object
    */
  def moveCenter(point: Point): PrimitiveGeometry
}

/**
  * Characteristics of a geometric figure with only three coordinates to define a position.
  */
trait Point {
  /**
    * Transform the point into the common interchangeable format for coordinates.
    * They're very similar, anyway.
    * @return a `Vector3` entity of the same denomination
    */
  def asVector3: Vector3
}

/**
  * Characteristics of a geometric figure defining a direction or a progressive change in coordinates.
  */
trait Slope {
  /**
    * The slope itself.
    * @return a `Vector3` entity
    */
  def d: Vector3

  /**
    * How long the slope goes on for.
    * @return The length of the slope
    */
  def length: Float
}

object Slope {
  /**
    * On occasions, the defined slope should have a length of one unit.
    * It is a unit vector.
    * @param v the input slope as a `Vector3` entity
    * @throws `AssertionError` if the length is more or less than 1.
    */
  def assertUnitVector(v: Vector3): Unit = {
    assert({
      val mag = Vector3.Magnitude(v)
      mag - 0.05f < 1f && mag + 0.05f > 1f
    }, "not a unit vector")
  }
}

/**
  * Characteristics of a geometric figure indicating an infinite slope - a mathematical line.
  * The slope is always a unit vector.
  * The point that assists to define the line is a constraint that the line must pass through.
  */
trait Line extends Slope {
  Slope.assertUnitVector(d)

  def p: Point

  /**
    * The length of a mathematical line is infinite.
    * @return The length of the slope
    */
  def length: Float = Float.PositiveInfinity
}

/**
  * Characteristics of a geometric figure that have two endpoints, defining a fixed-length slope.
  */
trait Segment extends Slope {
  /** The first point, considered the "start". */
  def p1: Point
  /** The second point, considered the "end". */
  def p2: Point

  def length: Float = Vector3.Magnitude(d)

  /**
    * Transform the segment into a matheatical line of the same slope.
    * @return
    */
  def asLine: PrimitiveGeometry
}
