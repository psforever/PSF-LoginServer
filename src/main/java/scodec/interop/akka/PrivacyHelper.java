package scodec.interop.akka;

import java.nio.ByteBuffer;

import akka.util.ByteString.ByteString1C;

interface PrivacyHelper {

    static ByteString1C createByteString1C(byte[] array) {
        return new ByteString1C(array);
    }

}