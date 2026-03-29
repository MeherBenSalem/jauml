package tn.naizo.jauml.platform;

import tn.naizo.jauml.Constants;
import tn.naizo.jauml.platform.services.IPlatformHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {
        final ServiceLoader<T> loader = ServiceLoader.load(clazz);
        final List<T> implementations = new ArrayList<>();
        loader.forEach(implementations::add);

        if (implementations.isEmpty()) {
            throw new IllegalStateException("Failed to load service for " + clazz.getName() + ". Check META-INF/services registration.");
        }

        final T loadedService = implementations.get(0);
        if (implementations.size() > 1) {
            Constants.LOG.warn("Multiple implementations found for {}. Using first: {}", clazz.getName(), loadedService.getClass().getName());
        } else {
            Constants.LOG.debug("Loaded {} for service {}", loadedService.getClass().getName(), clazz.getName());
        }
        return loadedService;
    }
}
