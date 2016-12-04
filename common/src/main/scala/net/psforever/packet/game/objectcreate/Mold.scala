// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import scodec.DecodeResult
import scodec.bits.BitVector

import scala.annotation.switch

case class Mold(objectClass : Int,
                data : BitVector) {
  private var obj : Option[ConstructorData] = Mold.decode(objectClass, data)

  def isDefined : Boolean = this.obj.isDefined

  def get : ConstructorData = this.obj.get

  def set(data : ConstructorData) : Boolean = {
    var ret = false
    if(Some(data).isDefined) {
      obj = Some(data)
      ret = true
    }
    ret
  }
}

object Mold {
  def apply(objectClass : Int, obj : ConstructorData) : Mold =
    new Mold( objectClass, Mold.encode(objectClass, obj) )

  def decode(objClass : Int, data : BitVector) : Option[ConstructorData] = {
    var out : Option[ConstructorData] = None
    if(!data.isEmpty) {
      var outOpt : Option[DecodeResult[_]] = None
      try {
        (objClass : @switch) match {
          case 0x79 => //avatars
            outOpt = CharacterData.codec.decode(data).toOption
          case 0x1C => //9mm
            outOpt = AmmoBoxData.codec.decode(data).toOption
          case 0x1D => //9mm ap
            outOpt = AmmoBoxData.codec.decode(data).toOption
          case 0x110 => //plasma
            outOpt = AmmoBoxData.codec.decode(data).toOption
          case 0x1C8 => //slot blocker?
            outOpt = AmmoBoxData.codec.decode(data).toOption
          case 0x21C => //forceblade (ammo)
            outOpt = AmmoBoxData.codec.decode(data).toOption
          case 0x46 => //beamer
            outOpt = WeaponData.codec.decode(data).toOption
          case 0x144 => //forceblade
            outOpt = WeaponData.codec.decode(data).toOption
          case 0x159 => //gauss
            outOpt = WeaponData.codec.decode(data).toOption
          case 0x34D => //suppressor
            outOpt = WeaponData.codec.decode(data).toOption
          case 0x2D8 => //rek
            outOpt = REKData.codec.decode(data).toOption
          case _ =>
        }
        if(outOpt.isDefined)
          out = Some(outOpt.get.value.asInstanceOf[ConstructorData])
      }
      catch {
        case ex : ClassCastException =>
        //TODO generate and log wrong class error message
        case ex : Exception =>
        //TODO generic error
      }
    }
    out
  }

  def encode(objClass : Int, obj : ConstructorData) : BitVector = {
    var out = BitVector.empty
    try {
      var outOpt : Option[BitVector] = None
      (objClass : @switch) match {
        case 0x1C => //9mm
          outOpt = AmmoBoxData.codec.encode(obj.asInstanceOf[AmmoBoxData]).toOption
        case 0x1D => //9mm ap
          outOpt = AmmoBoxData.codec.encode(obj.asInstanceOf[AmmoBoxData]).toOption
        case 0x110 => //plasma
          outOpt = AmmoBoxData.codec.encode(obj.asInstanceOf[AmmoBoxData]).toOption
        case 0x1C8 => //slot blocker?
          outOpt = AmmoBoxData.codec.encode(obj.asInstanceOf[AmmoBoxData]).toOption
        case 0x21C => //forceblade (ammo)
          outOpt = AmmoBoxData.codec.encode(obj.asInstanceOf[AmmoBoxData]).toOption
        case 0x46 => //beamer
          outOpt = WeaponData.codec.encode(obj.asInstanceOf[WeaponData]).toOption
        case 0x144 => //forceblade
          outOpt = WeaponData.codec.encode(obj.asInstanceOf[WeaponData]).toOption
        case 0x159 => //gauss
          outOpt = WeaponData.codec.encode(obj.asInstanceOf[WeaponData]).toOption
        case 0x34D => //suppressor
          outOpt = WeaponData.codec.encode(obj.asInstanceOf[WeaponData]).toOption
        case 0x2D8 => //rek
          outOpt = REKData.codec.encode(obj.asInstanceOf[REKData]).toOption
        case _ =>
          throw new ClassCastException("cannot find object code - "+objClass)
      }
      if(outOpt.isDefined)
        out = outOpt.get
    }
    catch {
      case ex : ClassCastException =>
      //TODO generate and log wrong class error message
      case ex : Exception =>
      //TODO generic error
    }
    out
  }
}
