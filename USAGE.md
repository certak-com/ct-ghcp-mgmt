# ghcp-mgmt — Usage Guide

CLI for managing GitHub Copilot user usage and budgets across a GitHub Enterprise organisation.

---

## Getting Started

Extract the archive and run `ghcp-mgmt` from inside the extracted `ghcp-mgmt/` folder:

```
ghcp-mgmt.exe [COMMAND]        # Windows
./ghcp-mgmt [COMMAND]          # Linux / macOS
```

Add the folder to your `PATH` to run it from anywhere.

---

## Configuration

Before using `ghcp-mgmt`, create a `.ghcp-mgmt.properties` file in the same folder as the executable:

```properties
github.token=ghp_yourPersonalAccessTokenHere
github.base-url=https://api.github.com
github.enterprise=your-enterprise-slug
github.org=your-org-name
```

| Property            | Required | Default                    | Description                                          |
|---------------------|----------|----------------------------|------------------------------------------------------|
| `github.token`      | **yes**  | —                          | GitHub personal access token (needs Copilot billing scopes) |
| `github.base-url`   | no       | `https://api.github.com`   | Override for GitHub Enterprise Server instances      |
| `github.enterprise` | no       | —                          | Enterprise slug — required for `billing` and `budget` commands |
| `github.org`        | no       | —                          | Organisation name — required for org-scoped commands |

---

## Custom Certificates

If your network uses a corporate TLS inspection proxy, drop any `.cer` or `.crt` certificate files into the `certs/` folder alongside the executable. They are loaded automatically at startup.

---

## Commands

### `user` — User info

```bash
# Show the authenticated user (confirms your token is working)
ghcp-mgmt user me

# Look up any GitHub user by username
ghcp-mgmt user show octocat
```

---

### `billing` — Copilot usage reports

#### `billing copilot-usage` — Download usage report

Downloads the current month's Copilot AI credit usage as a CSV into the `reports/` folder.
Requires `github.enterprise` in `.ghcp-mgmt.properties`.

```bash
# Download report (skips if today's report already exists)
ghcp-mgmt billing copilot-usage

# Force re-download, overwriting any existing report
ghcp-mgmt billing copilot-usage --overwrite
```

#### `billing report` — Analyse a report

Reads a CSV from `reports/`. Run `billing copilot-usage` first.

```bash
# Show all users ordered by usage (highest first)
ghcp-mgmt billing report all

# Specify the CSV directly to skip interactive selection
ghcp-mgmt billing report all --report reports/usage-2026-06-01_2026-06-30.csv

# Show users approaching their quota (within 10% — default)
ghcp-mgmt billing report approaching

# Show users approaching within 25%
ghcp-mgmt billing report approaching --within 25

# Create/update budgets at $50 for approaching users — fully non-interactive
ghcp-mgmt billing report approaching --report reports/usage.csv --budget-action 2 --budget-amount 50 --yes

# Show light users below 10% monthly quota usage (default)
ghcp-mgmt billing report light

# Show light users below 5%
ghcp-mgmt billing report light --below 5
```

`report approaching` options:

| Option               | Description                                                       |
|----------------------|-------------------------------------------------------------------|
| `--report <file>`    | Path to CSV report (skips interactive selection)                  |
| `--budget-action <1\|2>` | `1` = create budgets for users without one; `2` = create or update all |
| `--budget-amount <$>` | Budget in dollars (skips interactive prompt)                     |
| `--yes` / `-y`       | Auto-confirm when new budget is less than existing budget         |

---

### `budget` — Budget management

Requires `github.enterprise` in `.ghcp-mgmt.properties`.

#### `budget user-list` — List (and optionally delete) user budgets

```bash
# List all user-scoped budgets and prompt to delete
ghcp-mgmt budget user-list

# List and delete all user budgets without confirmation
ghcp-mgmt budget user-list --yes
```
