package com.certak.ghcpmgmt;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Group for Copilot usage report analysis commands.
 */
@CommandLine.Command(
        name = "report",
        description = "Analyse a downloaded Copilot usage report",
        mixinStandardHelpOptions = true,
        subcommands = {ReportAllCommand.class, ReportApproachingCommand.class, ReportLightUsersCommand.class})
public class ReportCommand implements Runnable {

    @Spec
    private CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }
}
