package se.vidstige.jadb.test.unit;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.managers.Package;
import se.vidstige.jadb.managers.PackageManager;
import se.vidstige.jadb.test.fakes.FakeAdbServer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PackageManagerTest {
    private static final String DEVICE_SERIAL = "serial-123";

    private FakeAdbServer server;
    private JadbDevice device;

    @BeforeEach
    public void setUp() throws Exception {
        server = new FakeAdbServer(15037);
        server.start();
        server.add(DEVICE_SERIAL);
        device = new JadbConnection("localhost", 15037).getDevices().get(0);
    }

    @AfterEach
    public void tearDown() throws Exception {
        server.stop();
        server.verifyExpectations();
    }

    @Test
    public void testGetPackagesWithSeveralPackages() throws Exception {
        // Arrange
        List<Package> expected = new ArrayList<>();
        expected.add(new Package("/system/priv-app/Contacts.apk-com.android.contacts"));
        expected.add(new Package("/system/priv-app/Teleservice.apk-com.android.phone"));

        String response = "package:/system/priv-app/Contacts.apk-com.android.contacts\n" +
                "package:/system/priv-app/Teleservice.apk-com.android.phone";

        server.expectShell(DEVICE_SERIAL, "pm 'list' 'packages'").returns(response);

        // Act
        List<Package> actual = new PackageManager(device).getPackages();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    public void testGetPackagesMalformedIgnoredString() throws Exception {
        // Arrange
        List<Package> expected = new ArrayList<>();
        expected.add(new Package("/system/priv-app/Contacts.apk-com.android.contacts"));
        expected.add(new Package("/system/priv-app/Teleservice.apk-com.android.phone"));

        String response = "package:/system/priv-app/Contacts.apk-com.android.contacts\n" +
                "[malformed_line]\n" +
                "package:/system/priv-app/Teleservice.apk-com.android.phone";

        server.expectShell(DEVICE_SERIAL, "pm 'list' 'packages'").returns(response);

        // Act
        List<Package> actual = new PackageManager(device).getPackages();

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    public void testGetPackagesWithNoPackages() throws Exception {
        // Arrange
        List<Package> expected = new ArrayList<>();
        String response = "";

        server.expectShell(DEVICE_SERIAL, "pm 'list' 'packages'").returns(response);

        // Act
        List<Package> actual = new PackageManager(device).getPackages();

        // Assert
        assertEquals(expected, actual);
    }
}