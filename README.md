# ghcp-mgmt

GitHub REST API management CLI.

## Prerequisites

- Java 25+
- Maven 3.6+
- API docs https://docs.github.com/en/enterprise-cloud@latest/rest/quickstart?apiVersion=2026-03-10

## Running

### From source (no packaging needed)

Run directly against compiled classes — fastest for development:

```bash
mvn exec:java ""-Dexec.args=user me"
```

After making changes, compile first (only touches changed files):

```bash
mvn compile exec:java ""-Dexec.args=user me"
```

### Packaged JAR (fat jar with all dependencies)

Build and run the shaded JAR:

```bash
mvn package
java -jar target/ghcp-mgmt-1.0.0.jar user me
```

### Installed scripts

A packaged JAR is generated in `target/` and can be run with the wrapper scripts:

```bash
./ghcp-mgmt.sh user me   # Linux/macOS
ghcp-mgmt.bat user me    # Windows
```

## Configuration

Place a `.ghcp-mgmt.properties` file in the project directory:

| Property             | Required | Default                  | Description                                   |
|----------------------|----------|--------------------------|-----------------------------------------------|
| `github.token`       | yes      | —                        | GitHub personal access token                  |
| `github.base-url`    | no       | `https://api.github.com` | API base URL (use for GitHub Enterprise)      |
| `github.enterprise`  | no       | —                        | Enterprise slug (required for billing commands) |
| `github.org`         | no       | —                        | Organisation name (for org-scoped commands)   |

## Usage

```
ghcp-mgmt [COMMAND]
```

### `user` — User commands

```bash
# Show authenticated user
ghcp-mgmt user me

# Look up any GitHub user by username
ghcp-mgmt user show octocat
```

### `billing` — Billing commands

#### `billing copilot-usage` — Download Copilot AI credit usage report

Downloads the current month's Copilot AI credit usage report as CSV into the `reports/` directory.
Requires `github.enterprise` to be set in `.ghcp-mgmt.properties`.

```bash
# Download report for the current month (skips if a report for today's date range already exists)
ghcp-mgmt billing copilot-usage

# Force re-download, overwriting any existing report
ghcp-mgmt billing copilot-usage --overwrite
```

#### `billing report` — Analyse a downloaded usage report

Reads from the latest CSV in `reports/`. Requires a prior `billing copilot-usage` download.

```bash
# Show all users ordered by usage (highest to lowest)
ghcp-mgmt billing report all

# Show users within 10% of their monthly quota (default threshold)
ghcp-mgmt billing report approaching

# Show users within 25% of their monthly quota
ghcp-mgmt billing report approaching --within 25

# Show light users below 10% monthly quota usage (default threshold)
ghcp-mgmt billing report light

# Show light users below 5% monthly quota usage
ghcp-mgmt billing report light --below 5
```
