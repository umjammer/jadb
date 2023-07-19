package se.vidstige.jadb.test.integration;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.abort;


public class ExecuteCmdTests {

    private static JadbConnection jadb;
    private static JadbDevice jadbDevice;

    @BeforeAll
    public static void connect() {
        try {
            jadb = new JadbConnection();
            jadb.getHostVersion();
            jadbDevice = jadb.getAnyDevice();
        } catch (Exception e) {
            abort(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "öäasd",
            "asf dsa",
            "sdf&g",
            "sd& fg",
            "da~f",
            "asd'as",
            "a¡f",
            "asüd",
            "adös tz",
            "⾀",
            "å",
            "æ",
            "{}"
    })
    public void testExecuteWithSpecialChars(String input) throws Exception {
        InputStream response = jadbDevice.execute("echo", input);
        assertEquals(input, Stream.readAll(response, StandardCharsets.UTF_8).replaceAll("\n$", ""));
    }
}
