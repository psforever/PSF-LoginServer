// Copyright (c) 2017-2020 PSForever
package net.psforever.objects.entity

import net.psforever.types.PlanetSideGUID

/**
  * The base for all complaints that can be raised regarding the management of global unique identifier numbers.
  * @param message the message string
  * @param cause the cause of this error
  * @param obj the entity being manipulated when the complaint arose
  * @param guid the identifier number being manipulated when the ciomplaint arose
  */
abstract class GUIDException(message : String, cause : Throwable, obj : IdentifiableEntity, guid : PlanetSideGUID)
  extends RuntimeException(message, cause) {
  private val entity : IdentifiableEntity = obj
  def getEntity : IdentifiableEntity = entity

  private val entityGUID : PlanetSideGUID = guid
  def getGUID : PlanetSideGUID = entityGUID
}

/**
  * The specific complaint for an instance where an entity does not possess a global unique identifier number
  * but the said number is requested.
  * In general, this `Exception` is only thrown if the entity has never been registered,
  * or provided the bare minimum or registration benefits.
  * @param message the message string
  * @param obj the entity being manipulated when the complaint arose
  * @param cause the cause of this error
  */
class NoGUIDException(message : String,
                      obj : IdentifiableEntity = None.orNull,
                      cause : Throwable = None.orNull
                     ) extends GUIDException(message, cause, obj, null)

object NoGUIDException {
  def unapply(e : NoGUIDException): Option[(String, IdentifiableEntity, Throwable)] = Some((e.getMessage, e.getEntity, e.getCause))
}

/**
  * The general complaint for an instance where an entity can not be assigned the given global unique identifier number.
  * @param message the message string
  * @param obj the entity being manipulated when the complaint arose
  * @param guid the identifier number being manipulated when the ciomplaint arose
  * @param cause the cause of this error
  */
class AssigningGUIDException(message : String,
                             obj : IdentifiableEntity,
                             guid : PlanetSideGUID,
                             cause : Throwable = None.orNull
                            ) extends GUIDException(message, cause, obj, guid)

object AssigningGUIDException {
  def unapply(e : AssigningGUIDException): Option[(String, Throwable, IdentifiableEntity, PlanetSideGUID)] = Some((e.getMessage, e.getCause, e.getEntity, e.getGUID))
}
