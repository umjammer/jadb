package se.vidstige.jadb.test.integration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.managers.Package;
import se.vidstige.jadb.managers.PackageManager;

import static org.junit.jupiter.api.Assumptions.abort;


public class PackageManagerTests {

    private static JadbConnection jadb;
    private PackageManager pm;
    private final File miniApk = new File("src/test/resources/data/Tiniest Smallest APK ever.apk");

    @BeforeAll
    public static void connect() {
        try {
            jadb = new JadbConnection();
            jadb.getHostVersion();
        } catch (Exception e) {
e.printStackTrace();
            abort(e.getMessage());
        }
    }

    @BeforeEach
    public void createPackageManager()
    {
        pm = new PackageManager(jadb.getAnyDevice());
    }

    @Test
    public void testLaunchActivity() throws Exception {
        pm.launch(new Package("com.android.settings"));
    }

    @Test
    public void testListPackages() throws Exception {
        List<Package> packages = pm.getPackages();
        for (Package p : packages) {
            System.out.println(p);
        }
    }

    @Test
    public void testInstallUninstallCycle() throws Exception {
        pm.install(miniApk);
        pm.forceInstall(miniApk);
        pm.uninstall(new Package("b.a"));
    }


    @Test
    public void testInstallWithOptionsUninstallCycle() throws Exception {
        pm.install(miniApk);
        pm.installWithOptions(miniApk, Arrays.asList(PackageManager.REINSTALL_KEEPING_DATA, PackageManager.ALLOW_VERSION_DOWNGRADE));
        pm.uninstall(new Package("b.a"));
    }
}
