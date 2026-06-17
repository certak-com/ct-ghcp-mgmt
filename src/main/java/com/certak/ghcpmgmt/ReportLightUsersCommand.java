package com.certak.ghcpmgmt;

import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Show light Copilot users below a specified usage percentage.
 */
@CommandLine.Command(
        name = "light",
        description = "Show light users below a specified usage percentage",
        mixinStandardHelpOptions = true)
public class ReportLightUsersCommand implements Callable<Integer> {

    @CommandLine.Option(
            names = {"--below"},
            description = "Show users below this usage percentage (default: ${DEFAULT-VALUE}%)",
            defaultValue = "10")
    private double belowPercent;

    @Override
    public Integer call() {
        try {
            Path report = CopilotReportUtils.selectReport();
            if (report == null) return 1;

            List<UserUsage> users = CopilotReportUtils.parseReport(report);
            List<UserUsage> light = users.stream()
                    .filter(u -> u.usagePercent() < belowPercent)
                    .toList();

            Map<String, String> names = CopilotReportUtils.resolveNames(light);
            System.out.println();
            System.out.printf("Light users below %.0f%% monthly quota usage:%n", belowPercent);
            System.out.println();
            CopilotReportUtils.printTable(light, names);
            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
}
