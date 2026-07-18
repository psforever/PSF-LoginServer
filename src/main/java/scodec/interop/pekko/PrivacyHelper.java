package scodec.interop.pekko;

import java.nio.ByteBuffer;

import org.apache.pekko.util.ByteString.ByteString1C;

interface PrivacyHelper {

    static ByteString1C createByteString1C(byte[] array) {
        return new ByteString1C(array);
    }

}
