package domain;

public class BalloonSortData {
  private String launchTime;
  private String line;
  private String ssid;

  public BalloonSortData(String launchTime, String line, String ssid) {
    this.launchTime = launchTime;
    this.line = line;
    this.ssid = ssid;
  }

  public String getLaunchTime() {
    return launchTime;
  }

  public void setLaunchTime(String launchTime) {
    this.launchTime = launchTime;
  }

  public String getLine() {
    return line;
  }

  public void setLine(String line) {
    this.line = line;
  }

  public String getSsid() {
    return ssid;
  }

  public void setSsid(String ssid) {
    this.ssid = ssid;
  }
}
