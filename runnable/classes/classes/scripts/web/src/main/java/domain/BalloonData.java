package domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BalloonData {
  private final String url;
  private final Map<String, String> parameters;
  private final boolean isRepeater;
  private final String comments;

  public BalloonData(String url, Map<String, String> parameters, boolean isRepeater,
      String comments) {
    this.url = url;
    this.parameters = new ConcurrentHashMap<>(parameters);
    this.isRepeater = isRepeater;
    this.comments = comments;
  }

  public String getUrl() {
    return url;
  }

  public boolean isRepeater() {
    return isRepeater;
  }

  public String getComments() {
    return comments;
  }

  public String getParameter(String key) {
    return parameters.getOrDefault(key, "");
  }

  @Override
  public String toString() {
    return "BalloonData [url=" + url + ", parameters=" + parameters + ", isRepeater=" + isRepeater
        + ", comments=" + comments + "]";
  }


}
