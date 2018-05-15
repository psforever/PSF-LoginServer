// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.{AvatarDefinition, ImplantDefinition}
import net.psforever.objects.equipment.EquipmentSize
import net.psforever.objects.loadouts.Loadout
import net.psforever.types.{CertificationType, CharacterGender, ImplantType, PlanetSideEmpire}

import scala.annotation.tailrec
import scala.collection.mutable

class Avatar(val name : String, val faction : PlanetSideEmpire.Value, val sex : CharacterGender.Value, val head : Int, val voice : Int) {
  /** Battle Experience Points */
  private var bep : Long = 0
  /** Command Experience Points */
  private var cep : Long = 0
  /** Certifications */
  private val certs : mutable.Set[CertificationType.Value] = mutable.Set[CertificationType.Value]()
  /** Implants<br>
    * Unlike other objects, all `ImplantSlot` objects are already built into the `Avatar`.
    * Additionally, implants do not have tightly-coupled "`Definition` objects" that explain a formal implant object.
    * The `ImplantDefinition` objects themselves are moved around as if they were the implants.
    * The terms externally used for the states of process is "installed" and "uninstalled."
    * @see `ImplantSlot`
    * @see `DetailedCharacterData.implants`
    */
  private val implants : Array[ImplantSlot] = Array.fill[ImplantSlot](3)(new ImplantSlot)
  /** Loadouts<br>
    * 0-9 are Infantry loadouts
    * 10-14 are Vehicle loadouts
    */
  private val loadouts : Array[Option[Loadout]] = Array.fill[Option[Loadout]](15)(None)
  /** Locker */
  private val locker : LockerContainer = new LockerContainer() {
    override def toString : String = {
      s"$name's ${Definition.Name}"
    }
  }

  def BEP : Long = bep

  def BEP_=(battleExperiencePoints : Long) : Long = {
    bep = math.max(0L, math.min(battleExperiencePoints, 4294967295L))
    BEP
  }

  def Certifications : mutable.Set[CertificationType.Value] = certs

  def CEP : Long = cep

  def CEP_=(commandExperiencePoints : Long) : Long = {
    cep = math.max(0L, math.min(commandExperiencePoints, 4294967295L))
    CEP
  }

  /**
    * Retrieve the three implant slots for this player.
    * @return an `Array` of `ImplantSlot` objects
    */
  def Implants : Array[ImplantSlot] = implants

  /**
    * What kind of implant is installed into the given slot number?
    * @see `ImplantType`
    * @param slot the slot number
    * @return the tye of implant
    */
  def Implant(slot : Int) : ImplantType.Value = {
    if(-1 < slot && slot < implants.length) { implants(slot).Implant } else { ImplantType.None }
  }

  /**
    * Given a new implant, assign it into a vacant implant slot on this player.<br>
    * <br>
    * The implant must be unique in terms of which implants have already been assigned to this player.
    * Multiple of a type of implant being assigned at once is not supported.
    * Additionally, the implant is inserted into the earliest yet-unknown but vacant slot.
    * Implant slots are vacant by just being unlocked or by having their previous implant uninstalled.
    * @param implant the implant being installed
    * @return the index of the `ImplantSlot` where the implant was installed
    */
  def InstallImplant(implant : ImplantDefinition) : Option[Int] = {
    implants.find({p => p.Installed.contains(implant) || p.Implant == implant.Type}) match { //try to find the installed implant
      case None =>
        recursiveFindImplantInSlot(implants.iterator, ImplantType.None) match { //install in a free slot
          case Some(slot) =>
            implants(slot).Implant = implant
            Some(slot)
          case None =>
            None
        }
      case Some(_) =>
        None
    }
  }

  /**
    * Remove a specific implant from a player's allocated installed implants.<br>
    * <br>
    * Due to the exclusiveness of installed implants,
    * any implant slot with a matching `Definition` can be uninstalled safely.
    * (There will never be any doubles.)
    * This operation can lead to an irregular pattern of installed and uninstalled `ImplantSlot` objects.
    * Despite that breach of pattern, the logic here is consistent as demonstrated by the client and by packets.
    * The client also assigns and removes implants based on slot numbers that only express availability of a "slot."
    * @see `AvatarImplantMessage.implantSlot`
    * @param implantType the type of implant being uninstalled
    * @return the index of the `ImplantSlot` where the implant was found and uninstalled
    */
  def UninstallImplant(implantType : ImplantType.Value) : Option[Int] = {
    recursiveFindImplantInSlot(implants.iterator, implantType) match {
      case Some(slot) =>
        implants(slot).Implant = None
        Some(slot)
      case None =>
        None
    }
  }

  /**
    * Locate the index of the encountered implant type.
    * Functional implants may be exclusive in as far as the input `Iterator`'s source is concerned,
    * but any number of `ImplantType.None` values are alway allowed in the source in any order.
    * @param iter an `Iterator` of `ImplantSlot` objects
    * @param implantType the target implant being sought
    * @param index a defaulted index value representing the structure underlying the `Iterator` param
    * @return the index where the target implant is installed
    */
  @tailrec private def recursiveFindImplantInSlot(iter : Iterator[ImplantSlot], implantType : ImplantType.Value, index : Int = 0) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val slot = iter.next
      if(slot.Unlocked && slot.Implant == implantType) {
        Some(index)
      }
      else {
        recursiveFindImplantInSlot(iter, implantType, index + 1)
      }
    }
  }

  def ResetAllImplants() : Unit = {
    implants.foreach(slot => {
      slot.Installed match {
        case Some(_) =>
          slot.Initialized = false
        case None => ;
      }
    })
  }

  def SaveLoadout(owner : Player, label : String, line : Int) : Unit = {
    if(line > -1 && line < 10) {
      loadouts(line) = Some(Loadout.Create(owner, label))
    }
  }

  def SaveLoadout(owner : Vehicle, label : String, line : Int) : Unit = {
    if(line > 9 && line < loadouts.length) {
      loadouts(line) = Some(Loadout.Create(owner, label))
    }
  }

  def LoadLoadout(line : Int) : Option[Loadout] = loadouts.lift(line).getOrElse(None)

  def DeleteLoadout(line : Int) : Unit = {
    loadouts(line) = None
  }

  def Locker : LockerContainer = locker

  def FifthSlot : EquipmentSlot = {
    new OffhandEquipmentSlot(EquipmentSize.Inventory) {
      Equipment = locker
    }
  }

  def Definition : AvatarDefinition = Avatar.definition

  /*
  Merit Commendations and Ribbons
   */
//  private var tosRibbon : MeritCommendation.Value = MeritCommendation.None
//  private var upperRibbon : MeritCommendation.Value = MeritCommendation.None
//  private var middleRibbon : MeritCommendation.Value = MeritCommendation.None
//  private var lowerRibbon : MeritCommendation.Value = MeritCommendation.None

  def canEqual(other: Any): Boolean = other.isInstanceOf[Avatar]

  override def equals(other : Any) : Boolean = other match {
    case that: Avatar =>
      (that canEqual this) &&
        name == that.name &&
        faction == that.faction &&
        sex == that.sex &&
        head == that.head &&
        voice == that.voice
    case _ =>
      false
  }

  override def hashCode() : Int = {
    val state = Seq(name, faction, sex, head, voice)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString: String = Avatar.toString(this)
}

object Avatar {
  final private val definition : AvatarDefinition = new AvatarDefinition(121)

  def apply(name : String, faction : PlanetSideEmpire.Value, sex : CharacterGender.Value, head : Int, voice : Int) : Avatar = {
    new Avatar(name, faction, sex, head, voice)
  }

  def toString(avatar : Avatar) : String = s"${avatar.faction} ${avatar.name}"
}
