package com.framework.base;

import com.framework.di.GuiceExtension;
import com.framework.reporting.TestLifecycleExtension;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Every API test class in this module should extend this. Two extensions do
 * the plumbing:
 *
 *  - GuiceExtension (framework-di): injects @Inject fields (Steps, Scenario
 *    classes, EnvironmentConfig, ...) from the platform's shared Guice injector
 *    right after the test instance is constructed.
 *  - TestLifecycleExtension (framework-reporting): fresh correlation id per
 *    test, Allure environment/correlation parameters, start/finish logging,
 *    and clears the ThreadLocal TestContext afterwards.
 *
 * Test classes themselves stay framework-agnostic beyond this one annotation -
 * no manual `new XSteps()` and no manual injector lookups anywhere.
 */
@ExtendWith(GuiceExtension.class)
@ExtendWith(TestLifecycleExtension.class)
public abstract class BaseApiTest {
}
