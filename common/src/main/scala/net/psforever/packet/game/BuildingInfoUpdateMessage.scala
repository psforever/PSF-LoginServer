// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideEmpire
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * An `Enumeration` `Codec` that represents that various states of a major facility's Generator.
  */
object PlanetSideGeneratorState extends Enumeration {
  type Type = Value
  val Normal,
      Critical,
      Destroyed,
      Unk3
       = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uintL(2))
}

/**
  * na
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  */
final case class Additional1(unk1 : String,
                             unk2 : Int,
                             unk3 : Long)

/**
  * na
  * @param unk1 na
  * @param unk2 na
  */
final case class Additional2(unk1 : Int,
                             unk2 : Long)

/**
  * na
  * @param unk1 na
  * @param unk2 na
  */
final case class Additional3(unk1 : Boolean,
                             unk2 : Int)

/**
  * Update the state of map asset for a client's specific building's state.
  * The most common application of this packet is to synchronize map state during world login.<br>
  * <br>
  * A "building" mainly constitutes any map-viewable structure that has properties or whose ownership can be asserted.
  * This packet is valid for all major facilities, field towers, warp gates, and some static environment elements.
  * Additional properties, if available, can be viewed by selecting the sphere of influence of a given building.
  * The combination of continent UID and building UID ensures that all buildings are uniquely-defined.
  * This packet can be applied on any continent and will affect the appropriate building on any other continent.
  * As the intercontinental map is always available, all map assets will publish real time updates to all players.
  * Map information configured by this packet is not obscured from any players, regardless of faction.
  * (Network state updates will be delayed for, and the type of compromise will not be specified to, defenders.)<br>
  * <br>
  * Aside from the map-viewable aspects, a few properties set by this packet also have game world effects.
  * Additionally, though most parameters are treated as mandatory, not all buildings will be able to use those parameters.
  * A parameter that is not applicable for a given asset, e.g., NTU for a field tower, will be ignored.
  * A collision between some parameters can occur.
  * For example, if `is_hacking` is `false`, the other hacking fields are considered invalid.
  * If `is_hacking` is `true` but the hacking empire is also the owning empire, the `is_hacking` state is invalid.<br>
  * <br>
  * Lattice benefits: (stackable)<br>
  * `
  * 00 - None<br>
  * 01 - Amp Station<br>
  * 02 - Dropship Center<br>
  * 04 - Bio Laboratory<br>
  * 08 - Interlink Facility<br>
  * 16 - Technology Plant<br>
  * `
  * <br>
  * Cavern benefits: (stackable)<br>
  * `
  * 000 - None<br>
  * 004 - Speed Module<br>
  * 008 - Shield Module<br>
  * 016 - Vehicle Module<br>
  * 032 - Equipment Module<br>
  * 064 - Health Module<br>
  * 128 - Pain Module<br>
  * `
  * @param continent_guid the continent (zone)
  * @param building_guid the building
  * @param ntu_level if the building has a silo, the amount of NTU in that silo;
  *                  NTU is reported in multiples of 10%;
  *                  valid for 0 (0%) to 10 (100%)
  * @param is_hacked if the building can be hacked and will take time to convert, whether the building is being hacked
  * @param empire_hack if the building is being hacked, the empire that is performing the hacking
  * @param hack_time_remaining if the building is being hacked, the amount of time remaining until the hack finishes/clears;
  *                            recorded in milliseconds (ms)
  * @param empire_own the empire that owns the building currently
  * @param unk1 na;
  *             value != 0 causes the next field to be defined
  * @param unk1x na
  * @param generator_state if the building has a generator, the state of the generator
  * @param spawn_tubes_normal if the building has spawn tubes, whether at least one of the tubes is powered and operational
  * @param force_dome_active if the building is a capitol facility, whether the force dome is active
  * @param lattice_benefit the benefits from other Lattice-linked bases does this building possess
  * @param cavern_benefit cavern benefits;
  *                        any non-zero value will cause the cavern module icon (yellow) to appear;
  *                        proper module values cause the cavern module icon to render green;
  *                        all benefits will report as due to a "Cavern Lock"
  * @param unk4 na
  * @param unk5 na
  * @param unk6 na
  * @param unk7 na;
  *             value != 8 causes the next field to be defined
  * @param unk7x na
  * @param boost_spawn_pain if the building has spawn tubes, the (boosted) strength of its enemy pain field
  * @param boost_generator_pain if the building has a generator, the (boosted) strength of its enemy pain field
  */
final case class BuildingInfoUpdateMessage(continent_guid : PlanetSideGUID,
                                           building_guid : PlanetSideGUID,
                                           ntu_level : Int,
                                           is_hacked : Boolean,
                                           empire_hack : PlanetSideEmpire.Value,
                                           hack_time_remaining : Long,
                                           empire_own : PlanetSideEmpire.Value,
                                           unk1 : Long,
                                           unk1x : Option[Additional1],
                                           generator_state : PlanetSideGeneratorState.Value,
                                           spawn_tubes_normal : Boolean,
                                           force_dome_active : Boolean,
                                           lattice_benefit : Int,
                                           cavern_benefit : Int,
                                           unk4 : List[Additional2],
                                           unk5 : Long,
                                           unk6 : Boolean,
                                           unk7 : Int,
                                           unk7x : Option[Additional3],
                                           boost_spawn_pain : Boolean,
                                           boost_generator_pain : Boolean)
  extends PlanetSideGamePacket {
  type Packet = BuildingInfoUpdateMessage
  def opcode = GamePacketOpcode.BuildingInfoUpdateMessage
  def encode = BuildingInfoUpdateMessage.encode(this)
}

object BuildingInfoUpdateMessage extends Marshallable[BuildingInfoUpdateMessage] {
  /**
    * A `Codec` for a set of additional fields.
    */
  private val additional1_codec : Codec[Additional1] = (
    ("unk1" | PacketHelpers.encodedWideStringAligned(3)) ::
      ("unk2" | uint8L) ::
      ("unk3" | uint32L)
    ).as[Additional1]

  /**
    * A `Codec` for a set of additional fields.
    */
  private val additional2_codec : Codec[Additional2] = (
    ("unk1" | uint4L) ::
      ("unk2" | uint32L)
    ).as[Additional2]

  /**
    * A `Codec` for a set of additional fields.
    */
  private val additional3_codec : Codec[Additional3] = (
    ("unk1" | bool) ::
      ("unk2" | uint2L)
    ).as[Additional3]

  implicit val codec : Codec[BuildingInfoUpdateMessage] = (
      ("continent_guid" | PlanetSideGUID.codec) ::
      ("building_guid" | PlanetSideGUID.codec) ::
      ("ntu_level" | uint4L) ::
      ("is_hacked" | bool ) ::
      ("empire_hack" | PlanetSideEmpire.codec) ::
      ("hack_time_remaining" | uint32L ) ::
      ("empire_own" | PlanetSideEmpire.codec) ::
      (("unk1" | uint32L) >>:~ { unk1 =>
        conditional(unk1 != 0L, "unk1x" | additional1_codec) ::
          ("generator_state" | PlanetSideGeneratorState.codec) ::
          ("spawn_tubes_normal" | bool) ::
          ("force_dome_active" | bool) ::
          ("lattice_benefit" | uintL(5)) ::
          ("cavern_benefit" | uintL(10)) ::
          ("unk4" | listOfN(uint4L, additional2_codec)) ::
          ("unk5" | uint32L) ::
          ("unk6" | bool) ::
          (("unk7" | uint4L) >>:~ { unk7 =>
            conditional(unk7 != 8, "unk7x" | additional3_codec) ::
              ("boost_spawn_pain" | bool) ::
              ("boost_generator_pain" | bool)
            })
      })
    ).exmap[BuildingInfoUpdateMessage] (
    {
      case a :: b :: c :: d :: e :: f :: g :: h :: i :: j :: k :: l :: m :: n :: o :: p :: q :: r :: s :: t :: u :: HNil =>
        Attempt.successful(BuildingInfoUpdateMessage(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u))
    },
    {
      case BuildingInfoUpdateMessage(_, _, _, _, _, _, _, 0, Some(x), _, _, _, _, _, _, _, _, _, _, _, _) =>
        Attempt.failure(Err("invalid properties when value == 0"))

      case BuildingInfoUpdateMessage(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, 8, Some(x), _, _) =>
        Attempt.failure(Err("invalid properties when value == 8"))

      case BuildingInfoUpdateMessage(a, b, c, d, e, f, g, h, i, j, k, l, m, n, lst, p, q, r, s, t, u) =>
        if(h != 0 && i.isEmpty) {
          Attempt.failure(Err(s"missing properties when value != 0 (actual: $h)")) //TODO can we recover by forcing value -> 0?
        }
        else if(r != 8 && s.isEmpty) {
          Attempt.failure(Err(s"missing properties when value != 8 (actual: $r)")) //TODO can we recover by forcing value -> 8?
        }
        val size = lst.size
        if(size > 15) {
          Attempt.failure(Err(s"too many elements in list (max: 15, actual: $size)"))
        }
        else {
          Attempt.successful(a :: b :: c :: d :: e :: f :: g :: h :: i :: j :: k :: l :: m :: n :: lst :: p :: q :: r :: s :: t :: u :: HNil)
        }
    }
  )
}
