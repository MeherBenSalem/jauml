package tn.naizo.jauml.platform;

import tn.naizo.jauml.Constants;
import tn.naizo.jauml.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

// Service loader to load platform specific services
public class Services {

    // Helper to load the service
    public static final IPlatformHelper PLATFORM;

    static {
        // Services are loaded here
        PLATFORM = load(IPlatformHelper.class);
    }

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
