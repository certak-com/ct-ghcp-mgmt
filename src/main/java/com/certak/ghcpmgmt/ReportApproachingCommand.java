package com.certak.ghcpmgmt;

import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Show Copilot users approaching their monthly quota limit.
 */
@CommandLine.Command(
        name = "approaching",
        description = "Show users approaching their monthly quota (within a specified percentage of the limit)",
        mixinStandardHelpOptions = true)
public class ReportApproachingCommand implements Callable<Integer> {

    @CommandLine.Option(
            names = {"--within"},
            description = "Flag users within this percentage of their limit (default: ${DEFAULT-VALUE}%)",
            defaultValue = "10")
    private double withinPercent;

    @Override
    public Integer call() {
        try {
            Path report = CopilotReportUtils.selectReport();
            if (report == null) return 1;

            List<UserUsage> users = CopilotReportUtils.parseReport(report);
            double threshold = 100.0 - withinPercent;
            List<UserUsage> approaching = users.stream()
                    .filter(u -> u.usagePercent() >= threshold)
                    .toList();

            Map<String, String> names = CopilotReportUtils.resolveNames(approaching);
            System.out.println();
            System.out.printf("Users within %.0f%% of their monthly quota (usage >= %.0f%%):%n",
                    withinPercent, threshold);
            System.out.println();
            CopilotReportUtils.printTable(approaching, names);
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
