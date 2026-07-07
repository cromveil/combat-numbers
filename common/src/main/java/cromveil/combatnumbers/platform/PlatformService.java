package cromveil.combatnumbers.platform;

import cromveil.combatnumbers.core.Constants;
import cromveil.combatnumbers.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class PlatformService {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz, PlatformService.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}