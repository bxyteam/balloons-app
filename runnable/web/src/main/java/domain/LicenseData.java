package domain;

public class LicenseData {
  private String callSign;
  private Integer hoursDiff;
  private String frequency;

  public LicenseData(String callSign, Integer hoursDiff, String frequency) {
    this.callSign = callSign;
    this.hoursDiff = hoursDiff;
    this.frequency = frequency;
  }

  // Getters and setters
  public String getCallSign() {
    return callSign;
  }

  public void setCallSign(String callSign) {
    this.callSign = callSign;
  }

  public Integer getHoursDiff() {
    return hoursDiff;
  }

  public void setHoursDiff(Integer hoursDiff) {
    this.hoursDiff = hoursDiff;
  }

  public String getFrequency() {
    return frequency;
  }

  public void setFrequency(String frequency) {
    this.frequency = frequency;
  }

  @Override
  public String toString() {
    return "LicenseData [callSign=" + callSign + ", hoursDiff=" + hoursDiff + ", frequency="
        + frequency + "]";
  }
}
