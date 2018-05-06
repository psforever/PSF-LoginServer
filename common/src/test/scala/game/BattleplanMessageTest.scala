// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class BattleplanMessageTest extends Specification {
  val string_start = hex"b3 3a197902 94 59006500740041006e006f0074006800650072004600610069006c0075007200650041006c007400 0000 01 e0"
  val string_stop = hex"b3 3a197902 94 59006500740041006e006f0074006800650072004600610069006c0075007200650041006c007400 0000 01 f0"
  val string_line = hex"b3 85647702 8c 4f0075007400730074006100620075006c006f0075007300 0a00 20 2aba2b4aae8bd2aba334aae8dd2aca3b4ab28fd2aca414ab29152aca474ab292d2ada4d4ab69452ada534ab695d2ada594ab696d2ada5d4ab697d2ada614ab698d2ada654ab699d2ada694ab69ad2aea6d4aba9bd2aea714aba9cd2aea754aba9dd2aea794aba9ed"
  val string_style = hex"b3856477028c4f0075007400730074006100620075006c006f00750073000a00031d22aba2f4aae8cd"
  val string_message = hex"b3 85647702 8c 4f0075007400730074006100620075006c006f0075007300 0a00 01 6aba2b5011c0480065006c006c006f00200041007500720061007800690073002100"
  //0xb3856477028c4f0075007400730074006100620075006c006f00750073000a000130

  "decode (start)" in {
    PacketCoding.DecodePacket(string_start).require match {
      case BattleplanMessage(char_id, player_name, zone_id, diagrams) =>
        char_id mustEqual 41490746
        player_name mustEqual "YetAnotherFailureAlt"
        zone_id mustEqual 0
        diagrams.size mustEqual 1
        //0
        diagrams.head.action mustEqual DiagramActionCode.StartDrawing
        diagrams.head.stroke.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (end)" in {
    PacketCoding.DecodePacket(string_stop).require match {
      case BattleplanMessage(char_id, player_name, zone_id, diagrams) =>
        char_id mustEqual 41490746
        player_name mustEqual "YetAnotherFailureAlt"
        zone_id mustEqual 0
        diagrams.size mustEqual 1
        //0
        diagrams.head.action mustEqual DiagramActionCode.StopDrawing
        diagrams.head.stroke.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (stop)" in {
    PacketCoding.DecodePacket(string_line).require match {
      case BattleplanMessage(char_id, player_name, zone_id, diagrams) =>
        char_id mustEqual 41378949
        player_name mustEqual "Outstabulous"
        zone_id mustEqual 10
        diagrams.size mustEqual 32
        //0
        diagrams.head.action mustEqual DiagramActionCode.Vertex
        diagrams.head.stroke.isDefined mustEqual true
        diagrams.head.stroke.get.isInstanceOf[Vertex] mustEqual true
        diagrams.head.stroke.get.asInstanceOf[Vertex].x mustEqual 7512.0f
        diagrams.head.stroke.get.asInstanceOf[Vertex].y mustEqual 6312.0f
        //1
        diagrams(1).action mustEqual DiagramActionCode.Vertex
        diagrams(1).stroke.get.asInstanceOf[Vertex].x mustEqual 7512.0f
        diagrams(1).stroke.get.asInstanceOf[Vertex].y mustEqual 6328.0f
        //2
        diagrams(2).action mustEqual DiagramActionCode.Vertex
        diagrams(2).stroke.get.asInstanceOf[Vertex].x mustEqual 7512.0f
        diagrams(2).stroke.get.asInstanceOf[Vertex].y mustEqual 6344.0f
        //3
        diagrams(3).action mustEqual DiagramActionCode.Vertex
        diagrams(3).stroke.get.asInstanceOf[Vertex].x mustEqual 7512.0f
        diagrams(3).stroke.get.asInstanceOf[Vertex].y mustEqual 6360.0f
        //4
        diagrams(4).action mustEqual DiagramActionCode.Vertex
        diagrams(4).stroke.get.asInstanceOf[Vertex].x mustEqual 7520.0f
        diagrams(4).stroke.get.asInstanceOf[Vertex].y mustEqual 6376.0f
        //5
        diagrams(5).action mustEqual DiagramActionCode.Vertex
        diagrams(5).stroke.get.asInstanceOf[Vertex].x mustEqual 7520.0f
        diagrams(5).stroke.get.asInstanceOf[Vertex].y mustEqual 6392.0f
        //6
        diagrams(6).action mustEqual DiagramActionCode.Vertex
        diagrams(6).stroke.get.asInstanceOf[Vertex].x mustEqual 7520.0f
        diagrams(6).stroke.get.asInstanceOf[Vertex].y mustEqual 6400.0f
        //7
        diagrams(7).action mustEqual DiagramActionCode.Vertex
        diagrams(7).stroke.get.asInstanceOf[Vertex].x mustEqual 7520.0f
        diagrams(7).stroke.get.asInstanceOf[Vertex].y mustEqual 6416.0f
        //8
        diagrams(8).action mustEqual DiagramActionCode.Vertex
        diagrams(8).stroke.get.asInstanceOf[Vertex].x mustEqual 7520.0f
        diagrams(8).stroke.get.asInstanceOf[Vertex].y mustEqual 6424.0f
        //9
        diagrams(9).action mustEqual DiagramActionCode.Vertex
        diagrams(9).stroke.get.asInstanceOf[Vertex].x mustEqual 7520.0f
        diagrams(9).stroke.get.asInstanceOf[Vertex].y mustEqual 6440.0f
        //10
        diagrams(10).action mustEqual DiagramActionCode.Vertex
        diagrams(10).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(10).stroke.get.asInstanceOf[Vertex].y mustEqual 6448.0f
        //11
        diagrams(11).action mustEqual DiagramActionCode.Vertex
        diagrams(11).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(11).stroke.get.asInstanceOf[Vertex].y mustEqual 6464.0f
        //12
        diagrams(12).action mustEqual DiagramActionCode.Vertex
        diagrams(12).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(12).stroke.get.asInstanceOf[Vertex].y mustEqual 6472.0f
        //13
        diagrams(13).action mustEqual DiagramActionCode.Vertex
        diagrams(13).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(13).stroke.get.asInstanceOf[Vertex].y mustEqual 6488.0f
        //14
        diagrams(14).action mustEqual DiagramActionCode.Vertex
        diagrams(14).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(14).stroke.get.asInstanceOf[Vertex].y mustEqual 6496.0f
        //15
        diagrams(15).action mustEqual DiagramActionCode.Vertex
        diagrams(15).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(15).stroke.get.asInstanceOf[Vertex].y mustEqual 6504.0f
        //16
        diagrams(16).action mustEqual DiagramActionCode.Vertex
        diagrams(16).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(16).stroke.get.asInstanceOf[Vertex].y mustEqual 6512.0f
        //17
        diagrams(17).action mustEqual DiagramActionCode.Vertex
        diagrams(17).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(17).stroke.get.asInstanceOf[Vertex].y mustEqual 6520.0f
        //18
        diagrams(18).action mustEqual DiagramActionCode.Vertex
        diagrams(18).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(18).stroke.get.asInstanceOf[Vertex].y mustEqual 6528.0f
        //19
        diagrams(19).action mustEqual DiagramActionCode.Vertex
        diagrams(19).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(19).stroke.get.asInstanceOf[Vertex].y mustEqual 6536.0f
        //20
        diagrams(20).action mustEqual DiagramActionCode.Vertex
        diagrams(20).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(20).stroke.get.asInstanceOf[Vertex].y mustEqual 6544.0f
        //21
        diagrams(21).action mustEqual DiagramActionCode.Vertex
        diagrams(21).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(21).stroke.get.asInstanceOf[Vertex].y mustEqual 6552.0f
        //22
        diagrams(22).action mustEqual DiagramActionCode.Vertex
        diagrams(22).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(22).stroke.get.asInstanceOf[Vertex].y mustEqual 6560.0f
        //23
        diagrams(23).action mustEqual DiagramActionCode.Vertex
        diagrams(23).stroke.get.asInstanceOf[Vertex].x mustEqual 7528.0f
        diagrams(23).stroke.get.asInstanceOf[Vertex].y mustEqual 6568.0f
        //24
        diagrams(24).action mustEqual DiagramActionCode.Vertex
        diagrams(24).stroke.get.asInstanceOf[Vertex].x mustEqual 7536.0f
        diagrams(24).stroke.get.asInstanceOf[Vertex].y mustEqual 6576.0f
        //25
        diagrams(25).action mustEqual DiagramActionCode.Vertex
        diagrams(25).stroke.get.asInstanceOf[Vertex].x mustEqual 7536.0f
        diagrams(25).stroke.get.asInstanceOf[Vertex].y mustEqual 6584.0f
        //26
        diagrams(26).action mustEqual DiagramActionCode.Vertex
        diagrams(26).stroke.get.asInstanceOf[Vertex].x mustEqual 7536.0f
        diagrams(26).stroke.get.asInstanceOf[Vertex].y mustEqual 6592.0f
        //27
        diagrams(27).action mustEqual DiagramActionCode.Vertex
        diagrams(27).stroke.get.asInstanceOf[Vertex].x mustEqual 7536.0f
        diagrams(27).stroke.get.asInstanceOf[Vertex].y mustEqual 6600.0f
        //28
        diagrams(28).action mustEqual DiagramActionCode.Vertex
        diagrams(28).stroke.get.asInstanceOf[Vertex].x mustEqual 7536.0f
        diagrams(28).stroke.get.asInstanceOf[Vertex].y mustEqual 6608.0f
        //29
        diagrams(29).action mustEqual DiagramActionCode.Vertex
        diagrams(29).stroke.get.asInstanceOf[Vertex].x mustEqual 7536.0f
        diagrams(29).stroke.get.asInstanceOf[Vertex].y mustEqual 6616.0f
        //30
        diagrams(30).action mustEqual DiagramActionCode.Vertex
        diagrams(30).stroke.get.asInstanceOf[Vertex].x mustEqual 7536.0f
        diagrams(30).stroke.get.asInstanceOf[Vertex].y mustEqual 6624.0f
        //31
        diagrams(31).action mustEqual DiagramActionCode.Vertex
        diagrams(31).stroke.get.asInstanceOf[Vertex].x mustEqual 7536.0f
        diagrams(31).stroke.get.asInstanceOf[Vertex].y mustEqual 6632.0f
      case _ =>
        ko
    }
  }

  "decode (style)" in {
    PacketCoding.DecodePacket(string_style).require match {
      case BattleplanMessage(char_id, player_name, zone_id, diagrams) =>
        char_id mustEqual 41378949
        player_name mustEqual "Outstabulous"
        zone_id mustEqual 10
        diagrams.size mustEqual 3
        //0
        diagrams.head.action mustEqual DiagramActionCode.Style
        diagrams.head.stroke.isDefined mustEqual true
        diagrams.head.stroke.get.isInstanceOf[Style] mustEqual true
        diagrams.head.stroke.get.asInstanceOf[Style].thickness mustEqual 3.0f
        diagrams.head.stroke.get.asInstanceOf[Style].color mustEqual 2
        //1
        diagrams(1).action mustEqual DiagramActionCode.Vertex
        diagrams(1).stroke.get.asInstanceOf[Vertex].x mustEqual 7512.0f
        diagrams(1).stroke.get.asInstanceOf[Vertex].y mustEqual 6328.0f
        //2
        diagrams(2).action mustEqual DiagramActionCode.Vertex
        diagrams(2).stroke.get.asInstanceOf[Vertex].x mustEqual 7512.0f
        diagrams(2).stroke.get.asInstanceOf[Vertex].y mustEqual 6344.0f
      case _ =>
        ko
    }
  }

  "decode (message)" in {
    PacketCoding.DecodePacket(string_message).require match {
      case BattleplanMessage(char_id, player_name, zone_id, diagrams) =>
        char_id mustEqual 41378949
        player_name mustEqual "Outstabulous"
        zone_id mustEqual 10
        diagrams.size mustEqual 1
        //0
        diagrams.head.action mustEqual DiagramActionCode.DrawString
        diagrams.head.stroke.isDefined mustEqual true
        diagrams.head.stroke.get.isInstanceOf[DrawString] mustEqual true
        diagrams.head.stroke.get.asInstanceOf[DrawString].x mustEqual 7512.0f
        diagrams.head.stroke.get.asInstanceOf[DrawString].y mustEqual 6312.0f
        diagrams.head.stroke.get.asInstanceOf[DrawString].color mustEqual 2
        diagrams.head.stroke.get.asInstanceOf[DrawString].channel mustEqual 0
        diagrams.head.stroke.get.asInstanceOf[DrawString].message mustEqual "Hello Auraxis!"
      case _ =>
        ko
    }
  }

  "encode (start)" in {
    val msg = BattleplanMessage(
      41490746,
      "YetAnotherFailureAlt",
      0,
      BattleDiagramAction(DiagramActionCode.StartDrawing) ::
        Nil
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_start
  }

  "encode (stop)" in {
    val msg = BattleplanMessage(
      41490746,
      "YetAnotherFailureAlt",
      0,
      BattleDiagramAction(DiagramActionCode.StopDrawing) ::
        Nil
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_stop
  }

  "encode (line)" in {
    val msg = BattleplanMessage(
      41378949,
      "Outstabulous",
      10,
      BattleDiagramAction.vertex(7512.0f, 6312.0f) ::
        BattleDiagramAction.vertex(7512.0f, 6328.0f) ::
        BattleDiagramAction.vertex(7512.0f, 6344.0f) ::
        BattleDiagramAction.vertex(7512.0f, 6360.0f) ::
        BattleDiagramAction.vertex(7520.0f, 6376.0f) ::
        BattleDiagramAction.vertex(7520.0f, 6392.0f) ::
        BattleDiagramAction.vertex(7520.0f, 6400.0f) ::
        BattleDiagramAction.vertex(7520.0f, 6416.0f) ::
        BattleDiagramAction.vertex(7520.0f, 6424.0f) ::
        BattleDiagramAction.vertex(7520.0f, 6440.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6448.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6464.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6472.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6488.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6496.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6504.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6512.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6520.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6528.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6536.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6544.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6552.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6560.0f) ::
        BattleDiagramAction.vertex(7528.0f, 6568.0f) ::
        BattleDiagramAction.vertex(7536.0f, 6576.0f) ::
        BattleDiagramAction.vertex(7536.0f, 6584.0f) ::
        BattleDiagramAction.vertex(7536.0f, 6592.0f) ::
        BattleDiagramAction.vertex(7536.0f, 6600.0f) ::
        BattleDiagramAction.vertex(7536.0f, 6608.0f) ::
        BattleDiagramAction.vertex(7536.0f, 6616.0f) ::
        BattleDiagramAction.vertex(7536.0f, 6624.0f) ::
        BattleDiagramAction.vertex(7536.0f, 6632.0f) ::
        Nil
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_line
  }

  "encode (style)" in {
    val msg = BattleplanMessage(
      41378949,
      "Outstabulous",
      10,
      BattleDiagramAction.style(3.0f, 2) ::
        BattleDiagramAction.vertex(7512.0f, 6328.0f) ::
        BattleDiagramAction.vertex(7512.0f, 6344.0f) ::
        Nil
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_style
  }

  "encode (message)" in {
    val msg = BattleplanMessage(
      41378949,
      "Outstabulous",
      10,
      BattleDiagramAction.drawString(7512.0f, 6312.0f, 2, 0, "Hello Auraxis!") :: Nil
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_message
  }
}
