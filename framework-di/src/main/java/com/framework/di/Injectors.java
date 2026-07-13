package com.framework.di;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Holds the one Guice Injector for the whole test JVM. Deliberately a tiny,
 * boring holder class rather than a service locator sprinkled through
 * business code - the only thing that should ever call
 * {@link #get()} directly is {@link GuiceExtension}. Everything else should
 * receive its dependencies via @Inject instead of reaching in here.
 */
public final class Injectors {

    private static final Injector INSTANCE = Guice.createInjector(new PlatformModule());

    private Injectors() {
    }

    public static Injector get() {
        return INSTANCE;
    }
}
