# REST Assured Enterprise Test Automation Platform

A multi-module Maven / Java 21 / REST Assured 6.0.0 platform. Instead of one
monolithic test project, the framework is split into independently
versionable building blocks, wired together with Google Guice, and consumed
by a dedicated test-execution module that holds the actual test cases.

## Why multi-module?

- **Separation of concerns** - config resolution, auth, HTTP client setup,
  reporting glue, and generic utilities are each a module with one job and a
  narrow, explicit set of dependencies (see the table below).
- **Independent versioning/reuse** - another test-execution module (e.g. one
  per squad, or one for UI-facing contract tests) can depend on the same
  `framework-*` jars without dragging in unrelated test code.
- **Enforced dependency direction** - Maven fails the build if a module
  reaches for something it shouldn't (e.g. `framework-utils` cannot
  accidentally depend on `framework-http`). `framework-di` is the single
  allowed composition root that is permitted to know about everything.
- **Faster, targeted builds** - `mvn install -pl framework-utils -am` builds
  only what that module actually needs.

## Module map

| Module                | Package                | Depends on                                   | Responsibility |
|------------------------|-------------------------|-----------------------------------------------|----------------|
| `framework-config`     | `com.framework.config`  | *(none of the others)*                        | `ConfigLoader`, `EnvironmentConfig`, `TestContext` - merges `application.yml` + `application-{env}.yml`, resolves `${...}` placeholders, per-thread variable bag |
| `framework-auth`       | `com.framework.auth`    | `framework-config`                            | `AuthenticationManager` - fetches/caches bearer tokens per role, auto-refresh |
| `framework-http`       | `com.framework.http`    | `framework-config`, `framework-auth`          | `HttpClientFactory` - builds REST Assured `RequestSpecification`s with base URL, timeouts, Allure filter, auth injection |
| `framework-reporting`  | `com.framework.reporting` | `framework-config`                          | `TestLifecycleExtension` (JUnit5), shared `logback-base.xml` |
| `framework-utils`      | `com.framework.utils`   | *(none of the others)*                        | `JsonUtils`, `ValidationUtils`, `RandomDataUtils` - stateless helpers, no DI needed |
| `framework-di`         | `com.framework.di`      | **all of the above**                          | `PlatformModule` (Guice bindings), `Injectors`, `GuiceExtension` (JUnit5) - the platform's composition root |
| `test-execution`       | `com.framework.*`       | `framework-di`, `framework-utils`             | The only module with actual `@Test` methods: `base/`, `steps/`, `scenarios/`, `tests/`, `examples/`, plus all environment config/test data/schemas |

```
rest-assured-platform/
  pom.xml                     <- parent: dependencyManagement for the whole platform
  framework-config/
  framework-auth/
  framework-http/
  framework-reporting/
  framework-utils/
  framework-di/
  test-execution/
    src/test/java/com/framework/
      base/       BaseApiTest              (@ExtendWith GuiceExtension + TestLifecycleExtension)
      steps/      UserSteps                (one class per resource, @Inject-ed HttpClientFactory)
      scenarios/  UserScenario             (chains Steps into a business flow, @Inject-ed Steps)
      tests/      SampleUserApiTest        (@Inject fields, actual @Test methods)
      examples/   restfulapidev, reqres, jsonplaceholder - same shape, hitting free public sandbox APIs
    src/test/resources/
      application.yml + application-{dev,qa,acc,prod}.yml
      testdata/, schemas/, logback-test.xml, junit-platform.properties
```

## Dependency injection - how it fits together

Every stateful framework service is a plain class with a `jakarta.inject.@Inject`
constructor - there's nothing Guice-specific baked into the classes
themselves:

```java
@Singleton
public class HttpClientFactory {
    @Inject
    public HttpClientFactory(EnvironmentConfig config, AuthenticationManager authManager) { ... }
}
```

`framework-di`'s `PlatformModule` is the only place that binds them:

```java
public class PlatformModule extends AbstractModule {
    protected void configure() {
        bind(EnvironmentConfig.class).asEagerSingleton();
        bind(AuthenticationManager.class).asEagerSingleton();
        bind(HttpClientFactory.class).asEagerSingleton();
    }
}
```

`Injectors` holds the one `Injector` for the JVM, and `GuiceExtension`
(a JUnit 5 `TestInstancePostProcessor`) injects `@Inject` fields into every
test instance right after construction:

```java
@ExtendWith(GuiceExtension.class)
@ExtendWith(TestLifecycleExtension.class)
public abstract class BaseApiTest { }

class SampleUserApiTest extends BaseApiTest {
    @Inject private UserSteps userSteps;      // Guice builds UserSteps -> HttpClientFactory -> ... for you
    @Inject private UserScenario userScenario;

    @Test
    void userLifecycle_createThenDelete() {
        userScenario.createThenDeleteUser("default");
    }
}
```

Not everything is DI-managed, on purpose:

- **`TestContext`** (framework-config) stays a static `ThreadLocal` utility.
  It's per-thread *state*, not a service with dependencies - injecting it
  would add ceremony for zero benefit.
- **`JsonUtils` / `ValidationUtils` / `RandomDataUtils`** (framework-utils)
  stay static utility classes. They're stateless pure functions; there's
  nothing to construct or inject.
- **`TestLifecycleExtension`** (framework-reporting) is a plain JUnit5
  extension, not Guice-managed - JUnit constructs extensions itself, before
  any injector exists, so it only touches the few static, side-effect-free
  accessors it needs (`ConfigLoader.activeEnvironment()`).

## Building

```bash
mvn install                              # builds every module, in dependency order
mvn test -pl test-execution -am          # build only what test-execution needs, then run its tests
mvn test -pl test-execution -am -Denv=qa # same, against the qa environment
```

## Environments

Same resolution model as before, now living in `framework-config` and
configured from `test-execution`'s resources:

```bash
mvn test -pl test-execution -am -Denv=qa
ENV_NAME=acc mvn test -pl test-execution -am
mvn test -pl test-execution -am -Denv=prod -DAPI_AUTH_CLIENTSECRET=xxxx
```

## Example suites against free public sandbox APIs

`test-execution`'s `examples/` package contains three full steps -> scenario
-> test suites, each against a different public API - see the module's own
README section (below) for details on `restful-api.dev`, `reqres.in`
(needs a free `REQRES_API_KEY` - see `.env.example`), and
`jsonplaceholder.typicode.com`.

```bash
mvn test -pl test-execution -am -Dtest=RestfulApiDevTest
mvn test -pl test-execution -am -Dtest=JsonPlaceholderTest
REQRES_API_KEY=your-key mvn test -pl test-execution -am -Dtest=ReqresApiTest
mvn test -pl test-execution -am -Dgroups=public-api
```

## Reports & logging

```bash
mvn allure:report -pl test-execution -am
mvn allure:serve -pl test-execution -am
```

`test-execution/src/test/resources/logback-test.xml` simply `<include>`s
`framework-reporting`'s shared `logback-base.xml`, so console/file/HTTP-traffic
appenders stay consistent across any test-execution-style module you add
later, while each module can still layer on its own loggers.

## Adding a new framework-* module

1. Add it to the parent `pom.xml`'s `<modules>` list and `dependencyManagement`.
2. Give it a narrow, explicit dependency list - only depend on the
   `framework-*` modules it genuinely needs.
3. If it has a stateful service to expose, give the class a public
   `@Inject`-annotated constructor and add a `bind(...).asEagerSingleton()`
   (or plain `bind(...)` if it doesn't need to be a singleton) to
   `framework-di`'s `PlatformModule` - that's the only file that needs to
   change to make the new service injectable everywhere.

## What's deliberately NOT included (add if you need it)

- **Cucumber/BDD layer** - the Steps/Scenarios split already gives reusable
  building blocks; add `cucumber-junit-platform-engine` (and a Guice-Cucumber
  bridge - `io.cucumber:cucumber-guice`) if you want Gherkin feature files.
- **Contract testing** (Pact) - separate concern, bolt on as its own module.
- **A `framework-bom` module** - the parent POM's `dependencyManagement`
  currently plays that role directly; split it out if you want other,
  unrelated projects to import just the version alignment without the
  parent/modules structure.
- **Retry-on-flaky-network logic** - `api.retry.*` config values are wired
  into the yml files but not yet enforced.
- **CI pipeline file** - add a GitHub Actions/GitLab CI yml invoking
  `mvn test -pl test-execution -am -Denv=qa` + `mvn allure:report` + publish
  the report as an artifact.
- **Secrets manager integration** - prod credentials currently come from
  plain environment variables; swap in Vault/AWS Secrets Manager/etc. inside
  `ConfigLoader.lookup()` (`framework-config`) if required.
