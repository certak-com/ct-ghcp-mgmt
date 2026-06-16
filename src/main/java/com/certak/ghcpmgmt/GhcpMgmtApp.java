package com.certak.ghcpmgmt;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Root command for the GitHub management CLI.
 *
 * Usage:
 *   ghcp-mgmt                    Show this help
 *   ghcp-mgmt user               Show user subcommands
 *   ghcp-mgmt user me            Get authenticated user
 */
@Command(name = "ghcp-mgmt",
        version = "ghcp-mgmt 1.0.0",
        description = "GitHub REST API management CLI",
        mixinStandardHelpOptions = true,
        subcommands = {UserCommand.class})
public class GhcpMgmtApp implements Runnable {

    @Spec
    private CommandSpec spec;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GhcpMgmtApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }
}
