package net.khalegh.qodqod.contorl;

public enum ReportType {
    SPAM("Spam", 1), CRIMINAL("Criminal", 2), SEXUAL("Sexual", 3),SOCIAL("Social", 4),OTHER("Other", 5);
    private final int reportCode;

    ReportType(String key, int value) {
        this.reportCode = value;
    }


    public int getReportCode() {
        return reportCode;
    }
}

