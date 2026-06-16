# AGENTS.md — ghcp-mgmt Project Rules

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them — don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- Prioritize information density. Be brief, direct, and task-focused.
- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- If you write 200 lines and it could be 50, rewrite it.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it — don't delete it.

## 4. Goal-Driven Execution

Define success criteria. Loop until verified.

---

## 5. Model Class Documentation (MANDATORY)

**Every model class MUST include a doc URL comment linking back to the GitHub API docs.**

```java
/**
 * GitHub User model.
 *
 * API Reference: https://docs.github.com/en/rest/users/users?apiVersion=2026-03-10#get-the-authenticated-user
 *
 * @see <a href="https://docs.github.com/en/rest/users/users?apiVersion=2026-03-10#get-the-authenticated-user">GET /user - GitHub REST API Docs</a>
 */
public class User { ... }
```

**Rules:**
- Always include the full doc URL as a class-level Javadoc comment.
- Use `@see` tag with the anchor link for easy navigation.
- If the API schema has `oneOf` / union types, note it in the comment and create a unified class with nullable wrapper types.
- When generating model classes from schemas, include all fields from all variants of `oneOf`.
- Use wrapper types (`String`, `Integer`, `Long`, `Boolean`) for ALL fields — never primitives — for null safety.

## 6. picocli CLI Menu Structure

**Always take a good stab at incorporating new endpoints into the CLI menu.**

- Use nested `@Command` hierarchy: group related endpoints under logical subcommands.
- Example: `ghcp-mgmt user me` for `GET /user`, `ghcp-mgmt repo list` for `GET /repos`.
- Each command should have a `description` matching the API endpoint description.
- When adding a new endpoint, create the model class, the picocli command, and wire it into the hierarchy.

## 7. Config File Conventions

- Config file: `.ghcp-mgmt.properties` in the project directory.
- Token is required; base URL defaults to `https://api.github.com`.
- Enterprise and org names are optional (defaults to empty string).
- `.ghcp-mgmt.properties` is `.gitignore`d — never commit tokens.

## 8. Error Handling

- On API errors: print HTTP status + response body to stderr, exit non-zero.
- On 429 (rate limit) and 5xx (server errors): retry with exponential backoff (max 3 retries).
- On 4xx (except 429): fail immediately, no retry.

## 9. No Unit Tests

Do NOT create unit tests or integration tests.

## 10. Output Format

- Never output raw JSON.
- Format responses as clean, readable key-value pairs, or tables, or some human-friendly format.
- Skip null fields.
- Indent nested objects.
