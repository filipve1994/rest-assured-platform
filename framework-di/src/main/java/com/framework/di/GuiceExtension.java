package com.framework.di;

import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that injects @Inject-annotated fields on a test instance
 * from the platform's shared Guice injector. Register it (usually via a base
 * test class) with:
 *
 *   @ExtendWith(GuiceExtension.class)
 *   public abstract class BaseApiTest { ... }
 *
 * Then any test class extending that base can simply declare:
 *
 *   @Inject
 *   private UserSteps userSteps;
 *
 * ...instead of `new UserSteps()`, and Guice resolves UserSteps' own
 * constructor dependencies (HttpClientFactory, AuthenticationManager, etc.)
 * automatically.
 */
public class GuiceExtension implements TestInstancePostProcessor {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        Injectors.get().injectMembers(testInstance);
    }
}
