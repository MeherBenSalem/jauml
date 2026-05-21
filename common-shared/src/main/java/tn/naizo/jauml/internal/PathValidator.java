package tn.naizo.jauml.internal;

import java.nio.file.Path;

public final class PathValidator {

    private PathValidator() {}

    /**
     * Resolves a subdirectory and filename relative to a base directory,
     * ensuring that no path traversal attacks can escape the base directory.
     *
     * @param baseDir the game's config directory
     * @param subdirectory the target subdirectory name
     * @param fileName the target config filename (e.g. "mymod.json")
     * @return the verified absolute, normalized Path
     * @throws IllegalArgumentException if a path traversal attempt is detected
     */
    public static Path resolveSafe(Path baseDir, String subdirectory, String fileName) {
        if (baseDir == null) {
            throw new IllegalArgumentException("Base directory cannot be null");
        }
        if (subdirectory == null || subdirectory.trim().isEmpty()) {
            throw new IllegalArgumentException("Subdirectory cannot be null or empty");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        // Standardize file extension to JSON
        String sanitizedFileName = fileName;
        if (!sanitizedFileName.toLowerCase().endsWith(".json")) {
            sanitizedFileName = sanitizedFileName + ".json";
        }

        // Normalize base path
        Path absoluteBase = baseDir.toAbsolutePath().normalize();

        // Resolve and normalize final path
        Path resolvedPath = absoluteBase.resolve(subdirectory).resolve(sanitizedFileName).toAbsolutePath().normalize();

        // Security check: Verify that the resolved path is strictly within the base directory
        if (!resolvedPath.startsWith(absoluteBase)) {
            throw new IllegalArgumentException("Directory traversal attack detected! Attempted to access path: " + resolvedPath + " which is outside base directory: " + absoluteBase);
        }

        return resolvedPath;
    }
}
