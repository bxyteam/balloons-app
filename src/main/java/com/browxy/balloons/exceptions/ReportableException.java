package com.browxy.balloons.exceptions;

public class ReportableException extends RuntimeException {

    private boolean isReportable = true;
    private static final long serialVersionUID = 1L;
    
    
    public ReportableException(String message) {
        super(message);
    }

    public ReportableException() {
        super();
    }

    public ReportableException(Throwable cause) {
        super(cause);
    }

    public ReportableException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public boolean isReportable() {
        return isReportable;
    }

    public void setReportable(boolean isReportable) {
        this.isReportable = isReportable;
    }

}
