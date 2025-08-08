package domain;

public class RequestCheckWsprProcessResult {
  private boolean success;
  private String message;
  private String callsign;

  public RequestCheckWsprProcessResult() {}

  public RequestCheckWsprProcessResult(boolean success, String message, String callsign) {
    this.success = success;
    this.message = message;
    this.callsign = callsign;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getCallsign() {
    return callsign;
  }

  public void setCallsign(String callsign) {
    this.callsign = callsign;
  }

}

