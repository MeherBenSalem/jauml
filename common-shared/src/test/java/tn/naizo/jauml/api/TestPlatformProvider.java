package tn.naizo.jauml.api;

import tn.naizo.jauml.spi.PlatformProvider;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Mock PlatformProvider for running JUnit tests without a Minecraft server/client environment.
 */
public class TestPlatformProvider implements PlatformProvider {

    private static Path tempDir;

    public static void setTempDir(Path dir) {
        tempDir = dir;
    }

    @Override
    public String getPlatformName() {
        return "UnitTest";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return false;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return true;
    }

    @Override
    public Path getConfigDirectory() {
        if (tempDir == null) {
            return Paths.get(System.getProperty("java.io.tmpdir")).resolve("jauml-test-config");
        }
        return tempDir;
    }
}
