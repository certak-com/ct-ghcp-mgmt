package com.certak.ghcpmgmt;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Group for billing-related commands.
 */
@CommandLine.Command(
        name = "billing",
        description = "Billing-related commands",
        mixinStandardHelpOptions = true,
        subcommands = {DownloadCopilotUsageCommand.class})
public class BillingCommand implements Runnable {

    @Spec
    private CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }
}
