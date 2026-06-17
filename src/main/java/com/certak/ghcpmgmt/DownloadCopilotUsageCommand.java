package com.certak.ghcpmgmt;

import com.certak.ghcpmgmt.api.GitHubApiException;
import com.certak.ghcpmgmt.api.GitHubClient;
import com.certak.ghcpmgmt.config.AppConfig;
import com.certak.ghcpmgmt.model.UsageReportExport;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Download Copilot AI credit usage report for the current month.
 *
 * API Reference: https://docs.github.com/en/enterprise-cloud@latest/rest/billing/usage-reports?apiVersion=2026-03-10#create-a-usage-report-export
 *
 * @see <a href="https://docs.github.com/en/enterprise-cloud@latest/rest/billing/usage-reports?apiVersion=2026-03-10#create-a-usage-report-export">POST /enterprises/{enterprise}/settings/billing/reports - GitHub REST API Docs</a>
 */
@CommandLine.Command(
        name = "copilot-usage",
        description = "Download Copilot AI credit usage report for the current month",
        mixinStandardHelpOptions = true)
public class DownloadCopilotUsageCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite existing report if one already exists for this date range")
    private boolean overwrite;

    private static final String REPORT_TYPE = "ai_credit";
    private static final long POLL_INTERVAL_MS = 10_000;
    private static final long POLL_TIMEOUT_MS = 10 * 60 * 1000L;
    private static final Path REPORTS_DIR = Path.of("reports");

    @Override
    public Integer call() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        String startDate = startOfMonth.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String filePrefix = REPORT_TYPE + "-" + startDate + "-to-" + endDate;

        if (!overwrite && localReportExists(filePrefix)) {
            System.out.println("Report already exists for " + startDate + " to " + endDate + " in " + REPORTS_DIR + "/");
            System.out.println("Use --overwrite to download again.");
            return 0;
        }

        try {
            AppConfig config = AppConfig.load();
            String enterprise = config.getEnterprise();
            if (enterprise == null || enterprise.isBlank()) {
                System.err.println("Error: github.enterprise is not set in .ghcp-mgmt.properties");
                return 1;
            }

            GitHubClient client = new GitHubClient(config);
            String reportsPath = "/enterprises/" + enterprise + "/settings/billing/reports";

            String body = String.format(
                    "{\"report_type\":\"%s\",\"start_date\":\"%s\",\"end_date\":\"%s\",\"send_email\":false}",
                    REPORT_TYPE, startDate, endDate);

            System.out.println("Requesting report for " + startDate + " to " + endDate + "...");
            UsageReportExport report = client.post(reportsPath, body, UsageReportExport.class);
            System.out.println("Report ID: " + report.getId() + " | Status: " + report.getStatus());

            if (report.getDownloadUrls() == null || report.getDownloadUrls().isEmpty()) {
                report = pollUntilCompleted(client, reportsPath + "/" + report.getId());
            }

            if (!"completed".equals(report.getStatus())) {
                System.err.println("Error: Report generation failed with status: " + report.getStatus());
                return 1;
            }

            Files.createDirectories(REPORTS_DIR);
            List<String> urls = report.getDownloadUrls();
            for (int i = 0; i < urls.size(); i++) {
                String suffix = (i == 0) ? "" : "_" + (i + 1);
                Path dest = REPORTS_DIR.resolve(filePrefix + suffix + ".csv");
                System.out.println("Downloading to " + dest + "...");
                client.downloadToFile(urls.get(i), dest);
                System.out.println("  Saved: " + dest.toAbsolutePath());
            }

            System.out.println("Done. " + urls.size() + " file(s) saved to " + REPORTS_DIR.toAbsolutePath());
            return 0;

        } catch (GitHubApiException e) {
            ApiUtils.printApiError(e);
            return 1;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    private boolean localReportExists(String filePrefix) {
        if (!Files.exists(REPORTS_DIR)) return false;
        try {
            return Files.list(REPORTS_DIR)
                    .anyMatch(p -> p.getFileName().toString().startsWith(filePrefix));
        } catch (IOException e) {
            return false;
        }
    }

    private UsageReportExport pollUntilCompleted(GitHubClient client, String pollPath) throws GitHubApiException {
        long deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new GitHubApiException("Polling interrupted", ie);
            }
            System.out.println("  Polling status...");
            UsageReportExport report = client.get(pollPath, UsageReportExport.class);
            System.out.println("  Status: " + report.getStatus());
            if ("completed".equals(report.getStatus()) || "failed".equals(report.getStatus())) {
                return report;
            }
        }
        throw new GitHubApiException("Timed out waiting for report to complete after 10 minutes");
    }
}
