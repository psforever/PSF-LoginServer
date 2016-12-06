// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import scodec.DecodeResult
import scodec.bits.BitVector

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
      val codec = ObjectClass.selectDataCodec(objClass)
      var outOpt : Option[DecodeResult[_]] = None
      try {
        outOpt = codec.decode(data).toOption
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
      val codec = ObjectClass.selectDataCodec(objClass)
      var outOpt : Option[BitVector] = None
      outOpt = codec.encode(obj.asInstanceOf[ConstructorData.genericPattern]).toOption
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
