package piechota.datacontrol;

import java.nio.ByteBuffer;

/**
 * Created by Konrad on 2014-09-19.
 */
public class ByteConverter {
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / 8);

    public static byte[] longToBytes(long x){
        buffer.putLong(0, x);
        return  buffer.array();
    }

    public static long bytesToLong(byte[] bytes){
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        return buffer.getLong();
    }
}
