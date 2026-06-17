package com.certak.ghcpmgmt;

import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Show all users from a Copilot usage report, ordered by usage (highest to lowest).
 */
@CommandLine.Command(
        name = "all",
        description = "Show all users from a usage report, ordered by usage (highest to lowest)",
        mixinStandardHelpOptions = true)
public class ReportAllCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        try {
            Path report = CopilotReportUtils.selectReport();
            if (report == null) return 1;

            List<UserUsage> users = CopilotReportUtils.parseReport(report);
            System.out.println();
            CopilotReportUtils.printTable(users);
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
