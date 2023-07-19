package se.vidstige.jadb.test.unit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.vidstige.jadb.AdbServerLauncher;
import se.vidstige.jadb.test.fakes.FakeSubprocess;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class AdbServerLauncherFixture {

    private FakeSubprocess subprocess;
    private Map<String, String> environment = new HashMap<>();

    @BeforeEach
    public void setUp() {
        subprocess = new FakeSubprocess();
    }
    @AfterEach
    public void tearDown() {
        subprocess.verifyExpectations();
    }

    @Test
    public void testStartServer() throws Exception {
        subprocess.expect(new String[]{"/abc/platform-tools/adb", "start-server"}, 0);
        Map<String, String> environment = new HashMap<>();
        environment.put("ANDROID_HOME", "/abc");
        new AdbServerLauncher(subprocess, environment).launch();
    }

    @Test
    public void testStartServerWithoutANDROID_HOME() throws Exception {
        subprocess.expect(new String[]{"adb", "start-server"}, 0);
        new AdbServerLauncher(subprocess, environment).launch();
    }

    @Test
    public void testStartServerFails() {
        assertThrows(IOException.class, () -> {
            subprocess.expect(new String[]{"adb", "start-server"}, -1);
            new AdbServerLauncher(subprocess, environment).launch();
        });
    }
}
