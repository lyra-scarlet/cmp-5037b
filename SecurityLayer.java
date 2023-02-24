import java.nio.ByteBuffer;
import java.util.Random;
import java.util.HexFormat;

public class SecurityLayer {
    private byte[] key = ByteBuffer.allocate(64).putInt(Config.getInt("key")).array();
    private int keyPosition = 0;

    public static byte byteFromKey() {
        return 0;
    }

    public static byte[] Encrypt(byte[] buffer) {
        return buffer;
    }

    public static void main (String[] args) {
        byte[] data = new byte[64];
        new Random().nextBytes(data);
        System.out.println(HexFormat.of().formatHex(data));
    }
}
