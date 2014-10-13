import java.nio.ByteBuffer;

/**
 * Created by OnlyUno on 2014/05/09.
 */
public class TestStringBuffer {
        public static void main(String... a) {
                ByteBuffer buffer = ByteBuffer.allocate(20);
                final byte[] org = "ABC".getBytes();
                byte[] tar = new byte[20];
                System.arraycopy(org, 0, tar, 0, org.length);
                buffer.put(tar, 0, 20);
                byte[] b = new byte[20];
                buffer.flip();
                buffer.get(b, 0, 20);
                System.out.println(new String(b).trim());
        }
}
