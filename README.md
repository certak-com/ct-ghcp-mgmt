# ghcp-mgmt

CLI for managing GitHub Copilot user usage and budgets across a GitHub Enterprise organisation. Wraps the GitHub REST API to download usage reports, identify heavy/light users, and create or update per-user spending budgets.

## Prerequisites

- Java 25+
- Maven 3.6+
- API docs: https://docs.github.com/en/enterprise-cloud@latest/rest/quickstart?apiVersion=2026-03-10

## Building & Running

### From source (development)

Run directly against compiled classes — no packaging step needed:

```bash
mvn exec:java "-Dexec.args=user me"
```

Recompile and run in one step:

```bash
mvn compile exec:java "-Dexec.args=user me"
```

### Packaged native app (bundled JRE)

`mvn package` produces a self-contained native application under `target/dist/ghcp-mgmt/` using `jpackage`. This bundles a trimmed JRE — no Java installation required on the target machine.

```bash
mvn package
```

Run the wrapper scripts from the `target/dist/ghcp-mgmt/` directory (or anywhere after adding it to `PATH`):

```bash
./ghcp-mgmt user me   # Linux/macOS
ghcp-mgmt user me     # Windows
```

## Configuration

Place a `.ghcp-mgmt.properties` file in the project directory:

| Property             | Required | Default                  | Description                                   |
|----------------------|----------|--------------------------|-----------------------------------------------|
| `github.token`       | yes      | —                        | GitHub personal access token                  |
| `github.base-url`    | no       | `https://api.github.com` | API base URL (use for GitHub Enterprise)      |
| `github.enterprise`  | no       | —                        | Enterprise slug (required for billing commands) |
| `github.org`         | no       | —                        | Organisation name (for org-scoped commands)   |

## Custom Certificates

Place any `.cer` or `.crt` files (PEM or DER encoded X.509) in the `certs/` folder alongside `.ghcp-mgmt.properties`. They are loaded at startup and added to the JVM trust store for all HTTPS connections — useful in environments with a corporate TLS inspection proxy.

The folder is pre-created by `mvn package` inside the dist bundle. Simply drop certificates there before distributing or running.

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

Reads from a CSV in `reports/`. Requires a prior `billing copilot-usage` download.
Use `--report <file>` on any report subcommand to specify the CSV directly and skip interactive selection.

```bash
# Show all users ordered by usage (highest to lowest)
ghcp-mgmt billing report all

# Skip interactive report selection by specifying the file directly
ghcp-mgmt billing report all --report reports/usage-2026-06-01_2026-06-30.csv

# Show users within 10% of their monthly quota (default threshold)
ghcp-mgmt billing report approaching

# Show users within 25% of their monthly quota
ghcp-mgmt billing report approaching --within 25

# Run fully non-interactively: create/update budgets at $50 for all approaching users
ghcp-mgmt billing report approaching --report reports/usage.csv --budget-action 2 --budget-amount 50 --yes

# Show light users below 10% monthly quota usage (default threshold)
ghcp-mgmt billing report light

# Show light users below 5% monthly quota usage
ghcp-mgmt billing report light --below 5
```

`report approaching` options:

| Option | Description |
|---|---|
| `--report <file>` | Path to CSV report (skips interactive selection) |
| `--budget-action <1\|2>` | `1` = create budgets for users without one; `2` = create or update all |
| `--budget-amount <dollars>` | Budget amount in dollars (skips interactive prompt) |
| `--yes` / `-y` | Auto-confirm when new budget is less than existing budget |

### `budget` — Budget management commands

Requires `github.enterprise` to be set in `.ghcp-mgmt.properties`.

#### `budget user-list` — List and optionally delete user-scoped budgets

Lists all user-scoped budgets for the enterprise sorted highest to lowest. After displaying the table, prompts interactively whether to delete all listed budgets. Use `--yes` to skip the confirmation prompt.

```bash
# List all user-scoped budgets and prompt to delete
ghcp-mgmt budget user-list

# List and delete all user budgets without confirmation
ghcp-mgmt budget user-list --yes
```
