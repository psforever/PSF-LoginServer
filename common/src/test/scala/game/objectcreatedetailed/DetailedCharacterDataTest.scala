// Copyright (c) 2017 PSForever
package game.objectcreatedetailed

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{ObjectCreateDetailedMessage, _}
import net.psforever.packet.game.objectcreate._
import net.psforever.types._
import scodec.bits._

class DetailedCharacterDataTest extends Specification {
  val string_testchar = hex"18 570C0000 BC8 4B00 6C2D7 65535 CA16 0 00 01 34 40 00 0970 49006C006C006C004900490049006C006C006C0049006C0049006C006C0049006C006C006C0049006C006C004900 84 52 70 76 1E 80 80 00 00 00 00 00 3FFFC 0 00 00 00 20 00 00 0F F6 A7 03 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FC 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 90 01 90 00 64 00 00 01 00 7E C8 00 C8 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 C0 00 42 C5 46  86 C7 00 00 00 80 00 00 12 40 78 70 65 5F 73 61 6E 63 74 75 61 72 79 5F 68 65 6C 70 90 78 70 65 5F 74 68 5F 66 69 72 65 6D 6F 64 65 73 8B 75 73 65 64 5F 62 65 61 6D 65 72 85 6D 61 70 31 33 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 0A 23 02 60 04 04 40 00 00 10 00 06 02 08 14 D0 08 0C 80 00 02 00 02 6B 4E 00 82 88 00 00 02 00 00 C0 41 C0 9E 01 01 90 00 00 64 00 44 2A 00 10 91 00 00 00 40 00 18 08 38 94 40 20 32 00 00 00 80 19 05 48 02 17 20 00 00 08 00 70 29 80 43 64 00 00 32 00 0E 05 40 08 9C 80 00 06 40 01 C0 AA 01 19 90 00 00 C8 00 3A 15 80 28 72 00 00 19 00 04 0A B8 05 26 40 00 03 20 06 C2 58 00 A7 88 00 00 02 00 00 80 00 00"
  val string_testchar_seated =
    hex"181f0c000066d5bc84b00808000012e049006c006c006c004900490049006c006c006c0049006c0049006c006c0049006c006c006c004900" ++
    hex"6c006c0049008452700000000000000000000000000000002000000fe6a703fffffffffffffffffffffffffffffffc000000000000000000" ++
    hex"00000000000000000000019001900064000001007ec800c80000000000000000000000000000000000000001c00042c54686c70000008000" ++
    hex"0012407870655f73616e6374756172795f68656c70907870655f74685f666972656d6f6465738b757365645f6265616d6572856d61703133" ++
    hex"0000000000000000000000000000000000000000000000000000000000010a2302600404400000100006020814d0080c80000200026b4e00" ++
    hex"82880000020000c041c09e01019000006400442a001091000000400018083894402032000000801905480217200000080070298043640000" ++
    hex"32000e0540089c8000064001c0aa0119900000c8003a1580287200001900040ab805264000032006c25800a7880000020000800000"
  val string_testchar_br32 = hex"18 2c e0 00 00 bc 84 B0 00 0b ea 00 6c 7d f1 10 00 00 02 40 00 08 60 4b 00 69 00 43 00 6b 00 4a 00 72 00 02 31 3a cc 82 c0 00 00 00 00 00 00 00 00 3e df 42 00 20 00 0e 00 40 43 40 4c 04 00 02 e8 00 00 03 a8 00 00 01 9c 04 00 00 b8 99 84 00 0e 68 28 00 00 00 00 00 00 00 00 00 00 00 00 01 90 01 90 00 c8 00 00 01 00 7e c8 00 5c 00 00 01 29 c1 cc 80 00 00 00 00 00 00 00 00 00 00 00 00 03 c0 00 40 81 01 c4 45 46 86 c8 88 c9 09 4a 4a 80 50 0c 13 00 00 15 00 80 00 48 00 7870655f6f766572686561645f6d6170 8d7870655f776172705f676174658f7870655f666f726d5f6f75746669748c7870655f626c61636b6f7073927870655f636f6d6d616e645f72616e6b5f35927870655f636f6d6d616e645f72616e6b5f33927870655f73616e6374756172795f68656c70927870655f626174746c655f72616e6b5f3133927870655f626174746c655f72616e6b5f3132927870655f626174746c655f72616e6b5f3130927870655f626174746c655f72616e6b5f3134927870655f626174746c655f72616e6b5f3135937870655f6f72626974616c5f73687574746c658c7870655f64726f705f706f64917870655f62696e645f666163696c697479917870655f626174746c655f72616e6b5f33917870655f626174746c655f72616e6b5f35917870655f626174746c655f72616e6b5f348e7870655f6a6f696e5f73717561648e7870655f666f726d5f7371756164927870655f696e7374616e745f616374696f6e917870655f626174746c655f72616e6b5f32937870655f776172705f676174655f7573616765917870655f626174746c655f72616e6b5f38927870655f626174746c655f72616e6b5f3131917870655f626174746c655f72616e6b5f368e7870655f6d61696c5f616c657274927870655f636f6d6d616e645f72616e6b5f31927870655f626174746c655f72616e6b5f3230927870655f626174746c655f72616e6b5f3138927870655f626174746c655f72616e6b5f3139907870655f6a6f696e5f706c61746f6f6e927870655f626174746c655f72616e6b5f3137927870655f626174746c655f72616e6b5f31368f7870655f6a6f696e5f6f7574666974927870655f626174746c655f72616e6b5f3235927870655f626174746c655f72616e6b5f3234927870655f636f6d6d616e645f72616e6b5f34907870655f666f726d5f706c61746f6f6e8c7870655f62696e645f616d73917870655f626174746c655f72616e6b5f39917870655f626174746c655f72616e6b5f378d7870655f74685f726f757465728c7870655f74685f666c61696c8a7870655f74685f616e748a7870655f74685f616d738f7870655f74685f67726f756e645f708c7870655f74685f6169725f708c7870655f74685f686f7665728d7870655f74685f67726f756e648a7870655f74685f626672927870655f74685f61667465726275726e65728a7870655f74685f6169728c7870655f74685f636c6f616b89757365645f6f69637791757365645f616476616e6365645f61636597766973697465645f73706974666972655f74757272657498766973697465645f73706974666972655f636c6f616b656493766973697465645f73706974666972655f616192766973697465645f74616e6b5f7472617073a1766973697465645f706f727461626c655f6d616e6e65645f7475727265745f6e63a1766973697465645f706f727461626c655f6d616e6e65645f7475727265745f74728e757365645f6d61676375747465728f757365645f636861696e626c6164658f757365645f666f726365626c61646593766973697465645f77616c6c5f74757272657498766973697465645f616e6369656e745f7465726d696e616c8b766973697465645f616d738b766973697465645f616e7490766973697465645f64726f707368697091766973697465645f6c6962657261746f7294766973697465645f6c6967687467756e7368697091766973697465645f6c696768746e696e6790766973697465645f6d616772696465728f766973697465645f70726f776c657293766973697465645f71756164737465616c746890766973697465645f736b7967756172649a766973697465645f74687265656d616e686561767962756767799d766973697465645f74776f5f6d616e5f61737361756c745f627567677998766973697465645f74776f6d616e6865617679627567677998766973697465645f74776f6d616e686f766572627567677990766973697465645f76616e67756172648d766973697465645f666c61696c8e766973697465645f726f7574657293766973697465645f737769746368626c6164658e766973697465645f6175726f726193766973697465645f626174746c657761676f6e8c766973697465645f6675727993766973697465645f7175616461737361756c7496766973697465645f67616c6178795f67756e736869708e766973697465645f6170635f74728e766973697465645f6170635f767390766973697465645f6c6f64657374617290766973697465645f7068616e7461736d91766973697465645f7468756e64657265728e766973697465645f6170635f6e638f766973697465645f76756c747572658c766973697465645f7761737090766973697465645f6d6f73717569746f97766973697465645f617068656c696f6e5f666c6967687497766973697465645f617068656c696f6e5f67756e6e657297766973697465645f636f6c6f737375735f666c6967687497766973697465645f636f6c6f737375735f67756e6e657298766973697465645f706572656772696e655f666c6967687498766973697465645f706572656772696e655f67756e6e657289757365645f62616e6b95766973697465645f7265736f757263655f73696c6f9e766973697465645f63657274696669636174696f6e5f7465726d696e616c94766973697465645f6d65645f7465726d696e616c93757365645f6e616e6f5f64697370656e73657295766973697465645f73656e736f725f736869656c649a766973697465645f62726f6164636173745f77617270676174658c757365645f7068616c616e7894757365645f7068616c616e785f6176636f6d626f96757365645f7068616c616e785f666c616b636f6d626f96766973697465645f77617270676174655f736d616c6c91757365645f666c616d657468726f7765729a757365645f616e6369656e745f7475727265745f776561706f6e92766973697465645f4c4c555f736f636b657492757365645f656e657267795f67756e5f6e6397766973697465645f6d656469756d7472616e73706f72749f757365645f617068656c696f6e5f696d6d6f6c6174696f6e5f63616e6e6f6e93757365645f6772656e6164655f706c61736d6193757365645f6772656e6164655f6a616d6d657298766973697465645f736869656c645f67656e657261746f7295766973697465645f6d6f74696f6e5f73656e736f7296766973697465645f6865616c74685f6372797374616c96766973697465645f7265706169725f6372797374616c97766973697465645f76656869636c655f6372797374616c91757365645f6772656e6164655f6672616788757365645f61636598766973697465645f6164765f6d65645f7465726d696e616c8b757365645f6265616d657290757365645f626f6c745f6472697665728b757365645f6379636c65728a757365645f676175737391757365645f68756e7465727365656b657288757365645f6973708b757365645f6c616e6365728b757365645f6c61736865728e757365645f6d61656c7374726f6d8c757365645f70686f656e69788b757365645f70756c7361728d757365645f70756e69736865728e757365645f725f73686f7467756e8d757365645f7261646961746f7288757365645f72656b8d757365645f72657065617465728c757365645f726f636b6c65748c757365645f737472696b65728f757365645f73757070726573736f728c757365645f7468756d7065729c766973697465645f76616e755f636f6e74726f6c5f636f6e736f6c6598766973697465645f636170747572655f7465726d696e616c92757365645f6d696e695f636861696e67756e91757365645f6c617a655f706f696e7465728c757365645f74656c657061648b757365645f7370696b657291757365645f68656176795f736e6970657293757365645f636f6d6d616e645f75706c696e6b8d757365645f66697265626972648e757365645f666c6563686574746594757365645f68656176795f7261696c5f6265616d89757365645f696c63399a766973697465645f67656e657261746f725f7465726d696e616c8e766973697465645f6c6f636b65729a766973697465645f65787465726e616c5f646f6f725f6c6f636b9c766973697465645f6169725f76656869636c655f7465726d696e616c97766973697465645f67616c6178795f7465726d696e616c98766973697465645f696d706c616e745f7465726d696e616c99766973697465645f7365636f6e646172795f6361707475726590757365645f32356d6d5f63616e6e6f6e99757365645f6c6962657261746f725f626f6d6261726469657293766973697465645f7265706169725f73696c6f93766973697465645f76616e755f6d6f64756c6591757365645f666c61696c5f776561706f6e8b757365645f73637974686598766973697465645f7265737061776e5f7465726d696e616c8c757365645f62616c6c67756e92757365645f656e657267795f67756e5f747295757365645f616e6e69766572736172795f67756e6195757365645f616e6e69766572736172795f67756e6294757365645f616e6e69766572736172795f67756e90757365645f37356d6d5f63616e6e6f6e92757365645f6170635f6e635f776561706f6e92757365645f6170635f74725f776561706f6e92757365645f6170635f76735f776561706f6e90757365645f666c75785f63616e6e6f6e9f757365645f617068656c696f6e5f706c61736d615f726f636b65745f706f6491757365645f617068656c696f6e5f7070618c757365645f666c7578706f6494766973697465645f6266725f7465726d696e616c9e757365645f636f6c6f737375735f636c75737465725f626f6d625f706f64a0757365645f636f6c6f737375735f6475616c5f3130306d6d5f63616e6e6f6e7399757365645f636f6c6f737375735f74616e6b5f63616e6e6f6e96766973697465645f656e657267795f6372797374616c9b757365645f68656176795f6772656e6164655f6c61756e6368657298757365645f33356d6d5f726f74617279636861696e67756e8b757365645f6b6174616e6190757365645f33356d6d5f63616e6e6f6e93757365645f7265617665725f776561706f6e7396757365645f6c696768746e696e675f776561706f6e738c757365645f6d65645f61707090757365645f32306d6d5f63616e6e6f6e98766973697465645f6d6f6e6f6c6974685f616d657269736899766973697465645f6d6f6e6f6c6974685f636572797368656e97766973697465645f6d6f6e6f6c6974685f637973736f7297766973697465645f6d6f6e6f6c6974685f6573616d697299766973697465645f6d6f6e6f6c6974685f666f72736572616c99766973697465645f6d6f6e6f6c6974685f697368756e64617298766973697465645f6d6f6e6f6c6974685f7365617268757397766973697465645f6d6f6e6f6c6974685f736f6c73617292757365645f6e635f6865765f66616c636f6e99757365645f6e635f6865765f7363617474657263616e6e6f6e93757365645f6e635f6865765f73706172726f7791757365645f61726d6f725f736970686f6e9f757365645f706572656772696e655f6475616c5f6d616368696e655f67756e9f757365645f706572656772696e655f6475616c5f726f636b65745f706f647399757365645f706572656772696e655f6d65636868616d6d65729e757365645f706572656772696e655f7061727469636c655f63616e6e6f6e96757365645f706572656772696e655f73706172726f7791757365645f3130356d6d5f63616e6e6f6e92757365645f31356d6d5f636861696e67756ea0757365645f70756c7365645f7061727469636c655f616363656c657261746f7293757365645f726f74617279636861696e67756e9f766973697465645f6465636f6e737472756374696f6e5f7465726d696e616c95757365645f736b7967756172645f776561706f6e7391766973697465645f67656e657261746f7291757365645f67617573735f63616e6e6f6e89757365645f7472656b95757365645f76616e67756172645f776561706f6e73a4766973697465645f616e6369656e745f6169725f76656869636c655f7465726d696e616ca2766973697465645f616e6369656e745f65717569706d656e745f7465726d696e616c96766973697465645f6f726465725f7465726d696e616ca7766973697465645f616e6369656e745f67726f756e645f76656869636c655f7465726d696e616c9f766973697465645f67726f756e645f76656869636c655f7465726d696e616c97757365645f76756c747572655f626f6d6261726469657298757365645f76756c747572655f6e6f73655f63616e6e6f6e98757365645f76756c747572655f7461696c5f63616e6e6f6e97757365645f776173705f776561706f6e5f73797374656d91766973697465645f636861726c6965303191766973697465645f636861726c6965303291766973697465645f636861726c6965303391766973697465645f636861726c6965303491766973697465645f636861726c6965303591766973697465645f636861726c6965303691766973697465645f636861726c6965303791766973697465645f636861726c6965303891766973697465645f636861726c6965303996766973697465645f67696e6765726d616e5f6174617298766973697465645f67696e6765726d616e5f646168616b6196766973697465645f67696e6765726d616e5f6876617296766973697465645f67696e6765726d616e5f697a686199766973697465645f67696e6765726d616e5f6a616d7368696498766973697465645f67696e6765726d616e5f6d697468726198766973697465645f67696e6765726d616e5f726173686e7599766973697465645f67696e6765726d616e5f7372616f73686198766973697465645f67696e6765726d616e5f79617a61746195766973697465645f67696e6765726d616e5f7a616c8e766973697465645f736c656430318e766973697465645f736c656430328e766973697465645f736c656430348e766973697465645f736c656430358e766973697465645f736c656430368e766973697465645f736c656430378e766973697465645f736c6564303897766973697465645f736e6f776d616e5f616d657269736898766973697465645f736e6f776d616e5f636572797368656e96766973697465645f736e6f776d616e5f637973736f7296766973697465645f736e6f776d616e5f6573616d697298766973697465645f736e6f776d616e5f666f72736572616c96766973697465645f736e6f776d616e5f686f7373696e98766973697465645f736e6f776d616e5f697368756e64617297766973697465645f736e6f776d616e5f7365617268757396766973697465645f736e6f776d616e5f736f6c736172857567643036857567643035857567643034857567643033857567643032857567643031856d61703939856d61703938856d61703937856d61703936856d61703135856d61703134856d61703131856d61703038856d61703034856d61703035856d61703033856d61703031856d61703036856d61703032856d61703039856d61703037856d617031300300000091747261696e696e675f73746172745f6e638b747261696e696e675f75698c747261696e696e675f6d61700000000000000000000000000000000000000000800000003d0c04d350840240000010000602429660f80c80000c8004200c1b81480000020000c046f18a47019000019000ca4644304900000040001809e6bb052032000008001a84787211200000080003010714889c06400000100320ff0a42e4000001009e95a7342e03200000080003010408c914064000000001198990c4e4000001000060223b9b2180c800000a00081c20c92c800003600414ec172d900000040001808de1284a0320000320008ef1c336b20000078011d830e6f6400000600569c417e2c80000020000c04102502f019000008c00ce31027d99000000400018099e6146203200004b0015a7d44002f720000008000301040c18dc064000023000b1240800636400000100006020e0e92280c80000c800081650c00cfc800006400ce32a1801a59000000400018099e6fc3e03200004b00058b14680463200000080003010742610c064000043000b16c8880916400000100006020e0d01580c80000c8006714e24012cc80000020000c04cf25c190190000258001032e240307900000c8019c74470061b2000000800030133ced8fc0640000960012d9a8d00f0640000010025b9c1401e4c8000002004b6b23c03d1900000040098f585007b3200000080131a58c00f864000001002536f1c01f4c8000002004a64e2a03f190000004015e1b4580873200000080003010711f8a406400000100110a00c010ee400000100006020e2a51380c8000002002218d21021ec80000020000c041c40249019000000400af18a44043f90000004000180838b44760320000008015e38c80088320000008000301071490cc064000001002bc35890110e400000100006020e2052180c800000200221f90d0222c80000020000c041c5e447019000000400442e62e044790000004000180838af032032000000800886d08c089320000008000301071738740640000010011098898112e400000100006020e2361c80c8000002002212a1b0226c80000020000c041c512170190000004004420a32044f900000040001808389104a0320000008008874c8808a3200000080003010715907c06400000100110c0898114e400000100006020e2771a80c800000200578bd13022ac80000020000c041c424330190000004004423848045790000004000180838bfc32032000000801a86506008b320000008000301071030dc06400000100129f68a0117640000010026353110232c8000002004b69438046d90000004015e2887008eb200000080003010715909406400000100350fb8e011de400000100006020e2881980c8000002005786d0f023cc80000020000c041c4cc3b019000000400af1ba1c047b90000004000180838af872032000000800886344408fb20000008000301071620d406400000100110c10b011fe400000100006020e2870d80c800000200578f30c0240c80000020000c041c5863b019000000400442ee300483900000040001808388605e032000000801a86f03c090b200000080003010712a8fc064000001002bc0d858121e400000100006020e2521c80c800000200578b7230244c80000020000c041c49629019000000400d434026048b90000004000180838afc42032000000801a86d864091b200000080003010711989c064000001003508c8c8123e400000100006020e2a82280c8000002006a14f110248c80000020000c041c4be21019000000400af12640049390000004000180838a54720320000008015e33430092b20000008000301071228cc064000001003546e8d432400000100004f34a631139000004001b0834723120000008000204000c2ed0fa1c800000200a8432234a90000004000180952b248a0320000018004024c569d20000008000250a4d0ebc480000020000c04a24bc43019000000c00e0"

  "DetailedCharacterData" should {
    "decode" in {
      PacketCoding.DecodePacket(string_testchar).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 3159
          cls mustEqual ObjectClass.avatar
          guid mustEqual PlanetSideGUID(75)
          parent.isDefined mustEqual false
          data match {
            case Some(DetailedPlayerData(Some(pos), basic, char, inv, hand)) =>
              pos.coord mustEqual Vector3(3674.8438f, 2726.789f, 91.15625f)
              pos.orient mustEqual Vector3(0, 0, 36.5625f)
              pos.vel.isDefined mustEqual false

              basic.app.name mustEqual "IlllIIIlllIlIllIlllIllI"
              basic.app.faction mustEqual PlanetSideEmpire.VS
              basic.app.sex mustEqual CharacterGender.Female
              basic.app.head mustEqual 41
              basic.app.voice mustEqual 1 //female 1
              basic.voice2 mustEqual 3
              basic.black_ops mustEqual false
              basic.jammered mustEqual false
              basic.exosuit mustEqual ExoSuitType.Standard
              basic.outfit_name mustEqual ""
              basic.outfit_logo mustEqual 0
              basic.backpack mustEqual false
              basic.facingPitch mustEqual 2.8125f
              basic.facingYawUpper mustEqual 210.9375f
              basic.lfs mustEqual true
              basic.grenade_state mustEqual GrenadeState.None
              basic.is_cloaking mustEqual false
              basic.charging_pose mustEqual false
              basic.on_zipline mustEqual false
              basic.ribbons.upper mustEqual MeritCommendation.None
              basic.ribbons.middle mustEqual MeritCommendation.None
              basic.ribbons.lower mustEqual MeritCommendation.None
              basic.ribbons.tos mustEqual MeritCommendation.None

              char.bep mustEqual 0
              char.cep mustEqual 0
              char.healthMax mustEqual 100
              char.health mustEqual 100
              char.armor mustEqual 50 //standard exosuit value
              char.unk1 mustEqual 1
              char.unk2 mustEqual 7
              char.unk3 mustEqual 7
              char.staminaMax mustEqual 100
              char.stamina mustEqual 100
              char.certs.length mustEqual 7
              char.certs.head mustEqual CertificationType.StandardAssault
              char.certs(1) mustEqual CertificationType.MediumAssault
              char.certs(2) mustEqual CertificationType.ATV
              char.certs(3) mustEqual CertificationType.Harasser
              char.certs(4) mustEqual CertificationType.StandardExoSuit
              char.certs(5) mustEqual CertificationType.AgileExoSuit
              char.certs(6) mustEqual CertificationType.ReinforcedExoSuit
              char.implants.length mustEqual 0
              char.firstTimeEvents.size mustEqual 4
              char.firstTimeEvents.head mustEqual "xpe_sanctuary_help"
              char.firstTimeEvents(1) mustEqual "xpe_th_firemodes"
              char.firstTimeEvents(2) mustEqual "used_beamer"
              char.firstTimeEvents(3) mustEqual "map13"
              char.tutorials.size mustEqual 0
              char.cosmetics.isDefined mustEqual false
              inv.isDefined mustEqual true
              val inventory = inv.get.contents
              inventory.size mustEqual 10
              //0
              inventory.head.objectClass mustEqual ObjectClass.beamer
              inventory.head.guid mustEqual PlanetSideGUID(76)
              inventory.head.parentSlot mustEqual 0
              var wep = inventory.head.obj.asInstanceOf[DetailedWeaponData]
              wep.ammo.head.objectClass mustEqual ObjectClass.energy_cell
              wep.ammo.head.guid mustEqual PlanetSideGUID(77)
              wep.ammo.head.parentSlot mustEqual 0
              wep.ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 16
              //1
              inventory(1).objectClass mustEqual ObjectClass.suppressor
              inventory(1).guid mustEqual PlanetSideGUID(78)
              inventory(1).parentSlot mustEqual 2
              wep = inventory(1).obj.asInstanceOf[DetailedWeaponData]
              wep.ammo.head.objectClass mustEqual ObjectClass.bullet_9mm
              wep.ammo.head.guid mustEqual PlanetSideGUID(79)
              wep.ammo.head.parentSlot mustEqual 0
              wep.ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 25
              //2
              inventory(2).objectClass mustEqual ObjectClass.forceblade
              inventory(2).guid mustEqual PlanetSideGUID(80)
              inventory(2).parentSlot mustEqual 4
              wep = inventory(2).obj.asInstanceOf[DetailedWeaponData]
              wep.ammo.head.objectClass mustEqual ObjectClass.melee_ammo
              wep.ammo.head.guid mustEqual PlanetSideGUID(81)
              wep.ammo.head.parentSlot mustEqual 0
              wep.ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 1
              //3
              inventory(3).objectClass mustEqual ObjectClass.locker_container
              inventory(3).guid mustEqual PlanetSideGUID(82)
              inventory(3).parentSlot mustEqual 5
              inventory(3).obj.isInstanceOf[DetailedLockerContainerData] mustEqual true
              inventory(3).obj.asInstanceOf[DetailedLockerContainerData].inventory.isDefined mustEqual false
              //4
              inventory(4).objectClass mustEqual ObjectClass.bullet_9mm
              inventory(4).guid mustEqual PlanetSideGUID(83)
              inventory(4).parentSlot mustEqual 6
              inventory(4).obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 50
              //5
              inventory(5).objectClass mustEqual ObjectClass.bullet_9mm
              inventory(5).guid mustEqual PlanetSideGUID(84)
              inventory(5).parentSlot mustEqual 9
              inventory(5).obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 50
              //6
              inventory(6).objectClass mustEqual ObjectClass.bullet_9mm
              inventory(6).guid mustEqual PlanetSideGUID(85)
              inventory(6).parentSlot mustEqual 12
              inventory(6).obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 50
              //7
              inventory(7).objectClass mustEqual ObjectClass.bullet_9mm_AP
              inventory(7).guid mustEqual PlanetSideGUID(86)
              inventory(7).parentSlot mustEqual 33
              inventory(7).obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 50
              //8
              inventory(8).objectClass mustEqual ObjectClass.energy_cell
              inventory(8).guid mustEqual PlanetSideGUID(87)
              inventory(8).parentSlot mustEqual 36
              inventory(8).obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 50
              //9
              inventory(9).objectClass mustEqual ObjectClass.remote_electronics_kit
              inventory(9).guid mustEqual PlanetSideGUID(88)
              inventory(9).parentSlot mustEqual 39
              //the rek has data but none worth testing here
              hand mustEqual DrawnSlot.Pistol1
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "decode (character, seated)" in {
      PacketCoding.DecodePacket(string_testchar_seated).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          len mustEqual 3103
          cls mustEqual ObjectClass.avatar
          guid mustEqual PlanetSideGUID(75)
          parent.isDefined mustEqual true
          parent.get.guid mustEqual PlanetSideGUID(43981)
          parent.get.slot mustEqual 0
          data match {
            case Some(DetailedPlayerData(None, basic, char, inv, hand)) =>
              basic.app.name mustEqual "IlllIIIlllIlIllIlllIllI"
              basic.app.faction mustEqual PlanetSideEmpire.VS
              basic.app.sex mustEqual CharacterGender.Female
              basic.app.head mustEqual 41
              basic.app.voice mustEqual 1 //female 1
              basic.voice2 mustEqual 3
              basic.black_ops mustEqual false
              basic.jammered mustEqual false
              basic.exosuit mustEqual ExoSuitType.Standard
              basic.outfit_name mustEqual ""
              basic.outfit_logo mustEqual 0
              basic.backpack mustEqual false
              basic.facingPitch mustEqual 2.8125f
              basic.facingYawUpper mustEqual 210.9375f
              basic.lfs mustEqual true
              basic.grenade_state mustEqual GrenadeState.None
              basic.is_cloaking mustEqual false
              basic.charging_pose mustEqual false
              basic.on_zipline mustEqual false
              basic.ribbons.upper mustEqual MeritCommendation.None
              basic.ribbons.middle mustEqual MeritCommendation.None
              basic.ribbons.lower mustEqual MeritCommendation.None
              basic.ribbons.tos mustEqual MeritCommendation.None

              char.bep mustEqual 0
              char.cep mustEqual 0
              char.healthMax mustEqual 100
              char.health mustEqual 100
              char.armor mustEqual 50 //standard exosuit value
              char.unk1 mustEqual 1
              char.unk2 mustEqual 7
              char.unk3 mustEqual 7
              char.staminaMax mustEqual 100
              char.stamina mustEqual 100
              char.certs.length mustEqual 7
              char.certs.head mustEqual CertificationType.StandardAssault
              char.certs(1) mustEqual CertificationType.MediumAssault
              char.certs(2) mustEqual CertificationType.ATV
              char.certs(3) mustEqual CertificationType.Harasser
              char.certs(4) mustEqual CertificationType.StandardExoSuit
              char.certs(5) mustEqual CertificationType.AgileExoSuit
              char.certs(6) mustEqual CertificationType.ReinforcedExoSuit
              char.implants.length mustEqual 0
              char.firstTimeEvents.size mustEqual 4
              char.firstTimeEvents.head mustEqual "xpe_sanctuary_help"
              char.firstTimeEvents(1) mustEqual "xpe_th_firemodes"
              char.firstTimeEvents(2) mustEqual "used_beamer"
              char.firstTimeEvents(3) mustEqual "map13"
              char.tutorials.size mustEqual 0
              char.cosmetics.isDefined mustEqual false
              inv.isDefined mustEqual true
              val inventory = inv.get.contents
              inventory.size mustEqual 10
              //0
              inventory.head.objectClass mustEqual ObjectClass.beamer
              inventory.head.guid mustEqual PlanetSideGUID(76)
              inventory.head.parentSlot mustEqual 0
              var wep = inventory.head.obj.asInstanceOf[DetailedWeaponData]
              wep.ammo.head.objectClass mustEqual ObjectClass.energy_cell
              wep.ammo.head.guid mustEqual PlanetSideGUID(77)
              wep.ammo.head.parentSlot mustEqual 0
              wep.ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 16
              //1
              inventory(1).objectClass mustEqual ObjectClass.suppressor
              inventory(1).guid mustEqual PlanetSideGUID(78)
              inventory(1).parentSlot mustEqual 2
              wep = inventory(1).obj.asInstanceOf[DetailedWeaponData]
              wep.ammo.head.objectClass mustEqual ObjectClass.bullet_9mm
              wep.ammo.head.guid mustEqual PlanetSideGUID(79)
              wep.ammo.head.parentSlot mustEqual 0
              wep.ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 25
              //2
              inventory(2).objectClass mustEqual ObjectClass.forceblade
              inventory(2).guid mustEqual PlanetSideGUID(80)
              inventory(2).parentSlot mustEqual 4
              wep = inventory(2).obj.asInstanceOf[DetailedWeaponData]
              wep.ammo.head.objectClass mustEqual ObjectClass.melee_ammo
              wep.ammo.head.guid mustEqual PlanetSideGUID(81)
              wep.ammo.head.parentSlot mustEqual 0
              wep.ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 1
              //3
              inventory(3).objectClass mustEqual ObjectClass.locker_container
              inventory(3).guid mustEqual PlanetSideGUID(82)
              inventory(3).parentSlot mustEqual 5
              inventory(3).obj.isInstanceOf[DetailedLockerContainerData] mustEqual true
              inventory(3).obj.asInstanceOf[DetailedLockerContainerData].inventory.isDefined mustEqual false
              //4
              inventory(4).objectClass mustEqual ObjectClass.bullet_9mm
              inventory(4).guid mustEqual PlanetSideGUID(83)
              inventory(4).parentSlot mustEqual 6
              inventory(4).obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 50
              //5
              inventory(5).objectClass mustEqual ObjectClass.bullet_9mm
              inventory(5).guid mustEqual PlanetSideGUID(84)
              inventory(5).parentSlot mustEqual 9
              inventory(5).obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 50
              //6
              inventory(6).objectClass mustEqual ObjectClass.bullet_9mm
              inventory(6).guid mustEqual PlanetSideGUID(85)
              inventory(6).parentSlot mustEqual 12
              inventory(6).obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 50
              //7
              inventory(7).objectClass mustEqual ObjectClass.bullet_9mm_AP
              inventory(7).guid mustEqual PlanetSideGUID(86)
              inventory(7).parentSlot mustEqual 33
              inventory(7).obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 50
              //8
              inventory(8).objectClass mustEqual ObjectClass.energy_cell
              inventory(8).guid mustEqual PlanetSideGUID(87)
              inventory(8).parentSlot mustEqual 36
              inventory(8).obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 50
              //9
              inventory(9).objectClass mustEqual ObjectClass.remote_electronics_kit
              inventory(9).guid mustEqual PlanetSideGUID(88)
              inventory(9).parentSlot mustEqual 39
              //the rek has data but none worth testing here
              hand mustEqual DrawnSlot.Pistol1
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "decode (BR32)" in {
      PacketCoding.DecodePacket(string_testchar_br32).require match {
        case ObjectCreateDetailedMessage(len, cls, guid, parent, data) =>
          //this test is mainly for an alternate bitstream parsing order
          //the object produced is massive and most of it is already covered in other tests
          //only certain details towards the end of the stream will be checked
          data match {
            case Some(DetailedPlayerData(Some(_), _, char, inv, hand)) =>
              DetailedCharacterData.isBR24(char.bep) mustEqual true
              char.certs.size mustEqual 15
              char.certs.head mustEqual CertificationType.StandardAssault
              char.certs(14) mustEqual CertificationType.CombatEngineering
              char.implants.size mustEqual 3
              char.implants.head.implant mustEqual ImplantType.AudioAmplifier
              char.implants.head.activation mustEqual None
              char.implants(1).implant mustEqual ImplantType.Targeting
              char.implants(1).activation mustEqual None
              char.implants(2).implant mustEqual ImplantType.Surge
              char.implants(2).activation mustEqual None
              char.firstTimeEvents.size mustEqual 298
              char.firstTimeEvents.head mustEqual "xpe_overhead_map"
              char.firstTimeEvents(297) mustEqual "map10"
              char.tutorials.size mustEqual 3
              char.tutorials.head mustEqual "training_start_nc"
              char.tutorials(1) mustEqual "training_ui"
              char.tutorials(2) mustEqual "training_map"
              char.cosmetics.isDefined mustEqual true
              char.cosmetics.get.no_helmet mustEqual true
              char.cosmetics.get.beret mustEqual true
              char.cosmetics.get.earpiece mustEqual true
              char.cosmetics.get.sunglasses mustEqual true
              char.cosmetics.get.brimmed_cap mustEqual false
              //inventory
              inv.isDefined mustEqual true
              inv.get.contents.size mustEqual 12
              //0
              inv.get.contents.head.objectClass mustEqual 531
              inv.get.contents.head.guid mustEqual PlanetSideGUID(4202)
              inv.get.contents.head.parentSlot mustEqual 0
              val wep1 = inv.get.contents.head.obj.asInstanceOf[DetailedWeaponData]
              wep1.unk1 mustEqual 2
              wep1.unk2 mustEqual 8
              wep1.ammo.head.objectClass mustEqual 389
              wep1.ammo.head.guid mustEqual PlanetSideGUID(3942)
              wep1.ammo.head.parentSlot mustEqual 0
              wep1.ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].unk mustEqual 8
              wep1.ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 100
              //4
              inv.get.contents(4).objectClass mustEqual 456
              inv.get.contents(4).guid mustEqual PlanetSideGUID(5374)
              inv.get.contents(4).parentSlot mustEqual 5
              inv.get.contents(4).obj.asInstanceOf[DetailedLockerContainerData].inventory.get.contents.size mustEqual 61
              //11
              inv.get.contents(11).objectClass mustEqual 673
              inv.get.contents(11).guid mustEqual PlanetSideGUID(3661)
              inv.get.contents(11).parentSlot mustEqual 60
              val wep2 = inv.get.contents(11).obj.asInstanceOf[DetailedWeaponData]
              wep2.unk1 mustEqual 2
              wep2.unk2 mustEqual 8
              wep2.ammo.head.objectClass mustEqual 674
              wep2.ammo.head.guid mustEqual PlanetSideGUID(8542)
              wep2.ammo.head.parentSlot mustEqual 0
              wep2.ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].unk mustEqual 8
              wep2.ammo.head.obj.asInstanceOf[DetailedAmmoBoxData].magazine mustEqual 3

              hand mustEqual DrawnSlot.None
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val pos : PlacementData = PlacementData(
        3674.8438f, 2726.789f, 91.15625f,
        0, 0, 36.5625f
      )
      val app : (Int)=>CharacterAppearanceData = CharacterAppearanceData(
        BasicCharacterData(
          "IlllIIIlllIlIllIlllIllI",
          PlanetSideEmpire.VS,
          CharacterGender.Female,
          41,
          1
        ),
        3,
        false,
        false,
        ExoSuitType.Standard,
        "",
        0,
        false,
        2.8125f, 210.9375f,
        true,
        GrenadeState.None,
        false,
        false,
        false,
        RibbonBars()
      )
      val char : (Option[Int])=>DetailedCharacterData = DetailedCharacterData(
        0,
        0,
        100, 100,
        50,
        1, 7, 7,
        100, 100,
        List(
          CertificationType.StandardAssault,
          CertificationType.MediumAssault,
          CertificationType.ATV,
          CertificationType.Harasser,
          CertificationType.StandardExoSuit,
          CertificationType.AgileExoSuit,
          CertificationType.ReinforcedExoSuit
        ),
        List(),
        "xpe_sanctuary_help" :: "xpe_th_firemodes" :: "used_beamer" :: "map13" :: Nil,
        List.empty,
        None
      )
      val inv = InventoryData(
        InventoryItemData(ObjectClass.beamer, PlanetSideGUID(76), 0, DetailedWeaponData(4, 8, ObjectClass.energy_cell, PlanetSideGUID(77), 0, DetailedAmmoBoxData(8, 16))) ::
        InventoryItemData(ObjectClass.suppressor, PlanetSideGUID(78), 2, DetailedWeaponData(4, 8, ObjectClass.bullet_9mm, PlanetSideGUID(79), 0, DetailedAmmoBoxData(8, 25))) ::
        InventoryItemData(ObjectClass.forceblade, PlanetSideGUID(80), 4, DetailedWeaponData(4, 8, ObjectClass.melee_ammo, PlanetSideGUID(81), 0, DetailedAmmoBoxData(8, 1))) ::
        InventoryItemData(ObjectClass.locker_container, PlanetSideGUID(82), 5, DetailedLockerContainerData(8)) ::
        InventoryItemData(ObjectClass.bullet_9mm, PlanetSideGUID(83), 6, DetailedAmmoBoxData(8, 50)) ::
        InventoryItemData(ObjectClass.bullet_9mm, PlanetSideGUID(84), 9, DetailedAmmoBoxData(8, 50)) ::
        InventoryItemData(ObjectClass.bullet_9mm, PlanetSideGUID(85), 12, DetailedAmmoBoxData(8, 50)) ::
        InventoryItemData(ObjectClass.bullet_9mm_AP, PlanetSideGUID(86), 33, DetailedAmmoBoxData(8, 50)) ::
        InventoryItemData(ObjectClass.energy_cell, PlanetSideGUID(87), 36, DetailedAmmoBoxData(8, 50)) ::
        InventoryItemData(ObjectClass.remote_electronics_kit, PlanetSideGUID(88), 39, DetailedREKData(8)) ::
        Nil
      )
      val obj = DetailedPlayerData.apply(pos, app, char, inv, DrawnSlot.Pistol1)

      val msg = ObjectCreateDetailedMessage(0x79, PlanetSideGUID(75), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      val pkt_bitv = pkt.toBitVector
      val ori_bitv = string_testchar.toBitVector
      pkt_bitv.take(153) mustEqual ori_bitv.take(153) //skip 1
      pkt_bitv.drop(154).take(422) mustEqual ori_bitv.drop(154).take(422) //skip 126
      pkt_bitv.drop(702).take(29) mustEqual ori_bitv.drop(702).take(29) //skip 1
      pkt_bitv.drop(732) mustEqual ori_bitv.drop(732)
      //TODO work on DetailedCharacterData to make this pass as a single stream
    }

    "encode (character, seated)" in {
      val app : (Int)=>CharacterAppearanceData = CharacterAppearanceData(
        BasicCharacterData(
          "IlllIIIlllIlIllIlllIllI",
          PlanetSideEmpire.VS,
          CharacterGender.Female,
          41,
          1
        ),
        3,
        false,
        false,
        ExoSuitType.Standard,
        "",
        0,
        false,
        2.8125f, 210.9375f,
        true,
        GrenadeState.None,
        false,
        false,
        false,
        RibbonBars()
      )
      val char : (Option[Int])=>DetailedCharacterData = DetailedCharacterData(
        0,
        0,
        100, 100,
        50,
        1, 7, 7,
        100, 100,
        List(
          CertificationType.StandardAssault,
          CertificationType.MediumAssault,
          CertificationType.ATV,
          CertificationType.Harasser,
          CertificationType.StandardExoSuit,
          CertificationType.AgileExoSuit,
          CertificationType.ReinforcedExoSuit
        ),
        List(),
        "xpe_sanctuary_help" :: "xpe_th_firemodes" :: "used_beamer" :: "map13" :: Nil,
        List.empty,
        None
      )
      val inv = InventoryData(
        InventoryItemData(ObjectClass.beamer, PlanetSideGUID(76), 0, DetailedWeaponData(4, 8, ObjectClass.energy_cell, PlanetSideGUID(77), 0, DetailedAmmoBoxData(8, 16))) ::
          InventoryItemData(ObjectClass.suppressor, PlanetSideGUID(78), 2, DetailedWeaponData(4, 8, ObjectClass.bullet_9mm, PlanetSideGUID(79), 0, DetailedAmmoBoxData(8, 25))) ::
          InventoryItemData(ObjectClass.forceblade, PlanetSideGUID(80), 4, DetailedWeaponData(4, 8, ObjectClass.melee_ammo, PlanetSideGUID(81), 0, DetailedAmmoBoxData(8, 1))) ::
          InventoryItemData(ObjectClass.locker_container, PlanetSideGUID(82), 5, DetailedLockerContainerData(8)) ::
          InventoryItemData(ObjectClass.bullet_9mm, PlanetSideGUID(83), 6, DetailedAmmoBoxData(8, 50)) ::
          InventoryItemData(ObjectClass.bullet_9mm, PlanetSideGUID(84), 9, DetailedAmmoBoxData(8, 50)) ::
          InventoryItemData(ObjectClass.bullet_9mm, PlanetSideGUID(85), 12, DetailedAmmoBoxData(8, 50)) ::
          InventoryItemData(ObjectClass.bullet_9mm_AP, PlanetSideGUID(86), 33, DetailedAmmoBoxData(8, 50)) ::
          InventoryItemData(ObjectClass.energy_cell, PlanetSideGUID(87), 36, DetailedAmmoBoxData(8, 50)) ::
          InventoryItemData(ObjectClass.remote_electronics_kit, PlanetSideGUID(88), 39, DetailedREKData(8)) ::
          Nil
      )
      val obj = DetailedPlayerData.apply(app, char, inv, DrawnSlot.Pistol1)

      val msg = ObjectCreateDetailedMessage(0x79, PlanetSideGUID(75), ObjectCreateMessageParent(PlanetSideGUID(43981), 0), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      val pkt_bitv = pkt.toBitVector
      val ori_bitv = string_testchar_seated.toBitVector
//      var test = pkt_bitv
//      while(test.nonEmpty) {
//        val (printHex, save) = test.splitAt(512)
//        test = save
//        println(printHex)
//      }
      pkt_bitv.take(16) mustEqual ori_bitv.take(16)
      pkt_bitv mustEqual ori_bitv
    }

    "encode (character, br32)" in {
      val pos : PlacementData = PlacementData(
        Vector3(5500.0f, 3800.0f, 71.484375f),
        Vector3(0, 0, 90.0f),
        None
      )
      val app : (Int)=>CharacterAppearanceData = CharacterAppearanceData(
        BasicCharacterData("KiCkJr", PlanetSideEmpire.NC, CharacterGender.Male, 24, 4),
        3,
        false, false,
        ExoSuitType.Agile,
        "",
        14,
        false,
        354.375f, 354.375f,
        false,
        GrenadeState.None,
        false, false, false,
        RibbonBars(
          MeritCommendation.Loser4,
          MeritCommendation.EventNCElite,
          MeritCommendation.HeavyAssault6,
          MeritCommendation.SixYearNC
        )
      )
       val char : (Option[Int])=>DetailedCharacterData = DetailedCharacterData(
        6366766,
        694787,
        100, 100, 100,
        1, 7, 7,
        100, 46,
        List(
          CertificationType.StandardAssault,
          CertificationType.MediumAssault,
          CertificationType.HeavyAssault,
          CertificationType.AntiVehicular,
          CertificationType.AirCavalryScout,
          CertificationType.GroundSupport,
          CertificationType.Harasser,
          CertificationType.StandardExoSuit,
          CertificationType.AgileExoSuit,
          CertificationType.Medical,
          CertificationType.AdvancedMedical,
          CertificationType.Hacking,
          CertificationType.AdvancedHacking,
          CertificationType.Engineering,
          CertificationType.CombatEngineering
        ),
        List(
          ImplantEntry(ImplantType.AudioAmplifier, None),
          ImplantEntry(ImplantType.Targeting, None),
          ImplantEntry(ImplantType.Surge, None)
        ),
        List(
          "xpe_overhead_map",
          "xpe_warp_gate",
          "xpe_form_outfit",
          "xpe_blackops",
          "xpe_command_rank_5",
          "xpe_command_rank_3",
          "xpe_sanctuary_help",
          "xpe_battle_rank_13",
          "xpe_battle_rank_12",
          "xpe_battle_rank_10",
          "xpe_battle_rank_14",
          "xpe_battle_rank_15",
          "xpe_orbital_shuttle",
          "xpe_drop_pod",
          "xpe_bind_facility",
          "xpe_battle_rank_3",
          "xpe_battle_rank_5",
          "xpe_battle_rank_4",
          "xpe_join_squad",
          "xpe_form_squad",
          "xpe_instant_action",
          "xpe_battle_rank_2",
          "xpe_warp_gate_usage",
          "xpe_battle_rank_8",
          "xpe_battle_rank_11",
          "xpe_battle_rank_6",
          "xpe_mail_alert",
          "xpe_command_rank_1",
          "xpe_battle_rank_20",
          "xpe_battle_rank_18",
          "xpe_battle_rank_19",
          "xpe_join_platoon",
          "xpe_battle_rank_17",
          "xpe_battle_rank_16",
          "xpe_join_outfit",
          "xpe_battle_rank_25",
          "xpe_battle_rank_24",
          "xpe_command_rank_4",
          "xpe_form_platoon",
          "xpe_bind_ams",
          "xpe_battle_rank_9",
          "xpe_battle_rank_7",
          "xpe_th_router",
          "xpe_th_flail",
          "xpe_th_ant",
          "xpe_th_ams",
          "xpe_th_ground_p",
          "xpe_th_air_p",
          "xpe_th_hover",
          "xpe_th_ground",
          "xpe_th_bfr",
          "xpe_th_afterburner",
          "xpe_th_air",
          "xpe_th_cloak",
          "used_oicw",
          "used_advanced_ace",
          "visited_spitfire_turret",
          "visited_spitfire_cloaked",
          "visited_spitfire_aa",
          "visited_tank_traps",
          "visited_portable_manned_turret_nc",
          "visited_portable_manned_turret_tr",
          "used_magcutter",
          "used_chainblade",
          "used_forceblade",
          "visited_wall_turret",
          "visited_ancient_terminal",
          "visited_ams",
          "visited_ant",
          "visited_dropship",
          "visited_liberator",
          "visited_lightgunship",
          "visited_lightning",
          "visited_magrider",
          "visited_prowler",
          "visited_quadstealth",
          "visited_skyguard",
          "visited_threemanheavybuggy",
          "visited_two_man_assault_buggy",
          "visited_twomanheavybuggy",
          "visited_twomanhoverbuggy",
          "visited_vanguard",
          "visited_flail",
          "visited_router",
          "visited_switchblade",
          "visited_aurora",
          "visited_battlewagon",
          "visited_fury",
          "visited_quadassault",
          "visited_galaxy_gunship",
          "visited_apc_tr",
          "visited_apc_vs",
          "visited_lodestar",
          "visited_phantasm",
          "visited_thunderer",
          "visited_apc_nc",
          "visited_vulture",
          "visited_wasp",
          "visited_mosquito",
          "visited_aphelion_flight",
          "visited_aphelion_gunner",
          "visited_colossus_flight",
          "visited_colossus_gunner",
          "visited_peregrine_flight",
          "visited_peregrine_gunner",
          "used_bank",
          "visited_resource_silo",
          "visited_certification_terminal",
          "visited_med_terminal",
          "used_nano_dispenser",
          "visited_sensor_shield",
          "visited_broadcast_warpgate",
          "used_phalanx",
          "used_phalanx_avcombo",
          "used_phalanx_flakcombo",
          "visited_warpgate_small",
          "used_flamethrower",
          "used_ancient_turret_weapon",
          "visited_LLU_socket",
          "used_energy_gun_nc",
          "visited_mediumtransport",
          "used_aphelion_immolation_cannon",
          "used_grenade_plasma",
          "used_grenade_jammer",
          "visited_shield_generator",
          "visited_motion_sensor",
          "visited_health_crystal",
          "visited_repair_crystal",
          "visited_vehicle_crystal",
          "used_grenade_frag",
          "used_ace",
          "visited_adv_med_terminal",
          "used_beamer",
          "used_bolt_driver",
          "used_cycler",
          "used_gauss",
          "used_hunterseeker",
          "used_isp",
          "used_lancer",
          "used_lasher",
          "used_maelstrom",
          "used_phoenix",
          "used_pulsar",
          "used_punisher",
          "used_r_shotgun",
          "used_radiator",
          "used_rek",
          "used_repeater",
          "used_rocklet",
          "used_striker",
          "used_suppressor",
          "used_thumper",
          "visited_vanu_control_console",
          "visited_capture_terminal",
          "used_mini_chaingun",
          "used_laze_pointer",
          "used_telepad",
          "used_spiker",
          "used_heavy_sniper",
          "used_command_uplink",
          "used_firebird",
          "used_flechette",
          "used_heavy_rail_beam",
          "used_ilc9",
          "visited_generator_terminal",
          "visited_locker",
          "visited_external_door_lock",
          "visited_air_vehicle_terminal",
          "visited_galaxy_terminal",
          "visited_implant_terminal",
          "visited_secondary_capture",
          "used_25mm_cannon",
          "used_liberator_bombardier",
          "visited_repair_silo",
          "visited_vanu_module",
          "used_flail_weapon",
          "used_scythe",
          "visited_respawn_terminal",
          "used_ballgun",
          "used_energy_gun_tr",
          "used_anniversary_guna",
          "used_anniversary_gunb",
          "used_anniversary_gun",
          "used_75mm_cannon",
          "used_apc_nc_weapon",
          "used_apc_tr_weapon",
          "used_apc_vs_weapon",
          "used_flux_cannon",
          "used_aphelion_plasma_rocket_pod",
          "used_aphelion_ppa",
          "used_fluxpod",
          "visited_bfr_terminal",
          "used_colossus_cluster_bomb_pod",
          "used_colossus_dual_100mm_cannons",
          "used_colossus_tank_cannon",
          "visited_energy_crystal",
          "used_heavy_grenade_launcher",
          "used_35mm_rotarychaingun",
          "used_katana",
          "used_35mm_cannon",
          "used_reaver_weapons",
          "used_lightning_weapons",
          "used_med_app",
          "used_20mm_cannon",
          "visited_monolith_amerish",
          "visited_monolith_ceryshen",
          "visited_monolith_cyssor",
          "visited_monolith_esamir",
          "visited_monolith_forseral",
          "visited_monolith_ishundar",
          "visited_monolith_searhus",
          "visited_monolith_solsar",
          "used_nc_hev_falcon",
          "used_nc_hev_scattercannon",
          "used_nc_hev_sparrow",
          "used_armor_siphon",
          "used_peregrine_dual_machine_gun",
          "used_peregrine_dual_rocket_pods",
          "used_peregrine_mechhammer",
          "used_peregrine_particle_cannon",
          "used_peregrine_sparrow",
          "used_105mm_cannon",
          "used_15mm_chaingun",
          "used_pulsed_particle_accelerator",
          "used_rotarychaingun",
          "visited_deconstruction_terminal",
          "used_skyguard_weapons",
          "visited_generator",
          "used_gauss_cannon",
          "used_trek",
          "used_vanguard_weapons",
          "visited_ancient_air_vehicle_terminal",
          "visited_ancient_equipment_terminal",
          "visited_order_terminal",
          "visited_ancient_ground_vehicle_terminal",
          "visited_ground_vehicle_terminal",
          "used_vulture_bombardier",
          "used_vulture_nose_cannon",
          "used_vulture_tail_cannon",
          "used_wasp_weapon_system",
          "visited_charlie01",
          "visited_charlie02",
          "visited_charlie03",
          "visited_charlie04",
          "visited_charlie05",
          "visited_charlie06",
          "visited_charlie07",
          "visited_charlie08",
          "visited_charlie09",
          "visited_gingerman_atar",
          "visited_gingerman_dahaka",
          "visited_gingerman_hvar",
          "visited_gingerman_izha",
          "visited_gingerman_jamshid",
          "visited_gingerman_mithra",
          "visited_gingerman_rashnu",
          "visited_gingerman_sraosha",
          "visited_gingerman_yazata",
          "visited_gingerman_zal",
          "visited_sled01",
          "visited_sled02",
          "visited_sled04",
          "visited_sled05",
          "visited_sled06",
          "visited_sled07",
          "visited_sled08",
          "visited_snowman_amerish",
          "visited_snowman_ceryshen",
          "visited_snowman_cyssor",
          "visited_snowman_esamir",
          "visited_snowman_forseral",
          "visited_snowman_hossin",
          "visited_snowman_ishundar",
          "visited_snowman_searhus",
          "visited_snowman_solsar",
          "ugd06",
          "ugd05",
          "ugd04",
          "ugd03",
          "ugd02",
          "ugd01",
          "map99",
          "map98",
          "map97",
          "map96",
          "map15",
          "map14",
          "map11",
          "map08",
          "map04",
          "map05",
          "map03",
          "map01",
          "map06",
          "map02",
          "map09",
          "map07",
          "map10"
        ),
        List(
          "training_start_nc",
          "training_ui",
          "training_map"
        ),
        Some(Cosmetics(true, true, true, true, false))
      )
      val inv = InventoryData(
        List(
          InternalSlot(531, PlanetSideGUID(4202), 0,
            DetailedWeaponData(2, 8, 0, List(InternalSlot(389, PlanetSideGUID(3942), 0,DetailedAmmoBoxData(8, 100))))
          ),
          InternalSlot(132, PlanetSideGUID(6924), 1,
            DetailedWeaponData(2, 8, 0, List(InternalSlot(111, PlanetSideGUID(9157), 0, DetailedAmmoBoxData(8, 100))))
          ),
          InternalSlot(714, PlanetSideGUID(8498), 2,
            DetailedWeaponData(2, 8, 0, List(InternalSlot(755, PlanetSideGUID(5356), 0, DetailedAmmoBoxData(8, 16))))
          ),
          InternalSlot(468, PlanetSideGUID(7198), 4,
            DetailedWeaponData(2, 8, 0, List(InternalSlot(540, PlanetSideGUID(5009), 0, DetailedAmmoBoxData(8, 1))))
          ),
          InternalSlot(456, PlanetSideGUID(5374), 5,
            DetailedLockerContainerData(8, Some(InventoryData(List(
              InternalSlot(429, PlanetSideGUID(3021), 0,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(272, PlanetSideGUID(8729), 0, DetailedAmmoBoxData(8, 0))))
              ),
              InternalSlot(838, PlanetSideGUID(8467), 9,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(839, PlanetSideGUID(8603), 0, DetailedAmmoBoxData(8, 5))))
              ),
              InternalSlot(272, PlanetSideGUID(3266), 18, DetailedAmmoBoxData(8, 27)),
              InternalSlot(577, PlanetSideGUID(2934), 22,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(111, PlanetSideGUID(4682), 0, DetailedAmmoBoxData(8, 100))))
              ),
              InternalSlot(839, PlanetSideGUID(3271), 90, DetailedAmmoBoxData(8, 15)),
              InternalSlot(839, PlanetSideGUID(7174), 94, DetailedAmmoBoxData(8, 6)),
              InternalSlot(429, PlanetSideGUID(6084), 98,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(272, PlanetSideGUID(5928), 0, DetailedAmmoBoxData(8, 35))))
              ),
              InternalSlot(462, PlanetSideGUID(5000), 108,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(463, PlanetSideGUID(6277), 0, DetailedAmmoBoxData(8, 150))))
              ),
              InternalSlot(429, PlanetSideGUID(4341), 189,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(272, PlanetSideGUID(7043), 0, DetailedAmmoBoxData(8, 35))))
              ),
              InternalSlot(556, PlanetSideGUID(4168), 198,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(28, PlanetSideGUID(8937), 0, DetailedAmmoBoxData(8, 100))))
              ),
              InternalSlot(272, PlanetSideGUID(3173), 207, DetailedAmmoBoxData(8, 50)),
              InternalSlot(462, PlanetSideGUID(3221), 210,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(463, PlanetSideGUID(4031), 0, DetailedAmmoBoxData(8, 150))))
              ),
              InternalSlot(556, PlanetSideGUID(6853), 280,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(29, PlanetSideGUID(8524), 0, DetailedAmmoBoxData(8, 67))))
              ),
              InternalSlot(556, PlanetSideGUID(4569), 290,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(28, PlanetSideGUID(5584), 0, DetailedAmmoBoxData(8, 100))))
              ),
              InternalSlot(462, PlanetSideGUID(9294), 300,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(463, PlanetSideGUID(3118), 0, DetailedAmmoBoxData(8, 150))))
              ),
              InternalSlot(272, PlanetSideGUID(4759), 387, DetailedAmmoBoxData(8, 50)),
              InternalSlot(462, PlanetSideGUID(7377), 390,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(463, PlanetSideGUID(8155), 0, DetailedAmmoBoxData(8, 150))))
              ),
              InternalSlot(843, PlanetSideGUID(6709), 480, DetailedAmmoBoxData(8, 1)),
              InternalSlot(843, PlanetSideGUID(5276), 484, DetailedAmmoBoxData(8, 1)),
              InternalSlot(843, PlanetSideGUID(7769), 488, DetailedAmmoBoxData(8, 1)),
              InternalSlot(844, PlanetSideGUID(5334), 492, DetailedAmmoBoxData(8, 1)),
              InternalSlot(844, PlanetSideGUID(6219), 496, DetailedAmmoBoxData(8, 1)),
              InternalSlot(842, PlanetSideGUID(7279), 500, DetailedAmmoBoxData(8, 1)),
              InternalSlot(842, PlanetSideGUID(5415), 504, DetailedAmmoBoxData(8, 1)),
              InternalSlot(175, PlanetSideGUID(5741), 540,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(5183), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(6208), 541,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(5029), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(8589), 542,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(9217), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(175, PlanetSideGUID(8901), 543,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(7633), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(175, PlanetSideGUID(8419), 544,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(6546), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(175, PlanetSideGUID(4715), 545,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(8453), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(3577), 546,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(9202), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(6003), 547,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(3260), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(9140), 548,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540,PlanetSideGUID(3815),0,DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(4913), 549,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540,PlanetSideGUID(7222),0,DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(6954), 550,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540,PlanetSideGUID(2953),0,DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(6405), 551,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540,PlanetSideGUID(4676),0,DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(8915), 552,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540,PlanetSideGUID(4018),0,DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(4993), 553,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540,PlanetSideGUID(6775),0,DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(175, PlanetSideGUID(5053), 554,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540,PlanetSideGUID(6418),0,DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(9244), 555,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540,PlanetSideGUID(3327),0,DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(468, PlanetSideGUID(6292), 556,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540,PlanetSideGUID(6918),0,DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(842, PlanetSideGUID(5357), 558, DetailedAmmoBoxData(8, 1)),
              InternalSlot(844, PlanetSideGUID(4435), 562, DetailedAmmoBoxData(8, 1)),
              InternalSlot(843, PlanetSideGUID(7242), 566, DetailedAmmoBoxData(8, 1)),
              InternalSlot(175, PlanetSideGUID(7330), 570,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(4786), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(468, PlanetSideGUID(7415), 571,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(6536), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(175, PlanetSideGUID(3949), 572,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(7526), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(175, PlanetSideGUID(3805), 573,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(7358), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(4493), 574,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(6852), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(5762), 575,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(3463), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(175, PlanetSideGUID(3315), 576,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(7619), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(324, PlanetSideGUID(6263), 577,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(5912), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(468, PlanetSideGUID(4028), 578,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(8021), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(175, PlanetSideGUID(2843), 579,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(7250), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(175, PlanetSideGUID(9143), 580,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(5195), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(468, PlanetSideGUID(5024), 581,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(4287), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(468, PlanetSideGUID(6582), 582,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(4915), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(468, PlanetSideGUID(6425), 583,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(8872), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(468, PlanetSideGUID(4431), 584,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(4191), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(175, PlanetSideGUID(8339), 585,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(7317), 0, DetailedAmmoBoxData(8, 1))))
              ),
              InternalSlot(175, PlanetSideGUID(3277), 586,
                DetailedWeaponData(6, 8, 0, List(InternalSlot(540, PlanetSideGUID(6469), 0, DetailedAmmoBoxData(8, 1))))
              )
            ))))
          ),
          InternalSlot(213, PlanetSideGUID(6877), 6, DetailedCommandDetonaterData(4, 8)),
          InternalSlot(755, PlanetSideGUID(6227), 9, DetailedAmmoBoxData(8, 16)),
          InternalSlot(728, PlanetSideGUID(7181), 12, DetailedREKData(4, 16)),
          InternalSlot(536, PlanetSideGUID(4077), 33, DetailedAmmoBoxData(8, 1)),
          InternalSlot(680, PlanetSideGUID(4377), 37,
            DetailedWeaponData(2, 8, 0, List(InternalSlot(681, PlanetSideGUID(8905), 0, DetailedAmmoBoxData(8, 3))))
          ),
          InternalSlot(32, PlanetSideGUID(5523), 39, DetailedACEData(4)),
          InternalSlot(673, PlanetSideGUID(3661), 60,
            DetailedWeaponData(2, 8, 0, List(InternalSlot(674, PlanetSideGUID(8542), 0, DetailedAmmoBoxData(8, 3))))
          )
        )
      )
      val obj = DetailedPlayerData(pos, app, char, inv, DrawnSlot.None)

      val msg = ObjectCreateDetailedMessage(ObjectClass.avatar, PlanetSideGUID(75), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      val pkt_bitv = pkt.toBitVector
      val ori_bitv = string_testchar_br32.toBitVector
      //pkt_bitv mustEqual ori_bitv
      pkt_bitv.take(153) mustEqual ori_bitv.take(153) //skip 1
      pkt_bitv.drop(154).take(144) mustEqual ori_bitv.drop(154).take(144) //skip 24
      pkt_bitv.drop(322).take(72) mustEqual ori_bitv.drop(322).take(72) //skip 24
      pkt_bitv.drop(418).take(55) mustEqual ori_bitv.drop(418).take(55) //skip 1
      pkt_bitv.drop(474).take(102) mustEqual ori_bitv.drop(474).take(102) //skip 126
      pkt_bitv.drop(702).take(192) mustEqual ori_bitv.drop(702).take(192) //skip 36
      pkt_bitv.drop(930) mustEqual ori_bitv.drop(930) //to end
    }
  }
}
