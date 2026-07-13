package com.framework.reporting;

import com.framework.config.ConfigLoader;
import com.framework.config.TestContext;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared JUnit 5 lifecycle glue every test module on the platform should
 * register (typically via a base test class annotated
 * {@code @ExtendWith(TestLifecycleExtension.class)}):
 *
 *  - assigns a fresh correlation id per test (traceable through logs)
 *  - attaches the active environment + correlation id to the Allure report
 *  - logs test start/finish
 *  - clears the ThreadLocal TestContext so state never leaks between tests
 *    on a reused thread-pool thread under parallel execution
 *
 * Deliberately a plain JUnit5 extension rather than a DI-injected class:
 * JUnit constructs extensions itself via @ExtendWith/ServiceLoader, before
 * any Guice injector exists, so it reaches for the few static, side-effect-free
 * accessors (ConfigLoader.activeEnvironment(), TestContext) rather than a
 * fully-loaded EnvironmentConfig instance.
 */
public class TestLifecycleExtension implements BeforeEachCallback, AfterEachCallback {

    private static final Logger log = LoggerFactory.getLogger(TestLifecycleExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        TestContext.newCorrelationId();
        String env = ConfigLoader.activeEnvironment();
        Allure.parameter("environment", env);
        Allure.parameter("correlationId", TestContext.correlationId());
        log.info(">>> Starting test '{}' [env={}, correlationId={}]",
                context.getDisplayName(), env, TestContext.correlationId());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        log.info("<<< Finished test '{}' [correlationId={}]", context.getDisplayName(), TestContext.correlationId());
        TestContext.clear();
    }
}
