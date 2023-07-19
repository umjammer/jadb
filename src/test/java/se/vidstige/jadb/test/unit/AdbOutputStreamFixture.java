package se.vidstige.jadb.test.unit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.Test;
import se.vidstige.jadb.AdbFilterOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;


public class AdbOutputStreamFixture {

    private byte[] passthrough(byte[] input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (OutputStream sut = new AdbFilterOutputStream(output)) {
            sut.write(input);
            sut.flush();
        }
        return output.toByteArray();
    }

    @Test
    public void testSimple() throws Exception {
        byte[] actual = passthrough(new byte[]{ 1, 2, 3});
        assertArrayEquals(new byte[]{ 1, 2, 3}, actual);
    }

    @Test
    public void testEmpty() throws Exception {
        byte[] actual = passthrough(new byte[]{});
        assertArrayEquals(new byte[]{}, actual);
    }

    @Test
    public void testSimpleRemoval() throws Exception {
        byte[] actual = passthrough(new byte[]{0x0d, 0x0a});
        assertArrayEquals(new byte[]{0x0a}, actual);
    }

    @Test
    public void testDoubleRemoval() throws Exception {
        byte[] actual = passthrough(new byte[]{0x0d, 0x0a, 0x0d, 0x0a});
        assertArrayEquals(new byte[]{0x0a, 0x0a}, actual);
    }
}
