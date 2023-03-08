import java.nio.ByteBuffer;
import java.util.Random;
import java.util.HexFormat;

public class SecurityLayer {
    private static final byte[] key;
    private static int keyPosition = 0;
    static {
        key = ByteBuffer.allocate(Long.BYTES).putLong(Config.getLong("key")).array();
    }

    private static byte nextKeyByte() {
        if (keyPosition >= key.length) keyPosition = 0;
        return key[keyPosition++];
    }

    public static void EncryptDecrypt(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] ^= nextKeyByte();
        }
        keyPosition = 0;
    }

    public static byte[] CalcChecksum(byte[] data) {
        short checksum = 0;
        for (byte datum : data) {
            checksum += Math.abs(datum) * 3;
        }
        return ByteBuffer.allocate(Short.BYTES).putShort(checksum).array();
    }

    public static void main (String[] args) {
        // Test harness
        byte[] data = new byte[512];
        new Random().nextBytes(data);
        System.out.println("Key:   "+HexFormat.of().formatHex(key));
        System.out.println("Original:   "+HexFormat.of().formatHex(data));
        System.out.println("Original checksum: "+HexFormat.of().formatHex(CalcChecksum(data)));
        EncryptDecrypt(data);
        System.out.println("Encrypted:  "+HexFormat.of().formatHex(data));
        EncryptDecrypt(data);
        System.out.println("Decrypted:  "+HexFormat.of().formatHex(data));
        System.out.println("Decrypted checksum: "+HexFormat.of().formatHex(CalcChecksum(data)));
    }
}
