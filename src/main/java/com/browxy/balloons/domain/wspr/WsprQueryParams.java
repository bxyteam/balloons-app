package com.browxy.balloons.domain.wspr;

public class WsprQueryParams {
    private String band;
    private String count;
    private String call;
    private String reporter;
    private String timeLimit;
    private String sortBy;
    private String sortRev;
    private String unique;
    private String mode;
    private String excludeSpecial;

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getCall() {
        return call;
    }

    public void setCall(String call) {
        this.call = call;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortRev() {
        return sortRev;
    }

    public void setSortRev(String sortRev) {
        this.sortRev = sortRev;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getExcludeSpecial() {
        return excludeSpecial;
    }

    public void setExcludeSpecial(String excludeSpecial) {
        this.excludeSpecial = excludeSpecial;
    }

}
