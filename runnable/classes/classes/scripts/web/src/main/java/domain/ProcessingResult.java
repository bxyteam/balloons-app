package domain;

import java.util.List;
import com.google.gson.GsonBuilder;

public class ProcessingResult {
  private final String jsonArray;
  private final int validCount;
  private final String newCalls;
  private final List<LicenseData> licenseData;
  private final List<Balloon> balloons;

  public ProcessingResult(String jsonArray, List<Balloon> balloons, String newCalls,
      List<LicenseData> licenseData) {
    this.jsonArray = jsonArray;
    this.balloons = balloons;
    this.validCount = balloons.size();
    this.newCalls = newCalls;
    this.licenseData = licenseData;
  }

  public String getJsonArray() {
    return jsonArray;
  }

  public int getValidCount() {
    return validCount;
  }

  public String getNewCalls() {
    return newCalls;
  }

  public List<LicenseData> getLicenseData() {
    return licenseData;
  }

  public List<Balloon> getBalloons() {
    return balloons;
  }

  public String toJson() {
    return new GsonBuilder().disableHtmlEscaping().serializeNulls().create().toJson(this);
  }
}
