package com.certak.ghcpmgmt.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * GitHub Billing Usage Report Export model.
 *
 * API Reference: https://docs.github.com/en/enterprise-cloud@latest/rest/billing/usage-reports?apiVersion=2026-03-10#create-a-usage-report-export
 *
 * @see <a href="https://docs.github.com/en/enterprise-cloud@latest/rest/billing/usage-reports?apiVersion=2026-03-10#create-a-usage-report-export">POST /enterprises/{enterprise}/settings/billing/reports - GitHub REST API Docs</a>
 */
public class UsageReportExport {

    private String id;

    @JsonProperty("report_type")
    private String reportType;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    private String status;

    @JsonProperty("download_urls")
    private List<String> downloadUrls;

    @JsonProperty("created_at")
    private String createdAt;

    private String actor;

    public String getId() { return id; }
    public String getReportType() { return reportType; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public List<String> getDownloadUrls() { return downloadUrls; }
    public String getCreatedAt() { return createdAt; }
    public String getActor() { return actor; }
}
