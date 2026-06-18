package com.certak.ghcpmgmt;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Group for budget-related commands.
 */
@CommandLine.Command(
        name = "budget",
        description = "Budget management commands",
        mixinStandardHelpOptions = true,
        subcommands = {BudgetUserListCommand.class})
public class BudgetCommand implements Runnable {

    @Spec
    private CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }
}
