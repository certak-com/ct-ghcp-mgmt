# ghcp-mgmt

GitHub REST API management CLI.

## Prerequisites

- Java 25+
- Maven 3.6+

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

| Property    | Required | Default              | Description              |
|-------------|----------|----------------------|--------------------------|
| `token`     | yes      | —                    | GitHub personal access token |
| `baseUrl`   | no       | `https://api.github.com` | API base URL (use for GitHub Enterprise) |
| `org`       | no       | —                    | Organization name (for org-scoped commands) |

## Usage

```
ghcp-mgmt                    Show this help
ghcp-mgmt user               Show user subcommands
ghcp-mgmt user me            Get authenticated user
```
