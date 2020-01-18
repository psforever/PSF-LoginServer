// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject

import akka.actor.ActorContext
import net.psforever.objects.guid.NumberPoolHub

/**
  * Customizable native entity instantiation logic at the `ZoneMap` level.
  * Produces environmental constants such as `Terminal` objects and `Door` objects.<br>
  * <br>
  * `ZoneMap` is the expected position where this class is defined.
  * Within `Zone.Init` is where this class is expected to be fully executed.
  * The former is a blueprint which provides as many emitted entities based on how many `Zone` objects utilize it.
  * `constructor` is required to have the form `(Int, ActorContext) => A` by the point where it executes
  * due to invocation of the `Build` method.
  * For that reason, it must exist in an `Actor` which has an `ActorContext` to lend, hence the `Zone`'s `Actor`.
  * Furthermore, the requirement of a `NumberPoolHub` means the region is accessible to and defined by
  * a closed number space, which is also the `Zone`.
  * It utilizes those qualities of the enclosing region to construct the entity within that region.<br>
  * <br>
  * Example: `ServerObjectBuilder(n, function)`<br>
  * Example: `new ServerBuilderObject[A](n, function)`, where `function` is a `(Int,Context)=>A`
  * @see `ZoneMap`
  * @see `Zone.Init`
  * @param id the unique id that will be assigned to this entity
  * @param constructor the logic that initializes the emitted entity
  * @tparam A any object that extends from PlanetSideServerObject that will be produced by this class;
  *           can be inferred from the output of `constructor`
  */
class ServerObjectBuilder[A <: PlanetSideServerObject](private val id : Int,
                                                       private val constructor : ServerObjectBuilder.ConstructorType[A]
                                                      ) {

  def Id : Int = id

  /**
    * Instantiate and configure the given server object.
    * Specific configuration should have been handled by curried parameters into `constructor`, i.e.,
    * `constructor(foo : Bar) => constructor(Int, ActorContext) => A`.
    * The main activity performed locally registers the created object to the provided number space.
    * @param context a context to allow the object to properly set up `ActorSystem` functionality;
    *                defaults to `null`
    * @param guid the local globally unique identifier system to complete the process of object introduction;
    *             defaults to `null`
    * @return the object that was created and integrated into the `Zone`
    */
  def Build(implicit context : ActorContext = null, guid : NumberPoolHub = null) : A = {
    val obj : A = constructor(id, context)
    guid.register(obj, id)
    obj
  }
}

object ServerObjectBuilder {
  type ConstructorType[A <: PlanetSideServerObject] = (Int, ActorContext)=>A

  /**
    * Overloaded constructor.
    * @param id the unqiue id that will be assigned to this entity
    * @param constructor the logic that initializes the emitted entity
    * @tparam A any object that extends from PlanetSideServerObject that will be produced by this class
    * @return a `ServerObjectBuilder` object
    */
  def apply[A <: PlanetSideServerObject](id : Int, constructor : ConstructorType[A]) : ServerObjectBuilder[A] = {
    new ServerObjectBuilder[A](id, constructor)
  }
}
