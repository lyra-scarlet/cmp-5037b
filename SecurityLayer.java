import java.nio.ByteBuffer;
import java.util.Random;
import java.util.HexFormat;

public class SecurityLayer {
    private static byte[] key;
    private static int keyPosition = 0;
    static {
        key = ByteBuffer.allocate(Long.BYTES).putLong(Config.getLong("key")).array();
    }

    public static byte nextKeyByte() {
        if (keyPosition >= key.length) keyPosition = 0;
        return key[keyPosition++];
    }

    public static byte[] EncryptDecrypt(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] ^= nextKeyByte();
        }
        keyPosition = 0;
        return data;
    }

    public static void main (String[] args) {
        // Test harness
        byte[] data = new byte[64];
        new Random().nextBytes(data);
        System.out.println("Key:   "+HexFormat.of().formatHex(key));
        System.out.println("Original:   "+HexFormat.of().formatHex(data));
        data = EncryptDecrypt(data);
        System.out.println("Encrypted:  "+HexFormat.of().formatHex(data));
        data = EncryptDecrypt(data);
        System.out.println("Decrypted:  "+HexFormat.of().formatHex(data));
    }
}
