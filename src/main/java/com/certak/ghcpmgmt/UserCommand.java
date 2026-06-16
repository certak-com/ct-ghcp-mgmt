package com.certak.ghcpmgmt;

import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

/**
 * Group for user-related commands.
 */
@CommandLine.Command(name = "user",
        description = "User-related commands",
        mixinStandardHelpOptions = true,
        subcommands = {MeCommand.class, ShowUserCommand.class})
public class UserCommand implements Runnable {

    @Spec
    private CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }
}
