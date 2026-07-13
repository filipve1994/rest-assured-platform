package com.framework.di;

import com.framework.auth.AuthenticationManager;
import com.framework.config.EnvironmentConfig;
import com.framework.http.HttpClientFactory;
import com.google.inject.AbstractModule;

/**
 * The platform's single Guice binding module. Binds the framework's core
 * services as eager singletons so:
 *   - config is loaded exactly once per JVM/test run
 *   - the auth token cache is shared across every Steps class
 *   - the HTTP client factory is shared too (cheap, but consistent)
 *
 * All three classes are also perfectly usable "by hand" (they have plain
 * public constructors annotated @Inject) - Guice isn't required to use them,
 * it just removes the need for every consumer to wire the constructor chain
 * (EnvironmentConfig -> AuthenticationManager -> HttpClientFactory) themselves.
 *
 * Add bindings for any NEW framework-* module here, and only here - this is
 * the platform's single composition root.
 */
public class PlatformModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(EnvironmentConfig.class).asEagerSingleton();
        bind(AuthenticationManager.class).asEagerSingleton();
        bind(HttpClientFactory.class).asEagerSingleton();
    }
}
