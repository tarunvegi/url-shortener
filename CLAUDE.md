# Project Conventions

Read this before generating or editing code in this repo. These are standing conventions, not per-prompt instructions. Don't ask about them, just follow them.

## Code style
- No unnecessary abstractions. This is a prototype/assessment codebase, so three similar lines beat a premature interface.
- Use Lombok `@RequiredArgsConstructor` for dependency injection, not manual constructors or field injection.
- No try-catch unless a specific failure mode is being handled. Don't wrap calls "just in case."
- `@Value`-injected fields must be non-final (Spring needs to set them after construction).

## Error handling contract
- Failures are custom exceptions (`UrlNotFoundException`, `UrlExpiredException`, etc.), caught centrally in `GlobalExceptionHandler`, and mapped to typed HTTP status codes. Don't handle HTTP status in controllers directly.
- Kafka publish failures are caught inside the producer and logged at WARN, never propagated to the caller. The redirect path must never fail because analytics failed.
- The AI safety check fails open: no API key configured, or the API call errors or times out, means the URL is allowed through. Never let the safety layer take down the core shortening function.

## Conventions that must stay consistent across files
- Redis key format for cached URLs is `url:{code}`. This is used in `WriteService`, `ReadService`, and the delete path. If you generate any of these separately, use this exact format, don't invent a new one.
- Kafka message key is `short_code`, so all events for a URL land on the same partition.
- API prefix is `/api/v1/...` for everything except the redirect endpoint, which is intentionally root-level (`GET /{code}`) so short links stay short.

## Testing
- Unit tests use Mockito, no Spring context.
- Integration tests use `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")`, backed by H2 (see `application-test.yml`). Kafka/Redis are mocked via `@MockBean` where the test doesn't need to exercise them.
- Integration test classes are named `*IT.java` and run via the `maven-failsafe-plugin` on `mvn verify`, not `mvn test` (Surefire only picks up `*Test.java`). If you add a new integration test class, use the `IT` suffix and confirm it shows up in a `mvn verify` run, not just `mvn test`.

## Before proposing a dependency upgrade or new library
- Don't run `npm audit fix --force` or an equivalent forced major bump without flagging it first. Check whether the vulnerability is exploitable in this app's actual usage (a dev-server-only issue vs. a production runtime issue, for example) before deciding it needs an immediate fix.
- Prefer Spring Boot's auto-configuration over hand-rolled config beans (like `StringRedisTemplate`) unless there's a concrete reason the default doesn't work.
